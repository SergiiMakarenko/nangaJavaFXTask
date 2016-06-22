package sergii.makarenko.domain;

import org.springframework.stereotype.Component;

/**
 * Entity for basic process information
 *
 * @author Serg
 */
@Component
public class ProcessInformation {

    private long processMemory;
    private String processName;

    public ProcessInformation() {
    }

    public ProcessInformation(long processMemory, String processName) {
        this.processMemory = processMemory;
        this.processName = processName;
    }

    public long getProcessMemory() {
        return processMemory;
    }

    public void setProcessMemory(long processMemory) {
        this.processMemory = processMemory;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }
}
