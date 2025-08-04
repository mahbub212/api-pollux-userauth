package id.co.msiglife.pollux.kafkalogger;

public interface KafkaMessageProducer {
    void sendMessage(String topic, String message);
}
