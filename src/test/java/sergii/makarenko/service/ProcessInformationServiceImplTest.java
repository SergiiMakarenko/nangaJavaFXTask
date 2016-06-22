package sergii.makarenko.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import sergii.makarenko.domain.ProcessInformation;
import sergii.makarenko.spring.Configurations;
import sergii.makarenko.domain.ProcessInformationDetail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Configurations.class)
public class ProcessInformationServiceImplTest {

    @Autowired
    private ProcessInformationServiceImpl processInformationServiceImpl;

    @Test
    public void stringToProcessInformationGoodParam() throws NoSuchMethodException {
        String rightProcessLine = processId + " " + processMemory + " " + processName;
        ProcessInformationDetail processInformationDetail = new ProcessInformationDetail();
        Method method = ProcessInformationServiceImpl.class.getDeclaredMethod("stringToProcessInformation", String.class);
        method.setAccessible(true);
        try {
            processInformationDetail = (ProcessInformationDetail)
                    method.invoke(processInformationServiceImpl, rightProcessLine);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        assertEquals(processId, processInformationDetail.getProcessID());
        assertEquals(processMemory, processInformationDetail.getProcessMemory());
        assertEquals(processName, processInformationDetail.getProcessName());
    }

    @Test
    public void stringToProcessInformationAdditionalWhiteSpaces() throws NoSuchMethodException {
        String rightProcessLine = "    " + processId + " " + processMemory + " " + processName + "   ";
        ProcessInformationDetail processInformationDetail = new ProcessInformationDetail();
        Method method = ProcessInformationServiceImpl.class.getDeclaredMethod("stringToProcessInformation", String.class);
        method.setAccessible(true);
        try {
            processInformationDetail = (ProcessInformationDetail)
                    method.invoke(processInformationServiceImpl, rightProcessLine);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        assertEquals(processId, processInformationDetail.getProcessID());
        assertEquals(processMemory, processInformationDetail.getProcessMemory());
        assertEquals(processName, processInformationDetail.getProcessName());
    }

    @Test
    public void testProcessInformationDetailToProcessInformationWithDuplicates() {
        List<ProcessInformationDetail> processInformationDetailList = new ArrayList<>();
        processInformationDetailList.add(new ProcessInformationDetail(processId, processMemory, processName));
        processInformationDetailList.add(new ProcessInformationDetail(processId + 1, processMemory, processName));
        List<ProcessInformation> processInformationList =
                processInformationServiceImpl.processInformationDetailToProcessInformation(processInformationDetailList);
        assertEquals(1, processInformationList.size());
        assertEquals(2 * processMemory, processInformationList.get(0).getProcessMemory());
    }

    @Test
    public void testProcessInformationDetailToProcessInformationWithoutDuplicates() {
        List<ProcessInformationDetail> processInformationDetailList = new ArrayList<>();
        processInformationDetailList.add(new ProcessInformationDetail(processId, processMemory, processName));
        processInformationDetailList.add(new ProcessInformationDetail(processId + 1, processMemory, processName + " "));
        List<ProcessInformation> processInformationList =
                processInformationServiceImpl.processInformationDetailToProcessInformation(processInformationDetailList);
        assertEquals(2, processInformationList.size());
        assertEquals(processMemory, processInformationList.get(0).getProcessMemory());
    }

    private String processName = "processName";
    private int processId = 12;
    private long processMemory = 10;

}