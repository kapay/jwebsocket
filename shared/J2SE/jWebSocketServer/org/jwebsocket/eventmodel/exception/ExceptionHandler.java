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
package org.jwebsocket.eventmodel.exception;

import java.lang.reflect.Method;
import org.jwebsocket.eventmodel.api.IExceptionHandler;
import org.jwebsocket.logging.Logging;
import org.apache.log4j.Logger;

/**
 *
 * @author kyberneees
 */
public class ExceptionHandler implements IExceptionHandler {

	private static Logger mLog = Logging.getLogger(ExceptionHandler.class);

	public void initialize() throws Exception {
	}

	/**
	 * Process uncaught exceptions
	 * @param ex
	 */
	public void process(Exception ex) {
		if (mLog.isDebugEnabled()) {
			mLog.error(ex.toString(), ex);
		} else {
			mLog.error(ex.getMessage());
		}
	}

	public static void callProcessException(IExceptionHandler aExceptionHandler, Exception aEx) {
		Class<? extends Exception> aExClass = aEx.getClass();
		Class<? extends IExceptionHandler> aExceptionHandlerClass = aExceptionHandler.getClass();

		try {
			Method aMethod = aExceptionHandlerClass.getMethod("process", aExClass);
			aMethod.invoke(aExceptionHandler, aExClass.cast(aEx));
		} catch (NoSuchMethodException ex) {
			//Calling the base method
			aExceptionHandler.process(aEx);
		} catch (Exception ex) {
			mLog.error(ex.getMessage(), ex);
		}
	}

	public void shutdown() throws Exception {
	}
}
