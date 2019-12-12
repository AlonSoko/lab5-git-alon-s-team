package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.net.ssl.SSLException;

public class Lab
{
    static private final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    // update USER, PASS and DB URL according to credentials provided by the website:
    // https://remotemysql.com/
    // in future move these hard coded strings into separated config file or even better env variables
    static private final String DB = "7ju1JhwnYF";
    static private final String DB_URL = "jdbc:mysql://remotemysql.com/"+ DB + "?useSSL=false";
    static private final String USER = "7ju1JhwnYF";
    static private final String PASS = "C61ezhJekY";

    public static void main(String[] args) throws SSLException {
        Connection conn = null;
        Statement stmt = null;
        Statement updateStmt = null;

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();

            // Updating the price of flight 387 to be 2019
            PreparedStatement updateFlightPrice = conn.prepareStatement("UPDATE flights "+"SET price = ? WHERE num = ?");
            updateFlightPrice.setInt(1,2019);
            updateFlightPrice.setInt(2,387);
            updateFlightPrice.executeUpdate();

            // Getting flight 387
            ResultSet rs = stmt.executeQuery("SELECT price FROM "+"flights WHERE num = 387");
            rs.next();
            int price = rs.getInt("price");
            System.out.format("Updated flight price is: %d\n", price);

            // Updating all flights with distance > 1000km to price+100
            updateStmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);

            rs = updateStmt.executeQuery("SELECT num, price FROM "+"flights WHERE distance > 1000");
            while(rs.next())
            {
                price = rs.getInt("price");
                rs.updateInt("price", price+100);
                rs.updateRow();
            }

            // Updating all flights with price less than 300 usd to price-25
            rs = updateStmt.executeQuery("SELECT num, price FROM "+"flights WHERE price < 300");
            while(rs.next())
            {
                price = rs.getInt("price");
                rs.updateInt("price", price-25);
                rs.updateRow();
            }

            // Printing all the flights
            rs = stmt.executeQuery("SELECT * FROM flights");
            while(rs.next())
            {
                int num = rs.getInt("num");
                String origin = rs.getString("origin");
                String destination = rs.getString("destination");
                int distance = rs.getInt("distance");
                price = rs.getInt("price");

                System.out.format("No. %5s Origin %15s Destination %18s Distance %5d Price %5d\n", num, origin, destination, distance, price);
            }

        } catch (SQLException se) {
            se.printStackTrace();
            System.out.println("SQLException: " + se.getMessage());
            System.out.println("SQLState: " + se.getSQLState());
            System.out.println("VendorError: " + se.getErrorCode());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
}
