package koltonguthrie.hypixel.player.stats.dao;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;

public class StatsDAO {

    private final DAOFactory daoFactory;

    final String QUERY_INSERT_STAT = "INSERT INTO stats (player_id, gamemode, subgamemode, stat_name, stat_value, timestamp) VALUES (?,?,?,?,?,?);";
    final String QUERY_FIND_STAT = " SELECT * FROM (SELECT *, CONVERT_TZ(`timestamp`, @@session.time_zone, '+00:00') AS `utc_timestamp`, CONVERT_TZ(`timestamp`, @@session.time_zone, '-05:00') AS `est_timestamp` FROM stats) as subquery"
                                 + " WHERE ( (? IS NULL OR id = ? ) AND ( ? IS NULL OR player_id = ? ) AND ( ? IS NULL OR gamemode = ? ) AND ( ? IS NULL OR subgamemode = ? ) AND ( ? IS NULL OR stat_name = ? ) AND ( ? IS NULL OR stat_value = ? ) AND ( ? IS NULL OR `utc_timestamp` = ? ) ) limit 1;";
    final String QUERY_LIST_STAT = " SELECT * FROM (SELECT *, CONVERT_TZ(`timestamp`, @@session.time_zone, '+00:00') AS `utc_timestamp`, CONVERT_TZ(`timestamp`, @@session.time_zone, '-05:00') AS `est_timestamp` FROM stats) as subquery"
                                 + " WHERE ( (? IS NULL OR id = ? ) AND ( ? IS NULL OR player_id = ? ) AND ( ? IS NULL OR gamemode = ? ) AND ( ? IS NULL OR subgamemode = ? ) AND ( ? IS NULL OR stat_name = ? ) AND ( ? IS NULL OR stat_value = ? ) AND ( ? IS NULL OR `utc_timestamp` >= ? ) AND ( ? IS NULL OR `utc_timestamp` <= ? ) ) ORDER BY `utc_timestamp`;";
    final String QUERY_LIST_GAMEMODES = "SELECT DISTINCT gamemode FROM stats WHERE player_id = ?;";
    final String QUERY_LIST_SUBGAMEMODES = "SELECT DISTINCT gamemode, subgamemode FROM stats WHERE player_id = ?;";
    
    StatsDAO(DAOFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public JsonObject find(HashMap<String, Object> map) {
        JsonObject json = new JsonObject();
        json.put("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        json.put("success", false); 
        json.put("message", "An unhandled error occurred.");

        if (!map.containsKey("uuid")) {
            json.put("status", HttpServletResponse.SC_BAD_REQUEST);
            json.put("message", "Bad request.");
            return json;
        }

        Connection conn = daoFactory.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            ps = conn.prepareStatement(QUERY_FIND_STAT);

            if (map.containsKey("id")) {
                ps.setInt(1, (Integer) map.get("id"));
                ps.setInt(2, (Integer) map.get("id"));
            } else {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.INTEGER);
            }

            if (map.containsKey("uuid")) {
                JsonObject p = (JsonObject) daoFactory.getPlayer().find(map).get("player");
                if (p == null) {
                    json.put("success", true);
                    json.put("status", HttpServletResponse.SC_OK);
                    json.put("message", "Player with that UUID does not exist.");

                    return json;
                }
                
                ps.setInt(3, (Integer) p.get("id"));
                ps.setInt(4, (Integer) p.get("id"));

            } else {
                ps.setNull(3, Types.INTEGER);
                ps.setNull(4, Types.INTEGER);
            }
            
            if (map.containsKey("gamemode")) {
                ps.setString(5, (String) map.get("gamemode"));
                ps.setString(6, (String) map.get("gamemode"));
            } else {
                ps.setNull(5, Types.VARCHAR);
                ps.setNull(6, Types.VARCHAR);
            }
            
            if (map.containsKey("subgamemode")) {
                ps.setString(7, (String) map.get("subgamemode"));
                ps.setString(8, (String) map.get("subgamemode"));
            } else {
                ps.setNull(7, Types.VARCHAR);
                ps.setNull(8, Types.VARCHAR);
            }

            if (map.containsKey("name")) {
                ps.setString(9, (String) map.get("name"));
                ps.setString(10, (String) map.get("name"));
            } else {
                ps.setNull(9, Types.VARCHAR);
                ps.setNull(10, Types.VARCHAR);
            }

            if (map.containsKey("value")) {
                ps.setBigDecimal(11, (BigDecimal) map.get("value"));
                ps.setBigDecimal(12, (BigDecimal) map.get("value"));
            } else {
                ps.setNull(11, Types.DECIMAL);
                ps.setNull(12, Types.DECIMAL);
            }

            if (map.containsKey("start") && map.containsKey("end")) {
                ps.setTimestamp(13, new Timestamp(Long.parseLong((String) map.get("start"))));
                ps.setTimestamp(14, new Timestamp(Long.parseLong((String) map.get("start"))));
                
                ps.setTimestamp(15, new Timestamp(Long.parseLong((String) map.get("end"))));
                ps.setTimestamp(16, new Timestamp(Long.parseLong((String) map.get("end"))));
            } else {
                ps.setNull(13, Types.TIMESTAMP);
                ps.setNull(14, Types.TIMESTAMP);
                ps.setNull(15, Types.TIMESTAMP);
                ps.setNull(16, Types.TIMESTAMP);
            }

            if (ps.execute()) {
                rs = ps.getResultSet();

                json.put("success", true);
                json.put("status", HttpServletResponse.SC_OK);
                json.put("message", "Unknown stat.");

                if (rs.next()) {
                    JsonObject stat = new JsonObject();
                    stat.put("id", rs.getInt("id"));
                    stat.put("uuid", rs.getString("player_id"));
                    stat.put("gamemode", rs.getString("gamemode"));
                    stat.put("name", rs.getString("stat_name"));
                    stat.put("value", rs.getBigDecimal("stat_value"));
                    stat.put("timestamp", rs.getString("est_timestamp"));
                    stat.put("utc_timestamp", rs.getString("utc_timestamp"));

                    json.put("status", HttpServletResponse.SC_OK);
                    json.put("message", "Found stat.");
                    json.put("stat", stat);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                    rs = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                    ps = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return json;
    }

    public JsonObject list(HashMap<String, Object> map) {
        JsonObject json = new JsonObject();
        json.put("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        json.put("success", false);
        json.put("message", "An unhandled error occurred.");
        
        if (!map.containsKey("uuid")) {
            json.put("status", HttpServletResponse.SC_BAD_REQUEST);
            json.put("message", "Bad request.");
            return json;
        }

        Connection conn = daoFactory.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            ps = conn.prepareStatement(QUERY_LIST_STAT);

            if (map.containsKey("id")) {
                ps.setInt(1, (Integer) map.get("id"));
                ps.setInt(2, (Integer) map.get("id"));
            } else {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.INTEGER);
            }

            if (map.containsKey("uuid")) {
                JsonObject p = (JsonObject) daoFactory.getPlayer().find(map).get("player");
                if (p == null) {
                    json.put("success", true);
                    json.put("status", HttpServletResponse.SC_OK);
                    json.put("message", "Player with that UUID does not exist.");

                    return json;
                }
                
                ps.setInt(3, (Integer) p.get("id"));
                ps.setInt(4, (Integer) p.get("id"));

            } else {
                ps.setNull(3, Types.INTEGER);
                ps.setNull(4, Types.INTEGER);
            }
            
            if (map.containsKey("gamemode")) {
                ps.setString(5, (String) map.get("gamemode"));
                ps.setString(6, (String) map.get("gamemode"));
            } else {
                ps.setNull(5, Types.VARCHAR);
                ps.setNull(6, Types.VARCHAR);
            }
            
            if (map.containsKey("subgamemode")) {
                ps.setString(7, (String) map.get("subgamemode"));
                ps.setString(8, (String) map.get("subgamemode"));
            } else {
                ps.setNull(7, Types.VARCHAR);
                ps.setNull(8, Types.VARCHAR);
            }

            if (map.containsKey("name")) {
                ps.setString(9, (String) map.get("name"));
                ps.setString(10, (String) map.get("name"));
            } else {
                ps.setNull(9, Types.VARCHAR);
                ps.setNull(10, Types.VARCHAR);
            }

            if (map.containsKey("value")) {
                ps.setBigDecimal(11, (BigDecimal) map.get("value"));
                ps.setBigDecimal(12, (BigDecimal) map.get("value"));
            } else {
                ps.setNull(11, Types.DECIMAL);
                ps.setNull(12, Types.DECIMAL);
            }

            if (map.containsKey("start") && map.containsKey("end")) {
                ps.setTimestamp(13, new Timestamp(Long.parseLong((String) map.get("start"))));
                ps.setTimestamp(14, new Timestamp(Long.parseLong((String) map.get("start"))));
                
                ps.setTimestamp(15, new Timestamp(Long.parseLong((String) map.get("end"))));
                ps.setTimestamp(16, new Timestamp(Long.parseLong((String) map.get("end"))));
            } else {
                ps.setNull(13, Types.TIMESTAMP);
                ps.setNull(14, Types.TIMESTAMP);
                ps.setNull(15, Types.TIMESTAMP);
                ps.setNull(16, Types.TIMESTAMP);
            }
            
            if (ps.execute()) {
                JsonArray stats = new JsonArray();
                rs = ps.getResultSet();

                json.put("success", true);
                json.put("status", HttpServletResponse.SC_OK);
                json.put("message", "Unknown stats.");

                while (rs.next()) {
                    JsonObject stat = new JsonObject();
                    
                    stat.put("id", rs.getInt("id"));
                    stat.put("uuid", rs.getString("player_id"));
                    stat.put("gamemode", rs.getString("gamemode"));
                    stat.put("name", rs.getString("stat_name"));
                    stat.put("value", rs.getBigDecimal("stat_value"));
                    stat.put("timestamp", rs.getString("est_timestamp"));
                    stat.put("utc_timestamp", rs.getString("utc_timestamp"));

                    json.put("status", HttpServletResponse.SC_OK);
                    json.put("message", "Found stats.");
                    
                    stats.add(stat);
                    
                }
                
                if(!stats.isEmpty()) json.put("stats", stats);

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                    rs = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                    ps = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return json;
    }

    public JsonObject create(HashMap<String, Object> map) {
        JsonObject json = new JsonObject();
        json.put("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        json.put("success", false);
        json.put("message", "An unhandled error occurred.");

        if (!map.containsKey("uuid") || !map.containsKey("gamemode") || !map.containsKey("stat_name") || !map.containsKey("stat_value")) {
            json.put("status", HttpServletResponse.SC_BAD_REQUEST);
            json.put("message", "Bad request.");
            return json;
        }

        Connection conn = daoFactory.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            JsonObject player = (JsonObject) daoFactory.getPlayer().find(map).get("player");

            if (player == null) {
                json.put("success", true);
                json.put("status", HttpServletResponse.SC_OK);
                json.put("message", "Player with that UUID does not exist.");

                return json;
            }

            ps = conn.prepareStatement(QUERY_INSERT_STAT, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, (Integer) player.get("id"));
            ps.setString(2, (String) map.get("gamemode"));
            ps.setString(3, (String) map.get("subgamemode"));
            ps.setString(4, (String) map.get("stat_name"));
            ps.setBigDecimal(5, (BigDecimal) map.get("stat_value"));
            if(map.containsKey("timestamp")) {
                ps.setTimestamp(6, (Timestamp) map.get("timestamp"));
            } else {
                ps.setTimestamp(6, new Timestamp(new Date().getTime()));
            }

            if (ps.executeUpdate() > 0) {

                rs = ps.getGeneratedKeys();

                json.put("success", true);
                json.put("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                json.put("message", "Failed to created stat.");

                Integer id = null;
                if (rs.next()) {
                    id = rs.getInt(1);
                }

                if (id != null) {

                    HashMap<String, Object> hm = new HashMap<>();
                    hm.put("id", id);

                    json.put("status", HttpServletResponse.SC_CREATED);
                    json.put("message", "Created stat.");
                    json.put("stat", find(hm).get("stat"));

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                    rs = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                    ps = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return json;
    }
    
    public JsonObject listGamemodes(String uuid) {
        
        HashMap<String, Object> map = new HashMap();
        map.put("uuid", uuid);
        
        JsonObject json = new JsonObject();
        json.put("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        json.put("success", false);
        json.put("message", "An unhandled error occurred.");

        Connection conn = daoFactory.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            ps = conn.prepareStatement(QUERY_LIST_GAMEMODES);


                JsonObject p = (JsonObject) daoFactory.getPlayer().find(map).get("player");
                if (p == null) {
                    json.put("success", true);
                    json.put("status", HttpServletResponse.SC_OK);
                    json.put("message", "Player with that UUID does not exist.");

                    return json;
                }
                
                ps.setInt(1, (Integer) p.get("id"));

            if (ps.execute()) {
                JsonArray gamemodes = new JsonArray();
                rs = ps.getResultSet();

                json.put("success", true);
                json.put("status", HttpServletResponse.SC_OK);
                json.put("message", "No gamemodes.");

                while (rs.next()) {
                    gamemodes.add(rs.getString("gamemode"));
                    json.put("status", HttpServletResponse.SC_OK);
                    json.put("message", "Found " + gamemodes.size() + " gamemode(s).");
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                    rs = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                    ps = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return json;
    }


}
