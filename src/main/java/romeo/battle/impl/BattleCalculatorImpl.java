package romeo.battle.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import romeo.battle.IBattleCalculator;
import romeo.battle.IBattleMetrics;
import romeo.fleet.model.FleetContents;
import romeo.fleet.model.FleetElement;
import romeo.ui.ErrorDialog;
import romeo.utils.Convert;
import romeo.xfactors.api.IXFactorCompiler;

/**
 * This is the implementation of the battle simulator logic.
 */
public class BattleCalculatorImpl implements IBattleCalculator {
  protected Random _rnd = new Random();
  protected int _numberOfBattles = 1;
  protected IProgressor _progressor;
  protected IXFactorCompiler _compiler;
  protected int _maxNotes = 5;

  protected Map<String, FleetContents> _fleets = new LinkedHashMap<String, FleetContents>();
  protected String _defenderName;
  protected BattleMetricsImpl _metrics;

  /**
   * Constructor. Note that defender and fleets will need to be set prior to
   * calling run.
   * @param compiler
   *          an xFactor compiler
   */
  public BattleCalculatorImpl(IXFactorCompiler compiler) {
    Objects.requireNonNull(compiler, "compiler must not be null");
    _compiler = compiler;
  }

  /**
   * Sets a players fleet. The fleet specified will be copied and normalised
   * without flattening.
   * @param player
   * @param fleet
   */
  @Override
  public synchronized void setFleet(String player, FleetContents fleet) {
    FleetContents inputFleet = new FleetContents(fleet);
    inputFleet.normalise(false);
    _fleets.put(player, inputFleet);
  }

  /**
   * Set the name of the defending player.
   */
  @Override
  public synchronized void setDefender(String player) {
    _defenderName = player;
    //nb: will be validated in run
  }

  /**
   * Run the battle. This method expects that the progressor and fleet objects
   * have already been set. The progressor will be updated as the battle runs,
   * and its isCancelled method checked each battle. If the simulation
   * encounters an exception it will be written to the log and an
   * {@link ErrorDialog} will be shown.
   */
  @Override
  public synchronized void run() {
    Log log = LogFactory.getLog(this.getClass());
    try {
      runInternal(log);
    } catch(Exception e) {
      log.error("Error running simulation", e);
      ErrorDialog dialog = new ErrorDialog("Simulation error", e, false);
      dialog.show();
    }
  }

  /**
   * Runs the battle. Called by run().
   * @param log
   */
  private void runInternal(Log log) {
    if(_fleets.size() < 2) { //Validate we have at least two fleets set
      String msg = "There are only " + _fleets.size() + " fleets set";
      log.error(msg);
      throw new IllegalStateException(msg);
    }

    if(_defenderName == null || _defenderName.length() == 0) { //Validate that defenderName was set
      String msg = "Defender not specified";
      log.error(msg);
      throw new IllegalStateException(msg);
    }

    if(_fleets.get(_defenderName) == null) { //Validate that defenderName refers to one of the set fleets
      String msg = "Defender \"" + _defenderName + "\" fleet not set";
      log.error(msg);
      throw new IllegalStateException(msg);
    }

    long startTime = System.currentTimeMillis();

    //Initialise the array of player names based on the fleets that were set
    String[] playerNames = new String[_fleets.size()];
    int i = 0;
    Iterator<String> j = _fleets.keySet().iterator();
    while(j.hasNext()) {
      playerNames[i] = j.next();
      i++;
    }

    if(_progressor != null) {
      _progressor.setCurrentBattle(0);
    }

    //Create the battle metrics object that will be used to return the battle results
    //and statistics to the battle report panel
    int numberOfBattles = getNumberOfBattles();
    _metrics = new BattleMetricsImpl(playerNames, _defenderName, numberOfBattles);
    RoundContext context = new RoundContext(playerNames);
    int sumOfRounds = 0; //Counter to tally total rounds simulated for later determing avg per battle
    StringBuffer notes = new StringBuffer(); //Buffer used to write details of first few battles etc

    Map<String, FleetContents> masters = new HashMap<String, FleetContents>(); //Stores master copies of input fleets
    for(int p = playerNames.length - 1; p >= 0; p--) { //Iterate players and prepare the master copies of input fleets, etc...
      String player = playerNames[p];
      FleetContents inputFleet = (FleetContents) _fleets.get(player);

      //Store copy of the initial input fleet (including non-combat units) in the metrics
      _metrics.setFleet(player, (FleetContents) _fleets.get(player));

      //Create a master copy of fleet from which combat units have been removed
      FleetContents master = new FleetContents(inputFleet);
      //noncoms.put(player, master.removeNonCombatUnits() );      
      master.normalise(false);
      String xErr = master.compileXFactors(_compiler);
      notes.append(xErr);
      masters.put(player, new FleetContents(master));

      //Set the master into the round context for pre battle XF calculation purposes
      context.setFleet(player, master);
    }

    context.setDefendingPlayer(_defenderName);
    _metrics.setRoundAchievement(0, numberOfBattles); //All battles achieve at least round 0

    //We need to pre-evaluate xFactors now to get the correct round zero FP
    for(int p = playerNames.length - 1; p >= 0; p--) {
      String player = playerNames[p];
      FleetContents fleet = context.getFleet(player);

      //Evaluate the xFactors in all the masters prior to the battle
      //Couldnt do this in the above loop as need to set up all the fleets first
      fleet.evaluateXFactors(context);

      //Now we can record the real initial firepower values taking xfactors into account
      double initialFp = fleet.getFirepower();
      _metrics.setFirepower(player, false, 0, initialFp);
      _metrics.setFirepower(player, true, 0, initialFp);

      //Record initial population damage
      double initialPd = fleet.getFleetPd();
      _metrics.setPopulationDamage(player, false, 0, initialPd);
      _metrics.setPopulationDamage(player, true, 0, initialPd);
    }

    //Create some counters that will be used later inside the nested rounds loop
    PlayerIndexedValues hitsOnPlayer = new PlayerIndexedValues(playerNames);
    PlayerIndexedValues lostByPlayer = new PlayerIndexedValues(playerNames);
    PlayerIndexedValues removedFromPlayer = new PlayerIndexedValues(playerNames);
    //

    ///////////////////////////////
    //Now to commence the fighting!
    boolean cancelled = false;
    for(int battle = 0; battle < numberOfBattles; battle++) {
      if(battle % 6475 == 0) { //Every so many battles give other threads a breath
        try {
          Thread.sleep(250);
        } catch(Exception e) {
          /* ignore */}
      }

      if(battle < _maxNotes) {
        notes.append("\nCommencing battle " + battle + "\n");
      }

      if(cancelled) { //Check once each battle to see if user cancelled the simulation
        _metrics = null;
        _progressor.complete(null);
        return;
      }

      //Makes working copies of the fleet masters
      for(int p = playerNames.length - 1; p >= 0; p--) {
        String player = playerNames[p];
        FleetContents master = (FleetContents) masters.get(player);
        FleetContents workingCopy = new FleetContents(master);
        //fleets.put(player, workingCopy);
        context.setFleet(player, workingCopy);
        //Note that we cant evaluate xfactors here as we need to copy all the fleets first

        //Reset the defender fleet reference to the appropriate working copy for this battle
        //if( defenderName.equals(player) ) context.setDefender(workingCopy);
      }

      context.setDefendingPlayer(_defenderName);

      //Evaluate the X-Factors for use in round 1, note however that we are
      //still in round 0 (pre-battle)
      int round = 1;
      context.setRound(round);
      int activeFleets = 0;
      for(int p = playerNames.length - 1; p >= 0; p--) {
        String player = playerNames[p];
        FleetContents fleet = (FleetContents) context.getFleet(player);
        fleet.evaluateXFactors(context);
        if(fleet.getSize(true) > 0)
          activeFleets++;
      }

      //Rounds loop//////////////////////
      while(activeFleets > 1) { //Iterate rounds for a battle while there are still units on both sides

        if(battle < _maxNotes) { //Record any units with active x-factors in the notes
          for(int p = playerNames.length - 1; p >= 0; p--) { //Append notes for xfactors prior to setting current round and evaluating xfactors
                                                               //(why this order?)
            String player = playerNames[p];
            FleetContents fleet = (FleetContents) context.getFleet(player);
            appendXFList(notes, battle + "." + round + " X-Factor " + player + ": ", fleet);
          }
        }

        //Now we can set correct round into the context
        context.setRound(round);

        //Now evaluate X-Factors with the correct round in context. This is a bit redundant 
        //In the case of round 1. We also record the population damage values for each fleet here.
        for(int p = playerNames.length - 1; p >= 0; p--) {
          String player = playerNames[p];
          FleetContents fleet = (FleetContents) context.getFleet(player);
          fleet.evaluateXFactors(context);
          //PD for the round is recorded BEFORE combat starts. Even if the unit dies
          //in the round its pd will still count. Add to the total pd for each player
          //in this round. This is added to the figures accumulated from previous battles
          //for this round of battle, and after the simulation is complete these totals will
          //be divided by the appropriate number of battles to get an average result.
          _metrics.addPopulationDamage(player, false, round, fleet.getFleetPd());
        }

        //Increment the counter of battles that reached or surpassed this round
        _metrics.addRoundAchievement(round);

        //Fleets fire 'simultaneously' and we calculate the number of well targeted shots
        //and allocate these to opponents.
        hitsOnPlayer.clear();
        for(int p = playerNames.length - 1; p >= 0; p--) {
          String player = playerNames[p];
          FleetContents fleet = context.getFleet(player);
          int numberOfHits = fleet.rollHits(); //get number of successful offense rolls
          //And allocate them randomly between the opponent fleets (with the chances being
          //proportionate to fleet sizes)
          allocateHits(hitsOnPlayer, player, context, numberOfHits);
        }

        // Now that allocation of hits is complete the fleets have to brave the
        // barrages they received and roll defense rolls.
        // Failed defense rolls see casualties being accumulated. It only takes one
        // failed roll for a particular targetted unit to die, but it may have
        // more hits targetting it. These are not reallocated and are thus wasted.
        lostByPlayer.clear();
        for(int p = playerNames.length - 1; p >= 0; p--) { //Iterate players and call fireOnPlayer() for each passing in number of hits on them
          String player = playerNames[p];
          int hitCount = (int) hitsOnPlayer.getValue(player);
          FleetContents fleet = (FleetContents) context.getFleet(player);
          int deadCount = fleet.fireOnFleet(hitCount);
          lostByPlayer.setValue(player, deadCount);
        }

        //Now we remove the casualties that were accumulated
        //(must be done _after_ firing loop completed???) why? xfactors already evaluated, defense rolls dont
        //take any other fleet into consideration. I dont see why it couldnt be rolled into the one loop?
        removedFromPlayer.clear();
        for(int p = playerNames.length - 1; p >= 0; p--) {
          String player = playerNames[p];
          FleetContents fleet = (FleetContents) context.getFleet(player);
          fleet.clearCasualties();
        }

        //Done the combat part. Now record post round metrics and prep for next round
        for(int p = playerNames.length - 1; p >= 0; p--) { //Reevaluate X-Factors prior to recording post-round metrics
          String player = playerNames[p];
          FleetContents fleet = (FleetContents) context.getFleet(player);
          fleet.evaluateXFactors(context);
        }

        //The round is over
        activeFleets = 0;
        StringBuffer roundNotes = (battle < _maxNotes) ? new StringBuffer() : null;
        for(int p = playerNames.length - 1; p >= 0; p--) { //Iterate fleets recording post round firepower and determining how many still active
          String player = playerNames[p];
          FleetContents fleet = (FleetContents) context.getFleet(player);
          _metrics.addFirepower(player, false, round, fleet.getFirepower());
          if(fleet.getSize(true) > 0)
            activeFleets++;
          appendRoundNotes(roundNotes, battle, round, player, hitsOnPlayer, lostByPlayer, context.getFleets());
        }

        if(roundNotes != null) { //Append any notes we made about this round to the main notes buffer
          notes.append(roundNotes);
          notes.append('\n');
        }

        round++;
      } //End of iterating rounds for battle
      //////////////////////////////////////

      round--; //Correct back to the last round
      sumOfRounds += round;
      _metrics.addBattleLength(round);

      if(battle < _maxNotes)
        notes.append("Battle " + battle + " over. Total rounds=" + round + "\n");

      //Create flag to remember if we found the winner yet. If its a tie we consider the winner found.
      boolean foundWinnerAlready = false;
      if(activeFleets == 0) { //The battle ended in a tie with all units eliminated from all sides
        foundWinnerAlready = true;
        if(battle < _maxNotes)
          notes.append("Tied result\n");
      }
      //Find who winner is (if there was one) and record it. Also do some error checking
      //This block is executed both for ties and where there is a winner (ie: always)
      String winner = null;
      if(activeFleets <= 1) {
        for(int p = playerNames.length - 1; p >= 0; p--) {
          String player = playerNames[p];
          FleetContents fleet = (FleetContents) context.getFleet(player);
          //Examine the number of units to determine if this fleet is the winner
          //and to do some error checking. We do not count noncoms towards size.
          int numberOfCombatUnits = fleet.getSize(true);
          if(numberOfCombatUnits == 0) { //This is one of the losing fleets
            ; //no-op (this block is here as it means using only 1 'if' for most common condition)
          } else if(numberOfCombatUnits < 0) { //Sanity check to catch potential logic errors
            throw new IllegalStateException("Negative combat unit count for " + player);
          } else { //This fleet has surviving units which means that we have found the winner
            if(foundWinnerAlready)
              throw new IllegalStateException("alreadyFoundWinner");
            winner = player;
            _metrics.addWin(player);
            if(battle < _maxNotes)
              notes.append(player + " wins\n");
          }
        }
      } else { //The battle should not be over if there are still multiple players active
        throw new IllegalStateException("activeFleets=" + activeFleets + " after battle complete");
      }

      //Now we know the winner we can give them all the noncoms and then add to the survivor
      //averaging tally for each player
      FleetContents winnerFleet = winner == null ? null : context.getFleet(winner); //TODO use defender?
      for(int p = playerNames.length - 1; p >= 0; p--) {
        String player = playerNames[p];
        FleetContents fleet = context.getFleet(player);
        if(fleet != winnerFleet) { //Note that we cant add the winners survivors to the metrics until after we have iterated
                                     //all the other players fleets and confiscated their noncom units.
          fleet.transferNonCombatUnitsTo(winnerFleet);
          // Record the numbers of surviving types and units after the battle
          // for this fleet (even if the fleet lost in which case there would be zero)
          //We call it anyway as there are internal counters and stuff that also need to update
          _metrics.addSurvivors(player, fleet);
        }
      }

      if(winner != null) { //Having confiscated opponents noncom units the winners survivors may now be added
                             //to the metrics
        _metrics.addSurvivors(winner, winnerFleet);
      }

      if(_progressor != null) { //Update the battle number in the progressor for the UI
        cancelled = _progressor.setCurrentBattle(battle);
      }
    } //End of iterating battles
    /////////////////////////////

    //convert fp lists to averages
    //nb: we iterate from round 1 as already averaged stuff for pre-battle round zero

    _metrics.calculateAverages(sumOfRounds);

    String notesString = notes.toString();
    log.info(notesString); //20080213
    _metrics.setNotes(notesString);

    _metrics.setTime(System.currentTimeMillis() - startTime);

    //And use the progressor to finish up if we have one
    if(_progressor != null) {
      _progressor.complete(_metrics);
    }
  }

  /**
   * Randomly allocates hits between the fleets opposing the specified player.
   * The hits are added to those recorded in the hitsOnPlayer object. The chance
   * of a hit going to a particular fleet is proportionate to its fraction of
   * the total number of live opposing units. This method will alter the current
   * player set in the round context.
   * @param hitsOnPlayer
   * @paran player
   * @paran roundContext
   * @param hits
   *          number of hits to be allocated
   */
  protected void allocateHits(PlayerIndexedValues hitsOnPlayer, String player, RoundContext context, int numberOfHits) {
    context.setThisPlayer(player);
    FleetContents[] opponents = context.getOpposingFleets();
    String[] opponentNames = context.getOpposingNames();

    /*
     * if(opponents.length == 1) { //Shortcut. If only one opponent, they get
     * allocated all the hits hitsOnPlayer.setValue( opponentNames[0] ,
     * numberOfHits ); return; //Done! }
     */

    int sizes[] = new int[opponents.length];
    int hits[] = new int[opponents.length];
    int numberOfTargets = 0;
    for(int i = 0; i < opponents.length; i++) { // Sum the number of units (Dead units not removed yet are included as
                                                  // they are still targets this round) and build a sizes arrays.
      int size = opponents[i].getSize(true);
      numberOfTargets += size;
      sizes[i] = size;
    }
    for(int i = 0; i < numberOfHits; i++) { //Allocate each hit to a particular opponent
      int target = _rnd.nextInt(numberOfTargets);
      int targetOpponentIndex = Convert.getIndexOfItem(sizes, target);
      hits[targetOpponentIndex]++;
    }
    //Now copy the allocated hits into the hitsOnPlayer object
    //We used a temp array to hold them first to avoid all the extra addValue calls
    //and their hash lookups that doing it in the main loop would entail
    for(int i = 0; i < opponents.length; i++) {
      hitsOnPlayer.addValue(opponentNames[i], hits[i]);
    }
  }

  /**
   * Appends details about the players performance in a round of battle to the
   * specified stringbuffer if it exists. Does nothing if it does not.
   * @param roundNotes
   * @param battle
   *          battle number
   * @param round
   *          round index
   * @param player
   *          player name
   * @param hitsOnPlayer
   * @param lostByPlayer
   * @param fleets
   *          map of FleetContents keyed by player
   */
  private void appendRoundNotes(StringBuffer roundNotes, int battle, int round, String player,
      PlayerIndexedValues hitsOnPlayer, PlayerIndexedValues lostByPlayer, Map<String, FleetContents> fleets) {
    if(roundNotes != null) { //Recording detailed information on hits and losses (first few battles only)
      FleetContents fleet = (FleetContents) fleets.get(player);
      roundNotes.append(battle);
      roundNotes.append('.');
      roundNotes.append(round);
      roundNotes.append(' ');
      roundNotes.append(player);
      roundNotes.append(" took ");
      roundNotes.append((int) hitsOnPlayer.getValue(player));
      roundNotes.append(" hits lost ");
      roundNotes.append((int) lostByPlayer.getValue(player));
      roundNotes.append(" left ");
      roundNotes.append(fleet.getSize(true));
      roundNotes.append(" (fp=");
      roundNotes.append(Convert.toStr(fleet.getFirepower(), 2));
      roundNotes.append(")\n");
    }
  }

  /**
   * Appends info on xfactors to the notes string
   * @param notes
   * @param name
   * @param fleet
   */
  private void appendXFList(StringBuffer notes, String name, FleetContents fleet) {
    List<FleetElement> list = fleet.getXFactorElements();
    if(!list.isEmpty()) {
      notes.append(name);
      Iterator<FleetElement> i = list.iterator();
      while(i.hasNext()) {
        FleetElement element = (FleetElement) i.next();
        notes.append(Convert.toStr(element.getQuantity(), 0));
        notes.append('*');
        notes.append(element.getUnit().getAcronym());
        notes.append("(");
        notes.append(element.getAttacks());
        notes.append("*");
        notes.append(element.getOffense());
        notes.append("/");
        notes.append(element.getDefense());
        notes.append(")");
        if(i.hasNext()) {
          notes.append(", ");
        }
      }
      notes.append("\n");
    }
  }

  /**
   * Returns the number of battles to run
   * @return battles
   */
  @Override
  public synchronized int getNumberOfBattles() {
    return _numberOfBattles;
  }

  /**
   * Sets the number of battles to run
   * @param battles
   * @throws IllegalArgumentException
   *           if negative
   */
  @Override
  public synchronized void setNumberOfBattles(int battles) {
    if(battles < 1) {
      throw new IllegalArgumentException("numberOfBattles < 1 =" + battles);
    }
    _numberOfBattles = battles;
  }

  /**
   * Returns the battle metrics object
   * @return metrics
   */
  @Override
  public synchronized IBattleMetrics getBattleMetrics() {
    return _metrics;
  }

  /**
   * Set the progressor used by the UI to track the progress of the simulation
   * @param progressor
   */
  @Override
  public void setProgressor(IProgressor progressor) {
    _progressor = progressor;
  }

}
