package voorraadBeheermain;

import java.util.*;

import voorraadBeheer.Insert;

public class Main {
    private static List<Insert> voorraadLijst = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Voorbeelddata laden
        laadVoorbeeldData();

        boolean actief = true;
        while (actief) {
            toonMenu();
            int keuze = scanner.nextInt();
            scanner.nextLine(); // Buffer legen

            switch (keuze) {
                case 1:
                    toonAlleArtikelen();
                    break;
                case 2:
                    afboekenArtikel();
                    break;
                case 3:
                    toevoegenArtikel();
                    break;
                case 4:
                    artikelWijzigen();
                    break;
                case 5:
                    checkLageLevels();
                    break;
                case 6:
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

    /**
     * Toont het hoofdmenu
     */
    private static void toonMenu() {
        System.out.println("=== VOORRAADBEHEER SYSTEEM ===");
        System.out.println("1. Alle artikelen weergeven");
        System.out.println("2. Artikel afboeken (voorraad verminderen)");
        System.out.println("3. Nieuw artikel toevoegen");
        System.out.println("4. Artikel wijzigen");
        System.out.println("5. Artikelen met lage voorraad controleren");
        System.out.println("6. Systeem afsluiten");
        System.out.print("Maak uw keuze (1-6): ");
    }

    /**
     * Toont alle artikelen in de voorraad
     */
    private static void toonAlleArtikelen() {
        if (voorraadLijst.isEmpty()) {
            System.out.println("Geen artikelen in het systeem.");
            return;
        }

        System.out.println("\n=== ALLE ARTIKELEN ===");
        System.out.println("─".repeat(80));
        System.out.printf("%-15s %-20s %-12s %-12s %-10s%n",
                "Artikelnummer", "Naam", "Voorraad", "Minimum", "Status");
        System.out.println("─".repeat(80));

        for (Insert artikel : voorraadLijst) {
            String status = artikel.getVoorraad() < artikel.getMinimumVoorraad() ? "⚠️ LAAG" : "✓ OK";
            System.out.printf("%-15s %-20s %-12d %-12d %-10s%n",
                    artikel.getArtikelNummer(),
                    artikel.getNaam(),
                    artikel.getVoorraad(),
                    artikel.getMinimumVoorraad(),
                    status);
        }
        System.out.println("─".repeat(80));
    }

    /**
     * Boekt een artikel af (vermindert de voorraad)
     */
    private static void afboekenArtikel() {
        System.out.print("Voer artikelnummer in: ");
        String artikelNummer = scanner.nextLine().trim();

        Insert artikel = zoekArtikel(artikelNummer);
        if (artikel == null) {
            System.out.println("❌ Artikel niet gevonden!");
            return;
        }

        System.out.print("Voer aantal in om af te boeken: ");
        int aantal = scanner.nextInt();
        scanner.nextLine();

        if (aantal <= 0) {
            System.out.println("❌ Aantal moet groter zijn dan 0!");
            return;
        }

        if (aantal > artikel.getVoorraad()) {
            System.out.println("❌ Onvoldoende voorraad! Beschikbaar: " + artikel.getVoorraad());
            return;
        }

        // Afboeking uitvoeren
        int nieuweVoorraad = artikel.getVoorraad() - aantal;
        artikel.setVoorraad(nieuweVoorraad);

        System.out.println("✓ Afboeking succesvol!");
        System.out.println("  Artikel: " + artikel.getNaam());
        System.out.println("  Afgecodeerd: " + aantal + " stuks");
        System.out.println("  Nieuwe voorraad: " + nieuweVoorraad);

        // Controleer of voorraad onder minimum zakt
        if (nieuweVoorraad < artikel.getMinimumVoorraad()) {
            geefMelding(artikel);
        }
    }

    /**
     * Voegt een nieuw artikel toe
     */
    private static void toevoegenArtikel() {
        System.out.print("Voer artikelnummer in: ");
        String artikelNummer = scanner.nextLine().trim();

        // Controleer of artikel al bestaat
        if (zoekArtikel(artikelNummer) != null) {
            System.out.println("❌ Dit artikelnummer bestaat al!");
            return;
        }

        System.out.print("Voer artikelnaam in: ");
        String naam = scanner.nextLine().trim();

        System.out.print("Voer voorraad in: ");
        int voorraad = scanner.nextInt();

        System.out.print("Voer minimum voorraad in: ");
        int minimumVoorraad = scanner.nextInt();
        scanner.nextLine();

        if (voorraad < 0 || minimumVoorraad < 0) {
            System.out.println("❌ Voorraad kan niet negatief zijn!");
            return;
        }

        Insert nieuwartikel = new Insert(naam, artikelNummer, voorraad, minimumVoorraad);
        voorraadLijst.add(nieuwartikel);

        System.out.println("✓ Artikel toegevoegd!");
        if (voorraad < minimumVoorraad) {
            geefMelding(nieuwartikel);
        }
    }

    /**
     * Wijzigt een bestaand artikel
     */
    private static void artikelWijzigen() {
        System.out.print("Voer artikelnummer in: ");
        String artikelNummer = scanner.nextLine().trim();

        Insert artikel = zoekArtikel(artikelNummer);
        if (artikel == null) {
            System.out.println("❌ Artikel niet gevonden!");
            return;
        }

        System.out.println("\n=== HUIDIGE GEGEVENS ===");
        System.out.println("Naam: " + artikel.getNaam());
        System.out.println("Voorraad: " + artikel.getVoorraad());
        System.out.println("Minimum voorraad: " + artikel.getMinimumVoorraad());

        System.out.println("\nWat wilt u wijzigen?");
        System.out.println("1. Naam");
        System.out.println("2. Voorraad");
        System.out.println("3. Minimum voorraad");
        System.out.print("Keuze (1-3): ");

        int keuze = scanner.nextInt();
        scanner.nextLine();

        switch (keuze) {
            case 1:
                System.out.print("Voer nieuwe naam in: ");
                artikel.setNaam(scanner.nextLine().trim());
                System.out.println("✓ Naam gewijzigd!");
                break;
            case 2:
                System.out.print("Voer nieuwe voorraad in: ");
                artikel.setVoorraad(scanner.nextInt());
                scanner.nextLine();
                System.out.println("✓ Voorraad gewijzigd!");
                break;
            case 3:
                System.out.print("Voer nieuw minimum in: ");
                artikel.setMinimumVoorraad(scanner.nextInt());
                scanner.nextLine();
                System.out.println("✓ Minimum gewijzigd!");
                break;
            default:
                System.out.println("❌ Ongeldige keuze!");
        }
    }

    /**
     * Controleert alle artikelen op lage voorraden
     */
    private static void checkLageLevels() {
        System.out.println("\n=== ARTIKELEN MET LAGE VOORRAAD ===");

        List<Insert> lageLevels = new ArrayList<>();
        for (Insert artikel : voorraadLijst) {
            if (artikel.getVoorraad() < artikel.getMinimumVoorraad()) {
                lageLevels.add(artikel);
            }
        }

        if (lageLevels.isEmpty()) {
            System.out.println("✓ Alle artikelen hebben voldoende voorraad!");
            return;
        }

        System.out.println("─".repeat(80));
        System.out.printf("%-15s %-20s %-12s %-12s %-15s%n",
                "Artikelnummer", "Naam", "Voorraad", "Minimum", "Tekort");
        System.out.println("─".repeat(80));

        for (Insert artikel : lageLevels) {
            int tekort = artikel.getMinimumVoorraad() - artikel.getVoorraad();
            System.out.printf("%-15s %-20s %-12d %-12d %-15d%n",
                    artikel.getArtikelNummer(),
                    artikel.getNaam(),
                    artikel.getVoorraad(),
                    artikel.getMinimumVoorraad(),
                    tekort);
        }
        System.out.println("─".repeat(80));
    }

    /**
     * Geeft een melding wanneer voorraad onder minimum zakt
     */
    private static void geefMelding(Insert artikel) {
        System.out.println("\n" + "⚠️".repeat(20));
        System.out.println("🚨 WAARSCHUWING: LAGE VOORRAAD GEDETECTEERD!");
        System.out.println("⚠️".repeat(20));
        System.out.println("Artikel: " + artikel.getNaam());
        System.out.println("Artikelnummer: " + artikel.getArtikelNummer());
        System.out.println("Huidige voorraad: " + artikel.getVoorraad() + " stuks");
        System.out.println("Minimale voorraad: " + artikel.getMinimumVoorraad() + " stuks");
        System.out.println("Tekort: " + (artikel.getMinimumVoorraad() - artikel.getVoorraad()) + " stuks");
        System.out.println("⚠️".repeat(20) + "\n");
    }

    /**
     * Zoekt een artikel op basis van artikelnummer
     */
    private static Insert zoekArtikel(String artikelNummer) {
        for (Insert artikel : voorraadLijst) {
            if (artikel.getArtikelNummer().equalsIgnoreCase(artikelNummer)) {
                return artikel;
            }
        }
        return null;
    }

    /**
     * Laadt voorbeelddata in het systeem
     */
    private static void laadVoorbeeldData() {
        voorraadLijst.add(new Insert("Laptoptas", "ART001", 5, 10));
        voorraadLijst.add(new Insert("USB-C Kabel", "ART002", 25, 20));
        voorraadLijst.add(new Insert("Draadloze Muis", "ART003", 8, 15));
        voorraadLijst.add(new Insert("Toetsenbord", "ART004", 12, 8));
        voorraadLijst.add(new Insert("Monitor 24\"", "ART005", 3, 5));

        System.out.println("✓ Voorbeelddata geladen!\n");
    }
}
