
function showToast(message, type = 'success') {
  const container = document.getElementById('toast-container');
  if (!container) return;
  const toast = document.createElement('div');
  toast.className = `toast ${type === 'error' ? 'error' : ''}`;
  toast.textContent = (type === 'error' ? '❌ ' : '✅ ') + message;
  container.appendChild(toast);
  setTimeout(() => toast.remove(), 3000);
}

function formatTien(amount) {
  return new Intl.NumberFormat('vi-VN').format(amount) + 'đ';
}

function formatNgay(dateStr) {
  if (!dateStr) return '—';
  const [y, m, d] = dateStr.split('-');
  return `${d}/${m}/${y}`;
}

function authHeader() {
  const token = localStorage.getItem('token');
  if (token && token !== 'null' && token.split('.').length === 3) {
    return { 'Authorization': 'Bearer ' + token };
  }
  return {};
}

function luuToken(token) {
  localStorage.setItem('token', token);
}

function layToken() {
  return localStorage.getItem('token');
}
