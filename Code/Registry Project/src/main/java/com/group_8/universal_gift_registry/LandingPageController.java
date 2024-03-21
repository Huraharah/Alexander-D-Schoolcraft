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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**JFX Controller class for the Landing Page scene
 * Enables the user to view their current lists, select one to edit, or create a new list.
 * Also allows for the user to search for another user's lists to purchase off of it.
 * Uses the currentUser persistent UserEntity to keep track of who is logged in.
 * Creates either the searchedUser UserEntity or currentList ListEntity, depending on the path
 * that the user takes.
 */
public class LandingPageController {
	private UserEntity currentUser;
	@FXML
	private AnchorPane anchor;
    @FXML
    private TableView<ListEntity> listOfLists;
    @FXML
    private TableColumn<ListEntity, String> listName;
    @FXML
    private TableColumn<ListEntity, String> Occasion;
    @FXML
    private Label noListsFound;
    @FXML
    private Button newList;
    @FXML
    private Button searchLists;
    @FXML
    private TextField searchEmail;
    @FXML
    private Button editList;

    /**Checks for a selected list from the table of lists, then, if found, moves onto the Edit List
     * page, creating a persistent ListEntity of the list selected to edit
     * @param event
     * @throws IOException
     */
    @FXML
    private void handleEditListAction(ActionEvent event) throws IOException {
        ListEntity selectedList = listOfLists.getSelectionModel().getSelectedItem();
        if (selectedList != null) {
            switchToEditListPage(selectedList);
        } else {
            ShowAlertUtil.showAlert("Selection Error", "Please select a list to edit.");
        }
    }

    /**Moves the user to the New List page to generate a new list.
     * @param event
     * @throws IOException
     */
    @FXML
    private void handleNewListAction(ActionEvent event) throws IOException {
       switchToNewListPage();
    }

    /**Checks the search box for an email address, and then connects to the database to pull that user, if found.
     * Then, generates a new UserEntity to keep track of which user was searched for, and moves to the Search List page.
     * @param event
     * @throws IOException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @FXML
    private void handleSearchListAction(ActionEvent event) throws IOException, SQLException, ClassNotFoundException {
    	UserEntity searchedUser = new UserEntity();
    	String query = "SELECT * FROM [User] WHERE Email = ?";
        String email = searchEmail.getText();
        
        if(email != null) {
	        try (Connection conn = GetConnectionUtil.getConnection();
	             PreparedStatement pstmt = conn.prepareStatement(query)) {
	
	            pstmt.setString(1, email);
	            ResultSet rs = pstmt.executeQuery();
	
	            if (rs.next()) {
	                searchedUser.setEmail(rs.getString("Email"));
	                searchedUser.setFirstName(rs.getString("First_Name"));
	                searchedUser.setLastName(rs.getString("Last_Name"));
	            }
	            switchToSearchLists(searchedUser);
	        	}
	       
        	}
        else {
        	ShowAlertUtil.showAlert("Error", "Please enter user email to search");
        }
        
    }

    /**When 'loader.load()' is called from the prior screen, initializes the page, and does the initial
     * setup for proper display
     */
	@FXML
    private void initialize() {
        listName.setCellValueFactory(new PropertyValueFactory<>("listName"));
        Occasion.setCellValueFactory(new PropertyValueFactory<>("occasion"));
    }

	/**Populates the table of lists with registry lists owned by the current user.
	 */
    private void loadUserLists() {
	    ObservableList<ListEntity> userLists = FXCollections.observableArrayList();
	    String query = "SELECT [ListID], [ListName], [Occasion], [Email] FROM [List] WHERE [Email] = ?";

	    try (Connection conn = GetConnectionUtil.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(query)) {

	        pstmt.setString(1, currentUser.getEmail());
	        ResultSet rs = pstmt.executeQuery();

	        while (rs.next()) {
	            int listID = rs.getInt("ListID");
	            String listName = rs.getString("ListName");
	            String occasionCode = rs.getString("Occasion");
	            String fullOccasionName = OccasionUtil.getOccasionForCode(occasionCode);
	            ListEntity newList = new ListEntity(listID, fullOccasionName, listName, currentUser);
	            userLists.add(newList);
	        }

	        listOfLists.setItems(userLists);

	        if (userLists.isEmpty()) {
	            noListsFound.setText("No lists found for user.");
	            noListsFound.setVisible(true);
	        } else {
	            noListsFound.setVisible(false);
	        }

	    } catch (SQLException | ClassNotFoundException e) {
	        ShowAlertUtil.showAlert("Database Error", "Error loading user lists: " + e.getMessage());
	    }
	}

    /**Called from the prior scene to pass the currently logged in user as a persistent UserEntity.
     * @param user: Current logged on user
     */
    public void setCurrentUser(UserEntity user) {
		currentUser = user;
        loadUserLists();
	}

    /**If the user selects a list of their own to edit, this method will switch the scene over to the
     * edit list scene, passing both the selected list, as well as the current user, to add or remove
     * items from the list
     * @param selectedList
     * @throws IOException
     */
    private void switchToEditListPage(ListEntity selectedList) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Edit_Lists.fxml"));
        Parent root = loader.load();

        EditListController editListController = loader.getController();
        editListController.setCurrentList(selectedList);
        editListController.setCurrentUser(currentUser);

        Stage newStage = new Stage();
        newStage.setScene(new Scene(root));
        newStage.show();

        Stage stage = (Stage) anchor.getScene().getWindow();
        stage.close();
    }

    /**If the user needs to make a new list, this will switch the user over to the new list screen,
     * where they can set the name of the list as well as select the occasion.
     * @throws IOException
     */
    private void switchToNewListPage() throws IOException {
        Stage stage = (Stage) anchor.getScene().getWindow();
        stage.close();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/New_List.fxml"));
        Parent root = loader.load();

        NewListController newListController = loader.getController();
        newListController.setCurrentUser(currentUser);

        Stage newStage = new Stage();
        newStage.setScene(new Scene(root));
        newStage.show();
    }

    /**If the user is looking to purchase for another user, this method will switch over to the searched
     * user's lists, allowing for the logged-in user to select a list to purchase from.
     * @param searchedUser
     * @throws IOException
     */
    private void switchToSearchLists(UserEntity searchedUser) throws IOException {
        Stage stage = (Stage) anchor.getScene().getWindow();
        stage.close();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Searched_Lists.fxml"));
        Parent root = loader.load();

        SearchedListsController searchedListsController = loader.getController();
        searchedListsController.setCurrentUser(currentUser);
        searchedListsController.setSearchedUser(searchedUser);
        searchedListsController.loadUserLists();

        Stage newStage = new Stage();
        newStage.setScene(new Scene(root));
        newStage.show();
    }
    
    /**When the user chooses to log out of the system, will remove the current user, and return the user to the login screen.
     * @throws IOException
     */
    @FXML
    private void handleLogOut() throws IOException {
        Stage stage = (Stage) anchor.getScene().getWindow();
        stage.close();
        
        currentUser = null;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Login_Register.fxml"));
        Parent root = loader.load();

        Stage newStage = new Stage();
        newStage.setScene(new Scene(root));
        newStage.show();
    }
}
