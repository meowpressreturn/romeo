package romeo.worlds.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Objects;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import romeo.utils.DbUtils;

/**
 * Provides some methods that retrieve data directly from the database and
 * return datasets for charts.
 */
public class HistoryChartsHelper {
  private DataSource _dataSource;
  private String _worldTurnSql;
  private String _teamWorldsSql;
  private String _teamFirepowerSql;
  private String _playerHistorySql;
  private String _teamLabourSql;
  private String _teamCapitalSql;

  public HistoryChartsHelper(DataSource dataSource) {
    _dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
  }

  public XYDataset getPlayerHistoryDataset(Set<String> players, String statSql) {
    String sql = Objects.requireNonNull(_playerHistorySql, "playerHistorySql may not be null");
    sql = StringUtils.replace(sql, "#STAT#", statSql);
    return buildOwnerTurnDataset(players, sql);
  }

  /**
   * Build an XYDataset with turns along the X axis (from column "turn"), and
   * the statistic of interest (in "value" column) on the Y. Each player
   * ("owner" column) is a series. Only players included in the players set
   * passed in will be included in the results. The sql must return results
   * ordered by team, then turn.
   * @param players
   * @param sql
   * @return dataset
   */
  private XYDataset buildOwnerTurnDataset(Set<String> players, String sql) {
    Objects.requireNonNull(sql, "sql may not be null");
    XYSeriesCollection dataset = new XYSeriesCollection();
    if(players == null || players.isEmpty()) { //Dont hit the DB if we arent going to assemble any data
      return dataset;
    }
    XYSeries series = null;
    String player = null;
    try {
      Connection connection = _dataSource.getConnection();
      try {
        ResultSet rs = DbUtils.readQuery(sql, null, connection);
        while(rs.next()) {
          String thisPlayer = rs.getString("owner");
          if(players.contains(thisPlayer)) {
            if(!thisPlayer.equals(player)) { //New series
              player = thisPlayer;
              if(series != null) {
                dataset.addSeries(series);
              }
              series = new XYSeries(player, false, true);
            }
            series.add((double) rs.getInt("turn"), rs.getDouble("value"));
          }
        }
        if(series != null) {
          dataset.addSeries(series);
        } //Add last series
      } finally {
        connection.close();
      }
    } catch(Exception e) {
      throw new RuntimeException("Error preparing owner turn dataset from sql " + sql, e);
    }
    return dataset;
  }

  /**
   * Build an XYDataset with turns ("turn" column) along X, and the statistic of
   * interest along Y (as identified by column parameter). A series is added for
   * each team, identified by "team" column. The sql must return results ordered
   * by team, then turn.
   * @param sql
   * @param column
   * @return
   */
  private XYDataset buildTeamTurnDataset(String sql, String column) {
    Objects.requireNonNull(sql, "sql may not be null");
    Objects.requireNonNull(column, "column may not be null");
    XYSeriesCollection dataset = new XYSeriesCollection();
    XYSeries series = null;
    try {
      Connection connection = _dataSource.getConnection();
      try {
        ResultSet rs = DbUtils.readQuery(sql, null, connection);
        //Results are ordered by team, so we do all the entries for all turns of a team
        //and then move onto the next team. Each team is thus a series, with an item for
        //each turn. 
        String team = null;
        while(rs.next()) {
          String thisTeam = rs.getString("team");
          if(this != null && thisTeam != null && !thisTeam.isEmpty()) {
            if(!thisTeam.equals(team)) { //New series
              team = thisTeam;
              if(series != null) {
                dataset.addSeries(series);
              }
              series = new XYSeries(team, false, true);
            }
            series.add((double) rs.getInt("turn"), rs.getDouble(column));
          }
        }
        if(series != null) {
          dataset.addSeries(series);
        } //Add last series
      } finally {
        connection.close();
      }
    } catch(Exception e) {
      throw new RuntimeException("Error preparing team turn dataset for " + column, e);
    }
    return dataset;
  }

  /**
   * Returns a dataset of team world counts by turn
   * @return dataset
   */
  public XYDataset getTeamWorldsDataset() {
    String sql = Objects.requireNonNull(_teamWorldsSql, "teamWorldsSql may not be null");
    return buildTeamTurnDataset(sql, "worlds");
  }

  /**
   * Queries the database and returns a Dataset of team visible firepower for
   * each turn.
   * @return
   */
  public XYDataset getTeamFirepowerDataset() {
    String sql = Objects.requireNonNull(_teamFirepowerSql, "teamFirepowerSql may not be null");
    return buildTeamTurnDataset(sql, "firepower");
  }

  public XYDataset getTeamLabourDataset() {
    String sql = Objects.requireNonNull(_teamLabourSql, "teamLabourSql may not be null");
    return buildTeamTurnDataset(sql, "labour");
  }

  public XYDataset getTeamCapitalDataset() {
    String sql = Objects.requireNonNull(_teamCapitalSql, "teamCapitalSql may not be null");
    return buildTeamTurnDataset(sql, "capital");
  }

  public String getWorldTurnSql() {
    return _worldTurnSql;
  }

  public void setWorldTurnSql(String worldTurnSql) {
    _worldTurnSql = worldTurnSql;
  }

  public String getTeamWorldsSql() {
    return _teamWorldsSql;
  }

  public void setTeamWorldsSql(String teamWorldsSql) {
    _teamWorldsSql = teamWorldsSql;
  }

  public String getTeamFirepowerSql() {
    return _teamFirepowerSql;
  }

  public void setTeamFirepowerSql(String teamFirepowerSql) {
    _teamFirepowerSql = teamFirepowerSql;
  }

  public String getPlayerHistorySql() {
    return _playerHistorySql;
  }

  public void setPlayerHistorySql(String playerHistorySql) {
    _playerHistorySql = playerHistorySql;
  }

  public String getTeamLabourSql() {
    return _teamLabourSql;
  }

  public void setTeamLabourSql(String _teamLabourSql) {
    this._teamLabourSql = _teamLabourSql;
  }

  public String getTeamCapitalSql() {
    return _teamCapitalSql;
  }

  public void setTeamCapitalSql(String _teamCapitalSql) {
    this._teamCapitalSql = _teamCapitalSql;
  }

}
