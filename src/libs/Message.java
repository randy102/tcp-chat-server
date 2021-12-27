package libs;

import org.json.JSONException;
import org.json.JSONObject;

public record Message(Command cmd, String data) {
    public Message(JSONObject data) throws JSONException {
        this(Command.valueOf(data.getString("cmd")), data.has("data") ? data.getString("data") : null);
    }

    public Message(Command cmd){
        this(cmd, null);
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("cmd", this.cmd.toString());
            jsonObject.put("data", this.data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
