package io.zipkin.brave.practice;

import brave.ScopedSpan;
import brave.Tracer;
import brave.Tracing;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author:liuwenqing
 * @Date:2024/10/30 09:42
 * @Description:
 **/
public class LogTracingScopeTest {

  public static void main(String[] args) {
    Tracing tracing = Tracing.newBuilder().localServiceName("test").build();
    Tracer tracer = tracing.tracer();
    ScopedSpan span = tracer.startScopedSpan("hello()");
    try {
      int rand = ThreadLocalRandom.current().nextInt(2);
      if (rand > 1) {
        System.out.println("rand > 1, rand:" + rand);
      } else {
        System.out.println("rand <= 1, rand:" + rand);
        int result = 1 / 0;
      }
    } catch (Exception e) {
      span.error(e);
    } finally {
      span.finish();
    }
    tracing.close();

  }

}
