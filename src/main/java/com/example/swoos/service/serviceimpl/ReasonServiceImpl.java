package com.example.swoos.service.serviceimpl;
import com.example.swoos.exception.CustomValidationException;
import com.example.swoos.exception.ErrorCode;
import com.example.swoos.model.DropDownModel;
import com.example.swoos.model.Reason;
import com.example.swoos.repository.DropDownRepository;
import com.example.swoos.repository.ReasonRepository;
import com.example.swoos.service.ReasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReasonServiceImpl implements ReasonService {
    @Autowired
    DropDownRepository dropDownRepository;
    private final ReasonRepository reasonRepository;

    @Autowired
    public ReasonServiceImpl(ReasonRepository reasonRepository) {
        this.reasonRepository = reasonRepository;
    }

    public String addReason(long rowId, String reason) throws CustomValidationException {
        Reason reason1 = reasonRepository.findById(rowId)
                .orElse(new Reason());
        reason1.setReason(reason);
        reasonRepository.save(reason1);
        return "Reason Added";
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



