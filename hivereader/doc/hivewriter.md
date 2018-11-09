# DataX HiveReader


---

## 1 快速介绍

通过hive sql 导出数据到目标库

## 2 实现原理

如果想要从hive表中把数据按照一列一列把数据取出来，可以使用hdfsreader.
在某些时候，我们想要使用更灵活的方式，比如使用hive sql 查询语句导出.
实现方式是:
根据配置的sql,通过将查询结果保存到一张新的临时hive表中这种方式.
然后获取新表的hdfs文件地址，然后读取文件到缓冲区，最后删除临时的表.

````
create table t_tmp LOCATION '/test'
as
select * from dept;

show create table t_tmp;

CREATE TABLE `t_tmp`(
  `deptno` int, 
  `dname` string, 
  `location` string)
ROW FORMAT SERDE 
  'org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe' 
STORED AS INPUTFORMAT 
  'org.apache.hadoop.mapred.TextInputFormat' 
OUTPUTFORMAT 
  'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
LOCATION
  'hdfs://hadoop001:8020/test'
TBLPROPERTIES (
  'COLUMN_STATS_ACCURATE'='true', 
  'numFiles'='0', 
  'numRows'='4', 
  'rawDataSize'='76', 
  'totalSize'='0', 
  'transient_lastDdlTime'='1541800695')

````

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
          "name": "hivewriter",
          "parameter": {
            "hivesql": "select * from t_tmp",
            "defaultFS": "hdfs://xxx:port"
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

