package com.example.swoos.service.serviceimpl;

import com.example.swoos.repository.MergedRepository;
import com.example.swoos.service.SalesLossService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SalesLossServiceImpl implements SalesLossService {

    @Autowired
    private MergedRepository mergedRepository;

    public Map<String, Integer> getLocationWiseSalesLoss() {
        List<Object[]> data = mergedRepository.findLocationWiseSalesLoss();
        Map<String, Integer> result = new HashMap<>();
        for (Object[] row : data) {
            result.put((String) row[0], (Integer) row[1]);
        }

        return result;
    }
}
