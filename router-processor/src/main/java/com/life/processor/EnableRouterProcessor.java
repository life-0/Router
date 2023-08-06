package com.life.processor;


import static com.sun.tools.javac.code.Flags.PARAMETER;
import static com.sun.tools.javac.code.Flags.PUBLIC;
import static com.sun.tools.javac.code.Flags.STATIC;
import static com.sun.tools.javac.code.Flags.VARARGS;

import com.life.annotation.EnableRouter;
import com.life.gradle.RouterMappingCollector;
import com.life.processor_utils.ObjectUtils;
import com.sun.source.tree.Tree;
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
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
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
            for (Element element : elements) {
                final TypeElement typeElement = (TypeElement) element;
                final String applicationClassName = typeElement.getQualifiedName().toString();  // 获取类的全名

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
                            "import java.util.AbstractMap;\n" +
                            "import java.util.ArrayList;\n" +
                            "import java.util.HashMap;\n" +
                            "import java.util.Map;\n" +
                            "import java.util.regex.Matcher;\n" +
                            "import java.util.regex.Pattern;\n" +
                            "\n" +
                            "import android.app.Activity;\n" +
                            "import android.app.Application;\n" +
                            "import android.content.ComponentName;\n" +
                            "import android.content.Context;\n" +
                            "import android.content.Intent;\n" +
                            "import android.content.pm.PackageInfo;\n" +
                            "import android.content.pm.PackageManager;\n" +
                            "import android.os.Bundle;\n" +
                            "import android.view.View;\n" +
                            "\n" +
                            "import androidx.appcompat.app.AppCompatActivity;\n" +
                            "import androidx.collection.SimpleArrayMap;\n" +
                            "import androidx.fragment.app.Fragment;\n" +
                            "import androidx.fragment.app.FragmentActivity;\n" +
                            "\n" +
                            "\n" +
                            "import com.life.app.R;\n" +
                            "import com.life.bean.ViewBean;\n" +
                            "import com.life.bean.ViewType;\n" +
                            "import com.life.processor_utils.ObjectUtils;\n" +
                            "import com.life.processor_utils.RouterLog;\n" +
                            "import com.life.processor_utils.UrlUtil;\n" +
                            "\n" +
                            "public class Router {\n" +
                            "    private WeakReference<Context> mContextReference;\n" +
                            "    private static HashMap<String, ViewBean> routerMapping;\n" +
                            "    private final static String TAG = Router.class.getSimpleName();\n" +
                            "    private static int layout_id;   //布局id\n" +
                            "\n" +
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
                            "            routerMapping = (HashMap<String, ViewBean>) getMethod.invoke(instance);\n" +
                            "        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |\n" +
                            "                IllegalAccessException | InvocationTargetException e) {\n" +
                            "            throw new RuntimeException(e);\n" +
                            "        }\n" +
                            "\n" +
                            "    }\n" +
                            "\n" +
                            "    private Context getContext() {\n" +
                            "        if (mContextReference != null) {\n" +
                            "            return mContextReference.get().getApplicationContext();\n" +
                            "        } else {\n" +
                            "            return null;\n" +
                            "        }\n" +
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
                            "     * 解析路径,返回对应的类名\n" +
                            "     *\n" +
                            "     * @param url\n" +
                            "     * @return\n" +
                            "     */\n" +
                            "    private ArrayList<ViewBean> analyzeUrl(ArrayList<String> urlList, HashMap<String, ViewBean> viewBeanHashMap) {\n" +
                            "        ArrayList<ViewBean> viewBeans = new ArrayList<>();\n" +
                            "        String url = urlList.remove(0);\n" +
                            "        ViewBean viewBean = viewBeanHashMap.get(url);\n" +
                            "        if (ObjectUtils.isNotEmpty(viewBean)) {\n" +
                            "            viewBeans.add(viewBean);\n" +
                            "            HashMap<String, ViewBean> children = viewBean.getChildren();\n" +
                            "            if (ObjectUtils.isNotEmpty(children)) {\n" +
                            "                analyzeUrl(urlList, children);\n" +
                            "            }\n" +
                            "        }\n" +
                            "        return viewBeans;\n" +
                            "    }\n" +
                            "\n" +
                            "    /**\n" +
                            "     * 获取映射表路径对应的类名\n" +
                            "     *\n" +
                            "     * @param url 路径\n" +
                            "     * @return Activity全类名\n" +
                            "     */\n" +
                            "    private ViewBean getRouterMapping(String url) {\n" +
                            "        return routerMapping.get(url);\n" +
                            "    }\n" +
                            "\n" +
                            "    public void redirect(String url, Bundle bundle) {\n" +
                            "        ArrayList<String> urlList = UrlUtil.getInstance().splitUrl(url);    //分割页面\n" +
                            "//        ArrayList<ViewBean> viewBeans = analyzeUrl(urlList, routerMapping); // 获取ViewBean\n" +
                            "        AppCompatActivity activity = null;  //主页面对象\n" +
                            "        Fragment parentFragment = null;   //上一级子页面对象\n" +
                            "        ViewBean tempViewBean = routerMapping.get(urlList.get(0));  //临时的ViewBean, 暂时存放url遍历出来的ViewBean\n" +
                            "        HashMap<String, ViewBean> tempChildrenMap = tempViewBean.getChildren();  //临时的ViewBean, 暂时存放url遍历出来的ViewBean\n" +
                            "        for (int i = 0; i < urlList.size(); i++) {\n" +
                            "            //给下一个页面元素赋值\n" +
                            "            if (i != 0 && ObjectUtils.isNotEmpty(tempChildrenMap)) {\n" +
                            "                tempViewBean = tempChildrenMap.get(urlList.get(i));\n" +
                            "            }\n" +
                            "\n" +
                            "            Intent intent = new Intent();\n" +
                            "            if (urlList.size() == (i + 1)) {    //跳转到最后一个子页面就装填数据\n" +
                            "                intent.putExtras(bundle);\n" +
                            "            }\n" +
                            "            if (tempViewBean.getViewType() == ViewType.ACTIVITY) {    //页面是Activity就按照activity的方式拉起\n" +
                            "                // 获取切换目标页面的类名\n" +
                            "                String targetViewName = tempViewBean.getClassName();\n" +
                            "                layout_id = tempViewBean.getContainerId() != -1 ? tempViewBean.getContainerId() : layout_id;  //  赋予fragment_layout的资源id\n" +
                            "                ComponentName componentName = new ComponentName(getCurrentPackageName(), targetViewName);\n" +
                            "                RouterLog.info(targetViewName);\n" +
                            "                intent.setComponent(componentName);\n" +
                            "                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);\n" +
                            "                getContext().startActivity(intent);// 执行跳转页面\n" +
                            "                //获取当前跳转的主页类 需要强转类型 applicationClassName\n" +
                            "                com.life.app.MyApplication applicationContext = (com.life.app.MyApplication) getContext();\n" +
                            "                activity = (AppCompatActivity) applicationContext.getCurrentActivity();\n" +
                            "\n" +
                            "            } else if (tempViewBean.getViewType() == ViewType.FRAGMENT) {\n" +
                            "                try {\n" +
                            "                    // 先获取fragment实例\n" +
                            "                    Class<?> fragmentClass = Class.forName(tempViewBean.getClassName());\n" +
                            "                    Fragment fragment = (Fragment) fragmentClass.newInstance();\n" +
                            "                    //判断上一级页面是不是Activity\n" +
                            "                       if (activity != null) { //在父Activity加载fragment\n" +
                            "                        activity.getSupportFragmentManager()\n" +
                            "                                .beginTransaction()\n" +
                            "                                .replace(layout_id, fragment)\n" +
                            "                                .commit();\n" +
                            "                        activity = null;\n" +
                            "\n" +
                            "                    }\n" +
                            "                    if (parentFragment != null) {// 在父 Fragment 中加载子 Fragment\n" +
                            "                        fragment.setArguments(bundle);  //装填参数\n" +
                            "                        parentFragment.getChildFragmentManager()\n" +
                            "                                .beginTransaction()\n" +
                            "                                .replace(layout_id, fragment)\n" +
                            "                                .addToBackStack(null) // 添加到返回栈，实现返回键返回父 Fragment\n" +
                            "                                .commit();\n" +
                            "                    }\n" +
                            "                    parentFragment = fragment;  //\n" +
                            "                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {\n" +
                            "                    e.printStackTrace();\n" +
                            "                }\n" +
                            "\n" +
                            "            } else if (tempViewBean.getViewType() == ViewType.DEFAULT) {\n" +
                            "                RouterLog.e(\"It was found that this class(\" + tempViewBean.getClassName() + \") is not a Fragment or Activity.\");\n" +
                            "            }\n" +
                            "            tempChildrenMap = tempViewBean.getChildren();   //获取当前页面的子页面\n" +
                            "        }\n" +
                            "\n" +
                            "\n" +
                            "    }\n" +
                            "\n" +
                            "    public void redirect(String url) {\n" +
                            "        // 获取切换目标页面的类名\n" +
                            "        String targetViewName = getRouterMapping(url).getClassName();\n" +
                            "        ComponentName componentName = new ComponentName(getCurrentPackageName(), targetViewName);\n" +
                            "        RouterLog.info(targetViewName);\n" +
                            "        Intent intent = new Intent();\n" +
                            "        intent.setComponent(componentName);\n" +
                            "        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);\n" +
                            "        try {\n" +
                            "            getContext().startActivity(intent);\n" +
                            "        } catch (Exception e) {\n" +
                            "            RouterLog.e(\"The Router framework has not been initialized, \" +\n" +
                            "                    \"and the `@EnableRouter` annotation needs to be added at the entry point. \");\n" +
                            "            e.printStackTrace();\n" +
                            "        }\n" +
                            "\n" +
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

                    //2 给Application里添加Router的初始化
                    note(" assertions inlined class " + ((TypeElement) element).getQualifiedName().toString());
                    JCTree tree = (JCTree) trees.getTree(element);
//                  因为系统中有很多的类或者方法使用了目标注解，
//                  而在代码生成过程中(也就是accept里重写visitClassDef为当前class append或prepend的元素),
//                  没有对treeMarker.pos的修改，使得其一直保持不变即使操作已经进行到下个类文件，pos仍然是不变的。
//                  当处理的方法或者处理的类抵达下一个之后, 就要先修改pos
                    treeMaker.pos = tree.pos;
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
//            note("visitMethodDef: ", jcMethodDecl.getName().toString(), jcMethodDecl.getBody().toString());

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

//            note("modifierName:" + jcModifiers.toString(), "className: " + name.toString(),
//                    "typeParameters:" + Arrays.toString(typeParameters.toArray()), "extending: " + extending.toString(),
//                    "implementsClause: " + implementsClause.toString(), "defs: " + Arrays.toString(defs.toArray()),
//                    "sym: " + sym.toString());
            //过滤方法属性
       /*     Map<Name, JCTree.JCMethodDecl> treeMap = jcClassDecl.defs.stream()
                    .filter(k -> k.getKind().equals(Tree.Kind.METHOD))
                    .map(tree -> (JCTree.JCMethodDecl) tree).collect(Collectors.toMap(JCTree.JCMethodDecl::getName, Function.identity()));
            Iterator<Map.Entry<Name, JCTree.JCMethodDecl>> iterator = treeMap.entrySet().stream().iterator();

            while (iterator.hasNext()) {
                Map.Entry<Name, JCTree.JCMethodDecl> jcVariableDeclEntry = iterator.next();
                note("name: " + jcVariableDeclEntry.getKey().toString());
                note("method: " + jcVariableDeclEntry.getValue().toString());
            }*/
          /*  if (treeMap.get(getNameFromString("onCreate")) == null) {
                jcClassDecl.defs = jcClassDecl.defs.prepend(generalRouterInitMethod());
            } else {

            }*/
//            jcClassDecl.defs = jcClassDecl.defs.prepend(generateVariable());
//            jcClassDecl.defs = jcClassDecl.defs.prepend(generalRouterInitMethod());
        }

    }

/*    private JCTree.JCMethodDecl routerInitStatement() {
        //public static Context mContext;

    }*/

    private JCTree.JCMethodDecl generalRouterInitMethod() {
        note("generalRouterInitMethod...");
        //修改方法级别
        JCTree.JCModifiers jcModifiers = treeMaker.Modifiers(Flags.PUBLIC);

        //添加方法名称
        Name methodName = getNameFromString("onCreate");
        //添加方法内容
        ListBuffer<JCTree.JCStatement> jcStatements = new ListBuffer<>();
        jcStatements.append(treeMaker.Exec(treeMaker.Apply(
                List.of(memberAccess("java.lang.String")),
                memberAccess("java.lang.System.out.println"),
                List.of(treeMaker.Literal("Inside try"))
        )));
//         super.onCreate();
        jcStatements.append(treeMaker.Exec(treeMaker.Apply(List.nil(), memberAccess("super.onCreate"), List.nil())));
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

//         routerClass.getMethod("init", Application.class)
        JCTree.JCExpressionStatement routerClassGetMethodStatement = treeMaker.Exec(treeMaker.Apply(
                List.nil(),
                treeMaker.Select(treeMaker.Ident(getNameFromString("routerClass")), getNameFromString("getMethod")),
                List.of(treeMaker.Literal("init"), treeMaker.Select(treeMaker.Ident(names.fromString("Application")), names.fromString("class")))
        ));

//          initMethod.invoke(router, getApplicationContext());
        JCTree.JCExpressionStatement initMethodInvokeStatement = treeMaker.Exec(treeMaker.Apply(
                List.nil(),
                treeMaker.Select(treeMaker.Ident(getNameFromString("initMethod")), getNameFromString("invoke")),
                List.of(treeMaker.Ident(getNameFromString("routerClass")),
                        treeMaker.Select(treeMaker.Ident(getNameFromString("this")), getNameFromString("getApplicationContext")))
        ));
        jcStatements.append(treeMaker.Exec(treeMaker.Assign(
                treeMaker.Ident(getNameFromString("mContext")),
                treeMaker.Apply(List.nil(),
                        treeMaker.Select(treeMaker.Ident(getNameFromString("this")), getNameFromString("getApplicationContext")),
                        List.nil()))));

        // 构造 try 语句块
        JCTree.JCBlock tryBlock = treeMaker.Block(0, List.of(
                treeMaker.Exec(treeMaker.Apply(
                        List.of(memberAccess("java.lang.String")),
                        memberAccess("java.lang.System.out.println"),
                        List.of(treeMaker.Literal("Inside try"))
                )), routerClassGetMethodStatement));

        // 构造 catch 语句块
        JCTree.JCVariableDecl exceptionVar = treeMaker.VarDef(
                treeMaker.Modifiers(PARAMETER),
                getNameFromString("e"),
                treeMaker.Ident(getNameFromString("Exception")), null);
        JCTree.JCBlock catchBlock = treeMaker.Block(0, List.of(
                treeMaker.Exec(treeMaker.Apply(
                        List.of(memberAccess("java.lang.String")),
                        memberAccess("java.lang.System.out.println"),
                        List.of(treeMaker.Literal("Inside catch"))
                )),
                treeMaker.Exec(treeMaker.Apply(
                        List.nil(),
                        memberAccess("e.printStackTrace"),
                        List.nil()
                ))));


        // 构造 finally 语句块   List.of(treeMaker.Literal("Inside finally"))
        JCTree.JCBlock finallyBlock = treeMaker.Block(0, List.of(
                treeMaker.Exec(treeMaker.Apply(
                        List.of(memberAccess("java.lang.String")),
                        memberAccess("java.lang.System.out.println"),
                        List.of(treeMaker.Literal("Inside finally"))
                ))));

        // 使用 TreetreeMaker.Try() 构造整个 try-catch-finally 语句
        JCTree.JCTry tryCatchFinally = treeMaker.Try(tryBlock, List.of(treeMaker.Catch(exceptionVar, catchBlock)), finallyBlock);
        jcStatements.append(tryCatchFinally);
        note(jcStatements.toString());
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
