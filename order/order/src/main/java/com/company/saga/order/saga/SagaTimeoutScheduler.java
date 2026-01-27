package com.company.saga.order.saga;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.company.saga.order.component.SagaStep;
import com.company.saga.order.config.SagaTimeoutConfig;
import com.company.saga.order.messaging.producer.OrderCommandProducer;
import com.company.saga.order.repository.OrderSagaRepository;

import java.time.Instant;
import java.util.List;

@Component
public class SagaTimeoutScheduler {

    private final OrderSagaRepository sagaRepository;
    private final OrderCommandProducer producer;

    public SagaTimeoutScheduler(OrderSagaRepository sagaRepository,
                                OrderCommandProducer producer) {
        this.sagaRepository = sagaRepository;
        this.producer = producer;
    }

//    @Scheduled(fixedDelay = 5000)
    public void checkForTimeouts() {
        Instant timeoutThreshold =
                Instant.now().minusSeconds(SagaTimeoutConfig.STEP_TIMEOUT_SECONDS);

        List<OrderSagaState> stalledSagas =
                sagaRepository.findAll().stream()
                        .filter(saga ->
                                saga.getStatus() == SagaStatus.IN_PROGRESS &&
                                saga.getLastUpdatedAt().isBefore(timeoutThreshold)
                        )
                        .toList();

        for (OrderSagaState saga : stalledSagas) {
            handleTimeout(saga);
        }
    }

    private void handleTimeout(OrderSagaState saga) {
        if (saga.getRetryCount() >= SagaTimeoutConfig.MAX_RETRIES) {
            saga.markCompensating();
            sagaRepository.save(saga);

            compensate(saga);
            saga.markFailed();
            sagaRepository.save(saga);
            return;
        }

        saga.incrementRetry();
        sagaRepository.save(saga);

        retryStep(saga);
    }

    private void retryStep(OrderSagaState saga) {
        switch (saga.getCurrentStep()) {
            case PAYMENT -> producer.sendPaymentCommand(saga.getOrderId());
            case INVENTORY -> producer.sendInventoryCommand(saga.getOrderId());
        }
    }

    private void compensate(OrderSagaState saga) {
        if (saga.getCurrentStep() == SagaStep.INVENTORY) {
            producer.sendRefundCommand(saga.getOrderId());
        }
    }
}