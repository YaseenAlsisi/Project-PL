import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class Main { // "El goz' da bey-set el alwan wel fonts (el visual style) 
// we bey-jahiz el folders elly fiha kol data el system we men elly logged in.

    // ── Palette ──────────────────────────────────────────────────────────────
    static final Color BG        = new Color(8,   10,  18);
    static final Color SURFACE   = new Color(14,  17,  30);
    static final Color CARD      = new Color(20,  25,  42);
    static final Color CARD2     = new Color(26,  32,  54);
    static final Color ACCENT    = new Color(0,   210, 160);   // vivid teal
    static final Color ACCENT2   = new Color(120,  90, 255);   // neon violet
    static final Color DANGER    = new Color(255,  55,  80);
    static final Color WARNING   = new Color(255, 185,  40);
    static final Color FG        = new Color(215, 225, 245);
    static final Color FG_DIM    = new Color(90,  108, 140);
    static final Color BORDER    = new Color(35,  45,  72);
    static final Color BORDER_LT = new Color(50,  65,  95);

    static final Font FONT_BIG  = new Font("Monospaced", Font.BOLD, 24);
    static final Font FONT_MED  = new Font("Monospaced", Font.BOLD, 13);
    static final Font FONT_SM   = new Font("Monospaced", Font.PLAIN, 12);
    static final Font FONT_XS   = new Font("Monospaced", Font.PLAIN, 10);

    // ── Global modules ───────────────────────────────────────────────────────
    static UserModule      user;
    static AdminModule     admin;
    static InventoryModule inventory;
    static MarketingModule marketing;
    static SalesModule     sales;
    static String          currentRole;

    // ── Globals also track logged-in display name ────────────────────────────
    static String currentUser;

    public static void main(String[] args) { // De el bedaya elly bteftah kol el modules we bt-call el login frame 'ala shan el app yebda' yeshtaghal.
        user      = new UserModule();
        admin     = new AdminModule();
        inventory = new InventoryModule();
        marketing = new MarketingModule();
        sales     = new SalesModule();
        SwingUtilities.invokeLater(Main::showLoginFrame);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  LOGIN SCREEN
    // ═════════════════════════════════════════════════════════════════════════
    static void showLoginFrame() { // This part of the code creates and displays the login window with a specific size, title, and layout.
        JFrame f = new JFrame("Saudi Market — Login");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(480, 590);
        f.setLocationRelativeTo(null);
        f.setResizable(false);
        f.setContentPane(buildLoginPanel(f));
        f.setVisible(true);
    }

    static JPanel buildLoginPanel(JFrame frame) {// El goz' da beyersem shakl el login screen b-alwan we effects modern, 
    // we fih el logic elly bey-check el username wel password wel role 'ala shan yeda5al el user lel system."
        JPanel root = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(25, 33, 55, 80));
                for (int x = 0; x < getWidth(); x += 36)  g2.drawLine(x, 0, x, getHeight());
                for (int y = 0; y < getHeight(); y += 36) g2.drawLine(0, y, getWidth(), y);
                RadialGradientPaint teal = new RadialGradientPaint(240, 180, 280,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0, 210, 160, 35), new Color(0, 0, 0, 0)});
                g2.setPaint(teal);
                g2.fillOval(-40, 40, 560, 480);
                RadialGradientPaint violet = new RadialGradientPaint(420, 400, 180,
                    new float[]{0f, 1f},
                    new Color[]{new Color(120, 90, 255, 20), new Color(0, 0, 0, 0)});
                g2.setPaint(violet);
                g2.fillOval(240, 220, 360, 360);
            }
        };
        root.setOpaque(true);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(18, 22, 38));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0, 210, 160, 80), 1),
            new EmptyBorder(40, 48, 40, 48)
        ));
        card.setPreferredSize(new Dimension(388, 510));
        card.setOpaque(true);

        JLabel logo = new JLabel("⬡ SAUDI MARKET", SwingConstants.CENTER);
        logo.setFont(FONT_BIG);
        logo.setForeground(ACCENT);

        JLabel sub = new JLabel("MANAGEMENT SYSTEM", SwingConstants.CENTER);
        sub.setFont(FONT_XS);
        sub.setForeground(new Color(0, 210, 160, 120));

        JTextField userField = styledField("Username");
        JPasswordField passField = styledPassField("Password");

        DefaultComboBoxModel<String> roleModel = new DefaultComboBoxModel<>(new String[]{"admin", "user", "employee", "manager"});
        JComboBox<String> roleCombo = new JComboBox<>(roleModel);
        styleCombo(roleCombo);

        JButton loginBtn = glowButton("LOGIN", ACCENT);

        JLabel status = new JLabel(" ", SwingConstants.CENTER);
        status.setFont(FONT_SM);
        status.setForeground(DANGER);

        loginBtn.addActionListener(e -> {
            String u    = userField.getText().trim();
            String p    = new String(passField.getPassword()).trim();
            String role = (String) roleModel.getSelectedItem();
            boolean ok  = false;
            // Check admin credentials
            if ("admin".equals(role) && u.equals(admin.adminUsername) && p.equals(admin.adminPassword)) {
                ok = true; currentRole = "admin"; currentUser = u;
            }
            // Check default user credentials
            if ("user".equals(role) && u.equals(user.userUsername) && p.equals(user.userPassword)) {
                ok = true; currentRole = "user"; currentUser = u;
            }
            // Check employee list — any role match
            if (!ok) {
                for (Employee emp : admin.employees) {
                    if (u.equals(emp.name) && p.equals(emp.password) && role.equals(emp.type)) {
                        ok = true; currentRole = emp.type; currentUser = emp.name; break;
                    }
                }
            }
            if (ok) {
                user.login(currentUser);
                frame.dispose();
                showMainFrame();
            } else {
                status.setText("❌  INVALID CREDENTIALS");
                passField.setText("");
            }
        });

        passField.addActionListener(loginBtn.getActionListeners()[0]);
        userField.addActionListener(e -> passField.requestFocus());

        addRow(card, logo);           addVGap(card, 3);
        addRow(card, sub);            addVGap(card, 20);
        addRow(card, thinDivider());  addVGap(card, 24);
        addRow(card, fieldBlock("USERNAME", userField));  addVGap(card, 14);
        addRow(card, fieldBlock("PASSWORD", passField));  addVGap(card, 14);
        addRow(card, fieldBlock("ROLE",     roleCombo));  addVGap(card, 28);
        addRow(card, loginBtn);       addVGap(card, 10);
        addRow(card, status);

        root.add(card);
        return root;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  MAIN DASHBOARD
    // ═════════════════════════════════════════════════════════════════════════
    static void showMainFrame() { // El goz' da beyfta7 el shasha el ra'iseya (el Dashboard) b-hagm kbir we bey-zher fi el
    //  'enwan esm el user we el (role) 3ala shan yebda' yesta5dem el system
        JFrame f = new JFrame("Saudi Market — Dashboard  [" + currentUser + "  |  " + currentRole.toUpperCase() + "]");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1180, 760);
        f.setLocationRelativeTo(null);
        f.setContentPane(buildDashboard(f));
        f.setVisible(true);
    }

    static JPanel buildDashboard(JFrame frame) {//"El goz' da beyebni el Dashboard we bey'sem el shasha le goz'en: sidebar fih el zorar bta'et el navigation,
    //  we goz' ra'isi bey-shill el panels elly btefta7 'ala hasab el role (admin aw user) 'ala shan kol wahed yeshof elly masmo7 lo bih."
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        // ── Sidebar ──────────────────────────────────────────────────────────
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(SURFACE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0, 210, 160, 35));
                g2.fillRect(getWidth() - 1, 0, 1, getHeight());
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(28, 14, 24, 14));

        JLabel brand = new JLabel("⬡ SAUDI", SwingConstants.CENTER);
        brand.setFont(new Font("Monospaced", Font.BOLD, 20));
        brand.setForeground(ACCENT);

        JLabel brandSub = new JLabel("MARKET", SwingConstants.CENTER);
        brandSub.setFont(FONT_XS);
        brandSub.setForeground(new Color(0, 210, 160, 100));

        JPanel content = new JPanel(new CardLayout());
        content.setBackground(BG);

        JPanel homePanel       = buildHomePanel();
        JPanel adminPanel      = AdminModule.buildAdminPanel(admin);
        JPanel inventoryPanel  = InventoryModule.buildInventoryPanel(inventory);
        JPanel marketingPanel  = MarketingModule.buildMarketingPanel(marketing, inventory);
        JPanel salesPanel      = SalesModule.buildSalesPanel(sales, inventory, user, marketing);
        JPanel historyPanel    = buildHistoryPanel();
        JPanel changePassPanel = buildChangePasswordPanel();

        content.add(homePanel,       "home");
        content.add(adminPanel,      "admin");
        content.add(inventoryPanel,  "inventory");
        content.add(marketingPanel,  "marketing");
        content.add(salesPanel,      "sales");
        content.add(historyPanel,    "history");
        content.add(changePassPanel, "changepass");

        CardLayout cl = (CardLayout) content.getLayout();

        addRow(sidebar, brand);         addVGap(sidebar, 2);
        addRow(sidebar, brandSub);      addVGap(sidebar, 12);
        addRow(sidebar, thinDivider()); addVGap(sidebar, 14);
        addRow(sidebar, makeBadge(currentRole.toUpperCase(),
            "admin".equals(currentRole) ? ACCENT2 : ACCENT));
        addVGap(sidebar, 22);

        boolean isAdmin   = "admin".equals(currentRole);
        boolean isManager = "manager".equals(currentRole);

        if (isAdmin || isManager) {
            String[][] nav = {
                {"🏠","Home","home"},{"👤","Admin","admin"},{"📦","Inventory","inventory"},
                {"📢","Marketing","marketing"},{"🛒","Sales","sales"},
                {"📋","History","history"},{"🔑","Account Settings","changepass"}
            };
            for (String[] i : nav) addNavBtn(sidebar, cl, content, historyPanel, i[0], i[1], i[2]);
        } else {
            String[][] nav = {
                {"🛒","Sales","sales"},{"📋","My History","history"},{"🔑","Account Settings","changepass"}
            };
            for (String[] i : nav) addNavBtn(sidebar, cl, content, historyPanel, i[0], i[1], i[2]);
            cl.show(content, "sales");
        }

        sidebar.add(Box.createVerticalGlue());
        addRow(sidebar, thinDivider()); addVGap(sidebar, 12);

        JButton logoutBtn = glowButton("↩  LOGOUT", DANGER);
        logoutBtn.addActionListener(e -> { frame.dispose(); showLoginFrame(); });
        addRow(sidebar, logoutBtn);

        root.add(sidebar, BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);
        return root;
    }

    //El goz' da bey'mel function bet-create zorar lel navigation, 
    // we kol ma btdos 'aleh bey-refresh el data bta'et el sales aw el history we yezherlak el panel el sa7
    static void addNavBtn(JPanel sidebar, CardLayout cl, JPanel content,
                          JPanel historyPanel, String icon, String label, String card) {
        JButton btn = navButton(icon + "  " + label);
        btn.addActionListener(e -> {
            if (card.equals("sales"))   SalesModule.refreshSalesTable(inventory);
            if (card.equals("history")) refreshHistory(historyPanel);
            cl.show(content, card);
        });
        addRow(sidebar, btn);
        addVGap(sidebar, 3);
    }


    //El goz' da bey-refresh el history 'ala el shasha,
    //  we fih logic bey-خلي el admin yashouf kol el a7dath, laken el user el 'adi yashouf el history bta'o hoa bs
    static void refreshHistory(JPanel historyPanel) { 
        boolean isAdminOrManager = "admin".equals(currentRole) || "manager".equals(currentRole);
        for (Component c : historyPanel.getComponents()) {
            if (c instanceof JScrollPane sp) {
                if (sp.getViewport().getView() instanceof JTextArea ta) {
                    StringBuilder sb = new StringBuilder();
                    for (String h : user.history) {
                        // For regular users/employees: only show entries containing their username
                        if (isAdminOrManager || h.contains(currentUser)) {
                            sb.append("  ▸  ").append(h).append("\n");
                        }
                    }
                    ta.setText(sb.length() > 0 ? sb.toString() : "  (no activity yet)");
                }
            }
        }
    }

    // ── Static home-card count labels — updated instantly from any module ──

    //El goz' da bey-update el arkam elly mawgoda 'ala el Home screen 'ala shan te-show 'adad el products,
    //  el employees, we el offers elly f-el system el delwa'ti
    static JLabel homeProductCountLbl;
    static JLabel homeEmployeeCountLbl;
    static JLabel homeOfferCountLbl;

    static void updateHomeCounts() {
        if (homeProductCountLbl  != null) homeProductCountLbl.setText(String.valueOf(inventory  != null ? inventory.products.size()  : 0));
        if (homeEmployeeCountLbl != null) homeEmployeeCountLbl.setText(String.valueOf(admin     != null ? admin.employees.size()     : 0));
        if (homeOfferCountLbl    != null) homeOfferCountLbl.setText(String.valueOf(marketing   != null ? marketing.offers.size()    : 0));
    }

    // ── Home Panel ───────────────────────────────────────────────────────────

    //El goz' da beyebni el shasha el ra'iseya (Home panel)
    //  elly fih kelmet welcome lel user we cards fihom 'adad el products wel employees wel offers elly f-el system.
    static JPanel buildHomePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 16, 10, 16);
        g.fill   = GridBagConstraints.BOTH;

        JLabel title = new JLabel("WELCOME BACK, " + (currentUser != null ? currentUser.toUpperCase() : ""), SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 38));
        title.setForeground(ACCENT);

        JLabel sub = new JLabel("Select a module from the sidebar to get started.", SwingConstants.CENTER);
        sub.setFont(FONT_SM);
        sub.setForeground(FG_DIM);

        // Live count labels — updated instantly via static refs
        homeProductCountLbl  = new JLabel("0", SwingConstants.CENTER);
        homeEmployeeCountLbl = new JLabel("0", SwingConstants.CENTER);
        homeOfferCountLbl    = new JLabel("0", SwingConstants.CENTER);
        JLabel productCountLbl  = homeProductCountLbl;
        JLabel employeeCountLbl = homeEmployeeCountLbl;
        JLabel offerCountLbl    = homeOfferCountLbl;
        productCountLbl.setFont(new Font("Monospaced", Font.BOLD, 28));
        productCountLbl.setForeground(ACCENT);
        employeeCountLbl.setFont(new Font("Monospaced", Font.BOLD, 28));
        employeeCountLbl.setForeground(ACCENT2);
        offerCountLbl.setFont(new Font("Monospaced", Font.BOLD, 28));
        offerCountLbl.setForeground(WARNING);

        updateHomeCounts(); // populate immediately

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 18, 0));
        statsRow.setOpaque(false);
        statsRow.add(statCard("PRODUCTS",  "📦", productCountLbl,  ACCENT));
        statsRow.add(statCard("EMPLOYEES", "👥", employeeCountLbl, ACCENT2));
        statsRow.add(statCard("OFFERS",    "🏷️", offerCountLbl,    WARNING));

        g.gridx=0; g.gridy=0; g.weightx=1; g.weighty=0.1;  p.add(title, g);
        g.gridy=1;             g.weighty=0.05;               p.add(sub, g);
        g.gridy=2;             g.weighty=0.3;                p.add(statsRow, g);
        return p;
    }

    static JPanel statCard(String label, String icon, JLabel numLbl, Color color) {
        JPanel c = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                GradientPaint gp = new GradientPaint(0, 0,
                    new Color(color.getRed(), color.getGreen(), color.getBlue(), 45),
                    0, getHeight(),
                    new Color(color.getRed(), color.getGreen(), color.getBlue(), 0));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        c.setOpaque(false);
        c.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 85), 1),
            new EmptyBorder(24, 20, 20, 20)
        ));
        JLabel ic = new JLabel(icon, SwingConstants.CENTER);
        ic.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));

        JLabel lb = new JLabel(label, SwingConstants.CENTER);
        lb.setFont(FONT_MED);
        lb.setForeground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 180));

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        centerPanel.setOpaque(false);
        centerPanel.add(ic);
        centerPanel.add(numLbl);

        c.add(centerPanel, BorderLayout.CENTER);
        c.add(lb, BorderLayout.SOUTH);
        return c;
    }

    // ── History Panel ────────────────────────────────────────────────────────
//El goz' da beyebni shakl el 'cards' elly btezhre el e7sayat,
//  we bey-design kol card b-shakl modern fih icon we gradient colors we el arkam bta'et el system
    static JPanel buildHistoryPanel() { 
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(28, 28, 28, 28));

        boolean isAdminOrManager = "admin".equals(currentRole) || "manager".equals(currentRole);
        String panelTitle = isAdminOrManager ? "📋  ACTIVITY HISTORY  (ALL USERS)" : "📋  MY ACTIVITY HISTORY";
        p.add(sectionTitle(panelTitle), BorderLayout.NORTH);

        JTextArea ta = new JTextArea("  (nothing yet)");
        ta.setEditable(false);
        ta.setBackground(CARD);
        ta.setForeground(ACCENT);
        ta.setFont(FONT_SM);
        ta.setBorder(new EmptyBorder(14, 14, 14, 14));
        JScrollPane sp = new JScrollPane(ta);
        styleScroll(sp);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ── Change Password Panel ────────────────────────────────────────────────

//"El goz' da beyebni shasha el Account Settings elly bte-allow lel user (admin aw employee aw user 'adi)
//  eno yeghayar el username aw el password bto'o b-shakl amen we bey-save el data el gedida f-el files.
    static JPanel buildChangePasswordPanel() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(120, 90, 255, 100), 1),
            new EmptyBorder(32, 48, 32, 48)
        ));
        card.setPreferredSize(new Dimension(480, 660));

        JLabel title = new JLabel("🔑  ACCOUNT SETTINGS", SwingConstants.CENTER);
        title.setFont(FONT_BIG);
        title.setForeground(ACCENT2);

        String roleDisplay = "admin".equals(currentRole) ? "Admin Account"
                           : "manager".equals(currentRole) ? "Manager Account"
                           : "employee".equals(currentRole) ? "Employee Account" : "User Account";
        JLabel sub = new JLabel(roleDisplay, SwingConstants.CENTER);
        sub.setFont(FONT_XS);
        sub.setForeground(FG_DIM);

        // ── Change Username section ──────────────────────────────────
        JLabel userSectionLbl = new JLabel("── CHANGE USERNAME ──");
        userSectionLbl.setFont(FONT_SM);
        userSectionLbl.setForeground(ACCENT);
        userSectionLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField newUserField = styledField("New username");
        JPasswordField userPassConfirm = styledPassField("Current password (to confirm)");

        JLabel userStatus = new JLabel(" ", SwingConstants.CENTER);
        userStatus.setFont(FONT_SM);

        JButton changeUserBtn = glowButton("💾  SAVE USERNAME", ACCENT);
        changeUserBtn.addActionListener(e -> {
            String newU  = newUserField.getText().trim();
            String confP = new String(userPassConfirm.getPassword()).trim();
            if (newU.isEmpty()) { setStatus(userStatus, "❌  Username cannot be empty.", DANGER); return; }
            if ("admin".equals(currentRole)) {
                if (!confP.equals(admin.adminPassword)) { setStatus(userStatus, "❌  Wrong password.", DANGER); return; }
                admin.adminUsername = newU;
                currentUser = newU;
                admin.saveCredentials();
                setStatus(userStatus, "✔  Username updated to: " + newU, ACCENT);
            } else if ("employee".equals(currentRole) || "manager".equals(currentRole)) {
                // Find employee record and update
                Employee found = null;
                for (Employee emp : admin.employees) {
                    if (emp.name.equals(currentUser) && confP.equals(emp.password)) { found = emp; break; }
                }
                if (found == null) { setStatus(userStatus, "❌  Wrong password.", DANGER); return; }
                found.name = newU;
                currentUser = newU;
                admin.saveEmployeesToFile();
                setStatus(userStatus, "✔  Username updated to: " + newU, ACCENT);
            } else {
                if (!confP.equals(user.userPassword)) { setStatus(userStatus, "❌  Wrong password.", DANGER); return; }
                user.userUsername = newU;
                currentUser = newU;
                user.saveCredentials();
                setStatus(userStatus, "✔  Username updated to: " + newU, ACCENT);
            }
            newUserField.setText(""); userPassConfirm.setText("");
        });

        // ── Change Password section ──────────────────────────────────
        JLabel passSectionLbl = new JLabel("── CHANGE PASSWORD ──");
        passSectionLbl.setFont(FONT_SM);
        passSectionLbl.setForeground(ACCENT2);
        passSectionLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField oldField = styledPassField("Current password");
        JPasswordField newField = styledPassField("New password");
        JPasswordField cfmField = styledPassField("Confirm new password");

        JLabel passStatus = new JLabel(" ", SwingConstants.CENTER);
        passStatus.setFont(FONT_SM);

        JButton changeBtn = glowButton("💾  SAVE PASSWORD", ACCENT2);
        changeBtn.addActionListener(e -> {
            String oldP = new String(oldField.getPassword()).trim();
            String newP = new String(newField.getPassword()).trim();
            String cfmP = new String(cfmField.getPassword()).trim();
            if (newP.isEmpty())     { setStatus(passStatus, "❌  New password cannot be empty.", DANGER); return; }
            if (!newP.equals(cfmP)) { setStatus(passStatus, "❌  Passwords do not match.", DANGER); return; }
            if ("admin".equals(currentRole)) {
                if (!oldP.equals(admin.adminPassword)) { setStatus(passStatus, "❌  Wrong current password.", DANGER); return; }
                admin.adminPassword = newP;
                admin.saveCredentials();
                setStatus(passStatus, "✔  Admin password updated!", ACCENT);
            } else if ("employee".equals(currentRole) || "manager".equals(currentRole)) {
                // Find employee record and update
                Employee found = null;
                for (Employee emp : admin.employees) {
                    if (emp.name.equals(currentUser) && oldP.equals(emp.password)) { found = emp; break; }
                }
                if (found == null) { setStatus(passStatus, "❌  Wrong current password.", DANGER); return; }
                found.password = newP;
                admin.saveEmployeesToFile();
                setStatus(passStatus, "✔  Password updated!", ACCENT);
            } else {
                if (!oldP.equals(user.userPassword)) { setStatus(passStatus, "❌  Wrong current password.", DANGER); return; }
                user.userPassword = newP;
                user.saveCredentials();
                setStatus(passStatus, "✔  Password updated!", ACCENT);
            }
            oldField.setText(""); newField.setText(""); cfmField.setText("");
        });

        addRow(card, title);          addVGap(card, 4);
        addRow(card, sub);            addVGap(card, 14);
        addRow(card, thinDivider());  addVGap(card, 14);

        addRow(card, userSectionLbl); addVGap(card, 8);
        addRow(card, fieldBlock("NEW USERNAME", newUserField));              addVGap(card, 10);
        addRow(card, fieldBlock("CONFIRM WITH PASSWORD", userPassConfirm));  addVGap(card, 12);
        addRow(card, changeUserBtn);  addVGap(card, 6);
        addRow(card, userStatus);     addVGap(card, 14);

        addRow(card, thinDivider());  addVGap(card, 14);

        addRow(card, passSectionLbl); addVGap(card, 8);
        addRow(card, fieldBlock("CURRENT PASSWORD", oldField)); addVGap(card, 10);
        addRow(card, fieldBlock("NEW PASSWORD",     newField)); addVGap(card, 10);
        addRow(card, fieldBlock("CONFIRM PASSWORD", cfmField)); addVGap(card, 12);
        addRow(card, changeBtn); addVGap(card, 8);
        addRow(card, passStatus);

        root.add(card);
        return root;
    }

    static void setStatus(JLabel lbl, String msg, Color color) {
        lbl.setText(msg); lbl.setForeground(color);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SHARED WIDGET HELPERS
    // ═════════════════════════════════════════════════════════════════════════


//El goz' da beyعمل 'block' fih label (esm el 7aga) we ta7toh el input field b-shakl monazzam
//  'ala shan yeb'a shakl el form mertb we sahl f-el isti5dam
    public static JPanel fieldBlock(String labelText, JComponent field) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_XS);
        lbl.setForeground(FG_DIM);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        block.add(lbl);
        block.add(Box.createVerticalStrut(5));
        block.add(field);
        return block;
    }


//El goz' da beydmn en kol component (zay el zorar aw el kelma) yet-resep f-el panel b-shakl monazzam,
//  we bey-5alihom kolohom yebdo men el shemal we ya5do el 'ard (width) el monaseb men gher ma el toul (height) yeboz.
    public static void addRow(JPanel panel, JComponent comp) {
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (!(comp instanceof Box.Filler)) {
            Dimension pref = comp.getPreferredSize();
            if (pref != null && pref.height > 0)
                comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height + 2));
        }
        panel.add(comp);
    }
//El goz' da bey-seeb masafa fadiya (gap) bel-toul ben el components we ba'daha,
//  'ala shan el shakl mayeb'ash zahma we el kalam may-elza'sh f-ba'do.
    public static void addVGap(JPanel panel, int gap) {
        panel.add(Box.createVerticalStrut(gap));
    }

//"El goz' da beyعمل design gedid le ay makan btektib fih (text field), bey-set el alwan wel fonts, 
// we bey-zher 'placeholder' (kalam fadi) toul ma enta lesa makatabtish 7aga 'ala shan te'raf el mafroud tekteb eh
    public static JTextField styledField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(FG_DIM);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, 10, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                }
            }
        };
        f.setBackground(SURFACE);
        f.setForeground(FG);
        f.setCaretColor(ACCENT);
        f.setFont(FONT_SM);
        f.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_LT, 1),
            new EmptyBorder(7, 10, 7, 10)
        ));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return f;
    }

//El goz' da zay elly abloh belzabt, bs da makhsous lel password.
//  Bey-khali el kalam mista5abi we bey-set el colors wel border 'ala shan yeb'a shaklo modern we monaseb le ba'et el design
    public static JPasswordField styledPassField(String placeholder) {
        JPasswordField f = new JPasswordField();
        f.setBackground(SURFACE);
        f.setForeground(FG);
        f.setCaretColor(ACCENT);
        f.setFont(FONT_SM);
        f.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_LT, 1),
            new EmptyBorder(7, 10, 7, 10)
        ));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return f;
    }

//El goz' da bey-zabat shakl el drop-down menu (el combo box) 'ala shan yeb'a labe' ma' el design. B
// ey-ghayar el alwan wel fonts we bey-5ali el menu (mesh shaffaf) 'ala shan el options teb'a bayna we el kalam yeb'a wade7.
    public static void styleCombo(JComboBox<?> cb) {
        cb.setBackground(SURFACE);
        cb.setForeground(FG);
        cb.setFont(FONT_SM);
        cb.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_LT, 1),
            new EmptyBorder(4, 6, 4, 6)
        ));
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        if (cb.getRenderer() instanceof JComponent jc) jc.setOpaque(true);
    }

//El goz' da beyebni zorar shaklo modern we fih 'glow effect'. Lama btmarrer el mouse fo' el zorar (rollover), lonoh bey-enwar (brighten) we bey-zher hawaleh 'glow' shaffaf,
//  we lama btdous 'aleh bey-eghma' (darker). Bey-5ali el design shaklo interactive we 7elw
    public static JButton glowButton(String text, Color color) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed() ? color.darker()
                           : getModel().isRollover() ? color.brighter() : color;
                if (getModel().isRollover()) {
                    g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 38));
                    g2.fillRoundRect(-3, -3, getWidth()+6, getHeight()+6, 14, 14);
                }
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BG);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        b.setFont(FONT_MED);
        b.setForeground(BG);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setPreferredSize(new Dimension(200, 40));
        return b;
    }


// El goz' da beyebni zorar el navigation elly f-el sidebar. Lama btmarrer el mouse 'aleh (hover), beyzher khat soghayar keda bel-loon el green (ACCENT) 'ala el shemal we el khalfeya bte-be'a shaffafa keda, 
// 'ala shan ye-bayenlak en el zorar da enta wa'ef 'aleh delwa'ti.
    static JButton navButton(String text) { 
        JButton b = new JButton(text) {
            boolean hov = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hov || getModel().isPressed()) {
                    g2.setColor(new Color(0, 210, 160, 22));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7);
                    g2.setColor(ACCENT);
                    g2.fillRoundRect(0, 5, 3, getHeight()-10, 3, 3);
                    g2.setColor(ACCENT);
                } else {
                    g2.setColor(FG_DIM);
                }
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), 14, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        b.setFont(FONT_SM);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        b.setPreferredSize(new Dimension(192, 36));
        return b;
    }

//El goz' da beyebni 'label' (kelma aw 'enwan soghayar), bey-set el font wel loon elly beykon f-el khalfeya shwaya (dimmed)
//  'ala shan yesta5demoh fo' el input fields aw f-ay makan mo7tag kelma tawde7eya basita
    public static JLabel labelFor(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_XS);
        l.setForeground(FG_DIM);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

//El goz' da beyebni 'enwan ra'isi (Section Title) lel-ajza' elly f-el shasha. Bey-khali el font kbir we 'Monospaced' we b-loon el ACCENT el akhdar,
//  we bey-seeb masafa ta7toh 'ala shan yefsel el 'enwan 'an el kalam elly gaya ta7toh
    public static JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Monospaced", Font.BOLD, 17));
        l.setForeground(ACCENT);
        l.setBorder(new EmptyBorder(0, 0, 14, 0));
        return l;
    }


//Da bey-rsem khat rafi' gedan (1 pixel) 'ala shan yefsel ben el sections b-shakl sheyak we mayekonsh za7ma.
    static JPanel thinDivider() {
        JPanel d = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BORDER);
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        d.setOpaque(false);
        d.setPreferredSize(new Dimension(Integer.MAX_VALUE, 1));
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        d.setAlignmentX(Component.LEFT_ALIGNMENT);
        return d;
    }

//Da bey-khla' "badge" modawar (rounded) fih kelma we ganbeha no'ta (dot), 
// zay masalan "● Active" aw "● Pending". Bey-yeb'a leh background shaffafa shwaya b-nafs el loon elly enta bet-ekhtaroh.
    static JPanel makeBadge(String text, Color color) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 28));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            }
        };
        badge.setOpaque(false);
        JLabel dot = new JLabel("● " + text);
        dot.setFont(FONT_XS);
        dot.setForeground(color);
        badge.add(dot);
        badge.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        return badge;
    }


//Hena enta bet-ghayar shakl el Scroll Bar el 'adi bta' Swing we bet-khalih modern.
//  Enta shelte el zorayer elly fo' we ta7t (arrows) we khalet el loon yeb'a labe' ma' el "Dark Theme" bta' el application.
    public static void styleScroll(JScrollPane sp) {
        sp.setBorder(new LineBorder(BORDER, 1));
        sp.getViewport().setBackground(CARD);
        sp.getVerticalScrollBar().setBackground(SURFACE);
        sp.getHorizontalScrollBar().setBackground(SURFACE);
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(50, 65, 95);
                trackColor = SURFACE;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            JButton zeroBtn() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
        });
    }


//El goz' da bey-zabat shakl el Tables. Bey-khali el rows height akbar shwaya (30px), 
// bey-shel el lines el vertical, we bey-khali el row elly enta bet-ekhtaroh (selection) yeb'a leh loon akhdar (ACCENT) shaffaf.
    public static JTable buildStyledTable(javax.swing.table.TableModel model) {
        JTable t = new JTable(model);
        t.setBackground(CARD);
        t.setForeground(FG);
        t.setFont(FONT_SM);
        t.setRowHeight(30);
        t.setGridColor(new Color(32, 42, 66));
        t.setSelectionBackground(new Color(0, 210, 160, 55));
        t.setSelectionForeground(ACCENT);
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.getTableHeader().setBackground(SURFACE);
        t.getTableHeader().setForeground(ACCENT2);
        t.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 11));
        t.getTableHeader().setBorder(new LineBorder(BORDER, 1));
        t.getTableHeader().setPreferredSize(new Dimension(0, 32));
        return t;
    }


//Da zay el fieldBlock, 
// bey-rattib el label fo' el input field b-masafa sabet (4px) 'ala shan kol el forms f-el system yeb'a leha nafs el layout.
    public static JPanel formRow(String label, JComponent field) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        JLabel lbl = labelFor(label);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row.add(lbl);
        row.add(Box.createVerticalStrut(4));
        row.add(field);
        return row;
    }
}