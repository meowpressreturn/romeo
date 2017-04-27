package romeo.xfactors.api;

import java.util.List;

import romeo.model.api.IService;

public interface IXFactorService extends IService {
  
  public List<IXFactor> getXFactors();

  public IXFactor loadXFactor(XFactorId id);

  public XFactorId saveXFactor(IXFactor xFactor);

  public void deleteXFactor(XFactorId id);

  public IXFactor getByName(String name);

}
