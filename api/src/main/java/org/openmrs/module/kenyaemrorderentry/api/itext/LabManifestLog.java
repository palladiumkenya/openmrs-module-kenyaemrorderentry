package org.openmrs.module.kenyaemrorderentry.api.itext;


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
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyacore.RegimenMappingUtils;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabOrderDataExchange;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.kenyaemrorderentry.util.Utils;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.apache.commons.lang3.time.DateUtils;
import org.openmrs.module.kenyaemrorderentry.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

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
        document.add(new Paragraph("Manifest / Shipping ID: " + (manifest.getIdentifier() != null ? manifest.getIdentifier() : "")).setTextAlignment(TextAlignment.LEFT).setBold().setFontSize(10).setFont(courier));

        // Start Summary Row
        Table sampleLogMetadata = new Table(4); // The number of columns in the manifest form (with manifest HEADER)
        sampleLogMetadata.setWidth(UnitValue.createPercentValue(100));
        sampleLogMetadata.setFont(font);

        // adding column 1
        Paragraph fDetailsCol1 = new Paragraph();
        // Text column1Label = new Text("Facility Details").setBold().setFontSize(10).setUnderline();
        Text totalSamples = new Text("Total Samples: ").setFontSize(10);
        Text totalSamplesVal = new Text(String.valueOf(Utils.getTotalSamplesInAManifest(manifest))).setFontSize(10).setFont(courier);
        // Text mflCodeLabel = new Text("MFL Code: ").setFontSize(10);
        // Text mflCodeVal = new Text(Utils.getDefaultLocationMflCode(Utils.getDefaultLocation())).setFontSize(10).setFont(courier);

        // Text facilityEmailLabel = new Text("Facility/CCC email: ").setFontSize(10);
        // Text facilityEmailVal = new Text((manifest.getFacilityEmail() != null ? manifest.getFacilityEmail() : "")).setFontSize(10).setFont(courier);
        // Text facilityPhoneNoLabel = new Text("Facility/CCC phone no: ").setFontSize(10);
        // Text facilityPhoneNoVal = new Text((manifest.getFacilityPhoneContact() != null ? manifest.getFacilityPhoneContact() : "")).setFontSize(10).setFont(courier);
        // fDetailsCol1.setFixedLeading(13); // sets line spacing
        // fDetailsCol1.add(column1Label).add("\n").add(facilityNameLabel).add(facilityNameVal).add("\n").add(mflCodeLabel).add(mflCodeVal).add("\n").add(facilityEmailLabel).add(facilityEmailVal).add("\n").add(facilityPhoneNoLabel).add(facilityPhoneNoVal);
        fDetailsCol1.add(totalSamples).add(totalSamplesVal);

        // adding column 2
        Paragraph fDetailsCol2 = new Paragraph();
        // Text column2Label = new Text("");
        Text totalSuppressed = new Text("Total Suppressed: ").setFontSize(10);
        Text totalSuppressedVal = new Text(String.valueOf(Utils.getSamplesSuppressedInAManifest(manifest))).setFontSize(10).setFont(courier);
        // Text countyVal = new Text((manifest.getCounty() != null ? manifest.getCounty() : "")).setFontSize(10).setFont(courier);
        // Text subCountyLabel = new Text("Sub-county: ").setFontSize(10);
        // Text subCountyVal = new Text((manifest.getSubCounty() != null ? manifest.getSubCounty() : "")).setFontSize(10).setFont(courier);
        // Text clinicianPhoneNoLabel = new Text("Clinician's phone no: ").setFontSize(10);
        // Text clinicianPhoneNoVal = new Text((manifest.getClinicianPhoneContact() != null ? manifest.getClinicianPhoneContact() : "")).setFontSize(10).setFont(courier);
        // Text clinicianNameLabel = new Text("Clinician's Name: ").setFontSize(10);
        // Text clinicianNameVal = new Text((manifest.getClinicianName() != null ? manifest.getClinicianName() : "")).setFontSize(10).setFont(courier);
        // fDetailsCol2.setFixedLeading(13);
        // fDetailsCol2.add(column2Label).add("\n").add(countyLabel).add(countyVal).add("\n").add(subCountyLabel).add(subCountyVal).add("\n").add(clinicianPhoneNoLabel).add(clinicianPhoneNoVal).add("\n").add(clinicianNameLabel).add(clinicianNameVal);
        fDetailsCol2.add(totalSuppressed).add(totalSuppressedVal);

        // adding column 3
        Paragraph fDetailsCol3 = new Paragraph();
        // Text column3Label = new Text("Facility Laboratory details").setBold().setFontSize(10).setUnderline();
        Text totalUnsuppressed = new Text("Total unsuppressed: ").setFontSize(10);
        Text totalUnsuppressedVal = new Text(String.valueOf(Utils.getSamplesUnsuppressedInAManifest(manifest))).setFontSize(10).setFont(courier);
        // Text facilityDispatchVal = new Text((manifest.getDispatchDate() != null ? Utils.getSimpleDateFormat("dd/MM/yyyy").format(manifest.getDispatchDate()) : "")).setFontSize(10).setFont(courier);
        // Text facilityFocalPointLabel = new Text("Lab focal person phone contact: ").setFontSize(10);
        // Text facilityFocalPointVal = new Text((manifest.getLabPocPhoneNumber() != null ? manifest.getLabPocPhoneNumber() : "")).setFontSize(10).setFont(courier);
        // Text hubDetailsLabel = new Text("Hub details").setFontSize(10).setBold().setFontSize(10).setUnderline();
        // Text hubDispatchLabel = new Text("Date & time sample dispatched: ........................").setFontSize(10);
        // Text hubFocalPointLabel = new Text("Lab focal person phone contact: ........................").setFontSize(10);
        // fDetailsCol3.setFixedLeading(13);
        // fDetailsCol3.add(column3Label).add("\n").add(facilityDispatchLabel).add(facilityDispatchVal).add("\n").add(facilityFocalPointLabel).add(facilityFocalPointVal).add("\n").add(hubDetailsLabel).add("\n").add(hubDispatchLabel).add("\n").add(hubFocalPointLabel);
        fDetailsCol3.add(totalUnsuppressed).add(totalUnsuppressedVal);

        // adding column 4
        Paragraph fDetailsCol4 = new Paragraph();
        // Text column3Label = new Text("Facility Laboratory details").setBold().setFontSize(10).setUnderline();
        Text totalRejected = new Text("Total Rejected: ").setFontSize(10);
        Text totalRejectedVal = new Text(String.valueOf(Utils.getSamplesRejectedInAManifest(manifest))).setFontSize(10).setFont(courier);
        // Text facilityDispatchVal = new Text((manifest.getDispatchDate() != null ? Utils.getSimpleDateFormat("dd/MM/yyyy").format(manifest.getDispatchDate()) : "")).setFontSize(10).setFont(courier);
        // Text facilityFocalPointLabel = new Text("Lab focal person phone contact: ").setFontSize(10);
        // Text facilityFocalPointVal = new Text((manifest.getLabPocPhoneNumber() != null ? manifest.getLabPocPhoneNumber() : "")).setFontSize(10).setFont(courier);
        // Text hubDetailsLabel = new Text("Hub details").setFontSize(10).setBold().setFontSize(10).setUnderline();
        // Text hubDispatchLabel = new Text("Date & time sample dispatched: ........................").setFontSize(10);
        // Text hubFocalPointLabel = new Text("Lab focal person phone contact: ........................").setFontSize(10);
        // fDetailsCol3.setFixedLeading(13);
        // fDetailsCol3.add(column3Label).add("\n").add(facilityDispatchLabel).add(facilityDispatchVal).add("\n").add(facilityFocalPointLabel).add(facilityFocalPointVal).add("\n").add(hubDetailsLabel).add("\n").add(hubDispatchLabel).add("\n").add(hubFocalPointLabel);
        fDetailsCol4.add(totalRejected).add(totalRejectedVal);


        Cell col1 = new Cell();
        col1.setBorder(Border.NO_BORDER);
        col1.setBorderTop(new SolidBorder(1f));
        col1.setBorderBottom(new SolidBorder(1f));
        col1.setBorderLeft(new SolidBorder(1f));
        col1.setBorderRight(new SolidBorder(1f));

        Cell col2 = new Cell();
        col2.setBorder(Border.NO_BORDER);
        col2.setBorderTop(new SolidBorder(1f));
        col2.setBorderBottom(new SolidBorder(1f));
        col2.setBorderLeft(new SolidBorder(1f));
        col2.setBorderRight(new SolidBorder(1f));

        Cell col3 = new Cell();
        col3.setBorder(Border.NO_BORDER);
        col3.setBorderTop(new SolidBorder(1f));
        col3.setBorderBottom(new SolidBorder(1f));
        col3.setBorderLeft(new SolidBorder(1f));
        col3.setBorderRight(new SolidBorder(1f));

        Cell col4 = new Cell();
        col4.setBorder(Border.NO_BORDER);
        col4.setBorderTop(new SolidBorder(1f));
        col4.setBorderBottom(new SolidBorder(1f));
        col4.setBorderLeft(new SolidBorder(1f));
        col4.setBorderRight(new SolidBorder(1f));

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

        Table table = new Table(13); // The number of columns in the sample log form (with manifest DATA)
        table.setWidth(UnitValue.createPercentValue(100));
        table.setFont(font);

        Table tWithHeaderRow = addHeaderRow(table);
        Table tWithData = addManifestSamples(this.manifest, tWithHeaderRow);


        document.add(tWithData);

        //Close document
        document.close();
        return returnFile;

    }
    private Table addHeaderRow(Table table) {

        // Patient Name
        table.addHeaderCell(new Paragraph("Patient Name").setBold());

        // NUPI / CCC Number
        Paragraph cccNumberCol = new Paragraph();
        Text cccNoText = new Text("NUPI / CCC No \n").setBold();
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

        // Patient Name
        Patient patient = sample.getOrder().getPatient();
        String fullName = patient.getGivenName().concat(" ").concat(
                patient.getFamilyName() != null ? sample.getOrder().getPatient().getFamilyName() : ""
        ).concat(" ").concat(
                patient.getMiddleName() != null ? sample.getOrder().getPatient().getMiddleName() : ""
        );
        table.addCell(new Paragraph(WordUtils.capitalizeFully(fullName))).setFontSize(10);

        // CCC / NUPI number
        table.addCell(new Paragraph(patient.getPatientIdentifier(Utils.getUniquePatientNumberIdentifierType()).getIdentifier())).setFontSize(10);

        //Age
        Integer age = patient.getAge();
        table.addCell(new Paragraph(String.valueOf(age))).setFontSize(10);

        // Sex (M/F)
        table.addCell(new Paragraph(sample.getOrder().getPatient().getGender())).setFontSize(10);

        // VL Justification Codes
        table.addCell(new Paragraph(sample.getOrder().getOrderReason() != null ? LabOrderDataExchange.getOrderReasonCode(sample.getOrder().getOrderReason().getUuid()) : "")).setFontSize(10);

        // Date of sample collection
        table.addCell(new Paragraph(sample.getSampleCollectionDate() != null ? Utils.getSimpleDateFormat("dd/MM/yyyy").format(sample.getSampleCollectionDate()) : "")).setFontSize(10);

        // Date of dispatch
        Date dispatchDate = sample.getLabManifest().getDispatchDate();
        table.addCell(new Paragraph(dispatchDate != null ? Utils.getSimpleDateFormat("dd/MM/yyyy").format(dispatchDate) : "")).setFontSize(10);

        // Date results received
        Date resultDate = sample.getResultDate();
        table.addCell(new Paragraph(resultDate != null ? Utils.getSimpleDateFormat("dd/MM/yyyy").format(resultDate) : "")).setFontSize(10);

        // Turn Around Time - TAT (DAYS)
        Integer tat = Utils.daysBetween(dispatchDate, resultDate);
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
