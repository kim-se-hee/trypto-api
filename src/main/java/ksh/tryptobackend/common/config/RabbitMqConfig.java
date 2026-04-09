package ksh.tryptobackend.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class RabbitMqConfig {

    public static final String TICKER_MARKETDATA_LISTENER_ID = "tickerMarketdataListener";
    public static final String MATCHED_ORDERS_LISTENER_ID = "matchedOrdersListener";

    private static final String MATCHED_ORDERS_EXCHANGE = "matched.orders";
    private static final String MATCHED_ORDERS_QUEUE = "matched.orders";
    private static final String MATCHED_ORDERS_DLQ = "matched.orders.dlq";

    @Value("${app.rabbitmq.ticker-exchange:ticker.exchange}")
    private String tickerExchangeName;

    @Bean
    public FanoutExchange tickerFanoutExchange() {
        return new FanoutExchange(tickerExchangeName, true, false);
    }

    @Bean
    public Queue tickerMarketdataQueue() {
        String queueName = "ticker.marketdata." + UUID.randomUUID().toString().substring(0, 8);
        return new Queue(queueName, false, true, true);
    }

    @Bean
    public Binding tickerMarketdataBinding(Queue tickerMarketdataQueue, FanoutExchange tickerFanoutExchange) {
        return BindingBuilder.bind(tickerMarketdataQueue).to(tickerFanoutExchange);
    }

    @Bean
    public DirectExchange matchedOrdersExchange() {
        return new DirectExchange(MATCHED_ORDERS_EXCHANGE, true, false);
    }

    @Bean
    public Queue matchedOrdersQueue() {
        return QueueBuilder.durable(MATCHED_ORDERS_QUEUE)
            .deadLetterExchange(MATCHED_ORDERS_EXCHANGE + ".dlx")
            .deadLetterRoutingKey(MATCHED_ORDERS_DLQ)
            .build();
    }

    @Bean
    public Queue matchedOrdersDlq() {
        return QueueBuilder.durable(MATCHED_ORDERS_DLQ).build();
    }

    @Bean
    public DirectExchange matchedOrdersDlx() {
        return new DirectExchange(MATCHED_ORDERS_EXCHANGE + ".dlx", true, false);
    }

    @Bean
    public Binding matchedOrdersBinding(Queue matchedOrdersQueue, DirectExchange matchedOrdersExchange) {
        return BindingBuilder.bind(matchedOrdersQueue).to(matchedOrdersExchange).with(MATCHED_ORDERS_QUEUE);
    }

    @Bean
    public Binding matchedOrdersDlqBinding(Queue matchedOrdersDlq, DirectExchange matchedOrdersDlx) {
        return BindingBuilder.bind(matchedOrdersDlq).to(matchedOrdersDlx).with(MATCHED_ORDERS_DLQ);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
