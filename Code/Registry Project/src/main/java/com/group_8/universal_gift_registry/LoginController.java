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
import java.util.ArrayList;

import com.group_8.universal_gift_registry.model.UserEntity;
import com.group_8.universal_gift_registry.util.GetConnectionUtil;
import com.group_8.universal_gift_registry.util.HashingUtils;
import com.group_8.universal_gift_registry.util.ShowAlertUtil;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**JFX Controller class for the Login/Register scene
 * Enables the user to register, login, and reset password, while creating the persistent
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
        String query = "SELECT COUNT(*) FROM [User] WHERE Email = ?";
        try (Connection conn = GetConnectionUtil.getConnection();
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
                ShowAlertUtil.showAlert("Login Error", "You must agree to the Terms and Conditions.");
                return;
            }

            UserEntity currentUser = verifyLogin(email, password);
            if (currentUser != null) {
                switchToLandingPage(currentUser);
            } else {
                ShowAlertUtil.showAlert("Login Error", "Incorrect email or password.");
            }
        } catch (Exception e) {
            ShowAlertUtil.showAlert("Database Error", "Error while logging in: " + e.getMessage());
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
            ShowAlertUtil.showAlert("Validation Error", "You must agree to the Terms and Conditions.");
            return;
        }


        String hashedPassword;
        String salt;
        String shadowHash;
        try {
            shadowHash= HashingUtils.generateStrongPasswordHash(passwordReg.getText());
            salt = shadowHash.split(":")[1];
            hashedPassword = shadowHash.split(":")[2];
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            ShowAlertUtil.showAlert("Hashing Error", "Error hashing password: " + e.getMessage());
            return;
        }
        
        ArrayList<String> hashedAnswers = showSetSecurityQuestionsScreen(salt);
        if (hashedAnswers == null || hashedAnswers.size() != 4) {
            ShowAlertUtil.showAlert("Security Questions", "You must choose and answer the security questions to complete registration.");
            return;
        }

        String query = "INSERT INTO [User] ([Email], [Street_Address], [City], [State], [Zip], [First_Name], [Last_Name], [Password], [Salt],"
        		+ "[SecurityQuestion1Code], [SecurityAnswer1], [SecurityQuestion2Code], [SecurityAnswer2]) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection conn = GetConnectionUtil.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, emailReg.getText());
                pstmt.setString(2, streetAddress.getText());
                pstmt.setString(3, city.getText());
                pstmt.setString(4, state.getValue());
                pstmt.setString(5, zip.getText());
                pstmt.setString(6, firstName.getText());
                pstmt.setString(7, lastName.getText());
                pstmt.setString(8, hashedPassword);
                pstmt.setString(9, salt);
                pstmt.setString(10, hashedAnswers.get(0));
                pstmt.setString(11, hashedAnswers.get(1));
                pstmt.setString(12, hashedAnswers.get(2));
                pstmt.setString(13, hashedAnswers.get(3));
                pstmt.executeUpdate();
                ShowAlertUtil.showInformationAlert("Registration Successful", "You are now registered");
            } catch (SQLException ex) {
                ShowAlertUtil.showAlert("Database Error", "Error inserting new user: " + ex.getMessage());
            }
        } catch (ClassNotFoundException e) {
            ShowAlertUtil.showAlert("Driver Error", "MSSQL JDBC driver not found");
        } catch (SQLException e) {
            ShowAlertUtil.showAlert("Database Error", "Error connecting to the database: " + e.getMessage());
        }
    }

    private ArrayList<String> showSetSecurityQuestionsScreen(String salt) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Set_Security_Questions.fxml"));
            Parent root = loader.load();

            SetSecurityQuestionsController secQuestionsController = loader.getController();
            secQuestionsController.setSalt(salt);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(tabPane.getScene().getWindow());
            stage.showAndWait();
            return secQuestionsController.getHashedAnswers();
        } catch (IOException e) {
            ShowAlertUtil.showAlert("UI Error", "Error loading the security questions screen: " + e.getMessage());
            return null;
        }
    }


    /**Connects to the server, verifies that the user exists, and then pulls up the security questions for the user. Then, once it has confirmed this, opens a pop-up with
     * the questions for the user to answer to confirm their identity, then allows them to update their password.
     * @throws ClassNotFoundException
     */
    private void retrievePassword() throws ClassNotFoundException {
        String email = emailForgot.getText();
        
        String query = "SELECT [SecurityQuestion1Code], [SecurityQuestion2Code] FROM [User] WHERE [Email] = ?";
        
        try (Connection conn = GetConnectionUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    openSecurityQuestionsWindow(email);
                } else {
                    ShowAlertUtil.showAlert("Forgot Password", "No account found for this email.");
                }
            }
        } catch (SQLException ex) {
            ShowAlertUtil.showAlert("Database Error", "Error checking user email: " + ex.getMessage());
        }
    }

    private void openSecurityQuestionsWindow(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Answer_Security_Question.fxml"));
            Parent root = loader.load();
            
            AnswerSecurityQuestionsController controller = loader.getController();
            controller.setUserEmail(email);
            
            Stage stage = new Stage();
            stage.setTitle("Answer Security Questions");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(emailForgot.getScene().getWindow());
            stage.show();
            
        } catch (IOException e) {
            ShowAlertUtil.showAlert("UI Error", "Error loading the security questions screen: " + e.getMessage());
        }
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
            ShowAlertUtil.showAlert("Validation Error", "All fields must be filled out");
            return false;
        }
        if (emailExists(emailReg.getText())) {
            ShowAlertUtil.showAlert("Validation Error", "Email already exists");
            return false;
        }
        if (!passwordReg.getText().equals(passwordConf.getText())) {
            ShowAlertUtil.showAlert("Validation Error", "Passwords do not match");
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
        String query = "SELECT [Salt], [Password] FROM [User] WHERE [Email] = ?";
        String storedHash = null;
        String storedSalt = null;

        try (Connection conn = GetConnectionUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    storedSalt = rs.getString("Salt");
                    storedHash = rs.getString("Password");
                } else {
                	ShowAlertUtil.showAlert("Error", "Incorrect email or password");
                    return null;
                }
            }
        }

        if (storedSalt != null) {
            String hashedInputPassword;
            try {
                hashedInputPassword = HashingUtils.hashWithExistingSalt(password, storedSalt);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                ShowAlertUtil.showAlert("Server Error", "Error recieving information from server: " + e);
                return null;
            }


            if (hashedInputPassword.equals(storedHash)) {
            	String query1 = "SELECT [Email], [Street_Address], [City], [State], [Zip], [First_Name], [Last_Name], [Salt], [Password] FROM [User] WHERE [Email] = ?";
            	try (Connection conn = GetConnectionUtil.getConnection();
            	        PreparedStatement pstmt = conn.prepareStatement(query1)) {
		            	pstmt.setString(1, email);
		                try (ResultSet rs = pstmt.executeQuery()) {
		                    if (rs.next()) {
		                    	currentUser = new UserEntity(
			                        rs.getString("Email"),
			                        rs.getString("Street_Address"),
			                        rs.getString("City"),
			                        rs.getString("State"),
			                        rs.getString("Zip"),
			                        rs.getString("First_Name"),
			                        rs.getString("Last_Name"),
			                        rs.getString("Salt"));
		                    	return currentUser;
		                    }
		                } catch (SQLException e) {
		                	ShowAlertUtil.showAlert("Error", "Server Error: " + e);
		                }
            	} catch (ClassNotFoundException e) {
            		ShowAlertUtil.showAlert("Error", "Login Error: " + e);
            	}
            }
            else {
            	ShowAlertUtil.showAlert("Error", "Incorrect email or password");
            }
        }

        return null;
    }

}
