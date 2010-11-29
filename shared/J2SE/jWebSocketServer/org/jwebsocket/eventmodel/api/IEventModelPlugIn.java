package org.jwebsocket.eventmodel.api;

import org.jwebsocket.eventmodel.context.EventModel;
import java.util.Map;

/**
 *
 * @author Itachi
 */
public interface IEventModelPlugIn extends IListener, IInitializable {

	public String getId();

	public void setId(String id);

	public EventModel getEm();

	public void setEm(EventModel em);

	public Map getClientAPI();

	public void setClientAPI(Map clientAPI);
}
