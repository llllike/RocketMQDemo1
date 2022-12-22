package com.rocket.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author yzy
 * @create 2022-12-22-22:35
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = "first-topic",consumerGroup = "RocketMQDemo1-consumer1")
public class Consumer1 implements RocketMQListener<String> {
    @Override
    public void onMessage(String s) {
        log.info(s);
    }
}
