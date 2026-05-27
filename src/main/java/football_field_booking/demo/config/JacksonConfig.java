package football_field_booking.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Jackson để xử lý LocalDate, LocalTime, LocalDateTime
 * trong JSON mà không bị lỗi
 *
 * Không có config này:
 *   LocalDate "2025-06-01" → Jackson không biết parse → lỗi 400
 *
 * Có config này:
 *   "2025-06-01"  ↔  LocalDate  ✓
 *   "17:30:00"    ↔  LocalTime  ✓
 *   "2025-06-01T17:30:00" ↔  LocalDateTime  ✓
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Đăng ký module xử lý Java 8 Date/Time
        mapper.registerModule(new JavaTimeModule());

        // Không serialize LocalDate thành timestamp số
        // → serialize thành "2025-06-01" (dạng string đọc được)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}
