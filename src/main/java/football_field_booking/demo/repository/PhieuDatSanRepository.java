package football_field_booking.demo.repository;

import football_field_booking.demo.entity.PhieuDatSan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhieuDatSanRepository extends JpaRepository<PhieuDatSan, Long> {


    List<PhieuDatSan> findByNguoiDatIdOrderByCreatedAtDesc(Long nguoiDatId);


    List<PhieuDatSan> findByNguoiDatIdAndTrangThai(
        Long nguoiDatId,
        PhieuDatSan.TrangThai trangThai
    );
    List<PhieuDatSan> findAllByOrderByCreatedAtDesc();

    @Query("""
        SELECT
            YEAR(p.ngayTao)  AS nam,
            MONTH(p.ngayTao) AS thang,
            COUNT(p)         AS soPhieu,
            SUM(p.tongTien)  AS doanhThu
        FROM PhieuDatSan p
        JOIN ChiTietDatSan ct ON ct.phieuDat = p
        JOIN SanBong s        ON s = ct.sanBong
        WHERE s.chuSan.id           = :chuSanId
          AND p.trangThai            = 'DA_DUYET'
        GROUP BY YEAR(p.ngayTao), MONTH(p.ngayTao)
        ORDER BY YEAR(p.ngayTao) DESC, MONTH(p.ngayTao) DESC
        """)
    List<Object[]> thongKeDoanhThu(@Param("chuSanId") Long chuSanId);

    @Query("""
        SELECT DISTINCT p FROM PhieuDatSan p
        JOIN p.danhSachChiTiet ct
        WHERE ct.sanBong.chuSan.id = :chuSanId
          AND p.trangThai = :trangThai
        ORDER BY p.createdAt DESC
        """)
    List<PhieuDatSan> findByChuSanIdAndTrangThai(
        @Param("chuSanId")   Long chuSanId,
        @Param("trangThai")  PhieuDatSan.TrangThai trangThai
    );
}
