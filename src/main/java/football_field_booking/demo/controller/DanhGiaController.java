package football_field_booking.demo.controller;

import football_field_booking.demo.dto.request.DanhGiaRequest;
import football_field_booking.demo.dto.response.ApiResponse;
import football_field_booking.demo.entity.NguoiDung;
import football_field_booking.demo.service.DanhGiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/danh-gia")
@RequiredArgsConstructor
public class DanhGiaController {

    private final DanhGiaService danhGiaService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> guiDanhGia(
            @Valid @RequestBody DanhGiaRequest request,
            @AuthenticationPrincipal NguoiDung nguoiDung) {

        danhGiaService.guiDanhGia(request, nguoiDung.getId());
        return ResponseEntity.ok(ApiResponse.ok("Cảm ơn bạn đã gửi đánh giá!", null));
    }

    @PutMapping("/{phieuDatId}")
    public ResponseEntity<ApiResponse<String>> suaDanhGia(
            @PathVariable Long phieuDatId,
            @Valid @RequestBody DanhGiaRequest request,
            @AuthenticationPrincipal NguoiDung nguoiDung) {

        danhGiaService.suaDanhGia(phieuDatId, request, nguoiDung.getId());
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật đánh giá thành công!", null));
    }

    @DeleteMapping("/{phieuDatId}")
    public ResponseEntity<ApiResponse<String>> xoaDanhGia(
            @PathVariable Long phieuDatId,
            @AuthenticationPrincipal NguoiDung nguoiDung) {

        danhGiaService.xoaDanhGia(phieuDatId, nguoiDung.getId());
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa đánh giá", null));
    }
}
