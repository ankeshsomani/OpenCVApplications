package application;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import Utils.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
public class FXController {
	// the FXML button
		@FXML
		private Button start_btn;
		// the FXML image view
		@FXML
		private ImageView currentFrame;
		
		// a timer for acquiring the video stream
		private ScheduledExecutorService timer;
		// the OpenCV object that realizes the video capture
		private VideoCapture capture = new VideoCapture();
		// a flag to change the button behavior
		private boolean cameraActive = false;
		// the id of the camera to be used
		private static int cameraId = 0;
		
		@FXML
		protected void startCamera(ActionEvent event)
		{
			if (!this.cameraActive)
			{
				
				// start the video capture
			//	this.capture.open(cameraId);
				System.out.println("before opening");
				this.capture.open("D:\\workspace\\March_2017\\CarsDrivingUnderBridge.mp4");
				System.out.println("after opening");
				if(!this.capture.isOpened()){
					System.err.println("Not able to open the vdieo file");
				}
				// is the video stream available?
				if (this.capture.isOpened())
				{
					//System.out.println("here");
					this.cameraActive = true;
					
					// grab a frame every 33 ms (30 frames/sec)
					Runnable frameGrabber = new Runnable() {
						
						@Override
						public void run()
						{	
							// effectively grab and process a single frame
							Mat frame = grabFrame();
							// convert and show the frame
							Image imageToShow = Utils.mat2Image(frame);
							updateImageView(currentFrame, imageToShow);
						}
					};
					
					this.timer = Executors.newSingleThreadScheduledExecutor();
					this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
					
					// update the button content
					this.start_btn.setText("Stop Camera");
				}
				
			}
			else
			{
				// the camera is not active at this point
				this.cameraActive = false;
				// update again the button content	
				this.start_btn.setText("Start Camera");
				
				// stop the timer
				this.stopAcquisition();
			}
		}
		/**
		 * Get a frame from the opened video stream (if any)
		 *
		 * @return the {@link Mat} to show
		 */
		private Mat grabFrame()
		{
			// init everything
			Mat frame = new Mat();
			
			// check if the capture is open
			if (this.capture.isOpened())
			{
				try
				{
					// read the current frame
					this.capture.read(frame);
					
					// if the frame is not empty, process it
					if (!frame.empty())
					{
						Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
					}
					
				}
				catch (Exception e)
				{
					// log the error
					System.err.println("Exception during the image elaboration: " + e);
				}
			}
			else{
				stopAcquisition();
			}
			
			return frame;
		}
		
		/**
		 * Stop the acquisition from the camera and release all the resources
		 */
		private void stopAcquisition()
		{
			if (this.timer!=null && !this.timer.isShutdown())
			{
				try
				{
					// stop the timer
					this.timer.shutdown();
					this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
				}
				catch (InterruptedException e)
				{
					// log any exception
					System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
				}
			}
			
			if (this.capture.isOpened())
			{
				// release the camera
				this.capture.release();
			}
		}
		
		/**
		 * Update the {@link ImageView} in the JavaFX main thread
		 * 
		 * @param view
		 *            the {@link ImageView} to update
		 * @param image
		 *            the {@link Image} to show
		 */
		private void updateImageView(ImageView view, Image image)
		{
			Utils.onFXThread(view.imageProperty(), image);
		}
		
		/**
		 * On application close, stop the acquisition from the camera
		 */
		protected void setClosed()
		{
			this.stopAcquisition();
		}
		
}
