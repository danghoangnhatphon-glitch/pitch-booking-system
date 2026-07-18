package football_field_booking.demo.service;

import football_field_booking.demo.dto.request.DangKyRequest;
import football_field_booking.demo.exception.AppException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quản lý mã OTP xác thực số điện thoại khi đăng ký tài khoản.
 *
 * LƯU Ý: dùng ConcurrentHashMap trong bộ nhớ — phù hợp cho demo/đồ án chạy 1
 * instance. Ở môi trường production nhiều instance, nên thay bằng Redis (có TTL
 * tự động) để các instance dùng chung trạng thái OTP.
 */
@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final long HET_HAN_GIAY = 5 * 60; // 5 phút
    private static final int SO_LAN_THU_TOI_DA = 5;

    private final ConcurrentHashMap<String, PhienOtp> phienDangKy = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    private record PhienOtp(String maOtp, DangKyRequest thongTinDangKy, Instant hetHan, int soLanThuSai) {
        PhienOtp withSoLanThuSai(int n) {
            return new PhienOtp(maOtp, thongTinDangKy, hetHan, n);
        }
    }

    /** Tạo mã OTP mới cho 1 số điện thoại, lưu kèm thông tin đăng ký chờ xác nhận. */
    public String taoOtp(String soDienThoai, DangKyRequest thongTinDangKy) {
        String maOtp = String.format("%0" + OTP_LENGTH + "d", random.nextInt(1_000_000));
        phienDangKy.put(soDienThoai,
                new PhienOtp(maOtp, thongTinDangKy, Instant.now().plusSeconds(HET_HAN_GIAY), 0));
        return maOtp;
    }

    /**
     * Xác nhận mã OTP đúng, trả về thông tin đăng ký đã lưu để tạo tài khoản.
     * Ném lỗi nếu sai mã, hết hạn, hoặc thử sai quá số lần cho phép.
     */
    public DangKyRequest xacNhanOtp(String soDienThoai, String maOtpNhap) {
        PhienOtp phien = phienDangKy.get(soDienThoai);

        if (phien == null) {
            throw new AppException.BadRequestException(
                    "Chưa yêu cầu mã OTP cho số điện thoại này hoặc mã đã hết hạn");
        }
        if (Instant.now().isAfter(phien.hetHan())) {
            phienDangKy.remove(soDienThoai);
            throw new AppException.BadRequestException("Mã OTP đã hết hạn, vui lòng yêu cầu lại");
        }
        if (phien.soLanThuSai() >= SO_LAN_THU_TOI_DA) {
            phienDangKy.remove(soDienThoai);
            throw new AppException.BadRequestException("Bạn đã nhập sai quá nhiều lần, vui lòng yêu cầu mã mới");
        }
        if (!phien.maOtp().equals(maOtpNhap)) {
            phienDangKy.put(soDienThoai, phien.withSoLanThuSai(phien.soLanThuSai() + 1));
            throw new AppException.BadRequestException("Mã OTP không đúng");
        }

        phienDangKy.remove(soDienThoai);
        return phien.thongTinDangKy();
    }
}
