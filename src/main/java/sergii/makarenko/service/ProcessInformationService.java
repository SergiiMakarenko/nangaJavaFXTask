package sergii.makarenko.service;

import org.springframework.stereotype.Service;
import sergii.makarenko.domain.ProcessDifference;
import sergii.makarenko.domain.ProcessInformation;
import sergii.makarenko.domain.ProcessInformationDetail;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * An interface for works with Processes
 *
 * @author serg
 */
@Service
public interface ProcessInformationService {
    List<ProcessInformationDetail> getProcessInformationDetailList()
            throws IOException;

    void saveProcessInformationToXMLFile(File file, List<ProcessInformation> processInformationList) throws JAXBException;

    List<ProcessInformation> processInformationDetailToProcessInformation(
            List<ProcessInformationDetail> processInformationDetailList);

    List<ProcessInformation> loadProcessInformationFromFile(File file) throws JAXBException;

    List<ProcessDifference> generateProcessDifferenceList(List<ProcessInformation> processInformationListNew,
                                                          List<ProcessInformation> processInformationListOld);
}
