package com.alibaba.datax.plugin.writer.kafkawriter;

import com.alibaba.datax.common.element.Column;
import com.alibaba.datax.common.element.Record;
import com.alibaba.datax.common.plugin.RecordReceiver;
import com.alibaba.datax.common.spi.Writer;
import com.alibaba.datax.common.util.Configuration;
import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author dalizu on 2018/11/7.
 * @version v1.0
 * @desc
 */
public class KafkaWriter extends Writer {


    public static class Job extends Writer.Job {

        private static final Logger log = LoggerFactory.getLogger(Job.class);

        private Configuration conf = null;

        @Override
        public void init() {
            this.conf = super.getPluginJobConf();//获取配置文件信息{parameter 里面的参数}
            log.info("kafka writer params:{}", conf.toJSON());
            //校验 参数配置
            this.validateParameter();
        }


        private void validateParameter() {
            //toipc 必须填
            this.conf
                    .getNecessaryValue(
                            Key.TOPIC,
                            KafkaWriterErrorCode.REQUIRED_VALUE);


            this.conf
                    .getNecessaryValue(
                            Key.BOOTSTRAP_SERVERS,
                            KafkaWriterErrorCode.REQUIRED_VALUE);

        }

        @Override
        public void prepare() {

        }

        @Override
        public List<Configuration> split(int mandatoryNumber) {
            //按照reader 配置文件的格式  来 组织相同个数的writer配置文件
            List<Configuration> configurations = new ArrayList<Configuration>(mandatoryNumber);
            for (int i = 0; i < mandatoryNumber; i++) {
                Configuration splitedTaskConfig = this.conf.clone();
                configurations.add(splitedTaskConfig);
            }
            return configurations;
        }

        @Override
        public void post() {

        }

        @Override
        public void destroy() {

        }

    }


    public static class Task extends Writer.Task {
        private static final Logger log = LoggerFactory.getLogger(Task.class);

        private static final String NEWLINE_FLAG = System.getProperty("line.separator", "\n");

        private Producer<String, String> producer;

        private String fieldDelimiter;

        private Configuration conf;

        @Override
        public void init() {
            this.conf = super.getPluginJobConf();
            fieldDelimiter = conf.getUnnecessaryValue(Key.FIELD_DELIMITER, "\t", null);
            //初始化kafka
            Properties props = new Properties();
            props.put("bootstrap.servers", conf.getString(Key.BOOTSTRAP_SERVERS));
            props.put("acks", "all");//这意味着leader需要等待所有备份都成功写入日志，这种策略会保证只要有一个备份存活就不会丢失数据。这是最强的保证。
            props.put("retries", 0);
            // Controls how much bytes sender would wait to batch up before publishing to Kafka.
            //控制发送者在发布到kafka之前等待批处理的字节数。
            //控制发送者在发布到kafka之前等待批处理的字节数。 满足batch.size和ling.ms之一，producer便开始发送消息
            //默认16384   16kb
            props.put("batch.size", 16384);
            props.put("linger.ms", 1);
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            producer = new KafkaProducer(props);

        }

        /**
         *
         * 开始写入kafka,不指定key,使用默认轮训的分区策略
         *
         * */
        @Override
        public void startWrite(RecordReceiver lineReceiver) {

            log.info("start to writer kafka");
            Record record = null;
            while ((record = lineReceiver.getFromReader()) != null) {//说明还在读取数据,或者读取的数据没处理完
                //获取一行数据，按照指定分隔符 拼成字符串 发送出去
                long startTime = System.currentTimeMillis();
                producer.send(new ProducerRecord<String, String>(this.conf.getString(Key.TOPIC),
                        recordToString(record)),new WriterCallback(startTime));

            }
        }

        @Override
        public void destroy() {
            if (producer != null) {
                producer.close();
            }
        }


        private String recordToString(Record record) {
            int recordLength = record.getColumnNumber();
            if (0 == recordLength) {
                return NEWLINE_FLAG;
            }

            Column column;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < recordLength; i++) {
                column = record.getColumn(i);
                sb.append(column.asString()).append(fieldDelimiter);
            }
            sb.setLength(sb.length() - 1);
            sb.append(NEWLINE_FLAG);

            return sb.toString();
        }


        class WriterCallback implements Callback {

            private final Logger logger = LoggerFactory.getLogger(WriterCallback.class);

            private long startTime;

            public WriterCallback(long startTime) {
                this.startTime = startTime;
            }

            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (exception != null) {
                    logger.error("Error sending message to Kafka {} ", exception.getMessage());
                }

                if (logger.isDebugEnabled()) {
                    long eventElapsedTime = System.currentTimeMillis() - startTime;
                    logger.debug("Acked message partition:{} ofset:{}",  metadata.partition(), metadata.offset());
                    logger.debug("Elapsed time for send: {}", eventElapsedTime);
                }
            }
        }

    }

}
