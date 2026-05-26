package football_field_booking.demo.service;

import football_field_booking.demo.dto.request.TimSanRequest;
import football_field_booking.demo.dto.response.KhungGioTrangThaiResponse;
import football_field_booking.demo.dto.response.SanBongResponse;
import football_field_booking.demo.entity.SanBong;
import football_field_booking.demo.exception.AppException;
import football_field_booking.demo.repository.DanhGiaRepository;
import football_field_booking.demo.repository.KhungGioRepository;
import football_field_booking.demo.repository.SanBongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SanBongService {

    private final SanBongRepository    sanBongRepository;
    private final KhungGioRepository   khungGioRepository;
    private final DanhGiaRepository    danhGiaRepository;

    // ================================================================
    // Lấy tất cả sân đang hoạt động
    // ================================================================
    @Transactional(readOnly = true)
    public List<SanBongResponse> layTatCaSan() {
        return sanBongRepository
                .findByTrangThai(SanBong.TrangThai.HOAT_DONG)
                .stream()
                .map(san -> {
                    Double diem = danhGiaRepository.tinhDiemTrungBinh(san.getId());
                    return SanBongResponse.from(san, diem);
                })
                .toList();
    }

    // ================================================================
    // Lấy chi tiết 1 sân
    // ================================================================
    @Transactional(readOnly = true)
    public SanBongResponse layChiTiet(Long sanId) {
        SanBong san = sanBongRepository.findById(sanId)
                .orElseThrow(() ->
                    new AppException.ResourceNotFoundException("Không tìm thấy sân #" + sanId)
                );
        Double diem = danhGiaRepository.tinhDiemTrungBinh(sanId);
        return SanBongResponse.from(san, diem);
    }

    // ================================================================
    // Tìm sân theo bộ lọc (khu vực + loại sân)
    // ================================================================
    @Transactional(readOnly = true)
    public List<SanBongResponse> timSan(TimSanRequest request) {
        return sanBongRepository
                .timSanTheoBoLoc(request.getQuanHuyen(), request.getLoaiSan())
                .stream()
                .map(san -> {
                    Double diem = danhGiaRepository.tinhDiemTrungBinh(san.getId());
                    return SanBongResponse.from(san, diem);
                })
                .toList();
    }

    // ================================================================
    // Lấy lịch sân theo ngày — dùng cho UI chọn giờ
    // Trả về danh sách khung giờ + trạng thái còn trống hay đã đặt
    // ================================================================
    @Transactional(readOnly = true)
    public List<KhungGioTrangThaiResponse> layLichSan(Long sanId, LocalDate ngay) {

        // Kiểm tra sân tồn tại
        sanBongRepository.findById(sanId)
                .orElseThrow(() ->
                    new AppException.ResourceNotFoundException("Không tìm thấy sân #" + sanId)
                );

        // Không cho xem lịch ngày trong quá khứ
        if (ngay.isBefore(LocalDate.now())) {
            throw new AppException.BadRequestException("Không thể xem lịch ngày đã qua");
        }

        // Query trả về Object[]: [khungGioId, gioBatDau, gioKetThuc,
        //                          laGioCaoDiem, donGia, conTrong]
        List<Object[]> rows = khungGioRepository.layLichSanTheoNgay(sanId, ngay);

        return rows.stream()
                .map(row -> KhungGioTrangThaiResponse.builder()
                        .khungGioId((Long)    row[0])
                        .gioBatDau( (LocalTime) row[1])
                        .gioKetThuc((LocalTime) row[2])
                        .laGioCaoDiem((boolean) row[3])
                        .donGia(    (BigDecimal) row[4])
                        .conTrong(  (boolean)    row[5])
                        .build())
                .toList();
    }

    // ================================================================
    // Lấy danh sách sân của 1 chủ sân (dùng cho trang quản lý)
    // ================================================================
    @Transactional(readOnly = true)
    public List<SanBongResponse> laySanCuaChuSan(Long chuSanId) {
        return sanBongRepository.findByChuSanId(chuSanId)
                .stream()
                .map(san -> SanBongResponse.from(san, null))
                .toList();
    }

    // ================================================================
    // Chủ sân đổi trạng thái sân (mở/đóng/bảo trì)
    // ================================================================
    @Transactional
    public SanBongResponse doiTrangThai(Long sanId, SanBong.TrangThai trangThai,
                                        Long nguoiYeuCauId) {
        SanBong san = sanBongRepository.findById(sanId)
                .orElseThrow(() ->
                    new AppException.ResourceNotFoundException("Không tìm thấy sân #" + sanId)
                );

        // Chỉ chủ sân của sân đó mới được đổi trạng thái
        if (!san.getChuSan().getId().equals(nguoiYeuCauId)) {
            throw new AppException.ForbiddenException("Bạn không có quyền chỉnh sửa sân này");
        }

        san.setTrangThai(trangThai);
        SanBong saved = sanBongRepository.save(san);
        return SanBongResponse.from(saved, null);
    }
}
