package com.leonardobishop.quests.sql;

import com.leonardobishop.quests.Quests;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLConnector {

    private Connection connection = null;
    private String tablePrefix;

    // "never store passwords as strins", well here's not the case xD
    public void loadMySQL(String host, String user, String password, String port, String name, boolean useSSL, String tablePrefix) {
        this.tablePrefix = tablePrefix;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + "/" + name + "?user=" + user + "&password=" + password + "&useSSL=" + useSSL + "&autoReconnect=true");
            // quests IDs can only go 50 characters long (the limitation of VARCHAR(50))
            PreparedStatement ps = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS progress(id INT PRIMARY KEY AUTOINCREMENT, player-uuid VARCHAR(36) NOT NULL, quest-id VARCHAR(50), started BOOLEAN, completed BOOLEAN, completed_before BOOLEAN, completition_date BIGINT(19));");
            // create the table "progress"
            ps.executeUpdate();
            if (ps != null) //Close the prepared statement
                ps.close();
            ps = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS task-progress(id INT PRIMARY KEY AUTOINCREMENT, player-uuid VARCHAR(36) NOT NULL, quest-id VARCHAR(50), task-id VARCHAR(50), completed BOOLEAN, progress INT);");
            // create the table "task-progress"
            ps.executeUpdate();
            if (ps != null) //Close the prepared statement
                ps.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
            // Oh no
        }
    }

    public void loaSQL(String fileName) {
        try {
            File file = new File(Quests.get().getDataFolder() + File.separator + fileName); // no need to create the file or check is exists or not
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            // quests IDs can only go 50 characters long (the limitation of VARCHAR(50))
            PreparedStatement ps = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS progress(id INT PRIMARY KEY AUTOINCREMENT, player-uuid VARCHAR(36) NOT NULL, quest-id VARCHAR(50), started BOOLEAN, completed BOOLEAN, completed_before BOOLEAN, completition_date BIGINT(19));");
            // create the table "progress"
            ps.executeUpdate();
            if (ps != null) //Close the prepared statement
                ps.close();
            ps = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS task-progress(id INT PRIMARY KEY AUTOINCREMENT, player-uuid VARCHAR(36) NOT NULL, quest-id VARCHAR(50), task-id VARCHAR(50), completed BOOLEAN, progress INT);");
            // create the table "task-progress"
            ps.executeUpdate();
            if (ps != null) //Close the prepared statement
                ps.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
            // Oh no
        }
    }

    public String getTablePrefix() {
        return this.tablePrefix;
    }

    /**
     * @return The connection to the sql database
     * <p>
     * If the connection is not used, return null
     */
    public Connection getConnection() {
        return this.connection;
    }
}
