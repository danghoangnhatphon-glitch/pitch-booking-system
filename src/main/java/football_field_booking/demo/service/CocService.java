package football_field_booking.demo.service;

import football_field_booking.demo.dto.response.PhieuDatSanResponse;
import football_field_booking.demo.entity.NguoiDung;
import football_field_booking.demo.entity.PhieuDatSan;
import football_field_booking.demo.exception.AppException;
import football_field_booking.demo.repository.NguoiDungRepository;
import football_field_booking.demo.repository.PhieuDatSanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CocService {

    private final PhieuDatSanRepository phieuDatSanRepository;
    private final NguoiDungRepository   nguoiDungRepository;

    @Transactional
    public PhieuDatSanResponse xacNhanDaChuyenKhoan(Long phieuId, Long nguoiDungId) {

        PhieuDatSan phieu = phieuDatSanRepository.findById(phieuId)
                .orElseThrow(() -> new AppException.ResourceNotFoundException(
                    "Không tìm thấy phiếu #" + phieuId));

        if (!phieu.getNguoiDat().getId().equals(nguoiDungId)) {
            throw new AppException.ForbiddenException("Không có quyền thực hiện");
        }

        if (phieu.getTrangThai() != PhieuDatSan.TrangThai.DA_DUYET) {
            throw new AppException.BadRequestException(
                "Phiếu chưa được chủ sân duyệt");
        }

        if (phieu.getTrangThaiCoc() == PhieuDatSan.TrangThaiCoc.DA_COC) {
            throw new AppException.BadRequestException("Phiếu đã được đặt cọc rồi");
        }

        phieu.setTrangThaiCoc(PhieuDatSan.TrangThaiCoc.DA_COC);
        phieu.setThoiGianCoc(LocalDateTime.now());

        PhieuDatSan saved = phieuDatSanRepository.save(phieu);
        return PhieuDatSanResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Map<String, String> layThongTinNganHang(Long phieuId) {

        PhieuDatSan phieu = phieuDatSanRepository.findById(phieuId)
                .orElseThrow(() -> new AppException.ResourceNotFoundException(
                    "Không tìm thấy phiếu #" + phieuId));

        NguoiDung chuSan = phieu.getDanhSachChiTiet()
                .get(0).getSanBong().getChuSan();

        if (chuSan.getSoTaiKhoan() == null) {
            throw new AppException.BadRequestException(
                "Chủ sân chưa cập nhật thông tin ngân hàng");
        }

        return Map.of(
            "tenNganHang", chuSan.getTenNganHang() != null ? chuSan.getTenNganHang() : "",
            "soTaiKhoan",  chuSan.getSoTaiKhoan(),
            "tenChuTk",    chuSan.getTenChuTk() != null ? chuSan.getTenChuTk() : "",
            "maNganHang",  layMaNganHang(chuSan.getTenNganHang())
        );
    }
    @Transactional
    public void capNhatNganHang(Long chuSanId, String tenNganHang,
                                 String soTaiKhoan, String tenChuTk) {
        NguoiDung chuSan = nguoiDungRepository.findById(chuSanId)
                .orElseThrow(() -> new AppException.ResourceNotFoundException(
                    "Không tìm thấy người dùng"));

        chuSan.setTenNganHang(tenNganHang);
        chuSan.setSoTaiKhoan(soTaiKhoan);
        chuSan.setTenChuTk(tenChuTk != null ? tenChuTk.toUpperCase() : null);
        nguoiDungRepository.save(chuSan);
    }


    private String layMaNganHang(String tenNganHang) {
        if (tenNganHang == null) return "970436";
        return switch (tenNganHang.toLowerCase()) {
            case "vietcombank", "vcb"       -> "970436";
            case "techcombank", "tcb"       -> "970407";
            case "mbbank", "mb"             -> "970422";
            case "bidv"                     -> "970418";
            case "vietinbank", "ctg"        -> "970415";
            case "agribank", "agr"          -> "970405";
            case "acb"                      -> "970416";
            case "vpbank"                   -> "970432";
            case "tpbank"                   -> "970423";
            case "momo"                     -> "970408";
            default                         -> "970436";
        };
    }
}
