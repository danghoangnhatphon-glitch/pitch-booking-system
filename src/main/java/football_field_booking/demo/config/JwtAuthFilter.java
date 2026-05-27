package football_field_booking.demo.config;

import football_field_booking.demo.service.NguoiDungService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService       jwtService;
    private final NguoiDungService nguoiDungService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // ── Không có header hoặc không đúng định dạng → bỏ qua
        // Session login (Thymeleaf form) không có header này → đi thẳng qua
        if (authHeader == null
                || !authHeader.startsWith("Bearer ")
                || authHeader.trim().equals("Bearer")
                || authHeader.trim().equals("Bearer null")
                || authHeader.length() <= 7) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7).trim();

        // Kiểm tra token có đúng 2 dấu chấm không (cấu trúc JWT: header.payload.signature)
        if (jwt.isEmpty() || jwt.equals("null") || countDots(jwt) != 2) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String email = jwtService.layEmail(jwt);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = nguoiDungService.loadUserByUsername(email);

                if (jwtService.kiemTraToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token lỗi → bỏ qua, để Security xử lý tiếp theo
            // Không throw exception để không phá request của session login
            logger.warn("JWT validation failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private int countDots(String s) {
        int count = 0;
        for (char c : s.toCharArray()) {
            if (c == '.') count++;
        }
        return count;
    }
}
