package football_field_booking.demo.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Trạng thái 1 khung giờ của 1 sân trong 1 ngày
 *
 * Đây là DTO quan trọng nhất với UI:
 * Client nhận danh sách này → vẽ grid chọn giờ như chọn ghế rạp phim
 *
 * Ví dụ response:
 * [
 *   { khungGioId: 5, gioBatDau: "17:30", gioKetThuc: "19:00",
 *     donGia: 250000, laGioCaoDiem: true, conTrong: true  },  ← có thể chọn
 *   { khungGioId: 6, gioBatDau: "19:00", gioKetThuc: "20:30",
 *     donGia: 280000, laGioCaoDiem: true, conTrong: false },  ← đã bị đặt
 * ]
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhungGioTrangThaiResponse {

    private Long khungGioId;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;
    private boolean laGioCaoDiem;
    private BigDecimal donGia;
    private boolean conTrong;   // true = chưa ai đặt, false = đã bị đặt

    // Label hiển thị trên UI: "17:30 - 19:00"
    public String getLabel() {
        return gioBatDau + " - " + gioKetThuc;
    }
}
