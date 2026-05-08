import java.util.ArrayList;

public class sales {
// e7na 34an gamdin fa hn n3ml class w n link it with the main system to sales 3shan n3ml 4 functions search product, list all products, make order, cancel order

    private ArrayList<Product> products;   // reference to system products
    private ArrayList<Product> order;      // current order

    public sales(ArrayList<Product> products) {
        this.products = products;
        this.order = new ArrayList<>();
    }

    // ha Search for el product by id
    
    public void searchProduct(int productId) {
        boolean found = false;

        for (Product p : products) {
            if (p.getProductId() == productId) {
                System.out.println("Product Found: " + p.getName());
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("Product not found!");
        }
    }

    // n view el List of all products ely fel system

    public void listAllProducts() {
        if (products.isEmpty()) {
            System.out.println("No products available.");
            return;
        }

        for (Product p : products) {
            System.out.println(
                "ID: " + p.getProductId() +
                " | Name: " + p.getName() +
                " | Price: " + p.getPrice() +
                " | Quantity: " + p.getQuantity()
            );
        }
    }

    // yala b2a n3ml order 

    public void makeOrder(int productId, int qty) {

        for (Product p : products) {
            if (p.getProductId() == productId) {

                if (p.getQuantity() >= qty) {
                    p.setQuantity(p.getQuantity() - qty);
                    order.add(p);
                    System.out.println("Order added successfully!");
                } else {
                    System.out.println("Not enough stock!");
                }

                return;
            }
        }

        System.out.println("Product not found!");
    }

    // fe customer r5m 3ayz y cancel el order bta3o yala bena n3ml cancel order function

    public void cancelOrder() {
        if (order.isEmpty()) {
            System.out.println("No order to cancel.");
            return;
        }

        for (Product p : order) {
            p.setQuantity(p.getQuantity() + 1); // restore stock (simple logic)
        }

        order.clear();
        System.out.println("Order cancelled successfully!");
    }
}