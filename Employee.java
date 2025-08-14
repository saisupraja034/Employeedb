import java.sql.*;
import java.util.Scanner;
public class Employee {

   
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/companydb?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";       
    private static final String DB_PASSWORD = "password";

  
    private static final String INSERT_SQL =
            "INSERT INTO employees (name, department, salary, email) VALUES (?, ?, ?, ?)";
    private static final String SELECT_ALL_SQL =
            "SELECT id, name, department, salary, email FROM employees";
    private static final String SELECT_BY_ID_SQL =
            "SELECT id, name, department, salary, email FROM employees WHERE id = ?";
    private static final String UPDATE_SQL =
            "UPDATE employees SET name = ?, department = ?, salary = ?, email = ? WHERE id = ?";
    private static final String DELETE_SQL =
            "DELETE FROM employees WHERE id = ?";

    public static void main(String[] args) {
        System.out.println("=== Employee Database App ===");
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Add connector jar to classpath.");
            return;
        }

        try (Scanner sc = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu();
                System.out.print("Choose option: ");
                String opt = sc.nextLine().trim();
                switch (opt) {
                    case "1":
                        addEmployee(sc);
                        break;
                    case "2":
                        viewAllEmployees();
                        break;
                    case "3":
                        updateEmployee(sc);
                        break;
                    case "4":
                        deleteEmployee(sc);
                        break;
                    case "5":
                        viewEmployeeById(sc);
                        break;
                    case "0":
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option. Try again.");
                }
            }
        }

        System.out.println("Exiting. Goodbye!");
    }

    private static void printMenu() {
        System.out.println("\nMenu:");
        System.out.println("1. Add employee");
        System.out.println("2. View all employees");
        System.out.println("3. Update employee");
        System.out.println("4. Delete employee");
        System.out.println("5. View employee by id");
        System.out.println("0. Exit");
    }

    
    private static void addEmployee(Scanner sc) {
        System.out.println("\n Add Employee  ");
        System.out.print("Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Department: ");
        String department = sc.nextLine().trim();
        System.out.print("Salary: ");
        double salary = parseDouble(sc.nextLine().trim(), 0.0);
        System.out.print("Email: ");
        String email = sc.nextLine().trim();

        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pst = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            
            pst.setString(1, name);
            pst.setString(2, department);
            pst.setDouble(3, salary);
            pst.setString(4, email);

            int affected = pst.executeUpdate();
            if (affected == 0) {
                System.out.println("Insert failed, no rows affected.");
                return;
            }

            try (ResultSet genKeys = pst.getGeneratedKeys()) {
                if (genKeys.next()) {
                    long id = genKeys.getLong(1);
                    System.out.println("Employee added with id: " + id);
                } else {
                    System.out.println("Employee added (generated id not returned).");
                }
            }

        } catch (SQLException e) {
            System.err.println("SQL error while adding: " + e.getMessage());
           
        }
    }

 
    private static void viewAllEmployees() {
        System.out.println("\n-- All Employees --");
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pst = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = pst.executeQuery()) {

            boolean any = false;
            System.out.printf("%-5s %-20s %-15s %-10s %-25s%n", "ID", "Name", "Department", "Salary", "Email");
            System.out.println("--------------------------------------------------------------------------------");
            while (rs.next()) {
                any = true;
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String dept = rs.getString("department");
                double sal = rs.getDouble("salary");
                String email = rs.getString("email");

                System.out.printf("%-5d %-20s %-15s %-10.2f %-25s%n", id, name, dept, sal, email);
            }
            if (!any) {
                System.out.println("(no employees found)");
            }

        } catch (SQLException e) {
            System.err.println("SQL error while viewing: " + e.getMessage());
        }
    }

    private static void viewEmployeeById(Scanner sc) {
        System.out.print("Enter employee id: ");
        int id = parseInt(sc.nextLine().trim(), -1);
        if (id < 0) {
            System.out.println("Invalid id.");
            return;
        }
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pst = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Employee details:");
                    System.out.println("ID: " + rs.getInt("id"));
                    System.out.println("Name: " + rs.getString("name"));
                    System.out.println("Department: " + rs.getString("department"));
                    System.out.println("Salary: " + rs.getDouble("salary"));
                    System.out.println("Email: " + rs.getString("email"));
                } else {
                    System.out.println("No employee with id " + id);
                }
            }

        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getMessage());
        }
    }

    
    private static void updateEmployee(Scanner sc) {
        System.out.print("Enter id of employee to update: ");
        int id = parseInt(sc.nextLine().trim(), -1);
        if (id < 0) {
            System.out.println("Invalid id.");
            return;
        }


        System.out.println("Current data (if exists):");
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement select = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            select.setInt(1, id);
            try (ResultSet rs = select.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No employee with id " + id);
                    return;
                }
                System.out.println("Name: " + rs.getString("name"));
                System.out.println("Department: " + rs.getString("department"));
                System.out.println("Salary: " + rs.getDouble("salary"));
                System.out.println("Email: " + rs.getString("email"));
            }
        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getMessage());
            return;
        }


        System.out.print("New Name (leave blank to keep current): ");
        String name = sc.nextLine().trim();
        System.out.print("New Department (leave blank to keep current): ");
        String department = sc.nextLine().trim();
        System.out.print("New Salary (leave blank to keep current): ");
        String salaryStr = sc.nextLine().trim();
        System.out.print("New Email (leave blank to keep current): ");
        String email = sc.nextLine().trim();

        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement select = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            select.setInt(1, id);
            try (ResultSet rs = select.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No employee with id " + id);
                    return;
                }

                String currName = rs.getString("name");
                String currDept = rs.getString("department");
                double currSalary = rs.getDouble("salary");
                String currEmail = rs.getString("email");

                String finalName = name.isEmpty() ? currName : name;
                String finalDept = department.isEmpty() ? currDept : department;
                double finalSalary = salaryStr.isEmpty() ? currSalary : parseDouble(salaryStr, currSalary);
                String finalEmail = email.isEmpty() ? currEmail : email;

                try (PreparedStatement update = conn.prepareStatement(UPDATE_SQL)) {
                    update.setString(1, finalName);
                    update.setString(2, finalDept);
                    update.setDouble(3, finalSalary);
                    update.setString(4, finalEmail);
                    update.setInt(5, id);

                    int rows = update.executeUpdate();
                    if (rows > 0) {
                        System.out.println("Employee updated successfully.");
                    } else {
                        System.out.println("Update failed.");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("SQL error while updating: " + e.getMessage());
        }
    }

    private static void deleteEmployee(Scanner sc) {
        System.out.print("Enter id of employee to delete: ");
        int id = parseInt(sc.nextLine().trim(), -1);
        if (id < 0) {
            System.out.println("Invalid id.");
            return;
        }
        System.out.print("Are you sure you want to delete id " + id + "? (yes/no): ");
        String confirm = sc.nextLine().trim().toLowerCase();
        if (!confirm.equals("yes") && !confirm.equals("y")) {
            System.out.println("Delete cancelled.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pst = conn.prepareStatement(DELETE_SQL)) {

            pst.setInt(1, id);
            int rows = pst.executeUpdate();
            if (rows > 0) {
                System.out.println("Employee deleted.");
            } else {
                System.out.println("No employee with id " + id);
            }

        } catch (SQLException e) {
            System.err.println("SQL error while deleting: " + e.getMessage());
        }
    }

    private static int parseInt(String s, int defaultVal) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private static double parseDouble(String s, double defaultVal) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
