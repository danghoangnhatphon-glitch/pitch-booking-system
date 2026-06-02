package football_field_booking.demo.controller;

import football_field_booking.demo.dto.request.SanBongRequest;
import football_field_booking.demo.dto.request.TimSanRequest;
import football_field_booking.demo.dto.response.ApiResponse;
import football_field_booking.demo.dto.response.KhungGioTrangThaiResponse;
import football_field_booking.demo.dto.response.SanBongResponse;
import football_field_booking.demo.entity.NguoiDung;
import football_field_booking.demo.entity.SanBong;
import football_field_booking.demo.service.SanBongService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/san-bong")
@RequiredArgsConstructor
public class SanBongController {

    private final SanBongService sanBongService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SanBongResponse>>> layTatCaSan() {
        return ResponseEntity.ok(ApiResponse.ok(sanBongService.layTatCaSan()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SanBongResponse>> layChiTiet(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(sanBongService.layChiTiet(id)));
    }

    @GetMapping("/tim-kiem")
    public ResponseEntity<ApiResponse<List<SanBongResponse>>> timSan(
            @RequestParam(required = false) String quanHuyen,
            @RequestParam(required = false) SanBong.LoaiSan loaiSan) {

        if (quanHuyen != null && quanHuyen.trim().isEmpty()) {
            quanHuyen = null;
        }

        TimSanRequest request = TimSanRequest.builder()
                .quanHuyen(quanHuyen)
                .loaiSan(loaiSan)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(sanBongService.timSan(request)));
    }

    @GetMapping("/{id}/lich")
    public ResponseEntity<ApiResponse<List<KhungGioTrangThaiResponse>>> layLichSan(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay) {

        return ResponseEntity.ok(ApiResponse.ok(sanBongService.layLichSan(id, ngay)));
    }

    @GetMapping("/cua-toi")
    @PreAuthorize("hasRole('CHU_SAN')")
    public ResponseEntity<ApiResponse<List<SanBongResponse>>> laySanCuaToi(
            @AuthenticationPrincipal NguoiDung nguoiDung) {

        return ResponseEntity.ok(
                ApiResponse.ok(sanBongService.laySanCuaChuSan(nguoiDung.getId()))
        );
    }

    @PatchMapping("/{id}/trang-thai")
    @PreAuthorize("hasRole('CHU_SAN')")
    public ResponseEntity<ApiResponse<SanBongResponse>> doiTrangThai(
            @PathVariable Long id,
            @RequestParam SanBong.TrangThai trangThai,
            @AuthenticationPrincipal NguoiDung nguoiDung) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Cập nhật trạng thái thành công",
                sanBongService.doiTrangThai(id, trangThai, nguoiDung.getId())
        ));
    }

    @PostMapping("/luu")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SanBongResponse>> luuSan(
            @RequestBody SanBongRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(
                request.getId() == null ? "Thêm sân thành công" : "Cập nhật sân thành công",
                sanBongService.luuSanBong(request)
        ));
    }
}