package com.rachel.seckill.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MQConfig {
    public static final String QUEUE = "queue";
    public static final String SECKILL_QUEUE = "seckill.queue";
    public static final String TOPIC_QUEUE1 = "topic.queue1";
    public static final String TOPIC_QUEUE2 = "topic.queue2";
    public static final String HEADER_QUEUE = "header.queue";
    public static final String TOPIC_EXCHANGE = "topic.exchange";
    public static final String FANOUT_EXCHANGE = "fanout.exchange";
    public static final String HEADER_EXCHANGE = "header.exchange";

    /**
     * for seckill
     * Direct模式 EXCHANGE交换机
     * @return
     */
    @Bean
    public Queue seckillQueue() {
        return new Queue(SECKILL_QUEUE, true);
    }

    /**
     * Direct模式 EXCHANGE交换机
     * @return
     */
    @Bean
    public Queue queue() {
    return new Queue(QUEUE, true);
    }

    /**
     * Topic模式 EXCHANGE交换机
     * @return
     */
    @Bean
    public Queue topicQueue1() {
        return new Queue(TOPIC_QUEUE1, true);
    }

    @Bean
    public Queue topicQueue2() {
        return new Queue(TOPIC_QUEUE2, true);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    @Bean
    public Binding bindingTopicQueue1() {
        return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with("topic.key1");
    }
    @Bean
    public Binding bindingTopicQueue2() {
        return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with("topic.#");
    }

    /**
     * Fanout模式 EXCHANGE交换机
     * @return
     */
    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(FANOUT_EXCHANGE);
    }

    @Bean
    public Binding bindingFanoutQueue1() {
        return BindingBuilder.bind(topicQueue1()).to(fanoutExchange());
    }
    @Bean
    public Binding bindingFanoutQueue2() {
        return BindingBuilder.bind(topicQueue2()).to(fanoutExchange());
    }


    /**
     * Headers模式 EXCHANGE交换机
     * @return
     */
    @Bean
    public HeadersExchange headersExchange() {
        return new HeadersExchange(HEADER_EXCHANGE);
    }
    @Bean
    public Queue headerQueue() {
        return new Queue(HEADER_QUEUE, true);
    }
    @Bean
    public Binding bindingHeadersQueue() {
        Map<String, Object> map = new HashMap<>();
        map.put("header1", "value1");
        map.put("header2", "value2");
        return BindingBuilder.bind(headerQueue()).to(headersExchange()).whereAll(map).match();
    }

}
