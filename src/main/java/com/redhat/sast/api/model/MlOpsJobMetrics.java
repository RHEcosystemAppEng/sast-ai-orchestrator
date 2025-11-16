package com.redhat.sast.api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "mlops_job_metrics",
        indexes = {@Index(name = "idx_mlops_job_metrics_id", columnList = "id")})
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "mlOpsJob")
public class MlOpsJobMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mlops_job_id", nullable = false)
    private MlOpsJob mlOpsJob;

    @Column(name = "package_name", nullable = false)
    private String packageName;

    @Column(name = "accuracy", precision = 5, scale = 4)
    private BigDecimal accuracy;

    @Column(name = "precision", precision = 5, scale = 4)
    private BigDecimal precision;

    @Column(name = "recall", precision = 5, scale = 4)
    private BigDecimal recall;

    @Column(name = "f1_score", precision = 5, scale = 4)
    private BigDecimal f1Score;

    @Column(name = "cm_tp")
    private Integer cmTp; // True Positives

    @Column(name = "cm_fp")
    private Integer cmFp; // False Positives

    @Column(name = "cm_tn")
    private Integer cmTn; // True Negatives

    @Column(name = "cm_fn")
    private Integer cmFn; // False Negatives

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
