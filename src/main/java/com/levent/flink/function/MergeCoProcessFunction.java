package com.levent.flink.function;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;

public class MergeCoProcessFunction extends CoProcessFunction<String, String, Tuple2<String, String>> {

    private ValueState<String> state1;
    private ValueState<String> state2;

    @Override
    public void open(Configuration parameters) throws Exception {
        state1 = getRuntimeContext().getState(
                new ValueStateDescriptor<>("state1", Types.STRING)
        );
        state2 = getRuntimeContext().getState(
                new ValueStateDescriptor<>("state2", Types.STRING)
        );
    }

    @Override
    public void processElement1(String value1, Context ctx, Collector<Tuple2<String, String>> out) throws Exception {
        state1.update(value1);

        String current2 = state2.value();
        if (current2 != null) {
            out.collect(new Tuple2<>(value1, current2));
            state1.clear();
            state2.clear();
        }
    }

    @Override
    public void processElement2(String value2, Context ctx, Collector<Tuple2<String, String>> out) throws Exception {
        state2.update(value2);

        String current1 = state1.value();
        if (current1 != null) {
            out.collect(new Tuple2<>(current1, value2));
            state1.clear();
            state2.clear();
        }
    }
}
