package com.xiajiajia.check;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.metamodel.FieldDeclarationMetaModel;
import com.xiajiajia.parseclass.GetAllMethod;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

/**
 * Created by xiajiajia on 8/30/2017.
 */
public class CheckField {

    private static final String ERROR_ANNOTATION = "Max";
    private static final String CHECK_TYPE = "String";

    public static void main(String[] args) throws Exception {
        String p = "C:\\awork\\lanmao\\hry-lanmao-facade\\src\\main\\java\\com\\haier\\hairy\\lanmao\\request";
        // 1.遍历包下面所有的文件
        GetFileList.getAllFile(p).stream().filter(file -> {
            try {
                // 2. 检查每一个文件
              return   hasErrorAnnotation(Files.newInputStream(file));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }).forEach(System.out::println);
    }

    private static boolean hasErrorAnnotation(InputStream in) throws Exception {
        // 3. 获取所有的field声明
        GetAllMethod getMethodUtil = new GetAllMethod(in);
        Set<FieldDeclaration> allFieldEmement = getMethodUtil.getAllField();
        return allFieldEmement.stream().anyMatch(fieldDeclaration -> {
            return hasSpecificAnnotation(fieldDeclaration);
        });
    }

    private static boolean hasSpecificAnnotation(final FieldDeclaration fieldDeclaration) {
        FieldDeclarationMetaModel fieldDeclarationMetaModel = fieldDeclaration.getMetaModel();
        if (!CHECK_TYPE.equalsIgnoreCase(fieldDeclarationMetaModel.getTypeName())) {
            return false;
        }
        List<AnnotationExpr> annotationExprList = fieldDeclaration.getAnnotations();
        return annotationExprList.stream().anyMatch(name -> {
            return ERROR_ANNOTATION.equalsIgnoreCase(name.getName().toString());
        });
    }

}
