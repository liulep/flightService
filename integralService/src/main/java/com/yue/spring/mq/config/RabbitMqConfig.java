package com.yue.spring.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;

@Configuration
public class RabbitMqConfig implements RabbitListenerConfigurer {
    /**
     * 交换机名称
     */
    public static final String EXCHANGE_NAME = "article_exchange";

    /**
     * 队列名称
     */
    public static final String QUEUE_NAME = "article_queue";

    public static final String ROUTING_KEY="article_key";

    /**
     * 创建一个队列
     *
     * @return
     */
    @Bean(QUEUE_NAME)
    public Queue ArticleQueue() {
        return new Queue(QUEUE_NAME,true);
    }

    /**
     * 创建一个交换机
     *
     * @return
     */
    @Bean(EXCHANGE_NAME)
    public DirectExchange ArticleExchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    /**
     * @param exchange 交换机对象
     * @param queue    队列对象
     */
    @Bean
    public Binding articleBinding(@Qualifier(EXCHANGE_NAME) Exchange exchange, @Qualifier(QUEUE_NAME) Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY).noargs();
    }


    // 可以将json串反序列化为对象
    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
        rabbitListenerEndpointRegistrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
    }

    @Bean
    MessageHandlerMethodFactory messageHandlerMethodFactory(){
        DefaultMessageHandlerMethodFactory messageHandlerMethodFactory = new DefaultMessageHandlerMethodFactory();
        messageHandlerMethodFactory.setMessageConverter(mappingJackson2MessageConverter());
        return messageHandlerMethodFactory;
    }

    @Bean
    public MappingJackson2MessageConverter mappingJackson2MessageConverter(){
        return  new MappingJackson2MessageConverter();
    }

    // 提供自定义RabbitTemplate,将对象序列化为json串
    @Bean
    public RabbitTemplate jacksonRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
}
