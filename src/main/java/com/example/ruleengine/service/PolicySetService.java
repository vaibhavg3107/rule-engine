package com.example.ruleengine.service;

import com.example.ruleengine.dto.request.CreatePolicySetRequest;
import com.example.ruleengine.dto.response.PolicySetResponse;
import com.example.ruleengine.entity.Policy;
import com.example.ruleengine.entity.PolicySet;
import com.example.ruleengine.entity.PolicySetOfferPolicy;
import com.example.ruleengine.entity.enums.PolicyType;
import com.example.ruleengine.exception.ResourceNotFoundException;
import com.example.ruleengine.exception.ValidationException;
import com.example.ruleengine.repository.PolicyRepository;
import com.example.ruleengine.repository.PolicySetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicySetService {

    private final PolicySetRepository policySetRepository;
    private final PolicyRepository policyRepository;

    @Transactional
    public PolicySetResponse createPolicySet(CreatePolicySetRequest request) {
        if (policySetRepository.existsByName(request.getName())) {
            throw new ValidationException("PolicySet with name '" + request.getName() + "' already exists");
        }

        Policy booleanPolicy = null;

        if (request.getBooleanPolicyId() != null) {
            booleanPolicy = policyRepository.findById(request.getBooleanPolicyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Policy", "id", request.getBooleanPolicyId()));
            if (booleanPolicy.getPolicyType() != PolicyType.BOOLEAN) {
                throw new ValidationException("Boolean policy must have type BOOLEAN");
            }
        }

        List<PolicySetOfferPolicy> offerPolicyList = new ArrayList<>();
        if (request.getOfferPolicies() != null && !request.getOfferPolicies().isEmpty()) {
            for (CreatePolicySetRequest.OfferPolicyWithPriority offerPolicyReq : request.getOfferPolicies()) {
                Policy policy = policyRepository.findById(offerPolicyReq.getPolicyId())
                        .orElseThrow(() -> new ResourceNotFoundException("Policy", "id", offerPolicyReq.getPolicyId()));
                if (policy.getPolicyType() != PolicyType.OFFER) {
                    throw new ValidationException("Offer policy must have type OFFER, but got: " + policy.getName());
                }
            }
        }

        boolean hasOfferPolicies = request.getOfferPolicies() != null && !request.getOfferPolicies().isEmpty();
        if (booleanPolicy == null && !hasOfferPolicies) {
            throw new ValidationException("PolicySet must have at least one policy (boolean or offer)");
        }

        PolicySet policySet = PolicySet.builder()
                .name(request.getName())
                .description(request.getDescription())
                .booleanPolicy(booleanPolicy)
                .evaluationStrategy(request.getEvaluationStrategy())
                .build();

        PolicySet savedPolicySet = policySetRepository.save(policySet);

        if (request.getOfferPolicies() != null && !request.getOfferPolicies().isEmpty()) {
            for (CreatePolicySetRequest.OfferPolicyWithPriority offerPolicyReq : request.getOfferPolicies()) {
                Policy policy = policyRepository.findById(offerPolicyReq.getPolicyId()).orElseThrow();
                PolicySetOfferPolicy psop = PolicySetOfferPolicy.builder()
                        .policySet(savedPolicySet)
                        .offerPolicy(policy)
                        .priority(offerPolicyReq.getPriority() != null ? offerPolicyReq.getPriority() : 0)
                        .enabled(offerPolicyReq.getEnabled() != null ? offerPolicyReq.getEnabled() : true)
                        .build();
                offerPolicyList.add(psop);
            }
            savedPolicySet.setOfferPolicies(offerPolicyList);
            savedPolicySet = policySetRepository.save(savedPolicySet);
        }

        return mapToResponse(savedPolicySet);
    }

    public PolicySetResponse getPolicySetById(UUID id) {
        PolicySet policySet = policySetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PolicySet", "id", id));
        return mapToResponse(policySet);
    }

    public List<PolicySetResponse> getAllPolicySets() {
        return policySetRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PolicySetResponse> getEnabledPolicySets() {
        return policySetRepository.findByEnabled(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PolicySetResponse updatePolicySet(UUID id, CreatePolicySetRequest request) {
        PolicySet policySet = policySetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PolicySet", "id", id));

        if (request.getName() != null && !request.getName().equals(policySet.getName())) {
            if (policySetRepository.existsByName(request.getName())) {
                throw new ValidationException("PolicySet with name '" + request.getName() + "' already exists");
            }
            policySet.setName(request.getName());
        }

        if (request.getDescription() != null) {
            policySet.setDescription(request.getDescription());
        }

        if (request.getBooleanPolicyId() != null) {
            Policy booleanPolicy = policyRepository.findById(request.getBooleanPolicyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Policy", "id", request.getBooleanPolicyId()));
            if (booleanPolicy.getPolicyType() != PolicyType.BOOLEAN) {
                throw new ValidationException("Boolean policy must have type BOOLEAN");
            }
            policySet.setBooleanPolicy(booleanPolicy);
        }

        if (request.getOfferPolicies() != null) {
            policySet.getOfferPolicies().clear();
            for (CreatePolicySetRequest.OfferPolicyWithPriority offerPolicyReq : request.getOfferPolicies()) {
                Policy policy = policyRepository.findById(offerPolicyReq.getPolicyId())
                        .orElseThrow(() -> new ResourceNotFoundException("Policy", "id", offerPolicyReq.getPolicyId()));
                if (policy.getPolicyType() != PolicyType.OFFER) {
                    throw new ValidationException("Offer policy must have type OFFER");
                }
                PolicySetOfferPolicy psop = PolicySetOfferPolicy.builder()
                        .policySet(policySet)
                        .offerPolicy(policy)
                        .priority(offerPolicyReq.getPriority() != null ? offerPolicyReq.getPriority() : 0)
                        .enabled(offerPolicyReq.getEnabled() != null ? offerPolicyReq.getEnabled() : true)
                        .build();
                policySet.getOfferPolicies().add(psop);
            }
        }

        if (request.getEvaluationStrategy() != null) {
            policySet.setEvaluationStrategy(request.getEvaluationStrategy());
        }

        policySet.setVersion(policySet.getVersion() + 1);
        PolicySet updatedPolicySet = policySetRepository.save(policySet);
        return mapToResponse(updatedPolicySet);
    }

    @Transactional
    public void deletePolicySet(UUID id) {
        if (!policySetRepository.existsById(id)) {
            throw new ResourceNotFoundException("PolicySet", "id", id);
        }
        policySetRepository.deleteById(id);
    }

    public PolicySet getPolicySetEntityById(UUID id) {
        return policySetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PolicySet", "id", id));
    }

    private PolicySetResponse mapToResponse(PolicySet policySet) {
        List<PolicySetResponse.OfferPolicyInfo> offerPolicyInfos = new ArrayList<>();
        if (policySet.getOfferPolicies() != null) {
            offerPolicyInfos = policySet.getOfferPolicies().stream()
                    .map(psop -> PolicySetResponse.OfferPolicyInfo.builder()
                            .policyId(psop.getOfferPolicy().getId())
                            .policyName(psop.getOfferPolicy().getName())
                            .priority(psop.getPriority())
                            .enabled(psop.getEnabled())
                            .build())
                    .collect(Collectors.toList());
        }

        return PolicySetResponse.builder()
                .id(policySet.getId())
                .name(policySet.getName())
                .description(policySet.getDescription())
                .booleanPolicyId(policySet.getBooleanPolicy() != null ? policySet.getBooleanPolicy().getId() : null)
                .booleanPolicyName(policySet.getBooleanPolicy() != null ? policySet.getBooleanPolicy().getName() : null)
                .offerPolicies(offerPolicyInfos)
                .evaluationStrategy(policySet.getEvaluationStrategy())
                .enabled(policySet.getEnabled())
                .version(policySet.getVersion())
                .createdAt(policySet.getCreatedAt())
                .updatedAt(policySet.getUpdatedAt())
                .build();
    }
}
