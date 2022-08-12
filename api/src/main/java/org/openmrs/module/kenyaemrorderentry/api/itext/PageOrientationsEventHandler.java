package org.openmrs.module.kenyaemrorderentry.api.itext;

import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;

public class PageOrientationsEventHandler implements IEventHandler {
    public static final PdfNumber PORTRAIT = new PdfNumber(0);

    private PdfNumber orientation = PORTRAIT;

    public void setOrientation(PdfNumber orientation) {
        this.orientation = orientation;
    }

    @Override
    public void handleEvent(Event currentEvent) {
        PdfDocumentEvent docEvent = (PdfDocumentEvent) currentEvent;
        docEvent.getPage().put(PdfName.Rotate, ViralLoadLabManifestReport.LANDSCAPE);
    }
}
