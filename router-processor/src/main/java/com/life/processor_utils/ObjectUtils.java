package com.life.processor_utils;

import java.util.HashMap;
import java.util.Map;

public class ObjectUtils {
    public static <T> boolean isNotEmpty(T data) {
        if (data instanceof String) {
            return ((String) data).length() != 0 || !data.equals("");
        } else if (data instanceof HashMap) {
            return !((HashMap<?, ?>) data).isEmpty();
        } else return data != null;
    }
}
