package football_field_booking.demo.controller.mvc;

import football_field_booking.demo.entity.NguoiDung;
import football_field_booking.demo.service.DatSanService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dat-san")
@RequiredArgsConstructor
public class KhachHangMvcController {

    private final DatSanService datSanService;

    // ── Lịch sử đặt sân của người đang đăng nhập ─────────────
    @GetMapping("/lich-su")
    public String lichSu(
            @AuthenticationPrincipal NguoiDung nguoiDung,
            Model model) {

        model.addAttribute("danhSachPhieu",
            datSanService.layLichSu(nguoiDung.getId()));
        model.addAttribute("currentPage", "lich-su");

        return "khachhang/lich-su";
    }
}
