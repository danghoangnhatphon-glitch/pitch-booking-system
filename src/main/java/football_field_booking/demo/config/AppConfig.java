package football_field_booking.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Config cơ bản — khai báo các Bean dùng chung toàn project
 * Security đầy đủ sẽ setup ở SecurityConfig sau
 */
@Configuration
public class AppConfig {

    /**
     * BCryptPasswordEncoder — dùng để:
     * 1. Hash mật khẩu khi đăng ký:  encoder.encode("123456")
     * 2. Kiểm tra mật khẩu khi login: encoder.matches("123456", hashInDb)
     *
     * BCrypt tự động thêm "salt" ngẫu nhiên vào mỗi lần hash
     * → cùng 1 mật khẩu, hash 2 lần ra 2 chuỗi khác nhau
     * → không thể dùng rainbow table để crack
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
