## 使用docker启动
### host 配置
修改/etc/hosts 文件,将 kafka 映射到本地的ip
```text
30.169.24.113 kafka
```

### 配置文件

参考 : https://bugstack.cn/md/road-map/kafka.html

```yaml

version: '3.0'
# docker-compose -f docker-compose.yml up -d
services:
  zookeeper:
    image: zookeeper:3.9.0
    container_name: zookeeper
    restart: always
    ports:
      - "2181:2181"
    environment:
      ZOO_MY_ID: 1
      ZOO_SERVERS: server.1=zookeeper:2888:3888;2181
      ZOOKEEPER_CLIENT_PORT: 2181
      ALLOW_ANONYMOUS_LOGIN: yes
      TZ: Asia/Shanghai
    networks:
      - my-network

  kafka:
    image: bitnami/kafka:3.7.0
    container_name: kafka
    volumes:
      - /etc/localtime:/etc/localtime
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_CFG_LISTENERS: PLAINTEXT://:9092
      KAFKA_CFG_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_CFG_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      ALLOW_PLAINTEXT_LISTENER: yes
      KAFKA_MESSAGE_MAX_BYTES: "2000000"
      KAFKA_ENABLE_KRAFT: no
      JMX_PORT: 9999
      TZ: Asia/Shanghai
    depends_on:
      - zookeeper
    networks:
      - my-network

  kafka-eagle:
    image: echo21bash/kafka-eagle:3.0.2
    container_name: kafka-eagle
    environment:
      KAFKA_EAGLE_ZK_LIST: zookeeper:2181
    volumes:
      - ./kafka-eagle/system-config.properties:/opt/kafka-eagle/conf/system-config.properties
    ports:
      - "8048:8048"
    depends_on:
      - kafka
    networks:
      - my-network

networks:
  my-network:
    driver: bridge

```

```properties

######################################
# multi zookeeper & kafka cluster list
# Settings prefixed with 'kafka.eagle.' will be deprecated, use 'efak.' instead
######################################
efak.zk.cluster.alias=cluster1
cluster1.zk.list=zookeeper:2181
# cluster1.zk.list=tdn1:2181,tdn2:2181,tdn3:2181
# cluster2.zk.list=xdn10:2181,xdn11:2181,xdn12:2181

######################################
# zookeeper enable acl
######################################
cluster1.zk.acl.enable=false
cluster1.zk.acl.schema=digest
cluster1.zk.acl.username=test
cluster1.zk.acl.password=test123

######################################
# broker size online list
######################################
cluster1.efak.broker.size=20

######################################
# zk client thread limit
######################################
kafka.zk.limit.size=16

######################################
# EFAK webui port
######################################
efak.webui.port=8048

######################################
# EFAK enable distributed
######################################
efak.distributed.enable=false
efak.cluster.mode.status=master
efak.worknode.master.host=localhost
efak.worknode.port=8085

######################################
# kafka jmx acl and ssl authenticate
######################################
cluster1.efak.jmx.acl=false
cluster1.efak.jmx.user=keadmin
cluster1.efak.jmx.password=keadmin123
cluster1.efak.jmx.ssl=false
cluster1.efak.jmx.truststore.location=/data/ssl/certificates/kafka.truststore
cluster1.efak.jmx.truststore.password=ke123456

######################################
# kafka offset storage
######################################
cluster1.efak.offset.storage=kafka
cluster2.efak.offset.storage=zk

######################################
# kafka jmx uri
######################################
cluster1.efak.jmx.uri=service:jmx:rmi:///jndi/rmi://%s/jmxrmi

######################################
# kafka metrics, 15 days by default
######################################
efak.metrics.charts=true
efak.metrics.retain=15

######################################
# kafka sql topic records max
######################################
efak.sql.topic.records.max=5000
efak.sql.topic.preview.records.max=10

######################################
# delete kafka topic token
######################################
efak.topic.token=keadmin

######################################
# kafka sasl authenticate
######################################
cluster1.efak.sasl.enable=false
cluster1.efak.sasl.protocol=SASL_PLAINTEXT
cluster1.efak.sasl.mechanism=SCRAM-SHA-256
cluster1.efak.sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="kafka" password="kafka-eagle";
cluster1.efak.sasl.client.id=
cluster1.efak.blacklist.topics=
cluster1.efak.sasl.cgroup.enable=false
cluster1.efak.sasl.cgroup.topics=
cluster2.efak.sasl.enable=false
cluster2.efak.sasl.protocol=SASL_PLAINTEXT
cluster2.efak.sasl.mechanism=PLAIN
cluster2.efak.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="kafka" password="kafka-eagle";
cluster2.efak.sasl.client.id=
cluster2.efak.blacklist.topics=
cluster2.efak.sasl.cgroup.enable=false
cluster2.efak.sasl.cgroup.topics=

######################################
# kafka ssl authenticate
######################################
cluster3.efak.ssl.enable=false
cluster3.efak.ssl.protocol=SSL
cluster3.efak.ssl.truststore.location=
cluster3.efak.ssl.truststore.password=
cluster3.efak.ssl.keystore.location=
cluster3.efak.ssl.keystore.password=
cluster3.efak.ssl.key.password=
cluster3.efak.ssl.endpoint.identification.algorithm=https
cluster3.efak.blacklist.topics=
cluster3.efak.ssl.cgroup.enable=false
cluster3.efak.ssl.cgroup.topics=

######################################
# kafka sqlite jdbc driver address
######################################
#efak.driver=com.mysql.cj.jdbc.Driver
#efak.url=jdbc:mysql://127.0.0.1:13306/ke?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull
#efak.username=root
#efak.password=123456

######################################
# kafka mysql jdbc driver address
######################################
efak.driver=org.sqlite.JDBC
efak.url=jdbc:sqlite:/opt/kafka-eagle/db/ke.db
efak.username=root
efak.password=root

```

启动成功后,可在 localhost:8048 监控kafka的信息. (kafka-eagles是一个kafka的管理平台),登录信息是admin:123456

### 核心代码

#### pom依赖
本次使用的springboot是3.3.12版本的.
```xml
<project>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.12</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
    
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
    </dependencies>
</project>
```

#### 配置文件
生产者和消费者的核心配置一致.
```yaml
spring:
  application:
    name: kafka_demo_producer
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: p_group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

```

#### 生产者代码
```java


@Service
public class KafkaProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
        System.out.println("Sent: " + message);
    }
}
```


#### 消费者代码
```java

@Service
public class KafkaConsumer {
    @KafkaListener(topics = "test-topic", groupId = "test-group")
    public void listen(String message) {
        System.out.println("Received: " + message);
    }
}

```

#### 测试用例

```java

    @Autowired
    KafkaProducer kafkaProducer;

    @Test
    public void testSendMessage() {
        for (int i = 0; i < 20; i++) {
            kafkaProducer.sendMessage("test-topic", "hello kafka-eagle " + i);
        }
    }
```

控制台打印  Received: hello kafka-eagle i 就表示消费者成功接收到了消息.

#### 问题
启动两个消费者实例. c1的端口为8082, c2的端口是8083. 由于c1 和c2 是在同一个消费者组, 所以按照定义用一消费者组内,只有一个消费者可以收到消息.

实测下来:
当生产者发送消息后,c1 的日志输出了以下内容:
```
2025-06-23T15:28:35.065+08:00  INFO 3566 --- [consumer] [ntainer#0-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : test-group: partitions assigned: [test-topic-0]
Received: hello kafka-eagle 0
Received: hello kafka-eagle 1
Received: hello kafka-eagle 2
Received: hello kafka-eagle 3
Received: hello kafka-eagle 4
...
```

观察日志可以得到,只有c1 会收到消息,所以问题就来了

#### Q: 如果一个消费者组存在多个消费者时,kafka会如何分发消息? 这个策略能否修改呢? 
由于目前的 `test-topic` 的分区数为1, c1 和 c2 两个消费者只会有一个消费者可以获取到消息. 

这个策略是可以修改的.假设我们期望c1 和 c2 轮流的收到消息. 可以这样来实现

首先需要再eagle中将`test-topic` 的分区数设置为2 (admin-token在之前的system-config.properties中). 然后重启c1 ,c2 .这时应该能在c1 和c2的控制台看到一下的日志


```shell
# c1 中的日志
2025-06-23T16:15:02.725+08:00  INFO 5494 --- [consumer] [ntainer#0-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : test-group: partitions assigned: [test-topic-0]


# c2 中的日志
2025-06-23T16:15:02.731+08:00  INFO 5508 --- [consumer] [ntainer#0-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : test-group: partitions assigned: [test-topic-1]

```

然后再生产者发送时指定partition
```java
 public void sendMessagePartition(String topic, Integer partition, String message) {
        kafkaTemplate.send(topic, partition, null, message);
        System.out.println("Sent: " + message);
    }
```

后续启动测试用例,就能看到c1 和c2 在轮流接受消息了.
