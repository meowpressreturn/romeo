package romeo.xfactors.api;

import romeo.persistence.ICanGetId;
import romeo.xfactors.impl.XFactorImpl;

/**
 * Interface defining the information a record of an XFactor must provide.
 * The standard convenience implementation is {@link XFactorImpl}
 */
public interface IXFactor extends ICanGetId<XFactorId> {
  
  /**
   * Returns the name of the XFactor
   * @return
   */
  public String getName();

  /**
   * Returns a description of the xFactor
   * @return
   */
  public String getDescription();

  /**
   * xfel expression defining the condition under which the x-factor is triggered to become
   * active for the unit
   * @return trigger
   */
  public String getTrigger();

  /**
   * xfel expression defining the altered attacks score of the unit
   * @return xfel
   */
  public String getXfAttacks();

  /**
   * xfel expression defining the altered offense score of the unit
   * @return xfel
   */
  public String getXfOffense();

  /**
   * xfel expression defining the altered defense score of the unit
   * @return xfel
   */
  public String getXfDefense();

  /**
   * xfel expression defining the altered population damage of the unit
   * @return xfel
   */
  public String getXfPd();

  /**
   * xfel expression defining the condition under which a unit will be removed/self-destruct
   * @return xfel
   */
  public String getXfRemove();

}
