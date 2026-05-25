package football_field_booking.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Map với bảng danh_gia
 * Cho phép khách hàng đánh giá sân sau khi đã sử dụng
 */
@Entity
@Table(name = "danh_gia",
       uniqueConstraints = {
           // Mỗi phiếu chỉ được đánh giá 1 lần
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

    // 1-5 sao
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

    // ================================================================
    // Quan hệ: 1 đánh giá thuộc về 1 phiếu (OneToOne)
    // ================================================================
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_dat_id", nullable = false)
    private PhieuDatSan phieuDat;

    // ================================================================
    // Quan hệ: nhiều đánh giá của 1 người dùng
    // ================================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_danh_gia", nullable = false)
    private NguoiDung nguoiDanhGia;

    // ================================================================
    // Quan hệ: đánh giá thuộc về 1 sân
    // (để tính điểm trung bình sân dễ hơn)
    // ================================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "san_id", nullable = false)
    private SanBong sanBong;
}
