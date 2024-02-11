package koltonguthrie.hypixel.player.stats.dao;

import com.github.cliftonlabs.json_simple.JsonObject;
import java.util.HashMap;

public class StatsDAO {
    
    private final DAOFactory daoFactory;

    StatsDAO(DAOFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public JsonObject list(HashMap<String, Object> map) {
        return new JsonObject();
    }

    public JsonObject create(HashMap<String, Object> map) {
        return new JsonObject();
    }
    
}
