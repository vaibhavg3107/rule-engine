package com.example.ruleengine.service;

import com.example.ruleengine.dto.request.CreatePolicyRequest;
import com.example.ruleengine.dto.response.PolicyResponse;
import com.example.ruleengine.entity.Policy;
import com.example.ruleengine.entity.enums.PolicyType;
import com.example.ruleengine.exception.ResourceNotFoundException;
import com.example.ruleengine.exception.ValidationException;
import com.example.ruleengine.repository.PolicyRepository;
import com.example.ruleengine.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final RuleRepository ruleRepository;

    @Transactional
    public PolicyResponse createPolicy(CreatePolicyRequest request) {
        if (policyRepository.existsByName(request.getName())) {
            throw new ValidationException("Policy with name '" + request.getName() + "' already exists");
        }

        validateRootNode(request.getRootNode());
        
        if (request.getPolicyType() == PolicyType.OFFER && request.getOutputMapping() == null) {
            throw new ValidationException("Output mapping is required for OFFER policy type");
        }

        Policy policy = Policy.builder()
                .name(request.getName())
                .description(request.getDescription())
                .policyType(request.getPolicyType())
                .rootNode(request.getRootNode())
                .outputMapping(request.getOutputMapping())
                .build();

        Policy savedPolicy = policyRepository.save(policy);
        return mapToResponse(savedPolicy);
    }

    public PolicyResponse getPolicyById(UUID id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", "id", id));
        return mapToResponse(policy);
    }

    public List<PolicyResponse> getAllPolicies() {
        return policyRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PolicyResponse> getPoliciesByType(PolicyType policyType) {
        return policyRepository.findByPolicyType(policyType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PolicyResponse updatePolicy(UUID id, CreatePolicyRequest request) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", "id", id));

        if (request.getName() != null && !request.getName().equals(policy.getName())) {
            if (policyRepository.existsByName(request.getName())) {
                throw new ValidationException("Policy with name '" + request.getName() + "' already exists");
            }
            policy.setName(request.getName());
        }

        if (request.getDescription() != null) {
            policy.setDescription(request.getDescription());
        }

        if (request.getPolicyType() != null) {
            policy.setPolicyType(request.getPolicyType());
        }

        if (request.getRootNode() != null) {
            validateRootNode(request.getRootNode());
            policy.setRootNode(request.getRootNode());
        }

        if (request.getOutputMapping() != null) {
            policy.setOutputMapping(request.getOutputMapping());
        }

        policy.setVersion(policy.getVersion() + 1);
        Policy updatedPolicy = policyRepository.save(policy);
        return mapToResponse(updatedPolicy);
    }

    @Transactional
    public void deletePolicy(UUID id) {
        if (!policyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Policy", "id", id);
        }
        policyRepository.deleteById(id);
    }

    public Policy getPolicyEntityById(UUID id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", "id", id));
    }

    private void validateRootNode(Map<String, Object> node) {
        String nodeType = (String) node.get("type");
        if (nodeType == null) {
            throw new ValidationException("Node must have a 'type' field (LEAF or COMPOSITE)");
        }

        if ("LEAF".equals(nodeType)) {
            String ruleIdStr = (String) node.get("ruleId");
            if (ruleIdStr == null) {
                throw new ValidationException("LEAF node must have a 'ruleId' field");
            }
            UUID ruleId = UUID.fromString(ruleIdStr);
            if (!ruleRepository.existsById(ruleId)) {
                throw new ValidationException("Rule with id '" + ruleId + "' does not exist");
            }
        } else if ("COMPOSITE".equals(nodeType)) {
            String operator = (String) node.get("operator");
            if (operator == null || (!operator.equals("AND") && !operator.equals("OR") && !operator.equals("NOT"))) {
                throw new ValidationException("COMPOSITE node must have an 'operator' field (AND, OR, or NOT)");
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
            if (children == null || children.isEmpty()) {
                throw new ValidationException("COMPOSITE node must have at least one child");
            }

            if ("NOT".equals(operator) && children.size() != 1) {
                throw new ValidationException("NOT operator must have exactly one child");
            }

            for (Map<String, Object> child : children) {
                validateRootNode(child);
            }
        } else {
            throw new ValidationException("Invalid node type: " + nodeType + ". Must be LEAF or COMPOSITE");
        }
    }

    private PolicyResponse mapToResponse(Policy policy) {
        return PolicyResponse.builder()
                .id(policy.getId())
                .name(policy.getName())
                .description(policy.getDescription())
                .policyType(policy.getPolicyType())
                .rootNode(policy.getRootNode())
                .outputMapping(policy.getOutputMapping())
                .enabled(policy.getEnabled())
                .version(policy.getVersion())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }
}
