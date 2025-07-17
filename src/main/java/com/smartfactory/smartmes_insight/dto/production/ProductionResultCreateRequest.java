package com.smartfactory.smartmes_insight.dto.production;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "생산 실적 생성 요청 DTO")
public class ProductionResultCreateRequest {

    @NotNull(message = "작업 지시 ID는 필수입니다")
    @Schema(description = "작업 지시 ID", example = "1", required = true)
    private Long workOrderId;

    @NotNull(message = "생산 수량은 필수입니다")
    @Min(value = 0, message = "생산 수량은 0개 이상이어야 합니다")
    @Schema(description = "생산 수량", example = "950", required = true)
    private Integer quantityProduced;

    @Min(value = 0, message = "불량 수량은 0개 이상이어야 합니다")
    @Schema(description = "불량 수량", example = "15", defaultValue = "0")
    private Integer quantityDefective = 0;

    @Schema(description = "생산 실적 메모", example = "야간 작업으로 인한 약간의 품질 저하")
    private String memo;
}
