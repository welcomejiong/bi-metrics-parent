package org.corps.bi.metrics.converter;

import org.corps.bi.metrics.Milestone;
import org.corps.bi.metrics.protobuf.MilestoneProto;

import com.google.protobuf.InvalidProtocolBufferException;

public class MilestoneConverter extends AbstractConverter<Milestone,MilestoneProto> {
	
	public MilestoneConverter() {
		super(new Milestone());
	}
	
	public MilestoneConverter(Milestone entity) {
		super(entity);
	}
	
	public MilestoneConverter(byte[] bytes,Milestone entity) {
		super(bytes,entity);
	}
	
	public MilestoneConverter(byte[] bytes) {
		super(bytes,new Milestone());
	}

	@Override
	public MilestoneProto copyTo() {
		MilestoneProto.Builder builder=MilestoneProto.newBuilder();
		super.toProto(builder);
		return builder.build();
	}

	@Override
	public byte[] toByteArray() {
		return this.copyTo().toByteArray();
	}

	@Override
	public Milestone parseFrom(byte[] bytes) {
		try {
			MilestoneProto proto = MilestoneProto.parseFrom(bytes);
		    return this.copyFrom(proto);
		} catch (InvalidProtocolBufferException e) {
		    throw new IllegalArgumentException(e);
		}
	}

	@Override
	public Milestone copyFrom(MilestoneProto proto) {
		return super.toEntity(proto);
	}


}
