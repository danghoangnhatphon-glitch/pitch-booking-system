package football_field_booking.demo.repository;

import football_field_booking.demo.entity.GiaSan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GiaSanRepository extends JpaRepository<GiaSan, Long> {

    // Lấy giá của 1 sân trong 1 khung giờ cụ thể
    // Dùng khi tính tiền lúc đặt sân
    Optional<GiaSan> findBySanBongIdAndKhungGioId(Long sanId, Long khungGioId);
}
