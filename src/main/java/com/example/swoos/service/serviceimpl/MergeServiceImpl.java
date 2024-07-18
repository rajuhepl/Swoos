package com.example.swoos.service.serviceimpl;
import com.example.swoos.dto.MergeRequestDTO;
import com.example.swoos.dto.PlatformOFSCount;
import com.example.swoos.projection.DataListProjection;
import com.example.swoos.dto.MergedModelDto;
import com.example.swoos.model.*;
import com.example.swoos.projection.PlatformCount;
import com.example.swoos.repository.*;
import com.example.swoos.response.PageResponse;
import com.example.swoos.response.SuccessResponse;
import com.example.swoos.service.MergeService;
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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
@Service
public class MergeServiceImpl implements MergeService {
    @Autowired
    private DataTableRepository dataTableRepository;
    @Autowired
    private EcomOffTakeRepository ecomOffTakeRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private MergedRepository mergedRepository;
    @Autowired
    private DropDownRepository dropDownRepository;
    @Autowired
    private LocationCodeRepository locationCodeRepository;

    public void readDataFromFile() {
        List<DataTable> dataList = dataTableRepository.findByTriggeredOnToday();
        List<EcomOffTake> ecomOffTakeList = ecomOffTakeRepository.findAllDataForMonth(ecomOffTakeRepository.findLastLoadedMonth());
        mergeLists(ecomOffTakeList, dataList);
    }
    private void mergeLists(List<EcomOffTake> csvData, List<DataTable> excelData) {
        Map<String, List<DataTable>> excelDataMap = groupExcelDataByASIN(excelData);
        mergeCSVAndExcelData(csvData, excelDataMap);
    }
    private Map<String, List<DataTable>> groupExcelDataByASIN(List<DataTable> excelData) {
        return excelData.stream()
                .collect(Collectors.groupingBy(DataTable::getAsin));
    }
    private void mergeCSVAndExcelData(List<EcomOffTake> csvData, Map<String, List<DataTable>> excelDataMap) {
        List<MergedModel> mergedList = new ArrayList<>();
        List<MergedModel> discontinued = mergedRepository.findAllDiscontinuedOrLocationNotAlign();
        List<DataListProjection> dataList = dataTableRepository.findByTriggeredOnTodayProjection();
        for (EcomOffTake ecomOfftake : csvData) {
            List<DataTable> dataTables = excelDataMap.get(ecomOfftake.getMainSKUCode());
            if (dataTables != null) {
                MergedModel mergedModel = createMergedModel(ecomOfftake, dataTables,discontinued,dataList);
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
            double totalValueLoss = filteredMergedList.stream()
                    .mapToDouble(mergedModel -> Double.parseDouble(mergedModel.getValueLoss()))
                    .sum();
            for (MergedModel mergedModel : filteredMergedList) {
                double contribution = totalValueLoss > 0? (Double.parseDouble(mergedModel.getValueLoss()) / totalValueLoss) * 100 : 0;
                String formattedLoss = String.format("%.2f ", contribution);
                mergedModel.setSWOOSContribution(formattedLoss + "%");
                finalMergedList.add(mergedModel);
            }
        }

        mergedRepository.saveAll(finalMergedList);
        System.out.println("Data Loaded to DataBase");

    }
    public PlatformOFSCount platformAndValueloss() {
        // Fetch platform counts from repository
        List<PlatformCount> platformCounts = mergedRepository.countDataByPlatform();

        // Initialize maps for different categories
        Map<String, Long> nationalMap = new HashMap<>();
        Map<String, Long> quickComMap = new HashMap<>();
        Map<String, Long> groceryMap = new HashMap<>();
        Map<String, Long> beautyMap = new HashMap<>();

        // Populate maps based on platform counts
        for (PlatformCount platformCount : platformCounts) {
            String platform = platformCount.getPlatform().toLowerCase(); // normalize platform name

            if (platform.equalsIgnoreCase("amazon") || platform.equalsIgnoreCase("flipkart")) {
                nationalMap.put(platform, platformCount.getOutOfStockCount());
            } else if (platform.equalsIgnoreCase("zepto") || platform.equalsIgnoreCase("blinkit")) {
                quickComMap.put(platform, platformCount.getOutOfStockCount());
            } else if (platform.equalsIgnoreCase("bigbasket")) {
                groceryMap.put(platform, platformCount.getOutOfStockCount());
            } else if (platform.equalsIgnoreCase("purplle")) {
                beautyMap.put(platform, platformCount.getOutOfStockCount());
            }
            // Add more conditions as needed for other categories
        }

        // Create and return PlatformOFSCount object with populated maps
        return new PlatformOFSCount(nationalMap, quickComMap, groceryMap, beautyMap);
    }

    private MergedModel createMergedModel(EcomOffTake ecomOfftake,
                                          List<DataTable> dataTables,
                                          List<MergedModel> discontinued,
                                          List<DataListProjection> dataList) {
        MergedModel mergedModel = new MergedModel();
        DataTable firstDataTable = dataTables.get(0);

        // Setting basic information
        mergedModel.setPlatform(firstDataTable.getPlatform());
        mergedModel.setAsin(firstDataTable.getAsin());
        mergedModel.setPname(firstDataTable.getPname());
        mergedModel.setInternalDivision(ecomOfftake.getInternalDivision());
        mergedModel.setBrand(ecomOfftake.getBrand());
        mergedModel.setCategory(ecomOfftake.getCategory());
        mergedModel.setSubCategory(ecomOfftake.getSubCategory());

        // Setting revenue and day sales
        double revenue = Double.parseDouble(ecomOfftake.getRevenue());
        String formattedRevenue = String.format("%.2f", revenue);
        mergedModel.setRevenue(formattedRevenue);

        double daySales = revenue / 30;
        String formattedDaySales = String.format("%.2f", daySales);
        mergedModel.setDaySales(formattedDaySales);

        // Collecting city status information
        Map<String, String> cityStatusMap = new LinkedHashMap<>();
        int countOfZeros = 0;
        int count = 0;
        for (DataTable dataTable : dataTables) {
            if (dataTable.getStatus().equals("1") || dataTable.getStatus().equals("0")) {
                count++;
            }
            if (dataTable.getCity() != null && !dataTable.getCity().isEmpty() && !dataTable.getCity().equals("-")) {
                cityStatusMap.put(dataTable.getCity(), dataTable.getStatus());
                if (dataTable.getStatus().equals("0")) {
                    countOfZeros++;
                }
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
        locationsModel(mergedModel,dataList);
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
        double a = (100.0 / count) * 100;
        double b = a * countOfZeros;
        double c = (b * Double.parseDouble(mergedModel.getRevenue()));
        double loss = c / 30;
        String formattedLoss = String.format("%.2f", (loss / 100) / 100);
        mergedModel.setValueLoss(formattedLoss);
    }

    private String getOthersCityMap(Map<String,String> cityStatusMap){
        Map<String, Integer> map = new HashMap<>();
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

        final var stockStatusMap = getStringStringMap(mergedModels);

        List<String> otherCities = List.of("ghaziabad", "gurgaon", "jaipur", "lucknow", "chandigarh hq", "ambala hq", "goa-panaji");

        Map<String, String> locations = new HashMap<>();
        List<DataTable> dataList = dataTableRepository.findAll();

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

    private static Map<String, String> getStringStringMap(MergedModel mergedModels) {
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
        return stockStatusMap;
    }

    public Map<String, Map<String, String>> locations(MergedModelDto mergedModels,List<DataListProjection> dataList){

        final var stockStatusMap = getStringMap(mergedModels);

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
                    locations.put(otherCity , data.getLocation());
                }
            });
        });
        return Map.of("location", locations);
    }

    private static Map<String, String> getStringMap(MergedModelDto mergedModels) {
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
        return stockStatusMap;
    }

    //For Testing
    public void locationsModel(MergedModel mergedModels,List<DataListProjection> dataList){

        final var stockStatusMap = getStringStringMap(mergedModels);

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
                    locations.put(otherCity , data.getLocation());
                }
            });
        });
        mergedModels.setLocation(saveLocationCode(locations));
    }

    private LocationPincode saveLocationCode(Map<String, String> locations) {
        LocationPincode location = new LocationPincode();
        Map<String,String> others = new HashMap<>();
        locations.forEach((key, value) -> {
            switch (key.toLowerCase()) {
                case "ahmedabad":
                    location.setAhmedabad(value);
                    break;
                case "bangalore":
                    location.setBangalore(value);
                    break;
                case "chennai":
                    location.setChennai(value);
                    break;
                case "delhi":
                    location.setDelhi(value);
                    break;
                case "hyderabad":
                    location.setHyderabad(value);
                    break;
                case "indore":
                    location.setIndore(value);
                    break;
                case "mumbai":
                    location.setMumbai(value);
                    break;
                case "nagpur":
                    location.setNagpur(value);
                    break;
                case "patna":
                    location.setPatna(value);
                    break;
                case "pune":
                    location.setPune(value);
                    break;
                default :
                    others.put(key, value);
                    break;
            }
        });
        location.setOtherCities(others.toString());
       return locationCodeRepository.save(location);
    }


    @Autowired
    ExcelWriterServiceImpl excelWriterService ;

    public List<MergedModelDto> readHistoryTrue(HttpServletResponse response) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twentyFourHoursAgo = now.minusHours(24);
        Timestamp fromDate = Timestamp.valueOf(twentyFourHoursAgo);
        Timestamp toDate = Timestamp.valueOf(now);
        List<MergedModel> mergedModels = mergedRepository.getAllHistoryTrue(fromDate,toDate);
        return mergedModels.stream()
                .map(this::convertToDto)
                .toList();
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
        Page<MergedModel> mergedModelList ;
        if (searchTerm==null) {
            mergedModelList = mergedRepository.findAllOrderByValueLossDescPageable(from,to,pageable);
        }else{
            mergedModelList = mergedModelRepositoryCustom.findAllOrderByValueLossDescPageable(field,searchTerm,pageable);
        }
        response.setData(mergedModelList.getContent());
        response.setHasPrevious(mergedModelList.hasPrevious());
        response.setTotalRecordCount(mergedModelList.getTotalElements());
        response.setHasNext(mergedModelList.hasNext());
        return response;
    }
    @Override
    public SuccessResponse<Object> MergedModel() {
        SuccessResponse<Object> response1 = new SuccessResponse<>();
        List<MergedModel>mergedModels =mergedRepository.findAllOrderByValueLossDesc();
        response1.setData(mergedModels);
        return response1;
    }

    @Override
    public PageResponse<Object> swoosFilter(String value, boolean greaterThan,int pageNo,int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        PageResponse<Object> response = new PageResponse<>();
        List<DataListProjection> dataList = dataTableRepository.findByTriggeredOnTodayProjection();
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

    public String update(List<MergeRequestDTO> mergeRequestDTO) {
        for(MergeRequestDTO mergeRequestDTO1 :mergeRequestDTO) {
            Optional<MergedModel> mergedModel1 = mergedRepository.findById(mergeRequestDTO1.getMergedId());
            DropDownModel dropDownModel = dropDownRepository.findByDescription(mergeRequestDTO1.getReason());
            if (mergedModel1.isPresent()) {
                if(dropDownModel != null) {
                    mergedModel1.get().setReason(dropDownModel.getDescription());
                }else{
                    DropDownModel drop = new DropDownModel();
                    drop.setDescription(mergeRequestDTO1.getReason());
                    dropDownRepository.save(drop);
                }
                mergedModel1.get().setRemarks(mergeRequestDTO1.getRemarks());
                mergedModel1.get().setHistoryFlag(true);
                mergedRepository.save(mergedModel1.get());
            }
        }
        return "updated";
    }
}