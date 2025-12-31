package com.example.ruleengine.service.operator.impl;

import com.example.ruleengine.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BetweenOperator extends BaseOperator {

    @Override
    public String getOperatorCode() {
        return "BETWEEN";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean evaluate(Object featureValue, Object operand) {
        if (!(operand instanceof Map)) {
            throw new ValidationException("BETWEEN operator requires a map with 'min' and 'max'");
        }
        Map<String, Object> range = (Map<String, Object>) operand;
        Object min = range.get("min");
        Object max = range.get("max");

        if (min == null || max == null) {
            throw new ValidationException("BETWEEN operator requires both 'min' and 'max' values");
        }

        return compareValues(featureValue, min) >= 0 && compareValues(featureValue, max) <= 0;
    }
}
