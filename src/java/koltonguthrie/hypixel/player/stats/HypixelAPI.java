package koltonguthrie.hypixel.player.stats;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Set;
import koltonguthrie.hypixel.player.stats.dao.DAOFactory;

public class HypixelAPI {
    
    private String API_KEY = System.getenv("API_KEY");
    private DAOFactory daoFactory;
    
    public HypixelAPI(DAOFactory daoFactory) {
        this.daoFactory = daoFactory;
    }
    
    public void getPlayerStats(String uuid, Timestamp ts) {
        getAllSkyblockProfiles(uuid, ts);
    }
    
    private void getAllSkyblockProfiles(String uuid, Timestamp ts) {
        try {
            JsonObject obj = jsonAPIResponse("https://api.hypixel.net/v2/skyblock/profiles?uuid=" + uuid);
            
            for(Object p : ((JsonArray) obj.get("profiles"))) {
                JsonObject profile = (JsonObject) p;
                JsonObject members = (JsonObject) profile.get("members");
                JsonObject player = (JsonObject) members.get(uuid);
                JsonObject player_data = (JsonObject) player.get("player_data");
                JsonObject dungeons = (JsonObject) player.get("dungeons");
                
                BigDecimal catacombs_experience = null;
                if(dungeons != null) {
                    JsonObject dungeon_type = (JsonObject) dungeons.get("dungeon_types");
                    if(dungeon_type != null) {
                        JsonObject catacombs = (JsonObject) dungeon_type.get("catacombs");
                        if(catacombs != null) {
                            catacombs_experience = (BigDecimal) catacombs.get("experience");
                        }
                    }
                }
                
                
                JsonObject experience = (JsonObject) player_data.get("experience");
                
                if(experience == null) {
                    continue;
                }
                
                
                
                System.out.println(catacombs_experience);
                
                if(catacombs_experience != null) {
                    HashMap<String, Object> map = new HashMap<>();
                    
                    map.put("uuid", uuid);
                    map.put("gamemode", "skyblock");
                    map.put("subgamemode", profile.get("cute_name"));
                    map.put("stat_name", "SKILL_DUNGEONEERING");
                    map.put("stat_value", catacombs_experience);
                    map.put("timestamp", ts);
                    
                    daoFactory.getStats().create(map);
                    
                }
                
                for(String key : experience.keySet()) {
                    HashMap<String, Object> map = new HashMap<>();
                    if("SKILL_DUNGEONEERING".equals(key)) continue;
                    
                    map.put("uuid", uuid);
                    map.put("gamemode", "skyblock");
                    map.put("subgamemode", profile.get("cute_name"));
                    map.put("stat_name", key);
                    map.put("stat_value", experience.get(key));
                    map.put("timestamp", ts);
                    
                    daoFactory.getStats().create(map);
                }
                
            }
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private JsonObject jsonAPIResponse(String _url) throws Exception {
        if(API_KEY == null) {
            System.err.println("API KEY IS UNKNOWN");
            return null;
        }
        
        URL url = new URL(_url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("API-Key", API_KEY);

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return Jsoner.deserialize(response.toString(), new JsonObject());

    }
        
}
