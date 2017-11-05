package com.xiajiajia.check;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilesUtil {
    
    public static void main(String[] args) throws IOException {
        getFileList("F:\\workspace\\10\\dcl-app\\src\\test\\java");
    }
    public static String getFixFileName(String temp) {
        temp.replaceAll("\\\\", "/");
//        if (!temp.endsWith("/")) {
//            temp += "/";
//        }
        return temp;
    }
    public static String getSvnNum(String str) {
        // String reg = "^M\\s*\\d*\\s*";
        String reg = "[^M|^A]\\s*\\d*\\s*";
        Pattern pattern = Pattern.compile(reg);
        Matcher m = pattern.matcher(str);
        boolean result = m.find();
        String targetResult = "";
        while (result) {
            String value = m.group();
            targetResult = value;
            result = m.find();
        }
        return targetResult;
    }
    public static  List<String>  getDifferent(String path) throws IOException {
        path = getFixFileName(path);
        List<String> lineList = new ArrayList<String>();
        String cmd = "svn diff " + path;
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(cmd);
        BufferedReader br = new BufferedReader(new InputStreamReader(
                process.getInputStream()));
        String line = null;
        while ((line = br.readLine()) != null) {
            lineList.add(line);
        }
        return lineList ;
    }
    
    public static  List<String> getFileList(String path) throws IOException {
        path = getFixFileName(path);
        List<String> pathList = new ArrayList<String>();
        String cmd = "svn status " + path;
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(cmd);
        BufferedReader br = new BufferedReader(new InputStreamReader(
                process.getInputStream()));
        String line = null;
        while ((line = br.readLine()) != null) {
            if (line.charAt(0) == 'M' || line.charAt(0) == 'A') {
                String svnPrdfix = getSvnNum(line);
                if (null != svnPrdfix) {
                    line = line.substring(svnPrdfix.length()).trim();
                    pathList.add(line);
                }
            }
        }
        return pathList;
    }
}