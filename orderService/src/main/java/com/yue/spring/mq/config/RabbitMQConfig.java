package com.yue.spring.mq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yue.spring.handler.utils.RequestContextUtils;
import com.yue.spring.pojo.NonWebRequestAttributes;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ配置类
 */
@Configuration
public class RabbitMQConfig implements RabbitListenerConfigurer {

    public static final String EXCHANGE_NAME = "boot_topic_exchange";

    public static final String DEAD_LETTER_EXCHANGE_NAME = "dlx_exchange";

    public static final String QUEUE_NAME = "boot_queue";

    public static final String QUEUE_TTL_NAME = "ttl_queue";

    public static final String DEAD_LETTER_QUEUE_NAME = "dlx_queue";

    public static final String DLX_NAME="dlx.order";


    // 设置TTL队列
    @Bean
    public Queue queue() {
        Map<String, Object> props = new HashMap<>();
        props.put("x-message-ttl", 1000*60*10);
        props.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE_NAME);
        props.put("x-dead-letter-routing-key", DLX_NAME);
        return QueueBuilder.durable(QUEUE_TTL_NAME).withArguments(props).build();
    }

    //交换机
    @Bean
    public Exchange bootExchange(){
        return ExchangeBuilder.topicExchange(EXCHANGE_NAME).durable(true).build();
    }

    //死信交换机
    @Bean
    public Exchange deadLetterExchange(){
        return ExchangeBuilder.topicExchange(DEAD_LETTER_EXCHANGE_NAME).durable(true).build();
    }

    //队列
    @Bean
    public Queue bootQueue(){
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    //死信交换机队列
    @Bean
    public Queue deadLetterQueue(){
        return QueueBuilder.durable(DEAD_LETTER_QUEUE_NAME).build();
    }

    //队列绑定关系
    @Bean
    public Binding bindingQueueExchange(@Qualifier("bootQueue") Queue queue, @Qualifier("bootExchange") Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("boot.#").noargs();
    }

    //绑定TTL队列到交换机
    @Bean
    public Binding bingQueueTTLExchange(@Qualifier("queue") Queue queue, @Qualifier("bootExchange") Exchange exchange) {

        return BindingBuilder.bind(queue).to(exchange).with("ttl.#").noargs();
    }


    //绑定死信队列到死信交换机
    @Bean
    public Binding bingDLQToDLX(@Qualifier("deadLetterQueue") Queue queue, @Qualifier("deadLetterExchange") Exchange exchange) {

        return BindingBuilder.bind(queue).to(exchange).with("dlx.#").noargs();
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

    public final static MessagePostProcessor messagePostProcessor = message -> {
        message.getMessageProperties().setContentType("application/json");
        message.getMessageProperties().setContentEncoding("UTF-8");
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if(requestAttributes instanceof ServletRequestAttributes){
            HttpServletRequest request = RequestContextUtils.getRequest();
            Enumeration<String> enumeration = request.getHeaderNames();
            if (enumeration != null) {
                while (enumeration.hasMoreElements()) {
                    String key = enumeration.nextElement();
                    String value = request.getHeader(key);
                    message.getMessageProperties().setHeader(key,value);
                }
            }
        }
        else if(requestAttributes instanceof NonWebRequestAttributes){
            NonWebRequestAttributes attributes = RequestContextUtils.getAttributes();
            message.getMessageProperties().setHeader("Authorization",(String)attributes.getAttribute("Authorization",0));
            message.getMessageProperties().setHeader("jwt-token",(String)attributes.getAttribute("jwt-token",0));
        }
        return message;
    };

}
