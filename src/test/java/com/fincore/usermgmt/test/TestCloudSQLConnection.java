package com.fincore.usermgmt.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestCloudSQLConnection {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java TestCloudSQLConnection <password>");
            System.exit(1);
        }
        
        String url = "jdbc:mysql://34.89.96.239:3306/fincore_db?useSSL=true&requireSSL=true&enabledTLSProtocols=TLSv1.2,TLSv1.3&serverTimezone=UTC";
        String user = "fincore_app";
        String password = args[0];
        
        System.out.println("========================================");
        System.out.println("Cloud SQL Connection Test");
        System.out.println("========================================");
        System.out.println();
        System.out.println("Attempting connection to: " + url);
        System.out.println("User: " + user);
        System.out.println();
        
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✓ MySQL driver loaded");
            
            // Establish connection
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✓ Database connection successful");
            
            // Test query
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT DATABASE(), USER(), VERSION()");
            if (rs.next()) {
                System.out.println();
                System.out.println("Connection Details:");
                System.out.println("  Database: " + rs.getString(1));
                System.out.println("  User: " + rs.getString(2));
                System.out.println("  MySQL Version: " + rs.getString(3));
            }
            
            // Check tables
            System.out.println();
            System.out.println("Checking tables...");
            rs = stmt.executeQuery("SHOW TABLES");
            int tableCount = 0;
            while (rs.next()) {
                System.out.println("  ✓ " + rs.getString(1));
                tableCount++;
            }
            System.out.println();
            System.out.println("Total tables: " + tableCount);
            
            // Check required tables
            String[] requiredTables = {"users", "roles", "permissions", "address", "organisation", "kyc_documents", "otp_tokens", "role_permissions"};
            System.out.println();
            System.out.println("Verifying required tables:");
            boolean allTablesExist = true;
            for (String table : requiredTables) {
                rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'fincore_db' AND table_name = '" + table + "'");
                rs.next();
                if (rs.getInt(1) > 0) {
                    System.out.println("  ✓ " + table);
                } else {
                    System.out.println("  ✗ " + table + " (MISSING)");
                    allTablesExist = false;
                }
            }
            
            // Check user permissions
            System.out.println();
            System.out.println("Checking user permissions:");
            rs = stmt.executeQuery("SHOW GRANTS FOR 'fincore_app'@'%'");
            while (rs.next()) {
                System.out.println("  " + rs.getString(1));
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
            System.out.println();
            if (allTablesExist) {
                System.out.println("========================================");
                System.out.println("✓ ALL CHECKS PASSED - Safe to deploy!");
                System.out.println("========================================");
                System.exit(0);
            } else {
                System.out.println("========================================");
                System.out.println("✗ MISSING TABLES - DO NOT DEPLOY");
                System.out.println("========================================");
                System.exit(1);
            }
            
        } catch (Exception e) {
            System.err.println();
            System.err.println("========================================");
            System.err.println("✗ Connection failed:");
            System.err.println("========================================");
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
