package com.smartfactory.smartmes_insight.dto.workorder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "작업 지시 수정 요청 DTO")
public class WorkOrderUpdateRequest {

    @Size(max = 100, message = "제품명은 100자를 초과할 수 없습니다")
    @Schema(description = "제품명", example = "자동차 부품 A")
    private String productName;

    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    @Schema(description = "생산 수량", example = "1000")
    private Integer quantity;

    @Pattern(regexp = "^(대기중|진행중|완료|취소|일시정지)$", message = "상태는 대기중, 진행중, 완료, 취소, 일시정지 중 하나여야 합니다")
    @Schema(description = "작업 상태", example = "진행중", allowableValues = {"대기중", "진행중", "완료", "취소", "일시정지"})
    private String status;

    @Schema(description = "작업 설명", example = "긴급 주문 처리")
    private String description;
}
