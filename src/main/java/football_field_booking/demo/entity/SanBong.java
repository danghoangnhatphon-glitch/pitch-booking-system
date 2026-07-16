package football_field_booking.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "san_bong",
        indexes = {
                @Index(name = "idx_san_quanhuyen", columnList = "quan_huyen"),
                @Index(name = "idx_san_loaisan",   columnList = "loai_san"),
                @Index(name = "idx_san_trangthai", columnList = "trang_thai")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanBong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_san", nullable = false, length = 100)
    private String tenSan;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_san", nullable = false, length = 10)
    private LoaiSan loaiSan;

    @Column(name = "vi_tri", length = 300)
    private String viTri;

    @Column(name = "quan_huyen", length = 100)
    private String quanHuyen;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "anh_san", length = 500)
    private String anhSan;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 20)
    @Builder.Default
    private TrangThai trangThai = TrangThai.HOAT_DONG;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chu_san_id", nullable = false)
    private NguoiDung chuSan;

    @OneToMany(mappedBy = "sanBong", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GiaSan> danhSachGia;

    public enum LoaiSan {
        SAN_5, SAN_7, SAN_11
    }

    public enum TrangThai {
        HOAT_DONG,
        DONG_CUA,
        BAO_TRI
    }
}