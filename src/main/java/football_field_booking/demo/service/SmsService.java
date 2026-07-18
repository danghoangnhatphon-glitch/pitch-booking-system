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

    @Value("${sms.speedsms.type:4}")
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

            // sms_type=4: gửi bằng brandname MẶC ĐỊNH của SpeedSMS dành riêng cho Verify/Notify
            // (KHÔNG cần đăng ký brandname riêng). Cố tình KHÔNG đưa key "sender" vào JSON —
            // gửi sender="" (rỗng) bị SpeedSMS hiểu nhầm thành "có sender nhưng không tìm thấy".
            Map<String, Object> body = Map.of(
                    "to", new String[]{chuanHoaSoDienThoai(soDienThoai)},
                    "content", noiDung,
                    "sms_type", smsType
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

    /** SpeedSMS yêu cầu số điện thoại không có số 0 đầu, thêm mã vùng 84 */
    private String chuanHoaSoDienThoai(String soDienThoai) {
        if (soDienThoai.startsWith("0")) {
            return "84" + soDienThoai.substring(1);
        }
        return soDienThoai;
    }
}
