import java.util.ArrayList;
import java.util.List;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class Blob {

	MatOfPoint contours;
	Rect boundingRect;
	List<Point> centerPositions;
	double dblDiagonalSize;
	int myId;
	double dblAspectRatio;
	boolean blnCurrentMatchFoundOrNewBlob;
	boolean blnStillBeingTracked;
	int intNumOfConsecutiveFramesWithoutAMatch;
	Point predictedNextPosition;

	public Blob(MatOfPoint contours,int id) {
		myId=id;
		centerPositions= new ArrayList<Point>();
		this.contours = contours;
		boundingRect = Imgproc.boundingRect(contours);
		Point centerPosition = new Point();
		centerPosition.x = (boundingRect.x + boundingRect.x + boundingRect.width) / 2;
		centerPosition.y = (boundingRect.y + boundingRect.y + boundingRect.height) / 2;
		centerPositions.add(centerPosition);
		dblDiagonalSize = Math.sqrt(Math.pow(boundingRect.width, 2) + Math.pow(boundingRect.height, 2));

		dblAspectRatio = (float) boundingRect.width / (float) boundingRect.height;
		blnStillBeingTracked = true;
		blnCurrentMatchFoundOrNewBlob = true;
		intNumOfConsecutiveFramesWithoutAMatch = 0;
		predictedNextPosition=new Point();
	}

	public void predictNextPosition() {
		
		int numPositions = (int) centerPositions.size();
		if (numPositions == 1) {

			predictedNextPosition.x = centerPositions.get(0).x;
			predictedNextPosition.y = centerPositions.get(0).y;

		} else if (numPositions == 2) {

			int deltaX = (int) (centerPositions.get(1).x - centerPositions.get(0).x);
			int deltaY = (int) (centerPositions.get(1).y - centerPositions.get(0).y);

			predictedNextPosition.x = centerPositions.get(numPositions - 1).x + deltaX;
			predictedNextPosition.y = centerPositions.get(numPositions - 1).y + deltaY;

		} else if (numPositions == 3) {

			int sumOfXChanges = (int) (((centerPositions.get(2).x - centerPositions.get(1).x) * 2)
					+ ((centerPositions.get(1).x - centerPositions.get(0).x) * 1));

			int deltaX = (int) Math.round((float) sumOfXChanges / 3.0);

			int sumOfYChanges = (int) (((centerPositions.get(2).y - centerPositions.get(1).y) * 2)
					+ ((centerPositions.get(1).y - centerPositions.get(0).y) * 1));

			int deltaY = (int) Math.round((float) sumOfYChanges / 3.0);

			predictedNextPosition.x = centerPositions.get(numPositions - 1).x + deltaX;
			predictedNextPosition.y = centerPositions.get(numPositions - 1).y + deltaY;

		} else if (numPositions == 4) {

			int sumOfXChanges = (int) (((centerPositions.get(3).x - centerPositions.get(2).x) * 3)
					+ ((centerPositions.get(2).x - centerPositions.get(1).x) * 2)
					+ ((centerPositions.get(1).x - centerPositions.get(0).x) * 1));

			int deltaX = (int) Math.round((float) sumOfXChanges / 6.0);

			int sumOfYChanges = (int) (((centerPositions.get(3).y - centerPositions.get(2).y) * 3)
					+ ((centerPositions.get(2).y - centerPositions.get(1).y) * 2)
					+ ((centerPositions.get(1).y - centerPositions.get(0).y) * 1));

			int deltaY = (int) Math.round((float) sumOfYChanges / 6.0);

			predictedNextPosition.x = centerPositions.get(numPositions - 1).x + deltaX;
			predictedNextPosition.y = centerPositions.get(numPositions - 1).y + deltaY;

		} else if (numPositions >= 5) {

			int sumOfXChanges = (int) (((centerPositions.get(numPositions - 1).x
					- centerPositions.get(numPositions - 2).x) * 4)
					+ ((centerPositions.get(numPositions - 2).x - centerPositions.get(numPositions - 3).x) * 3)
					+ ((centerPositions.get(numPositions - 3).x - centerPositions.get(numPositions - 4).x) * 2)
					+ ((centerPositions.get(numPositions - 4).x - centerPositions.get(numPositions - 5).x) * 1));

			int deltaX = (int) Math.round((float) sumOfXChanges / 10.0);

			int sumOfYChanges = (int) (((centerPositions.get(numPositions - 1).y
					- centerPositions.get(numPositions - 2).y) * 4)
					+ ((centerPositions.get(numPositions - 2).y - centerPositions.get(numPositions - 3).y) * 3)
					+ ((centerPositions.get(numPositions - 3).y - centerPositions.get(numPositions - 4).y) * 2)
					+ ((centerPositions.get(numPositions - 4).y - centerPositions.get(numPositions - 5).y) * 1));

			int deltaY = (int) Math.round((float) sumOfYChanges / 10.0);

			predictedNextPosition.x = centerPositions.get(numPositions - 1).x + deltaX;
			predictedNextPosition.y = centerPositions.get(numPositions - 1).y + deltaY;

		} else {
			// should never get here
		}

	}

}
