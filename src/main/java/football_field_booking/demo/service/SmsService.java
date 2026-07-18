package football_field_booking.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Map;

/**
 * Gửi SMS qua SpeedSMS (https://speedsms.vn/). Đăng ký tài khoản miễn phí để lấy
 * Access Token, điền vào application.properties (sms.speedsms.access-token).
 */
@Service
@Slf4j
public class SmsService {

    @Value("${sms.speedsms.access-token}")
    private String accessToken;

    @Value("${sms.speedsms.api-url}")
    private String apiUrl;

    @Value("${sms.speedsms.sender:}")
    private String sender;

    @Value("${sms.speedsms.type:2}")
    private int smsType;

    private final RestClient restClient = RestClient.create();

    /**
     * Gửi SMS chứa mã OTP tới số điện thoại. Nếu chưa cấu hình access-token
     * (giá trị mặc định CHANGE_ME), chỉ log ra console để tiện demo/test mà
     * không cần tài khoản SMS thật.
     */
    public void guiOtp(String soDienThoai, String maOtp) {
        String noiDung = "Ma xac thuc SanBong.vn cua ban la: " + maOtp + ". Ma co hieu luc trong 5 phut.";

        if (accessToken == null || accessToken.isBlank() || accessToken.equals("CHANGE_ME")) {
            log.warn("⚠️  [DEMO MODE] Chưa cấu hình SpeedSMS access-token — KHÔNG gửi SMS thật.");
            log.warn("⚠️  [DEMO MODE] Mã OTP cho {} là: {}", soDienThoai, maOtp);
            return;
        }

        try {
            String basicAuth = "Basic " + Base64.getEncoder()
                    .encodeToString((accessToken + ":x").getBytes());

            // Theo hướng dẫn trực tiếp từ SpeedSMS: field JSON là "sms_type" + "sender",
            // số điện thoại cần mã vùng 84 (không giữ số 0 đầu).
            Map<String, Object> body = Map.of(
                    "to", new String[]{chuanHoaSoDienThoai(soDienThoai)},
                    "content", noiDung,
                    "sms_type", 4,         // Chuyển sang số 4 (Brandname mặc định)
                    "sender", "Verify"     // Điền chữ "Verify" hoặc "Notify"
            );

            ResponseEntity<String> response = restClient.post()
                    .uri(apiUrl)
                    .header(HttpHeaders.AUTHORIZATION, basicAuth)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toEntity(String.class);

            log.info("Đã gửi OTP tới {} — phản hồi SpeedSMS: {}", soDienThoai, response.getBody());
        } catch (Exception e) {
            // Không throw lỗi ra ngoài để tránh lộ chi tiết hạ tầng SMS cho người dùng cuối,
            // nhưng vẫn log đầy đủ để debug. Người dùng vẫn có thể "Gửi lại mã" nếu cần.
            log.error("Lỗi gửi SMS OTP tới {}: {}", soDienThoai, e.getMessage());
        }
    }

    /** SpeedSMS yêu cầu định dạng 84xxxxxxxxx thay vì 0xxxxxxxxx */
    private String chuanHoaSoDienThoai(String soDienThoai) {
        if (soDienThoai.startsWith("0")) {
            return "84" + soDienThoai.substring(1);
        }
        return soDienThoai;
    }
}