package football_field_booking.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Map với bảng phieu_dat_san
 *
 * Đây là HEADER của đơn đặt sân.
 * 1 phiếu có thể chứa nhiều chi tiết (đặt nhiều ca cùng lúc).
 *
 * Ví dụ thực tế:
 *   Phiếu #001 — Trần Văn A — ngày 01/06
 *     ├── Chi tiết 1: Sân 5 số 1, 17:30-19:00 → 280,000đ
 *     └── Chi tiết 2: Sân 5 số 1, 19:00-20:30 → 280,000đ
 *     Tổng tiền: 560,000đ
 */
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

    // LocalDate map với DATE trong SQL Server
    @Column(name = "ngay_tao", nullable = false)
    private LocalDate ngayTao;

    @Column(name = "tong_tien", nullable = false, precision = 12, scale = 0)
    private BigDecimal tongTien;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 20)
    private TrangThai trangThai = TrangThai.CHO_DUYET;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai_thanh_toan", nullable = false, length = 20)
    private TrangThaiThanhToan trangThaiThanhToan = TrangThaiThanhToan.CHUA_THANH_TOAN;

    @Enumerated(EnumType.STRING)
    @Column(name = "phuong_thuc_thanh_toan", length = 30)
    private PhuongThucThanhToan phuongThucThanhToan;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.ngayTao   = LocalDate.now();
        this.createdAt = LocalDateTime.now();
    }

    // ================================================================
    // Quan hệ: nhiều phiếu thuộc về 1 người đặt
    // ================================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_dat_id", nullable = false)
    private NguoiDung nguoiDat;

    // ================================================================
    // Quan hệ: 1 phiếu có nhiều dòng chi tiết
    // CascadeType.ALL → lưu phiếu thì tự lưu luôn chi tiết
    // orphanRemoval → xóa chi tiết khỏi list thì tự xóa trong DB
    // ================================================================
    @OneToMany(mappedBy = "phieuDat",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    private List<ChiTietDatSan> danhSachChiTiet;

    // ================================================================
    // Quan hệ: 1 phiếu có 1 đánh giá
    // ================================================================
    @OneToOne(mappedBy = "phieuDat", fetch = FetchType.LAZY)
    private DanhGia danhGia;

    // ================================================================
    // Enums
    // ================================================================
    public enum TrangThai {
        CHO_DUYET,
        DA_DUYET,
        DA_HUY
    }

    public enum TrangThaiThanhToan {
        CHUA_THANH_TOAN,
        DA_THANH_TOAN,
        HOAN_TIEN
    }

    public enum PhuongThucThanhToan {
        TIEN_MAT,
        CHUYEN_KHOAN,
        MOMO,
        VNPAY
    }
}
