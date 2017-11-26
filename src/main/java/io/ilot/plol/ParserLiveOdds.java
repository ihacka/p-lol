package io.ilot.plol;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import io.ilot.plol.model.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.springframework.stereotype.Component;


@Component
public class ParserLiveOdds
{

    private InputStream inputStream;
    private OutputStream outputStream;

    private HashMap<Long, Long> repliesForMatches = new HashMap<Long, Long>();
    private List<Long> matchesWaitingForReply = new ArrayList<Long>();
    private List<Long> matchesForTransition = new ArrayList<Long>();

    private List<MarketLiveOdds> marketLiveOdds = new ArrayList<>();
    private LiveOddsMessage currentMessage = null;
    private Event currentMatch = null;
    private MarketLiveOdds currentOdd = null;
    private int messageCounter = 1;
    private String currentClearedScore = null;
    private boolean isRunning = false;
    private boolean shouldStop = false;
    private boolean keepFeedFiles = true;
    protected int testNum;
    protected boolean useStoredMessages;
    private boolean useBetradarTestMatches;
    protected String storedMessagesFolder;


    @PostConstruct
    protected void init(){
//        useBetradarTestMatches = true;
//        testNum = 13;  //1=football, 2=tennis, 5=ice hockey, 6=basketball, 7=Handball, 8=Volleyball, 9=Beach Volleyball , 12=Baseball, 13=Rugby, 21=Table Tennis
        useStoredMessages = true;
        storedMessagesFolder = "C:\\ODDSMessages\\ArsenalVSTot\\";
//        parserStarter.start();
    }




    public void startParser() throws Exception
    {
        try
        {
            System.out.println("Parser Live Odds Started");
            if (useStoredMessages)
                messageCounter = 1;
            XMLStreamReader2 xmlr = null;

            XMLInputFactory xmlif = getXmlInputFactory2Instance();

            shouldStop = false;
            isRunning = true;

            while (!shouldStop())
            {
                xmlr = getXmlStreamReader(xmlif);

                if (xmlr==null) //if stops getting data from socket or error happens
                    break;

                long processStartTimestamp = System.currentTimeMillis();
                int eventType;
                boolean xmlDocEnded = false;
                while(!xmlDocEnded && xmlr.hasNext())
                {
                    eventType = xmlr.next();
                    switch (eventType)
                    {
                        case XMLStreamConstants.START_ELEMENT:
                            String elementName = xmlr.getName().toString();
                            parseElement(elementName, xmlr);
                            break;
                        case XMLStreamConstants.END_ELEMENT:
                            String endElementName = xmlr.getName().toString();
                            if (endElementName.equals(ConstantsLiveOdds.E_BETRADARLIVEODDS))
                            {
                                xmlDocEnded = true;
                                System.out.println("Parser Live Odds - Received xml no:"+messageCounter);
                                handleCurrentMessage();
                                currentMessage = null;
                            }
                            else if (endElementName.equals(ConstantsLiveOdds.E_MATCH))
                            {
//                                currentMessage.addSportEvent(currentMatch);
                                currentMatch = null;
                                currentClearedScore = null;
                            }
                            else if (endElementName.equals(ConstantsLiveOdds.E_ODDS))
                            {
//                                currentMatch.addOdd(currentOdd);
                                marketLiveOdds.add(currentOdd);
                                currentOdd = null;
                            }
                            break;
                    }
                }
                System.out.println("Parser Live Odds -xml no:"+messageCounter+" proccessed in "+ (System.currentTimeMillis()-processStartTimestamp) +"ms");
                messageCounter++;
            }
            isRunning = false;
            System.out.println("Parser Live Odds stopped!");

            //send betstop to all feed only/passthrough events
//            stopBetting2FeedModeEvents();

        }
        catch (Exception e)
        {
            e.printStackTrace();

            currentMessage = null;
            currentMatch = null;
            currentOdd = null;
            if (useStoredMessages)
                messageCounter = 1;
            currentClearedScore = null;
            System.out.println("Parser Live Odds stopped!");
            isRunning = false;
//            stopBetting2FeedModeEvents();
            throw e;
        }
    }


    protected boolean shouldStop() {
        return shouldStop;
    }


    private XMLInputFactory getXmlInputFactory2Instance()
    {
        XMLInputFactory xmlif = XMLInputFactory2.newInstance();
//		XMLInputFactory2 xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
        xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
//		xmlif.configureForSpeed();
        return xmlif;
    }



    private XMLStreamReader2 getXmlStreamReader(XMLInputFactory xmlif)
    {
        XMLStreamReader2 xmlr = null;

        boolean succeeded = false;
        while (!succeeded)
        {
            try
            {
                if (useStoredMessages)
                {
                    File storedFile = new File(storedMessagesFolder+(messageCounter)+".xml");
                    int counter = 0;
                    while (!storedFile.exists() && counter<100000)
                    {
                        counter++;
                        storedFile = new File(storedMessagesFolder+(++messageCounter)+".xml");
                    }
                    FileInputStream fin = new FileInputStream(storedFile);

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    xmlr = (XMLStreamReader2) xmlif.createXMLStreamReader(fin);
                    succeeded = true;
                }

            }
            catch (XMLStreamException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                if (useStoredMessages)
                {
                    System.out.println("All stored messages parsed.");
                    break;
                }
                else
                {
                    e.printStackTrace();
                }
            }
        }

        return xmlr;
    }



    private void parseElement(String elementName, XMLStreamReader2 xmlr) throws XMLStreamException, Exception
    {
        if (elementName.equals(ConstantsLiveOdds.E_BETRADARLIVEODDS))
            parseMessageBasicInfo(xmlr);
        else if (elementName.equals(ConstantsLiveOdds.E_MATCH))
            parseMatchElement(xmlr);
        else if (elementName.equals(ConstantsLiveOdds.E_MATCHINFO))
        {/*Ignore*/}
        else if (elementName.equals(ConstantsLiveOdds.E_ODDS))
            parseOddElement(xmlr);
        else if (elementName.equals(ConstantsLiveOdds.E_ODDSFIELD))
            parseOddsFieldElement(xmlr);
        else if (elementName.equals(ConstantsLiveOdds.E_SCORE))
            parseScoreElement(xmlr);
        else if (elementName.equals(ConstantsLiveOdds.E_CARD))
            parseCardElement(xmlr);
        else if (elementName.equals(ConstantsLiveOdds.E_MESSAGE))
            parseMessageElement(xmlr);
        else if (elementName.equals(ConstantsLiveOdds.E_HOMETEAM))
            parseHomeTeamElement(xmlr);
        else if (elementName.equals(ConstantsLiveOdds.E_AWAYTEAM))
            parseAwayTeamElement(xmlr);
        else if (elementName.equals(ConstantsLiveOdds.E_CATEGORY))
            parseCategoryElement(xmlr);
        else if (elementName.equals(ConstantsLiveOdds.E_TOURNAMENT))
            parseTournamentElement(xmlr);
        else if (elementName.equals(ConstantsLiveOdds.E_TVCHANNELS))
        {/*Ignore*/}
        else if (elementName.equals(ConstantsLiveOdds.E_TVCHANNEL))
            parseTVChannelElement(xmlr);
        else if (elementName.equals(ConstantsLiveOdds.E_EXTRAINFO))
        {/*Ignore*/}
        else if (elementName.equals(ConstantsLiveOdds.E_INFO))
            parseInfoElement(xmlr);
        else if (elementName.equals(ConstantsLiveOdds.E_TRANSLATION))
            xmlr.skipElement();/*Ignore*/
        else if (elementName.equals(ConstantsLiveOdds.E_SPORT))
            parseSportElement(xmlr);
        else if (elementName.equals(ConstantsLiveOdds.E_DATEOFMATCH))
            parseDateOfMatchElement(xmlr);
        else
            System.out.println("Encountered unknown element '"+elementName+"'");
//			throw new ParserBetRadarException("Encountered unknown element '"+elementName+"'");

    }

    private void parseMessageBasicInfo(XMLStreamReader2 xmlr) throws XMLStreamException, Exception
    {
        LiveOddsMessage message = new LiveOddsMessage();
        for (int i=0; i<xmlr.getAttributeCount();i++)
        {
            String attrName = xmlr.getAttributeLocalName(i);
            if (attrName.equals(ConstantsLiveOdds.A_BETRADARLIVEODDS_TIMESTAMP))
                message.setCurrentTimestamp(xmlr.getAttributeAsLong(i));
            else if (attrName.equals(ConstantsLiveOdds.A_BETRADARLIVEODDS_TIME))
                message.setTime(xmlr.getAttributeAsLong(i));
            else if (attrName.equals(ConstantsLiveOdds.A_BETRADARLIVEODDS_STATUS))
                message.setMessageStatus(MessageStatusLiveOdds.getMessageStatus(xmlr.getAttributeValue(i)));
            else if (attrName.equals(ConstantsLiveOdds.A_BETRADARLIVEODDS_REPLYTYPE))
                message.setReplyType(ReplyTypeLiveOdds.getReplyType(xmlr.getAttributeValue(i)));
            else if (attrName.equals(ConstantsLiveOdds.A_BETRADARLIVEODDS_REPLYNR))
                message.setReplyNr(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_BETRADARLIVEODDS_STARTTIME))
                message.setStartTime(xmlr.getAttributeAsLong(i));
            else if (attrName.equals(ConstantsLiveOdds.A_BETRADARLIVEODDS_ENDTIME))
                message.setEndTime(xmlr.getAttributeAsLong(i));
            else if (attrName.equals(ConstantsLiveOdds.A_BETRADARLIVEODDS_XMLNS))
            {/*Ignore*/}
            else if (attrName.equals(ConstantsLiveOdds.A_BETRADARLIVEODDS_TYPE))
                message.setMessageType(MessageTypeLiveOdds.getMessageType(xmlr.getAttributeValue(i)));
            else
                System.out.println("Encountered unknown attribute '"+attrName+"' in element "+ConstantsLiveOdds.E_BETRADARLIVEODDS);
//				throw new ParserBetRadarException("Encountered unknown attribute '"+attrName+"' in element "+ConstantsLiveOdds.E_BETRADARLIVEODDS);
        }
        currentMessage = message;
    }

    private void parseDateOfMatchElement(XMLStreamReader2 xmlr) throws XMLStreamException
    {
//        xmlr.next();
//        long eventDateTimestamp = Long.parseLong(xmlr.getText().trim());
//        currentMatch.setEventDate(eventDateTimestamp);
    }

    private void parseSportElement(XMLStreamReader2 xmlr) throws Exception, XMLStreamException
    {
       /* int sportID = Integer.parseInt(xmlr.getAttributeValue(null, ConstantsLiveOdds.A_SPORT_ID));
        xmlr.next();
        String sportName = xmlr.getText().trim();
        currentMatch.setSport(sportID);
        currentMatch.setSportName(sportName);*/
    }

    private void parseTVChannelElement(XMLStreamReader2 xmlr) throws XMLStreamException
    {
//        xmlr.next();
//        currentMatch.addTVChannel(xmlr.getText().trim());
    }

    private void parseInfoElement(XMLStreamReader2 xmlr) throws XMLStreamException
    {
//        String infoType = xmlr.getAttributeValue(0);
//        xmlr.next();
//        String value = xmlr.getText().trim();
//        currentMatch.addExtraInfo(infoType, value);
    }

    private void parseTournamentElement(XMLStreamReader2 xmlr) throws XMLStreamException
    {
//        Tournament tournament = new Tournament();
//        tournament.setId(xmlr.getAttributeAsInt(0));
//        xmlr.next();
//        tournament.setName(xmlr.getText().trim());
//        currentMatch.setTournament(tournament);
    }

    private void parseCategoryElement(XMLStreamReader2 xmlr) throws XMLStreamException
    {
//        Category category = new Category();
//        category.setId(xmlr.getAttributeAsInt(0));
//        xmlr.next();
//        category.setName(xmlr.getText().trim());
//        currentMatch.setCategory(category);
    }

    private void parseHomeTeamElement(XMLStreamReader2 xmlr) throws XMLStreamException
    {
//        SportsTeam team = new SportsTeam();
//        for (int i=0; i<xmlr.getAttributeCount(); i++)
//        {
//            String attrName = xmlr.getAttributeLocalName(i);
//            if (attrName.equals(ConstantsLiveOdds.A_HOMETEAM_ID))
//                team.setId(xmlr.getAttributeAsInt(i));
//            else if (attrName.equals(ConstantsLiveOdds.A_HOMETEAM_UNIQUEID))
//                team.setUniqueId(xmlr.getAttributeAsInt(i));
//            else
//                System.out.println("Match id:"+currentMatch.getId()+". Encountered unknown attribute '"+attrName+"' in element "+ConstantsLiveOdds.E_HOMETEAM);
////				throw new ParserBetRadarException("Match id:"+currentMatch.getId()+". Encountered unknown attribute '"+attrName+"' in element "+ConstantsLiveOdds.E_HOMETEAM);
//        }
//        xmlr.next();
//        team.setName(xmlr.getText().trim());
//        currentMatch.setHomeTeam(team);

    }

    private void parseAwayTeamElement(XMLStreamReader2 xmlr) throws XMLStreamException
    {
//        SportsTeam team = new SportsTeam();
//        for (int i=0; i<xmlr.getAttributeCount(); i++)
//        {
//            String attrName = xmlr.getAttributeLocalName(i);
//            if (attrName.equals(ConstantsLiveOdds.A_AWAYTEAM_ID))
//                team.setId(xmlr.getAttributeAsInt(i));
//            else if (attrName.equals(ConstantsLiveOdds.A_AWAYTEAM_UNIQUEID))
//                team.setUniqueId(xmlr.getAttributeAsInt(i));
//            else
//                System.out.println("Match id:"+currentMatch.getId()+". Encountered unknown attribute '"+attrName+"' in element "+ConstantsLiveOdds.E_AWAYTEAM);
////				throw new ParserBetRadarException("Match id:"+currentMatch.getId()+". Encountered unknown attribute '"+attrName+"' in element "+ConstantsLiveOdds.E_AWAYTEAM);
//        }
//        xmlr.next();
//        team.setName(xmlr.getText().trim());
//        currentMatch.setAwayTeam(team);

    }

    private void parseMatchElement(XMLStreamReader2 xmlr) throws XMLStreamException
    {
       /* Event match;
//		long matchID = Long.parseLong(xmlr.getAttributeValue(null, ConstantsLiveOdds.A_MATCH_MATCHID));
//		if (activeMatches.containsKey(matchID))
//			match = activeMatches.get(matchID);
//		else
        match = new Event();

        //We use this if/else way because the Match element doesn't always have the same number of attributes.
        for (int i=0; i<xmlr.getAttributeCount(); i++)
        {
            String attrName = xmlr.getAttributeLocalName(i);
            if (attrName.equals(ConstantsLiveOdds.A_MATCH_ACTIVE))
                match.setActive(xmlr.getAttributeAsBoolean(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_MATCHID))
                match.setId(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_BETSTATUS))
                match.setBetStatus(BetStatus.getBetStatus(xmlr.getAttributeValue(i)));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_EARLYBETSTATUS))
                match.setEarlyBetStatus(BetStatus.getBetStatus(xmlr.getAttributeValue(i)));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_BOOKED))
            {
                boolean booked = xmlr.getAttributeAsBoolean(i);
                match.setBooked(booked);
//				if (!booked)
//					unbookedMatches.add(matchID);
            }
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_CLEAREDSCORE))
                currentClearedScore = xmlr.getAttributeValue(i);
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_MATCHTIME)){
                match.setMatchtime(xmlr.getAttributeAsInt(i));
                match.setMatchtimeString(xmlr.getAttributeValue(i));
            }
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_MSGNR))
                match.setMsgNum(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_SCORE))
                match.setScore(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_REMAINING_TIME))
                match.setRemainingTime(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_REMAINING_TIME_IN_PERIOD))
                match.setRemainingTimeInPeriod(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_CLOCK_STOPPED))
                match.setClockStopped(xmlr.getAttributeAsBoolean(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_GAMESCORE))
                match.setGameScore(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_TIEBREAK))
                match.setTieBreak(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_SERVER))
                match.setCurrentServer(xmlr.getAttributeValue(i));
//				match.setCurrentGameServe(TeamHomeAway.getTeamHomeAway(xmlr.getAttributeAsInt(i)));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_SETSCORES))
                match.setSetscores(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_SETSCORE1))
                match.setSetscore1(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_SETSCORE2))
                match.setSetscore2(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_SETSCORE3))
                match.setSetscore3(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_SETSCORE4))
                match.setSetscore4(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_SETSCORE5))
                match.setSetscore5(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_SETSCORE6))
                match.setSetscore6(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_SETSCORE7))
                match.setSetscore7(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_SETSCORE8))
                match.setSetscore8(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_SETSCORE9))
                match.setSetscore9(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_SETSCORE10))
                match.setSetscore10(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_STATUS))
                match.setStatus(xmlr.getAttributeValue(i));
//				match.setStatus(SportEventStatusFactory.getSportEventStatusInstance((int)match.getSportID(), xmlr.getAttributeValue(i)));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_CORNERSAWAY))
                match.setCornersAway(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_CORNERSHOME))
                match.setCornersHome(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_YELLOWCARDSAWAY))
                match.setYellowCardsAway(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_YELLOWCARDSHOME))
                match.setYellowCardsHome(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_YELLOWREDCARDSAWAY))
                match.setYellowRedCardsAway(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_YELLOWREDCARDSHOME))
                match.setYellowRedCardsHome(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_REDCARDSAWAY))
                match.setRedCardsAway(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_REDCARDSHOME))
                match.setRedCardsHome(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_SUSPENDHOME))
                match.setSuspendHome(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_SUSPENDAWAY))
                match.setSuspendAway(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_BALLS))
                match.setBalls(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_OUTS))
                match.setOuts(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_STRIKES))
                match.setStrikes(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_HOMEBATTER))
                match.setHomeBatter(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_AWAYBATTER))
                match.setAwayBatter(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_BASES))
                match.setBases(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_POSITION))
                match.setPosition(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_POSSESSION))
                match.setPossession(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_YARDS))
                match.setYards(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_TRY))
                match.setTries(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_MATCH_EXPEDITE))
                match.setExpedite(xmlr.getAttributeAsInt(i));
            else
                System.out.println("Encountered unknown attribute '"+attrName+"' in element "+ConstantsLiveOdds.E_MATCH);
//				throw new ParserBetRadarException("Match id:"+currentMatch.getId()+". Encountered unknown attribute '"+attrName+"' in element "+ConstantsLiveOdds.E_MATCH);
        }
        match.clearOdds(); //TODO: we should remove that in the future. We should update.
        currentMatch = match;*/
    }

    private void parseOddsFieldElement(XMLStreamReader2 xmlr) throws XMLStreamException
    {
        OddField oddfield = new OddField();
        boolean  hasOutcome = false;
        for (int i=0; i<xmlr.getAttributeCount();i++)
        {
            String attrName = xmlr.getAttributeLocalName(i);
            if (attrName.equals(ConstantsLiveOdds.A_ODDSFIELD_ACTIVE))
                oddfield.setActive(xmlr.getAttributeAsBoolean(i));
            else if (attrName.equals(ConstantsLiveOdds.A_ODDSFIELD_TYPE))
                oddfield.setType(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_ODDSFIELD_VOIDFACTOR))
                oddfield.setVoidFactor(xmlr.getAttributeAsFloat(i));
            else if (attrName.equals(ConstantsLiveOdds.A_ODDSFIELD_OUTCOME))
            {
                oddfield.setFinalOutcome(xmlr.getAttributeAsBoolean(i));
                hasOutcome = true;
            }
            else
                System.out.println("Match id:"+currentMatch.getId()+". Encountered unknown attribute '"+attrName+"' in element "+ConstantsLiveOdds.E_ODDSFIELD);
//				throw new ParserBetRadarException("Match id:"+currentMatch.getId()+". Encountered unknown attribute '"+attrName+"' in element "+ConstantsLiveOdds.E_ODDSFIELD);
        }
        if (oddfield.isActive() && !hasOutcome)
        {
            //There should be a CHARACTER event following
            xmlr.next();
            if (xmlr.isCharacters())
                oddfield.setValue(Float.parseFloat(xmlr.getText().trim()));
            else
                oddfield.setValue(0);
        }
        currentOdd.addOddField(oddfield);
    }

    private void parseOddElement(XMLStreamReader2 xmlr) throws XMLStreamException
    {
        MarketLiveOdds odd = new MarketLiveOdds();

        for (int i=0; i<xmlr.getAttributeCount();i++)
        {
            String attrName = xmlr.getAttributeLocalName(i);
            if (attrName.equals(ConstantsLiveOdds.A_ODDS_ID))
                odd.setId(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_ODDS_FREETEXT))
                odd.setTitle(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_ODDS_TYPEID))
                odd.setTypeID(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_ODDS_TYPE))
                odd.setType(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_ODDS_SUBTYPE))
                odd.setSubtype(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_ODDS_SPECIALODDSVALUE))
                odd.setSpecialOddsValue(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_ODDS_MOSTBALANCED))
                odd.setMostBalanced(xmlr.getAttributeAsInt(i)==1);
            else if (attrName.equals(ConstantsLiveOdds.A_ODDS_COMBINATION))
                odd.setCombinations(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_ODDS_ACTIVE))
                odd.setActive(xmlr.getAttributeAsInt(i)==1);
            else if (attrName.equals(ConstantsLiveOdds.A_ODDS_CHANGED))
                odd.setChanged(xmlr.getAttributeAsBoolean(i));
            else
                System.out.println("Match id:"+currentMatch.getId()+". Encountered unknown attribute '"+attrName+"' in element "+ConstantsLiveOdds.E_ODDS);
//				throw new ParserBetRadarException("Match id:"+currentMatch.getId()+". Encountered unknown attribute '"+attrName+"' in element "+ConstantsLiveOdds.E_ODDS);

        }
        odd.setClearedScore(currentClearedScore);
        odd.setCleared(currentClearedScore!=null);
        currentOdd = odd;
    }

    private void parseMessageElement(XMLStreamReader2 xmlr) throws XMLStreamException
    {
        //There should be a CHARACTER event following
        xmlr.next();
        @SuppressWarnings("unused")
        String message = xmlr.getText().trim();
        // TODO: Should show to the operator's gui or log?
    }

    private void parseScoreElement(XMLStreamReader2 xmlr) throws XMLStreamException
    {
        /*ScoreType scoreType = CommonScoreType.getScoreType(xmlr.getAttributeValue(null, ConstantsLiveOdds.A_SCORE_TYPE));
        if (scoreType.equals(CommonScoreType.SCORE) || scoreType.equals(CommonScoreType.LIVE)) // it's a single goal
        {
            Goal goal = new Goal();
            goal.setActivityType("score");
            for (int i=0; i<xmlr.getAttributeCount();i++)
            {
                String attrName = xmlr.getAttributeLocalName(i);
                if (attrName.equals(ConstantsLiveOdds.A_SCORE_ID))
                    goal.setId(xmlr.getAttributeAsInt(i));
                else if (attrName.equals(ConstantsLiveOdds.A_SCORE_TIME))
                    goal.setMatchTime(xmlr.getAttributeAsInt(i));
                else if (attrName.equals(ConstantsLiveOdds.A_SCORE_AWAY))
                    goal.setAwayTeamScore(xmlr.getAttributeAsInt(i));
                else if (attrName.equals(ConstantsLiveOdds.A_SCORE_HOME))
                    goal.setHomeTeamScore(xmlr.getAttributeAsInt(i));
                else if (attrName.equals(ConstantsLiveOdds.A_SCORE_PLAYER))
                    goal.setPlayer(xmlr.getAttributeValue(i));//we only got player name
                else if (attrName.equals(ConstantsLiveOdds.A_SCORE_SCORINGTEAM))
                    goal.setTeam(TeamHomeAway.getTeamHomeAway(xmlr.getAttributeValue(i)));
                else if (attrName.equals(ConstantsLiveOdds.A_SCORE_CANCELED))
                    goal.setCanceled(xmlr.getAttributeAsBoolean(i));
                else if (attrName.equals(ConstantsLiveOdds.A_SCORE_TYPE))
                {*//*Ignore, already processed*//*}
                else
                    System.out.println("Match id:"+currentMatch.getId()+". Encountered unknown attribute '"+attrName+"' in element "+ConstantsLiveOdds.E_SCORE);
//					throw new ParserBetRadarException("Match id:"+currentMatch.getId()+". Encountered unknown attribute '"+attrName+"' in element "+ConstantsLiveOdds.E_SCORE);
            }
            currentMatch.addActivity(goal);
        }
        else //it the score of a specific game period, ie halftime
        {
            SportScore score = new SportScore();
            score.setType(scoreType);
            for (int i=0; i<xmlr.getAttributeCount();i++)
            {
                String attrName = xmlr.getAttributeLocalName(i);
                if (attrName.equals(ConstantsLiveOdds.A_SCORE_ID))
                {*//*Ignore*//*}
                else if (attrName.equals(ConstantsLiveOdds.A_SCORE_TIME))
                {*//*Ignore*//*}
                else if (attrName.equals(ConstantsLiveOdds.A_SCORE_AWAY))
                    score.setAwayTeamScore(xmlr.getAttributeAsInt(i));
                else if (attrName.equals(ConstantsLiveOdds.A_SCORE_HOME))
                    score.setHomeTeamScore(xmlr.getAttributeAsInt(i));
                else if (attrName.equals(ConstantsLiveOdds.A_SCORE_PLAYER))
                {*//*Ignore*//*}
                else if (attrName.equals(ConstantsLiveOdds.A_SCORE_SCORINGTEAM))
                {*//*Ignore*//*}
                else if (attrName.equals(ConstantsLiveOdds.A_SCORE_TYPE))
                {*//*Ignore, already processed*//*}
                else
                    System.out.println("Match id:"+currentMatch.getId()+". Encountered unknown attribute '"+attrName+"' in element "+ConstantsLiveOdds.E_SCORE);
//					throw new ParserBetRadarException("Match id:"+currentMatch.getId()+". Encountered unknown attribute '"+attrName+"' in element "+ConstantsLiveOdds.E_SCORE);
            }
            currentMatch.addScore(score);
        }*/
    }

    private void parseCardElement(XMLStreamReader2 xmlr) throws XMLStreamException,Exception
    {
        /*SoccerBooking booking = new SoccerBooking();

        for (int i=0; i<xmlr.getAttributeCount();i++)
        {
            String attrName = xmlr.getAttributeLocalName(i);
            if (attrName.equals(ConstantsLiveOdds.A_CARD_ID))
                booking.setId(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_CARD_TYPE))
            {
                booking.setCardType(SoccerCardType.getCardType(xmlr.getAttributeValue(i)));
                booking.setActivityType(xmlr.getAttributeValue(i));
            }
            else if (attrName.equals(ConstantsLiveOdds.A_CARD_TEAM))
                booking.setTeam(TeamHomeAway.getTeamHomeAway(xmlr.getAttributeValue(i)));
            else if (attrName.equals(ConstantsLiveOdds.A_CARD_TIME))
                booking.setMatchTime(xmlr.getAttributeAsInt(i));
            else if (attrName.equals(ConstantsLiveOdds.A_CARD_PLAYER))
                booking.setPlayer(xmlr.getAttributeValue(i));
            else if (attrName.equals(ConstantsLiveOdds.A_CARD_TEAM))
                booking.setTeam(TeamHomeAway.getTeamHomeAway(xmlr.getAttributeValue(i)));
            else if (attrName.equals(ConstantsLiveOdds.A_CARD_CANCELED))
                booking.setCanceled(xmlr.getAttributeAsBoolean(i));
            else
                System.out.println("Match id:"+currentMatch.getId()+". Encountered unknown attribute '"+attrName+"' in element "+ConstantsLiveOdds.E_CARD);
//				throw new ParserBetRadarException("Match id:"+currentMatch.getId()+". Encountered unknown attribute '"+attrName+"' in element "+ConstantsLiveOdds.E_CARD);
        }

        currentMatch.addActivity(booking);*/
    }


    private void handleCurrentMessage() throws XMLStreamException, IOException
    {
        /*update event markets*/
        List <Market> newMarkets = new ArrayList<>();
        List<Long> prevMarketIds = currentMatch.getMarkets().stream().map(m->m.getId()).collect(Collectors.toList());



        if (currentMessage.getMessageStatus().equals(MessageStatusLiveOdds.CHANGE) || currentMessage.getMessageStatus().equals(MessageStatusLiveOdds.CLEARBET ))
        {

            for (MarketLiveOdds mlo : marketLiveOdds) {

                if (prevMarketIds.contains(mlo.getId()) && mlo.isChanged()) {
                /*update existing market*/
                    Optional<Market> optionalM = currentMatch.getMarkets().stream().filter(m -> m.getId().equals((long) mlo.getId())).findFirst();

                    if (optionalM.isPresent()) {
                        Market updatedMarket = optionalM.get();
                        updatedMarket.setActive(mlo.isActive());

                        List<Selection> selections = new ArrayList<>();
                        for (OddField of : mlo.getOddFieldsList()) {
                            Selection selection = new Selection();
                            selection.setName(of.getType());
                            selection.setOdd(of.getValue());
                            selection.setActive(of.isActive());
                            selections.add(selection);
                        }

                        updatedMarket.setSelections(selections);
                    }
                } else {
                /*add new market*/
                    Market m = new Market();
                    m.setName(mlo.getTitle());
                    m.setId((long) mlo.getId());
                    m.setActive(mlo.isActive());

                    List<Selection> selections = new ArrayList<>();
                    for (OddField of : mlo.getOddFieldsList()) {
                        Selection selection = new Selection();
                        selection.setName(of.getType());
                        selection.setOdd(of.getValue());
                        selection.setActive(of.isActive());
                        selections.add(selection);
                    }
                    m.setSelections(selections);
                    newMarkets.add(m);
                }
            }


            currentMatch.getMarkets().addAll(newMarkets);

            if (currentMessage.getMessageStatus().equals(MessageStatusLiveOdds.CLEARBET)) {

                    /*result outcomes*/

                for (MarketLiveOdds mlo : marketLiveOdds) {
                    for (OddField of : mlo.getOddFieldsList()) {
                        if (of.isFinalOutcome() && !mlo.isActive()) {
                            System.out.println("Market " + mlo.getType() + "is cleared. Outcome " + of.getType() + " wins");
                        }
                    }
                }
            }
        }
        }



    /*protected List<BasicEventDataBetradar> getBasicEventDataListForMeta(LiveOddsMessage currentMessage)
    {
        List<BasicEventDataBetradar> basicEventDataList = new ArrayList<BasicEventDataBetradar>();

        for (LiveoddsSportEvent liveOddsEvent: currentMessage.getSportEvents())
        {
            if (ignoreEventInMeta(liveOddsEvent))
                continue;

            BasicEventDataBetradar bedb = new BasicEventDataBetradar();
            bedb.setEventID(liveOddsEvent.getId());
            bedb.setAwayTeamID(getTeamID(liveOddsEvent.getAwayTeam()));
            bedb.setAwayTeamName(liveOddsEvent.getAwayTeam().getName());
            bedb.setHomeTeamID(getTeamID(liveOddsEvent.getHomeTeam()));
            bedb.setHomeTeamName(liveOddsEvent.getHomeTeam().getName());
            bedb.setSportID(liveOddsEvent.getSportID());
            bedb.setSportName(liveOddsEvent.getSportName());
            bedb.setCategoryID(liveOddsEvent.getCategory().getId());
            bedb.setCategoryName(liveOddsEvent.getCategory().getName());
            bedb.setTournamentID(liveOddsEvent.getTournament().getId());
            bedb.setTournamentName(liveOddsEvent.getTournament().getName());
            bedb.setDateTimestamp(liveOddsEvent.getEventDate());
            String numberOfSets = liveOddsEvent.getExtraInfos().get(ConstantsLiveOdds.V_INFO_TYPE_NUM_OF_SETS);
            if (numberOfSets!=null){
                try {
                    bedb.setNumberOfSets(Integer.parseInt(numberOfSets));
                }
                catch (NumberFormatException nfe){
                    System.out.println("Match id:"+currentMatch.getId()+". Encountered invalid value for "+ConstantsLiveOdds.V_INFO_TYPE_NUM_OF_SETS);
                }
            }
            basicEventDataList.add(bedb);
        }

        return basicEventDataList;
    }*/

    /*protected boolean ignoreEventInMeta(LiveoddsSportEvent liveOddsEvent)
    {
        try{
            //if the event is not booked or has already started ignore it
            return (!liveOddsEvent.isBooked()
                    || (liveOddsEvent.getStatus()!=null && !SportEventStatus.getStatusNotStarted().equals(liveOddsEvent.getStatus())));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return true;
        }
    }*/

    /*protected long getTeamID(SportsTeam team) {
        return team.getUniqueId();
    }*/

    //Returns true if new event handled successfully, false if missing messages or event already ended
    /*protected boolean handleNewActiveMatch(LiveoddsSportEvent event, long timestamp)
    {
        long eventID = event.getId();
        if (event.getMsgNum()==1 )
        {
            if (translator.addActiveEvent(eventID))
            {
                translator.updateMessageNum(eventID, 1);
                translator.updatePeriod(eventID, event.getStatus(), timestamp, event.getMsgNum());
                activeMatches.put(eventID, event);
                System.out.println("First message received for event with ID "+eventID);
                return true;
            }
            else
                return false;
        }
        else
        {
            try {
                int firstMessageToResend = translator.getLatestEventMessageNumberByExternalID(eventID)+1;
                if (translator.eventExistsAndNotEnded(eventID) && !useBetradarTestMatches &&!useStoredMessages && firstMessageToResend!=-1 && !matchesWaitingForReply.contains(eventID) && event.getMsgNum()>0)
                {
                    System.out.println("Requesting missed messages "+firstMessageToResend+" to "+event.getMsgNum()+" of event "+event.getId());
                    requestPreviousMessages(event.getId(), firstMessageToResend, event.getMsgNum());
                }

            } catch (XMLStreamException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }
*/
    private void updatePeriodAndTimeOrRemoveMatches()
    {
        /*HashMap<Long, LiveoddsSportEvent> tempMap = new HashMap<Long, LiveoddsSportEvent>(activeMatches);
        for (long matchID:tempMap.keySet())
        {
            if (repliesForMatches.containsValue(matchID))
            {
                System.out.println("Parser - Ignoring new message while receiving error reply messages for event "+matchID);
                continue;
            }
            else if(matchesWaitingForReply.contains(matchID))
            {
                System.out.println("Parser - Ignoring new message while waiting error reply messages for event "+matchID);
                continue;
            }

            boolean isStillActive = false;
            for (LiveoddsSportEvent event: currentMessage.getSportEvents())
            {
                if (event.getId()==matchID)
                {
                    isStillActive = true;
                    performStandardEventChecks(event, currentMessage.getCurrentTimestamp(), false);
                    break;
                }
            }
            if (!isStillActive && isEndedStatus(tempMap.get(matchID).getStatus()))
            {
                activeMatches.remove(matchID);
                translator.removeInactiveEvent(matchID);
                System.out.println("Stop receiving messages for match with id "+matchID);
            }
        }*/
    }

    private boolean isEndedStatus(String status)
    {
        return (status.equals("ended") /*|| status.equals("after_ot")*/);
    }

//    private boolean performStandardEventChecks(Event event, long timestamp, boolean isErrorReply)
//    {
//        long eventID = event.getId();
//        boolean justAddedErrorReplyEvent = false;
//        if (!activeMatches.containsKey(eventID))//check if new event
//        {
//            if (isErrorReply)
//            {
//                if (translator.addActiveEvent(eventID))
//                {
//                    activeMatches.put(eventID, event);
//                    justAddedErrorReplyEvent = true;
//                }
//                else
//                    return false;
//            }
//            else
//                return handleNewActiveMatch(event, timestamp);
//        }
//
//        LiveoddsSportEvent storedEvent = activeMatches.get(eventID);
//
//        //check message number
//        int messageNum = getMessageNumber(event);
//        if (messageNum<0)
//            return false;
//
//
////			if (event.getTieBreak()!=null || event.getCurrentServer()!=null) //update score for set-based sports like tennis
//        if (translator.isMatchOfSetBasedSport(eventID))
//            checkForChangeOfSetbasedScore(event, storedEvent);
//        else //update score for sports that don't have goals, like basketball and baseball
//            checkForChangeOfScore(event, storedEvent);
//
//        //check for event status/period change
//        if (!event.getStatus().equals(storedEvent.getStatus()) || justAddedErrorReplyEvent)
//            translator.updatePeriod(eventID, event.getStatus(), timestamp, event.getMsgNum());
//
//        //update event time
//        if (event.getRemainingTime()!=null)
//        {
//            if (storedEvent.getRemainingTime()==null || !storedEvent.getRemainingTime().equals(event.getRemainingTime()))
//                translator.updateEventRemainingTime(eventID, event.getRemainingTime(), event.getStatus().equals("ot"), timestamp, event.getMsgNum());
//        }
//        else if (storedEvent.getMatchtime() < event.getMatchtime())
//            translator.updateEventCurrentTime(eventID, event.getMatchtime(), timestamp, event.getMsgNum());
//
//        if (messageNum>0)
//            translator.updateMessageNum(eventID, messageNum);
//
//        //replace event in HashMap
//        activeMatches.put(eventID, event);
//
//        return true;
//    }



   /* private void checkForChangeOfScore(LiveoddsSportEvent newEvent, LiveoddsSportEvent oldEvent)
    {
        checkForAmericanFootballStats(newEvent, oldEvent);

        String score = newEvent.getScore();
        if (score!=null && !score.contains("-") && !score.equals(oldEvent.getScore()))
        {
            try
            {
                String scoreHomeStr = score.substring(0, score.indexOf(":"));
                String scoreAwayStr = score.substring(score.indexOf(":")+1);
                int scoreHome = Integer.parseInt(scoreHomeStr);
                int scoreAway = Integer.parseInt(scoreAwayStr);
                translator.handleChangedScore(newEvent.getId(), scoreHome, scoreAway, currentMessage.getCurrentTimestamp(), newEvent.getMsgNum());
            }
            catch (Exception e)
            {
                System.out.println("Parser - Unknown format of score for event "+newEvent.getId());
            }
        }
    }*/




    /*
     * return values:
     * -1 = We have missed one or more messages. Requests resent.
     * 0 = Same message number as before, no need to update our database
     * >0 = New message number, we should update our database
     */
    /*private int getMessageNumber(LiveoddsSportEvent event)
    {
        long eventID = event.getId();
        LiveoddsSportEvent storedEvent = activeMatches.get(eventID);
        int storedMsgNum = 0;
        if(storedEvent!=null)
            storedMsgNum = storedEvent.getMsgNum();

        //check message number
        int messageNumDiff = event.getMsgNum()-storedMsgNum;//storedEvent.getMsgNum();
        if (messageNumDiff>1 && !useBetradarTestMatches && !useStoredMessages && !repliesForMatches.containsValue(eventID) && !matchesWaitingForReply.contains(eventID))
        {
            //TODO Log
            System.out.println("Missed "+(messageNumDiff-1)+" message(s) of match with betradar id = "+eventID+" after message #"+storedEvent.getMsgNum());
            try {
                requestPreviousMessages(eventID, (storedMsgNum+1), event.getMsgNum());
            } catch (XMLStreamException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return -1;
        }
        else if (messageNumDiff<=0)
            return 0;
        else //messageNumDiff==1
            return event.getMsgNum();
    }*/

    /*private void stopBetting2FeedModeEvents(){
        for(long eventExtId : activeMatches.keySet()){
            translator.handleEventForStoppedParser(eventExtId);
        }
    }*/


    @Entity
    @Data
    @NoArgsConstructor
    class LiveOddsMessage {
        @Id
        long id;
        long currentTimestamp, time, startTime, endTime, replyNr;
        MessageStatusLiveOdds messageStatus = null;
        MessageTypeLiveOdds messageType = null;
        ReplyTypeLiveOdds replyType;
        @OneToOne
        private Event event;
    }

}
