package io.ilot.plol.model;

public class OddField
{
    public OddField()
    {

    }
    public OddField(boolean isActive, String type, float value,
                    float voidFactor, boolean isFinalOutcome) {
        super();
        this.isActive = isActive;
        this.type = type;
        this.value = value;
        this.voidFactor = voidFactor;
        this.isFinalOutcome = isFinalOutcome;
    }

    public OddField(boolean isActive, String type, float value)
    {
        this(isActive, type, value, 0, false);
    }

    private boolean isActive;
    private String type;
    private float value, voidFactor;
    private boolean isFinalOutcome = false;



    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getValue() {
        return value;
    }

    public boolean isFinalOutcome() {
        return isFinalOutcome;
    }

    public void setFinalOutcome(boolean isFinalOutcome) {
        this.isFinalOutcome = isFinalOutcome;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getVoidFactor() {
        return voidFactor;
    }

    public void setVoidFactor(float voidFactor) {
        this.voidFactor = voidFactor;
    }



    @Override
    public String toString()
    {
        return "Oddfield [active="+isActive+", type="+type+", value="+value
                +", finalOutcome="+isFinalOutcome+", voidFactor="+voidFactor+"]";
    }

}
