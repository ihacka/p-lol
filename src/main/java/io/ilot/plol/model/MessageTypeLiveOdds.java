package io.ilot.plol.model;

public enum MessageTypeLiveOdds
{
    LIVEODDS, LIVEODDS45, SCOUTMATCH;

    public static MessageTypeLiveOdds getMessageType(String type) throws Exception
    {
        if (type.equals("liveodds"))
            return LIVEODDS;
        else if (type.equals("liveodds45"))
            return LIVEODDS45;
        else if (type.equals("scoutmatch"))
            return SCOUTMATCH;
        else
            throw new Exception("Unknown Message Type '"+type+"'");
    }

}