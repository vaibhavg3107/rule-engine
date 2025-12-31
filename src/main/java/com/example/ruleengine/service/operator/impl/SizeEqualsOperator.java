package com.example.ruleengine.service.operator.impl;

import org.springframework.stereotype.Component;

@Component
public class SizeEqualsOperator extends BaseOperator {

    @Override
    public String getOperatorCode() {
        return "SIZE_EQ";
    }

    @Override
    public boolean evaluate(Object featureValue, Object operand) {
        return getSize(featureValue) == getNumericOperand(operand);
    }
}
