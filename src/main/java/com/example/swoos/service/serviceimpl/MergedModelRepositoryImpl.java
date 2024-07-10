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
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class MergedModelRepositoryImpl implements MergedModelRepositoryCustom {

    @Autowired
    private EntityManager entityManager;
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

//        baseQuery += " ORDER BY CAST(m.ValueLoss AS double) DESC";

        TypedQuery<MergedModel> query = entityManager.createQuery(baseQuery, MergedModel.class);
        if (searchTerm != null && !searchTerm.isEmpty()) {
            query.setParameter("searchTerm", "%" + searchTerm.toLowerCase() + "%");
        }
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<MergedModel> results;
        try {
            results = query.getResultList();
        } catch (Exception e) {
            e.printStackTrace(); // For debugging any potential issues with the query execution
            throw e;
        }

        Query countQuery = entityManager.createQuery(countQueryStr);
        if (searchTerm != null && !searchTerm.isEmpty()) {
            countQuery.setParameter("searchTerm", "%" + searchTerm.toLowerCase() + "%");
        }

        Long total = (Long) countQuery.getSingleResult();

        // Debugging output
        System.out.println("Base Query: " + baseQuery);
        System.out.println("Count Query: " + countQueryStr);
        System.out.println("Search Term: " + searchTerm);
        System.out.println("Total Records: " + total);
        System.out.println("Result Size: " + results.size());

        return new PageImpl<>(results, pageable, total);
    }


/*    @Override
    public Page<MergedModel> findAllOrderByValueLossDescPageable(
            Timestamp fromDate,
            Timestamp toDate,
            String field,
            String searchTerm,
            Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<MergedModel> query = cb.createQuery(MergedModel.class);
        Root<MergedModel> root = query.from(MergedModel.class);

        Predicate datePredicate = cb.between(root.get("createAt"), fromDate, toDate);
        Predicate historyFlagPredicate = cb.isFalse(root.get("historyFlag"));
        Predicate searchPredicate = cb.like(root.get(field).as(String.class), "%" + searchTerm + "%");

        query.select(root).where(cb.and(datePredicate, historyFlagPredicate, searchPredicate))
                .orderBy(cb.desc(cb.function("CAST", Double.class, root.get("ValueLoss"))));

        TypedQuery<MergedModel> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<MergedModel> results = typedQuery.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<MergedModel> countRoot = countQuery.from(MergedModel.class);
        countQuery.select(cb.count(countRoot))
                .where(cb.and(datePredicate, historyFlagPredicate, searchPredicate));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }*/

}
