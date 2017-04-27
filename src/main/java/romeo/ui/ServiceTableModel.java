package romeo.ui;

import java.util.Collections;
import java.util.EventObject;
import java.util.List;

import romeo.model.api.IService;
import romeo.model.api.IServiceListener;

/**
 * A {@link BeanTableModel} that updates when it receives a dataChanged event
 * from an {@link IService}
 */
public abstract class ServiceTableModel extends BeanTableModel implements IServiceListener {
  protected IService _service;

  public ServiceTableModel(ColumnDef[] columns, IService service) {
    super(columns, Collections.emptyList());
    _service = service;
    _service.addListener(this);
    _data = fetchNewData();
  }

  protected abstract List<? extends Object> fetchNewData();

  @Override
  public void dataChanged(EventObject event) {
    _data = fetchNewData();
    sortRows();
    fireTableDataChanged();
  }

}
