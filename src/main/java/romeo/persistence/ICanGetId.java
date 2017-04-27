package romeo.persistence;

/**
 * Interface to beans that have a string id we can get.
 * This interface is now also required to provide the logic to determine if the bean is 'new'
 */
public interface ICanGetId<T> {
  
  public T getId();
  
  public boolean isNew();
}
