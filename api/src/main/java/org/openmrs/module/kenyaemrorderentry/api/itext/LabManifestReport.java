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
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.List;
import com.itextpdf.layout.element.ListItem;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.kenyaemrorderentry.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class LabManifestReport {

    public static final String LOGO = "src/main/resources/img/moh.png";
    public static final PdfNumber LANDSCAPE = new PdfNumber(90);
    LabManifest manifest;


    public LabManifestReport() {
    }

    public LabManifestReport(LabManifest manifest) {
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
        // Add a Paragraph

        document.add(new Paragraph("\n"));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("MINISTRY OF HEALTH").setTextAlignment(TextAlignment.CENTER).setFontSize(12));
        document.add(new Paragraph("Viral Load Request Form").setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(16));
        document.add(new Paragraph("\n"));

        Table table = new Table(new float[]{4, 1, 3, 4, 3, 3, 3, 3, 1});
        table.setWidth(UnitValue.createPercentValue(100));

        document.add(addHeaderRow(table));

        document.add(addManifestSamples(this.manifest, table));
        document.add(new Paragraph("iText is:").setFont(font));
        // Create a List
        List list = new List()
                .setSymbolIndent(12)
                .setListSymbol("\u2022")
                .setFont(font);
        // Add ListItem objects
        list.add(new ListItem("Never gonna give you up"))
                .add(new ListItem("Never gonna let you down"))
                .add(new ListItem("Never gonna run around and desert you"))
                .add(new ListItem("Never gonna make you cry"))
                .add(new ListItem("Never gonna say goodbye"))
                .add(new ListItem("Never gonna tell a lie and hurt you"));
        // Add the list
        document.add(list);

        //Close document
        document.close();
        return returnFile;

    }
    private Table addHeaderRow(Table table) {

        table.addHeaderCell(new Paragraph("Patient Name"));
        table.addHeaderCell(new Paragraph("CCC No"));
        table.addHeaderCell(new Paragraph("DOB"));
        table.addHeaderCell(new Paragraph("Sex"));
        table.addHeaderCell(new Paragraph("If female, select the following"));
        table.addHeaderCell(new Paragraph("Sample type"));
        table.addHeaderCell(new Paragraph("Date & time of collection"));
        table.addHeaderCell(new Paragraph("Date & time of separation/centrifugation"));
        table.addHeaderCell(new Paragraph("Date started on ART"));
        return table;
    }

    private Table addManifestSamples(LabManifest manifest, Table table) {

        java.util.List<LabManifestOrder> samples = Context.getService(KenyaemrOrdersService.class).getLabManifestOrderByManifest(manifest);

        for (LabManifestOrder sample : samples) {
            addManifestRow(sample, table);
        }

        table.complete();
        return table;
    }

    private void addManifestRow(LabManifestOrder sample, Table table) {
        table.addCell(new Paragraph(sample.getOrder().getPatient().getNames().toString()));
        table.addCell(new Paragraph(sample.getOrder().getPatient().getPatientIdentifier(Utils.getUniquePatientNumberIdentifierType()).getIdentifier()));
        table.addCell(new Paragraph(Utils.getSimpleDateFormat("dd/MM/yyyy").format(sample.getOrder().getPatient().getBirthdate())));
        table.addCell(new Paragraph(sample.getOrder().getPatient().getGender()));
        table.addCell(new Paragraph("3"));
        table.addCell(new Paragraph("1"));
        table.addCell(new Paragraph(Utils.getSimpleDateFormat("dd/MM/yyyy").format(sample.getDateCreated())));
        table.addCell(new Paragraph("20/06/2020"));
        table.addCell(new Paragraph("21/06/2020"));
    }

    public LabManifest getManifest() {
        return manifest;
    }

    public void setManifest(LabManifest manifest) {
        this.manifest = manifest;
    }
}
