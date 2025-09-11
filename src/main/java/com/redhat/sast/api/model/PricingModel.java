package com.redhat.sast.api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pricing_models")
@Data
@NoArgsConstructor
public class PricingModel {

    @Id
    @Column(name = "pricing_model_id", length = 100)
    private String pricingModelId;

    @Column(name = "model_name", length = 100, nullable = false)
    private String modelName;

    @Column(name = "provider", length = 50, nullable = false)
    private String provider;

    @Column(name = "input_price_per_1k", precision = 8, scale = 6, nullable = false)
    private BigDecimal inputPricePer1k;

    @Column(name = "output_price_per_1k", precision = 8, scale = 6, nullable = false)
    private BigDecimal outputPricePer1k;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "USD";

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @PrePersist
    public void prePersist() {
        if (this.effectiveFrom == null) {
            this.effectiveFrom = LocalDateTime.now();
        }
    }
}
