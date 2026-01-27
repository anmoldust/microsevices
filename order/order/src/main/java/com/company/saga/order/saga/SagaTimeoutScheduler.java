package com.company.saga.order.saga;

import com.company.saga.order.component.SagaStep;
import com.company.saga.order.config.SagaTimeoutConfig;
import com.company.saga.order.outbox.OutboxEvent;
import com.company.saga.order.repository.OrderSagaRepository;
import com.company.saga.order.repository.OutboxRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class SagaTimeoutScheduler {

    private final OrderSagaRepository sagaRepository;
    private final OutboxRepository outboxRepository;

    public SagaTimeoutScheduler(OrderSagaRepository sagaRepository,
                                OutboxRepository outboxRepository) {
        this.sagaRepository = sagaRepository;
        this.outboxRepository = outboxRepository;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
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
            case PAYMENT -> outboxRepository.save(
                    new OutboxEvent("payment-commands", saga.getOrderId().toString())
            );
            case INVENTORY -> outboxRepository.save(
                    new OutboxEvent("inventory-commands", saga.getOrderId().toString())
            );
        }
    }

    private void compensate(OrderSagaState saga) {
        if (saga.getCurrentStep() == SagaStep.INVENTORY) {
            outboxRepository.save(
                    new OutboxEvent("payment-refund-commands", saga.getOrderId().toString())
            );
        }
    }
}
