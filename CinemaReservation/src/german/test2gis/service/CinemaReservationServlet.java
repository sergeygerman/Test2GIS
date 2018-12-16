package german.test2gis.service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.IOException;

/**
 * Created by s_german on 15.12.2018.
 * Servlet for autolaunch after deploy on Tomcat
 */
//@WebServlet("/reservation")
public class CinemaReservationServlet extends HttpServlet {
    @Override
    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
    }

    @Override
    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
    }

    @Override
    public void init() throws ServletException {
        super.init();
        StaticInit.main(null);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
