package controllers;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCountNonZero;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvNot;
import static com.googlecode.javacv.cpp.opencv_core.cvOr;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_core.cvSet;
import static com.googlecode.javacv.cpp.opencv_core.cvSub;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_GAUSSIAN;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_MOP_BLACKHAT;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RGB2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_OTSU;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvDilate;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvErode;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvMorphologyEx;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvSmooth;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvThreshold;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class SigImgProcessingController {

	final private static int NUM_OF_BLOCKS = 96;
	final private static int LOWER_LIMIT=85;
	final private static int UPPER_LIMIT=115;
	final private static int MATCHING_PERCENTAGE=80;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	/**
	 * @param angles
	 * @return
	 */
	public static float[] calculateMeanAngle(float[]... angles) {
		int size = angles.length;
		float[] mean_angle = new float[NUM_OF_BLOCKS];
		for (int i = 0; i < NUM_OF_BLOCKS; i++) {
			float total = 0;
			for (int j = 0; j < size; j++) {
				total += angles[j][i];
			}
			mean_angle[i] = total / size;
		}
		return mean_angle;
	}

	/**
	 * @param filename
	 * @param smoothfilename
	 */
	public static void smoothing(String filename, String smoothfilename) {
		IplImage image = cvLoadImage(filename);
		if (image != null) {
			cvSmooth(image, image, CV_GAUSSIAN, 3);
			cvSaveImage(smoothfilename, image);
			cvReleaseImage(image);
		}
	}

	/**
	 * @param filename
	 * @param binaryfilename
	 */
	public static void binarization(String filename, String binaryfilename) {
		IplImage image_rgb = cvLoadImage(filename);

		// Convert RGB to GRAY
		if (image_rgb != null) {
			IplImage image_gray = cvCreateImage(cvGetSize(image_rgb),
					IPL_DEPTH_8U, 1);
			cvCvtColor(image_rgb, image_gray, CV_RGB2GRAY);

			// Convert GRAY to Binary
			if (image_gray != null) {
				IplImage image_bw = cvCreateImage(cvGetSize(image_gray),
						IPL_DEPTH_8U, 1);
				cvThreshold(image_gray, image_bw, 128, 255, CV_THRESH_BINARY
						| CV_THRESH_OTSU);

				if (image_bw != null) {
					cvSaveImage(binaryfilename, image_bw);
					cvReleaseImage(image_bw);
				}
			}
		}
	}

	/**
	 * @param binaryfilename
	 * @param thinfilename
	 */
	public static void thining(String binaryfilename, String thinfilename) {
		// load binary image
		IplImage image = cvLoadImage(binaryfilename, 0);
		cvThreshold(image, image, 127, 255, 0);

		IplImage image_morp = cvCreateImage(cvGetSize(image), image.depth(), 1);
		cvMorphologyEx(image, image_morp, null, null, CV_MOP_BLACKHAT, 100);
		cvThreshold(image_morp, image_morp, 127, 255, 0);

		IplImage skel = cvCreateImage(cvGetSize(image_morp),
				image_morp.depth(), 1);
		cvSet(skel, CvScalar.ZERO);

		boolean done = false;
		do {
			IplImage eroded = cvCreateImage(cvGetSize(image), image.depth(), 1);
			cvErode(image, eroded, null, 1);

			IplImage dilate = cvCreateImage(cvGetSize(image), image.depth(), 1);
			cvDilate(eroded, dilate, null, 1);

			IplImage sub = cvCreateImage(cvGetSize(image), image.depth(), 1);
			cvSub(image, dilate, sub, null);

			IplImage skelt = cvCreateImage(cvGetSize(skel), skel.depth(), 1);
			cvOr(skel, sub, skelt, null);

			image = eroded.clone();
			skel = skelt.clone();
			done = (cvCountNonZero(image) == 0);
		} while (!done);

		IplImage not = cvCreateImage(cvGetSize(skel), skel.depth(), 1);
		cvNot(skel, not);
		cvSaveImage(thinfilename, not);
		cvReleaseImage(skel);
	}

	/**
	 * @param thinFileName
	 * @param sizeNormalizeFileName
	 */
	public static void sizeNormalization(String thinFileName,
			String sizeNormalizeFileName) {
		IplImage image = cvLoadImage(thinFileName, 0);
		BufferedImage bufferImage = image.getBufferedImage();

		int[][] a = readBinaryImage(bufferImage);
		cvReleaseImage(image);

		int[][] b = sizeNormalization(a);

		int height = b.length;
		int width = b[0].length;

		BufferedImage binarized = new BufferedImage(b[0].length, b.length,
				bufferImage.getType());

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (b[i][j] == 0)
					binarized.setRGB(j, i, Color.WHITE.getRGB());
				else
					binarized.setRGB(j, i, Color.BLACK.getRGB());
			}
		}

		IplImage newimage = IplImage.createFrom(binarized);

		cvSaveImage(sizeNormalizeFileName, newimage);
		cvReleaseImage(newimage);
	}

	/**
	 * @param sizeNormalizeFileName
	 * @return
	 */
	public static float[] calculateAngles(String sizeNormalizeFileName) {
		IplImage image = cvLoadImage(sizeNormalizeFileName, 0);
		BufferedImage bufferedImage = image.getBufferedImage();
		int[][] imageArray = readBinaryImage(bufferedImage);
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		cvReleaseImage(image);
		float[] angles = getBlockAngles(imageArray, width, height);
		return angles;
	}

	/**
	 * Length of source and destination should be same. Matching criteria =>
	 * Should be in between 85% to 115%
	 * 
	 * @param src
	 * @param dest
	 */
	public static boolean compareBlockAngles(final float[] src,
			final float[] dest) {
		boolean result = false;
		int count = 0;
		for (int i = 0; i < src.length; i++) {
			float lower = (src[i] * LOWER_LIMIT) / 100;
			float upper = (src[i] * UPPER_LIMIT) / 100;
			if (dest[i] >= lower && dest[i] <= upper) {
				count++;
			}
		}

		if (count >=MATCHING_PERCENTAGE) {
			result = true;
		}

		return result;
	}

	/* Size Normalization Utility Methods */

	private static int[][] readBinaryImage(BufferedImage bufferImage) {
		int[][] a = new int[bufferImage.getHeight()][bufferImage.getWidth()];
		for (int y = 0; y < bufferImage.getHeight(); y++) {
			if (y != 0)
				for (int x = 0; x < bufferImage.getWidth(); x++) {
					Color color = new Color(bufferImage.getRGB(x, y));
					if (color.getRed() <= 10 && color.getGreen() <= 10
							&& color.getBlue() <= 10) {
						a[y][x] = 1;
					} else if (color.getRed() >= 200 && color.getGreen() >= 200
							&& color.getBlue() >= 200) {
						a[y][x] = 0;
					} else {
						a[y][x] = 1;
					}
				}
		}
		return a;
	}

	private static int[][] sizeNormalization(int[][] a) {
		// iterate for rows
		List<Integer> rows = new ArrayList<Integer>();
		for (int i = 0; i < a.length; i++) {
			int count = 0;
			for (int j = 0; j < a[0].length; j++) {
				if (a[i][j] == 1)
					continue;
				count++;
			}
			if (count == a[0].length) {
				rows.add(i);
			}
		}

		// iterate for columns
		List<Integer> cols = new ArrayList<Integer>();
		for (int i = 0; i < a[0].length; i++) {
			int count = 0;
			for (int j = 0; j < a.length; j++) {
				if (a[j][i] == 1)
					continue;
				count++;
			}
			if (count == a.length) {
				cols.add(i);
			}
		}

		int height = a.length - rows.size();
		int width = a[0].length - cols.size();
		// for array after size normalization;

		int[][] b = new int[height][width];
		for (int i = 0, x = 0; i < a.length; i++) {
			if (rows.contains(i))
				continue;
			for (int j = 0, y = 0; j < a[0].length; j++) {
				if (cols.contains(j))
					continue;
				b[x][y] = a[i][j];
				y++;
			}
			x++;
		}
		// display(b);
		return b;
	}

	/* Feature Extraction Utility Methods */

	/**
	 * 
	 * @param image
	 * @param width
	 * @param height
	 * @return
	 */
	private static float[] getBlockAngles(final int[][] image, int width,
			int height) {
		int width_step = width / 8;
		int[] eight_blocks = splitIntoEightBlocks(width);
		int[] four_rows = splitIntoFourRows(height);
		List<Block> blocks = new ArrayList<Block>();

		// System.out.println("Eight Blocks ...");
		for (int i = 0; i < 16; i += 2) {
			// System.out.print(eight_blocks[i] + " " + eight_blocks[i+1] +
			// " ");
			int[] three_cols = splitIntoThreeCols(eight_blocks[i],
					eight_blocks[i + 1], width_step, width);
			// Divide single block into 3 columns
			for (int j = 0; j < 8; j += 2) {
				int starty = four_rows[j];
				int endy = four_rows[j + 1];
				int yheight = endy - starty;
				for (int k = 0; k < 6; k += 2) {
					int startx = three_cols[k];
					int endx = three_cols[k + 1];
					int xwidth = endx - startx;
					// System.out.print(col_index[ii] + " " + col_index[ii+1]);
					Block block = new Block(startx, starty, xwidth, yheight);
					blocks.add(block);
				}
			}
		}

		// Traverse each block
		float[] angles = new float[96];
		int i = 0;
		for (Block block : blocks) {
			block.angle = calculateBlockAngle(image, block);
			angles[i] = block.angle;
			i++;
			// System.out.println(block);
		}

		return angles;
	}

	/**
	 * 
	 * @param image
	 * @param block
	 * @return
	 */
	private static float calculateBlockAngle(final int[][] image,
			final Block block) {
		int totalPixelCount = 0;
		float blackPixelCount = 0;
		for (int i = block.starty, y = 0; i <= (block.starty + block.height); i++, y++) {
			for (int j = block.startx, x = 0; j <= (block.startx + block.width); j++, x++) {
				// System.out.println(j + " <> " + i);
				if (image[i][j] == 1) {
					blackPixelCount += Math.sqrt((x * x) + (y * y));
					// blackPixelCount++;
				}
				totalPixelCount++;
			}
		}
		return blackPixelCount / totalPixelCount;
	}

	/**
	 * 
	 * @param width
	 * @return
	 */
	private static int[] splitIntoEightBlocks(final int width) {
		int[] eight_blocks = new int[16];
		int j = 0;
		int step = width / 8;
		for (int i = 0, index = 0; i < 8; i++, index += 2) {
			eight_blocks[index] = j;
			if (i == 7) {
				eight_blocks[index + 1] = width - 1;
			} else {
				eight_blocks[index + 1] = j + step - 1;
			}
			j += step;
		}
		return eight_blocks;
	}

	/**
	 * Divide Block into three columns
	 * 
	 * @param xstart
	 * @param xend
	 * @param step
	 * @param width
	 * @return
	 */
	private static int[] splitIntoThreeCols(final int xstart, final int xend,
			final int step, final int width) {
		int[] three_cols = new int[6];
		int j = xstart;
		int colstep = step / 3;
		for (int i = 0, index = 0; i < 3; i++, index += 2) {
			three_cols[index] = j;
			if (i == 2) {
				three_cols[index + 1] = xend - 1;
			} else {
				three_cols[index + 1] = j + colstep - 1;
			}
			j += colstep;
		}
		return three_cols;
	}

	/**
	 * Divide image height into four rows.
	 * 
	 * @param y
	 * @return
	 */
	private static int[] splitIntoFourRows(final int y) {
		int[] four_rows = new int[8];
		int j = 0;
		int rowstep = y / 4;
		for (int i = 0, index = 0; i < 4; i++, index += 2) {
			four_rows[index] = j;
			if (i == 3) {
				four_rows[index + 1] = y - 1;
			} else {
				four_rows[index + 1] = j + rowstep - 1;
			}
			j += rowstep;
		}
		return four_rows;
	}

}

class Block {
	public int startx;
	public int starty;
	public int width;
	public int height;
	public float angle;

	public Block() {
	}

	public Block(final int sx, final int sy, final int wdth, final int hght) {
		this.startx = sx;
		this.starty = sy;
		this.width = wdth;
		this.height = hght;
	}

	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("|");
		strBuilder.append(startx);
		strBuilder.append("|");
		strBuilder.append(starty);
		strBuilder.append("|");
		strBuilder.append(width);
		strBuilder.append("|");
		strBuilder.append(height);
		strBuilder.append("|");
		strBuilder.append(angle);
		strBuilder.append("|");
		return strBuilder.toString();
	}

}
