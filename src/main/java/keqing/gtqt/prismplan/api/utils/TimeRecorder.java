package keqing.gtqt.prismplan.api.utils;


import java.util.ArrayDeque;
import java.util.Deque;

public class TimeRecorder {
    private final Deque<Integer> usedTimeList = new ArrayDeque(102);
    private final Deque<Integer> searchUsedTimeList = new ArrayDeque(22);
    private int usedTimeCache = 0;
    private int searchUsedTimeCache = 0;

    public TimeRecorder() {
    }

    public void updateUsedTime(ActionExecutor executor) {
        this.addUsedTime(executor == null ? 0 : executor.usedTime);
    }

    public void incrementUsedTime(int add) {
        this.usedTimeCache += add;
        Integer first = (Integer)this.usedTimeList.getFirst();
        if (first != null) {
            this.usedTimeList.removeFirst();
            this.usedTimeList.addFirst(first + add);
        } else {
            this.usedTimeList.addFirst(add);
        }

    }

    public void addUsedTime(int time) {
        this.usedTimeCache += time;
        this.usedTimeList.addFirst(time);
        if (this.usedTimeList.size() > 100) {
            this.usedTimeCache -= (Integer)this.usedTimeList.pollLast();
        }

    }

    public void addRecipeResearchUsedTime(int time) {
        this.searchUsedTimeCache += time;
        this.searchUsedTimeList.addFirst(time);
        if (this.searchUsedTimeList.size() > 20) {
            this.searchUsedTimeCache -= (Integer)this.searchUsedTimeList.pollLast();
        }

    }

    public void incrementRecipeResearchUsedTime(int add) {
        this.searchUsedTimeCache += add;
        Integer first = (Integer)this.searchUsedTimeList.getFirst();
        if (first != null) {
            this.searchUsedTimeList.removeFirst();
            this.searchUsedTimeList.addFirst(first + add);
        } else {
            this.searchUsedTimeList.addFirst(add);
        }

    }

    public int usedTimeAvg() {
        return this.usedTimeList.isEmpty() ? 0 : this.usedTimeCache / this.usedTimeList.size();
    }

    public int recipeSearchUsedTimeAvg() {
        return this.searchUsedTimeList.isEmpty() ? 0 : this.searchUsedTimeCache / this.searchUsedTimeList.size();
    }
}
