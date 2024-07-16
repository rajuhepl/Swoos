package com.example.swoos.service.serviceimpl;

import com.example.swoos.dto.MergeRequestDTO;
import com.example.swoos.model.*;
import com.example.swoos.repository.DropDownRepository;
import com.example.swoos.repository.MergedRepository;
import com.example.swoos.repository.RemarksRepository;
import com.example.swoos.service.MergeFileService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MergeFileServiceImpl implements MergeFileService {
    @Autowired
    private MergedRepository mergedRepository;
    @Autowired
    private DropDownRepository dropDownRepository;
    @Autowired
    private RemarksRepository remarksRepository;

    public ResponseEntity<String> readCSVData(MultipartFile csvFile, MultipartFile excelFile, HttpServletResponse response) {
        try {
            List<CSVModel> csvData = readCSV(csvFile);
            List<ExcelModel> excelData = readExcel(excelFile);
            String responseData = "CSV Data: " + csvData.toString() + "\n" +
                    "Excel Data: " + excelData.toString();
            return ResponseEntity.ok().body(responseData);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private List<CSVModel> readCSV(MultipartFile csvFile) throws IOException {
        List<CSVModel> data = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvFile.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                CSVModel csvModel = new CSVModel();
                csvModel.setBusiness(values[0]);
                csvModel.setPlatform(values[1]);
                csvModel.setMainSKUCode(values[2]);
                csvModel.setInternalDivision(values[3]);
                csvModel.setBrand(values[4]);
                csvModel.setCategory(values[5]);
                csvModel.setSubCategory(values[6]);
                csvModel.setRevenue(values[7]);
                data.add(csvModel);
            }
        }
        return data;
    }

    private List<ExcelModel> readExcel(MultipartFile excelFile) throws IOException {
        List<ExcelModel> data = new ArrayList<>();

        try (
                Workbook workbook = new XSSFWorkbook(excelFile.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                ExcelModel excelModel = new ExcelModel();
                excelModel.setPlatform(row.getCell(0).getStringCellValue());
                excelModel.setAsin(row.getCell(1).getStringCellValue());
                excelModel.setPname(row.getCell(2).getStringCellValue());
                excelModel.setBrand(row.getCell(3).getStringCellValue());
                excelModel.setLocation(row.getCell(4).getStringCellValue());
                excelModel.setCity(row.getCell(5).getStringCellValue());
                excelModel.setStatus(row.getCell(6).getStringCellValue());
                data.add(excelModel);
            }
        }
        return data;
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