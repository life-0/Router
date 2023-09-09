package com.life.visitor;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class JavaParserClassVisitor extends VoidVisitorAdapter<Void> {
    private String oldClassName;
    private String newClassName;

    public JavaParserClassVisitor(String oldClassName, String newClassName) {
        this.oldClassName = oldClassName;
        this.newClassName = newClassName;
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration cid, Void arg) {
        super.visit(cid, arg);

        // 修改类名
        if (cid.getNameAsString().equals(oldClassName)) {
            // 修改类名为新名称
            cid.setName(newClassName);
        }
    }


}
