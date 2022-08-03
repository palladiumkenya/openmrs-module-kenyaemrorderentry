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
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class HeiLabManifestReport {

    public static final String LOGO = "src/main/resources/img/moh.png";
    public static final PdfNumber LANDSCAPE = new PdfNumber(90);
    LabManifest manifest;


    public HeiLabManifestReport() {
    }

    public HeiLabManifestReport(LabManifest manifest) {
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
        document.add(new Paragraph("EID (DNA-PCR) Laboratory Requisition Form").setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(16));
        document.add(new Paragraph("Manifest/Shipping ID: " + manifest.getIdentifier()).setTextAlignment(TextAlignment.LEFT).setBold().setFontSize(10).setFont(courier));

        Table manifestMetadata = new Table(4);
        manifestMetadata.setWidth(UnitValue.createPercentValue(100));
        manifestMetadata.setFont(font);

        Paragraph fDetailsCol1 = new Paragraph();
        Text column1Label = new Text("Facility Details").setBold().setFontSize(10).setUnderline();
        Text facilityNameLabel = new Text("Facility Name: ").setFontSize(10);
        Text facilityNameVal = new Text(Utils.getDefaultLocation().getName()).setFontSize(10).setFont(courier);
        Text mflCodeLabel = new Text("MFL Code: ").setFontSize(10);
        Text mflCodeVal = new Text(Utils.getDefaultLocationMflCode(Utils.getDefaultLocation())).setFontSize(10).setFont(courier);

        Text facilityEmailLabel = new Text("Facility/CCC email: ").setFontSize(10);
        Text facilityEmailVal = new Text((manifest.getFacilityEmail() != null ? manifest.getFacilityEmail() : "")).setFontSize(10).setFont(courier);
        Text facilityPhoneNoLabel = new Text("Facility/CCC phone no: ").setFontSize(10);
        Text facilityPhoneNoVal = new Text((manifest.getFacilityPhoneContact() != null ? manifest.getFacilityPhoneContact() : "")).setFontSize(10).setFont(courier);
        fDetailsCol1.setFixedLeading(13); // sets line spacing
        fDetailsCol1.add(column1Label).add("\n").add(facilityNameLabel).add(facilityNameVal).add("\n").add(mflCodeLabel).add(mflCodeVal).add("\n").add(facilityEmailLabel).add(facilityEmailVal).add("\n").add(facilityPhoneNoLabel).add(facilityPhoneNoVal);

        // adding column 2
        Paragraph fDetailsCol2 = new Paragraph();
        Text column2Label = new Text("");
        Text countyLabel = new Text("County: ").setFontSize(10);
        Text countyVal = new Text((manifest.getCounty() != null ? manifest.getCounty() : "")).setFontSize(10).setFont(courier);
        Text subCountyLabel = new Text("Sub-county: ").setFontSize(10);
        Text subCountyVal = new Text((manifest.getSubCounty() != null ? manifest.getSubCounty() : "")).setFontSize(10).setFont(courier);
        Text clinicianPhoneNoLabel = new Text("Clinician's phone no: ").setFontSize(10);
        Text clinicianPhoneNoVal = new Text((manifest.getClinicianPhoneContact() != null ? manifest.getClinicianPhoneContact() : "")).setFontSize(10).setFont(courier);
        Text clinicianNameLabel = new Text("Clinician's Name: ").setFontSize(10);
        Text clinicianNameVal = new Text((manifest.getClinicianName() != null ? manifest.getClinicianName() : "")).setFontSize(10).setFont(courier);
        fDetailsCol2.setFixedLeading(13);
        fDetailsCol2.add(column2Label).add("\n").add(countyLabel).add(countyVal).add("\n").add(subCountyLabel).add(subCountyVal).add("\n").add(clinicianPhoneNoLabel).add(clinicianPhoneNoVal).add("\n").add(clinicianNameLabel).add(clinicianNameVal);

        Paragraph fDetailsCol3 = new Paragraph();
        Text column3Label = new Text("Facility Laboratory details").setBold().setFontSize(10).setUnderline();
        Text facilityDispatchLabel = new Text("Date & time sample dispatched: ").setFontSize(10);
        Text facilityDispatchVal = new Text((manifest.getDispatchDate() != null ? Utils.getSimpleDateFormat("dd/MM/yyyy").format(manifest.getDispatchDate()) : "")).setFontSize(10).setFont(courier);
        Text facilityFocalPointLabel = new Text("Lab focal person phone contact: ").setFontSize(10);
        Text facilityFocalPointVal = new Text((manifest.getLabPocPhoneNumber() != null ? manifest.getLabPocPhoneNumber() : "")).setFontSize(10).setFont(courier);
        Text hubDetailsLabel = new Text("Hub details").setFontSize(10).setBold().setFontSize(10).setUnderline();
        Text hubDispatchLabel = new Text("Date & time sample dispatched: ........................").setFontSize(10);
        Text hubFocalPointLabel = new Text("Lab focal person phone contact: ........................").setFontSize(10);
        fDetailsCol3.setFixedLeading(13);
        fDetailsCol3.add(column3Label).add("\n").add(facilityDispatchLabel).add(facilityDispatchVal).add("\n").add(facilityFocalPointLabel).add(facilityFocalPointVal).add("\n").add(hubDetailsLabel).add("\n").add(hubDispatchLabel).add("\n").add(hubFocalPointLabel);

        Cell col1 = new Cell();
        col1.setBorder(Border.NO_BORDER);
        col1.setBorderTop(new SolidBorder(1f));
        col1.setBorderLeft(new SolidBorder(1f));

        Cell col2 = new Cell();
        col2.setBorder(Border.NO_BORDER);
        col2.setBorderTop(new SolidBorder(1f));

        Cell col3 = new Cell();
        col3.setBorder(Border.NO_BORDER);
        col3.setBorderTop(new SolidBorder(1f));
        col3.setBorderRight(new SolidBorder(1f));

        col1.add(fDetailsCol1);
        col2.add(fDetailsCol2);
        col3.add(fDetailsCol3);

        manifestMetadata.addCell(col1);
        manifestMetadata.addCell(col2);
        manifestMetadata.addCell(col3);

        document.add(manifestMetadata);

        Table table = new Table(12);
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

        table.addHeaderCell(new Paragraph("Infant Name").setBold());

        Paragraph cccNumberCol = new Paragraph();
        Text cccNoText = new Text("HEI ID Number \n").setBold();
        Text cccNoDetail1 = new Text("(MFL-YYYY-NNNN)").setItalic().setFontSize(8);
        cccNumberCol.add(cccNoText);
        cccNumberCol.add(cccNoDetail1);
        table.addHeaderCell(cccNumberCol);

        Paragraph dobCol = new Paragraph();
        Text dobText = new Text("Date of Birth \n").setBold();
        Text dobDetails = new Text("(dd/mm/yyy)").setItalic().setFontSize(8).setBold();
        dobCol.add(dobText);
        dobCol.add(dobDetails);
        table.addHeaderCell(dobCol);

        table.addHeaderCell(new Paragraph("Sex").setBold());

        Paragraph pregnancyCol = new Paragraph();
        Text pregnancyText = new Text("(M/F)").setBold();

        pregnancyCol.add(pregnancyText);

        table.addHeaderCell(pregnancyCol);
        table.addHeaderCell(new Paragraph("Sample \ntype").setBold().setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Paragraph("Date & \ntime of \ncollection").setBold().setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Paragraph("Date & time \nof separation \n/centrifugation").setBold().setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Paragraph("Date \nstarted \non ART").setBold().setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Paragraph("Current \nART \nRegimen").setBold().setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Paragraph("Date \ninitiated \non \ncurrent \nRegimen").setBold().setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Paragraph("Justification \ncode").setBold().setTextAlignment(TextAlignment.CENTER));
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

        Patient patient = sample.getOrder().getPatient();
        String fullName = patient.getGivenName().concat(" ").concat(
                patient.getFamilyName() != null ? sample.getOrder().getPatient().getFamilyName() : ""
        ).concat(" ").concat(
                patient.getMiddleName() != null ? sample.getOrder().getPatient().getMiddleName() : ""
        );



        table.addCell(new Paragraph(WordUtils.capitalizeFully(fullName))).setFontSize(10);
        table.addCell(new Paragraph(patient.getPatientIdentifier(Utils.getHeiNumberIdentifierType()).getIdentifier())).setFontSize(10);
        table.addCell(new Paragraph(Utils.getSimpleDateFormat("dd/MM/yyyy").format(sample.getOrder().getPatient().getBirthdate()))).setFontSize(10);
        table.addCell(new Paragraph(sample.getOrder().getPatient().getGender())).setFontSize(10);
        if (patient.getGender().equals("F")) {
            table.addCell(new Paragraph("3")).setFontSize(10);
        } else {
            table.addCell(new Paragraph(""));
        }
        table.addCell(new Paragraph(LabOrderDataExchange.getSampleTypeCode(sample.getSampleType()))).setFontSize(10);
        table.addCell(new Paragraph(sample.getSampleCollectionDate() != null ? Utils.getSimpleDateFormat("dd/MM/yyyy").format(sample.getSampleCollectionDate()) : "")).setFontSize(10);
        table.addCell(new Paragraph(sample.getSampleSeparationDate() != null ? Utils.getSimpleDateFormat("dd/MM/yyyy").format(sample.getSampleSeparationDate()) : "")).setFontSize(10);
        table.addCell(new Paragraph("")).setFontSize(10);
        table.addCell(new Paragraph("")).setFontSize(10);
        table.addCell(new Paragraph( "")).setFontSize(10);
        table.addCell(new Paragraph(sample.getOrder().getOrderReason() != null ? LabOrderDataExchange.getOrderReasonCode(sample.getOrder().getOrderReason().getUuid()) : "")).setFontSize(10);

    }

    public LabManifest getManifest() {
        return manifest;
    }

    public void setManifest(LabManifest manifest) {
        this.manifest = manifest;
    }
}
