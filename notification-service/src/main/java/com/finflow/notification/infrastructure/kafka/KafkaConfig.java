package com.finflow.notification.infrastructure.kafka;

import com.finflow.events.payment.PaymentApprovedEvent;
import com.finflow.events.payment.PaymentRejectedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
        ));
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentApprovedEvent> paymentApprovedListenerFactory(
            KafkaTemplate<String, Object> kafkaTemplate) {

        var deserializer = new JsonDeserializer<>(PaymentApprovedEvent.class, false);
        var consumerFactory = new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "notification-service",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false
        ), new StringDeserializer(), deserializer);

        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition("payment-approved.DLQ", 0));
        var factory = new ConcurrentKafkaListenerContainerFactory<String, PaymentApprovedEvent>();
        factory.setConsumerFactory(consumerFactory);
        var backoff1 = new ExponentialBackOff(1_000L, 2.0);
        backoff1.setMaxElapsedTime(8_000L);
        factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, backoff1));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentRejectedEvent> paymentRejectedListenerFactory(
            KafkaTemplate<String, Object> kafkaTemplate) {

        var deserializer = new JsonDeserializer<>(PaymentRejectedEvent.class, false);
        var consumerFactory = new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "notification-service",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false
        ), new StringDeserializer(), deserializer);

        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition("payment-rejected.DLQ", 0));
        var factory = new ConcurrentKafkaListenerContainerFactory<String, PaymentRejectedEvent>();
        factory.setConsumerFactory(consumerFactory);
        var backoff2 = new ExponentialBackOff(1_000L, 2.0);
        backoff2.setMaxElapsedTime(8_000L);
        factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, backoff2));
        return factory;
    }
}
