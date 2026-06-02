package football_field_booking.demo.exception;


public class AppException {


    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    public static class SanDaDatException extends RuntimeException {
        public SanDaDatException(String message) {
            super(message);
        }
    }


    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) {
            super(message);
        }
    }


    public static class EmailDaTonTaiException extends RuntimeException {
        public EmailDaTonTaiException(String email) {
            super("Email '" + email + "' đã được sử dụng");
        }
    }

    public static class ForbiddenException extends RuntimeException {
        public ForbiddenException(String message) {
            super(message);
        }
    }
}
