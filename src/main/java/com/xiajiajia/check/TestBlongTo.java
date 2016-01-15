package com.xiajiajia.check;

import static com.xiajiajia.parseclass.MethodUtil.filePathID;
import static com.xiajiajia.parseclass.MethodUtil.getResut;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.xiajiajia.parseclass.MethodUtil;

public class TestBlongTo {
    private static Map<String, String> testMap = new HashMap<>();
    static {
        testMap.put("ASL-Integration-Test",
                "/activestream-loans/integration-test");
        testMap.put("ASL-UnitTest-Bridge",
                "/activestream-loans/bridge/impl/src/test");
        testMap.put("ASL-UnitTest-RequestHandler",
                "/activestream-loans/request/src/test");
        testMap.put("ASL-UnitTest-Serializer",
                "activestream-loans/serializer/src/test");
        testMap.put("ASL-UnitTest-Service", "dcl-app/src/test");
        testMap.put("Connectivity-App-UnitTest", "connectivity/test");
        testMap.put("Workflow-UnitTest-Rules", "loans-workflow/impl/src/test");
        testMap.put("WF-SmokeTest",
                "loans-workflow/workflow-integration-test/wf-smoke-test");
    }
    
    public static String getPath() {
        return MethodUtil.getTemplate(filePathID).replaceAll("\\\\", "/");
    }
    
    public static void getTestModule(String javaClass) {
        Path filePath = Paths.get(javaClass);
        System.out.println( "cd /d "+javaClass.substring(0,javaClass.indexOf("src/")));
        System.out.println("mvn test -Dtest="+filePath.getFileName().toString().replace(".java", ""));
        System.out.print("h4. Create unit test "+filePath.getFileName()+" for ");
        testMap.entrySet().stream()
                .filter(path -> javaClass.contains(path.getValue())).limit(1)
                .forEach(v -> {
                    System.out.println(v.getKey());
                });
        System.out.println("Path: " +javaClass );
        System.out.println("Method:");
        System.out.println("{code}");
        System.out.println(getResut(filePath));
        System.out.println("{code}");
        System.out.println("Result:");
        System.out.println("{code}");
        System.out.println("{code}");
    }
    
    public static void main(String[] args) throws IOException {
        
        //Files.readAllLines(Paths.get("f:/t/a.txt")).stream().map(v->"com.quartetfs.workflow.rules.test."+v+"Test").forEach(System.out::println);;
        
         getTestModule(getPath());
    }
    
}
