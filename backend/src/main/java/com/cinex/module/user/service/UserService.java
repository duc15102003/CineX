package com.cinex.module.user.service;

import com.cinex.common.exception.BusinessException;
import com.cinex.common.exception.ErrorCode;
import com.cinex.common.response.PageResponse;
import com.cinex.common.service.FileUploadService;
import com.cinex.common.util.SecurityUtil;
import com.cinex.module.auth.entity.User;
import com.cinex.module.auth.repository.UserRepository;
import com.cinex.module.user.dto.ChangePasswordRequest;
import com.cinex.module.user.dto.UpdateProfileRequest;
import com.cinex.module.user.dto.UpdateRoleRequest;
import com.cinex.module.user.dto.UserProfileResponse;
import com.cinex.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileUploadService fileUploadService;

    /**
     * Lấy profile user hiện tại từ SecurityContext.
     * Không truyền userId qua URL → tránh user sửa profile người khác.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile() {
        User user = getCurrentUser();
        return userMapper.toProfileResponse(user);
    }

    /**
     * Cập nhật profile: chỉ cho sửa fullName, phone.
     * Username, email, role → KHÔNG cho sửa qua API này.
     */
    @Transactional
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        userRepository.save(user);
        log.info("User {} updated profile", user.getUsername());
        return userMapper.toProfileResponse(user);
    }

    /**
     * Đổi mật khẩu: verify old password → hash new password → save.
     *
     * Business rules:
     * 1. Old password phải đúng (so sánh bằng BCrypt.matches)
     * 2. New password và confirm password phải giống nhau
     * 3. New password không được trùng old password
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD, "Old password is incorrect");
        }

        // Verify confirm password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD,
                    "New password and confirm password do not match");
        }

        // Không cho đặt password mới trùng password cũ
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD,
                    "New password must be different from old password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("User {} changed password", user.getUsername());
    }

    /**
     * Upload avatar lên Cloudinary, lưu URL vào User.avatarUrl.
     * Folder trên Cloudinary: "cinex/avatars" → dễ quản lý, phân loại ảnh.
     */
    @Transactional
    public UserProfileResponse uploadAvatar(MultipartFile file) {
        User user = getCurrentUser();
        String avatarUrl = fileUploadService.uploadImage(file, "cinex/avatars");
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        log.info("User {} uploaded avatar", user.getUsername());
        return userMapper.toProfileResponse(user);
    }

    /**
     * (ADMIN) Lấy danh sách user có phân trang.
     * Dùng PageResponse.from() để wrap kết quả Page<UserProfileResponse>.
     */
    @Transactional(readOnly = true)
    public PageResponse<UserProfileResponse> listUsers(Pageable pageable) {
        Page<UserProfileResponse> page = userRepository.findAll(pageable)
                .map(userMapper::toProfileResponse);
        return PageResponse.from(page);
    }

    /**
     * (ADMIN) Đổi role user.
     * Admin không thể đổi role của chính mình → tránh tự tước quyền.
     */
    @Transactional
    public UserProfileResponse updateRole(Long userId, UpdateRoleRequest request) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Không cho admin đổi role chính mình
        String currentUsername = SecurityUtil.getCurrentUsername();
        if (target.getUsername().equals(currentUsername)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Cannot change your own role");
        }

        target.setRole(request.getRole());
        userRepository.save(target);
        log.info("Admin {} changed role of user {} to {}", currentUsername, target.getUsername(), request.getRole());
        return userMapper.toProfileResponse(target);
    }

    private User getCurrentUser() {
        String username = SecurityUtil.getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
