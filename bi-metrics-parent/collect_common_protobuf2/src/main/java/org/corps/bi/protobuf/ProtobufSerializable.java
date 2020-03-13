package org.corps.bi.protobuf;

import com.google.protobuf.Message;

public interface ProtobufSerializable<E extends Object,P extends Message> {
	
	P copyTo();

	byte[] toByteArray();

	E parseFrom(byte[] bytes);

	E copyFrom(P proto);
}
