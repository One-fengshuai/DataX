# DataX KafkaWriter


---

## 1 快速介绍

数据导入kafka的插件

## 2 实现原理

使用kafka的kafka-clients  maven依赖 api接口， 批量把从reader读入的数据写入kafka

## 3 功能说明

### 3.1 配置样例

#### job.json

```
{
  "job": {
    "setting": {
      "speed": {
        "channel":1
      }
    },
    "content": [
      {
        "reader": {
          ...
        },
        "writer": {
          "name": "kafkawriter",
          "parameter": {
            "topic": "test_topic",
            "bootstrapServers": "192.168.88.129:9092",
            "fieldDelimiter":""
          }
        }
        }
    ]
  }

}
```

#### 3.2 参数说明

* topic
 * 描述：kafka的主题
 * 必选：是
 * 默认值：无
 
* bootstrapServers 
  * 描述：kafka broker地址
  * 必选：是
  * 默认值

* fieldDelimiter
 * 描述：如果插入数据是array，就使用指定分隔符
 * 必选：否
 * 默认值：-,-


## 4 性能报告

### 4.1 环境准备

* 总数据量 1kw条数据, 每条0.1kb
* 1个shard, 0个replica
* 不加id，这样默认是append_only模式，不检查版本，插入速度会有20%左右的提升

#### 4.1.1 输入数据类型(streamreader)



#### 4.1.2 输出数据类型(kafkawriter)



#### 4.1.2 机器参数



#### 4.1.3 DataX jvm 参数

-Xms1024m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError

### 4.2 测试报告



### 4.3 测试总结


## 5 约束限制

