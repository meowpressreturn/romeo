package romeo.persistence;

import java.util.Objects;

import javax.sql.DataSource;

import romeo.model.impl.AbstractService;
import romeo.utils.IKeyGen;

/**
 * Extends {@link AbstractService} to hold references to the datasource and keygen.
 */
public abstract class AbstractPersistenceService extends AbstractService {
  
  protected DataSource _dataSource;
  protected IKeyGen _keyGen;

  /**
   * Constructor
   * @param keyGen
   *          an implementation of {@link IKeyGen} used for generating primary
   *          keys (required)
   */
  public AbstractPersistenceService(DataSource dataSource, IKeyGen keyGen) {
    _dataSource = Objects.requireNonNull(dataSource, "dataSource may not be null");
    _keyGen = Objects.requireNonNull(keyGen, "keyGen may not be null");
  }

}
