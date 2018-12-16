package german.test2gis.utilz;

import spark.ResponseTransformer;
import com.google.gson.Gson;

/**
 * Created by s_german on 16.12.2018.
 */
public class JsonTransformer implements ResponseTransformer {
    private Gson gson = new Gson();

    @Override
    public String render(Object model) {
        return gson.toJson(model);
    }
}
