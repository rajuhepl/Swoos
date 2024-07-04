package com.example.swoos.service.serviceimpl;

import com.example.swoos.model.DropDownModel;
import com.example.swoos.repository.DropDownRepository;
import com.example.swoos.service.DropDownService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DropDownServiceImpl implements DropDownService {
    @Autowired
    private DropDownRepository dropDownRepository;

    public List<DropDownModel> getAllIssues() {
        return dropDownRepository.findAll();
    }

    public DropDownModel createIssue(DropDownModel issue) {
        return dropDownRepository.save(issue);
    }


    @Transactional
    public void deleteDropDownById(Long dropdownId) {
        if (dropDownRepository.existsById(dropdownId)) {
            dropDownRepository.deleteById(dropdownId);
        } else {
            throw new IllegalArgumentException("Dropdown with ID " + dropdownId + " does not exist.");
        }
    }
}