package football_field_booking.demo.repository;

import football_field_booking.demo.entity.SanBong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanBongRepository extends JpaRepository<SanBong, Long> {

    List<SanBong> findByQuanHuyenAndTrangThai(String quanHuyen, SanBong.TrangThai trangThai);

    List<SanBong> findByLoaiSanAndTrangThai(SanBong.LoaiSan loaiSan, SanBong.TrangThai trangThai);

    List<SanBong> findByTrangThai(SanBong.TrangThai trangThai);

    List<SanBong> findByChuSanId(Long chuSanId);

    @Query("""
        SELECT s FROM SanBong s
        WHERE s.trangThai = 'HOAT_DONG'
          AND (:quanHuyen IS NULL OR s.quanHuyen = :quanHuyen)
          AND (:loaiSan   IS NULL OR s.loaiSan   = :loaiSan)
        ORDER BY s.tenSan
        """)
    List<SanBong> timSanTheoBoLoc(
        @Param("quanHuyen") String quanHuyen,
        @Param("loaiSan")   SanBong.LoaiSan loaiSan
    );
}
