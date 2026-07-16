package football_field_booking.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

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

    @Column(name = "gio_bat_dau", nullable = false)
    private LocalTime gioBatDau;

    @Column(name = "gio_ket_thuc", nullable = false)
    private LocalTime gioKetThuc;


    @Column(name = "la_gio_cao_diem", nullable = false)
    @Builder.Default
    private boolean laGioCaoDiem = false;

    @OneToMany(mappedBy = "khungGio", fetch = FetchType.LAZY)
    private List<GiaSan> danhSachGia;

    public String getMoTa() {
        String label = String.format("%s - %s", gioBatDau, gioKetThuc);
        return laGioCaoDiem ? label + " ⭐ Cao điểm" : label;
    }
}
