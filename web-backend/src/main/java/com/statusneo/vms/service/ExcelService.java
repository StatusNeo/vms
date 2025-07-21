/* ********************************************************************
 *
 *  Copyright (c) 2025 Statusneo.
 *
 * All rights Reserved.
 *
 * Redistribution and use of any source or binary or in any form, without
 * written approval and permission is prohibited.
 *
 * Please read the Terms of use, Disclaimer and Privacy Policy on https://www.statusneo.com/
 *
 * **********************************************************************
 */
package com.statusneo.vms.service;

import com.statusneo.vms.model.Email;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.model.Attachment;
import com.statusneo.vms.repository.VisitorRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelService.class);
    private final VisitorRepository visitorRepository;
    private final EmailService emailService;
    @Value("${vms.system-email}")
    private String systemEmail;
    @Value("${vms.admin-email}")
    private String adminEmail;

    public ExcelService(VisitorRepository visitorRepository, EmailService emailService) {
        this.visitorRepository = visitorRepository;
        this.emailService = emailService;
    }

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

    void sendVisitorReport() {
        try {
            List<Visitor> visitors = visitorRepository.findAll();
            byte[] excelFile = generateVisitorExcel(visitors);
            Attachment attachment = new Attachment(
                    "visitor_report.xlsx",
                    excelFile,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );
            emailService.sendEmail(
                    new Email(
                            systemEmail,
                            List.of(adminEmail),
                            "Visitor Report",
                            "Please find attached the visitor report.",
                            List.of(attachment)
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to send visitor report", e);
        }
    }
}