package football_field_booking.demo.controller;

import football_field_booking.demo.dto.request.DangKyRequest;
import football_field_booking.demo.dto.response.ApiResponse;
import football_field_booking.demo.dto.response.NguoiDungResponse;
import football_field_booking.demo.service.NguoiDungService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý đăng ký / đăng nhập
 *
 * Lưu ý: đăng nhập thực tế do Spring Security xử lý tại /api/auth/login
 * (cấu hình trong SecurityConfig) — không cần viết method login thủ công.
 * Ở đây chỉ cần viết đăng ký.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final NguoiDungService nguoiDungService;

    /**
     * POST /api/auth/dang-ky
     * Body: { hoTen, email, matKhau, soDienThoai }
     *
     * @Valid → Spring tự validate DangKyRequest trước khi vào method
     * Nếu lỗi → GlobalExceptionHandler bắt và trả 400
     */
    @PostMapping("/dang-ky")
    public ResponseEntity<ApiResponse<NguoiDungResponse>> dangKy(
            @Valid @RequestBody DangKyRequest request) {

        NguoiDungResponse response = nguoiDungService.dangKy(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)  // HTTP 201
                .body(ApiResponse.ok("Đăng ký thành công! Chào mừng bạn.", response));
    }
}
