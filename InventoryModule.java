import java.awt.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

class Product {
    int id;
    String name;
    double price;
    int quantity;
    int minStock;
    LocalDate expiry;

    Product(int id, String name, double price, int quantity, int minStock, LocalDate expiry) {
        this.id=id; this.name=name; this.price=price;
        this.quantity=quantity; this.minStock=minStock; this.expiry=expiry;
    }

    void display() {
        System.out.println(id+" | "+name+" | $"+price+" | Qty:"+quantity);
    }
}

public class InventoryModule {

    ArrayList<Product> products = new ArrayList<>();

    // ── Swing model — all UI syncs through this ───────────────────────
    static DefaultTableModel tableModel;
    static JPanel            alertPanel;

    public InventoryModule() {
        loadProductsFromFile();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  BUILD PANEL
    // ═══════════════════════════════════════════════════════════════════
    public static JPanel buildInventoryPanel(InventoryModule mod) {
        JPanel root = new JPanel(new BorderLayout(0,12));
        root.setBackground(Main.BG);
        root.setBorder(new EmptyBorder(24,24,24,24));

        // Title
        root.add(Main.sectionTitle("📦  MARKET INVENTORY"), BorderLayout.NORTH);

        // ── Alert strip ────────────────────────────────────────────────
        alertPanel = new JPanel();
        alertPanel.setLayout(new BoxLayout(alertPanel, BoxLayout.Y_AXIS));
        alertPanel.setBackground(Main.BG);
        refreshAlerts(mod);

        JScrollPane alertScroll = new JScrollPane(alertPanel);
        alertScroll.setPreferredSize(new Dimension(0, 80));
        alertScroll.setBorder(new LineBorder(new Color(40,55,80),1));
        alertScroll.getViewport().setBackground(Main.BG);

        // ── Table ──────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(
            new String[]{"ID","Name","Price","Qty","MinStock","Expiry","Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c==2) return Double.class;
                if (c==3||c==4) return Integer.class;
                return String.class;
            }
        };
        refreshTable(mod);

        JTable table = Main.buildStyledTable(tableModel);
        // Custom renderer for Status column
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                String s = v == null ? "" : v.toString();
                l.setForeground(s.contains("OK") ? Main.ACCENT
                    : s.contains("EXPIRED") ? Main.DANGER : Main.WARNING);
                l.setBackground(sel ? new Color(0,200,150,60) : Main.CARD);
                l.setOpaque(true);
                return l;
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        Main.styleScroll(tableScroll);

        // ── Form side panel ────────────────────────────────────────────
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Main.CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(40,55,80),1),
            new EmptyBorder(20,18,20,18)
        ));
        form.setPreferredSize(new Dimension(280,0));

        // ADD product fields
        JLabel addTitle = new JLabel("ADD NEW PRODUCT");
        addTitle.setFont(Main.FONT_MED);
        addTitle.setForeground(Main.ACCENT);
        addTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        addTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JTextField fId    = Main.styledField("ID");
        JTextField fName  = Main.styledField("Name");
        JTextField fPrice = Main.styledField("Price ($)");
        JTextField fQty   = Main.styledField("Quantity");
        JTextField fMin   = Main.styledField("Min Stock");
        JTextField fExp   = Main.styledField("Expiry (yyyy/MM/dd)");

        // ── Input filters ──────────────────────────────────────────────
        // ID: digits only
        ((javax.swing.text.AbstractDocument) fId.getDocument())
            .setDocumentFilter(new javax.swing.text.DocumentFilter() {
                @Override public void insertString(FilterBypass fb, int off, String s, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    if (s != null && s.matches("\\d+")) super.insertString(fb, off, s, a);
                }
                @Override public void replace(FilterBypass fb, int off, int len, String s, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    if (s != null && s.matches("\\d*")) super.replace(fb, off, len, s, a);
                }
            });
        // Name: letters, spaces, hyphens only
        ((javax.swing.text.AbstractDocument) fName.getDocument())
            .setDocumentFilter(new javax.swing.text.DocumentFilter() {
                @Override public void insertString(FilterBypass fb, int off, String s, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    if (s != null && s.matches("[a-zA-Z\\s\\-]+")) super.insertString(fb, off, s, a);
                }
                @Override public void replace(FilterBypass fb, int off, int len, String s, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    if (s != null && s.matches("[a-zA-Z\\s\\-]*")) super.replace(fb, off, len, s, a);
                }
            });
        // Price: digits and single dot only
        ((javax.swing.text.AbstractDocument) fPrice.getDocument())
            .setDocumentFilter(new javax.swing.text.DocumentFilter() {
                @Override public void insertString(FilterBypass fb, int off, String s, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    String cur = fb.getDocument().getText(0, fb.getDocument().getLength());
                    if (s != null && s.matches("[\\d.]+") && !(s.contains(".") && cur.contains(".")))
                        super.insertString(fb, off, s, a);
                }
                @Override public void replace(FilterBypass fb, int off, int len, String s, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    String cur = fb.getDocument().getText(0, fb.getDocument().getLength());
                    String remaining = cur.substring(0, off) + cur.substring(Math.min(off + len, cur.length()));
                    if (s != null && s.matches("[\\d.]*") && !(s.contains(".") && remaining.contains(".")))
                        super.replace(fb, off, len, s, a);
                }
            });
        // Quantity: digits only
        ((javax.swing.text.AbstractDocument) fQty.getDocument())
            .setDocumentFilter(new javax.swing.text.DocumentFilter() {
                @Override public void insertString(FilterBypass fb, int off, String s, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    if (s != null && s.matches("\\d+")) super.insertString(fb, off, s, a);
                }
                @Override public void replace(FilterBypass fb, int off, int len, String s, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    if (s != null && s.matches("\\d*")) super.replace(fb, off, len, s, a);
                }
            });
        // Min Stock: digits only
        ((javax.swing.text.AbstractDocument) fMin.getDocument())
            .setDocumentFilter(new javax.swing.text.DocumentFilter() {
                @Override public void insertString(FilterBypass fb, int off, String s, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    if (s != null && s.matches("\\d+")) super.insertString(fb, off, s, a);
                }
                @Override public void replace(FilterBypass fb, int off, int len, String s, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    if (s != null && s.matches("\\d*")) super.replace(fb, off, len, s, a);
                }
            });
        // Expiry: digits and slash only (yyyy/MM/dd)
        ((javax.swing.text.AbstractDocument) fExp.getDocument())
            .setDocumentFilter(new javax.swing.text.DocumentFilter() {
                @Override public void insertString(FilterBypass fb, int off, String s, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    if (s != null && s.matches("[\\d/]+")) super.insertString(fb, off, s, a);
                }
                @Override public void replace(FilterBypass fb, int off, int len, String s, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    if (s != null && s.matches("[\\d/]*")) super.replace(fb, off, len, s, a);
                }
            });

        JButton addBtn = Main.glowButton("+ ADD PRODUCT", Main.ACCENT);
        addBtn.addActionListener(e -> {
            try {
                int id       = Integer.parseInt(fId.getText().trim());
                String name  = fName.getText().trim();
                // duplicate check
                for (Product p : mod.products) {
                    if (p.id == id) { AdminModule.showErr("❌ ID " + id + " is already taken. Choose a different ID."); return; }
                    if (p.name.equalsIgnoreCase(name)) { AdminModule.showErr("❌ Product \"" + name + "\" already exists. Use Restock to update quantity."); return; }
                }
                double price = Double.parseDouble(fPrice.getText().trim());
                int qty   = Integer.parseInt(fQty.getText().trim());
                int min   = Integer.parseInt(fMin.getText().trim());
                DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                LocalDate exp = LocalDate.parse(fExp.getText().trim(), f);
                mod.products.add(new Product(id,name,price,qty,min,exp));
                mod.saveProductsToFile();
                refreshTable(mod);
                refreshAlerts(mod);
                Main.updateHomeCounts();
                // ── Live sync: push new product into Marketing combo ──────────
                if (MarketingModule.productComboModel != null)
                    MarketingModule.productComboModel.addElement(name);
                clearFields(fId,fName,fPrice,fQty,fMin,fExp);
            } catch (Exception ex) { AdminModule.showErr("Invalid input: "+ex.getMessage()); }
        });

        // RESTOCK  — uses BoundedRangeModel via JSpinner
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(40,55,80));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE,1));

        JLabel restockTitle = new JLabel("RESTOCK / DAMAGED");
        restockTitle.setFont(Main.FONT_MED);
        restockTitle.setForeground(Main.ACCENT2);
        restockTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        restockTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JTextField restockId  = Main.styledField("Product ID");
        // Restock ID: digits only
        ((javax.swing.text.AbstractDocument) restockId.getDocument())
            .setDocumentFilter(new javax.swing.text.DocumentFilter() {
                @Override public void insertString(FilterBypass fb, int off, String s, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    if (s != null && s.matches("\\d+")) super.insertString(fb, off, s, a);
                }
                @Override public void replace(FilterBypass fb, int off, int len, String s, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    if (s != null && s.matches("\\d*")) super.replace(fb, off, len, s, a);
                }
            });

        // BoundedRangeModel powering a JSpinner
        SpinnerNumberModel qtyModel = new SpinnerNumberModel(1, 1, 10000, 1);
        JSpinner qtySpinner = new JSpinner(qtyModel);
        styleSpinner(qtySpinner);

        // ButtonGroup for Restock / Damaged
        ButtonGroup opGroup = new ButtonGroup();
        JRadioButton rbRestock  = AdminModule.styledRadio("Restock",  Main.ACCENT);
        JRadioButton rbDamaged  = AdminModule.styledRadio("Damaged",  Main.DANGER);
        opGroup.add(rbRestock); opGroup.add(rbDamaged);
        rbRestock.setSelected(true);

        JPanel opRow = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        opRow.setOpaque(false);
        opRow.add(rbRestock); opRow.add(rbDamaged);
        opRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
        opRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        opRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JButton restockBtn = Main.glowButton("APPLY", Main.ACCENT2);
        restockBtn.addActionListener(e -> {
            try {
                int id  = Integer.parseInt(restockId.getText().trim());
                int qty = (int) qtyModel.getValue();
                boolean found = false;
                for (Product p : mod.products) {
                    if (p.id == id) {
                        found = true;
                        if (rbRestock.isSelected()) {
                            p.quantity += qty;
                        } else {
                            if (p.quantity < qty) { AdminModule.showErr("Not enough stock."); return; }
                            p.quantity -= qty;
                        }
                        mod.saveProductsToFile();
                        refreshTable(mod);
                        refreshAlerts(mod);
                        restockId.setText("");
                        break;
                    }
                }
                if (!found) AdminModule.showErr("Product ID not found.");
            } catch (NumberFormatException ex) { AdminModule.showErr("ID must be a number."); }
        });

        // DELETE
        JButton delBtn = Main.glowButton("🗑  DELETE SELECTED", Main.DANGER);
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row<0) { AdminModule.showErr("Select a row first."); return; }
            int id = (int) tableModel.getValueAt(row,0);
            String delName = (String) tableModel.getValueAt(row,1);
            mod.products.removeIf(p -> p.id==id);
            mod.saveProductsToFile();
            refreshTable(mod);
            refreshAlerts(mod);
            Main.updateHomeCounts();
            // ── Live sync: remove from Marketing combo ────────────────────
            if (MarketingModule.productComboModel != null)
                MarketingModule.productComboModel.removeElement(delName);
        });

        form.add(addTitle);
        form.add(Box.createVerticalStrut(12));
        form.add(Main.formRow("ID",       fId));
        form.add(Box.createVerticalStrut(8));
        form.add(Main.formRow("NAME",     fName));
        form.add(Box.createVerticalStrut(8));
        form.add(Main.formRow("PRICE",    fPrice));
        form.add(Box.createVerticalStrut(8));
        form.add(Main.formRow("QUANTITY", fQty));
        form.add(Box.createVerticalStrut(8));
        form.add(Main.formRow("MIN STOCK",fMin));
        form.add(Box.createVerticalStrut(8));
        form.add(Main.formRow("EXPIRY",   fExp));
        form.add(Box.createVerticalStrut(10));
        form.add(addBtn);
        form.add(Box.createVerticalStrut(16));
        form.add(sep);
        form.add(Box.createVerticalStrut(12));
        form.add(restockTitle);
        form.add(Box.createVerticalStrut(10));
        form.add(Main.formRow("PRODUCT ID", restockId));
        form.add(Box.createVerticalStrut(8));
        form.add(Main.labelFor("QUANTITY"));
        form.add(Box.createVerticalStrut(4));
        form.add(qtySpinner);
        form.add(Box.createVerticalStrut(8));
        form.add(opRow);
        form.add(Box.createVerticalStrut(10));
        form.add(restockBtn);
        form.add(Box.createVerticalStrut(12));
        form.add(delBtn);
        form.add(Box.createVerticalStrut(8));

        JPanel center = new JPanel(new BorderLayout(0,8));
        center.setBackground(Main.BG);
        center.add(alertScroll,  BorderLayout.NORTH);
        center.add(tableScroll,  BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);
        root.add(form,   BorderLayout.EAST);
        return root;
    }

    // ── Refresh helpers ───────────────────────────────────────────────
    static void refreshTable(InventoryModule mod) {
        tableModel.setRowCount(0);
        LocalDate today = LocalDate.now();
        for (Product p : mod.products) {
            String status;
            if (p.expiry.isBefore(today))               status = "EXPIRED";
            else if (p.expiry.minusDays(3).isBefore(today)) status = "⚠ NEAR EXPIRY";
            else if (p.quantity <= p.minStock)           status = "⚠ LOW STOCK";
            else                                          status = "OK";
            tableModel.addRow(new Object[]{
                p.id, p.name, p.price, p.quantity, p.minStock, p.expiry.toString(), status
            });
        }
    }

    static void refreshAlerts(InventoryModule mod) {
        alertPanel.removeAll();
        LocalDate today = LocalDate.now();
        boolean any = false;
        for (Product p : mod.products) {
            String msg = null;
            Color  col = Main.ACCENT;
            if (p.expiry.isBefore(today)) {
                msg = "❌ EXPIRED: " + p.name; col = Main.DANGER;
            } else if (p.expiry.minusDays(3).isBefore(today)) {
                msg = "⚠  NEAR EXPIRY: " + p.name + "  ("+p.expiry+")"; col = Main.WARNING;
            }
            if (p.quantity <= p.minStock) {
                JLabel l = alertLabel("⚠  LOW STOCK: " + p.name + "  (qty="+p.quantity+")", Main.WARNING);
                alertPanel.add(l); any = true;
            }
            if (msg != null) {
                alertPanel.add(alertLabel(msg, col)); any = true;
            }
        }
        if (!any) alertPanel.add(alertLabel("✔  All products are within safe levels.", Main.ACCENT));
        alertPanel.revalidate();
        alertPanel.repaint();
    }

    static JLabel alertLabel(String msg, Color color) {
        JLabel l = new JLabel("  " + msg);
        l.setFont(Main.FONT_SM);
        l.setForeground(color);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return l;
    }

    static void clearFields(JTextField... fields) {
        for (JTextField f : fields) f.setText("");
    }

    static void styleSpinner(JSpinner sp) {
        sp.setBackground(Main.SURFACE);
        sp.setForeground(Main.FG);
        sp.setFont(Main.FONT_SM);
        sp.setBorder(new LineBorder(new Color(50,65,95),1));
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        JFormattedTextField tf = ((JSpinner.DefaultEditor) sp.getEditor()).getTextField();
        tf.setBackground(Main.SURFACE);
        tf.setForeground(Main.FG);
        tf.setCaretColor(Main.ACCENT);
        tf.setFont(Main.FONT_SM);
    }

    // ── Legacy console stub ────────────────────────────────────────────
    public void menu() {}

    public ArrayList<Product> getProducts() { return products; }

    public void saveProductsToFile() {
        try {
            PrintWriter w = new PrintWriter("products.txt");
            w.println("================================================");
            w.println("          SAUDI MARKET — PRODUCT INVENTORY");
            w.println("================================================");
            w.println("  Total products: " + products.size());
            w.println("================================================");
            w.println();
            int idx = 1;
            for (Product p : products) {
                w.println("  Product #" + idx++);
                w.println("  ----------------------------------------");
                w.println("  ID         : " + p.id);
                w.println("  Name       : " + p.name);
                w.printf ("  Price      : $%.2f%n", p.price);
                w.println("  Quantity   : " + p.quantity + " units");
                w.println("  Min. Stock : " + p.minStock + " units");
                w.println("  Expiry     : " + p.expiry);
                w.println();
            }
            w.println("================================================");
            w.println("  END OF INVENTORY");
            w.println("================================================");
            w.close();

            // machine-readable copy for loading
            PrintWriter raw = new PrintWriter("products_data.txt");
            for (Product p : products)
                raw.println(p.id+"|"+p.name+"|"+p.price+"|"+p.quantity+"|"+p.minStock+"|"+p.expiry);
            raw.close();
        } catch (Exception e) { System.out.println("Error saving products"); }
    }

    public void loadProductsFromFile() {
        try {
            File raw = new File("products_data.txt");
            File old = new File("products.txt");
            File src = raw.exists() ? raw : old;
            if (!src.exists()) return;
            Scanner fr = new Scanner(src);
            while (fr.hasNextLine()) {
                String line = fr.nextLine().trim();
                if (line.isEmpty() || line.startsWith("=") || line.startsWith(" ")) continue;
                String[] d = line.split(raw.exists() ? "\\|" : ",");
                if (d.length < 6) continue;
                try {
                    products.add(new Product(
                        Integer.parseInt(d[0].trim()), d[1].trim(),
                        Double.parseDouble(d[2].trim()),
                        Integer.parseInt(d[3].trim()),
                        Integer.parseInt(d[4].trim()),
                        LocalDate.parse(d[5].trim())
                    ));
                } catch (Exception ignored) {}
            }
            fr.close();
        } catch (Exception e) { System.out.println("Error loading products"); }
    }
}