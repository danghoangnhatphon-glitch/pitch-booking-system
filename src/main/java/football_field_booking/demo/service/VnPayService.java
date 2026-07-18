package football_field_booking.demo.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Tích hợp cổng thanh toán VNPay (môi trường sandbox).
 * Tài liệu: https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.html
 */
@Service
@Slf4j
public class VnPayService {

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.pay-url}")
    private String payUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    /**
     * Trim() phòng lỗi rất hay gặp: copy-paste TmnCode/HashSecret từ email dính
     * khoảng trắng hoặc xuống dòng thừa ở đầu/cuối, khiến chữ ký luôn sai mà
     * không có dấu hiệu gì rõ ràng để nhận biết.
     */
    @PostConstruct
    private void chuanHoaCauHinh() {
        if (tmnCode != null) tmnCode = tmnCode.trim();
        if (hashSecret != null) hashSecret = hashSecret.trim();
        if (payUrl != null) payUrl = payUrl.trim();
        if (returnUrl != null) returnUrl = returnUrl.trim();

        log.info("VNPay config: tmnCode='{}' (dài {} ký tự), hashSecret dài {} ký tự, payUrl={}",
                tmnCode, tmnCode == null ? 0 : tmnCode.length(),
                hashSecret == null ? 0 : hashSecret.length(), payUrl);

        if ("CHANGE_ME".equals(tmnCode) || "CHANGE_ME".equals(hashSecret)) {
            log.warn("⚠️  VNPay CHƯA được cấu hình (còn giá trị mặc định CHANGE_ME) — thanh toán VNPay sẽ luôn thất bại cho tới khi bạn điền vnpay.tmn-code và vnpay.hash-secret trong application.properties");
        }
    }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Tạo URL thanh toán VNPay cho 1 phiếu đặt sân.
     *
     * @param phieuId    id phiếu đặt sân — dùng làm 1 phần mã giao dịch
     * @param soTienVnd  số tiền cần thanh toán, đơn vị VNĐ (chưa nhân 100)
     * @param diaChiIp   IP của khách hàng (VNPay yêu cầu bắt buộc)
     */
    public String taoUrlThanhToan(Long phieuId, long soTienVnd, String diaChiIp) {

        String txnRef = phieuId + "_" + System.currentTimeMillis();
        String createDate = LocalDateTime.now().format(FMT);
        String expireDate = LocalDateTime.now().plusMinutes(15).format(FMT);

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(soTienVnd * 100));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", "Thanh toan coc san bong - Phieu " + phieuId);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", (diaChiIp == null || diaChiIp.isBlank()) ? "127.0.0.1" : diaChiIp);
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);

        String query = xayDungQueryVaChuKy(params);
        log.info("VNPay tạo giao dịch cho phiếu #{}: txnRef={}, amount={}",
                phieuId, txnRef, soTienVnd * 100);
        log.info("VNPay full payment URL: {}?{}", payUrl, query);

        return payUrl + "?" + query;
    }

    /**
     * Kiểm tra tính hợp lệ chữ ký của dữ liệu VNPay trả về (return URL hoặc IPN).
     */
    public boolean kiemTraChuKy(Map<String, String> vnpParams) {
        String receivedHash = vnpParams.get("vnp_SecureHash");
        if (receivedHash == null) return false;

        Map<String, String> filtered = new TreeMap<>(vnpParams);
        filtered.remove("vnp_SecureHash");
        filtered.remove("vnp_SecureHashType");

        String hashData = xayDungHashData(filtered);
        String calculatedHash = hmacSha512(hashSecret, hashData);
        boolean hopLe = calculatedHash.equalsIgnoreCase(receivedHash);

        if (!hopLe) {
            log.warn("VNPay chữ ký không khớp! hashData='{}', hash tính được='{}', hash VNPay gửi='{}'",
                    hashData, calculatedHash, receivedHash);
        }
        return hopLe;
    }

    public boolean laThanhCong(Map<String, String> vnpParams) {
        return "00".equals(vnpParams.get("vnp_ResponseCode"))
                && "00".equals(vnpParams.get("vnp_TransactionStatus"));
    }

    /** Trích id phiếu đặt sân ra từ vnp_TxnRef (định dạng {phieuId}_{timestamp}) */
    public Long trichPhieuIdTuTxnRef(String txnRef) {
        if (txnRef == null || !txnRef.contains("_")) return null;
        try {
            return Long.parseLong(txnRef.substring(0, txnRef.indexOf('_')));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────

    private String xayDungQueryVaChuKy(Map<String, String> params) {
        TreeMap<String, String> sorted = new TreeMap<>(params);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            String value = entry.getValue();
            if (value == null || value.isEmpty()) continue;

            String encodedValue = URLEncoder.encode(value, StandardCharsets.US_ASCII);

            // hashData: KHÔNG encode key, chỉ encode value — đúng chuẩn thuật toán VNPay
            if (hashData.length() > 0) hashData.append('&');
            hashData.append(entry.getKey()).append('=').append(encodedValue);

            // query string thực tế thì encode cả key lẫn value
            if (query.length() > 0) query.append('&');
            query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII))
                    .append('=')
                    .append(encodedValue);
        }

        String secureHash = hmacSha512(hashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);
        return query.toString();
    }

    /** Chuỗi hashData theo đúng chuẩn VNPay: KHÔNG encode key, chỉ encode value */
    private String xayDungHashData(Map<String, String> sortedParams) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) continue;
            if (sb.length() > 0) sb.append('&');
            sb.append(entry.getKey())
                    .append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
        }
        return sb.toString();
    }

    private String hmacSha512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            hmac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi tạo chữ ký VNPay: " + e.getMessage(), e);
        }
    }
}
