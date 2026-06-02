package football_field_booking.demo.config;

import football_field_booking.demo.entity.NguoiDung;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        NguoiDung nguoiDung = (NguoiDung) authentication.getPrincipal();

        String redirectUrl = switch (nguoiDung.getVaiTro()) {
            case ADMIN    -> "/admin/dashboard";
            case CHU_SAN  -> "/chusan/dashboard";
            default       -> "/";
        };

        response.sendRedirect(redirectUrl);
    }
}
