package application;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("FirstJFX.fxml"));
		BorderPane rootElement  = (BorderPane) loader.load();
		// create and style a scene
		Scene scene = new Scene(rootElement, 800, 600);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		primaryStage.setTitle("JavaFX meets OpenCV");
		primaryStage.setScene(scene);
		// show the GUI
		primaryStage.show();
		
		FXController controller = loader.getController();
		primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we)
			{
				controller.setClosed();
			}
		}));
		
	}
	
	
	public static void main(String[] args) {
		// load the native OpenCV library
				System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		    launch(args);
	}
}
