package football_field_booking.demo.service;

import football_field_booking.demo.exception.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Lưu file upload (ảnh sân, ảnh đại diện) ra thư mục ngoài classpath
 * để dữ liệu không bị mất khi build lại project.
 */
@Service
public class FileStorageService {

    // Thư mục vật lý lưu file — nằm ngoài JAR/target để tồn tại lâu dài
    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif"
    );
    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024; // 5MB, khớp application.properties

    /**
     * Lưu file vào {uploadDir}/{subFolder}/ với tên file random (UUID) để tránh trùng
     * và tránh path traversal. Trả về URL công khai để lưu vào DB (vd: /uploads/san-bong/abc.jpg)
     */
    public String luuFile(MultipartFile file, String subFolder) {
        if (file == null || file.isEmpty()) {
            throw new AppException.BadRequestException("Vui lòng chọn file để upload");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new AppException.BadRequestException("File vượt quá dung lượng cho phép (5MB)");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new AppException.BadRequestException("Chỉ chấp nhận file ảnh (jpg, png, webp, gif)");
        }

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalName.substring(dotIndex).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new AppException.BadRequestException("Định dạng file không hợp lệ");
        }

        String newFileName = UUID.randomUUID() + extension;

        try {
            Path targetDir = Paths.get(uploadDir, subFolder).normalize().toAbsolutePath();
            Files.createDirectories(targetDir);

            Path targetPath = targetDir.resolve(newFileName).normalize();
            // Chống path traversal: đảm bảo file đích vẫn nằm trong targetDir
            if (!targetPath.startsWith(targetDir)) {
                throw new AppException.BadRequestException("Tên file không hợp lệ");
            }

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new AppException.BadRequestException("Lỗi khi lưu file: " + e.getMessage());
        }

        return "/uploads/" + subFolder + "/" + newFileName;
    }
}
