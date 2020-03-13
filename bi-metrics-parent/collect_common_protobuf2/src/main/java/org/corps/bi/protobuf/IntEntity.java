package org.corps.bi.protobuf;

import org.corps.bi.protobuf.common.IntProto;

import com.google.protobuf.InvalidProtocolBufferException;

public class IntEntity implements ProtobufSerializable<IntEntity, IntProto> {
	
	private int value;

	public IntEntity() {
		super();
	}

	public IntEntity(int value) {
		super();
		this.value = value;
	}
	
	public IntEntity(byte[] bytes) {
		super();
		this.parseFrom(bytes);
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public IntProto copyTo() {
		IntProto.Builder builder=IntProto.newBuilder();
		builder.setValue(this.value);
		return builder.build();
	}

	@Override
	public byte[] toByteArray() {
		return this.copyTo().toByteArray();
	}

	@Override
	public IntEntity parseFrom(byte[] bytes) {
		try {
			IntProto proto = IntProto.parseFrom(bytes);
		    return this.copyFrom(proto);
		} catch (InvalidProtocolBufferException e) {
		    throw new IllegalArgumentException(e);
		}
	}

	@Override
	public IntEntity copyFrom(IntProto proto) {
		this.value=proto.getValue();
		return this;
	}


}
