package io.ilot.plol.model;

public enum ReplyTypeLiveOdds
{
    ERROR, CURRENT, REGISTER, UNREGISTER, SCOREANDCARDSUMMARY, RISKADJUSTMENT_UPDATE;

    public static ReplyTypeLiveOdds getReplyType(String replyTypeStr) throws Exception
    {
        if (replyTypeStr.equals("error"))
            return ERROR;
        else if (replyTypeStr.equals("current"))
            return CURRENT;
        else if (replyTypeStr.equals("register"))
            return REGISTER;
        else if (replyTypeStr.equals("unregister"))
            return UNREGISTER;
        else if (replyTypeStr.equals("scoreandcardsummary"))
            return SCOREANDCARDSUMMARY;
        else if (replyTypeStr.equals("riskadjustment_update"))
            return RISKADJUSTMENT_UPDATE;
        else
            throw new Exception("Unknown reply type '"+replyTypeStr+"'");
    }
}