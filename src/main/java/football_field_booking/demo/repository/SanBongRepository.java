package football_field_booking.demo.repository;

import football_field_booking.demo.entity.SanBong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanBongRepository extends JpaRepository<SanBong, Long> {

    // Lọc sân theo khu vực — dùng cho bộ lọc trang chủ
    List<SanBong> findByQuanHuyenAndTrangThai(String quanHuyen, SanBong.TrangThai trangThai);

    // Lọc theo loại sân (5, 7, 11)
    List<SanBong> findByLoaiSanAndTrangThai(SanBong.LoaiSan loaiSan, SanBong.TrangThai trangThai);

    // Tất cả sân đang hoạt động
    List<SanBong> findByTrangThai(SanBong.TrangThai trangThai);

    // Tất cả sân của 1 chủ sân (dùng cho trang quản lý của chủ sân)
    List<SanBong> findByChuSanId(Long chuSanId);

    // ================================================================
    // Query lọc kết hợp: khu vực + loại sân (cả 2 filter cùng lúc)
    // Dùng @Query vì Spring không tự sinh được method name 3 điều kiện
    // + có điều kiện null (nếu không chọn filter thì bỏ qua)
    // ================================================================
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
