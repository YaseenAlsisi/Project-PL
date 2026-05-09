import java.util.*;

public class UserModule {

    ArrayList<String> history = new ArrayList<>();

    public void login(String user) {
        history.add("LOGIN -> " + user);
    }

    public void addPurchase(String product, int qty, double total) {
        history.add("BUY -> " + product + " x" + qty + " = " + total);
    }

    // 🛒 Show available products menu
    public void showAvailableProducts(ArrayList<Product> products) {

        System.out.println("\n====================================");
        System.out.println("🛒 AVAILABLE PRODUCTS");
        System.out.println("====================================");

        if (products.isEmpty()) {
            System.out.println("❌ No products available.");
            return;
        }

        for (Product p : products) {

            System.out.println(
                    "ID: " + p.id +
                            " | " + p.name +
                            " | Price: $" + p.price +
                            " | Qty: " + p.quantity
            );
        }

        System.out.println("====================================");
    }

    public void showHistory() {

        System.out.println("\n HISTORY:");

        for (String h : history)
            System.out.println("- " + h);
    }
}