package tests_universal_gift_registry;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.TextInputControlMatchers.hasText;

public class LoginControllerTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        Parent mainNode = FXMLLoader.load(LoginControllerTest.class.getResource("Login.fxml"));
        stage.setScene(new Scene(mainNode));
        stage.show();
        stage.toFront();
    }


    @Test
    public void testSwitchToRegisterTab() {
        clickOn("Register");
        verifyThat("#registerTab", isVisible());
    }
    
    @Test
    public void testRegisterInputFailure() {
    	clickOn("Register Account");
    	verifyThat("All fields must be filled out", isVisible());
    	clickOn("OK");
    	
    }

    @Test
    public void testRegisterInputValidation() {
        clickOn("#firstName").write("John");
        clickOn("#lastName").write("Doe");
        clickOn("#emailReg").write("johndoe@example.com");
        clickOn("#passwordReg").write("ValidPassword123!");
        clickOn("#passwordConf").write("ValidPassword123!");
        clickOn("#agreeReg").clickOn();
        clickOn("Register Account");
        verifyThat("#Set_Security_Questions.fxml", isVisible());
    }
    
    /*@Test
    public void testSwitchToLoginTab() {
        clickOn("Log In");
        verifyThat("#loginTab", isVisible());
    }

    @Test
    public void testValidLogin() {
        // Assume these actions navigate and perform a valid login
        clickOn("#emailLogin").write("valid@example.com");
        clickOn("#passwordLogin").write("validPass123");
        clickOn("#agreeLogin").clickOn();
        clickOn("Log In");

        // Verify successful login, e.g., by checking if a new scene is loaded or a session variable is set
        // This is more complex to verify without specific outcomes in the controller, such as opening a new window or scene
    }

    @Test
    public void testInvalidLogin() {
        // Similar to valid login but enter invalid credentials
        clickOn("#emailLogin").write("invalid@example.com");
        clickOn("#passwordLogin").write("invalid");
        clickOn("#agreeLogin").clickOn();
        clickOn("Log In");

        // Verify login failure, perhaps by checking for an error message
        // verifyThat("#errorLabel", hasText("Incorrect email or password."));
    }


    // Additional tests can be added for forgot password functionality and specific field validations*/

}
