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

/**
 * Service quản lý tài khoản người dùng
 *
 * Implement UserDetailsService → Spring Security dùng để load user khi login
 * (Spring gọi loadUserByUsername() với email người dùng nhập vào)
 */
@Service
@RequiredArgsConstructor  // Lombok tự tạo constructor inject tất cả final field
public class NguoiDungService implements UserDetailsService {

    private final NguoiDungRepository nguoiDungRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // ================================================================
    // Spring Security gọi method này khi người dùng đăng nhập
    // "username" ở đây thực ra là email
    // ================================================================
    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        System.out.println(">>> Load user: [" + email + "]");

        NguoiDung user = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println(">>> KHÔNG TÌM THẤY email này trong DB!");
                    return new UsernameNotFoundException("Không tìm thấy: " + email);
                });

        System.out.println(">>> Tìm thấy: " + user.getEmail());
        System.out.println(">>> Hash trong DB: " + user.getMatKhau());
        System.out.println(">>> isActive: " + user.isActive());
        System.out.println(">>> Test match: " +
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                        .matches("123456", user.getMatKhau()));
        System.out.println(">>> Hash chuẩn của 123456: " +
                new BCryptPasswordEncoder().encode("123456"));
        return user;
    }
    // ================================================================
    // Đăng ký tài khoản mới
    // ================================================================
    @Transactional
    public NguoiDungResponse dangKy(DangKyRequest request) {

        // 1. Kiểm tra email đã tồn tại chưa
        if (nguoiDungRepository.existsByEmail(request.getEmail())) {
            throw new AppException.EmailDaTonTaiException(request.getEmail());
        }

        // 2. Tạo entity mới — dùng Builder pattern của Lombok
        NguoiDung nguoiDung = NguoiDung.builder()
                .hoTen(request.getHoTen())
                .email(request.getEmail().toLowerCase().trim())  // chuẩn hoá email
                .matKhau(passwordEncoder.encode(request.getMatKhau()))  // hash mật khẩu
                .soDienThoai(request.getSoDienThoai())
                .vaiTro(NguoiDung.VaiTro.KHACH_HANG)  // mặc định là khách hàng
                .isActive(true)
                .build();

        // 3. Lưu vào DB
        NguoiDung saved = nguoiDungRepository.save(nguoiDung);

        // 4. Trả về DTO (không trả Entity trực tiếp)
        return NguoiDungResponse.from(saved);
    }

    // ================================================================
    // Lấy thông tin người dùng theo ID
    // ================================================================
    @Transactional(readOnly = true)  // readOnly = true → tối ưu hiệu năng đọc
    public NguoiDungResponse layThongTin(Long id) {
        NguoiDung nguoiDung = nguoiDungRepository.findById(id)
                .orElseThrow(() ->
                    new AppException.ResourceNotFoundException("Không tìm thấy người dùng #" + id)
                );
        return NguoiDungResponse.from(nguoiDung);
    }
}
