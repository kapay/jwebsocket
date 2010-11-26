
package org.jwebsocket.eventmodel.observable;

import java.util.concurrent.Callable;
import org.jwebsocket.eventmodel.api.IListener;

/**
 *
 * @author Itachi
 */
public class CallableListener implements Callable{
  IListener aListener;
  Event aEvent;
  ResponseEvent aResponseEvent;

  public CallableListener(IListener aListener, Event aEvent, ResponseEvent aResponseEvent){
    this.aListener  = aListener;
    this.aEvent     = aEvent;
    this.aListener  = aListener;
  }

  @Override
  public Object call(){
    aListener.processEvent(aEvent, aResponseEvent);
    return null;
  }
}
