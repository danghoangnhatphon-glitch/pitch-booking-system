package football_field_booking.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

/**
 * Map với bảng khung_gio
 *
 * Đây là bảng TEMPLATE — định nghĩa các ca cố định (06:00-07:30, 07:30-09:00...)
 * Dùng lại cho mọi ngày, không tạo lại theo ngày
 */
@Entity
@Table(name = "khung_gio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhungGio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // LocalTime map tự nhiên với kiểu TIME của SQL Server
    @Column(name = "gio_bat_dau", nullable = false)
    private LocalTime gioBatDau;

    @Column(name = "gio_ket_thuc", nullable = false)
    private LocalTime gioKetThuc;

    // boolean → BIT trong SQL Server (Hibernate tự xử lý)
    @Column(name = "la_gio_cao_diem", nullable = false)
    private boolean laGioCaoDiem = false;

    // ================================================================
    // Quan hệ: 1 khung giờ có thể có nhiều mức giá (cho các sân khác nhau)
    // ================================================================
    @OneToMany(mappedBy = "khungGio", fetch = FetchType.LAZY)
    private List<GiaSan> danhSachGia;

    // ================================================================
    // Helper method — tiện dùng trong code
    // VD: khungGio.getMoTa() → "17:30 - 19:00 (Cao điểm)"
    // ================================================================
    public String getMoTa() {
        String label = String.format("%s - %s", gioBatDau, gioKetThuc);
        return laGioCaoDiem ? label + " ⭐ Cao điểm" : label;
    }
}
