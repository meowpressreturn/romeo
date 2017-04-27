package romeo.worlds.api;

public interface IHistory {
  
  public WorldId getWorldId();

  public int getTurn();

  public String getOwner();

  public double getFirepower();

  public int getLabour();

  public int getCapital();
}
