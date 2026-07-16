package football_field_booking.demo.service;

import football_field_booking.demo.dto.request.DanhGiaRequest;
import football_field_booking.demo.entity.DanhGia;
import football_field_booking.demo.entity.PhieuDatSan;
import football_field_booking.demo.entity.SanBong;
import football_field_booking.demo.exception.AppException;
import football_field_booking.demo.repository.DanhGiaRepository;
import football_field_booking.demo.repository.PhieuDatSanRepository;
import football_field_booking.demo.repository.SanBongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DanhGiaService {

    private final DanhGiaRepository danhGiaRepository;
    private final PhieuDatSanRepository phieuDatSanRepository;
    private final SanBongRepository sanBongRepository;

    @Transactional
    public void guiDanhGia(DanhGiaRequest request, Long nguoiDungId) {

        PhieuDatSan phieu = phieuDatSanRepository.findById(request.getPhieuDatId())
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy phiếu đặt"));

        if (!phieu.getNguoiDat().getId().equals(nguoiDungId)) {
            throw new AppException.ForbiddenException("Bạn không có quyền đánh giá phiếu này");
        }
        if (phieu.getTrangThai() != PhieuDatSan.TrangThai.DA_DUYET) {
            throw new AppException.BadRequestException("Chỉ có thể đánh giá phiếu đã được duyệt");
        }
        if (danhGiaRepository.existsByPhieuDatId(phieu.getId())) {
            throw new AppException.BadRequestException("Phiếu này đã được đánh giá");
        }

        SanBong san = sanBongRepository.findById(request.getSanId())
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy sân"));

        DanhGia danhGia = new DanhGia();
        danhGia.setPhieuDat(phieu);
        danhGia.setNguoiDanhGia(phieu.getNguoiDat());
        danhGia.setSanBong(san);
        danhGia.setSoSao(request.getSoSao().byteValue());
        danhGia.setNoiDung(request.getNoiDung());


        danhGiaRepository.save(danhGia);
    }

    @Transactional
    public void suaDanhGia(Long phieuDatId, DanhGiaRequest request, Long nguoiDungId) {

        DanhGia danhGia = danhGiaRepository.findByPhieuDatId(phieuDatId)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy đánh giá"));

        if (!danhGia.getNguoiDanhGia().getId().equals(nguoiDungId)) {
            throw new AppException.ForbiddenException("Bạn không có quyền sửa đánh giá này");
        }

        danhGia.setSoSao(request.getSoSao().byteValue());
        danhGia.setNoiDung(request.getNoiDung());
        danhGiaRepository.save(danhGia);
    }

    @Transactional
    public void xoaDanhGia(Long phieuDatId, Long nguoiDungId) {

        DanhGia danhGia = danhGiaRepository.findByPhieuDatId(phieuDatId)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy đánh giá"));

        if (!danhGia.getNguoiDanhGia().getId().equals(nguoiDungId)) {
            throw new AppException.ForbiddenException("Bạn không có quyền xóa đánh giá này");
        }

        danhGiaRepository.delete(danhGia);
    }
}