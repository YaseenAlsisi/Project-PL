import java.util.*;

public class SalesModule {

    Scanner sc = new Scanner(System.in);

    public void menu(ArrayList<Product> products, UserModule user) {

        while (true) {

            System.out.println("\n SALES");
            System.out.println("1. Make Order");
            System.out.println("2. Back");
            System.out.print("--> Choice: ");
            int c = sc.nextInt();

            if (c == 1) makeOrder(products, user);
            else break;
        }
    }

    public void makeOrder(ArrayList<Product> products, UserModule user) {

        // 🛒 AVAILABLE PRODUCTS MENU
        System.out.println("\n====================================");
        System.out.println(" AVAILABLE PRODUCTS");
        System.out.println("====================================");

        for (Product p : products) {

            System.out.println(
                    "ID: " + p.id +
                            " | " + p.name +
                            " | Price: $" + p.price +
                            " | Qty: " + p.quantity
            );
        }

        System.out.println("====================================");

        System.out.print("Product ID: ");
        int id = sc.nextInt();

        System.out.print("Quantity: ");
        int q = sc.nextInt();

        boolean found = false;

        for (Product p : products) {

            if (p.id == id) {
                found = true;

                if (p.quantity >= q) {

                    p.quantity -= q;

                    double total = p.price * q;

                    System.out.println("✔ Total = " + total);

                    user.addPurchase(p.name, q, total);

                    System.out.println(" Thank you for your purchase!");
                } else {
                    System.out.println("❌ Not enough stock");
                }
            }
        }

        if (!found) {
            System.out.println("❌ Product not found");
        }
    }
}