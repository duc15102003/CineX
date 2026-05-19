package com.cinex.module.user.controller;

import com.cinex.common.response.ApiResponse;
import com.cinex.common.response.PageResponse;
import com.cinex.module.user.dto.ChangePasswordRequest;
import com.cinex.module.user.dto.UpdateProfileRequest;
import com.cinex.module.user.dto.UpdateRoleRequest;
import com.cinex.module.user.dto.UserProfileResponse;
import com.cinex.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller quản lý user profile.
 *
 * Nguyên tắc: Controller CHỈ nhận request → gọi service → trả ApiResponse.
 * KHÔNG chứa business logic (validation password, check role, ...) → để trong Service.
 *
 * [Chain of Responsibility] Request đi qua:
 * JwtAuthFilter → SecurityConfig → @PreAuthorize → Controller → Service → Repository → DB
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User profile management")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ApiResponse<UserProfileResponse> getProfile() {
        return ApiResponse.ok(userService.getProfile());
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ApiResponse<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok("Profile updated", userService.updateProfile(request));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.ok("Password changed successfully", null);
    }

    @PostMapping("/me/avatar")
    @Operation(summary = "Upload avatar image")
    public ApiResponse<UserProfileResponse> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok("Avatar uploaded", userService.uploadAvatar(file));
    }

    // ===== ADMIN APIs =====

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "(Admin) List all users with pagination")
    public ApiResponse<PageResponse<UserProfileResponse>> listUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(userService.listUsers(pageable));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "(Admin) Update user role")
    public ApiResponse<UserProfileResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        return ApiResponse.ok("Role updated", userService.updateRole(id, request));
    }
}
