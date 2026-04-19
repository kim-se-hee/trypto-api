package ksh.tryptobackend.trading.adapter.out.event;

import ksh.tryptobackend.trading.adapter.out.messages.OrderCanceledEngineMessage;
import ksh.tryptobackend.trading.adapter.out.messages.OrderPlacedEngineMessage;
import ksh.tryptobackend.trading.domain.event.OrderCanceledEvent;
import ksh.tryptobackend.trading.domain.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class EngineInboxPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final MessageConverter messageConverter;

    @Value("${engine.inbox.queue:engine.inbox}")
    private String inboxQueue;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(OrderPlacedEvent event) {
        OrderPlacedEngineMessage payload = OrderPlacedEngineMessage.from(event.order());
        publish("OrderPlaced", payload, payload.orderId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCanceled(OrderCanceledEvent event) {
        OrderCanceledEngineMessage payload = OrderCanceledEngineMessage.from(event.order());
        publish("OrderCanceled", payload, payload.orderId());
    }

    private void publish(String type, Object payload, Long orderId) {
        try {
            MessageProperties props = new MessageProperties();
            props.setHeader("event_type", type);
            props.setDeliveryMode(MessageProperties.DEFAULT_DELIVERY_MODE);
            Message msg = messageConverter.toMessage(payload, props);
            rabbitTemplate.send("", inboxQueue, msg);
        } catch (Exception e) {
            log.warn("engine.inbox publish failed type={} orderId={}", type, orderId, e);
        }
    }
}
