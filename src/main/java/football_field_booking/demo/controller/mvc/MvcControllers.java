package football_field_booking.demo.controller.mvc;

import football_field_booking.demo.dto.response.ApiResponse;
import football_field_booking.demo.entity.NguoiDung;
import football_field_booking.demo.service.AdminService;
import football_field_booking.demo.service.CocService;
import football_field_booking.demo.service.DatSanService;
import football_field_booking.demo.service.SanBongService;
import football_field_booking.demo.service.ThongKeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// ================================================================
// ADMIN CONTROLLER
// ================================================================
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
class AdminMvcController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Map<String, Object> tk = adminService.thongKeTongQuan();
        model.addAttribute("tongNguoiDung", tk.get("tongNguoiDung"));
        model.addAttribute("tongSan",       tk.get("tongSan"));
        model.addAttribute("tongPhieu",     tk.get("tongPhieu"));
        model.addAttribute("tongDoanhThu",  tk.get("tongDoanhThu"));
        model.addAttribute("danhSachNguoiDung", adminService.layTatCaNguoiDung());
        model.addAttribute("danhSachSan",       adminService.layTatCaSan());
        model.addAttribute("currentPage", "admin");
        return "admin/dashboard";
    }

    // ── API endpoints cho JS gọi ─────────────────────────────
    @PatchMapping("/api/nguoi-dung/{id}/khoa")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> khoaTaiKhoan(@PathVariable Long id) {
        adminService.khoaTaiKhoan(id);
        return ResponseEntity.ok(ApiResponse.ok("Đã khóa tài khoản"));
    }

    @PatchMapping("/api/nguoi-dung/{id}/mo-khoa")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> moKhoaTaiKhoan(@PathVariable Long id) {
        adminService.moKhoaTaiKhoan(id);
        return ResponseEntity.ok(ApiResponse.ok("Đã mở khóa tài khoản"));
    }

    @DeleteMapping("/api/san-bong/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> xoaSan(@PathVariable Long id) {
        adminService.xoaSan(id);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa sân"));
    }
}

// ================================================================
// CHU SAN CONTROLLER
// ================================================================
@Controller
@RequestMapping("/chusan")
@PreAuthorize("hasRole('CHU_SAN')")
@RequiredArgsConstructor
class ChuSanMvcController {

    private final DatSanService  datSanService;
    private final SanBongService sanBongService;
    private final ThongKeService thongKeService;
    private final CocService     cocService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal NguoiDung nguoiDung, Model model) {
        Long id = nguoiDung.getId();
        model.addAttribute("phieuChoDuyet", datSanService.layPhieuChoDuyet(id));
        model.addAttribute("danhSachSan",   sanBongService.laySanCuaChuSan(id));
        model.addAttribute("tongQuan",      thongKeService.tongQuan(id));
        model.addAttribute("doanhThu",      thongKeService.thongKeDoanhThu(id));
        model.addAttribute("nguoiDung",     nguoiDung);
        model.addAttribute("currentPage",   "dashboard");
        return "chusan/dashboard";
    }

    // ── Duyệt phiếu ─────────────────────────────────────────
    @PostMapping("/duyet-phieu/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> duyetPhieu(
            @PathVariable Long id,
            @AuthenticationPrincipal NguoiDung nguoiDung) {
        datSanService.duyetPhieu(id, nguoiDung.getId());
        return ResponseEntity.ok(ApiResponse.ok("Duyệt phiếu thành công"));
    }

    // ── Từ chối phiếu ────────────────────────────────────────
    @PostMapping("/tu-choi-phieu/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> tuChoiPhieu(
            @PathVariable Long id,
            @AuthenticationPrincipal NguoiDung nguoiDung) {
        datSanService.huyPhieu(id, nguoiDung.getId());
        return ResponseEntity.ok(ApiResponse.ok("Đã từ chối phiếu"));
    }

    // ── Đổi trạng thái sân ───────────────────────────────────
    @PostMapping("/doi-trang-thai-san")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> doiTrangThaiSan(
            @RequestParam Long sanId,
            @RequestParam football_field_booking.demo.entity.SanBong.TrangThai trangThai,
            @AuthenticationPrincipal NguoiDung nguoiDung) {
        sanBongService.doiTrangThai(sanId, trangThai, nguoiDung.getId());
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công"));
    }

    // ── Cập nhật ngân hàng ───────────────────────────────────
    @PostMapping("/cap-nhat-ngan-hang")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> capNhatNganHang(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal NguoiDung nguoiDung) {
        cocService.capNhatNganHang(
            nguoiDung.getId(),
            body.get("tenNganHang"),
            body.get("soTaiKhoan"),
            body.get("tenChuTk")
        );
        return ResponseEntity.ok(ApiResponse.ok("Lưu thông tin ngân hàng thành công"));
    }
}

// ================================================================
// KHACH HANG CONTROLLER
// ================================================================
@Controller
@RequestMapping("/dat-san")
@PreAuthorize("hasRole('KHACH_HANG')")
@RequiredArgsConstructor
class KhachHangMvcController {

    private final DatSanService datSanService;
    private final CocService    cocService;

    // ── Lịch sử đặt sân ──────────────────────────────────────
    @GetMapping("/lich-su")
    public String lichSu(@AuthenticationPrincipal NguoiDung nguoiDung, Model model) {
        model.addAttribute("danhSachPhieu",
            datSanService.layLichSu(nguoiDung.getId()));
        model.addAttribute("currentPage", "lich-su");
        return "khachhang/lich-su";
    }

    // ── Trang thanh toán cọc ─────────────────────────────────
    @GetMapping("/{id}/thanh-toan-coc")
    public String trangThanhToanCoc(
            @PathVariable Long id,
            @AuthenticationPrincipal NguoiDung nguoiDung,
            Model model) {

        // Lấy thông tin phiếu
        var phieuList = datSanService.layLichSu(nguoiDung.getId());
        var phieu = phieuList.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new football_field_booking.demo.exception.AppException
                    .ResourceNotFoundException("Không tìm thấy phiếu #" + id));

        model.addAttribute("phieu", phieu);

        // Lấy thông tin ngân hàng chủ sân
        try {
            Map<String, String> nganhang = cocService.layThongTinNganHang(id);
            model.addAttribute("tenNganHang", nganhang.get("tenNganHang"));
            model.addAttribute("soTaiKhoan",  nganhang.get("soTaiKhoan"));
            model.addAttribute("tenChuTk",    nganhang.get("tenChuTk"));
            model.addAttribute("maNganHang",  nganhang.get("maNganHang"));
        } catch (Exception e) {
            model.addAttribute("tenNganHang", "Chưa cập nhật");
            model.addAttribute("soTaiKhoan",  "");
            model.addAttribute("tenChuTk",    "");
            model.addAttribute("maNganHang",  "970436");
        }

        return "khachhang/thanh-toan-coc";
    }

    // ── Xác nhận đã chuyển khoản cọc ────────────────────────
    @PostMapping("/{id}/xac-nhan-coc")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> xacNhanCoc(
            @PathVariable Long id,
            @AuthenticationPrincipal NguoiDung nguoiDung) {
        cocService.xacNhanDaChuyenKhoan(id, nguoiDung.getId());
        return ResponseEntity.ok(ApiResponse.ok("Xác nhận đặt cọc thành công! Chờ chủ sân xác nhận."));
    }
}
