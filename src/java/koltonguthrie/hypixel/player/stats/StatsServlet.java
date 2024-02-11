package koltonguthrie.hypixel.player.stats;

import com.github.cliftonlabs.json_simple.Jsoner;
import java.io.PrintWriter;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import koltonguthrie.hypixel.player.stats.dao.DAOFactory;

public class StatsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        DAOFactory daoFactory = getDAOFactory(request);

        response.setContentType("application/json;charset=UTF-8");

        try ( PrintWriter out = response.getWriter()) {
            
            HashMap<String, Object> map = new HashMap<>();
            
            if(request.getParameter("uuid") != null) map.put("uuid", request.getParameter("uuid"));
            if(request.getParameter("gamemode") != null) map.put("gamemode", request.getParameter("gamemode"));
            if(request.getParameter("name") != null) map.put("name", request.getParameter("name"));
            if(request.getParameter("value") != null) map.put("value", request.getParameter("value"));
            if(request.getParameter("timestamp") != null) map.put("timestamp", request.getParameter("timestamp"));
            
            if(request.getParameter("all") != null && request.getParameter("all").equals("true")) {
                out.write(Jsoner.serialize(daoFactory.getStats().list(map)));
            } else {
                out.write(Jsoner.serialize(daoFactory.getStats().find(map)));
            }
                
            

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        DAOFactory daoFactory = getDAOFactory(request);

        response.setContentType("application/json;charset=UTF-8");

        try ( PrintWriter out = response.getWriter()) {

            HashMap<String, Object> map = getParameters(request);
            
            out.write(Jsoner.serialize(daoFactory.getStats().create(map)));
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private DAOFactory getDAOFactory(HttpServletRequest r) {
        
        DAOFactory daoFactory = null;
        ServletContext context = r.getServletContext();

        if (context.getAttribute("daoFactory") == null) {
            System.err.println("*** Creating New DAOFactory Instance ...");
            daoFactory = new DAOFactory();
            context.setAttribute("daoFactory", daoFactory);
        }
        else {
            daoFactory = (DAOFactory) context.getAttribute("daoFactory");
        }
        
        return daoFactory;
        
    }
    
    private HashMap<String, Object> getParameters(HttpServletRequest request) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String p = URLDecoder.decode(br.readLine().trim(), Charset.defaultCharset());
        HashMap<String , Object> parameters = new HashMap<>();
        String[] pairs = p.trim().split("&");
        System.out.println(p);
        for (int i = 0; i < pairs.length; ++i) {
            String[] pair = pairs[i].split("=");
            parameters.put(pair[0], pair[1]);
        }
        return parameters;
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

}