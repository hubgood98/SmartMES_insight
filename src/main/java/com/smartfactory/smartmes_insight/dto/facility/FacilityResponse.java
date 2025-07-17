package com.smartfactory.smartmes_insight.dto.facility;

import com.smartfactory.smartmes_insight.domain.facility.Facility;
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
@Schema(description = "설비 응답 DTO")
public class FacilityResponse {

    @Schema(description = "설비 ID", example = "1")
    private Long id;

    @Schema(description = "설비명", example = "CNC 가공기 #1")
    private String name;

    @Schema(description = "설비 타입", example = "CNC")
    private String type;

    @Schema(description = "설비 위치", example = "A동 1층")
    private String location;

    @Schema(description = "설비 상태", example = "가동중")
    private String status;

    @Schema(description = "설비 사양", example = "최대 가공 크기: 500x500x300mm")
    private String specifications;

    @Schema(description = "설비 설명", example = "자동차 부품 가공용 CNC 머신")
    private String description;

    @Schema(description = "생성일시", example = "2025-01-20T10:30:00")
    private LocalDateTime createdDate;

    @Schema(description = "수정일시", example = "2025-01-20T14:30:00")
    private LocalDateTime updatedDate;

    public static FacilityResponse from(Facility facility) {
        return FacilityResponse.builder()
                .id(facility.getId())
                .name(facility.getName())
                .type(facility.getType())
                .location(facility.getLocation())
                .status(facility.getStatus())
                .specifications(facility.getSpecifications())
                .description(facility.getDescription())
                .createdDate(facility.getCreatedDate())
                .updatedDate(facility.getUpdatedDate())
                .build();
    }
}
