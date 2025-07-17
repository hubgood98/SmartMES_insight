package com.smartfactory.smartmes_insight.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API 공통 응답 형식")
public class ApiResponse<T> {
    
    @Schema(description = "응답 코드", example = "200")
    private int code;
    
    @Schema(description = "응답 메시지", example = "성공")
    private String message;
    
    @Schema(description = "응답 데이터")
    private T data;
    
    @Schema(description = "응답 시간", example = "2025-01-20T10:30:00")
    private LocalDateTime timestamp;
    
    @Schema(description = "성공 여부", example = "true")
    private boolean success;
    
    // 성공 응답 생성
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("성공")
                .data(data)
                .timestamp(LocalDateTime.now())
                .success(true)
                .build();
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .success(true)
                .build();
    }
    
    // 실패 응답 생성
    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .success(false)
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return error(400, message);
    }
    
    // 201 Created 응답
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .code(201)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .success(true)
                .build();
    }
    
    // 204 No Content 응답
    public static <T> ApiResponse<T> noContent(String message) {
        return ApiResponse.<T>builder()
                .code(204)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .success(true)
                .build();
    }
}
