package io.ilot.plol.model;

public class ConstantsLiveScout
{
    // E_ for element, A_ for attribute
    public static String
            E_MATCH = "match" ,
            A_MATCH_BETSTATUS = "betstatus",
            A_MATCH_DISTANCE  = "distance",
            A_MATCH_EXTRAINFO =	"extrainfo",
            A_MATCH_FEEDTYPE = "feedtype",
            A_MATCH_MATCHID = "matchid",
            A_MATCH_START = "start",
            A_MATCH_T1ID = "t1id",
            A_MATCH_T1UID = "st1id",
            A_MATCH_T1NAME = "t1name",
            A_MATCH_T2ID = "t2id",
            A_MATCH_T2UID = "st2id",
            A_MATCH_T2NAME = "t2name",
            A_MATCH_DC = "dc",
            A_MATCH_FIRSTSERVE = "firstserve",
            A_MATCH_TIEBREAKLASTSET = "tiebreaklastset",
            A_MATCH_NUMBEROFSETS = "numberofsets",
            A_MATCH_TIMERUNNING = "timerunning",
            A_MATCH_WONJUMPBALL = "wonjumpball",
            A_MATCH_CONNECTIONSTATUS = "connectionstatus",
            A_MATCH_TIME = "matchtime",

    E_INFOS = "infos",
            E_INFO = "info",
            A_INFO_HEADER = "header",
            A_INFO_LINK = "link",
            A_INFO_ORDER = "order",
            A_INFO_VALUE = "value",

    E_MATCHSTOP = "matchstop",
            A_MATCHSTOP_MATCHID = "matchid",
            A_MATCHSTOP_REASON = "reason",

    E_SERVERTIME = "servertime",
            A_SERVERTIME_VALUE = "value",

    E_MATCHLIST = "matchlist",
            E_MATCHLIST_UPDATE = "matchlistupdate",
            E_LOGOUT = "logout",

    E_STATUS = "status",
            A_STATUS_ID = "id",
            A_STATUS_NAME = "name",
            A_STATUS_START = "start",

    E_SCORE = "score",
            A_SCORE_T1 = "t1",
            A_SCORE_T2 = "t2",
            A_SCORE_TYPE = "type",

    E_EVENT = "event",
            A_EVENT_ID = "id",
            A_EVENT_INFO = "info",
            A_EVENT_MTIME = "mtime",
            A_EVENT_SIDE = "side",
            A_EVENT_STIME = "stime",
            A_EVENT_TYPE = "type",
            A_EVENT_POSX = "posx",
            A_EVENT_POSY = "posy",
            A_EVENT_PLAYER1 = "player1",
            A_EVENT_PLAYER2 = "player2",
            A_EVENT_GAMENUMBER = "gamenumber",
            A_EVENT_SETNUMBER = "setnumber",
            A_EVENT_GAMESCORE = "gamescore",
            A_EVENT_SETSCORE = "setscore",
            A_EVENT_MATCHSCORE = "matchscore",
            A_EVENT_PERIODNUMBER = "periodnumber",
            A_EVENT_REMAININGTIMEPERIOD = " remainingtimeperiod",
            A_EVENT_EXTRAINFO =	"extrainfo",
            A_EVENT_CORRECTEDFROM =	"correctedfrom",
            A_EVENT_CORRECTEDTO=	"correctedto",

    E_TIEBREAK = "tiebreak",
            A_TIEBREAK_VALUE = "value",

    E_SERVE = "serve",
            A_SERVE_TEAM = "team",

    E_RED = "red",
            A_RED_T1 = "t1",
            A_RED_T2 = "t2",

    E_YELLOW = "yellow",
            A_YELLOW_T1 = "t1",
            A_YELLOW_T2 = "t2",

    E_BLACK = "black",
            A_BLACK_T1 = "t1",
            A_BLACK_T2 = "t2",

    E_CORNERS = "corners",
            A_CORNERS_T1 = "t1",
            A_CORNERS_T2 = "t2",

    E_DANGEROUSATTACKS = "dangerousattacks",
            A_DANGEROUSATTACKS_T1 = "t1",
            A_DANGEROUSATTACKS_T2 = "t2",

    E_PENALTIES = "penalties",
            A_PENALTIES_T1 = "t1",
            A_PENALTIES_T2 = "t2",

    E_SHOTSOFFTARGET = "shotsofftarget",
            A_SHOTSOFFTARGET_T1 = "t1",
            A_SHOTSOFFTARGET_T2 = "t2",

    E_SHOTSONTARGET = "shotsontarget",
            A_SHOTSONTARGET_T1 = "t1",
            A_SHOTSONTARGET_T2 = "t2",

    E_THROWINS = "throwins",
            A_THROWINS_T1 = "t1",
            A_THROWINS_T2 = "t2",

    E_GOALKICKS = "goalkicks",
            A_GOALKICKS_T1 = "t1",
            A_GOALKICKS_T2 = "t2",

    E_FREEKICKS = "freekicks",
            A_FREEKICKS_T1 = "t1",
            A_FREEKICKS_T2 = "t2",

    E_KICKOFFTEAM = "kickoffteam",
            A_KICKOFFTEAM_TEAM = "team",

    E_SHOTSBLOCKED = "shotsblocked",
            A_SHOTSBLOCKED_T1 = "t1",
            A_SHOTSBLOCKED_T2 = "t2",

    E_GOALKEEPERSAVES = "goalkeepersaves",
            A_GOALKEEPERSAVES_T1 = "t1",
            A_GOALKEEPERSAVES_T2 = "t2",

    E_OFFSIDES = "offsides",
            A_OFFSIDES_T1 = "t1",
            A_OFFSIDES_T2 = "t2",

    E_INJURIES = "injuries",
            A_INJURIES_T1 = "t1",
            A_INJURIES_T2 = "t2",

    E_SUSPENSIONS = "suspensions",
            A_SUSPENSIONS_T1 = "t1",
            A_SUSPENSIONS_T2 = "t2",

    E_FREETHROWS = "freethrows",
            A_FREETHROWS_T1 = "t1",
            A_FREETHROWS_T2 = "t2",

    E_DIRECTFOULSPERIOD = "directfoulsperiod",
            A_DIRECTFOULSPERIOD_T1 = "t1",
            A_DIRECTFOULSPERIOD_T2 = "t2",

    E_DIRECTFREEKICKS = "directfreekicks",
            A_DIRECTFREEKICKS_T1 = "t1",
            A_DIRECTFREEKICKS_T2 = "t2",

    E_POSSESSION = "possession",
            A_POSSESSION_T1 = "t1",
            A_POSSESSION_T2 = "t2",
            A_POSSESSION_TEAM = "team",

    E_WEATHERCONDITIONS = "weatherconditions",
            A_WEATHERCONDITIONS_ID = "id",
            A_WEATHERCONDITIONS_NAME = "name",

    E_PITCHCONDITIONS = "pitchconditions",
            A_PITCHCONDITIONS_ID = "id",
            A_PITCHCONDITIONS_NAME = "name",

    E_SURFACETYPE = "surfacetype",
            A_SURFACETYPE_ID = "id",
            A_SURFACETYPE_NAME = "name",

    E_ODDSSUGGESTIONS = "OddsSuggestions",
            A_ODDSSUGGESTIONS_MATCHID = "matchid",

    E_ODDS = "Odds" ,
            A_ODDS_BOOKID = "bookId",
            A_ODDS_CHANGENUMBER = "changenumber",
            A_ODDS_DESCRIPTION = "description",
            A_ODDS_GUTHMATCHID = "guthMatchId",
            A_ODDS_MANUALACTIVE = "manualActive",
            A_ODDS_MATCHID = "matchId",
            A_ODDS_SPECIALODDSVALUE = "specialOddsValue",
            A_ODDS_SUBTYPE = "subtype",
            A_ODDS_TYPE = "type",
            A_ODDS_VALIDDATE = "validDate",
            A_ODDS_PREF = "pref",
            A_ODDS_ALSOODDS = "alsoOdds",

    E_ODDSFIELD = "OddsField" ,
            A_ODDSFIELD_DESCRIPTION = "description",
            A_ODDSFIELD_SIDE = "side",

    E_TOURNAMENT = "tournament",
            A_TOURNAMENT_ID = "id",
            A_TOURNAMENT_NAME = "name",

    E_CATEGORY = "category" ,
            A_CATEGORY_ID = "id" ,
            A_CATEGORY_NAME = "name",

    E_SPORT = "sport" ,
            A_SPORT_ID = "id" ,
            A_SPORT_NAME = "name",

    E_LOGIN = "login",
            A_LOGIN_RESULT = "result",

    E_USER = "user",
            A_USER_BOOKMAKERID = "bookmakerid",

    E_FLASHCONFIG = "flashconfig",
            E_EVENTCONFIG = "eventconfig",

    E_EVENTS = "events",

    E_BOOKMATCH = "bookmatch",
            A_BOOKMATCH_MATCHID = "matchid",
            A_BOOKMATCH_RESULT = "result",
            A_BOOKMATCH_MESSAGE = "message",

    E_DBFAIL = "dbfail",
            A_DBFAIL_REQUEST = "request"
                    ;
}
