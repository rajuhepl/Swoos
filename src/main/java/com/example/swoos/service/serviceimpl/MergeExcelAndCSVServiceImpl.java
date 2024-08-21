package com.example.swoos.service.serviceimpl;
import com.example.swoos.dto.DataListProjection;
import com.example.swoos.dto.HistoryDto;
import com.example.swoos.dto.MergedModelDto;
import com.example.swoos.model.*;
import com.example.swoos.repository.*;
import com.example.swoos.response.PageResponse;
import com.example.swoos.response.SuccessResponse;
import com.example.swoos.service.MergeExcelAndCSVService;
import com.example.swoos.service.MergedModelRepositoryCustom;
import com.example.swoos.util.Constant;
import jakarta.servlet.http.HttpServletResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;

import java.net.Inet4Address;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
@Service
public class MergeExcelAndCSVServiceImpl implements MergeExcelAndCSVService {
    @Autowired
    private ExcelRepository excelRepository ;
    @Autowired
    private CsvRepository csvRepository ;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private MergedRepository mergedRepository;
    @Autowired
    private PlatformRepository platformRepository;

    public void readDataFromFile() {
        List<ExcelModel> dataList = excelRepository.findByTriggeredOnToday();
        List<CSVModel> csvModelList = csvRepository.findAllDataForMonth(csvRepository.findLastLoadedMonth());
        mergeLists(csvModelList, dataList);
    }
    private void mergeLists(List<CSVModel> csvData, List<ExcelModel> excelData) {
        Map<String, List<ExcelModel>> excelDataMap = groupExcelDataByASIN(excelData);
        mergeCSVAndExcelData(csvData, excelDataMap);
    }
    private Map<String, List<ExcelModel>> groupExcelDataByASIN(List<ExcelModel> excelData) {
        return excelData.stream()
                .collect(Collectors.groupingBy(ExcelModel::getAsin));
    }
    private void mergeCSVAndExcelData(List<CSVModel> csvData, Map<String, List<ExcelModel>> excelDataMap) {
        List<MergedModel> mergedList = new ArrayList<>();
        List<MergedModel> discontinued = mergedRepository.findAllDiscontinuedOrLocationNotAlign();
        for (CSVModel csvModel : csvData) {
            List<ExcelModel> excelModels = excelDataMap.get(csvModel.getMainSKUCode());
            if (excelModels!= null) {
                MergedModel mergedModel = createMergedModel(csvModel, excelModels,discontinued);
                if (!mergedModel.getValueLoss().equals("0.00")) {
                    mergedList.add(mergedModel);
                }
            }
        }
        calculateContributionAndSave(mergedList);
    }
    private void calculateContributionAndSave(List<MergedModel> mergedList) {
        List<String> distinctPlatforms = mergedList.stream()
                .map(MergedModel::getPlatform)
                .distinct()
                .toList();
        List<MergedModel> finalMergedList = new ArrayList<>();
        for (String platform : distinctPlatforms) {
            List<MergedModel> filteredMergedList = mergedList.stream()
                    .filter(mergedModel -> mergedModel.getPlatform().equals(platform))
                    .toList();

            for (MergedModel mergedModel : filteredMergedList) {
                double totalValueLoss = filteredMergedList.stream()
                        .filter(merged -> merged.getPlatform().equals(mergedModel.getPlatform()))
                        .mapToDouble(merged -> Double.parseDouble(merged.getValueLoss()))
                        .sum();
                double contribution = totalValueLoss > 0? (Double.parseDouble(mergedModel.getValueLoss()) / totalValueLoss) * 100 : 0;
                String formattedLoss = String.format("%.2f ", contribution);
                mergedModel.setSWOOSContribution(formattedLoss + "%");
                finalMergedList.add(mergedModel);
            }
        }

        mergedRepository.saveAll(finalMergedList);
        System.out.println("Data Loaded to DataBase");

    }
    public PlatformAndValueloss platformAndValueloss() {
        List<MergedModel> mergedList = mergedRepository.findAll();
        Map<String, Integer> outOfStockCounts = countOutOfStockByPlatform(mergedList);

        PlatformAndValueloss platformAndValueloss = new PlatformAndValueloss();
        List<Platform> national = new ArrayList<>();
        List<Platform> beauty = new ArrayList<>();
        List<Platform> grocery = new ArrayList<>();
        List<Platform> quickComm = new ArrayList<>();

        Map<String, Double> platformValueLossMap = calculatePlatformValueLoss(mergedList);
        savePlatforms(platformValueLossMap, outOfStockCounts, national, beauty, grocery, quickComm);

        platformAndValueloss.setNational(national);
        platformAndValueloss.setBeauty(beauty);
        platformAndValueloss.setGrocery(grocery);
        platformAndValueloss.setQuickCom(quickComm);

        double flipkartValueLoss = platformValueLossMap.getOrDefault(Constant.FLIPKART  , 0.0);
        double amazonValueLoss = platformValueLossMap.getOrDefault(Constant.AMAZON  , 0.0);
        double totalFlipkartAmazon = flipkartValueLoss + amazonValueLoss;

        platformAndValueloss.setTotalFlipkartAmazon(totalFlipkartAmazon);
        platformAndValueloss.setFlipkartPercentage(calculatePercentage(flipkartValueLoss, totalFlipkartAmazon));
        platformAndValueloss.setAmazonPercentage(calculatePercentage(amazonValueLoss, totalFlipkartAmazon));
        platformAndValueloss.setCountAmazon(outOfStockCounts.getOrDefault(Constant.AMAZON  , 0));
        platformAndValueloss.setCountFlipkart(outOfStockCounts.getOrDefault(Constant.FLIPKART  , 0));

        return platformAndValueloss;
    }


    private Map<String, Double> calculatePlatformValueLoss(List<MergedModel> mergedList) {
        return mergedList.stream()
                .collect(Collectors.groupingBy(
                        MergedModel::getPlatform,
                        Collectors.summingDouble(model -> Double.parseDouble(model.getValueLoss()))
                ));
    }

    private void savePlatforms(Map<String, Double> platformValueLossMap, Map<String, Integer> outOfStockCounts,
                               List<Platform> national, List<Platform> beauty, List<Platform> grocery, List<Platform> quickComm) {
        platformValueLossMap.forEach((platformName, totalValueLoss) -> {
            Platform platform = platformRepository.findByName(platformName);
            if (platform == null) {
                platform = new Platform();
                platform.setName(platformName);
            }

            platform.setValueLoss(totalValueLoss);
            platform.setOutOfStockCount(outOfStockCounts.getOrDefault(platformName, 0));

            double percentage = calculateCategoryPercentage(platformName, platformValueLossMap);
            platform.setPercentage(percentage);

            platformRepository.save(platform);

            categorizePlatform(platform, national, beauty, grocery, quickComm);
        });
    }

    private double calculateCategoryPercentage(String platformName, Map<String, Double> platformValueLossMap) {
        double totalCategoryLoss = platformValueLossMap.entrySet().stream()
                .filter(entry -> isCategoryPlatform(platformName, entry.getKey()))
                .mapToDouble(Map.Entry::getValue)
                .sum();

        return totalCategoryLoss != 0 ? (platformValueLossMap.get(platformName) / totalCategoryLoss) * 100 : 0;
    }

    private boolean isCategoryPlatform(String platformName, String key) {
        return (Constant.FLIPKART .equalsIgnoreCase(platformName) && (Constant.FLIPKART .equalsIgnoreCase(key) || Constant.AMAZON .equalsIgnoreCase(key))) ||
                (Constant.MYNTRA.equalsIgnoreCase(platformName) && (Constant.MYNTRA.equalsIgnoreCase(key) || Constant.NYKAA.equalsIgnoreCase(key) || Constant.PURPLLE.equalsIgnoreCase(key))) ||
                (Constant.FK_GROCERY .equalsIgnoreCase(platformName) && (Constant.FK_GROCERY.equalsIgnoreCase(key) || Constant.BIG_BASKET.equalsIgnoreCase(key))) ||
                (Constant.SWIGGY .equalsIgnoreCase(platformName) && (Constant.SWIGGY.equalsIgnoreCase(key) || Constant.BLINKIT.equalsIgnoreCase(key) || Constant.ZEPTO.equalsIgnoreCase(key)));
    }

    private void categorizePlatform(Platform platform, List<Platform> national, List<Platform> beauty, List<Platform> grocery, List<Platform> quickComm) {
        String platformName = platform.getName();

        if (isNationalPlatform(platformName)) {
            addToCategory(platform, national);
        } else if (isBeautyPlatform(platformName)) {
            addToCategory(platform, beauty);
        } else if (isGroceryPlatform(platformName)) {
            addToCategory(platform, grocery);
        } else if (isQuickCommPlatform(platformName)) {
            addToCategory(platform, quickComm);
        }
    }

    private boolean isNationalPlatform(String platformName) {
        return Constant.FLIPKART.equalsIgnoreCase(platformName) || Constant.AMAZON.equalsIgnoreCase(platformName);
    }

    private boolean isBeautyPlatform(String platformName) {
        return Constant.MYNTRA.equalsIgnoreCase(platformName) || Constant.NYKAA.equalsIgnoreCase(platformName) || Constant.PURPLLE.equalsIgnoreCase(platformName);
    }

    private boolean isGroceryPlatform(String platformName) {
        return Constant.FK_GROCERY.equalsIgnoreCase(platformName) || Constant.BIG_BASKET.equalsIgnoreCase(platformName);
    }

    private boolean isQuickCommPlatform(String platformName) {
        return Constant.SWIGGY.equalsIgnoreCase(platformName) || Constant.BLINKIT.equalsIgnoreCase(platformName) || Constant.ZEPTO.equalsIgnoreCase(platformName);
    }

    private void addToCategory(Platform platform, List<Platform> category) {
        if (!category.contains(platform)) {
            category.add(platform);
        }
    }


    private double calculatePercentage(double value, double total) {
        return total != 0 ? (value / total) * 100 : 0;
    }

    public static Map<String, Integer> countOutOfStockByPlatform(List<MergedModel> mergedModels) {
        return mergedModels.stream()
                .collect(Collectors.toMap(
                        MergedModel::getPlatform,
                        MergeExcelAndCSVServiceImpl::countOutOfStock,
                        Integer::sum
                ));
    }

    private static int countOutOfStock(MergedModel model) {
        int outOfStockCount = 0;
        if (Constant.OUT_OF_STOCK.equals(model.getAhmedabad())) outOfStockCount++;
        if (Constant.OUT_OF_STOCK.equals(model.getBangalore())) outOfStockCount++;
        if (Constant.OUT_OF_STOCK.equals(model.getChennai())) outOfStockCount++;
        if (Constant.OUT_OF_STOCK.equals(model.getDelhi())) outOfStockCount++;
        if (Constant.OUT_OF_STOCK.equals(model.getHyderabad())) outOfStockCount++;
        if (Constant.OUT_OF_STOCK.equals(model.getIndore())) outOfStockCount++;
        if (Constant.OUT_OF_STOCK.equals(model.getCalcutta())) outOfStockCount++;
        if (Constant.OUT_OF_STOCK.equals(model.getMumbai())) outOfStockCount++;
        if (Constant.OUT_OF_STOCK.equals(model.getNagpur())) outOfStockCount++;
        if (Constant.OUT_OF_STOCK.equals(model.getPatna())) outOfStockCount++;
        if (Constant.OUT_OF_STOCK.equals(model.getPune())) outOfStockCount++;
        if (Constant.OUT_OF_STOCK.equals(model.getOther())) outOfStockCount++;
        return outOfStockCount;
    }


    private MergedModel createMergedModel(CSVModel csvModel,
                                          List<ExcelModel> excelModels,
                                          List<MergedModel> discontinued) {
        MergedModel mergedModel = new MergedModel();
        ExcelModel firstExcelModel = excelModels.get(0);

        // Setting basic information
        mergedModel.setPlatform(firstExcelModel.getPlatform());
        mergedModel.setAsin(firstExcelModel.getAsin());
        mergedModel.setPname(firstExcelModel.getPname());
        mergedModel.setInternalDivision(csvModel.getInternalDivision());
        mergedModel.setBrand(csvModel.getBrand());
        mergedModel.setCategory(csvModel.getCategory());
        mergedModel.setSubCategory(csvModel.getSubCategory());

        // Setting revenue and day sales
        double revenue = Double.parseDouble(csvModel.getRevenue());
        String formattedRevenue = String.format("%.2f", revenue);
        mergedModel.setRevenue(formattedRevenue);

        double daySales = revenue / 30;
        String formattedDaySales = String.format("%.2f", daySales);
        mergedModel.setDaySales(formattedDaySales);
        int unit = csvModel.getUnits()/30;
        mergedModel.setMonthlySales(String.valueOf(unit));

        // Collecting city status information
        Map<String, String> cityStatusMap = new LinkedHashMap<>();
        int countOfZeros = 0;
        int count = 0;
        for (ExcelModel excelModel : excelModels) {
            if (excelModel.getStatus().equals("1") || excelModel.getStatus().equals("0")) {
                if (excelModel.getStatus().equals("0")) {
                    countOfZeros++;
                }
                count++;
            }
            if (excelModel.getCity() != null && !excelModel.getCity().isEmpty() && !excelModel.getCity().equals("-")) {
                cityStatusMap.put(excelModel.getCity(), excelModel.getStatus());
            }
        }

        // Setting city statuses
        mergedModel.setAhmedabad(findValuesForCity(cityStatusMap, "Ahmedabad"));
        mergedModel.setBangalore(findValuesForCity(cityStatusMap, "Bangalore"));
        mergedModel.setChennai(findValuesForCity(cityStatusMap, "Chennai"));
        mergedModel.setDelhi(findValuesForCity(cityStatusMap, "Delhi"));
        mergedModel.setHyderabad(findValuesForCity(cityStatusMap, "Hyderabad"));
        mergedModel.setIndore(findValuesForCity(cityStatusMap, "Indore"));
        mergedModel.setCalcutta(findValuesForCity(cityStatusMap, "Calcutta"));
        mergedModel.setMumbai(findValuesForCity(cityStatusMap, "Mumbai"));
        mergedModel.setNagpur(findValuesForCity(cityStatusMap, "Nagpur"));
        mergedModel.setPatna(findValuesForCity(cityStatusMap, "Patna"));
        mergedModel.setPune(findValuesForCity(cityStatusMap, "Pune"));
        mergedModel.setOther(getOthersCityMap(cityStatusMap));
        // Setting percentage of each city
        setCityPercentage(mergedModel, cityStatusMap, count);
        // Calculating SWOOS percentage
        if (countOfZeros > 0) {
            setSwoosPercentage(mergedModel, countOfZeros);
        }
        // Calculating value loss
        calculateValueLoss(mergedModel, count, countOfZeros);
        Map<String, MergedModel> latestDiscontinuedMap = discontinued.stream()
                .collect(Collectors.toMap(
                        MergedModel::getAsin,
                        Function.identity(),
                        (existing, replacement) -> existing.getUpdatedAt().after(replacement.getUpdatedAt()) ? existing : replacement
                ));

        latestDiscontinuedMap.values().stream()
                .filter(disCont -> mergedModel.getAsin().equalsIgnoreCase(disCont.getAsin()))
                .findFirst()
                .ifPresent(disCont -> {
                    if (disCont.getReason().equalsIgnoreCase(Constant.DISCONTINUED)) {
                        mergedModel.setHistoryFlag(true);
                        mergedModel.setReason(disCont.getReason());
                        mergedModel.setLastDayReason(disCont.getReason());
                        mergedModel.setRemarks(disCont.getRemarks());
                        mergedModel.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                    } else {
                        if(locationNotAlign(disCont,mergedModel)){
                          mergedModel.setHistoryFlag(true);
                          mergedModel.setReason(disCont.getReason());
                          mergedModel.setLastDayReason(disCont.getReason());
                          mergedModel.setRemarks(disCont.getRemarks());
                          mergedModel.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                        }
                    }
                });

        return mergedModel;
    }

    private boolean locationNotAlign(MergedModel oldAsin, MergedModel newAsin) {
        // Define an array of cities to check
        String[] citiesToCheck = {"Bangalore", "Ahmedabad", "Chennai", "Delhi", "Pune", "Nagpur", "Mumbai", "Calcutta", "Indore", "Hyderabad"};

        // Iterate through the cities and check the condition
        for (String city : citiesToCheck) {
            // Get the getter method name dynamically
            String getterMethodName = "get" + city;
            try {
                // Use reflection to invoke the getter method
                Method getterMethod = MergedModel.class.getMethod(getterMethodName);
                String oldValue = (String) getterMethod.invoke(oldAsin);
                String newValue = (String) getterMethod.invoke(newAsin);

                // Check if the city is "Out-of-Stock" in both old and new Asin
                if (oldValue.equalsIgnoreCase(Constant.OUT_OF_STOCK) && oldValue.equalsIgnoreCase(newValue)) {
                    return true; // If condition matches, return true
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // Handle exceptions if necessary
                e.printStackTrace();
            }
        }

        return false; // Return false if no condition matches
    }

    private void setCityPercentage(MergedModel mergedModel, Map<String, String> cityStatusMap, int count) {
        if (count == 0) return;

        setPercentage(mergedModel::setAhmedabadPercentage, cityStatusMap, "Ahmedabad", count);
        setPercentage(mergedModel::setBangalorePercentage, cityStatusMap, "Bangalore", count);
        setPercentage(mergedModel::setChennaiPercentage, cityStatusMap, "Chennai", count);
        setPercentage(mergedModel::setDelhiPercentage, cityStatusMap, "Delhi", count);
        setPercentage(mergedModel::setHyderabadPercentage, cityStatusMap, "Hyderabad", count);
        setPercentage(mergedModel::setIndorePercentage, cityStatusMap, "Indore", count);
        setPercentage(mergedModel::setCalcuttaPercentage, cityStatusMap, "Calcutta", count);
        setPercentage(mergedModel::setMumbaiPercentage, cityStatusMap, "Mumbai", count);
        setPercentage(mergedModel::setNagpurPercentage, cityStatusMap, "Nagpur", count);
        setPercentage(mergedModel::setPatnaPercentage, cityStatusMap, "Patna", count);
        setPercentage(mergedModel::setPunePercentage, cityStatusMap, "Pune", count);
    }

    private void setPercentage(Consumer<String> setter, Map<String, String> cityStatusMap, String city, int count) {
        if (cityStatusMap.containsKey(city)) {
            String percentage = String.format("%.2f", 100.0 / count);
            setter.accept(percentage);
        }
    }

    private void setSwoosPercentage(MergedModel mergedModel, int countOfZeros) {
        // Ensure to set only two digits after the decimal point
        List<String> cityPercentages = Arrays.asList(
                mergedModel.getAhmedabadPercentage(),
                mergedModel.getBangalorePercentage(),
                mergedModel.getCalcuttaPercentage(),
                mergedModel.getChennaiPercentage(),
                mergedModel.getDelhiPercentage(),
                mergedModel.getHyderabadPercentage(),
                mergedModel.getIndorePercentage(),
                mergedModel.getMumbaiPercentage(),
                mergedModel.getNagpurPercentage(),
                mergedModel.getPatnaPercentage(),
                mergedModel.getPunePercentage()
        );


        for (String percentage : cityPercentages) {
            if (percentage != null && !percentage.equals("-")) {
                BigDecimal cityPercentage = new BigDecimal(percentage);
                BigDecimal swoosPercentage = BigDecimal.valueOf(100).subtract(cityPercentage.multiply(BigDecimal.valueOf(countOfZeros)));
                swoosPercentage = swoosPercentage.setScale(2, RoundingMode.HALF_UP); // Set to 2 decimal places
                mergedModel.setSwoosPercentage(swoosPercentage);
                break;  // Only need to set it once
            }
        }
    }

    private void calculateValueLoss(MergedModel mergedModel, int count, int countOfZeros) {
        if (count == 0) return;

        double a = (100.0 / count);
        double b = a * countOfZeros;
        double c = (b * Double.parseDouble(mergedModel.getRevenue()));
        double loss = c / 30;
        String formattedLoss = String.format("%.2f", loss);
        mergedModel.setValueLoss(formattedLoss);
    }

    private String getOthersCityMap(Map<String,String> cityStatusMap){
        Map<String, Integer> map = new HashMap<>();
        System.out.println(cityStatusMap);
        List<String> othersCities = List.of("Ghaziabad","Lucknow  HQ", "Gurgaon", "Jaipur", "Lucknow", "Chandigarh hq", "Ambalahq", "Goa-panaji");
        for(String cityStatus : othersCities){
            if(cityStatusMap.containsKey(cityStatus)){
                String a;
                if (Objects.isNull(cityStatusMap.get(cityStatus))) {
                    int count = map.getOrDefault("NA",0);
                    map.put("NA", count);
                } else {
                    a = cityStatusMap.get(cityStatus);
                    if (a.equals("0")) {
                        a = Constant.OUT_OF_STOCK;
                        int count = map.getOrDefault(a,0);
                        map.put(a, count);

                    } else if (a.equals("1")) {
                        a = "Available";
                        int count = map.getOrDefault(a,0);
                        map.put(a, count);
                    }
                }

            }
        }
        Optional<Map.Entry<String, Integer>> maxEntry = map.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue());
        if(maxEntry.isPresent()){
            return maxEntry.get().getKey();
        }else{
            return "NA";
        }

    }
    private String value(Map<String, String> cityStatusMap, String b) {
        String a;
        if (Objects.isNull(cityStatusMap.get(b))) {
            a = "NA";
        } else {
            a = cityStatusMap.get(b);
            if (a.equals("0")) {
                a = Constant.OUT_OF_STOCK;

            } else if (a.equals("1")) {
                a = "Available";
            }
        }
        return a;
    }

    public String findValuesForCity(Map<String, String> cityStatusMap, String cityName) {

        for (Map.Entry<String, String> entry : cityStatusMap.entrySet()) {
            if (entry.getKey().contains(cityName)) {
                return value(cityStatusMap, entry.getKey());
            }
        }
        return value(cityStatusMap, cityName);
    }
    @Override
    public Map<String, Map<String, String>> locations(long id){
        MergedModel mergedModels = mergedRepository.findById(id).orElseThrow();

        Map<String, String> stockStatusMap = new HashMap<>();
        stockStatusMap.put("ahmedabad", mergedModels.getAhmedabad());
        stockStatusMap.put("bangalore", mergedModels.getBangalore());
        stockStatusMap.put("indore", mergedModels.getIndore());
        stockStatusMap.put("chennai", mergedModels.getChennai());
        stockStatusMap.put("delhi", mergedModels.getDelhi());
        stockStatusMap.put("hyderabad", mergedModels.getHyderabad());
        stockStatusMap.put("calcutta", mergedModels.getCalcutta());
        stockStatusMap.put("mumbai", mergedModels.getMumbai());
        stockStatusMap.put("nagpur", mergedModels.getNagpur());
        stockStatusMap.put("patna", mergedModels.getPatna());
        stockStatusMap.put("pune", mergedModels.getPune());

        List<String> otherCities = List.of("ghaziabad", "gurgaon", "jaipur", "lucknow", "chandigarh hq", "ambala hq", "goa-panaji");

        Map<String, String> locations = new HashMap<>();
        List<ExcelModel> dataList = excelRepository.findAll();

        dataList.forEach(data -> {
            String city = data.getCity().toLowerCase();
            stockStatusMap.forEach((key, value) -> {
                if (city.contains(key) && "out-of-stock".equalsIgnoreCase(value)) {
                    locations.put(key, data.getLocation());
                }
            });

            otherCities.forEach(otherCity -> {
                if (city.contains(otherCity) && "out-of-stock".equalsIgnoreCase(mergedModels.getOther())) {
                    locations.put(otherCity, data.getLocation());
                }
            });
        });

        return Map.of("location", locations);
    }
    public Map<String, Map<String, String>> locations(MergedModelDto mergedModels,List<DataListProjection> dataList){

        Map<String, String> stockStatusMap = new HashMap<>();
        stockStatusMap.put("ahmedabad", mergedModels.getAhmedabad());
        stockStatusMap.put("bangalore", mergedModels.getBangalore());
        stockStatusMap.put("indore", mergedModels.getIndore());
        stockStatusMap.put("chennai", mergedModels.getChennai());
        stockStatusMap.put("delhi", mergedModels.getDelhi());
        stockStatusMap.put("hyderabad", mergedModels.getHyderabad());
        stockStatusMap.put("calcutta", mergedModels.getCalcutta());
        stockStatusMap.put("mumbai", mergedModels.getMumbai());
        stockStatusMap.put("nagpur", mergedModels.getNagpur());
        stockStatusMap.put("patna", mergedModels.getPatna());
        stockStatusMap.put("pune", mergedModels.getPune());

        List<String> otherCities = List.of("ghaziabad", "gurgaon", "jaipur", "lucknow", "chandigarh hq", "ambala hq", "goa-panaji");

        Map<String, String> locations = new HashMap<>();
        dataList.forEach(data -> {
            String city = data.getCity().toLowerCase();
            stockStatusMap.forEach((key, value) -> {
                if (city.contains(key) && "out-of-stock".equalsIgnoreCase(value)) {
                    locations.put(key, data.getLocation());
                }
            });

            otherCities.forEach(otherCity -> {
                if (city.contains(otherCity) && "out-of-stock".equalsIgnoreCase(mergedModels.getOther())) {
                    locations.put(otherCity, data.getLocation());
                }
            });
        });

        return Map.of("location", locations);
    }
/*    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }*/

    @Autowired
    ExcelWriterServiceImpl excelWriterService ;

    public SuccessResponse<Object> readHistoryTrue(HttpServletResponse response) {
        SuccessResponse<Object> successResponse = new SuccessResponse<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twentyFourHoursAgo = now.minusHours(24);
        Timestamp fromDate = Timestamp.valueOf(twentyFourHoursAgo);
        Timestamp toDate = Timestamp.valueOf(now);
        List<MergedModel> mergedModels = mergedRepository.getAllHistoryTrue(fromDate,toDate);
        List<MergedModelDto> mergedModelDtos = mergedModels.stream()
                .map(this::convertToDto)
                .toList();
        successResponse.setStatusCode(200);
        successResponse.setData(mergedModelDtos);
        return successResponse;
    }


    private MergedModelDto convertToDto(MergedModel mergedModel) {
        MergedModelDto mergedModelDto = new MergedModelDto();

        mergedModelDto.setMergedId(mergedModel.getMergedId());
        mergedModelDto.setPlatform(mergedModel.getPlatform());
        mergedModelDto.setAsin(mergedModel.getAsin());
        mergedModelDto.setPname(mergedModel.getPname());
        mergedModelDto.setInternalDivision(mergedModel.getInternalDivision());
        mergedModelDto.setBrand(mergedModel.getBrand());
        mergedModelDto.setCategory(mergedModel.getCategory());
        mergedModelDto.setSubCategory(mergedModel.getSubCategory());
        mergedModelDto.setAhmedabad(mergedModel.getAhmedabad());
        mergedModelDto.setBangalore(mergedModel.getBangalore());
        mergedModelDto.setChennai(mergedModel.getChennai());
        mergedModelDto.setDelhi(mergedModel.getDelhi());
        mergedModelDto.setHyderabad(mergedModel.getHyderabad());
        mergedModelDto.setIndore(mergedModel.getIndore());
        mergedModelDto.setCalcutta(mergedModel.getCalcutta());
        mergedModelDto.setMumbai(mergedModel.getMumbai());
        mergedModelDto.setNagpur(mergedModel.getNagpur());
        mergedModelDto.setPatna(mergedModel.getPatna());
        mergedModelDto.setPune(mergedModel.getPune());
        mergedModelDto.setAhmedabadPercentage(mergedModel.getAhmedabadPercentage());
        mergedModelDto.setBangalorePercentage(mergedModel.getBangalorePercentage());
        mergedModelDto.setChennaiPercentage(mergedModel.getChennaiPercentage());
        mergedModelDto.setDelhiPercentage(mergedModel.getDelhiPercentage());
        mergedModelDto.setHyderabadPercentage(mergedModel.getHyderabadPercentage());
        mergedModelDto.setIndorePercentage(mergedModel.getIndorePercentage());
        mergedModelDto.setCalcuttaPercentage(mergedModel.getCalcuttaPercentage());
        mergedModelDto.setMumbaiPercentage(mergedModel.getMumbaiPercentage());
        mergedModelDto.setNagpurPercentage(mergedModel.getNagpurPercentage());
        mergedModelDto.setPatnaPercentage(mergedModel.getPatnaPercentage());
        mergedModelDto.setPunePercentage(mergedModel.getPunePercentage());
        mergedModelDto.setNA(mergedModel.getNA());
        mergedModelDto.setRevenue(mergedModel.getRevenue());
        mergedModelDto.setMonthlySales(mergedModel.getMonthlySales());
        mergedModelDto.setDaySales(mergedModel.getDaySales());
        mergedModelDto.setSwoosPercentage(mergedModel.getSwoosPercentage());
        mergedModelDto.setValueLoss(mergedModel.getValueLoss());
        mergedModelDto.setSWOOSContribution(mergedModel.getSWOOSContribution());
        mergedModelDto.setTotalValueLoss(mergedModel.getTotalValueLoss());
        mergedModelDto.setOther(mergedModel.getOther());
        mergedModelDto.setRemarks(mergedModel.getRemarks());
        mergedModelDto.setReason(mergedModel.getReason());
        mergedModelDto.setDay(mergedModel.getDay());
        mergedModelDto.setLastDayReason(mergedModel.getLastDayReason());
        mergedModelDto.setCity(mergedModel.getCity());
        mergedModelDto.setHistory(mergedModel.isHistoryFlag());
        mergedModelDto.setIsSubmited(mergedModel.getIsSubmited());
        mergedModelDto.setDeletedFlag(mergedModel.isDeletedFlag());
        mergedModelDto.setCreateAt(String.valueOf(mergedModel.getCreateAt()));
        mergedModelDto.setUpdatedAt(String.valueOf(mergedModel.getUpdatedAt()));
        mergedModelDto.setLastDayReason(mergedModel.getLastDayReason());
        return mergedModelDto;
    }
    @Autowired
    private MergedModelRepositoryCustom mergedModelRepositoryCustom;

    public PageResponse<Object> getMergedModel(int pageSize, int pageNo, LocalDate fromDate, String field,
                                               String searchTerm) {
        PageResponse<Object> response = new PageResponse<>();
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        LocalDateTime fromDateTime = LocalDateTime.of(fromDate, LocalTime.MIN);
        Timestamp from = Timestamp.valueOf(fromDateTime);
        LocalDateTime endDate = LocalDateTime.of(fromDate, LocalTime.MAX);
        Timestamp to = Timestamp.valueOf(endDate);
        List<DataListProjection> dataList = excelRepository.findByTriggeredOnTodayProjection();
        Page<MergedModel> mergedModelList = null;
        if (searchTerm==null) {
            mergedModelList = mergedRepository.findAllOrderByValueLossDescPageable(from,to,pageable);
        }else{
            mergedModelList = mergedModelRepositoryCustom.findAllOrderByValueLossDescPageable(field,searchTerm,pageable);
        }
        List<MergedModelDto> mergedModels = mergedModelList.getContent().stream()
                .map(mergedModel -> modelMapper.map(mergedModel, MergedModelDto.class))
                .toList();
        mergedModels.forEach(mergedModelDto ->
                mergedModelDto.setStockStatus(locations(mergedModelDto,dataList)));
        response.setData(mergedModels);
        response.setHasPrevious(mergedModelList.hasPrevious());
        response.setTotalRecordCount(mergedModelList.getTotalElements());
        response.setHasNext(mergedModelList.hasNext());
        return response;
    }


    @Override
    public PageResponse<Object> swoosFilter(String value, boolean greaterThan,int pageNo,int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        PageResponse<Object> response = new PageResponse<>();
        List<DataListProjection> dataList = excelRepository.findByTriggeredOnTodayProjection();
        Page<MergedModel> mergedModelList;
        if(greaterThan){
        mergedModelList = mergedRepository.findAllBySWOOSContributionGreaterThanAndCreatedAtToday(value,pageable);
        }else{
        mergedModelList = mergedRepository.findAllBySWOOSContributionLesserThanAndCreatedAtToday(value,pageable);
        }
        List<MergedModelDto> mergedModels = mergedModelList.getContent().stream()
                .map(mergedModel -> modelMapper.map(mergedModel, MergedModelDto.class))
                .toList();
        mergedModels.forEach(mergedModelDto ->
                mergedModelDto.setStockStatus(locations(mergedModelDto,dataList)));
        response.setData(mergedModels);
        response.setHasPrevious(mergedModelList.hasPrevious());
        response.setTotalRecordCount(mergedModelList.getTotalElements());
        response.setHasNext(mergedModelList.hasNext());
        return response;
    }
}