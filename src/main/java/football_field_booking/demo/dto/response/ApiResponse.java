package football_field_booking.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * Wrapper chuẩn cho MỌI response của API
 *
 * Thay vì mỗi API trả về format khác nhau, tất cả đều có dạng:
 * {
 *   "success": true,
 *   "message": "Đặt sân thành công",
 *   "data": { ... }       ← null nếu không có data
 * }
 *
 * Hoặc khi lỗi:
 * {
 *   "success": false,
 *   "message": "Sân đã được đặt trong khung giờ này",
 *   "data": null
 * }
 *
 * @JsonInclude(NON_NULL) → không trả field null về client (response gọn hơn)
 *
 * Generic type <T> → data có thể là bất kỳ kiểu nào:
 *   ApiResponse<PhieuDatSanResponse>
 *   ApiResponse<List<SanBongResponse>>
 *   ApiResponse<Void>  (khi không cần trả data)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    // ================================================================
    // Factory methods — dùng trong Controller cho gọn
    // ================================================================

    // Thành công có data
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    // Thành công có data + message
    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    // Thành công không có data (VD: xóa, duyệt)
    public static <T> ApiResponse<T> ok(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    // Lỗi
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
