package football_field_booking.demo.controller;

import football_field_booking.demo.dto.request.CapNhatThongTinRequest;
import football_field_booking.demo.dto.request.DoiMatKhauRequest;
import football_field_booking.demo.dto.response.ApiResponse;
import football_field_booking.demo.dto.response.NguoiDungResponse;
import football_field_booking.demo.entity.NguoiDung;
import football_field_booking.demo.service.NguoiDungService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nguoi-dung")
@RequiredArgsConstructor
public class NguoiDungController {

    private final NguoiDungService nguoiDungService;

    @GetMapping("/toi")
    public ResponseEntity<ApiResponse<NguoiDungResponse>> layThongTinCuaToi(
            @AuthenticationPrincipal NguoiDung nguoiDung) {
        return ResponseEntity.ok(ApiResponse.ok(nguoiDungService.layThongTin(nguoiDung.getId())));
    }

    @PutMapping("/toi")
    public ResponseEntity<ApiResponse<NguoiDungResponse>> capNhatThongTin(
            @Valid @RequestBody CapNhatThongTinRequest request,
            @AuthenticationPrincipal NguoiDung nguoiDung) {

        NguoiDungResponse response = nguoiDungService.capNhatThongTin(nguoiDung.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thông tin thành công!", response));
    }

    @PutMapping("/doi-mat-khau")
    public ResponseEntity<ApiResponse<Void>> doiMatKhau(
            @Valid @RequestBody DoiMatKhauRequest request,
            @AuthenticationPrincipal NguoiDung nguoiDung) {

        nguoiDungService.doiMatKhau(nguoiDung.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Đổi mật khẩu thành công!"));
    }
}
