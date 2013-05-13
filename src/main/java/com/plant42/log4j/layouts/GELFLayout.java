package com.plant42.log4j.layouts;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @autor Johannes Graf - graf@synyx.de
 */
public class GELFLayout extends Layout {

    private static final String VERSION = "1.0";
    private static final Integer LEVEL = 1;
    private static final String FACILITY = "GELF";
    private static final Integer MAX_SHORT_MESSAGE_LENGTH = 250;

    @Override
    public String format(LoggingEvent event) {

        JSONObject gelfMessage = new JSONObject();

        try {

            // set all mandatory fields
            gelfMessage.put("version", VERSION);



            gelfMessage.put("host", getHostname());
            gelfMessage.put("short_message", getShortMessage(event));
            gelfMessage.put("timestamp", event.getTimeStamp());
            gelfMessage.put("facility", FACILITY);

            // set all optional fields
            gelfMessage.put("full_message", event.getRenderedMessage());
            gelfMessage.put("level", event.getLevel().getSyslogEquivalent());
            gelfMessage.put("line", event.getLocationInformation().getLineNumber());
            gelfMessage.put("file", event.getLocationInformation().getFileName());

            // custom fields
            gelfMessage.put("_class", event.getLocationInformation().getClassName());
            gelfMessage.put("_thread", event.getThreadName());





            ThrowableInformation ti = event.getThrowableInformation();
            if (ti != null) {
                Throwable t = ti.getThrowable();
                JSONObject throwable = new JSONObject();

                throwable.put("message", t.getMessage());
                throwable.put("className", t.getClass().getCanonicalName());
                List<JSONObject> traceObjects = new ArrayList<JSONObject>();
                for(StackTraceElement ste : t.getStackTrace()) {
                    JSONObject element = new JSONObject();
                    element.put("class", ste.getClassName());
                    element.put("method", ste.getMethodName());
                    element.put("line", ste.getLineNumber());
                    element.put("file", ste.getFileName());
                    traceObjects.add(element);
                }

                gelfMessage.put("_stackTrace", traceObjects);
                gelfMessage.put("_throwable", throwable);
            }

            gelfMessage.put("_eventMessage", event.getMessage());

        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return gelfMessage.toString();
    }

    private String getShortMessage(LoggingEvent event) {
        String message = event.getRenderedMessage();
        if(message.length() > MAX_SHORT_MESSAGE_LENGTH) {
            return message.substring(0, MAX_SHORT_MESSAGE_LENGTH - 1);
        } else {
            return message;
        }
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
}
