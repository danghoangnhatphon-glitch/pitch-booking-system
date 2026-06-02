package football_field_booking.demo.service;

import football_field_booking.demo.dto.request.SanBongRequest;
import football_field_booking.demo.dto.request.TimSanRequest;
import football_field_booking.demo.dto.response.KhungGioTrangThaiResponse;
import football_field_booking.demo.dto.response.SanBongResponse;
import football_field_booking.demo.entity.SanBong;
import football_field_booking.demo.exception.AppException;
import football_field_booking.demo.repository.DanhGiaRepository;
import football_field_booking.demo.repository.KhungGioRepository;
import football_field_booking.demo.repository.NguoiDungRepository;
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
    private final NguoiDungRepository nguoiDungRepository;
    @Transactional
    public SanBongResponse luuSanBong(SanBongRequest request) {
        SanBong san;

        if (request.getId() != null) {
            // Trường hợp Sửa: Tìm sân có sẵn
            san = sanBongRepository.findById(request.getId())
                    .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy sân"));
        } else {
            san = new SanBong();
            san.setTrangThai(SanBong.TrangThai.HOAT_DONG);

            football_field_booking.demo.entity.NguoiDung chuSan = nguoiDungRepository.findById(request.getChuSanId())
                    .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy chủ sân"));
            san.setChuSan(chuSan);
        }

        san.setTenSan(request.getTenSan());
        san.setLoaiSan(SanBong.LoaiSan.valueOf(request.getLoaiSan()));
        san.setQuanHuyen(request.getQuanHuyen());
        san.setViTri(request.getViTri());
        san.setMoTa(request.getMoTa());
        san.setAnhSan(request.getAnhSan());

        return SanBongResponse.from(sanBongRepository.save(san), null);
    }

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

    @Transactional(readOnly = true)
    public SanBongResponse layChiTiet(Long sanId) {
        SanBong san = sanBongRepository.findById(sanId)
                .orElseThrow(() ->
                    new AppException.ResourceNotFoundException("Không tìm thấy sân #" + sanId)
                );
        Double diem = danhGiaRepository.tinhDiemTrungBinh(sanId);
        return SanBongResponse.from(san, diem);
    }


    @Transactional(readOnly = true)
    public List<SanBongResponse> timSan(TimSanRequest request) {
        String qh = request.getQuanHuyen();
        SanBong.LoaiSan ls = request.getLoaiSan();
        SanBong.TrangThai tt = SanBong.TrangThai.HOAT_DONG;

        List<SanBong> dsSan;


        if (qh == null && ls == null) {
            dsSan = sanBongRepository.findByTrangThai(tt);
        } else if (qh != null && ls == null) {
            dsSan = sanBongRepository.findByQuanHuyenAndTrangThai(qh, tt);
        } else if (qh == null && ls != null) {
            dsSan = sanBongRepository.findByLoaiSanAndTrangThai(ls, tt);
        } else {
            dsSan = sanBongRepository.timSanTheoBoLoc(qh, ls);
        }

        return dsSan.stream()
                .map(san -> {
                    Double diem = danhGiaRepository.tinhDiemTrungBinh(san.getId());
                    return SanBongResponse.from(san, diem);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<KhungGioTrangThaiResponse> layLichSan(Long sanId, LocalDate ngay) {


        sanBongRepository.findById(sanId)
                .orElseThrow(() ->
                    new AppException.ResourceNotFoundException("Không tìm thấy sân #" + sanId)
                );


        if (ngay.isBefore(LocalDate.now())) {
            throw new AppException.BadRequestException("Không thể xem lịch ngày đã qua");
        }

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


    @Transactional(readOnly = true)
    public List<SanBongResponse> laySanCuaChuSan(Long chuSanId) {
        return sanBongRepository.findByChuSanId(chuSanId)
                .stream()
                .map(san -> SanBongResponse.from(san, null))
                .toList();
    }

    @Transactional
    public SanBongResponse doiTrangThai(Long sanId, SanBong.TrangThai trangThai,
                                        Long nguoiYeuCauId) {
        SanBong san = sanBongRepository.findById(sanId)
                .orElseThrow(() ->
                    new AppException.ResourceNotFoundException("Không tìm thấy sân #" + sanId)
                );


        if (!san.getChuSan().getId().equals(nguoiYeuCauId)) {
            throw new AppException.ForbiddenException("Bạn không có quyền chỉnh sửa sân này");
        }

        san.setTrangThai(trangThai);
        SanBong saved = sanBongRepository.save(san);
        return SanBongResponse.from(saved, null);
    }
}
