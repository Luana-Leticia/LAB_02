package LAB_02;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.gui.WaitForUserDialog;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class Region_Growing {

	private int w;
	private int h;
	private ImagePlus imp;
	private ImageProcessor ip;
	private Roi GivedRoi;
//	private int[] binPixels;
	private byte[] binPixels;
	
	public Region_Growing(ImagePlus imp) {
		// TODO Auto-generated constructor stub
		this.imp = imp;
		this.ip = imp.getProcessor();
		this.w = imp.getWidth();
		this.h = imp.getHeight();
//		this.binPixels = new int[w * h];
		this.binPixels = new byte[w * h];
		Arrays.fill(binPixels, (byte) 255);
	}

	public Roi drawROI() {
		WaitForUserDialog wait = new WaitForUserDialog(
				"Selecionar ROI correspondente ao plano de fundo (maior possível):");
		wait.show();
		Roi roi = imp.getRoi();
		this.GivedRoi = roi;
		return roi;
	}

	public ROI_Statistics calculateRoiStatistics(Roi roi) {
		ROI_Statistics roiStat = new ROI_Statistics();
		// System.out.println(roi.toString());
		roiStat.calculateROIStatistics(roi);
		return roiStat;
	}

//	public ColorProcessor createBackgroundSegmentation() {
	public ByteProcessor createBackgroundSegmentation() {
		ImagePlus binaryImp = NewImage.createByteImage("Binary Image", w, h, 1, NewImage.FILL_WHITE);
		//ColorProcessor binaryIp = binaryImp.getProcessor().convertToColorProcessor();
		ByteProcessor binaryIp = (ByteProcessor) binaryImp.getProcessor();
				
		Roi givedRoi = drawROI();
		//System.out.println(givedRoi.toString());
		ROI_Statistics roiStat = calculateRoiStatistics(givedRoi);
		// System.out.println(roiStat.toString());
		double mean = roiStat.getMean();
		double stdDev = roiStat.getStdDev();
		System.out.print("desvio padrao:"+stdDev);
		Point seed = givedRoi.getContainedPoints()[0]; // um ponto qualquer da roi, que, por consequência, é plano de
														// fundo com certeza
		int seedIndex = getIndex(seed.x, seed.y);

		ArrayList<Point> connectedPoints = new ArrayList<Point>();
		connectedPoints.add(seed);
		
		PriorityQueue<Integer> connectedIndexes = new PriorityQueue<>();
		PriorityQueue<Integer> backgroundIndexes = new PriorityQueue<>();
		connectedIndexes.add(seedIndex);
		backgroundIndexes.add(seedIndex);

		while (!connectedPoints.isEmpty()) {
			int a = connectedPoints.get(0).x;
			int b = connectedPoints.get(0).y; // (a,b)=> ponto central atual

			for (int i = a - 1; i <= a + 1; i++) {
				for (int j = b - 1; j <= b + 1; j++) {
					if ((isInside(i, j)) && (!isItself(i, j, a, b))) { // dentro da imagem e não é ele mesmo
						Point p = new Point(i, j);
						int tempIndex = getIndex(i, j);
						if ((obeysIntesityCriterion(i, j, mean, stdDev)) && (isBackGround(connectedIndexes, tempIndex))
								&& (!wasNeverVisited(backgroundIndexes, tempIndex))) {
							connectedPoints.add(p);
							markAsVisited(backgroundIndexes, tempIndex);	
						} else {
							binPixels[tempIndex] = 0;
						}
					}
				}
			}
			connectedPoints.remove(0);
		}

		binaryIp.setPixels(binPixels);
		return binaryIp;
	}

	private void markAsVisited(PriorityQueue<Integer> queue, int index) { // Pixel já visitado, classificado como background e
																	// retirado da lista iteradora => Não pode
																	// participar de outra iteração
		queue.add(index);
	}

	private boolean obeysIntesityCriterion(int x, int y, double mean, double stdDev) {
		int pix = ip.get(x, y);
		return (Math.abs((double) (pix - mean)) <= 3 * stdDev);
	}

	private boolean isBackGround(PriorityQueue<Integer> queue, int index) { // Pixel foi marcado como plano de fundo em
																		// iteração anterior e ainda não teve vizinhaça
																		// analisada
		return (!queue.contains(index));
	}

	private boolean isInside(int x, int y) { // dentro da imagem
		return (x >= 0 && x < w && y >= 0 && y < h);
	}

	private boolean isItself(int x, int y, int a, int b) { // (x,y)=> iteração atual (a,b) => ponto central
		return (x == a && y == b);
	}

	private int getIndex(int u, int v) {
		return v * w + u;
	}

	private boolean wasNeverVisited(PriorityQueue<Integer> queue, int index) { // Pixel é plano de fundo e não foi visitado em
																		// iterações anteriores
		return queue.contains(index);
	}

	public Roi getGivedRoi() {
		return GivedRoi;
	}

}