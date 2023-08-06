package com.life.processor;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.life.annotation.RequestMapping;
import com.life.bean.ViewType;
import com.life.gradle.RouterMappingCollector;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;

import javax.annotation.processing.RoundEnvironment;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;


public class RequestMappingProcessor extends AbstractProcessor {
    private final String TAG = RequestMappingProcessor.class.getSimpleName();
    private final String classFilePath = "com.life.router.mapping";
    private final String jsonFilePath = "resource/router/";

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(RequestMapping.class.getCanonicalName());
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()) {
            //获取所有标记注解的类的信息
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(RequestMapping.class);
            if (elements.isEmpty()) {
                return false;
            }
//            生成路由类
            String className = "RequestMapping_" + System.currentTimeMillis();
            StringBuilder builder = new StringBuilder();
            builder.append("package " + classFilePath + ";\n\n");
            builder.append("import java.util.ArrayList;\n" +
                    "import java.util.HashMap;\n" +
                    "import java.util.Map;\n" +
                    "\n" +
                    "import com.life.bean.ViewBean;\n" +
                    "import com.life.bean.ViewType;\n" +
                    "import com.life.processor_utils.ObjectUtils;\n" +
                    "import com.life.processor_utils.UrlUtil;\n\n");
            builder.append("public class ").append(className).append(" {\n");
            builder.append("\tprivate static HashMap<String, ViewBean> mapping = new HashMap<>();\n\n ");
            builder.append("\tpublic " + className + "() {\n ");

            final JsonArray requestMappingJsonArray = new JsonArray();
            String mappingFullClassName = classFilePath + "." + className;
            //便利所有的@RequestMapping
            for (Element element : elements) {
                final TypeElement typeElement = (TypeElement) element;

                RequestMapping annotation = typeElement.getAnnotation(RequestMapping.class);
                if (annotation == null) continue;
                final String url = annotation.url();    // 获取注解的url值
                final String description = annotation.description();    //  获取注解的description值
                final int container = annotation.container();    //  获取注解的container值
                final String viewClassName = typeElement.getQualifiedName().toString();  // 获取类的全名
                Types types = null; // 在实际应用中，您需要获取Types对象
                ViewType viewType = getGrandparentType(typeElement);
                note("url= " + url, "description= " + description, "realPath= " + viewClassName, "container= " + container);

                //  map装填数据
              /*  builder.append("\t\tmapping.put(");
                builder.append("\"" + url + "\", ");
                builder.append("new ViewBean("
                        + "\"" + url + "\","
                        + "\"" + viewType.name() + "\","
                        + "\"" + viewClassName + "\","
                        + "\"" + description + "\","
                        + container
                        + "));\n");*/
                builder.append("\t\tgenerateViewBean(UrlUtil.getInstance().splitUrl(\"").append(url).append("\"), " +
                        "mapping, " +
                        "new ViewBean("
                        + "\"" + url + "\","
                        + "ViewType." + viewType + ","

                        + "\"" + viewClassName + "\", "
                        + container + ","
                        + "\"" + description + "\","
                        + " new HashMap<String, ViewBean>()"
                        + "));\n");
                //  组装json对象
                JsonObject item = new JsonObject();
                item.addProperty("url:", url);
                item.addProperty("description:", description);
                item.addProperty("viewClassName:", viewClassName);
                item.addProperty("viewType:", viewType.name());
                item.addProperty("containerId:", container);
                requestMappingJsonArray.add(item);
            }
            builder.append("\t}\n\n");

            builder.append("\tpublic static HashMap<String,ViewBean> " + "get() {\n ");
            builder.append("\t\treturn mapping;\n");
            builder.append("\t}\n");

            builder.append("\n" +
                    "    /**\n" +
                    "     * 生成ViewBean\n" +
                    "     *\n" +
                    "     * @param url\n" +
                    "     * @return\n" +
                    "     */\n" +
                    "    private void generateViewBean(ArrayList<String> list, HashMap<String, ViewBean> childrenViewBeanMap, ViewBean targetViewBean) {\n" +
                    "        String url = list.remove(0);\n" +
                    "        ViewBean bean = childrenViewBeanMap.get(url);\n" +
                    "        HashMap<String, ViewBean> children = ObjectUtils.isNotEmpty(bean) ? bean.getChildren() : null;\n" +
                    "        //判断当前的url是否为最后一个\n" +
                    "        if (list.size() == 0) {\n" +
                    "            //子页面存在, 只更新数据, 不覆盖子页面数据\n" +
                    "            if (ObjectUtils.isNotEmpty(children)) {\n" +
                    "                bean.setUrl(targetViewBean.getUrl());\n" +
                    "                bean.setViewType(targetViewBean.getViewType());\n" +
                    "                bean.setClassName(targetViewBean.getClassName());\n" +
                    "                bean.setContainerId(targetViewBean.getContainerId());\n" +
                    "                bean.setDescription(targetViewBean.getDescription());\n" +
                    "                childrenViewBeanMap.put(url, bean);\n" +
                    "                return;\n" +
                    "            } else {\n" +
                    "                //子页面不存在,直接创建目标页面数据\n" +
                    "                bean = new ViewBean(targetViewBean.getUrl(), targetViewBean.getViewType(),\n" +
                    "                        targetViewBean.getClassName(), targetViewBean.getContainerId(), targetViewBean.getDescription());\n" +
                    "                childrenViewBeanMap.put(url, bean);\n" +
                    "                return;\n" +
                    "            }\n" +
                    "        }\n" +
                    "\n" +
                    "        if (ObjectUtils.isNotEmpty(bean)) { //不为空就获取子页面集合\n" +
                    "            generateViewBean(list, children, targetViewBean);   //走递归\n" +
                    "        } else {    //不存在就创建子页面\n" +
                    "            ViewBean viewBean = new ViewBean(url, ViewType.DEFAULT, \"null\", -1,\n" +
                    "                    \"null\", new HashMap<String, ViewBean>());\n" +
                    "            mapping.put(url, viewBean);\n" +
                    "            generateViewBean(list, viewBean.getChildren(), targetViewBean);   //走递归\n" +
                    "        }\n" +
                    "    }\n");
            builder.append("}");
            note("mappingFullClassName = " + mappingFullClassName);
//            note("class content = \n" + builder);

            /*写入自动生成的类到本地文件中*/
            try {
                JavaFileObject sourceClassFile = processingEnv.getFiler().createSourceFile(mappingFullClassName);
                Writer writer = sourceClassFile.openWriter();
                writer.write(builder.toString());
                writer.flush();
                writer.close();
                RouterMappingCollector.getInstance().collect(mappingFullClassName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            /*生成Mapping_xxxx.json文件*/
            try {
                FileObject resource = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT,
                        "", jsonFilePath + className + ".json");
                Writer writer = resource.openWriter();
                writer.write(requestMappingJsonArray.toString());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            note("process finish ...");

        } else {
            note("assertions inlined.");
        }
        // 返回值表示这些注解是否由此 Processor 声明 如果返回 true，则这些注解不会被后续 Processor 处理；
        // 如果返回 false，则这些注解可以被后续的 Processor 处理。
        return true;
    }

    // 递归获取父类的父类类型
    private ViewType getGrandparentType(TypeElement typeElement) {
        note("getGrandparentType...");
        String parentType = typeElement.getSuperclass().toString();
        if (parentType.equals("androidx.fragment.app.Fragment")) {
            return ViewType.FRAGMENT; // 已经到达 Object 类，返回 null 表示没有父类的父类
        } else if (parentType.equals("androidx.appcompat.app.AppCompatActivity")) {
            return ViewType.ACTIVITY;
        } else if (parentType.equals("java.lang.Object")) {
            return ViewType.DEFAULT;
        } else {
            TypeElement parentElement = processingEnv.getElementUtils().getTypeElement(parentType);
            return getGrandparentType(parentElement);
        }
    }

    public void note(String... args) {
        for (String arg : args) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, TAG + "\t" + arg);
        }
    }
}