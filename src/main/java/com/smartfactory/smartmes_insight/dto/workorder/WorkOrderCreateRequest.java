package com.smartfactory.smartmes_insight.dto.workorder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "작업 지시 생성 요청 DTO")
public class WorkOrderCreateRequest {

    @NotNull(message = "설비 ID는 필수입니다")
    @Schema(description = "설비 ID", example = "1", required = true)
    private Long facilityId;

    @NotBlank(message = "제품명은 필수입니다")
    @Size(max = 100, message = "제품명은 100자를 초과할 수 없습니다")
    @Schema(description = "제품명", example = "자동차 부품 A", required = true)
    private String productName;

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    @Schema(description = "생산 수량", example = "1000", required = true)
    private Integer quantity;

    @Schema(description = "작업 상태", example = "대기중", defaultValue = "대기중")
    private String status = "대기중";

    @Schema(description = "작업 설명", example = "긴급 주문 처리")
    private String description;
}
