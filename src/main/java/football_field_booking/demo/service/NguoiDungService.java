package football_field_booking.demo.service;

import football_field_booking.demo.dto.request.DangKyRequest;
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

        if (nguoiDungRepository.existsByEmail(request.getEmail())) {
            throw new AppException.EmailDaTonTaiException(request.getEmail());
        }

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

    @Transactional(readOnly = true)
    public NguoiDungResponse layThongTin(Long id) {
        NguoiDung nguoiDung = nguoiDungRepository.findById(id)
                .orElseThrow(() ->
                    new AppException.ResourceNotFoundException("Không tìm thấy người dùng #" + id)
                );
        return NguoiDungResponse.from(nguoiDung);
    }
}
