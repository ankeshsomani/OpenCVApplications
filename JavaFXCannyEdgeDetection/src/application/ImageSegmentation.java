package application;
	
import org.opencv.core.Core;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class ImageSegmentation extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			//get fxml loader
			FXMLLoader loader=new FXMLLoader(getClass().getResource("ImageSeg.fxml"));
			BorderPane root=(BorderPane)loader.load();
			
			root.setStyle("-fx-background-color: whitesmoke;");
			
			//create a style and a scene
			Scene scene = new Scene(root,800,600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			primaryStage.setTitle("Image Segmentation");
			primaryStage.setScene(scene);
			
			primaryStage.show();
			
			ImageSegController1 controller=loader.getController();
			controller.init();
			
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>(){

				@Override
				public void handle(WindowEvent event) {
					controller.setClosed();
					
				}
				
			});
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// load the native OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		launch(args);
	}
}
