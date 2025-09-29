package com.peakbeats.controller;

import com.peakbeats.model.Inquiry;
import com.peakbeats.service.InquiryService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/inquiries")
@CrossOrigin(origins = "http://127.0.0.1:5502")
public class InquiryController {

    private final InquiryService service;
    public InquiryController(InquiryService service) { this.service = service; }

    @PostMapping
    public Inquiry create(@RequestBody Inquiry inquiry) {
        return service.saveInquiry(inquiry);
    }

    @GetMapping
    public List<Inquiry> list() {
        return service.getAll();
    }
}
