package io.github.dsheirer.module.decode.dmr.message.type;

import java.util.Map;
import java.util.TreeMap;

/**
 * Acknowledge message reasons
 *
 * Note: each entry is prefixed with MS or TS where:
 *  MS = Mobile Subscriber
 *  TS = Trunking System
 */
public enum Reason
{
    MS_SERVICE_NOT_SUPPORTED(0x00, "SERVICE_NOT SUPPORTED"),
    MS_LINE_NOT_SUPPORTED(0x11, "LINE NOT SUPPORTED"),
    MS_REFUSED_STACK_FULL(0x12, "MS REFUSED - STACK FULL"),
    MS_REFUSED_EQUIPMENT_BUSY(0x13, "MS REFUSED - EQUIPMENT BUSY"),
    MS_REFUSED_BY_RECIPIENT(0x14, "REFUSED BY RECIPIENT"),
    MS_REFUSED_CUSTOM(0x15, "REFUSED-CUSTOM"),
    MS_DUPLEX_NOT_SUPPORTED(0x16, "DUPLEX NOT SUPPORTED BY MS"),
    MS_REFUSED_REASON_UNKNOWN(0x1F, "REFUSED-REASON UNKNOWN"),

    TS_SERVICE_NOT_SUPPORTED(0x20, "SERVICE NOT SUPPORTED"),
    TS_REFUSED_NOT_PERMITTED(0x21, "REFUSED-NOT PERMISSION"),
    TS_REFUSED_SERVICE_TEMP_UNAVAILABLE(0x22, "REFUSED-SERVICE TEMPORARY UNAVAILABLE"),
    TS_REFUSED_SERVICE_UNAVAILABLE(0x23, "REFUSED-SERVICE UNAVAILABLE"),
    TS_REFUSED_CALLED_RADIO_NOT_REGISTERED(0x24, "REFUSED-CALLED RADIO NOT REGISTERED"),
    TS_REFUSED_CALLED_RADIO_OFFLINE(0x25, "REFUSED-CALLED RADIO OFFLINE"),
    TS_REFUSED_CALLED_RADIO_CALL_DIVERT(0x26, "REFUSED-CALLED RADIO HAS CALL DIVERSION"),
    TS_REFUSED_NETWORK_CONGESTION(0x27, "REFUSED-NETWORK CONGESTION"),
    TS_REFUSED_NETWORK_NOT_READY(0x28, "REFUSED-NETWORK NOT READY"),
    TS_REFUSED_CANNOT_CANCEL_CALL(0x29, "REFUSED-CANNOT CANCEL CALL"),
    TS_REGISTRATION_REFUSED(0x2A, "REGISTRATION REFUSED"),
    TS_REGISTRATION_DENIED(0x2B, "REGISTRATION DENIED"),
    TS_IP_CONNECTION_FAILED(0x2C, "IP CONNECTION FAILED"),
    TS_REFUSED_RADIO_NOT_REGISTERED(0x2D, "REFUSED-RADIO NOT REGISTERED"),
    TS_CALLED_PARTY_BUSY(0x2E, "CALLED PARTY BUSY"),
    TS_CALLED_TALKGROUP_NOT_ALLOWED(0x2F, "CALLED TALKGROUP NOT ALLOWED"),
    TS_CRC_ERROR_IN_UDT_UPLOAD(0x30, "CRC ERROR IN UDT UPLOAD"),
    TS_REFUSED_DUPLEX_CALL_NETWORK_CONGESTION(0x31, "REFUSED DUPLEX CALL-NETWORK CONGESTION"),
    TS_REFUSED_REASON_UNKNOWN(0x3F, "REFUSED-REASON UNKNONW"),


    MS_MESSAGE_ACCEPTED(0x44, "MESSAGE ACCEPTED"),
    MS_CALLBACK(0x45, "CALLBACK"),
    MS_ALERTING(0x46, "ALERTING BUT NOT READY"),
    MS_ACCEPTED_FOR_POLLING_STATUS_SERVICE(0x47, "ACCEPTED FOR POLLING STATUS SERVICE"),
    MS_AUTHENTICATION_RESPONSE(0x48, "AUTHENTICATION RESPONSE"),

    TS_MESSAGE_ACCEPTED(0x60, "MESSAGE ACCEPTED"),
    TS_STORE_AND_FORWARD(0x61, "STORE AND FORWARD"),
    TS_REGISTRATION_ACCEPTED(0x62, "REGISTRATION ACCEPTED"),
    TS_ACCEPTED_FOR_STATUS_POLLING_SERVICE(0x63, "ACCEPTED FOR STATUS POLLING SERVICE"),
    TS_AUTHENTICATION_RESPONSE(0x64, "AUTHENTICATION RESPONSE"),
    TS_SUBSCRIPTION_SERVICE_REGISTRATION_ACCEPTED(0x65, "SUBSCRIPTION SERVICE REGISTRATION ACCEPTED"),

    TS_QUEUED_FOR_RESOURCE(0xA0, "QUEUED FOR RESOURCE"),
    TS_QUEUED_FOR_BUSY_RADIO(0xAa, "QUEUED FOR BUSY RADIO"),

    TS_WAIT(0xE0, "WAIT"),

    UNKNOWN(-1, "UNKNOWN");

    private int mValue;
    private String mLabel;

    Reason(int value, String label)
    {
        mValue = value;
        mLabel = label;
    }

    public int getValue()
    {
        return mValue;
    }

    @Override
    public String toString()
    {
        return mLabel;
    }

    private static final Map<Integer,Reason> LOOKUP_MAP = new TreeMap<>();

    static
    {
        for(Reason reason: Reason.values())
        {
            LOOKUP_MAP.put(reason.getValue(), reason);
        }
    }

    /**
     * Utility method to lookup a Reason entry from a value.
     * @param value to lookup
     * @return enum entry or UNKNOWN
     */
    public static Reason fromValue(int value)
    {
        if(LOOKUP_MAP.containsKey(value))
        {
            return LOOKUP_MAP.get(value);
        }

        return UNKNOWN;
    }
}