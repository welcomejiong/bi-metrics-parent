// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: metrics.proto

package org.corps.bi.metrics.protobuf;

public interface EconomyProtoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:metrics.EconomyProto)
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
   * 游戏中的币种。
   * 例如coin、gold、honor、qpoint等虚拟或可获取或可花费的货币或者类货币。
   * </pre>
   *
   * <code>string currency = 5;</code>
   */
  java.lang.String getCurrency();
  /**
   * <pre>
   **
   * 游戏中的币种。
   * 例如coin、gold、honor、qpoint等虚拟或可获取或可花费的货币或者类货币。
   * </pre>
   *
   * <code>string currency = 5;</code>
   */
  com.google.protobuf.ByteString
      getCurrencyBytes();

  /**
   * <pre>
   **
   * 用户花费的总数
   * </pre>
   *
   * <code>string amount = 6;</code>
   */
  java.lang.String getAmount();
  /**
   * <pre>
   **
   * 用户花费的总数
   * </pre>
   *
   * <code>string amount = 6;</code>
   */
  com.google.protobuf.ByteString
      getAmountBytes();

  /**
   * <pre>
   **
   * 用户购买物品的数量
   * </pre>
   *
   * <code>string value = 7;</code>
   */
  java.lang.String getValue();
  /**
   * <pre>
   **
   * 用户购买物品的数量
   * </pre>
   *
   * <code>string value = 7;</code>
   */
  com.google.protobuf.ByteString
      getValueBytes();

  /**
   * <code>string kingdom = 8;</code>
   */
  java.lang.String getKingdom();
  /**
   * <code>string kingdom = 8;</code>
   */
  com.google.protobuf.ByteString
      getKingdomBytes();

  /**
   * <code>string phylum = 9;</code>
   */
  java.lang.String getPhylum();
  /**
   * <code>string phylum = 9;</code>
   */
  com.google.protobuf.ByteString
      getPhylumBytes();

  /**
   * <code>string classfield = 10;</code>
   */
  java.lang.String getClassfield();
  /**
   * <code>string classfield = 10;</code>
   */
  com.google.protobuf.ByteString
      getClassfieldBytes();

  /**
   * <code>string family = 11;</code>
   */
  java.lang.String getFamily();
  /**
   * <code>string family = 11;</code>
   */
  com.google.protobuf.ByteString
      getFamilyBytes();

  /**
   * <code>string genus = 12;</code>
   */
  java.lang.String getGenus();
  /**
   * <code>string genus = 12;</code>
   */
  com.google.protobuf.ByteString
      getGenusBytes();

  /**
   * <code>string economyDate = 13;</code>
   */
  java.lang.String getEconomyDate();
  /**
   * <code>string economyDate = 13;</code>
   */
  com.google.protobuf.ByteString
      getEconomyDateBytes();

  /**
   * <code>string economyTime = 14;</code>
   */
  java.lang.String getEconomyTime();
  /**
   * <code>string economyTime = 14;</code>
   */
  com.google.protobuf.ByteString
      getEconomyTimeBytes();

  /**
   * <code>string extra = 15;</code>
   */
  java.lang.String getExtra();
  /**
   * <code>string extra = 15;</code>
   */
  com.google.protobuf.ByteString
      getExtraBytes();

  /**
   * <code>int32 version = 16;</code>
   */
  int getVersion();

  /**
   * <code>string udid = 17;</code>
   */
  java.lang.String getUdid();
  /**
   * <code>string udid = 17;</code>
   */
  com.google.protobuf.ByteString
      getUdidBytes();

  /**
   * <code>string roleid = 18;</code>
   */
  java.lang.String getRoleid();
  /**
   * <code>string roleid = 18;</code>
   */
  com.google.protobuf.ByteString
      getRoleidBytes();
}