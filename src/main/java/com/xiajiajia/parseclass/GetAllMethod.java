package com.xiajiajia.parseclass;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class GetAllMethod extends VoidVisitorAdapter {

    private CompilationUnit cu;

    public CompilationUnit getCu() {
        return cu;
    }

    public void setCu(CompilationUnit cu) {
        this.cu = cu;
    }

    private Set<MethodDeclaration> allMethodEmement = new HashSet<>();
    private Set<FieldDeclaration> allFieldEmement = new HashSet<>();

    public GetAllMethod(InputStream in) throws Exception
    {
        try {
            cu = JavaParser.parse(in);
        } finally {
            in.close();
        }
    }

    public GetAllMethod(String path) {
        try {

        } catch ( Exception e) {
            e.printStackTrace();
        }
    }

    public CompilationUnit getCompilationUnit() {
        return cu;
    }

    public String getSourceCode() {
        return cu.toString();
    }

    public Set<MethodDeclaration> getAllMethod() {
        this.visit(cu, null);
        return allMethodEmement;
    }

    public Set<FieldDeclaration> getAllField() {
        this.visit(cu, null);
        return allFieldEmement;
    }

    @Override
    public void visit(MethodDeclaration n, Object arg) {
       // System.out.println("visit = "+n.getName());
        allMethodEmement.add(n);
    }

    @Override
    public void visit(FieldDeclaration n, Object arg) {
        allFieldEmement.add(n);
    }

}
