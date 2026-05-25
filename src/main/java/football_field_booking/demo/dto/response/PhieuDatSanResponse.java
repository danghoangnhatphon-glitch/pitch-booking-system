package football_field_booking.demo.dto.response;

import football_field_booking.demo.entity.PhieuDatSan;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Thông tin phiếu đặt sân trả về cho client
 * Dùng ở: trang xác nhận đặt sân, lịch sử đặt sân
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuDatSanResponse {

    private Long id;
    private LocalDate ngayTao;
    private BigDecimal tongTien;
    private PhieuDatSan.TrangThai trangThai;
    private PhieuDatSan.TrangThaiThanhToan trangThaiThanhToan;
    private PhieuDatSan.PhuongThucThanhToan phuongThucThanhToan;
    private String ghiChu;
    private LocalDateTime createdAt;

    // Thông tin người đặt
    private String tenNguoiDat;
    private String sdtNguoiDat;

    // Danh sách ca đặt (1 phiếu có thể đặt nhiều ca)
    private List<ChiTietResponse> danhSachChiTiet;

    // ================================================================
    // Nested class — thông tin 1 ca đặt
    // Đặt nested trong PhieuDatSanResponse vì chỉ dùng ở đây
    // ================================================================
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChiTietResponse {
        private String tenSan;
        private String loaiSan;
        private LocalDate ngaySuDung;
        private LocalTime gioBatDau;
        private LocalTime gioKetThuc;
        private BigDecimal donGia;
    }

    // ================================================================
    // Static factory method — chuyển Entity → DTO
    // ================================================================
    public static PhieuDatSanResponse from(PhieuDatSan entity) {
        List<ChiTietResponse> chiTietList = entity.getDanhSachChiTiet()
                .stream()
                .map(ct -> ChiTietResponse.builder()
                        .tenSan(ct.getSanBong().getTenSan())
                        .loaiSan(ct.getSanBong().getLoaiSan().name())
                        .ngaySuDung(ct.getNgaySuDung())
                        .gioBatDau(ct.getKhungGio().getGioBatDau())
                        .gioKetThuc(ct.getKhungGio().getGioKetThuc())
                        .donGia(ct.getDonGia())
                        .build())
                .toList();

        return PhieuDatSanResponse.builder()
                .id(entity.getId())
                .ngayTao(entity.getNgayTao())
                .tongTien(entity.getTongTien())
                .trangThai(entity.getTrangThai())
                .trangThaiThanhToan(entity.getTrangThaiThanhToan())
                .phuongThucThanhToan(entity.getPhuongThucThanhToan())
                .ghiChu(entity.getGhiChu())
                .createdAt(entity.getCreatedAt())
                .tenNguoiDat(entity.getNguoiDat().getHoTen())
                .sdtNguoiDat(entity.getNguoiDat().getSoDienThoai())
                .danhSachChiTiet(chiTietList)
                .build();
    }
}
