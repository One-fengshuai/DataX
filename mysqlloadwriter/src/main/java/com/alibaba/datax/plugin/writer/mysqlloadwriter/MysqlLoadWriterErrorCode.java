package com.alibaba.datax.plugin.writer.mysqlloadwriter;

import com.alibaba.datax.common.spi.ErrorCode;

public enum MysqlLoadWriterErrorCode implements ErrorCode {

    REQUIRED_VALUE("MysqlLoadWriter-00", "您缺失了必须填写的参数值."),
    LOAD_MYSQL_ERROR("MysqlLoadWriter-01", "load数据到mysql异常.")
    ;

    private final String code;
    private final String description;

    private MysqlLoadWriterErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return String.format("Code:[%s], Description:[%s].", this.code,
                this.description);
    }

}
