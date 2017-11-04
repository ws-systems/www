package systems.whitestar.API.Models;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;

/**
 * @author Tom Paulus
 * Created on 10/19/17.
 */
@SuppressWarnings("unused")
public @AllArgsConstructor class SimpleMessage {
    private String type;
    private String message;

    public String asJson() {
        return new Gson().toJson(this);
    }
}
