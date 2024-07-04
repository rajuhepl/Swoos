package com.example.swoos.service.serviceimpl;

import com.example.swoos.model.MergedModel;
import com.example.swoos.repository.MergedRepository;
import com.example.swoos.response.SuccessResponse;
import com.example.swoos.service.ExcelService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ExcelWriterServiceImpl implements ExcelService {
    @Autowired
    private MergedRepository mergedRepository;

    public SuccessResponse<Object> historyToExcel(HttpServletResponse response) {
        try {

            LocalDate todayDate = LocalDate.now();
            LocalDateTime fromDateTime = LocalDateTime.of(todayDate, LocalTime.MIN);
            LocalDateTime toDateTime = LocalDateTime.of(todayDate, LocalTime.MAX);
            Timestamp fromDate = Timestamp.valueOf(fromDateTime);
            Timestamp toDate = Timestamp.valueOf(toDateTime);
            List<MergedModel> mergedModels = mergedRepository.getAllHistoryTrue(fromDate, toDate);
            if (mergedModels.isEmpty()) {
                throw new RuntimeException("No data found in the specified date range.");
            }
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Merged Data");

            // Create headers
            Row headerRow = sheet.createRow(0);
            String[] headers = {"MergedID", "Ahmedabad", "Ahmedabad%", "ASIN", "Bangalore", "Bangalore%", "Calcutta", "Calcutta%", "Category",
                    "Chennai", "Chennai %", "Delhi", "Delhi %", "Hyderabad", "Hyderabad %", "Indore", "Indore %", "InternalDivision", "Mumbai", "Mumbai%",
                    "NA", "Nagpur", "Nagpur%", "Patna", "Patna%", "Platform", "ProductName", "Pune", "Pune%", "Revenue", "SWOOSContribution", "Sub-Category",
                    "SWOOS(%)", "ValueLoss", "Brand", "City", "Day", "DaySales", "LastDayReason", "MonthlySales", "Other", "Reason", "Remarks",
                    "TotalValueLoss", "CreatedAt", "DeletedFlag", "IsSubmitted", "UpdatedAt", "Location", "Date", "HistoryFlag"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (MergedModel mergedModel : mergedModels) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(mergedModel.getPlatform());
                row.createCell(1).setCellValue(mergedModel.getAsin());
                row.createCell(2).setCellValue(mergedModel.getPname());
                row.createCell(3).setCellValue(mergedModel.getRevenue());
                row.createCell(4).setCellValue(mergedModel.getInternalDivision());
                row.createCell(5).setCellValue(mergedModel.getBrand());
                row.createCell(6).setCellValue(mergedModel.getCategory());
                row.createCell(7).setCellValue(mergedModel.getSubCategory());
                row.createCell(8).setCellValue(mergedModel.getAhmedabad());
                row.createCell(9).setCellValue(mergedModel.getBangalore());
                row.createCell(10).setCellValue(mergedModel.getChennai());
                row.createCell(11).setCellValue(mergedModel.getDelhi());
                row.createCell(12).setCellValue(mergedModel.getHyderabad());
                row.createCell(13).setCellValue(mergedModel.getIndore());
                row.createCell(14).setCellValue(mergedModel.getCalcutta());
                row.createCell(15).setCellValue(mergedModel.getMumbai());
                row.createCell(16).setCellValue(mergedModel.getNagpur());
                row.createCell(17).setCellValue(mergedModel.getPatna());
                row.createCell(18).setCellValue(mergedModel.getPune());
                row.createCell(19).setCellValue(mergedModel.getAhmedabadPercentage());
                row.createCell(20).setCellValue(mergedModel.getBangalorePercentage());
                row.createCell(21).setCellValue(mergedModel.getChennaiPercentage());
                row.createCell(22).setCellValue(mergedModel.getDelhiPercentage());
                row.createCell(23).setCellValue(mergedModel.getHyderabadPercentage());
                row.createCell(24).setCellValue(mergedModel.getIndorePercentage());
                row.createCell(25).setCellValue(mergedModel.getCalcuttaPercentage());
                row.createCell(26).setCellValue(mergedModel.getMumbaiPercentage());
                row.createCell(27).setCellValue(mergedModel.getNagpurPercentage());
                row.createCell(28).setCellValue(mergedModel.getPatnaPercentage());
                row.createCell(29).setCellValue(mergedModel.getPunePercentage());
                row.createCell(30).setCellValue(mergedModel.getNA());
                row.createCell(31).setCellValue(mergedModel.getMonthlySales());
                row.createCell(32).setCellValue(mergedModel.getDaySales());

                // Handling nullable fields
                row.createCell(33).setCellValue(mergedModel.getSwoosPercentage() != null ? mergedModel.getSwoosPercentage().toString() : "");
                row.createCell(34).setCellValue(mergedModel.getValueLoss());
                row.createCell(35).setCellValue(mergedModel.getSWOOSContribution());
                row.createCell(36).setCellValue(mergedModel.getTotalValueLoss() != null ? mergedModel.getTotalValueLoss().toString() : "");
                row.createCell(37).setCellValue(mergedModel.getOther());
                row.createCell(38).setCellValue(mergedModel.getRemarks());
                row.createCell(39).setCellValue(mergedModel.getReason());

                // Handling LocalDateTime or LocalDate fields
                row.createCell(40).setCellValue(mergedModel.getDay() != null ? mergedModel.getDay().toString() : "");
                row.createCell(41).setCellValue(mergedModel.getLastDayReason());
                row.createCell(42).setCellValue(mergedModel.getCity());

                // Handling Boolean fields
                Boolean isSubmitted = mergedModel.getIsSubmited();
                row.createCell(43).setCellValue(isSubmitted != null && isSubmitted);

                Boolean deletedFlag = mergedModel.isDeletedFlag();
                row.createCell(44).setCellValue(deletedFlag != null && deletedFlag);

                // Handling Timestamp or LocalDateTime fields
                row.createCell(45).setCellValue(mergedModel.getCreateAt() != null ? mergedModel.getCreateAt().toString() : "");
                row.createCell(46).setCellValue(mergedModel.getUpdatedAt() != null ? mergedModel.getUpdatedAt().toString() : "");

                row.createCell(47).setCellValue(mergedModel.getLocation());

                // Handling LocalDate or LocalDateTime fields
                row.createCell(48).setCellValue(mergedModel.getDate() != null ? mergedModel.getDate().toString() : "");

                Boolean historyFlag = mergedModel.isHistoryFlag();
                row.createCell(49).setCellValue(historyFlag != null && historyFlag);
            }

            // Set response headers
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=History.xlsx");

            // Write to response output stream
            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            workbook.close();

            // Return success response
            SuccessResponse<Object> successResponse = new SuccessResponse<>();
            successResponse.setStatusCode(200);
            return successResponse;
        } catch (IOException e) {
            throw new RuntimeException("Failed to write Excel file.", e);
        }
    }

}