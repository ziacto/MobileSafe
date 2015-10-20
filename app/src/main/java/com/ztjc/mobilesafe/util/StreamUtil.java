package com.ztjc.mobilesafe.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 流工具类
 */
public class StreamUtil {

    /**
     * 把流解析成字符串
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String decodeToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(b)) != -1) {
            outputStream.write(b, 0, len);
        }
        return new String(outputStream.toByteArray());
    }
}
