package football_field_booking.demo.service;

import football_field_booking.demo.dto.request.DatSanRequest;
import football_field_booking.demo.dto.response.PhieuDatSanResponse;
import football_field_booking.demo.entity.*;
import football_field_booking.demo.exception.AppException;
import football_field_booking.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DatSanService {

    private final PhieuDatSanRepository    phieuDatSanRepository;
    private final ChiTietDatSanRepository  chiTietDatSanRepository;
    private final SanBongRepository        sanBongRepository;
    private final KhungGioRepository       khungGioRepository;
    private final GiaSanRepository         giaSanRepository;
    private final NguoiDungRepository      nguoiDungRepository;

    // ================================================================
    // ĐẶT SÂN — đây là method quan trọng nhất toàn bộ project
    //
    // @Transactional đảm bảo:
    //   - Toàn bộ logic chạy trong 1 transaction
    //   - Nếu bất kỳ bước nào lỗi → rollback hết, không ghi dở dang
    //   - @Lock trong Repository chỉ hoạt động khi có @Transactional
    // ================================================================
    @Transactional
    public PhieuDatSanResponse datSan(DatSanRequest request, Long nguoiDatId) {

        // ── BƯỚC 1: Validate dữ liệu đầu vào ──────────────────────
        SanBong san = sanBongRepository.findById(request.getSanId())
                .orElseThrow(() ->
                    new AppException.ResourceNotFoundException(
                        "Không tìm thấy sân #" + request.getSanId())
                );

        if (san.getTrangThai() != SanBong.TrangThai.HOAT_DONG) {
            throw new AppException.BadRequestException(
                "Sân " + san.getTenSan() + " hiện không hoạt động");
        }

        NguoiDung nguoiDat = nguoiDungRepository.findById(nguoiDatId)
                .orElseThrow(() ->
                    new AppException.ResourceNotFoundException("Không tìm thấy người dùng")
                );

        // ── BƯỚC 2: Kiểm tra từng khung giờ + tính tiền ───────────
        List<ChiTietDatSan> danhSachChiTiet = new ArrayList<>();
        BigDecimal tongTien = BigDecimal.ZERO;

        for (Long khungGioId : request.getDanhSachKhungGioId()) {

            KhungGio khungGio = khungGioRepository.findById(khungGioId)
                    .orElseThrow(() ->
                        new AppException.ResourceNotFoundException(
                            "Không tìm thấy khung giờ #" + khungGioId)
                    );

            // ── CHỐNG TRÙNG LỊCH: Pessimistic Lock ──────────────
            // findWithLock() thực thi: SELECT ... WITH (UPDLOCK, ROWLOCK)
            // → nếu có 2 request cùng lúc, cái thứ 2 PHẢI CHỜ ở đây
            // → đến khi transaction 1 kết thúc mới được tiếp tục
            boolean daDat = chiTietDatSanRepository
                    .findWithLock(san.getId(), khungGioId, request.getNgaySuDung())
                    .isPresent();

            if (daDat) {
                // Ném lỗi → @Transactional tự rollback toàn bộ
                throw new AppException.SanDaDatException(
                    "Sân " + san.getTenSan() + " đã được đặt lúc "
                    + khungGio.getGioBatDau() + " - " + khungGio.getGioKetThuc()
                    + " ngày " + request.getNgaySuDung()
                );
            }

            // Lấy đơn giá tại thời điểm đặt
            GiaSan giaSan = giaSanRepository
                    .findBySanBongIdAndKhungGioId(san.getId(), khungGioId)
                    .orElseThrow(() ->
                        new AppException.BadRequestException(
                            "Sân này chưa có giá cho khung giờ đã chọn")
                    );

            // Tạo chi tiết đặt sân (chưa save, gom vào list trước)
            ChiTietDatSan chiTiet = ChiTietDatSan.builder()
                    .sanBong(san)
                    .khungGio(khungGio)
                    .ngaySuDung(request.getNgaySuDung())
                    .donGia(giaSan.getDonGia())   // lưu giá TẠI THỜI ĐIỂM ĐẶT
                    .build();

            danhSachChiTiet.add(chiTiet);
            tongTien = tongTien.add(giaSan.getDonGia());
        }

        // ── BƯỚC 3: Tạo phiếu đặt sân (header) ───────────────────
        PhieuDatSan phieu = PhieuDatSan.builder()
                .nguoiDat(nguoiDat)
                .tongTien(tongTien)
                .trangThai(PhieuDatSan.TrangThai.CHO_DUYET)
                .trangThaiThanhToan(PhieuDatSan.TrangThaiThanhToan.CHUA_THANH_TOAN)
                .ghiChu(request.getGhiChu())
                .build();

        // Gán phieu vào từng chi tiết (cần để JPA biết FK)
        danhSachChiTiet.forEach(ct -> ct.setPhieuDat(phieu));
        phieu.setDanhSachChiTiet(danhSachChiTiet);

        // ── BƯỚC 4: Lưu vào DB ────────────────────────────────────
        // CascadeType.ALL trên PhieuDatSan → save phieu sẽ tự save chi tiết
        PhieuDatSan saved = phieuDatSanRepository.save(phieu);

        // ── BƯỚC 5: Trả về response ───────────────────────────────
        return PhieuDatSanResponse.from(saved);

        // Khi method return → @Transactional COMMIT → lock được nhả
        // → nếu có request B đang chờ, B sẽ được vào và thấy đã có người đặt
    }

    // ================================================================
    // Lấy lịch sử đặt sân của người dùng
    // ================================================================
    @Transactional(readOnly = true)
    public List<PhieuDatSanResponse> layLichSu(Long nguoiDatId) {
        return phieuDatSanRepository
                .findByNguoiDatIdOrderByCreatedAtDesc(nguoiDatId)
                .stream()
                .map(PhieuDatSanResponse::from)
                .toList();
    }

    // ================================================================
    // Hủy phiếu đặt sân
    // Chỉ hủy được khi đang CHO_DUYET và chưa thanh toán
    // ================================================================
    @Transactional
    public PhieuDatSanResponse huyPhieu(Long phieuId, Long nguoiYeuCauId) {

        PhieuDatSan phieu = phieuDatSanRepository.findById(phieuId)
                .orElseThrow(() ->
                    new AppException.ResourceNotFoundException(
                        "Không tìm thấy phiếu #" + phieuId)
                );

        // Chỉ người đặt mới được hủy
        if (!phieu.getNguoiDat().getId().equals(nguoiYeuCauId)) {
            throw new AppException.ForbiddenException("Bạn không có quyền hủy phiếu này");
        }

        // Chỉ hủy được khi đang chờ duyệt
        if (phieu.getTrangThai() != PhieuDatSan.TrangThai.CHO_DUYET) {
            throw new AppException.BadRequestException(
                "Không thể hủy phiếu đã được duyệt hoặc đã hủy trước đó");
        }

        phieu.setTrangThai(PhieuDatSan.TrangThai.DA_HUY);
        PhieuDatSan saved = phieuDatSanRepository.save(phieu);
        return PhieuDatSanResponse.from(saved);
    }

    // ================================================================
    // Chủ sân DUYỆT phiếu đặt
    // ================================================================
    @Transactional
    public PhieuDatSanResponse duyetPhieu(Long phieuId, Long chuSanId) {

        PhieuDatSan phieu = phieuDatSanRepository.findById(phieuId)
                .orElseThrow(() ->
                    new AppException.ResourceNotFoundException(
                        "Không tìm thấy phiếu #" + phieuId)
                );

        // Kiểm tra phiếu này có thuộc sân của chủ sân này không
        boolean laSanCuaMinh = phieu.getDanhSachChiTiet().stream()
                .anyMatch(ct -> ct.getSanBong().getChuSan().getId().equals(chuSanId));

        if (!laSanCuaMinh) {
            throw new AppException.ForbiddenException("Bạn không có quyền duyệt phiếu này");
        }

        if (phieu.getTrangThai() != PhieuDatSan.TrangThai.CHO_DUYET) {
            throw new AppException.BadRequestException("Phiếu này không ở trạng thái chờ duyệt");
        }

        phieu.setTrangThai(PhieuDatSan.TrangThai.DA_DUYET);
        PhieuDatSan saved = phieuDatSanRepository.save(phieu);
        return PhieuDatSanResponse.from(saved);
    }

    // ================================================================
    // Chủ sân lấy danh sách phiếu CHỜ DUYỆT
    // ================================================================
    @Transactional(readOnly = true)
    public List<PhieuDatSanResponse> layPhieuChoDuyet(Long chuSanId) {
        return phieuDatSanRepository
                .findByChuSanIdAndTrangThai(chuSanId, PhieuDatSan.TrangThai.CHO_DUYET)
                .stream()
                .map(PhieuDatSanResponse::from)
                .toList();
    }

    // ================================================================
    // Xác nhận thanh toán (chủ sân đánh dấu đã nhận tiền)
    // ================================================================
    @Transactional
    public PhieuDatSanResponse xacNhanThanhToan(Long phieuId,
                                                 PhieuDatSan.PhuongThucThanhToan phuongThuc,
                                                 Long chuSanId) {
        PhieuDatSan phieu = phieuDatSanRepository.findById(phieuId)
                .orElseThrow(() ->
                    new AppException.ResourceNotFoundException(
                        "Không tìm thấy phiếu #" + phieuId)
                );

        boolean laSanCuaMinh = phieu.getDanhSachChiTiet().stream()
                .anyMatch(ct -> ct.getSanBong().getChuSan().getId().equals(chuSanId));

        if (!laSanCuaMinh) {
            throw new AppException.ForbiddenException("Bạn không có quyền xác nhận phiếu này");
        }

        phieu.setTrangThaiThanhToan(PhieuDatSan.TrangThaiThanhToan.DA_THANH_TOAN);
        phieu.setPhuongThucThanhToan(phuongThuc);
        PhieuDatSan saved = phieuDatSanRepository.save(phieu);
        return PhieuDatSanResponse.from(saved);
    }
}
