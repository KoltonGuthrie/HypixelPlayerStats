package koltonguthrie.hypixel.player.stats;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import koltonguthrie.hypixel.player.stats.dao.DAOFactory;

public class Main {
    
    private ScheduledExecutorService scheduler = null;
    private DAOFactory daoFactory;
    private HypixelAPI API;

    
    public Main() {
        daoFactory = new DAOFactory();
        API = new HypixelAPI(daoFactory);
        
    }
    
    public DAOFactory getDAOFactory() {
        return daoFactory;
    }
    
    public boolean startTask(boolean runAtStart) {
        if(scheduler == null) {
            scheduleDailyTask(runAtStart);
            return true;
        }
        return false;
    } 
    
    public boolean stopTask() {
        try {
            scheduler.shutdown();
            return true;
        } catch(Exception e) {
            return false;
        }
    }
    
    private void updateData() {
        String[] uuids = {"bf8846c7-7f16-4174-a4f7-ebd979d6641f"};
        for(String uuid : uuids) {
            API.getPlayerStats(uuid.replaceAll("-", ""));
        }
    }

    private void scheduleDailyTask(boolean runAtStart) {
        
        if(runAtStart) {
            updateData();
        }
        
        scheduler = Executors.newScheduledThreadPool(1);

        Calendar estCalendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));

        long initialDelay = calculateInitialDelay(estCalendar);

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("Executing daily task at: " + new Date());
            }
        }, initialDelay, 24 * 60 * 60 * 1000, TimeUnit.MILLISECONDS);
    }

    private long calculateInitialDelay(Calendar estCalendar) {
        estCalendar.set(Calendar.HOUR_OF_DAY, 0);
        estCalendar.set(Calendar.MINUTE, 0);
        estCalendar.set(Calendar.SECOND, 0);
        estCalendar.set(Calendar.MILLISECOND, 0);

        long currentTimeMillis = System.currentTimeMillis();
        long nextMidnightMillis = estCalendar.getTimeInMillis();

        if (nextMidnightMillis <= currentTimeMillis) {
            estCalendar.add(Calendar.DAY_OF_MONTH, 1);
            nextMidnightMillis = estCalendar.getTimeInMillis();
        }

        return nextMidnightMillis - currentTimeMillis;
    }
    
    
}
