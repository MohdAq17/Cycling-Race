package cycling;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;



/**
 * CyclingPortal - A compiling and fucntioning implementor
 * of the CycllingPortalInterface Interface.
 */
@SuppressWarnings("unchecked")
public class CyclingPortal implements CyclingPortalInterface {
    private ArrayList<Team> teamObjects;
    private ArrayList<Race> raceObjects;
    private CounterStates counterStates;

    public CyclingPortal() {
        teamObjects = new ArrayList<>();
        raceObjects = new ArrayList<>();
        counterStates = new CounterStates();
    }

    @Override
    public int[] getRaceIds() {
        int raceCount = raceObjects.size();
        int[] idArray = new int[raceCount];
        // storing raceIds in an array which is returned later
        for (int i = 0; i < raceCount; i++) {
            idArray[i] = raceObjects.get(i).getId();
        }
        return idArray;
    }

    @Override
    public int createRace(String name, String description) throws IllegalNameException, InvalidNameException {
        if (doesRaceNameExist(name)) {
            throw new IllegalNameException("Race name already exists.");
        } else if (name == null || name == "" || name.length() > 30 || name.matches(".*\\s+.*")) {
            throw new InvalidNameException(
                    "Invalid name, must not be null, empty, not longer than 30 characters and not contain white spaces.");
        }
        // creating a race, adding it to the platform and then incrementing the counter
        int raceid = counterStates.getRaceCounter();
        Race race = new Race(raceid, name, description);
        registerRace(race);
        counterStates.incrementRaceCounter();
        return race.getId();
    }

    @Override
    public String viewRaceDetails(int raceId) throws IDNotRecognisedException {
        /*
         * checking if the race Id exists then returning
         * a string with the race details
         */
        if (doesRaceIdExist(raceId)) {
            int raceCount = raceObjects.size();
            for (int i = 0; i < raceCount; i++) {
                if (raceObjects.get(i).getId() == raceId) {
                    Race selectedRace = raceObjects.get(i);
                    String returnString = "Race ID: " + selectedRace.getId()
                            + "\nRace Name: " + selectedRace.getName()
                            + "\nRace Description: " + selectedRace.getDescription()
                            + "\nNumber of Stages: " + selectedRace.getNumberOfStages()
                            + "\nTotal Length: " + selectedRace.getTotalLength();
                    return returnString;
                }
            }
            return "";
        } else {
            throw new IDNotRecognisedException("The ID does not match to any race in the system.");
        }
    }

    @Override
    public void removeRaceById(int raceId) throws IDNotRecognisedException {
        /*
         * checking if the race Id exists then removing the
         * race from the system
         */
        if (doesRaceIdExist(raceId)) {
            int raceCount = raceObjects.size();
            for (int i = 0; i < raceCount; i++) {
                if (raceObjects.get(i).getId() == raceId) {
                    raceObjects.remove(i);
                    break;
                }
            }
        } else {
            throw new IDNotRecognisedException("The ID does not match to any race in the system.");
        }
    }

    @Override
    public int getNumberOfStages(int raceId) throws IDNotRecognisedException {
        if (doesRaceIdExist(raceId)) {
            int raceCount = raceObjects.size();
            for (int i = 0; i < raceCount; i++) {
                if (raceObjects.get(i).getId() == raceId) {
                    return raceObjects.get(i).getNumberOfStages();
                }
            }
            return 0;
        } else {
            throw new IDNotRecognisedException("The ID does not match to any race in the system.");
        }
    }

    @Override
    public int addStageToRace(int raceId, String stageName, String description, double length, LocalDateTime startTime,
            StageType type)
            throws IDNotRecognisedException, IllegalNameException, InvalidNameException, InvalidLengthException {
        if (!doesRaceIdExist(raceId)) {
            throw new IDNotRecognisedException("The ID does not match to any race in the system.");
        } else if (doesStageNameExist(raceId, stageName)) {
            throw new IllegalNameException("Stage name already exists in the system.");
        } else if (stageName == null || stageName == "" || stageName.length() > 30) {
            throw new InvalidNameException(
                    "Invalid stage name, must not be null, empty and not longer than 30 characters.");
        } else if (length < 5) {
            throw new InvalidLengthException("Stage length can not be less than 5km.");
        }
        int stageId = counterStates.getStageCounter();
        Stage stage = new Stage(stageId, raceId, stageName, description, length, startTime, type);
        addStageToRaceObject(raceId, stage);
        counterStates.incrementStageCounter();
        return stage.getId();
    }

    @Override
    public int[] getRaceStages(int raceId) throws IDNotRecognisedException {
        if (!doesRaceIdExist(raceId)) {
            throw new IDNotRecognisedException("Race id does not match to any race id in the system.");
        }
        ArrayList<Stage> stageObjects = getStages(raceId);
        int stageCount = stageObjects.size();
        int[] idArray = new int[stageCount];
        for (int i = 0; i < stageCount; i++) {
            idArray[i] = stageObjects.get(i).getId();
        }
        return idArray;
    }

    @Override
    public double getStageLength(int stageId) throws IDNotRecognisedException {
        if (!doesStageIdExist(stageId)) {
            throw new IDNotRecognisedException("Stage id does not match to any stage id in the system.");
        }
        int[] raceIds = getRaceIds();
        int raceCount = raceIds.length;
        for (int i = 0; i < raceCount; i++) {
            ArrayList<Stage> stageObjects = getStages(raceIds[i]);
            int stageCount = stageObjects.size();
            for (int j = 0; j < stageCount; j++) {
                if (stageObjects.get(j).getId() == stageId) {
                    return stageObjects.get(j).getLength();
                }
            }
        }
        return 1.0;
    }

    @Override
    public void removeStageById(int stageId) throws IDNotRecognisedException {
        if (!doesStageIdExist(stageId)) {
            throw new IDNotRecognisedException("Stage id does not match to any stage id in the system.");
        }
        int[] raceIds = getRaceIds();
        int raceCount = raceIds.length;
        for (int i = 0; i < raceCount; i++) {
            ArrayList<Stage> stageObjects = getStages(raceIds[i]);
            int stageCount = stageObjects.size();
            for (int j = 0; j < stageCount; j++) {
                if (stageObjects.get(j).getId() == stageId) {
                    int raceIndex = i;
                    int stageIndex = j;
                    removeStage(raceIndex, stageIndex);
                    break;
                }
            }
        }
    }

    @Override
    public int addCategorizedClimbToStage(int stageId, Double location, SegmentType type, Double averageGradient,
            Double length) throws IDNotRecognisedException, InvalidLocationException, InvalidStageStateException,
            InvalidStageTypeException {
        if (!doesStageIdExist(stageId)) {
            throw new IDNotRecognisedException("Stage ID does not match to any stage ID in the system.");
        } else if (location > getStageLength(stageId) || location < 0) {
            throw new InvalidLocationException("The segment location is out of bounds of the stage length.");
        } else if (getStageStateByStageId(stageId).equals("waiting for results")) {
            throw new InvalidStageStateException("The stage is currently `waiting for results`.");
        } else if (getStageType(stageId).equals(StageType.TT)) {
            throw new InvalidStageTypeException("Time trial stages cannot contain any segments.");
        }
        int segmentId = counterStates.getSegmentCounter();
        Segment segment = new Segment(segmentId, stageId, location, type, averageGradient, length);
        addSegmentToStage(stageId, segment);
        counterStates.incrementSegmentCounter();
        return segment.getId();
    }

    @Override
    public int addIntermediateSprintToStage(int stageId, double location) throws IDNotRecognisedException,
            InvalidLocationException, InvalidStageStateException, InvalidStageTypeException {
        if (!doesStageIdExist(stageId)) {
            throw new IDNotRecognisedException("Stage ID does not match to any stage ID in the system.");
        } else if (location > getStageLength(stageId) || location < 0) {
            throw new InvalidLocationException("The segment location is out of bounds of the stage length.");
        } else if (getStageStateByStageId(stageId).equals("waiting for results")) {
            throw new InvalidStageStateException("The stage is currently `waiting for results`.");
        } else if (getStageType(stageId).equals(StageType.TT)) {
            throw new InvalidStageTypeException("Time trial stages cannot contain any segments.");
        }
        int segmentId = counterStates.getSegmentCounter();
        Segment segment = new Segment(segmentId, stageId, location);
        addSegmentToStage(stageId, segment);
        counterStates.incrementSegmentCounter();
        return segment.getId();
    }

    @Override
    public void removeSegment(int segmentId) throws IDNotRecognisedException, InvalidStageStateException {
        if (!doesSegmentIdExist(segmentId)) {
            throw new IDNotRecognisedException("Segment ID was not found in the system.");
        } else if (getStageStateBySegmentId(segmentId).equals("waiting for results")) {
            throw new InvalidStageStateException("The stage is currently `waiting for results`.");
        }
        int[] raceIds = getRaceIds();
        int raceCount = raceIds.length;
        for (int i = 0; i < raceCount; i++) {
            ArrayList<Stage> stageObjects = getStages(raceIds[i]);
            int stageCount = stageObjects.size();
            for (int j = 0; j < stageCount; j++) {
                int[] segmentIds = stageObjects.get(j).getSegmentIds();
                for (int k = 0; k < segmentIds.length; k++) {
                    if (segmentIds[k] == segmentId) {
                        stageObjects.get(j).removeSegment(k);
                    }
                }
            }
        }
    }

    @Override
    public void concludeStagePreparation(int stageId) throws IDNotRecognisedException, InvalidStageStateException {
        if (!doesStageIdExist(stageId)) {
            throw new IDNotRecognisedException("Stage ID was not found in the system.");
        } else if (getStageStateByStageId(stageId).equals("waiting for results")) {
            throw new InvalidStageStateException("The stage is currently `waiting for results`.");
        }
        int[] raceIds = getRaceIds();
        int raceCount = raceIds.length;
        for (int i = 0; i < raceCount; i++) {
            ArrayList<Stage> stageObjects = getStages(raceIds[i]);
            int stageCount = stageObjects.size();
            for (int j = 0; j < stageCount; j++) {
                if (stageObjects.get(j).getId() == stageId) {
                    stageObjects.get(j).conclude();
                }
            }
        }
    }

    @Override
    public int[] getStageSegments(int stageId) throws IDNotRecognisedException {
        if (!doesStageIdExist(stageId)) {
            throw new IDNotRecognisedException("Stage ID was not found in the system.");
        }
        int[] raceIds = getRaceIds();
        int raceCount = raceIds.length;
        for (int i = 0; i < raceCount; i++) {
            ArrayList<Stage> stageObjects = getStages(raceIds[i]);
            int stageCount = stageObjects.size();
            for (int j = 0; j < stageCount; j++) {
                if (stageObjects.get(j).getId() == stageId) {
                    return stageObjects.get(j).getSegmentIds();
                }
            }
        }
        return new int[] {};
    }

    @Override
    public int createTeam(String name, String description) throws IllegalNameException, InvalidNameException {
        if (doesTeamNameExist(name)) {
            throw new IllegalNameException("Team name already exists.");
        } else if (name == null || name == "" || name.length() > 30) {
            throw new InvalidNameException("Invalid name, must not be null, empty and not longer than 30 characters.");
        } else {
            int teamId = counterStates.getTeamCounter();
            Team team = new Team(teamId, name, description);
            registerTeam(team);
            counterStates.incrementTeamCounter();
            return team.getId();
        }
    }

    @Override
    public void removeTeam(int teamId) throws IDNotRecognisedException {
        if (doesTeamIdExist(teamId)) {
            removeTeamById(teamId);
        } else {
            throw new IDNotRecognisedException("The ID does not match to any team in the system.");
        }
    }

    @Override
    public int[] getTeams() {
        int teamCount = teamObjects.size();
        int[] idArray = new int[teamCount];
        for (int i = 0; i < teamCount; i++) {
            idArray[i] = teamObjects.get(i).getId();
        }
        return idArray;
    }

    @Override
    public int[] getTeamRiders(int teamId) throws IDNotRecognisedException {
        if (!doesTeamIdExist(teamId)) {
            throw new IDNotRecognisedException("Team could not be found.");
        }
        int teamCount = teamObjects.size();
        for (int i = 0; i < teamCount; i++) {
            if (teamObjects.get(i).getId() == teamId) {
                Team foundTeam = teamObjects.get(i);

                int riderCount = foundTeam.getRiderCount();
                int[] idArray = new int[riderCount];
                for (int j = 0; j < riderCount; j++) {
                    idArray[j] = foundTeam.getRiderIdAtIndex(j);
                }
                return idArray;
            }
        }
        return new int[] {};
    }

    @Override
    public int createRider(int teamID, String name, int yearOfBirth)
            throws IDNotRecognisedException, IllegalArgumentException {
        if (!doesTeamIdExist(teamID)) {
            throw new IDNotRecognisedException("Team could not be found.");
        } else if (name == null || name == "" || yearOfBirth < 1900) {
            throw new IllegalArgumentException(
                    "Invalid name must not be null nor empty. Or invalid year of birth, must be greater than or equal to 1900.");
        } else {
            int riderId = counterStates.getRiderCounter();
            Rider rider = new Rider(riderId, teamID, name, yearOfBirth);
            addRiderToTeam(teamID, rider);
            counterStates.incrementRiderCounter();
            return rider.getId();
        }
    }

    @Override
    public void removeRider(int riderId) throws IDNotRecognisedException {
        if (!doesRiderIdExist(riderId)) {
            throw new IDNotRecognisedException("Rider ID does not match to any rider in the system.");
        }
        removeAllResultsForRider(riderId);
        ArrayList<Team> teamObjects = getTeamObjects();
        int teamCount = teamObjects.size();
        for (int i = 0; i < teamCount; i++) {
            if (teamObjects.get(i).doesRiderExist(riderId)) {
                teamObjects.get(i).removeRider(riderId);
            }
        }
    }

    @Override
    public void registerRiderResultsInStage(int stageId, int riderId, LocalTime... checkpoints)
            throws IDNotRecognisedException, DuplicatedResultException, InvalidCheckpointsException,
            InvalidStageStateException {
        if (!doesRiderIdExist(riderId)) {
            throw new IDNotRecognisedException("Rider ID does not match to any rider in the system.");
        } else if (!doesStageIdExist(stageId)) {
            throw new IDNotRecognisedException("Stage ID does not match to any stage in the system.");
        } else if (doesRiderHaveResult(stageId, riderId)) {
            throw new DuplicatedResultException("Rider already has a result in this stage.");
        } else if (getSegmentsCount(stageId) + 2 != checkpoints.length) {
            throw new InvalidCheckpointsException(
                    "The length of the checkpoints is invalid, must be equal to the number of segment + 2 (start and finish).");
        } else if (getStageStateByStageId(stageId).equals("waiting for results")) {
            throw new InvalidStageStateException("The stage is currently `waiting for results`.");
        } else {
            Result result = new Result(riderId, checkpoints);
            linkRiderResultsInStage(stageId, result);
        }
    }

    @Override
    public LocalTime[] getRiderResultsInStage(int stageId, int riderId) throws IDNotRecognisedException {
        if (!doesRiderIdExist(riderId)) {
            throw new IDNotRecognisedException("Rider ID does not match to any rider in the system.");
        } else if (!doesStageIdExist(stageId)) {
            throw new IDNotRecognisedException("Stage ID does not match to any stage in the system.");
        }
        int[] raceIds = getRaceIds();
        int raceCount = raceIds.length;
        for (int i = 0; i < raceCount; i++) {
            ArrayList<Stage> stageObjects = getStages(raceIds[i]);
            int stageCount = stageObjects.size();
            for (int j = 0; j < stageCount; j++) {
                if (stageObjects.get(j).getId() == stageId) {
                    return stageObjects.get(j).getRiderResultsInStage(riderId);
                }
            }
        }
        return new LocalTime[] {};
    }

    @Override
    public LocalTime getRiderAdjustedElapsedTimeInStage(int stageId, int riderId) throws IDNotRecognisedException {
        if (!doesRiderIdExist(riderId)) {
            throw new IDNotRecognisedException("Rider ID does not match to any rider in the system.");
        } else if (!doesStageIdExist(stageId)) {
            throw new IDNotRecognisedException("Stage ID does not match to any stage in the system.");
        }
        int[] raceIds = getRaceIds();
        int raceCount = raceIds.length;
        for (int i = 0; i < raceCount; i++) {
            ArrayList<Stage> stageObjects = getStages(raceIds[i]);
            int stageCount = stageObjects.size();
            for (int j = 0; j < stageCount; j++) {
                if (stageObjects.get(j).getId() == stageId) {
                    return stageObjects.get(j).getRiderAdjustedElapsedTimeInStage(riderId);
                }
            }
        }
        return null;
    }

    @Override
    public void deleteRiderResultsInStage(int stageId, int riderId) throws IDNotRecognisedException {
        if (!doesRiderIdExist(riderId)) {
            throw new IDNotRecognisedException("Rider ID does not match to any rider in the system.");
        } else if (!doesStageIdExist(stageId)) {
            throw new IDNotRecognisedException("Stage ID does not match to any stage in the system.");
        }
        int[] raceIds = getRaceIds();
        int raceCount = raceIds.length;
        for (int i = 0; i < raceCount; i++) {
            ArrayList<Stage> stageObjects = getStages(raceIds[i]);
            int stageCount = stageObjects.size();
            for (int j = 0; j < stageCount; j++) {
                if (stageObjects.get(j).getId() == stageId) {
                    stageObjects.get(j).removeAllRiderResults(riderId);
                }
            }
        }
    }

    @Override
    public int[] getRidersRankInStage(int stageId) throws IDNotRecognisedException {
        if (!doesStageIdExist(stageId)) {
            throw new IDNotRecognisedException("Stage ID does not match to any stage in the system.");
        }
        if (getStageResultsCount(stageId) > 0) {
            int[] raceIds = getRaceIds();
            int raceCount = raceIds.length;
            for (int i = 0; i < raceCount; i++) {
                ArrayList<Stage> stageObjects = getStages(raceIds[i]);
                int stageCount = stageObjects.size();
                for (int j = 0; j < stageCount; j++) {
                    if (stageObjects.get(j).getId() == stageId) {
                        return stageObjects.get(j).getRidersRankInStage();
                    }
                }
            }
            return new int[] {};
        }
        return new int[] {};
    }

    @Override
    public LocalTime[] getRankedAdjustedElapsedTimesInStage(int stageId) throws IDNotRecognisedException {
        if (!doesStageIdExist(stageId)) {
            throw new IDNotRecognisedException("Stage ID does not match to any stage in the system.");
        }
        int[] raceIds = getRaceIds();
        int raceCount = raceIds.length;
        for (int i = 0; i < raceCount; i++) {
            ArrayList<Stage> stageObjects = getStages(raceIds[i]);
            int stageCount = stageObjects.size();
            for (int j = 0; j < stageCount; j++) {
                if (stageObjects.get(j).getId() == stageId) {
                    return stageObjects.get(j).getRankedAdjustedElapsedTimesInStage();
                }
            }
        }
        return new LocalTime[] {};
    }

    @Override
    public int[] getRidersPointsInStage(int stageId) throws IDNotRecognisedException {
        if (!doesStageIdExist(stageId)) {
            throw new IDNotRecognisedException("Stage ID does not match to any stage in the system.");
        }
        if (getStageResultsCount(stageId) > 0) {
            int[] raceIds = getRaceIds();
            int raceCount = raceIds.length;
            for (int i = 0; i < raceCount; i++) {
                ArrayList<Stage> stageObjects = getStages(raceIds[i]);
                int stageCount = stageObjects.size();
                for (int j = 0; j < stageCount; j++) {
                    if (stageObjects.get(j).getId() == stageId) {
                        return stageObjects.get(j).getRidersPointsInStage();
                    }
                }
            }
            return new int[] {};
        }
        return new int[] {};
    }

    @Override
    public int[] getRidersMountainPointsInStage(int stageId) throws IDNotRecognisedException {
        if (!doesStageIdExist(stageId)) {
            throw new IDNotRecognisedException("Stage ID does not match to any stage in the system.");
        }
        if (getStageResultsCount(stageId) > 0) {
            int[] raceIds = getRaceIds();
            int raceCount = raceIds.length;
            for (int i = 0; i < raceCount; i++) {
                ArrayList<Stage> stageObjects = getStages(raceIds[i]);
                int stageCount = stageObjects.size();
                for (int j = 0; j < stageCount; j++) {
                    if (stageObjects.get(j).getId() == stageId) {
                        return stageObjects.get(j).getRidersMountainPointsInStage();
                    }
                }
            }
            return new int[] {};
        }
        return new int[] {};
    }

    @Override
    public void eraseCyclingPortal() {
        raceObjects = new ArrayList<Race>();
        teamObjects = new ArrayList<Team>();
        counterStates.resetAllCounts();
    }

    @Override
    public void saveCyclingPortal(String filename) throws IOException {
        FileOutputStream file = new FileOutputStream(filename);
        ObjectOutputStream out = new ObjectOutputStream(file);
        out.writeObject(teamObjects);
        out.writeObject(raceObjects);
        out.writeObject(counterStates);
        out.close();
        file.close();
    }

    @Override
    public void loadCyclingPortal(String filename) throws IOException, ClassNotFoundException {
        FileInputStream file = new FileInputStream(filename);
        ObjectInputStream in = new ObjectInputStream(file);
        teamObjects = (ArrayList<Team>) in.readObject();
        raceObjects = (ArrayList<Race>) in.readObject();
        counterStates = (CounterStates) in.readObject();
        in.close();
        file.close();
    }

    @Override
    public void removeRaceByName(String name) throws NameNotRecognisedException {
        if (doesRaceNameExist(name)) {
            int raceCount = raceObjects.size();
            for (int i = 0; i < raceCount; i++) {
                if (raceObjects.get(i).getName().equals(name)) {
                    raceObjects.remove(i);
                    break;
                }
            }
        } else {
            throw new NameNotRecognisedException("The name does not match to any race in the system.");
        }
    }

    @Override
    public LocalTime[] getGeneralClassificationTimesInRace(int raceId) throws IDNotRecognisedException {
        if (!doesRaceIdExist(raceId)) {
            throw new IDNotRecognisedException("Race ID was not found in the system.");
        }
        int raceCount = raceObjects.size();
        for (int i = 0; i < raceCount; i++) {
            if (raceObjects.get(i).getId() == raceId) {
                return raceObjects.get(i).getGeneralClassificationTimesInRace();
            }
        }
        return new LocalTime[] {};
    }

    @Override
    public int[] getRidersPointsInRace(int raceId) throws IDNotRecognisedException {
        if (!doesRaceIdExist(raceId)) {
            throw new IDNotRecognisedException("Race ID was not found in the system.");
        }
        // Finds the relevant race object and gets the rider points in the race.
        int raceCount = raceObjects.size();
        for (int i = 0; i < raceCount; i++) {
            if (raceObjects.get(i).getId() == raceId) {
                return raceObjects.get(i).getRidersPointsInRace();
            }
        }
        return new int[] {};
    }

    @Override
    public int[] getRidersMountainPointsInRace(int raceId) throws IDNotRecognisedException {
        if (!doesRaceIdExist(raceId)) {
            throw new IDNotRecognisedException("Race ID was not found in the system.");
        }
        int raceCount = raceObjects.size();
        for (int i = 0; i < raceCount; i++) {
            if (raceObjects.get(i).getId() == raceId) {
                return raceObjects.get(i).getRidersMountainPointsInRace();
            }
        }
        return new int[] {};
    }

    @Override
    public int[] getRidersGeneralClassificationRank(int raceId) throws IDNotRecognisedException {
        if (!doesRaceIdExist(raceId)) {
            throw new IDNotRecognisedException("Race ID was not found in the system.");
        }
        int raceCount = raceObjects.size();
        for (int i = 0; i < raceCount; i++) {
            if (raceObjects.get(i).getId() == raceId) {
                return raceObjects.get(i).getRidersGeneralClassificationRank();
            }
        }
        return new int[] {};
    }

    @Override
    public int[] getRidersPointClassificationRank(int raceId) throws IDNotRecognisedException {
        if (!doesRaceIdExist(raceId)) {
            throw new IDNotRecognisedException("Race ID was not found in the system.");
        }
        int raceCount = raceObjects.size();
        for (int i = 0; i < raceCount; i++) {
            if (raceObjects.get(i).getId() == raceId) {
                return raceObjects.get(i).getRidersPointClassificationRank();
            }
        }
        return new int[] {};
    }

    @Override
    public int[] getRidersMountainPointClassificationRank(int raceId) throws IDNotRecognisedException {
        if (!doesRaceIdExist(raceId)) {
            throw new IDNotRecognisedException("Race ID was not found in the system.");
        }
        int raceCount = raceObjects.size();
        for (int i = 0; i < raceCount; i++) {
            if (raceObjects.get(i).getId() == raceId) {
                return raceObjects.get(i).getRidersMountainPointClassificationRank();
            }
        }
        return new int[] {};
    }

    // Segment Handler Functions
    /**
     * adds a segment to the stage
     * 
     * @param stageId the Id of the stage
     * @param segment the segment object
     */
    public void addSegmentToStage(int stageId, Segment segment) {
        int[] raceIds = getRaceIds();
        int raceCount = raceIds.length;
        for (int i = 0; i < raceCount; i++) {
            ArrayList<Stage> stageObjects = getStages(raceIds[i]);
            int stageCount = stageObjects.size();
            for (int j = 0; j < stageCount; j++) {
                if (stageObjects.get(j).getId() == stageId) {
                    stageObjects.get(j).addSegment(segment);
                }
            }
        }
    }

    /**
     * gets the current state of the stage by the segment Id
     * 
     * @param segmentId the Id of the segment
     * @return whether the stage is waiting for results or not
     */
    public String getStageStateBySegmentId(int segmentId) {
        int[] raceIds = getRaceIds();
        int raceCount = raceIds.length;
        for (int i = 0; i < raceCount; i++) {
            ArrayList<Stage> stageObjects = getStages(raceIds[i]);
            int stageCount = stageObjects.size();
            for (int j = 0; j < stageCount; j++) {
                int[] segmentIds = stageObjects.get(j).getSegmentIds();
                for (int k = 0; k < segmentIds.length; k++) {
                    if (segmentIds[k] == segmentId) {
                        return stageObjects.get(j).getState();
                    }
                }
            }
        }
        return "";
    }

    /**
     * checks if the segment Id exsits
     * 
     * @param segmentId the Id of the segment
     * @return true if the Id exists, false if it doesn't
     */
    public boolean doesSegmentIdExist(int segmentId) {
        int[] raceIds = getRaceIds();
        int raceCount = raceIds.length;
        for (int i = 0; i < raceCount; i++) {
            ArrayList<Stage> stageObjects = getStages(raceIds[i]);
            int stageCount = stageObjects.size();
            for (int j = 0; j < stageCount; j++) {
                int[] segmentIds = stageObjects.get(j).getSegmentIds();
                for (int k = 0; k < segmentIds.length; k++) {
                    if (segmentIds[k] == segmentId) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Rider Handler Functions
    /**
     * checks if the rider Id exists
     * 
     * @param riderId the Id of he rider
     * @return true if the riderId exists, false if it doesn't
     */
    public boolean doesRiderIdExist(int riderId) {
        ArrayList<Team> teamObjects = getTeamObjects();
        int teamCount = teamObjects.size();
        for (int i = 0; i < teamCount; i++) {
            if (teamObjects.get(i).doesRiderExist(riderId)) {
                return true;
            }
        }
        return false;
    }

    // Team Handler Functions
    /**
     * registers a Team
     * 
     * @param team team object
     */
    public void registerTeam(Team team) {
        teamObjects.add(team);
    }

    /**
     * adds a rider to the team
     * 
     * @param teamId the Id of the team
     * @param rider the rider Object to be added
     */
    public void addRiderToTeam(int teamId, Rider rider) {
        int teamCount = teamObjects.size();
        for (int i = 0; i < teamCount; i++) {
            if (teamObjects.get(i).getId() == teamId) {
                teamObjects.get(i).addRider(rider);
            }
        }
    }

    /**
     * checks if a team name exists
     * 
     * @param nameSearch Team's name
     * @return true if the team's name exists, false if it doesn't
     */
    public boolean doesTeamNameExist(String nameSearch) {
        int teamCount = teamObjects.size();
        for (int i = 0; i < teamCount; i++) {
            if (teamObjects.get(i).getName().equals(nameSearch)) {
                return true;
            }
        }
        return false;
    }

    /**
     * checks if the team Id exists
     * 
     * @param idSearch the Id of the team
     * @return True if the team Id exists, false if it doesn't
     */
    public boolean doesTeamIdExist(int idSearch) {
        int teamCount = teamObjects.size();
        for (int i = 0; i < teamCount; i++) {
            if (teamObjects.get(i).getId() == idSearch) {
                return true;
            }
        }
        return false;
    }

    /**
     * removes a team from the system and
     * the riders associated with the team
     * alongside their results
     * 
     * @param teamId the id of the team
     */
    public void removeTeamById(int teamId) {
        int teamCount = teamObjects.size();
        for (int i = 0; i < teamCount; i++) {
            if (teamObjects.get(i).getId() == teamId) {
                Team teamTBD = teamObjects.get(i);
                int riderCount = teamTBD.getRiderCount();
                for (int j = 0; j < riderCount; j++) {
                    int riderId = teamTBD.getRiderIdAtIndex(j);
                    removeAllResultsForRider(riderId);
                }
                teamObjects.remove(i);
                break;
            }
        }
    }

    /**
     * gets the teamObjects
     * 
     * @return Arraylist of teamObjects
     */
    public ArrayList<Team> getTeamObjects() {
        return teamObjects;
    }

    /**
     * remove the rider's results from the system
     * 
     * @param riderId the id of the rider
     */
    public void removeAllResultsForRider(int riderId) {
        int[] raceIds = getRaceIds();
        int raceCount = raceIds.length;
        for (int i = 0; i < raceCount; i++) {
            ArrayList<Stage> stageObjects = getStages(raceIds[i]);
            int stageCount = stageObjects.size();
            for (int j = 0; j < stageCount; j++) {
                stageObjects.get(j).removeAllRiderResults(riderId);
            }
        }
    }

    // Stage Handler Functions
    /**
     * checks if the stage name exists
     * 
     * @param raceId the Id of the race
     * @param name   the name of the stage
     * @return true if the stage name exists, false if it doesn't
     */
    public boolean doesStageNameExist(int raceId, String name) {
        ArrayList<Stage> stageObjects = getStages(raceId);
        int stageCount = stageObjects.size();
        for (int i = 0; i < stageCount; i++) {
            if (stageObjects.get(i).getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * gets the current state of the stage by the stage Id
     * 
     * @param stageId the Id of the stage
     * @return whether the stage is waiting for results or not
     */
    public String getStageStateByStageId(int stageId) {
        int[] raceIds = getRaceIds();
        int raceCount = raceIds.length;
        for (int i = 0; i < raceCount; i++) {
            ArrayList<Stage> stageObjects = getStages(raceIds[i]);
            int stageCount = stageObjects.size();
            for (int j = 0; j < stageCount; j++) {
                if (stageObjects.get(j).getId() == stageId) {
                    return stageObjects.get(j).getState();
                }
            }
        }
        return "";
    }

    /**
     * gets the type of the stage
     * 
     * @param stageId the Id of the stage
     * @return the type of the stage
     */
    public StageType getStageType(int stageId) {
        int[] raceIds = getRaceIds();
        int raceCount = raceIds.length;
        for (int i = 0; i < raceCount; i++) {
            ArrayList<Stage> stageObjects = getStages(raceIds[i]);
            int stageCount = stageObjects.size();
            for (int j = 0; j < stageCount; j++) {
                if (stageObjects.get(j).getId() == stageId) {
                    return stageObjects.get(j).getType();
                }
            }
        }
        return null;
    }

    /**
     * checks if the Id of the stage exists
     * 
     * @param stageId the Id of the stage
     * @return true if the stage Id exists, false if it doesn't
     */
    public boolean doesStageIdExist(int stageId) {
        int[] raceIds = getRaceIds();
        int raceCount = raceIds.length;
        for (int i = 0; i < raceCount; i++) {
            ArrayList<Stage> stageObjects = getStages(raceIds[i]);
            int stageCount = stageObjects.size();
            for (int j = 0; j < stageCount; j++) {
                if (stageObjects.get(j).getId() == stageId) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * checks if the rider has a result
     * 
     * @param stageId the Id of the Stage
     * @param riderId the Id of the Rider
     * @return True if the rider has a result, false if he doesn't
     */
    public boolean doesRiderHaveResult(int stageId, int riderId) {
        int[] raceIds = getRaceIds();
        int raceCount = raceIds.length;
        for (int i = 0; i < raceCount; i++) {
            ArrayList<Stage> stageObjects = getStages(raceIds[i]);
            int stageCount = stageObjects.size();
            for (int j = 0; j < stageCount; j++) {
                if (stageObjects.get(j).getId() == stageId) {
                    return stageObjects.get(j).doesRiderHaveResult(riderId);
                }
            }
        }
        return false;
    }

    /**
     * gets the number of segments for a specific stage
     * 
     * @param stageId the Id of the stage
     * @return the number of segments in the stage
     */
    public int getSegmentsCount(int stageId) {
        int[] raceIds = getRaceIds();
        int raceCount = raceIds.length;
        for (int i = 0; i < raceCount; i++) {
            ArrayList<Stage> stageObjects = getStages(raceIds[i]);
            int stageCount = stageObjects.size();
            for (int j = 0; j < stageCount; j++) {
                if (stageObjects.get(j).getId() == stageId) {
                    return stageObjects.get(j).getSegmentsCount();
                }
            }
        }
        return 0;
    }

    /**
     * adds results to stages
     * 
     * @param stageId stage's Id
     * @param result  result Object
     */
    public void linkRiderResultsInStage(int stageId, Result result) {
        int[] raceIds = getRaceIds();
        int raceCount = raceIds.length;
        for (int i = 0; i < raceCount; i++) {
            ArrayList<Stage> stageObjects = getStages(raceIds[i]);
            int stageCount = stageObjects.size();
            for (int j = 0; j < stageCount; j++) {
                if (stageObjects.get(j).getId() == stageId) {
                    stageObjects.get(j).addResult(result);
                }
            }
        }
    }

    /**
     * gets the number of results in a stage
     * 
     * @param stageId the stage's Id
     * @return the number of results in the stage
     */
    public int getStageResultsCount(int stageId) {
        int[] raceIds = getRaceIds();
        int raceCount = raceIds.length;
        for (int i = 0; i < raceCount; i++) {
            ArrayList<Stage> stageObjects = getStages(raceIds[i]);
            int stageCount = stageObjects.size();
            for (int j = 0; j < stageCount; j++) {
                if (stageObjects.get(j).getId() == stageId) {
                    return stageObjects.get(j).getResultsCount();
                }
            }
        }
        return 0;
    }

    // Race Handler Functions
    /**
     * registers a race
     * 
     * @param race the race object
     */
    public void registerRace(Race race) {
        raceObjects.add(race);
    }

    /**
     * checks if the race name exists
     * 
     * @param nameSearch the name of the race
     * @return true if the race name exists, false if it doesn't
     */
    public boolean doesRaceNameExist(String nameSearch) {
        int raceCount = raceObjects.size();
        for (int i = 0; i < raceCount; i++) {
            if (raceObjects.get(i).getName().equals(nameSearch)) {
                return true;
            }
        }
        return false;
    }

    /**
     * checks if the Race Id exsits
     * 
     * @param idSearch the Race's Id
     * @return true if the race id exists, false if it doesn't
     */
    public boolean doesRaceIdExist(int idSearch) {
        int raceCount = raceObjects.size();
        for (int i = 0; i < raceCount; i++) {
            if (raceObjects.get(i).getId() == idSearch) {
                return true;
            }
        }
        return false;
    }

    /**
     * adds a stage to a race
     * 
     * @param raceId race's Id
     * @param stage  stage's Id
     */
    public void addStageToRaceObject(int raceId, Stage stage) {
        int raceCount = raceObjects.size();
        for (int i = 0; i < raceCount; i++) {
            if (raceObjects.get(i).getId() == raceId) {
                raceObjects.get(i).addStage(stage);
                break;
            }
        }
    }

    /**
     * gets the stages in a race
     * 
     * @param raceId race's Id
     * @return a list of the stages in the race
     */
    public ArrayList<Stage> getStages(int raceId) {
        int raceCount = raceObjects.size();
        for (int i = 0; i < raceCount; i++) {
            if (raceObjects.get(i).getId() == raceId) {
                return raceObjects.get(i).getStages();
            }
        }
        return new ArrayList<Stage>();
    }

    /**
     * removes a stage from a race
     * 
     * @param raceIndex  race object's position
     * @param stageIndex stage object's position
     */
    public void removeStage(int raceIndex, int stageIndex) {
        raceObjects.get(raceIndex).removeStage(stageIndex);
    }
}
