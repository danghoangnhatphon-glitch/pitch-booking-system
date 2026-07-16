package football_field_booking.demo.repository;

import football_field_booking.demo.entity.DanhGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DanhGiaRepository extends JpaRepository<DanhGia, Long> {

    List<DanhGia> findBySanBongIdOrderByCreatedAtDesc(Long sanId);
    boolean existsByPhieuDatId(Long phieuDatId);
    Optional<DanhGia> findByPhieuDatId(Long phieuDatId);

    @Query("SELECT AVG(d.soSao) FROM DanhGia d WHERE d.sanBong.id = :sanId")
    Double tinhDiemTrungBinh(@Param("sanId") Long sanId);
}
