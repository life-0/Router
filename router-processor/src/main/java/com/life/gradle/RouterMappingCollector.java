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
     * 收集路由映射表 RequestMapping_XXXX
     */
    public void collect(String className) {
        mappingClassNames = className;
    }

}
