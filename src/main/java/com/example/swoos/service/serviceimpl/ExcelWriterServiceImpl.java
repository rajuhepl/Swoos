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
import java.time.LocalDateTime;
import java.util.List;
@Service
public class ExcelWriterServiceImpl implements ExcelService {
    @Autowired
    private MergedRepository mergedRepository;
    public SuccessResponse<Object> historyToExcel(HttpServletResponse response) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime twentyFourHoursAgo = now.minusHours(24);
            Timestamp fromDate = Timestamp.valueOf(twentyFourHoursAgo);
            Timestamp toDate = Timestamp.valueOf(now);
            List<MergedModel> mergedModels = mergedRepository.getAllHistoryTrue(fromDate, toDate);
            if (mergedModels.isEmpty()) {
                throw new RuntimeException("No data found in the specified date range.");
            }
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Merged Data");
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Platform", "ASIN", "ProductName", "Revenue", "InternalDivision", "Brand", "Category",
                    "SubCategory", "Ahmedabad", "Bangalore", "Chennai", "Delhi", "Hyderabad", "Indore",
                    "Calcutta", "Mumbai", "Nagpur", "Patna", "Pune", "Ahmedabad%", "Bangalore%", "Chennai%",
                    "Delhi%", "Hyderabad%", "Indore%", "Calcutta%", "Mumbai%", "Nagpur%", "Patna%", "Pune%",
                    "NA","DaySales", "SWOOS%", "ValueLoss", "SWOOSContribution",
                    "Other", "Remarks", "Reason", "LastDayReason", "Deleted",
                    "CreatedAt", "UpdatedAt", "HistoryFlag"};

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
                row.createCell(31).setCellValue(mergedModel.getDaySales());
                row.createCell(32).setCellValue(mergedModel.getSwoosPercentage() != null ? mergedModel.getSwoosPercentage().toString() : "");
                row.createCell(33).setCellValue(mergedModel.getValueLoss());
                row.createCell(34).setCellValue(mergedModel.getSWOOSContribution());
                row.createCell(35).setCellValue(mergedModel.getOther());
                row.createCell(36).setCellValue(mergedModel.getRemarks());
                row.createCell(37).setCellValue(mergedModel.getReason());
                row.createCell(38).setCellValue(mergedModel.getLastDayReason());
                Boolean deletedFlag = mergedModel.isDeletedFlag();
                row.createCell(39).setCellValue(deletedFlag != null && deletedFlag);
                row.createCell(40).setCellValue(mergedModel.getCreateAt() != null ? mergedModel.getCreateAt().toString() : "");
                row.createCell(41).setCellValue(mergedModel.getUpdatedAt() != null ? mergedModel.getUpdatedAt().toString() : "");
                Boolean historyFlag = mergedModel.isHistoryFlag();
                row.createCell(42).setCellValue(historyFlag != null && historyFlag);
            }
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=History.xlsx");
            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            workbook.close();
            SuccessResponse<Object> successResponse = new SuccessResponse<>();
            successResponse.setStatusCode(200);
            return successResponse;
        } catch (IOException e) {
            throw new RuntimeException("Failed to write Excel file.", e);
        }
    }
}