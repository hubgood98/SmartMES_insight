package com.smartfactory.smartmes_insight.dto.workorder;

import com.smartfactory.smartmes_insight.domain.workorder.WorkOrder;
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
@Schema(description = "작업 지시 응답 DTO")
public class WorkOrderResponse {

    @Schema(description = "작업 지시 ID", example = "1")
    private Long id;

    @Schema(description = "제품명", example = "자동차 부품 A")
    private String productName;

    @Schema(description = "생산 수량", example = "1000")
    private Integer quantity;

    @Schema(description = "작업 상태", example = "진행중")
    private String status;

    @Schema(description = "작업 시작 시간", example = "2025-01-20T09:00:00")
    private LocalDateTime startTime;

    @Schema(description = "작업 종료 시간", example = "2025-01-20T18:00:00")
    private LocalDateTime endTime;

    @Schema(description = "작업 지시 생성 시간", example = "2025-01-20T08:30:00")
    private LocalDateTime createdDate;

    @Schema(description = "설비 ID", example = "1")
    private Long facilityId;

    @Schema(description = "설비명", example = "CNC 가공기 #1")
    private String facilityName;

    @Schema(description = "설비 타입", example = "CNC")
    private String facilityType;

    @Schema(description = "설비 위치", example = "A동 1층")
    private String facilityLocation;

    public static WorkOrderResponse from(WorkOrder workOrder) {
        return WorkOrderResponse.builder()
                .id(workOrder.getId())
                .productName(workOrder.getProductName())
                .quantity(workOrder.getQuantity())
                .status(workOrder.getStatus())
                .startTime(workOrder.getStartTime())
                .endTime(workOrder.getEndTime())
                .createdDate(workOrder.getCreatedDate())
                .facilityId(workOrder.getFacility().getId())
                .facilityName(workOrder.getFacility().getName())
                .facilityType(workOrder.getFacility().getType())
                .facilityLocation(workOrder.getFacility().getLocation())
                .build();
    }

    /**
     * 작업 진행률 계산 (시간 기준)
     */
    @Schema(description = "작업 진행률", example = "65.5")
    public Double getProgressPercentage() {
        if (startTime == null || "대기중".equals(status)) {
            return 0.0;
        }
        
        if ("완료".equals(status)) {
            return 100.0;
        }
        
        // 현재 시간 기준으로 진행률 계산 (임시 로직)
        long elapsedMinutes = java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes();
        // 8시간 기준으로 계산 (실제로는 예상 소요시간 필요)
        double progress = (elapsedMinutes / 480.0) * 100;
        return Math.min(progress, 99.0); // 최대 99%까지만
    }

    /**
     * 작업 소요 시간 계산 (분 단위)
     */
    @Schema(description = "작업 소요 시간(분)", example = "480")
    public Long getElapsedMinutes() {
        if (startTime == null) {
            return 0L;
        }
        
        LocalDateTime endTimeForCalc = endTime != null ? endTime : LocalDateTime.now();
        return java.time.Duration.between(startTime, endTimeForCalc).toMinutes();
    }

    /**
     * 작업 상태 한글 변환
     */
    @Schema(description = "작업 상태 한글", example = "진행중")
    public String getStatusKorean() {
        return switch (status) {
            case "대기중" -> "대기중";
            case "진행중" -> "진행중";
            case "완료" -> "완료";
            case "취소" -> "취소";
            case "일시정지" -> "일시정지";
            default -> status;
        };
    }
}
