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

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SanBongService sanBongService;

    @GetMapping("/")
    public String trangChu(
            @RequestParam(required = false) String quanHuyen,
            @RequestParam(required = false) String loaiSan,
            Model model) {
        if (quanHuyen != null && quanHuyen.trim().isEmpty()) {
            quanHuyen = null;
        }

        SanBong.LoaiSan enumLoaiSan = null;
        if (loaiSan != null && !loaiSan.trim().isEmpty()) {
            enumLoaiSan = SanBong.LoaiSan.valueOf(loaiSan);
        }

        var request = TimSanRequest.builder()
                .quanHuyen(quanHuyen)
                .loaiSan(enumLoaiSan)
                .build();

        model.addAttribute("danhSachSan",     sanBongService.timSan(request));
        model.addAttribute("quanHuyenFilter", quanHuyen);
        model.addAttribute("loaiSanFilter",   (enumLoaiSan != null) ? enumLoaiSan.name() : "");
        model.addAttribute("currentPage",     "home");

        model.addAttribute("danhSachQuan", List.of(
                "Quận 1", "Quận 3", "Quận 5", "Quận 7",
                "Quận Bình Thạnh", "Quận Phú Nhuận",
                "Quận Gò Vấp", "Thủ Đức"
        ));

        return "khachhang/trang-chu";
    }
    @GetMapping("/san-bong")
    public String danhSachSan(
            @RequestParam(required = false) String quanHuyen,
            // Tương tự như trang chủ
            @RequestParam(required = false) String loaiSan,
            Model model) {

        if (quanHuyen != null && quanHuyen.trim().isEmpty()) {
            quanHuyen = null;
        }

        SanBong.LoaiSan enumLoaiSan = null;
        if (loaiSan != null && !loaiSan.trim().isEmpty()) {
            enumLoaiSan = SanBong.LoaiSan.valueOf(loaiSan);
        }

        var request = TimSanRequest.builder()
                .quanHuyen(quanHuyen)
                .loaiSan(enumLoaiSan)
                .build();

        model.addAttribute("danhSachSan",     sanBongService.timSan(request));
        model.addAttribute("quanHuyenFilter", quanHuyen);
        model.addAttribute("loaiSanFilter",   (enumLoaiSan != null) ? enumLoaiSan.name() : "");
        model.addAttribute("currentPage",     "san-bong");
        model.addAttribute("danhSachQuan", List.of(
                "Quận 1", "Quận 3", "Quận 5", "Quận 7",
                "Quận Bình Thạnh", "Quận Phú Nhuận",
                "Quận Gò Vấp", "Thủ Đức"
        ));

        return "khachhang/trang-chu";
    }

    @GetMapping("/san-bong/{id}")
    public String chiTietSan(
            @PathVariable Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay,
            Model model) {

        LocalDate ngayXem = (ngay != null) ? ngay : LocalDate.now();

        model.addAttribute("san", sanBongService.layChiTiet(id));

        List<KhungGioTrangThaiResponse> lichSan = sanBongService.layLichSan(id, ngayXem);
        model.addAttribute("danhSachKhungGio", lichSan);
        model.addAttribute("ngayChon", ngayXem.toString());
        model.addAttribute("currentPage", "san-bong");

        return "khachhang/chi-tiet-san";
    }
}