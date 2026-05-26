package football_field_booking.demo.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Xử lý JWT — tạo token, đọc token, kiểm tra hết hạn
 *
 * JWT gồm 3 phần: Header.Payload.Signature
 * Payload chứa: email (subject), ngày tạo, ngày hết hạn, role
 */
@Service
public class JwtService {

    // Đọc từ application.properties — không hardcode secret vào code
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;  // milliseconds, VD: 86400000 = 24 giờ

    // ================================================================
    // Tạo token từ thông tin người dùng
    // ================================================================
    public String taoToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Nhúng role vào token — client có thể đọc để hiển thị menu đúng role
        claims.put("role", userDetails.getAuthorities()
                .iterator().next().getAuthority());
        return taoToken(claims, userDetails);
    }

    private String taoToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())  // email
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ================================================================
    // Đọc email từ token
    // ================================================================
    public String layEmail(String token) {
        return layClaim(token, Claims::getSubject);
    }

    // ================================================================
    // Kiểm tra token còn hợp lệ không
    // ================================================================
    public boolean kiemTraToken(String token, UserDetails userDetails) {
        final String email = layEmail(token);
        return email.equals(userDetails.getUsername()) && !hetHan(token);
    }

    // ================================================================
    // Các method helper
    // ================================================================
    private boolean hetHan(String token) {
        return layClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T layClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = layTatCaClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims layTatCaClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
