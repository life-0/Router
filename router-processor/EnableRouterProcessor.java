package com.life.processor;


import com.life.annotation.EnableRouter;
import com.life.gradle.RouterMappingCollector;
import com.life.processor_utils.ObjectUtils;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;


public class EnableRouterProcessor extends AbstractProcessor {
    private final String TAG = EnableRouterProcessor.class.getSimpleName();
    private Filer mFiler;   // 文件管理工具类
    private Context context;
    private TreeMaker treeMaker;
    private Trees trees;
    private Name.Table names;
    /**
     * 用来处理Element的工具类
     * Elements接口的对象，用于操作元素的工具类。
     */
    private JavacElements elementUtils;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(EnableRouter.class.getCanonicalName());
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        context = ((JavacProcessingEnvironment) processingEnv).getContext();
        treeMaker = TreeMaker.instance(context);
        mFiler = processingEnv.getFiler();
        names = Names.instance(context).table;
        trees = Trees.instance(processingEnv);
        elementUtils = (JavacElements) processingEnv.getElementUtils();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()) {
            //获取所有标记注解的类的信息
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(EnableRouter.class);
            if (elements.isEmpty()) {
                return false;
            }
            //获取路由总表
            String mappingClassName = RouterMappingCollector.getInstance().getMappingClassName();
            if (ObjectUtils.isNotEmpty(mappingClassName)) {
                note("get MappingClass: " + mappingClassName);

                //1 生成Router类
                String routerFullClassName = "com.life.router.Router";
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("package com.life.router;\n" +
                        "\n" +
                        "import java.lang.ref.WeakReference;\n" +
                        "import java.lang.reflect.InvocationTargetException;\n" +
                        "import java.lang.reflect.Method;\n" +
                        "import java.util.HashMap;\n" +
                        "\n" +
                        "import android.content.ComponentName;\n" +
                        "import android.content.Context;\n" +
                        "import android.content.Intent;\n" +
                        "import android.content.pm.PackageInfo;\n" +
                        "import android.content.pm.PackageManager;\n" +
                        "import android.os.Bundle;\n" +
                        "\n" +
                        "import com.life.processor_utils.RouterLog;\n" +
                        "\n" +
                        "public class Router {\n" +
                        "    private WeakReference<Context> mContextReference;\n" +
                        "    private static HashMap<String, String> routerMapping;\n" +
                        "\n" +
                        "    private static class RouterHolder {\n" +
                        "        private final static Router router = new Router();\n" +
                        "    }\n" +
                        "\n" +
                        "    public Router() {\n" +
                        "\n" +
                        "    }\n" +
                        "\n" +
                        "    public void init(Context context) {\n" +
                        "        mContextReference = new WeakReference<>(context);\n" +
                        "        //获取生成的路由总表类, 并且实例化 mappingClassName\n" +
                        "        try {\n" +
                        "            Class<?> aClass = Class.forName(\"" + mappingClassName + "\");\n" +
                        "            Object instance = aClass.newInstance();\n" +
                        "            Method getMethod = aClass.getMethod(\"get\");\n" +
                        "            routerMapping = (HashMap<String, String>) getMethod.invoke(instance);\n" +
                        "        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |\n" +
                        "                 IllegalAccessException | InvocationTargetException e) {\n" +
                        "            throw new RuntimeException(e);\n" +
                        "        }\n" +
                        "\n" +
                        "    }\n" +
                        "\n" +
                        "    private Context getContext() {\n" +
                        "        return mContextReference.get();\n" +
                        "\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Router getInstance() {\n" +
                        "        return RouterHolder.router;\n" +
                        "    }\n" +
                        "\n" +
                        "    /**\n" +
                        "     * 获取当前应用的包名\n" +
                        "     *\n" +
                        "     * @return String packageName\n" +
                        "     */\n" +
                        "    private String getCurrentPackageName() {\n" +
                        "        PackageManager packageManager = getContext().getPackageManager();\n" +
                        "        try {\n" +
                        "            PackageInfo packageInfo = packageManager.getPackageInfo(getContext().getPackageName(), 0);\n" +
                        "            return packageInfo.packageName;\n" +
                        "        } catch (PackageManager.NameNotFoundException e) {\n" +
                        "            throw new RuntimeException(e);\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    /**\n" +
                        "     * 获取映射表路径对应的类名\n" +
                        "     *\n" +
                        "     * @param url 路径\n" +
                        "     * @return Activity全类名\n" +
                        "     */\n" +
                        "    private String getRouterMapping(String url) {\n" +
                        "        return routerMapping.get(url);\n" +
                        "    }\n" +
                        "\n" +
                        "    public void redirect(String url, Bundle bundle) {\n" +
                        "        //判断Router类是否初始化, 如果初始化了,如果没有初始化,那就执行初始化\n" +
                        "        if (getContext() == null) {\n" +
                        "            ClassLoader classLoader = this.getClass().getClassLoader();\n" +
                        "            Class classzz = null;\n" +
                        "            try {\n" +
                        "                classzz = classLoader.loadClass(\"com.life.app.MyApplication\");\n" +
                        "                System.out.println(classzz.getName());\n" +
                        "            } catch (ClassNotFoundException e) {\n" +
                        "                throw new RuntimeException(e);\n" +
                        "            }\n" +
                        "\n" +
                        "        }\n" +
                        "        // 获取切换目标页面的类名\n" +
                        "        String targetViewName = getRouterMapping(url);\n" +
                        "        ComponentName componentName = new ComponentName(getCurrentPackageName(), targetViewName);\n" +
                        "        RouterLog.info(targetViewName);\n" +
                        "        Intent intent = new Intent();\n" +
                        "        intent.setComponent(componentName);\n" +
                        "        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);\n" +
                        "        intent.putExtra(\"test1\", \"test2\");\n" +
                        "        intent.putExtras(bundle);\n" +
                        "        getContext().startActivity(intent);\n" +
                        "    }\n" +
                        "\n" +
                        "}\n");
                try {
                    JavaFileObject sourceFile = mFiler.createSourceFile(routerFullClassName);
                    Writer writer = sourceFile.openWriter();
                    writer.write(stringBuilder.toString());
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                for (Element element : elements) {
                    //2 给Application里添加Router的初始化
                    note(" assertions inlined class " + ((TypeElement) element).getQualifiedName().toString());

                    JCTree tree = (JCTree) trees.getTree(element);
                    TreeTranslator visitor = new ActivityVisitor();
                    tree.accept(visitor);
                }

            }


        }
        return true;
    }

    private class ActivityVisitor extends TreeTranslator {
        @Override
        public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
            super.visitMethodDef(jcMethodDecl);
            //过滤方法
            note("visitMethodDef: ", jcMethodDecl.getName().toString(), jcMethodDecl.getBody().toString());

//            if (jcMethodDecl.getName().toString().equals("onCreate")) {
//                JCTree.JCMethodDecl methodDecl = treeMaker.MethodDef(jcMethodDecl.getModifiers(),
//                        getNameFromString("testMethod"), jcMethodDecl.restype,
//                        jcMethodDecl.getTypeParameters(), jcMethodDecl.getParameters(),
//                        jcMethodDecl.getThrows(), jcMethodDecl.getBody(), jcMethodDecl.defaultValue);
//                result = methodDecl;
//            }

        }

        @Override
        public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
            super.visitClassDef(jcClassDecl);
            JCTree.JCModifiers jcModifiers = jcClassDecl.mods;  //  修饰符public...
            Name name = jcClassDecl.name;   // 类名
            List<JCTree.JCTypeParameter> typeParameters = jcClassDecl.getTypeParameters();//泛型参数列表
            JCTree.JCExpression extending = jcClassDecl.extending;  //  获取父类
            List<JCTree.JCExpression> implementsClause = jcClassDecl.getImplementsClause(); //接口列表
            List<JCTree> defs = jcClassDecl.defs;//变量,方法列表
            Symbol.ClassSymbol sym = jcClassDecl.sym;   //包名+类名
            note("modifierName:" + jcModifiers.toString(), "className: " + name.toString(), "typeParameters:" + Arrays.toString(typeParameters.toArray()), "extending: " + extending.toString(), "implementsClause: " + implementsClause.toString(), "defs: " + Arrays.toString(defs.toArray()), "sym: " + sym.toString());
            //过滤属性
//            Map<Name, JCTree.JCVariableDecl> treeMap = jcClassDecl.defs.stream()
//                    .filter(k -> k.getKind().equals(Tree.Kind.VARIABLE))
//                    .map(tree -> (JCTree.JCVariableDecl) tree).collect(Collectors.toMap(JCTree.JCVariableDecl::getName, Function.identity()));
            jcClassDecl.defs = jcClassDecl.defs.prepend(modifyMethod());
         /*   //处理变量
            treeMap.forEach((k, jcVariable) -> {
                note(String.format("fields:%s", k));
                try {
                    //增加get方法
                    jcClassDecl.defs = jcClassDecl.defs.prepend(generateGetterMethod(jcVariable));
                    //增加set方法
                    jcClassDecl.defs = jcClassDecl.defs.prepend(generateSetterMethod(jcVariable));
                } catch (Exception e) {
                    note(Throwables.getStackTraceAsString(e));
                }
            });
*/
        }

    }

    private JCTree.JCMethodDecl modifyMethod() {
        note("modifyMethod...");
        //修改方法级别
        JCTree.JCModifiers jcModifiers = treeMaker.Modifiers(Flags.PUBLIC);

        //添加方法名称
        Name methodName = getNameFromString("onCreate");
        //添加方法内容
        ListBuffer<JCTree.JCStatement> jcStatements = new ListBuffer<>();
//        jcStatements.append(treeMaker.Exec(treeMaker.Apply(
//                //参数类型(传入方法的参数的类型) 如果是无参的不能设置为null 使用 List.nil()
//                List.of(memberAccess("java.lang.String")),
//                memberAccess("java.lang.System.out.println"),
//                //因为不需要传递参数,所以直接设置为List.nil() 不能设置为null
//                List.of(treeMaker.Literal("test...."))
//                //参数集合[集合中每一项的类型需要跟第一个参数对照]
////                List.of(treeMaker.Literal())
//        )));
//         super.onCreate();
        jcStatements.append(treeMaker.Exec(treeMaker.Apply(
                List.nil(),
                memberAccess("super.onCreate"),
                List.nil()
        )));

//        ListBuffer<JCTree.JCStatement> routerObjectStatement = jcStatements.append(treeMaker.Exec(treeMaker.Apply(
//                List.nil(),
//                treeMaker.Select(
//                        treeMaker.Ident(
//                                getNameFromString("Router")),
//                        getNameFromString("getInstance")),
//                List.nil()
//        )));
//         Router router = Router.getInstance();
        jcStatements.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER),
                getNameFromString("router"),
                treeMaker.Ident(getNameFromString("Router")),
                treeMaker.Apply(
                        List.nil(),
                        treeMaker.Select(
                                treeMaker.Ident(
                                        getNameFromString("Router")),
                                getNameFromString("getInstance")),
                        List.nil()
                ))
        );
        //  Class<Router> routerClass = Router.class;
        jcStatements.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER),
                getNameFromString("routerClass"), treeMaker.Ident(getNameFromString("Class")),
                treeMaker.Select(treeMaker.Ident(names.fromString("Router")), getNameFromString("class"))
        ));

//         routerClass.getMethod("init", Context.class)
        jcStatements.append(treeMaker.Exec(treeMaker.Apply(
                List.nil(),
                treeMaker.Select(treeMaker.Ident(getNameFromString("routerClass")), getNameFromString("getMethod")),
                List.of(treeMaker.Literal("init"), treeMaker.Select(treeMaker.Ident(names.fromString("Context")), names.fromString("class")))
        )));

//        jcStatements.append(treeMaker.Exec(treeMaker.Select(treeMaker.Ident(names.fromString("Router")), names.fromString("class"))));
//        NoSuchMethodException e
//        JCTree.JCVariableDecl noSuchMethodExceptionVariableDecl = treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), getNameFromString("e"), treeMaker.Ident(getNameFromString("NoSuchMethodException")), null);
//        System.out.println(e.toString())
    /*    jcStatements.append( treeMaker.Exec(treeMaker.Apply(
                List.of(memberAccess("java.lang.String")),
                memberAccess("java.lang.System.out.println"),
                List.of( treeMaker.Apply(
                        List.nil(),
                        treeMaker.Select(
                                treeMaker.Ident(
                                        getNameFromString("e")),
                                getNameFromString("toString")),
                        List.nil()
                ))
        )));*/
      /*  JCTree.JCCatch aCatch = treeMaker.Catch(noSuchMethodExceptionVariableDecl,
                treeMaker.Block(0L, List.of(outExceptionStatement)));
        jcStatements.append(
                treeMaker.Try(
                        treeMaker.Block(0, List.of(getMethodStatement)),
                        List.of(aCatch),
                        treeMaker.Block(0, List.nil())
                )
        );*/

        //定义方法体
        JCTree.JCBlock jcBlock = treeMaker.Block(0, jcStatements.toList());
        //添加返回值类型  返回为空
        JCTree.JCExpression returnType = treeMaker.Type(new Type.JCVoidType());

        //参数类型
        List<JCTree.JCTypeParameter> typeParameters = List.nil();

        //参数变量
        List<JCTree.JCVariableDecl> parameters = List.nil();

        //声明异常
        List<JCTree.JCExpression> throwsClauses = List.nil();
        //构建方法
        return treeMaker.MethodDef(jcModifiers, methodName, returnType, typeParameters, parameters, throwsClauses, jcBlock, null);
    }

    /**
     * 根据字符串获取Name，（利用Names的fromString静态方法）
     *
     * @param s
     * @return
     */
    private Name getNameFromString(String s) {
        return names.fromString(s);
    }

    /**
     * 创建 域/方法 的多级访问, 方法的标识只能是最后一个
     *
     * @param components
     * @return
     */
    private JCTree.JCExpression memberAccess(String components) {
        String[] componentArray = components.split("\\.");
        JCTree.JCExpression expr = treeMaker.Ident(getNameFromString(componentArray[0]));
        for (int i = 1; i < componentArray.length; i++) {
            expr = treeMaker.Select(expr, getNameFromString(componentArray[i]));
        }
        return expr;
    }

    public void note(String... args) {
        for (String arg : args) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, TAG + "\t" + arg);
        }
    }

}
