package application;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import Utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageSegController1 {

	// FXML buttons
	@FXML
	private Button cameraButton;
	// the FXML area for showing the current frame
	@FXML
	private ImageView originalFrame;
	// checkbox for enabling/disabling Canny

	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;
	// the OpenCV object that performs the video capture
	private VideoCapture capture = new VideoCapture();
	// a flag to change the button behavior
	private boolean cameraActive;

	Mat oldFrame;

	protected void init() {
		System.out.println("in init method");
	}

	/**
	 * The action triggered by pushing the button on the GUI
	 */
	@FXML
	protected void startCamera() {
		// set a fixed width for the frame
				originalFrame.setFitWidth(380);
				// preserve image ratio
				originalFrame.setPreserveRatio(true);
			
				if (!this.cameraActive) {

					// start the video capture
					this.capture.open(0);
					if (this.capture.isOpened()) {
						// start the video capture
						this.capture.open(0);

						// is the video stream available?
						if (this.capture.isOpened()) {
							this.cameraActive = true;

							// grab a frame every 33 ms (30 frames/sec)
							Runnable frameGrabber = new Runnable() {

								@Override
								public void run() {
									// effectively grab and process a single frame
									Mat frame = grabFrame();
									// convert and show the frame
									Image imageToShow = Utils.mat2Image(frame);
									updateImageView(originalFrame, imageToShow);
								}
							};
					}
					else{
						// log the error
						System.err.println("Failed to open the camera connection...");
					}
					
				}
				else{
					this.cameraActive = false;
					// update again the button content
					this.cameraButton.setText("Start Camera");
					// enable setting checkboxes
				   

					// stop the timer
					this.stopAcquisition();
				}
	}
	}

	/**
	 * Get a frame from the opened video stream (if any)
	 * 
	 * @return the {@link Image} to show
	 */
	private Mat grabFrame() {
		Mat frame = new Mat();

		// check if the capture is open
		if (this.capture.isOpened()) {
			try {
				// read the current frame
				this.capture.read(frame);

				// if the frame is not empty, process it
				if (!frame.empty()) {
					// do something with frame

				}

			} catch (Exception e) {
				// log the (full) error
				System.err.print("Exception in the image elaboration...");
				e.printStackTrace();
			}
		}

		return frame;
	}

	private void updateImageView(ImageView view, Image image) {
		Utils.onFXThread(view.imageProperty(), image);
	}

	/**
	 * Stop the acquisition from the camera and release all the resources
	 */
	private void stopAcquisition() {
		if (this.timer != null && !this.timer.isShutdown()) {
			try {
				// stop the timer
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// log any exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}

		if (this.capture.isOpened()) {
			// release the camera
			this.capture.release();
		}
	}
}
