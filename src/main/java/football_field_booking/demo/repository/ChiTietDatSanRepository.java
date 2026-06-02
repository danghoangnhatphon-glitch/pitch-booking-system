package football_field_booking.demo.repository;

import football_field_booking.demo.entity.ChiTietDatSan;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;


@Repository
public interface ChiTietDatSanRepository extends JpaRepository<ChiTietDatSan, Long> {


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT c FROM ChiTietDatSan c
        WHERE c.sanBong.id    = :sanId
          AND c.khungGio.id   = :khungGioId
          AND c.ngaySuDung    = :ngay
        """)
    Optional<ChiTietDatSan> findWithLock(
        @Param("sanId")      Long sanId,
        @Param("khungGioId") Long khungGioId,
        @Param("ngay")       LocalDate ngay
    );

    boolean existsBySanBongIdAndKhungGioIdAndNgaySuDung(
        Long sanId,
        Long khungGioId,
        LocalDate ngaySuDung
    );
}
