package org.example;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.FileOutputStream;
import java.time.LocalDate;

public class Recy_Payment {

    public static void generateRecy(int paymentId,
                                    int factureID,
                                    LocalDate datePayment,
                                    String paymentMethod,
                                    double amountPaid,
                                    double remainingAmount) {

        String name = "recu_paiement_" + paymentId + ".pdf";

        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(name));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("*************** REÇU DE PAIEMENT ****************").setBold());
            document.add(new Paragraph("Numéro du paiement : " + paymentId));
            document.add(new Paragraph("Numéro de la facture : " + factureID));
            document.add(new Paragraph("Date du paiement : " + datePayment));
            document.add(new Paragraph("Méthode de paiement : " + paymentMethod));
            document.add(new Paragraph("Montant payé : " + amountPaid + " DH"));
            document.add(new Paragraph("Reste à payer : " + remainingAmount + " DH"));

            document.close();

            System.out.println("PDF généré avec succès : " + name);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}