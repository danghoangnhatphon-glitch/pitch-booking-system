package football_field_booking.demo.controller.mvc;

import football_field_booking.demo.dto.request.TimSanRequest;
import football_field_booking.demo.dto.response.KhungGioTrangThaiResponse;
import football_field_booking.demo.entity.SanBong;
import football_field_booking.demo.service.SanBongService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller render HTML qua Thymeleaf
 *
 * Khác với @RestController (trả JSON),
 * @Controller trả tên template → Thymeleaf tìm file trong /templates/
 *
 * VD: return "khachhang/trang-chu"
 *   → render file /templates/khachhang/trang-chu.html
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SanBongService sanBongService;

    // ================================================================
    // GET /  →  trang chủ
    // ================================================================
    @GetMapping("/")
    public String trangChu(
            @RequestParam(required = false) String quanHuyen,
            @RequestParam(required = false) SanBong.LoaiSan loaiSan,
            Model model) {

        // Lấy danh sách sân theo bộ lọc
        var request = TimSanRequest.builder()
                .quanHuyen(quanHuyen)
                .loaiSan(loaiSan)
                .build();

        model.addAttribute("danhSachSan",    sanBongService.timSan(request));
        model.addAttribute("quanHuyenFilter", quanHuyen);
        model.addAttribute("loaiSanFilter",   loaiSan);
        model.addAttribute("currentPage",     "home");

        // Danh sách quận để render dropdown bộ lọc
        model.addAttribute("danhSachQuan", List.of(
            "Quận 1", "Quận 3", "Quận 5", "Quận 7",
            "Quận Bình Thạnh", "Quận Phú Nhuận",
            "Quận Gò Vấp", "Thủ Đức"
        ));

        return "khachhang/trang-chu";
    }

    // ================================================================
    // GET /san-bong  →  danh sách sân (giống trang chủ, có bộ lọc)
    // ================================================================
    @GetMapping("/san-bong")
    public String danhSachSan(
            @RequestParam(required = false) String quanHuyen,
            @RequestParam(required = false) SanBong.LoaiSan loaiSan,
            Model model) {

        var request = TimSanRequest.builder()
                .quanHuyen(quanHuyen)
                .loaiSan(loaiSan)
                .build();

        model.addAttribute("danhSachSan",    sanBongService.timSan(request));
        model.addAttribute("quanHuyenFilter", quanHuyen);
        model.addAttribute("loaiSanFilter",   loaiSan);
        model.addAttribute("currentPage",     "san-bong");
        model.addAttribute("danhSachQuan", List.of(
            "Quận 1", "Quận 3", "Quận 5", "Quận 7",
            "Quận Bình Thạnh", "Quận Phú Nhuận",
            "Quận Gò Vấp", "Thủ Đức"
        ));

        return "khachhang/trang-chu";  // dùng chung template
    }

    // ================================================================
    // GET /san-bong/{id}  →  chi tiết sân + lịch hôm nay
    // ================================================================
    @GetMapping("/san-bong/{id}")
    public String chiTietSan(
            @PathVariable Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay,
            Model model) {

        // Mặc định xem lịch hôm nay
        LocalDate ngayXem = (ngay != null) ? ngay : LocalDate.now();

        // Thông tin sân
        model.addAttribute("san", sanBongService.layChiTiet(id));

        // Lịch khung giờ theo ngày
        List<KhungGioTrangThaiResponse> lichSan = sanBongService.layLichSan(id, ngayXem);
        model.addAttribute("danhSachKhungGio", lichSan);
        model.addAttribute("ngayChon", ngayXem.toString());
        model.addAttribute("currentPage", "san-bong");

        return "khachhang/chi-tiet-san";
    }
}
