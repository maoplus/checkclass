package com.xiajiajia.parseclass;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;
import org.stringtemplate.v4.ST;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.common.base.Strings;

public class MethodUtil {
    private static String                  className;
    public final static String            filePathID                     = "filePath";
    private final static String            unitTestTemplateID             = "unitTestTemplate";
    private final static String            validatorMethodID              = "validatorMethod";
    private final static String            validatorMethodWithExceptionID = "validatorMethodWithException";
    private static ApplicationContext      ctx                            = null;
    private final static String            regException                   = "throw\\s*new\\s*\\w+\\s*\\(.*?\\)?;";
    private final static String            regaddResponse                 = "addResponse\\(.*?\\)?;";
    private final static String            testAnnotation                 = "@Test";
    private final static String            PUBLIC_CLASS                   = "public class";
    private static List<ImportDeclaration> importsLists;
    private static List<String>            importsClasses                 = new ArrayList<>();
    private static String                  resultStr;
    private static final int               VOID_LEN                       = "void"
            .length();
    private static StringBuilder           fields                         = new StringBuilder();
                                                                          
    private MethodUtil() {
    }
    
    static {
        ctx = new ClassPathXmlApplicationContext("dao.xml");
    }
    
    public static String getTemplate(String beanName) {
        return ctx.getBean(beanName, String.class).trim();
    }
    
    public static InputStream getFileInputStream()
            throws BeansException, IOException {
        return Files.newInputStream(
                Paths.get(ctx.getBean(filePathID, String.class).trim()));
    }
    
    public static Path getFilePath() throws BeansException, IOException {
        return Paths.get(ctx.getBean(filePathID, String.class).trim());
    }
    
    
    public static String getResut(Path path) {
        try {
            if (path.toString().endsWith("Test.java")) {
                return MethodUtil.printUnitMethod(Files.readAllLines(path));
            }
            return MethodUtil.printMethod(Files.newInputStream(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    public static String printUnitMethod(List<String> list) throws Exception {
        final int len = list.size();
        StringBuilder resultAll = new StringBuilder();
        final String classNameTemp = list.stream()
                .filter(v -> v.contains(PUBLIC_CLASS)).map(name -> {
                    return name
                            .substring(
                                    name.lastIndexOf(PUBLIC_CLASS)
                                            + PUBLIC_CLASS.length(),
                                    name.indexOf("{"))
                            .trim() + " ";
                            
                }).findFirst().get();
        int count = 0;
        for (int i = 0; i < len; i++) {
            if (list.get(i).contains(testAnnotation)) {
                count++;
              // System.out.println(classNameTemp);
                if (i + 1 < len) {
                    String uniteMethod = list.get(i + 1);
                    resultAll.append("\n")
                            .append(uniteMethod.substring(
                                    uniteMethod.lastIndexOf("void") + VOID_LEN,
                                    uniteMethod.indexOf("(")).trim());
                                    
                }
                
            }
        }
        System.out.println(resultAll.toString());
        return count + " tests";
    }
    
    private static final List<String> unneedFieldType = new ArrayList<String>(
            Arrays.asList("Logger", "String"));
            
    public static void mockField(CompilationUnit cu) {
        List<FieldDeclaration> fieldDeclarationList = getAllVariables(cu);
        for (FieldDeclaration myType : fieldDeclarationList) {
            List<VariableDeclarator> myFields = myType.getVariables();
            String fileType = myType.getType().toString();
            if (!unneedFieldType.contains(fileType)) {
                fields.append("@Mock\n");
                fields.append(fileType + "   " + myFields.get(0) + ";\n");
            }
        }
    }
    
    public static String getField(FieldDeclaration myType) {
        StringBuilder myfields = new StringBuilder();
        List<VariableDeclarator> myFields = myType.getVariables();
        String fileType = myType.getType().toString();
        String modifierStr = Modifier.toString(myType.getModifiers());
        if (!myType.toString().contains("=")) {
            myfields.append(modifierStr + "   " + fileType + "  "
                    + myFields.get(0) + ";\n");
        }
        return myfields.toString();
    }
    
    public static List<FieldDeclaration> getAllVariables(CompilationUnit cu) {
        List<TypeDeclaration> f_vars = cu.getTypes();
        List<FieldDeclaration> allFieldDeclaration = new ArrayList<>();
        for (TypeDeclaration type : f_vars) {
            List<BodyDeclaration> members = type.getMembers();
            for (BodyDeclaration member : members) {
                if (member instanceof FieldDeclaration) {
                    FieldDeclaration myType = (FieldDeclaration) member;
                    allFieldDeclaration.add(myType);
                }
            }
        }
        return allFieldDeclaration;
    }
    
    public static String getClassName(CompilationUnit cu) {
        List<TypeDeclaration> typeList = cu.getTypes();
        if (null != typeList && !typeList.isEmpty()) {
            return typeList.stream().findFirst().get().getName();
        }
        return "CanNotFindClassName";
    }
    
    public static void createBuilderClass(CompilationUnit cu) {
        
        String importsList = getImports(cu).stream().reduce((a, b) -> a + b)
                .get();
        List<FieldDeclaration> fieldList = getAllVariables(cu);
        String className = getClassName(cu);
        String fieldsAll = fieldList.stream().map(field -> {
            return getField(field);
        }).reduce((a, b) -> a + b).get();
        ST st = new ST(getTemplate("beanBuilder"));
        st.add("import", importsList);
        st.add("IBuilder", className + "Builder");
        st.add("Iclasstest", className);
        st.add("IFieldsAll", fieldsAll);
        st.add("IclasstestLowerCase", firstLetterLowercase(className));
        st.add("IBuilderMethod", createBuilderMethod(fieldList,
                className + "Builder", firstLetterLowercase(className)));
        System.out.println(st.render());
    }
    
    public static String createBuilderMethod(List<FieldDeclaration> fieldList,
            String ibuilder, String classtestLowerCase) {
        StringBuilder builderMethods = new StringBuilder();
        for (FieldDeclaration fields : fieldList) {
            if (fields.toString().contains("=")) {
                continue;
            }
            List<VariableDeclarator> myFields = fields.getVariables();
            String fileType = fields.getType().toString();
            String fieldName = myFields.get(0).toString();
            ST st = new ST(getTemplate("builderMethod"));
            st.add("IBuilder", ibuilder);
            st.add("IField", fileType);
            st.add("IclasstestLowerCase", classtestLowerCase);
            st.add("IFieldLowerCase", firstLetterLowercase(fieldName));
            st.add("IFieldUpperCase", capitalizationName(fieldName));
            builderMethods.append(st.render().toString()).append("\n");
        }
        return builderMethods.toString();
    }
    
    public static List<String> getImports(CompilationUnit cu) {
        List<TypeDeclaration> typeList = cu.getTypes();
        importsLists = cu.getImports();
        if (null != importsLists) {
            importsClasses = importsLists.stream().map(v -> {
                return "import " + v.getName().toString() + ";\n";
            }).collect(Collectors.toList());
        }
        PackageDeclaration packagesPaths = cu.getPackage();
        importsClasses.add("import " + packagesPaths.getName().toString() + "."
                + getClassName(cu) + ";\n");
        return importsClasses;
    }
    
    public static String printMethod(InputStream in) throws Exception {
        GetAllMethod getMethodUtil = new GetAllMethod(in);
        CompilationUnit cu = getMethodUtil.getCompilationUnit();
        className = getClassName(cu);
        mockField(cu);
        getImports(cu);
        methodVisitor(getMethodUtil);
        return resultStr;
    }
    
    private static String capitalizationName(String name) {
        char[] cs = name.toCharArray();
        if ('a' <= cs[0] && cs[0] <= 'z') {
            cs[0] -= 32;
        }
        else {
            return name;
        }
        return String.valueOf(cs);
    }
    
    private static String firstLetterLowercase(String name) {
        char[] cs = name.toCharArray();
        if ('A' <= cs[0] && cs[0] <= 'Z') {
            cs[0] += 32;
        }
        else {
            return name;
        }
        return String.valueOf(cs);
    }
    
    @Test
    public void printTemp() {
        char a = 'x';
        System.out.println(a <= 'z');
    }
    
    private static String generateMethodNameByErrorMessage(
            String errorMessage) {
        String reg = "\\\".+\\\"";
        Pattern pattern = Pattern.compile(reg, Pattern.DOTALL);
        Matcher m = pattern.matcher(errorMessage);
        boolean result = m.find();
        while (result) {
            String value = m.group();
            if (StringUtils.hasText(value)) {
                String methodName = Arrays.asList(value.split("[^a-zA-Z]"))
                        .stream().filter(word -> word.matches("\\w{2,}"))
                        .map(word -> capitalizationName(word))
                        .reduce("", (a, b) -> a + b);
                return "shouldReturn" + methodName;
            }
            result = m.find();
        }
        return "";
    }
    
    private static String getErrorMessage(String x, String reg)
            throws IOException, URISyntaxException {
        Pattern pattern = Pattern.compile(reg, Pattern.DOTALL);
        Matcher m = pattern.matcher(x);
        boolean result = m.find();
        if (result) {
            String value = m.group();
            ST st;
            if (reg.equals(regaddResponse)) {
                st = new ST(getTemplate(validatorMethodID));
            }
            else {
                st = new ST(getTemplate(validatorMethodWithExceptionID));
            }
            st.add("methodName", generateMethodNameByErrorMessage(value));
            st.add("comment", value);
            return st.render() + "\n";
        }
        return "";
    }
    
    private static String getTestMethod(String x)
            throws IOException, URISyntaxException {
        x = x.replace("n't", "nnot");
        StringBuilder ivalidatorTestMethod = new StringBuilder("");
        String reg = "if\\s*\\(.*?\\)\\s*\\{.*?(throw new|addResponse).*?;\\s*\\}";
        Pattern pattern = Pattern.compile(reg, Pattern.DOTALL);
        Matcher m = pattern.matcher(x);
        boolean result = m.find();
        while (result) {
            String value = m.group();
            if (StringUtils.hasText(value)) {
                ivalidatorTestMethod.append("\n");
                ivalidatorTestMethod
                        .append(getErrorMessage(value, regException));
                ivalidatorTestMethod
                        .append(getErrorMessage(value, regaddResponse));
                ivalidatorTestMethod.append("\n");
            }
            result = m.find();
        }
        return ivalidatorTestMethod.toString();
    }
    
    private static void getTemplate(boolean isComposer, MethodDeclaration n) {
        ST template = null;
        List<Parameter> parameterList = n.getParameters();
        String requestType = n.getType().toString();
        try {
            template = new ST(getTemplate(unitTestTemplateID));
            template.add("isComposer", isComposer);
            if (isComposer) {
                // template.add("IEXTENDS", composerParentClass);
                template.add("IrequestType", requestType);
            }
            else {
                try {
                    template.add("IvalidatorTestMethod",
                            getTestMethod(n.getParentNode().toString()));
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
                // template.add("IEXTENDS", validateParentClass);
            }
            try {
                template.add("Irequest",
                        parameterList.get(0).getType().toString());
                template.add("Icontext",
                        parameterList.get(1).getType().toString());
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            template.add("IFields", fields.toString());
            template.add("Iclasstest", className + "Test");
            template.add("Iclass", className);
            template.add("import",
                    importsClasses.stream().sorted((a, b) -> a.compareTo(b))
                            .filter(value -> !Strings.isNullOrEmpty(value))
                            .reduce("", (a, b) -> a + b));
        } catch (Exception e) {
            e.printStackTrace();
        }
        resultStr = "\n--------------------------------------------------------------\n"
                + template.render()
                + "\n--------------------------------------------------------------\n";
        // System.out.println(template.render());
    }
    
    static Predicate<MethodDeclaration> isCompose  = method -> method.getName()
            .equals("compose");
            
    static Predicate<MethodDeclaration> isValidate = method -> method.getName()
            .endsWith("_validate");
            
    private static void methodVisitor(GetAllMethod get) {
        Set<MethodDeclaration> methodSet = get.getAllMethod();
        for (Object o : methodSet) {
            if (o instanceof MethodDeclaration) {
                MethodDeclaration n = (MethodDeclaration) o;
                if (isCompose.test(n)) {
                    getTemplate(true, n);
                }
                else if (isValidate.test(n)) {
                    getTemplate(false, n);
                }
            }
        }
    }
    
}