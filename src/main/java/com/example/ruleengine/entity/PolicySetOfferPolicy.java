package com.example.ruleengine.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "policy_set_offer_policies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicySetOfferPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_set_id", nullable = false)
    private PolicySet policySet;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offer_policy_id", nullable = false)
    private Policy offerPolicy;

    @Column(nullable = false)
    private Integer priority;

    @Column
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
