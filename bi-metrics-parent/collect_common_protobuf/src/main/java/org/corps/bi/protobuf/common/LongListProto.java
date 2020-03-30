// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tools.proto

package org.corps.bi.protobuf.common;

/**
 * Protobuf type {@code common.LongListProto}
 */
public  final class LongListProto extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:common.LongListProto)
    LongListProtoOrBuilder {
private static final long serialVersionUID = 0L;
  // Use LongListProto.newBuilder() to construct.
  private LongListProto(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private LongListProto() {
    elements_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private LongListProto(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    int mutable_bitField0_ = 0;
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          default: {
            if (!parseUnknownFieldProto3(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
          case 8: {
            if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
              elements_ = new java.util.ArrayList<java.lang.Long>();
              mutable_bitField0_ |= 0x00000001;
            }
            elements_.add(input.readInt64());
            break;
          }
          case 10: {
            int length = input.readRawVarint32();
            int limit = input.pushLimit(length);
            if (!((mutable_bitField0_ & 0x00000001) == 0x00000001) && input.getBytesUntilLimit() > 0) {
              elements_ = new java.util.ArrayList<java.lang.Long>();
              mutable_bitField0_ |= 0x00000001;
            }
            while (input.getBytesUntilLimit() > 0) {
              elements_.add(input.readInt64());
            }
            input.popLimit(limit);
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      if (((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
        elements_ = java.util.Collections.unmodifiableList(elements_);
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return org.corps.bi.protobuf.common.ToolsProto.internal_static_common_LongListProto_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return org.corps.bi.protobuf.common.ToolsProto.internal_static_common_LongListProto_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            org.corps.bi.protobuf.common.LongListProto.class, org.corps.bi.protobuf.common.LongListProto.Builder.class);
  }

  public static final int ELEMENTS_FIELD_NUMBER = 1;
  private java.util.List<java.lang.Long> elements_;
  /**
   * <code>repeated int64 elements = 1;</code>
   */
  public java.util.List<java.lang.Long>
      getElementsList() {
    return elements_;
  }
  /**
   * <code>repeated int64 elements = 1;</code>
   */
  public int getElementsCount() {
    return elements_.size();
  }
  /**
   * <code>repeated int64 elements = 1;</code>
   */
  public long getElements(int index) {
    return elements_.get(index);
  }
  private int elementsMemoizedSerializedSize = -1;

  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    getSerializedSize();
    if (getElementsList().size() > 0) {
      output.writeUInt32NoTag(10);
      output.writeUInt32NoTag(elementsMemoizedSerializedSize);
    }
    for (int i = 0; i < elements_.size(); i++) {
      output.writeInt64NoTag(elements_.get(i));
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    {
      int dataSize = 0;
      for (int i = 0; i < elements_.size(); i++) {
        dataSize += com.google.protobuf.CodedOutputStream
          .computeInt64SizeNoTag(elements_.get(i));
      }
      size += dataSize;
      if (!getElementsList().isEmpty()) {
        size += 1;
        size += com.google.protobuf.CodedOutputStream
            .computeInt32SizeNoTag(dataSize);
      }
      elementsMemoizedSerializedSize = dataSize;
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof org.corps.bi.protobuf.common.LongListProto)) {
      return super.equals(obj);
    }
    org.corps.bi.protobuf.common.LongListProto other = (org.corps.bi.protobuf.common.LongListProto) obj;

    boolean result = true;
    result = result && getElementsList()
        .equals(other.getElementsList());
    result = result && unknownFields.equals(other.unknownFields);
    return result;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    if (getElementsCount() > 0) {
      hash = (37 * hash) + ELEMENTS_FIELD_NUMBER;
      hash = (53 * hash) + getElementsList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static org.corps.bi.protobuf.common.LongListProto parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.corps.bi.protobuf.common.LongListProto parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.corps.bi.protobuf.common.LongListProto parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.corps.bi.protobuf.common.LongListProto parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.corps.bi.protobuf.common.LongListProto parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.corps.bi.protobuf.common.LongListProto parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.corps.bi.protobuf.common.LongListProto parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.corps.bi.protobuf.common.LongListProto parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.corps.bi.protobuf.common.LongListProto parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static org.corps.bi.protobuf.common.LongListProto parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.corps.bi.protobuf.common.LongListProto parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.corps.bi.protobuf.common.LongListProto parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(org.corps.bi.protobuf.common.LongListProto prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code common.LongListProto}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:common.LongListProto)
      org.corps.bi.protobuf.common.LongListProtoOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.corps.bi.protobuf.common.ToolsProto.internal_static_common_LongListProto_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.corps.bi.protobuf.common.ToolsProto.internal_static_common_LongListProto_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.corps.bi.protobuf.common.LongListProto.class, org.corps.bi.protobuf.common.LongListProto.Builder.class);
    }

    // Construct using org.corps.bi.protobuf.common.LongListProto.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    public Builder clear() {
      super.clear();
      elements_ = java.util.Collections.emptyList();
      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return org.corps.bi.protobuf.common.ToolsProto.internal_static_common_LongListProto_descriptor;
    }

    public org.corps.bi.protobuf.common.LongListProto getDefaultInstanceForType() {
      return org.corps.bi.protobuf.common.LongListProto.getDefaultInstance();
    }

    public org.corps.bi.protobuf.common.LongListProto build() {
      org.corps.bi.protobuf.common.LongListProto result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public org.corps.bi.protobuf.common.LongListProto buildPartial() {
      org.corps.bi.protobuf.common.LongListProto result = new org.corps.bi.protobuf.common.LongListProto(this);
      int from_bitField0_ = bitField0_;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        elements_ = java.util.Collections.unmodifiableList(elements_);
        bitField0_ = (bitField0_ & ~0x00000001);
      }
      result.elements_ = elements_;
      onBuilt();
      return result;
    }

    public Builder clone() {
      return (Builder) super.clone();
    }
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return (Builder) super.setField(field, value);
    }
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return (Builder) super.clearField(field);
    }
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return (Builder) super.clearOneof(oneof);
    }
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return (Builder) super.setRepeatedField(field, index, value);
    }
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return (Builder) super.addRepeatedField(field, value);
    }
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof org.corps.bi.protobuf.common.LongListProto) {
        return mergeFrom((org.corps.bi.protobuf.common.LongListProto)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(org.corps.bi.protobuf.common.LongListProto other) {
      if (other == org.corps.bi.protobuf.common.LongListProto.getDefaultInstance()) return this;
      if (!other.elements_.isEmpty()) {
        if (elements_.isEmpty()) {
          elements_ = other.elements_;
          bitField0_ = (bitField0_ & ~0x00000001);
        } else {
          ensureElementsIsMutable();
          elements_.addAll(other.elements_);
        }
        onChanged();
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    public final boolean isInitialized() {
      return true;
    }

    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      org.corps.bi.protobuf.common.LongListProto parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (org.corps.bi.protobuf.common.LongListProto) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private java.util.List<java.lang.Long> elements_ = java.util.Collections.emptyList();
    private void ensureElementsIsMutable() {
      if (!((bitField0_ & 0x00000001) == 0x00000001)) {
        elements_ = new java.util.ArrayList<java.lang.Long>(elements_);
        bitField0_ |= 0x00000001;
       }
    }
    /**
     * <code>repeated int64 elements = 1;</code>
     */
    public java.util.List<java.lang.Long>
        getElementsList() {
      return java.util.Collections.unmodifiableList(elements_);
    }
    /**
     * <code>repeated int64 elements = 1;</code>
     */
    public int getElementsCount() {
      return elements_.size();
    }
    /**
     * <code>repeated int64 elements = 1;</code>
     */
    public long getElements(int index) {
      return elements_.get(index);
    }
    /**
     * <code>repeated int64 elements = 1;</code>
     */
    public Builder setElements(
        int index, long value) {
      ensureElementsIsMutable();
      elements_.set(index, value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated int64 elements = 1;</code>
     */
    public Builder addElements(long value) {
      ensureElementsIsMutable();
      elements_.add(value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated int64 elements = 1;</code>
     */
    public Builder addAllElements(
        java.lang.Iterable<? extends java.lang.Long> values) {
      ensureElementsIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(
          values, elements_);
      onChanged();
      return this;
    }
    /**
     * <code>repeated int64 elements = 1;</code>
     */
    public Builder clearElements() {
      elements_ = java.util.Collections.emptyList();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:common.LongListProto)
  }

  // @@protoc_insertion_point(class_scope:common.LongListProto)
  private static final org.corps.bi.protobuf.common.LongListProto DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new org.corps.bi.protobuf.common.LongListProto();
  }

  public static org.corps.bi.protobuf.common.LongListProto getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<LongListProto>
      PARSER = new com.google.protobuf.AbstractParser<LongListProto>() {
    public LongListProto parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new LongListProto(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<LongListProto> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<LongListProto> getParserForType() {
    return PARSER;
  }

  public org.corps.bi.protobuf.common.LongListProto getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
