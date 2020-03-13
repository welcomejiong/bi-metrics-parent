package org.corps.bi.metrics.converter;

import org.corps.bi.metrics.CustomBinaryBodyMetric;
import org.corps.bi.metrics.protobuf.CustomBinaryBodyMetricProto;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class CustomBinaryBodyMetricConverter extends AbstractConverter<CustomBinaryBodyMetric,CustomBinaryBodyMetricProto> {
	
	public CustomBinaryBodyMetricConverter() {
		super(new CustomBinaryBodyMetric());
	}
	
	public CustomBinaryBodyMetricConverter(CustomBinaryBodyMetric entity) {
		super(entity);
	}
	
	public CustomBinaryBodyMetricConverter(byte[] bytes,CustomBinaryBodyMetric entity) {
		super(bytes,entity);
	}
	
	public CustomBinaryBodyMetricConverter(byte[] bytes) {
		super(bytes,new CustomBinaryBodyMetric());
	}

	@Override
	public CustomBinaryBodyMetricProto copyTo() {
		CustomBinaryBodyMetricProto.Builder builder=CustomBinaryBodyMetricProto.newBuilder();
		super.toProto(CustomBinaryBodyMetricProto.class,builder);
		if(this.getEntity().getBody()!=null) {
			builder.setBody(ByteString.copyFrom(this.getEntity().getBody()));
		}
		return builder.build();
	}

	@Override
	public byte[] toByteArray() {
		return this.copyTo().toByteArray();
	}

	@Override
	public CustomBinaryBodyMetric parseFrom(byte[] bytes) {
		try {
			CustomBinaryBodyMetricProto proto = CustomBinaryBodyMetricProto.parseFrom(bytes);
		    return this.copyFrom(proto);
		} catch (InvalidProtocolBufferException e) {
		    throw new IllegalArgumentException(e);
		}
	}

	@Override
	public CustomBinaryBodyMetric copyFrom(CustomBinaryBodyMetricProto proto) {
		CustomBinaryBodyMetric ret=super.toEntity(proto,CustomBinaryBodyMetricProto.Builder.class);
		if(proto.getBody()!=null) {
			ret.setBody(proto.getBody().toByteArray());
		}
		return ret;
	}


}
