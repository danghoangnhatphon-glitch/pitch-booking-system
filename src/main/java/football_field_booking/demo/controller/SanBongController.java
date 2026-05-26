package football_field_booking.demo.controller;

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

    // ================================================================
    // GET /api/san-bong
    // Lấy tất cả sân đang hoạt động — ai cũng xem được (public)
    // ================================================================
    @GetMapping
    public ResponseEntity<ApiResponse<List<SanBongResponse>>> layTatCaSan() {
        return ResponseEntity.ok(ApiResponse.ok(sanBongService.layTatCaSan()));
    }

    // ================================================================
    // GET /api/san-bong/{id}
    // Chi tiết 1 sân — public
    // ================================================================
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SanBongResponse>> layChiTiet(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(sanBongService.layChiTiet(id)));
    }

    // ================================================================
    // GET /api/san-bong/tim-kiem?quanHuyen=Quận 5&loaiSan=SAN_5
    // Tìm sân theo bộ lọc — public
    // ================================================================
    @GetMapping("/tim-kiem")
    public ResponseEntity<ApiResponse<List<SanBongResponse>>> timSan(
            @RequestParam(required = false) String quanHuyen,
            @RequestParam(required = false) SanBong.LoaiSan loaiSan) {

        TimSanRequest request = TimSanRequest.builder()
                .quanHuyen(quanHuyen)
                .loaiSan(loaiSan)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(sanBongService.timSan(request)));
    }

    // ================================================================
    // GET /api/san-bong/{id}/lich?ngay=2025-06-01
    // Xem lịch sân theo ngày — ai cũng xem được (public)
    // Đây là API cho UI chọn giờ như chọn ghế rạp phim
    // ================================================================
    @GetMapping("/{id}/lich")
    public ResponseEntity<ApiResponse<List<KhungGioTrangThaiResponse>>> layLichSan(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay) {

        return ResponseEntity.ok(ApiResponse.ok(sanBongService.layLichSan(id, ngay)));
    }

    // ================================================================
    // GET /api/san-bong/cua-toi
    // Chủ sân xem danh sách sân của mình
    // @PreAuthorize → chỉ CHU_SAN mới vào được (Security check)
    // ================================================================
    @GetMapping("/cua-toi")
    @PreAuthorize("hasRole('CHU_SAN')")
    public ResponseEntity<ApiResponse<List<SanBongResponse>>> laySanCuaToi(
            @AuthenticationPrincipal NguoiDung nguoiDung) {

        // @AuthenticationPrincipal → Spring tự inject người đang đăng nhập
        // Không cần query DB lại, lấy trực tiếp từ Security context
        return ResponseEntity.ok(
            ApiResponse.ok(sanBongService.laySanCuaChuSan(nguoiDung.getId()))
        );
    }

    // ================================================================
    // PATCH /api/san-bong/{id}/trang-thai?trangThai=DONG_CUA
    // Chủ sân đổi trạng thái sân
    // ================================================================
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
}
