package io.ilot.plol.model;

public enum MessageStatusLiveOdds
{
    ALIVE, LOGINOK, LOGINFAILED, REPLYSTART, REPLYEND, CHANGE, BETSTOP, BETSTART,
    CLEARBET, META, SCORE, CARDS, TRANSLATION, ROLLBACK, UNDOCANCELBET, CANCELBET,
    IRRELEVANTCHANGE;

    public static MessageStatusLiveOdds getMessageStatus(String statusStr) throws Exception {
        if (statusStr.equals("alive"))
            return ALIVE;
        else if (statusStr.equals("loginok"))
            return LOGINOK;
        else if (statusStr.equals("loginfailed"))
            return LOGINFAILED;
        else if (statusStr.equals("replystart"))
            return REPLYSTART;
        else if (statusStr.equals("replyend"))
            return REPLYEND;
        else if (statusStr.equals("change"))
            return CHANGE;
        else if (statusStr.equals("betstop"))
            return BETSTOP;
        else if (statusStr.equals("betstart"))
            return BETSTART;
        else if (statusStr.equals("clearbet"))
            return CLEARBET;
        else if (statusStr.equals("meta"))
            return META;
        else if (statusStr.equals("score"))
            return SCORE;
        else if (statusStr.equals("cards"))
            return CARDS;
        else if (statusStr.equals("translation"))
            return TRANSLATION;
        else if (statusStr.equals("rollback"))
            return ROLLBACK;
        else if (statusStr.equals("undocancelbet"))
            return UNDOCANCELBET;
        else if (statusStr.equals("cancelbet"))
            return CANCELBET;
        else if (statusStr.equals("irrelevantchange"))
            return IRRELEVANTCHANGE;
        else
            throw new Exception("Unknown Status");
    }
}
