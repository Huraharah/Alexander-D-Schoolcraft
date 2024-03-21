package com.group_8.universal_gift_registry;
/**@application: UniversalGiftRegistry
 * @author: Alexander Schoolcraft, Benjamin King, Brandon King, Gabe Woolums
 * @date: 3/21/2024
 * @version: 3.0
 */

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.group_8.universal_gift_registry.model.ListEntity;
import com.group_8.universal_gift_registry.model.UserEntity;
import com.group_8.universal_gift_registry.util.GetConnectionUtil;
import com.group_8.universal_gift_registry.util.OccasionUtil;
import com.group_8.universal_gift_registry.util.ShowAlertUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;

/**JFX Controller class for the New List scene
 * Enables the user to choose an occasion and name for the list, then creates the new list entity and
 * publishes the information to the database.
 */
public class NewListController {

	private UserEntity currentUser;
	@FXML
	private TitledPane newList;
	@FXML
	private Button makeList;
	@FXML
	private TextField listName;
	@FXML
	private ComboBox<String> occasion;
	@FXML
	private Button backToLists;



    /**In conjunction with the "List Name" and "Occasion" fields on the screen, this method handles the
     * action when the button to create a new list is clicked.
     * @param event
     * @throws IOException
     * @throws ClassNotFoundException
     */
	@FXML
	private void handleMakeListAction(ActionEvent event) throws IOException, ClassNotFoundException {
	    String occasionCode = OccasionUtil.getCodeForOccasion(occasion.getValue());

	    ListEntity newList = new ListEntity();
	    newList.setListName(listName.getText());
	    newList.setOccasion(occasionCode);
	    newList.setUser(currentUser);

	    insertNewList(newList);

	    switchToEditListPage(newList);
	}

    /**Returns the user to the home screen.
     * @param event
     */
    @FXML
    private void handleReturnToHome(ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Landing_Page.fxml"));
            Parent root = loader.load();

            LandingPageController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = (Stage) ( newList.getScene()).getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            ShowAlertUtil.showAlert("Navigation Error", "Error when trying to return to the home page: " + e.getMessage());
        }
    }

    /**When 'loader.load()' is called from the prior screen, initializes the page, and does the initial
     * setup for proper display
     */
	@FXML
	private void initialize() {
	    ObservableList<String> occasions = FXCollections.observableArrayList(
	        "Birthday", "Anniversary", "Wedding", "Baby Shower",
	        "Graduation", "Holiday", "Retirement", "Housewarming",
	        "Engagement", "None", "Other");
	    occasion.setItems(occasions);
	}

	/**When the "New List" button is pressed, creates the new list entity within the database under the current
	 * user.
	 * @param list
	 * @throws ClassNotFoundException
	 */
    private void insertNewList(ListEntity list) throws ClassNotFoundException {
	    String query = "INSERT INTO [List] ([ListName], [Occasion], [Email]) VALUES (?, ?, ?)";

	    try (Connection conn = GetConnectionUtil.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

	        pstmt.setString(1, list.getListName());
	        pstmt.setString(2, list.getOccasion());
	        pstmt.setString(3, currentUser.getEmail());

	        int affectedRows = pstmt.executeUpdate();
	        if (affectedRows == 0) {
	            throw new SQLException("Creating list failed, no rows affected.");
	        }

	        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
	            if (generatedKeys.next()) {
	                list.setListID(generatedKeys.getInt(1));
	            } else {
	                throw new SQLException("Creating list failed, no ID obtained.");
	            }
	        }
	    } catch (SQLException e) {
	        ShowAlertUtil.showAlert("Database Error", e.getMessage());
	    }
	}

    /**Called from the prior scene to pass the currently logged in user as a persistent UserEntity.
     * @param user: Current logged on user
     */
    public void setCurrentUser(UserEntity user) {
		currentUser = user;
	}

    /**After the new list is created in the system, this method builds and displays the edit list screen,
     * passing both the newly created list, and the current user to the next scene.
     * @param newList
     * @throws IOException
     */
	private void switchToEditListPage(ListEntity newList) throws IOException {
	    FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Edit_Lists.fxml"));
	    Parent root = loader.load();

	    EditListController editListController = loader.getController();
	    editListController.setCurrentList(newList);
	    editListController.setCurrentUser(currentUser);

	    Stage newStage = new Stage();
	    newStage.setScene(new Scene(root));
	    newStage.show();

	    Stage stage = (Stage) makeList.getScene().getWindow();
	    stage.close();
	}
}
