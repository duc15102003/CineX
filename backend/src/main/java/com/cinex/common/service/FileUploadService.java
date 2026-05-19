package com.cinex.common.service;

import com.cinex.common.exception.BusinessException;
import com.cinex.common.exception.ErrorCode;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Service upload file lên Cloudinary.
 *
 * Dùng chung cho toàn bộ dự án: avatar user, poster phim, ảnh rạp, ...
 * Mỗi nơi gọi chỉ cần truyền file + folder là xong.
 *
 * [Single Responsibility] Class này CHỈ lo việc upload.
 * Validation file type/size cũng nằm ở đây vì nó gắn liền với upload.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final Cloudinary cloudinary;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    /**
     * Upload ảnh lên Cloudinary, trả về URL public.
     *
     * @param file   file ảnh từ client (MultipartFile)
     * @param folder thư mục trên Cloudinary (VD: "cinex/avatars", "cinex/posters")
     * @return URL public của ảnh đã upload
     *
     * Luồng chi tiết:
     * 1. Client gửi file binary qua HTTP multipart/form-data
     * 2. Spring nhận → wrap thành MultipartFile (giữ nguyên binary gốc, KHÔNG nén)
     * 3. Server gửi byte[] gốc sang Cloudinary qua HTTPS (TLS mã hóa đường truyền)
     * 4. Cloudinary nhận → lưu bản gốc + tự động tối ưu:
     *    - "quality"="auto": nén ảnh mức tốt nhất (giữ chất lượng, giảm dung lượng 60-80%)
     *    - "fetch_format"="auto": chọn format tối ưu theo browser (WebP cho Chrome, AVIF cho Safari)
     *    - Tạo public_id unique (tránh trùng tên khi 2 người upload file cùng tên)
     * 5. Trả về secure_url (HTTPS + CDN)
     */
    public String uploadImage(MultipartFile file, String folder) {
        validateImage(file);

        try {
            // Gửi byte[] gốc sang Cloudinary qua HTTPS
            // Không cần mã hóa/nén trước — Cloudinary lo việc tối ưu sau khi nhận
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image",
                    "quality", "auto",       // Tự chọn mức nén tốt nhất (ảnh 2MB → ~300-500KB)
                    "fetch_format", "auto"   // Tự chọn format theo browser: WebP, AVIF, ...
            ));
            String url = (String) result.get("secure_url");
            log.info("Uploaded image to Cloudinary: {}", url);
            return url;
        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new BusinessException(ErrorCode.UNCATEGORIZED, "Failed to upload image");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_FILE, "File is empty");
        }
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new BusinessException(ErrorCode.INVALID_FILE,
                    "Only JPG, PNG, WEBP images are allowed");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_FILE,
                    "File size must not exceed 2MB");
        }
    }
}
