package com.xiajiajia.check;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.xiajiajia.parseclass.GetAllMethod;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckWhileTrue {
    
    private static final String methodCallType = "com.github.javaparser.ast.expr.MethodCallExpr";
    
    private boolean checkMehod(String filepath) {
        GetAllMethod getMethod = new GetAllMethod(filepath);
        if (checkWhile(getMethod.getSourceCode())) {
            return true;
        }
        if (checkRecursive(getMethod.getAllMethod())) {
            return true;
        }
        getMethod.getAllMethod().clear();
        return false;
    }
    

    public void testMethod() {
        System.out
                .println(checkMehod("C:/code/project/RequestSerializer.java"));
    }
    
    private boolean prtChild(List<Node> list, String rootMethodName,
            int paramterCount) {
        boolean isRecursive = false;
        for (Node n : list) {
            String className = n.getClass().getName();
            if (className.equals(methodCallType)) {
                MethodCallExpr methodCall = (MethodCallExpr) n;
                if (methodCall.getParentNode().toString()
                        .contains("." + rootMethodName)) {
                    continue;
                }
                String methodNameTemp = methodCall.getName().asString();
                List<Expression> listArgs = methodCall.getArguments();
                final int argsCount = null == listArgs ? 0 : listArgs.size();
                if (methodNameTemp.equals(rootMethodName)
                        && argsCount == paramterCount) {
                    System.out.println(methodNameTemp);
                    return true;
                }
            }
            isRecursive = prtChild(n.getChildNodes(), rootMethodName,
                    paramterCount);
            if (isRecursive) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isRecursive(MethodDeclaration method) {
        final String methodName = method.getName().asString();
        List<Parameter> list = method.getParameters();
        final int paramterCount = null == list ? 0 : list.size();
        BlockStmt body = method.getBody().get();
        if (null == body) {
            return false;
        }
        if (null == body.getStatements()) {
            return false;
        }
        for (Statement state : body.getStatements()) {
            if (null == state) {
                continue;
            }
            if (state.toString().contains(methodName)) {
                if (prtChild(state.getChildNodes(), methodName,
                        paramterCount)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean checkRecursive(Set<MethodDeclaration> methods) {
        return methods.stream().anyMatch(method -> isRecursive(method));
    }
    
    private boolean checkWhile(String valueMessage) {
        String reg = "while\\s*\\(.*?\\)";
        Pattern pattern = Pattern.compile(reg, Pattern.DOTALL);
        Matcher m = pattern.matcher(valueMessage);
        boolean result = m.find();
        List<String> whileList = new ArrayList<>();
        while (result) {
            whileList.add(m.group());
            result = m.find();
        }
        return whileList.stream().filter(value -> !value.contains("hasNext"))
                .count() > 0;
    }
    
    private static final String End = ".java";
    
    private static class FileVisitorUtil extends SimpleFileVisitor<Path> {
        private Set<String> list = new HashSet<>();
        
        public Set<String> getList() {
            return list;
        }
        
        private void isFile(String dir) {
            if (dir.endsWith(End)) {
                list.add(dir);
            }
        }
        
        @Override
        public FileVisitResult preVisitDirectory(Path dir,
                BasicFileAttributes attrs) throws IOException {
            String filePath = dir.toString();
            String fileName = dir.getFileName().toString();
            if (filePath.contains("old") || filePath.contains(".svn")
                    || filePath.contains(".settings") || fileName.equals("xml")) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
            String filePath = file.toString();
            isFile(filePath);
            return FileVisitResult.CONTINUE;
        }
    }
    
    private String getFileNames(String fileName) {
        Path p = Paths.get(fileName);
        String tempName = p.getName(p.getNameCount() - 1).toString();
        return tempName.substring(0, tempName.lastIndexOf(End)) + "Test" + End;
    }
    
    private boolean checkHasTest(final Set<String> result,
            final String implClass) {
        return result.stream().anyMatch(
                fileName -> fileName.endsWith(getFileNames(implClass)));
    }
    

    public void checkAllMethod() throws Exception {
        FileVisitorUtil f = new FileVisitorUtil();
        // Files.walkFileTree(Paths.get("C:\\code\\project\\dcl-app"), f);
        Files.walkFileTree(Paths.get("C:/code/project"), f);
        final Set<String> result = f.getList();
        Predicate<String> checkWhileTrue = filepath -> checkMehod(filepath)
                && checkHasTest(result, filepath);
        result.stream().filter(checkWhileTrue).forEach(System.out::println);
    }
    
}