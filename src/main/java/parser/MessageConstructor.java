package main.java.parser;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import main.java.gdh.Constants;
import main.java.gdh.ExchangeState;
import main.java.gdh.Group;
import main.java.gdh.Node;

public final class MessageConstructor {
    private MessageConstructor() {

    }

    public static JsonObject groupInfo(Group g) {
        JsonObject msg = new JsonObject();
        msg.put(Constants.PRIME, g.getPrime().toString());
        msg.put(Constants.GENERATOR, g.getGenerator().toString());

        JsonArray members = new JsonArray();
        for (Node n : g.getTreeNodes()) {
            JsonObject obj = new JsonObject();
            obj.put(Constants.IP, n.getIP());
            obj.put(Constants.PORT, n.getPort());
            members.add(obj);
        }
        msg.put(Constants.MEMBERS, members);
        return msg;
    }

    public static JsonObject roundInfo(ExchangeState state) {
        JsonObject msg = new JsonObject();
        msg.put(Constants.GROUPID, String.valueOf(state.getGroupId()));
        msg.put(Constants.ROUND, String.valueOf(state.getRound()));
        msg.put(Constants.PARTIAL_KEY, state.getPartial_key().toString());
        return msg;
    }
}
