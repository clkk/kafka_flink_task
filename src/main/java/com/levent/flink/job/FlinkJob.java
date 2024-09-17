package com.levent.flink.job;

import com.levent.flink.function.MergeCoProcessFunction;
import com.levent.flink.function.TupleLogicFlatMapFunction;
import com.levent.flink.function.TupleMergeFlatMapFunction;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;

public class FlinkJob {
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    public static void main( String[] args ) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().disableClosureCleaner();

        KafkaSource<String> sourceE = KafkaSource.<String>builder()
                .setBootstrapServers(BOOTSTRAP_SERVERS)
                .setTopics("eventE")
                .setGroupId("flink-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        KafkaSource<String> sourceA = KafkaSource.<String>builder()
                .setBootstrapServers(BOOTSTRAP_SERVERS)
                .setTopics("statusA")
                .setGroupId("flink-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        KafkaSource<String> sourceB = KafkaSource.<String>builder()
                .setBootstrapServers(BOOTSTRAP_SERVERS)
                .setTopics("statusB")
                .setGroupId("flink-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        KafkaSource<String> sourceC = KafkaSource.<String>builder()
                .setBootstrapServers(BOOTSTRAP_SERVERS)
                .setTopics("valueC")
                .setGroupId("flink-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        KafkaSource<String> sourceD = KafkaSource.<String>builder()
                .setBootstrapServers(BOOTSTRAP_SERVERS)
                .setTopics("valueD")
                .setGroupId("flink-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStream<String> streamE = env.fromSource(sourceE, WatermarkStrategy.noWatermarks(), "Source E");

        DataStream<String> streamA = env.fromSource(sourceA, WatermarkStrategy
                .<String>forBoundedOutOfOrderness(Duration.ZERO)
                .withTimestampAssigner((event, timestamp) -> System.currentTimeMillis()), "Source A");

        DataStream<String> streamB = env.fromSource(sourceB, WatermarkStrategy
                .<String>forBoundedOutOfOrderness(Duration.ofMillis(100))
                .withTimestampAssigner((event, timestamp) -> System.currentTimeMillis()), "Source B");

        DataStream<String> streamC = env.fromSource(sourceC, WatermarkStrategy
                .<String>forBoundedOutOfOrderness(Duration.ofMillis(150))
                .withTimestampAssigner((event, timestamp) -> System.currentTimeMillis()), "Source C");

        DataStream<String> streamD = env.fromSource(sourceD, WatermarkStrategy
                .<String>forBoundedOutOfOrderness(Duration.ofSeconds(1))
                .withTimestampAssigner((event, timestamp) -> System.currentTimeMillis()), "Source D");


        DataStream<Tuple2<String, String>> connectedCDStream = streamC
                .connect(streamD.broadcast())
                .keyBy(value -> 1, value -> 1)
                .process(new MergeCoProcessFunction());

        DataStream<Tuple2<String, String>> connectedABStream = streamA
                .connect(streamB)
                .keyBy(value -> 1, value -> 1)
                .process(new MergeCoProcessFunction());

        DataStream<Tuple4<String, String, String, String>> tuple4ResultStream = connectedABStream
                .connect(connectedCDStream)
                .flatMap(new TupleMergeFlatMapFunction());

        DataStream<String> resultStream = tuple4ResultStream.connect(streamE.broadcast()).flatMap(new TupleLogicFlatMapFunction());


        KafkaSink<String> resultSink = KafkaSink.<String>builder()
                .setBootstrapServers(BOOTSTRAP_SERVERS)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("eventE")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                .build();

        resultStream.sinkTo(resultSink);

        env.execute("Flink Kafka Example 1");
    }
}





