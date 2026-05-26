package football_field_booking.demo.config;

import football_field_booking.demo.service.NguoiDungService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Cấu hình Spring Security
 *
 * @EnableWebSecurity       → bật Spring Security
 * @EnableMethodSecurity    → bật @PreAuthorize trong Controller
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final NguoiDungService   nguoiDungService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtAuthFilter      jwtAuthFilter;

    // ================================================================
    // Quy tắc bảo vệ URL
    // ================================================================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Tắt CSRF — vì dùng JWT (stateless), không cần CSRF token
            .csrf(AbstractHttpConfigurer::disable)

            // Không dùng Session — JWT tự mang thông tin người dùng
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Quy tắc phân quyền URL
            .authorizeHttpRequests(auth -> auth

                // ── PUBLIC — ai cũng vào được ──────────────────────
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/san-bong").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/san-bong/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/san-bong/tim-kiem").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/san-bong/{id}/lich").permitAll()

                // ── KHACH_HANG ──────────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/api/dat-san").hasRole("KHACH_HANG")
                .requestMatchers(HttpMethod.DELETE, "/api/dat-san/*/huy").hasRole("KHACH_HANG")

                // ── CHU_SAN ─────────────────────────────────────────
                .requestMatchers("/api/san-bong/cua-toi").hasRole("CHU_SAN")
                .requestMatchers("/api/dat-san/cho-duyet").hasRole("CHU_SAN")
                .requestMatchers("/api/thong-ke/**").hasAnyRole("CHU_SAN", "ADMIN")

                // ── Còn lại phải đăng nhập ──────────────────────────
                .anyRequest().authenticated()
            )

            // Cắm JWT filter vào trước filter mặc định của Spring
            // → mỗi request đến, JwtAuthFilter chạy trước để đọc token
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ================================================================
    // AuthenticationProvider — dùng email + BCrypt để xác thực
    // ================================================================
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(nguoiDungService);  // load user bằng email
        provider.setPasswordEncoder(passwordEncoder);       // so sánh BCrypt hash
        return provider;
    }

    // ================================================================
    // AuthenticationManager — dùng trong AuthService để login thủ công
    // ================================================================
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
