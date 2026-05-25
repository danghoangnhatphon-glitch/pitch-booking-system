package football_field_booking.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Map với bảng chi_tiet_dat_san
 *
 * ĐÂY LÀ BẢNG QUAN TRỌNG NHẤT trong hệ thống.
 * Mỗi row = 1 ca đặt sân cụ thể.
 *
 * UNIQUE CONSTRAINT (san_id, khung_gio_id, ngay_su_dung)
 * → chính là "vũ khí" chống trùng lịch ở tầng Database
 *
 * Khi 2 người cùng đặt sân 1, ca 17:30, ngày 01/06:
 *   - Người A INSERT trước → thành công
 *   - Người B INSERT sau → DB ném DataIntegrityViolationException
 *   - Spring bắt exception → trả lỗi "Sân đã được đặt"
 */
@Entity
@Table(name = "chi_tiet_dat_san",
       indexes = {
           @Index(name = "idx_chitiet_ngay",     columnList = "ngay_su_dung"),
           @Index(name = "idx_chitiet_san_ngay",  columnList = "san_id, ngay_su_dung")
       },
       uniqueConstraints = {
           // ✅ CONSTRAINT CHỐNG TRÙNG LỊCH — không được xóa cái này
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

    // Ngày thực tế đá bóng (khác với ngày_tao của phiếu)
    // Ví dụ: đặt ngày hôm nay nhưng ngày sử dụng là tuần sau
    @Column(name = "ngay_su_dung", nullable = false)
    private LocalDate ngaySuDung;

    // ⚠️ QUAN TRỌNG: Lưu giá TẠI THỜI ĐIỂM ĐẶT
    // Không reference sang gia_san vì giá có thể bị chủ sân thay đổi sau
    // Khi cần hoàn tiền hay tranh chấp → đây là căn cứ pháp lý
    @Column(name = "don_gia", nullable = false, precision = 12, scale = 0)
    private BigDecimal donGia;

    // ================================================================
    // Quan hệ: nhiều chi tiết thuộc về 1 phiếu
    // ================================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_dat_id", nullable = false)
    private PhieuDatSan phieuDat;

    // ================================================================
    // Quan hệ: nhiều chi tiết liên quan đến 1 sân
    // ================================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "san_id", nullable = false)
    private SanBong sanBong;

    // ================================================================
    // Quan hệ: nhiều chi tiết liên quan đến 1 khung giờ
    // ================================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khung_gio_id", nullable = false)
    private KhungGio khungGio;
}
