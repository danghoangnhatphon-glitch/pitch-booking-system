package football_field_booking.demo.controller;

import football_field_booking.demo.dto.request.DangKyRequest;
import football_field_booking.demo.dto.request.XacNhanOtpRequest;
import football_field_booking.demo.dto.response.ApiResponse;
import football_field_booking.demo.dto.response.NguoiDungResponse;
import football_field_booking.demo.service.NguoiDungService;
import football_field_booking.demo.service.OtpService;
import football_field_booking.demo.service.SmsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final NguoiDungService nguoiDungService;
    private final OtpService otpService;
    private final SmsService smsService;

    @PostMapping("/dang-ky")
    public ResponseEntity<ApiResponse<NguoiDungResponse>> dangKy(
            @Valid @RequestBody DangKyRequest request) {

        NguoiDungResponse response = nguoiDungService.dangKy(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Đăng ký thành công! Chào mừng bạn.", response));
    }

    /**
     * Bước 1 của đăng ký có xác thực OTP: kiểm tra dữ liệu hợp lệ (email chưa
     * tồn tại, mật khẩu đúng định dạng...), gửi mã OTP về số điện thoại, và lưu
     * tạm thông tin đăng ký chờ xác nhận — CHƯA tạo tài khoản ở bước này.
     */
    @PostMapping("/gui-otp-dang-ky")
    public ResponseEntity<ApiResponse<Void>> guiOtpDangKy(
            @Valid @RequestBody DangKyRequest request) {

        nguoiDungService.kiemTraEmailChuaTonTai(request.getEmail());

        String maOtp = otpService.taoOtp(request.getSoDienThoai(), request);
        smsService.guiOtp(request.getSoDienThoai(), maOtp);

        return ResponseEntity.ok(ApiResponse.ok(
                "Đã gửi mã OTP tới số điện thoại " + request.getSoDienThoai()));
    }

    /**
     * Bước 2: xác nhận mã OTP đúng → tạo tài khoản với thông tin đã lưu ở bước 1.
     */
    @PostMapping("/xac-nhan-otp-dang-ky")
    public ResponseEntity<ApiResponse<NguoiDungResponse>> xacNhanOtpDangKy(
            @Valid @RequestBody XacNhanOtpRequest request) {

        DangKyRequest thongTinDangKy = otpService.xacNhanOtp(request.getSoDienThoai(), request.getMaOtp());
        NguoiDungResponse response = nguoiDungService.dangKy(thongTinDangKy);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Xác thực thành công! Tài khoản đã được tạo.", response));
    }
}
