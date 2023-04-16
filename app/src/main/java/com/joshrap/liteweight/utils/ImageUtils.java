package com.joshrap.liteweight.utils;

import com.joshrap.liteweight.imports.BackendConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    public static byte[] getImageByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static String getIconUrl(String fileName) {
        return BackendConfig.s3ImageUrl + fileName; // todo should this be taken care of by the backend?
    }
}
