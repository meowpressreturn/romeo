package romeo.players.impl;

import java.awt.Color;
import java.util.Objects;

import romeo.persistence.IdBean;
import romeo.players.api.IPlayer;
import romeo.players.api.PlayerId;
import romeo.players.api.PlayerUtils;

/**
 * Value object to hold data about a player.
 * As of 0.6.3 this is now immutable.
 */
public class PlayerImpl extends IdBean<PlayerId> implements IPlayer {
  
  private String _name = "";
  private String _status = "";
  private String _notes = "";
  private Color _color = PlayerUtils.SOMEBODY_COLOR;
  private String _team ="";

  /**
   * No-args constructor. Default color will be set to red
   */
  public PlayerImpl() {
    ;
  }
  
  /**
   * Constructs a player with the specified id and other properties will be read from the source.
   * @param id may be null (indicates new record)
   * @param source IPlayer to read values from , may not be null
   */
  public PlayerImpl(PlayerId id, IPlayer source) {
    this(id, source.getName(), source.getStatus(), source.getNotes(), source.getColor(), source.getTeam());
  }
  
  /**
   * Constructor.
   * Apart from the id, all parameters must not be null, although empty strings are permitted.
   * @param id if null indicates this is a new record not yet persisted
   * @param name
   * @param status
   * @param notes
   * @param color
   * @param team
   */
  public PlayerImpl(PlayerId id, String name, String status, String notes, Color color, String team) {
    setId(id);
    _name = Objects.requireNonNull(name, "name may not be null").trim();
    _status = Objects.requireNonNull(status, "status may not be null");
    _notes = Objects.requireNonNull(notes, "notes may not be null");
    _color = Objects.requireNonNull(color, "color may not be null");
    _team = Objects.requireNonNull(team, "team may not be null").trim();  
  }

  /**
   * Gets the players name
   * @return name
   */
  @Override
  public String getName() {
    return _name;
  }

  /**
   * Gets the players status string. This is free text whose meaning is up to
   * the user. We just persist it.
   * @return status
   */
  @Override
  public String getStatus() {
    return _status;
  }

  /**
   * Return the notes for this player
   * @return notes
   */
  @Override
  public String getNotes() {
    return _notes;
  }

  /**
   * Return the color for this player. This should never be null.
   * @return color
   */
  @Override
  public Color getColor() {
    return _color;
  }

  /**
   * Returns the name of the player's team (if any, may be null)
   * @return team
   */
  @Override
  public String getTeam() {
    return _team;
  }

}
