package sergii.makarenko.jaxb;

import sergii.makarenko.domain.ProcessInformation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * xml pattern for ProcessInformation
 *
 * @author Serg
 */
@XmlRootElement(name = "processes")
public class ProcessInformationXML {

    private List<ProcessInformation> processes;

    @XmlElement(name = "process")
    public List<ProcessInformation> getProcesses() {
        return processes;
    }

    public void setProcesses(List<ProcessInformation> processes) {
        this.processes = processes;
    }
}
