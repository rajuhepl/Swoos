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
import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {
    @Autowired
    private MergedRepository mergedRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Override
    public SuccessResponse<Object> getDashboardCalculation(String platform,
                                                           String channel,
                                                           long productId,
                                                           LocalDate from,
                                                           LocalDate to) {
        SuccessResponse<Object> response = new SuccessResponse<>();
        DashboardCalcDto calcDto;
        List<MergedModelProjection> mergedModels ;
            if(productId!=0){
                mergedModels = mergedRepository.findByIdProduct(productId);
                calcDto = swoosLoss(mergedModels);
                calcDto.setSwoosContribution(mergedModels.get(0).getSWOOSContribution());
                response.setData(calcDto);
                return response;
            }
        if (from == null&&to == null) {
            mergedModels = datesNotPresented(platform, channel);
        }else{
            mergedModels = datesPresented(platform, channel, from, to);
        }
        assert mergedModels != null;
        response.setData(swoosLoss(mergedModels));
        return response;
    }


    @Override
    public SuccessResponse<Object> getProductList(String platform,
                                                  String channel,
                                                  LocalDate fromDate,
                                                  LocalDate toDate) {
        SuccessResponse<Object> response = new SuccessResponse<>();
        List<Object[]> mergedModels;
        if (fromDate == null&&toDate == null) {
            mergedModels = datesNotPresentedGetProducts(platform, channel);
        }else{
            mergedModels = datesPresentedGetProducts(platform, channel, fromDate, toDate);
        }
        List<ProductDto> products = mergedModels.stream()
                .map(mergedModel -> {
                    ProductDto productDto = new ProductDto();
                    productDto.setProductName((String) mergedModel[0]);
                    productDto.setId((long) mergedModel[1]);
                    return productDto;
                })
                .toList();
        response.setData(products);

        return response;
    }

    private List<Object[]> datesNotPresentedGetProducts(String platform, String channel) {
        List<Object[]> mergedModels;
        if (platform !=null) {
            if(channel !=null){
                mergedModels=   mergedRepository.findAllByPlatformProduct(channel);
            }else{
                mergedModels = mergedRepository.findAllByPlatformsNativeProducts(getChannels(platform));
            }
        }else{
            mergedModels = mergedRepository.getAllProducts();
        }
        return mergedModels;
    }


    private List<Object[]> datesPresentedGetProducts(String platform, String channel, LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = LocalDateTime.of(from, LocalTime.MIN);
        LocalDateTime toDateTime = LocalDateTime.of(to, LocalTime.MAX);
        Timestamp fromDate = Timestamp.valueOf(fromDateTime);
        Timestamp toDate = Timestamp.valueOf(toDateTime);

        List<Object[]> mergedModels;
        if (platform !=null) {

            if(channel !=null){
                mergedModels=   mergedRepository.findAllByPlatformDateProducts(channel,fromDate,toDate);
            }else{
                mergedModels = mergedRepository.findAllByPlatformsNativeDateProduct(getChannels(platform),fromDate,toDate);
            }
        }else{
            mergedModels = mergedRepository.getAllPlatformProductsDate(fromDate,toDate);
        }
        return mergedModels;

    }


    private List<MergedModelProjection> datesNotPresented(String platform, String channel) {
        List<MergedModelProjection> mergedModels;
        if (platform !=null) {

            if(channel !=null){
                mergedModels=   mergedRepository.findAllByPlatform(channel);
            }else{
                mergedModels = mergedRepository.findAllByPlatformsNative(getChannels(platform));
            }
        }else{
            mergedModels = mergedRepository.getAll();
        }
        return mergedModels;
    }

    private List<MergedModelProjection> datesPresented(String platform, String channel, LocalDate from,LocalDate to) {
        LocalDateTime fromDateTime = LocalDateTime.of(from, LocalTime.MIN);
        LocalDateTime toDateTime = LocalDateTime.of(to, LocalTime.MAX);
        Timestamp fromDate = Timestamp.valueOf(fromDateTime);
        Timestamp toDate = Timestamp.valueOf(toDateTime);

        List<MergedModelProjection> mergedModels;
        if (platform !=null) {

            if(channel !=null){
                mergedModels=   mergedRepository.findAllByPlatformDate(channel,fromDate,toDate);
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
        double swoosLossTotal = 0;
        double revenue = 0;
        double swoosContribution = 0;
        double totalValueLoss = 0;
        Map<String,Long>reasonLevelCount = new HashMap<>();
        Map<String,Double> locationLossMap = new HashMap<>();
        for (MergedModelProjection model : mergedModels) {
            String swoosCont = model.getSWOOSContribution();
            String valueLoss =  model.getValueLoss();
            String rev = model.getRevenue();
            String reason= "No Reasons";
            if (model.getReason()!=null) {
                reason =  model.getReason();
            }
            try {
                long count = reasonLevelCount.getOrDefault(reason,0L);
                reasonLevelCount.put(reason,count+1);
                swoosLossTotal += Double.parseDouble(swoosCont.replace("%", ""));
                revenue +=(long) Double.parseDouble(rev) ;
                swoosContribution += Double.parseDouble(swoosCont.replace("%", ""));
                totalValueLoss += Double.parseDouble(valueLoss.replace("%", ""));
            } catch (NumberFormatException e) {
                // Handle the exception as needed
                // For example, log the error and continue with the next model
                System.err.println("Invalid number format for model ");
            }
            locationLossCalculations(model,locationLossMap);
        }

        double percentage = 0;
        if (revenue > 0) {
            percentage = (swoosLossTotal / revenue) * 100;
        }

        double swoowCont = 0;
        if (!mergedModels.isEmpty()) {
            swoowCont = (swoosContribution / mergedModels.size());
        }

        double totalLoss = 0;
        if (!mergedModels.isEmpty()) {
            totalLoss = (totalValueLoss / revenue) * 100;
        }

        calcDto.setValueLoss(String.format("%.2f%%", totalLoss));
        calcDto.setSwoosContribution(String.format("%.2f%%", swoowCont));
        calcDto.setSwoosLoss(String.format("%.2f%%", percentage));
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


}
