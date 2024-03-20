package com.group_8.universal_gift_registry;
/**@application: UniversalGiftRegistry
 * @author: Alexander Schoolcraft, Benjamin King, Brandon King, Gabe Woolums
 * @date: 3/14/2024
 * @version: 0.1
 */

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.group_8.universal_gift_registry.model.ListEntity;
import com.group_8.universal_gift_registry.model.UserEntity;
import com.group_8.universal_gift_registry.util.OccasionUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**JFX Controller class for the Searched Lists scene
 * After the user has selected another user to purchase items for, this controller allows the user to
 * choose one of the selected user's lists to purchase off of.
 */
public class SearchedListsController {
	private UserEntity currentUser;
	private UserEntity searchedUser;
	@FXML
	private TableView<ListEntity> ListTable;
	@FXML
	private TableColumn<ListEntity, String> ListName;
	@FXML
	private TableColumn<ListEntity, String> Occasion;
	@FXML
	private Text ListHeader;
	@FXML
	private Label noListsFound;
	@FXML
	private AnchorPane anchor;

    /**getConnection uses the information for the SQL database to generate a connection with
     * the back-end server to store information.
     * @return DriverManager connection object using the required information to connect
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private Connection getConnection() throws SQLException, ClassNotFoundException {
        String url = "jdbc:sqlserver://ugr.database.windows.net:1433;database=universal_gift_registry;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
        String user = "UGRAdmin@ugr";
        String password = "UGRP@ssw0rd!";
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        return DriverManager.getConnection(url, user, password);
    }

    /**Identifies which list the user has selected to purchase from, and then calls the method to set up
     * the next scene.
     * @param event
     * @throws IOException
     */
    @FXML
    private void handlePurchaseList(ActionEvent event) throws IOException {
    	ListEntity selectedList = ListTable.getSelectionModel().getSelectedItem();
        if (selectedList != null) {
            switchToPurchaseScene(selectedList);
        } else {
            showAlert("Selection Error", "Please select a list to edit.");
        }
    }

    /**When 'loader.load()' is called from the prior screen, initializes the page, and does the initial
     * setup for proper display
     */
    @FXML
    private void initialize() {
        ListName.setCellValueFactory(new PropertyValueFactory<>("listName"));
        Occasion.setCellValueFactory(new PropertyValueFactory<>("occasion"));
    }

	/**Populates the table of lists with registry lists owned by the searched user.
	 */
    void loadUserLists() {
	    ObservableList<ListEntity> userLists = FXCollections.observableArrayList();
	    String query = "SELECT [ListID], [ListName], [Occasion], [Email] FROM [List] WHERE [Email] = ?";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(query)) {

	        pstmt.setString(1, searchedUser.getEmail());
	        ResultSet rs = pstmt.executeQuery();

	        while (rs.next()) {
	            int listID = rs.getInt("ListID");
	            String listName = rs.getString("ListName");
	            String occasionCode = rs.getString("Occasion");
	            String fullOccasionName = OccasionUtil.getOccasionForCode(occasionCode);
	            ListEntity newList = new ListEntity(listID, fullOccasionName, listName, searchedUser);
	            userLists.add(newList);
	        }

	        ListTable.setItems(userLists);

	        if (userLists.isEmpty()) {
	            noListsFound.setText("No lists found for user.");
	            noListsFound.setVisible(true);
	        } else {
	            noListsFound.setVisible(false);
	        }

	    } catch (SQLException | ClassNotFoundException e) {
	        showAlert("Database Error", "Error loading user lists: " + e.getMessage());
	    }
	}

    /**Called from the prior scene to pass the currently logged in user as a persistent UserEntity.
     * @param user: Current logged on user
     */
    void setCurrentUser(UserEntity user) {
    	currentUser = user;
    }

    /**Called from the prior scene to pass the searched user as a persistent UserEntity.
     * @param user: User searched for by the logged-in user.
     */
    void setSearchedUser(UserEntity user) {
    	searchedUser = user;
        ListHeader.setText(searchedUser.getFirstName() + " " + searchedUser.getLastName().charAt(0) + ".'s Registry Lists");
        loadUserLists();
    }

	/**showAlert method builds and displays a warning-type alert message to the user
     * This alert is specifically a scrolling-type alert box, in case the message is
     * too long to fit into the standardized window size (such as a file path).
     * @param title: The header of the alert
     * @param content: The content of the alert box
     */
    private void showAlert(String title, String content) {
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(false);

        textArea.setFont(javafx.scene.text.Font.font("Monospaced", 12));

        textArea.setPrefRowCount(10);
        textArea.setPrefColumnCount(50);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 1);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(expContent);

        alert.showAndWait();
    }

    /**Sets up the scene for the purchase screen, so that the user can purchase items off of the selected
     * list.
     * @param selectedList
     * @throws IOException
     */
    private void switchToPurchaseScene(ListEntity selectedList) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Purchases.fxml"));
        Parent root = loader.load();

        PurchasesController purchasesController = loader.getController();
        purchasesController.setCurrentList(selectedList);
        purchasesController.setCurrentUser(currentUser);
        purchasesController.setSearchedUser(searchedUser);


        Stage newStage = new Stage();
        newStage.setScene(new Scene(root));
        newStage.show();

        Stage stage = (Stage) anchor.getScene().getWindow();
        stage.close();
    }
    /**Returns the user to the home screen.
     * @param event
     */
    @FXML
    private void handleBackToHome(ActionEvent event) {
        try {
            searchedUser = null;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Landing_Page.fxml"));
            Parent root = loader.load();

            LandingPageController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = (Stage) (anchor.getScene()).getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            showAlert("Navigation Error", "Error when trying to return to the home page: " + e.getMessage());
        }
    }
}
