package sergii.makarenko.domain;

import org.springframework.stereotype.Component;

/**
 * Entity for detailed process information
 *
 * @author Serg
 */
@Component
public class ProcessInformationDetail extends ProcessInformation {

    private int processID;

    public ProcessInformationDetail() {
    }

    public ProcessInformationDetail(int processID, long processMemory, String processName) {
        super(processMemory, processName);
        this.processID = processID;
    }

    public int getProcessID() {
        return processID;
    }

    public void setProcessID(int processID) {
        this.processID = processID;
    }

}
