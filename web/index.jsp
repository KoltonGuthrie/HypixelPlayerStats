<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="jakarta.servlet.ServletContext"%>
<%@page import="jakarta.servlet.http.*"%>
<%@page import="koltonguthrie.hypixel.player.stats.dao.DAOFactory"%>
<%@page import="com.github.cliftonlabs.json_simple.*"%>
<%@page import="java.util.HashMap"%>

<%
    String uuid = request.getParameter("uuid");
    
    if(uuid == null) {
        response.sendRedirect("home.jsp#noUUID");
        return;
    }
    
    DAOFactory daoFactory = null;
    ServletContext context = request.getServletContext();

    if (context.getAttribute("daoFactory") == null) {
        System.err.println("*** Creating New DAOFactory Instance ...");
        daoFactory = new DAOFactory();
        context.setAttribute("daoFactory", daoFactory);
    }
    else {
        daoFactory = (DAOFactory) context.getAttribute("daoFactory");
    }
    
    HashMap<String, Object> map = new HashMap();
    map.put("uuid", uuid);
    
    JsonObject player = (JsonObject) daoFactory.getPlayer().find(map).get("player");
    
    if (player == null) {
        response.sendRedirect("home.jsp#unknownPlayer");
        return;
    }
%>

<!DOCTYPE html>
<html>
    <script>const UUID = "<%=uuid%>"</script>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/3.1.1/chart.min.js"></script>
    <script src="scripts/home.js"></script>
    <body>
        <canvas id="myChart" style="width:100%;max-width:800px"></canvas>
        <select id="gamemodes" name="gamemodes">
            <%= daoFactory.getStats().listGamemodes(uuid) %>
            <option value="volvo">Volvo</option>
            <option value="saab">Saab</option>
            <option value="fiat">Fiat</option>
            <option value="audi">Audi</option>
        </select>
    </body>
</html>
