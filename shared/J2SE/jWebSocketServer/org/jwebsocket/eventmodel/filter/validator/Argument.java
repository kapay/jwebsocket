package org.jwebsocket.eventmodel.filter.validator;

/**
 *
 * @author Itachi
 */
public class Argument {
  private String name;
  private Class type;
  private boolean optional;

  public Argument(String name, Class type, boolean optional){
    setName(name);
    setType(type);
    setOptional(optional);
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the type
   */
  public Class getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(Class type) {
    this.type = type;
  }

  /**
   * @return the optional
   */
  public boolean isOptional() {
    return optional;
  }

  /**
   * @param optional the optional to set
   */
  public void setOptional(boolean optional) {
    this.optional = optional;
  }

}
