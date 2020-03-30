// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: metrics.proto

package org.corps.bi.metrics.protobuf;

public interface MilestoneProtoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:metrics.MilestoneProto)
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
   * <pre>
   **
   * 里程碑的信息
   * </pre>
   *
   * <code>string milestone = 5;</code>
   */
  java.lang.String getMilestone();
  /**
   * <pre>
   **
   * 里程碑的信息
   * </pre>
   *
   * <code>string milestone = 5;</code>
   */
  com.google.protobuf.ByteString
      getMilestoneBytes();

  /**
   * <pre>
   **
   * 里程碑的相对应的值
   * </pre>
   *
   * <code>string value = 6;</code>
   */
  java.lang.String getValue();
  /**
   * <pre>
   **
   * 里程碑的相对应的值
   * </pre>
   *
   * <code>string value = 6;</code>
   */
  com.google.protobuf.ByteString
      getValueBytes();

  /**
   * <pre>
   **
   * 记录的日期 yyyy-MM-dd
   * </pre>
   *
   * <code>string milestoneDate = 7;</code>
   */
  java.lang.String getMilestoneDate();
  /**
   * <pre>
   **
   * 记录的日期 yyyy-MM-dd
   * </pre>
   *
   * <code>string milestoneDate = 7;</code>
   */
  com.google.protobuf.ByteString
      getMilestoneDateBytes();

  /**
   * <pre>
   **
   * 记录的时间 HH:mm:ss
   * </pre>
   *
   * <code>string milestoneTime = 8;</code>
   */
  java.lang.String getMilestoneTime();
  /**
   * <pre>
   **
   * 记录的时间 HH:mm:ss
   * </pre>
   *
   * <code>string milestoneTime = 8;</code>
   */
  com.google.protobuf.ByteString
      getMilestoneTimeBytes();

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