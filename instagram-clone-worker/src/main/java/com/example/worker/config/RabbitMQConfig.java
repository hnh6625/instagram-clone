package com.example.worker.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String MEDIA_QUEUE = "media.processing.queue";
    public static final String MEDIA_EXCHANGE = "media.exchange";
    public static final String MEDIA_ROUTING_KEY = "media.process";

    public static final String RETRY_EXCHANGE = "media.retry.exchange";
    public static final String RETRY_QUEUE = "media.retry.queue";
    public static final String RETRY_ROUTING_KEY = "media.retry";

    public static final String DLQ_EXCHANGE = "media.dlq.exchange";
    public static final String DLQ_QUEUE = "media.dlq.queue";
    public static final String DLQ_ROUTING_KEY = "media.dlq";

    @Bean
    public DirectExchange mediaExchange() {
        return new DirectExchange(MEDIA_EXCHANGE);
    }

    @Bean
    public Queue mediaQueue() {
        return QueueBuilder.durable(MEDIA_QUEUE)
                .withArgument("x-dead-letter-exchange", RETRY_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RETRY_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding mediaBinding(Queue mediaQueue, DirectExchange mediaExchange) {
        return BindingBuilder.bind(mediaQueue).to(mediaExchange).with(MEDIA_ROUTING_KEY);
    }

    @Bean
    public DirectExchange retryExchange() {
        return new DirectExchange(RETRY_EXCHANGE);
    }

    @Bean
    public Queue retryQueue() {
        return QueueBuilder.durable(RETRY_QUEUE)
                .withArgument("x-message-ttl", 5000)
                .withArgument("x-dead-letter-exchange", MEDIA_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", MEDIA_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding retryBinding(Queue retryQueue, DirectExchange retryExchange) {
        return BindingBuilder.bind(retryQueue).to(retryExchange).with(RETRY_ROUTING_KEY);
    }

    @Bean
    public DirectExchange dlqExchange() {
        return new DirectExchange(DLQ_EXCHANGE);
    }
    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }

    @Bean
    public Binding dlqBinding(Queue dlqQueue, DirectExchange dlqExchange) {
        return BindingBuilder.bind(dlqQueue).to(dlqExchange).with(DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}