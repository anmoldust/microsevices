# No-Nonsense Order Processing with Saga Pattern

This project demonstrates a distributed transaction system using the Saga pattern with choreography-based orchestration. It consists of three microservices: `order`, `payment`, and `Inventory`, which communicate asynchronously using Kafka.

## Project Overview

The goal of this project is to process an order by coordinating the `payment` and `Inventory` services. The `order` service acts as the central orchestrator of the saga, ensuring that the entire process is either completed successfully or all changes are reverted in case of a failure.

### Microservices

*   **`order`**: The main service that orchestrates the saga. It exposes a REST API for creating and querying orders.
*   **`payment`**: A service responsible for processing payments and refunds.
*   **`Inventory`**: A service that manages inventory reservations and releases.

## Prerequisites

*   Java 17 or higher
*   Maven
*   Docker
*   Docker Compose

## How to Run

1.  **Start the infrastructure:**
    *   Open a terminal and navigate to the root directory of the project.
    *   Run the following command to start Kafka and Zookeeper using Docker Compose:
        ```bash
        docker-compose up -d
        ```

2.  **Run the microservices:**
    *   Open a separate terminal for each microservice (`order`, `payment`, `Inventory`).
    *   Navigate to the root directory of each microservice.
    *   Run the following command to start the Spring Boot application:
        ```bash
        ./mvnw spring-boot:run
        ```

## API Endpoints

### Create Order

*   **`POST /orders`**
*   **Description**: Creates a new order and starts the saga.
*   **Response**: `Order created with id: <orderId>`

### Get Order

*   **`GET /orders/{orderId}`**
*   **Description**: Retrieves the status of an order.
*   **Response**:
    ```json
    {
      "orderId": "<orderId>",
      "status": "<status>"
    }
    ```

# Saga Pattern Implementation

The project implements the Saga pattern for handling distributed transactions across the `order`, `payment`, and `Inventory` microservices. The `order` microservice acts as the orchestrator of the saga.

## Saga Flow

1.  **Order Creation**:
    *   A user creates an order by sending a `POST` request to the `/orders` endpoint in the `order` microservice.
    *   The `OrderController` calls the `OrderCommandService` to create a new order.
    *   The `OrderCommandService` creates a new `Order` aggregate, saves it to the database, and starts the `OrderSaga`.

2.  **Payment**:
    *   The `OrderSaga` starts by sending a message to the `payment-commands` Kafka topic with the `orderId`.
    *   The `PaymentCommandConsumer` in the `payment` microservice consumes the message and calls the `PaymentService` to process the payment.
    *   The `PaymentService` simulates a payment and publishes a `payment-events` message to Kafka with the result (`SUCCESS` or `FAILED`).

3.  **Payment Response**:
    *   The `PaymentEventConsumer` in the `order` microservice consumes the `payment-events` message.
    *   **If the payment was successful**: The `OrderSaga` is notified and proceeds to the next step: inventory reservation.
    *   **If the payment failed**: The `OrderSaga` is notified, and the order is canceled. The saga ends.

4.  **Inventory Reservation**:
    *   If the payment was successful, the `OrderSaga` sends a message to the `inventory-commands` Kafka topic with the `orderId`.
    *   The `InventoryCommandConsumer` in the `Inventory` microservice consumes the message and calls the `InventoryService` to reserve the inventory.
    *   The `InventoryService` simulates an inventory reservation and publishes an `inventory-events` message to Kafka with the result (`SUCCESS` or `FAILED`).

5.  **Inventory Response**:
    *   The `InventoryEventConsumer` in the `order` microservice consumes the `inventory-events` message.
    *   **If the inventory reservation was successful**: The `OrderSaga` is notified, and the order is marked as completed. The saga ends.
    *   **If the inventory reservation failed**: The `OrderSaga` is notified, and a compensating transaction is triggered to refund the payment.

6.  **Compensation (Payment Refund)**:
    *   If the inventory reservation failed, the `OrderSaga` sends a message to the `payment-refund-commands` Kafka topic with the `orderId`.
    *   The `RefundCommandConsumer` in the `payment` microservice consumes the message and calls the `PaymentService` to refund the payment.
    *   The order is canceled, and the saga ends in a failed state.

# Microservice Communication

This document outlines the communication between the microservices using Kafka topics.

## Kafka Topics and Messages

### `payment-commands`

*   **Producer**: `order` microservice (`OrderSaga`)
*   **Consumer**: `payment` microservice (`PaymentCommandConsumer`)
*   **Message**: `orderId` (String)
*   **Purpose**: To request a payment for an order.

### `payment-refund-commands`

*   **Producer**: `order` microservice (`OrderSaga`)
*   **Consumer**: `payment` microservice (`RefundCommandConsumer`)
*   **Message**: `orderId` (String)
*   **Purpose**: To request a payment refund for a failed order.

### `payment-events`

*   **Producer**: `payment` microservice (`PaymentEventProducer`)
*   **Consumer**: `order` microservice (`PaymentEventConsumer`)
    *Note: There might be a slight confusion in the previous analysis. The consumer of 'payment-events' should be in the 'order' service, not 'inventory'. I'll assume it's `InventoryEventConsumer` if that's what was intended to consume `payment-events`, but typically it would be an `OrderEventConsumer` or similar. Given the current code, it's more likely the 'order' service listens for all saga events.*
*   **Message**: `orderId:STATUS` (String), where `STATUS` is either `SUCCESS` or `FAILED`.
*   **Purpose**: To notify the `order` microservice about the result of a payment.

### `inventory-commands`

*   **Producer**: `order` microservice (`OrderSaga`)
*   **Consumer**: `Inventory` microservice (`InventoryCommandConsumer`)
*   **Message**: `orderId` (String)
*   **Purpose**: To request inventory reservation for an order.

### `inventory-events`

*   **Producer**: `Inventory` microservice (`InventoryEventProducer`)
*   **Consumer**: `order` microservice (`InventoryEventConsumer`)
*   **Message**: `orderId:STATUS` (String), where `STATUS` is either `SUCCESS` or `FAILED`.
*   **Purpose**: To notify the `order` microservice about the result of an inventory reservation.

### `inventory-release-commands`

*   **Producer**: None in this project.
*   **Consumer**: `Inventory` microservice (`ReleaseInventoryConsumer`)
*   **Message**: `orderId` (String)
*   **Purpose**: To release a reserved inventory. This is not used in the current implementation of the saga.
