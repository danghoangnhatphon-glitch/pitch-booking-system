package football_field_booking.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Map với bảng gia_san
 *
 * Bảng này lưu GIÁ HIỆN TẠI của từng sân theo từng khung giờ.
 * Lưu ý: khi đặt sân, giá sẽ được COPY sang chi_tiet_dat_san.don_gia
 * để giữ lịch sử giá — nếu chủ sân đổi giá sau, đơn cũ không bị ảnh hưởng.
 */
@Entity
@Table(name = "gia_san",
       // Đảm bảo mỗi sân + khung giờ chỉ có 1 mức giá
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

    // Dùng BigDecimal cho tiền — KHÔNG dùng double/float vì lỗi làm tròn
    // DECIMAL(12,0) → không có phần thập phân (VNĐ không dùng xu)
    @Column(name = "don_gia", nullable = false, precision = 12, scale = 0)
    private BigDecimal donGia;

    // ================================================================
    // Quan hệ: nhiều mức giá thuộc về 1 sân
    // ================================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "san_id", nullable = false)
    private SanBong sanBong;

    // ================================================================
    // Quan hệ: nhiều mức giá thuộc về 1 khung giờ
    // ================================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khung_gio_id", nullable = false)
    private KhungGio khungGio;
}
