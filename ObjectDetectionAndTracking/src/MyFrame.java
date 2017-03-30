import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

public class MyFrame {
	JFrame frame;
	JLabel vidPanel;
	int xLocation;
	int yLocation;
	int IMAX, JMAX;
	
	public MyFrame(String windowTitle,int xLocation,int yLocation){
		frame=new JFrame(windowTitle);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		vidPanel=new JLabel();
		frame.setContentPane(vidPanel);
		frame.setLocation(xLocation, yLocation);
		frame.setVisible(true);
	}
	
	public void paintFrame(Mat frame){
		ImageIcon image2 = new ImageIcon(Mat2bufferedImage(frame));
		vidPanel.setIcon(image2);
		vidPanel.repaint();
	}
	public void setFrameSize(int IMAX, int JMAX){
		this.IMAX=IMAX;
		this.JMAX=JMAX;
	}
	private static BufferedImage Mat2bufferedImage(Mat image) {
		MatOfByte bytemat = new MatOfByte();
		Imgcodecs.imencode(".jpg", image, bytemat);
		byte[] bytes = bytemat.toArray();
		InputStream in = new ByteArrayInputStream(bytes);
		BufferedImage img = null;
		try {
			img = ImageIO.read(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return img;
	}
}

