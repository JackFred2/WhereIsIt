package red.jackf.whereisit.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class WhereIsItGSON {
    static Gson get() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }
}
