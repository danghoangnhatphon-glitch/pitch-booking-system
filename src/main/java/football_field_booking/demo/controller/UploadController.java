package football_field_booking.demo.controller;

import football_field_booking.demo.dto.response.ApiResponse;
import football_field_booking.demo.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/anh-san", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadAnhSan(
            @RequestParam("file") MultipartFile file) {

        String url = fileStorageService.luuFile(file, "san-bong");
        return ResponseEntity.ok(ApiResponse.ok("Upload ảnh thành công", Map.of("url", url)));
    }

    @PostMapping(value = "/avatar", consumes = "multipart/form-data")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadAvatar(
            @RequestParam("file") MultipartFile file) {

        String url = fileStorageService.luuFile(file, "avatar");
        return ResponseEntity.ok(ApiResponse.ok("Upload ảnh đại diện thành công", Map.of("url", url)));
    }
}
