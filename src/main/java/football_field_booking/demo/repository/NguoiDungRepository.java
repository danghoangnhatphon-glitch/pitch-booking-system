package football_field_booking.demo.repository;

import football_field_booking.demo.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho bảng nguoi_dung
 *
 * JpaRepository<NguoiDung, Long> cho sẵn các method:
 *   save(), findById(), findAll(), deleteById(), count()...
 * Bạn chỉ cần thêm những query ĐẶC THÙ của project.
 */
@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {

    // Spring đọc tên method → tự sinh: SELECT * FROM nguoi_dung WHERE email = ?
    Optional<NguoiDung> findByEmail(String email);

    // Kiểm tra email đã tồn tại chưa (dùng khi đăng ký)
    // → SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 FROM nguoi_dung WHERE email = ?
    boolean existsByEmail(String email);

    // Kiểm tra SĐT đã tồn tại chưa
    boolean existsBySoDienThoai(String soDienThoai);
}
