package sergii.makarenko.domain;

import org.springframework.stereotype.Component;

/**
 * Entity for process changing
 *
 * @author Serg
 */
@Component
public class ProcessDifference {

    private String processName;
    private long memoryNew;
    private long memoryOld;
    private long memoryDifference;

    public ProcessDifference() {
    }

    public ProcessDifference(String processName, long memoryNew, long memoryOld, long memoryDifference) {
        this.processName = processName;
        this.memoryNew = memoryNew;
        this.memoryOld = memoryOld;
        this.memoryDifference = memoryDifference;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public long getMemoryNew() {
        return memoryNew;
    }

    public void setMemoryNew(long memoryNew) {
        this.memoryNew = memoryNew;
    }

    public long getMemoryOld() {
        return memoryOld;
    }

    public void setMemoryOld(long memoryOld) {
        this.memoryOld = memoryOld;
    }

    public long getMemoryDifference() {
        return memoryDifference;
    }

    public void setMemoryDifference(long memoryDifference) {
        this.memoryDifference = memoryDifference;
    }
}
