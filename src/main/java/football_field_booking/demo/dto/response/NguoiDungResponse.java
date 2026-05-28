package football_field_booking.demo.dto.response;

import football_field_booking.demo.entity.NguoiDung;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Thông tin người dùng trả về cho client
 *
 * ⚠️ KHÔNG bao gồm matKhau — không bao giờ trả password về client
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NguoiDungResponse {
    private boolean active;  // ← thêm dòng này
    private Long id;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String anhDaiDien;
    private NguoiDung.VaiTro vaiTro;
    private LocalDateTime createdAt;

    // ================================================================
    // Static factory method — chuyển Entity → DTO trong 1 dòng
    // Dùng: NguoiDungResponse.from(nguoiDung)
    // ================================================================
    public static NguoiDungResponse from(NguoiDung entity) {
        return NguoiDungResponse.builder()
                .active(entity.isActive()) // ← thêm dòng này
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
