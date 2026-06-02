package football_field_booking.demo.exception;

import football_field_booking.demo.dto.response.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {

        // Gom tất cả lỗi validation thành Map: { "email": "...", "matKhau": "..." }
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field   = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });

        return ResponseEntity
                .badRequest()  // HTTP 400
                .body(ApiResponse.error("Dữ liệu không hợp lệ"));
    }

    @ExceptionHandler(AppException.ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            AppException.ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)  // HTTP 404
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(AppException.SanDaDatException.class)
    public ResponseEntity<ApiResponse<Void>> handleSanDaDat(
            AppException.SanDaDatException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)   // HTTP 409
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateKey(
            DataIntegrityViolationException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Sân này đã được đặt. Vui lòng chọn khung giờ khác."));
    }

    @ExceptionHandler(AppException.BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
            AppException.BadRequestException ex) {
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(AppException.EmailDaTonTaiException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailTrung(
            AppException.EmailDaTonTaiException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(AppException.ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(
            AppException.ForbiddenException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Hệ thống đang gặp sự cố. Vui lòng thử lại sau."));
    }
}
