package com.example.swoos.service.serviceimpl;
import com.example.swoos.model.Reason;
import com.example.swoos.repository.ReasonRepository;
import com.example.swoos.service.ReasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReasonServiceImpl implements ReasonService {
    private final ReasonRepository reasonRepository;

    @Autowired
    public ReasonServiceImpl(ReasonRepository reasonRepository) {
        this.reasonRepository = reasonRepository;
    }

    public void addReason(int rowId, String reason) {
        Reason newReason = new Reason(rowId, reason);
        reasonRepository.save(newReason);
    }

    public Reason getLastReason(int rowId) {
        List<Reason> reasons = reasonRepository.findByRowIdOrderByCreatedDateDesc(rowId);
        return reasons.isEmpty() ? null : reasons.get(0); // Get the latest reason
    }

    public Reason getPreviousReason(int rowId) {
        List<Reason> reasons = reasonRepository.findByRowIdOrderByCreatedDateDesc(rowId);
        return reasons.size() < 2 ? null : reasons.get(1); // Get the second latest
    }
}



