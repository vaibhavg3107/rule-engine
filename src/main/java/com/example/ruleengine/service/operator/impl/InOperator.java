package com.example.ruleengine.service.operator.impl;

import com.example.ruleengine.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InOperator extends BaseOperator {

    @Override
    public String getOperatorCode() {
        return "IN";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean evaluate(Object featureValue, Object operand) {
        if (!(operand instanceof List)) {
            throw new ValidationException("IN operator requires a list operand");
        }
        List<Object> list = (List<Object>) operand;
        for (Object item : list) {
            if (evaluateEquals(featureValue, item)) {
                return true;
            }
        }
        return false;
    }
}
