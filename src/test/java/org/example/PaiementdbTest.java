
package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaiementdbTest {
    @Test
    @DisplayName("test facture payée")
    void testcas1(){
        double montantTotal = 1000;
        double montantPaye=1000;
        String Status=Paiementdb.testfacture(montantPaye,montantTotal);
        assertEquals("PAID",Status);
    }
    @Test
    @DisplayName("le status facture doit etre p partiel")
    void testcas2(){
        double montantTotal = 1000;
        double montantPaye=100;
        String Status=Paiementdb.testfacture(montantPaye,montantTotal);
        assertEquals("PENDING",Status);
    }

    @Test
    @DisplayName("le status de facture doit etre aucun paiement")
    void testcas3(){
        double montantTotal = 1000;
        double montantPaye=0;
        String Status=Paiementdb.testfacture(montantPaye,montantTotal);
        assertEquals("PENDING",Status);
    }



}