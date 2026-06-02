package football_field_booking.demo.service;

import football_field_booking.demo.dto.response.NguoiDungResponse;
import football_field_booking.demo.dto.response.SanBongResponse;
import football_field_booking.demo.entity.NguoiDung;
import football_field_booking.demo.exception.AppException;
import football_field_booking.demo.repository.DanhGiaRepository;
import football_field_booking.demo.repository.NguoiDungRepository;
import football_field_booking.demo.repository.PhieuDatSanRepository;
import football_field_booking.demo.repository.SanBongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final NguoiDungRepository  nguoiDungRepository;
    private final SanBongRepository    sanBongRepository;
    private final PhieuDatSanRepository phieuDatSanRepository;
    private final DanhGiaRepository    danhGiaRepository;

    @Transactional(readOnly = true)
    public List<NguoiDungResponse> layTatCaNguoiDung() {
        return nguoiDungRepository.findAll()
                .stream()
                .map(NguoiDungResponse::from)
                .toList();
    }

    @Transactional
    public void khoaTaiKhoan(Long id) {
        NguoiDung nd = nguoiDungRepository.findById(id)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy #" + id));
        if (nd.getVaiTro() == NguoiDung.VaiTro.ADMIN) {
            throw new AppException.ForbiddenException("Không thể khóa tài khoản Admin");
        }
        nd.setActive(false);
        nguoiDungRepository.save(nd);
    }

    @Transactional
    public void moKhoaTaiKhoan(Long id) {
        NguoiDung nd = nguoiDungRepository.findById(id)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy #" + id));
        nd.setActive(true);
        nguoiDungRepository.save(nd);
    }

    @Transactional
    public void xoaSan(Long sanId) {
        if (!sanBongRepository.existsById(sanId)) {
            throw new AppException.ResourceNotFoundException("Không tìm thấy sân #" + sanId);
        }
        sanBongRepository.deleteById(sanId);
    }
    @Transactional(readOnly = true)
    public Map<String, Object> thongKeTongQuan() {
        long tongNguoiDung = nguoiDungRepository.count();
        long tongSan       = sanBongRepository.count();
        long tongPhieu     = phieuDatSanRepository.count();

        BigDecimal tongDoanhThu = phieuDatSanRepository.findAll()
                .stream()
                .filter(p -> p.getTrangThai() == football_field_booking.demo.entity.PhieuDatSan.TrangThai.DA_DUYET)
                .map(football_field_booking.demo.entity.PhieuDatSan::getTongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "tongNguoiDung", tongNguoiDung,
                "tongSan",       tongSan,
                "tongPhieu",     tongPhieu,
                "tongDoanhThu",  tongDoanhThu
        );
    }

    @Transactional(readOnly = true)
    public List<SanBongResponse> layTatCaSan() {
        return sanBongRepository.findAll()
                .stream()
                .map(san -> {
                    Double diem = danhGiaRepository.tinhDiemTrungBinh(san.getId());
                    return SanBongResponse.from(san, diem);
                })
                .toList();
    }
}
