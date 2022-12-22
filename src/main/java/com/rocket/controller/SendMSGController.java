package com.rocket.controller;

import com.alibaba.fastjson.JSON;
import com.rocket.pojo.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author yzy
 * @create 2022-12-22-22:16
 */
@Slf4j
@RestController
public class SendMSGController {
    @Resource
    private SimpleDateFormat simpleDateFormat;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @PostMapping("/send")
    public String send(Message message){
        message.setTime(simpleDateFormat.format(new Date()));
        String s = JSON.toJSONString(message);
        rocketMQTemplate.convertAndSend("first-topic",s);
        return s;
    }
    @PostMapping("/sendSync")
    public String send1(Message message){
        message.setTime(simpleDateFormat.format(new Date()));
        String s = JSON.toJSONString(message);
        SendResult sendResult = rocketMQTemplate
                .syncSend("first-topic", MessageBuilder.withPayload(s)
                .setHeader(RocketMQHeaders.KEYS, message.getId())
                .build());
        return JSON.toJSONString(sendResult);
    }
    @PostMapping("/sendAsync")
    public String send2(Message message){
        message.setTime(simpleDateFormat.format(new Date()));
        String s = JSON.toJSONString(message);
        rocketMQTemplate.asyncSend("first-topic", MessageBuilder.withPayload(s)
                .setHeader(RocketMQHeaders.KEYS, message.getId())
                .build(), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info(JSON.toJSONString(sendResult));
            }

            @Override
            public void onException(Throwable throwable) {
                log.warn(JSON.toJSONString(throwable));
            }
        });
        return s;
    }
    // 发送单向消息
    @PostMapping("/sendOneWay")
    public String send3(Message message){
        message.setTime(simpleDateFormat.format(new Date()));
        String s = JSON.toJSONString(message);
        rocketMQTemplate.sendOneWay("first-topic",MessageBuilder.withPayload(s)
                .setHeader(RocketMQHeaders.KEYS, message.getId())
                .build());
        return s;
    }
    // 发送延迟消息
    @PostMapping("/sendDelay/{time}")
    public String send4(Message message, @PathVariable("time")Integer time){
        message.setTime(simpleDateFormat.format(new Date()));
        String s = JSON.toJSONString(message);
        //1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h;
        rocketMQTemplate
                .syncSend("first-topic", MessageBuilder.withPayload(s)
                .setHeader(RocketMQHeaders.KEYS, message.getId())
                .build(), time, 1);
        return s;
    }
    // 发送事务消息
    @PostMapping("/sendTransaction")
    public String send5(Message message){
        message.setTime(simpleDateFormat.format(new Date()));
        String s = JSON.toJSONString(message);
        TransactionSendResult transactionSendResult = rocketMQTemplate
                .sendMessageInTransaction("first-topic", MessageBuilder.withPayload(s)
                        .setHeader(RocketMQHeaders.KEYS, message.getId())
                        .build(), null);
        return JSON.toJSONString(transactionSendResult);
    }

}
