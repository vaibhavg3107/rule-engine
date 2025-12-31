package com.example.ruleengine.service.operator.impl;

import org.springframework.stereotype.Component;

@Component
public class SizeGreaterThanOperator extends BaseOperator {

    @Override
    public String getOperatorCode() {
        return "SIZE_GT";
    }

    @Override
    public boolean evaluate(Object featureValue, Object operand) {
        return getSize(featureValue) > getNumericOperand(operand);
    }
}
