# DataX HiveReader


---

## 1 快速介绍

通过hive sql 导出数据

## 2 实现原理



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

* xxxx
 * 描述：xxxx
 * 必选：是
 * 默认值：无
 


## 4 性能报告

### 4.1 环境准备


#### 4.1.3 DataX jvm 参数

-Xms1024m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError

### 4.2 测试报告



### 4.3 测试总结


## 5 约束限制

