package com.xiajiajia.parseclass;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class GetAllMethod extends VoidVisitorAdapter {
    private CompilationUnit        cu;
                                   
    private Set<MethodDeclaration> allEmement = new HashSet<>();
                                              
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
            cu = JavaParser.parse(Files.newInputStream(Paths.get(path)));
        } catch (ParseException | IOException e) {
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
        return allEmement;
    }
    
    @Override
    public void visit(MethodDeclaration n, Object arg) {
        allEmement.add(n);
    }
}
