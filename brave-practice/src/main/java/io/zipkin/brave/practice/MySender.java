package io.zipkin.brave.practice;

import brave.ScopedSpan;
import brave.Tag;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.TraceContext;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.okhttp3.OkHttpSender;

/**
 * 定义发送器
 */
public class MySender {

  public static void main(String[] args) {

    /**
     * 创建发送器
     */
    OkHttpSender sender = OkHttpSender.create("http://127.0.0.1:9411/api/v2/spans");

    AsyncZipkinSpanHandler spanHandler = AsyncZipkinSpanHandler.newBuilder(sender).build();

    Tracing tracing = Tracing.newBuilder()
      .localServiceName("my-service")
      .addSpanHandler(spanHandler).build();

    Tracer tracer = tracing.tracer();
    ScopedSpan span = tracer.startScopedSpan("system.out");
    try {
      System.out.println("tracing practice");
      Tag<String> tag = new Tag<String>("tag") {

        @Override
        protected String parseValue(String input, TraceContext context) {
          return input;
        }
      };
      tag.tag("tag test", span);
    } catch (Exception e) {
      span.error(e);
    } finally {
      span.finish();
    }
    tracing.close();
    spanHandler.close();
    sender.close();

  }

}
