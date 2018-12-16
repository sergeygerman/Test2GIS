package german.test2gis.model;

/**
 * Created by s_german on 15.12.2018.
 */
public class Seat {
    private Integer rowNum;
    private Integer placeInRow;

    public Integer getRowNum() {
        return rowNum;
    }

    public Integer getPlaceInRow() {
        return placeInRow;
    }

    public Seat(Integer rowNum, Integer placeInRow){
        this.rowNum = rowNum;
        this.placeInRow = placeInRow;
    }
}
