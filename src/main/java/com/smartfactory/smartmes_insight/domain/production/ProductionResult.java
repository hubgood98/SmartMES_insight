package com.smartfactory.smartmes_insight.domain.production;

import com.smartfactory.smartmes_insight.domain.workorder.WorkOrder;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "production_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @Column(name = "quantity_produced", nullable = false)
    private Integer quantityProduced;

    @Column(name = "quantity_defective")
    private Integer quantityDefective;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "memo", length = 1000)
    private String memo;
}