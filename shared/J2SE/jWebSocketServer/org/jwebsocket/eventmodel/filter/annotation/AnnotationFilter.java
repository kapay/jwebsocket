//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.eventmodel.filter.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.annotation.ImportFromToken;
import org.jwebsocket.eventmodel.event.WebSocketEvent;
import org.jwebsocket.eventmodel.filter.EventModelFilter;

/**
 *
 * @author kyberneees
 */
public class AnnotationFilter extends EventModelFilter {

	private static Logger mLog = Logging.getLogger(AnnotationFilter.class);

	@Override
	public void beforeCall(WebSocketConnector aConnector, WebSocketEvent aEvent) throws Exception {
		//Getting all fields
		for (Field f : aEvent.getClass().getDeclaredFields()) {
			//Processing the ImportFromToken annotation in fields
			processAnnotation(ImportFromToken.class, f, aConnector, aEvent);
		}
	}

	/**
	 * Process the ImportFromToken annotation
	 * 
	 * @param c
	 * @param f
	 * @param aConnector
	 * @param aEvent
	 * @throws Exception
	 */
	public void processAnnotation(Class<ImportFromToken> c, Field f,
			WebSocketConnector aConnector, WebSocketEvent aEvent) throws Exception {

		//Getting fields with the "ImportFromToken" annotation
		if (f.isAnnotationPresent(c)) {
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> Processing annotation '" + c.toString() + "' in field '" + f.getName() + "'...");
			}

			ImportFromToken annotation = f.getAnnotation(c);
			Object value;
			String fieldName = f.getName();
			String key = (annotation.key().isEmpty()) ? fieldName : annotation.key();

			//Importing parameter if exists
			if (aEvent.getArgs().getMap().containsKey(key)) {
				//Getting the value
				value = aEvent.getArgs().getObject(key);

				//Getting the setter method for the field
				Method setter = aEvent.getClass().getMethod(
						"set"
						+ fieldName.substring(0, 1).toUpperCase()
						+ fieldName.substring(1),
						value.getClass());

				//Processing the importing strategy
				if (annotation.strategy().equals("move")) {
					aEvent.getArgs().remove(key);
				}

				//Invoking the setter method for the annotated field
				setter.invoke(aEvent, value.getClass().cast(value));
			}
		}
	}
}
