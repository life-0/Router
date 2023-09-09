package com.life.visitor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.life.processor_utils.ObjectUtils;

import java.util.ArrayList;

public class JavaParserMethodVisitor extends ModifierVisitor<Void> {
    private String oldMethodName;
    private String newMethodName;
    private ArrayList<StringBuilder> statementList;

    /**
     * 修改方法名,或往方法里添加语句
     *
     * @param oldMethodName 原方法名
     * @param newMethodName 新方法名
     * @param statementList 新增语句
     */
    public JavaParserMethodVisitor(String oldMethodName, String newMethodName, ArrayList<StringBuilder> statementList) {
        this.oldMethodName = oldMethodName;
        this.newMethodName = newMethodName;
        this.statementList = statementList;
    }

    /**
     * 修改普通方法名和添加语句
     *
     * @param md  MethodDeclaration
     * @param arg Void
     * @return MethodDeclaration
     */
    @Override
    public MethodDeclaration visit(MethodDeclaration md, Void arg) {

        if (md.getName().toString().equals(oldMethodName)) {
            // 修改方法名
            md.setName(newMethodName);
            // 在方法体末尾添加新的语句
            BlockStmt methodBody = md.getBody().orElse(null);
            if (ObjectUtils.isNotEmpty(methodBody)) {
                // 要添加的新语句
                for (StringBuilder generateViewBeanStringBuilder : statementList) {
                    // 将新语句解析为 Statement 对象
                    Statement statement = StaticJavaParser.parseStatement(generateViewBeanStringBuilder.toString());
                    methodBody.addStatement(statement); //添加语句
                }
            }
        }
        return md;
    }

    /**
     * 修改构造方法名和添加语句
     *
     * @param constructorDeclaration ConstructorDeclaration
     * @param arg                    Void
     * @return Visitable
     */
    @Override
    public Visitable visit(ConstructorDeclaration constructorDeclaration, Void arg) {

        if (constructorDeclaration.getName().toString().equals(oldMethodName)) {
            // 修改方法名
            constructorDeclaration.setName(newMethodName);
            // 在方法体末尾添加新的语句
            BlockStmt methodBody = constructorDeclaration.getBody();
            if (ObjectUtils.isNotEmpty(methodBody) && statementList.size() != 0) {
                // 要添加的新语句
                for (StringBuilder generateViewBeanStringBuilder : statementList) {
                    // 将新语句解析为 Statement 对象
                    Statement statement = StaticJavaParser.parseStatement(generateViewBeanStringBuilder.toString());
                    methodBody.addStatement(statement); //添加语句
                }
            }
        }

        return super.visit(constructorDeclaration, arg);

    }
}
