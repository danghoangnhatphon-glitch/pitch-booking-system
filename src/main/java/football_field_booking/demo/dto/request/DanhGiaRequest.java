package football_field_booking.demo.dto.request;

import lombok.Data;

@Data
public class DanhGiaRequest {
    private Long phieuDatId;
    private Long sanId;
    private Integer soSao;
    private String noiDung;
}