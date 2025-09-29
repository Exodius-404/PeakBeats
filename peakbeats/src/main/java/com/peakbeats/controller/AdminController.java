
package com.peakbeats.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.peakbeats.service.AdminService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://127.0.0.1:5502")
public class AdminController {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "PeakBeats2025!";
    private String currentToken = null;

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        if (ADMIN_USERNAME.equals(body.get("username")) && ADMIN_PASSWORD.equals(body.get("password"))) {
            currentToken = UUID.randomUUID().toString();
            return ResponseEntity.ok(Map.of("token", currentToken));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    private boolean isAuthorized(String token) {
        return token != null && token.equals(currentToken);
    }

    @GetMapping("/data")
    public ResponseEntity<?> getAdminData(@RequestHeader("Authorization") String token) {
        if (!isAuthorized(token))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Map<String, Object> response = new HashMap<>();
        response.put("prices", adminService.getPrices());
        response.put("blocked", adminService.getBlocked());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/prices")
    public ResponseEntity<Void> updatePrices(@RequestHeader("Authorization") String token,
            @RequestBody Map<String, Double> prices) {
        if (!isAuthorized(token))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        adminService.updatePrices(prices);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/blocked")
    public ResponseEntity<Void> updateBlocked(@RequestHeader("Authorization") String token,
            @RequestBody List<String> blocked) {
        if (!isAuthorized(token))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        adminService.updateBlocked(blocked);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset(@RequestHeader("Authorization") String token) {
        if (!isAuthorized(token))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        adminService.resetPrices();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/save")
    public ResponseEntity<Void> saveAll(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> body) {

        if (!isAuthorized(token)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        // Preise
        @SuppressWarnings("unchecked")
		Map<String, Double> prices = (Map<String, Double>) body.get("prices");
        if (prices != null) {
            adminService.updatePrices(prices);
        }

        // Blockierte
        @SuppressWarnings("unchecked")
		List<String> blocked = (List<String>) body.get("blocked");
        if (blocked != null) {
            adminService.updateBlocked(blocked);
        }

        return ResponseEntity.ok().build();
    }
}
