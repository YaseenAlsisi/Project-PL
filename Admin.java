import java.util.ArrayList;
import java.util.Scanner;

public class Admin {
    private String username;
    private String password;
    private String id;
    private String type;

    // orginal list for all emplyees
    private static ArrayList<Admin> employeeList = new ArrayList<>();

    // The Mother 
    public Admin(String id, String username, String password, String type) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.type = type;
    }

    // update Account username
    public void updateAccount(String newusername) {
        if (newusername != null && !newusername.trim().isEmpty()) {
            this.username = newusername;
            System.out.println("Username successfully updated!");
        } else {
            System.out.println("Invalid username.");
        }
    }

    // update password
    public void updatePassword(String oldpassword, String newpassword) {
        if (this.password.equals(oldpassword)) {
            if (newpassword != null && newpassword.length() >= 6) {
                this.password = newpassword;
                System.out.println("Password successfully updated");
            } else {
                System.out.println("New password must be >= 6 characters.");
            }
        } else {
            System.out.println("Incorrect old password.");
        }
    }

    // to update ID
    public void updateID(String newId) {
        if (newId != null && !newId.trim().isEmpty()) {
            this.id = newId;
            System.out.println("ID updated successfully!");
        }
    }

    // to update Type
    public void updateType(String newType) {
        if (newType != null && !newType.trim().isEmpty()) {
            this.type = newType;
            System.out.println("Type updated successfully!");
        }
    }

    // Display Employee added
    public static void addEmployee(String id, String user, String password, String type){
        Admin newemp = new Admin(id, user, password, type);
        employeeList.add(newemp);
        System.out.println("Employee added: " + user);
    }

    // List for Employees
    public static void listEmployees() {
        System.out.println(" Employees List");
        for (Admin emp : employeeList) {
            emp.displayProfile();
        }
    }

    // To search for an employee with ID
    public static void searchEmployee(String searchId) {
        for (Admin emp : employeeList) {
            if (emp.id != null && emp.id.equals(searchId)) {
                System.out.println("Employee is here");
                emp.displayProfile();
                return;
            }
        }
        System.out.println("Employee with ID " + searchId + " not found.");
    }

    // to delete employee
    public static void deleteEmployee(String deleteId) {
        employeeList.removeIf(emp -> emp.id != null && emp.id.equals(deleteId));
        System.out.println("Employee removed ");
    }

    // Displays ID, Username, Type, and Password
    public void displayProfile() {
        System.out.println("Profile -> ID: " + id + " | User: " + username + " | Type: " + type + " | Pass: " + password);
    }

    // DISPLAY THE RUNNING (WITHOUT GUI)
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        Admin admin = new Admin("123", "admin", "123456", "TopAdmin");
        employeeList.add(admin);
        boolean exit = false;

        while (!exit) {
            System.out.println("\n--- Hyper Market Menu ---");
            System.out.println("1. Add Employee");
            System.out.println("2. List All Employees");
            System.out.println("3. Search Employee by ID");
            System.out.println("4. Delete Employee by ID");
            System.out.println("5. Show Admin Profile");
            System.out.println("6. Update Admin Profile");
            System.out.println("7. Exit");
            System.out.print("Select an option: ");
            
            int choice = input.nextInt();
            input.nextLine(); 
            switch (choice) {
                case 1:
                    System.out.print("Enter ID: ");
                    String id = input.nextLine();
                    System.out.print("Enter Username: ");
                    String user = input.nextLine();
                    System.out.print("Enter Password: ");
                    String pass = input.nextLine();
                    System.out.print("Enter Type: ");
                    String type = input.nextLine();
                    addEmployee(id, user, pass, type);
                    break;
                case 2:
                    listEmployees();
                    break;
                case 3:
                    System.out.print("Enter ID to search: ");
                    searchEmployee(input.nextLine());
                    break;
                case 4:
                    System.out.print("Enter ID to delete: ");
                    deleteEmployee(input.nextLine());
                    break;
                case 5:
                    admin.displayProfile();
                    break;
                case 6:
                    System.out.println("\n--- Update Admin Profile ---");
                    System.out.println("1. Update ID");
                    System.out.println("2. Update Username");
                    System.out.println("3. Update Type");
                    System.out.println("4. Update Password");
                    System.out.print("Choice: ");
                    int subChoice = input.nextInt();
                    input.nextLine(); 

                    if (subChoice == 1) {
                        System.out.print("Enter new Admin ID: ");
                        admin.updateID(input.nextLine());
                    } else if (subChoice == 2) {
                        System.out.print("Enter new Username: ");
                        admin.updateAccount(input.nextLine());
                    } else if (subChoice == 3) {
                        System.out.print("Enter new Type: ");
                        admin.updateType(input.nextLine());
                    } else if (subChoice == 4) {
                        System.out.print("Enter Current Password: ");
                        String oldP = input.nextLine();
                        System.out.print("Enter New Password: ");
                        String newP = input.nextLine();
                        admin.updatePassword(oldP, newP);
                    } else {
                        System.out.println("Invalid Choice!");
                    }
                    break;

                case 7:
                    exit = true;
                    System.out.println("System closed.");
                    break;
                default:
                    System.out.println("Invalid option!");
            }
        }
        input.close();
    }
}
