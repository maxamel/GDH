package main.java.parser;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import main.java.gdh.Constants;
import main.java.gdh.ExchangeState;
import main.java.gdh.Group;
import main.java.gdh.Node;

/**
 * 
 * @author Max Amelchenko
 * 
 *         MessageConstructor is a class for constructing json messages
 *
 */
public final class MessageConstructor {
    private MessageConstructor() {

    }

    /**
     * Construct a json object from the group parameter
     * 
     * @param g
     *            the group which needs to be jsonized
     * @return a json object representing the group
     */
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

    /**
     * Construct a json object from the ExchangeState parameter
     * 
     * @param state
     *            the ExchangeState which needs to be jsonized
     * @return a json object representing ExchangeState
     */
    public static JsonObject roundInfo(ExchangeState state) {
        JsonObject msg = new JsonObject();
        msg.put(Constants.GROUPID, String.valueOf(state.getGroupId()));
        msg.put(Constants.ROUND, String.valueOf(state.getRound()));
        msg.put(Constants.PARTIAL_KEY, state.getPartial_key().toString());
        return msg;
    }
}
