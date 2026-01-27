package com.company.saga.order.messaging.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderCommandProducer {

    private static final Logger log =
            LoggerFactory.getLogger(OrderCommandProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderCommandProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentCommand(UUID orderId) {
        log.info("Sending PAYMENT command for orderId={}", orderId);

        kafkaTemplate.send("payment-commands", orderId.toString())
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info(
                            "PAYMENT command sent successfully | topic={} | partition={} | offset={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                        );
                    } else {
                        log.error("Failed to send PAYMENT command for orderId={}", orderId, ex);
                    }
                });
    }

    public void sendInventoryCommand(UUID orderId) {
        log.info("Sending INVENTORY command for orderId={}", orderId);

        kafkaTemplate.send("inventory-commands", orderId.toString())
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info(
                            "INVENTORY command sent successfully | topic={} | partition={} | offset={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                        );
                    } else {
                        log.error("Failed to send INVENTORY command for orderId={}", orderId, ex);
                    }
                });
    }

    public void sendRefundCommand(UUID orderId) {
        log.info("Sending REFUND command for orderId={}", orderId);

        kafkaTemplate.send("payment-refund-commands", orderId.toString())
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info(
                            "REFUND command sent successfully | topic={} | partition={} | offset={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                        );
                    } else {
                        log.error("Failed to send REFUND command for orderId={}", orderId, ex);
                    }
                });
    }
}
