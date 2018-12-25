package com.karmazin.model;

import com.karmazin.controller.SimplePopup;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteBaseAPI {
    Connection connection;

    SQLiteBaseAPI(String path) {

        try {
            File dbFile = new File(path);
            if (!dbFile.exists()) {
                dbFile.createNewFile();
            }
            Class.forName("org.sqlite.JDBC");
            new File(path).getParentFile().mkdirs();
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC driver error!");
            connection = null;
            new SimplePopup().setupWindow(e.getMessage());
        } catch (SQLException e) {
            System.out.println("Can't open a file. Driver manager connection error!");
            connection = null;
            new SimplePopup().setupWindow(e.getMessage());
        } catch (IOException e) {
            System.out.println("Can't create a file!");
            connection = null;
            new SimplePopup().setupWindow(e.getMessage());
        }
    }

    public Statement createStatement() {
        Statement statement = null;
        try {
            statement =  connection.createStatement();
        } catch (SQLException e) {
            System.out.println("DB connection closed!");
            e.printStackTrace();
            return null;
        }
        return statement;
    }

    @Override
    public void finalize() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            connection = null;
        }
    }
}
