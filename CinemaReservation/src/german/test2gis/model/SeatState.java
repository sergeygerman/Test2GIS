package german.test2gis.model;

import java.util.Date;

/**
 * Created by s_german on 15.12.2018.
 * Info about the one seat of hall
 */
public class SeatState {
    private Seat seat;
    private Date dateOfReservation;

    public SeatState(Integer rowNum, Integer placeInRow, Date dateOfReservation){
        this.seat = new Seat(rowNum, placeInRow);
        this.dateOfReservation = dateOfReservation;
    }

    public Integer getRowNum() {
        return seat.getRowNum();
    }

    public Integer getPlaceInRow() {
        return seat.getPlaceInRow();
    }

    public Date getDateOfReservation() {
        return dateOfReservation;
    }
}
