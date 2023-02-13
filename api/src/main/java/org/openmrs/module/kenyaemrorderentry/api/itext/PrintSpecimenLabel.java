package org.openmrs.module.kenyaemrorderentry.api.itext;

import com.itextpdf.barcodes.Barcode128;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import org.apache.commons.lang.WordUtils;
import org.openmrs.Patient;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.kenyaemrorderentry.util.Utils;

import java.io.File;
import java.io.FileOutputStream;

public class PrintSpecimenLabel {

    private LabManifestOrder order;
    public PrintSpecimenLabel() {
    }

    public PrintSpecimenLabel(LabManifestOrder order) {
        this.order = order;
    }

    public File downloadSpecimenLabel() throws Exception {

        Patient patient = order.getOrder().getPatient();
        String fullName = patient.getGivenName().concat(" ").concat(
                patient.getFamilyName() != null ? order.getOrder().getPatient().getFamilyName() : ""
        ).concat(" ").concat(
                patient.getMiddleName() != null ? order.getOrder().getPatient().getMiddleName() : ""
        );

        File returnFile = File.createTempFile("printSpecimenLabel", ".pdf");
        FileOutputStream fos = new FileOutputStream(returnFile);

        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(fos));
        Document doc = new Document(pdfDoc, PageSize.A5);

        String patientCCCNumber = patient.getPatientIdentifier(Utils.getUniquePatientNumberIdentifierType()).getIdentifier();

        Text nameLabel = new Text(WordUtils.capitalizeFully(fullName));
        Text cccNoLabel = new Text(patientCCCNumber);
        Text specimenDateLabel = new Text(Utils.getSimpleDateFormat("dd/MM/yyyy").format(order.getOrder().getDateActivated()));

        Paragraph paragraph = new Paragraph();
        paragraph.setFontSize(10);
        paragraph.add("CCC No:").add(cccNoLabel).add("\n");
        paragraph.add("Name: ").add(nameLabel).add("\n");
        paragraph.add("Sample Date: ").add(specimenDateLabel);

        Barcode128 code128 = new Barcode128(pdfDoc);
        String code = patientCCCNumber;
        code128.setBaseline(-1);
        code128.setFont(null);
        code128.setSize(12);
        code128.setCode(code);
        code128.setCodeType(Barcode128.CODE128);
        Image code128Image = new Image(code128.createFormXObject(pdfDoc));

        doc.add(code128Image);
        doc.add(paragraph);
        doc.close();
        return returnFile;
    }

    public LabManifestOrder getOrder() {
        return order;
    }

    public void setOrder(LabManifestOrder order) {
        this.order = order;
    }
}
