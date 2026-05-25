package football_field_booking.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Dữ liệu client gửi lên khi ĐĂNG KÝ tài khoản
 *
 * @NotBlank    → không được null, không được rỗng, không được toàn khoảng trắng
 * @Email       → phải đúng định dạng email
 * @Size        → giới hạn độ dài
 * @Pattern     → phải khớp regex (SĐT Việt Nam)
 *
 * Spring tự động validate trước khi vào Controller
 * nếu Controller có annotation @Valid
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DangKyRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không quá 100 ký tự")
    private String hoTen;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 50, message = "Mật khẩu từ 6 đến 50 ký tự")
    private String matKhau;

    // Regex SĐT Việt Nam: bắt đầu 03,05,07,08,09 + 8 số
    @Pattern(regexp = "^(03|05|07|08|09)\\d{8}$",
             message = "Số điện thoại không hợp lệ")
    private String soDienThoai;
}
