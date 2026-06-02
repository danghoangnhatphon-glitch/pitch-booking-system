package football_field_booking.demo.repository;

import football_field_booking.demo.entity.GiaSan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GiaSanRepository extends JpaRepository<GiaSan, Long> {

    Optional<GiaSan> findBySanBongIdAndKhungGioId(Long sanId, Long khungGioId);
}
