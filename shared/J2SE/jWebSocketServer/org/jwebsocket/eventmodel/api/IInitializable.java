package org.jwebsocket.eventmodel.api;

/**
 *
 * @author Itachi
 */
public interface IInitializable {

	public void initialize() throws Exception;

	public void shutdown() throws Exception;
}
