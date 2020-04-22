package org.corps.bi.metrics.converter;

import org.corps.bi.metrics.Counter;
import org.corps.bi.metrics.protobuf.CounterProto;

import com.google.protobuf.InvalidProtocolBufferException;

public class CounterConverter extends AbstractConverter<Counter,CounterProto> {
	
	public CounterConverter() {
		super(new Counter());
	}
	
	public CounterConverter(Counter entity) {
		super(entity);
	}
	
	public CounterConverter(byte[] bytes,Counter entity) {
		super(bytes,entity);
	}
	
	public CounterConverter(byte[] bytes) {
		super(bytes,new Counter());
	}

	@Override
	public CounterProto copyTo() {
		CounterProto.Builder builder=CounterProto.newBuilder();
		super.toProto(builder);
		return builder.build();
	}

	@Override
	public byte[] toByteArray() {
		return this.copyTo().toByteArray();
	}

	@Override
	public Counter parseFrom(byte[] bytes) {
		try {
			CounterProto proto = CounterProto.parseFrom(bytes);
		    return this.copyFrom(proto);
		} catch (InvalidProtocolBufferException e) {
		    throw new IllegalArgumentException(e);
		}
	}

	public Counter copyFrom(CounterProto proto) {
		return super.toEntity(proto);
	}


}
