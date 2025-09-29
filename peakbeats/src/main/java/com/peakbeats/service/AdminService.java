package com.peakbeats.service;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;


@Service
public class AdminService {

    private Map<String, Double> prices = new HashMap<>();
    private Set<String> blocked = new HashSet<>();

    // Default-Preise merken
    private final Map<String, Double> defaultPrices = Map.of(
        "Small Gig", 99.0,
        "Party Pro", 149.0,
        "Open Air", 249.0,
        "Individuell", 99.0
    );
    
    public void resetDefaults() {
        prices.clear();
        prices.putAll(defaultPrices);
        blocked.clear();
    }    

    public AdminService() {
        resetPrices(); // mit Defaults starten
    }

    public Map<String, Double> getPrices() {
        return prices;
    }

    public Set<String> getBlocked() {
        return blocked;
    }

    public void updatePrices(Map<String, Double> newPrices) {
        prices.putAll(newPrices);
    }

    public void updateBlocked(List<String> blockedList) {
        blocked.clear();
        blocked.addAll(blockedList);
    }

    public void resetPrices() {
        prices = new HashMap<>(defaultPrices);
        blocked.clear();
    }

    public Map<String, Double> getDefaultPrices() {
        return defaultPrices;
    }
}
