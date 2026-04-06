package voorraadBeheer;


public class Insert {
    private String naam;
    private String artikelNummer;
    private int voorraad;
    private int minimumVoorraad;

    /**
     * Constructor voor de Insert class
     * @param naam De naam van het artikel
     * @param artikelNummer Het unieke artikelnummer
     * @param voorraad De huidige hoeveelheid in voorraad
     * @param minimumVoorraad De minimaal vereiste hoeveelheid in voorraad
     */
    public Insert(String naam, String artikelNummer, int voorraad, int minimumVoorraad) {
        this.naam = naam;
        this.artikelNummer = artikelNummer;
        this.voorraad = voorraad;
        this.minimumVoorraad = minimumVoorraad;
    }

    // Getters
    /**
     * Geeft de naam van het artikel terug
     * @return De naam als String
     */
    public String getNaam() {
        return naam;
    }

    /**
     * Geeft het artikelnummer terug
     * @return Het artikelnummer als String
     */
    public String getArtikelNummer() {
        return artikelNummer;
    }

    /**
     * Geeft de huidige voorraad terug
     * @return De voorraad als int
     */
    public int getVoorraad() {
        return voorraad;
    }

    /**
     * Geeft de minimale voorraad terug
     * @return De minimumVoorraad als int
     */
    public int getMinimumVoorraad() {
        return minimumVoorraad;
    }

    // Setters
    /**
     * Stelt de naam van het artikel in
     * @param naam De nieuwe naam
     */
    public void setNaam(String naam) {
        this.naam = naam;
    }

    /**
     * Stelt het artikelnummer in
     * @param artikelNummer Het nieuwe artikelnummer
     */
    public void setArtikelNummer(String artikelNummer) {
        this.artikelNummer = artikelNummer;
    }

    /**
     * Stelt de voorraad in
     * @param voorraad De nieuwe voorraadwaarde
     */
    public void setVoorraad(int voorraad) {
        this.voorraad = voorraad;
    }

    /**
     * Stelt de minimale voorraad in
     * @param minimumVoorraad De nieuwe minimale voorraadwaarde
     */
    public void setMinimumVoorraad(int minimumVoorraad) {
        this.minimumVoorraad = minimumVoorraad;
    }
}