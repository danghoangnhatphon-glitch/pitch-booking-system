package football_field_booking.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "phieu_dat_san",
       indexes = {
           @Index(name = "idx_phieu_nguoidat", columnList = "nguoi_dat_id"),
           @Index(name = "idx_phieu_ngaytao",  columnList = "ngay_tao")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuDatSan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ngay_tao", nullable = false)
    private LocalDate ngayTao;

    @Column(name = "tong_tien", nullable = false, precision = 12, scale = 0)
    private BigDecimal tongTien;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 20)
    @Builder.Default
    private TrangThai trangThai = TrangThai.CHO_DUYET;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai_thanh_toan", nullable = false, length = 20)
    @Builder.Default
    private TrangThaiThanhToan trangThaiThanhToan = TrangThaiThanhToan.CHUA_THANH_TOAN;

    @Enumerated(EnumType.STRING)
    @Column(name = "phuong_thuc_thanh_toan", length = 30)
    private PhuongThucThanhToan phuongThucThanhToan;


    @Column(name = "tien_coc", precision = 12, scale = 0)
    private BigDecimal tienCoc;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai_coc", length = 20)
    @Builder.Default
    private TrangThaiCoc trangThaiCoc = TrangThaiCoc.CHUA_COC;

    @Column(name = "thoi_gian_coc")
    private LocalDateTime thoiGianCoc;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.ngayTao   = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        if (this.tongTien != null) {
            this.tienCoc = this.tongTien
                .multiply(new java.math.BigDecimal("0.3"))
                .setScale(0, java.math.RoundingMode.CEILING);
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_dat_id", nullable = false)
    private NguoiDung nguoiDat;

    @OneToMany(mappedBy = "phieuDat",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    private List<ChiTietDatSan> danhSachChiTiet;

    @OneToOne(mappedBy = "phieuDat", fetch = FetchType.LAZY)
    private DanhGia danhGia;

    public enum TrangThai { CHO_DUYET, DA_DUYET, DA_HUY }

    public enum TrangThaiThanhToan { CHUA_THANH_TOAN, DA_THANH_TOAN, HOAN_TIEN }

    public enum PhuongThucThanhToan { TIEN_MAT, CHUYEN_KHOAN, MOMO, VNPAY }

    public enum TrangThaiCoc { CHUA_COC, DA_COC, HOAN_COC }
}
