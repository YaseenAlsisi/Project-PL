import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class SalesModule {

    // Swing model for available products list
    static DefaultTableModel productsTableModel;

    // ── Static ref for sidebar refresh ────────────────────────────────
    static InventoryModule invRef;

    // ═══════════════════════════════════════════════════════════════════
    //  BUILD PANEL
    // ═══════════════════════════════════════════════════════════════════
    public static JPanel buildSalesPanel(SalesModule mod, InventoryModule inventory,
                                         UserModule user, MarketingModule marketing) {
        invRef = inventory;

        JPanel root = new JPanel(new BorderLayout(0,0));
        root.setBackground(Main.BG);
        root.setBorder(new EmptyBorder(24,24,24,24));

        root.add(Main.sectionTitle("🛒  SALES — MAKE ORDER"), BorderLayout.NORTH);

        // ── Products table ─────────────────────────────────────────────
        productsTableModel = new DefaultTableModel(
            new String[]{"ID","Product","Price $","Stock","Offer"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        refreshSalesTable(inventory);

        JTable table = Main.buildStyledTable(productsTableModel);

        // ── Search bar (filters by product name, case-insensitive) ─────
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setOpaque(false);
        searchBar.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel searchIcon = new JLabel("🔍  SEARCH PRODUCT");
        searchIcon.setFont(Main.FONT_XS);
        searchIcon.setForeground(Main.FG_DIM);

        JTextField searchField = Main.styledField("Type product name...");

        JButton clearSearch = Main.glowButton("✕", Main.FG_DIM);
        clearSearch.setPreferredSize(new Dimension(40, 36));
        clearSearch.setMaximumSize(new Dimension(40, 36));

        searchBar.add(searchIcon,  BorderLayout.WEST);
        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(clearSearch, BorderLayout.EAST);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void filter() {
                String query = searchField.getText().trim().toLowerCase();
                productsTableModel.setRowCount(0);
                for (Product p : inventory.products) {
                    if (query.isEmpty() || p.name.toLowerCase().contains(query)) {
                        String offerStr = "—";
                        if (Main.marketing != null) {
                            Offer o = Main.marketing.getOfferForProduct(p.name);
                            if (o != null)
                                offerStr = String.format("%.0f%% OFF → $%.2f", o.discount, o.finalPrice);
                        }
                        productsTableModel.addRow(new Object[]{
                            p.id, p.name,
                            String.format("$%.2f", p.price),
                            p.quantity,
                            offerStr
                        });
                    }
                }
            }
            public void insertUpdate (javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate (javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });
        clearSearch.addActionListener(e -> searchField.setText(""));
        // Offer column renderer
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                String s = v==null ? "" : v.toString();
                l.setForeground(s.equals("—") ? Main.FG_DIM : Main.WARNING);
                l.setBackground(sel ? new Color(0,200,150,60) : Main.CARD);
                l.setOpaque(true);
                return l;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        Main.styleScroll(sp);

        // ── Order form ─────────────────────────────────────────────────
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Main.CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(40,55,80),1),
            new EmptyBorder(20,18,20,18)
        ));
        form.setPreferredSize(new Dimension(300,0));

        JLabel formTitle = new JLabel("PLACE ORDER");
        formTitle.setFont(Main.FONT_MED);
        formTitle.setForeground(Main.ACCENT);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JTextField idField = Main.styledField("Product ID");
        // ID: digits only
        ((javax.swing.text.AbstractDocument) idField.getDocument())
            .setDocumentFilter(new javax.swing.text.DocumentFilter() {
                @Override public void insertString(FilterBypass fb, int off, String s, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    if (s != null && s.matches("\\d+")) super.insertString(fb, off, s, a);
                }
                @Override public void replace(FilterBypass fb, int off, int len, String s, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    if (s != null && s.matches("\\d*")) super.replace(fb, off, len, s, a);
                }
            });

        // SpinnerNumberModel (BoundedRangeModel) for quantity
        SpinnerNumberModel qtyModel = new SpinnerNumberModel(1, 1, 9999, 1);
        JSpinner qtySpinner = new JSpinner(qtyModel);
        InventoryModule.styleSpinner(qtySpinner);

        // Live price preview
        JLabel previewLabel = new JLabel("Select a product first");
        previewLabel.setFont(Main.FONT_SM);
        previewLabel.setForeground(Main.FG_DIM);
        previewLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        previewLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        Runnable updatePreview = () -> {
            String idTxt = idField.getText().trim();
            if (idTxt.isEmpty()) { previewLabel.setText("—"); return; }
            try {
                int id = Integer.parseInt(idTxt);
                int q  = (int) qtyModel.getValue();
                for (Product p : inventory.products) {
                    if (p.id == id) {
                        Offer o = marketing.getOfferForProduct(p.name);
                        double unit = (o!=null) ? o.finalPrice : p.price;
                        double total = unit * q;
                        String offerNote = (o!=null)
                            ? String.format("  (%.0f%% off)", o.discount) : "";
                        previewLabel.setText(String.format(
                            "<html><span style='color:#00C896'>$%.2f</span> × %d%s = " +
                            "<b style='color:#00C896'>$%.2f</b></html>",
                            unit, q, offerNote, total));
                        return;
                    }
                }
                previewLabel.setText("Product not found");
            } catch (NumberFormatException ex) { previewLabel.setText("Invalid ID"); }
        };

        idField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { updatePreview.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { updatePreview.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updatePreview.run(); }
        });
        qtyModel.addChangeListener(e -> updatePreview.run());

        // Auto-fill from table row click
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            if (row >= 0) {
                idField.setText(String.valueOf(productsTableModel.getValueAt(row, 0)));
                updatePreview.run();
            }
        });

        JButton orderBtn = Main.glowButton("CONFIRM ORDER", Main.ACCENT);

        // Order receipt area — uses Document model (PlainDocument via JTextArea)
        JTextArea receipt = new JTextArea("  Your receipt will appear here.");
        receipt.setEditable(false);
        receipt.setBackground(new Color(14,18,30));
        receipt.setForeground(Main.ACCENT);
        receipt.setFont(Main.FONT_SM);
        receipt.setBorder(new EmptyBorder(10,10,10,10));
        receipt.setLineWrap(true);
        receipt.setWrapStyleWord(true);
        JScrollPane receiptScroll = new JScrollPane(receipt);
        Main.styleScroll(receiptScroll);
        receiptScroll.setPreferredSize(new Dimension(0,160));
        receiptScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE,180));
        receiptScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        receiptScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        orderBtn.addActionListener(e -> {
            String idTxt = idField.getText().trim();
            if (idTxt.isEmpty()) { AdminModule.showErr("Enter a product ID."); return; }
            try {
                int id = Integer.parseInt(idTxt);
                int q  = (int) qtyModel.getValue();
                boolean found = false;
                for (Product p : inventory.products) {
                    if (p.id == id) {
                        found = true;
                        if (p.quantity < q) {
                            AdminModule.showErr("❌ Not enough stock. Available: "+p.quantity);
                            return;
                        }
                        p.quantity -= q;
                        Offer offer = marketing.getOfferForProduct(p.name);
                        double original  = p.price;
                        double unit      = (offer!=null) ? offer.finalPrice : original;
                        double total     = unit * q;
                        inventory.saveProductsToFile();
                        InventoryModule.refreshTable(inventory);
                        InventoryModule.refreshAlerts(inventory);
                        refreshSalesTable(inventory);
                        user.addPurchase(p.name, q, total);

                        // Write to Document model (PlainDocument)
                        String receiptText =
                            "=====================================\n" +
                            "           PURCHASE RECEIPT\n" +
                            "=====================================\n" +
                            "Product  : " + p.name + "\n" +
                            "Unit $   : " + String.format("%.2f", original) + "\n" +
                            (offer!=null
                                ? "Discount : " + offer.discount + "%\n" +
                                  "After    : $" + String.format("%.2f", unit) + "\n"
                                : "No active offer\n") +
                            "Qty      : " + q + "\n" +
                            "-------------------------------------\n" +
                            "TOTAL    : $" + String.format("%.2f", total) + "\n" +
                            "=====================================\n" +
                            "       Thank you for shopping!\n";

                        receipt.setText(receiptText);
                        idField.setText("");
                        qtyModel.setValue(1);
                        return;
                    }
                }
                if (!found) AdminModule.showErr("❌ Product ID not found.");
            } catch (NumberFormatException ex) { AdminModule.showErr("Invalid ID."); }
        });

        form.add(formTitle);
        form.add(Box.createVerticalStrut(14));
        form.add(Main.formRow("PRODUCT ID  (or click row)", idField));
        form.add(Box.createVerticalStrut(10));
        form.add(Main.formRow("QUANTITY", qtySpinner));
        form.add(Box.createVerticalStrut(10));
        form.add(Main.labelFor("PRICE PREVIEW"));
        form.add(Box.createVerticalStrut(5));
        form.add(previewLabel);
        form.add(Box.createVerticalStrut(16));
        form.add(orderBtn);
        form.add(Box.createVerticalStrut(14));
        form.add(Main.labelFor("RECEIPT"));
        form.add(Box.createVerticalStrut(5));
        form.add(receiptScroll);
        form.add(Box.createVerticalGlue());

        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setOpaque(false);
        center.add(searchBar, BorderLayout.NORTH);
        center.add(sp,        BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);
        root.add(form,   BorderLayout.EAST);
        return root;
    }

    public static void refreshSalesTable(InventoryModule inventory) {
        if (productsTableModel == null) return;
        productsTableModel.setRowCount(0);
        for (Product p : inventory.products) {
            // Check offer via Main's marketing module ref
            String offerStr = "—";
            if (Main.marketing != null) {
                Offer o = Main.marketing.getOfferForProduct(p.name);
                if (o != null) offerStr = String.format("%.0f%% OFF → $%.2f", o.discount, o.finalPrice);
            }
            productsTableModel.addRow(new Object[]{
                p.id, p.name,
                String.format("$%.2f", p.price),
                p.quantity,
                offerStr
            });
        }
    }

    // ── Legacy stubs ───────────────────────────────────────────────────
    public void menu(ArrayList<Product> products, UserModule user, MarketingModule marketing) {}
    public void makeOrder(ArrayList<Product> products, UserModule user, MarketingModule marketing) {}
}