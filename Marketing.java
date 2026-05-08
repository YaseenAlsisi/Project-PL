import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

class Product {
    private String name;
    private double originalPrice;
    private double offerPrice;
    private LocalDateTime offerExpiry;

    public Product(String name, double price) {
        this.name = name;
        this.originalPrice = price;
        this.offerPrice = price;
    }

    public void setSpecialOffer(double discountedPrice, int minutes) {
        this.offerPrice = discountedPrice;
        this.offerExpiry = LocalDateTime.now().plusMinutes(minutes); // functions for time of the Speacil offer
    }

    public double getCurrentPrice() {
    if (offerExpiry != null && LocalDateTime.now().isBefore(offerExpiry)) { // checking if the offer time is still vaild or no to remove it 
        return offerPrice;
    }
        return originalPrice;
    }

    // function to store in the offer
    public boolean hasActiveOffer() {
        return offerExpiry != null && LocalDateTime.now().isBefore(offerExpiry);
    }

    public String getName() { 
        return name; 
    }
    public LocalDateTime getExpiry() { 
        return offerExpiry;
    }
}

public class Marketing {
    private static ArrayList<Product> inventory = new ArrayList<>(); // to store the products in inventory
    private static Scanner scanner = new Scanner(System.in); // System.in this for to catch the number you type and the scanner scans it

    public static void main(String[] args) {
        //example
        inventory.add(new Product("Milk", 2.50));
        inventory.add(new Product("Bread", 1.80));
        inventory.add(new Product("Cheese", 5.00));

        while (true) {
            System.out.println("\n--- MARKETING DEPARTMENT MENU ---");
            System.out.println("1. Create Special Offer");
            System.out.println("2. View Marketing Report");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); 

            switch (choice) {
                case 1:
                    makeSpecialOffer();
                    break;
                case 2:
                    generateReport();
                    break;
                case 3:
                    System.out.println("Exiting");
                    return;
                default:
                    System.out.println("Wrong choice.");
            }
        }
    }

    // creating the specil offer 
    private static void makeSpecialOffer() {
        System.out.println("\nSelect Product Number:");
        for (int i = 0; i < inventory.size(); i++) { // loop for checking every product
            System.out.println(i + ". " + inventory.get(i).getName()); // to list the product for you to choose 
        }
        int index = scanner.nextInt();
        
        System.out.print("Enter Offer Price: ");
        double price = scanner.nextDouble();
        
        System.out.print("Enter Duration (in minutes): ");
        int mins = scanner.nextInt();

        inventory.get(index).setSpecialOffer(price, mins);
        System.out.println("Offer sent to inventory successfully");
    }

    // to create the report and it shows if it got offer or no and also how long left
    private static void generateReport() {
        System.out.println("\n--- CURRENT PRODUCT STATUS REPORT ---");
        System.out.printf("%-10s | %-15s | %-10s | %-15s\n", "Product", "Price", "Status", "Time Left");

        for (Product p : inventory) { // it loops till the offers ends so it comes back to normal
            String status = "Normal";
            String timeLeft = "N/A";

            // to say this product is on sale and how long
            if (p.hasActiveOffer()) { 
                status = "ON SALE";
                long diff = Duration.between(LocalDateTime.now(), p.getExpiry()).toMinutes();
                timeLeft = diff + " mins";
            }

            System.out.printf("%-10s | $%-14.2f | %-10s | %-15s\n", 
                            p.getName(), p.getCurrentPrice(), status, timeLeft);
        }
    }
}
