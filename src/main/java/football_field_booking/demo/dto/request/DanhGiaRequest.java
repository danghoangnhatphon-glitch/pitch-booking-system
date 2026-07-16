package football_field_booking.demo.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DanhGiaRequest {

    @NotNull(message = "Thiếu mã phiếu đặt")
    private Long phieuDatId;

    @NotNull(message = "Thiếu mã sân")
    private Long sanId;

    @NotNull(message = "Vui lòng chọn số sao")
    @Min(value = 1, message = "Số sao tối thiểu là 1")
    @Max(value = 5, message = "Số sao tối đa là 5")
    private Integer soSao;

    @NotBlank(message = "Vui lòng nhập nhận xét")
    private String noiDung;
}