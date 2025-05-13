package org.openmrs.module.kenyaemrorderentry.api.itext;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang.WordUtils;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.kenyaemrorderentry.util.Utils;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.ui.framework.SimpleObject;

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

public class HeiLabManifestReport {

    public static final String LOGO = "src/main/resources/img/moh.png";
    public static final PdfNumber LANDSCAPE = new PdfNumber(90);
    LabManifest manifest;


    public HeiLabManifestReport() {
    }

    public HeiLabManifestReport(LabManifest manifest) {
        this.manifest = manifest;
    }

    /**
	 * Get the latest OBS (Observation)
	 * @param patient
	 * @param conceptIdentifier
	 * @return
	 */
	public Obs getLatestObs(Patient patient, String conceptIdentifier) {
		Concept concept = Context.getConceptService().getConceptByUuid(conceptIdentifier);
		List<Obs> obs = Context.getObsService().getObservationsByPersonAndConcept(patient, concept);
		if (obs.size() > 0) {
			// these are in reverse chronological order
			return obs.get(0);
		}
		return null;
	}

    /**
     * Generate the HEI manifest report
     * @param dest
     * @return
     * @throws IOException
     */
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
        document.add(new Paragraph("EARLY INFANT DIAGNOSIS (DNA-PCR) Laboratory Requisition Form").setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(16));
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

        Table table = new Table(10); // 10 columns for the report details
        table.setWidth(UnitValue.createPercentValue(100));
        table.setFont(font);

        Table tWithHeaderRow = addHeaderRow(table);
        Table tWithData = addManifestSamples(this.manifest, tWithHeaderRow);


        document.add(tWithData);

        //Close document
        document.close();
        return returnFile;

    }

    /**
     * Adding the header row
     * @param table
     * @return
     */
    private Table addHeaderRow(Table table) {
        // Adding 10 columns
        // Col 1 - Date of sample collection
        table.addHeaderCell(new Paragraph("Date of Sample \ncollection").setBold().setTextAlignment(TextAlignment.CENTER));

        // Col 2 - Infant Name
        table.addHeaderCell(new Paragraph("Infant Name").setBold());

        // Col 3 - HEI Id number
        Paragraph cccNumberCol = new Paragraph();
        Text cccNoText = new Text("HEI ID Number \n").setBold();
        Text cccNoDetail1 = new Text("(MFL-YYYY-NNNN)").setItalic().setFontSize(8);
        cccNumberCol.add(cccNoText);
        cccNumberCol.add(cccNoDetail1);
        table.addHeaderCell(cccNumberCol);
  
        // Col 4 - PCR sample code
        table.addHeaderCell(new Paragraph("PCR sample \n(code)").setBold().setTextAlignment(TextAlignment.CENTER));

        // Col 5 - Date of birth
        Paragraph dobCol = new Paragraph();
        Text dobText = new Text("Date of Birth \n").setBold();
        Text dobDetails = new Text("(dd/mm/yyy)").setItalic().setFontSize(8).setBold();
        dobCol.add(dobText);
        dobCol.add(dobDetails);
        table.addHeaderCell(dobCol);

        // Col 6 - Sex
        table.addHeaderCell(new Paragraph("Sex (M/F)").setBold());
        
        // Col 7 - Infant Prophylaxis
        table.addHeaderCell(new Paragraph("Infant Prophylaxis \n(code)").setBold().setTextAlignment(TextAlignment.CENTER));
        
        // Col 8 - Infant NUPI/CCC No
        table.addHeaderCell(new Paragraph("Infant NUPI / CCC No").setBold().setTextAlignment(TextAlignment.CENTER));
        
        // Col 9 - Mothers NUPI\nCCC Number
        table.addHeaderCell(new Paragraph("Mothers NUPI\nCCC Number").setBold().setTextAlignment(TextAlignment.CENTER));

        // Col 10 - Code for rejection
        table.addHeaderCell(new Paragraph("Code for rejection").setBold().setTextAlignment(TextAlignment.CENTER));
        
        return table;
    }

    /**
     * Loop through the orders in the manifest
     * @param manifest
     * @param table
     * @return
     */
    private Table addManifestSamples(LabManifest manifest, Table table) {

        java.util.List<LabManifestOrder> samples = Context.getService(KenyaemrOrdersService.class).getLabManifestOrderByManifest(manifest);

        for (LabManifestOrder sample : samples) {
            addManifestRow(sample, table);
        }

        return table;
    }

    /**
     * Add table row data
     * @param sample
     * @param table
     */
    private void addManifestRow(LabManifestOrder sample, Table table) {
        // Adding 10 columns
        
        Patient patient = sample.getOrder().getPatient();
        Order order = sample.getOrder();
        SimpleObject heiDetailsObject = Utils.getHeiDetailsForEidPostObject(patient, order);
        String mothersCCC = Utils.getMothersUniquePatientNumber(patient);

        // Is DOD
        AdministrationService administrationService = Context.getAdministrationService();
        final String isKDoD = (administrationService.getGlobalProperty("kenyaemr.isKDoD"));

        // Col 1 - Date of sample collection
        table.addCell(new Paragraph(sample.getSampleCollectionDate() != null ? Utils.getSimpleDateFormat("dd/MM/yyyy").format(sample.getSampleCollectionDate()) : "")).setFontSize(10);

        // Col 2 - Infant Name
        String fullName = patient.getGivenName().concat(" ").concat(
                patient.getFamilyName() != null ? sample.getOrder().getPatient().getFamilyName() : ""
        ).concat(" ").concat(
                patient.getMiddleName() != null ? sample.getOrder().getPatient().getMiddleName() : ""
        );
        table.addCell(new Paragraph(WordUtils.capitalizeFully(fullName))).setFontSize(10);

        // Col 3 - HEI Id number
        table.addCell(new Paragraph(patient.getPatientIdentifier(Utils.getHeiNumberIdentifierType()).getIdentifier())).setFontSize(10);

        // Col 4 - PCR sample code
        String pcrSampleCode = "";
        pcrSampleCode = heiDetailsObject.get("pcrSampleCodeAnswer") != null ? heiDetailsObject.get("pcrSampleCodeAnswer").toString() : "";
        table.addCell(new Paragraph(pcrSampleCode)).setFontSize(10);

        // Col 5 - Date of birth
        table.addCell(new Paragraph(Utils.getSimpleDateFormat("dd/MM/yyyy").format(sample.getOrder().getPatient().getBirthdate()))).setFontSize(10);
        
        // Col 6 - Sex
        table.addCell(new Paragraph(sample.getOrder().getPatient().getGender())).setFontSize(10);
        
        // Col 7 - Infant Prophylaxis
        String infantProphylaxisCode = "";
        infantProphylaxisCode = heiDetailsObject.get("prophylaxisAnswer") != null ? heiDetailsObject.get("prophylaxisAnswer").toString() : "";
        table.addCell(new Paragraph(infantProphylaxisCode)).setFontSize(10);

        // Col 8 - Infant NUPI/CCC No
        String infantUniqueNo = "";

        PatientIdentifierType pit;
        if(isKDoD.trim().equalsIgnoreCase("true")) {
            pit = MetadataUtils.existing(PatientIdentifierType.class, Utils.getKDODIdentifierType().getUuid());
        } else {
            pit = MetadataUtils.existing(PatientIdentifierType.class, Utils.getUniquePatientNumberIdentifierType().getUuid());
        }
        PatientIdentifier heiObject = patient.getPatientIdentifier(pit);
        if (heiObject != null) {
            infantUniqueNo= heiObject.getIdentifier();
        }
        table.addCell(new Paragraph(infantUniqueNo)).setFontSize(10);

        // Col 9 - Mothers NUPI\nCCC Number
        mothersCCC = mothersCCC !=null ? mothersCCC : "";
        table.addCell(new Paragraph(mothersCCC)).setFontSize(10);

        // Col 10 - Code for rejection
        table.addCell(new Paragraph("____")).setFontSize(10);
    }

    /**
     * Get the main manifest
     * @return the main lab manifest
     */
    public LabManifest getManifest() {
        return manifest;
    }

    /**
     * Set the main lab manifest
     * @param manifest
     */
    public void setManifest(LabManifest manifest) {
        this.manifest = manifest;
    }
}
