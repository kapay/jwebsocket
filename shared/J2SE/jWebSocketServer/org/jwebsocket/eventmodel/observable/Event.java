
package org.jwebsocket.eventmodel.observable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Itachi
 */
public abstract class Event {
  private String id;
  private Map args = new HashMap();
  private Object subject;
  private boolean processed = false;

  @Override
  public String toString(){
    return getId();
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the args
   */
  public Map getArgs() {
    return args;
  }

  /**
   * @param args the args to set
   */
  public void setArgs(Map args) {
    this.args = args;
  }

  /**
   * @return the subject
   */
  public Object getSubject() {
    return subject;
  }

  /**
   * @param subject the subject to set
   */
  public void setSubject(Object subject) {
    this.subject = subject;
  }

  /**
   * @return the processed
   */
  public boolean isProcessed() {
    return processed;
  }

  /**
   * @param processed the processed to set
   */
  public void setProcessed(boolean processed) {
    this.processed = processed;
  }
}

