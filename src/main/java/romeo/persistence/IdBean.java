package romeo.persistence;

/**
 * Superclass for beans with an id property. This was the superclass for the
 * record objects that Romeo persists via its services classes. The getter is
 * now exposed via the ICanGetId subclasses that are still mutable can
 * explicitly make setId() public and implement ICanSetId interface.
 */
public class IdBean<T> implements ICanGetId<T> {
  private T _id;

  /**
   * Returns the id
   * @return id
   */
  @Override
  public T getId() {
    return _id;
  }

  /**
   * For use by subclasses to set the id property internally.
   * This is not longer exposed by default. Mutable subclasses must explicity
   * make it public and implment {@link ICanSetId} themselves.
   * @param id
   */
  protected void setId(T id) {
    _id = id;
  }

  /**
   * Returns true if the bean has an id set
   * @return hasId
   */
  public boolean hasId() {
    return !(_id == null);
  }
  
  @Override
  public boolean isNew() {
    return hasId() == false;
  }

  /**
   * Returns a string containing the classname and id
   * @return string
   */
  @Override
  public String toString() {
    return this.getClass().getName() + ", id=" + getId();
  }

  /**
   * If the obj is also an IdBean and both it and this IdBean have an id then
   * the id will be compared, otherwise normal Object.equals() logic is applied.
   * @param obj
   * @return equals
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if(obj instanceof IdBean) {
      IdBean<T> otherBean = (IdBean<T>) obj;
      if(hasId() && otherBean.hasId()) {
        return _id.equals(otherBean.getId());
      }
    }
    return super.equals(obj);
  }

  /**
   * If this IdBean has an id the hashcode is that of the id. Otherwise normal
   * Object.hashCode() applies. This has obvious implications if you make use of
   * the hashcode in a map or something and then change the id or set it for the
   * first time...
   * @return hashCode
   */
  @Override
  public int hashCode() {
    return hasId() ? _id.hashCode() : super.hashCode();
  }

}
