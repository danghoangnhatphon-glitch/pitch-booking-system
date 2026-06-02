package football_field_booking.demo.controller.mvc;

import football_field_booking.demo.dto.request.DangKyRequest;
import football_field_booking.demo.service.NguoiDungService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthMvcController {

    private final NguoiDungService nguoiDungService;

    @GetMapping("/dang-nhap")
    public String trangDangNhap() {
        return "auth/dang-nhap";
    }

    @GetMapping("/dang-ky")
    public String trangDangKy() {
        return "auth/dang-ky";
    }

    @PostMapping("/dang-ky")
    public String xuLyDangKy(
            @RequestParam String hoTen,
            @RequestParam String email,
            @RequestParam String matKhau,
            @RequestParam String xacNhanMatKhau,
            @RequestParam(required = false) String soDienThoai,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!matKhau.equals(xacNhanMatKhau)) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp");
            // Giữ lại dữ liệu đã nhập để không phải nhập lại
            model.addAttribute("hoTen", hoTen);
            model.addAttribute("email", email);
            model.addAttribute("soDienThoai", soDienThoai);
            return "auth/dang-ky";
        }

        try {
            DangKyRequest request = DangKyRequest.builder()
                    .hoTen(hoTen)
                    .email(email)
                    .matKhau(matKhau)
                    .soDienThoai(soDienThoai)
                    .build();

            nguoiDungService.dangKy(request);

            redirectAttributes.addFlashAttribute("successMessage",
                "Đăng ký thành công! Đăng nhập để tiếp tục.");
            return "redirect:/auth/dang-nhap";

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("hoTen", hoTen);
            model.addAttribute("email", email);
            model.addAttribute("soDienThoai", soDienThoai);
            return "auth/dang-ky";
        }
    }
}
