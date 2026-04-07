package voorraadBeheer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service-klasse die alle bedrijfslogica voor het voorraadbeheer afhandelt.
 * Beheert de lijst van producten en biedt methodes om producten toe te voegen,
 * te verwijderen, te wijzigen, af te boeken en te doorzoeken.
 */
public class InventoryService {
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
     * Boekt voorraad in voor een product (verhoogt de voorraad).
     *
     * @param artikelNummer Het artikelnummer
     * @param aantal        Het aantal om in te boeken (moet groter zijn dan 0)
     * @return Het bijgewerkte Product
     * @throws IllegalArgumentException als het product niet gevonden is of het aantal ongeldig is
     */
    public Product boekIn(String artikelNummer, int aantal) {
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
     * Werkt een bestaand product bij met nieuwe gegevens.
     *
     * @param artikelNummer Het artikelnummer van het te wijzigen product
     * @param naam          De nieuwe naam
     * @param voorraad      De nieuwe voorraad
     * @param minimum       De nieuwe minimumvoorraad
     * @param prijs         De nieuwe prijs
     * @throws IllegalArgumentException als het product niet gevonden is of waarden ongeldig zijn
     */
    public void updateProduct(String artikelNummer, String naam, int voorraad, int minimum, double prijs) {
        Product product = zoekProduct(artikelNummer);
        if (product == null) {
            throw new IllegalArgumentException("Artikel niet gevonden: " + artikelNummer);
        }
        product.setNaam(naam);
        product.setVoorraad(voorraad);
        product.setMinimumVoorraad(minimum);
        product.setPrijs(prijs);
    }

    /**
     * Berekent de totale waarde van alle voorraad (som van voorraad × prijs per product).
     *
     * @return De totale voorraadwaarde
     */
    public double berekenTotaleWaarde() {
        double totaal = 0.0;
        for (Product product : producten) {
            totaal += product.getVoorraad() * product.getPrijs();
        }
        return totaal;
    }

    /**
     * Slaat alle producten op als CSV-bestand.
     * Formaat per regel: artikelNummer;naam;voorraad;minimum;prijs
     *
     * @param bestandspad Pad naar het CSV-bestand
     * @return true als opslaan gelukt is, anders false
     */
    public boolean slaCSVOp(String bestandspad) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(bestandspad))) {
            for (Product product : producten) {
                writer.write(String.join(";",
                        product.getArtikelNummer(),
                        product.getNaam(),
                        String.valueOf(product.getVoorraad()),
                        String.valueOf(product.getMinimumVoorraad()),
                        String.valueOf(product.getPrijs())));
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Fout bij opslaan CSV: " + e.getMessage());
            return false;
        }
    }

    /**
     * Laadt producten vanuit een CSV-bestand.
     * Verwacht formaat per regel: artikelNummer;naam;voorraad;minimum;prijs
     *
     * @param bestandspad Pad naar het CSV-bestand
     * @return true als laden gelukt is, anders false
     */
    public boolean laadVanCSV(String bestandspad) {
        try (BufferedReader reader = new BufferedReader(new FileReader(bestandspad))) {
            List<Product> geladen = new ArrayList<>();
            String regel;
            while ((regel = reader.readLine()) != null) {
                String[] delen = regel.split(";", -1);
                if (delen.length < 5) continue; // Ongeldige regel overslaan
                String artikelNummer = delen[0].trim();
                String naam          = delen[1].trim();
                int    voorraad      = Integer.parseInt(delen[2].trim());
                int    minimum       = Integer.parseInt(delen[3].trim());
                double prijs         = Double.parseDouble(delen[4].trim());
                geladen.add(new Product(naam, artikelNummer, voorraad, minimum, prijs));
            }
            producten.clear();
            producten.addAll(geladen);
            return true;
        } catch (IOException | NumberFormatException e) {
            System.err.println("Fout bij laden CSV: " + e.getMessage());
            return false;
        }
    }

    /**
     * Laadt voorbeelddata in het systeem.
     */
    public void laadVoorbeeldData() {
        producten.add(new Product("Laptoptas",      "ART001",  5, 10,  29.99));
        producten.add(new Product("USB-C Kabel",    "ART002", 25, 20,   9.99));
        producten.add(new Product("Draadloze Muis", "ART003",  8, 15,  24.99));
        producten.add(new Product("Toetsenbord",    "ART004", 12,  8,  49.99));
        producten.add(new Product("Monitor 24\"",   "ART005",  3,  5, 199.99));
    }
}
