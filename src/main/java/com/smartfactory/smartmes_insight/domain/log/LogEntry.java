package com.smartfactory.smartmes_insight.domain.log;

import com.smartfactory.smartmes.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String action;

    @Column(name = "target_table", nullable = false, length = 50)
    private String targetTable;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(length = 255)
    private String message;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}