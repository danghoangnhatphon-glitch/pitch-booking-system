package football_field_booking.demo.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;


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
    private boolean conTrong;

    public String getLabel() {
        return gioBatDau + " - " + gioKetThuc;
    }
}
