package football_field_booking.demo.dto.response;

import football_field_booking.demo.entity.SanBong;
import lombok.*;

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
    private String tenChuSan;
    private String sdtChuSan;
    private Double diemTrungBinh;

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

    public static SanBongResponse from(SanBong entity, Double diemTrungBinh) {
        SanBongResponse dto = from(entity);
        dto.setDiemTrungBinh(diemTrungBinh);
        return dto;
    }
}
