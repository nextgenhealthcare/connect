package com.webreach.mirth.model.converters;

public interface Serializer<E> {
	public String serialize(E source);
	public E deserialize(String source);
}
