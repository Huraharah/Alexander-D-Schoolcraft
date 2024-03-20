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

import com.group_8.universal_gift_registry.model.UserEntity;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
/**JFX Controller class for the Login/Register scene
 * Enables the user to register, login, and retrieve password, while creating the persistent
 * user entity to track which user is logged into the system.
 */

public class LoginController {
    private UserEntity currentUser;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab loginTab;
    @FXML
    private Tab registerTab;
    @FXML
    private Tab forgotPasswordTab;
    @FXML
    private TextField emailLogin;
    @FXML
    private PasswordField passwordLogin;
    @FXML
    private CheckBox agreeLogin;
    @FXML
    private TextField emailReg;
    @FXML
    private TextField streetAddress;
    @FXML
    private TextField city;
    @FXML
    private ComboBox<String> state;
    @FXML
    private TextField zip;
    @FXML
    private TextField firstName;
    @FXML
    private TextField lastName;
    @FXML
    private PasswordField passwordReg;
    @FXML
    private PasswordField passwordConf;
    @FXML
    private CheckBox agreeReg;
    @FXML
    private TextField emailForgot;
    @FXML
    private Label passwordRetrieved;

    /**Method to confirm that the email does not exist within the server
     * @param email: Email of the user as input into the registration page
     * @return True if the email does exist on the server, false otherwise
     * @throws ClassNotFoundException
     */
    private boolean emailExists(String email) throws ClassNotFoundException {
        String query = "SELECT COUNT(*) FROM User WHERE Email = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; //If count is greater than 0, the email exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

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

    /**handleLoginButtonAction first confirms that the login screen is filled out correctly,
     * then confirms that the user email and password match what is in the database, and if
     * it all does, switches the program to the landing page.
     * @param event
     * @throws IOException
     */
    @FXML
    private void handleLoginButtonAction(ActionEvent event) throws IOException {
        String email = emailLogin.getText();
        String password = passwordLogin.getText();

        try {
            if (!agreeLogin.isSelected()) {
                showAlert("Login Error", "You must agree to the Terms and Conditions.");
                return;
            }

            UserEntity currentUser = verifyLogin(email, password);
            if (currentUser != null) {
                switchToLandingPage(currentUser);
            } else {
                showAlert("Login Error", "Incorrect email or password.");
            }
        } catch (Exception e) {
            showAlert("Database Error", "Error while logging in: " + e.getMessage());
        }
    }
    @FXML
    private void handleRegisterButtonAction(ActionEvent event) throws ClassNotFoundException {
        registerUser();
    }
    @FXML
    private void handleRetrievePasswordAction(ActionEvent event) throws ClassNotFoundException {
        retrievePassword();
    }

    /**Confirms that all of the user information is filled out correctly, then verifies
     * the information against the database, and if everything passes, creates a new user
     * in the database off of the information provided
     * @throws ClassNotFoundException
     */
    private void registerUser() throws ClassNotFoundException {
        if (!validateRegistrationInput()) {
            return;
        }
        if (!agreeReg.isSelected()) {
            showAlert("Validation Error", "You must agree to the Terms and Conditions.");
            return;
        }


        String query = "INSERT INTO [User] ([Email], [Street_Address], [City], [State], [Zip], [First_Name], [Last_Name], [Password]) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection conn = getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, emailReg.getText());
                pstmt.setString(2, streetAddress.getText());
                pstmt.setString(3, city.getText());
                pstmt.setString(4, state.getValue());
                pstmt.setString(5, zip.getText());
                pstmt.setString(6, firstName.getText());
                pstmt.setString(7, lastName.getText());
                pstmt.setString(8, passwordReg.getText());
                pstmt.executeUpdate();//builds the SQL query to post to the server and sends it
                showInformationAlert("Registration Successful", "You are now registered");
            } catch (SQLException ex) {
                showAlert("Database Error", "Error inserting new user: " + ex.getMessage());
            }
        } catch (ClassNotFoundException e) {
            showAlert("Driver Error", "MySQL JDBC driver not found");
        } catch (SQLException e) {
            showAlert("Database Error", "Error connecting to the database: " + e.getMessage());
        }
    }


    /**Connects to the server, verifies that the user exists, and retrieves the password for the user
     * ALERT: THIS IS NOT A SECURE WAY TO RETRIEVE PASSWORDS, AND ONLY DONE WITHIN THE CLASS ASSIGNMENT
     * If this was turned into a production application, this method would be heavily changed, to email
     * the user a link to reset the password. Additionally, the passwords would not be stored in the
     * database as plain-text, but they would be hashed, as would all comparisons to the password, which
     * would not allow for this method of retrieval anyways.
     * @throws ClassNotFoundException
     */
    private void retrievePassword() throws ClassNotFoundException {
        String email = emailForgot.getText();
        String query = "SELECT [Password] FROM [User] WHERE [Email] = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String password = rs.getString("Password");
                    passwordRetrieved.setText("Your password is: " + password);
                } else {
                    showAlert("Retrieve Password", "No account found for this email.");
                }
            }
        } catch (SQLException ex) {
            showAlert("Database Error", "Error retrieving password: " + ex.getMessage());
        }
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

    /**showInformationAlert shows an information-type alert (A blue circle icon) to the user
     * This method specifically is used to show an action has been completed successfully
     * @param title: The header of the alert
     * @param content: The content of the alert box
     */
    private void showInformationAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void switchToForgotPassword() {
        tabPane.getSelectionModel().select(forgotPasswordTab);
    }

    /**Once the user is confirmed, and the UserEntity is made, sets the stage for the next
     * scene, populating it with the newly created UserEntity's lists.
     * @param currentUser: UserEntity of the logging-in user
     * @throws IOException
     */
    private void switchToLandingPage(UserEntity currentUser) throws IOException {
        Stage stage = (Stage) tabPane.getScene().getWindow();
        stage.close();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Landing_Page.fxml"));
        Parent root = loader.load();

        LandingPageController landingPageController = loader.getController();
        landingPageController.setCurrentUser(currentUser);

        Stage newStage = new Stage();
        newStage.setScene(new Scene(root));
        newStage.show();
    }

    @FXML
    public void switchToLogin() {
        tabPane.getSelectionModel().select(loginTab);
    }

    //Series of FXML-tagged action methods for various action fields
    @FXML
    public void switchToRegister() {
        tabPane.getSelectionModel().select(registerTab);
    }

    /**Actual method to verify that all of the information is filled out correctly, the
     * email does not exist in the database, and that the passwords match
     * @return True if all information is correct, shows an alert and false otherwise
     * @throws ClassNotFoundException
     */
    private boolean validateRegistrationInput() throws ClassNotFoundException {
        if (emailReg.getText().isEmpty() || streetAddress.getText().isEmpty() ||
            city.getText().isEmpty() || state.getValue() == null ||
            zip.getText().isEmpty() || firstName.getText().isEmpty() ||
            lastName.getText().isEmpty() || passwordReg.getText().isEmpty() ||
            passwordConf.getText().isEmpty()) { //checking that all fields are filled out
            showAlert("Validation Error", "All fields must be filled out");
            return false;
        }
        if (emailExists(emailReg.getText())) {
            showAlert("Validation Error", "Email already exists");
            return false;
        }
        if (!passwordReg.getText().equals(passwordConf.getText())) {
            showAlert("Validation Error", "Passwords do not match");
            return false;
        }
        return true;
    }

    /**verifyLogin takes the email and password from the login button action to create the
     * persistent currentUser UserEntity from the information in the database.
     * @param email: email from the email field on the login page
     * @param password: password from the password field on the login page
     * @return If the user exists in the database, the UserEntity, otherwise, nothing
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private UserEntity verifyLogin(String email, String password) throws SQLException, ClassNotFoundException {
        String query = "SELECT * FROM [User] WHERE [Email] = ? AND [Password] = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                currentUser = new UserEntity();
                currentUser.setEmail(rs.getString("Email"));
                currentUser.setFirstName(rs.getString("First_Name"));
                currentUser.setLastName(rs.getString("Last_Name"));
                return currentUser;
            }
            return null;
        }
    }
}
