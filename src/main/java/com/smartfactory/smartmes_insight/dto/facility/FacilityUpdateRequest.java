package com.smartfactory.smartmes_insight.dto.facility;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "설비 수정 요청 DTO")
public class FacilityUpdateRequest {

    @Size(max = 100, message = "설비명은 100자를 초과할 수 없습니다")
    @Schema(description = "설비명", example = "CNC 가공기 #1")
    private String name;

    @Size(max = 50, message = "설비 타입은 50자를 초과할 수 없습니다")
    @Schema(description = "설비 타입", example = "CNC")
    private String type;

    @Size(max = 100, message = "위치는 100자를 초과할 수 없습니다")
    @Schema(description = "설비 위치", example = "A동 1층")
    private String location;

    @Pattern(regexp = "^(가동중|정지중|점검중|고장중)$", message = "상태는 가동중, 정지중, 점검중, 고장중 중 하나여야 합니다")
    @Schema(description = "설비 상태", example = "가동중", allowableValues = {"가동중", "정지중", "점검중", "고장중"})
    private String status;

    @Schema(description = "설비 사양", example = "최대 가공 크기: 500x500x300mm")
    private String specifications;

    @Schema(description = "설비 설명", example = "자동차 부품 가공용 CNC 머신")
    private String description;
}
