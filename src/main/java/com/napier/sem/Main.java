package com.napier.sem;

import java.sql.*;

public class Main
{
    public static void main(String[] args)
    {
        // Create new Application
        Main M = new Main();

        // Connect to database
        M.Connect();

        Employee emp = M.GetEmployee(255530);

        M.DisplayEmployee(emp);

        // Disconnect from database
        M.Disconnect();
    }

    /**
     * Connection to MySQL database.
     */
    private Connection con = null;

    /**
     * Connect to the MySQL database.
     */
    public void Connect()
    {
        try
        {
            // Load Database driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }

        int retries = 10;
        for (int i = 0; i < retries; ++i)
        {
            System.out.println("Connecting to database...");
            try
            {
                // Wait a bit for db to start
                Thread.sleep(30000);
                // Connect to database
                con = DriverManager.getConnection("jdbc:mysql://db:3306/employees?useSSL=false", "root", "example");
                System.out.println("Successfully connected");
                break;
            }
            catch (SQLException sqle)
            {
                System.out.println("Failed to connect to database attempt " + i);
                System.out.println(sqle.getMessage());
            }
            catch (InterruptedException ie)
            {
                System.out.println("Thread interrupted? Should not happen.");
            }
        }
    }

    /**
     * Disconnect from the MySQL database.
     */
    public void Disconnect()
    {
        if (con != null)
        {
            try
            {
                // Close connection
                con.close();
            }
            catch (Exception e)
            {
                System.out.println("Error closing connection to database");
            }
        }
    }

    public Employee GetEmployee(int ID)
    {
        try
        {
            // Create an SQL statement
            Statement stmt = con.createStatement();

            // Create string for SQL statement
            String employeeSelect =
                    "SELECT employees.emp_no, first_name, last_name, title, salary, dept_name, dept_no " +
                            "FROM employees " +
                            "JOIN salaries ON salaries.emp_no = employees.emp_no " +
                            "AND salaries.to_date = " +
                            "(SELECT MAX(to_date) " +
                            "FROM salaries " +
                            "WHERE salaries.emp_no = employees.emp_no)" +
                            "JOIN titles ON titles.emp_no = employees.emp_no " +
                            "AND titles.to_date = salaries.to_date " +
                            "JOIN departments ON departments.dept_no = " +
                            "(SELECT dept_no " +
                            "FROM dept_emp " +
                            "WHERE dept_emp.emp_no = " + ID + " " +
                            "AND dept_emp.to_date = salaries.to_date) " +
                            "WHERE employees.emp_no = " + ID;



            // Execute SQL statement
            ResultSet employeeResult = stmt.executeQuery(employeeSelect);



            // Return new employee if valid.
            // Check one is returned
            if (employeeResult.next())
            {
                Employee emp = new Employee();
                emp.emp_no = employeeResult.getInt("emp_no");
                emp.first_name = employeeResult.getString("first_name");
                emp.last_name = employeeResult.getString("last_name");
                emp.title = employeeResult.getString("title");
                emp.salary = employeeResult.getInt("salary");
                emp.dept_name = employeeResult.getString("dept_name");

                // query for getting the managers name
                String managerSelect =
                        "SELECT CONCAT(first_name, ' ', last_name) \"name\" " +
                                "FROM employees " +
                                "WHERE emp_no = " +
                                "(SELECT emp_no " +
                                "FROM dept_manager " +
                                "WHERE dept_no = '" + employeeResult.getString("dept_no") + "' " +
                                "AND to_date = " +
                                "(SELECT MAX(to_date)" +
                                "FROM dept_manager " +
                                "WHERE dept_no = '" + employeeResult.getString("dept_no") + "'))";

                ResultSet managerResult = stmt.executeQuery(managerSelect);

                if (managerResult.next())
                {
                    emp.manager = managerResult.getString("name");
                }

                return emp;
            }
            else
                return null;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Failed to get employee details");
            return null;
        }
    }

    public void DisplayEmployee(Employee emp)
    {
        if (emp != null)
        {
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

