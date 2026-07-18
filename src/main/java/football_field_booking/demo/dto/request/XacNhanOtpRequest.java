package football_field_booking.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class XacNhanOtpRequest {

    @NotBlank(message = "Thiếu số điện thoại")
    private String soDienThoai;

    @NotBlank(message = "Vui lòng nhập mã OTP")
    @Pattern(regexp = "^\\d{6}$", message = "Mã OTP gồm 6 chữ số")
    private String maOtp;
}
