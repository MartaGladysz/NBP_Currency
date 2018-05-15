package com.app.service;

import com.app.model.Report;
import com.app.model.ReportConfig;
import com.app.repository.ReportRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Transactional
@Service
public class ReportServiceImpl implements ReportService {

    private ReportRepository reportRepository;

    public ReportServiceImpl(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public String createMessage(Map<String, LinkedList<BigDecimal>> chosenCurrenciesMap, ReportConfig reportConfig) {
        StringBuilder message = new StringBuilder();
        for (int i=0; i < reportConfig.getNumberOfQuotations(); i++){
            message.append("[");
            for (int j=0; j < reportConfig.getCurrenciesList().size(); j++){
                Pattern pattern = Pattern.compile("[A-Z]{3}");
                Matcher matcher = pattern.matcher(reportConfig.getCurrenciesList().get(j));
                matcher.find();
                message.append(matcher.group(0)).append(": ")
                        .append(chosenCurrenciesMap.get(reportConfig.getCurrenciesList().get(j)).get(i)).append(", ");
            }
            message.delete(message.lastIndexOf(", "), message.length());
            message.append("],");
        }
        message.delete(message.lastIndexOf(","), message.length());
        return message.toString();
    }

    @Override
    public Optional<Report> getLastReport() {
        return reportRepository.findAll().stream().max(Comparator.comparing(Report::getId));
    }

    @Override
    public ByteArrayInputStream currenciesReport(Report report) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(95);
            table.setWidths(new int[]{1, 5});

            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);

            PdfPCell hCell;

            hCell = new PdfPCell(new Phrase("Date", headFont));
            hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(hCell);

            hCell = new PdfPCell(new Phrase("Message", headFont));
            hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(hCell);


            PdfPCell cell;

            cell = new PdfPCell(new Phrase(report.getDate().toString()));
            cell.setPaddingRight(5);
            cell.setPaddingLeft(5);
            cell.setPaddingBottom(5);
            cell.setPaddingTop(5);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase((report.getMessage())));
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPaddingRight(5);
            cell.setPaddingLeft(5);
            cell.setPaddingBottom(5);
            cell.setPaddingTop(5);
            table.addCell(cell);


            PdfWriter.getInstance(document, out);
            document.open();
            document.add(table);

            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
