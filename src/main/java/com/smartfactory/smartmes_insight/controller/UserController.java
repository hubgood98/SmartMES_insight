package com.smartfactory.smartmes_insight.controller;

import com.smartfactory.smartmes_insight.common.ApiResponse;
import com.smartfactory.smartmes_insight.dto.user.UserCreateRequest;
import com.smartfactory.smartmes_insight.dto.user.UserResponse;
import com.smartfactory.smartmes_insight.dto.user.UserUpdateRequest;
import com.smartfactory.smartmes_insight.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "👥 사용자 관리", description = "사용자 계정 관리 및 권한 설정 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다. (ADMIN 권한 필요)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody @Parameter(description = "사용자 생성 요청 데이터") UserCreateRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(user, "사용자가 성공적으로 생성되었습니다."));
    }

    @Operation(summary = "사용자 회원가입", description = "새로운 사용자를 등록합니다. (ADMIN 권한 필요)")
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(
            @Valid @RequestBody @Parameter(description = "사용자 회원가입 요청 데이터") UserCreateRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(user, "사용자가 성공적으로 생성되었습니다."));
    }

    @Operation(summary = "전체 사용자 조회", description = "모든 사용자 목록을 조회합니다. (ADMIN, MANAGER 권한 필요)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "사용자 목록 조회 성공"));
    }

    @Operation(summary = "사용자 상세 조회", description = "특정 사용자의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable @Parameter(description = "사용자 ID") Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "사용자 조회 성공"));
    }

    @Operation(summary = "사용자 정보 수정", description = "사용자 정보를 수정합니다. (ADMIN 권한 또는 본인만 가능)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable @Parameter(description = "사용자 ID") Long id,
            @Valid @RequestBody @Parameter(description = "사용자 수정 요청 데이터") UserUpdateRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(user, "사용자 정보가 성공적으로 수정되었습니다."));
    }

    @Operation(summary = "사용자 삭제", description = "사용자를 삭제합니다. (ADMIN 권한 필요)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable @Parameter(description = "사용자 ID") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContent("사용자가 성공적으로 삭제되었습니다."));
    }

    @Operation(summary = "사용자명으로 조회", description = "사용자명(username)으로 사용자를 조회합니다.")
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(
            @PathVariable @Parameter(description = "사용자명") String username) {
        UserResponse user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user, "사용자 조회 성공"));
    }

    @Operation(summary = "역할별 사용자 조회", description = "특정 역할의 사용자 목록을 조회합니다. (ADMIN, MANAGER 권한 필요)")
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(
            @PathVariable @Parameter(description = "역할 (ADMIN, MANAGER, OPERATOR)") String role) {
        List<UserResponse> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(ApiResponse.success(users, "역할별 사용자 조회 성공"));
    }

    @Operation(summary = "활성 사용자 조회", description = "활성 상태인 사용자 목록을 조회합니다. (ADMIN, MANAGER 권한 필요)")
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getActiveUsers() {
        List<UserResponse> users = userService.getActiveUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "활성 사용자 조회 성공"));
    }

    @Operation(summary = "사용자 활성화/비활성화", description = "사용자의 활성화 상태를 변경합니다. (ADMIN 권한 필요)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(
            @PathVariable @Parameter(description = "사용자 ID") Long id) {
        UserResponse user = userService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.success(user, "사용자 상태가 성공적으로 변경되었습니다."));
    }
}
