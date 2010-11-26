
package org.jwebsocket.eventmodel.observable;




/**
 *
 * @author Itachi
 */
public class ResponseEvent extends Event {
  private long elapsedTime;

  /**
   * @return the elapsedTime
   */
  public long getElapsedTime() {
    return elapsedTime;
  }

  /**
   * @param elapsedTime the elapsedTime to set
   */
  public void setElapsedTime(long elapsedTime) {
    this.elapsedTime = elapsedTime;
  }

}

