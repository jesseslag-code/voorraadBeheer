package voorraadBeheer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Hoofdklasse met het consolemenu voor het voorraadbeheersysteem.
 * Alle bedrijfslogica wordt afgehandeld via {@link InventoryService}.
 */
public class Main {

    // ──────────────────────────────────────────────
    //  Lay-out constanten
    // ──────────────────────────────────────────────
    private static final int BREEDTE = 82;
    private static final String LIJN_H  = "═".repeat(BREEDTE);
    private static final String LIJN_M  = "─".repeat(BREEDTE);
    private static final String VERSIE  = "v2.0";

    private static final InventoryService service = new InventoryService();
    private static final Scanner scanner = new Scanner(System.in);

    // ──────────────────────────────────────────────
    //  Main
    // ──────────────────────────────────────────────

    public static void main(String[] args) {
        toonSplashScreen();
        service.laden();

        List<Product> lageVoorraad = service.getProductenMetLageVoorraad();
        if (!lageVoorraad.isEmpty()) {
            toonMelding("WAARSCHUWING", lageVoorraad.size()
                    + " product(en) hebben een te lage voorraad bij het opstarten!");
        }

        boolean actief = true;
        while (actief) {
            toonMenu();
            int keuze = leesInt("Maak uw keuze (1-9): ");

            System.out.println();
            switch (keuze) {
                case 1: toonAlleProducten();   break;
                case 2: zoekProduct();          break;
                case 3: afboekenProduct();      break;
                case 4: bijboekenProduct();     break;
                case 5: toevoegenProduct();     break;
                case 6: productWijzigen();      break;
                case 7: verwijderProduct();     break;
                case 8: checkLageVoorraad();    break;
                case 9:
                    opslaan();
                    drukKader("Systeem afgesloten. Tot ziens!", true);
                    actief = false;
                    break;
                default:
                    toonFout("Ongeldige keuze. Kies een optie tussen 1 en 9.");
            }
            if (actief) {
                System.out.print("\nDruk op ENTER om door te gaan...");
                scanner.nextLine();
            }
        }
        scanner.close();
    }

    // ──────────────────────────────────────────────
    //  Splash-scherm
    // ──────────────────────────────────────────────

    private static void toonSplashScreen() {
        System.out.println();
        System.out.println("╔" + LIJN_H + "╗");
        drukCenteredRegel("VOORRAADBEHEER SYSTEEM  " + VERSIE, true);
        drukCenteredRegel("Professioneel voorraad- en artikelbeheer", false);
        System.out.println("╚" + LIJN_H + "╝");
        System.out.println();
    }

    // ──────────────────────────────────────────────
    //  Menu
    // ──────────────────────────────────────────────

    private static void toonMenu() {
        String tijdstip = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy  HH:mm:ss"));
        System.out.println();
        System.out.println("╔" + LIJN_H + "╗");
        drukCenteredRegel("HOOFDMENU", true);
        drukCenteredRegel(tijdstip, false);
        System.out.println("╠" + LIJN_H + "╣");
        drukMenuOptie("1", "Alle producten weergeven");
        drukMenuOptie("2", "Product zoeken");
        drukMenuOptie("3", "Product afboeken  (voorraad verminderen)");
        drukMenuOptie("4", "Product bijboeken  (voorraad aanvullen)");
        drukMenuOptie("5", "Nieuw product toevoegen");
        drukMenuOptie("6", "Product wijzigen");
        drukMenuOptie("7", "Product verwijderen");
        drukMenuOptie("8", "Producten met lage voorraad controleren");
        drukMenuOptie("9", "Opslaan en afsluiten");
        System.out.println("╚" + LIJN_H + "╝");
    }

    // ──────────────────────────────────────────────
    //  1. Alle producten weergeven
    // ──────────────────────────────────────────────

    private static void toonAlleProducten() {
        List<Product> producten = service.getAlleProducten();
        drukSchermTitel("PRODUCTOVERZICHT  (" + producten.size() + " artikel(en))");

        if (producten.isEmpty()) {
            toonMelding("INFO", "Geen producten in het systeem.");
            return;
        }

        toonProductTabelHeader();
        for (Product p : producten) {
            toonProductRegel(p);
        }
        System.out.println("╠" + LIJN_H + "╣");
        System.out.printf("║  %-76s  ║%n",
                String.format("Totale voorraadwaarde:  € %,.2f", service.getTotaleVoorraadWaarde()));
        System.out.println("╚" + LIJN_H + "╝");
    }

    // ──────────────────────────────────────────────
    //  2. Zoeken
    // ──────────────────────────────────────────────

    private static void zoekProduct() {
        drukSchermTitel("PRODUCT ZOEKEN");
        System.out.print("  Zoekterm (naam of artikelnummer): ");
        String zoekterm = scanner.nextLine().trim();

        // Probeer eerst op artikelnummer
        Product exactMatch = service.zoekProduct(zoekterm);
        if (exactMatch != null) {
            toonProductTabelHeader();
            toonProductRegel(exactMatch);
            System.out.println("╚" + LIJN_H + "╝");
            return;
        }

        // Daarna op naam
        List<Product> resultaten = service.zoekOpNaam(zoekterm);
        if (resultaten.isEmpty()) {
            toonFout("Geen producten gevonden voor: \"" + zoekterm + "\"");
            return;
        }
        toonProductTabelHeader();
        for (Product p : resultaten) {
            toonProductRegel(p);
        }
        System.out.println("╚" + LIJN_H + "╝");
    }

    // ──────────────────────────────────────────────
    //  3. Afboeken
    // ──────────────────────────────────────────────

    private static void afboekenProduct() {
        drukSchermTitel("PRODUCT AFBOEKEN");
        System.out.print("  Artikelnummer: ");
        String artikelNummer = scanner.nextLine().trim();

        Product gevonden = service.zoekProduct(artikelNummer);
        if (gevonden == null) {
            toonFout("Artikel niet gevonden: " + artikelNummer);
            return;
        }
        toonProductKaart(gevonden);

        int aantal = leesInt("  Aantal af te boeken: ");
        try {
            Product product = service.boekAf(artikelNummer, aantal);
            opslaan();
            toonSucces("Afboeking verwerkt — nieuwe voorraad: " + product.getVoorraad() + " stuks");
            if (product.isLageVoorraad()) {
                toonLageVoorraadWaarschuwing(product);
            }
        } catch (IllegalArgumentException e) {
            toonFout(e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  4. Bijboeken
    // ──────────────────────────────────────────────

    private static void bijboekenProduct() {
        drukSchermTitel("PRODUCT BIJBOEKEN");
        System.out.print("  Artikelnummer: ");
        String artikelNummer = scanner.nextLine().trim();

        Product gevonden = service.zoekProduct(artikelNummer);
        if (gevonden == null) {
            toonFout("Artikel niet gevonden: " + artikelNummer);
            return;
        }
        toonProductKaart(gevonden);

        int aantal = leesInt("  Aantal bij te boeken: ");
        try {
            Product product = service.boekBij(artikelNummer, aantal);
            opslaan();
            toonSucces("Bijboeking verwerkt — nieuwe voorraad: " + product.getVoorraad() + " stuks");
        } catch (IllegalArgumentException e) {
            toonFout(e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  5. Toevoegen
    // ──────────────────────────────────────────────

    private static void toevoegenProduct() {
        drukSchermTitel("NIEUW PRODUCT TOEVOEGEN");
        System.out.print("  Artikelnummer  : ");
        String artikelNummer = scanner.nextLine().trim();

        System.out.print("  Productnaam    : ");
        String naam = scanner.nextLine().trim();

        int voorraad      = leesInt("  Huidige voorraad : ");
        int minimum       = leesInt("  Minimum voorraad : ");
        double prijs      = leesDouble("  Stukprijs (€)    : ");

        try {
            Product product = new Product(naam, artikelNummer, voorraad, minimum, prijs);
            if (service.voegProductToe(product)) {
                opslaan();
                toonSucces("Product \"" + naam + "\" toegevoegd aan het systeem.");
                if (product.isLageVoorraad()) {
                    toonLageVoorraadWaarschuwing(product);
                }
            } else {
                toonFout("Artikelnummer \"" + artikelNummer + "\" bestaat al in het systeem.");
            }
        } catch (IllegalArgumentException e) {
            toonFout(e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  6. Wijzigen
    // ──────────────────────────────────────────────

    private static void productWijzigen() {
        drukSchermTitel("PRODUCT WIJZIGEN");
        System.out.print("  Artikelnummer: ");
        String artikelNummer = scanner.nextLine().trim();

        Product product = service.zoekProduct(artikelNummer);
        if (product == null) {
            toonFout("Artikel niet gevonden: " + artikelNummer);
            return;
        }
        toonProductKaart(product);

        System.out.println("  Wat wilt u wijzigen?");
        System.out.println("    [1] Naam");
        System.out.println("    [2] Voorraad");
        System.out.println("    [3] Minimum voorraad");
        System.out.println("    [4] Stukprijs");
        int keuze = leesInt("  Keuze (1-4): ");

        try {
            switch (keuze) {
                case 1:
                    System.out.print("  Nieuwe naam: ");
                    product.setNaam(scanner.nextLine().trim());
                    break;
                case 2:
                    product.setVoorraad(leesInt("  Nieuwe voorraad: "));
                    break;
                case 3:
                    product.setMinimumVoorraad(leesInt("  Nieuw minimum: "));
                    break;
                case 4:
                    product.setPrijs(leesDouble("  Nieuwe prijs (€): "));
                    break;
                default:
                    toonFout("Ongeldige keuze.");
                    return;
            }
            opslaan();
            toonSucces("Wijziging opgeslagen.");
            if (product.isLageVoorraad()) {
                toonLageVoorraadWaarschuwing(product);
            }
        } catch (IllegalArgumentException e) {
            toonFout(e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  7. Verwijderen
    // ──────────────────────────────────────────────

    private static void verwijderProduct() {
        drukSchermTitel("PRODUCT VERWIJDEREN");
        System.out.print("  Artikelnummer: ");
        String artikelNummer = scanner.nextLine().trim();

        Product product = service.zoekProduct(artikelNummer);
        if (product == null) {
            toonFout("Artikel niet gevonden: " + artikelNummer);
            return;
        }
        toonProductKaart(product);

        System.out.print("  Bevestig verwijdering (j/n): ");
        String bevestiging = scanner.nextLine().trim().toLowerCase();

        if (bevestiging.equals("j")) {
            service.verwijderProduct(artikelNummer);
            opslaan();
            toonSucces("Product \"" + product.getNaam() + "\" is verwijderd uit het systeem.");
        } else {
            toonMelding("INFO", "Verwijdering geannuleerd.");
        }
    }

    // ──────────────────────────────────────────────
    //  8. Lage voorraad controleren
    // ──────────────────────────────────────────────

    private static void checkLageVoorraad() {
        List<Product> lageLevels = service.getProductenMetLageVoorraad();
        drukSchermTitel("LAGE VOORRAAD — " + lageLevels.size() + " alert(s)");

        if (lageLevels.isEmpty()) {
            toonSucces("Alle producten hebben voldoende voorraad.");
            return;
        }

        System.out.println("╔" + LIJN_H + "╗");
        System.out.printf("║  %-14s %-22s %-10s %-10s %-10s %-10s  ║%n",
                "Artikelnummer", "Naam", "Voorraad", "Minimum", "Tekort", "Waarde");
        System.out.println("╠" + LIJN_H + "╣");
        for (Product p : lageLevels) {
            System.out.printf("║  %-14s %-22s %-10d %-10d %-10d %-10s  ║%n",
                    p.getArtikelNummer(),
                    afkappen(p.getNaam(), 22),
                    p.getVoorraad(),
                    p.getMinimumVoorraad(),
                    p.getTekort(),
                    String.format("€ %.2f", p.getVoorraadWaarde()));
        }
        System.out.println("╚" + LIJN_H + "╝");
    }

    // ──────────────────────────────────────────────
    //  Opslaan
    // ──────────────────────────────────────────────

    private static void opslaan() {
        try {
            service.slaanOp();
        } catch (IOException e) {
            toonFout("Kon data niet opslaan: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  UI-hulpmethodes
    // ──────────────────────────────────────────────

    private static void toonProductTabelHeader() {
        System.out.println("╔" + LIJN_H + "╗");
        System.out.printf("║  %-14s %-22s %-10s %-10s %-10s %-10s  ║%n",
                "Artikelnummer", "Naam", "Voorraad", "Minimum", "Prijs", "Status");
        System.out.println("╠" + LIJN_H + "╣");
    }

    private static void toonProductRegel(Product p) {
        String status = p.isLageVoorraad() ? "! LAAG" : "OK";
        System.out.printf("║  %-14s %-22s %-10d %-10d %-10s %-10s  ║%n",
                p.getArtikelNummer(),
                afkappen(p.getNaam(), 22),
                p.getVoorraad(),
                p.getMinimumVoorraad(),
                String.format("€ %.2f", p.getPrijs()),
                status);
    }

    private static void toonProductKaart(Product p) {
        System.out.println("  " + LIJN_M);
        System.out.printf("  %-20s %s%n",  "Artikelnummer:",  p.getArtikelNummer());
        System.out.printf("  %-20s %s%n",  "Naam:",           p.getNaam());
        System.out.printf("  %-20s %d stuks%n", "Voorraad:",  p.getVoorraad());
        System.out.printf("  %-20s %d stuks%n", "Minimum:",   p.getMinimumVoorraad());
        System.out.printf("  %-20s € %.2f%n",   "Stukprijs:", p.getPrijs());
        System.out.printf("  %-20s € %.2f%n",   "Voorraadwaarde:", p.getVoorraadWaarde());
        System.out.println("  " + LIJN_M);
    }

    private static void toonLageVoorraadWaarschuwing(Product p) {
        System.out.println();
        System.out.println("  ┌─ ! LAGE VOORRAAD WAARSCHUWING " + "─".repeat(48) + "┐");
        System.out.printf("  │  %-76s  │%n", "Product  : " + p.getNaam() + " (" + p.getArtikelNummer() + ")");
        System.out.printf("  │  %-76s  │%n",
                "Voorraad : " + p.getVoorraad() + " stuks  |  Minimum: " + p.getMinimumVoorraad()
                        + " stuks  |  Tekort: " + p.getTekort() + " stuks");
        System.out.println("  └" + "─".repeat(80) + "┘");
    }

    private static void drukSchermTitel(String titel) {
        System.out.println("┌" + LIJN_M + "┐");
        System.out.printf("│  %-78s  │%n", "  " + titel);
        System.out.println("└" + LIJN_M + "┘");
    }

    private static void toonSucces(String bericht) {
        System.out.println("  [ OK ]  " + bericht);
    }

    private static void toonFout(String bericht) {
        System.out.println("  [FOUT]  " + bericht);
    }

    private static void toonMelding(String type, String bericht) {
        System.out.println("  [" + type + "]  " + bericht);
    }

    private static void drukKader(String tekst, boolean dubbel) {
        String h = dubbel ? "═" : "─";
        String l = h.repeat(BREEDTE);
        System.out.println((dubbel ? "╔" : "┌") + l + (dubbel ? "╗" : "┐"));
        System.out.printf((dubbel ? "║" : "│") + "  %-78s  " + (dubbel ? "║" : "│") + "%n", tekst);
        System.out.println((dubbel ? "╚" : "└") + l + (dubbel ? "╝" : "┘"));
    }

    private static void drukCenteredRegel(String tekst, boolean bold) {
        int ruimte = BREEDTE - tekst.length();
        int links  = ruimte / 2;
        int rechts = ruimte - links;
        String gevuld = " ".repeat(links) + tekst + " ".repeat(rechts);
        System.out.println("║" + gevuld + "║");
    }

    private static void drukMenuOptie(String nummer, String omschrijving) {
        System.out.printf("║    [%s]  %-73s║%n", nummer, omschrijving);
    }

    private static String afkappen(String tekst, int maxLengte) {
        if (tekst == null) return "";
        return tekst.length() <= maxLengte ? tekst : tekst.substring(0, maxLengte - 1) + "…";
    }

    // ──────────────────────────────────────────────
    //  Input-helpers
    // ──────────────────────────────────────────────

    private static int leesInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String invoer = scanner.nextLine().trim().replace(",", ".");
            try {
                return Integer.parseInt(invoer);
            } catch (NumberFormatException e) {
                toonFout("Voer een geldig geheel getal in.");
            }
        }
    }

    private static double leesDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String invoer = scanner.nextLine().trim().replace(",", ".");
            try {
                return Double.parseDouble(invoer);
            } catch (NumberFormatException e) {
                toonFout("Voer een geldig decimaal getal in (bijv. 9.99).");
            }
        }
    }
}
