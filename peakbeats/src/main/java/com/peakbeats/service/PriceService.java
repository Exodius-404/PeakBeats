package com.peakbeats.service;

import com.peakbeats.model.Price;
import com.peakbeats.repository.PriceRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@Service
public class PriceService {

    private final PriceRepository priceRepository;

    public PriceService(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }

    public Map<String, Double> getPriceMap() {
        Map<String, Double> map = new HashMap<>();
        for (Price p : priceRepository.findAll()) {
            map.put(p.getName(), p.getPrice());
        }
        return map;
    }

    public List<String> getBlockedList() {
        List<String> blocked = new ArrayList<>();
        for (Price p : priceRepository.findAll()) {
            if (p.isBlocked())
                blocked.add(p.getName());
        }
        return blocked;
    }

    public void updatePrices(Map<String, Double> prices) {
        List<Price> all = priceRepository.findAll();
        for (Price p : all) {
            if (prices.containsKey(p.getName())) {
                p.setPrice(prices.get(p.getName()));
            }
        }
        priceRepository.saveAll(all);
    }

    public void updateBlocked(List<String> blockedNames) {
        List<Price> all = priceRepository.findAll();
        for (Price p : all) {
            p.setBlocked(blockedNames.contains(p.getName()));
        }
        priceRepository.saveAll(all);
    }
}
