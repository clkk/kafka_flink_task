# Flink Kafka Streaming Project

## Overview

This project demonstrates a real-time data processing pipeline using Apache Flink and Apache Kafka. It involves consuming multiple Kafka topics, merging stream data, applying specific business logic, and then producing results to a Kafka topic. Additionally, the project includes database management for storing processed results using MySQL.

## Features

- **Kafka Producers:** Generates random data and sends it to Kafka topics.
- **Flink Job:** Consumes the Kafka topics and processes the streams with custom logic.
- **Stream Merging:** Combines multiple streams using both FlatMap and CoProcess functions.
- **Dynamic Processing Control:** Processes data based on event triggers (start/stop commands) from an `eventE` Kafka topic.
- **Database Management:** Buffers and batches results, which are then inserted into a MySQL database.

## Components

### 1. **DBManager.java**
Handles all database operations, including connecting to the MySQL database and batching the insertion of processed data.

### 2. **KafkaProducers.java**
Produces random values into Kafka topics like `valueC` and `valueD` for testing the streaming job.

### 3. **FlinkJob.java**
The core of the project, containing the Flink job definition, which sets up the Kafka consumers, processes the incoming streams, and applies the custom business logic.

### 4. **TupleMergeFlatMapFunction.java**
Merges multiple streams into Tuple types and performs necessary transformations on the data.

### 5. **MergeCoProcessFunction.java**
CoProcessFunction to merge the streams from Kafka topics into a single stream, managing state for event-driven control flow.

### 6. **TupleLogicFlatMapFunction.java**
Applies the final logic to the merged data, including mathematical operations like sum, dividing, and conditionally processing based on the status of stream values.

## Prerequisites

- **Apache Kafka**
- **Apache Flink**
- **MySQL Database**
- **Docker (for running Kafka and Zookeeper)**

## Setup

1. **MySQL Configuration:**  
   Update the database connection properties in config.properties.
   Before running the project, ensure that the necessary table is created in MySQL by executing the SQL schema provided in schema.sql. The table is required to store the processed calculation results.

2. **Run the Kafka Producers:**  
   Use `KafkaProducers.java` to start producing data into Kafka.

3. **Run the Flink Job:**  
   Use `FlinkJob.java` to start the stream processing pipeline.

4. **Monitoring Results:**  
   The processed results are either printed or inserted into the MySQL database, depending on the state of the event stream.

## Running on Docker

Use the provided `docker-compose.yml` file to start the necessary services