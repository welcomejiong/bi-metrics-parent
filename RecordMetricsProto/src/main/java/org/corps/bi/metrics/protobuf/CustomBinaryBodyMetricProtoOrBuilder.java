// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: metrics.proto

package org.corps.bi.metrics.protobuf;

public interface CustomBinaryBodyMetricProtoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:metrics.CustomBinaryBodyMetricProto)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   *string gameId = 1;
   * </pre>
   *
   * <code>string clientId = 2;</code>
   */
  java.lang.String getClientId();
  /**
   * <pre>
   *string gameId = 1;
   * </pre>
   *
   * <code>string clientId = 2;</code>
   */
  com.google.protobuf.ByteString
      getClientIdBytes();

  /**
   * <pre>
   *string ds = 3;
   * </pre>
   *
   * <code>string userId = 4;</code>
   */
  java.lang.String getUserId();
  /**
   * <pre>
   *string ds = 3;
   * </pre>
   *
   * <code>string userId = 4;</code>
   */
  com.google.protobuf.ByteString
      getUserIdBytes();

  /**
   * <code>string customMetircName = 5;</code>
   */
  java.lang.String getCustomMetircName();
  /**
   * <code>string customMetircName = 5;</code>
   */
  com.google.protobuf.ByteString
      getCustomMetircNameBytes();

  /**
   * <code>string metricDate = 6;</code>
   */
  java.lang.String getMetricDate();
  /**
   * <code>string metricDate = 6;</code>
   */
  com.google.protobuf.ByteString
      getMetricDateBytes();

  /**
   * <code>string metricTime = 7;</code>
   */
  java.lang.String getMetricTime();
  /**
   * <code>string metricTime = 7;</code>
   */
  com.google.protobuf.ByteString
      getMetricTimeBytes();

  /**
   * <code>bytes body = 8;</code>
   */
  com.google.protobuf.ByteString getBody();

  /**
   * <code>string extra = 9;</code>
   */
  java.lang.String getExtra();
  /**
   * <code>string extra = 9;</code>
   */
  com.google.protobuf.ByteString
      getExtraBytes();

  /**
   * <code>int32 version = 10;</code>
   */
  int getVersion();

  /**
   * <code>string udid = 11;</code>
   */
  java.lang.String getUdid();
  /**
   * <code>string udid = 11;</code>
   */
  com.google.protobuf.ByteString
      getUdidBytes();

  /**
   * <code>string roleid = 12;</code>
   */
  java.lang.String getRoleid();
  /**
   * <code>string roleid = 12;</code>
   */
  com.google.protobuf.ByteString
      getRoleidBytes();
}
