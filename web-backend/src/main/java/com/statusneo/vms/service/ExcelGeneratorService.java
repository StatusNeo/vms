package com.statusneo.vms.service;

import com.statusneo.vms.model.Visitor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelGeneratorService {

    public static byte[] generateExcel(List<Visitor> visitors) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Visitors");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Name", "Email", "Phone Number", "Address", "Registered At"};

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Populate data
        int rowNum = 1;
        for (Visitor visitor : visitors) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(visitor.getId());
            row.createCell(1).setCellValue(visitor.getName());
            row.createCell(2).setCellValue(visitor.getEmail());
            row.createCell(3).setCellValue(visitor.getPhoneNumber());
            row.createCell(4).setCellValue(visitor.getAddress());
            row.createCell(5).setCellValue(visitor.getCreatedAt().toString());
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }
}