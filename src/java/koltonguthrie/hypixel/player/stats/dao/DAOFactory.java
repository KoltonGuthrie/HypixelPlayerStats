package koltonguthrie.hypixel.player.stats.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NoInitialContextException;
import javax.sql.DataSource;

public final class DAOFactory {

    private static final String PREFIX = "hypixel_stats";
    private static final String PROPERTY_ENVCONTEXT = "envcontext";
    private static final String PROPERTY_INITCONTEXT = "initcontext";

    private Connection conn = null;

    public DAOFactory() {
        DAOProperties properties = new DAOProperties(PREFIX);

        String envcontext = properties.getProperty(PROPERTY_ENVCONTEXT);
        String initcontext = properties.getProperty(PROPERTY_INITCONTEXT);

        try {
            // Check if the InitialContext class is available (indicating a Java EE environment)
            Class.forName("javax.naming.InitialContext");

            // If available, use JNDI and DataSource
            Context envContext = new InitialContext();
            Context initContext = (Context) envContext.lookup(envcontext);
            DataSource ds = (DataSource) initContext.lookup(initcontext);
            if (ds != null) {
                this.conn = ds.getConnection();
            }
        } catch (ClassNotFoundException | NoInitialContextException e) {
            // If not available (not in a Java EE environment), use DriverManager
            String url = properties.getProperty("url");
            String user = properties.getProperty("user");
            String password = properties.getProperty("password");

            try {
                this.conn = DriverManager.getConnection(url, user, password);
            } catch (SQLException ex) {
                conn = null;
                ex.printStackTrace();
            }
        } catch (Exception e) {
            conn = null;
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return conn;
    }

    public boolean isClosed() {

        boolean isClosed = true;

        try {
            isClosed = conn.isClosed();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isClosed;

    }

    public PlayerDAO getPlayer() {
        return new PlayerDAO(this);
    }

    public StatsDAO getStats() {
        return new StatsDAO(this);
    }

}
