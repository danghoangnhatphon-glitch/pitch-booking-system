package football_field_booking.demo.dto.response;

import football_field_booking.demo.entity.PhieuDatSan;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuDatSanResponse {

    private Long id;
    private LocalDate ngayTao;
    private BigDecimal tongTien;
    private BigDecimal tienCoc;
    private PhieuDatSan.TrangThai trangThai;
    private PhieuDatSan.TrangThaiThanhToan trangThaiThanhToan;
    private PhieuDatSan.PhuongThucThanhToan phuongThucThanhToan;
    private PhieuDatSan.TrangThaiCoc trangThaiCoc;
    private LocalDateTime thoiGianCoc;
    private String ghiChu;
    private LocalDateTime createdAt;

    private String tenNguoiDat;
    private String sdtNguoiDat;

    private List<ChiTietResponse> danhSachChiTiet;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ChiTietResponse {
        private String tenSan;
        private String loaiSan;
        private LocalDate ngaySuDung;
        private LocalTime gioBatDau;
        private LocalTime gioKetThuc;
        private BigDecimal donGia;
    }

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
                .tienCoc(entity.getTienCoc())
                .trangThai(entity.getTrangThai())
                .trangThaiThanhToan(entity.getTrangThaiThanhToan())
                .phuongThucThanhToan(entity.getPhuongThucThanhToan())
                .trangThaiCoc(entity.getTrangThaiCoc())
                .thoiGianCoc(entity.getThoiGianCoc())
                .ghiChu(entity.getGhiChu())
                .createdAt(entity.getCreatedAt())
                .tenNguoiDat(entity.getNguoiDat().getHoTen())
                .sdtNguoiDat(entity.getNguoiDat().getSoDienThoai())
                .danhSachChiTiet(chiTietList)
                .build();
    }
}
