package football_field_booking.demo.exception;

/**
 * Tập trung toàn bộ custom exception ở đây
 *
 * Tại sao cần custom exception thay vì dùng RuntimeException thẳng?
 * → Để GlobalExceptionHandler phân biệt được từng loại lỗi
 *   và trả về HTTP status code phù hợp cho client.
 *
 * VD:
 *   ResourceNotFoundException  → HTTP 404
 *   SanDaDatException          → HTTP 409 Conflict
 *   BadRequestException        → HTTP 400
 */
public class AppException {

    // ================================================================
    // 404 — Không tìm thấy tài nguyên
    // ================================================================
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    // ================================================================
    // 409 — Xung đột dữ liệu (trùng lịch đặt sân)
    // ================================================================
    public static class SanDaDatException extends RuntimeException {
        public SanDaDatException(String message) {
            super(message);
        }
    }

    // ================================================================
    // 400 — Dữ liệu đầu vào không hợp lệ
    // ================================================================
    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) {
            super(message);
        }
    }

    // ================================================================
    // 409 — Email đã tồn tại khi đăng ký
    // ================================================================
    public static class EmailDaTonTaiException extends RuntimeException {
        public EmailDaTonTaiException(String email) {
            super("Email '" + email + "' đã được sử dụng");
        }
    }

    // ================================================================
    // 403 — Không có quyền thực hiện hành động này
    // ================================================================
    public static class ForbiddenException extends RuntimeException {
        public ForbiddenException(String message) {
            super(message);
        }
    }
}
