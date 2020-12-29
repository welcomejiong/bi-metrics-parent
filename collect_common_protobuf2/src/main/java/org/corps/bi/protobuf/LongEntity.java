package org.corps.bi.protobuf;

import org.corps.bi.protobuf.common.LongProto;

import com.google.protobuf.InvalidProtocolBufferException;

public class LongEntity implements ProtobufSerializable<LongEntity, LongProto> {
	
	private long value;

	public LongEntity() {
		super();
	}

	public LongEntity(long value) {
		super();
		this.value = value;
	}
	
	public LongEntity(byte[] bytes) {
		super();
		this.parseFrom(bytes);
	}
	
	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	@Override
	public LongProto copyTo() {
		LongProto.Builder builder=LongProto.newBuilder();
		builder.setValue(this.value);
		return builder.build();
	}

	@Override
	public byte[] toByteArray() {
		return this.copyTo().toByteArray();
	}

	@Override
	public LongEntity parseFrom(byte[] bytes) {
		try {
			LongProto proto = LongProto.parseFrom(bytes);
		    return this.copyFrom(proto);
		} catch (InvalidProtocolBufferException e) {
		    throw new IllegalArgumentException(e);
		}
	}

	@Override
	public LongEntity copyFrom(LongProto proto) {
		this.value=proto.getValue();
		return this;
	}


}
