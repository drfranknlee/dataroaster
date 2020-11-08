package com.cloudcheflabs.dataroaster.streaming;

import com.cloudcheflabs.dataroaster.util.StringUtils;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class EventProducer {

    public static void main(String[] args) throws Exception{
        OptionParser parser = new OptionParser();
        parser.accepts("bootstrap.servers").withRequiredArg().ofType(String.class);
        parser.accepts("topic").withRequiredArg().ofType(String.class);

        OptionSet options = parser.parse(args);
        String bootstrapServers = (String) options.valueOf("bootstrap.servers");
        String topic = (String) options.valueOf("topic");

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "any-client-id");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Producer<Integer, String> producer = new KafkaProducer<>(props);

        // read json.
        String jsonFile = StringUtils.fileToString("data/test.json");
        String lines[] = jsonFile.split("\\r?\\n");

        long MAX = Long.MAX_VALUE;
        for(int i = 0; i < MAX; i++) {
            int key = i % 3;
            for(String json : lines) {
                System.out.println("json: [" + json + "]");
                RecordMetadata metadata = producer.send(new ProducerRecord<Integer, String>(topic, key, json)).get();
                System.out.println("partition: [" + metadata.partition() + "], offset: [" + metadata.offset() + "]");
            }

            Thread.sleep(5000);
            System.out.println("json sent [" + i + "]");
        }
    }
}
