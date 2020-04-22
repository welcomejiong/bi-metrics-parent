package org.corps.bi.metrics.converter;

import org.corps.bi.metrics.Meta;
import org.corps.bi.metrics.protobuf.MetaProto;

import com.google.protobuf.InvalidProtocolBufferException;

public class MetaConverter extends AbstractConverter<Meta,MetaProto> {
	
	public MetaConverter() {
		super(new Meta());
	}
	
	public MetaConverter(Meta entity) {
		super(entity);
	}
	
	public MetaConverter(byte[] bytes,Meta entity) {
		super(bytes,entity);
	}
	
	public MetaConverter(byte[] bytes) {
		super(bytes,new Meta());
	}

	@Override
	public MetaProto copyTo() {
		MetaProto.Builder builder=MetaProto.newBuilder();
		super.toProto(builder);
		return builder.build();
	}

	@Override
	public byte[] toByteArray() {
		return this.copyTo().toByteArray();
	}

	@Override
	public Meta parseFrom(byte[] bytes) {
		try {
			MetaProto proto = MetaProto.parseFrom(bytes);
		    return this.copyFrom(proto);
		} catch (InvalidProtocolBufferException e) {
		    throw new IllegalArgumentException(e);
		}
	}

	public Meta copyFrom(MetaProto proto) {
		return super.toEntity(proto);
	}


}
