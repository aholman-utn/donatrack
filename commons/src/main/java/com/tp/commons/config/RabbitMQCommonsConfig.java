package com.tp.commons.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQCommonsConfig {

    public static final String LOGISTICA_EVENTOS_EXCHANGE = "logistica.eventos.exchange";

    public static final String COLA_PLANIFICAR = "logistica.planificar.queue";
    public static final String COLA_EVENTOS_DONACIONES = "donaciones.logistica.eventos.queue";

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    @Bean
    public Queue notificacionesQueue() {
        return new Queue("notificaciones.queue", true);
    }

    // --- Exchanges ---
    @Bean
    public TopicExchange logisticaEventosExchange() {
        return new TopicExchange(LOGISTICA_EVENTOS_EXCHANGE);
    }

    // --- Colas Principales ---
    @Bean
    public Queue logisticaPlanificarQueue() {
        return new Queue(COLA_PLANIFICAR, true);
    }

    @Bean
    public Queue donacionesLogisticaEventosQueue() {
        return new Queue(COLA_EVENTOS_DONACIONES, true);
    }

    // --- Bindings Principales ---
    @Bean
    public Binding bindingEventosDonaciones() {
        return BindingBuilder.bind(donacionesLogisticaEventosQueue())
                .to(logisticaEventosExchange())
                .with("logistica.eventos.#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("*");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}