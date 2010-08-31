package org.jwebsocket.plugins.rpc.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.token.JSONToken;
import org.jwebsocket.token.Token;

import javolution.util.FastList;

public class ListConverter<E> {
	private List mList;
	public ListConverter (List aList) {
		mList = aList;
	}
	public List<E> convert() throws Exception {
		List<E> lReturnedList = new FastList<E>();
		for (Object lObject : mList) {
			lReturnedList.add((E)lObject); 
		}
		return lReturnedList ;
	}

	public static List convert(List aList, Type aType) throws Exception {		
		ParameterizedType lParameterizedType = (ParameterizedType) aType;
		Type[] parameterArgTypes = lParameterizedType.getActualTypeArguments();
		List lReturnedList = new FastList();
		for (Object lObject : aList) {
			//If it's a list inside another list
			if (parameterArgTypes[0] instanceof ParameterizedType &&
					((ParameterizedType) parameterArgTypes[0]).getActualTypeArguments()[0] == List.class) {
				lReturnedList.add(convert((List)lObject, (ParameterizedType) parameterArgTypes[0]));
			} else {
				Class parameterArgClass = (Class) parameterArgTypes[0] ;
				lReturnedList.add(parameterArgClass.cast(lObject));
			}
		}
		return lReturnedList ;
	}
}
