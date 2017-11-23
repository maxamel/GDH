package main.java.gdh;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

/**
 * 
 * 
 * Configuration is the object which holds all the configurable parameters of a
 * GDHVertex. Implements the Builder pattern.
 * 
 * Diffie-Hellman requires a safe prime and a cyclic group generator, both
 * publicly known to everyone.
 * 
 * The Configuration object has these two numbers built-in, taken from
 * https://tools.ietf.org/html/rfc5114.
 * 
 * Note these numbers must fulfill several rules in order to be safe. If you are
 * not using the default numbers
 * 
 * take extreme care when choosing new one's, as your key exchange might be
 * vulnerable to all kinds of attacks.
 *
 * @author Max Amelchenko
 */
public class Configuration {
    private String IP = "localhost";
    private String port = "1080";
    private int retries = 5;
    private int exchangeTimeout = 60000;
    private String prime = "AD107E1E9123A9D0D660FAA79559C51FA20D64E5683B9FD1B54B1597B61D0A75E6FA141DF95A56DBAF9A3C"
            + "407BA1DF15EB3D688A309C180E1DE6B85A1274A0A66D3F8152AD6AC2129037C9EDEFDA4DF8D91E8FEF55B7"
            + "394B7AD5B7D0B6C12207C9F98D11ED34DBF6C6BA0B2C8BBC27BE6A00E0A0B9C49708B3BF8A317091883681"
            + "286130BC8985DB1602E714415D9330278273C7DE31EFDC7310F7121FD5A07415987D9ADC0A486DCDF93ACC"
            + "44328387315D75E198C641A480CD86A1B9E587E8BE60E69CC928B2B9C52172E413042E9B23F10B0E16E797"
            + "63C9B53DCF4BA80A29E3FB73C16B8E75B97EF363E2FFA31F71CF9DE5384E71B81C0AC4DFFE0C10E64F";
    private String generator = "AC4032EF4F2D9AE39DF30B5C8FFDAC506CDEBE7B89998CAF74866A08CFE4FFE3A6824A4E10B9A6F0DD"
            + "921F01A70C4AFAAB739D7700C29F52C57DB17C620A8652BE5E9001A8D66AD7C17669101999024AF4D02727"
            + "5AC1348BB8A762D0521BC98AE247150422EA1ED409939D54DA7460CDB5F6C6B250717CBEF180EB34118E98"
            + "D119529A45D6F834566E3025E316A330EFBB77A86F0C1AB15B051AE3D428C8F8ACB70A8137150B8EEB10E1"
            + "83EDD19963DDD9E263E4770589EF6AA21E7F5F2FF381B539CCE3409D13CD566AFBB48D6C019181E1BCFE94"
            + "B30269EDFE72FE9B6AA4BD7B5A0F1C71CFFF4C19C418E1F6EC017981BC087F2A7065B384B890D3191F2BFA";

    private final Logger log4jLogger;

    public Configuration() {
        SecureRandom random = new SecureRandom();
        this.log4jLogger = Logger.getLogger("Logger"+ random.nextInt());
        
        //this.log4jLogger.removeAllAppenders();
        ConsoleAppender appender = new ConsoleAppender();
        appender.setWriter(new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8)));
        appender.setLayout(new PatternLayout(PatternLayout.DEFAULT_CONVERSION_PATTERN));
        this.log4jLogger.addAppender(appender);
        
        this.log4jLogger.setLevel(Level.OFF);
    }

    /**
     * 
     * @param IP
     *            the ip the GDHVertex will be listening on
     * @return the updated configuration
     */
    public Configuration setIP(String IP) {
        assert (IP.matches("(([0-1]?[0-9]{1,2}\\.)|" + "(2[0-4][0-9]\\.)|" + "(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|"
                + "(2[0-4][0-9])|(25[0-5]))") || IP.contains("localhost"));
        this.IP = IP;
        return this;
    }

    /**
     * 
     * @param port
     *            the port the GDHVertex will be listening on
     * @return the updated configuration
     */
    public Configuration setPort(String port) {
        assert port.matches("[1-9]\\d*");
        this.port = port;
        return this;
    }

    /**
     * 
     * @param retries
     *            the number of times a GDHVertex will attempt to send a message
     *            if delivery fails. In case the number of retries is reached
     *            without an ack received, the key exchange is aborted.
     * @return the updated configuration
     */
    public Configuration setRetries(int retries) {
        this.retries = retries;
        return this;
    }

    public Configuration setExchangeTimeout(int exchangeTimeout) {
        this.exchangeTimeout = exchangeTimeout;
        return this;
    }

    public Configuration setGenerator(String generator) {
        assert generator.matches("[0-9A-F]*");
        this.generator = generator;
        return this;
    }

    public Configuration setPrime(String prime) {
        assert prime.matches("[0-9A-F]*");
        this.prime = prime;
        return this;
    }

    /**
     * 
     * @param level
     *            the log level to set. Default value is OFF.
     * @return the updated configuration
     */
    public Configuration setLogLevel(Level level) {
        this.log4jLogger.setLevel(level);
        return this;
    }

    public String getIP() {
        return IP;
    }

    public String getPort() {
        return port;
    }

    public int getRetries() {
        return retries;
    }

    public Node getNode() {
        return new Node(IP, port);
    }

    public String getPrime() {
        return prime;
    }

    public String getGenerator() {
        return generator;
    }

    public Logger getLogger() {
        return log4jLogger;
    }

    public int getExchangeTimeout() {
        return exchangeTimeout;
    }

    /**
     * Setting the appender of the logger. Only one appender at a time is
     * allowed.
     * 
     * @param app
     *            the Appender to be set
     *            
     * @return the updated configuration
     */
    public Configuration setAppender(Appender app) {
        log4jLogger.removeAllAppenders();
        log4jLogger.addAppender(app);
        return this;
    }

    /**
     * 
     * @param path
     *            the path to the configuration file
     * @return the Configuration read from the file
     */
    public static Configuration readConfigFile(String path) {
        Configuration conf = new Configuration();
        Vertx vertx = Vertx.vertx();
        vertx.fileSystem().readFile(path, res -> {
            Buffer buf = res.result();
            JsonObject json = new JsonObject(buf);
            String IP = json.getString(Constants.IP);
            String port = json.getString(Constants.PORT);
            String generator = json.getString(Constants.GENERATOR);
            String prime = json.getString(Constants.PRIME);
            int retries = json.getInteger(Constants.RETRIES);
            conf.setIP(IP).setPort(port).setGenerator(generator).setPrime(prime).setRetries(retries);
        });

        return conf;
    }

    @Override
    public int hashCode() {
        final int primal = 31;
        int result = 1;
        result = primal * result + ((IP == null) ? 0 : IP.hashCode());
        result = primal * result + ((generator == null) ? 0 : generator.hashCode());
        result = primal * result + ((port == null) ? 0 : port.hashCode());
        result = primal * result + ((this.prime == null) ? 0 : this.prime.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Configuration other = (Configuration) obj;
        if (IP == null) {
            if (other.IP != null)
                return false;
        } else if (!IP.equals(other.IP))
            return false;
        if (generator == null) {
            if (other.generator != null)
                return false;
        } else if (!generator.equals(other.generator))
            return false;
        if (port == null) {
            if (other.port != null)
                return false;
        } else if (!port.equals(other.port))
            return false;
        if (prime == null) {
            if (other.prime != null)
                return false;
        } else if (!prime.equals(other.prime))
            return false;
        return true;
    }
}
