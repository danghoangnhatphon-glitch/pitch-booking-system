package football_field_booking.demo.service;

import football_field_booking.demo.dto.request.DatSanRequest;
import football_field_booking.demo.dto.response.PhieuDatSanResponse;
import football_field_booking.demo.entity.*;
import football_field_booking.demo.exception.AppException;
import football_field_booking.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DatSanService {

    private final PhieuDatSanRepository    phieuDatSanRepository;
    private final ChiTietDatSanRepository  chiTietDatSanRepository;
    private final SanBongRepository        sanBongRepository;
    private final KhungGioRepository       khungGioRepository;
    private final GiaSanRepository         giaSanRepository;
    private final NguoiDungRepository      nguoiDungRepository;


    @Transactional
    public PhieuDatSanResponse datSan(DatSanRequest request, Long nguoiDatId) {


        SanBong san = sanBongRepository.findById(request.getSanId())
                .orElseThrow(() ->
                    new AppException.ResourceNotFoundException(
                        "Không tìm thấy sân #" + request.getSanId())
                );

        if (san.getTrangThai() != SanBong.TrangThai.HOAT_DONG) {
            throw new AppException.BadRequestException(
                "Sân " + san.getTenSan() + " hiện không hoạt động");
        }

        NguoiDung nguoiDat = nguoiDungRepository.findById(nguoiDatId)
                .orElseThrow(() ->
                    new AppException.ResourceNotFoundException("Không tìm thấy người dùng")
                );

        List<ChiTietDatSan> danhSachChiTiet = new ArrayList<>();
        BigDecimal tongTien = BigDecimal.ZERO;

        for (Long khungGioId : request.getDanhSachKhungGioId()) {

            KhungGio khungGio = khungGioRepository.findById(khungGioId)
                    .orElseThrow(() ->
                        new AppException.ResourceNotFoundException(
                            "Không tìm thấy khung giờ #" + khungGioId)
                    );

            boolean daDat = chiTietDatSanRepository
                    .findWithLock(san.getId(), khungGioId, request.getNgaySuDung())
                    .isPresent();

            if (daDat) {
                throw new AppException.SanDaDatException(
                    "Sân " + san.getTenSan() + " đã được đặt lúc "
                    + khungGio.getGioBatDau() + " - " + khungGio.getGioKetThuc()
                    + " ngày " + request.getNgaySuDung()
                );
            }

            GiaSan giaSan = giaSanRepository
                    .findBySanBongIdAndKhungGioId(san.getId(), khungGioId)
                    .orElseThrow(() ->
                        new AppException.BadRequestException(
                            "Sân này chưa có giá cho khung giờ đã chọn")
                    );

            ChiTietDatSan chiTiet = ChiTietDatSan.builder()
                    .sanBong(san)
                    .khungGio(khungGio)
                    .ngaySuDung(request.getNgaySuDung())
                    .donGia(giaSan.getDonGia())
                    .build();

            danhSachChiTiet.add(chiTiet);
            tongTien = tongTien.add(giaSan.getDonGia());
        }


        PhieuDatSan phieu = PhieuDatSan.builder()
                .nguoiDat(nguoiDat)
                .tongTien(tongTien)
                .trangThai(PhieuDatSan.TrangThai.CHO_DUYET)
                .trangThaiThanhToan(PhieuDatSan.TrangThaiThanhToan.CHUA_THANH_TOAN)
                .ghiChu(request.getGhiChu())
                .build();

        danhSachChiTiet.forEach(ct -> ct.setPhieuDat(phieu));
        phieu.setDanhSachChiTiet(danhSachChiTiet);

        PhieuDatSan saved = phieuDatSanRepository.save(phieu);


        return PhieuDatSanResponse.from(saved);

    }


    @Transactional(readOnly = true)
    public List<PhieuDatSanResponse> layLichSu(Long nguoiDatId) {
        return phieuDatSanRepository
                .findByNguoiDatIdOrderByCreatedAtDesc(nguoiDatId)
                .stream()
                .map(PhieuDatSanResponse::from)
                .toList();
    }

    @Transactional
    public PhieuDatSanResponse huyPhieu(Long phieuId, Long nguoiYeuCauId) {

        PhieuDatSan phieu = phieuDatSanRepository.findById(phieuId)
                .orElseThrow(() ->
                    new AppException.ResourceNotFoundException(
                        "Không tìm thấy phiếu #" + phieuId)
                );


        if (!phieu.getNguoiDat().getId().equals(nguoiYeuCauId)) {
            throw new AppException.ForbiddenException("Bạn không có quyền hủy phiếu này");
        }

        if (phieu.getTrangThai() != PhieuDatSan.TrangThai.CHO_DUYET) {
            throw new AppException.BadRequestException(
                "Không thể hủy phiếu đã được duyệt hoặc đã hủy trước đó");
        }

        phieu.setTrangThai(PhieuDatSan.TrangThai.DA_HUY);
        PhieuDatSan saved = phieuDatSanRepository.save(phieu);
        return PhieuDatSanResponse.from(saved);
    }

    @Transactional
    public PhieuDatSanResponse duyetPhieu(Long phieuId, Long chuSanId) {

        PhieuDatSan phieu = phieuDatSanRepository.findById(phieuId)
                .orElseThrow(() ->
                    new AppException.ResourceNotFoundException(
                        "Không tìm thấy phiếu #" + phieuId)
                );

        boolean laSanCuaMinh = phieu.getDanhSachChiTiet().stream()
                .anyMatch(ct -> ct.getSanBong().getChuSan().getId().equals(chuSanId));

        if (!laSanCuaMinh) {
            throw new AppException.ForbiddenException("Bạn không có quyền duyệt phiếu này");
        }

        if (phieu.getTrangThai() != PhieuDatSan.TrangThai.CHO_DUYET) {
            throw new AppException.BadRequestException("Phiếu này không ở trạng thái chờ duyệt");
        }

        phieu.setTrangThai(PhieuDatSan.TrangThai.DA_DUYET);
        PhieuDatSan saved = phieuDatSanRepository.save(phieu);
        return PhieuDatSanResponse.from(saved);
    }
    @Transactional(readOnly = true)
    public List<PhieuDatSanResponse> layPhieuChoDuyet(Long chuSanId) {
        return phieuDatSanRepository
                .findByChuSanIdAndTrangThai(chuSanId, PhieuDatSan.TrangThai.CHO_DUYET)
                .stream()
                .map(PhieuDatSanResponse::from)
                .toList();
    }

    @Transactional
    public PhieuDatSanResponse xacNhanThanhToan(Long phieuId,
                                                 PhieuDatSan.PhuongThucThanhToan phuongThuc,
                                                 Long chuSanId) {
        PhieuDatSan phieu = phieuDatSanRepository.findById(phieuId)
                .orElseThrow(() ->
                    new AppException.ResourceNotFoundException(
                        "Không tìm thấy phiếu #" + phieuId)
                );

        boolean laSanCuaMinh = phieu.getDanhSachChiTiet().stream()
                .anyMatch(ct -> ct.getSanBong().getChuSan().getId().equals(chuSanId));

        if (!laSanCuaMinh) {
            throw new AppException.ForbiddenException("Bạn không có quyền xác nhận phiếu này");
        }

        phieu.setTrangThaiThanhToan(PhieuDatSan.TrangThaiThanhToan.DA_THANH_TOAN);
        phieu.setPhuongThucThanhToan(phuongThuc);
        PhieuDatSan saved = phieuDatSanRepository.save(phieu);
        return PhieuDatSanResponse.from(saved);
    }
    // 1. Lấy tất cả phiếu đặt trong hệ thống (dành cho Admin)
    @Transactional(readOnly = true)
    public List<PhieuDatSanResponse> layTatCaPhieu() {
        return phieuDatSanRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(PhieuDatSanResponse::from)
                .toList();
    }

    @Transactional
    public PhieuDatSanResponse adminCapNhatTrangThai(Long phieuId, PhieuDatSan.TrangThai trangThai) {
        PhieuDatSan phieu = phieuDatSanRepository.findById(phieuId)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy phiếu #" + phieuId));

        phieu.setTrangThai(trangThai);
        PhieuDatSan saved = phieuDatSanRepository.save(phieu);
        return PhieuDatSanResponse.from(saved);
    }
}
