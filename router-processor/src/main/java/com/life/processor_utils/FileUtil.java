package com.life.processor_utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtil {
    private static class FileUtilHolder {
        private final static FileUtil urlUtil = new FileUtil();
    }

    public static FileUtil getInstance() {
        return FileUtilHolder.urlUtil;
    }

    public FileUtil() {
    }

    //读取文本转成字符串
    public String readFileAsString(String filePath) {
        InputStream inputStream = this.getClass().getResourceAsStream(filePath);
//            return new StringBuilder().append(Arrays.toString(Files.readAllBytes(Paths.get(filePath))));

        StringBuilder content = new StringBuilder();
        if (ObjectUtils.isNotEmpty(inputStream)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            } catch (IOException e) {
                RouterLog.e("Failed to load template file.");
                e.printStackTrace();
            }

            return content.toString();
        }

        return null;
    }
}
