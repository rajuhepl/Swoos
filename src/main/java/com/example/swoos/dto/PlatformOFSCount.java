package com.example.swoos.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class PlatformOFSCount {

    private Map<String, Long> nationalMap;
    private Map<String, Long> quickComMap;
    private Map<String, Long> groceryMap;
    private Map<String, Long> beautyMap;

    public PlatformOFSCount(Map<String, Long> nationalMap, Map<String, Long> quickComMap,
                            Map<String, Long> groceryMap, Map<String, Long> beautyMap) {
        this.nationalMap = nationalMap;
        this.quickComMap = quickComMap;
        this.groceryMap = groceryMap;
        this.beautyMap = beautyMap;
    }


}
