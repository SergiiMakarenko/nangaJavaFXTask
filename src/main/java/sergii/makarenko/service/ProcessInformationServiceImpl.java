package sergii.makarenko.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sergii.makarenko.domain.ProcessDifference;
import sergii.makarenko.domain.ProcessHeader;
import sergii.makarenko.domain.ProcessInformation;
import sergii.makarenko.domain.ProcessInformationDetail;
import sergii.makarenko.jaxb.ProcessInformationXML;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * implementations methods for work with processes
 *
 * @author serg
 */
@Service
public class ProcessInformationServiceImpl implements ProcessInformationService {

    @Autowired
    private ProcessHeader processHeader;
    private int[] borders = new int[4];
    private List<String> stringProcess;

    /**
     * method for obtain ProcessInformationDetail list from Process
     *
     * @return list of ProcessInformationDetail
     * @throws IOException
     */
    public List<ProcessInformationDetail> getProcessInformationDetailList()
            throws IOException {
        List<ProcessInformationDetail> processInformationDetailList = new ArrayList<>();
        String osName = System.getProperty("os.name");
        Process process;
        if ("Linux".equals(osName)) {
            process = Runtime.getRuntime().exec("ps -eo pid,vsize,cmd");
            BufferedReader inputProcesses =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            getProcessInformationDetailLinux(
                    processInformationDetailList, inputProcesses);
            inputProcesses.close();
        } else if (osName.contains("Windows")) {
            process = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe");
            BufferedReader inputProcesses =
                    new BufferedReader(new InputStreamReader(process.getInputStream(), "Cp866"));
            getProcessInformationDetailWindows(processInformationDetailList,
                    inputProcesses);
            inputProcesses.close();
        } else {
            throw new NangaJavaFXException("Unknown OS: " + osName);
        }
        return processInformationDetailList;
    }

    /**
     * Method for saving ProcessInformation list to file
     *
     * @param file                   xml file
     * @param processInformationList list of current process
     * @throws JAXBException
     */
    @Override
    public void saveProcessInformationToXMLFile(File file, List<ProcessInformation> processInformationList)
            throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(ProcessInformationXML.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        ProcessInformationXML wrapper = new ProcessInformationXML();
        wrapper.setProcesses(processInformationList);
        m.marshal(wrapper, file);
    }

    /**
     * Method for obtaining a list of ProcessInformation from file
     *
     * @param file
     * @return list of ProcessInformation
     * @throws JAXBException
     */
    @Override
    public List<ProcessInformation> loadProcessInformationFromFile(File file) throws JAXBException {
        List<ProcessInformation> processInformationList = new ArrayList<>();
        JAXBContext context = JAXBContext.newInstance(ProcessInformationXML.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        ProcessInformationXML processInformationXML = (ProcessInformationXML) unmarshaller.unmarshal(file);
        processInformationList.addAll(processInformationXML.getProcesses());
        return processInformationList;
    }

    /**
     * Convert a list of ProcessInformationDetail to list of ProcessInformation
     * If processName the same processes will be combined
     *
     * @param processInformationDetailList current process list
     * @return a list of ProcessInformation
     */
    @Override
    public List<ProcessInformation> processInformationDetailToProcessInformation(
            List<ProcessInformationDetail> processInformationDetailList) {
        List<ProcessInformation> processInformationList = new ArrayList<>();
        if (processInformationDetailList == null || processInformationDetailList.isEmpty())
            return processInformationList;
        boolean addNewProcess;
        for (ProcessInformationDetail detailProcess : processInformationDetailList) {
            addNewProcess = true;
            for (ProcessInformation process : processInformationList) {
                if (Objects.equals(detailProcess.getProcessName(), process.getProcessName())) {
                    process.setProcessMemory(process.getProcessMemory() + detailProcess.getProcessMemory());
                    addNewProcess = false;
                    break;
                }
            }
            if (addNewProcess)
                processInformationList.add(new ProcessInformation(
                        detailProcess.getProcessMemory(), detailProcess.getProcessName()));
        }
        return processInformationList;
    }

    /**
     * Method for find differences between current processes and processes from file
     *
     * @param processInformationListNew - current processes in UI
     * @param processInformationListOld - processes from file
     * @return a list of ProcessDifference
     */
    @Override
    public List<ProcessDifference> generateProcessDifferenceList(List<ProcessInformation> processInformationListNew,
                                                                 List<ProcessInformation> processInformationListOld) {
        List<ProcessDifference> processDifferenceList = new ArrayList<>();
        if (processInformationListNew != null) {
            for (ProcessInformation processInformation : processInformationListNew) {
                processDifferenceList.add(new ProcessDifference(processInformation.getProcessName(),
                        processInformation.getProcessMemory(), 0, processInformation.getProcessMemory()));
            }
        }
        boolean needAddNew;
        if (processInformationListOld != null) {
            for (ProcessInformation processInformation : processInformationListOld) {
                needAddNew = true;
                for (ProcessDifference processDifference : processDifferenceList) {
                    if (Objects.equals(processInformation.getProcessName(), processDifference.getProcessName())) {
                        processDifference.setMemoryOld(processInformation.getProcessMemory());
                        processDifference.setMemoryDifference(
                                processDifference.getMemoryNew() - processDifference.getMemoryOld());
                        needAddNew = false;
                        break;
                    }
                }
                if (needAddNew)
                    processDifferenceList.add(new ProcessDifference(processInformation.getProcessName(),
                            0, processInformation.getProcessMemory(), -processInformation.getProcessMemory()));
            }
        }
        return processDifferenceList;
    }

    /**
     * transform information from BufferReader to ProcessInformationDetail
     *
     * @param processInformationDetailList list of ProcessInformationDetail
     * @param inputProcesses               buffer reader with processes
     * @return list of ProcessInformationDetail
     * @throws IOException
     */
    private List<ProcessInformationDetail> getProcessInformationDetailLinux(
            List<ProcessInformationDetail> processInformationDetailList,
            BufferedReader inputProcesses) throws IOException {
        String line;
        int rowNumber = 0;
        while ((line = inputProcesses.readLine()) != null) {
            if (rowNumber == 0) {
                createHeaderLinux(line);
            } else {
                processInformationDetailList.add(stringToProcessInformation(line));
            }
            rowNumber++;
        }
        return processInformationDetailList;
    }

    /**
     * Parsing String to  ProcessInformationDetail
     *
     * @param processLine String with process information
     * @return ProcessInformationDetail
     * @throws NangaJavaFXException
     */
    private ProcessInformationDetail stringToProcessInformation(String processLine) throws NangaJavaFXException {
        List<String> strings;
        String trimProcessLine = processLine.trim();
        String delims = "[ ]+";
        strings = Arrays.asList(trimProcessLine.split(delims));
        if (strings.size() < 3)
            throw new NangaJavaFXException("parameter processLine contain " + strings.size() +
                    " separated words." + processLine);
        long memory;
        int pid;
        StringBuilder name = new StringBuilder();
        try {
            memory = Long.parseLong(strings.get(1));
            pid = Integer.parseInt(strings.get(0));
        } catch (NumberFormatException nfe) {
            throw new NangaJavaFXException("Number format exception at : " + processLine, nfe);
        }
        for (int i = 2; i < strings.size(); i++)
            name.append(strings.get(i)).append(" ");
        return new ProcessInformationDetail(pid, memory, name.toString().trim());
    }

    /**
     * Parsing String (Header) for ProcessHeader
     *
     * @param processLine String with process information
     * @throws NangaJavaFXException
     */
    private void createHeaderLinux(String processLine) throws NangaJavaFXException {
        List<String> strings;
        String trimProcessLine = processLine.trim();
        String delims = "[ ]+";
        strings = Arrays.asList(trimProcessLine.split(delims));
        if (strings.size() != 3)
            throw new NangaJavaFXException("parameter processLine contain " + strings.size() +
                    " separated words." + processLine);
        processHeader.setPidName(strings.get(0));
        processHeader.setProcessMemoryName(strings.get(1));
        processHeader.setProcessNameName(strings.get(2));
    }

    /**
     * transform information from BufferReader to ProcessInformationDetail
     *
     * @param processInformationDetailList list of ProcessInformationDetail
     * @param inputProcesses               buffer reader with processes
     * @return list of ProcessInformationDetail
     * @throws IOException
     */
    private List<ProcessInformationDetail> getProcessInformationDetailWindows(
            List<ProcessInformationDetail> processInformationDetailList,
            BufferedReader inputProcesses) throws IOException {
        String line;
        stringProcess = new ArrayList<>();
        while ((line = inputProcesses.readLine()) != null) {
            if (!line.isEmpty()) {
                stringProcess.add(line);
                if (line.contains("=="))
                    fillBorders(line);
            }
        }
        if (!stringProcess.isEmpty())
            createHeaderForWindows(stringProcess.get(0));
        for (int i = 2; i < stringProcess.size(); i++) {
            processInformationDetailList.add(stringToProcessInformationWindows(
                    stringProcess.get(i)));
        }
        return processInformationDetailList;
    }

    /**
     * Parsing String to  ProcessInformationDetail
     *
     * @param processLine String with process information
     * @return ProcessInformationDetail
     * @throws NangaJavaFXException
     */
    private ProcessInformationDetail stringToProcessInformationWindows(String processLine)
            throws NangaJavaFXException {
        String mem = processLine.substring(borders[3],
                processLine.length()).replaceAll("[^\\d]", "");
        long memory;
        int pid;
        try {
            memory = Long.parseLong(mem);
            pid = Integer.parseInt(processLine.substring(borders[0], borders[1]).trim());
        } catch (NumberFormatException nfe) {
            throw new NangaJavaFXException(
                    "Number format exception at : " + processLine);
        }
        return new ProcessInformationDetail(pid, memory,
                processLine.substring(0, borders[0]).trim());
    }

    /**
     * Set fields in ProcessHeader from String
     *
     * @param processLine String
     */
    private void createHeaderForWindows(String processLine) {
        processHeader.setPidName(processLine.substring(borders[0], borders[1]));
        processHeader.setProcessMemoryName(processLine.substring(borders[3],
                processLine.length()));
        processHeader.setProcessNameName(processLine.substring(0, borders[0]));
    }


    /**
     * Fill borders. Need to parsing String in Windows
     *
     * @param processLine String with process info
     */
    private void fillBorders(String processLine) {
        List<String> strings;
        String trimProcessLine = processLine.trim();
        String delims = "[ ]+";
        strings = Arrays.asList(trimProcessLine.split(delims));
        if (strings.size() == 5) {
            int sum = 0;
            for (int i = 0; i < borders.length; i++) {
                sum = sum + strings.get(i).length();
                borders[i] = sum;
                sum++;
            }
        } else {
            for (int i = 0; i < borders.length; i++) {
                borders[i] = 0;
            }
        }
    }
}
