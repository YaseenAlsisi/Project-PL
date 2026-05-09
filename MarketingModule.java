import java.util.*;
import java.time.*;

class Offer {

    String productName;
    double originalPrice;
    double discount;
    double finalPrice;

    Offer(String productName, double originalPrice, double discount, int days) {

        this.productName = productName;
        this.originalPrice = originalPrice;
        this.discount = discount;

        this.finalPrice = originalPrice - (originalPrice * discount / 100);
    }

    public void show() {
        System.out.println(
                productName +
                        " | Before: $" + originalPrice +
                        " | After: $" + finalPrice +
                        " | " + discount + "% OFF"
        );
    }
}

public class MarketingModule {

    ArrayList<Offer> offers = new ArrayList<>();
    Scanner sc = new Scanner(System.in);

    public void menu(ArrayList<Product> products) {

        while (true) {

            System.out.println("\n MARKETING MENU");
            System.out.println("1 Create Offer");
            System.out.println("2 Show Offers");
            System.out.println("3 Back");

            int c = sc.nextInt();

            if (c == 1) createOffer(products);
            else if (c == 2) showOffers();
            else break;
        }
    }

    public void createOffer(ArrayList<Product> products) {

        System.out.println("\n Products:");

        for (Product p : products)
            System.out.println(p.id + " | " + p.name + " | $" + p.price);

        System.out.print("Product ID: ");
        int id = sc.nextInt();

        Product selected = null;

        for (Product p : products)
            if (p.id == id)
                selected = p;

        if (selected == null) {
            System.out.println("❌ Not found");
            return;
        }

        System.out.print("Discount %: ");
        double discount = sc.nextDouble();

        System.out.print("Days: ");
        int days = sc.nextInt();

        offers.add(new Offer(
                selected.name,
                selected.price,
                discount,
                days
        ));

        System.out.println("✔ Offer created");
    }

    public void showOffers() {
        for (Offer o : offers)
            o.show();
    }
}