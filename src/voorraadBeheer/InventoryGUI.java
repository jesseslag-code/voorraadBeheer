package voorraadBeheer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Hoofdvenster van het voorraadbeheer GUI.
 *
 * Lay-out (BorderLayout):
 *   NORTH  – Invoerpaneel met tekstvelden voor productgegevens
 *   CENTER – Scrollbare JTable met alle producten
 *   SOUTH  – Knoppenbalk + label met de totale voorraadwaarde
 *
 * Gebruik:
 *   - Klik een rij aan om de gegevens in de invoervelden te laden.
 *   - Pas de velden aan en klik op "Bijwerken" om op te slaan.
 *   - Gebruik de overige knoppen voor toevoegen / verwijderen / in- en uitboeken.
 */
public class InventoryGUI extends JFrame {

    // ─── Constanten ───────────────────────────────────────────────────────────

    /** Bestandsnaam voor CSV-persistentie. */
    private static final String CSV_BESTAND = "voorraad.csv";

    /** Kolomnamen van de tabel. */
    private static final String[] KOLOMMEN = {
        "Artikelnummer", "Naam", "Voorraad", "Minimum", "Prijs (€)", "Status"
    };

    // ─── Services ─────────────────────────────────────────────────────────────
    private final InventoryService service;

    // ─── Tabel ────────────────────────────────────────────────────────────────
    private JTable tabel;
    private DefaultTableModel tabelModel;

    // ─── Invoervelden ─────────────────────────────────────────────────────────
    private JTextField veldArtikelNummer;
    private JTextField veldNaam;
    private JTextField veldVoorraad;
    private JTextField veldMinimum;
    private JTextField veldPrijs;

    // ─── Statusbalk ───────────────────────────────────────────────────────────
    private JLabel lblTotaleWaarde;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Maakt het GUI-venster aan en laadt de begindata.
     *
     * @param service De te gebruiken {@link InventoryService}
     */
    public InventoryGUI(InventoryService service) {
        this.service = service;

        setTitle("Voorraadbeheer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(970, 580);
        setMinimumSize(new Dimension(800, 450));
        setLocationRelativeTo(null); // Gecentreerd op het scherm

        initComponents();
        laadBegindata();
        verversTable();
    }

    // ─── Initialisatie ────────────────────────────────────────────────────────

    /**
     * Bouwt alle GUI-componenten op en voegt ze toe aan het frame.
     */
    private void initComponents() {
        // Gebruik een wrapper-paneel zodat we een rand om de inhoud kunnen zetten
        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(wrapper);

        wrapper.add(maakInvoerPanel(),  BorderLayout.NORTH);
        wrapper.add(maakTabelPanel(),   BorderLayout.CENTER);
        wrapper.add(maakOnderPanel(),   BorderLayout.SOUTH);
    }

    /**
     * Maakt het invoerpaneel met labels en tekstvelden voor alle productgegevens.
     * De velden worden horizontaal naast elkaar weergegeven met GridBagLayout.
     */
    private JPanel maakInvoerPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Productgegevens"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(5, 8, 5, 8);
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.gridy   = 0;

        // Initialiseer tekstvelden
        veldArtikelNummer = new JTextField(10);
        veldNaam          = new JTextField(18);
        veldVoorraad      = new JTextField(7);
        veldMinimum       = new JTextField(7);
        veldPrijs         = new JTextField(8);

        // Voeg elk label-veld paar toe aan het grid
        voegVeldToe(panel, gbc, "Artikelnummer:", veldArtikelNummer, 0);
        voegVeldToe(panel, gbc, "Naam:",          veldNaam,          1);
        voegVeldToe(panel, gbc, "Voorraad:",      veldVoorraad,      2);
        voegVeldToe(panel, gbc, "Minimum:",       veldMinimum,       3);
        voegVeldToe(panel, gbc, "Prijs (€):",     veldPrijs,         4);

        return panel;
    }

    /**
     * Voegt één label-veld paar toe op de opgegeven kolompositie in het GridBagLayout.
     *
     * @param panel    Het doelpaneel
     * @param gbc      De lay-outrestricties (worden lokaal aangepast)
     * @param label    Tekst van het label
     * @param veld     Het tekstveld
     * @param kolom    Positie in de rij (0 = meest links)
     */
    private void voegVeldToe(JPanel panel, GridBagConstraints gbc,
                              String label, JTextField veld, int kolom) {
        // Label staat in een even kolom (niet uitrekbaar)
        gbc.gridx   = kolom * 2;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        // Veld staat in de oneven kolom ernaast (neemt beschikbare ruimte)
        gbc.gridx   = kolom * 2 + 1;
        gbc.weightx = 1.0;
        panel.add(veld, gbc);
    }

    /**
     * Maakt het tabelpaneel: een niet-bewerkbare JTable in een JScrollPane.
     * Rijen zijn selecteerbaar; een klik vult automatisch de invoervelden.
     */
    private JScrollPane maakTabelPanel() {
        // Tabelmodel: overschrijf isCellEditable zodat rijen niet direct bewerkbaar zijn
        tabelModel = new DefaultTableModel(KOLOMMEN, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Bewerken gaat via de invoervelden
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Correcte typen zodat sorteren goed werkt
                switch (columnIndex) {
                    case 2: case 3: return Integer.class;
                    case 4:         return Double.class;
                    default:        return String.class;
                }
            }
        };

        tabel = new JTable(tabelModel);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.setRowHeight(24);
        tabel.setAutoCreateRowSorter(true);           // Klikmogelijkheid op kolomkoppen
        tabel.getTableHeader().setReorderingAllowed(false);

        // Stel kolombreedten in
        int[] breedten = {110, 200, 80, 80, 90, 80};
        for (int i = 0; i < breedten.length; i++) {
            tabel.getColumnModel().getColumn(i).setPreferredWidth(breedten[i]);
        }

        // Prijskolom: rendeer als "€ 0,00" (europese opmaak)
        tabel.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public void setValue(Object value) {
                setText(value instanceof Double
                        ? String.format("€ %.2f", (Double) value)
                        : String.valueOf(value));
            }
        });

        // Luisteraar: geselecteerde rij vult de invoervelden
        tabel.getSelectionModel().addListSelectionListener(this::rijGeselecteerd);

        return new JScrollPane(tabel);
    }

    /**
     * Maakt het onderste paneel met de actieknoppen en het totaalwaarde-label.
     */
    private JPanel maakOnderPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // ── Knoppen ──────────────────────────────────────────────────────────
        JPanel knoppenPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        knoppenPanel.add(maakKnop("Toevoegen",     this::actieToevoegen));
        knoppenPanel.add(maakKnop("Verwijderen",   this::actieVerwijderen));
        knoppenPanel.add(maakKnop("Bijwerken",     this::actieBijwerken));
        knoppenPanel.add(maakKnop("Inboeken (+)",  this::actieInboeken));
        knoppenPanel.add(maakKnop("Uitboeken (-)", this::actieUitboeken));
        knoppenPanel.add(maakKnop("Leegmaken",     e -> veldenLeegmaken()));
        panel.add(knoppenPanel, BorderLayout.CENTER);

        // ── Totale voorraadwaarde ─────────────────────────────────────────────
        lblTotaleWaarde = new JLabel("Totale voorraadwaarde: € 0,00");
        lblTotaleWaarde.setFont(lblTotaleWaarde.getFont().deriveFont(Font.BOLD, 13f));
        lblTotaleWaarde.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        panel.add(lblTotaleWaarde, BorderLayout.SOUTH);

        return panel;
    }

    /** Maakt een knop met de opgegeven tekst en actie-listener. */
    private JButton maakKnop(String tekst, java.awt.event.ActionListener actie) {
        JButton knop = new JButton(tekst);
        knop.addActionListener(actie);
        return knop;
    }

    // ─── Datalaad en tabelververs ──────────────────────────────────────────────

    /**
     * Laadt begindata: CSV als het bestand bestaat, anders voorbeelddata.
     */
    private void laadBegindata() {
        if (!service.laadVanCSV(CSV_BESTAND)) {
            service.laadVoorbeeldData();
            service.slaCSVOp(CSV_BESTAND);
        }
    }

    /**
     * Ververst de tabel volledig vanuit de service en werkt het totaalwaarde-label bij.
     */
    private void verversTable() {
        tabelModel.setRowCount(0); // Wis alle bestaande rijen

        for (Product p : service.getAlleProducten()) {
            String status = p.isLageVoorraad() ? "⚠ LAAG" : "✓ OK";
            tabelModel.addRow(new Object[]{
                p.getArtikelNummer(),
                p.getNaam(),
                p.getVoorraad(),
                p.getMinimumVoorraad(),
                p.getPrijs(),           // Opgeslagen als Double voor getColumnClass()
                status
            });
        }

        // Totale voorraadwaarde bijwerken
        double totaal = service.berekenTotaleWaarde();
        lblTotaleWaarde.setText(String.format("Totale voorraadwaarde: € %.2f", totaal));
    }

    /**
     * Vult de invoervelden met de gegevens van de geselecteerde tabelrij.
     * Wordt automatisch aangeroepen bij een rijselectie.
     */
    private void rijGeselecteerd(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return; // Negeer tussentijdse selectie-events

        // Zet modelrij-index om (nodig bij gesorteerde tabel)
        int viewRij = tabel.getSelectedRow();
        if (viewRij < 0) return;
        int modelRij = tabel.convertRowIndexToModel(viewRij);

        veldArtikelNummer.setText((String)  tabelModel.getValueAt(modelRij, 0));
        veldNaam.setText((String)           tabelModel.getValueAt(modelRij, 1));
        veldVoorraad.setText(String.valueOf( tabelModel.getValueAt(modelRij, 2)));
        veldMinimum.setText(String.valueOf(  tabelModel.getValueAt(modelRij, 3)));

        // Prijs is opgeslagen als Double; opmaak met twee decimalen
        Double prijs = (Double) tabelModel.getValueAt(modelRij, 4);
        veldPrijs.setText(String.format("%.2f", prijs));
    }

    /**
     * Wist alle invoervelden en heft de tabelselectie op.
     */
    private void veldenLeegmaken() {
        veldArtikelNummer.setText("");
        veldNaam.setText("");
        veldVoorraad.setText("");
        veldMinimum.setText("");
        veldPrijs.setText("");
        tabel.clearSelection();
    }

    // ─── Acties (knoppen) ─────────────────────────────────────────────────────

    /**
     * Voegt een nieuw product toe op basis van de waarden in de invoervelden.
     * Toont een foutmelding bij ontbrekende of ongeldige invoer.
     */
    private void actieToevoegen(ActionEvent e) {
        try {
            String artikelNummer = valideerNietLeeg(veldArtikelNummer, "Artikelnummer");
            String naam          = valideerNietLeeg(veldNaam,          "Naam");
            int    voorraad      = valideerGeheel(veldVoorraad,        "Voorraad");
            int    minimum       = valideerGeheel(veldMinimum,         "Minimum");
            double prijs         = valideerDecimaal(veldPrijs,         "Prijs");

            Product product = new Product(naam, artikelNummer, voorraad, minimum, prijs);

            if (service.voegProductToe(product)) {
                service.slaCSVOp(CSV_BESTAND);
                verversTable();
                veldenLeegmaken();
                toonSucces("Product '" + naam + "' is succesvol toegevoegd.");
                controleerLageVoorraad(product);
            } else {
                toonFout("Artikelnummer '" + artikelNummer + "' bestaat al!");
            }
        } catch (IllegalArgumentException ex) {
            toonFout(ex.getMessage());
        }
    }

    /**
     * Verwijdert het product dat geselecteerd is in de tabel.
     * Vraagt eerst om bevestiging via een JOptionPane-dialoog.
     */
    private void actieVerwijderen(ActionEvent e) {
        int viewRij = tabel.getSelectedRow();
        if (viewRij < 0) {
            toonFout("Selecteer eerst een product in de tabel.");
            return;
        }
        int modelRij = tabel.convertRowIndexToModel(viewRij);

        String artikelNummer = (String) tabelModel.getValueAt(modelRij, 0);
        String naam          = (String) tabelModel.getValueAt(modelRij, 1);

        int bevestiging = JOptionPane.showConfirmDialog(this,
                "Weet u zeker dat u '" + naam + "' wilt verwijderen?",
                "Verwijderen bevestigen",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (bevestiging == JOptionPane.YES_OPTION) {
            service.verwijderProduct(artikelNummer);
            service.slaCSVOp(CSV_BESTAND);
            verversTable();
            veldenLeegmaken();
            toonSucces("Product '" + naam + "' is verwijderd.");
        }
    }

    /**
     * Werkt het geselecteerde product bij met de waarden uit de invoervelden.
     * Het artikelnummer is het zoekkenmerk en kan niet worden gewijzigd.
     */
    private void actieBijwerken(ActionEvent e) {
        int viewRij = tabel.getSelectedRow();
        if (viewRij < 0) {
            toonFout("Selecteer eerst een product in de tabel.");
            return;
        }
        int modelRij = tabel.convertRowIndexToModel(viewRij);

        try {
            // Artikelnummer uit de tabel (niet uit het veld, want dat mag men niet wijzigen)
            String origArtikelNummer = (String) tabelModel.getValueAt(modelRij, 0);
            String naam    = valideerNietLeeg(veldNaam,     "Naam");
            int    voorraad = valideerGeheel(veldVoorraad,  "Voorraad");
            int    minimum  = valideerGeheel(veldMinimum,   "Minimum");
            double prijs    = valideerDecimaal(veldPrijs,   "Prijs");

            service.updateProduct(origArtikelNummer, naam, voorraad, minimum, prijs);
            service.slaCSVOp(CSV_BESTAND);
            verversTable();

            // Herselect dezelfde rij (rij-index kan na verversing verschoven zijn)
            selecteerRijMetArtikelNummer(origArtikelNummer);

            Product bijgewerkt = service.zoekProduct(origArtikelNummer);
            toonSucces("Product '" + naam + "' is bijgewerkt.");
            controleerLageVoorraad(bijgewerkt);
        } catch (IllegalArgumentException ex) {
            toonFout(ex.getMessage());
        }
    }

    /**
     * Boekt een aantal stuks in (verhoogt de voorraad) voor het geselecteerde product.
     * Het aantal wordt gevraagd via een invoerdialoog.
     */
    private void actieInboeken(ActionEvent e) {
        int viewRij = tabel.getSelectedRow();
        if (viewRij < 0) {
            toonFout("Selecteer eerst een product in de tabel.");
            return;
        }
        int modelRij = tabel.convertRowIndexToModel(viewRij);

        String artikelNummer = (String) tabelModel.getValueAt(modelRij, 0);
        String naam          = (String) tabelModel.getValueAt(modelRij, 1);

        String invoer = JOptionPane.showInputDialog(this,
                "Hoeveel stuks wilt u inboeken voor '" + naam + "'?",
                "Inboeken", JOptionPane.PLAIN_MESSAGE);
        if (invoer == null || invoer.trim().isEmpty()) return; // Geannuleerd

        try {
            int aantal = Integer.parseInt(invoer.trim());
            Product product = service.boekIn(artikelNummer, aantal);
            service.slaCSVOp(CSV_BESTAND);
            verversTable();
            selecteerRijMetArtikelNummer(artikelNummer);
            veldVoorraad.setText(String.valueOf(product.getVoorraad()));
            toonSucces(aantal + " stuks ingeboekt. Nieuwe voorraad: " + product.getVoorraad());
        } catch (NumberFormatException ex) {
            toonFout("Voer een geldig geheel getal in.");
        } catch (IllegalArgumentException ex) {
            toonFout(ex.getMessage());
        }
    }

    /**
     * Boekt een aantal stuks uit (verlaagt de voorraad) voor het geselecteerde product.
     * Het aantal wordt gevraagd via een invoerdialoog.
     * Toont een waarschuwing als de voorraad daarna onder het minimum zakt.
     */
    private void actieUitboeken(ActionEvent e) {
        int viewRij = tabel.getSelectedRow();
        if (viewRij < 0) {
            toonFout("Selecteer eerst een product in de tabel.");
            return;
        }
        int modelRij = tabel.convertRowIndexToModel(viewRij);

        String artikelNummer = (String) tabelModel.getValueAt(modelRij, 0);
        String naam          = (String) tabelModel.getValueAt(modelRij, 1);

        String invoer = JOptionPane.showInputDialog(this,
                "Hoeveel stuks wilt u uitboeken voor '" + naam + "'?",
                "Uitboeken", JOptionPane.PLAIN_MESSAGE);
        if (invoer == null || invoer.trim().isEmpty()) return; // Geannuleerd

        try {
            int aantal = Integer.parseInt(invoer.trim());
            Product product = service.boekAf(artikelNummer, aantal);
            service.slaCSVOp(CSV_BESTAND);
            verversTable();
            selecteerRijMetArtikelNummer(artikelNummer);
            veldVoorraad.setText(String.valueOf(product.getVoorraad()));
            toonSucces(aantal + " stuks uitgeboekt. Nieuwe voorraad: " + product.getVoorraad());
            controleerLageVoorraad(product);
        } catch (NumberFormatException ex) {
            toonFout("Voer een geldig geheel getal in.");
        } catch (IllegalArgumentException ex) {
            toonFout(ex.getMessage());
        }
    }

    // ─── Validatiehulpmethodes ────────────────────────────────────────────────

    /**
     * Controleert of een tekstveld niet leeg is.
     *
     * @param veld      Het te controleren tekstveld
     * @param veldNaam  Naam die in de foutmelding verschijnt
     * @return De getrimde tekst
     * @throws IllegalArgumentException als het veld leeg is
     */
    private String valideerNietLeeg(JTextField veld, String veldNaam) {
        String waarde = veld.getText().trim();
        if (waarde.isEmpty()) {
            throw new IllegalArgumentException(veldNaam + " mag niet leeg zijn.");
        }
        return waarde;
    }

    /**
     * Leest en valideert een geheel getal uit een tekstveld.
     *
     * @throws IllegalArgumentException als de inhoud geen geldig getal is
     */
    private int valideerGeheel(JTextField veld, String veldNaam) {
        try {
            return Integer.parseInt(veld.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(veldNaam + " moet een geheel getal zijn.");
        }
    }

    /**
     * Leest en valideert een decimaal getal uit een tekstveld.
     * Accepteert zowel punt als komma als decimaalscheidingsteken.
     *
     * @throws IllegalArgumentException als de inhoud geen geldig getal is
     */
    private double valideerDecimaal(JTextField veld, String veldNaam) {
        try {
            return Double.parseDouble(veld.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(veldNaam + " moet een geldig getal zijn (bijv. 9.99).");
        }
    }

    // ─── Meldingshulpmethodes ─────────────────────────────────────────────────

    /** Toont een foutmelding via JOptionPane. */
    private void toonFout(String bericht) {
        JOptionPane.showMessageDialog(this, bericht, "Fout", JOptionPane.ERROR_MESSAGE);
    }

    /** Toont een succesbericht via JOptionPane. */
    private void toonSucces(String bericht) {
        JOptionPane.showMessageDialog(this, bericht, "Succes", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Toont een waarschuwingsdialoog wanneer de voorraad van een product onder het minimum ligt.
     *
     * @param product Het product om te controleren (mag null zijn)
     */
    private void controleerLageVoorraad(Product product) {
        if (product != null && product.isLageVoorraad()) {
            JOptionPane.showMessageDialog(this,
                    String.format(
                        "⚠  WAARSCHUWING: Lage voorraad!%n%n" +
                        "Product:          %s (%s)%n" +
                        "Huidige voorraad: %d stuks%n" +
                        "Minimum voorraad: %d stuks%n" +
                        "Tekort:           %d stuks",
                        product.getNaam(),
                        product.getArtikelNummer(),
                        product.getVoorraad(),
                        product.getMinimumVoorraad(),
                        product.getTekort()),
                    "Lage voorraad",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // ─── Hulpmethode tabel ────────────────────────────────────────────────────

    /**
     * Selecteert de tabelrij die overeenkomt met het opgegeven artikelnummer.
     * Werkt ook bij gesorteerde tabellen door view- en modelindexen om te zetten.
     *
     * @param artikelNummer Het te zoeken artikelnummer
     */
    private void selecteerRijMetArtikelNummer(String artikelNummer) {
        for (int modelRij = 0; modelRij < tabelModel.getRowCount(); modelRij++) {
            if (artikelNummer.equals(tabelModel.getValueAt(modelRij, 0))) {
                int viewRij = tabel.convertRowIndexToView(modelRij);
                tabel.setRowSelectionInterval(viewRij, viewRij);
                tabel.scrollRectToVisible(tabel.getCellRect(viewRij, 0, true));
                return;
            }
        }
    }
}
