package football_field_booking.demo.service;

import football_field_booking.demo.dto.request.CapNhatThongTinRequest;
import football_field_booking.demo.dto.request.DangKyRequest;
import football_field_booking.demo.dto.request.DoiMatKhauRequest;
import football_field_booking.demo.dto.response.NguoiDungResponse;
import football_field_booking.demo.entity.NguoiDung;
import football_field_booking.demo.exception.AppException;
import football_field_booking.demo.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NguoiDungService implements UserDetailsService {

    private final NguoiDungRepository nguoiDungRepository;
    private final BCryptPasswordEncoder passwordEncoder;


    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        return nguoiDungRepository.findByEmail(email)
                .orElseThrow(() ->
                    new UsernameNotFoundException("Không tìm thấy: " + email)
                );
    }
    @Transactional
    public NguoiDungResponse dangKy(DangKyRequest request) {

        kiemTraEmailChuaTonTai(request.getEmail());

        NguoiDung nguoiDung = NguoiDung.builder()
                .hoTen(request.getHoTen())
                .email(request.getEmail().toLowerCase().trim())
                .matKhau(passwordEncoder.encode(request.getMatKhau()))
                .soDienThoai(request.getSoDienThoai())
                .vaiTro(NguoiDung.VaiTro.KHACH_HANG)
                .isActive(true)
                .build();
        NguoiDung saved = nguoiDungRepository.save(nguoiDung);

        return NguoiDungResponse.from(saved);
    }

    /** Ném lỗi nếu email đã được đăng ký — tách riêng để tái sử dụng ở bước gửi OTP (kiểm tra sớm, trước khi tốn phí SMS) */
    @Transactional(readOnly = true)
    public void kiemTraEmailChuaTonTai(String email) {
        if (nguoiDungRepository.existsByEmail(email)) {
            throw new AppException.EmailDaTonTaiException(email);
        }
    }

    @Transactional(readOnly = true)
    public NguoiDungResponse layThongTin(Long id) {
        NguoiDung nguoiDung = nguoiDungRepository.findById(id)
                .orElseThrow(() ->
                    new AppException.ResourceNotFoundException("Không tìm thấy người dùng #" + id)
                );
        return NguoiDungResponse.from(nguoiDung);
    }

    @Transactional
    public NguoiDungResponse capNhatThongTin(Long id, CapNhatThongTinRequest request) {
        NguoiDung nguoiDung = nguoiDungRepository.findById(id)
                .orElseThrow(() ->
                    new AppException.ResourceNotFoundException("Không tìm thấy người dùng #" + id)
                );

        nguoiDung.setHoTen(request.getHoTen());
        nguoiDung.setSoDienThoai(request.getSoDienThoai());
        if (request.getAnhDaiDien() != null && !request.getAnhDaiDien().isBlank()) {
            nguoiDung.setAnhDaiDien(request.getAnhDaiDien());
        }

        NguoiDung saved = nguoiDungRepository.save(nguoiDung);
        return NguoiDungResponse.from(saved);
    }

    @Transactional
    public void doiMatKhau(Long id, DoiMatKhauRequest request) {
        NguoiDung nguoiDung = nguoiDungRepository.findById(id)
                .orElseThrow(() ->
                    new AppException.ResourceNotFoundException("Không tìm thấy người dùng #" + id)
                );

        if (!passwordEncoder.matches(request.getMatKhauCu(), nguoiDung.getMatKhau())) {
            throw new AppException.BadRequestException("Mật khẩu hiện tại không đúng");
        }

        nguoiDung.setMatKhau(passwordEncoder.encode(request.getMatKhauMoi()));
        nguoiDungRepository.save(nguoiDung);
    }
}
