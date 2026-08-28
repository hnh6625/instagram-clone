package com.example.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String MEDIA_QUEUE = "media.processing.queue";
    public static final String MEDIA_EXCHANGE = "media.exchange";
    public static final String MEDIA_ROUTING_KEY = "media.process";

//    @Bean
//    public Queue mediaQueue() {
//        return new Queue(MEDIA_QUEUE, true);
//    }
//
//    @Bean
//    public DirectExchange mediaExchange() {
//        return new DirectExchange(MEDIA_EXCHANGE);
//    }
//
//    @Bean
//    public Binding mediaBinding(Queue mediaQueue, DirectExchange mediaExchange) {
//        return BindingBuilder
//                .bind(mediaQueue)
//                .to(mediaExchange)
//                .with(MEDIA_ROUTING_KEY);
//    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}