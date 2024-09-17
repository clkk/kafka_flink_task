package com.levent.flink.produce;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Random;

public class KafkaProducers {
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        Thread producerThreadE = new Thread(new KafkaProducerTask(producer, "eventE", 1000));
        Thread producerThreadA = new Thread(new KafkaProducerTask(producer, "statusA", 100));
        Thread producerThreadB = new Thread(new KafkaProducerTask(producer, "statusB", 143));
        Thread producerThreadC = new Thread(new KafkaProducerTask(producer, "valueC", 67));
        Thread producerThreadD = new Thread(new KafkaProducerTask(producer, "valueD", 50));

        producerThreadE.start();
        producerThreadA.start();
        producerThreadB.start();
        producerThreadC.start();
        producerThreadD.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            producer.close();
        }));
    }
}

class KafkaProducerTask implements Runnable {

    private final KafkaProducer<String, String> producer;
    private final String topic;
    private final int interval;
    private static final Random RANDOM = new Random();

    public KafkaProducerTask(KafkaProducer<String, String> producer, String topic, int interval) {
        this.producer = producer;
        this.topic = topic;
        this.interval = interval;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String message = "";
                if (topic.equals("eventE"))
                    message = "start"; // "close";
                else
                    message = (topic.equals("statusA") || topic.equals("statusB")) ?
                        String.valueOf(RANDOM.nextInt(2)) : String.valueOf(RANDOM.nextFloat() * 100);
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);
                producer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        exception.printStackTrace();
                    } else {
                        System.out.printf("Sent message to topic %s with offset %d%n", topic, metadata.offset());
                    }
                });
                if (topic.equals("eventE"))
                    break;
                Thread.sleep(this.interval);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        producer.close();
    }
}
