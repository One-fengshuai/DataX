package com.alibaba.datax.plugin.writer.kafkawriter;

/**
 * @author dalizu on 2018/11/8.
 * @version v1.0
 * @desc
 */
public class Key {

    // must have
    public static final String TOPIC = "topic";

    public static final String BOOTSTRAP_SERVERS="bootstrapServers";

    // not must , not default
    public static final String FIELD_DELIMITER = "fieldDelimiter";

}
