package tests_universal_gift_registry;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

public class UniversalGiftRegistryApplicationTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Login_Register.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
        stage.toFront();
    }

    @Test
    public void testInitialScene() {
        sleep(1000);
        FxAssert.verifyThat("#usernameField", NodeMatchers.isVisible());
        FxAssert.verifyThat("#loginButton", NodeMatchers.isVisible());
    }
}
