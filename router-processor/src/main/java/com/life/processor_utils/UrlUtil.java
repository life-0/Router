package com.life.processor_utils;

import java.util.ArrayList;

public class UrlUtil {
    private static class UrlUtilHolder {
        private final static UrlUtil urlUtil = new UrlUtil();
    }

    public static UrlUtil getInstance() {
        return UrlUtilHolder.urlUtil;
    }

    public UrlUtil() {
    }

    /**
     * 切割路径
     *
     * @param url
     * @return ArrayList<String>
     */
    public static ArrayList<String> splitUrl(String url) {
        ArrayList<String> urlList = new ArrayList<>();
        int firstSlashIndex = url.indexOf('/'); // 第一个'/'的位置
        int secondSlashIndex = url.indexOf('/', firstSlashIndex + 1); // 第二个'/'的位置，从第一个'/'的下一个位置开始查找

        if (firstSlashIndex == 0 && secondSlashIndex > firstSlashIndex) {
            String item = url.substring(firstSlashIndex, secondSlashIndex); // 截取两个'/'之间的内容
            urlList.add(item);
            String remainUrl = url.substring(secondSlashIndex);
            urlList.addAll(splitUrl(remainUrl));
        } else if (secondSlashIndex == -1) {
            String item = url.substring(firstSlashIndex); // 截取'/'之后的内容
            urlList.add(item);
        }

        return urlList;
    }
}
