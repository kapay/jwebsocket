
package org.jwebsocket.eventmodel.events;

import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.filter.validator.Argument;
import org.jwebsocket.eventmodel.api.IInitializable;
import java.util.Set;
import java.util.HashSet;
import org.jwebsocket.token.Token;

/**
 *
 * @author Itachi
 */
 public class WebSocketEvent extends Event implements IInitializable{

   private Set<Argument> argsValidation     = new HashSet<Argument>();
   private Set<Argument> responseValidation = new HashSet<Argument>();
   private boolean responseRequired         = false;
   private Token token;

   @Override
   public void initialize(){}

   @Override
   public void shutdown(){}

   
  /**
   * @return the argsValidation
   */
  public Set<Argument> getArgsValidation() {
    return argsValidation;
  }

  /**
   * @param argsValidation the argsValidation to set
   */
  public void setArgsValidation(Set<Argument> argsValidation) {
    this.argsValidation = argsValidation;
  }

  /**
   * @return the responseValidation
   */
  public Set<Argument> getResponseValidation() {
    return responseValidation;
  }

  /**
   * @param responseValidation the responseValidation to set
   */
  public void setResponseValidation(Set<Argument> responseValidation) {
    this.responseValidation = responseValidation;
  }

  /**
   * @return the responseRequired
   */
  public boolean isResponseRequired() {
    return responseRequired;
  }

  /**
   * @param responseRequired the responseRequired to set
   */
  public void setResponseRequired(boolean responseRequired) {
    this.responseRequired = responseRequired;
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