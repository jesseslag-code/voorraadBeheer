package voorraadBeheer;

import java.util.List;
import java.util.Scanner;

/**
 * Hoofdklasse met het consolemenu voor het voorraadbeheersysteem.
 * Alle bedrijfslogica wordt afgehandeld via {@link InventoryService}.
 */
public class Main {
    private static final InventoryService service = new InventoryService();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        service.laadVoorbeeldData();
        System.out.println("✓ Voorbeelddata geladen!\n");

        boolean actief = true;
        while (actief) {
            toonMenu();
            int keuze = scanner.nextInt();
            scanner.nextLine(); // Buffer legen

            switch (keuze) {
                case 1:
                    toonAlleProducten();
                    break;
                case 2:
                    afboekenProduct();
                    break;
                case 3:
                    toevoegenProduct();
                    break;
                case 4:
                    productWijzigen();
                    break;
                case 5:
                    verwijderProduct();
                    break;
                case 6:
                    checkLageVoorraad();
                    break;
                case 7:
                    actief = false;
                    System.out.println("Systeem afgesloten. Tot ziens!");
                    break;
                default:
                    System.out.println("Ongeldige keuze. Probeer opnieuw.");
            }
            System.out.println();
        }
        scanner.close();
    }

    // ──────────────────────────────────────────────
    //  Menu
    // ──────────────────────────────────────────────

    private static void toonMenu() {
        System.out.println("=== VOORRAADBEHEER SYSTEEM ===");
        System.out.println("1. Alle producten weergeven");
        System.out.println("2. Product afboeken (voorraad verminderen)");
        System.out.println("3. Nieuw product toevoegen");
        System.out.println("4. Product wijzigen");
        System.out.println("5. Product verwijderen");
        System.out.println("6. Producten met lage voorraad controleren");
        System.out.println("7. Systeem afsluiten");
        System.out.print("Maak uw keuze (1-7): ");
    }

    // ──────────────────────────────────────────────
    //  1. Alle producten weergeven
    // ──────────────────────────────────────────────

    private static void toonAlleProducten() {
        List<Product> producten = service.getAlleProducten();
        if (producten.isEmpty()) {
            System.out.println("Geen producten in het systeem.");
            return;
        }

        System.out.println("\n=== ALLE PRODUCTEN ===");
        printTabelHeader();
        for (Product product : producten) {
            String status = product.isLageVoorraad() ? "⚠️ LAAG" : "✓ OK";
            System.out.printf("%-15s %-20s %-12d %-12d %-10s%n",
                    product.getArtikelNummer(),
                    product.getNaam(),
                    product.getVoorraad(),
                    product.getMinimumVoorraad(),
                    status);
        }
        printLijn();
    }

    // ──────────────────────────────────────────────
    //  2. Afboeken
    // ──────────────────────────────────────────────

    private static void afboekenProduct() {
        System.out.print("Voer artikelnummer in: ");
        String artikelNummer = scanner.nextLine().trim();

        System.out.print("Voer aantal in om af te boeken: ");
        int aantal = scanner.nextInt();
        scanner.nextLine();

        try {
            Product product = service.boekAf(artikelNummer, aantal);
            System.out.println("✓ Afboeking succesvol!");
            System.out.println("  Product: " + product.getNaam());
            System.out.println("  Afgeboekt: " + aantal + " stuks");
            System.out.println("  Nieuwe voorraad: " + product.getVoorraad());

            if (product.isLageVoorraad()) {
                toonLageVoorraadMelding(product);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  3. Toevoegen
    // ──────────────────────────────────────────────

    private static void toevoegenProduct() {
        System.out.print("Voer artikelnummer in: ");
        String artikelNummer = scanner.nextLine().trim();

        System.out.print("Voer productnaam in: ");
        String naam = scanner.nextLine().trim();

        System.out.print("Voer voorraad in: ");
        int voorraad = scanner.nextInt();

        System.out.print("Voer minimum voorraad in: ");
        int minimumVoorraad = scanner.nextInt();
        scanner.nextLine();

        try {
            Product product = new Product(naam, artikelNummer, voorraad, minimumVoorraad);
            if (service.voegProductToe(product)) {
                System.out.println("✓ Product toegevoegd!");
                if (product.isLageVoorraad()) {
                    toonLageVoorraadMelding(product);
                }
            } else {
                System.out.println("❌ Dit artikelnummer bestaat al!");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  4. Wijzigen
    // ──────────────────────────────────────────────

    private static void productWijzigen() {
        System.out.print("Voer artikelnummer in: ");
        String artikelNummer = scanner.nextLine().trim();

        Product product = service.zoekProduct(artikelNummer);
        if (product == null) {
            System.out.println("❌ Product niet gevonden!");
            return;
        }

        System.out.println("\n=== HUIDIGE GEGEVENS ===");
        System.out.println("Naam: " + product.getNaam());
        System.out.println("Voorraad: " + product.getVoorraad());
        System.out.println("Minimum voorraad: " + product.getMinimumVoorraad());

        System.out.println("\nWat wilt u wijzigen?");
        System.out.println("1. Naam");
        System.out.println("2. Voorraad");
        System.out.println("3. Minimum voorraad");
        System.out.print("Keuze (1-3): ");

        int keuze = scanner.nextInt();
        scanner.nextLine();

        try {
            switch (keuze) {
                case 1:
                    System.out.print("Voer nieuwe naam in: ");
                    product.setNaam(scanner.nextLine().trim());
                    System.out.println("✓ Naam gewijzigd!");
                    break;
                case 2:
                    System.out.print("Voer nieuwe voorraad in: ");
                    product.setVoorraad(scanner.nextInt());
                    scanner.nextLine();
                    System.out.println("✓ Voorraad gewijzigd!");
                    break;
                case 3:
                    System.out.print("Voer nieuw minimum in: ");
                    product.setMinimumVoorraad(scanner.nextInt());
                    scanner.nextLine();
                    System.out.println("✓ Minimum gewijzigd!");
                    break;
                default:
                    System.out.println("❌ Ongeldige keuze!");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  5. Verwijderen (NIEUW)
    // ──────────────────────────────────────────────

    private static void verwijderProduct() {
        System.out.print("Voer artikelnummer in van het te verwijderen product: ");
        String artikelNummer = scanner.nextLine().trim();

        Product product = service.zoekProduct(artikelNummer);
        if (product == null) {
            System.out.println("❌ Product niet gevonden!");
            return;
        }

        System.out.println("Product gevonden: " + product.getNaam()
                + " (voorraad: " + product.getVoorraad() + ")");
        System.out.print("Weet u zeker dat u dit product wilt verwijderen? (j/n): ");
        String bevestiging = scanner.nextLine().trim().toLowerCase();

        if (bevestiging.equals("j")) {
            service.verwijderProduct(artikelNummer);
            System.out.println("✓ Product '" + product.getNaam() + "' is verwijderd.");
        } else {
            System.out.println("Verwijdering geannuleerd.");
        }
    }

    // ──────────────────────────────────────────────
    //  6. Lage voorraad controleren
    // ──────────────────────────────────────────────

    private static void checkLageVoorraad() {
        System.out.println("\n=== PRODUCTEN MET LAGE VOORRAAD ===");

        List<Product> lageLevels = service.getProductenMetLageVoorraad();
        if (lageLevels.isEmpty()) {
            System.out.println("✓ Alle producten hebben voldoende voorraad!");
            return;
        }

        System.out.println("─".repeat(80));
        System.out.printf("%-15s %-20s %-12s %-12s %-15s%n",
                "Artikelnummer", "Naam", "Voorraad", "Minimum", "Tekort");
        System.out.println("─".repeat(80));

        for (Product product : lageLevels) {
            System.out.printf("%-15s %-20s %-12d %-12d %-15d%n",
                    product.getArtikelNummer(),
                    product.getNaam(),
                    product.getVoorraad(),
                    product.getMinimumVoorraad(),
                    product.getTekort());
        }
        printLijn();
    }

    // ──────────────────────────────────────────────
    //  Hulpmethodes
    // ──────────────────────────────────────────────

    /**
     * Toont een waarschuwingsmelding wanneer de voorraad onder het minimum zakt.
     */
    private static void toonLageVoorraadMelding(Product product) {
        System.out.println("\n" + "⚠️".repeat(20));
        System.out.println("🚨 WAARSCHUWING: LAGE VOORRAAD GEDETECTEERD!");
        System.out.println("⚠️".repeat(20));
        System.out.println("Product: " + product.getNaam());
        System.out.println("Artikelnummer: " + product.getArtikelNummer());
        System.out.println("Huidige voorraad: " + product.getVoorraad() + " stuks");
        System.out.println("Minimale voorraad: " + product.getMinimumVoorraad() + " stuks");
        System.out.println("Tekort: " + product.getTekort() + " stuks");
        System.out.println("⚠️".repeat(20) + "\n");
    }

    private static void printTabelHeader() {
        printLijn();
        System.out.printf("%-15s %-20s %-12s %-12s %-10s%n",
                "Artikelnummer", "Naam", "Voorraad", "Minimum", "Status");
        printLijn();
    }

    private static void printLijn() {
        System.out.println("─".repeat(80));
    }
}
