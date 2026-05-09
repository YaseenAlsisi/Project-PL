import java.util.*;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        //  CREATE ONCE (FIX: no data loss)
        UserModule user = new UserModule();
        AdminModule admin = new AdminModule();
        InventoryModule inventory = new InventoryModule();
        MarketingModule marketing = new MarketingModule();
        SalesModule sales = new SalesModule();

        System.out.println("\n====================================");
        System.out.println(" HYPER MARKET MANAGEMENT SYSTEM");
        System.out.println("====================================");

        while (true) {

            System.out.print("\n Enter Username: ");
            String username = sc.nextLine();

            System.out.print(" Enter Password: ");
            String password = sc.nextLine();

            System.out.print(" Enter Role (admin/user): ");
            String role = sc.nextLine();

            boolean valid = false;

            if (role.equals("admin")) {
                if (username.equals("admin") && password.equals("1234")) valid = true;
            } else if (role.equals("user")) {
                if (username.equals("user") && password.equals("1111")) valid = true;
            }

            if (!valid) {
                System.out.println("\n❌ INVALID CREDENTIALS");
                continue;
            }

            System.out.println("\n✔ LOGIN SUCCESSFUL");
            user.login(username);

            boolean running = true;

            while (running) {

                System.out.println("\n====================================");
                System.out.println(" MAIN MENU");
                System.out.println("====================================");

                System.out.println("1 User/Admin Panel");
                System.out.println("2 Inventory Module");
                System.out.println("3 Marketing / Sales");
                System.out.println("4 Logout");
                System.out.println("5 Exit System");

                System.out.print("\n--> Choose option: ");
                int choice = sc.nextInt();
                sc.nextLine(); // IMPORTANT FIX (buffer)

                switch (choice) {

                    case 1:
                        if (role.equals("admin")) admin.menu();
                        else user.showHistory();
                        break;

                    case 2:
                        if (role.equals("admin")) inventory.menu();
                        else System.out.println("❌ Access Denied");
                        break;

                    case 3:
                        if (role.equals("admin"))
                            marketing.menu(inventory.getProducts());
                        else
                            sales.menu(inventory.getProducts(), user);
                        break;

                    case 4:
                        System.out.println(" Logging out...");
                        running = false;
                        break;

                    case 5:
                        System.out.println(" System shutting down...");
                        return;
                }
            }
        }
    }
}