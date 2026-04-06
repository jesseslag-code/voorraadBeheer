package voorraadBeheer;

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
     * Laadt voorbeelddata in het systeem.
     */
    public void laadVoorbeeldData() {
        producten.add(new Product("Laptoptas", "ART001", 5, 10));
        producten.add(new Product("USB-C Kabel", "ART002", 25, 20));
        producten.add(new Product("Draadloze Muis", "ART003", 8, 15));
        producten.add(new Product("Toetsenbord", "ART004", 12, 8));
        producten.add(new Product("Monitor 24\"", "ART005", 3, 5));
    }
}
