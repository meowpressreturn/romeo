package romeo.xfactors.api;

import java.util.List;

import romeo.model.api.IService;

public interface IXFactorService extends IService {
  
  public List<IXFactor> getXFactors();

  public IXFactor getXFactor(XFactorId id);

  public XFactorId saveXFactor(IXFactor xFactor);

  public void deleteXFactor(XFactorId id);

  public IXFactor getXFactorByName(String name);

}
