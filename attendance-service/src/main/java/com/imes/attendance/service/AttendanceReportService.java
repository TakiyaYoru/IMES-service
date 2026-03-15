package com.imes.attendance.service;

import com.imes.infra.entity.AttendanceEntity;
import com.imes.infra.repository.AttendanceRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AttendanceReportService {

    private final AttendanceRepository attendanceRepository;

    public ReportFile generateMonthlyReport(int year, int month, String format) {
        validateMonth(year, month);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();
        List<AttendanceEntity> records = attendanceRepository.findAllInDateRange(startDate, endDate);

        String normalizedFormat = normalizeFormat(format);
        String filePrefix = "attendance_report_" + year + "_" + String.format("%02d", month);

        if ("EXCEL".equals(normalizedFormat)) {
            return new ReportFile(
                    generateExcel(records, year, month),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    filePrefix + ".xlsx"
            );
        }

        return new ReportFile(
                generatePdf(records, year, month),
                "application/pdf",
                filePrefix + ".pdf"
        );
    }

    private byte[] generatePdf(List<AttendanceEntity> records, int year, int month) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(output);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            document.add(new Paragraph("Attendance Monthly Report"));
            document.add(new Paragraph("Period: " + year + "-" + String.format("%02d", month)));
            document.add(new Paragraph("Generated at: " + LocalDateTime.now()));
            document.add(new Paragraph("Total records: " + records.size()));

            Table table = new Table(UnitValue.createPercentArray(new float[]{1.1f, 1.1f, 1.1f, 1.2f, 1.2f, 1.0f}));
            table.useAllAvailableWidth();
            table.addHeaderCell("Intern ID");
            table.addHeaderCell("Date");
            table.addHeaderCell("Status");
            table.addHeaderCell("Check-in");
            table.addHeaderCell("Check-out");
            table.addHeaderCell("Total Hours");

            for (AttendanceEntity record : records) {
                table.addCell(String.valueOf(record.getInternProfileId()));
                table.addCell(String.valueOf(record.getDate()));
                table.addCell(record.getStatus() != null ? record.getStatus().name() : "-");
                table.addCell(record.getCheckInTime() != null ? record.getCheckInTime().toString() : "-");
                table.addCell(record.getCheckOutTime() != null ? record.getCheckOutTime().toString() : "-");
                table.addCell(record.getTotalHours() != null ? record.getTotalHours().toString() : "0");
            }

            document.add(table);
            document.close();
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate PDF attendance report", e);
        }
    }

    private byte[] generateExcel(List<AttendanceEntity> records, int year, int month) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Attendance " + year + "-" + String.format("%02d", month));

            int rowIdx = 0;
            Row info = sheet.createRow(rowIdx++);
            info.createCell(0).setCellValue("Attendance Monthly Report");
            Row period = sheet.createRow(rowIdx++);
            period.createCell(0).setCellValue("Period");
            period.createCell(1).setCellValue(year + "-" + String.format("%02d", month));
            Row total = sheet.createRow(rowIdx++);
            total.createCell(0).setCellValue("Total records");
            total.createCell(1).setCellValue(records.size());

            rowIdx++;
            Row header = sheet.createRow(rowIdx++);
            header.createCell(0).setCellValue("Intern ID");
            header.createCell(1).setCellValue("Date");
            header.createCell(2).setCellValue("Status");
            header.createCell(3).setCellValue("Check-in");
            header.createCell(4).setCellValue("Check-out");
            header.createCell(5).setCellValue("Total Hours");

            for (AttendanceEntity record : records) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(record.getInternProfileId() != null ? record.getInternProfileId() : 0);
                row.createCell(1).setCellValue(record.getDate() != null ? record.getDate().toString() : "");
                row.createCell(2).setCellValue(record.getStatus() != null ? record.getStatus().name() : "");
                row.createCell(3).setCellValue(record.getCheckInTime() != null ? record.getCheckInTime().toString() : "");
                row.createCell(4).setCellValue(record.getCheckOutTime() != null ? record.getCheckOutTime().toString() : "");
                row.createCell(5).setCellValue(record.getTotalHours() != null ? record.getTotalHours().doubleValue() : 0.0);
            }

            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate Excel attendance report", e);
        }
    }

    private String normalizeFormat(String format) {
        if (format == null || format.isBlank()) {
            return "PDF";
        }
        String value = format.trim().toUpperCase(Locale.ROOT);
        if (!"PDF".equals(value) && !"EXCEL".equals(value)) {
            throw new IllegalArgumentException("format must be PDF or EXCEL");
        }
        return value;
    }

    private void validateMonth(int year, int month) {
        if (year < 2000 || year > 2100) {
            throw new IllegalArgumentException("year must be between 2000 and 2100");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }
    }

    public record ReportFile(byte[] content, String contentType, String fileName) {
    }
}
