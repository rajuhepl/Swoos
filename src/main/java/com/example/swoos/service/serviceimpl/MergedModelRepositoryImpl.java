package com.example.swoos.service.serviceimpl;

import com.example.swoos.model.MergedModel;
import com.example.swoos.service.MergedModelRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MergedModelRepositoryImpl implements MergedModelRepositoryCustom {

    @Autowired
     EntityManager entityManager;
    @Override

    public Page<MergedModel> findAllOrderByValueLossDescPageable(
            String field,
            String searchTerm,
            Pageable pageable) {

        String baseQuery = "SELECT m FROM MergedModel m WHERE FUNCTION('DATE', m.createAt) = FUNCTION('CURDATE') AND m.historyFlag = false";
        String countQueryStr = "SELECT COUNT(m) FROM MergedModel m WHERE FUNCTION('DATE', m.createAt) = FUNCTION('CURDATE') AND m.historyFlag = false";

        if (searchTerm != null && !searchTerm.isEmpty()) {
            baseQuery += " AND LOWER(m." + field + ") LIKE :searchTerm";
            countQueryStr += " AND LOWER(m." + field + ") LIKE :searchTerm";
        }

        TypedQuery<MergedModel> query = entityManager.createQuery(baseQuery, MergedModel.class);
        if (searchTerm != null && !searchTerm.isEmpty()) {
            query.setParameter("searchTerm", "%" + searchTerm.toLowerCase() + "%");
        }
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<MergedModel> results;
        results = query.getResultList();

        Query countQuery = entityManager.createQuery(countQueryStr);
        if (searchTerm != null && !searchTerm.isEmpty()) {
            countQuery.setParameter("searchTerm", "%" + searchTerm.toLowerCase() + "%");
        }

        Long total = (Long) countQuery.getSingleResult();
        return new PageImpl<>(results, pageable, total);
    }

}
