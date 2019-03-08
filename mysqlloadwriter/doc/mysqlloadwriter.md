# DataX MysqlLoadWriter

## 1 快速介绍

数据导入mysql的插件

## 2 实现原理

使用Mysql LOAD DATA导入纯数据,跳过了SQL的校验和优化,导入的速度会大大提升了。

## 3 功能说明

### 3.1 配置样例

#### job.json

```json
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
        },
        "writer": {
          "name": "mysqlloadwriter",
          "parameter": {
                          "writeMode": "REPLAC",
                          "username": "root",
                          "password": "root",
                          "column": [
                              "id",
                              "name"
                          ],
                          "preSql": [
                              "delete from test"
                          ],
                          "connection": [
                              {
                                  "jdbcUrl": "jdbc:mysql://127.0.0.1:3306/datax?useUnicode=true&characterEncoding=gbk",
                                  "table": [
                                      "test"
                                  ]
                              }
                          ]
                         }
        }
        }
    ]
  }

}
```

#### 3.2 参数说明

* writeMode
 * 描述：REPLACE和IGNORE关键词控制对现有的唯一键记录的重复的处理。如果你指定REPLACE，新行将代替有相同的唯一键值的现有行。
        如果你指定IGNORE，跳过有唯一键的现有行的重复行的输入。如果你不指定任何一个选项，当找到重复键键时，出现一个错误，并且文本文件的余下部分被忽略时。
 * 必选：否
 * 默认值：IGNORE
 
 
 * table
  * 描述：表名称,只能写一个,必须填写
  * 必选：是
  * 默认值：无
 
## 4 性能报告

### 4.1 环境准备


#### 4.1.3 DataX jvm 参数

-Xms1024m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError

### 4.2 测试报告



### 4.3 测试总结


## 5 约束限制

