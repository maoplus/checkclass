package com.xiajiajia.classbuilder;

import static com.xiajiajia.parseclass.MethodUtil.createBuilderClass;
import static com.xiajiajia.parseclass.MethodUtil.getFileInputStream;

import com.github.javaparser.ast.CompilationUnit;
import com.xiajiajia.parseclass.GetAllMethod;

public class GenerteBuilder {
    public static void main(String[] args) throws Exception {
        
        GetAllMethod getMethodUtil = new GetAllMethod(getFileInputStream());
        CompilationUnit cu = getMethodUtil.getCompilationUnit();
        createBuilderClass(cu);
    }
   
    
}
