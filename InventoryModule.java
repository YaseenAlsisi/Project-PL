import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

class Product {

    int id;
    String name;
    double price;
    int quantity;
    int minStock;
    LocalDate expiry;

    Product(int id, String name, double price, int quantity, int minStock, LocalDate expiry) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.minStock = minStock;
        this.expiry = expiry;
    }

    void display() {
        System.out.println(id + " | " + name + " | $" + price + " | Qty: " + quantity);
    }
}

public class InventoryModule {

    ArrayList<Product> products = new ArrayList<>();
    Scanner sc = new Scanner(System.in);

    public void menu() {

        while (true) {

            System.out.println("\n INVENTORY");
            System.out.println("1. Add Product");
            System.out.println("2. List Products");
            System.out.println("3. Search Product");
            System.out.println("4. Back");

            System.out.print("--> Choice: ");
            int c = sc.nextInt();

            if (c == 1) addProduct();
            else if (c == 2) listProducts();
            else if (c == 3) searchProduct();
            else break;
        }
    }

    public void addProduct() {

        System.out.print("ID: ");
        int id = sc.nextInt();

        System.out.print("Name: ");
        String name = sc.next();

        System.out.print("Price: ");
        double price = sc.nextDouble();

        System.out.print("Qty: ");
        int qty = sc.nextInt();

        System.out.print("Min Stock: ");
        int min = sc.nextInt();

        System.out.print("Expiry (yyyy/MM/dd): ");
        String date = sc.next();

        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate expiry = LocalDate.parse(date, f);

        products.add(new Product(id, name, price, qty, min, expiry));

        System.out.println("✔ Added");
    }

    public void listProducts() {
        for (Product p : products) p.display();
    }

    public void searchProduct() {

        System.out.print("Enter ID: ");
        int id = sc.nextInt();

        for (Product p : products)
            if (p.id == id)
                p.display();
    }

    public ArrayList<Product> getProducts() {
        return products;
    }
}