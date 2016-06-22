package sergii.makarenko.service;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sergii.makarenko.domain.ProcessHeader;
import sergii.makarenko.domain.ProcessInformation;
import sergii.makarenko.domain.ProcessInformationDetail;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * implementation methods for UI
 *
 * @author serg
 */
@Service
public class UIMethodsServiceImpl implements UIMethodsService {

    @Autowired
    private ProcessInformationService processInformationService;
    @Autowired
    private ProcessHeader processHeader;
    private static final String TEMPLATE = "template.xlsx";


    /**
     * Show FileChooser for choosing file. Call method for save data into file
     *
     * @param stage
     * @param processInformationDetailList
     */
    @Override
    public void saveToXML(Stage stage, List<ProcessInformationDetail> processInformationDetailList) {
        FileChooser chooserSaveToXML = new FileChooser();
        chooserSaveToXML.setTitle("Save to XML");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TEXT files (*.xml)", "*.xml");
        chooserSaveToXML.getExtensionFilters().add(extFilter);
        File file = chooserSaveToXML.showSaveDialog(stage);

        if (file != null) {
            try {
                file.createNewFile();
                try {
                    processInformationService.saveProcessInformationToXMLFile(
                            file, processInformationService.processInformationDetailToProcessInformation(
                                    processInformationDetailList));
                } catch (JAXBException e) {
                    showAlert(Alert.AlertType.ERROR,"Error", e.getCause()==null ? e.getLocalizedMessage():
                            e.getCause().getLocalizedMessage(),"");
                    e.printStackTrace();
                }
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR,"Error", e.getCause()==null ? e.getLocalizedMessage():
                        e.getCause().getLocalizedMessage(),"");
                e.printStackTrace();
            }
        }
    }

    /**
     * Show FileChooser for selecting file with data. Call method for obtain data from file.
     *
     * @param stage
     * @return
     */
    @Override
    public List<ProcessInformation> loadFromXML(Stage stage) {
        FileChooser loadFromXML = new FileChooser();
        loadFromXML.setTitle("Load from XML");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TEXT files (*.xml)", "*.xml");
        loadFromXML.getExtensionFilters().add(extFilter);
        File file = loadFromXML.showOpenDialog(stage);
        if (file != null) {
            try {
                return processInformationService.loadProcessInformationFromFile(file);
            } catch (UnmarshalException ue) {
                showAlert(Alert.AlertType.ERROR, "Error", "Unmarshalling exception",
                        ue.getCause() == null ? ue.getLocalizedMessage() : ue.getCause().getLocalizedMessage());
                ue.printStackTrace();
            } catch (JAXBException jaxb) {
                showAlert(Alert.AlertType.ERROR, "Error", "Load from file error",
                        jaxb.getCause().getLocalizedMessage());
                jaxb.printStackTrace();

            }
        }
        return null;
    }

    /**
     * Save data into xlsx or xls file
     *
     * @param stage
     * @param processInformationDetailList
     */
    @Override
    public void saveToExcel(Stage stage, List<ProcessInformationDetail> processInformationDetailList) {
        List<ProcessInformation> processInformationList = processInformationService.processInformationDetailToProcessInformation(
                processInformationDetailList);
        FileChooser chooserExcelSave = new FileChooser();
        chooserExcelSave.setTitle("Save to Excel");
        FileChooser.ExtensionFilter excelFilter = new FileChooser.ExtensionFilter("EXCEL files", "*.xlsx", "*.xls");
        chooserExcelSave.getExtensionFilters().add(excelFilter);
        File file = chooserExcelSave.showSaveDialog(stage);
        if (file != null) {
            String ext = FilenameUtils.getExtension(file.getName());
            if (!("xls".equals(ext) || "xlsx".equals(ext))) {
                showAlert(Alert.AlertType.ERROR, "Wrong file extention", "", ext);
            } else {
                try {
                    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                    String pathTemplate = classloader.getResource(TEMPLATE).getPath();
                    Workbook workbook = new XSSFWorkbook(OPCPackage.open(new FileInputStream(pathTemplate)));
                    Sheet sheet = workbook.getSheetAt(0);
                    Iterator<Row> rows = sheet.iterator();
                    while (rows.hasNext()){
                        Row row =  rows.next();
                        row.getCell(0).setCellValue("");
                        row.getCell(1).setCellValue("");
                    }
                    processInformationList.sort(new Comparator<ProcessInformation>() {
                        @Override
                        public int compare(ProcessInformation pr1, ProcessInformation pr2) {
                            if (pr1.getProcessMemory() > pr2.getProcessMemory())
                                return -1;
                            if(pr1.getProcessMemory() == pr2.getProcessMemory())
                                return 0;
                            return 1;
                        }
                    });
                    XSSFRow row = (XSSFRow) sheet.createRow(0);
                    row.createCell(0).setCellValue(processHeader.getProcessNameName());
                    row.createCell(1).setCellValue(processHeader.getProcessMemoryName());

                    for (int i = 0; i < processInformationList.size(); i++) {
                        row = (XSSFRow) sheet.createRow(1 + i);
                        row.createCell(0).setCellValue(processInformationList.get(i).getProcessName());
                        row.createCell(1).setCellValue(processInformationList.get(i).getProcessMemory());
                    }
                    
                    Name rangeCell = workbook.getName("name");
                    String formula = rangeCell.getRefersToFormula();
                    String referenceName = "OFFSET(processes!$A$2,,," +
                            processInformationList.size() + ",1)";
                    rangeCell.setRefersToFormula(referenceName);
                    rangeCell = workbook.getName("memory");
                    String referenceMemory = "OFFSET(processes!$B$2,,," +
                            processInformationList.size() + ",1)";
                    rangeCell.setRefersToFormula(referenceMemory);
                    
                    FileOutputStream fileOut = new FileOutputStream(pathTemplate);
                    workbook.write(fileOut);
                    FileChannel sourceChannel = new FileInputStream(pathTemplate).getChannel();
                    FileChannel destChannel = new FileOutputStream(file.getPath()).getChannel();
                    destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
                    fileOut.close();
                    sourceChannel.close();
                    destChannel.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (InvalidFormatException ife) {
                    ife.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Show alert
     *
     * @param type
     * @param title
     * @param headerText
     * @param contentText
     */
    @Override
    public void showAlert(Alert.AlertType type, String title, String headerText, String contentText) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }
}
