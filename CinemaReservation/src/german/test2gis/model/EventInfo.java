package german.test2gis.model;

import german.test2gis.utilz.XmlConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by s_german on 15.12.2018.
 * Info about the requested event: name of cinema, hall number, event (film) name, date of begin and date of end for requested event, seats list (with reservation state)
 */
public class EventInfo {
    private String cinemaName;
    private Integer hallNumber;
    private String eventName;
    private Date eventBeginDate;
    private Date eventEndDate;
    private List<SeatState> seatsStates;

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public Integer getHallNumber() {
        return hallNumber;
    }

    public void setHallNumber(Integer hallNumber) {
        this.hallNumber = hallNumber;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Date getEventBeginDate() {
        return eventBeginDate;
    }

    public void setEventBeginDate(Date eventBeginDate) {
        this.eventBeginDate = eventBeginDate;
    }

    public Date getEventEndDate() {
        return eventEndDate;
    }

    public void setEventEndDate(Date eventEndDate) {
        this.eventEndDate = eventEndDate;
    }

    public List<SeatState> getSeatsStates() {
        if(seatsStates == null)
            seatsStates = new ArrayList<>();
        return seatsStates;
    }

    public SeatState getSeatsState(Integer row, Integer placeInRow) {
        for(SeatState s : getSeatsStates()){
            if(s.getRowNum().equals(row) && s.getPlaceInRow().equals(placeInRow))
                return s;
        }
        return null;
    }

    @Override
    public String toString(){//TODO сделать маршаллизацию
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            Element rootElement = doc.createElement("EventInfo");
            rootElement.setAttribute("xmlns","http://www.german.test/test2gis/v1");
            doc.appendChild(rootElement);

            Element cn = doc.createElement("CinemaName");
            cn.appendChild(doc.createTextNode(cinemaName));
            rootElement.appendChild(cn);

            Element hn = doc.createElement("HallNumber");
            hn.appendChild(doc.createTextNode(hallNumber.toString()));
            rootElement.appendChild(hn);

            Element en = doc.createElement("EventName");
            en.appendChild(doc.createTextNode(eventName));
            rootElement.appendChild(en);

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Element ebd = doc.createElement("EventBeginDate");
            ebd.appendChild(doc.createTextNode(df.format(eventBeginDate)));
            rootElement.appendChild(ebd);

            Element eed = doc.createElement("EventEndDate");
            eed.appendChild(doc.createTextNode(df.format(eventEndDate)));
            rootElement.appendChild(eed);

            if(getSeatsStates().size() > 0){
                Element seats = doc.createElement("Seats");
                for(SeatState seat : getSeatsStates()){
                    Element s = doc.createElement("Seat");

                    Element row = doc.createElement("Row");
                    row.appendChild(doc.createTextNode(seat.getRowNum().toString()));
                    s.appendChild(row);

                    Element pir = doc.createElement("PlaceInRow");
                    pir.appendChild(doc.createTextNode(seat.getPlaceInRow().toString()));
                    s.appendChild(pir);

                    if(seat.getDateOfReservation() != null){
                        Element ir = doc.createElement("IsReserved");
                        ir.appendChild(doc.createTextNode("Reserved on " + df.format(seat.getDateOfReservation())));
                        s.appendChild(ir);
                    }

                    seats.appendChild(s);
                }
                rootElement.appendChild(seats);
            }

            return XmlConverter.xmlToString(doc);
        } catch (Exception e){
            String staticResponse = "<result xmlns=\"http://www.german.test/test2gis/v1\">\n" +
                    "      <Error>" + e.getMessage() + "</Error>\n" +
                    "   </result>";
            return staticResponse;
        }
    }
}
