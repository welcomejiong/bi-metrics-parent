package org.corps.bi.metrics.converter;

import org.corps.bi.metrics.GameInfo;
import org.corps.bi.metrics.protobuf.GameInfoProto;

import com.google.protobuf.InvalidProtocolBufferException;

public class GameInfoConverter extends AbstractConverter<GameInfo,GameInfoProto> {
	
	public GameInfoConverter() {
		super(new GameInfo());
	}
	
	public GameInfoConverter(GameInfo entity) {
		super(entity);
	}
	
	public GameInfoConverter(byte[] bytes,GameInfo entity) {
		super(bytes,entity);
	}
	
	public GameInfoConverter(byte[] bytes) {
		super(bytes,new GameInfo());
	}

	@Override
	public GameInfoProto copyTo() {
		GameInfoProto.Builder builder=GameInfoProto.newBuilder();
		super.toProto(builder);
		return builder.build();
	}

	@Override
	public byte[] toByteArray() {
		return this.copyTo().toByteArray();
	}

	@Override
	public GameInfo parseFrom(byte[] bytes) {
		try {
			GameInfoProto proto = GameInfoProto.parseFrom(bytes);
		    return this.copyFrom(proto);
		} catch (InvalidProtocolBufferException e) {
		    throw new IllegalArgumentException(e);
		}
	}

	public GameInfo copyFrom(GameInfoProto proto) {
		return super.toEntity(proto);
	}


}
