package io.ilot.plol.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MarketLiveOdds
{
    private int id, typeID, subtype, combinations;
    private String title, type, specialOddsValue, clearedScore;
    private boolean isActive, isChanged, isCleared;
    private Boolean isMostBalanced;

    private List<OddField> oddFieldsList;

    public MarketLiveOdds()
    {
        id = typeID = combinations = subtype = 0;
        title = type = specialOddsValue = clearedScore = "";
        isActive = isChanged = isCleared = false;
        isMostBalanced = null;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public int getSubtype() {
        return subtype;
    }
    public void setSubtype(int subtype) {
        this.subtype = subtype;
    }
    public int getTypeID() {
        return typeID;
    }
    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }
    public String getSpecialOddsValue() {
        return specialOddsValue;
    }
    public void setSpecialOddsValue(String specialOddsValue) {
        this.specialOddsValue = specialOddsValue;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    public Boolean isMostBalanced() {
        return isMostBalanced;
    }
    public void setMostBalanced(boolean isMostBalanced) {
        this.isMostBalanced = isMostBalanced;
    }
    public boolean isChanged() {
        return isChanged;
    }
    public void setChanged(boolean isChanged) {
        this.isChanged = isChanged;
    }
    public boolean isCleared() {
        return isCleared;
    }
    public void setCleared(boolean isCleared) {
        this.isCleared = isCleared;
    }
    public int getCombinations() {
        return combinations;
    }
    public void setClearedScore(String clearedScore)
    {
        this.clearedScore = clearedScore;
    }
    public String getClearedScore()
    {
        return clearedScore;
    }
    public void setCombinations(int combinations)
    {
        this.combinations = combinations;
    }

    public List<OddField> getOddFieldsList()
    {
        if (oddFieldsList!=null)
            return oddFieldsList;
        else
            return Collections.<OddField>emptyList();
    }

    public void addOddField(OddField oddField)
    {
        if (oddFieldsList==null)
            oddFieldsList = new ArrayList<OddField>();

        oddFieldsList.add(oddField);
    }

//	public void toXml(XMLStreamWriter2 xmlw) throws XMLStreamException
//	{
//		xmlw.writeStartElement(ConstantsOutput.E_MARKET);
//		xmlw.writeAttribute(ConstantsOutput.A_MARKET_ID, ""+getId());
//		xmlw.writeAttribute(ConstantsOutput.A_MARKET_ACTIVE, ""+isActive());
//		xmlw.writeAttribute(ConstantsOutput.A_MARKET_CHANGED, ""+isChanged());
//		xmlw.writeAttribute(ConstantsOutput.A_MARKET_DESCR, getTitle());
//		xmlw.writeAttribute(ConstantsOutput.A_MARKET_CLEARED, ""+isCleared());
//		xmlw.writeAttribute(ConstantsOutput.A_MARKET_CLEAREDSCORE, ""+getClearedScore());
//		if (getOddFieldsList().size()>0)
//		{
//			xmlw.writeRaw("\n");
//			for (OddField oddField: getOddFieldsList())
//			{
//				oddField.toXml(xmlw);
//			}
//		}
//		xmlw.writeEndElement();xmlw.writeRaw("\n");
//	}

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Odd: ");
        sb.append("id="+id);
        sb.append(", isActive="+isActive);
        sb.append(", isChanged="+isChanged);
        sb.append(", title="+title);
        sb.append(", type="+type);
        sb.append(", subtype="+subtype);
        sb.append(", typeID="+typeID);
        sb.append(", specialOddsValue="+specialOddsValue);
        sb.append(", isMostBalanced="+isMostBalanced);
        sb.append(", clearedScore="+clearedScore);
        sb.append(", combinations="+combinations);

        if (oddFieldsList!=null)
            for (OddField of: oddFieldsList)
                sb.append("\n -"+of);

        return sb.toString();
    }




}
