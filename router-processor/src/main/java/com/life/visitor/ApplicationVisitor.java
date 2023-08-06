package com.life.visitor;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.utils.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;

public class ApplicationVisitor {
    private static final String FILE_PATH = "ReversePolishNotation.java";

    public void modify(String applicationJavaFile) {
        JavaParser javaParser = new JavaParser();
        ParseResult<CompilationUnit> result = null;
        try {
            result = javaParser.parse(new File("D:\\AndroidStudioLearing\\AndroidStudioProjects\\AnnotationDemo\\11-router\\app\\src\\main\\java\\com\\life\\app\\MyApplication.java"));
        } catch (FileNotFoundException e) {
            Log.error(e);
            return;
        }
        result.getResult().get().getTypes().forEach(type -> {
            System.out.println(type.getName());
            //获取类名
            type.getMethods().forEach(method -> {
                System.out.println(method.getName());
                //获取方法名
                Iterator<Statement> iterator = method.getBody().get().getStatements().iterator();
                while (iterator.hasNext()) {
                    Statement statement = iterator.next();
                    if (statement.toString().contains("789"))
                        iterator.remove();
                    else
                        System.out.println("statement:" + statement.toString());
                }
                //此处在读文件的过程中添加了一行代码
                Expression whileCounterExpression = StaticJavaParser.parseVariableDeclarationExpr(" int i " + " = 0");
                whileCounterExpression.setLineComment("这是个注释，这里是新增的代码");
                method.getBody().get().addStatement(whileCounterExpression);
            });
        });
        System.out.println(result.getResult().get().toString());
    }
}
