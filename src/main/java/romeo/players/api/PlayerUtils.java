package romeo.players.api;

import java.awt.Color;

import org.apache.commons.logging.LogFactory;

import romeo.utils.Convert;
import romeo.worlds.impl.WorldServiceImpl;

public class PlayerUtils {
  
  /**
   * Default color for a Nobody world (eg: a certain shade of yellow)
   */
  public static final Color NOBODY_COLOR;
  static { //Attempt to set nobody color default, fall back to yellow if that
             //color doesnt work (which on some systems it seems not to for reasons I
           //havent been able to understand yet as I don't have one to test with)
    Color c = Color.YELLOW;
    try {
      c = new Color(255, 204, 102);
    } catch(Exception e) {
      LogFactory.getLog(WorldServiceImpl.class).error("Failed to correctly set NOBODY_COLOR", e);
    } finally {
      NOBODY_COLOR = c; //ugly hack is ugly
    }
  }

  /**
   * Default color for a non-nobody world. (eg: Red)
   */
  public static final Color SOMEBODY_COLOR;
  static {
    Color c = Color.RED;
    try {
      c = new Color(255, 0, 0);
    } catch(Exception e) {
      LogFactory.getLog(WorldServiceImpl.class).error("Failed to correctly set SOMEBODY_COLOR", e);
    } finally {
      SOMEBODY_COLOR = c;
    }
  }

  public static final Color[] TEAM_COLORS;
  static {
    Color[] c = new Color[8];
    c[0] = NOBODY_COLOR;
    c[1] = Color.RED;
    c[2] = Color.BLUE;
    c[3] = Color.GREEN;
    c[4] = Color.CYAN;
    c[5] = Color.MAGENTA;
    c[6] = Color.ORANGE;
    c[7] = Color.GRAY;
    try {
      c[1] = new Color(255, 0, 53); //red, but not somebody red
      c[2] = new Color(102, 102, 255); //blue, but not so sweet
      c[3] = new Color(102, 255, 102); //a cleaner green
      c[4] = new Color(153, 204, 255); //cyan with a bit less cyanide
      c[5] = new Color(255, 102, 153); //sort of unmanly pink. Thats vaguely magenta right?
      c[6] = new Color(204, 153, 0); //khakiesque
      c[7] = new Color(153, 153, 102); //grayish, kinda
    } catch(Exception e) {
      LogFactory.getLog(WorldServiceImpl.class).error("Failed to correctly set TEAM_COLORS", e);
    } finally {
      TEAM_COLORS = c;
    }
  }

  /**
   * Given a team name will return a colour that can be used as its team colour.
   * In the expected case of team 'names' that are actually just an int value it
   * will use this as a (wrapped) index into the predefined list of teams
   * colours. For other strings it will just take the hashCode of the string and
   * use that as the (wrapped) index - which will probably lead to many
   * collisions as this method doesnt bother to see if that value was already
   * allocated to a different team. Team 0 is considered to be Nobody and its
   * colour set accordingly (index wrapping excludes 0). The explicit
   * case-insensitive String "Nobody" will also be treated as the Nobody team.
   * @param team
   *          a team name
   * @return a team color
   */
  public static Color getTeamColor(String team) {
    if(team == null || team.isEmpty()) {
      return TEAM_COLORS[0];
    }
    if("0".equals(team) || "Nobody".equalsIgnoreCase(team)) {
      return TEAM_COLORS[0];
    }

    int index = Convert.toInt(team); //with team 0 handled above, a zero here indicates NaN
    if(index < 1) { //For non numeric (or negative!) teams we just make something up
      index = team.hashCode();
    }
    if(index >= TEAM_COLORS.length) {
      index = (index % (TEAM_COLORS.length - 1)) + 1;
    }
    return TEAM_COLORS[index];
  }
}
