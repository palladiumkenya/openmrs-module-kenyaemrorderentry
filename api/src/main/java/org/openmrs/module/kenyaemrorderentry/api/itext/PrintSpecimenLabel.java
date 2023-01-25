package org.openmrs.module.kenyaemrorderentry.api.itext;

import com.itextpdf.barcodes.Barcode128;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.barcodes.qrcode.EncodeHintType;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.commons.lang.WordUtils;
import org.openmrs.Patient;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.kenyaemrorderentry.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

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
        Table table = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
        Text nameLabel = new Text(WordUtils.capitalizeFully(fullName));
        Text cccNoLabel = new Text(patientCCCNumber);
        Text specimenDateLabel = new Text(Utils.getSimpleDateFormat("dd/MM/yyyy").format(order.getOrder().getDateActivated()));

        Paragraph paragraph = new Paragraph();
        paragraph.add("Name: ").add(nameLabel).add("\n");
        paragraph.add("CCC No:").add(cccNoLabel).add("\n");
        paragraph.add("Sample Date: ").add(specimenDateLabel);
        table.addCell(paragraph);

        Barcode128 code128 = new Barcode128(pdfDoc);

        // If the value is positive, the text distance under the bars. If zero or negative,
        // the text distance above the bars.
        String code = patientCCCNumber;
        code128.setBaseline(-1);
        code128.setSize(12);
        code128.setCode(code);
        code128.setCodeType(Barcode128.CODE128);
        Image code128Image = new Image(code128.createFormXObject(pdfDoc));

        // Notice that in iText5 in default PdfPCell constructor (new PdfPCell(Image img))
        // this image does not fit the cell, but it does in addCell().
        // In iText7 there is no constructor (new Cell(Image img)),
        // so the image adding to the cell can be done only using method add().
        Cell cell = new Cell().add(code128Image);
        table.addCell(cell);
        //table.addCell("Add text and bar code separately:");

        code128 = new Barcode128(pdfDoc);

        // Suppress the barcode text
        code128.setFont(null);
        code128.setCode(code);
        code128.setCodeType(Barcode128.CODE128);

        // Let the image resize automatically by setting it to be autoscalable.
        code128Image = new Image(code128.createFormXObject(pdfDoc)).setAutoScale(true);
        cell = new Cell();
        cell.add(new Paragraph("PO #: " + code));
        cell.add(code128Image);
        //table.addCell(cell);

        Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
        BarcodeQRCode qrcode = new BarcodeQRCode(code, hints);
        Image qrcodeImage = new Image(qrcode.createFormXObject(ColorConstants.BLACK, pdfDoc));
        qrcodeImage.setAutoScale(true);
        Cell qrCodeCell = new Cell();
        qrCodeCell.add(qrcodeImage);
        table.addCell(new Cell());
        table.addCell(qrCodeCell);

        doc.add(table);
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
