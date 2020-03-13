package org.corps.bi.protobuf;

import org.corps.bi.protobuf.common.StringProto;

import com.google.protobuf.InvalidProtocolBufferException;

public class StringEntity implements ProtobufSerializable<StringEntity, StringProto> {
	
	private String value;

	public StringEntity() {
		super();
	}

	public StringEntity(String value) {
		super();
		this.value = value;
	}
	
	public StringEntity(byte[] bytes) {
		super();
		this.parseFrom(bytes);
	}
	
	

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public StringProto copyTo() {
		StringProto.Builder builder=StringProto.newBuilder();
		builder.setValue(this.value);
		return builder.build();
	}

	@Override
	public byte[] toByteArray() {
		return this.copyTo().toByteArray();
	}

	@Override
	public StringEntity parseFrom(byte[] bytes) {
		try {
			StringProto proto = StringProto.parseFrom(bytes);
		    return this.copyFrom(proto);
		} catch (InvalidProtocolBufferException e) {
		    throw new IllegalArgumentException(e);
		}
	}

	@Override
	public StringEntity copyFrom(StringProto proto) {
		this.value=proto.getValue();
		return this;
	}


}
