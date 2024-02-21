

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import java.util.HashMap;
import koltonguthrie.hypixel.player.stats.dao.DAOFactory;
import koltonguthrie.hypixel.player.stats.dao.PlayerDAO;
import junit.framework.TestCase;
import org.junit.*;
import static org.junit.Assert.*;

public class PlayerDAOTest {
    
    private DAOFactory daoFactory;

    @Before
    public final void setUp() {
        
        daoFactory = new DAOFactory();

    }
    
    @Test
    public void testCreatePlayer1() throws JsonException {
        final String ALREADY_CREATED_PLAYER_MESSAGE = "Player with that UUID already exists.";
        
        if(daoFactory.isClosed()) {
            fail("Connection is closed.");
            return;
        }
        
        final String uuid = "TEST-CREATE-PLAYER-1-UUID";
        
        PlayerDAO playerDAO = daoFactory.getPlayer();
        
        HashMap<String, Object> map = new HashMap();
        map.put("uuid", uuid);
        
        JsonObject createJson = (JsonObject) playerDAO.create(map);

        if(createJson == null) {
            fail("Did not create player.");
            return;

        }
        
        if(createJson.get("message").equals(ALREADY_CREATED_PLAYER_MESSAGE)) {
            fail("Player already exists!");
            return;
        }
        
        Integer id = (Integer) ((JsonObject) createJson.get("player")).get("id");
        
        HashMap<String, Object> m = new HashMap();
        m.put("id", id);
        System.out.println(createJson);
        
        JsonObject findJson = (JsonObject) daoFactory.getPlayer().find(m).get("player");

        assertEquals(findJson, createJson);
        
    }

}
