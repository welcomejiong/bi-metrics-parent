// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tools.proto

package org.corps.bi.protobuf.common;

public final class ToolsProto {
  private ToolsProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  static com.google.protobuf.Descriptors.Descriptor
    internal_static_common_SimpleListProto_descriptor;
  static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_common_SimpleListProto_fieldAccessorTable;
  static com.google.protobuf.Descriptors.Descriptor
    internal_static_common_IntListProto_descriptor;
  static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_common_IntListProto_fieldAccessorTable;
  static com.google.protobuf.Descriptors.Descriptor
    internal_static_common_LongListProto_descriptor;
  static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_common_LongListProto_fieldAccessorTable;
  static com.google.protobuf.Descriptors.Descriptor
    internal_static_common_StringListProto_descriptor;
  static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_common_StringListProto_fieldAccessorTable;
  static com.google.protobuf.Descriptors.Descriptor
    internal_static_common_StorageKeyProto_descriptor;
  static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_common_StorageKeyProto_fieldAccessorTable;
  static com.google.protobuf.Descriptors.Descriptor
    internal_static_common_IntProto_descriptor;
  static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_common_IntProto_fieldAccessorTable;
  static com.google.protobuf.Descriptors.Descriptor
    internal_static_common_LongProto_descriptor;
  static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_common_LongProto_fieldAccessorTable;
  static com.google.protobuf.Descriptors.Descriptor
    internal_static_common_StringProto_descriptor;
  static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_common_StringProto_fieldAccessorTable;
  static com.google.protobuf.Descriptors.Descriptor
    internal_static_common_KVProto_descriptor;
  static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_common_KVProto_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\013tools.proto\022\006common\"#\n\017SimpleListProto" +
      "\022\020\n\010elements\030\001 \003(\014\" \n\014IntListProto\022\020\n\010el" +
      "ements\030\001 \003(\005\"!\n\rLongListProto\022\020\n\010element" +
      "s\030\001 \003(\003\"#\n\017StringListProto\022\020\n\010elements\030\001" +
      " \003(\t\"D\n\017StorageKeyProto\022\016\n\006userId\030\001 \002(\003\022" +
      "\020\n\010busiFlag\030\002 \002(\003\022\017\n\007keyFlag\030\003 \002(\003\"\031\n\010In" +
      "tProto\022\r\n\005value\030\001 \002(\005\"\032\n\tLongProto\022\r\n\005va" +
      "lue\030\001 \002(\003\"\034\n\013StringProto\022\r\n\005value\030\001 \002(\t\"" +
      "\037\n\007KVProto\022\t\n\001k\030\001 \002(\014\022\t\n\001v\030\002 \002(\014B,\n\034org." +
      "corps.bi.protobuf.commonB\nToolsProtoP\001"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_common_SimpleListProto_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_common_SimpleListProto_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_common_SimpleListProto_descriptor,
              new java.lang.String[] { "Elements", });
          internal_static_common_IntListProto_descriptor =
            getDescriptor().getMessageTypes().get(1);
          internal_static_common_IntListProto_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_common_IntListProto_descriptor,
              new java.lang.String[] { "Elements", });
          internal_static_common_LongListProto_descriptor =
            getDescriptor().getMessageTypes().get(2);
          internal_static_common_LongListProto_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_common_LongListProto_descriptor,
              new java.lang.String[] { "Elements", });
          internal_static_common_StringListProto_descriptor =
            getDescriptor().getMessageTypes().get(3);
          internal_static_common_StringListProto_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_common_StringListProto_descriptor,
              new java.lang.String[] { "Elements", });
          internal_static_common_StorageKeyProto_descriptor =
            getDescriptor().getMessageTypes().get(4);
          internal_static_common_StorageKeyProto_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_common_StorageKeyProto_descriptor,
              new java.lang.String[] { "UserId", "BusiFlag", "KeyFlag", });
          internal_static_common_IntProto_descriptor =
            getDescriptor().getMessageTypes().get(5);
          internal_static_common_IntProto_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_common_IntProto_descriptor,
              new java.lang.String[] { "Value", });
          internal_static_common_LongProto_descriptor =
            getDescriptor().getMessageTypes().get(6);
          internal_static_common_LongProto_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_common_LongProto_descriptor,
              new java.lang.String[] { "Value", });
          internal_static_common_StringProto_descriptor =
            getDescriptor().getMessageTypes().get(7);
          internal_static_common_StringProto_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_common_StringProto_descriptor,
              new java.lang.String[] { "Value", });
          internal_static_common_KVProto_descriptor =
            getDescriptor().getMessageTypes().get(8);
          internal_static_common_KVProto_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_common_KVProto_descriptor,
              new java.lang.String[] { "K", "V", });
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }

  // @@protoc_insertion_point(outer_class_scope)
}
