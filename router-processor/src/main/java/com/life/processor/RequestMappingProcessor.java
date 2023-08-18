package com.life.processor;


import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.life.annotation.RequestMapping;
import com.life.bean.ViewType;
import com.life.gradle.RouterMappingCollector;
import com.life.processor_utils.FileUtil;
import com.life.visitor.JavaParserClassVisitor;

import com.life.visitor.JavaParserMethodVisitor;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
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
    private final static String classFilePath = "com.life.router.mapping";
    private final static String jsonFilePath = "resource/router/";
    private static  String requestMappingClassName;  // 路由表类名


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
            // 生成路由类名
            requestMappingClassName = "RequestMapping_" + System.currentTimeMillis();
            //  获取模板文件
            String requestMappingTemplateContent = FileUtil.getInstance()
                    .readFileAsString("/template/RequestMappingTemplate.java");
            //  解析源代码
            CompilationUnit javaParse = StaticJavaParser.parse(requestMappingTemplateContent);

            //  使用访问者模式找到对应的类并进行修改
            javaParse.accept(new JavaParserClassVisitor("RequestMapping", requestMappingClassName), null);
            //  添加的语句集合
            ArrayList<StringBuilder> generateViewBeanBuilderList = new ArrayList<>();
            // 生成json文件
            final JsonArray requestMappingJsonArray = new JsonArray();
            String mappingFullClassName = classFilePath + "." + requestMappingClassName;
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
                generateViewBeanBuilderList.add(
                        new StringBuilder("\t\tgenerateViewBean(UrlUtil.getInstance().splitUrl(\"")
                                .append(url).append("\"), " + "mapping, " + "new ViewBean(" + "\"")
                                .append(url).append("\",").append("ViewType.").append(viewType)
                                .append(",").append("\"").append(viewClassName).append("\", ")
                                .append(container).append(",").append("\"").append(description)
                                .append("\",").append(" new HashMap<String, ViewBean>()").append("));\n")
                );
                //  组装json对象
                JsonObject item = new JsonObject();
                item.addProperty("url:", url);
                item.addProperty("description:", description);
                item.addProperty("viewClassName:", viewClassName);
                item.addProperty("viewType:", viewType.name());
                item.addProperty("containerId:", container);
                requestMappingJsonArray.add(item);
            }
            //添加路由数据
            javaParse.accept(new JavaParserMethodVisitor("RequestMapping", requestMappingClassName, generateViewBeanBuilderList), null);
            note("mappingFullClassName = " + mappingFullClassName);
//            note("class content = \n" + builder);

            /*写入自动生成的类到本地文件中*/
            try {
                JavaFileObject sourceClassFile = processingEnv.getFiler().createSourceFile(mappingFullClassName);
                Writer writer = sourceClassFile.openWriter();
                writer.write(javaParse.toString());
                writer.flush();
                writer.close();
                RouterMappingCollector.getInstance().collect(mappingFullClassName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            /*生成Mapping_xxxx.json文件*/
            try {
                FileObject resource = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT,
                        "", jsonFilePath + requestMappingClassName + ".json");
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
        switch (parentType) {
            case "androidx.fragment.app.Fragment":
                return ViewType.FRAGMENT; // 已经到达 Object 类，返回 null 表示没有父类的父类

            case "androidx.appcompat.app.FragmentActivity":
            case "androidx.appcompat.app.AppCompatActivity":
            case "androidx.activity.ComponentActivity":
            case "androidx.core.app.ComponentActivity":
            case "android.app.Activity":
                return ViewType.ACTIVITY;
            case "java.lang.Object":
                return ViewType.DEFAULT;
            default:
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