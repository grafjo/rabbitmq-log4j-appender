package com.plant42.log4j.layouts;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @autor Johannes Graf - graf@synyx.de
 */
public class GELFLayout extends Layout {

    public static final String FACILITY = "GELF";
    public static final Integer MAX_SHORT_MESSAGE_LENGTH = 250;
    public static final String VERSION = "1.0";

    @Override
    public String format(LoggingEvent event) {

        JSONObject gelfMessage = new JSONObject();

        try {

            gelfMessage.put("version", VERSION);
            gelfMessage.put("host", getHostname());
            gelfMessage.put("timestamp", event.getTimeStamp());
            gelfMessage.put("facility", FACILITY);
            gelfMessage.put("level", event.getLevel().getSyslogEquivalent());
            gelfMessage.put("_thread", event.getThreadName());


            ThrowableInformation throwableInformation = event.getThrowableInformation();
            if(throwableInformation != null) {

                StackTraceElement stackTraceElement = throwableInformation.getThrowable().getStackTrace()[0];
                gelfMessage.put("file", stackTraceElement.getFileName());
                gelfMessage.put("line", stackTraceElement.getLineNumber());
                gelfMessage.put("_class", stackTraceElement.getClassName());
                gelfMessage.put("_method", stackTraceElement.getMethodName());

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                throwableInformation.getThrowable().printStackTrace(pw);

                String fullMessage = event.getRenderedMessage() + "\n" +  sw.toString();
                gelfMessage.put("full_message", fullMessage);
                gelfMessage.put("short_message", getShortMessage(fullMessage));

            } else {
                String fullMessage = event.getRenderedMessage();
                gelfMessage.put("full_message", fullMessage);
                gelfMessage.put("short_message", getShortMessage(fullMessage));
            }

        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return gelfMessage.toString();
    }

    @Override
    public boolean ignoresThrowable() {
        return false;
    }

    @Override
    public void activateOptions() {
        // not used!
    }

    private String getHostname() {
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {}

        return (hostname == null) ? "Unknown host" : hostname;
    }

    private String getShortMessage(String fullMessage) {
        if(fullMessage.length() > MAX_SHORT_MESSAGE_LENGTH) {
            return fullMessage.substring(0, MAX_SHORT_MESSAGE_LENGTH - 1);
        } else {
            return fullMessage;
        }
    }
}
