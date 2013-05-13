package com.plant42.log4j.layouts;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;

/**
 * @author Johannes Graf - graf@synyx.de
 */
public class GELFLayoutTest {

    private ObjectMapper mapper;
    private GELFLayout gelfLayout;
    private Logger logger;
    private String hostname;
    private StackTraceElement[] stackTraceElements;
    private String stackTraceMessage;
    private Throwable throwable;

    @Before
    public void setup() throws UnknownHostException {
        mapper = new ObjectMapper();
        gelfLayout = new GELFLayout();
        logger = Logger.getLogger("GELFLayoutTest");
        hostname = InetAddress.getLocalHost().getHostName();

        stackTraceElements = new StackTraceElement[]{
                new StackTraceElement("org.junit.runner.JUnitCore", "run", "JUnitCore.java", 160),
                new StackTraceElement("com.intellij.junit4.JUnit4IdeaTestRunner", "startRunnerWithArgs", "JUnit4IdeaTestRunner.java", 77),
                new StackTraceElement("com.intellij.rt.execution.junit.JUnitStarter", "prepareStreamsAndStart", "JUnitStarter.java", 195),
                new StackTraceElement("com.intellij.rt.execution.junit.JUnitStarter", "main", "JUnitStarter.java", 63)
        };

        throwable = new IOException();
        throwable.setStackTrace(stackTraceElements);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        stackTraceMessage = sw.toString();
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

        Level level = Level.ERROR;
        String message = "I'm the message!";
        LoggingEvent event = new LoggingEvent(Logger.class.getName(), logger, level, message, throwable);

        String expectedFullMessage = message + "\n" + stackTraceMessage;
        String expectedShortMessage = expectedFullMessage.substring(0, GELFLayout.MAX_SHORT_MESSAGE_LENGTH - 1);

        final JsonNode gelfMessage = mapper.readTree(gelfLayout.format(event));

        assertEquals(GELFLayout.VERSION, gelfMessage.get("version").asText());
        assertEquals(hostname, gelfMessage.get("host").asText());
        assertEquals(expectedShortMessage, gelfMessage.get("short_message").asText());
        assertEquals(expectedFullMessage, gelfMessage.get("full_message").asText());
        assertEquals(event.getTimeStamp(), gelfMessage.get("timestamp").longValue());
        assertEquals(level.getSyslogEquivalent(), gelfMessage.get("level").asInt());
        assertEquals(GELFLayout.FACILITY, gelfMessage.get("facility").asText());
        assertEquals("JUnitCore.java", gelfMessage.get("file").asText());
        assertEquals("160", gelfMessage.get("line").asText());
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

        assertEquals(GELFLayout.VERSION, gelfMessage.get("version").asText());
        assertEquals(hostname, gelfMessage.get("host").asText());
        assertEquals(message, gelfMessage.get("short_message").asText());
        assertEquals(message, gelfMessage.get("full_message").asText());
        assertEquals(event.getTimeStamp(), gelfMessage.get("timestamp").longValue());
        assertEquals(level.getSyslogEquivalent(), gelfMessage.get("level").asInt());
        assertEquals(GELFLayout.FACILITY, gelfMessage.get("facility").asText());
    }
}
