package football_field_booking.demo.dto.request;

import lombok.Data;

@Data
public class SanBongRequest {
    private Long id;
    private String tenSan;
    private String loaiSan;
    private String quanHuyen;
    private String viTri;
    private String moTa;
    private String anhSan;
    private Long chuSanId;
}