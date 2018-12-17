package german.test2gis.service;
import com.google.gson.Gson;
import german.test2gis.adapter.AdapterFactory;
import german.test2gis.adapter.IAdapter;
import german.test2gis.model.ReservationResult;
import german.test2gis.model.EventInfo;
import german.test2gis.model.Seat;
import german.test2gis.settings.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static spark.Spark.*;

/**
 * Created by s_german on 15.12.2018.
 * Service initialization
 */
public class StaticInit {
    static Logger logger = LogManager.getLogger();
    static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static IAdapter adapter = AdapterFactory.getAdapter(Settings.getInstance().getAdapterType());

    public static void main(String[] args) {
        try{
            port(Settings.getInstance().getHttpPort());

            initExceptionHandler((e) -> {
                System.out.println("Exception: " + e.getMessage());
                stop();
                System.exit(100);
            });

            int maxThreads = 8;
            int minThreads = 1;
            int timeOut = 30;
            threadPool(maxThreads, minThreads, timeOut*1000);

            if(Settings.getInstance().getJsonMode()){
                Gson gson = new Gson();
                get("/showEvents", (req, res) -> events(), gson::toJson);

                get("/showEventSeats", (req, res) -> eventCurrentState(req), gson::toJson);

                put("/reserve", (request, response) -> reservationOfSeats(request), gson::toJson);
            } else{
                get("/showEvents", (req, res) -> {
                    List<EventInfo> info = events();
                    if(info == null)
                        return "Unable to get events";

                    StringBuffer sBuffer = new StringBuffer("<Events>");
                    for(EventInfo eventInfo : info){
                        sBuffer.append("\n" + eventInfo.toString());
                    }
                    sBuffer.append("\n</Events>");
                    return sBuffer.toString();
                });

                get("/showEventSeats", (req, res) -> {
                    EventInfo info = eventCurrentState(req);
                    if(info != null)
                        return info.toString();
                    else
                        return "No events found by requested params. Check values of params";
                });

                put("/reserve", (request, response) -> reservationOfSeats(request));
            }

            get("/hello", (req, res) -> "This is a CinemaReservation app");

            notFound("<html><body><h1>Nothing to response: 404 - is serious</h1></body></html>");

        } catch (Exception e){
            logger.error("Fatal error: ", e);
        }
    }

    /*
    * get events with a begin date later than now
    */
    private static List<EventInfo> events(){
        try {
            List<EventInfo> result = adapter.getFutureEvents();
            return result;
        } catch (Exception e){
            logger.error("Unable to get events with a begin date later than now: " + e.getMessage());
            return null;
        }
    }

    /*
     * get info about selected event with a info of reservations of seats for this event
     */
    private static EventInfo eventCurrentState(Request req){
        String cinemaName = req.queryMap("cinemaName").value();
        String eventName = req.queryMap("eventName").value();
        String eventBeginDate = req.queryMap("eventBeginDate").value();
        try {
            Integer hallNumber = req.queryMap("hallNumber").integerValue();
            Date beginDate = null;
            try{
                beginDate = dateFormat.parse(eventBeginDate);
            }catch (Exception e){
                logger.error("Unable to parse a parameter 'eventBeginDate'");
            }
            if(cinemaName == null || cinemaName.isEmpty() || eventName == null || eventName.isEmpty() || hallNumber == null || beginDate == null){
                logger.error("Not all parameters are valid");
                return null;
            }

            EventInfo result = adapter.getSeats(cinemaName, hallNumber, eventName, beginDate);
            if(result == null){
                logger.error("Event is not found by requested params");
                return null;
            }

            return result;
        } catch (Exception e){
            logger.error("Unable to get seats of event [" + eventName + "]: " + e.getMessage());
            return null;
        }
    }

    /*
     * set reservation of selected seats for selected event
     */
    private static String reservationOfSeats(Request req){
        String cinemaName = req.queryMap("cinemaName").value();
        String eventName = req.queryMap("eventName").value();
        String eventBeginDate = req.queryMap("eventBeginDate").value();
        String fio = req.queryMap("fio").value();
        String phone = req.queryMap("phone").value();
        String seats = req.queryMap("seats").value();
        try {
            Integer hallNumber = req.queryMap("hallNumber").integerValue();
            if(hallNumber == null){
                String message = "Parameter hallNumber is missing.";
                logger.error(message);
                return message;
            }
            Date beginDate = null;
            try{
                beginDate = dateFormat.parse(eventBeginDate);
            }catch (Exception e){
                String message = "Unable to parse a parameter 'eventBeginDate': " + e.getMessage();
                logger.error("Unable to parse a parameter 'eventBeginDate': " + e.getMessage());
                return message;
            }

            if(cinemaName == null || cinemaName.isEmpty() || eventName == null || eventName.isEmpty() || fio == null || fio.isEmpty() || seats == null || seats.isEmpty()){
                String message = "One or more required parameters are missing or has invalid value";
                logger.error(message);
                return message;
            }

            List<Seat> seatsToReservation = new ArrayList<>();
            String[] seatsArray = seats.split(",");
            for(String s : seatsArray){
                Integer row = Integer.valueOf(s.split("-")[0]);
                Integer placeInRow = Integer.valueOf(s.split("-")[1]);
                seatsToReservation.add(new Seat(row, placeInRow));
            }
            ReservationResult result = adapter.reserve(cinemaName, hallNumber, eventName, beginDate, fio, phone, seatsToReservation);
            switch (result){
                case RESERVED_SUCCESSFULLY: return "All selected seats are reserved on event [" + eventName + "]";
                case WRONG_SEAT: return "One or all of selected seats is not exist in hall " + hallNumber + " of cinema [" + cinemaName + "]";
                case ALREADY_RESERVED_EARLIER: return "One or all of selected seats is already reserved";
                case ERROR: return "Unable to reserve selected seat(s)";
            }
            return result.toString();
        } catch (Exception e){
            String errorMessage = "Unable to reserve seats of event [" + eventName + "]: " + e.getMessage();
            logger.error(errorMessage);
            return errorMessage;
        }

    }
}
