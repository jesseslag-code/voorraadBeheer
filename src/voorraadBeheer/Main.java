package voorraadBeheer;

import javax.swing.SwingUtilities;

/**
 * Startpunt van de applicatie.
 * Start de Swing GUI op de Event Dispatch Thread (EDT),
 * zoals vereist door de Swing thread-veiligheidsregels.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InventoryService service = new InventoryService();
            InventoryGUI gui = new InventoryGUI(service);
            gui.setVisible(true);
        });
    }
}

