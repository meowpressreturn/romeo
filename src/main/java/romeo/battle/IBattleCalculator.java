package romeo.battle;

import romeo.fleet.model.FleetContents;

/**
 * Interface for the battle calculator. The battle calculator will execute the
 * battles and update an instance of {@link IProgressor} which is provided by
 * the UI to obtain progress information for display as the simulations are run
 * since it is a time consuming process. The progressor also allows for the
 * operation to be cancelled.
 */
public interface IBattleCalculator extends Runnable {
  /**
   * Interface for the object that exposes progress information to the UI code
   * and allows for the UI code to request the cancellation of the simulation
   */
  public static interface IProgressor {
    /**
     * Calculator calls this to set the current battle number and checks the
     * result to see if the cancel button was clicked.
     * @param progress
     */
    public boolean setCurrentBattle(int progress);

    /**
     * When complete the calculator calls this and passes in the results object
     * @param metrics
     */
    public void complete(IBattleMetrics metrics);
  }

  /**
   * Run the simulations. After this is complete result data may be obtained
   * from the {@link BattleMetricsImpl} object returned by getBattleMetrics().
   * This method also implements the run() method defined in the Runnable
   * interface, allowing for the simulation to run in a seperate thread.
   */
  @Override
  public void run();

  /**
   * Returns data on the outcome of the simulations
   * @return metrics
   */
  public IBattleMetrics getBattleMetrics();

  /**
   * Set the input fleet for the named player. At least two fleets must be set
   * before combat can be simulated.
   * @param player
   * @param fleet
   */
  public void setFleet(String player, FleetContents fleet);

  /**
   * Set the name of the defending player. This must be set before combat can be
   * simulated. Obviously the name specified should have a fleet set for it too.
   * @param defender
   */
  public void setDefender(String player);

  /**
   * Sets the number of battles to be simulated
   * @paran n the number of battles to run
   */
  public void setNumberOfBattles(int n);

  /**
   * Returns the number of battles to be run
   */
  public int getNumberOfBattles();

  /**
   * Sets the reference to the progressor object. The UI code provides the
   * implementation object. Its 'cancelled' property will be checked from time
   * to time by the calculator to see if it should abort the simulation, and the
   * current battle number will be set as the calculator runs each battle. When
   * complete the calculator will call the progressors complete() method with
   * the metrics object.
   * @param progressor
   */
  public void setProgressor(IProgressor progressor);
}
