package LAB_02;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.gui.WaitForUserDialog;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class Region_Growing {

	private int w;
	private int h;
	private ImagePlus imp;
	private ImageProcessor ip;
	private Roi GivedRoi;
	private int[] binPixels;

	public Region_Growing(ImagePlus imp) {
		// TODO Auto-generated constructor stub
		this.imp = imp;
		this.ip = imp.getProcessor();
		this.w = imp.getWidth();
		this.h = imp.getHeight();
		this.binPixels = new int[w * h];
		Arrays.fill(binPixels, 255);

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

	public ColorProcessor createBackgroundSegmentation() {

		ImagePlus binaryImp = NewImage.createRGBImage("Binary Image", w, h, 1, NewImage.FILL_WHITE);
		ColorProcessor binaryIp = (ColorProcessor) binaryImp.getProcessor();

		Roi givedRoi = drawROI();
		System.out.println(givedRoi.toString());
		ROI_Statistics roiStat = calculateRoiStatistics(givedRoi);
		// System.out.println(roiStat.toString());
		double mean = roiStat.getMean();
		double stdDev = roiStat.getStdDev();
		System.out.print("desvio padrao:"+stdDev);
		Point seed = givedRoi.getContainedPoints()[0]; // um ponto qualquer da roi, que, por consequência, é plano de
														// fundo com certeza

		ArrayList<Point> connectedPoints = new ArrayList<Point>();
		ArrayList<Point> backgroundPoints = new ArrayList<Point>();
		connectedPoints.add(seed);
		backgroundPoints.add(seed);

		while (!connectedPoints.isEmpty()) {
			int a = connectedPoints.get(0).x;
			int b = connectedPoints.get(0).y; // (a,b)=> ponto central atual

			/*for (int i = a - 1; i < a + 1; i++) {
				for (int j = b - 1; j < b + 1; j++) {
					if ((isInside(i, j)) && (!isItself(i, j, a, b))) { // dentro da imagem e não é ele mesmo
						Point p = new Point(i, j);
						if ((obeysIntesityCriterion(i, j, mean, stdDev)) && (!isBackGround(connectedPoints, p))
								&& (wasNeverVisited(backgroundPoints, p))) {
						//if ((obeysIntesityCriterion(i, j, mean, stdDev)) && (!(connectedPoints.contains(p))) 
								//&& (!(backgroundPoints.contains(p)))) { 
							connectedPoints.add(p);
							markAsVisited(backgroundPoints, p);	
							//System.out.print("n =" + connectedPoints.lenght);

						} else {
							// binaryIp.set(i, j, 0); // cor de componente
							int index = getIndex(i, j);
							binPixels[index] = 0;
						}
					}
				}
			}*/
			
			for (int i = a - 1; i < a + 1; i++) {
			for (int j = b - 1; j < b + 1; j++) {
				if ((isInside(i, j)) && (!isItself(i, j, a, b))) { // dentro da imagem e não é ele mesmo
					Point p = new Point(i, j);
					if (obeysIntesityCriterion(i, j, mean, stdDev)) {
						if ((isBackGround(connectedPoints, p)) && (!wasNeverVisited(backgroundPoints, p))) {
						} 
						else {						
							connectedPoints.add(p);
							markAsVisited(backgroundPoints, p);	
							//System.out.print("n =" + connectedPoints.lenght);
						}
					}
					else {
						// binaryIp.set(i, j, 0); // cor de componente
						int index = getIndex(i, j);
						binPixels[index] = 0;
					}
				}
			}
		}
			connectedPoints.remove(0);
		}

		binaryIp.setPixels(binPixels);
		return binaryIp;
	}

	private void markAsVisited(ArrayList<Point> list, Point p) { // Pixel já visitado, classificado como background e
																	// retirado da lista iteradora => Não pode
																	// participar de outra iteração
		list.add(p);
	}

	private boolean obeysIntesityCriterion(int x, int y, double mean, double stdDev) {
		int pix = ip.get(x, y);
		return (Math.abs((double) (pix - mean)) <= 3 * stdDev);
	}

	private boolean isBackGround(ArrayList<Point> list, Point point) { // Pixel foi marcado como plano de fundo em
																		// iteração anterior e ainda não teve vizinhaça
																		// analisada
		return (!list.contains(point));
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

	private boolean wasNeverVisited(ArrayList<Point> list, Point p) { // Pixel é plano de fundo e não foi visitado em
																		// iterações anteriores
		return list.contains(p);
	}

	public Roi getGivedRoi() {
		return GivedRoi;
	}

}