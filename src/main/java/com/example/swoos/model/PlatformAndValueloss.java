package com.example.swoos.model;
import jakarta.persistence.*;
import lombok.Data;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PlatformAndValueloss {

    private Map<String, Long> nationalMap;
    private Map<String, Long> quickComMap;
    private Map<String, Long> groceryMap;
    private Map<String, Long> beautyMap;

    public PlatformAndValueloss(Map<String, Long> nationalMap, Map<String, Long> quickComMap,
                                Map<String, Long> groceryMap, Map<String, Long> beautyMap) {
        this.nationalMap = nationalMap;
        this.quickComMap = quickComMap;
        this.groceryMap = groceryMap;
        this.beautyMap = beautyMap;
    }


}

