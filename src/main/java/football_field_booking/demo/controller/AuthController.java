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


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final NguoiDungService nguoiDungService;

    @PostMapping("/dang-ky")
    public ResponseEntity<ApiResponse<NguoiDungResponse>> dangKy(
            @Valid @RequestBody DangKyRequest request) {

        NguoiDungResponse response = nguoiDungService.dangKy(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Đăng ký thành công! Chào mừng bạn.", response));
    }
}
