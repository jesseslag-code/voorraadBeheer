package voorraadBeheer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service-klasse die alle bedrijfslogica voor het voorraadbeheer afhandelt.
 * Beheert de lijst van producten en biedt methodes om producten toe te voegen,
 * te verwijderen, te wijzigen, af/bij te boeken en te doorzoeken.
 * Data wordt opgeslagen in een CSV-bestand voor persistentie.
 */
public class InventoryService {
    private static final String BESTANDSNAAM = "voorraad.csv";
    private final List<Product> producten = new ArrayList<>();

    /**
     * Geeft een onwijzigbare kopie van alle producten.
     *
     * @return Lijst van alle producten
     */
    public List<Product> getAlleProducten() {
        return Collections.unmodifiableList(producten);
    }

    /**
     * Zoekt een product op basis van artikelnummer (hoofdletterongevoelig).
     *
     * @param artikelNummer Het artikelnummer om op te zoeken
     * @return Het gevonden Product, of null als het niet bestaat
     */
    public Product zoekProduct(String artikelNummer) {
        if (artikelNummer == null) {
            return null;
        }
        for (Product product : producten) {
            if (product.getArtikelNummer().equalsIgnoreCase(artikelNummer)) {
                return product;
            }
        }
        return null;
    }

    /**
     * Zoekt producten waarvan de naam de zoekterm bevat (hoofdletterongevoelig).
     *
     * @param zoekterm De naam of een deel ervan
     * @return Lijst van overeenkomende producten
     */
    public List<Product> zoekOpNaam(String zoekterm) {
        List<Product> resultaten = new ArrayList<>();
        if (zoekterm == null || zoekterm.isBlank()) {
            return resultaten;
        }
        String lowerZoekterm = zoekterm.toLowerCase();
        for (Product product : producten) {
            if (product.getNaam().toLowerCase().contains(lowerZoekterm)) {
                resultaten.add(product);
            }
        }
        return resultaten;
    }

    /**
     * Voegt een nieuw product toe aan de voorraad.
     *
     * @param product Het toe te voegen product
     * @return true als het product succesvol is toegevoegd, false als het artikelnummer al bestaat
     */
    public boolean voegProductToe(Product product) {
        if (zoekProduct(product.getArtikelNummer()) != null) {
            return false;
        }
        producten.add(product);
        return true;
    }

    /**
     * Verwijdert een product uit de voorraad op basis van artikelnummer.
     *
     * @param artikelNummer Het artikelnummer van het te verwijderen product
     * @return true als het product is verwijderd, false als het niet gevonden is
     */
    public boolean verwijderProduct(String artikelNummer) {
        Product product = zoekProduct(artikelNummer);
        if (product == null) {
            return false;
        }
        producten.remove(product);
        return true;
    }

    /**
     * Boekt voorraad af van een product (vermindert de voorraad).
     *
     * @param artikelNummer Het artikelnummer
     * @param aantal        Het aantal om af te boeken (moet groter zijn dan 0)
     * @return Het bijgewerkte Product
     * @throws IllegalArgumentException als het product niet gevonden is,
     *         het aantal ongeldig is, of er onvoldoende voorraad is
     */
    public Product boekAf(String artikelNummer, int aantal) {
        Product product = zoekProduct(artikelNummer);
        if (product == null) {
            throw new IllegalArgumentException("Artikel niet gevonden: " + artikelNummer);
        }
        if (aantal <= 0) {
            throw new IllegalArgumentException("Aantal moet groter zijn dan 0.");
        }
        if (aantal > product.getVoorraad()) {
            throw new IllegalArgumentException(
                    "Onvoldoende voorraad! Beschikbaar: " + product.getVoorraad());
        }
        product.setVoorraad(product.getVoorraad() - aantal);
        return product;
    }

    /**
     * Boekt extra voorraad bij voor een product (verhoogt de voorraad).
     *
     * @param artikelNummer Het artikelnummer
     * @param aantal        Het aantal om bij te boeken (moet groter zijn dan 0)
     * @return Het bijgewerkte Product
     * @throws IllegalArgumentException als het product niet gevonden is of het aantal ongeldig is
     */
    public Product boekBij(String artikelNummer, int aantal) {
        Product product = zoekProduct(artikelNummer);
        if (product == null) {
            throw new IllegalArgumentException("Artikel niet gevonden: " + artikelNummer);
        }
        if (aantal <= 0) {
            throw new IllegalArgumentException("Aantal moet groter zijn dan 0.");
        }
        product.setVoorraad(product.getVoorraad() + aantal);
        return product;
    }

    /**
     * Geeft een lijst van alle producten waarvan de voorraad onder het minimum ligt.
     *
     * @return Lijst van producten met lage voorraad
     */
    public List<Product> getProductenMetLageVoorraad() {
        List<Product> lageVoorraad = new ArrayList<>();
        for (Product product : producten) {
            if (product.isLageVoorraad()) {
                lageVoorraad.add(product);
            }
        }
        return lageVoorraad;
    }

    /**
     * Berekent de totale waarde van alle producten in het systeem.
     *
     * @return Totale voorraadwaarde in euro's
     */
    public double getTotaleVoorraadWaarde() {
        double totaal = 0;
        for (Product product : producten) {
            totaal += product.getVoorraadWaarde();
        }
        return totaal;
    }

    /**
     * Slaat alle producten op in het CSV-bestand.
     *
     * @throws IOException als het bestand niet geschreven kan worden
     */
    public void slaanOp() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(BESTANDSNAAM), StandardCharsets.UTF_8))) {
            for (Product product : producten) {
                writer.write(String.format("%s;%s;%d;%d;%.2f%n",
                        product.getArtikelNummer(),
                        product.getNaam(),
                        product.getVoorraad(),
                        product.getMinimumVoorraad(),
                        product.getPrijs()));
            }
        }
    }

    /**
     * Laadt producten uit het CSV-bestand. Als het bestand niet bestaat, wordt voorbeelddata geladen.
     */
    public void laden() {
        File bestand = new File(BESTANDSNAAM);
        if (!bestand.exists()) {
            laadVoorbeeldData();
            return;
        }
        producten.clear();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(bestand), StandardCharsets.UTF_8))) {
            String regel;
            while ((regel = reader.readLine()) != null) {
                if (regel.isBlank()) continue;
                String[] delen = regel.split(";");
                if (delen.length < 5) continue;
                String artikelNummer = delen[0].trim();
                String naam = delen[1].trim();
                int voorraad = Integer.parseInt(delen[2].trim());
                int minimumVoorraad = Integer.parseInt(delen[3].trim());
                double prijs = Double.parseDouble(delen[4].trim().replace(",", "."));
                producten.add(new Product(naam, artikelNummer, voorraad, minimumVoorraad, prijs));
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Waarschuwing: fout bij laden van data — voorbeelddata geladen.");
            producten.clear();
            laadVoorbeeldData();
        }
    }

    /**
     * Laadt voorbeelddata in het systeem.
     */
    public void laadVoorbeeldData() {
        producten.add(new Product("Laptoptas", "ART001", 5, 10, 29.99));
        producten.add(new Product("USB-C Kabel", "ART002", 25, 20, 9.95));
        producten.add(new Product("Draadloze Muis", "ART003", 8, 15, 24.50));
        producten.add(new Product("Toetsenbord", "ART004", 12, 8, 49.99));
        producten.add(new Product("Monitor 24\"", "ART005", 3, 5, 199.00));
    }
}
