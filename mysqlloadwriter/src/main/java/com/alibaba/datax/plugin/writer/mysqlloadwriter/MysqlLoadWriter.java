package com.alibaba.datax.plugin.writer.mysqlloadwriter;


import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.RecordReceiver;
import com.alibaba.datax.common.spi.Writer;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.plugin.rdbms.util.DBUtil;
import com.alibaba.datax.plugin.rdbms.util.DataBaseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dalizu on 2019/3/6.
 * @version v1.0
 * @desc
 */
public class MysqlLoadWriter extends Writer {

    public static class Job extends Writer.Job {

        private static final Logger log = LoggerFactory.getLogger(Job.class);

        private Configuration conf = null;

        @Override
        public void init() {
            this.conf = super.getPluginJobConf();//获取配置文件信息{parameter 里面的参数}
            //校验参数
            this.validateParameter();

        }

        private void validateParameter() {
            this.conf
                    .getNecessaryValue(
                            Key.USERNAME,
                            MysqlLoadWriterErrorCode.REQUIRED_VALUE);


            this.conf
                    .getNecessaryValue(
                            Key.PASSWORD,
                            MysqlLoadWriterErrorCode.REQUIRED_VALUE);

            List<String> userConfiguredColumns = this.conf.getList(Key.COLUMN, String.class);
            if (userConfiguredColumns.size() <= 0) {
                throw DataXException.asDataXException(MysqlLoadWriterErrorCode.REQUIRED_VALUE, "必须配置列信息");
            }

            List<Object> conns = this.conf.getList(Key.CONN_MARK,
                    Object.class);

            if (conns.size() <= 0) {
                throw DataXException.asDataXException(MysqlLoadWriterErrorCode.REQUIRED_VALUE, "必须配置连接信息");
            }

            Configuration connConf = Configuration.from(conns.get(0)
                    .toString());

            // 这里的 jdbcUrl 已经 append 了合适后缀参数
            String jdbcUrl = connConf.getNecessaryValue(Key.JDBC_URL, MysqlLoadWriterErrorCode.REQUIRED_VALUE);

            List<String> tables = connConf.getList(Key.TABLE, String.class);
            if (tables.size() <= 0) {
                throw DataXException.asDataXException(MysqlLoadWriterErrorCode.REQUIRED_VALUE, "必须配置表信息");
            }

        }

        @Override
        public void prepare() {
            //执行preSql

        }

        @Override
        public List<Configuration> split(int mandatoryNumber) {

            //按照reader 配置文件的格式  来 组织相同个数的writer配置文件
            List<Configuration> configurations = new ArrayList<Configuration>(mandatoryNumber);
            for (int i = 0; i < mandatoryNumber; i++) {
                Configuration splitedTaskConfig = this.conf.clone();

                List<Object> conns = splitedTaskConfig.getList(Key.CONN_MARK,
                        Object.class);
                Configuration connConf = Configuration.from(conns.get(0)
                        .toString());

                // 这里的 jdbcUrl 已经 append 了合适后缀参数
                String jdbcUrl = connConf.getString(Key.JDBC_URL);
                splitedTaskConfig.set(Key.JDBC_URL, jdbcUrl);

                List<String> tables = connConf.getList(Key.TABLE, String.class);
                splitedTaskConfig.set(Key.TABLE, tables.get(0));


                configurations.add(splitedTaskConfig);
            }
            return configurations;
        }

        @Override
        public void post() {
            //全局的post方法,所有task执行完成后触发

        }


        @Override
        public void destroy() {

        }
    }


    public static class Task extends Writer.Task {

        private static final Logger log = LoggerFactory.getLogger(Task.class);

        private Configuration conf;

        private Connection conn;

        private String table;

        private List<String> columns;

        private String columnsStr="";

        private String writeMode;

        @Override
        public void init() {
            this.conf = super.getPluginJobConf();
            String username = this.conf.getString(Key.USERNAME);
            String password = this.conf.getString(Key.PASSWORD);
            String jdbcUrl = this.conf.getString(Key.JDBC_URL);

            //获取表信息和列信息
            this.table = this.conf.getString(Key.TABLE);
            this.writeMode = this.conf.getString(Key.WRITE_MODE);
            this.columns = this.conf.getList(Key.COLUMN, String.class);

            if (1 == this.columns.size() && "*".equals(this.columns.get(0))) {
                columnsStr = "";
            } else {
                columnsStr += " (";
                for (int i=0;i<columns.size();i++) {
                    columnsStr += columns.get(i);
                    if(i!=(columns.size()-1)){
                       columnsStr+=",";
                    }
                }
                columnsStr += ")";
            }

            //获取数据库连接
            conn = DBUtil.getConnection(DataBaseType.MySql,
                    jdbcUrl, username, password);
        }


        @Override
        public void startWrite(RecordReceiver lineReceiver) {
            //【注意:数据的格式 列分隔符行分隔符 在转换的时候指定】
            String loadSql = "load data local infile 'datax_tmp.txt' " + this.writeMode + " into table ";
            loadSql += this.table;
            loadSql += " character set 'utf8' ";
            loadSql += " fields terminated by '\001' ";
            loadSql += " escaped by '\\\\' ";
            loadSql += " lines terminated by '\\n'";
            loadSql += this.columnsStr;

            log.info("Load SQL========>:{}", loadSql);

            try {
                InputStream inputStream = new ConvertInputStream(lineReceiver);
                PreparedStatement statement = conn.prepareStatement(loadSql);
                if (statement.isWrapperFor(com.mysql.jdbc.Statement.class)) {
                    com.mysql.jdbc.PreparedStatement mysqlStatement = statement.unwrap(com.mysql.jdbc.PreparedStatement.class);
                    mysqlStatement.setLocalInfileInputStream(inputStream);
                    mysqlStatement.executeUpdate();
                } else {
                    log.error("未知错误,请检查!!!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw DataXException.asDataXException(
                        MysqlLoadWriterErrorCode.LOAD_MYSQL_ERROR,
                        "当前task写入数据失败!!!");
            }
        }

        @Override
        public void post() {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }

        @Override
        public void destroy() {

        }

    }

}
