package com.finflow.audit.infrastructure.kafka;

import com.finflow.events.payment.PaymentApprovedEvent;
import com.finflow.events.payment.PaymentCreatedEvent;
import com.finflow.events.payment.PaymentRejectedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> consumerProps(String groupId) {
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, groupId,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false
        );
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> factory(
            Class<T> type, String dlqTopic, KafkaTemplate<String, Object> kafkaTemplate) {

        var deserializer = new JsonDeserializer<>(type, false);
        var consumerFactory = new DefaultKafkaConsumerFactory<>(
                consumerProps("audit-service"), new StringDeserializer(), deserializer);

        var backoff = new ExponentialBackOff(1_000L, 2.0);
        backoff.setMaxElapsedTime(8_000L);

        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition(dlqTopic, 0));

        var f = new ConcurrentKafkaListenerContainerFactory<String, T>();
        f.setConsumerFactory(consumerFactory);
        f.setCommonErrorHandler(new DefaultErrorHandler(recoverer, backoff));
        return f;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentCreatedEvent> paymentCreatedListenerFactory(
            KafkaTemplate<String, Object> kafkaTemplate) {
        return factory(PaymentCreatedEvent.class, "payment-created.DLQ", kafkaTemplate);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentApprovedEvent> paymentApprovedListenerFactory(
            KafkaTemplate<String, Object> kafkaTemplate) {
        return factory(PaymentApprovedEvent.class, "payment-approved.DLQ", kafkaTemplate);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentRejectedEvent> paymentRejectedListenerFactory(
            KafkaTemplate<String, Object> kafkaTemplate) {
        return factory(PaymentRejectedEvent.class, "payment-rejected.DLQ", kafkaTemplate);
    }
}
