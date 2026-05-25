package football_field_booking.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Dữ liệu client gửi lên khi ĐẶT SÂN
 *
 * Một request có thể đặt NHIỀU CA cùng lúc
 * Ví dụ: đặt sân 1, ngày 01/06, ca 17:30 VÀ 19:00
 *   → sanId = 1
 *   → ngaySuDung = 2025-06-01
 *   → danhSachKhungGioId = [6, 7]
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatSanRequest {

    @NotNull(message = "Vui lòng chọn sân")
    private Long sanId;

    @NotNull(message = "Vui lòng chọn ngày")
    @Future(message = "Ngày đặt phải là ngày trong tương lai")
    private LocalDate ngaySuDung;

    @NotEmpty(message = "Vui lòng chọn ít nhất 1 khung giờ")
    @Size(max = 4, message = "Tối đa 4 khung giờ mỗi lần đặt")
    private List<Long> danhSachKhungGioId;

    private String ghiChu;

    // Phương thức thanh toán (có thể null nếu thanh toán tại sân)
    private String phuongThucThanhToan;
}
