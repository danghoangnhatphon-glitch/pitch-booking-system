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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final NguoiDungService          nguoiDungService;
    private final BCryptPasswordEncoder     passwordEncoder;
    private final JwtAuthFilter             jwtAuthFilter;
    private final CustomLoginSuccessHandler loginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/chusan/**", "/admin/**"))

            .authorizeHttpRequests(auth -> auth
                // Static
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                // Public pages
                .requestMatchers("/", "/san-bong", "/san-bong/**", "/auth/**").permitAll()
                // Public API
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/san-bong/**").permitAll()
                // Admin
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Chủ sân
                .requestMatchers("/chusan/**").hasRole("CHU_SAN")
                // Khách hàng
                .requestMatchers("/dat-san/**").hasRole("KHACH_HANG")
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/auth/dang-nhap")
                .loginProcessingUrl("/auth/login")
                .usernameParameter("username")
                .passwordParameter("password")
                // Dùng handler tùy chỉnh → redirect đúng trang theo role
                .successHandler(loginSuccessHandler)
                .failureUrl("/auth/dang-nhap?error")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout"))
                .logoutSuccessUrl("/auth/dang-nhap?logout")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )

            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(nguoiDungService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
