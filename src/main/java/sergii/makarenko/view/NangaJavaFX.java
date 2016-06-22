package sergii.makarenko.view;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import sergii.makarenko.domain.ProcessDifference;
import sergii.makarenko.domain.ProcessHeader;
import sergii.makarenko.domain.ProcessInformation;
import sergii.makarenko.domain.ProcessInformationDetail;
import sergii.makarenko.service.*;
import sergii.makarenko.spring.Configurations;

import java.io.IOException;
import java.util.List;

/**
 * Created by serg on 13.06.16.
 */
public class NangaJavaFX extends Application {

    private ProcessInformationService processInformationService;
    private UIMethodsService uiMethodsService;
    private ProcessHeader processHeader;
    private TableView tableProcessList = new TableView();
    private TableView tableProcessDifferentList = new TableView();
    private ObservableList<ProcessInformationDetail> processInformationDetailObservableList;
    private List<ProcessInformationDetail> processInformationDetailList;
    private List<ProcessDifference> processDifferenceList;

    @Override
    public void init() {
        try {
            super.init();
            ApplicationContext context = new AnnotationConfigApplicationContext(Configurations.class);
            this.processInformationService = context.getBean("processInformationServiceImpl",
                    ProcessInformationServiceImpl.class);
            this.uiMethodsService = context.getBean("UIMethodsServiceImpl",
                    UIMethodsServiceImpl.class);
            this.processHeader = context.getBean("processHeader",
                    ProcessHeader.class);
            processInformationService.getProcessInformationDetailList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane bp = new BorderPane();
        Scene scene = new Scene(bp);
        primaryStage.setTitle("Process list");
        primaryStage.setWidth(PRIMARY_STAGE_WIDTH);
        primaryStage.setHeight(PRIMARY_STAGE_HEIGHT);
        bp.setMaxWidth(PRIMARY_STAGE_WIDTH);

        addTableToBorderPane(bp);
        updateDataInProcessListTable();

        addMenuToBorderPane(bp, primaryStage);

        primaryStage.setScene(scene);
        primaryStage.show();
    }


    /**
     * adding tableProcessList to the BorderPane
     *
     * @param bp - BorderPane
     */
    private void addTableToBorderPane(BorderPane bp) {
        tableProcessList.setMaxWidth(PRIMARY_STAGE_WIDTH - 20);

        TableColumn pid = new TableColumn(processHeader.getPidName());
        pid.setCellValueFactory(new PropertyValueFactory<>("processID"));
        pid.setMinWidth(COLUMN_WIDTH);

        TableColumn processMemory = new TableColumn(processHeader.getProcessMemoryName());
        processMemory.setCellValueFactory(new PropertyValueFactory<>("processMemory"));
        processMemory.setMinWidth(COLUMN_WIDTH);

        TableColumn processName = new TableColumn(processHeader.getProcessNameName());
        processName.setCellValueFactory(new PropertyValueFactory<>("processName"));
        processName.setMinWidth(COLUMN_WIDTH);

        tableProcessList.getColumns().removeAll(tableProcessList.getColumns());
        tableProcessList.getColumns().addAll(processName, pid, processMemory);

        VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(tableProcessList);
        bp.setCenter(vbox);
    }

    /**
     * Create menu
     *
     * @param bp
     * @param primaryStage
     */
    private void addMenuToBorderPane(BorderPane bp, Stage primaryStage) {
        MenuBar menuBar = new MenuBar();
        Menu processListMenu = new Menu("Process list");
        MenuItem findProcess = new MenuItem("Update process list");
        findProcess.setOnAction((event) -> {
            addTableToBorderPane(bp);
            updateDataInProcessListTable();
        });
        MenuItem compare = new MenuItem("Compare process list");
        compare.setOnAction((event) -> {
            List<ProcessInformation> processInformations = uiMethodsService.loadFromXML(primaryStage);
            if (processInformations != null) {
                addTableForDifference(bp);
                processDifferenceList = processInformationService.generateProcessDifferenceList(
                        processInformationService.processInformationDetailToProcessInformation(processInformationDetailList),
                        processInformations);
                tableProcessDifferentList.setItems(FXCollections.observableList(processDifferenceList));
            }
        });
        processListMenu.getItems().addAll(findProcess, compare);

        Menu saveMenu = new Menu("Save");
        MenuItem saveXML = new MenuItem("Save XML");
        saveXML.setOnAction((event) -> {
            uiMethodsService.saveToXML(primaryStage, processInformationDetailList);
        });
        MenuItem saveExcel = new MenuItem("Save Excel");
        saveExcel.setOnAction((event) -> {
            uiMethodsService.saveToExcel(primaryStage, processInformationDetailList);
        });

        saveMenu.getItems().addAll(saveXML, saveExcel);

        menuBar.getMenus().addAll(processListMenu, saveMenu);
        bp.setTop(menuBar);
    }

    /**
     * Create table to show differences
     *
     * @param bp
     */
    private void addTableForDifference(BorderPane bp) {
        tableProcessDifferentList.setMinWidth(PRIMARY_STAGE_WIDTH);
        tableProcessDifferentList.setMaxWidth(PRIMARY_STAGE_WIDTH);

        TableColumn processName = new TableColumn("Name");
        processName.setCellValueFactory(new PropertyValueFactory<>("processName"));
        processName.setMinWidth(COLUMN_WIDTH);

        TableColumn processMemoryCurrent = new TableColumn("Current memory use");
        processMemoryCurrent.setCellValueFactory(new PropertyValueFactory<>("memoryNew"));
        processMemoryCurrent.setMinWidth(COLUMN_WIDTH);

        TableColumn processMemoryPrevious = new TableColumn("Previous memory use");
        processMemoryPrevious.setCellValueFactory(new PropertyValueFactory<>("memoryOld"));
        processMemoryPrevious.setMinWidth(COLUMN_WIDTH);

        TableColumn processMemoryDifference = new TableColumn("Difference memory use");
        processMemoryDifference.setCellValueFactory(new PropertyValueFactory<>("memoryDifference"));
        processMemoryDifference.setMinWidth(COLUMN_WIDTH);

        tableProcessDifferentList.getColumns().removeAll(tableProcessDifferentList.getColumns());
        tableProcessDifferentList.getColumns().addAll(processName,
                processMemoryCurrent, processMemoryPrevious, processMemoryDifference);

        VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(tableProcessDifferentList);
        bp.setCenter(vbox);
    }

    /**
     * Update list of current process.
     */
    private void updateDataInProcessListTable() {
        try {
            processInformationDetailList = processInformationService.getProcessInformationDetailList();
        } catch (NangaJavaFXException nje) {
            uiMethodsService.showAlert(Alert.AlertType.ERROR, "Error", "", nje.getLocalizedMessage());
            nje.printStackTrace();
        } catch (IOException e) {
            uiMethodsService.showAlert(Alert.AlertType.ERROR, "Error", "", e.getLocalizedMessage());
            e.printStackTrace();
        }
        processInformationDetailObservableList = FXCollections.observableList(
                processInformationDetailList);
        tableProcessList.setItems(processInformationDetailObservableList);
    }

    private final static int PRIMARY_STAGE_WIDTH = 600;
    private final static int PRIMARY_STAGE_HEIGHT = 400;
    private final static int COLUMN_WIDTH = 100;

}
