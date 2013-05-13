package com.plant42.log4j.layouts;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Johannes Graf - graf@synyx.de
 */
public class GelfLayoutTest {

    private ObjectMapper mapper;
    private GelfLayout gelfLayout;
    private Logger logger;
    private String hostname;

    @Before
    public void setup() throws UnknownHostException {
        mapper = new ObjectMapper();
        gelfLayout = new GelfLayout();
        logger = Logger.getLogger("GelfLayoutTest");
        hostname = InetAddress.getLocalHost().getHostName();
    }

    /**
     * This test covers logging a message and passing the throwable:
     * <code>
     * try{
     *   ...
     * }
     * catch(IOException exception) {
     *   LOG.error("I'm the message!", exception);
     * }
     * </code>
     *
     * @throws IOException
     */
    @Test
    public void formatNormalEvent() throws IOException {

        Logger logger = Logger.getLogger("GelfLayoutTest");
        Level level = Level.ERROR;
        String message = "I'm the message!";
        Throwable throwable = new IOException();
        LoggingEvent event = new LoggingEvent(Logger.class.getName(), logger, level, message, throwable);

        final JsonNode gelfMessage = mapper.readTree(gelfLayout.format(event));

        Assert.assertEquals("1.0", gelfMessage.get("version").asText());
        Assert.assertEquals(hostname, gelfMessage.get("host").asText());
        Assert.assertEquals("I'm the message!", gelfMessage.get("short_message").asText());
        Assert.assertEquals("I'm the message!", gelfMessage.get("full_message").asText());
        Assert.assertEquals(event.getTimeStamp(), gelfMessage.get("timestamp").longValue());
        Assert.assertEquals(level.getSyslogEquivalent(), gelfMessage.get("level").asInt());
        Assert.assertEquals("GELF", gelfMessage.get("facility").asText());
        Assert.assertEquals("?", gelfMessage.get("file").asText());
        Assert.assertEquals("?", gelfMessage.get("line").asText());

        Assert.assertTrue(gelfMessage.has("_stackTrace"));
        Assert.assertTrue(gelfMessage.has("_throwable"));
    }

    /**
     * This test covers logging a message without a throwable:
     * <code>
     * try{
     *   ...
     * }
     * catch(IOException exception) {
     *   LOG.error("I'm the message!");
     * }
     * </code>
     *
     * @throws IOException
     */
    @Test
    public void formatNormalEventWithoutThrowable() throws IOException {

        Level level = Level.ERROR;
        String message = "I'm the message!";
        LoggingEvent event = new LoggingEvent(Logger.class.getName(), logger, level, message, null);

        final JsonNode gelfMessage = mapper.readTree(gelfLayout.format(event));

        Assert.assertEquals("1.0", gelfMessage.get("version").asText());
        Assert.assertEquals(hostname, gelfMessage.get("host").asText());
        Assert.assertEquals("I'm the message!", gelfMessage.get("short_message").asText());
        Assert.assertEquals("I'm the message!", gelfMessage.get("full_message").asText());
        Assert.assertEquals(event.getTimeStamp(), gelfMessage.get("timestamp").longValue());
        Assert.assertEquals(level.getSyslogEquivalent(), gelfMessage.get("level").asInt());
        Assert.assertEquals("GELF", gelfMessage.get("facility").asText());
        Assert.assertEquals("?", gelfMessage.get("file").asText());
        Assert.assertEquals("?", gelfMessage.get("line").asText());

        Assert.assertFalse(gelfMessage.has("_stackTrace"));
        Assert.assertFalse(gelfMessage.has("_throwable"));
    }
}
