package org.example;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class exportFileTest {

    @Test
    void exporterFacturesPrestataire() {
        String input = "2\n2\n2026\n";
        InputStream originalIn = System.in;
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        double resultat = exportFile.exporterFacturesPrestataire();
        assertEquals(484.0, resultat, "Le total payé pour le prestataire 2 devrait être 484.0");
    }

    @Test
    void testSommeCasListeVide() {
        String input = "999\n2\n2026\n";
      System.setIn(new ByteArrayInputStream(input.getBytes()));
        double result = exportFile.exporterFacturesPrestataire();
        assertEquals(0.0, result, "Le total doit être 0 si aucune donnée n'est trouvée");
    }

    @Test
    void testFormatsNomsFichiers() {

        assertEquals("Facture_123.pdf", exportFile.formatNomFacture(123));

        assertEquals("recu_456.pdf", exportFile.formatNomRecu(456));

        assertEquals("rapport012026.xls", exportFile.formatNomRapport(1, 2026));
    }

}