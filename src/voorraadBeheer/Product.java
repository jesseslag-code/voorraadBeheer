package voorraadBeheer;

/**
 * Representeert een product in het voorraadbeheersysteem.
 * Bevat alle gegevens over een artikel: naam, artikelnummer, voorraad, minimumvoorraad en prijs.
 */
public class Product {
    private String naam;
    private String artikelNummer;
    private int voorraad;
    private int minimumVoorraad;
    private double prijs;

    /**
     * Constructor voor een nieuw Product.
     *
     * @param naam             De naam van het product
     * @param artikelNummer    Het unieke artikelnummer
     * @param voorraad         De huidige hoeveelheid in voorraad
     * @param minimumVoorraad  De minimaal vereiste hoeveelheid in voorraad
     * @param prijs            De stuksprijs van het product (mag niet negatief zijn)
     * @throws IllegalArgumentException als voorraad, minimumVoorraad of prijs negatief is
     */
    public Product(String naam, String artikelNummer, int voorraad, int minimumVoorraad, double prijs) {
        if (voorraad < 0) {
            throw new IllegalArgumentException("Voorraad kan niet negatief zijn.");
        }
        if (minimumVoorraad < 0) {
            throw new IllegalArgumentException("Minimum voorraad kan niet negatief zijn.");
        }
        if (prijs < 0) {
            throw new IllegalArgumentException("Prijs kan niet negatief zijn.");
        }
        this.naam = naam;
        this.artikelNummer = artikelNummer;
        this.voorraad = voorraad;
        this.minimumVoorraad = minimumVoorraad;
        this.prijs = prijs;
    }

    // --- Getters ---

    /** @return De naam van het product */
    public String getNaam() {
        return naam;
    }

    /** @return Het unieke artikelnummer */
    public String getArtikelNummer() {
        return artikelNummer;
    }

    /** @return De huidige voorraad */
    public int getVoorraad() {
        return voorraad;
    }

    /** @return De minimaal vereiste voorraad */
    public int getMinimumVoorraad() {
        return minimumVoorraad;
    }

    /** @return De stuksprijs van het product */
    public double getPrijs() {
        return prijs;
    }

    // --- Setters ---

    /** @param naam De nieuwe naam van het product */
    public void setNaam(String naam) {
        this.naam = naam;
    }

    /** @param artikelNummer Het nieuwe artikelnummer */
    public void setArtikelNummer(String artikelNummer) {
        this.artikelNummer = artikelNummer;
    }

    /**
     * Stelt de voorraad in.
     *
     * @param voorraad De nieuwe voorraadwaarde (mag niet negatief zijn)
     * @throws IllegalArgumentException als de waarde negatief is
     */
    public void setVoorraad(int voorraad) {
        if (voorraad < 0) {
            throw new IllegalArgumentException("Voorraad kan niet negatief zijn.");
        }
        this.voorraad = voorraad;
    }

    /**
     * Stelt de minimale voorraad in.
     *
     * @param minimumVoorraad De nieuwe minimale voorraadwaarde (mag niet negatief zijn)
     * @throws IllegalArgumentException als de waarde negatief is
     */
    public void setMinimumVoorraad(int minimumVoorraad) {
        if (minimumVoorraad < 0) {
            throw new IllegalArgumentException("Minimum voorraad kan niet negatief zijn.");
        }
        this.minimumVoorraad = minimumVoorraad;
    }

    /**
     * Stelt de stuksprijs in.
     *
     * @param prijs De nieuwe prijs (mag niet negatief zijn)
     * @throws IllegalArgumentException als de waarde negatief is
     */
    public void setPrijs(double prijs) {
        if (prijs < 0) {
            throw new IllegalArgumentException("Prijs kan niet negatief zijn.");
        }
        this.prijs = prijs;
    }

    // --- Hulpmethodes ---

    /**
     * Controleert of de voorraad onder het minimum ligt.
     *
     * @return true als de voorraad lager is dan de minimumVoorraad
     */
    public boolean isLageVoorraad() {
        return voorraad < minimumVoorraad;
    }

    /**
     * Berekent het tekort ten opzichte van de minimumVoorraad.
     *
     * @return Het aantal producten dat tekort is, of 0 als er genoeg voorraad is
     */
    public int getTekort() {
        return isLageVoorraad() ? minimumVoorraad - voorraad : 0;
    }

    @Override
    public String toString() {
        return String.format("Product{artikelNummer='%s', naam='%s', voorraad=%d, minimum=%d, prijs=%.2f}",
                artikelNummer, naam, voorraad, minimumVoorraad, prijs);
    }
}
