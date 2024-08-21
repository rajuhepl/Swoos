package com.example.swoos.service.serviceimpl;

import com.example.swoos.dto.DashboardCalcDto;
import com.example.swoos.dto.MergedModelProjection;
import com.example.swoos.dto.ProductDto;
import com.example.swoos.repository.MergedRepository;
import com.example.swoos.response.SuccessResponse;
import com.example.swoos.service.DashboardService;
import com.example.swoos.util.Constant;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {
    @Autowired
    private MergedRepository mergedRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Override
    public SuccessResponse<Object> getDashboardCalculation(String platform,
                                                           String channel,
                                                           String productId,
                                                           LocalDate from,
                                                           LocalDate to) {
        SuccessResponse<Object> response = new SuccessResponse<>();
        DashboardCalcDto calcDto;
        List<MergedModelProjection> mergedModels ;

        if (from == null && to == null) {
            mergedModels = datesNotPresented(platform,productId, channel);
        }else{
            mergedModels = datesPresented(platform,productId, channel, from, to);
        }
        assert mergedModels != null;
        response.setData(swoosLoss(mergedModels));
        return response;
    }


    @Override
    public SuccessResponse<Object> getProductList(String platform,
                                                  String channel,
                                                  LocalDate fromDate,
                                                  LocalDate toDate,
                                                  String search) {
        SuccessResponse<Object> response = new SuccessResponse<>();
        List<Object[]> mergedModels;
        if (fromDate == null&&toDate == null) {
            mergedModels = datesNotPresentedGetProducts(platform, channel,search);
        }else{
            mergedModels = datesPresentedGetProducts(platform, channel, fromDate, toDate,search);
        }
        Map<String, ProductDto> productDtoMap = mergedModels.stream()
                .map(mergedModel -> {
                    ProductDto productDto = new ProductDto();
                    productDto.setProductName((String) mergedModel[0]);
                    productDto.setId((String) mergedModel[1]);
                    productDto.setChannel((String) mergedModel[2]);
                    Timestamp createAt = (Timestamp) mergedModel[3];
                    productDto.setDate(createAt.toInstant().atZone(ZoneOffset.UTC).toLocalDateTime().toString());
                    productDto.setPlatform(getPlatform(productDto.getChannel()));
                    return productDto;
                })
                // Collect into a LinkedHashMap to keep the order and ensure uniqueness based on ID
                .collect(Collectors.toMap(
                        ProductDto::getId,
                        Function.identity(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
        response.setData(productDtoMap.values().toArray());

        return response;
    }

    private List<Object[]> datesNotPresentedGetProducts(String platform, String channel,String search) {
        List<Object[]> mergedModels;
        if (search==null) {
            if (platform !=null) {
                if(channel !=null){
                    mergedModels=   mergedRepository.findAllByPlatformProduct(channel);
                }else{
                    mergedModels = mergedRepository.findAllByPlatformsNativeProducts(getChannels(platform));
                }
            }else{
                mergedModels = mergedRepository.getAllProducts();
            }
        }else{
            if (platform != null) {
                if (channel != null) {
                    mergedModels = mergedRepository.findAllByPlatformProduct(channel, search);
                } else {
                    mergedModels = mergedRepository.findAllByPlatformsNativeProducts(getChannels(platform), search);
                }
            } else {
                mergedModels = mergedRepository.getAllProducts(search);
            }
        }
        return mergedModels;
    }


    private List<Object[]> datesPresentedGetProducts(String platform, String channel, LocalDate from, LocalDate to,String search) {
        LocalDateTime fromDateTime = LocalDateTime.of(from, LocalTime.MIN);
        LocalDateTime toDateTime = LocalDateTime.of(to, LocalTime.MAX);
        Timestamp fromDate = Timestamp.valueOf(fromDateTime);
        Timestamp toDate = Timestamp.valueOf(toDateTime);

        List<Object[]> mergedModels;
        if (search==null) {
            if (platform !=null) {
                if(channel !=null){
                    mergedModels=   mergedRepository.findAllByPlatformDateProducts(channel,fromDate,toDate);
                }else{
                    mergedModels = mergedRepository.findAllByPlatformsNativeDateProduct(getChannels(platform),fromDate,toDate);
                }
            }else{
                mergedModels = mergedRepository.getAllPlatformProductsDate(fromDate,toDate);
            }
        }else{
            if (platform != null) {
                if (channel != null) {
                    mergedModels = mergedRepository.findAllByPlatformDateProductsSearch(channel, fromDate, toDate, search);
                } else {
                    mergedModels = mergedRepository.findAllByPlatformsNativeDateProductSearch(getChannels(platform), fromDate, toDate, search);
                }
            } else {
                mergedModels = mergedRepository.getAllPlatformProductsDateSearch(fromDate, toDate, search);
            }
        }
        return mergedModels;

    }


    private List<MergedModelProjection> datesNotPresented(String platform,String productId, String channel) {
        List<MergedModelProjection> mergedModels;

        if (platform !=null) {
            if(channel !=null){
                mergedModels=   mergedRepository.findAllByPlatform(channel);
            }else{
                mergedModels = mergedRepository.findAllByPlatformsNative(getChannels(platform));
            }
        } else if (productId!=null) {
            mergedModels = mergedRepository.findByIdProduct(productId);
        } else{
            mergedModels = mergedRepository.getAll();
        }
        return mergedModels;
    }

    private List<MergedModelProjection> datesPresented(String platform, String channel,String productId, LocalDate from,LocalDate to) {
        LocalDateTime fromDateTime = LocalDateTime.of(from, LocalTime.MIN);
        LocalDateTime toDateTime = LocalDateTime.of(to, LocalTime.MAX);
        Timestamp fromDate = Timestamp.valueOf(fromDateTime);
        Timestamp toDate = Timestamp.valueOf(toDateTime);

        List<MergedModelProjection> mergedModels;
        if (platform !=null) {
            if(channel !=null){
                mergedModels=   mergedRepository.findAllByPlatformDate(channel,fromDate,toDate);
            }else if (productId!=null) {
                mergedModels = mergedRepository.findByIdProductAndDate(productId,fromDate,toDate);
            }else{
                mergedModels = mergedRepository.findAllByPlatformsNativeDate(getChannels(platform),fromDate,toDate);
            }
        }else{
            mergedModels = mergedRepository.getAllPlat(fromDate,toDate);
        }
        return mergedModels;
    }

    private DashboardCalcDto swoosLoss(List<MergedModelProjection> mergedModels) {
        DashboardCalcDto calcDto = new DashboardCalcDto();
        double daySalesTotal = 0;
        double revenue = 0;
        int quantityLoss = 0;
        double valueLoss = 0;
        Map<String,Long>reasonLevelCount = new HashMap<>();
        Map<String,Double> locationLossMap = new HashMap<>();
        for (MergedModelProjection model : mergedModels) {
            String daySales = model.getDaySales();
            String valueLossString =  model.getValueLoss();
            String rev = model.getRevenue();
            int unit =Integer.parseInt(model.getMonthlySales());
            int monthUnit = unit * 30;
            String reason= "No Reasons";
            if (model.getReason()!=null) {
                reason =  model.getReason();
            }
            try {
                long count = reasonLevelCount.getOrDefault(reason,0L);
                reasonLevelCount.put(reason,count+1);
                daySalesTotal += Double.parseDouble(daySales.replace("%", ""));
                revenue +=(long) Double.parseDouble(rev) ;
                quantityLoss += unit/monthUnit;
                valueLoss += Double.parseDouble(valueLossString.replace("%", ""));
            } catch (NumberFormatException e) {
                System.err.println("Invalid number format for model ");
            }
            locationLossCalculations(model,locationLossMap);
        }
        double swoosLoss = 0;
        if (revenue > 0) {
            swoosLoss = (valueLoss / daySalesTotal);
        }
        int quantityLos = 0;
        if (!mergedModels.isEmpty()) {
            quantityLos = (quantityLoss / mergedModels.size());
        }
        double totalLoss = 0;
        if (!mergedModels.isEmpty()) {
            totalLoss = (valueLoss / revenue);
        }
        calcDto.setValueLoss(String.format("%.2f%%", totalLoss));
        calcDto.setQuantityLoss(String.valueOf(quantityLos));
        calcDto.setSwoosLoss(String.format("%.2f%%", swoosLoss));
        calcDto.setReasonLevelCount(reasonLevelCount);
        calcDto.setLocationLevelCount(locationLossMap);
        return calcDto;
    }


    private List<String> getChannels(String platform){
        if(platform.equalsIgnoreCase("National")){
            return List.of("Amazon", "Flipkart");
        }else if(platform.equalsIgnoreCase("QuickCom")){
            return List.of("Swiggy", "Zepto","Blinkit");
        }else if(platform.equalsIgnoreCase("Grocery")){
            return List.of("Flipkart Grocery", "BigBasket");
        }else if(platform.equalsIgnoreCase("Beauty")){
            return List.of("Myntra", "Nykaa", "Purplle");
        }else{
            return List.of("Amazon", "Flipkart");
        }
    }
    public String getPlatform(String channel) {
        return switch (channel.toLowerCase()) {
            case "swiggy", "zepto", "blinkit" -> "QuickCom";
            case "flipkart grocery", "bigbasket" -> "Grocery";
            case "myntra", "nykaa", "purplle" -> "Beauty";
            default -> "National";
        };
    }

    private void locationLossCalculations(MergedModelProjection location,
                                                  Map<String,Double> locationLossMap){
            String[] cityNames = {
                    "Pune", "Other", "Patna", "Mumbai", "Indore",
                    "Hyderabad", "Delhi", "Chennai", "Calcutta",
                    "Bangalore", "Ahmedabad"
            };

            // Array of city values from the location object
            String[] cities = {
                    location.getPune(),
                    location.getOther(),
                    location.getPatna(),
                    location.getMumbai(),
                    location.getIndore(),
                    location.getHyderabad(),
                    location.getDelhi(),
                    location.getChennai(),
                    location.getCalcutta(),
                    location.getBangalore(),
                    location.getAhmedabad()
            };
            List<String> availableCities = new ArrayList<>();

            // Iterate over the array and check if each city is presented
            for (int i = 0; i < cities.length; i++) {
                if (isPresented(cities[i])) {
                    availableCities.add(cityNames[i]);
                }
            }
            double dividedCount =Double.parseDouble(location.getValueLoss())/availableCities.size();
            for (String city : availableCities) {
                double revenue = locationLossMap.getOrDefault(city,0D);
                locationLossMap.put(city,roundToTwoDecimalPlaces(revenue+dividedCount));
            }
        }
    private double roundToTwoDecimalPlaces(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    private boolean isPresented(String check){
        return check.equalsIgnoreCase(Constant.OUT_OF_STOCK) || check.equalsIgnoreCase("Available");
    }
    @Override
    public Map<String,Long> getPlatformSukCount(){
        List<Object[]> mergedModels = mergedRepository.getAllMergedModel();
        Map<String,Long>sukCountMap = new HashMap<>();
        for(Object[] model : mergedModels){
            String platform = (String) model[0];
            long sukCount = sukCountMap.getOrDefault(platform,0L);
            sukCountMap.put(platform,sukCount+1);
        }
        return sukCountMap;
    }
    public SuccessResponse<Object> getDashboardCalculationReason(String platform,
                                                           String channel,
                                                           String productId,
                                                           LocalDate from,
                                                           LocalDate to) {
        SuccessResponse<Object> response = new SuccessResponse<>();
        List<MergedModelProjection> mergedModels ;

        if (from == null && to == null) {
            mergedModels = datesNotPresented(platform,productId, channel);
        }else{
            mergedModels = datesPresented(platform,channel,productId , from, to);
        }
        Map<String, List<MergedModelProjection>> groupedByReason = mergedModels.stream()
                .collect(Collectors.groupingBy(MergedModelProjection::getReason));

        Map<String, DashboardCalcDto> reasonDashboardMap = groupedByReason.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> swoosLoss(entry.getValue())
                ));

        response.setData(reasonDashboardMap);
        return response;
    }


}
