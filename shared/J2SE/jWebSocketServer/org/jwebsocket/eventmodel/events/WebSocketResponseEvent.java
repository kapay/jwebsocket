
package org.jwebsocket.eventmodel.events;

import org.jwebsocket.eventmodel.observable.ResponseEvent;
import java.util.LinkedHashSet;
import org.jwebsocket.api.WebSocketConnector;
import java.util.Set;
import org.jwebsocket.token.Token;

/**
 *
 * @author Itachi
 */
public class WebSocketResponseEvent extends ResponseEvent {

  private int code                    = 0;
  public final static int OK          = 0;
  public final static int NOT_OK      = -1;
  private Set<WebSocketConnector> to  = new LinkedHashSet<WebSocketConnector>();
  private Token token;
  private String message;

  /**
   * @return the code
   */
  public int getCode() {
    return code;
  }

  /**
   * @param code the code to set
   */
  public void setCode(int code) {
    this.code = code;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * @param message the message to set
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * @return the to
   */
  public Set<WebSocketConnector> getTo() {
    return to;
  }

  /**
   * @param to the to to set
   */
  public void setTo(Set<WebSocketConnector> to) {
    this.to = to;
  }

  /**
   * @return the token
   */
  public Token getToken() {
    return token;
  }

  /**
   * @param token the token to set
   */
  public void setToken(Token token) {
    this.token = token;
  }

}
