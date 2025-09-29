package com.peakbeats.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Inquiry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vorname;
    private String nachname;
    private String email;
    private String phone;
    private String startDate;
    private String endDate;
    private String packageName;
    private String tech;
    private String message;
    private String zip;
    private Double deliveryPrice;
    private String estimate;
    private Boolean liability;

    private LocalDateTime createdAt = LocalDateTime.now();
}
