package com.xiajiajia.check;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.xiajiajia.parseclass.GetAllMethod;
import java.io.InputStream;
import java.nio.file.Files;

import java.util.List;

/**
 * Created by xiajiajia on 8/30/2017.
 */
public class CheckReponse {

    public static void main(String[] args) throws Exception {

        // 1.遍历包下面所有的文件
        GetFileList.getAllFile("D:\\awork\\WEB-3488\\p2p_WEB-3488\\p2p-facade").stream().filter(path -> {
            return !Files.isDirectory(path) && path.toAbsolutePath().toString().endsWith(".java");
        }).forEach(file -> {
            try {
                // 2. 检查每一个文件
                prtmethod(Files.newInputStream(file));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void prtmethod(InputStream in) throws Exception {
        // 3. 获取所有的方法声明
        GetAllMethod getMethodUtil = new GetAllMethod(in);
        for (MethodDeclaration m : getMethodUtil.getAllMethod()) {
            String response = m.getType().asString();
            // 4. 取得方法体上所有的注解
            List<AnnotationExpr> list = m.getAnnotations();
            for (AnnotationExpr annotation : list) {
                // 5. 取得 swagger ApiOperation注解
                if ("ApiOperation".equalsIgnoreCase(annotation.getName().toString())) {
                    List<Node> childNode = annotation.getChildNodes();
                    // 6. 继续遍历注解的子节点
                    for (Node n : childNode) {
                        if (n instanceof MemberValuePair) {
                            MemberValuePair memberValuePair = (MemberValuePair) n;
                            // 7. 获取 swagger 的response 名字
                            if ("response".equalsIgnoreCase(memberValuePair.getName().asString())) {
                                // 8. 比对方法体的response跟swagger 的response是否一致
                                if (!memberValuePair.getValue().toString().contains(response)) {
                                    // 9. 打印出声明不一致的swagger注解
                                    System.out.println(
                                        "method = " + m.getName() + "; response = " + response + " ; ApiOperation ="
                                            + memberValuePair.getValue());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
