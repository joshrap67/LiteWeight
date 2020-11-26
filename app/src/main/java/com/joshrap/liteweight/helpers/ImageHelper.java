package com.joshrap.liteweight.helpers;

import com.joshrap.liteweight.imports.ApiConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageHelper {

    public static byte[] getImageByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static String getIconUrl(String fileName) {
        return ApiConfig.s3ImageUrl + fileName;
    }
}
