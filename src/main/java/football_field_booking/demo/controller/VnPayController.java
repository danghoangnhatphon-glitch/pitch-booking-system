package football_field_booking.demo.controller;

import football_field_booking.demo.dto.response.ApiResponse;
import football_field_booking.demo.entity.NguoiDung;
import football_field_booking.demo.entity.PhieuDatSan;
import football_field_booking.demo.exception.AppException;
import football_field_booking.demo.repository.PhieuDatSanRepository;
import football_field_booking.demo.service.CocService;
import football_field_booking.demo.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class VnPayController {

    private final VnPayService vnPayService;
    private final CocService cocService;
    private final PhieuDatSanRepository phieuDatSanRepository;

    /**
     * Khách hàng bấm "Thanh toán qua VNPay" trên trang thanh-toan-coc →
     * gọi API này để lấy URL redirect sang cổng VNPay.
     */
    @PostMapping("/api/vnpay/tao-thanh-toan/{phieuId}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, String>>> taoThanhToan(
            @PathVariable Long phieuId,
            @AuthenticationPrincipal NguoiDung nguoiDung,
            HttpServletRequest request) {

        PhieuDatSan phieu = phieuDatSanRepository.findById(phieuId)
                .orElseThrow(() -> new AppException.ResourceNotFoundException(
                        "Không tìm thấy phiếu #" + phieuId));

        if (!phieu.getNguoiDat().getId().equals(nguoiDung.getId())) {
            throw new AppException.ForbiddenException("Không có quyền thực hiện");
        }
        if (phieu.getTrangThai() != PhieuDatSan.TrangThai.DA_DUYET) {
            throw new AppException.BadRequestException("Phiếu chưa được chủ sân duyệt");
        }
        if (phieu.getTrangThaiCoc() == PhieuDatSan.TrangThaiCoc.DA_COC) {
            throw new AppException.BadRequestException("Phiếu đã được đặt cọc rồi");
        }

        long soTienCoc = cocService.tinhTienCoc(phieuId);
        String diaChiIp = layDiaChiIpThuc(request);
        String url = vnPayService.taoUrlThanhToan(phieuId, soTienCoc, diaChiIp);

        return ResponseEntity.ok(ApiResponse.ok("Tạo giao dịch thành công", Map.of("url", url)));
    }

    /**
     * VNPay redirect trình duyệt khách hàng về đây sau khi thanh toán (thành công hoặc thất bại).
     * Đây là trải nghiệm hiển thị cho người dùng — nguồn xác nhận đáng tin cậy hơn là IPN bên dưới.
     */
    @GetMapping("/vnpay/return")
    public String vnpayReturn(HttpServletRequest request) {
        Map<String, String> params = layTatCaThamSo(request);
        Long phieuId = vnPayService.trichPhieuIdTuTxnRef(params.get("vnp_TxnRef"));

        if (phieuId == null) {
            return "redirect:/dat-san/lich-su?vnpay=error";
        }

        boolean chuKyHopLe = vnPayService.kiemTraChuKy(params);

        if (chuKyHopLe && vnPayService.laThanhCong(params)) {
            cocService.xacNhanQuaVnpay(phieuId);
            return "redirect:/dat-san/" + phieuId + "/thanh-toan-coc?vnpay=success";
        }

        log.warn("VNPay return thất bại hoặc sai chữ ký - phieuId={}, params={}", phieuId, params);
        return "redirect:/dat-san/" + phieuId + "/thanh-toan-coc?vnpay=fail";
    }

    /**
     * IPN (Instant Payment Notification) — VNPay gọi server-to-server để xác nhận
     * giao dịch, độc lập với việc khách hàng có đóng trình duyệt hay không.
     * Chỉ hoạt động khi server có địa chỉ public (xem hướng dẫn deploy Ngày 4).
     * Phải trả về đúng format JSON theo chuẩn VNPay, KHÔNG bọc trong ApiResponse.
     */
    @GetMapping(value = "/vnpay/ipn", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> vnpayIpn(HttpServletRequest request) {
        Map<String, String> params = layTatCaThamSo(request);
        Map<String, String> ketQua = new HashMap<>();

        if (!vnPayService.kiemTraChuKy(params)) {
            ketQua.put("RspCode", "97");
            ketQua.put("Message", "Invalid signature");
            return ketQua;
        }

        Long phieuId = vnPayService.trichPhieuIdTuTxnRef(params.get("vnp_TxnRef"));
        if (phieuId == null || phieuDatSanRepository.findById(phieuId).isEmpty()) {
            ketQua.put("RspCode", "01");
            ketQua.put("Message", "Order not found");
            return ketQua;
        }

        if (vnPayService.laThanhCong(params)) {
            cocService.xacNhanQuaVnpay(phieuId);
        }

        ketQua.put("RspCode", "00");
        ketQua.put("Message", "Confirm Success");
        return ketQua;
    }

    // ─────────────────────────────────────────────────────────

    private Map<String, String> layTatCaThamSo(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) params.put(key, values[0]);
        });
        return params;
    }

    private String layDiaChiIpThuc(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
