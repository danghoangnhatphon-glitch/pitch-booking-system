package football_field_booking.demo.dto.response;

import football_field_booking.demo.entity.NguoiDung;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NguoiDungResponse {
    private boolean active;
    private Long id;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String anhDaiDien;
    private NguoiDung.VaiTro vaiTro;
    private LocalDateTime createdAt;

    public static NguoiDungResponse from(NguoiDung entity) {
        return NguoiDungResponse.builder()
                .active(entity.isActive())
                .id(entity.getId())
                .hoTen(entity.getHoTen())
                .email(entity.getEmail())
                .soDienThoai(entity.getSoDienThoai())
                .anhDaiDien(entity.getAnhDaiDien())
                .vaiTro(entity.getVaiTro())
                .createdAt(entity.getCreatedAt())
                .build();

    }
}
