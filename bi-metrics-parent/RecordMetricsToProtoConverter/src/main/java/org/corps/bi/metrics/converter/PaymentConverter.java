package org.corps.bi.metrics.converter;

import org.corps.bi.metrics.Payment;
import org.corps.bi.metrics.protobuf.PaymentProto;

import com.google.protobuf.InvalidProtocolBufferException;

public class PaymentConverter extends AbstractConverter<Payment,PaymentProto> {
	
	public PaymentConverter() {
		super(new Payment());
	}
	
	public PaymentConverter(Payment entity) {
		super(entity);
	}
	
	public PaymentConverter(byte[] bytes,Payment entity) {
		super(bytes,entity);
	}
	
	public PaymentConverter(byte[] bytes) {
		super(bytes,new Payment());
	}

	@Override
	public PaymentProto copyTo() {
		PaymentProto.Builder builder=PaymentProto.newBuilder();
		super.toProto(PaymentProto.class,builder);
		return builder.build();
	}

	@Override
	public byte[] toByteArray() {
		return this.copyTo().toByteArray();
	}

	@Override
	public Payment parseFrom(byte[] bytes) {
		try {
			PaymentProto proto = PaymentProto.parseFrom(bytes);
		    return this.copyFrom(proto);
		} catch (InvalidProtocolBufferException e) {
		    throw new IllegalArgumentException(e);
		}
	}

	@Override
	public Payment copyFrom(PaymentProto proto) {
		return super.toEntity(proto,PaymentProto.Builder.class);
	}


}
