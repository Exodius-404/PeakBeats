package com.peakbeats.service;

import com.peakbeats.model.Inquiry;
import com.peakbeats.repository.InquiryRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InquiryService {
    private final InquiryRepository repo;
    public InquiryService(InquiryRepository repo) { this.repo = repo; }

    public Inquiry saveInquiry(Inquiry inquiry) { return repo.save(inquiry); }
    public List<Inquiry> getAll() { return repo.findAll(); }
}
