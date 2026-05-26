package football_field_booking.demo.controller;

import football_field_booking.demo.dto.request.DatSanRequest;
import football_field_booking.demo.dto.response.ApiResponse;
import football_field_booking.demo.dto.response.PhieuDatSanResponse;
import football_field_booking.demo.entity.NguoiDung;
import football_field_booking.demo.entity.PhieuDatSan;
import football_field_booking.demo.service.DatSanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dat-san")
@RequiredArgsConstructor
public class DatSanController {

    private final DatSanService datSanService;

    // ================================================================
    // POST /api/dat-san
    // Khách hàng đặt sân
    // Body: { sanId, ngaySuDung, danhSachKhungGioId, ghiChu }
    // ================================================================
    @PostMapping
    @PreAuthorize("hasRole('KHACH_HANG')")
    public ResponseEntity<ApiResponse<PhieuDatSanResponse>> datSan(
            @Valid @RequestBody DatSanRequest request,
            @AuthenticationPrincipal NguoiDung nguoiDung) {

        PhieuDatSanResponse response = datSanService.datSan(request, nguoiDung.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Đặt sân thành công! Vui lòng chờ xác nhận.", response));
    }

    // ================================================================
    // GET /api/dat-san/lich-su
    // Khách hàng xem lịch sử đặt sân của mình
    // ================================================================
    @GetMapping("/lich-su")
    @PreAuthorize("hasAnyRole('KHACH_HANG', 'CHU_SAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<PhieuDatSanResponse>>> layLichSu(
            @AuthenticationPrincipal NguoiDung nguoiDung) {

        return ResponseEntity.ok(
            ApiResponse.ok(datSanService.layLichSu(nguoiDung.getId()))
        );
    }

    // ================================================================
    // DELETE /api/dat-san/{id}/huy
    // Khách hàng hủy phiếu đặt (chỉ được hủy khi đang CHO_DUYET)
    // ================================================================
    @DeleteMapping("/{id}/huy")
    @PreAuthorize("hasRole('KHACH_HANG')")
    public ResponseEntity<ApiResponse<PhieuDatSanResponse>> huyPhieu(
            @PathVariable Long id,
            @AuthenticationPrincipal NguoiDung nguoiDung) {

        return ResponseEntity.ok(ApiResponse.ok(
            "Hủy phiếu thành công",
            datSanService.huyPhieu(id, nguoiDung.getId())
        ));
    }

    // ================================================================
    // GET /api/dat-san/cho-duyet
    // Chủ sân xem danh sách phiếu đang chờ duyệt
    // ================================================================
    @GetMapping("/cho-duyet")
    @PreAuthorize("hasRole('CHU_SAN')")
    public ResponseEntity<ApiResponse<List<PhieuDatSanResponse>>> layPhieuChoDuyet(
            @AuthenticationPrincipal NguoiDung nguoiDung) {

        return ResponseEntity.ok(
            ApiResponse.ok(datSanService.layPhieuChoDuyet(nguoiDung.getId()))
        );
    }

    // ================================================================
    // PATCH /api/dat-san/{id}/duyet
    // Chủ sân duyệt phiếu
    // ================================================================
    @PatchMapping("/{id}/duyet")
    @PreAuthorize("hasRole('CHU_SAN')")
    public ResponseEntity<ApiResponse<PhieuDatSanResponse>> duyetPhieu(
            @PathVariable Long id,
            @AuthenticationPrincipal NguoiDung nguoiDung) {

        return ResponseEntity.ok(ApiResponse.ok(
            "Duyệt phiếu thành công",
            datSanService.duyetPhieu(id, nguoiDung.getId())
        ));
    }

    // ================================================================
    // PATCH /api/dat-san/{id}/thanh-toan?phuongThuc=TIEN_MAT
    // Chủ sân xác nhận đã nhận tiền
    // ================================================================
    @PatchMapping("/{id}/thanh-toan")
    @PreAuthorize("hasRole('CHU_SAN')")
    public ResponseEntity<ApiResponse<PhieuDatSanResponse>> xacNhanThanhToan(
            @PathVariable Long id,
            @RequestParam PhieuDatSan.PhuongThucThanhToan phuongThuc,
            @AuthenticationPrincipal NguoiDung nguoiDung) {

        return ResponseEntity.ok(ApiResponse.ok(
            "Xác nhận thanh toán thành công",
            datSanService.xacNhanThanhToan(id, phuongThuc, nguoiDung.getId())
        ));
    }
}
