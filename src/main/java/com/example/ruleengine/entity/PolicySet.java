package com.example.ruleengine.entity;

import com.example.ruleengine.entity.enums.EvaluationStrategy;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "policy_sets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicySet {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boolean_policy_id")
    private Policy booleanPolicy;

    @OneToMany(mappedBy = "policySet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("priority DESC")
    @Builder.Default
    private List<PolicySetOfferPolicy> offerPolicies = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_strategy", nullable = false)
    @Builder.Default
    private EvaluationStrategy evaluationStrategy = EvaluationStrategy.BOOLEAN_FIRST;

    @Column(name = "enabled")
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
