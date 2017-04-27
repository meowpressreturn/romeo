package romeo.players.api;

import java.awt.Color;

import romeo.persistence.ICanGetId;
import romeo.utils.INamed;

public interface IPlayer extends INamed, ICanGetId<PlayerId> {
  /**
   * Value for the player name of the owner of unowned worlds. Romeo will just
   * import this as another player record, but gives it special treatment with
   * regards to setting a default colour.
   */
  public static final String NOBODY = "Nobody";

  /**
   * Value for the player name of worlds whose owner has been deleted from the
   * game. Romeo will import this as another player , but gives special
   * treatment with regards to allocating a default colour.
   */
  public static final String DELETED_PLAYER = "Deleted Player";

  @Override
  public PlayerId getId();

  @Override
  public String getName();

  public String getStatus();

  public String getNotes();

  public Color getColor();

  public String getTeam();

}
