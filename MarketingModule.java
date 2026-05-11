import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

class Offer {
    String productName;
    double originalPrice;
    double discount;
    double finalPrice;
    long   expiryTime; // epoch ms when offer expires

    Offer(String productName, double originalPrice, double discount, int days) {
        this.productName   = productName;
        this.originalPrice = originalPrice;
        this.discount      = discount;
        this.finalPrice    = originalPrice - (originalPrice * discount / 100);
        this.expiryTime    = System.currentTimeMillis() + (long) days * 24 * 60 * 60 * 1000L;
    }

    /** Human-readable countdown, or "EXPIRED" */
    String timeLeft() {
        long ms = expiryTime - System.currentTimeMillis();
        if (ms <= 0) return "EXPIRED";
        long secs  = ms / 1000;
        long days  = secs / 86400;
        long hours = (secs % 86400) / 3600;
        long mins  = (secs % 3600)  / 60;
        long s     = secs % 60;
        if (days  > 0) return days  + "d " + hours + "h " + mins + "m";
        if (hours > 0) return hours + "h " + mins  + "m " + s    + "s";
        return mins + "m " + s + "s";
    }

    public void show() {
        System.out.println(productName+" | Before:$"+originalPrice+" | After:$"+finalPrice+" | "+discount+"%");
    }
}

public class MarketingModule {

    ArrayList<Offer> offers = new ArrayList<>();

    // Swing models
    static DefaultTableModel  offersTableModel;
    static DefaultComboBoxModel<String> productComboModel;

    public MarketingModule() {
        loadOffersFromFile();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  BUILD PANEL
    // ═══════════════════════════════════════════════════════════════════
    public static JPanel buildMarketingPanel(MarketingModule mod, InventoryModule inventory) {
        JPanel root = new JPanel(new BorderLayout(0,0));
        root.setBackground(Main.BG);
        root.setBorder(new EmptyBorder(24,24,24,24));

        root.add(Main.sectionTitle("📢  MARKETING — OFFERS"), BorderLayout.NORTH);

        // ── Offers table ───────────────────────────────────────────────
        offersTableModel = new DefaultTableModel(
            new String[]{"Product","Original $","Discount %","Final $","Time Left"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c==1||c==2||c==3) return Double.class;
                return String.class;
            }
        };
        refreshOffersTable(mod);

        JTable table = Main.buildStyledTable(offersTableModel);
        // Discount column renderer — green/yellow by value
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                double d = v == null ? 0 : ((Number)v).doubleValue();
                l.setForeground(d >= 30 ? Main.DANGER : d >= 15 ? Main.WARNING : Main.ACCENT);
                l.setBackground(sel ? new Color(0,200,150,60) : Main.CARD);
                l.setOpaque(true);
                l.setText(String.format("%.1f%%", d));
                l.setHorizontalAlignment(SwingConstants.CENTER);
                return l;
            }
        });

        // Time Left column renderer — green/yellow/red by remaining time
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                String s = v == null ? "" : v.toString();
                Color  col = s.equals("EXPIRED") ? Main.DANGER
                           : s.contains("h") || s.contains("m") && !s.contains("d") ? Main.WARNING
                           : Main.ACCENT;
                l.setForeground(col);
                l.setBackground(sel ? new Color(0,200,150,60) : Main.CARD);
                l.setOpaque(true);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                return l;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        Main.styleScroll(sp);

        // ── Countdown Timer — ticks every second ───────────────────────
        // Removes expired offers and refreshes the table live
        javax.swing.Timer countdown = new javax.swing.Timer(1000, e -> {
            boolean changed = false;
            Iterator<Offer> it = mod.offers.iterator();
            while (it.hasNext()) {
                Offer o = it.next();
                if (o.expiryTime <= System.currentTimeMillis()) {
                    it.remove();
                    changed = true;
                }
            }
            if (changed) {
                mod.saveOffersToFile();
                Main.updateHomeCounts();
                SalesModule.refreshSalesTable(SalesModule.invRef);
            }
            refreshOffersTable(mod);
        });
        countdown.start();

        // ── Form ───────────────────────────────────────────────────────
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Main.CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(40,55,80),1),
            new EmptyBorder(20,18,20,18)
        ));
        form.setPreferredSize(new Dimension(280,0));

        JLabel formTitle = new JLabel("CREATE OFFER");
        formTitle.setFont(Main.FONT_MED);
        formTitle.setForeground(Main.WARNING);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        // ComboBoxModel populated from live product list
        productComboModel = new DefaultComboBoxModel<>();
        refreshProductCombo(inventory);

        JComboBox<String> productCombo = new JComboBox<>(productComboModel);
        Main.styleCombo(productCombo);
        productCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        productCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        // SpinnerNumberModel for discount %
        SpinnerNumberModel discountModel = new SpinnerNumberModel(10.0, 1.0, 90.0, 0.5);
        JSpinner discountSpinner = new JSpinner(discountModel);
        InventoryModule.styleSpinner(discountSpinner);

        // SpinnerNumberModel for days
        SpinnerNumberModel daysModel = new SpinnerNumberModel(7, 1, 365, 1);
        JSpinner daysSpinner = new JSpinner(daysModel);
        InventoryModule.styleSpinner(daysSpinner);

        // Live preview label — updates as spinner changes
        JLabel preview = new JLabel("—");
        preview.setFont(Main.FONT_SM);
        preview.setForeground(Main.ACCENT);
        preview.setAlignmentX(Component.LEFT_ALIGNMENT);
        preview.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        Runnable updatePreview = () -> {
            String sel = (String) productComboModel.getSelectedItem();
            if (sel == null) { preview.setText("—"); return; }
            for (Product p : inventory.products) {
                if (p.name.equals(sel)) {
                    double disc  = (double) discountModel.getValue();
                    double after = p.price - (p.price * disc / 100);
                    preview.setText(String.format("$%.2f  →  $%.2f  (save %.1f%%)",
                        p.price, after, disc));
                    return;
                }
            }
        };

        productCombo.addActionListener(e -> updatePreview.run());
        discountModel.addChangeListener(e -> updatePreview.run());

        JButton createBtn = Main.glowButton("CREATE OFFER", Main.WARNING);
        createBtn.addActionListener(e -> {
            String sel = (String) productComboModel.getSelectedItem();
            if (sel == null) { AdminModule.showErr("No product selected."); return; }
            double disc = (double) discountModel.getValue();
            int days    = (int) daysModel.getValue();
            for (Product p : inventory.products) {
                if (p.name.equals(sel)) {
                    mod.offers.add(new Offer(p.name, p.price, disc, days));
                    mod.saveOffersToFile();
                    refreshOffersTable(mod);
                    Main.updateHomeCounts();
                    return;
                }
            }
        });

        // Delete selected offer
        JButton delBtn = Main.glowButton("🗑  DELETE SELECTED", Main.DANGER);
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { AdminModule.showErr("Select an offer."); return; }
            String name = (String) offersTableModel.getValueAt(row, 0);
            mod.offers.removeIf(o -> o.productName.equals(name));
            mod.saveOffersToFile();
            refreshOffersTable(mod);
            Main.updateHomeCounts();
        });

        form.add(formTitle);
        form.add(Box.createVerticalStrut(14));
        form.add(Main.labelFor("SELECT PRODUCT"));
        form.add(Box.createVerticalStrut(5));
        form.add(productCombo);
        form.add(Box.createVerticalStrut(10));
        form.add(Main.formRow("DISCOUNT %", discountSpinner));
        form.add(Box.createVerticalStrut(10));
        form.add(Main.formRow("DURATION (DAYS)", daysSpinner));
        form.add(Box.createVerticalStrut(12));
        form.add(Main.labelFor("PRICE PREVIEW"));
        form.add(Box.createVerticalStrut(5));
        form.add(preview);
        form.add(Box.createVerticalStrut(16));
        form.add(createBtn);
        form.add(Box.createVerticalStrut(10));
        form.add(delBtn);
        form.add(Box.createVerticalGlue());

        root.add(sp,   BorderLayout.CENTER);
        root.add(form, BorderLayout.EAST);
        return root;
    }

    static void refreshOffersTable(MarketingModule mod) {
        offersTableModel.setRowCount(0);
        for (Offer o : mod.offers)
            offersTableModel.addRow(new Object[]{
                o.productName, o.originalPrice, o.discount, o.finalPrice, o.timeLeft()
            });
    }

    static void refreshProductCombo(InventoryModule inventory) {
        productComboModel.removeAllElements();
        for (Product p : inventory.products)
            productComboModel.addElement(p.name);
    }

    // ── Lookup used by SalesModule ─────────────────────────────────────
    public Offer getOfferForProduct(String productName) {
        for (Offer o : offers)
            if (o.productName.equalsIgnoreCase(productName)) return o;
        return null;
    }

    // ── Legacy console stub ────────────────────────────────────────────
    public void menu(ArrayList<Product> products) {}

    public void saveOffersToFile() {
        try {
            // Human-readable offers file
            PrintWriter w = new PrintWriter("offers.txt");
            w.println("================================================");
            w.println("          SAUDI MARKET — ACTIVE OFFERS");
            w.println("================================================");
            w.println("  Total offers: " + offers.size());
            w.println("================================================");
            w.println();
            int idx = 1;
            for (Offer o : offers) {
                w.println("  Offer #" + idx++);
                w.println("  ----------------------------------------");
                w.println("  Product        : " + o.productName);
                w.printf ("  Original Price : $%.2f%n", o.originalPrice);
                w.printf ("  Discount       : %.1f%%%n", o.discount);
                w.printf ("  Final Price    : $%.2f%n", o.finalPrice);
                w.println();
            }
            w.println("================================================");
            w.println("  END OF OFFERS");
            w.println("================================================");
            w.close();

            // Machine-readable copy for loading
            PrintWriter raw = new PrintWriter("offers_data.txt");
            for (Offer o : offers)
                raw.println(o.productName+"|"+o.originalPrice+"|"+o.discount+"|"+o.finalPrice+"|"+o.expiryTime);
            raw.close();
        } catch (Exception e) { System.out.println("Error saving offers"); }
    }

    public void loadOffersFromFile() {
        try {
            File raw = new File("offers_data.txt");
            File old = new File("offers.txt");
            File src = raw.exists() ? raw : old;
            if (!src.exists()) return;
            Scanner fr = new Scanner(src);
            while (fr.hasNextLine()) {
                String line = fr.nextLine().trim();
                if (line.isEmpty() || line.startsWith("=") || line.startsWith(" ")) continue;
                String[] d = line.split(raw.exists() ? "\\|" : ",");
                if (d.length < 4) continue;
                try {
                    Offer o = new Offer(d[0].trim(), Double.parseDouble(d[1].trim()), Double.parseDouble(d[2].trim()), 0);
                    o.finalPrice  = Double.parseDouble(d[3].trim());
                    // restore saved expiry if present; otherwise treat as already expired
                    o.expiryTime  = (d.length >= 5) ? Long.parseLong(d[4].trim())
                                                     : System.currentTimeMillis() - 1;
                    // skip offers that already expired while the app was closed
                    if (o.expiryTime > System.currentTimeMillis())
                        offers.add(o);
                } catch (Exception ignored) {}
            }
            fr.close();
        } catch (Exception e) { System.out.println("Error loading offers"); }
    }
}