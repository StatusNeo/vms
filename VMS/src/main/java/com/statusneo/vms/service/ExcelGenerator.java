package com.statusneo.vms.service;

import com.statusneo.vms.model.Visitor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelGenerator {

    public static byte[] generateExcel(List<Visitor> visitors) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            var sheet = workbook.createSheet("Visitors");

            // Creating header row
            var headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Name", "Phone Number", "Email", "Address", "Created At"};
            for (int i = 0; i < columns.length; i++) {
                headerRow.createCell(i).setCellValue(columns[i]);
            }

            // Filling data rows
            int rowIdx = 1;
            for (var visitor : visitors) {
                var row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(visitor.getId());
                row.createCell(1).setCellValue(visitor.getName());
                row.createCell(2).setCellValue(visitor.getPhoneNumber());
                row.createCell(3).setCellValue(visitor.getEmail());
                row.createCell(4).setCellValue(visitor.getAddress());
                row.createCell(5).setCellValue(visitor.getCreatedAt().toString()); // Assuming createdAt is a Date
            }

            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating Excel file", e);
        }

    }
}