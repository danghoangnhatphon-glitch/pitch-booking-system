// ================================================================
//  dat-san.js — Logic trang chi tiết sân (chọn giờ + đặt)
// ================================================================

let selectedSlots = [];

// ── Khởi tạo khi trang load ──────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  const today = new Date().toISOString().split('T')[0];
  document.getElementById('summary-ngay').textContent = formatNgay(today);

  // Event delegation — 1 listener trên grid, bắt click của tất cả slot
  // Hoạt động cho cả slot Thymeleaf render lẫn slot JS render lại sau đổi ngày
  document.getElementById('slot-grid').addEventListener('click', (e) => {
    const slot = e.target.closest('.slot-item');
    if (!slot) return;
    // Chỉ cho click nếu còn trống hoặc đang selected
    if (slot.classList.contains('booked')) return;
    chonGio(slot);
  });
});

// ── Tải lại lịch sân khi đổi ngày ───────────────────────────
async function taiLichSan(ngay) {
  const grid    = document.getElementById('slot-grid');
  const loading = document.getElementById('slot-loading');

  loading.style.display = 'block';
  grid.style.display    = 'none';

  selectedSlots = [];
  capNhatSummary();

  try {
    const res  = await fetch(`/api/san-bong/${SAN_ID}/lich?ngay=${ngay}`);
    const data = await res.json();

    if (!data.success) {
      showToast(data.message || 'Không tải được lịch sân', 'error');
      return;
    }

    // Render lại grid — không cần gắn listener vì đã dùng delegation
    grid.innerHTML = '';
    data.data.forEach(kg => {
      const el = document.createElement('div');
      el.className       = `slot-item ${kg.conTrong ? 'available' : 'booked'}`;
      el.dataset.id      = kg.khungGioId;
      el.dataset.gia     = kg.donGia;
      el.dataset.label   = kg.label;

      el.innerHTML = `
        <div class="slot-time">${kg.label}</div>
        <div class="slot-price">${formatTien(kg.donGia)}</div>
        ${kg.laGioCaoDiem ? '<div class="slot-peak-badge">CAO ĐIỂM</div>' : ''}
        ${!kg.conTrong ? '<div style="font-size:11px;color:var(--red-400);margin-top:2px;">Đã đặt</div>' : ''}
      `;

      grid.appendChild(el);
    });

    document.getElementById('summary-ngay').textContent = formatNgay(ngay);

  } catch (err) {
    showToast('Lỗi kết nối. Vui lòng thử lại.', 'error');
  } finally {
    loading.style.display = 'none';
    grid.style.display    = 'grid';
  }
}

// ── Chọn / bỏ chọn 1 khung giờ ──────────────────────────────
function chonGio(el) {
  const id    = el.dataset.id;
  const gia   = parseFloat(el.dataset.gia);
  const label = el.dataset.label;

  if (!id || isNaN(gia)) {
    console.error('Slot thiếu data-id hoặc data-gia:', el);
    return;
  }

  const idx = selectedSlots.findIndex(s => s.id === id);

  if (idx === -1) {
    selectedSlots.push({ id, label, gia });
    el.classList.remove('available');
    el.classList.add('selected');
  } else {
    selectedSlots.splice(idx, 1);
    el.classList.remove('selected');
    el.classList.add('available');
  }

  capNhatSummary();
}

// ── Cập nhật hộp tóm tắt bên phải ───────────────────────────
function capNhatSummary() {
  const container = document.getElementById('summary-slots');
  const totalEl   = document.getElementById('summary-total');
  const btnDat    = document.getElementById('btn-dat-san');

  if (selectedSlots.length === 0) {
    container.innerHTML = `
      <div style="font-size:13px;color:var(--slate-400);text-align:center;padding:8px 0;">
        Chưa chọn khung giờ nào
      </div>`;
    totalEl.textContent = '0đ';
    btnDat.disabled = true;
    return;
  }

  container.innerHTML = selectedSlots.map(s => `
    <div class="summary-row">
      <span style="color:var(--white);">⏰ ${s.label}</span>
      <span style="color:var(--amber);font-weight:600;">${formatTien(s.gia)}</span>
    </div>
  `).join('');

  const total = selectedSlots.reduce((sum, s) => sum + s.gia, 0);
  totalEl.textContent = formatTien(total);

  btnDat.disabled = !IS_AUTH;
  if (!IS_AUTH) btnDat.title = 'Vui lòng đăng nhập để đặt sân';
}

// ── Đặt sân ──────────────────────────────────────────────────
async function xacNhanDatSan() {
  if (selectedSlots.length === 0) {
    showToast('Vui lòng chọn ít nhất 1 khung giờ', 'error');
    return;
  }

  const ngay   = document.getElementById('ngayChon').value;
  const ghiChu = document.getElementById('ghiChu').value;

  if (!ngay) {
    showToast('Vui lòng chọn ngày', 'error');
    return;
  }

  const btnDat = document.getElementById('btn-dat-san');
  btnDat.disabled    = true;
  btnDat.textContent = '⏳ Đang xử lý...';

  try {
    const res = await fetch('/api/dat-san', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...authHeader()
      },
      body: JSON.stringify({
        sanId: SAN_ID,
        ngaySuDung: ngay,
        danhSachKhungGioId: selectedSlots.map(s => parseInt(s.id)),
        ghiChu: ghiChu
      })
    });

    const data = await res.json();

    if (data.success) {
      showToast('Đặt sân thành công! Chờ chủ sân duyệt nhé.');
      setTimeout(() => window.location.href = '/dat-san/lich-su', 1500);
    } else {
      showToast(data.message || 'Đặt sân thất bại', 'error');
      btnDat.disabled    = false;
      btnDat.textContent = '⚡ Đặt sân ngay';
      if (res.status === 409) setTimeout(() => taiLichSan(ngay), 500);
    }

  } catch (err) {
    showToast('Lỗi kết nối. Vui lòng thử lại.', 'error');
    btnDat.disabled    = false;
    btnDat.textContent = '⚡ Đặt sân ngay';
  }
}
