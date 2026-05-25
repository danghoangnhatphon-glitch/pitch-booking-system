package football_field_booking.demo.repository;

import football_field_booking.demo.entity.DanhGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DanhGiaRepository extends JpaRepository<DanhGia, Long> {

    // Tất cả đánh giá của 1 sân (hiển thị trên trang chi tiết sân)
    List<DanhGia> findBySanBongIdOrderByCreatedAtDesc(Long sanId);

    // Kiểm tra phiếu đã được đánh giá chưa
    boolean existsByPhieuDatId(Long phieuDatId);

    // Tính điểm trung bình của 1 sân
    @Query("SELECT AVG(d.soSao) FROM DanhGia d WHERE d.sanBong.id = :sanId")
    Double tinhDiemTrungBinh(@Param("sanId") Long sanId);
}
