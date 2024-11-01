package io.zipkin.brave.practice;

import brave.Span;
import brave.Tracer;
import brave.Tracing;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author:liuwenqing
 * @Date:2024/10/30 09:51
 * @Description:
 **/
public class LogTracingTest {

  public static void main(String[] args) {

    Tracing tracing = Tracing.newBuilder().localServiceName("LogTracingTest").build();

    Tracer tracer = tracing.tracer();

    Span span = tracer.nextSpan().name("span()").start();

    try (Tracer.SpanInScope scope = tracer.withSpanInScope(span)) {
      int rand = ThreadLocalRandom.current().nextInt(3);
      if (rand > 1) {
        System.out.println("rand > 1, rand: " + rand);
      } else {
        System.out.println("rand <= 1, rand: " + rand);
        int result = 1 / 0;
      }
    } catch (Exception e) {
      span.error(e);
    } finally {
      span.finish();
    }




  }

}
