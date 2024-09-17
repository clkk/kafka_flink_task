package com.levent.flink.function;

import com.levent.flink.db.DBManager;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction;
import org.apache.flink.util.Collector;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public class TupleLogicFlatMapFunction
        extends RichCoFlatMapFunction<Tuple4<String, String, String, String>, String, String> {

    private transient DBManager dbManager;
    private boolean shouldProcess = false;

    @Override
    public void open(Configuration parameters) throws Exception {
        Properties props = new Properties();
        try (FileInputStream input = new FileInputStream("config.properties")) {
            props.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        dbManager = new DBManager(props);
    }

    @Override
    public void flatMap1(Tuple4<String, String, String, String> value, Collector<String> out) throws Exception {
        if (!shouldProcess)
            return;

        double result = 0.0;

        int statusA = Integer.parseInt(value.f0);
        int statusB = Integer.parseInt(value.f1);
        double valueC = Double.parseDouble(value.f2);
        double valueD = Double.parseDouble(value.f3);

        if (statusA == 1 && statusB == 1) {
            result = valueC + valueD;
        } else if (statusA == 0 && statusB == 0) {
            if (valueD != 0.0)
                result = valueC / valueD;
            else
                result = valueC; // **
        }
        if (result != 0) {
            out.collect(result + "");

            try {
                dbManager.insertRecord(valueC, valueD, statusA, statusB, result);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void flatMap2(String event, Collector<String> out)
    {
        if ("start".equals(event)) {
            shouldProcess = true;
        } else if ("close".equals(event)) {
            shouldProcess = false;
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        if (dbManager != null) {
            dbManager.shutdown();
        }
    }
}