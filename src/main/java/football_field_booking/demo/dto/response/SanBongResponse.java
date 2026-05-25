package football_field_booking.demo.dto.response;

import football_field_booking.demo.entity.SanBong;
import lombok.*;

/**
 * Thông tin sân bóng trả về cho client
 * Dùng ở trang danh sách sân và trang chi tiết sân
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanBongResponse {

    private Long id;
    private String tenSan;
    private SanBong.LoaiSan loaiSan;
    private String viTri;
    private String quanHuyen;
    private String moTa;
    private String anhSan;
    private SanBong.TrangThai trangThai;

    // Thông tin chủ sân (chỉ cần tên + SĐT, không cần toàn bộ object)
    private String tenChuSan;
    private String sdtChuSan;

    // Điểm đánh giá trung bình (tính từ bảng danh_gia)
    private Double diemTrungBinh;

    // ================================================================
    // Static factory method
    // ================================================================
    public static SanBongResponse from(SanBong entity) {
        return SanBongResponse.builder()
                .id(entity.getId())
                .tenSan(entity.getTenSan())
                .loaiSan(entity.getLoaiSan())
                .viTri(entity.getViTri())
                .quanHuyen(entity.getQuanHuyen())
                .moTa(entity.getMoTa())
                .anhSan(entity.getAnhSan())
                .trangThai(entity.getTrangThai())
                // getChuSan() không null vì FK NOT NULL
                .tenChuSan(entity.getChuSan().getHoTen())
                .sdtChuSan(entity.getChuSan().getSoDienThoai())
                .build();
    }

    // Overload: kèm điểm đánh giá
    public static SanBongResponse from(SanBong entity, Double diemTrungBinh) {
        SanBongResponse dto = from(entity);
        dto.setDiemTrungBinh(diemTrungBinh);
        return dto;
    }
}
