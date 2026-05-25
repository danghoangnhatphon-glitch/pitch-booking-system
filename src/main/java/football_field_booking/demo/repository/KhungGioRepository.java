package football_field_booking.demo.repository;

import football_field_booking.demo.entity.KhungGio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KhungGioRepository extends JpaRepository<KhungGio, Long> {

    // Lấy tất cả khung giờ sắp xếp theo giờ bắt đầu
    // → dùng để hiển thị danh sách ca trên UI
    List<KhungGio> findAllByOrderByGioBatDauAsc();

    // ================================================================
    // Query quan trọng nhất của hệ thống:
    // Lấy tất cả khung giờ của 1 sân trong 1 ngày,
    // KÈM THEO trạng thái còn trống hay đã đặt
    //
    // Kết quả trả về dạng: [khungGioId, gioBatDau, gioKetThuc,
    //                        laGioCaoDiem, donGia, conTrong]
    //
    // LEFT JOIN chi_tiet_dat_san:
    //   - Nếu không tìm thấy row → ca đó CHƯA được đặt → conTrong = true
    //   - Nếu tìm thấy row       → ca đó ĐÃ được đặt  → conTrong = false
    // ================================================================
    @Query("""
        SELECT
            kg.id           AS khungGioId,
            kg.gioBatDau    AS gioBatDau,
            kg.gioKetThuc   AS gioKetThuc,
            kg.laGioCaoDiem AS laGioCaoDiem,
            gs.donGia       AS donGia,
            CASE WHEN ct.id IS NULL THEN true ELSE false END AS conTrong
        FROM KhungGio kg
        JOIN GiaSan gs
            ON gs.khungGio = kg AND gs.sanBong.id = :sanId
        LEFT JOIN ChiTietDatSan ct
            ON ct.khungGio = kg
            AND ct.sanBong.id = :sanId
            AND ct.ngaySuDung = :ngay
        ORDER BY kg.gioBatDau ASC
        """)
    List<Object[]> layLichSanTheoNgay(
        @Param("sanId") Long sanId,
        @Param("ngay")  java.time.LocalDate ngay
    );
}
