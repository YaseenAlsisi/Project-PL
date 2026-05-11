import javax.swing.*;
import java.util.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserModule {

    // Username & Password — mutable so Change Password/Username panel can update it
    String userUsername = "user";
    String userPassword = "1111";

    // Document model — history stored in DefaultListModel for Swing use
    DefaultListModel<String> historyListModel = new DefaultListModel<>();

    // Keep plain ArrayList for compatibility
    ArrayList<String> history = new ArrayList<>();

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public UserModule() {
        loadCredentials();
        loadHistory();
    }

    public void login(String user) {
        String entry = "[" + LocalDateTime.now().format(DT) + "]  LOGIN  →  " + user;
        addEntry(entry);
        saveHistory();
    }

    public void addPurchase(String product, int qty, double total) {
        String entry = String.format("[%s]  BUY  →  %-20s  x%-4d  =  $%.2f",
                LocalDateTime.now().format(DT), product, qty, total);
        addEntry(entry);
        saveHistory();

        // GUI receipt dialog
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Main.CARD);
        panel.setBorder(new javax.swing.border.EmptyBorder(20,30,20,30));

        JLabel h = new JLabel("✔  PURCHASE INVOICE", SwingConstants.CENTER);
        h.setFont(Main.FONT_BIG);
        h.setForeground(Main.ACCENT);
        h.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        JLabel l1 = makeLabel("Product  :  " + product);
        JLabel l2 = makeLabel("Quantity :  " + qty);
        JLabel l3 = makeLabel(String.format("Total    :  $%.2f", total));

        panel.add(h);
        panel.add(Box.createVerticalStrut(14));
        panel.add(l1); panel.add(Box.createVerticalStrut(6));
        panel.add(l2); panel.add(Box.createVerticalStrut(6));
        panel.add(l3);
    }

    private void addEntry(String entry) {
        history.add(entry);
        historyListModel.addElement(entry);
    }

    // ── Save history to human-readable file ───────────────────────────────
    public void saveHistory() {
        try {
            PrintWriter w = new PrintWriter("sales_history.txt");
            w.println("================================================");
            w.println("          SAUDI MARKET — SALES & ACTIVITY LOG");
            w.println("================================================");
            w.println("  Total entries: " + history.size());
            w.println("================================================");
            w.println();
            for (String h : history) {
                w.println("  " + h);
            }
            w.println();
            w.println("================================================");
            w.println("  END OF LOG");
            w.println("================================================");
            w.close();

            // machine-readable copy
            PrintWriter raw = new PrintWriter("sales_history_data.txt");
            for (String h : history) raw.println(h);
            raw.close();
        } catch (Exception e) { System.out.println("Error saving history"); }
    }

    public void loadHistory() {
        try {
            File f = new File("sales_history_data.txt");
            if (!f.exists()) return;
            Scanner sc = new Scanner(f);
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (!line.isEmpty()) addEntry(line);
            }
            sc.close();
        } catch (Exception e) { System.out.println("Error loading history"); }
    }

    // ── Save/load credentials ─────────────────────────────────────────────
    public void saveCredentials() {
        try {
            PrintWriter w = new PrintWriter("user_config.txt");
            w.println("================================================");
            w.println("          SAUDI MARKET — USER CREDENTIALS");
            w.println("================================================");
            w.println("  Username : " + userUsername);
            w.println("  Password : " + userPassword);
            w.println("================================================");
            w.close();
            PrintWriter raw = new PrintWriter("user_config_data.txt");
            raw.println(userUsername + "|" + userPassword);
            raw.close();
        } catch (Exception e) { System.out.println("Error saving user credentials"); }
    }

    public void loadCredentials() {
        try {
            File f = new File("user_config_data.txt");
            if (!f.exists()) return;
            Scanner sc = new Scanner(f);
            if (sc.hasNextLine()) {
                String[] d = sc.nextLine().split("\\|");
                if (d.length >= 2) { userUsername = d[0].trim(); userPassword = d[1].trim(); }
            }
            sc.close();
        } catch (Exception e) { System.out.println("Error loading user credentials"); }
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Main.FONT_SM);
        l.setForeground(Main.FG);
        l.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        return l;
    }

    public void showAvailableProducts(ArrayList<Product> products) {
        // handled by SalesModule GUI
    }

    public void showHistory() {
        // handled by history panel in Main dashboard
    }
}
