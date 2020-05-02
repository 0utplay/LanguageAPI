package de.tentact.languageapi.mysql;
/*  Created in the IntelliJ IDEA.
    Created by 0utplay | Aldin Sijamhodzic
    Datum: 25.04.2020
    Uhrzeit: 16:53
*/

import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQL {

    private String hostname, database, username, password;
    private int port;
    private Connection con;

    public MySQL(String hostname, String database, String username, String password, int port) {
        this.hostname = hostname;
        this.database = database;
        this.username = username;
        this.password = password;
        this.port = port;
    }

    public void connect() {
        if (!isConnected()) {
            try {
                con = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database, username, password);
                Bukkit.getConsoleSender().sendMessage("§aMySQL Connected");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

    }

    public boolean isConnected() {
        return con != null;
    }

    public void close() {
        if (isConnected()) {
            try {
                con.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void createDefaultTable() {
        try {
            con.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS choosenlang(uuid VARCHAR(64), language VARCHAR(64));");
            con.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS languages(language VARCHAR(64));");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void createTable(String tableName) {
        try {
            con.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + "(transkey VARCHAR(64), translation VARCHAR(2000));");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void update(String qry) {
        if (isConnected()) {
            try {
                con.createStatement().executeUpdate(qry);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public ResultSet getResult(String qry) {
        if (isConnected()) {
            try {
                return con.createStatement().executeQuery(qry);
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

}
