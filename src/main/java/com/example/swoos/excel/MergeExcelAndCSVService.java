/*
package com.example.swoos.excel;

import com.example.swoos.dto.MergedModelDto;
import com.example.swoos.model.*;
import com.example.swoos.repository.*;
import com.example.swoos.response.SuccessResponse;
import com.example.swoos.service.ExcelWriterService;
import com.example.swoos.util.Constant;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class MergeExcelAndCSVService {
    @Autowired
    ExcelWriterService excelWriterService;
    @Autowired
    ExcelRepository excelRepository ;
    @Autowired
    CsvRepository csvRepository ;

    @Autowired
    MergedRepository mergedRepository;
    @Autowired
    PlatformAndValuelossRepository platformValueRepository;
    @Autowired
    PlatformRepository platformRepository;
    @Autowired
    ModelMapper modelMapper;

    public SuccessResponse readDataFromFile(HttpServletResponse response) {

        SuccessResponse response1 = new SuccessResponse();

        List<ExcelModel> dataList = excelRepository.findAll();
        List<CSVModel> csvModelList = csvRepository.findAll();

        List<MergedModel> mergedModels = mergeLists(csvModelList, dataList);
        //mergedRepository.saveAll(mergedModels);
        response1.setData(mergedModels);

        return response1;
    }


    private List<ExcelModel> readExcel(String excelPath) throws IOException {
        List<ExcelModel> data = new ArrayList<>();
        try (FileInputStream inputStream = new FileInputStream(excelPath); Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Set<String> hashSet = new HashSet<>();
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                ExcelModel excelModel = new ExcelModel();
                excelModel.setPlatform(getCellValueAsString(row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)));
                excelModel.setAsin(getCellValueAsString(row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)));
                excelModel.setPname(getCellValueAsString(row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)));
                excelModel.setBrand(getCellValueAsString(row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)));
                excelModel.setLocation(getCellValueAsString(row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)));
                excelModel.setCity(getCellValueAsString(row.getCell(5, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)));
                excelModel.setStatusText(getCellValueAsString(row.getCell(6, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)));
                excelModel.setStatus(getCellValueAsString(row.getCell(7, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)));
                data.add(excelModel);
            }
            return data;
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            default:
                return "";
        }
    }

    public List<MergedModel> mergeLists(List<CSVModel> csvData, List<ExcelModel> excelData) {
        Map<String, List<ExcelModel>> excelDataMap = groupExcelDataByASIN(excelData);
        return mergeCSVAndExcelData(csvData, excelDataMap);
    }

    private Map<String, List<ExcelModel>> groupExcelDataByASIN(List<ExcelModel> excelData) {
        Map<String, List<ExcelModel>> excelDataMap = new HashMap<>();
        for (ExcelModel excelModel : excelData) {
            excelDataMap.computeIfAbsent(excelModel.getAsin(), k -> new ArrayList<>()).add(excelModel);
        }
        return excelDataMap;
    }

    private List<MergedModel> mergeCSVAndExcelData
            (List<CSVModel> csvData, Map<String, List<ExcelModel>> excelDataMap) {
        List<MergedModel> mergedList = new ArrayList<>();
        List<MergedModel> finalMergedList = new ArrayList<>();
        for (CSVModel csvModel : csvData) {
            List<ExcelModel> excelModels = excelDataMap.get(csvModel.getMainSKUCode());
            if (excelModels != null) {
                MergedModel mergedModel = createMergedModel(csvModel, excelModels);
                if (!mergedModel.getValueLoss().equals("0.00")) {
                    mergedList.add(mergedModel);
                }
            }
        }
        List<String> distinctMergedModel = mergedList.stream()
                .map(MergedModel::getAsin)
                .distinct()
                .toList();
        List<String> distinctPlatforms = mergedList.stream()
                .map(MergedModel::getPlatform)
                .distinct()
                .toList();
        for(String platform :distinctPlatforms){
            List<MergedModel> filteredMergedList = mergedList.stream()
                    .filter(mergedModel -> mergedModel.getPlatform().equals(platform))
                    .toList();
            double totalValueLoss = filteredMergedList.stream()
                    .mapToDouble(mergedModel -> Double.parseDouble(mergedModel.getValueLoss()))
                    .sum();

            for(MergedModel mergedModel : filteredMergedList){
                double contribution = 0.0;

                if (totalValueLoss > 0) {
                    contribution = (Double.valueOf(mergedModel.getValueLoss())/totalValueLoss)*100;
                }

                //mergedModel.setSWOOSContribution(String.valueOf(contribution));
                String formattedLoss = String.format("%.2f ", contribution);
                mergedModel.setSWOOSContribution(formattedLoss +"%");
                // mergedModel.setDropDownModel(null);
                finalMergedList.add(mergedModel);
            }
        }

        List<MergedModel> existingMergedModels = mergedRepository.findAll();
        List<MergedModel> uniqueMergedModels = finalMergedList.stream()
                .filter(mergedModel -> existingMergedModels.stream()
                        .noneMatch(existing -> existing.getAsin().equals(mergedModel.getAsin())))
                .collect(Collectors.toList());

        if (!uniqueMergedModels.isEmpty()) {
            mergedRepository.saveAll(uniqueMergedModels);
        }
        return mergedRepository.findAllOrderByValueLossDesc();
    }
         // mergedRepository.saveAll(finalMergedList);
        //return mergedRepository.findAllOrderByValueLossDesc();



    public PlatformAndValueloss platfromAndValueloss() {
        List<MergedModel> mergedList = mergedRepository.findAll();
        List<String> distinctPlatforms = mergedList.stream()
                .map(MergedModel::getPlatform)
                .distinct()
                .toList();
        Map<String, Integer> outOfStockCounts=countOutOfStockByPlatform(mergedList);
        double totalValueLoss = 0.0;
        PlatformAndValueloss platformAndValueloss = new PlatformAndValueloss();
        List<Platform> national = new ArrayList<>();
        List<Platform> beauty = new ArrayList<>();
        List<Platform> grocery = new ArrayList<>();
        List<Platform> quickComm = new ArrayList<>();
        double flipkartValueLoss = 0.0;
        double amazonValueLoss = 0.0;
        double myntraValueLoss = 0;
        double nykaaValueLoss = 0;
        double purplleValueLoss = 0;
        double fkGroceryValueLoss = 0;
        double bigBasketValueLoss = 0;
        double swiggyValueLoss = 0;
        double blinkitValueLoss = 0;
        double zeptoValueLoss = 0;
        int amazonCount = 0;
        int flipkartCount = 0;

        for (String s : distinctPlatforms) {
            List<MergedModel> filteredMergedList = mergedList.stream()
                    .filter(mergedModel -> mergedModel.getPlatform().equals(s))
                    .toList();
            totalValueLoss = filteredMergedList.stream()
                    .mapToDouble(mergedModel -> Double.parseDouble(mergedModel.getValueLoss()))
                    .sum();

            Platform platforms = platformRepository.findByName(s);
            if (platforms == null) {
                platforms = new Platform();
                platforms.setName(s);
            }
            platforms.setValueLoss(totalValueLoss);
            String formattedValueLoss = String.format("%.2f", totalValueLoss);
            platforms.setValueLoss(Double.parseDouble(formattedValueLoss));
            platformRepository.save(platforms);
            if (s.equalsIgnoreCase(Constant.FLIPKART) || s.equalsIgnoreCase(Constant.AMAZON)) {
                if (s.equalsIgnoreCase(Constant.FLIPKART)) {
                    flipkartValueLoss = totalValueLoss;
                    flipkartCount = filteredMergedList.size();
                    Integer flipkartOutOfStockCount = outOfStockCounts.get(Constant.FLIPKART);
                    if (flipkartOutOfStockCount != null) {
                        platforms.setOutOfStockCount(flipkartOutOfStockCount);
                    }
                } else if (s.equalsIgnoreCase(Constant.AMAZON)) {
                    amazonValueLoss = totalValueLoss;
                    amazonCount = filteredMergedList.size();
                    Integer amazonOutOfStockCount = outOfStockCounts.get(Constant.AMAZON);
                    if (amazonOutOfStockCount != null) {
                        platforms.setOutOfStockCount(amazonOutOfStockCount);
                    }
                }
                double totalFlipkartAmazon = flipkartValueLoss + amazonValueLoss;
                double flipkartPercentage = totalFlipkartAmazon != 0 ? (flipkartValueLoss / totalFlipkartAmazon) * 100 : 0;
                double amazonPercentage = totalFlipkartAmazon != 0 ? (amazonValueLoss / totalFlipkartAmazon) * 100 : 0;

                platforms.setPercentage(flipkartPercentage);
                platforms.setPercentage(amazonPercentage);
                platformAndValueloss.setCountAmazon(amazonCount);
                platformAndValueloss.setCountFlipkart(flipkartCount);

                if (!national.contains(platforms)) {
                    national.add(platforms);
                }


            } else if (s.equalsIgnoreCase("Myntra") || s.equalsIgnoreCase("Nykaa") || s.equalsIgnoreCase(Constant.PURPLLE)) {
                if (s.equalsIgnoreCase("Myntra")) {
                    myntraValueLoss = totalValueLoss;
                    Integer myntraOutOfStockCount = outOfStockCounts.get("Myntra");
                    if (myntraOutOfStockCount != null) {
                        platforms.setOutOfStockCount(myntraOutOfStockCount);
                    }
                    //countMyntra++;
                } else if (s.equalsIgnoreCase("Nykaa")) {
                    nykaaValueLoss = totalValueLoss;
                    Integer nykaaOutOfStockCount = outOfStockCounts.get("Nykaa");
                    if (nykaaOutOfStockCount != null) {
                        platforms.setOutOfStockCount(nykaaOutOfStockCount);
                    }
                    // countNykaa++;
                } else if (s.equalsIgnoreCase(Constant.PURPLLE)) {
                    purplleValueLoss = totalValueLoss;
                    Integer purplleOutOfStockCount = outOfStockCounts.get(Constant.PURPLLE);
                    if (purplleOutOfStockCount != null) {
                        platforms.setOutOfStockCount(purplleOutOfStockCount);
                    }
                    // countPurplle++;
                }
                double totalBeauty = myntraValueLoss + nykaaValueLoss + purplleValueLoss;
                double myntraPercentage = totalBeauty != 0 ? (myntraValueLoss / totalBeauty) * 100 : 0;
                double nykaaPercentage = totalBeauty != 0 ? (nykaaValueLoss / totalBeauty) * 100 : 0;
                double purpllePercentage = totalBeauty != 0 ? (purplleValueLoss / totalBeauty) * 100 : 0;
                platforms.setPercentage(myntraPercentage);
                platforms.setPercentage(nykaaPercentage);
                platforms.setPercentage(purpllePercentage);
                if (!beauty.contains(platforms)) {
                    beauty.add(platforms);
                }
            } else if (s.equalsIgnoreCase("FKGrocery") || s.equalsIgnoreCase("BigBasket")) {
                if (s.equalsIgnoreCase("FKGrocery")) {
                    fkGroceryValueLoss = totalValueLoss;
                    Integer fkGroceryOutOfStockCount = outOfStockCounts.get("FKGrocery");
                    if (fkGroceryOutOfStockCount != null) {
                        platforms.setOutOfStockCount(fkGroceryOutOfStockCount);
                    }
                } else if (s.equalsIgnoreCase("BigBasket")) {
                    bigBasketValueLoss = totalValueLoss;
                    Integer bigBasketOutOfStockCount = outOfStockCounts.get("BigBasket");
                    if (bigBasketOutOfStockCount != null) {
                        platforms.setOutOfStockCount(bigBasketOutOfStockCount);
                    }
                }
                double totalGrocery = fkGroceryValueLoss + bigBasketValueLoss;
                double fkGroceryPercentage = totalGrocery != 0 ? (fkGroceryValueLoss / totalGrocery) * 100 : 0;
                double bigBasketPercentage = totalGrocery != 0 ? (bigBasketValueLoss / totalGrocery) * 100 : 0;
                platforms.setPercentage(fkGroceryPercentage);
                platforms.setPercentage(bigBasketPercentage);
                platformAndValueloss.setBigBasketPercentage(bigBasketPercentage);
                platformAndValueloss.setFKGroceryPercentage(fkGroceryPercentage);

                if (!grocery.contains(platforms)) {
                    grocery.add(platforms);
                }
            } else if (s.equalsIgnoreCase("Swiggy") || s.equalsIgnoreCase("Blinkit") || s.equalsIgnoreCase("Zepto")) {
                if (s.equalsIgnoreCase("Swiggy")) {
                    swiggyValueLoss = totalValueLoss;
                    Integer swiggyOutOfStockCount = outOfStockCounts.get("Swiggy");
                    if (swiggyOutOfStockCount != null) {
                        platforms.setOutOfStockCount(swiggyOutOfStockCount);
                    }
                } else if (s.equalsIgnoreCase("Blinkit")) {
                    blinkitValueLoss = totalValueLoss;
                    Integer blinkitOutOfStockCount = outOfStockCounts.get("Blinkit");
                    if (blinkitOutOfStockCount != null) {
                        platforms.setOutOfStockCount(blinkitOutOfStockCount);
                    }
                } else if (s.equalsIgnoreCase("Zepto")) {
                    zeptoValueLoss = totalValueLoss;
                    Integer zeptoOutOfStockCount = outOfStockCounts.get("Zepto");
                    if (zeptoOutOfStockCount != null) {
                        platforms.setOutOfStockCount(zeptoOutOfStockCount);
                    }
                    // countZepto++;
                }
                double totalQuickComm = swiggyValueLoss + blinkitValueLoss + zeptoValueLoss;
                double swiggyPercentage = totalQuickComm != 0 ? (swiggyValueLoss / totalQuickComm) * 100 : 0;
                double blinkitPercentage = totalQuickComm != 0 ? (blinkitValueLoss / totalQuickComm) * 100 : 0;
                double zeptoPercentage = totalQuickComm != 0 ? (zeptoValueLoss / totalQuickComm) * 100 : 0;
                platforms.setPercentage(swiggyPercentage);
                platforms.setPercentage(blinkitPercentage);
                platforms.setPercentage(zeptoPercentage);
                platformAndValueloss.setSwiggyPercentage(swiggyPercentage);
                platformAndValueloss.setBlinkitPercentage(blinkitPercentage);
                platformAndValueloss.setZeptoPercentage(zeptoPercentage);

                if (!quickComm.contains(platforms)) {
                    quickComm.add(platforms);
                }
            }
        }
        double totalFlipkartAmazon = flipkartValueLoss + amazonValueLoss;
        double flipkartPercentage = totalFlipkartAmazon != 0 ? (flipkartValueLoss / totalFlipkartAmazon) * 100 : 0;
        double amazonPercentage = totalFlipkartAmazon != 0 ? (amazonValueLoss / totalFlipkartAmazon) * 100 : 0;
        platformAndValueloss.setNational(national);
        platformAndValueloss.setBeauty(beauty);
        platformAndValueloss.setGrocery(grocery);
        platformAndValueloss.setQuickCom(quickComm);
        platformAndValueloss.setTotalFlipkartAmazon(totalFlipkartAmazon);
        platformAndValueloss.setFlipkartPercentage(flipkartPercentage);
        platformAndValueloss.setAmazonPercentage(amazonPercentage);

        return platformAndValueloss;
    }


    public static Map<String, Integer> countOutOfStockByPlatform(List<MergedModel> mergedModels) {
        Map<String, Integer> outOfStockCountByPlatform = new HashMap<>();

        for (MergedModel model : mergedModels) {
            int outOfStockCount = 0;

            if ("Out-of-Stock".equals(model.getAhmedabad())) outOfStockCount++;
            if ("Out-of-Stock".equals(model.getBangalore())) outOfStockCount++;
            if ("Out-of-Stock".equals(model.getChennai())) outOfStockCount++;
            if ("Out-of-Stock".equals(model.getDelhi())) outOfStockCount++;
            if ("Out-of-Stock".equals(model.getHyderabad())) outOfStockCount++;
            if ("Out-of-Stock".equals(model.getIndore())) outOfStockCount++;
            if ("Out-of-Stock".equals(model.getCalcutta())) outOfStockCount++;
            if ("Out-of-Stock".equals(model.getMumbai())) outOfStockCount++;
            if ("Out-of-Stock".equals(model.getNagpur())) outOfStockCount++;
            if ("Out-of-Stock".equals(model.getPatna())) outOfStockCount++;
            if ("Out-of-Stock".equals(model.getPune())) outOfStockCount++;
            if ("Out-of-Stock".equals(model.getOther())) outOfStockCount++;

            outOfStockCountByPlatform.merge(model.getPlatform(), outOfStockCount, Integer::sum);
        }

        return outOfStockCountByPlatform;
    }

    private MergedModel createMergedModel(CSVModel csvModel, List<ExcelModel> excelModels) {
        MergedModel mergedModel = new MergedModel();
        ExcelModel firstExcelModel = excelModels.get(0);
        Set<String> platforms = new HashSet<>();
        Set<String> outOfStockLocations = new HashSet<>();
        excelModels.stream()
                .map(ExcelModel::getPlatform)
                .distinct()
                .forEach(platforms::add);



        mergedModel.setPlatform(firstExcelModel.getPlatform());
        mergedModel.setAsin(firstExcelModel.getAsin());
        mergedModel.setPname(firstExcelModel.getPname());
        mergedModel.setInternalDivision(csvModel.getInternalDivision());
        mergedModel.setBrand(csvModel.getBrand());
        mergedModel.setCategory(csvModel.getCategory());
        mergedModel.setSubCategory(csvModel.getSubCategory());
        double revenue = Double.parseDouble(csvModel.getRevenue());
        String formattedRevenue = String.format("%.2f", revenue);
        mergedModel.setRevenue(formattedRevenue);

        double daySales = revenue / 30;
        String formattedDaySales = String.format("%.2f", daySales);
        mergedModel.setDaySales(formattedDaySales);

        Map<String, String> cityStatusMap = new LinkedHashMap<>();
        int countOfZeros = 0;
        int count = 0;
        for (ExcelModel excelModel : excelModels) {

            if (excelModel.getStatus().equals("1") || excelModel.getStatus().equals("0")) {
                count++;
            }

            if (excelModel.getCity() != null && !excelModel.getCity().isEmpty() && !excelModel.getCity().equals("-")) {

                cityStatusMap.put(excelModel.getCity(), excelModel.getStatus());

                if (excelModel.getStatus().equals("0")) {
                    countOfZeros++;
                }
            }
        }
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


        mergedModel.setOther(findValuesForCity(cityStatusMap, "Other"));


        for (ExcelModel excel : excelModels) {

            if (excel.getCity().equalsIgnoreCase("Ahmedabad") && count != 0 ) {
                mergedModel.setAhmedabadPercentage(String.valueOf(100 / count));
            }
            if (excel.getCity().equalsIgnoreCase("Bangalore") && count != 0) {
                mergedModel.setBangalorePercentage(String.valueOf(100 / count));
            }

            if (excel.getCity().equalsIgnoreCase("Chennai") && count != 0) {
                mergedModel.setChennaiPercentage(String.valueOf(100 / count));
            }
            if (excel.getCity().equalsIgnoreCase("Delhi") && count != 0) {
                mergedModel.setDelhiPercentage(String.valueOf(100 / count));
            }
            if (excel.getCity().equalsIgnoreCase("Hyderabad") && count != 0) {
                mergedModel.setHyderabadPercentage(String.valueOf(100 / count));
            }
            if (excel.getCity().equalsIgnoreCase("Indore") && count != 0) {
                mergedModel.setIndorePercentage(String.valueOf(100 / count));
            }
            if (excel.getCity().equalsIgnoreCase("calcutta") && count != 0) {
                mergedModel.setCalcuttaPercentage(String.valueOf(100 / count));
            }
            if (excel.getCity().equalsIgnoreCase("Mumbai") && count != 0) {
                mergedModel.setMumbaiPercentage(String.valueOf(100 / count));
            }
            if (excel.getCity().equalsIgnoreCase("Nagpur") && count != 0) {
                mergedModel.setNagpurPercentage(String.valueOf(100 / count));
            }
            if (excel.getCity().equalsIgnoreCase("Patna") && count != 0) {
                mergedModel.setPatnaPercentage(String.valueOf(100 / count));
            }
            if (excel.getCity().equalsIgnoreCase("Pune") && count != 0) {
                mergedModel.setPunePercentage(String.valueOf(100 / count));
            }
        }

//        if (countOfZeros > 0) {
        if (!mergedModel.getAhmedabadPercentage().equals("-")) {
            double city = Double.parseDouble(mergedModel.getAhmedabadPercentage());
            mergedModel.setSwoosPercentage(100 - city * countOfZeros);
        }
        if (!mergedModel.getBangalorePercentage().equals("-")) {
            double city = Double.parseDouble(mergedModel.getBangalorePercentage());
            mergedModel.setSwoosPercentage(100 - city * countOfZeros);
        }
        if (!mergedModel.getCalcuttaPercentage().equals("-")) {
            double city = Double.parseDouble(mergedModel.getCalcuttaPercentage());
            mergedModel.setSwoosPercentage(100 - city * countOfZeros);
        }
        if (!mergedModel.getChennaiPercentage().equals("-")) {
            double city = Double.parseDouble(mergedModel.getChennaiPercentage());
            mergedModel.setSwoosPercentage(100 - city * countOfZeros);
        }
        if (!mergedModel.getDelhiPercentage().equals("-")) {
            double city = Double.parseDouble(mergedModel.getDelhiPercentage());
            mergedModel.setSwoosPercentage(100 - city * countOfZeros);
        }
        if (!mergedModel.getHyderabadPercentage().equals("-")) {
            double city = Double.parseDouble(mergedModel.getHyderabadPercentage());
            mergedModel.setSwoosPercentage(100 - city * countOfZeros);
        }
        if (!mergedModel.getIndorePercentage().equals("-")) {
            double city = Double.parseDouble(mergedModel.getIndorePercentage());
            mergedModel.setSwoosPercentage(100 - city * countOfZeros);
        }
        if (!mergedModel.getMumbaiPercentage().equals("-")) {
            double city = Double.parseDouble(mergedModel.getMumbaiPercentage());
            mergedModel.setSwoosPercentage(100 - city * countOfZeros);
        }
        if (!mergedModel.getNagpurPercentage().equals("-")) {
            double city = Double.parseDouble(mergedModel.getNagpurPercentage());
            mergedModel.setSwoosPercentage(100 - city * countOfZeros);
        }
        if (!mergedModel.getPatnaPercentage().equals("-")) {
            double city = Double.parseDouble(mergedModel.getPatnaPercentage());
            mergedModel.setSwoosPercentage(100 - city * countOfZeros);
        }
        if (!mergedModel.getPunePercentage().equals("-")) {
            double city = Double.parseDouble(mergedModel.getPunePercentage());
            mergedModel.setSwoosPercentage(100 - city * countOfZeros);
        }

        double a= 0;
        if (count != 0) {
            a = ((100 / count) * 100);

        }

        Map<String, Double> locationLossMap = new HashMap<>();
        double b = a * countOfZeros;
        double c = (b * Double.parseDouble(mergedModel.getRevenue()));
        double loss = c / 30;
        String formattedLoss = String.format("%.2f", (loss / 100) / 100);
        //System.out.print(formattedLoss);
        mergedModel.setValueLoss(formattedLoss);
        return mergedModel;
    }

    private String value(Map<String, String> cityStatusMap, String b) {
        String a;
        if (Objects.isNull(cityStatusMap.get(b))) {
            a = "NA";
        } else {
            a = cityStatusMap.get(b);
            int count = 0;
            if (a.equals("0")) {
                a = "Out-of-Stock";

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
    public Map<String,Map<String,String>> locations(Long id){
        MergedModel mergedModels = mergedRepository.findById(id).orElseThrow();
        Map<String,String> locations = new HashMap<>();
        List<ExcelModel> dataList = excelRepository.findAll();
        for(ExcelModel data : dataList){
            if (data.getCity().toLowerCase().contains("ahmedabad")) {
                if(mergedModels.getAhmedabad().equalsIgnoreCase("out-of-stock")){
                    locations.put("Ahmedabad",data.getLocation());
                }
            }
            if (data.getCity().toLowerCase().contains("bangalore")) {
                if(mergedModels.getBangalore().equalsIgnoreCase("Out-of-Stock")){
                    locations.put("Bangalore",data.getLocation());
                }
            }
            if (data.getCity().toLowerCase().contains("indore")) {
                if(mergedModels.getIndore().equalsIgnoreCase("Out-of-Stock")){
                    locations.put("Indore",data.getLocation());
                }
            }
            if (data.getCity().toLowerCase().contains("chennai")) {
                if(mergedModels.getChennai().equalsIgnoreCase("Out-of-Stock")){
                    locations.put("Chennai",data.getLocation());
                }
            }
            if (data.getCity().toLowerCase().contains("delhi")) {
                if(mergedModels.getDelhi().equalsIgnoreCase("Out-of-Stock")){
                    locations.put("Delhi",data.getLocation());
                }
            }
            if (data.getCity().toLowerCase().contains("hyderabad")) {
                if(mergedModels.getHyderabad().equalsIgnoreCase("Out-of-Stock")){
                    locations.put("Hyderabad",data.getLocation());
                }
            }
            if (data.getCity().toLowerCase().contains("calcutta")) {
                if(mergedModels.getCalcutta().equalsIgnoreCase("Out-of-Stock")){
                    locations.put("calcutta",data.getLocation());
                }
            }
            if (data.getCity().toLowerCase().contains("mumbai")) {
                if(mergedModels.getMumbai().equalsIgnoreCase("Out-of-Stock")){
                    locations.put("Mumbai",data.getLocation());
                }
            }
            if (data.getCity().toLowerCase().contains("nagpur")) {
                if(mergedModels.getNagpur().equalsIgnoreCase("Out-of-Stock")){
                    locations.put("Nagpur",data.getLocation());
                }
            }
            if (data.getCity().toLowerCase().contains("patna")) {
                if(mergedModels.getPatna().equalsIgnoreCase("Out-of-Stock")){
                    locations.put("Patna",data.getLocation());
                }
            }
            if (data.getCity().toLowerCase().contains("pune")) {
                if(mergedModels.getPune().equalsIgnoreCase("Out-of-Stock")){
                    locations.put("Pune",data.getLocation());
                }
            }
//            if (data.getCity().toLowerCase().contains("pune1")) {
//                if (mergedModels.getOther() != null && mergedModels.getOther().equalsIgnoreCase("Out-of-Stock")){
//                    locations.put("Pune",data.getLocation());
//                }
//            }

            if (data.getCity().toLowerCase().contains("ghaziabad")) {
                if (mergedModels.getOther() != null && mergedModels.getOther().equalsIgnoreCase("Out-of-Stock")){
                    locations.put("Ghaziabad",data.getLocation());
                }
            }
            if (data.getCity().toLowerCase().contains("gurgaon")) {
                if (mergedModels.getOther() != null && mergedModels.getOther().equalsIgnoreCase("Out-of-Stock")){
                    locations.put("Gurgaon",data.getLocation());
                }
            }
            if (data.getCity().toLowerCase().contains("jaipur")) {
                if (mergedModels.getOther() != null && mergedModels.getOther().equalsIgnoreCase("Out-of-Stock")){
                    locations.put("Jaipur",data.getLocation());
                }
            }
            if (data.getCity().toLowerCase().contains("lucknow")) {
                if (mergedModels.getOther() != null && mergedModels.getOther().equalsIgnoreCase("Out-of-Stock")){
                    locations.put("Lucknow",data.getLocation());
                }
            }
            if (data.getCity().toLowerCase().contains("chandigarh hq")) {
                if (mergedModels.getOther() != null && mergedModels.getOther().equalsIgnoreCase("Out-of-Stock")){
                    locations.put("Chandigarh HQ",data.getLocation());
                }
            }
            if (data.getCity().toLowerCase().contains("ambala hq")) {
                if (mergedModels.getOther() != null && mergedModels.getOther().equalsIgnoreCase("Out-of-Stock")){
                    locations.put("Ambala HQ",data.getLocation());
                }
            }
            if (data.getCity().toLowerCase().contains("Goa-Panaji")) {
                if (mergedModels.getOther() != null && mergedModels.getOther().equalsIgnoreCase("Out-of-Stock")){
                    locations.put("Goa-Panaji",data.getLocation());
                }
            }

         mergedModels.getTotalValueLoss();
        }
        Map<String,Map<String,String>>dto = new HashMap<>();
        dto.put("location",locations);
      return  dto;
    }

    public SuccessResponse<Object> readHistoryTrue(HttpServletResponse response) {
        SuccessResponse<Object>successResponse = new SuccessResponse<>();
        List<MergedModel> mergedModels =mergedRepository.getAllHistoryTrue();
        successResponse.setStatusCode(200);
        List<MergedModelDto>mergedModelDtos=new ArrayList<>();
        for(MergedModel mergedModel :mergedModels){
            MergedModelDto mergedModelDto =new MergedModelDto();
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
            //mergedModelDto.setLocation(mergedModel.getLocation());
            mergedModelDto.setHistory(mergedModel.isHistoryFlag());
            mergedModelDto.setIsSubmited(mergedModel.getIsSubmited());
            mergedModelDto.setDeletedFlag(mergedModel.isDeletedFlag());
            mergedModelDto.setCreateAt(String.valueOf(mergedModel.getCreateAt()));
            mergedModelDto.setUpdatedAt(String.valueOf(mergedModel.getUpdatedAt()));
            mergedModelDtos.add(mergedModelDto);
        }
        successResponse.setData(mergedModelDtos);
        return successResponse;
    }
}
*/
