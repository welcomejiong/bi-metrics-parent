package org.corps.bi.protobuf;

import org.corps.bi.protobuf.common.KVProto;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class KVEntity implements ProtobufSerializable<KVEntity, KVProto> {
	
	private byte[] k;
	
	private byte[] v;

	public KVEntity() {
		super();
	}

	public KVEntity(byte[] k,byte[] v) {
		super();
		this.k=k;
		this.v=v;
	}
	
	public KVEntity(byte[] bytes) {
		super();
		this.parseFrom(bytes);
	}

	public byte[] getK() {
		return k;
	}

	public void setK(byte[] k) {
		this.k = k;
	}

	public byte[] getV() {
		return v;
	}

	public void setV(byte[] v) {
		this.v = v;
	}

	@Override
	public KVProto copyTo() {
		KVProto.Builder builder=KVProto.newBuilder();
		if(this.k!=null) {
			builder.setK(ByteString.copyFrom(this.k));
		}
		if(this.v!=null) {
			builder.setV(ByteString.copyFrom(this.v));
		}
		return builder.build();
	}

	@Override
	public byte[] toByteArray() {
		return this.copyTo().toByteArray();
	}

	@Override
	public KVEntity parseFrom(byte[] bytes) {
		try {
			KVProto proto = KVProto.parseFrom(bytes);
		    return this.copyFrom(proto);
		} catch (InvalidProtocolBufferException e) {
		    throw new IllegalArgumentException(e);
		}
	}

	@Override
	public KVEntity copyFrom(KVProto proto) {
		if(proto.getK()!=null) {
			this.k=proto.getK().toByteArray();
		}
		if(proto.getV()!=null) {
			this.v=proto.getV().toByteArray();
		}
		return this;
	}


}
