package org.corps.bi.metrics.converter;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.corps.bi.protobuf.ProtobufSerializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;

public abstract class AbstractConverter<E extends Object,P extends Message> implements ProtobufSerializable<E,P>{
	
	private static final Logger LOGGER=LoggerFactory.getLogger(AbstractConverter.class);
	
	private static final ConcurrentHashMap<String, Field> ENTITY_FIELD_CACHE_MAP=new ConcurrentHashMap<String, Field>();
	
	private static final ConcurrentHashMap<String, List<Descriptors.FieldDescriptor>> METRIC_PROTO_FIELD_DESCRIPTORS=new ConcurrentHashMap<String, List<FieldDescriptor>>();

	private final E entity;

	public AbstractConverter(E entity) {
		super();
		this.entity = entity;
	}
	
	public AbstractConverter(byte[] bytes,E entity) {
		super();
		this.entity=entity;
		this.parseFrom(bytes);
	}
	
	public E getEntity() {
		return entity;
	}
	
	protected E toEntity(P proto) {
		try {
			final List<Descriptors.FieldDescriptor> fieldDescriptorList = this.getProtoFieldDescriptor(proto);
			for (Descriptors.FieldDescriptor descriptor : fieldDescriptorList) {
				Object fieldValue=proto.getField(descriptor);
				if(fieldValue==null) {
					continue;
				}
				Field entityField=this.getEntityField(descriptor.getName());
				FieldUtils.writeField(entityField, this.entity, fieldValue, true);
			}
		} catch (IllegalAccessException e1) {
			LOGGER.error(e1.getMessage(), e1);
		}
		return this.entity;
	}
	
	protected void toProto(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
		try {
			final List<Descriptors.FieldDescriptor> fieldDescriptorList = this.getProtoFieldDescriptor(builder.getDefaultInstanceForType());
			for (Descriptors.FieldDescriptor descriptor : fieldDescriptorList) {
				Field entityField=this.getEntityField(descriptor.getName());
				Object fieldValue=FieldUtils.readField(entityField, this.entity, true);
				if(fieldValue==null) {
					continue;
				}
				builder.setField(descriptor, fieldValue);
			}
		} catch (IllegalAccessException e1) {
			LOGGER.error(e1.getMessage(), e1);
		}
	}
	
	private Field getEntityField(String fieldName) {
		String key=this.entity.getClass().getName()+"."+fieldName;
		if(ENTITY_FIELD_CACHE_MAP.containsKey(key)) {
			return ENTITY_FIELD_CACHE_MAP.get(key);
		}
		Field field=FieldUtils.getField(this.entity.getClass(), fieldName, true);
		ENTITY_FIELD_CACHE_MAP.put(key, field);
		return field;
	}
	
	private List<Descriptors.FieldDescriptor> getProtoFieldDescriptor(Message message){
		String protoClassName=message.getClass().getName();
		if(!METRIC_PROTO_FIELD_DESCRIPTORS.containsKey(protoClassName)) {
			METRIC_PROTO_FIELD_DESCRIPTORS.put(protoClassName, message.getDescriptorForType().getFields());
		}
		return METRIC_PROTO_FIELD_DESCRIPTORS.get(protoClassName);
	}

	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder(this.getClass().getName());
		try {
			sb.append("[").append("\n");
			List<Field> entityFieldList=FieldUtils.getAllFieldsList(this.entity.getClass());
			for (Field field : entityFieldList) {
				Object fieldVal=FieldUtils.readField(field, this.entity, true);
				sb.append(field.getName()).append(":").append(fieldVal).append("\n");
			}
			sb.append("]");
		} catch (IllegalAccessException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return sb.toString();
	}
	
	
	
}
