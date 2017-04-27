package romeo.worlds.impl;

import java.util.Objects;

import romeo.worlds.api.IHistory;
import romeo.worlds.api.WorldId;

/**
 * An observation of world history. This is associated with a particular World
 * and turn.
 * As of 0.6.3 this is immutable.
 */
public class HistoryImpl implements IHistory {
  protected WorldId _worldId;
  protected int _turn;
  protected String _owner;
  protected double _firepower;
  protected int _labour;
  protected int _capital;
  
  /**
   * Constructor
   * @param worldId may be null but such a history may not be saved
   * @param turn
   * @param owner
   * @param firepower
   * @param labour
   * @param capital
   */
  public HistoryImpl(WorldId worldId, int turn, String owner, double firepower, int labour, int capital) {
    _worldId = worldId;
    _turn = turn;
    _owner = Objects.requireNonNull(owner,"owner may not be null").trim();
    _firepower = firepower;
    _labour = labour;
    _capital = capital;    
  }

  /**
   * Copy constructor.
   * Constructs an new history object with the same values as in the source but a different id.
   * @param id
   * @param source
   */
  public HistoryImpl(WorldId id, IHistory source) {
    this(
        id,
        Objects.requireNonNull(source,"source may not be null").getTurn(),
        source.getOwner(),
        source.getFirepower(),
        source.getLabour(),
        source.getCapital() );
  }

  /**
   * Returns a string describing this hitory bean which is suitable for use in
   * debugging messages
   * @return string
   */
  @Override
  public String toString() {
    return "History[world=" + getWorldId() + ", turn=" + getTurn() + "]";
  }

  @Override
  public WorldId getWorldId() {
    return _worldId;
  }

  @Override
  public int getTurn() {
    return _turn;
  }

  @Override
  public String getOwner() {
    return _owner;
  }

  @Override
  public double getFirepower() {
    return _firepower;
  }

  @Override
  public int getLabour() {
    return _labour;
  }

  @Override
  public int getCapital() {
    return _capital;
  }
}
