package com.life.gradle;

public class RouterMappingCollector {
    private static class RouterMappingCollectorHolder {
        private final static RouterMappingCollector routerMappingCollector = new RouterMappingCollector();
    }

    public static RouterMappingCollector getInstance() {
        return RouterMappingCollectorHolder.routerMappingCollector;
    }

    // 映射路径表类名
    private String mappingClassNames = null;

    /**
     * 获取收集好的映射表类名
     */
    public String getMappingClassName() {
        return mappingClassNames;
    }

    /**
     * 收集class文件或者class文件目录中的映射表
     */
    public void collect(String className) {
        mappingClassNames = className;
    }

}
