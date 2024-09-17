package com.levent.flink.function;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction;
import org.apache.flink.util.Collector;

public class TupleMergeFlatMapFunction implements CoFlatMapFunction<Tuple2<String, String>, Tuple2<String, String>, Tuple4<String, String, String, String>> {

    private Tuple2<String, String> stateAB = null;
    private Tuple2<String, String> stateCD = null;

    @Override
    public void flatMap1(Tuple2<String, String> valueAB, Collector<Tuple4<String, String, String, String>> out) throws Exception {
        this.stateAB = valueAB;
        if (this.stateCD != null) {
            out.collect(new Tuple4<>(this.stateAB.f0, this.stateAB.f1, this.stateCD.f0, this.stateCD.f1));
            this.stateAB = null;
            this.stateCD = null;
        }
    }

    @Override
    public void flatMap2(Tuple2<String, String> valueCD, Collector<Tuple4<String, String, String, String>> out) throws Exception {
        this.stateCD = valueCD;
        if (this.stateAB != null) {
            out.collect(new Tuple4<>(this.stateAB.f0, this.stateAB.f1, this.stateCD.f0, this.stateCD.f1));
            this.stateAB = null;
            this.stateCD = null;
        }
    }
}
