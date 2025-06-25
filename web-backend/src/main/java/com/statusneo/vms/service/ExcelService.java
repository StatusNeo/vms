package com.statusneo.vms.service;

import com.statusneo.vms.model.Visitor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelService.class);

    public byte[] generateVisitorExcel(List<Visitor> visitors) throws IOException {
        logger.info("Generating Excel file for {} visitors", visitors.size());
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Visitors");

            // Header Row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Name", "Email", "Phone", "Address", "Registered At"};

            // Apply header styling
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data Rows
            int rowNum = 1;
            for (Visitor visitor : visitors) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(visitor.getId());
                row.createCell(1).setCellValue(visitor.getName());
                row.createCell(2).setCellValue(visitor.getEmail());
                row.createCell(3).setCellValue(visitor.getPhoneNumber());
                row.createCell(4).setCellValue(visitor.getAddress());
                row.createCell(5).setCellValue(visitor.getCreatedAt().toString());  // createdAt is a LocalDateTime
//                row.createCell(5).setCellValue(visitor.getRegisteredAt().toString());
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            byte[] excelData = out.toByteArray();
            logger.info("Excel file generated with size: {} bytes", excelData.length);
            return excelData;
        } catch (IOException e) {
            logger.error("Failed to generate Excel file", e);
            throw e;
        }
    }
}