package com.napier.sem;

import java.sql.*;

public class App {
    public static void main(String[] args) {
        // Create new Application
        App a = new App();
        // Connect to database
        a.connect();
        // Get Employee
        Employee emp = a.getEmployee(255530);
        // Display results
        a.displayEmployee(emp);
        // Disconnect from database
        a.disconnect();
    }

    /**
     * Connection to MySQL database.
     */
    private Connection con = null;

    /**
     * Connect to the MySQL database.
     */
    public void connect() {
        try {
            // Load Database driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }

        int retries = 10;
        for (int i = 0; i < retries; ++i) {
            System.out.println("Connecting to database...");
            try {
                // Wait a bit for db to start needed for travis but can be removed locally if db running
                Thread.sleep(30000);

                // Connect to database locally
                //con = DriverManager.getConnection("jdbc:mysql://localhost:33060/employees?useSSL=true", "root", "example");

                // Connect to database inside docker
                con = DriverManager.getConnection("jdbc:mysql://db:3306/employees?useSSL=false", "root", "example");

                System.out.println("Successfully connected");
                break;
            } catch (SQLException sql) {
                System.out.println("Failed to connect to database attempt " + i);
                System.out.println(sql.getMessage());
            } catch (InterruptedException ie) {
                System.out.println("Thread interrupted? Should not happen.");
            }
        }
    }

    /**
     * Disconnect from the MySQL database.
     */
    public void disconnect() {
        if (con != null) {
            try {
                // Close connection
                con.close();
            } catch (Exception e) {
                System.out.println("Error closing connection to database");
            }
        }
    }

    public Employee getEmployee(int ID) {
        try {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement

            //could break this down into two SQL statements to retrieve employee details using joins
            // then another query to get the manager

            String strSelect = "SELECT e1.emp_no, e1.first_name, e1.last_name, titles.title, salaries.salary, " +
                    "dp1.dept_name, e2.first_name as manager_firstname, e2.last_name as manager_lastname " +
                    "FROM employees e1 JOIN titles ON titles.emp_no = e1.emp_no " +
                    "JOIN dept_emp ON dept_emp.emp_no = e1.emp_no " +
                    "JOIN departments dp1 ON dp1.dept_no = dept_emp.dept_no " +
                    "JOIN dept_manager dm1 ON dm1.dept_no = dp1.dept_no " +
                    "JOIN salaries ON salaries.emp_no = e1.emp_no JOIN employees e2 ON e2.emp_no IN " +
                    "(SELECT dm2.emp_no FROM dept_manager dm2 WHERE dm2.dept_no = dp1.dept_no AND dm2.to_date = '9999-01-01') " +
                    "WHERE dept_emp.emp_no = '" + ID + "' AND salaries.to_date = '9999-1-1' AND dm1.to_date = '9999-1-1' " +
                    "AND titles.to_date = '9999-1-1' AND dept_emp.to_date = '9999-1-1';" ;
            // Execute SQL statement
            ResultSet reset = stmt.executeQuery(strSelect);
            // Return new employee if valid.
            // Check one is returned
            if (reset.next()) {
                Employee emp = new Employee();
                emp.emp_no = reset.getInt("emp_no");
                emp.first_name = reset.getString("first_name");
                emp.last_name = reset.getString("last_name");
                emp.title = reset.getString("titles.title");
                emp.salary = reset.getInt("salaries.salary");
                emp.dept_name = reset.getString("dp1.dept_name");
                emp.manager = reset.getString("manager_firstname") + " " + reset.getString("manager_lastname");
                return emp;
            } else
                return null;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get employee details");
            return null;
        }
    }


    public void displayEmployee(Employee emp) {
        if (emp != null) {
            System.out.println(
                    emp.emp_no + " "
                            + emp.first_name + " "
                            + emp.last_name + "\n"
                            + emp.title + "\n"
                            + "Salary:" + emp.salary + "\n"
                            + emp.dept_name + "\n"
                            + "Manager: " + emp.manager + "\n");
        }
    }
}