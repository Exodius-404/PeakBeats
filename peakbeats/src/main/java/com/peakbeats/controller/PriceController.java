package com.peakbeats.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.peakbeats.service.AdminService;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PriceController {

    private final AdminService adminService;

    public PriceController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/prices")
    public Map<String, Double> getPrices() {
        return adminService.getPrices();
    }
}
