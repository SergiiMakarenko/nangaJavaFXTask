package sergii.makarenko.service;

import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.springframework.stereotype.Service;
import sergii.makarenko.domain.ProcessInformation;
import sergii.makarenko.domain.ProcessInformationDetail;

import java.util.List;

/**
 * An interface for methods on UI
 *
 * @author serg
 */
@Service
public interface UIMethodsService {
    void saveToXML(Stage stage, List<ProcessInformationDetail> processInformationDetailList);

    List<ProcessInformation> loadFromXML(Stage stage);

    void saveToExcel(Stage stage, List<ProcessInformationDetail> processInformationDetailList);

    void showAlert(Alert.AlertType type, String title, String headerText, String contentText);
}
