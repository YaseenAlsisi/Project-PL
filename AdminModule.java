import java.util.*;

class Employee {

    int id;
    String name;
    String password;
    String type;

    Employee(int id, String name, String password, String type) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.type = type;
    }
}

public class AdminModule {

    ArrayList<Employee> employees = new ArrayList<>();
    String adminPassword = "1234";

    Scanner sc = new Scanner(System.in);

    public void menu() {

        while (true) {

            System.out.println("\n ADMIN PANEL");
            System.out.println("1. Add Employee");
            System.out.println("2. List Employees");
            System.out.println("3. Delete Employee");
            System.out.println("4. Change Password");
            System.out.println("5. Back");

            System.out.print("--> Choice: ");
            int choice = sc.nextInt();

            if (choice == 1) addEmployee();
            else if (choice == 2) listEmployees();
            else if (choice == 3) deleteEmployee();
            else if (choice == 4) changePassword();
            else break;
        }
    }

    public void addEmployee() {

        System.out.println("\n+ ADD EMPLOYEE");

        System.out.print("ID: ");
        int id = sc.nextInt();

        System.out.print("Name: ");
        String name = sc.next();

        System.out.print("Password: ");
        String pass = sc.next();

        System.out.print("Type: ");
        String type = sc.next();

        employees.add(new Employee(id, name, pass, type));

        System.out.println("✔ Employee added");
    }

    public void listEmployees() {

        System.out.println("\n EMPLOYEES:");

        if (employees.isEmpty()) {
            System.out.println("No employees.");
            return;
        }

        for (Employee e : employees)
            System.out.println(e.id + " | " + e.name + " | " + e.type);
    }

    public void deleteEmployee() {

        System.out.print("Enter ID to delete: ");
        int id = sc.nextInt();

        employees.removeIf(e -> e.id == id);

        System.out.println("✔ Done");
    }

    public void changePassword() {

        System.out.print("Old password: ");
        String old = sc.next();

        if (old.equals(adminPassword)) {
            System.out.print("New password: ");
            adminPassword = sc.next();
            System.out.println("✔ Updated");
        } else {
            System.out.println("❌ Wrong password");
        }
    }
}