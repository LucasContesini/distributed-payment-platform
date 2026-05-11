package com.finflow.payment.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.events.fraud.FraudAnalysisRequestedEvent;
import com.finflow.events.payment.PaymentApprovedEvent;
import com.finflow.events.payment.PaymentRejectedEvent;
import com.finflow.payment.domain.outbox.OutboxEvent;
import com.finflow.payment.domain.outbox.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class OutboxRelayService {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayService.class);
    private static final int BATCH_SIZE = 100;

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxRelayService(
            OutboxEventRepository outboxRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 500, initialDelay = 2000)
    @Transactional
    public void relay() {
        var pending = outboxRepository.findUnpublishedWithLock(BATCH_SIZE);
        if (pending.isEmpty()) return;

        log.debug("Relaying {} outbox events", pending.size());

        for (var event : pending) {
            try {
                var payload = deserialize(event);
                kafkaTemplate.send(event.getTopic(), event.getAggregateId().toString(), payload)
                        .get(5, TimeUnit.SECONDS);
                event.markPublished();
                outboxRepository.save(event);
                log.info("outbox relayed id={} topic={} eventType={}", event.getId(), event.getTopic(), event.getEventType());
            } catch (Exception e) {
                log.error("outbox relay failed id={} topic={}", event.getId(), event.getTopic(), e);
            }
        }
    }

    private Object deserialize(OutboxEvent event) throws Exception {
        return switch (event.getEventType()) {
            case "fraud.analysis.requested" -> objectMapper.readValue(event.getPayload(), FraudAnalysisRequestedEvent.class);
            case "payment.approved"          -> objectMapper.readValue(event.getPayload(), PaymentApprovedEvent.class);
            case "payment.rejected"          -> objectMapper.readValue(event.getPayload(), PaymentRejectedEvent.class);
            default -> throw new IllegalStateException("Unknown outbox event type: " + event.getEventType());
        };
    }
}
