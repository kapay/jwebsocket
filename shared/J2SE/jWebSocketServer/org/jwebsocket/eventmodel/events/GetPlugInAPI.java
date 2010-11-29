package org.jwebsocket.eventmodel.events;

import org.jwebsocket.eventmodel.filter.validator.Argument;
import java.util.Map;

/**
 *
 * @author Itachi
 */
public class GetPlugInAPI extends WebSocketEvent {

	@Override
	public void initialize() {
		//Inconming event args validation
		getArgsValidation().add(new Argument("plugin_id", String.class, false));

		//Response args validation
		setResponseRequired(true);
		getResponseValidation().add(new Argument("api", Map.class, true));
	}
}
