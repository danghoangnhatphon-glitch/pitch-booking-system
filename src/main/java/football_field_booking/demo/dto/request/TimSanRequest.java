package football_field_booking.demo.dto.request;

import football_field_booking.demo.entity.SanBong;
import lombok.*;

import java.time.LocalDate;

/**
 * Bộ lọc tìm kiếm sân
 * Tất cả field đều optional — không chọn thì bỏ qua
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimSanRequest {

    private String quanHuyen;           // Lọc theo khu vực
    private SanBong.LoaiSan loaiSan;    // Lọc theo loại sân (5, 7, 11)
    private LocalDate ngay;             // Lọc theo ngày còn trống
    private Long khungGioId;            // Lọc theo khung giờ còn trống
}
