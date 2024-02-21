package koltonguthrie.hypixel.player.stats.dao;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;

public class PlayerDAO {

    private final DAOFactory daoFactory;

    final String QUERY_INSERT_PLAYER = "INSERT INTO Players (uuid) VALUES (?);";
    final String QUERY_FIND_PLAYER = "SELECT * FROM Players WHERE ( (? IS NULL OR id = ? ) AND ( ? IS NULL OR uuid = ? ) ) limit 1;";
    final String QUERY_LIST_PLAYERS = "SELECT * FROM Players;";

    PlayerDAO(DAOFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public JsonObject create(HashMap<String, Object> map) {
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

            final JsonObject p = (JsonObject) find(map).get("player");

            if (p != null) {
                json.put("success", true);
                json.put("status", HttpServletResponse.SC_OK);
                json.put("message", "Player with that UUID already exists.");
                json.put("player", p);

                return json;
            }

            ps = conn.prepareStatement(QUERY_INSERT_PLAYER, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, ((String) map.get("uuid")).replaceAll("-", QUERY_FIND_PLAYER));

            ps.executeUpdate();

            rs = ps.getGeneratedKeys();

            json.put("success", true);
            json.put("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.put("message", "Failed to created player.");

            Integer id = null;
            if (rs.next()) {
                id = rs.getInt(1);
            }

            if (id != null) {

                HashMap<String, Object> hm = new HashMap<>();
                hm.put("id", id);

                json.put("status", HttpServletResponse.SC_CREATED);
                json.put("message", "Created player.");
                json.put("player", find(hm).get("player"));

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

    public JsonObject find(HashMap<String, Object> map) {
        System.out.println(map);
        JsonObject json = new JsonObject();
        json.put("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        json.put("success", false);
        json.put("message", "An unhandled error occurred.");

        if (!((map.containsKey("uuid") && map.get("uuid") != null) || map.containsKey("id"))) {
            json.put("status", HttpServletResponse.SC_BAD_REQUEST);
            json.put("message", "Bad request.");
            return json;
        }

        Connection conn = daoFactory.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            ps = conn.prepareStatement(QUERY_FIND_PLAYER);

            if (map.containsKey("id")) {
                ps.setInt(1, (Integer) map.get("id"));
                ps.setInt(2, (Integer) map.get("id"));
            } else {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.INTEGER);
            }

            if (map.containsKey("uuid")) {
                System.out.println(map.get("uuid"));
                ps.setString(3, ((String) map.get("uuid")).replaceAll("-", ""));
                ps.setString(4, ((String) map.get("uuid")).replaceAll("-", ""));
            } else {
                ps.setNull(3, Types.VARCHAR);
                ps.setNull(4, Types.VARCHAR);
            }

            if (ps.execute()) {
                rs = ps.getResultSet();

                json.put("success", true);
                json.put("status", HttpServletResponse.SC_OK);
                json.put("message", "Unknown Player.");

                if (rs.next()) {
                    JsonObject player = new JsonObject();
                    player.put("id", rs.getInt("id"));
                    player.put("uuid", rs.getString("uuid"));
                    player.put("updated", rs.getString("updated"));

                    json.put("status", HttpServletResponse.SC_OK);
                    json.put("message", "Found player.");
                    json.put("player", player);
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

    public JsonObject list() {
        JsonObject json = new JsonObject();
        json.put("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        json.put("success", false);
        json.put("message", "An unhandled error occurred.");

        Connection conn = daoFactory.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            ps = conn.prepareStatement(QUERY_LIST_PLAYERS);

            if (ps.execute()) {
                rs = ps.getResultSet();

                json.put("success", true);
                json.put("status", HttpServletResponse.SC_OK);
                json.put("message", "No players.");

                JsonArray players = new JsonArray();

                int count = 0;
                if (rs.next()) {
                    JsonObject player = new JsonObject();
                    player.put("id", rs.getInt("id"));
                    player.put("uuid", rs.getString("uuid"));
                    player.put("updated", rs.getString("updated"));

                    json.put("status", HttpServletResponse.SC_OK);
                    json.put("message", "Found players.");

                    players.add(player);
                    count++;
                }

                json.put("players", players);
                json.put("count", count);

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
