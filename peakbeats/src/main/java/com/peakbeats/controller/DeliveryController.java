package com.peakbeats.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private static final String ORS_URL = "https://api.openrouteservice.org/v2/directions/driving-car";
    private static final String ORS_API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjI4MDU5ZWNiNjhhYTQ3NDZhZDczNDNmZDg5ODEyYWEzIiwiaCI6Im11cm11cjY0In0=";

    // Lagerstandort Oldenburg
    private static final double BASE_LAT = 53.117720;
    private static final double BASE_LNG = 8.221091;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("rawtypes")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDeliveryPrice(@RequestParam String zip) {
        try {
            // 1️⃣ Geocoding PLZ -> Koordinaten
            String geocodeUrl = "https://nominatim.openstreetmap.org/search?postalcode=" + zip + "&country=DE&format=json";
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> geoRes = restTemplate.getForObject(geocodeUrl, List.class);

            if (geoRes == null || geoRes.isEmpty()) {
                return ResponseEntity.ok(Map.of("notAvailable", true, "price", 0, "km", 0));
            }

            double destLat = Double.parseDouble((String) geoRes.get(0).get("lat"));
            double destLng = Double.parseDouble((String) geoRes.get(0).get("lon"));

            // 2️⃣ Route berechnen
            String body = "{ \"coordinates\": [[" + BASE_LNG + "," + BASE_LAT + "],[" + destLng + "," + destLat + "]] }";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", ORS_API_KEY);
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> routeRes = restTemplate.exchange(ORS_URL, HttpMethod.POST, entity, Map.class);

            Map<?, ?> routesMap = (Map<?, ?>) ((List<?>) routeRes.getBody().get("routes")).get(0);
            Map<?, ?> summary = (Map<?, ?>) routesMap.get("summary");

            double distanceMeters = ((Number) summary.get("distance")).doubleValue();
            int km = (int) Math.round(distanceMeters / 1000.0);

            // 3️⃣ Lieferpreis berechnen
            int price;
            if (km <= 5) {
                price = 5;
            } else if (km <= 15) {
                price = km; // 1€/km
            } else {
                price = km * 2; // 2€/km
            }

            Map<String, Object> result = new HashMap<>();
            result.put("price", price);
            result.put("km", km);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("notAvailable", true, "price", 0, "km", 0));
        }
    }
}
