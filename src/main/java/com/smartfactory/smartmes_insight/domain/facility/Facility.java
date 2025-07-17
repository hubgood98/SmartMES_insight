package com.smartfactory.smartmes_insight.domain.facility;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "facilities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String type;

    @Column(length = 100)
    private String location;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 200)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

    public void updateInfo(Facility updatedFacility){
        this.name = updatedFacility.getName();
        this.type = updatedFacility.getType();
        this.location = updatedFacility.getLocation();
        this.status = updatedFacility.getStatus();
        this.description = updatedFacility.getDescription();
    }
}