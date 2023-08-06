package com.life.processor_utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RouterLog {
    private static final Logger logger = Logger.getLogger(RouterLog.class.getSimpleName());

    private RouterLog() {
        logger.setLevel(Level.ALL);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);
    }

    public static void info(String targetClass) {
        StackTraceElement[] arr = Thread.currentThread().getStackTrace();
        StringBuilder builder = new StringBuilder();
        builder.append(arr[4].getClassName()).append(".");
        builder.append(arr[4].getMethodName());
        builder.append("() Line_");
        builder.append(arr[4].getLineNumber());
        builder.append(" -> ");
        builder.append(targetClass);
        i(builder.toString());  //日志输出

        /*
         int count = 0;
        for (StackTraceElement stackTraceElement : arr) {
            logger.log(Level.INFO,String.format("%s | %s%n", " index: " + count, stackTraceElement.getClassName()));
            count++;
        }*/
//        System.out.println(String.format("%s | %s", inferCaller(arr), msg));
    }

    public static void i(String content) {
        logger.info(content);
    }

    public static void w(String content) {
        logger.warning(content);
    }

    public static void e(String content) {
        logger.severe(content);
    }
}
