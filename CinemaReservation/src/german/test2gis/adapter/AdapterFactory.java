package german.test2gis.adapter;

/**
 * Created by s_german on 17.12.2018.
 */
public class AdapterFactory {
    public static IAdapter getAdapter (String adapterType){
        switch (adapterType){
            case "Oracle": {return OracleDBAdapter.getInstance();}
            default: {return null;}
        }
    }
}
