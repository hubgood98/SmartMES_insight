package com.smartfactory.smartmes_insight.domain.workorder;

import com.smartfactory.smartmes_insight.domain.facility.Facility;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "work_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, length = 20)
    private String status;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;
    
    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}