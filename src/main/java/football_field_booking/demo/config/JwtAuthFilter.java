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

/**
 * Filter chạy MỘT LẦN cho mỗi request
 * Nhiệm vụ: đọc JWT từ header → xác thực → set vào SecurityContext
 *
 * Luồng hoạt động:
 * Request đến
 *   → đọc header "Authorization: Bearer <token>"
 *   → lấy email từ token
 *   → load NguoiDung từ DB theo email
 *   → kiểm tra token hợp lệ
 *   → set vào SecurityContext (Spring biết ai đang gọi API)
 *   → cho request đi tiếp vào Controller
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService         jwtService;
    private final NguoiDungService   nguoiDungService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Đọc header Authorization
        final String authHeader = request.getHeader("Authorization");

        // Không có token hoặc sai định dạng → bỏ qua (public API)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Cắt bỏ "Bearer " (7 ký tự) để lấy token thuần
        final String jwt   = authHeader.substring(7);
        final String email = jwtService.layEmail(jwt);

        // Nếu đọc được email VÀ chưa có authentication trong context
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load NguoiDung từ DB
            UserDetails userDetails = nguoiDungService.loadUserByUsername(email);

            // Kiểm tra token còn hợp lệ không
            if (jwtService.kiemTraToken(jwt, userDetails)) {

                // Tạo authentication object và đặt vào SecurityContext
                // → từ đây @AuthenticationPrincipal trong Controller mới hoạt động
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Cho request đi tiếp
        filterChain.doFilter(request, response);
    }
}
