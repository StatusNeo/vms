package com.statusneo.vms.service;

import com.statusneo.vms.model.Visitor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelGeneratorService {
    public static byte[] generateExcel(List<Visitor> visitors) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Visitors");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Name", "Phone", "Email", "Address"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (Visitor visitor : visitors) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(visitor.getId());
                row.createCell(1).setCellValue(visitor.getName());
                row.createCell(2).setCellValue(visitor.getPhoneNumber());
                row.createCell(3).setCellValue(visitor.getEmail());
                row.createCell(4).setCellValue(visitor.getAddress());
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}