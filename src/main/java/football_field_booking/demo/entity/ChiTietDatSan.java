package football_field_booking.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "chi_tiet_dat_san",
       indexes = {
           @Index(name = "idx_chitiet_ngay",     columnList = "ngay_su_dung"),
           @Index(name = "idx_chitiet_san_ngay",  columnList = "san_id, ngay_su_dung")
       },
       uniqueConstraints = {
           @UniqueConstraint(
               name = "uq_san_khunggio_ngay",
               columnNames = {"san_id", "khung_gio_id", "ngay_su_dung"}
           )
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietDatSan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ngay_su_dung", nullable = false)
    private LocalDate ngaySuDung;
    @Column(name = "don_gia", nullable = false, precision = 12, scale = 0)
    private BigDecimal donGia;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_dat_id", nullable = false)
    private PhieuDatSan phieuDat;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "san_id", nullable = false)
    private SanBong sanBong;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khung_gio_id", nullable = false)
    private KhungGio khungGio;
}
