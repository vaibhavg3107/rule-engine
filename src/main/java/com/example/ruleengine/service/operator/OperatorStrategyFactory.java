package com.example.ruleengine.service.operator;

import com.example.ruleengine.exception.ValidationException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OperatorStrategyFactory {

    private final List<OperatorStrategy> strategies;
    private final Map<String, OperatorStrategy> strategyMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (OperatorStrategy strategy : strategies) {
            strategyMap.put(strategy.getOperatorCode(), strategy);
            log.debug("Registered operator strategy: {}", strategy.getOperatorCode());
        }
        log.info("Registered {} operator strategies", strategyMap.size());
    }

    public OperatorStrategy getStrategy(String operatorCode) {
        OperatorStrategy strategy = strategyMap.get(operatorCode);
        if (strategy == null) {
            throw new ValidationException("Unknown operator: " + operatorCode);
        }
        return strategy;
    }

    public boolean hasStrategy(String operatorCode) {
        return strategyMap.containsKey(operatorCode);
    }
}
