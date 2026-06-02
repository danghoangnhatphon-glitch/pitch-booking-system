package football_field_booking.demo.controller;

import football_field_booking.demo.dto.response.ApiResponse;
import football_field_booking.demo.entity.NguoiDung;
import football_field_booking.demo.service.ThongKeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/thong-ke")
@RequiredArgsConstructor
public class ThongKeController {

    private final ThongKeService thongKeService;

    @GetMapping("/doanh-thu")
    @PreAuthorize("hasAnyRole('CHU_SAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> doanhThu(
            @AuthenticationPrincipal NguoiDung nguoiDung) {

        return ResponseEntity.ok(
            ApiResponse.ok(thongKeService.thongKeDoanhThu(nguoiDung.getId()))
        );
    }
    @GetMapping("/tong-quan")
    @PreAuthorize("hasAnyRole('CHU_SAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> tongQuan(
            @AuthenticationPrincipal NguoiDung nguoiDung) {

        return ResponseEntity.ok(
            ApiResponse.ok(thongKeService.tongQuan(nguoiDung.getId()))
        );
    }
}
