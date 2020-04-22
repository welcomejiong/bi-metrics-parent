package org.corps.bi.metrics.converter;

import org.corps.bi.metrics.AdTracking;
import org.corps.bi.metrics.protobuf.AdTrackingProto;

import com.google.protobuf.InvalidProtocolBufferException;

public class AdTrackingConverter extends AbstractConverter<AdTracking,AdTrackingProto> {
	
	public AdTrackingConverter() {
		super(new AdTracking());
	}
	
	public AdTrackingConverter(AdTracking entity) {
		super(entity);
	}
	
	public AdTrackingConverter(byte[] bytes,AdTracking entity) {
		super(bytes,entity);
	}
	
	public AdTrackingConverter(byte[] bytes) {
		super(bytes,new AdTracking());
	}

	@Override
	public AdTrackingProto copyTo() {
		AdTrackingProto.Builder builder=AdTrackingProto.newBuilder();
		super.toProto(builder);
		return builder.build();
	}

	@Override
	public byte[] toByteArray() {
		return this.copyTo().toByteArray();
	}

	@Override
	public AdTracking parseFrom(byte[] bytes) {
		try {
			AdTrackingProto proto = AdTrackingProto.parseFrom(bytes);
		    return this.copyFrom(proto);
		} catch (InvalidProtocolBufferException e) {
		    throw new IllegalArgumentException(e);
		}
	}

	@Override
	public AdTracking copyFrom(AdTrackingProto proto) {
		return super.toEntity(proto);
	}


}
