package german.test2gis.adapter;

import german.test2gis.model.ReservationResult;
import german.test2gis.model.EventInfo;
import german.test2gis.model.Seat;

import java.util.Date;
import java.util.List;

/**
 * Created by s_german on 15.12.2018.
 */
public interface IAdapter {
    List<EventInfo> getFutureEvents();
    EventInfo getSeats(String cinemaName, Integer hallNumber, String eventName, Date eventBeginDate);
    ReservationResult reserve(String cinemaName, Integer hallNumber, String eventName, Date eventBeginDate, String fio, String phone, List<Seat> seatsToReserve);
    void close();
}
