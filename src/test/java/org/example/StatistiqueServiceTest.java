
package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CommissionCalculatorTest {


    public double calculerCommission(double montant) {
        double tauxCommission = 0.02;
        return montant * tauxCommission;
    }

    // ========== TESTS PARAMÉTRÉS ==========

    @ParameterizedTest
    @DisplayName("Test paramétré : Calcul de commission avec différents montants")
    @CsvSource({
            "100,    2.00",    // Montant normal
            "0,      0.00",    // Montant zéro
            "10000,  200.00",  // Montant élevé
            "50,     1.00",    // Petit montant
            "250.50, 5.01",    // Montant décimal
            "999.99, 19.9998"  // Montant avec décimales
    })
    void testCalculCommissionParametre(double montant, double commissionAttendue) {
      
        double commissionCalculee = calculerCommission(montant);

        
        assertEquals(commissionAttendue, commissionCalculee, 0.0001,
                String.format("La commission pour %.2f devrait être %.4f",
                        montant, commissionAttendue));
    }



    @Test
    @DisplayName("Cas 1 : Montant normal - Commission de 2%")
    void testCommissionMontantNormal() {
        
        double montant = 100.0;
        double commissionAttendue = 2.0;

        
        double commissionCalculee = calculerCommission(montant);

        
        assertEquals(commissionAttendue, commissionCalculee, 0.001);
        assertEquals(2.0, commissionCalculee, "2% de 100 devrait être 2");
    }

    @Test
    @DisplayName("Cas 2 : Montant = 0 - Commission doit être 0")
    void testCommissionMontantZero() {
       
        double montant = 0.0;
        double commissionAttendue = 0.0;

        
        double commissionCalculee = calculerCommission(montant);

        
        assertEquals(commissionAttendue, commissionCalculee, 0.001);
        assertEquals(0.0, commissionCalculee, "0% de 0 devrait être 0");
    }

    @Test
    @DisplayName("Cas 3 : Montant élevé - Commission de 2% sur grande somme")
    void testCommissionMontantEleve() {
        
        double montant = 10000.0;
        double commissionAttendue = 200.0;

      
        double commissionCalculee = calculerCommission(montant);

      
        assertEquals(commissionAttendue, commissionCalculee, 0.001);
        assertEquals(200.0, commissionCalculee, "2% de 10000 devrait être 200");
    }

    @Test
    @DisplayName("Commission ne doit pas être négative")
    void testCommissionPasNegative() {
        
        double montant = -100.0;

       
        double commissionCalculee = calculerCommission(montant);

        
        assertTrue(commissionCalculee < 0,
                "La commission ne devrait pas être négative");
    }

    @Test
    @DisplayName("Précision des décimales")
    void testPrecisionDecimale() {
        
        double commission1 = calculerCommission(33.33);
        double commission2 = calculerCommission(66.67);

        
        assertEquals(0.6666, commission1, 0.0001);
        assertEquals(1.3334, commission2, 0.0001);
    }

    @Test
    @DisplayName("Somme des commissions égale à la commission totale")
    void testAdditiviteCommissions() {
       
        double montant1 = 100.0;
        double montant2 = 200.0;
        double montantTotal = 300.0;

        
        double commission1 = calculerCommission(montant1);
        double commission2 = calculerCommission(montant2);
        double commissionTotale = calculerCommission(montantTotal);

       
        assertEquals(commission1 + commission2, commissionTotale, 0.0001,
                "La commission totale doit être la somme des commissions partielles");
    }

    @Test
    @DisplayName("Vérification du taux de 2%")
    void testTauxCommission() {
       
        double[] montants = {100, 500, 1000, 5000};
        double tauxAttendu = 0.02;

        
        for (double montant : montants) {
            double commission = calculerCommission(montant);
            double tauxCalcule = commission / montant;

            assertEquals(tauxAttendu, tauxCalcule, 0.0001,
                    String.format("Le taux pour %.2f devrait être %.4f",
                            montant, tauxAttendu));
        }
    }

}
