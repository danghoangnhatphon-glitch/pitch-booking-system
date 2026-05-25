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

/**
 * Repository quan trọng nhất — chứa logic chống trùng lịch
 */
@Repository
public interface ChiTietDatSanRepository extends JpaRepository<ChiTietDatSan, Long> {

    // ================================================================
    // KIỂM TRA TRÙNG LỊCH với Pessimistic Lock
    //
    // @Lock(PESSIMISTIC_WRITE) → Hibernate sinh ra:
    //   SELECT ... FROM chi_tiet_dat_san WITH (UPDLOCK, ROWLOCK) ...
    //   (cú pháp riêng của SQL Server)
    //
    // Khi A đang chạy method này, B gọi cùng lúc sẽ BỊ BLOCK tại đây
    // cho đến khi transaction của A kết thúc (commit hoặc rollback).
    // Sau đó B mới được vào và sẽ thấy row đã tồn tại → từ chối đặt.
    //
    // ⚠️ Method này CHỈ hoạt động đúng khi được gọi trong @Transactional
    // ================================================================
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

    // Kiểm tra không lock — dùng khi chỉ cần hiển thị trạng thái (không đặt)
    boolean existsBySanBongIdAndKhungGioIdAndNgaySuDung(
        Long sanId,
        Long khungGioId,
        LocalDate ngaySuDung
    );
}
