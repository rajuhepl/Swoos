package com.example.swoos.service.serviceimpl;

import com.example.swoos.model.Remarks;
import com.example.swoos.repository.RemarksRepository;
import com.example.swoos.service.RemarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class RemarkServiceImpl implements RemarkService {
@Autowired
    RemarksRepository remarksRepository;

    public Remarks createRemark(Remarks remarks) {
        return remarksRepository.save(remarks);

    }

}
