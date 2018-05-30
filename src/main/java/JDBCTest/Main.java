package JDBCTest;

import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class Main {
    private static final String DB_CONNECTION = "jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";
    private static String driver = "com.mysql.cj.jdbc.Driver";

    private static Connection conn;

    public static void main(String[] args) {
        try {
            try (Scanner sc = new Scanner(System.in)) {
                Class.forName(driver).newInstance();
                conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
                initDB();

                while (true) {
                    System.out.println("1: add client");
                    System.out.println("2: add random clients");
                    System.out.println("3: delete client");
                    System.out.println("4: change client");
                    System.out.println("5: view clients");
                    System.out.print("-> ");

                    String s = sc.nextLine();
                    switch (s) {
                        case "1":
                            addClient(sc);
                            break;
                        case "2":
                            insertRandomClients(sc);
                            break;
                        case "3":
                            deleteClient(sc);
                            break;
                        case "4":
                            changeClient(sc);
                            break;
                        case "5":
                            viewClients();
                            break;
                        default:
                            return;
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            } finally {
                if (conn != null) conn.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void initDB() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS Clients");
            st.execute("CREATE TABLE Clients (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(20) NOT NULL, age INT)");
        }
    }

    private static void addClient(Scanner sc) throws SQLException {
        System.out.print("Enter client name: ");
        String name = sc.nextLine();
        System.out.print("Enter client age: ");
        String sAge = sc.nextLine();
        int age = Integer.parseInt(sAge);

        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO Clients (name, age) VALUES(?, ?)")) {
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.executeUpdate(); // for INSERT, UPDATE & DELETE
        }
    }

    private static void deleteClient(Scanner sc) throws SQLException {
        System.out.print("Enter client name: ");
        String name = sc.nextLine();

        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Clients WHERE name = ?")) {
            ps.setString(1, name);
            ps.executeUpdate(); // for INSERT, UPDATE & DELETE
        }
    }

    private static void changeClient(Scanner sc) throws SQLException {
        System.out.print("Enter client name: ");
        String name = sc.nextLine();
        System.out.print("Enter new age: ");
        String sAge = sc.nextLine();
        int age = Integer.parseInt(sAge);

        try (PreparedStatement ps = conn.prepareStatement("UPDATE Clients SET age = ? WHERE name = ?")) {
            ps.setInt(1, age);
            ps.setString(2, name);
            ps.executeUpdate(); // for INSERT, UPDATE & DELETE
        }
    }

    private static void insertRandomClients(Scanner sc) throws SQLException {
        System.out.print("Enter clients count: ");
        String sCount = sc.nextLine();
        int count = Integer.parseInt(sCount);
        Random rnd = new Random();

        conn.setAutoCommit(false); // enable transactions
        try {
            try {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO Clients (name, age) VALUES(?, ?)")) {
                    for (int i = 0; i < count; i++) {
                        ps.setString(1, "Name" + i);
                        ps.setInt(2, rnd.nextInt(100));
                        ps.executeUpdate();
                    }
                    conn.commit();
                }
            } catch (Exception ex) {
                conn.rollback();
            }
        } finally {
            conn.setAutoCommit(true); // return to default mode
        }
    }

    private static void viewClients() throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM Clients")) {
            // table of data representing a database result set,
            try (ResultSet rs = ps.executeQuery()) {
                // can be used to get information about the types and properties of the columns in a ResultSet object
                ResultSetMetaData md = rs.getMetaData();

                for (int i = 1; i <= md.getColumnCount(); i++)
                    System.out.print(md.getColumnName(i) + "\t\t");
                System.out.println();

                while (rs.next()) {
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        System.out.print(rs.getString(i) + "\t\t");
                    }
                    System.out.println();
                }
            }
            // rs can't be null according to the docs
        }
    }
}
