package koltonguthrie.hypixel.player.stats;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class HypixelAPI {
    
    static String API_KEY = System.getenv("API_KEY");
    
    public static void getPlayerStats(String uuid) {
        getAllSkyblockProfiles(uuid);
    }
    
    private static void getAllSkyblockProfiles(String uuid) {
        try {
            JsonObject obj = jsonAPIResponse("https://api.hypixel.net/v2/skyblock/profiles?uuid=" + uuid);
            
            for(Object p : ((JsonArray) obj.get("profiles"))) {
                JsonObject profile = (JsonObject) p;
                JsonObject members = (JsonObject) profile.get("members");
                JsonObject player = (JsonObject) members.get(uuid);
                JsonObject player_data = (JsonObject) player.get("player_data");
                JsonObject experience = (JsonObject) player_data.get("experience");
                
                System.out.println(profile.get("cute_name"));
                System.out.println(experience);
            }
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private static JsonObject jsonAPIResponse(String _url) throws Exception {
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
