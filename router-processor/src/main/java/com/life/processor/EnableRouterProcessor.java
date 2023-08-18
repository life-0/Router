package com.life.processor;

import static com.sun.tools.javac.code.Flags.PARAMETER;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.life.annotation.EnableRouter;
import com.life.gradle.RouterMappingCollector;
import com.life.processor_utils.FileUtil;
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
    private static String routerMappingClassName;  //路由总表名称

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
                //  获取入口模板并初始化类
                String RouterActivityLifecycleListener = FileUtil.getInstance()
                        .readFileAsString("/template/RouterActivityLifecycleListener.java");
                String newRouterActivityLifecycleListenerPackageName = "com.life.app";
                String routerActivityLifecycleListenerClassName = newRouterActivityLifecycleListenerPackageName
                        + '.' + "RouterActivityLifecycleListener";
                CompilationUnit applicationParse = StaticJavaParser.parse(RouterActivityLifecycleListener);
                applicationParse.removePackageDeclaration();
                applicationParse.setPackageDeclaration(newRouterActivityLifecycleListenerPackageName);
                try {
                    JavaFileObject sourceFile = mFiler.createSourceFile(routerActivityLifecycleListenerClassName);
                    Writer writer = sourceFile.openWriter();
                    writer.write(applicationParse.toString());
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //  获取路由总表
                routerMappingClassName = RouterMappingCollector.getInstance().getMappingClassName();
                //  创建Router类文件
                if (ObjectUtils.isNotEmpty(routerMappingClassName)) {
                    note("get MappingClass: " + routerMappingClassName);
                    String routerTemplateContent = FileUtil.getInstance().readFileAsString("/template/RouterTemplate.java");
//                    InputStream inputStream = this.getClass().getResourceAsStream("/template/RouterTemplate.java");
                    // 解析源代码
                    CompilationUnit javaParse = StaticJavaParser.parse(routerTemplateContent);
                    // 使用访问者模式找到方法并进行修改
                    javaParse.accept(new RouterVisitor(), null);

                    //1 生成Router类
                    String routerFullClassName = "com.life.router.Router";
                 /*   StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(Arrays.toString(javaParse.stream().toArray()));*/
                    try {
                        JavaFileObject sourceFile = mFiler.createSourceFile(routerFullClassName);
                        Writer writer = sourceFile.openWriter();
                        writer.write(javaParse.toString());
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


    private static class RouterVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(MethodDeclaration methodDeclaration, Void arg) {
            super.visit(methodDeclaration, arg);
            // 检查方法名是否为要修改的方法名 init
            if (methodDeclaration.getNameAsString().equals("init")) {
                // 遍历方法体的语句
                methodDeclaration.getBody().ifPresent(methodBody -> {
                    // 检查方法名是否为要修改的方法名
                    for (Statement statement : methodBody.getStatements()) {
                        if (statement instanceof TryStmt) {
                            BlockStmt tryBlock = ((TryStmt) statement).getTryBlock();
                            // 遍历 try 块内的语句
                            for (MethodCallExpr methodCallExpr : tryBlock.findAll(MethodCallExpr.class)) {

                                // 检查方法调用是否是 Class.forName, 并替换参数
                                if (methodCallExpr.getNameAsString().equals("forName")) {
                                    // 检查是否是特定的参数
                                    if (methodCallExpr.getArguments().size() == 1 &&
                                            methodCallExpr.getArguments().get(0).isNameExpr() &&
                                            methodCallExpr.getArguments().get(0).asNameExpr().getNameAsString().equals("mappingClassName")) {
                                        // 替换参数为新值
                                        methodCallExpr.getArgument(0).replace(StaticJavaParser.parseExpression("\"" + routerMappingClassName + "\""));
                                    }
                                }
                            }
                        }
                    }

                });
            }
        }
    }


    private class ActivityVisitor extends TreeTranslator {
        @Override
        public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
            super.visitMethodDef(jcMethodDecl);
            //过滤方法
//            note("visitMethodDef: ", jcMethodDecl.getName().toString(), jcMethodDecl.getBody().toString());
            if (jcMethodDecl.getName().toString().equals("onCreate")) {
                // 创建新的语句
                // 创建 RouterActivityLifecycleListener.getInstance(getApplicationContext())
                JCTree.JCFieldAccess getInstance = treeMaker.Select(
                        treeMaker.Ident(names.fromString("RouterActivityLifecycleListener")),
                        names.fromString("getInstance")
                );
                //  this.getApplicationContext();
                JCTree.JCMethodInvocation getInstanceMethodInvocation = treeMaker.Apply(
                        List.nil(),
                        getInstance,
                        List.of(
                                treeMaker.Apply(
                                        List.nil(),
                                        treeMaker.Select(
                                                treeMaker.Ident(names.fromString("this")),
                                                names.fromString("getApplicationContext")
                                        ),
                                        List.nil()
                                )
                        )
                );

                // 创建 registerActivityLifecycleCallbacks 方法调用
                // registerActivityLifecycleCallbacks (this.getApplicationContext())
                JCTree.JCExpressionStatement statement = treeMaker.Exec(treeMaker.Apply(
                        List.nil(),
                        treeMaker.Ident(names.fromString("registerActivityLifecycleCallbacks")),
                        List.of(getInstanceMethodInvocation)
                ));
                // 获取原有方法体
                JCTree.JCBlock body = jcMethodDecl.body;

                // 在原有方法体的末尾添加新语句
                JCTree.JCBlock newBody = treeMaker.Block(
                        body.flags,
                        body.stats.append(statement));
                // 替换原有方法体
                jcMethodDecl.body = newBody;
            }


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
            Map<Name, JCTree.JCMethodDecl> treeMap = jcClassDecl.defs.stream()
                    .filter(k -> k.getKind().equals(Tree.Kind.METHOD))
                    .map(tree -> (JCTree.JCMethodDecl) tree).collect(Collectors.toMap(JCTree.JCMethodDecl::getName, Function.identity()));
            Iterator<Map.Entry<Name, JCTree.JCMethodDecl>> iterator = treeMap.entrySet().stream().iterator();

//            while (iterator.hasNext()) {
//                Map.Entry<Name, JCTree.JCMethodDecl> jcVariableDeclEntry = iterator.next();
//                note("name: " + jcVariableDeclEntry.getKey().toString());
//                note("method: " + jcVariableDeclEntry.getValue().toString());
//            }

        }

    }

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
        //
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
     * @ NAme
     */
    private Name getNameFromString(String s) {
        return names.fromString(s);
    }

    /**
     * 创建 域/方法 的多级访问, 方法的标识只能是最后一个
     *
     * @param components type
     * @return JCExpression
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
