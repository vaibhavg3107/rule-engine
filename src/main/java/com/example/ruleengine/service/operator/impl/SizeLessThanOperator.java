package com.example.ruleengine.service.operator.impl;

import org.springframework.stereotype.Component;

@Component
public class SizeLessThanOperator extends BaseOperator {

    @Override
    public String getOperatorCode() {
        return "SIZE_LT";
    }

    @Override
    public boolean evaluate(Object featureValue, Object operand) {
        return getSize(featureValue) < getNumericOperand(operand);
    }
}
