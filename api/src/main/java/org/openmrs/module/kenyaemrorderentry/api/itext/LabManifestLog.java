package org.openmrs.module.kenyaemrorderentry.api.itext;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabOrderDataExchange;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.kenyaemrorderentry.util.Utils;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

public class LabManifestLog {

    public static final String LOGO = "src/main/resources/img/moh.png";
    public static final PdfNumber LANDSCAPE = new PdfNumber(90);
    LabManifest manifest;


    public LabManifestLog() {
    }

    public LabManifestLog(LabManifest manifest) {
        this.manifest = manifest;
    }

    public File generateReport(String dest) throws IOException {

        File returnFile = File.createTempFile("labManifest", ".pdf");
        FileOutputStream fos = new FileOutputStream(returnFile);

        //Initialize PDF writer
        PdfWriter writer = new PdfWriter(fos);


        //Initialize PDF document
        PdfDocument pdf = new PdfDocument(writer);

        // The default page rotation is set to portrait in the custom event handler.
        /*PageOrientationsEventHandler eventHandler = new PageOrientationsEventHandler();
        pdf.addEventHandler(PdfDocumentEvent.START_PAGE, eventHandler);*/
        //eventHandler.setOrientation(LANDSCAPE);

        // Initialize document
        Document document = new Document(pdf, PageSize.A4.rotate());


        URL logoUrl = LabManifest.class.getClassLoader().getResource("img/moh.png");
        // Compose Paragraph
        Image logiImage = new Image(ImageDataFactory.create(logoUrl));
        logiImage.scaleToFit(80, 80);
        logiImage.setFixedPosition((PageSize.A4.rotate().getWidth() - logiImage.getImageScaledWidth()) /2, (PageSize.A4.rotate().getHeight() - logiImage.getImageScaledHeight()) - 40);

        //logoSection.add(logiImage);


        document.add(logiImage);
        // Create a PdfFont
        PdfFont font = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
        PdfFont courier = PdfFontFactory.createFont(StandardFonts.COURIER_BOLD);
        // Add a Paragraph

        document.add(new Paragraph("\n"));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("MINISTRY OF HEALTH").setTextAlignment(TextAlignment.CENTER).setFontSize(12));
        document.add(new Paragraph("HEALTH FACILITY SAMPLES AND RESULTS TRACKING LOG BOOK").setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(12));
        document.add(new Paragraph("FACILITY: " + Utils.getDefaultLocation().getName() + " (" + Utils.getDefaultLocationMflCode(Utils.getDefaultLocation()) + ")").setTextAlignment(TextAlignment.LEFT).setFontSize(10).setFont(courier));
        document.add(new Paragraph("Manifest/Shipping ID: " + (manifest.getIdentifier() != null ? manifest.getIdentifier() : "")).setTextAlignment(TextAlignment.LEFT).setFontSize(10).setFont(courier));

        // Start Summary Row
        Table sampleLogMetadata = new Table(4); // The number of columns in the manifest form (with manifest HEADER)
        sampleLogMetadata.setWidth(UnitValue.createPercentValue(100));
        sampleLogMetadata.setFont(font);

        // adding column 1
        Paragraph fDetailsCol1 = new Paragraph();
        Text totalSamples = new Text("Total Samples: ").setFontSize(10);
        Text totalSamplesVal = new Text(String.valueOf(Utils.getTotalSamplesInAManifest(manifest))).setFontSize(10).setFont(courier);
        fDetailsCol1.add(totalSamples).add(totalSamplesVal);

        // adding column 2
        Paragraph fDetailsCol2 = new Paragraph();
        Text totalSuppressed = new Text("Total Suppressed: ").setFontSize(10);
        Text totalSuppressedVal = new Text(String.valueOf(Utils.getSamplesSuppressedInAManifest(manifest))).setFontSize(10).setFont(courier);
        fDetailsCol2.add(totalSuppressed).add(totalSuppressedVal);

        // adding column 3
        Paragraph fDetailsCol3 = new Paragraph();
        Text totalUnsuppressed = new Text("Total unsuppressed: ").setFontSize(10);
        Text totalUnsuppressedVal = new Text(String.valueOf(Utils.getSamplesUnsuppressedInAManifest(manifest))).setFontSize(10).setFont(courier);
        fDetailsCol3.add(totalUnsuppressed).add(totalUnsuppressedVal);

        // adding column 4
        Paragraph fDetailsCol4 = new Paragraph();
        Text totalRejected = new Text("Total Rejected: ").setFontSize(10);
        Text totalRejectedVal = new Text(String.valueOf(Utils.getSamplesRejectedInAManifest(manifest))).setFontSize(10).setFont(courier);
        fDetailsCol4.add(totalRejected).add(totalRejectedVal);

        Cell col1 = new Cell();
        col1.setBorder(Border.NO_BORDER);
        
        Cell col2 = new Cell();
        col2.setBorder(Border.NO_BORDER);
        
        Cell col3 = new Cell();
        col3.setBorder(Border.NO_BORDER);
        
        Cell col4 = new Cell();
        col4.setBorder(Border.NO_BORDER);
        
        col1.add(fDetailsCol1);
        col2.add(fDetailsCol2);
        col3.add(fDetailsCol3);
        col4.add(fDetailsCol4);

        sampleLogMetadata.addCell(col1);
        sampleLogMetadata.addCell(col2);
        sampleLogMetadata.addCell(col3);
        sampleLogMetadata.addCell(col4);

        document.add(sampleLogMetadata);

        // End Summary Row

        Table table = new Table(14); // The number of columns in the sample log form (with manifest DATA)
        table.setWidth(UnitValue.createPercentValue(100));
        table.setFont(font);

        Table tWithHeaderRow = addHeaderRow(table);
        Table tWithData = addManifestSamples(this.manifest, tWithHeaderRow);

        document.add(tWithData);

        // Add Empty Row
        Table emptyRow = new Table(1); // The number of columns
        emptyRow.setWidth(UnitValue.createPercentValue(100));
        emptyRow.setHeight(20);
        document.add(emptyRow);

        //Start signature row

        Table signatureRow = new Table(3); // The number of columns
        signatureRow.setWidth(UnitValue.createPercentValue(100));
        signatureRow.setFont(font);

        // adding column 1
        Paragraph sDetailsCol1 = new Paragraph();
        Text reviewedBy = new Text("Reviewed by: ").setFontSize(10);
        Text reviewedByVal = new Text("...................").setFontSize(10).setFont(courier);
        sDetailsCol1.add(reviewedBy).add(reviewedByVal);

        // adding column 2
        Paragraph sDetailsCol2 = new Paragraph();
        Text signature = new Text("Signature: ").setFontSize(10);
        Text signatureVal = new Text("...................").setFontSize(10).setFont(courier);
        sDetailsCol2.add(signature).add(signatureVal);

        // adding column 3
        Paragraph sDetailsCol3 = new Paragraph();
        Text date = new Text("Date: ").setFontSize(10);
        Text dateVal = new Text("...................").setFontSize(10).setFont(courier);
        sDetailsCol3.add(date).add(dateVal);

        Cell scol1 = new Cell();
        scol1.setBorder(Border.NO_BORDER);
        
        Cell scol2 = new Cell();
        scol2.setBorder(Border.NO_BORDER);
        
        Cell scol3 = new Cell();
        scol3.setBorder(Border.NO_BORDER);
        
        scol1.add(sDetailsCol1);
        scol2.add(sDetailsCol2);
        scol3.add(sDetailsCol3);

        signatureRow.addCell(scol1);
        signatureRow.addCell(scol2);
        signatureRow.addCell(scol3);

        document.add(signatureRow);

        //End signature row

        //Add Empty Row
        document.add(emptyRow);

        // Start Date Row
        Table dateRow = new Table(1); // The number of columns
        dateRow.setWidth(UnitValue.createPercentValue(100));
        dateRow.setFont(font);

        // adding column 1
        Paragraph cDetailsCol1 = new Paragraph();
        cDetailsCol1.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        Text effectiveDate = new Text("Effective date: ").setFontSize(10);
        Date currentDate = new Date();
        Text effectiveDateVal = new Text(Utils.getSimpleDateFormat("dd/MM/yyyy").format(currentDate)).setFontSize(10).setFont(courier);
        cDetailsCol1.add(effectiveDate).add(effectiveDateVal);

        Cell ccol1 = new Cell();
        ccol1.setBorder(Border.NO_BORDER);
        ccol1.setHorizontalAlignment(HorizontalAlignment.RIGHT);

        ccol1.add(cDetailsCol1);

        dateRow.addCell(ccol1);
        dateRow.setHorizontalAlignment(HorizontalAlignment.RIGHT);

        document.add(dateRow);

        // End Date Row

        //Close document
        document.close();
        return returnFile;

    }
    private Table addHeaderRow(Table table) {

        // Patient Name
        table.addHeaderCell(new Paragraph("Patient Name").setBold());

        // NUPI / CCC Number
        Paragraph cccNumberCol = new Paragraph();
        Text cccNoText = new Text("CCC/KDOD No \n").setBold();
        cccNumberCol.add(cccNoText);
        table.addHeaderCell(cccNumberCol);

        //Age
        Paragraph recencyNumberCol = new Paragraph();
        Text recencyNoText = new Text("Age \n").setBold();
        recencyNumberCol.add(recencyNoText);
        table.addHeaderCell(recencyNumberCol);

        // Sex
        table.addHeaderCell(new Paragraph("Sex (M/F)").setBold().setTextAlignment(TextAlignment.CENTER));

        // VL Justification Codes
        Paragraph vlJustCol = new Paragraph();
        Text vlText = new Text("Viral Load \n").setBold();
        Text vlDetails = new Text("Justification \n codes").setItalic().setFontSize(8).setBold();
        vlJustCol.add(vlText);
        vlJustCol.add(vlDetails);
        table.addHeaderCell(vlJustCol);

        //Batch Number
        table.addHeaderCell(new Paragraph("Batch No.").setBold());

        // Date of sample collection
        table.addHeaderCell(new Paragraph("Date & \n time of \ncollection").setBold().setTextAlignment(TextAlignment.CENTER));

        // Date of dispatch
        table.addHeaderCell(new Paragraph("Date \n of \n dispatch").setBold().setTextAlignment(TextAlignment.CENTER));

        // Date results received
        table.addHeaderCell(new Paragraph("Date \n results \n received").setBold().setTextAlignment(TextAlignment.CENTER));

        // Turn Around Time - TAT (DAYS)
        table.addHeaderCell(new Paragraph("TAT (DAYS)").setBold());

        // Results
        table.addHeaderCell(new Paragraph("Results").setBold());

        // Suppressed
        Paragraph suppCol = new Paragraph();
        Text suppText = new Text("Suppressed \n").setBold();
        Text suppDetails = new Text("(<200cps/ml) \n").setItalic().setFontSize(8);
        Text suppDetails1 = new Text("(Y/N)").setItalic().setFontSize(8);
        suppCol.add(suppText);
        suppCol.add(suppDetails);
        suppCol.add(suppDetails1);
        table.addHeaderCell(suppCol);

        // Received by
        table.addHeaderCell(new Paragraph("Received \nby").setBold().setTextAlignment(TextAlignment.CENTER));
        
        // Comments
        table.addHeaderCell(new Paragraph("Comments").setBold().setTextAlignment(TextAlignment.CENTER));
        
        return table;
    }

    private Table addManifestSamples(LabManifest manifest, Table table) {

        java.util.List<LabManifestOrder> samples = Context.getService(KenyaemrOrdersService.class).getLabManifestOrderByManifest(manifest);

        for (LabManifestOrder sample : samples) {
            addManifestRow(sample, table);
        }

        return table;
    }

    private void addManifestRow(LabManifestOrder sample, Table table) {

        // Is DOD
        AdministrationService administrationService = Context.getAdministrationService();
        final String isKDoD = (administrationService.getGlobalProperty("kenyaemr.isKDoD"));

        // Patient Name
        Patient patient = sample.getOrder().getPatient();
        String fullName = patient.getGivenName().concat(" ").concat(
                patient.getFamilyName() != null ? sample.getOrder().getPatient().getFamilyName() : ""
        ).concat(" ").concat(
                patient.getMiddleName() != null ? sample.getOrder().getPatient().getMiddleName() : ""
        );
        table.addCell(new Paragraph(WordUtils.capitalizeFully(fullName))).setFontSize(10);

        // CCC / KDOD number
        if(isKDoD.trim().equalsIgnoreCase("true")) {
            String uniqueNumber = patient.getPatientIdentifier(Utils.getKDODIdentifierType()) != null ? patient.getPatientIdentifier(Utils.getKDODIdentifierType()).getIdentifier() : "";
            table.addCell(new Paragraph(uniqueNumber)).setFontSize(10);
        } else {
            String uniqueNumber = patient.getPatientIdentifier(Utils.getUniquePatientNumberIdentifierType()) != null ? patient.getPatientIdentifier(Utils.getKDODIdentifierType()).getIdentifier() : "";
            table.addCell(new Paragraph(uniqueNumber)).setFontSize(10);
        }

        //Age
        Integer age = patient.getAge();
        table.addCell(new Paragraph(String.valueOf(age))).setFontSize(10);

        // Sex (M/F)
        table.addCell(new Paragraph(sample.getOrder().getPatient().getGender())).setFontSize(10);

        // VL Justification Codes
        table.addCell(new Paragraph(sample.getOrder().getOrderReason() != null ? LabOrderDataExchange.getOrderReasonCode(sample.getOrder().getOrderReason().getUuid()) : "")).setFontSize(10);

        //Batch Number
        String batchNumber = sample.getBatchNumber();
        table.addCell(new Paragraph(batchNumber != null ? batchNumber : "")).setFontSize(10);

        // Date of sample collection
        table.addCell(new Paragraph(sample.getSampleCollectionDate() != null ? Utils.getSimpleDateFormat("dd/MM/yyyy").format(sample.getSampleCollectionDate()) : "")).setFontSize(10);

        // Date of dispatch
        Date dispatchDate = sample.getLabManifest().getDispatchDate();
        table.addCell(new Paragraph(dispatchDate != null ? Utils.getSimpleDateFormat("dd/MM/yyyy").format(dispatchDate) : "")).setFontSize(10);

        // Date results received
        Date resultDate = sample.getResultDate();
        table.addCell(new Paragraph(resultDate != null ? Utils.getSimpleDateFormat("dd/MM/yyyy").format(resultDate) : "")).setFontSize(10);

        // Turn Around Time - TAT (DAYS)
        Integer tat = 0;
        if(dispatchDate != null && resultDate != null) {
            tat = Utils.daysBetween(dispatchDate, resultDate);
        }
        table.addCell(new Paragraph(String.valueOf(tat))).setFontSize(10);

        // Results
        String results = sample.getResult();
        table.addCell(new Paragraph(results != null ? results : "")).setFontSize(10);

        // Suppressed
        String suppressed = ""; // Empty for unknown
        if(results != null) {
            results = results.toLowerCase().trim();
            // if results contain LDL
            if(results.equalsIgnoreCase("LDL") || results.contains("ldl")) {
                suppressed = "Y";
            } else if(results.endsWith("copies/ml")) {
                int index = results.indexOf("copies/ml");
                if (index != -1) {
                    String val = results.substring(0, index); // Get the 40 from (40 copies/ml)
                    val = val.trim();
                    Integer vlVal = NumberUtils.toInt(val);
                    if(vlVal >= 200) {
                        suppressed = "N";
                    } else {
                        suppressed = "Y";
                    }
                }
            } else {
                Integer vlVal = NumberUtils.toInt(results);
                if(vlVal >= 200) {
                    suppressed = "N";
                } else {
                    suppressed = "Y";
                }
            }
        }
        table.addCell(new Paragraph(suppressed)).setFontSize(10);

        // Received by
        table.addCell(new Paragraph("")).setFontSize(10);
        
        // Comments
        table.addCell(new Paragraph("")).setFontSize(10);
    }

    public LabManifest getManifest() {
        return manifest;
    }

    public void setManifest(LabManifest manifest) {
        this.manifest = manifest;
    }
}
