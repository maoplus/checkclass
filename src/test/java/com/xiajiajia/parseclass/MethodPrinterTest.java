package com.xiajiajia.parseclass;

import org.junit.Test;

public class MethodPrinterTest {
  @Test
  public void mytest(){
      String requestExceptionType = "123- buyer";
      int index = requestExceptionType.lastIndexOf("- ");
      String aa = requestExceptionType.substring(index+2);
      System.out.println(aa);
  }
}
