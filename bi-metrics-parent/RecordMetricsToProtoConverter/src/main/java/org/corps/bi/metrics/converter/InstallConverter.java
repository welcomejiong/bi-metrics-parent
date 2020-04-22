package org.corps.bi.metrics.converter;

import org.corps.bi.metrics.Install;
import org.corps.bi.metrics.protobuf.InstallProto;

import com.google.protobuf.InvalidProtocolBufferException;

public class InstallConverter extends AbstractConverter<Install,InstallProto> {
	
	public InstallConverter() {
		super(new Install());
	}
	
	public InstallConverter(Install entity) {
		super(entity);
	}
	
	public InstallConverter(byte[] bytes,Install entity) {
		super(bytes,entity);
	}
	
	public InstallConverter(byte[] bytes) {
		super(bytes,new Install());
	}
	
	@Override
	public InstallProto copyTo() {
		InstallProto.Builder builder=InstallProto.newBuilder();
		super.toProto(builder);
		return builder.build();
	}

	@Override
	public byte[] toByteArray() {
		return this.copyTo().toByteArray();
	}

	@Override
	public Install parseFrom(byte[] bytes) {
		try {
			InstallProto proto = InstallProto.parseFrom(bytes);
		    return this.copyFrom(proto);
		} catch (InvalidProtocolBufferException e) {
		    throw new IllegalArgumentException(e);
		}
	}

	@Override
	public Install copyFrom(InstallProto proto) {
		return super.toEntity(proto);
	}


}
