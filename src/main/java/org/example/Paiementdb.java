package org.example;

import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class Paiementdb {
    private int id;
    private double montant;
    private String date;

    public static void paimentDBservice(Scanner scanner) {
        System.out.println("Entrer votre choix \n 1: enregistrer Payment  \n 2: mettre à jour Payment \n 3: lister Payment");
        int choix;
        do {
            choix = scanner.nextInt();
            scanner.nextLine();
            if (choix == 1) {
                Paiementdb.enregistrerPayment(scanner);
            } else if (choix == 2) {
                Paiementdb.mettreAJourPyment(scanner);
            } else if (choix == 3) {
                Paiementdb.listerPyment();
            }
        } while (choix != 0);
    }

//    public static void enregistrerPayment(Scanner scanner) {
//        try {
//            System.out.print("Facture ID: ");
//            int idFacture = Integer.parseInt(scanner.nextLine());
//
//            try (Connection con = DBConnection.createConnection()) {
//                con.setAutoCommit(false);
//
//                // Vérification montant facture
//                double montantFacture;
//                String sqlFacture = "SELECT montant FROM facture WHERE id = ?";
//                PreparedStatement psFacture = con.prepareStatement(sqlFacture);
//                psFacture.setInt(1, idFacture);
//                ResultSet rsFacture = psFacture.executeQuery();
//
//                if (!rsFacture.next()) {
//                    System.out.println("Facture introuvable.");
//                    con.rollback();
//                    return;
//                }
//                montantFacture = rsFacture.getDouble("montant");
//
//                // Total déjà payé
//                String sqlTotal = "SELECT COALESCE(SUM(montant),0) AS total FROM paiement WHERE idFacture = ?";
//                PreparedStatement psTotal = con.prepareStatement(sqlTotal);
//                psTotal.setInt(1, idFacture);
//                ResultSet rsTotal = psTotal.executeQuery();
//                rsTotal.next();
//                double totalPaye = rsTotal.getDouble("total");
//
//                double restant = montantFacture - totalPaye;
//
//                System.out.println("Montant facture : " + montantFacture);
//                System.out.println("Total déjà payé : " + totalPaye);
//                System.out.println("Montant restant : " + restant);
//
//                if (restant <= 0) {
//                    System.out.println("Cette facture est déjà payée.");
//                    con.rollback();
//                    return;
//                }
//
//                System.out.print("Montant à payer: ");
//                double montantPaye = Double.parseDouble(scanner.nextLine());
//
//                if (montantPaye > restant || montantPaye <= 0) {
//                    System.out.println("Paiement refusé.");
//                    con.rollback();
//                    return;
//                }
//
//                System.out.print("Date (YYYY-MM-DD): ");
//                LocalDate date = LocalDate.parse(scanner.nextLine());
//
//                System.out.print("Méthode de paiement (ex: Espece, Carte, Cheque): ");
//                String paymentMethod = scanner.nextLine().trim().toLowerCase();
//
//                // Insert paiement
//                String sqlInsert = "INSERT INTO paiement (montant, date, idFacture, methode) VALUES (?, ?, ?, ?)";
//                PreparedStatement psInsert = con.prepareStatement(sqlInsert, PreparedStatement.RETURN_GENERATED_KEYS);
//                psInsert.setDouble(1, montantPaye);
//                psInsert.setDate(2, Date.valueOf(date));
//                psInsert.setInt(3, idFacture);
//                psInsert.setString(4, paymentMethod);
//                psInsert.executeUpdate();
//
//                ResultSet generatedKeys = psInsert.getGeneratedKeys();
//                int paymentId = 0;
//                if (generatedKeys.next()) {
//                    paymentId = generatedKeys.getInt(1);
//                }
//
//                double nouveauTotal = totalPaye + montantPaye;
//                double remainingAfter = montantFacture - nouveauTotal;
//
//                String nouveauStatus = (nouveauTotal == montantFacture) ? "PAID" : "PARTIAL";
//
//                // Update facture status
//                String sqlUpdate = "UPDATE facture SET status = ? WHERE id = ?";
//                PreparedStatement psUpdate = con.prepareStatement(sqlUpdate);
//                psUpdate.setString(1, nouveauStatus);
//                psUpdate.setInt(2, idFacture);
//                psUpdate.executeUpdate();
//
//                con.commit();
//
//
//                System.out.println("Paiement enregistré");
//                System.out.println("Nouveau total payé : " + nouveauTotal);
//                System.out.println("Nouveau status facture : " + nouveauStatus);
//
//                // GENERATE receipt
//                Recy_Payment.generateRecy(paymentId, idFacture, date, paymentMethod, montantPaye, remainingAfter);
//
//            }
//        } catch (Exception e) {
//            System.out.println("Erreur lors de l'enregistrement du paiement");
//            e.printStackTrace();
//        }
//    }
public static void enregistrerPayment(Scanner scanner) {

    System.out.print("Facture ID: ");
    int idFacture = Integer.parseInt(scanner.nextLine());

    Connection con = null;

    try {
        con = DBConnection.createConnection();
        con.setAutoCommit(false); // 🔥 début transaction

        // Vérifier la facture
        String sqlFacture = "SELECT montant, status FROM facture WHERE id = ? FOR UPDATE";
        try (PreparedStatement psFacture = con.prepareStatement(sqlFacture)) {

            psFacture.setInt(1, idFacture);
            ResultSet rsFacture = psFacture.executeQuery();

            if (!rsFacture.next()) {
                System.out.println("Facture introuvable.");
                con.rollback();
                return;
            }

            double montantFacture = rsFacture.getDouble("montant");

            //  Total déjà payé
            String sqlTotal = "SELECT COALESCE(SUM(montant),0) FROM paiement WHERE idFacture = ?";
            double totalPaye = 0;

            try (PreparedStatement psTotal = con.prepareStatement(sqlTotal)) {
                psTotal.setInt(1, idFacture);
                ResultSet rsTotal = psTotal.executeQuery();
                if (rsTotal.next()) {
                    totalPaye = rsTotal.getDouble(1);
                }
            }

            double restant = montantFacture - totalPaye;

            System.out.println("Montant facture : " + montantFacture);
            System.out.println("Total déjà payé : " + totalPaye);
            System.out.println("Montant restant : " + restant);

            if (restant <= 0) {
                System.out.println("Cette facture est déjà payée.");
                con.rollback();
                return;
            }

            // Saisie paiement
            System.out.print("Montant à payer: ");
            double montantPaye = Double.parseDouble(scanner.nextLine());

            if (montantPaye <= 0 || montantPaye > restant) {
                System.out.println("Paiement refusé.");
                con.rollback();
                return;
            }

            System.out.print("Date (YYYY-MM-DD): ");
            LocalDate date = LocalDate.parse(scanner.nextLine());

            System.out.print("Méthode de paiement: ");
            String paymentMethod = scanner.nextLine().trim();

            // Insert paiement
            String sqlInsert = "INSERT INTO paiement (montant, date, idFacture, methode) VALUES (?, ?, ?, ?)";
            int paymentId = 0;

            try (PreparedStatement psInsert = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {

                psInsert.setDouble(1, montantPaye);
                psInsert.setDate(2, Date.valueOf(date));
                psInsert.setInt(3, idFacture);
                psInsert.setString(4, paymentMethod);

                psInsert.executeUpdate();

                ResultSet keys = psInsert.getGeneratedKeys();
                if (keys.next()) {
                    paymentId = keys.getInt(1);
                }
            }

            //  Mise à jour status
            double nouveauTotal = totalPaye + montantPaye;
            double remainingAfter = montantFacture - nouveauTotal;

            String nouveauStatus =testfacture(nouveauTotal,montantFacture);

//            if (nouveauTotal == montantFacture) {
//                nouveauStatus = "PAID";
//            } else {
//                nouveauStatus = "PARTIAL";
//            }

            String sqlUpdate = "UPDATE facture SET status = ? WHERE id = ?";
            try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdate)) {
                psUpdate.setString(1, nouveauStatus);
                psUpdate.setInt(2, idFacture);
                psUpdate.executeUpdate();
            }

            //  commit final
            con.commit();

            System.out.println("Paiement enregistré avec succès !");
            System.out.println("Nouveau total payé : " + nouveauTotal);
            System.out.println("Nouveau status : " + nouveauStatus);

            // Générer reçu PDF
            Recy_Payment.generateRecy(
                    paymentId,
                    idFacture,
                    date,
                    paymentMethod,
                    montantPaye,
                    remainingAfter
            );

        }

    } catch (Exception e) {
        try {
            if (con != null) con.rollback(); // 🔥 rollback sécurisé
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        System.out.println("Erreur lors de l'enregistrement du paiement");
        e.printStackTrace();

    } finally {
        try {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
public static String testfacture(double nouveauTotal, double montantFacture){
    if (nouveauTotal == montantFacture) {
        return  "PAID";
    } else {
        return  "PENDING";
    }
}

    public static void mettreAJourPyment(Scanner scanner) {
        try {
            System.out.print("Paiement ID: ");
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("Nouveau montant: ");
            double montant = Double.parseDouble(scanner.nextLine());

            System.out.print("Nouvelle date (YYYY-MM-DD): ");
            LocalDate date = LocalDate.parse(scanner.nextLine());

            String sql = "UPDATE paiement SET montant = ?, date = ? WHERE id = ?";

            try (Connection con = DBConnection.createConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setDouble(1, montant);
                ps.setDate(2, Date.valueOf(date));
                ps.setInt(3, id);

                int rows = ps.executeUpdate();
                if (rows > 0) System.out.println("Paiement modifié");
                else System.out.println("ID introuvable");
            }

        } catch (Exception e) {
            System.out.println("Erreur (vérifiez les valeurs)");
            e.printStackTrace();
        }
    }

    public static void listerPyment() {
        String sql = "SELECT id, idFacture, montant, date FROM paiement";

        try (Connection con = DBConnection.createConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("=== Liste des paiements ===");

            boolean empty = true;
            while (rs.next()) {
                empty = false;
                System.out.println(
                        "ID: " + rs.getInt("id") +
                                " | Facture: " + rs.getInt("idFacture") +
                                " | Montant: " + rs.getDouble("montant") +
                                " | Date: " + rs.getDate("date")
                );
            }

            if (empty) System.out.println("(aucun paiement)");

        } catch (Exception e) {
            System.out.println("Erreur SQL");
            e.printStackTrace();
        }
    }
}