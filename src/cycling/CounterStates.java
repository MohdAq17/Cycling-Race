package cycling;

import java.io.Serializable;

/**
 * CounterStates - A class used to generate the IDs of races, stages, segments,
 * teams and riders.
 */
public class CounterStates implements Serializable {

    private int raceCounter;
    private int teamCounter;
    private int riderCounter;
    private int segmentCounter;
    private int stageCounter;

    public CounterStates() {
        raceCounter = 1;
        teamCounter = 1;
        riderCounter = 1;
        segmentCounter = 1;
        stageCounter = 1;
    }

    public int getRaceCounter() {
        return raceCounter;
    }

    public void incrementRaceCounter() {
        raceCounter = raceCounter + 1;
    }

    public int getTeamCounter() {
        return teamCounter;
    }

    public void incrementTeamCounter() {
        teamCounter = teamCounter + 1;
    }

    public int getRiderCounter() {
        return riderCounter;
    }

    public void incrementRiderCounter() {
        riderCounter = riderCounter + 1;
    }

    public int getSegmentCounter() {
        return segmentCounter;
    }

    public void incrementSegmentCounter() {
        segmentCounter = segmentCounter + 1;
    }

    public int getStageCounter() {
        return stageCounter;
    }

    public void incrementStageCounter() {
        stageCounter = stageCounter + 1;
    }

    /**
     * resets all the Ids
     */
    public void resetAllCounts() {
        raceCounter = 1;
        teamCounter = 1;
        riderCounter = 1;
        segmentCounter = 1;
        stageCounter = 1;
    }
}
