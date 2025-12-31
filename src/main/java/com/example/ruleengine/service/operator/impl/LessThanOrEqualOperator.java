package com.example.ruleengine.service.operator.impl;

import org.springframework.stereotype.Component;

@Component
public class LessThanOrEqualOperator extends BaseOperator {

    @Override
    public String getOperatorCode() {
        return "LTE";
    }

    @Override
    public boolean evaluate(Object featureValue, Object operand) {
        return compareValues(featureValue, operand) <= 0;
    }
}
