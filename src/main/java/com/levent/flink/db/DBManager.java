package com.levent.flink.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DBManager {
    private static HikariDataSource dataSource;
    private static List<Double[]> buffer;
    private static int batchSize;

    public DBManager(Properties props) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.user"));
        config.setPassword(props.getProperty("db.password"));
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.maxPoolSize")));

        dataSource = new HikariDataSource(config);

        buffer = new ArrayList<>();
        batchSize = Integer.parseInt(props.getProperty("db.batchSize"));
    }

    public void insertRecord(Double valueC, Double valueD, int statusA, int statusB, Double result) throws SQLException {
        buffer.add(new Double[]{valueC, valueD, (double) statusA, (double) statusB, result});

        if (buffer.size() >= batchSize) {
            flushBuffer();
        }
    }

    public void flushBuffer() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO calculation_results (valueC, valueD, statusA, statusB, result) VALUES (?, ?, ?, ?, ?)")) {

            for (Double[] record : buffer) {
                ps.setDouble(1, record[0]);
                ps.setDouble(2, record[1]);
                ps.setInt(3, record[2].intValue());
                ps.setInt(4, record[3].intValue());
                ps.setDouble(5, record[4]);
                ps.addBatch();
            }

            ps.executeBatch();
            buffer.clear();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        try {
            if (!buffer.isEmpty()) {
                flushBuffer();
            }
            dataSource.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
