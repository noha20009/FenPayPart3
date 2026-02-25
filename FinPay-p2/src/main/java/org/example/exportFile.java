package org.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.DataFormat;


import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.FileNotFoundException;



public class exportFile {

    public static void exporterDOC(Scanner sc) {
        String choix;
        do {
            System.out.println("\n===== MENU EXPORT =====");
            System.out.println("1: Exporter les factures d'un prestataire");
            System.out.println("2: Exporter les factures impayées");
            System.out.println("3: Générer une facture PDF");
            System.out.println("4: Générer rapport mensuel");
            System.out.println("0: Retour au menu principal");
            System.out.print("Votre choix: ");
            choix = sc.nextLine();

            switch (choix) {
                case "1" -> exporterFacturesPrestataire();
                case "2" -> exporterFacturImpier();
                case "3" -> {
                    System.out.print("Entrer l'ID facture: ");
                    int id = Integer.parseInt(sc.nextLine());
                    Facture f = new FactureService().findById(id);
                    if (f != null) {
                        try {
                            generateInvoice(f);
                            System.out.println("PDF généré pour la facture " + id);
                        } catch (Exception e) {
                            System.out.println("Erreur génération PDF");
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("Facture introuvable");
                    }
                }
                case "4" -> genererRapportMensuel();
                case "0" -> {
                    System.out.println("Retour au menu principal");
                }
                default -> System.out.println("Choix invalide");
            }
        } while (!choix.equals("0"));
    }

    public static String formatNomFacture(int id) {
        return "Facture_" + id + ".pdf";
    }

    public static String formatNomRecu(int id) {
        return "recu_" + id + ".pdf";
    }

    public static String formatNomRapport(int mois, int annee) {
        String moisStr = (mois < 10) ? "0" + mois : String.valueOf(mois);
        return "rapport" + moisStr + annee + ".xls";
    }



    public static double exporterFacturesPrestataire() {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Entrer votre ID Prestataire : ");
        int idPrestataire = Integer.parseInt(scanner.nextLine());

        System.out.print("Entrer le mois (1-12) : ");
        int mois = Integer.parseInt(scanner.nextLine());

        System.out.print("Entrer l'année (ex: 2026) : ");
        int annee = Integer.parseInt(scanner.nextLine());

        double totalFacture = 0;
        double totalPaye = 0;
        String query =
                "SELECT f.id, f.date, c.nom AS clientNom, f.montant, f.status, " +
                        "       COALESCE(SUM(p.montant), 0) AS totalPaye " +
                        "FROM facture f " +
                        "JOIN client c ON c.id = f.idClient " +
                        "LEFT JOIN paiement p ON p.idFacture = f.id " +
                        "WHERE f.idPrestataire = ? " +
                        "  AND MONTH(f.date) = ? " +
                        "  AND YEAR(f.date) = ? " +
                        "GROUP BY f.id, f.date, c.nom, f.montant, f.status " +
                        "ORDER BY f.date ASC";

        try (Connection conn = DBConnection.createConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idPrestataire);
            ps.setInt(2, mois);
            ps.setInt(3, annee);

            ResultSet rs = ps.executeQuery();


            Workbook workbook = new HSSFWorkbook();
            Sheet sheet = workbook.createSheet("Factures");


            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            CellStyle moneyStyle = workbook.createCellStyle();
            DataFormat df = workbook.createDataFormat();
            moneyStyle.setDataFormat(df.getFormat("#,##0.00"));

            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Date", "Client", "Montant", "Statut"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;


            while (rs.next()) {

                Row row = sheet.createRow(rowNum++);

                int id = rs.getInt("id");
                String date = rs.getDate("date").toString();
                String clientNom = rs.getString("clientNom");
                double montant = rs.getDouble("montant");
                String status = rs.getString("status");
                totalPaye = rs.getDouble("totalPaye");

                row.createCell(0).setCellValue(id);
                row.createCell(1).setCellValue(date);
                row.createCell(2).setCellValue(clientNom);

                Cell cMontant = row.createCell(3);
                cMontant.setCellValue(montant);
                cMontant.setCellStyle(moneyStyle);

                row.createCell(4).setCellValue(status);

                totalFacture += montant;
            }


            if (rowNum == 1) {
                Row r = sheet.createRow(rowNum);
                r.createCell(0).setCellValue("Aucune facture pour ce prestataire ce mois.");
            } else {
                double totalAttente = totalFacture - totalPaye;
                rowNum++;

                Row totalRow1 = sheet.createRow(rowNum++);
                totalRow1.createCell(2).setCellValue("Total facturé");
                Cell tf = totalRow1.createCell(3);
                tf.setCellValue(totalFacture);
                tf.setCellStyle(moneyStyle);

                Row totalRow2 = sheet.createRow(rowNum++);
                totalRow2.createCell(2).setCellValue("Total payé");
                Cell tp = totalRow2.createCell(3);
                tp.setCellValue(totalPaye);
                tp.setCellStyle(moneyStyle);

                Row totalRow3 = sheet.createRow(rowNum++);
                totalRow3.createCell(2).setCellValue("Total en attente");
                Cell ta = totalRow3.createCell(3);
                ta.setCellValue(totalAttente);
                ta.setCellStyle(moneyStyle);
            }


            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            String nomFichier = formatNomRapport(mois, annee);
            String fileName = "C:\\Users\\ENAA\\Downloads\\" + nomFichier;
            try (FileOutputStream out = new FileOutputStream(fileName)) {
                workbook.write(out);
            }
            workbook.close();

            System.out.println("Export Excel réussi !");
            System.out.println("Fichier créé : " + fileName);

        } catch (Exception e) {
            System.out.println("Erreur export");
            e.printStackTrace();
        }

        return totalPaye;
    }

    public static void exporterFacturImpier() {
        String sql = "SELECT f.id, f.date, f.montant, c.nom " +
                "FROM facture f " +
                "JOIN client c ON f.idClient = c.id " +
                "WHERE f.status = 'UNPAID' OR f.status = 'PARTIAL'";

        try (Connection conn = DBConnection.createConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Factures impayées");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID Facture");
            header.createCell(1).setCellValue("Nom Client");
            header.createCell(2).setCellValue("Date Facture");
            header.createCell(3).setCellValue("Montant");
            header.createCell(4).setCellValue("Jours de retard");

            int rowNum = 1;
            LocalDate dateExport = LocalDate.now();

            while (rs.next()) {
                Row row = sheet.createRow(rowNum++);

                Date sqlDate = rs.getDate("date");
                long joursRetard = 0;

                if (sqlDate != null) {
                    LocalDate dateFacture = sqlDate.toLocalDate();
                    joursRetard = ChronoUnit.DAYS.between(dateFacture, dateExport);
                }

                row.createCell(0).setCellValue(rs.getInt("id"));
                row.createCell(1).setCellValue(rs.getString("nom"));
                row.createCell(2).setCellValue(sqlDate != null ? sqlDate.toString() : "");
                row.createCell(3).setCellValue(rs.getDouble("montant"));
                row.createCell(4).setCellValue(joursRetard);
            }

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            String fileName = "C:\\Users\\ENAA\\Downloads\\factureimpayees.xlsx";
            FileOutputStream fileOut = new FileOutputStream(fileName);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            System.out.println("Rapport généré avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generateInvoice(Facture facture) throws FileNotFoundException {

        String folderPath = "C:\\Users\\ENAA\\Downloads";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String fileName = formatNomFacture(facture.getId());
        String filePath = folderPath + "\\" + fileName;

        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);


        Paragraph title = new Paragraph("FinPay")
                .setFontSize(24)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold();
        document.add(title);


        try {
            Image logo = new Image(ImageDataFactory.create("org\\example\\img.png"));
            logo.setWidth(UnitValue.createPercentValue(20));
            document.add(logo);
        } catch (Exception e) {

        }


        Client client = facture.getClient();
        document.add(new Paragraph("\nInformations Client").setBold().setFontSize(16));
        if (client != null) {
            document.add(new Paragraph("Nom : " + client.getNom()));
            document.add(new Paragraph("Téléphone : " + client.getTelephone()));
            document.add(new Paragraph("Email : " + client.getEmail()));
        }


        Prestatairedb prest = facture.getPrestataire();
        document.add(new Paragraph("\nInformations Prestataire").setBold().setFontSize(16));
        if (prest != null) {
            document.add(new Paragraph("Nom : " + prest.getNom()));
            document.add(new Paragraph("Type : " + prest.getType()));
            document.add(new Paragraph("ID : " + prest.getIdPrestat()));
        }


        document.add(new Paragraph("\nDétails de la Facture").setBold().setFontSize(16));
        document.add(new Paragraph("Date : " + facture.getDate()));
        document.add(new Paragraph("Montant Total : " + facture.getMontant() + " dh"));


        Paragraph status = new Paragraph("Statut : " + facture.getStatus());
        if ("PAID".equalsIgnoreCase(facture.getStatus())) {
            status.setFontColor(ColorConstants.GREEN);
        } else {
            status.setFontColor(ColorConstants.RED);
        }
        document.add(status);

        document.close();
        System.out.println("Facture PDF générée avec succès : " + filePath);
    }



    public static void genererRapportMensuel() {
        try (Connection conn = DBConnection.createConnection()) {

            String sql =
                    "SELECT p.nom, COUNT(f.id) AS nombreFactures, SUM(f.montant) AS totalGenere " +
                            "FROM facture f " +
                            "JOIN prestataire p ON f.idPrestataire = p.id " +
                            "GROUP BY p.nom";

            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            Workbook workbook = new HSSFWorkbook();
            Sheet sheet = workbook.createSheet("Rapport Global");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Prestataire");
            header.createCell(1).setCellValue("Nombre Factures");
            header.createCell(2).setCellValue("Total Généré");

            int rowIndex = 1;

            while (rs.next()) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(rs.getString("nom"));
                row.createCell(1).setCellValue(rs.getInt("nombreFactures"));
                row.createCell(2).setCellValue(rs.getDouble("totalGenere"));
            }

           String path = "C:\\Users\\ENAA\\Downloads\\rapportglobal_mois.xls";
            File file = new File(path);
            if (file.exists()){
                file.delete();
            }
            FileOutputStream fileOut = new FileOutputStream(path);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            System.out.println("Export terminé");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}