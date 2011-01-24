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
package org.jwebsocket.eventmodel.filter.validator;

import java.util.Set;
import org.jwebsocket.eventmodel.filter.EventModelFilter;
import org.jwebsocket.eventmodel.event.WebSocketEvent;
import org.jwebsocket.eventmodel.event.WebSocketResponseEvent;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.api.WebSocketConnector;

import org.apache.log4j.Logger;
import org.jwebsocket.eventmodel.event.WebSocketEventDefinition;
import org.jwebsocket.logging.Logging;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Errors;

/**
 *
 * @author kyberneees
 */
public class ValidatorFilter extends EventModelFilter {

	private static Logger mLog = Logging.getLogger(ValidatorFilter.class);
	private TypesMap types;

	@Override
	public void firstCall(WebSocketConnector aConnector, WebSocketEvent aEvent) throws Exception {
		Set<Argument> args = getEm().getEventFactory().getEventDefinitions().
				getDefinition(aEvent.getId()).getIncomingArgsValidation();

		if (args.size() > 0) {
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> Validating incoming arguments for '" + aEvent.getId() + "' event ...");
			}

			//Incoming event args validation
			MapBindingResult errors = new MapBindingResult(aEvent.getArgs().getMap(), "request.errors");
			for (Argument aArg : args) {
				validateArg(aArg, aEvent, errors);
			}
			if (errors.hasErrors()) {
				throw new Exception(errors.getAllErrors().toString());
			}

		}
	}

	@Override
	public void secondCall(WebSocketConnector aConnector, WebSocketResponseEvent aResponseEvent) throws Exception {
		WebSocketEventDefinition def = getEm().getEventFactory().getEventDefinitions().
				getDefinition(aResponseEvent.getId());

		if (def.isResponseRequired()) {
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> Validating outgoing arguments for '" + aResponseEvent.getId() + "' event ...");
			}

			//Response event args validation
			MapBindingResult errors = new MapBindingResult(aResponseEvent.getArgs().getMap(), "response.errors");
			for (Argument aArg : def.getOutgoingArgsValidation()) {
				validateArg(aArg, aResponseEvent, errors);
			}
			if (errors.hasErrors()) {
				throw new Exception(errors.getAllErrors().toString());
			}

			//Adding owner connector in the response if checked
			if (def.isResponseToOwnerConnector()){
				aResponseEvent.getTo().add(aConnector);
			}

			//At least 1 connector is needed for delivery
			if (aResponseEvent.getTo().isEmpty()) {
				throw new NullPointerException("A 'WebSocketConnector' set with > 0 size is required for delivery the response!");
			}
		}
	}

	private void validateArg(Argument aArg, Event aEvent, Errors errors) throws Exception {
		//Argument validation
		if (!aEvent.getArgs().getMap().containsKey(aArg.getName())) {
			if (!aArg.isOptional()) {
				throw new Exception("The argument: '" + aArg.getName() + "' is required!");
			}
		} else if (!types.swapType(aArg.getType()).isInstance(aEvent.getArgs().getObject(aArg.getName()))) {
			throw new Exception("The argument: '" + aArg.getName() + "', needs to be type of " + aArg.getType().toString());
		}

		//Hydrating the argument with the value
		aArg.setValue(aEvent.getArgs().getObject(aArg.getName()));

		//Spring validation mechanism compatibility
		if (null != aArg.getValidator()) {
			if (aArg.getValidator().supports(types.swapType(aArg.getType()))) {
				aArg.getValidator().validate(aArg, errors);
			}
		}
	}

	/**
	 * @return the types
	 */
	public TypesMap getTypes() {
		return types;
	}

	/**
	 * @param types the types to set
	 */
	public void setTypes(TypesMap types) {
		this.types = types;
	}
}
