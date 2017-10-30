package main.java.gdh;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class ExchangeState {
    private final int groupId;

    private BigInteger partial_key;

    private BigInteger secret;

    private int round = 0;

    private boolean isDone = false;

    private final CompletableFuture<BigInteger> key = new CompletableFuture<BigInteger>();

    private Handler<AsyncResult<BigInteger>> aHandler;

    public ExchangeState(int groupId, BigInteger gen) {
        this.groupId = groupId;
        this.partial_key = gen;
        initSecret();
    }

    public ExchangeState(int groupId, BigInteger partial_key, int round) {
        this.groupId = groupId;
        this.partial_key = partial_key;
        this.round = round;
        initSecret();
    }

    private void initSecret() {
        byte[] sec = new byte[32];
        Random random = new SecureRandom();
        random.nextBytes(sec);
        secret = (new BigInteger(sec)).abs();
    }

    public int getGroupId() {
        return groupId;
    }

    public BigInteger getPartial_key() {
        return partial_key;
    }

    public int getRound() {
        return round;
    }

    public BigInteger getSecret() {
        return secret;
    }

    public void incRound() {
        round++;
    }

    public void decRound() {
        round--;
    }

    public void setPartial_key(BigInteger partial_key) {
        this.partial_key = partial_key;
    }

    public void done() {
        isDone = true;
        key.complete(partial_key);
        if (aHandler != null)
            aHandler.handle(Future.succeededFuture(partial_key));
    }

    public boolean isDone() {
        return isDone;
    }

    public CompletableFuture<BigInteger> getKey() {
        return key;
    }

    public void registerHandler(Handler<AsyncResult<BigInteger>> aHandler) {
        this.aHandler = aHandler;
    }
}
