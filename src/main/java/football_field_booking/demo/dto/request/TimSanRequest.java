package football_field_booking.demo.dto.request;

import football_field_booking.demo.entity.SanBong;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimSanRequest {

    private String quanHuyen;
    private SanBong.LoaiSan loaiSan;
    private LocalDate ngay;
    private Long khungGioId;
}
