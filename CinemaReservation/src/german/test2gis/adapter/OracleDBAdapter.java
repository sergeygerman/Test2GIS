package german.test2gis.adapter;

import german.test2gis.model.ReservationResult;
import german.test2gis.model.EventInfo;
import german.test2gis.model.Seat;
import german.test2gis.model.SeatState;
import german.test2gis.settings.Settings;
import oracle.jdbc.pool.OracleDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by s_german on 15.12.2018.
 * Connector to Oracle DB, Adapter and Handler
 */
public class OracleDBAdapter implements IAdapter{
    private String sName;
    private Logger logger;
    private Settings settings;

    private DataSource dataSource;
    private Connection mDatabaseConnection;
    private PreparedStatement mGetEventsStmnt;
    private PreparedStatement mGetEventByParamsStmnt;
    private PreparedStatement mGetSeatsStmnt;
    private PreparedStatement mReservationStmnt;

    private static final String GET_EVENTS =
                    "select c.name, chl.hall_number, cs.event_name, cs.date_begin, cs.date_end\n" +
                    "from cinema_halls chl\n" +
                    "join cinemas c on chl.id_cinemas = c.id\n" +
                    "join cinema_schedules cs on chl.id = cs.id_cinema_halls\n" +
                    "where cs.date_begin > sysdate";

    private static final String GET_EVENT_BY_PARAMS =
                            "select c.name, chl.hall_number, cs.event_name, cs.date_begin, cs.date_end,\n" +
                            "cs.id_cinema_halls, cs.id as ID_CINEMA_SCHEDULES\n" +
                            "from cinema_halls chl\n" +
                            "join cinemas c on chl.id_cinemas = c.id\n" +
                            "join cinema_schedules cs on chl.id = cs.id_cinema_halls\n" +
                            "where c.name=?\n" +
                            "and chl.hall_number = ?\n" +
                            "and cs.event_name=?\n" +
                            "and cs.date_begin = ?";

    private static final String GET_SEATS_BY_EVENT =
                    "select s.row_num, s.place_in_row, sr.date_reservation from seats s\n" +
                    "left outer join seats_reservation sr on s.id = sr.id_seats and sr.id_cinema_schedules = ?\n" +
                    "where s.id_cinema_halls = ?\n" +
                    "order by s.row_num, s.place_in_row asc";

    private static final String SET_RESERVATION =
                    "insert into CINEMA_RESERVATION.SEATS_RESERVATION (ID_CINEMA_SCHEDULES, ID_SEATS, DATE_RESERVATION, RESERVE_OWNER_NAME, RESERVE_OWNER_PHONE) values (\n" +
                    "(   select ID from cinema_schedules where id_cinema_halls=\n" +
                    "        (select id from cinema_halls where cinema_halls.id_cinemas=(select id from cinemas where name = ?) and hall_number = ?)\n" +
                    "    and event_name=? and date_begin = ?\n" +
                    "),\n" +
                    "(\n" +
                    "    select id from seats where id_cinema_halls=(select id from cinema_halls where cinema_halls.id_cinemas=(select id from cinemas where name = ?) and hall_number = ?)\n" +
                    "    and row_num=? and place_in_row=?\n" +
                    "), SYSDATE, ?, ?)";


    private static volatile OracleDBAdapter instance = null;
    public static OracleDBAdapter getInstance(){
        if (instance == null) {
            synchronized(OracleDBAdapter.class) {
                instance = new OracleDBAdapter(Settings.getInstance());
            }
        }
        return instance;
    }

    private OracleDBAdapter(Settings settings){
        this.logger = LogManager.getLogger(this.getClass());
        this.settings = settings;
        sName = "\t[DBAdapter] ";
        init();
    }

    private Boolean initCheck(){
        try {
            if(dataSource != null && mDatabaseConnection != null){
                try {
                    if(!mDatabaseConnection.isClosed())
                        return true;
                    else
                        return init();
                } catch (SQLException sqlex){
                    return init();
                }
            } else
                return init();
        } catch (Exception ex) {
            return false;
        }
    }

    private Boolean init(){
        close();

        if(settings == null)
            return false;

        logger.info(sName + "Connect to DB");

        try {
            OracleDataSource ds = new OracleDataSource();
            ds.setDriverType("thin");
            ds.setServerName(settings.getOracleDBSettings().getHost());
            ds.setDatabaseName(settings.getOracleDBSettings().getSid());
            ds.setPortNumber(settings.getOracleDBSettings().getPort());
            ds.setUser(settings.getOracleDBSettings().getUsername());
            ds.setPassword(settings.getOracleDBSettings().getPassword());

            dataSource = ds;

            if (dataSource == null) {
                logger.error(sName + "Error when getting connection dataSource.");
                return false;
            }
        } catch (Exception e) {
            this.logger.error(sName + "Error when getting connection dataSource: ", e);
            return false;
        }

        try {
            mDatabaseConnection = dataSource.getConnection();
            if (mDatabaseConnection == null || mDatabaseConnection.isClosed()) {
                logger.error(sName + "Connection to DB is closed.");
                return false;
            }
            mDatabaseConnection.setAutoCommit(false);
            logger.info(sName + "Connection to DB established.");
        } catch (Exception e) {
            logger.error(sName + "Connection to DB is in unusable state.");
            return false;
        }

        return true;
    }

    private Boolean commit() {
        try {
            mDatabaseConnection.commit();
            return true;
        } catch (SQLException e) {
            logger.error(sName + "Error when commit: ", e);
            return false;
        }
    }

    private Boolean rollback() {
        try {
            mDatabaseConnection.rollback();
            return true;
        } catch (SQLException e) {
            logger.error(sName + "Error when rollback: ", e);
            return false;
        }
    }

    public synchronized List<EventInfo> getFutureEvents(){
        List<EventInfo> result = new ArrayList<>();

        try {
            PreparedStatement eventStmnt = getGetEventsStatement();
            ResultSet rs = eventStmnt.executeQuery();
            while (rs.next()){
                EventInfo event = new EventInfo();
                event.setCinemaName(rs.getString(1));
                event.setHallNumber(rs.getInt(2));
                event.setEventName(rs.getString(3));
                event.setEventBeginDate(new Date(rs.getTimestamp(4).getTime()));
                event.setEventEndDate(new Date(rs.getTimestamp(5).getTime()));
                result.add(event);
            }
        } catch (Exception e){
            logger.error(sName + "Unable to get events with a begin date later than now", e);
            result = null;
        }

        return result;
    }

    @Override
    public synchronized EventInfo getSeats(String cinemaName, Integer hallNumber, String eventName, Date eventBeginDate){
        EventInfo result = new EventInfo();
        result.setCinemaName(cinemaName);
        result.setHallNumber(hallNumber);
        result.setEventName(eventName);
        result.setEventBeginDate(eventBeginDate);

        try {
            PreparedStatement eventStmnt = getGetEventByParamsStatement();
            eventStmnt.clearParameters();

            eventStmnt.setString(1, cinemaName);
            eventStmnt.setInt(2, hallNumber);
            eventStmnt.setString(3, eventName);
            eventStmnt.setTimestamp(4, new Timestamp(eventBeginDate.getTime()));

            Date dateEnd = null;
            Integer idCinemaHall = null;
            Integer idCinemaSchedules = null;
            ResultSet rs = eventStmnt.executeQuery();
            if (rs.next()) {
                dateEnd = new Date(rs.getTimestamp(5).getTime());
                idCinemaHall = rs.getInt(6);
                idCinemaSchedules = rs.getInt(7);

                result.setEventEndDate(dateEnd);

                PreparedStatement seatsStmnt = getGetSeatsStatement();
                seatsStmnt.clearParameters();

                seatsStmnt.setInt(1, idCinemaSchedules);
                seatsStmnt.setInt(2, idCinemaHall);

                ResultSet rseats = seatsStmnt.executeQuery();
                while (rseats.next()){
                    Integer idRow = null;
                    Integer idPlaceInRow = null;
                    Date dateReservation = null;
                    idRow = rseats.getInt(1);
                    idPlaceInRow = rseats.getInt(2);
                    if(rseats.getTimestamp(3) != null)
                        dateReservation = new Date(rseats.getTimestamp(3).getTime());
                    result.getSeatsStates().add(new SeatState(idRow, idPlaceInRow, dateReservation));
                }
            }
            else {
                logger.error(sName + "Found no events");
                return null;
            }
        } catch (SQLException se){
            logger.error(sName + "Unable to get reservations of Event [" + eventName + "]", se);
            result = null;
        }

        return result;
    }

    @Override
    public synchronized ReservationResult reserve(String cinemaName, Integer hallNumber, String eventName, Date eventBeginDate, String fio, String phone, List<Seat> seatsToReserve){
        EventInfo currenInfoOfEvent = getSeats(cinemaName, hallNumber, eventName, eventBeginDate);
        //check all requested seats on existing and reservation
        for (Seat seatToReserve : seatsToReserve){
            SeatState s = currenInfoOfEvent.getSeatsState(seatToReserve.getRowNum(), seatToReserve.getPlaceInRow());
            if(s == null)
                return ReservationResult.WRONG_SEAT;
            if(s.getDateOfReservation() != null)
                return ReservationResult.ALREADY_RESERVED_EARLIER;
        }
        //if all requested seats are exists and not in reservation
        for (Seat seatToReserve : seatsToReserve){
            try {
                PreparedStatement reserveStmnt = getReservationStatement();
                reserveStmnt.clearParameters();

                reserveStmnt.setString(1, cinemaName);
                reserveStmnt.setInt(2, hallNumber);
                reserveStmnt.setString(3, eventName);
                reserveStmnt.setTimestamp(4, new Timestamp(eventBeginDate.getTime()));
                reserveStmnt.setString(5, cinemaName);
                reserveStmnt.setInt(6, hallNumber);
                reserveStmnt.setInt(7, seatToReserve.getRowNum());
                reserveStmnt.setInt(8, seatToReserve.getPlaceInRow());
                reserveStmnt.setString(9, fio);
                if(phone != null && !phone.isEmpty())
                    reserveStmnt.setString(10, phone);
                else
                    reserveStmnt.setNull(10, java.sql.Types.VARCHAR);

                Integer countOfReservedSeats = reserveStmnt.executeUpdate();
                if(countOfReservedSeats == 0)
                    throw new Exception("Cannot insert info to table");
            } catch (Exception e){
                logger.error(sName + "Unable to reserve seat " + seatToReserve.getRowNum() + "x" + seatToReserve.getPlaceInRow() + " on event [" + eventName + "]");
                rollback();
                return ReservationResult.ERROR;
            }
        }
        commit();
        return ReservationResult.RESERVED_SUCCESSFULLY;
    }

    private PreparedStatement getGetEventsStatement(){
        if(initCheck()){
            try{
                Boolean _isClosed = false;
                try {
                    _isClosed = mGetEventsStmnt == null ? true : mGetEventsStmnt.isClosed();
                } catch (Exception ex){
                    logger.warn(sName + "GetEventsStatement is broken: " + ex.getMessage());
                    _isClosed = true;
                }
                if(mGetEventsStmnt != null && !_isClosed)
                    return mGetEventsStmnt;
                else{
                    mGetEventsStmnt = mDatabaseConnection.prepareStatement(GET_EVENTS);
                    return mGetEventsStmnt;
                }
            } catch (Exception ex){
                logger.error(sName + "init GetEventsStatement failed: " + ex.getMessage(), ex);
                mGetEventsStmnt = null;
                return null;
            }
        } else{
            logger.error(sName + "Unable to connect to DB");
            return null;
        }
    }

    private PreparedStatement getGetEventByParamsStatement(){
        if(initCheck()){
            try{
                Boolean _isClosed = false;
                try {
                    _isClosed = mGetEventByParamsStmnt == null ? true : mGetEventByParamsStmnt.isClosed();
                } catch (Exception ex){
                    logger.warn(sName + "GetEventByParamsStatement is broken: " + ex.getMessage());
                    _isClosed = true;
                }
                if(mGetEventByParamsStmnt != null && !_isClosed)
                    return mGetEventByParamsStmnt;
                else{
                    mGetEventByParamsStmnt = mDatabaseConnection.prepareStatement(GET_EVENT_BY_PARAMS);
                    return mGetEventByParamsStmnt;
                }
            } catch (Exception ex){
                logger.error(sName + "init GetEventByParamsStatement failed: " + ex.getMessage(), ex);
                mGetEventByParamsStmnt = null;
                return null;
            }
        } else{
            logger.error(sName + "Unable to connect to DB");
            return null;
        }
    }

    private PreparedStatement getGetSeatsStatement(){
        if(initCheck()){
            try{
                Boolean _isClosed = false;
                try {
                    _isClosed = mGetSeatsStmnt == null ? true : mGetSeatsStmnt.isClosed();
                } catch (Exception ex){
                    logger.warn(sName + "GetSeatsStatement is broken: " + ex.getMessage());
                    _isClosed = true;
                }
                if(mGetSeatsStmnt != null && !_isClosed)
                    return mGetSeatsStmnt;
                else{
                    mGetSeatsStmnt = mDatabaseConnection.prepareStatement(GET_SEATS_BY_EVENT);
                    return mGetSeatsStmnt;
                }
            } catch (Exception ex){
                logger.error(sName + "init GetSeatsStatement failed: " + ex.getMessage(), ex);
                mGetSeatsStmnt = null;
                return null;
            }
        } else{
            logger.error(sName + "Unable to connect to DB");
            return null;
        }
    }

    private PreparedStatement getReservationStatement(){
        if(initCheck()){
            try{
                Boolean _isClosed = false;
                try {
                    _isClosed = mReservationStmnt == null ? true : mReservationStmnt.isClosed();
                } catch (Exception ex){
                    logger.warn(sName + "ReservationStatement is broken: " + ex.getMessage());
                    _isClosed = true;
                }
                if(mReservationStmnt != null && !_isClosed)
                    return mReservationStmnt;
                else{
                    mReservationStmnt = mDatabaseConnection.prepareStatement(SET_RESERVATION);
                    return mReservationStmnt;
                }
            } catch (Exception ex){
                logger.error(sName + "init ReservationStatement failed: " + ex.getMessage(), ex);
                mReservationStmnt = null;
                return null;
            }
        } else{
            logger.error(sName + "Unable to connect to DB");
            return null;
        }
    }

    @Override
    public void close(){
        try {
            if (mGetEventsStmnt != null) {
                mGetEventsStmnt.close();
            }
        } catch (SQLException e) {
            logger.error(e);
        }

        try {
            if (mGetEventByParamsStmnt != null) {
                mGetEventByParamsStmnt.close();
            }
        } catch (SQLException e) {
            logger.error(e);
        }

        try {
            if (mGetSeatsStmnt != null) {
                mGetSeatsStmnt.close();
            }
        } catch (SQLException e) {
            logger.error(e);
        }

        try {
            if (mReservationStmnt != null) {
                mReservationStmnt.close();
            }
        } catch (SQLException e) {
            logger.error(e);
        }

        try {
            if (mDatabaseConnection != null) {
                mDatabaseConnection.close();
            }
        } catch (SQLException e) {
            logger.error(e);
        }

        dataSource = null;
    }
}
