import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

class Employee {
    int id;
    String name;
    String password;
    String type;
    Employee(int id, String name, String password, String type) {
        this.id = id; this.name = name; this.password = password; this.type = type;
    }
}

public class AdminModule {

    ArrayList<Employee> employees = new ArrayList<>();
    String adminUsername = "admin";
    String adminPassword = "1234";

    // Swing Model — central source of truth for the employee table
    DefaultTableModel tableModel;

    public AdminModule() {
        loadCredentials();
        loadEmployeesFromFile();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  BUILD PANEL  (called once by Main)
    // ═══════════════════════════════════════════════════════════════════
    public static JPanel buildAdminPanel(AdminModule mod) {
        JPanel root = new JPanel(new BorderLayout(0,0));
        root.setBackground(Main.BG);
        root.setBorder(new EmptyBorder(24,24,24,24));

        // ── Title ──────────────────────────────────────────────────────
        JLabel title = Main.sectionTitle("👤  ADMIN PANEL — EMPLOYEES");
        root.add(title, BorderLayout.NORTH);

        // ── Table with DefaultTableModel ───────────────────────────────
        mod.tableModel = new DefaultTableModel(
            new String[]{"ID","Name","Type"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        mod.refreshTable();

        JTable table = Main.buildStyledTable(mod.tableModel);

        // ── Search bar (filters table by ID) ───────────────────────────
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setOpaque(false);
        searchBar.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel searchIcon = new JLabel("🔍  SEARCH BY ID");
        searchIcon.setFont(Main.FONT_XS);
        searchIcon.setForeground(Main.FG_DIM);

        JTextField searchField = Main.styledField("Enter Employee ID...");
        ((javax.swing.text.AbstractDocument) searchField.getDocument())
            .setDocumentFilter(new javax.swing.text.DocumentFilter() {
                @Override public void insertString(FilterBypass fb, int off, String s, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    if (s != null && s.matches("\\d+")) super.insertString(fb, off, s, a);
                }
                @Override public void replace(FilterBypass fb, int off, int len, String s, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    if (s != null && s.matches("\\d*")) super.replace(fb, off, len, s, a);
                }
            });

        JButton clearBtn = Main.glowButton("✕", Main.FG_DIM);
        clearBtn.setPreferredSize(new Dimension(40, 36));
        clearBtn.setMaximumSize(new Dimension(40, 36));

        searchBar.add(searchIcon,  BorderLayout.WEST);
        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(clearBtn,    BorderLayout.EAST);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void filter() {
                String txt = searchField.getText().trim();
                mod.tableModel.setRowCount(0);
                for (Employee em : mod.employees) {
                    if (txt.isEmpty() || String.valueOf(em.id).equals(txt))
                        mod.tableModel.addRow(new Object[]{em.id, em.name, em.type});
                }
            }
            public void insertUpdate (javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate (javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });
        clearBtn.addActionListener(e -> searchField.setText(""));

        JScrollPane sp = new JScrollPane(table);
        Main.styleScroll(sp);

        // ── Form panel ─────────────────────────────────────────────────
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Main.CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(40,55,80),1),
            new EmptyBorder(20,20,20,20)
        ));
        form.setPreferredSize(new Dimension(270,0));

        JLabel formTitle = new JLabel("ADD EMPLOYEE");
        formTitle.setFont(Main.FONT_MED);
        formTitle.setForeground(Main.ACCENT2);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JTextField idField   = Main.styledField("Employee ID");
        JTextField nameField = Main.styledField("Full Name");
        JTextField passField = Main.styledField("Password");

        // ── Input filters ──────────────────────────────────────────────
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
        // Name: letters, spaces, hyphens only
        ((javax.swing.text.AbstractDocument) nameField.getDocument())
            .setDocumentFilter(new javax.swing.text.DocumentFilter() {
                @Override public void insertString(FilterBypass fb, int off, String s, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    if (s != null && s.matches("[a-zA-Z\\s\\-]+")) super.insertString(fb, off, s, a);
                }
                @Override public void replace(FilterBypass fb, int off, int len, String s, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    if (s != null && s.matches("[a-zA-Z\\s\\-]*")) super.replace(fb, off, len, s, a);
                }
            });

        // ButtonGroup + ButtonModel for role radio buttons
        ButtonGroup roleGroup = new ButtonGroup();
        JRadioButton rbAdmin    = styledRadio("admin",    Main.ACCENT2);
        JRadioButton rbEmployee = styledRadio("employee", Main.ACCENT);
        JRadioButton rbManager  = styledRadio("manager",  Main.WARNING);
        JRadioButton rbUser     = styledRadio("user",     Main.DANGER);
        roleGroup.add(rbAdmin);
        roleGroup.add(rbEmployee);
        roleGroup.add(rbManager);
        roleGroup.add(rbUser);
        rbEmployee.setSelected(true);

        // Two rows so all 4 options are fully visible
        JPanel radioRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        radioRow1.setOpaque(false);
        radioRow1.add(rbAdmin); radioRow1.add(rbEmployee);
        radioRow1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        radioRow1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel radioRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        radioRow2.setOpaque(false);
        radioRow2.add(rbManager); radioRow2.add(rbUser);
        radioRow2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        radioRow2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel radioRow = new JPanel();
        radioRow.setLayout(new BoxLayout(radioRow, BoxLayout.Y_AXIS));
        radioRow.setOpaque(false);
        radioRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        radioRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        radioRow.add(radioRow1);
        radioRow.add(radioRow2);

        JButton addBtn = Main.glowButton("+ ADD", Main.ACCENT2);
        addBtn.addActionListener(e -> {
            try {
                int id       = Integer.parseInt(idField.getText().trim());
                String name  = nameField.getText().trim();
                String pass  = passField.getText().trim();
                String type  = rbAdmin.isSelected()   ? "admin"
                             : rbManager.isSelected() ? "manager"
                             : rbUser.isSelected()    ? "user" : "employee";
                if (name.isEmpty() || pass.isEmpty()) { showErr("Fill all fields."); return; }
                for (Employee ex : mod.employees) {
                    if (ex.id == id) { showErr("❌ ID " + id + " is already taken. Choose a different ID."); return; }
                    if (ex.name.equalsIgnoreCase(name)) { showErr("❌ Employee \"" + name + "\" already exists."); return; }
                }
                mod.employees.add(new Employee(id, name, pass, type));
                mod.saveEmployeesToFile();
                mod.refreshTable();
                Main.updateHomeCounts();
                idField.setText(""); nameField.setText(""); passField.setText("");
            } catch (NumberFormatException ex) { showErr("ID must be a number."); }
        });

        // ── Delete row by selection ────────────────────────────────────
        JButton delBtn = Main.glowButton("🗑  DELETE SELECTED", Main.DANGER);
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showErr("Select a row first."); return; }
            int id = (int) mod.tableModel.getValueAt(row, 0);
            mod.employees.removeIf(em -> em.id == id);
            mod.saveEmployeesToFile();
            mod.refreshTable();
            Main.updateHomeCounts();
        });

        // ── Change password note (handled by sidebar "Change Password" panel) ─
        JLabel noteLabel = new JLabel("<html><i>Use the 🔑 Change Password<br>option in the sidebar.</i></html>");
        noteLabel.setFont(new Font("Monospaced", Font.ITALIC, 10));
        noteLabel.setForeground(Main.FG_DIM);
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        noteLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        form.add(formTitle);
        form.add(Box.createVerticalStrut(14));
        form.add(Main.formRow("EMPLOYEE ID", idField));
        form.add(Box.createVerticalStrut(10));
        form.add(Main.formRow("NAME", nameField));
        form.add(Box.createVerticalStrut(10));
        form.add(Main.formRow("PASSWORD", passField));
        form.add(Box.createVerticalStrut(10));
        form.add(Main.labelFor("ROLE"));
        form.add(Box.createVerticalStrut(4));
        form.add(radioRow);
        form.add(Box.createVerticalStrut(14));
        form.add(addBtn);
        form.add(Box.createVerticalStrut(8));
        form.add(delBtn);
        form.add(Box.createVerticalStrut(18));
        form.add(noteLabel);
        form.add(Box.createVerticalGlue());

        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setOpaque(false);
        center.add(searchBar, BorderLayout.NORTH);
        center.add(sp,        BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);
        root.add(form,   BorderLayout.EAST);
        return root;
    }

    // ── Refresh table from employees list ─────────────────────────────
    void refreshTable() {
        tableModel.setRowCount(0);
        for (Employee e : employees)
            tableModel.addRow(new Object[]{e.id, e.name, e.type});
    }

    static JRadioButton styledRadio(String text, Color color) {
        JRadioButton rb = new JRadioButton(text);
        rb.setFont(Main.FONT_SM);
        rb.setForeground(color);
        rb.setOpaque(false);
        rb.setFocusPainted(false);
        return rb;
    }

    static void showErr(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ── Legacy console methods kept for compatibility ──────────────────
    public void menu() {}

    public void saveEmployeesToFile() {
        try {
            PrintWriter w = new PrintWriter("employees.txt");
            w.println("================================================");
            w.println("          SAUDI MARKET — EMPLOYEE RECORDS");
            w.println("================================================");
            w.println("  Total employees: " + employees.size());
            w.println("================================================");
            w.println();
            int idx = 1;
            for (Employee e : employees) {
                w.println("  Employee #" + idx++);
                w.println("  ----------------------------------------");
                w.println("  ID       : " + e.id);
                w.println("  Name     : " + e.name);
                w.println("  Password : " + e.password);
                w.println("  Role     : " + e.type.toUpperCase());
                w.println();
            }
            w.println("================================================");
            w.println("  END OF RECORDS");
            w.println("================================================");
            w.close();

            // machine-readable copy for loading (hidden, used internally)
            PrintWriter raw = new PrintWriter("employees_data.txt");
            for (Employee e : employees)
                raw.println(e.id + "|" + e.name + "|" + e.password + "|" + e.type);
            raw.close();
        } catch (Exception e) { System.out.println("Error saving employees"); }
    }

    public void loadEmployeesFromFile() {
        try {
            // prefer machine-readable file; fall back to old comma format
            java.io.File raw = new java.io.File("employees_data.txt");
            java.io.File old = new java.io.File("employees.txt");
            java.io.File src = raw.exists() ? raw : old;
            if (!src.exists()) return;
            Scanner fr = new Scanner(src);
            while (fr.hasNextLine()) {
                String line = fr.nextLine().trim();
                if (line.isEmpty() || line.startsWith("=") || line.startsWith(" ")) continue;
                String[] d = line.split(raw.exists() ? "\\|" : ",");
                if (d.length < 4) continue;
                try {
                    employees.add(new Employee(Integer.parseInt(d[0].trim()), d[1].trim(), d[2].trim(), d[3].trim()));
                } catch (NumberFormatException ignored) {}
            }
            fr.close();
        } catch (Exception e) { System.out.println("Error loading employees"); }
    }

    public void saveCredentials() {
        try {
            PrintWriter w = new PrintWriter("admin_config.txt");
            w.println("================================================");
            w.println("          SAUDI MARKET — ADMIN CREDENTIALS");
            w.println("================================================");
            w.println("  Username : " + adminUsername);
            w.println("  Password : " + adminPassword);
            w.println("================================================");
            w.close();
            PrintWriter raw = new PrintWriter("admin_config_data.txt");
            raw.println(adminUsername + "|" + adminPassword);
            raw.close();
        } catch (Exception e) { System.out.println("Error saving admin credentials"); }
    }

    public void loadCredentials() {
        try {
            java.io.File f = new java.io.File("admin_config_data.txt");
            if (!f.exists()) return;
            Scanner sc = new Scanner(f);
            if (sc.hasNextLine()) {
                String[] d = sc.nextLine().split("\\|");
                if (d.length >= 2) { adminUsername = d[0].trim(); adminPassword = d[1].trim(); }
            }
            sc.close();
        } catch (Exception e) { System.out.println("Error loading admin credentials"); }
    }
}