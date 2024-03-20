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

import com.group_8.universal_gift_registry.model.ItemEntity;
import com.group_8.universal_gift_registry.model.ListEntity;
import com.group_8.universal_gift_registry.model.UserEntity;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**JFX Controller class for the Purchases scene
 * Enables the user to navigate through the Amazon.com web store, or even select an item off of the list
 * and directly navigate to it, then purchase it and mark it as purchased on the list.
 */
public class PurchasesController {

	private ListEntity currentList;
	private UserEntity currentUser;
	private UserEntity searchedUser;
	@FXML
	private AnchorPane Purchase;
    @FXML
    private TableView<ItemEntity> CurrentList;
    @FXML
    private TableColumn<ItemEntity, String> ItemName;
    @FXML
    private TableColumn<ItemEntity, String> URL;
    @FXML
    private TableColumn<ItemEntity, String> ItemColor;
    @FXML
    private TableColumn<ItemEntity, String> ItemSize;
    @FXML
    private TableColumn<ItemEntity, Double> ItemPrice;
    @FXML
    private TableColumn<ItemEntity, Boolean> Purchased;
    @FXML
    private WebView WebView;
    @FXML
    private Button GoTo;
    @FXML
    private Button PurchasedItem;
    @FXML
    private TextField AddressBar;
    @FXML
    private Button GoButton;
    @FXML
    private Button Back;
    @FXML
    private Button Forward;
    @FXML
    private Button ReturnToHome;

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

	/**The next two methods enable to Back and Forward buttons on the scene to act like they would
	 * in a real browser.
	 * @param event
	 */
	@FXML
    private void handleBackAction(ActionEvent event) {
        final WebHistory history = WebView.getEngine().getHistory();
        if (history.getCurrentIndex() > 0) {
            history.go(-1);
        }
    }

    @FXML
    private void handleForwardAction(ActionEvent event) {
        final WebHistory history = WebView.getEngine().getHistory();
        if (history.getCurrentIndex() < history.getEntries().size() - 1) {
            history.go(1);
        }
    }

    /**When the user selects an item from the list, this action handler will make the WebView pane load
     * the URL of the item selected.
     * @param event
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @FXML
    private void handleGoToAction(ActionEvent event) throws SQLException, ClassNotFoundException {
        ItemEntity selectedItem = CurrentList.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            WebView.getEngine().load(selectedItem.getItemURL());
        } else {
            showAlert("Selection Error", "Please select an item.");
        }
    }

    /**After the user has purchased an item from the list, this action handler will update the item information
     * to mark it as purchased.
     * @param event
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @FXML
    private void handlePurchaseAction(ActionEvent event) throws SQLException, ClassNotFoundException {
        ItemEntity selectedItem = CurrentList.getSelectionModel().getSelectedItem();
        if (selectedItem != null && !selectedItem.getPurchased()) {
            String updateQuery = "UPDATE [Item] SET [Purchased] = 1 WHERE [ItemID] = ?";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                pstmt.setInt(1, selectedItem.getItemID());
                pstmt.executeUpdate();
                selectedItem.setPurchased(true);
                CurrentList.refresh();
            } catch (SQLException e) {
                showAlert("Database Error", "Error updating item status: " + e.getMessage());
            }
        } else {
            showAlert("Selection Error", "Please select an unpurchased item.");
        }
    }

    /**Returns the user to the home screen.
     * @param event
     */
    @FXML
    private void handleReturnToHome(ActionEvent event) {
        try {
            currentList = null;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Landing_Page.fxml"));
            Parent root = loader.load();

            LandingPageController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = (Stage) ( Purchase.getScene()).getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            showAlert("Navigation Error", "Error when trying to return to the home page: " + e.getMessage());
        }
    }

    /**When 'loader.load()' is called from the prior screen, initializes the page, and does the initial
     * setup for proper display
     */
	@FXML
    private void initialize() {
        ItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        URL.setCellValueFactory(new PropertyValueFactory<>("itemURL"));
        ItemColor.setCellValueFactory(new PropertyValueFactory<>("itemColor"));
        ItemSize.setCellValueFactory(new PropertyValueFactory<>("itemSize"));
        ItemPrice.setCellValueFactory(new PropertyValueFactory<>("itemPrice"));
        Purchased.setCellValueFactory(new PropertyValueFactory<>("purchased"));
        WebView.getEngine().load("https://www.amazon.com");
        CurrentList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        GoButton.setOnAction(e -> {
            String url = AddressBar.getText();
            boolean isAmazonUrl = url.contains("amazon.com");

            if (!isAmazonUrl) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Restricted Access");
                alert.setHeaderText(null);
                alert.setContentText("You are only allowed to visit Amazon.com");
                alert.showAndWait();
                return;
            }

            if (!url.startsWith("http") && isAmazonUrl) {
                url = "http://" + url;
            }

            WebView.getEngine().load(url);
        });
    }

    /**A part of the initialization of the page, this method is called by the prior scene before switching
     * over to this one, so that when it does load, the items on the currently selected list are displayed
     * properly.
     */
    public void loadListItems() {
	    ObservableList<ItemEntity> items = FXCollections.observableArrayList();
	    String query = "SELECT * FROM [Item] WHERE [ListID] = ?";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(query)) {

	        pstmt.setInt(1, currentList.getListID());
	        ResultSet rs = pstmt.executeQuery();

	        while (rs.next()) {
	            ItemEntity item = new ItemEntity(
	                rs.getInt("ItemID"),
	                rs.getString("ItemURL"),
	                rs.getString("ItemImage"),
	                rs.getString("ItemName"),
	                rs.getString("ItemSize"),
	                rs.getString("ItemColor"),
	                rs.getDouble("ItemPrice"),
	                rs.getBoolean("Purchased"),
	                searchedUser,
	                currentList
	            );
	            items.add(item);
	        }

	        CurrentList.setItems(items);
	    } catch (SQLException | ClassNotFoundException e) {

	        showAlert("Database Error", "Error loading list items: " + e.getMessage());
	    }
	}
    /**Called from the prior scene to pass the currently selected ListEntity.
     * @param list: Current ListEntity to purchase
     */
	public void setCurrentList(ListEntity list) {
		currentList = list;
		loadListItems();
	}

    /**Called from the prior scene to pass the currently logged in user as a persistent UserEntity.
     * @param user: Current logged on user
     */
    public void setCurrentUser(UserEntity user) {
		currentUser = user;
	}

    /**Called from the prior scene to pass the searched user as a persistent UserEntity.
     * @param user: User searched for by the logged-in user.
     */
    public void setSearchedUser(UserEntity user) {
		searchedUser = user;
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
}
