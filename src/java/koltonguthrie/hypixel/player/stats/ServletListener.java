package koltonguthrie.hypixel.player.stats;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class ServletListener implements ServletContextListener {

    Main main;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("STARTED!!!!!");
        main = new Main();
        main.startTask(true);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Stop your background process when the web application is shutting down
        //YourBackgroundTask.stop();
        main.stopTask();
    }

}
