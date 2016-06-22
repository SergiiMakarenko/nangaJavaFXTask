package sergii.makarenko.domain;

import org.springframework.stereotype.Component;

/**
 * @author Serg
 */
@Component
public class ProcessHeader {

    private String pidName;
    private String processMemoryName;
    private String processNameName;

    public ProcessHeader() {
    }

    public String getPidName() {
        return pidName;
    }

    public void setPidName(String pidName) {
        this.pidName = pidName;
    }

    public String getProcessMemoryName() {
        return processMemoryName;
    }

    public void setProcessMemoryName(String processMemoryName) {
        this.processMemoryName = processMemoryName;
    }

    public String getProcessNameName() {
        return processNameName;
    }

    public void setProcessNameName(String processNameName) {
        this.processNameName = processNameName;
    }
}
