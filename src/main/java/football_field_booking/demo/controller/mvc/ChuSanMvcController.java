package football_field_booking.demo.controller.mvc;

import football_field_booking.demo.entity.NguoiDung;
import football_field_booking.demo.service.DatSanService;
import football_field_booking.demo.service.SanBongService;
import football_field_booking.demo.service.ThongKeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/chusan")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CHU_SAN')")  // Toàn bộ controller chỉ CHU_SAN mới vào được
public class ChuSanMvcController {

    private final DatSanService  datSanService;
    private final SanBongService sanBongService;
    private final ThongKeService thongKeService;

    // ── Dashboard tổng quan ───────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(
            @AuthenticationPrincipal NguoiDung nguoiDung,
            Model model) {

        Long id = nguoiDung.getId();

        model.addAttribute("phieuChoDuyet", datSanService.layPhieuChoDuyet(id));
        model.addAttribute("danhSachSan",   sanBongService.laySanCuaChuSan(id));
        model.addAttribute("tongQuan",      thongKeService.tongQuan(id));
        model.addAttribute("doanhThu",      thongKeService.thongKeDoanhThu(id));
        model.addAttribute("currentPage",   "dashboard");

        return "chusan/dashboard";
    }
}
