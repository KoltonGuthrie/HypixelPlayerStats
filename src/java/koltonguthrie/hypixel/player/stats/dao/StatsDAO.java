package koltonguthrie.hypixel.player.stats.dao;

import com.github.cliftonlabs.json_simple.JsonObject;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

public class StatsDAO {
    
    private final DAOFactory daoFactory;
    
    final String QUERY_INSERT_STAT = "INSERT INTO stats (player_id, gamemode, stat_name, stat_value) VALUES (?,?,?,?);";
    
    StatsDAO(DAOFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public JsonObject find(HashMap<String, Object> map) {
        return new JsonObject();
    }
    
    public JsonObject list(HashMap<String, Object> map) {
        return new JsonObject();
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

            System.out.println(player);
            
            ps.setInt(1, (Integer) player.get("id"));
            ps.setString(2, (String) map.get("gamemode"));
            ps.setString(3, (String) map.get("stat_name"));
            ps.setFloat(4, Float.parseFloat((String) map.get("stat_value")));

            if(ps.executeUpdate() > 0) {

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
    
}
