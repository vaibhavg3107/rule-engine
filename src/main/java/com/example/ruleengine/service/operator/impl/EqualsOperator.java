package com.example.ruleengine.service.operator.impl;

import org.springframework.stereotype.Component;

@Component
public class EqualsOperator extends BaseOperator {

    @Override
    public String getOperatorCode() {
        return "EQ";
    }

    @Override
    public boolean evaluate(Object featureValue, Object operand) {
        return evaluateEquals(featureValue, operand);
    }
}
