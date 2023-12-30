package romeo.ui.forms;

import java.util.List;

import romeo.xfactors.api.IXFactor;
import romeo.xfactors.api.IXFactorService;
import romeo.xfactors.api.XFactorId;

public class XFactorCombo extends AbstractRecordCombo {

  public XFactorCombo(IXFactorService xFactorService) {
    super(xFactorService);
  }

  @Override
  protected List<IXFactor> loadRecords() {
    IXFactorService service = (IXFactorService) getService();
    return service.getXFactors();
  }

  public IXFactor getXFactor() {
    return (IXFactor) getSelectedRecord();
  }

  public void setXFactor(IXFactor xf) {
    setSelectedRecord(xf);
  }

  public void setXFactor(XFactorId id) {
    if(id==null) {
      setXFactor((IXFactor)null);
    } else {
      IXFactorService service = (IXFactorService) getService();
      IXFactor xf = service.getXFactor(id);
      setXFactor(xf);
    }
  }

}
