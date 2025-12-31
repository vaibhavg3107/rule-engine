package com.example.ruleengine.service.operator.impl;

import org.springframework.stereotype.Component;

@Component
public class GreaterThanOrEqualOperator extends BaseOperator {

    @Override
    public String getOperatorCode() {
        return "GTE";
    }

    @Override
    public boolean evaluate(Object featureValue, Object operand) {
        return compareValues(featureValue, operand) >= 0;
    }
}
