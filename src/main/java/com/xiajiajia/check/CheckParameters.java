package com.xiajiajia.check;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.google.common.collect.Lists;
import com.xiajiajia.parseclass.GetAllMethod;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Created by xiajiajia on 8/30/2017.
 */
public class CheckParameters {

    private static final String ERROR_ANNOTATION = "Max";
    private static final String CHECK_TYPE = "String";

    private static List<Path> fileList = null;
    private static String p = "D:\\awork\\WEB-3488\\p2p_WEB-3488";
    private static final String db = "D:\\awork\\WEB-3488\\p2p_WEB-3488\\p2p-service\\src\\main\\java\\com\\haier\\hairy\\p2p\\db\\dao";
    private static final String enumPath = "D:\\awork\\WEB-3488\\p2p_WEB-3488\\p2p-service\\src\\main\\java\\com\\haier\\hairy\\p2p\\enums";

    private static List<String> ignoreTypeList = Lists.newArrayList("", "boolean", "Boolean", "long", "Byte", "byte[]", "double",
        "Double", "int", "BigDecimal", "Paging", "List", "String", "Date", "Integer", "Long", "Map", "Object", "\"Map<Object, Object>", "Map<String, "
            + "Object>",
        "Map<String, "
            + "String>");

    private static List<String> ignoreMethodList = Lists.newArrayList("updateByPrimaryKeySelective", "insertSelective", "insert", "updateByPrimaryKey", "deleteByPrimaryKey");

    static {
        try {
            fileList = GetFileList.getAllFile(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        // 1.遍历包下面所有的文件
        fileList.stream().filter(file -> {
            if (file.toAbsolutePath().toString().contains(db)) {
                return checkParameter(file);
            }
            return false;
        }).forEach(System.out::println);
    }

    private static boolean checkParameter(Path file) {

        InputStream in = null;
        try {
            in = Files.newInputStream(file);
            // 3. 获取所有的field声明
            GetAllMethod getMethodUtil = new GetAllMethod(in);
            Set<MethodDeclaration> methodDeclarations = getMethodUtil.getAllMethod();
            for (MethodDeclaration m : methodDeclarations) {
                String methodName = m.getName().asString();
                if (ignoreMethodList.contains(methodName)) {
                    continue;
                }
                List<Parameter> parameters = m.getParameters();
                boolean flag =  analyzeSpecicParameter(parameters);
                if(flag){
                    System.out.println(file.getFileName() + "-" + m.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean analyzeSpecicParameter(List<Parameter> parameters ) {
        for (Parameter parameter : parameters) {
            String typeName = parameter.getType().toString();

            if (ignoreTypeList.contains(typeName) || typeName.startsWith("List<")) {
                return false;
            } else {
               return   checkEnum(typeName);
            }
        }
        return false;

    }

    private static boolean checkEnum(String fileName) {
        Path path = fileList.stream().filter(p -> p.toAbsolutePath().toString().endsWith(fileName + ".java")).findFirst().orElse(null);
        if (path == null) {
            return false;
        }
        if(path.toAbsolutePath().toString().startsWith("enumPath")){
           return true;
        }
        InputStream in = null;
        try {
            in = Files.newInputStream(path);
            // 3. 获取所有的field声明
            GetAllMethod getMethodUtil = new GetAllMethod(in);
            CompilationUnit compilationUnit = getMethodUtil.getCu();
            List<ImportDeclaration> importDeclarationList =  compilationUnit.getImports();
            if( importDeclarationList==null){
              return false;
            }
            for(ImportDeclaration dec:importDeclarationList){
                if(dec.getName().toString().startsWith("com.haier.hairy.p2p.enums") ){
                    return true;
                }
            }
            //  System.out.println( compilationUnit.getTypes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //  System.out.println(path);
        return false;
    }
}
