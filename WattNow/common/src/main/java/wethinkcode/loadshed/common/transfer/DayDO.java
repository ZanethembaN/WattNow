package wethinkcode.loadshed.common.transfer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * TODO: javadoc DayDO
 */
public class DayDO
{
    private List<SlotDO> loadSheddingSlots;

    public DayDO(){
    }

    @JsonCreator
    public DayDO(
        @JsonProperty( value = "slots" ) List<SlotDO> slots ){
        loadSheddingSlots = slots;
    }

    public List<SlotDO> getSlots(){
        return loadSheddingSlots;
    }

    public int numberOfSlots(){
        return getSlots().size();
    }

    @Override
    public String toString() {
        StringBuilder slots = new StringBuilder();
        slots.append("[");
        for (SlotDO slot : loadSheddingSlots) {
            slots.append(slot.toString());
            slots.append(",");
        }
        slots.delete(slots.lastIndexOf(","), slots.lastIndexOf(","));
        slots.append("]");
        return "{"+
            "slots:" + slots.toString()+
            "}";
    }

}
