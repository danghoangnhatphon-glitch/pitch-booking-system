package football_field_booking.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "gia_san",
       uniqueConstraints = {
           @UniqueConstraint(
               name = "uq_san_khunggio",
               columnNames = {"san_id", "khung_gio_id"}
           )
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiaSan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "don_gia", nullable = false, precision = 12, scale = 0)
    private BigDecimal donGia;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "san_id", nullable = false)
    private SanBong sanBong;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khung_gio_id", nullable = false)
    private KhungGio khungGio;
}
