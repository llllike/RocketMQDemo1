##  如果内容对你有帮助的话，点一个免费的star吧，非常感谢!
# 1 简单消息发送

## 1.1 配置文件

```yml
server:
  port: 9000

rocketmq:
  name-server: 192.168.255.132:9876
  producer:
    group: RocketMQDemo1-producer1
```

## 1.2 配置类

```java
@Configuration
public class BaseConfig {
    @Bean
    public SimpleDateFormat getSimpleDateFormat(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }
}
```

## 1.3 发送消息

```java
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
}
```

## 1.4 监听消息

```java
@Component
@Slf4j
@RocketMQMessageListener(topic = "first-topic",consumerGroup = "RocketMQDemo1-consumer1")
public class Consumer1 implements RocketMQListener<String> {
    @Override
    public void onMessage(String s) {
        log.info(s);
    }
}
```

## 1.5 测试消息发送

![](C:\Users\Lenovo\Desktop\Snipaste_2022-12-22_22-54-24.png)

# 2 其他类型消息发送

## 2.1 发送同步消息

```java
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
```

## 2.2 发送异步消息

```java
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
```

## 2.3 发送单向消息

```java
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
```

## 2.4 发送延迟消息

```java
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
```

## 2.5 发送事务消息

```java
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
```

### 监听事务

```java
@Slf4j
@RocketMQTransactionListener
@RequiredArgsConstructor
public class TransactionListener implements RocketMQLocalTransactionListener {
    private static final Map<String, RocketMQLocalTransactionState> TRANSACTION_STATE_MAP = new HashMap<>();
    /**
     * 处理本地事务
     * @param message
     * @param o
     * @return
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        log.info("执行本地事务");
        MessageHeaders headers = message.getHeaders();
        //获取事务ID
        String transactionId = (String) headers.get(RocketMQHeaders.TRANSACTION_ID);
        TRANSACTION_STATE_MAP.put(transactionId, RocketMQLocalTransactionState.UNKNOWN);
        log.info("transactionId is {}",transactionId);

        RocketMQLocalTransactionState state = RocketMQLocalTransactionState.ROLLBACK;
        if (Integer.parseInt(transactionId) % 2 == 0) {
            //执行成功，可以提交事务
            state = RocketMQLocalTransactionState.COMMIT;
        }
        log.info("transactionId is {}, state {}",transactionId, state.toString());
        TRANSACTION_STATE_MAP.remove(transactionId);
        return state;
    }
 
    /**
     * 校验事务状态
     * @param message
     * @return
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        MessageHeaders headers = message.getHeaders();
        //获取事务ID
        String transactionId = (String) headers.get(RocketMQHeaders.TRANSACTION_ID);
        log.info("检查本地事务,事务ID:{}",transactionId);
        RocketMQLocalTransactionState state = TRANSACTION_STATE_MAP.get(transactionId);
        if(null != state){
            return state;
        }
        return RocketMQLocalTransactionState.ROLLBACK;
 
    }
}
```
