package com.smartfactory.smartmes_insight.domain.sensor;

import com.smartfactory.smartmes_insight.domain.facility.Facility;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sensors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sensor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String type;

    private Double thresholdMin;
    private Double thresholdMax;

    @Column(length = 20)
    private String unit;
}