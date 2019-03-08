package com.alibaba.datax.plugin.writer.mysqlloadwriter;



import com.alibaba.datax.common.element.Column;
import com.alibaba.datax.common.element.Record;
import com.alibaba.datax.common.plugin.RecordReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author dalizu on 2018/9/20.
 * @version v1.0
 * @desc 自定义转换类  需要和需求的格式保持一致 注意:数据的格式 每列分隔符  每行是\n进行分隔
 */
public class ConvertInputStream extends InputStream{

    private static final Logger log = LoggerFactory.getLogger(MysqlLoadWriter.Task.class);

    private char columnSplit='\001';//列分隔

    private String lineSpliter="\r\n";//行分隔

    RecordReceiver lineReceiver;

    private Record record;

    private byte buffer[] = null;

    private int preLen=0;//上次剩下的长度

    private int preOff=0;//上次读取到的位置

    public ConvertInputStream(RecordReceiver lineReceiver) {
        this.lineReceiver = lineReceiver;
    }

    //重写此方法,从lineReceiver 读取数据到b[] 数组中
    public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        //获取数据写入
        int c = getLine(b,off,len);
        if (c == -1) {
            return -1;
        }
        return c;
    }


    //b   开始位置  写入长度
    public int getLine(byte b[], int off, int len) throws UnsupportedEncodingException {
        int ret=-1;
        //查看上次是否已经读取完毕
        if(this.preLen>0){
            //说明有剩余,继续读取
            int minLen = Math.min(this.preLen, len);//比较剩余和当前设定的数组大小
            System.arraycopy(this.buffer, this.preOff, b, off, minLen);

            this.preOff+=minLen;
            this.preLen=this.preLen-minLen;//是否还有剩余

            //返回长度
            ret=minLen;
            return ret;
        }

        record = this.lineReceiver.getFromReader();

        if(record==null){
            log.info("从channel读取数据结束");
            ret=-1;
            return ret;
        }

        //构造数据
        String lines=buildDataLine(record).toString();
        //log.info("line=================>"+lines);
        this.buffer = lines.toString().getBytes("UTF8");//获取到长度
        int bufLen=b.length;// 需要读入的大小
        int curLineLen=this.buffer.length;//当前数据的大小

        //判断是否超过给定的b[]长度
        if(bufLen<curLineLen){
            System.arraycopy(this.buffer, 0, b, off, bufLen);
            this.preLen=curLineLen-bufLen;//剩余多少没读取
            this.preOff=bufLen;//这条数据读取到的位置
            ret=bufLen;
            return ret;
        }

        System.arraycopy(this.buffer, 0, b, off, curLineLen);
        ret=curLineLen;
        return ret;
    }


    @Override
    public int read() throws IOException {
        return 0;
    }

    //解析数据转换为需要的字符串
    private StringBuilder buildDataLine(Record record) {

        StringBuilder stringBuilder=new StringBuilder();

        for (int i = 0; i < record.getColumnNumber(); i++) {
            Column column=record.getColumn(i);
            stringBuilder.append(column.getRawData());
            if(i<(record.getColumnNumber()-1)){
                stringBuilder.append(columnSplit);
            }
        }
        stringBuilder.append(lineSpliter);
        return stringBuilder;
    }

}
