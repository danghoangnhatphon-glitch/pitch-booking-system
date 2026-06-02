package football_field_booking.demo.service;

import football_field_booking.demo.repository.PhieuDatSanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ThongKeService {

    private final PhieuDatSanRepository phieuDatSanRepository;

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> thongKeDoanhThu(Long chuSanId) {

        List<Object[]> rows = phieuDatSanRepository.thongKeDoanhThu(chuSanId);
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            int nam       = ((Number) row[0]).intValue();
            int thang     = ((Number) row[1]).intValue();
            BigDecimal dt = (BigDecimal) row[3];

            String key = String.format("%d-%02d", nam, thang);
            result.put(key, dt);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> tongQuan(Long chuSanId) {
        List<Object[]> rows = phieuDatSanRepository.thongKeDoanhThu(chuSanId);

        long totalPhieu    = 0;
        BigDecimal totalDt = BigDecimal.ZERO;

        for (Object[] row : rows) {
            totalPhieu += ((Number) row[2]).longValue();
            totalDt     = totalDt.add((BigDecimal) row[3]);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tongPhieu",    totalPhieu);
        result.put("tongDoanhThu", totalDt);
        return result;
    }
}
