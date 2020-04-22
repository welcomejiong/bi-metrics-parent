package org.corps.bi.metrics.converter;

import org.corps.bi.metrics.Dau;
import org.corps.bi.metrics.protobuf.DauProto;

import com.google.protobuf.InvalidProtocolBufferException;

public class DauConverter extends AbstractConverter<Dau,DauProto> {
	
	public DauConverter() {
		super(new Dau());
	}
	
	public DauConverter(Dau entity) {
		super(entity);
	}
	
	public DauConverter(byte[] bytes,Dau entity) {
		super(bytes,entity);
	}
	
	public DauConverter(byte[] bytes) {
		super(bytes,new Dau());
	}

	@Override
	public DauProto copyTo() {
		DauProto.Builder builder=DauProto.newBuilder();
		super.toProto(builder);
		return builder.build();
	}

	@Override
	public byte[] toByteArray() {
		return this.copyTo().toByteArray();
	}

	@Override
	public Dau parseFrom(byte[] bytes) {
		try {
			DauProto proto = DauProto.parseFrom(bytes);
		    return this.copyFrom(proto);
		} catch (InvalidProtocolBufferException e) {
		    throw new IllegalArgumentException(e);
		}
	}

	@Override
	public Dau copyFrom(DauProto proto) {
		return super.toEntity(proto);
	}


}
