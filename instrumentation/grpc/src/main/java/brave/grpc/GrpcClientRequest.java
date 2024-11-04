/*
 * Copyright The OpenZipkin Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package brave.grpc;

import brave.rpc.RpcClientRequest;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.MethodDescriptor;
import java.util.Map;

/**
 * Allows access gRPC specific aspects of a client request during sampling and parsing.
 *
 * @see GrpcClientResponse
 * @see GrpcRequest for a parsing example
 * @since 5.12
 */
public final class GrpcClientRequest extends RpcClientRequest implements GrpcRequest {
  final Map<String, Key<String>> nameToKey;
  final MethodDescriptor<?, ?> methodDescriptor;
  final CallOptions callOptions;
  final ClientCall<?, ?> call;
  final Metadata headers;

  GrpcClientRequest(Map<String, Key<String>> nameToKey, MethodDescriptor<?, ?> methodDescriptor,
      CallOptions callOptions, ClientCall<?, ?> call, Metadata headers) {
    if (nameToKey == null) throw new NullPointerException("nameToKey == null");
    if (methodDescriptor == null) throw new NullPointerException("methodDescriptor == null");
    if (callOptions == null) throw new NullPointerException("callOptions == null");
    if (call == null) throw new NullPointerException("call == null");
    if (headers == null) throw new NullPointerException("headers == null");
    this.nameToKey = nameToKey;
    this.methodDescriptor = methodDescriptor;
    this.callOptions = callOptions;
    this.call = call;
    this.headers = headers;
  }

  /** Returns the {@link #call()} */
  @Override
  public Object unwrap() {
    return call;
  }

  @Override
  public String method() {
    return GrpcParser.method(methodDescriptor.getFullMethodName());
  }

  @Override
  public String service() {
    // MethodDescriptor.getServiceName() is not in our floor version: gRPC 1.2
    return GrpcParser.service(methodDescriptor.getFullMethodName());
  }

  /**
   * Returns the {@linkplain MethodDescriptor method descriptor} passed to {@link
   * ClientInterceptor#interceptCall}.
   *
   * @since 5.12
   */
  @Override
  public MethodDescriptor<?, ?> methodDescriptor() {
    return methodDescriptor;
  }

  /**
   * Returns the {@linkplain CallOptions call options} passed to {@link
   * ClientInterceptor#interceptCall}.
   *
   * @since 5.12
   */
  public CallOptions callOptions() {
    return callOptions;
  }

  /**
   * Returns the {@linkplain ClientCall client call} generated by {@link Channel#newCall} during
   * {@link ClientInterceptor#interceptCall}.
   *
   * @since 5.12
   */
  public ClientCall<?, ?> call() {
    return call;
  }

  /**
   * Returns the {@linkplain Metadata headers} passed to {@link ClientCall#start(ClientCall.Listener,
   * Metadata)}.
   *
   * @since 5.12
   */
  @Override
  public Metadata headers() {
    return headers;
  }

  @Override
  protected void propagationField(String keyName, String value) {

    if (keyName == null) throw new NullPointerException("keyName == null");
    if (value == null) throw new NullPointerException("value == null");
    Key<String> key = nameToKey.get(keyName);
    if (key == null) {
      assert false : "We currently don't support setting headers except propagation fields";
      return;
    }
    headers.removeAll(key);
    headers.put(key, value);
  }
}
