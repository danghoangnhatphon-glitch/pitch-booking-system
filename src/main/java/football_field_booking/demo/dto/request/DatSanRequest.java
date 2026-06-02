package football_field_booking.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatSanRequest {

    @NotNull(message = "Vui lòng chọn sân")
    private Long sanId;

    @NotNull(message = "Vui lòng chọn ngày")
    @FutureOrPresent(message = "Không thể đặt sân ngày đã qua")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngaySuDung;

    @NotEmpty(message = "Vui lòng chọn ít nhất 1 khung giờ")
    @Size(max = 4, message = "Tối đa 4 khung giờ mỗi lần đặt")
    private List<Long> danhSachKhungGioId;

    private String ghiChu;

    private String phuongThucThanhToan;
}
