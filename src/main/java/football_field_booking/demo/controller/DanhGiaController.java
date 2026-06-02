package football_field_booking.demo.controller;

import football_field_booking.demo.dto.request.DanhGiaRequest;
import football_field_booking.demo.dto.response.ApiResponse;
import football_field_booking.demo.entity.NguoiDung;
import football_field_booking.demo.service.DanhGiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/danh-gia")
@RequiredArgsConstructor
public class DanhGiaController {

    private final DanhGiaService danhGiaService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> guiDanhGia(
            @RequestBody DanhGiaRequest request,
            @AuthenticationPrincipal NguoiDung nguoiDung) {

        danhGiaService.guiDanhGia(request, nguoiDung.getId());
        return ResponseEntity.ok(ApiResponse.ok("Cảm ơn bạn đã gửi đánh giá!", null));
    }
}