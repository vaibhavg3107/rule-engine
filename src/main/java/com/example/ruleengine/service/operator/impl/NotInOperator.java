package com.example.ruleengine.service.operator.impl;

import com.example.ruleengine.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotInOperator extends BaseOperator {

    @Override
    public String getOperatorCode() {
        return "NOT_IN";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean evaluate(Object featureValue, Object operand) {
        if (!(operand instanceof List)) {
            throw new ValidationException("NOT_IN operator requires a list operand");
        }
        List<Object> list = (List<Object>) operand;
        for (Object item : list) {
            if (evaluateEquals(featureValue, item)) {
                return false;
            }
        }
        return true;
    }
}
