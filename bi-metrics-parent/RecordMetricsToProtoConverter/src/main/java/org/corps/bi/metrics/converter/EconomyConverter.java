package org.corps.bi.metrics.converter;

import org.corps.bi.metrics.Economy;
import org.corps.bi.metrics.protobuf.EconomyProto;

import com.google.protobuf.InvalidProtocolBufferException;

public class EconomyConverter extends AbstractConverter<Economy,EconomyProto> {
	
	public EconomyConverter() {
		super(new Economy());
	}
	
	public EconomyConverter(Economy entity) {
		super(entity);
	}
	
	public EconomyConverter(byte[] bytes,Economy entity) {
		super(bytes,entity);
	}
	
	public EconomyConverter(byte[] bytes) {
		super(bytes,new Economy());
	}

	@Override
	public EconomyProto copyTo() {
		EconomyProto.Builder builder=EconomyProto.newBuilder();
		super.toProto(EconomyProto.class,builder);
		return builder.build();
	}

	@Override
	public byte[] toByteArray() {
		return this.copyTo().toByteArray();
	}

	@Override
	public Economy parseFrom(byte[] bytes) {
		try {
			EconomyProto proto = EconomyProto.parseFrom(bytes);
		    return this.copyFrom(proto);
		} catch (InvalidProtocolBufferException e) {
		    throw new IllegalArgumentException(e);
		}
	}

	@Override
	public Economy copyFrom(EconomyProto proto) {
		return super.toEntity(proto,EconomyProto.Builder.class);
	}


}
