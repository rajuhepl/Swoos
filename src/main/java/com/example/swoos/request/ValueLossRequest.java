package com.example.swoos.request;

import com.example.swoos.model.MergedModel;
import com.example.swoos.model.PlatformAndValueloss;

import java.util.List;
import java.util.Map;

public class ValueLossRequest {
    private List<String> distinctPlatforms;
    private List<String> distinctLocations;
    private List<MergedModel> mergedList;
    private Map<String, Integer> outOfStockCounts;
    private PlatformAndValueloss platformAndValueLoss;
    private List<Object> national;

    // Getters and setters for all fields
    public List<String> getDistinctPlatforms() {
        return distinctPlatforms;
    }

    public void setDistinctPlatforms(List<String> distinctPlatforms) {
        this.distinctPlatforms = distinctPlatforms;
    }

    public List<String> getDistinctLocations() {
        return distinctLocations;
    }

    public void setDistinctLocations(List<String> distinctLocations) {
        this.distinctLocations = distinctLocations;
    }

    public List<MergedModel> getMergedList() {
        return mergedList;
    }

    public void setMergedList(List<MergedModel> mergedList) {
        this.mergedList = mergedList;
    }

    public Map<String, Integer> getOutOfStockCounts() {
        return outOfStockCounts;
    }

    public void setOutOfStockCounts(Map<String, Integer> outOfStockCounts) {
        this.outOfStockCounts = outOfStockCounts;
    }

    public PlatformAndValueloss getPlatformAndValueLoss() {
        return platformAndValueLoss;
    }

    public void setPlatformAndValueLoss(PlatformAndValueloss platformAndValueLoss) {
        this.platformAndValueLoss = platformAndValueLoss;
    }

    public List<Object> getNational() {
        return national;
    }

    public void setNational(List<Object> national) {
        this.national = national;
    }
}

