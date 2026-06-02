package football_field_booking.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "danh_gia",
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_danhgia_phieu", columnNames = {"phieu_dat_id"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DanhGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "so_sao", nullable = false)
    private Byte soSao;

    @Column(name = "noi_dung", length = 1000)
    private String noiDung;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_dat_id", nullable = false)
    private PhieuDatSan phieuDat;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_danh_gia", nullable = false)
    private NguoiDung nguoiDanhGia;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "san_id", nullable = false)
    private SanBong sanBong;
}
