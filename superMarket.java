
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import java.time.LocalDate; // --> 3shan el expiry date
import java.time.temporal.ChronoUnit; // --> 3shan a3raf a7seb el days left 3la expiry date
import java.util.ArrayList;
import java.util.Scanner;

class Product {

    private int productId;
    private String name;
    private double price;
    private int quantity;
    private int minimumStockLevel;
    private LocalDate expiryDate;

    // el constructor da 3shan a3raf a3ml object mn el class Product w a7ot feh el data bta3t each product
    public Product(int productId, String name, double price,
            int quantity, int minimumStockLevel,
            LocalDate expiryDate) {

        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.minimumStockLevel = minimumStockLevel;
        this.expiryDate = expiryDate;

    }

    // Getter //--> to read the value in private attributes |||| 3ks el setter --> to change the value in private attributes{

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
//--------------------------------------------------------{
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public String getName() {
        return name;
    }
    public double getPrice() {
        return price;
    }
    //----------------------------------------------------}
    //--------->>>> used this to link in sales 34an a3rf a recall el functions fe sales}

    // Display Product
    public void displayProduct() {

        System.out.println("\n------------------------");
        System.out.println("Product ID: " + productId);
        System.out.println("Name: " + name);
        System.out.println("Price: " + price);
        System.out.println("Quantity: " + quantity);
        System.out.println("Minimum Stock Level: " + minimumStockLevel);
        System.out.println("Expiry Date: " + expiryDate);
    }

    // to show up the Low Stock msg
    public void checkStockStatus() {

        if (quantity <= minimumStockLevel) {

            System.out.println("WARNING: Low Stock!");
        }
    }

    // same bs to show the expiry msg
    public void checkExpiryStatus() {

        LocalDate today = LocalDate.now(); // --> method est5dmtha 3shan a3raf a7seb el days left 3la expiry date source : gemini <\__/>

        long daysLeft = ChronoUnit.DAYS.between(today, expiryDate);

        if (daysLeft <= 30 && daysLeft >= 0) {

            System.out.println("WARNING: Product expires soon!");
        } else if (daysLeft < 0) {

            System.out.println("WARNING: Product already expired!");
        }
    }

    // Sell Product
    public void sellProduct(int soldQuantity) {

        if (soldQuantity <= quantity) {

            quantity -= soldQuantity; //--> 3shan a7seb el quantity el ba2y mn ba3d ma b3t el product

            System.out.println("Product sold successfully.");

            checkStockStatus();
        } else {

            System.out.println("Not enough quantity available!");
        }
    }

    // Return Product
    public void returnProduct(int returnedQuantity) {

        quantity += returnedQuantity; //--> 3shan a7seb el quantity el ba2y mn ba3d ma raga3 el product

        System.out.println("Product returned successfully.");
    }

    // Damaged Product
    public void damageProduct(int damagedQuantity) {

        if (damagedQuantity <= quantity) {

            quantity -= damagedQuantity; //--> 3shan a7seb el quantity el ba2y mn ba3d ma el product etdamaged

            System.out.println("Damaged items recorded.");

            checkStockStatus();
        } else {

            System.out.println("Invalid damaged quantity!");
        }
    }
}
// hnsmy el class b esm zy ma basmohnds Ziad w A'laa alolna fe awel sec

public class superMarket {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        ArrayList<Product> inventory = new ArrayList<>();

        boolean running = true;

        while (running) {

            System.out.println("\n★★★★★★ Hyper Market System ★★★★★★");

            System.out.println("1. Add Product");
            System.out.println("2. Display Products");
            System.out.println("3. Search Product");
            System.out.println("4. Delete Product");
            System.out.println("5. Sell Product");
            System.out.println("6. Return Product");
            System.out.println("7. Record Damaged Product");
            System.out.println("8. Exit");

            System.out.print("Choose an option: ");

            int choice = input.nextInt();

            switch (choice) {

                // Add Product
                case 1:

                    System.out.print("Enter Product ID: ");
                    int id = input.nextInt();

                    input.nextLine();

                    System.out.print("Enter Product Name: ");
                    String name = input.nextLine();

                    System.out.print("Enter Product Price: ");
                    double price = input.nextDouble();

                    System.out.print("Enter Quantity: ");
                    int quantity = input.nextInt();

                    System.out.print("Enter Minimum Stock Level: ");
                    int minStock = input.nextInt();

                    input.nextLine();

                    System.out.print("Enter Expiry Date (YYYY-MM-DD): ");
                    String expiryInput = input.nextLine();


                    LocalDate expiryDate = LocalDate.parse(expiryInput);

                    Product newProduct = new Product(
                            id,
                            name,
                            price,
                            quantity,
                            minStock,
                            expiryDate
                    );

                    inventory.add(newProduct);

                    System.out.println("Product Added Successfully!");

                    break;

                // Display Products
                case 2:

                    if (inventory.isEmpty()) {

                        System.out.println("Inventory is Empty!");
                    } else {

                        for (Product item : inventory) {

                            item.displayProduct();

                            item.checkStockStatus();

                            item.checkExpiryStatus();
                        }
                    }

                    break;

                // Search Product
                case 3:

                    System.out.print("Enter Product ID to Search: ");

                    int searchId = input.nextInt();

                    boolean found = false;

                    for (Product item : inventory) {

                        if (item.getProductId() == searchId) {

                            item.displayProduct();

                            found = true;

                            break;
                        }
                    }

                    if (!found) {

                        System.out.println("Product Not Found!");
                    }

                    break;

                // Delete Product
                case 4:

                    System.out.print("Enter Product ID to Delete: ");

                    int deleteId = input.nextInt();

                    boolean deleted = false;

                    for (int i = 0; i < inventory.size(); i++) {

                        if (inventory.get(i).getProductId() == deleteId) {

                            inventory.remove(i);

                            deleted = true;

                            System.out.println("Product Deleted Successfully!");

                            break;
                        }
                    }

                    if (!deleted) {

                        System.out.println("Product Not Found!");
                    }

                    break;

                // Sell Product
                case 5:

                    System.out.print("Enter Product ID: ");

                    int sellId = input.nextInt();

                    System.out.print("Enter Sold Quantity: ");

                    int soldQuantity = input.nextInt();

                    boolean sold = false;

                    for (Product item : inventory) {

                        if (item.getProductId() == sellId) {

                            item.sellProduct(soldQuantity);

                            sold = true;

                            break;
                        }
                    }

                    if (!sold) {

                        System.out.println("Product Not Found!");
                    }

                    break;

                // Return Product
                case 6:

                    System.out.print("Enter Product ID: ");

                    int returnId = input.nextInt();

                    System.out.print("Enter Returned Quantity: ");

                    int returnedQuantity = input.nextInt();

                    boolean returned = false;

                    for (Product item : inventory) {

                        if (item.getProductId() == returnId) {

                            item.returnProduct(returnedQuantity);

                            returned = true;

                            break;
                        }
                    }

                    if (!returned) {

                        System.out.println("Product Not Found!");
                    }

                    break;

                // Damaged Product
                case 7:

                    System.out.print("Enter Product ID: ");

                    int damageId = input.nextInt();

                    System.out.print("Enter Damaged Quantity: ");

                    int damagedQuantity = input.nextInt();

                    boolean damaged = false;

                    for (Product item : inventory) {

                        if (item.getProductId() == damageId) {

                            item.damageProduct(damagedQuantity);

                            damaged = true;

                            break;
                        }
                    }

                    if (!damaged) {

                        System.out.println("Product Not Found!");
                    }

                    break;

                // Exit
                case 8:

                    running = false;

                    System.out.println("System Closed.");

                    break;

                // Invalid Choice
                default:

                    System.out.println("Invalid Choice!");
            }
        }

        input.close();
    }
}
