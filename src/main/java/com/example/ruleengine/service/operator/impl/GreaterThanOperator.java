package com.example.ruleengine.service.operator.impl;

import org.springframework.stereotype.Component;

@Component
public class GreaterThanOperator extends BaseOperator {

    @Override
    public String getOperatorCode() {
        return "GT";
    }

    @Override
    public boolean evaluate(Object featureValue, Object operand) {
        return compareValues(featureValue, operand) > 0;
    }
}
