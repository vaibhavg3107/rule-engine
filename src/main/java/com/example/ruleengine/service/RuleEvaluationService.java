package com.example.ruleengine.service;

import com.example.ruleengine.entity.Rule;
import com.example.ruleengine.service.operator.OperatorStrategy;
import com.example.ruleengine.service.operator.OperatorStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleEvaluationService {

    private final OperatorStrategyFactory operatorStrategyFactory;

    public boolean evaluateRule(Rule rule, Object featureValue) {
        String operatorCode = rule.getOperator().getCode();
        Object operand = rule.getOperand();
        
        OperatorStrategy strategy = operatorStrategyFactory.getStrategy(operatorCode);
        
        if (featureValue == null) {
            return strategy.handleNullValue();
        }
        
        return strategy.evaluate(featureValue, operand);
    }
}
