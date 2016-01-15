package com.xiajiajia.parseclass;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MethodPrinterTest {
  @Test
  public void mytest(){
      String requestExceptionType = "123- buyer";
      int index = requestExceptionType.lastIndexOf("- ");
      String aa = requestExceptionType.substring(index+2);
      System.out.println(aa);
  }
}
