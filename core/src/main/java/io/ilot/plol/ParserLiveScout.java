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
import java.util.Map.Entry;
import java.util.concurrent.Future;

import javax.persistence.Entity;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import io.ilot.plol.model.Event;
import io.ilot.plol.repos.EventRepository;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;


public class ParserLiveScout
{


    public EventRepository eventRepository;
    private InputStream inputStream;
    private OutputStream outputStream;

    private LiveScoutMessage currentMessage = null;
    private /*LiveScoutMatch*/ Event currentMatch = null;
    private int messageCounter = 1;
    private boolean isMatchList = false;
    private boolean isRunning = false;
    private boolean shouldStop = false;
//    private Odds currentOdds = null;
    private boolean xmlDocEnded = false;

    private boolean useStoredMessages = false;
    private String folderToReadMessagesFrom = "C:\\StoredBetradarMessages\\LiveScout\\27footballs\\";

    private long lastMessageTimestamp = 0;

//    @Interceptors({ParsersInterceptor.class})
    public void run() throws Exception
    {
        System.out.println("Parser Live Scout Started");

        XMLStreamReader2 xmlr = null;

        try {
            XMLInputFactory xmlif = getXmlInputFactory2Instance();

            messageCounter = 1;

            shouldStop = false;
            isRunning = true;
            while (!shouldStop)
            {
                xmlr = getXmlStreamReader(inputStream, xmlif);
                if (xmlr == null) // if stops getting data from socket or error
                    break; // happens

                int eventType;
                xmlDocEnded = false;
                while (!xmlDocEnded && xmlr.hasNext()) {
                    eventType = xmlr.next();
                    switch (eventType) {
                        case XMLStreamConstants.START_ELEMENT:
                            String elementName = xmlr.getName().toString();
                            parseElement(elementName, xmlr);
                            break;
                        case XMLStreamConstants.END_ELEMENT:
                            String endElementName = xmlr.getName().toString();
                            parseEndElement(endElementName);
                            break;
                    }
                }

                System.out.println("Parser Live Scout -xml no:"+messageCounter+" proccessed");
                messageCounter++;

            }
            isRunning = false;
            System.out.println("Parser Live Scout stopped!");

        }
        catch (Exception e)
        {
            e.printStackTrace();
            currentMessage = null;
            currentMatch = null;
            isMatchList = false;
//            currentOdds = null;
            xmlDocEnded = false;
            System.out.println("Parser Live Scout stopped!");
            isRunning = false;
            throw e;
        }
    }


    private void parseEndElement(String endElementName) {
        if ((currentMessage.isMatchList() && (endElementName
                .equals(ConstantsLiveScout.E_MATCHLIST)||endElementName
                .equals(ConstantsLiveScout.E_MATCHLIST_UPDATE)))
                || (!currentMessage.isMatchList() && endElementName
                .equals(ConstantsLiveScout.E_MATCH))
                || endElementName.equals(ConstantsLiveScout.E_ODDSSUGGESTIONS)
                || endElementName.equals(ConstantsLiveScout.E_BOOKMATCH)
                || endElementName.equals(ConstantsLiveScout.E_MATCHSTOP)
                || endElementName.equals(ConstantsLiveScout.E_SERVERTIME)
                || endElementName.equals(ConstantsLiveScout.E_INFOS)
                || endElementName.equals(ConstantsLiveScout.E_LOGIN)
                || endElementName.equals(ConstantsLiveScout.E_DBFAIL)
                || endElementName.equals("ct")) {
            String waitTimeSinceLastMessage = "";
            long currentTimestamp = System.currentTimeMillis();
            if (lastMessageTimestamp>0){
                waitTimeSinceLastMessage = " (time since last xml "+(currentTimestamp - lastMessageTimestamp) + "ms)";
            }
            lastMessageTimestamp = currentTimestamp;
            System.out.println("Parser Live Scout - Received xml no:"+messageCounter + waitTimeSinceLastMessage);
            handleCurrentMessage();
            xmlDocEnded = true;
            currentMessage = null;
            currentMatch = null;
//            currentOdds = null;
        }
        if (endElementName.equals(ConstantsLiveScout.E_MATCH)) {
            currentMatch = null;
        } else if (endElementName.equals(ConstantsLiveScout.E_ODDS)) {
            /*currentMessage.getOdds().add(currentOdds);
            currentOdds = null;*/
        }
    }


    public void matchlist() {
        XMLStreamWriter2 xtw = null;
        try {
            XMLOutputFactory xof = XMLOutputFactory.newInstance();
            xtw = (XMLStreamWriter2) xof.createXMLStreamWriter(outputStream);
            xtw.writeStartElement("matchlist");
            xtw.writeAttribute("hoursback", "4");
            xtw.writeAttribute("hoursforward", "168");
            xtw.writeEndElement();
            xtw.writeRaw("\n");

            xtw.flush();
            xtw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private XMLInputFactory getXmlInputFactory2Instance() {
        XMLInputFactory xmlif = XMLInputFactory2.newInstance();

        xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,
                Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
                Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        return xmlif;
    }

    private XMLStreamReader2 getXmlStreamReader(InputStream socketInputStream,
                                                XMLInputFactory xmlif) {
        XMLStreamReader2 xmlr = null;

        boolean succeeded = false;
        while (!succeeded)
        {
            try
            {
                    String storedMessagesFolder = folderToReadMessagesFrom;
                    File storedFile = new File(storedMessagesFolder +(messageCounter)+ ".xml");
                    int counter = 0;
                    while (!storedFile.exists() && counter < 100000) {
                        counter++;
                        storedFile = new File(storedMessagesFolder + (++messageCounter)+ ".xml");
                    }
                    FileInputStream fin = new FileInputStream(storedFile);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    xmlr = (XMLStreamReader2) xmlif.createXMLStreamReader(fin);
                    succeeded = true;

            }
            catch (XMLStreamException e) {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                if (useStoredMessages){
                    System.out.println("All stored messages parsed.");
                    break;
                }
            }
        }
        return xmlr;
    }


    private void parseElement(String elementName, XMLStreamReader2 xmlr)
            throws XMLStreamException{

        if (currentMessage == null
                && elementName.equals(ConstantsLiveScout.E_MATCHLIST)) {
            currentMessage = new LiveScoutMessage(true, false);
        } else if (currentMessage == null
                && elementName.equals(ConstantsLiveScout.E_MATCHLIST_UPDATE)) {
            currentMessage = new LiveScoutMessage(true, false);
        } else if (currentMessage == null
                && elementName.equals(ConstantsLiveScout.E_ODDSSUGGESTIONS)) {
            currentMessage = new LiveScoutMessage(false, true);
        } else if (currentMessage == null) {
            currentMessage = new LiveScoutMessage();
        }

        if (elementName.equals(ConstantsLiveScout.E_MATCH))
            parseMatchElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_MATCHLIST))
            parseMatchlistElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_MATCHLIST_UPDATE))
            parseMatchlistElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_STATUS))
            parseStatusElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_SCORE))
            parseScoreElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_EVENT))
            parseEventElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_SERVE))
            parseServeElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_RED))
            parseRedElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_YELLOW))
            parseYellowElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_CORNERS))
            parseCornersElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_DANGEROUSATTACKS))
            parseDangerousattacksElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_PENALTIES))
            parsePenaltiesElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_THROWINS))
            parseThrowinsElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_GOALKICKS))
            parseGoalkicksElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_FREEKICKS))
            parseFreekicksElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_KICKOFFTEAM))
            parseKickoffteamElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_SHOTSBLOCKED))
            parseShotsblockedElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_GOALKEEPERSAVES))
            parseGoalkeepersavesElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_OFFSIDES))
            parseOffsidesElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_SUSPENSIONS))
            parseSuspensionsElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_DIRECTFREEKICKS))
            parseDirectfreekicksElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_POSSESSION))
            parsePossessionElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_ODDSSUGGESTIONS))
            parseOddssuggestionsElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_ODDS))
            parseOddsElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_ODDSFIELD))
            parseOddsfieldElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_TOURNAMENT))
            parseTournamentElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_CATEGORY))
            parseCategoryElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_SPORT))
            parseSportElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_LOGIN))
            /*parseLoginElement(xmlr)*/;
        else if (elementName.equals(ConstantsLiveScout.E_USER))
           /* parseUserElement(xmlr)*/;
        else if (elementName.equals(ConstantsLiveScout.E_EVENTS))
            parseEventsElement(xmlr);
        else if (elementName.equals(ConstantsLiveScout.E_BOOKMATCH))
            /*parseBookmatchElement(xmlr)*/;
        else if (elementName.equals("ct")) {

        } else
            System.out.println("Encountered unknown element '"+ elementName + "'");
    }

    private void parseKickoffteamElement(XMLStreamReader2 xmlr) {
        /*for (int i = 0; i < xmlr.getAttributeCount(); i++) {
            String attrName = xmlr.getAttributeLocalName(i);
            if (attrName.equals(ConstantsLiveScout.A_KICKOFFTEAM_TEAM)) {
                currentMatch.setKickoffteam(HomeAwayType.getHomeAwayType(xmlr
                        .getAttributeValue(i)));
            }
        }*/
    }


    private void parseEventsElement(XMLStreamReader2 xmlr) {
//        currentMatch.setActivities(new ArrayList<Activity>());
    }

    private void parseSportElement(XMLStreamReader2 xmlr) {
        /*try {
            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
                String attrName = xmlr.getAttributeLocalName(i);
                if (attrName.equals(ConstantsLiveScout.A_SPORT_ID)) {
                    currentMatch.setSport(Sport.getSport(xmlr
                            .getAttributeAsInt(i)));
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }*/

    }

    private void parseTournamentElement(XMLStreamReader2 xmlr) {
        /*Tournament tournament = new Tournament();
        try {
            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
                String attrName = xmlr.getAttributeLocalName(i);
                if (attrName.equals(ConstantsLiveScout.A_TOURNAMENT_ID)) {
                    tournament.setId(xmlr.getAttributeAsLong(i));
                } else if (attrName
                        .equals(ConstantsLiveScout.A_TOURNAMENT_NAME)) {
                    tournament.setName(xmlr.getAttributeValue(i));
                }
            }
            currentMatch.setTournament(tournament);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }*/

    }

    private void parseOddsfieldElement(XMLStreamReader2 xmlr) {
        /*OddField oddField = new OddField();
        try {
            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
                String attrName = xmlr.getAttributeLocalName(i);
                if (attrName.equals(ConstantsLiveScout.A_ODDSFIELD_DESCRIPTION)) {
                    oddField.setDescription(xmlr.getAttributeValue(i));
                } else if (attrName.equals(ConstantsLiveScout.A_ODDSFIELD_SIDE)) {
                    oddField.setSide(xmlr.getAttributeValue(i));
                }
            }

            if (xmlr.next() == XMLStreamConstants.CHARACTERS) {
                oddField.setOdd(Double.parseDouble(xmlr.getText()));
            }
            currentOdds.getOddFields().add(oddField);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }

    private void parseOddsElement(XMLStreamReader2 xmlr) {
       /* try {
            Odds anOdds = new Odds();
            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
                String attrName = xmlr.getAttributeLocalName(i);
                if (attrName.equals(ConstantsLiveScout.A_ODDS_BOOKID)) {
                    anOdds.setBookId(xmlr.getAttributeAsInt(i));
                } else if (attrName
                        .equals(ConstantsLiveScout.A_ODDS_CHANGENUMBER)) {
                    anOdds.setChangeNumber(xmlr.getAttributeAsInt(i));
                } else if (attrName
                        .equals(ConstantsLiveScout.A_ODDS_DESCRIPTION)) {
                    anOdds.setDescription(xmlr.getAttributeValue(i));
                } else if (attrName
                        .equals(ConstantsLiveScout.A_ODDS_GUTHMATCHID)) {
                    anOdds.setGuthMatchId(xmlr.getAttributeAsLong(i));
                } else if (attrName
                        .equals(ConstantsLiveScout.A_ODDS_MANUALACTIVE)) {
                    anOdds.setManualActive(xmlr.getAttributeAsInt(i));
                } else if (attrName.equals(ConstantsLiveScout.A_ODDS_MATCHID)) {
                    anOdds.setMatchId(xmlr.getAttributeAsLong(i));
                } else if (attrName
                        .equals(ConstantsLiveScout.A_ODDS_SPECIALODDSVALUE)) {
                    anOdds.setSpecialOddsValue(xmlr.getAttributeValue(i));
                } else if (attrName.equals(ConstantsLiveScout.A_ODDS_SUBTYPE)) {
                    anOdds.setSubtype(xmlr.getAttributeAsInt(i));
                } else if (attrName.equals(ConstantsLiveScout.A_ODDS_TYPE)) {
                    anOdds.setType(xmlr.getAttributeAsInt(i));
                } else if (attrName.equals(ConstantsLiveScout.A_ODDS_VALIDDATE)) {
                    anOdds.setValidDate(xmlr.getAttributeAsLong(i));
                } else if (attrName.equals(ConstantsLiveScout.A_ODDS_PREF)) {
                    //anOdds.setPref(xmlr.getAttributeAsInt(i));
                } else if (attrName.equals(ConstantsLiveScout.A_ODDS_ALSOODDS)) {
                    anOdds.setAlsoOdds(xmlr.getAttributeAsInt(i));
                }
            }
            currentOdds = anOdds;
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }*/

    }

    private void parseOddssuggestionsElement(XMLStreamReader2 xmlr) {

    }

    private void parseSurfacetypeElement(XMLStreamReader2 xmlr) {
//        for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//            String attrName = xmlr.getAttributeLocalName(i);
//            if (attrName.equals(ConstantsLiveScout.A_SURFACETYPE_NAME)) {
//                currentMatch.setSurfaceType(xmlr.getAttributeValue(i));
//            }
//        }

    }

    private void parsePitchconditionsElement(XMLStreamReader2 xmlr) {
//        for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//            String attrName = xmlr.getAttributeLocalName(i);
//            if (attrName.equals(ConstantsLiveScout.A_PITCHCONDITIONS_NAME)) {
//                currentMatch.setPitchConditions(xmlr.getAttributeValue(i));
//            }
//        }

    }

    private void parseWeatherconditionsElement(XMLStreamReader2 xmlr) {
//        for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//            String attrName = xmlr.getAttributeLocalName(i);
//            if (attrName.equals(ConstantsLiveScout.A_WEATHERCONDITIONS_NAME)) {
//                currentMatch.setWeatherConditions(xmlr.getAttributeValue(i));
//            }
//        }

    }

    private void parsePossessionElement(XMLStreamReader2 xmlr) {
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_POSSESSION_T1)) {
//                    currentMatch.setPossessionHome(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_POSSESSION_T2)) {
//                    currentMatch.setPossessionAway(xmlr.getAttributeAsInt(i));
//                } else if (attrName
//                        .equals(ConstantsLiveScout.A_POSSESSION_TEAM)) {
//                    currentMatch.setPossesionTeam(HomeAwayType
//                            .getHomeAwayType(xmlr.getAttributeValue(i)));
//                }
//            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }

    }

    private void parseDirectfreekicksElement(XMLStreamReader2 xmlr) {
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_DIRECTFREEKICKS_T1)) {
//                    currentMatch.setDirectfreekicksHome(xmlr
//                            .getAttributeAsInt(i));
//                } else if (attrName
//                        .equals(ConstantsLiveScout.A_DIRECTFREEKICKS_T2)) {
//                    currentMatch.setDirectfreekicksAway(xmlr
//                            .getAttributeAsInt(i));
//                }
//            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }

    }

    private void parseDirectfoulsperiodElement(XMLStreamReader2 xmlr) {
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_DIRECTFOULSPERIOD_T1)) {
//                    currentMatch.setDirectfoulsperiodHome(xmlr
//                            .getAttributeAsInt(i));
//                } else if (attrName
//                        .equals(ConstantsLiveScout.A_DIRECTFOULSPERIOD_T2)) {
//                    currentMatch.setDirectfoulsperiodAway(xmlr
//                            .getAttributeAsInt(i));
//                }
//            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }

    }

    private void parseFreethrowsElement(XMLStreamReader2 xmlr) {
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_FREETHROWS_T1)) {
//                    currentMatch.setFreethrowsHome(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_FREETHROWS_T2)) {
//                    currentMatch.setFreethrowsAway(xmlr.getAttributeAsInt(i));
//                }
//            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }

    }

    private void parseSuspensionsElement(XMLStreamReader2 xmlr) {
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_SUSPENSIONS_T1)) {
//                    currentMatch.setSuspensionsHome(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_SUSPENSIONS_T2)) {
//                    currentMatch.setSuspensionsAway(xmlr.getAttributeAsInt(i));
//                }
//            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }

    }

    private void parseInjuriesElement(XMLStreamReader2 xmlr) {
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_INJURIES_T1)) {
//                    currentMatch.setInjuriesHome(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_INJURIES_T2)) {
//                    currentMatch.setInjuriesAway(xmlr.getAttributeAsInt(i));
//                }
//            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }

    }

    private void parseOffsidesElement(XMLStreamReader2 xmlr) {
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_OFFSIDES_T1)) {
//                    currentMatch.setOffsidesHome(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_OFFSIDES_T2)) {
//                    currentMatch.setOffsidesAway(xmlr.getAttributeAsInt(i));
//                }
//            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }

    }

    private void parseGoalkeepersavesElement(XMLStreamReader2 xmlr) {
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_GOALKEEPERSAVES_T1)) {
//                    currentMatch.setGoalkeepersavesHome(xmlr
//                            .getAttributeAsInt(i));
//                } else if (attrName
//                        .equals(ConstantsLiveScout.A_GOALKEEPERSAVES_T2)) {
//                    currentMatch.setGoalkeepersavesAway(xmlr
//                            .getAttributeAsInt(i));
//                }
//            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }

    }

    private void parseShotsblockedElement(XMLStreamReader2 xmlr) {
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_SHOTSBLOCKED_T1)) {
//                    currentMatch.setShotsblockedHome(xmlr.getAttributeAsInt(i));
//                } else if (attrName
//                        .equals(ConstantsLiveScout.A_SHOTSBLOCKED_T2)) {
//                    currentMatch.setShotsblockedAway(xmlr.getAttributeAsInt(i));
//                }
//            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }

    }

    private void parseFreekicksElement(XMLStreamReader2 xmlr) {
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_FREEKICKS_T1)) {
//                    currentMatch.setFreekicksHome(xmlr.getAttributeAsInt(0));
//                } else if (attrName.equals(ConstantsLiveScout.A_FREEKICKS_T2)) {
//                    currentMatch.setFreekicksAway(xmlr.getAttributeAsInt(0));
//                }
//            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }

    }

    private void parseGoalkicksElement(XMLStreamReader2 xmlr) {
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_GOALKICKS_T1)) {
//                    currentMatch.setGoalkicksHome(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_GOALKICKS_T2)) {
//                    currentMatch.setGoalkicksAway(xmlr.getAttributeAsInt(i));
//                }
//            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }

    }

    private void parseThrowinsElement(XMLStreamReader2 xmlr) {
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_THROWINS_T1)) {
//                    currentMatch.setThrowinsHome(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_THROWINS_T2)) {
//                    currentMatch.setThrowinsAway(xmlr.getAttributeAsInt(i));
//                }
//            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }
    }

    private void parseShotsontargetElement(XMLStreamReader2 xmlr) {
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_SHOTSONTARGET_T1)) {
//                    currentMatch
//                            .setShotsOnTargetHome(xmlr.getAttributeAsInt(i));
//                } else if (attrName
//                        .equals(ConstantsLiveScout.A_SHOTSONTARGET_T2)) {
//                    currentMatch
//                            .setShotsOnTargetAway(xmlr.getAttributeAsInt(i));
//                }
//            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }

    }

    private void parseShotsofftargetElement(XMLStreamReader2 xmlr) {
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_SHOTSOFFTARGET_T1)) {
//                    currentMatch.setShotsOfftargetHome(xmlr
//                            .getAttributeAsInt(i));
//                } else if (attrName
//                        .equals(ConstantsLiveScout.A_SHOTSOFFTARGET_T2)) {
//                    currentMatch.setShotsOfftargetAway(xmlr
//                            .getAttributeAsInt(i));
//                }
//            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }

    }

    private void parsePenaltiesElement(XMLStreamReader2 xmlr) {
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_PENALTIES_T1)) {
//                    currentMatch.setPenaltiesHome(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_PENALTIES_T2)) {
//                    currentMatch.setPenaltiesAway(xmlr.getAttributeAsInt(i));
//                }
//            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }
    }

    private void parseDangerousattacksElement(XMLStreamReader2 xmlr) {
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_DANGEROUSATTACKS_T1)) {
//                    currentMatch.setDangerousAttacksHome(xmlr
//                            .getAttributeAsInt(i));
//                } else if (attrName
//                        .equals(ConstantsLiveScout.A_DANGEROUSATTACKS_T2)) {
//                    currentMatch.setDangerousAttacksAway(xmlr
//                            .getAttributeAsInt(i));
//                }
//            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }

    }

    private void parseYellowElement(XMLStreamReader2 xmlr) {
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_YELLOW_T1)) {
//                    currentMatch.setYellowCardsHome(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_YELLOW_T2)) {
//                    currentMatch.setYellowCardsAway(xmlr.getAttributeAsInt(i));
//                }
//            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }

    }

    private void parseCornersElement(XMLStreamReader2 xmlr) {
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_CORNERS_T1)) {
//                    currentMatch.setCornersHome(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_CORNERS_T2)) {
//                    currentMatch.setCornersAway(xmlr.getAttributeAsInt(i));
//                }
//            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }

    }

    private void parseRedElement(XMLStreamReader2 xmlr) {
        try {
            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
                String attrName = xmlr.getAttributeLocalName(i);
                if (attrName.equals(ConstantsLiveScout.A_RED_T1)) {
                    currentMatch.setRedCardsHome(xmlr.getAttributeAsInt(i));
                } else if (attrName.equals(ConstantsLiveScout.A_RED_T2)) {
                    currentMatch.setRedCardsAway(xmlr.getAttributeAsInt(i));
                }
            }

        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private void parseServeElement(XMLStreamReader2 xmlr) {
//        for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//            String attrName = xmlr.getAttributeLocalName(i);
//            if (attrName.equals(ConstantsLiveScout.A_SERVE_TEAM)) {
//                currentMatch.setServe(HomeAwayType.getHomeAwayType(xmlr
//                        .getAttributeValue(i)));
//            }
//        }
    }

    private void parseTiebreakElement(XMLStreamReader2 xmlr) {
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_TIEBREAK_VALUE)) {
//                    currentMatch.setTiebreak(xmlr.getAttributeAsBoolean(i));
//
//                }
//            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }

    }

    private void parseMatchlistElement(XMLStreamReader2 xmlr) {
    }

    private void parseScoreElement(XMLStreamReader2 xmlr) {

//        try {
//            Score score = new Score();
//
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_SCORE_T1)) {
//                    score.setT1(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_SCORE_T2)) {
//                    score.setT2(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_SCORE_TYPE)) {
//                    score.setType(ScoreType.getScoreType(xmlr.getAttributeValue(i)));
//                }
//            }
//            currentMatch.getScores().add(score);
//            currentMatch.setScore(score.getT1()+":"+score.getT2());
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }

    }



    private void parseStatusElement(XMLStreamReader2 xmlr) {
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_STATUS_ID)) {
//                    currentMatch.setStatusId(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_STATUS_NAME)) {
//                    currentMatch.setStatus(xmlr.getAttributeValue(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_STATUS_START)) {
//                    currentMatch.setStatusTimeStamp(xmlr.getAttributeAsLong(i));
//                }
//            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }
    }

    private void parseCategoryElement(XMLStreamReader2 xmlr) {
//        Category category = new Category();
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_CATEGORY_ID)) {
//                    category.setId(xmlr.getAttributeAsLong(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_CATEGORY_NAME)) {
//                    category.setName(xmlr.getAttributeValue(i));
//                }
//            }
//            currentMatch.setCategory(category);
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }

    }

    private void parseEventElement(XMLStreamReader2 xmlr) {
//        Activity newActivity = new Activity();
//        try {
//            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
//                String attrName = xmlr.getAttributeLocalName(i);
//                if (attrName.equals(ConstantsLiveScout.A_EVENT_ID)) {
//                    newActivity.setId(xmlr.getAttributeAsLong(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_EVENT_INFO)) {
//                    newActivity.setInfo(xmlr.getAttributeValue(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_EVENT_MTIME)) {
//                    newActivity.setMtime(xmlr.getAttributeValue(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_EVENT_SIDE)) {
//                    newActivity.setSide(HomeAwayType.getHomeAwayType(xmlr.getAttributeValue(i)));
//                } else if (attrName.equals(ConstantsLiveScout.A_EVENT_STIME)) {
//                    newActivity.setStime(xmlr.getAttributeAsLong(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_EVENT_TYPE)) {
//                    newActivity.setType(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_EVENT_POSX)) {
//                    newActivity.setPosx(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_EVENT_POSY)) {
//                    newActivity.setPosy(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_EVENT_PLAYER1)) {
//                    newActivity.setPlayer1(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_EVENT_PLAYER2)) {
//                    newActivity.setPlayer2(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_EVENT_GAMENUMBER)) {
//                    newActivity.setGameNumber(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_EVENT_SETNUMBER)) {
//                    newActivity.setSetNumber(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_EVENT_GAMESCORE)) {
//                    newActivity.setGameScore(xmlr.getAttributeValue(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_EVENT_SETSCORE)) {
//                    newActivity.setSetScore(xmlr.getAttributeValue(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_EVENT_MATCHSCORE)) {
//                    newActivity.setMatchScore(xmlr.getAttributeValue(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_EVENT_PERIODNUMBER)) {
//                    newActivity.setPeriodNumber(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_EVENT_REMAININGTIMEPERIOD)) {
//                    newActivity.setRemainingTimePeriod(xmlr.getAttributeValue(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_EVENT_EXTRAINFO)) {
//                    newActivity.setExtraInfo(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_EVENT_CORRECTEDFROM)) {
//                    newActivity.setCorrectedfrom(xmlr.getAttributeAsInt(i));
//                } else if (attrName.equals(ConstantsLiveScout.A_EVENT_CORRECTEDTO)) {
//                    newActivity.setCorrectedTo(xmlr.getAttributeAsInt(i));
//                }
//            }
//            currentMatch.getActivities().add(newActivity);
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }

    }

    private void parseMatchElement(XMLStreamReader2 xmlr)
    {
        currentMatch = new Event();
//
        for (int i = 0; i < xmlr.getAttributeCount(); i++) {
            String attrName = xmlr.getAttributeLocalName(i);
            try {
                if (attrName.equals(ConstantsLiveScout.A_MATCH_BETSTATUS)) {
                    currentMatch.setBetStatus(xmlr.getAttributeValue(i));
                } else if (attrName.equals(ConstantsLiveScout.A_MATCH_CONNECTIONSTATUS)) {
//                    currentMatch.setConnectionStatus(xmlr.getAttributeAsBoolean(i));
                } else if (attrName.equals(ConstantsLiveScout.A_MATCH_DC)) {
//                    currentMatch.setDeepCoverage(xmlr.getAttributeAsBoolean(i));
                } else if (attrName.equals(ConstantsLiveScout.A_MATCH_DISTANCE)) {
//                    currentMatch.setDistance(xmlr.getAttributeAsInt(i));
                } else if (attrName.equals(ConstantsLiveScout.A_MATCH_EXTRAINFO)) {
//                    currentMatch.setExtraInfo(ExtraInfo.getExtraInfo(xmlr.getAttributeAsInt(i)));
                } else if (attrName.equals(ConstantsLiveScout.A_MATCH_FEEDTYPE)) {
//                    currentMatch.setFeedType(FeedType.getFeedType(xmlr.getAttributeValue(i)));
                } else if (attrName.equals(ConstantsLiveScout.A_MATCH_FIRSTSERVE)) {
//                    currentMatch.setFirstServe(HomeAwayType.getHomeAwayType(xmlr.getAttributeValue(i)));
                } else if (attrName.equals(ConstantsLiveScout.A_MATCH_MATCHID)) {
                    currentMatch.setId(xmlr.getAttributeAsLong(i));
                } else if (attrName.equals(ConstantsLiveScout.A_MATCH_NUMBEROFSETS)) {
//                    currentMatch.setNumberOfSets(xmlr.getAttributeAsInt(i));
                } else if (attrName.equals(ConstantsLiveScout.A_MATCH_START)) {
                    currentMatch.setKickOff(xmlr.getAttributeAsLong(i));
                } else if (attrName.equals(ConstantsLiveScout.A_MATCH_T1ID)) {
//                    currentMatch.setT1id(xmlr.getAttributeAsInt(i));
                } else if (attrName.equals(ConstantsLiveScout.A_MATCH_T1UID)) {
//                    currentMatch.setSt1id(xmlr.getAttributeAsInt(i));
                } else if (attrName.equals(ConstantsLiveScout.A_MATCH_T2ID)) {
//                    currentMatch.setT2id(xmlr.getAttributeAsInt(i));
                } else if (attrName.equals(ConstantsLiveScout.A_MATCH_T2UID)) {
//                    currentMatch.setSt2id(xmlr.getAttributeAsInt(i));
                }else if (attrName.equals(ConstantsLiveScout.A_MATCH_T1NAME)) {
//                    currentMatch.setT1Name(xmlr.getAttributeValue(i));
                } else if (attrName.equals(ConstantsLiveScout.A_MATCH_T2NAME)) {
//                    currentMatch.setT2Name(xmlr.getAttributeValue(i));
                } else if (attrName.equals(ConstantsLiveScout.A_MATCH_TIEBREAKLASTSET)) {
//                    currentMatch.setTieBreakLastSet(xmlr.getAttributeAsBoolean(i));
                } else if (attrName.equals(ConstantsLiveScout.A_MATCH_TIMERUNNING)) {
//                    currentMatch.setTimeRunning(xmlr.getAttributeAsBoolean(i));
                } else if (attrName.equals(ConstantsLiveScout.A_MATCH_WONJUMPBALL)) {
//                    currentMatch.setWonJumpBall(HomeAwayType.getHomeAwayType(xmlr.getAttributeValue(i)));
                } else if (attrName.equals(ConstantsLiveScout.A_MATCH_TIME)) {
                    currentMatch.setMatchTime(xmlr.getAttributeValue(i));
                }else{

                }
            }  catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }

        if (currentMessage.isMatchList()) {
            currentMessage.setEvent(currentMatch);
        }

    }

    private void handleCurrentMessage()
    {
        try{
            if (currentMessage.isMatchList() || currentMatch != null)
            {
                eventRepository.save(currentMatch);
            }
            else if (currentMessage.isHasOdds())
            {
                //do nothing, we ignore odds offered by liveScout feed.
            }
           /* else if (currentMatch != null) {



                boolean recovery = false;
                boolean continueToNext = false;
                boolean isDeltaUpdate = false;
                if(currentMatch.getFeedType() == FeedType.FULL)
                {
                    //Manual to Feed EventMode transition
                    if(matchesForTransition.contains(currentMatch.getMatchId())){
                        //call liveScoutTranslator
                        System.out.println("Start manual to feed/normal mode transition for event "+currentMatch.getMatchId());
                        translator.handleManualToFeedTransition(currentMatch, currentMatch.getStatusTimeStamp());
                        activeMatches.put(currentMatch.getMatchId(), currentMatch);
                        translator.addTransitionActiveEvent(currentMatch.getMatchId());
                    }
                    matchesForTransition.remove(currentMatch.getMatchId());

                    if(translator.eventExistsAndNotEnded(currentMatch.getMatchId()) &&
                            !SportEventStatus.getStatusNotStarted().equalsIgnoreCase(currentMatch.getStatus()))
                    {
                        translator.addActiveEvent(currentMatch.getMatchId());
                        //translator.updatePeriod(currentMatch.getMatchId(),currentMatch.getStatus(),currentMatch.getStatusTimeStamp());
                        recovery = true;
                        activeMatches.put(currentMatch.getMatchId(), currentMatch);
                    }
                    else{
                        currentMatch.setBetstatus(BetStatus.CHANGE);
                        activeMatches.put(currentMatch.getMatchId(), currentMatch);
                    }
                }
                else
                {
                    if(!translator.checkEventIdMap(currentMatch.getMatchId())
                            && translator.findInternalEvent(currentMatch.getMatchId()) !=null
                            && !EventPeriodHelper.hasEnded(translator.findInternalEvent(currentMatch.getMatchId())) )
                    {
                        if(!endedMatches.containsKey(currentMatch.getMatchId()))
                        {
                            translator.addActiveEvent(currentMatch.getMatchId());
                            if(translator.checkEventIdMap(currentMatch.getMatchId()))
                            {
                                ArrayList<Long> tempMatchId = new ArrayList<Long>();
                                tempMatchId.add(currentMatch.getMatchId());
                                continueToNext = true;
                            }
                        }
                    }
                    else
                        updateChanges(currentMatch,	activeMatches.get(currentMatch.getMatchId()));
                }

                if(!continueToNext && activeMatches.get(currentMatch.getMatchId())!=null)
                {
                    if(currentMatch.getFeedType() == FeedType.DELTAUPDATE)
                        isDeltaUpdate = true;

                    if (currentMatch.getActivities() != null)
                    {
                        translator.handleActivities(currentMatch, recovery, isDeltaUpdate);
                        String matchStatus = activeMatches.get(currentMatch.getMatchId()).getStatus();
                        if (matchStatus.equals("ENDED")
                                || matchStatus.equals("AFTER_PENALTIES")
                                || matchStatus.equals("AFTER_OT")
                                || matchStatus.equals("AFTER_SD"))
                        {
                            activeMatches.remove(currentMatch.getMatchId());
                            endedMatches.put(currentMatch.getMatchId(), new DateTime());
                        }
                    }
                }
            }*/
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /*private void updateChanges(LiveScoutMatch currentMatch, LiveScoutMatch storedMatch)
    {
        if(storedMatch!=null)
        {
            if (currentMatch.getStatusId() != storedMatch.getStatusId() && currentMatch.getStatus() != null)
            {
                storedMatch.setStatus(currentMatch.getStatus());
                storedMatch.setStatusId(currentMatch.getStatusId());
                storedMatch.setStatusTimeStamp(currentMatch.getStatusTimeStamp());
            }
            else if (!currentMatch.getBetstatus().equals(storedMatch.getBetstatus()))
            {
                if (currentMatch.getBetstatus().equals(BetStatus.STARTED))
                    translator.handleBetstart(currentMatch.getMatchId(), (new DateTime()).getMillis());
                else if (currentMatch.getBetstatus().equals(BetStatus.STOPPED))
                    translator.handleBetstop(currentMatch.getMatchId(), (new DateTime()).getMillis());
                storedMatch.setBetstatus(currentMatch.getBetstatus());
            }

            if (storedMatch.getFirstServe()==null && currentMatch.getFirstServe()!=null)
            {
                translator.handleFirstServer(currentMatch.getMatchId(), currentMatch.getFirstServe(), (new DateTime()).getMillis());
                storedMatch.setFirstServe(currentMatch.getFirstServe());
            }

            if (currentMatch.getPossesionTeam()!=null && !currentMatch.getPossesionTeam().equals(storedMatch.getPossesionTeam()))
            {
                translator.handlePossession(currentMatch.getMatchId(), currentMatch.getPossesionTeam(), currentMatch.getStatusTimeStamp());
                storedMatch.setPossesionTeam(currentMatch.getPossesionTeam());
            }
        }
*/
    }


    @Entity @Data @NoArgsConstructor class LiveScoutMessage {

        private int matchid;
        private boolean isMatchList;
        private boolean hasOdds;
        private Event event;

        LiveScoutMessage(boolean isMatchList, boolean hasOdds){
            this.hasOdds = hasOdds;
            this.isMatchList = isMatchList;
        }
    }

}
