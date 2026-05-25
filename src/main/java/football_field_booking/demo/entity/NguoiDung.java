package football_field_booking.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Map với bảng nguoi_dung
 *
 * Implement UserDetails để Spring Security dùng trực tiếp
 * → không cần viết thêm lớp wrapper riêng
 */
@Entity
@Table(name = "nguoi_dung")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NguoiDung implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // map với IDENTITY(1,1)
    private Long id;

    @Column(name = "ho_ten", nullable = false, length = 100)
    private String hoTen;

    // unique = true → Hibernate tự tạo unique constraint nếu ddl-auto=create
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "mat_khau", nullable = false, length = 255)
    private String matKhau;

    @Column(name = "so_dien_thoai", length = 15)
    private String soDienThoai;

    // EnumType.STRING → lưu "KHACH_HANG" vào DB thay vì số 0,1,2
    // Nếu dùng ORDINAL, thêm role vào giữa sẽ sai hết data cũ
    @Enumerated(EnumType.STRING)
    @Column(name = "vai_tro", nullable = false, length = 20)
    private VaiTro vaiTro = VaiTro.KHACH_HANG;

    @Column(name = "anh_dai_dien", length = 500)
    private String anhDaiDien;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // updatable = false → created_at không bị đổi khi UPDATE
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Tự gán thời gian trước khi INSERT lần đầu
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ================================================================
    // Quan hệ: 1 người dùng có nhiều phiếu đặt sân
    // mappedBy = tên field trong PhieuDatSan trỏ về NguoiDung
    // fetch = LAZY → không load toàn bộ phiếu khi chỉ cần info người dùng
    // ================================================================
    @OneToMany(mappedBy = "nguoiDat", fetch = FetchType.LAZY)
    private List<PhieuDatSan> danhSachPhieu;

    // ================================================================
    // UserDetails interface — Spring Security đọc các method này
    // ================================================================
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // "ROLE_" prefix là quy ước của Spring Security
        return List.of(new SimpleGrantedAuthority("ROLE_" + vaiTro.name()));
    }

    @Override
    public String getPassword() {
        return matKhau;  // Spring Security gọi getPassword() để lấy hash
    }

    @Override
    public String getUsername() {
        return email;  // dùng email làm username để đăng nhập
    }

    @Override public boolean isAccountNonExpired()  { return true; }
    @Override public boolean isAccountNonLocked()   { return isActive; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()            { return isActive; }

    // ================================================================
    // Enum VaiTro — đặt trong cùng file cho gọn
    // ================================================================
    public enum VaiTro {
        KHACH_HANG,
        CHU_SAN,
        ADMIN
    }
}
