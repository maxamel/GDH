package main.java.parser;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import main.java.gdh.Constants;
import main.java.gdh.ExchangeState;
import main.java.gdh.Group;
import main.java.gdh.Node;

/**
 * 
 * JsonMessageParser is a class for parsing json messages
 * 
 * Parses two types of messages:
 * 
 * Messages relaying the public parameters of the group, e.g. large prime (N) and cyclic generator (g)
 * 
 * and messages relaying the partial keys computed during the key exchange.
 *
 * @author Max Amelchenko
 */
public class JsonMessageParser implements MessageParser {

    private final Map<Integer, Group> groupMappings;
    private final Map<Integer, ExchangeState> stateMappings;

    public JsonMessageParser(Map<Integer, Group> groupMappings, Map<Integer, ExchangeState> stateMappings) {
        this.groupMappings = groupMappings;
        this.stateMappings = stateMappings;
    }

    /**
     * @param msg
     *            the information to be parsed
     * @return the group id of the group contained in the message or error code
     */
    @Override
    public int parse(String msg) {
        if (msg.contains(Constants.ROUND))
            return extractRoundInfo(msg);
        return extractGroupInfo(msg);
    }

    /**
     * 
     * @param msg
     *            the group details in json format to be parsed
     * @return the group id of the group contained in the message
     *         -2 if this message has already been received
     */
    private int extractGroupInfo(String msg) {
        TreeSet<Node> set = new TreeSet<>();
        Group group = null;
        JsonObject obj = new JsonObject(msg);
        String prime = obj.getString(Constants.PRIME);
        String generator = obj.getString(Constants.GENERATOR);
        JsonArray members = obj.getJsonArray(Constants.MEMBERS);
        Iterator<?> iter = members.iterator();
        while (iter.hasNext()) {
            JsonObject member = (JsonObject) iter.next();
            String ip = member.getString(Constants.IP);
            String port = member.getString(Constants.PORT);
            Node n = new Node(ip, port);
            set.add(n);
        }
        group = new Group(generator, prime, set);
        if (stateMappings.containsKey(group.getGroupId()) && !stateMappings.get(group.getGroupId()).isDone())
            return -2; // message received twice
        group.setGenerator(new BigInteger(generator));
        group.setPrime(new BigInteger(prime));
        groupMappings.put(group.getGroupId(), group);
        stateMappings.put(group.getGroupId(), new ExchangeState(group.getGroupId(), group.getGenerator()));
        return group.getGroupId();
    }

    /**
     * Get the info about the current round of the key exchange. These include
     * the id of the group exchanging keys and the partial key computed by this
     * Node's counterpart. This partial key will be used by this Node for
     * further computation.
     * 
     * @param msg
     *            the round details in json format to be parsed
     * @return the group id of the group contained in the message 
     *         -1 if the state does not exist yet, which means the group info was not received yet 
     *         -2 if this message has already been received
     */
    private int extractRoundInfo(String msg) {
        JsonObject obj = new JsonObject(msg);
        String groupId = obj.getString(Constants.GROUPID);
        String round = obj.getString(Constants.ROUND);
        int ret = Integer.parseInt(groupId);
        String partial_key = obj.getString(Constants.PARTIAL_KEY);
        ExchangeState state = stateMappings.get(ret);
        if (state == null) // State does not exist
            return -1;
        if (state.getRound() == Integer.parseInt(round) - 1) // message received twice
            return -2;
        state.setPartial_key(new BigInteger(partial_key));
        return ret;
    }
}
