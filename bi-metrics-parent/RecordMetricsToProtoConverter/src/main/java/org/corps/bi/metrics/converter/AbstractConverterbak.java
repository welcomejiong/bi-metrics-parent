package org.corps.bi.metrics.converter;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.corps.bi.protobuf.ProtobufSerializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;

public abstract class AbstractConverterbak<E extends Object,P extends Message> implements ProtobufSerializable<E,P>{
	
	private static final Logger LOGGER=LoggerFactory.getLogger(AbstractConverterbak.class);
	
	private static final Set<String> EXCLUDE_FIELDS=new HashSet<String>();
	
	private static final ConcurrentHashMap<String, List<String>> FIELD_CACHE_MAP=new ConcurrentHashMap<String, List<String>>();
	
	private static final ConcurrentHashMap<String, EntityAndProtoMethods> ENTITY_PROTO_METHOD_CACHE_MAP=new ConcurrentHashMap<String, EntityAndProtoMethods>();
	
	static {
		EXCLUDE_FIELDS.add("FIELD_SEPARATOR");
		EXCLUDE_FIELDS.add("extraCache");
	}

	private final E entity;

	public AbstractConverterbak(E entity) {
		super();
		this.entity = entity;
	}
	
	public AbstractConverterbak(byte[] bytes,E entity) {
		super();
		this.entity=entity;
		this.parseFrom(bytes);
	}
	
	public E getEntity() {
		return entity;
	}

	protected E toEntity(P proto,Class<?> builderClazz) {
		List<String> fieldList=this.getCurrentEntityFields(proto.getClass(),builderClazz);
		String entityClassName=this.entity.getClass().getName();
		EntityAndProtoMethods entityAndProtoMethods=ENTITY_PROTO_METHOD_CACHE_MAP.get(entityClassName);
		for (String field : fieldList) {
			ReadAndWriteMethod entityPropertyDescriptor=entityAndProtoMethods.getEntityReadAndWriteMethod(field);
			ReadAndWriteMethod protoPropertyDescriptor=entityAndProtoMethods.getProtoReadAndWriteMethod(field);
			if(entityPropertyDescriptor==null||protoPropertyDescriptor==null) {
				LOGGER.warn("field:{} entityPropertyDescriptor:{} protoPropertyDescriptor:{}",field,entityPropertyDescriptor,protoPropertyDescriptor);
				continue;
			}
			Method entityWriteMethod=entityPropertyDescriptor.getWriteMethod();
			Method protoReadMethod=protoPropertyDescriptor.getReadMethod();
			if(entityWriteMethod==null||protoReadMethod==null) {
				LOGGER.warn("field:{} entityWriteMethod:{} protoReadMethod:{}",field,entityWriteMethod,protoReadMethod);
				continue;
			}
			try {
				Object res=protoReadMethod.invoke(proto, new Object[] {});
				if(res==null) {
					LOGGER.warn("field:{} readRes is null.",field);
					continue;
				}
				entityWriteMethod.invoke(this.entity, res);
			} catch (Exception e) {
				 LOGGER.error(e.getMessage(), e);
			}
		}
		return this.entity;
	}
	
	protected void toProto(Class<?> protoClazz,com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
		List<String> fieldList=this.getCurrentEntityFields(protoClazz,builder.getClass());
		String entityClassName=this.entity.getClass().getName();
		EntityAndProtoMethods entityAndProtoMethods=ENTITY_PROTO_METHOD_CACHE_MAP.get(entityClassName);
		for (String field : fieldList) {
			ReadAndWriteMethod entityPropertyDescriptor=entityAndProtoMethods.getEntityReadAndWriteMethod(field);
			ReadAndWriteMethod protoPropertyDescriptor=entityAndProtoMethods.getProtoReadAndWriteMethod(field);
			if(entityPropertyDescriptor==null||protoPropertyDescriptor==null) {
				continue;
			}
			Method entityReadMethod=entityPropertyDescriptor.getReadMethod();
			Method protoWriteMethod=protoPropertyDescriptor.getWriteMethod();
			if(entityReadMethod==null||protoWriteMethod==null) {
				continue;
			}
			try {
				Object res=entityReadMethod.invoke(this.entity, new Object[] {});
				if(res==null) {
					continue ;
				}
				protoWriteMethod.invoke(builder, res);
			} catch (Exception e) {
				 LOGGER.error(e.getMessage(), e);
			}
		}
	}
	
	private List<String> getCurrentEntityFields(Class<?> protoClazz,Class<?> builderClazz){
		List<String> fieldList=Collections.emptyList();
		String entityClassName=this.entity.getClass().getName();
		if(FIELD_CACHE_MAP.containsKey(entityClassName)){
			fieldList=FIELD_CACHE_MAP.get(entityClassName);
		}else {
			fieldList=new ArrayList<String>();
			EntityAndProtoMethods entityAndProtoMethods=null;
			if(ENTITY_PROTO_METHOD_CACHE_MAP.containsKey(entityClassName)) {
				entityAndProtoMethods=ENTITY_PROTO_METHOD_CACHE_MAP.get(entityClassName);
			}else {
				entityAndProtoMethods=new EntityAndProtoMethods(entityClassName);
				ENTITY_PROTO_METHOD_CACHE_MAP.put(entityClassName, entityAndProtoMethods);
			}
			Class<?> tempClass = this.entity.getClass();
			while(tempClass!=null){//当父类为null的时候说明到达了最上层的父类(Object类).
				Field[] fields = tempClass.getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
				      fields[i].setAccessible(true);
				      try {
				    	  String filedName=fields[i].getName();
				    	  if(EXCLUDE_FIELDS.contains(filedName)) {
				    		  continue;
				    	  }
				    	  fieldList.add(filedName);
				    	  PropertyDescriptor entityPropertyDescriptor=new PropertyDescriptor(filedName, tempClass);
				    	  ReadAndWriteMethod entityReadAndWriteMethod=new ReadAndWriteMethod(filedName,entityPropertyDescriptor.getReadMethod(),entityPropertyDescriptor.getWriteMethod());
				    	  entityAndProtoMethods.addEntityReadAndWriteMethod(filedName, entityReadAndWriteMethod);
				    	  
				    	  Method buildWriteMethod=this.getWriteMethod(filedName, builderClazz,entityPropertyDescriptor.getWriteMethod().getParameterTypes());
				    	  Method protoReadMethod=this.getReadMethod(filedName, protoClazz);
				    	  ReadAndWriteMethod protoReadAndWriteMethod=new ReadAndWriteMethod(filedName,protoReadMethod,buildWriteMethod);
				    	  entityAndProtoMethods.addProtoReadAndWriteMethod(filedName, protoReadAndWriteMethod);
				      } catch (Exception e) {
				    	  e.printStackTrace();
				    	  LOGGER.error(e.getMessage(), e);
				      } 
				}
				tempClass=tempClass.getSuperclass();
			}
			
			FIELD_CACHE_MAP.put(entityClassName, fieldList);
		}
		return fieldList;
	}
	
	/**
     * Return a capitalized version of the specified property name.
     *
     * @param s The property name
     */
    private  String capitalizePropertyName(String s) {
        if (s.length() == 0) {
            return s;
        }

        char[] chars = s.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }
    
    private Method getReadMethod(String propertyName,Class<?> clazz) {
    	if(propertyName==null) {
    		return null;
    	}
    	String baseName=this.capitalizePropertyName(propertyName);
    	String readMethodName="get"+baseName;
    	Method method=MethodUtils.getAccessibleMethod(clazz, readMethodName, new Class<?>[]{});
    	if(method==null) {
    		 method=MethodUtils.getAccessibleMethod(clazz, "is"+baseName, new Class<?>[]{});
    	}
    	return method;
    }
    
    private Method getWriteMethod(String propertyName,Class<?> clazz,Class<?>... parameterTypes) {
    	if(propertyName==null) {
    		return null;
    	}
    	String baseName=this.capitalizePropertyName(propertyName);
    	String writeMethodName="set"+baseName;
    	Method method=MethodUtils.getAccessibleMethod(clazz, writeMethodName, parameterTypes);
    	return method;
    }
    
    
    
	
	public static class EntityAndProtoMethods{
		
		private final String entityName;
		
		private final ConcurrentHashMap<String, ReadAndWriteMethod> entityPropertyDescriptorCache=new ConcurrentHashMap<String, ReadAndWriteMethod>();
		
		private final ConcurrentHashMap<String, ReadAndWriteMethod> protoPropertyDescriptorCache=new ConcurrentHashMap<String, ReadAndWriteMethod>();
		
		public EntityAndProtoMethods(String entityName) {
			super();
			this.entityName = entityName;
		}

		public String getEntityName() {
			return entityName;
		}

		public ReadAndWriteMethod getEntityReadAndWriteMethod(String property) {
			return this.entityPropertyDescriptorCache.get(property);
		}
		
		public ReadAndWriteMethod getProtoReadAndWriteMethod(String property) {
			return this.protoPropertyDescriptorCache.get(property);
		}
		
		public void addEntityReadAndWriteMethod(String propertyName,ReadAndWriteMethod ReadAndWriteMethod) {
			this.entityPropertyDescriptorCache.put(propertyName, ReadAndWriteMethod);
		}
		
		public void addProtoReadAndWriteMethod(String propertyName,ReadAndWriteMethod ReadAndWriteMethod) {
			this.protoPropertyDescriptorCache.put(propertyName, ReadAndWriteMethod);
		}
		
		public boolean isExistEntityReadAndWriteMethod(String property) {
			return this.entityPropertyDescriptorCache.containsKey(property);
		}
		
		public boolean isExistProtoReadAndWriteMethod(String property) {
			return this.protoPropertyDescriptorCache.containsKey(property);
		}
		
	}
	
	public static class ReadAndWriteMethod{
		
		private final String property;
		
		private final Method readMethod;
		
		private final Method writeMethod;

		public ReadAndWriteMethod(String property, Method readMethod, Method writeMethod) {
			super();
			this.property = property;
			this.readMethod = readMethod;
			this.writeMethod = writeMethod;
		}

		public String getProperty() {
			return property;
		}

		public Method getReadMethod() {
			return readMethod;
		}

		public Method getWriteMethod() {
			return writeMethod;
		}
		
		
		
	}
	
	
}
