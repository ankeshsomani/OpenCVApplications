import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;

/**
 *
 * @author ankeshs
 */

public class CarDetectionSimple{
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	static int IMAX, JMAX;
	static Mat source = null;
	static MyFrame origVideoFrame = new MyFrame("CAR VIDEO", 0, 0);
	//static MyFrame backgroundSubtractedFrame = new MyFrame("BACKGROUND SUBTRACTION",
	//		Toolkit.getDefaultToolkit().getScreenSize().width / 3, 500);
	//static MyFrame convexHullsFrame = new MyFrame("CONVEX UHLLS",
	//		Toolkit.getDefaultToolkit().getScreenSize().width / 2 + 10, 500);
	//static MyFrame trackedBlobsFrame = new MyFrame("TRACKED BLOBS",
	//		Toolkit.getDefaultToolkit().getScreenSize().width / 4 + 10, 500);
	static boolean firstFrame;
	
	static Point[] crossingLine=new Point[2];
	static int horizontalLinePosition;
	static int carCount = 0,myCount=0;;
	static int frameCount = 1;
	static List<Blob> existingBlobs = new ArrayList<Blob>();
	static VideoCapture capture ;
	static List<Blob> currentFrameBlobs = new ArrayList<Blob>();
	static BackgroundSubtractorMOG2 mBGSub = Video.createBackgroundSubtractorMOG2();
	static Mat actualFrame,backgroundSubtractedFrame;
	static List<MatOfPoint> contours ;
	static String videoFilePath="CarsDrivingUnderBridge.mp4";
	public static void main(String[] args) throws Exception {
		actualFrame = new Mat();

		capture = new VideoCapture();
		while (!capture.isOpened()) {
			System.out.println("Can not open Camera, try it later.");
			capture.open(videoFilePath);
		}

		capture.read(actualFrame);
		source = actualFrame.clone();

		IMAX = actualFrame.height();
		JMAX = actualFrame.width();
		horizontalLinePosition = (int)Math.round((double)actualFrame.rows() * 0.35);
		
		
		initializeCrossingLine();
		origVideoFrame.setFrameSize(JMAX + 17, IMAX + 40);
		//backgroundSubtractedFrame.setFrameSize(JMAX + 17, IMAX + 40);
	//	convexHullsFrame.setFrameSize(JMAX + 17, IMAX + 40);
		//trackedBlobsFrame.setFrameSize(JMAX + 17, IMAX + 40);
		Mat outbox = new Mat();
		backgroundSubtractedFrame = new Mat(outbox.size(), CvType.CV_8UC1);
		backgroundSubtractedFrame = outbox.clone();
		
		firstFrame = true;
		while (capture.isOpened()) {
			
			backgroundSubtractedFrame = new Mat(actualFrame.size(), CvType.CV_8UC1);
			
			startProcessing();

			if (!capture.read(actualFrame))
				break;
			source = actualFrame.clone();
			firstFrame = false;

			frameCount++;
			
		}
	}

	private static void initializeCrossingLine() {
		Point point1=new Point();
		
		point1.x = 0;
		point1.y = horizontalLinePosition;

		Point point2=new Point();
		point2.x = actualFrame.cols() - 1;
		point2.y = horizontalLinePosition;
		crossingLine[0]=point1;
		crossingLine[1]=point2;
		
	}

	protected static void startProcessing() throws Exception {

		//Do background removal
		mBGSub.apply(actualFrame, backgroundSubtractedFrame, 0.01);
	
		//Apply morphological operations
		applyMorphology();
	

		//Apply thresholding
		applyThresholding();

		//backgroundSubtractedFrame.paintFrame(mFGMask);
				
		findAndDrawContours();

	
		findAndDrawConvexHulls();
		
		
	
		//Start object tracking
		if (firstFrame) {
			for (Blob blob : currentFrameBlobs) {
				existingBlobs.add(blob);
			}
			
		} else {
			matchCurrentFrameBlobsToExistingBlobs();
		}
		
		
		//Mat trackedBlobsImg=trackAndDrawContours(backgroundSubtractedFrame.size(),existingBlobs,"imgBlobs");
		//trackedBlobsFrame.paintFrame(trackedBlobsImg);
		
		Mat sourceCopy=source.clone();
		drawBlobInfoOnImage( sourceCopy);
		
		
		//check if tracked objects crossed the line
		if(blobsCrossedTheLine()){	
			//System.out.println("car crossed");
			
			Imgproc.line(sourceCopy, crossingLine[0], crossingLine[1], Constants.SCALAR_GREEN, 2);
		}
		else{
			Imgproc.line(sourceCopy, crossingLine[0], crossingLine[1], Constants.SCALAR_RED, 2);
		}
		
		
		drawCarCountOnImage(sourceCopy);
		origVideoFrame.paintFrame(sourceCopy);
		currentFrameBlobs.clear();

      
		
	}
	
	
	

	private static void findAndDrawConvexHulls() {
		Mat imgThreshCopy = backgroundSubtractedFrame.clone();
		List<MatOfInt> convexHulls = new ArrayList<MatOfInt>(contours.size());
		;
		for (int i = 0; i < contours.size(); i++) {
			convexHulls.add(new MatOfInt());
		}

		if (contours.size() > 0) {
			for (int i = 0; i < contours.size(); i++) {
				Imgproc.convexHull(contours.get(i), convexHulls.get(i));
			}
		}

		List<MatOfPoint> convexHullsPoint = new ArrayList<MatOfPoint>();
		if (convexHulls.size() > 0) {
			for (int i = 0; i < convexHulls.size(); i++) {
				MatOfPoint hull = convertIndexesToPoints(contours.get(i), convexHulls.get(i));
				convexHullsPoint.add(hull);
				Blob possibleBlob = new Blob(hull);
				
				if (possibleBlob.boundingRect.area() > 400 && possibleBlob.dblAspectRatio > 0.2
						&& possibleBlob.dblAspectRatio < 4.0 && possibleBlob.boundingRect.width > 30
						&& possibleBlob.boundingRect.height > 30 && possibleBlob.dblDiagonalSize > 60.0
						&& (Imgproc.contourArea(possibleBlob.contours)/(possibleBlob.boundingRect.area())>0.50)
						) {
					
					currentFrameBlobs.add(possibleBlob);
				}
			}
		}
		Mat imgConvexHulls = new Mat(imgThreshCopy.size(), CvType.CV_8UC3, Constants.SCALAR_BLACK);

		convexHullsPoint.clear();
		for (Blob blob : currentFrameBlobs) {
			convexHullsPoint.add(blob.contours);
		}

		Imgproc.drawContours(imgConvexHulls, convexHullsPoint, -1, Constants.SCALAR_WHITE, -1);
		//convexHullsFrame.paintFrame(imgConvexHulls);
		
	}

	private static void findAndDrawContours() {
		Mat hierarchy = new Mat();
		contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(backgroundSubtractedFrame, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		Mat imgContours = new Mat(backgroundSubtractedFrame.size(), Imgproc.RETR_EXTERNAL, Constants.SCALAR_BLACK);
		Imgproc.drawContours(imgContours, contours, -1, Constants.SCALAR_WHITE, -1);
		
	}

	private static void applyThresholding() {
		Imgproc.threshold(backgroundSubtractedFrame, backgroundSubtractedFrame, 127, 255, Imgproc.THRESH_BINARY);
		
	}

	private static void applyMorphology() {
		Mat openElem = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5), new Point(2, 2));
		Mat closeElem = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7, 7), new Point(3, 3));

		Imgproc.morphologyEx(backgroundSubtractedFrame, backgroundSubtractedFrame, Imgproc.MORPH_OPEN, openElem);
		Imgproc.morphologyEx(backgroundSubtractedFrame, backgroundSubtractedFrame, Imgproc.MORPH_CLOSE, closeElem);
		Imgproc.blur(backgroundSubtractedFrame, backgroundSubtractedFrame, new Size(15, 15), new Point(-1, -1));

		
	}

	private static void matchCurrentFrameBlobsToExistingBlobs() throws Exception {

		for (Blob existingBlob : existingBlobs) {
			existingBlob.blnCurrentMatchFoundOrNewBlob = false;
			existingBlob.predictNextPosition();
		}
		
		for (Blob currentFrameBlob : currentFrameBlobs) {

			
			int intIndexOfLeastDistance = 0;
			double dblLeastDistance = 100000.0;
			//System.out.println("existingBlobs.size()" +existingBlobs.size()); 
			for (int i = 0; i < existingBlobs.size(); i++) {
				//System.out.println(currentFrameBlob.myId);
				if (existingBlobs.get(i).blnStillBeingTracked == true) {

					double dblDistance = distanceBetweenPoints(
							currentFrameBlob.centerPositions.get(currentFrameBlob.centerPositions.size() - 1),
							existingBlobs.get(i).predictedNextPosition);

					
					if (dblDistance < dblLeastDistance) {
						dblLeastDistance = dblDistance;
						intIndexOfLeastDistance = i;
					}
					
				}
				
			}
			if (dblLeastDistance <  currentFrameBlob.dblDiagonalSize * 0.5) {
				//System.out.println("added o existig");
				addBlobToExistingBlobs(currentFrameBlob,  intIndexOfLeastDistance);
			} else {
				addNewBlob(currentFrameBlob);
			}

		}

		for (Blob existingBlob : existingBlobs) {

			if (existingBlob.blnCurrentMatchFoundOrNewBlob == false) {
				existingBlob.intNumOfConsecutiveFramesWithoutAMatch++;
			}

			if (existingBlob.intNumOfConsecutiveFramesWithoutAMatch >= 5) {
				existingBlob.blnStillBeingTracked = false;
			}

		}

	}

	static void addBlobToExistingBlobs(Blob currentFrameBlob, int intIndex) {

		//System.out.println("here in addBlobToExistingBlobs");
		existingBlobs.get(intIndex).contours = currentFrameBlob.contours;
		existingBlobs.get(intIndex).boundingRect = currentFrameBlob.boundingRect;

		existingBlobs.get(intIndex).centerPositions
				.add(currentFrameBlob.centerPositions.get(currentFrameBlob.centerPositions.size() - 1));

		
		existingBlobs.get(intIndex).dblDiagonalSize = currentFrameBlob.dblDiagonalSize;
		existingBlobs.get(intIndex).dblAspectRatio = currentFrameBlob.dblAspectRatio;

		existingBlobs.get(intIndex).blnStillBeingTracked = true;
		existingBlobs.get(intIndex).blnCurrentMatchFoundOrNewBlob = true;
	}

	static void addNewBlob(Blob currentFrameBlob) {
		myCount++;
		currentFrameBlob.myId=myCount;
		currentFrameBlob.blnCurrentMatchFoundOrNewBlob = true;
		existingBlobs.add(currentFrameBlob);
	}

	public static double distanceBetweenPoints(Point point1, Point point2) throws Exception {

		if(point1==null || point2==null){
			throw new Exception("Points are empty");
		}

		int intX = (int) Math.abs(point1.x - point2.x);
		int intY = (int) Math.abs(point1.y - point2.y);

		return (Math.sqrt(Math.pow(intX, 2) + Math.pow(intY, 2)));
	}

	public static MatOfPoint convertIndexesToPoints(MatOfPoint contour, MatOfInt indexes) {
		int[] arrIndex = indexes.toArray();
		Point[] arrContour = contour.toArray();

		Point[] arrPoints = new Point[arrIndex.length];
		for (int i = 0; i < arrIndex.length; i++) {
			arrPoints[i] = arrContour[arrIndex[i]];
		}
		MatOfPoint hull = new MatOfPoint();
		hull.fromArray(arrPoints);
		return hull;
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	static Mat trackAndDrawContours(Size imageSize, List<Blob> blobs, String strImageName) {

		Mat image = new Mat(imageSize, CvType.CV_8UC3, Constants.SCALAR_BLACK);

		List<MatOfPoint> contours=new ArrayList<MatOfPoint>();

		for (Blob blob : blobs) {
			if (blob.blnStillBeingTracked == true) {
				contours.add(blob.contours);
			}
		}

		Imgproc.drawContours(image, contours, -1, Constants.SCALAR_WHITE, -1);
		return image;
		
	}

	static boolean blobsCrossedTheLine() {
		boolean blnAtLeastOneBlobCrossedTheLine = false;

		for (Blob blob : existingBlobs) {
			//System.out.println("m here "+(blob.blnStillBeingTracked) +"***"+(blob.centerPositions.size()));
			if (blob.blnStillBeingTracked == true && blob.centerPositions.size() >= 2) {
				int prevFrameIndex = (int) blob.centerPositions.size() - 2;
				int currFrameIndex = (int) blob.centerPositions.size() - 1;

				
				boolean condition1=(blob.centerPositions.get(prevFrameIndex).y > horizontalLinePosition);
				boolean condition2=(blob.centerPositions.get(currFrameIndex).y <= horizontalLinePosition);
				//System.out.println("here "+condition1 +"**"+condition2);
				if(condition1 && condition2) {
					carCount++;
					blnAtLeastOneBlobCrossedTheLine = true;
				}
			}

		}
		return blnAtLeastOneBlobCrossedTheLine;
	}

	static void drawBlobInfoOnImage( Mat imgFrame2Copy) {

		for (int i = 0; i < existingBlobs.size(); i++) {

			if (existingBlobs.get(i).blnStillBeingTracked == true) {
				Rect rect=existingBlobs.get(i).boundingRect;
				
				Imgproc.rectangle(imgFrame2Copy,rect.tl(),rect.br(), Constants.SCALAR_RED, 2);

				int intFontFace = Core.FONT_HERSHEY_SIMPLEX;
				double dblFontScale = existingBlobs.get(i).dblDiagonalSize / 60.0;
				int intFontThickness = (int) Math.round(dblFontScale * 1.0);

				Imgproc.putText(imgFrame2Copy, Integer.toString(existingBlobs.get(i).myId),
						existingBlobs.get(i).centerPositions.get(existingBlobs.get(i).centerPositions.size() - 1), intFontFace,
						dblFontScale, Constants.SCALAR_GREEN, intFontThickness);
			}
		}
	}

	static void drawCarCountOnImage( Mat imgFrame2Copy) {

		int intFontFace = Core.FONT_HERSHEY_SIMPLEX;
		double dblFontScale = (imgFrame2Copy.rows() * imgFrame2Copy.cols()) / 300000.0;
		int intFontThickness = (int) Math.round(dblFontScale * 1.5);
		int tempArrray[] = null;

		Size textSize = Imgproc.getTextSize(Integer.toString(carCount), intFontFace, dblFontScale, intFontThickness, tempArrray);

		Point ptTextBottomLeftPosition = new Point();

		ptTextBottomLeftPosition.x = imgFrame2Copy.cols() - 1 - (int) ((double) textSize.width * 1.25);
		ptTextBottomLeftPosition.y = (int) ((double) textSize.height * 1.25);

		Imgproc.putText(imgFrame2Copy, Integer.toString(carCount), ptTextBottomLeftPosition, intFontFace, dblFontScale,
				Constants.SCALAR_GREEN, intFontThickness);

	}

}
