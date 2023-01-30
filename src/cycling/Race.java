package cycling;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Race- A class which stores information about races
 * and handles some of the operations related to races.
 */
public class Race implements Serializable {
    private int id;
    private String name;
    private String description;
    private ArrayList<Stage> stageObjects = new ArrayList<Stage>();

    /**
     * Constructor for the Objects of Race class
     * 
     * @param id          the Id of the race
     * @param name        the name of the race
     * @param description the description of the race
     */
    public Race(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addStage(Stage stage) {
        stageObjects.add(stage);
    }

    public int getNumberOfStages() {
        return stageObjects.size();
    }

    /**
     * calculates the total length of all stages in a race
     * 
     * @return the total length in kms
     */
    public double getTotalLength() {
        int stage_count = stageObjects.size();
        double totalLength = 0;
        for (int i = 0; i < stage_count; i++) {
            totalLength += stageObjects.get(i).getLength();
        }
        return totalLength;
    }

    public ArrayList<Stage> getStages() {
        return stageObjects;
    }

    /**
     * Removes a stage from the race
     * 
     * @param stageIndex position of the stage in the arraylist
     */
    public void removeStage(int stageIndex) {
        stageObjects.remove(stageIndex);
    }

    public int[] getRidersGeneralClassificationRank() {
        /*
         * A loop to check if the stage has results
         * if not an empty array is returned
         */
        int stage_count = stageObjects.size();
        for (int j = 0; j < stage_count; j++) {
            if (stageObjects.get(j).getResultsCount() == 0) {
                return new int[] {};
            }
        }

        // A sample of the results of a stage are stored in an arraylist
        ArrayList<Result> sampleResultObjects = stageObjects.get(0).getResultObjects();
        int result_count = sampleResultObjects.size();

        /*
         * A Hashmap to store the riderId along with its corresponding
         * score is created and is initialized by storing the riderIds
         * from the array of the sample results
         */
        HashMap<Integer, LocalTime> riderHashMap = new HashMap<Integer, LocalTime>();
        for (int i = 0; i < result_count; i++) {
            riderHashMap.put(sampleResultObjects.get(i).getRiderId(), LocalTime.ofNanoOfDay(0));
        }

        LocalTime currentAdjElapsedTime;
        int rider_count = riderHashMap.size();
        /*
         * the key part of the hashmap, which consists of the riderIds
         * is converted to an arrray which will store the ridersIds
         */
        int[] rider_ids = riderHashMap.keySet().stream().mapToInt(Number::intValue).toArray();

        /*
         * A loop to check if the rider has taken part in all of the stages
         * of a specific race, if not his riderId is removed from the Hashmap
         * as it will provide an unfair advantage
         */
        for (int j = 0; j < stage_count; j++) {
            for (int i = 0; i < rider_count; i++) {
                if (!stageObjects.get(j).doesRiderHaveResult(rider_ids[i])) {
                    riderHashMap.remove(rider_ids[i]);
                    rider_count = riderHashMap.size();
                    rider_ids = riderHashMap.keySet().stream().mapToInt(Number::intValue).toArray();
                }
            }
        }

        for (int i = 0; i < rider_count; i++) {
            /*
             * a loop that loops for the number of stages in a race
             * it gets current elapsed time of a rider by the function
             * getRiderAdjustedElapsedTimeInStage and updates the Hashmap
             * after adding it to the previous sum of elapsed times
             */
            for (int j = 0; j < stage_count; j++) {
                currentAdjElapsedTime = stageObjects.get(j).getRiderAdjustedElapsedTimeInStage(rider_ids[i]);
                LocalTime elapsedTimeSum = riderHashMap.get(rider_ids[i]);
                riderHashMap.replace(rider_ids[i],
                        elapsedTimeSum.plus(currentAdjElapsedTime.toNanoOfDay(), ChronoUnit.NANOS));
            }
        }

        ArrayList<Result> resultsArray = new ArrayList<Result>();
        /*
         * A loop that creates a result object for each rider to store
         * the riderID along with the result, then each object is
         * stored in an Arraylist named resultsArray
         */
        for (int i = 0; i < rider_count; i++) {
            Result result = new Result(rider_ids[i], riderHashMap.get(rider_ids[i]));
            resultsArray.add(result);
        }

        // the resultsArray is sorted by the elapsed time
        Collections.sort(resultsArray);

        int[] finalIdsArray = new int[resultsArray.size()];
        /*
         * A loop that takes the riderId part of the Hashmap after it
         * has been sorted by elapsed time to store it an Array named
         * finalIds which is returned later
         */
        for (int i = 0; i < resultsArray.size(); i++) {
            finalIdsArray[i] = resultsArray.get(i).getRiderId();
        }
        return finalIdsArray;
    }

    public LocalTime[] getGeneralClassificationTimesInRace() {

        int stage_count = stageObjects.size();
        /*
         * A loop to check that the each Stage of the Race has results
         * otherwise an empty array of localtime is returned
         */
        for (int j = 0; j < stage_count; j++) {
            if (stageObjects.get(j).getResultsCount() == 0) {
                return new LocalTime[] {};
            }
        }

        ArrayList<Result> sampleResultObjects = stageObjects.get(0).getResultObjects();
        int result_count = sampleResultObjects.size();

        HashMap<Integer, LocalTime> riderHashMap = new HashMap<Integer, LocalTime>();
        // the riderHashmap is initialized by using the riderIds of a specific stage
        for (int i = 0; i < result_count; i++) {
            riderHashMap.put(sampleResultObjects.get(i).getRiderId(), LocalTime.ofNanoOfDay(0));
        }

        LocalTime currentAdjElapsedTime;
        int rider_count = riderHashMap.size();
        int[] rider_ids = riderHashMap.keySet().stream().mapToInt(Number::intValue).toArray();

        /*
         * A loop to check if the rider has participated in all stages of
         * the race, if not the riderId is removed from the Hashmap
         */
        for (int j = 0; j < stage_count; j++) {
            for (int i = 0; i < rider_count; i++) {
                if (!stageObjects.get(j).doesRiderHaveResult(rider_ids[i])) {
                    riderHashMap.remove(rider_ids[i]);
                    rider_count = riderHashMap.size();
                    rider_ids = riderHashMap.keySet().stream().mapToInt(Number::intValue).toArray();
                }
            }
        }

        /*
         * A loop to update the Elapsed time part of the Hashmap after
         * going through all the results of the rider in all stages
         */
        for (int i = 0; i < rider_count; i++) {
            for (int j = 0; j < stage_count; j++) {
                currentAdjElapsedTime = stageObjects.get(j).getRiderAdjustedElapsedTimeInStage(rider_ids[i]);
                LocalTime elapsedTimeSum = riderHashMap.get(rider_ids[i]);
                riderHashMap.replace(rider_ids[i],
                        elapsedTimeSum.plus(currentAdjElapsedTime.toNanoOfDay(), ChronoUnit.NANOS));
            }
        }

        ArrayList<Result> resultsArray = new ArrayList<Result>();
        /*
         * using a loop to make a result object for each rider along with the
         * elapsed time which is stored in an array named resultsArray
         */
        for (int i = 0; i < rider_count; i++) {
            Result result = new Result(rider_ids[i], riderHashMap.get(rider_ids[i]));
            resultsArray.add(result);
        }

        // resultsArray is sorted by Elapsed time
        Collections.sort(resultsArray);

        LocalTime[] finalElapsedTimesArray = new LocalTime[resultsArray.size()];
        /*
         * after the result Array has been sorted, the elapsed time
         * of each rider is stored in an array named finalElapsedTimesArray
         * by the following loop
         */
        for (int i = 0; i < resultsArray.size(); i++) {
            finalElapsedTimesArray[i] = resultsArray.get(i).getCheckPoint(0);
        }

        return finalElapsedTimesArray;
    }

    public int[] getRidersPointsInRace() {
        /*
         * a list of rider Ids sorted by the sum of elapsed time
         * will be stored in an array named riderIds
         */
        int[] riderIds = getRidersGeneralClassificationRank();
        // If there are no riderIds an empty array will be returned
        if (riderIds.length == 0) {
            return new int[] {};
        }
        // the riderHashmap is initialized
        HashMap<Integer, Integer> riderHashMap = new HashMap<Integer, Integer>();
        for (int i = 0; i < riderIds.length; i++) {
            riderHashMap.put(riderIds[i], 0);
        }
        int stage_count = stageObjects.size();
        /*
         * a loop that loops for the number of stages in the race to make sure that
         * the points of all stages are added up
         */
        for (int i = 0; i < stage_count; i++) {
            /*
             * two arrrays to store the riderIds and their corresponding points
             * sorted by their elapsed time
             */
            int[] stageRiderIds = stageObjects.get(i).getRidersRankInStage();
            int[] stageRiderPoints = stageObjects.get(i).getRidersPointsInStage();
            // a loop which will update the hashmap with the points for each rider
            for (int j = 0; j < stageRiderIds.length; j++) {
                if (riderHashMap.containsKey(stageRiderIds[j])) {
                    int previousPoints = riderHashMap.get(stageRiderIds[j]);
                    riderHashMap.replace(stageRiderIds[j], previousPoints + stageRiderPoints[j]);
                }
            }
        }
        int[] finalPointsArray = new int[riderIds.length];
        /*
         * a loop to store the points part of the hashmap into an array
         * named finalpointsArray
         */
        for (int i = 0; i < riderIds.length; i++) {
            finalPointsArray[i] = riderHashMap.get(riderIds[i]);
        }
        return finalPointsArray;
    }

    public int[] getRidersMountainPointsInRace() {
        /*
         * the function getRidersGenenralClassificationRank will return a list of
         * riders' IDs sorted ascending by the sum of their adjusted elasped times
         * which will be stored in an array named riderIds
         */
        int[] riderIds = getRidersGeneralClassificationRank();
        // if the array has no rider Ids an empty array will be returned
        if (riderIds.length == 0) {
            return new int[] {};
        }
        // A hashmap with the riderIds and their respective point is initialized
        HashMap<Integer, Integer> riderHashMap = new HashMap<Integer, Integer>();
        for (int i = 0; i < riderIds.length; i++) {
            riderHashMap.put(riderIds[i], 0);
        }
        int stage_count = stageObjects.size();

        for (int i = 0; i < stage_count; i++) {
            /*
             * the riderIds and their points are retrieved by calling the functions
             * getRidersRankInstage and getRidersMountainPointsInStage and are
             * stored in two separate arrays
             */
            int[] stageRiderIds = stageObjects.get(i).getRidersRankInStage();
            int[] stageRiderPoints = stageObjects.get(i).getRidersMountainPointsInStage();
            // the following loop for the number of riders in that stage
            // and will update the hasmap with the points of each rider
            for (int j = 0; j < stageRiderIds.length; j++) {
                if (riderHashMap.containsKey(stageRiderIds[j])) {
                    int previousPoints = riderHashMap.get(stageRiderIds[j]);
                    riderHashMap.replace(stageRiderIds[j], previousPoints + stageRiderPoints[j]);
                }
            }
        }

        int[] finalPointsArray = new int[riderIds.length];
        /*
         * the values part of the hashmap which contains the points of each
         * rider is called and its values are stored in an array
         * named finalPointsArray which is returned later
         */
        for (int i = 0; i < riderIds.length; i++) {
            finalPointsArray[i] = riderHashMap.get(riderIds[i]);
        }
        return finalPointsArray;
    }

    public int[] getRidersPointClassificationRank() {
        /*
         * the riderIds and their points are retrieved by calling the functions
         * getRidersGeneralClassificationRank and getRidersPointsInRace and are
         * stored in two separate arrays
         */
        int[] riderIds = getRidersGeneralClassificationRank();
        int[] riderPoints = getRidersPointsInRace();

        // the bubblesort function is callled to sort by the riderPoints
        bubbleSort(riderPoints, riderIds);

        return riderIds;
    }

    public int[] getRidersMountainPointClassificationRank() {
        /*
         * the riderIds and their points are retrieved by calling the functions
         * getRidersGeneralClassificationRank and getRidersMountainPointsInRace and are
         * stored in two separate arrays
         */
        int[] riderIds = getRidersGeneralClassificationRank();
        int[] riderPoints = getRidersMountainPointsInRace();

        // the bubblesort function is called to sort by the riderPoints
        bubbleSort(riderPoints, riderIds);

        return riderIds;
    }

    /**
     * An algorithm for sorting by riderPoints
     */
    public void bubbleSort(int[] pointsArray, int[] idsArray) {
        boolean sorted = false;
        int temp;
        while (!sorted) {
            sorted = true;
            for (int i = 0; i < pointsArray.length - 1; i++) {
                if (pointsArray[i] < pointsArray[i + 1]) {

                    temp = pointsArray[i];
                    pointsArray[i] = pointsArray[i + 1];
                    pointsArray[i + 1] = temp;

                    temp = idsArray[i];
                    idsArray[i] = idsArray[i + 1];
                    idsArray[i + 1] = temp;

                    sorted = false;
                }
            }
        }
    }

}
