package LAB_02;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class Region_Growing_Grupo_03 implements PlugInFilter {

	private int ATTRIBUTES = 13;
	private String[] HEADINGS = new String[] { "X", "Y", "Area", "Perimeter", "Circularity", "Min", "Max", "Mean", "Median",
			"StdDev", "Variance", "Mode", "Borderline" };
	private ImagePlus binaryImp;
	private ImagePlus filteredImp;
	private ImageProcessor filteredIp;
	private int numberOfComponents;
	private ArrayList<String> borderline;
	private double[][] resultsMatrix;
	private int w;
	private int h;
	private Roi givedRoi;
//	private ImagePlus labeledImp;
//	private ColorProcessor labeledIp;
	private ImagePlus imp;

	public Region_Growing_Grupo_03() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		this.w = imp.getWidth();
		this.h = imp.getHeight();
		this.imp = imp;
		this.numberOfComponents = 0;
		this.borderline = new ArrayList<String>();
		this.binaryImp = NewImage.createByteImage("Binary image", w, h, 1, NewImage.FILL_WHITE);
		//this.labeledImp = NewImage.createRGBImage("Binary image", w, h, 1, NewImage.FILL_WHITE);
		return DOES_8G;
	}

	@Override
	public void run(ImageProcessor ip) {
		Region_Growing binarizer = new Region_Growing(imp);
		//ColorProcessor binIp = binarizer.createBackgroundSegmentation(ip);
		ByteProcessor binIp = binarizer.createBackgroundSegmentation();
		// System.out.println("End segmentation");
		this.givedRoi = binarizer.getGivedRoi();
		binaryImp.setProcessor(binIp);
		binaryImp.show();

		//labeledImp.setProcessor(binIp);
		//labeledIp = binIp;

		Labeling_Binary_Image labeler = new Labeling_Binary_Image(); // Rotula e mostra a imagem na tela
		labeler.setup("", binaryImp);
		labeler.run(binIp);
		// System.out.println("End labeling");

		HashMap<Color, LinkedHashSet<Point>> islands = labeler.getComponentsMap();
		HashMap<Color, LinkedHashSet<Point>> components = filterComponents(islands);
		this.numberOfComponents = components.size();

		this.filteredImp = NewImage.createRGBImage("Labeled Image", w, h, 1, NewImage.FILL_WHITE);
		this.filteredIp = filteredImp.getProcessor();
		filteredImp.show();

		/*
		 * Set<Color> colors = components.keySet(); for (Iterator<Color> iterator =
		 * colors.iterator(); iterator.hasNext();) { Color color = (Color)
		 * iterator.next(); LinkedHashSet<Point> tempPoints =
		 * components.get(iterator.next()); PointRoi roi =
		 * generateROI4component(tempPoints); filteredImp.setRoi(roi); } Testar depois
		 */

		Set<Color> colors = components.keySet();
		for (Iterator<Color> iterator = colors.iterator(); iterator.hasNext();) {
			Color color = (Color) iterator.next();
			LinkedHashSet<Point> points = components.get(color);
			for (Iterator iterator2 = points.iterator(); iterator2.hasNext();) {
				Point point = (Point) iterator2.next();
				filteredIp.set(point.x, point.y, color.getRGB());
			}
//			LinkedHashSet<Point> tempPoints = components.get(iterator.next());
//			for (Iterator<Point> iterator2 = tempPoints.iterator(); iterator2.hasNext();) {
//				Point point = (Point) iterator2.next();
//				filteredIp.set(point.x, point.y, color.getRGB());
//			}
		}
		filteredImp.updateAndDraw();
		// System.out.println("End filtering");

		calculateAllComponentsResults(components);
		displayAndPlotGivedRoiResults(this.givedRoi);
		displayAndPlotComponentsResults(this.resultsMatrix);
	}

	public HashMap<Color, LinkedHashSet<Point>> filterComponents(HashMap<Color, LinkedHashSet<Point>> islands) {
		Set<Color> colors = islands.keySet();
		int max = 0;
		for (Iterator<Color> iterator = colors.iterator(); iterator.hasNext();) {
			int area = islands.get(iterator.next()).size();
			if (area > max) {
				max = area;
			} // max => Área do maior componente
		}
		int limiar = max / 10; // Componentes c/ área menor q 10% da maior área serão descartados.
		for (Iterator<Color> iterator2 = colors.iterator(); iterator2.hasNext();) {
			PointRoi component = generateROI4component(islands.get(iterator2.next()));
			if (component.getContainedPoints().length < limiar) {
				islands.remove(iterator2.next());
			}
		}
		return islands;
	}

	public PointRoi generateROI4component(LinkedHashSet<Point> component) {
		Point[] points = component.toArray(new Point[0]);
		PointRoi roi = new PointRoi(points[0].x, points[0].y);
		for (int index = 1; index < points.length; index++) {
			roi.addPoint(points[index].x, points[index].y);
		}
		return roi;
	}

	public double[] calculateComponentResults(PointRoi roi, ROI_Statistics statistics) {
		statistics.calculateROIStatistics(roi);
		double[] componentStatistics = new double[this.ATTRIBUTES - 1];
		componentStatistics[0] = statistics.getxCentroid();
		componentStatistics[1] = statistics.getyCentroid();
		componentStatistics[2] = statistics.getArea();
		componentStatistics[3] = statistics.getPerimeter();
		componentStatistics[4] = statistics.getCircularity();
		componentStatistics[5] = statistics.getMin();
		componentStatistics[6] = statistics.getMax();
		componentStatistics[7] = statistics.getMean();
		componentStatistics[8] = statistics.getMedian();
		componentStatistics[9] = statistics.getStdDev();
		componentStatistics[10] = statistics.getVar();
		componentStatistics[11] = statistics.getMode();
		this.borderline.add(statistics.getIsBorderline().toString());
		return componentStatistics;
	}

	public void calculateAllComponentsResults(HashMap<Color, LinkedHashSet<Point>> components) {
		double[][] resultsMatrix = new double[this.numberOfComponents][this.ATTRIBUTES - 1];
		ROI_Statistics statistics = new ROI_Statistics();
		int index = 0;
		Set<Color> colors = components.keySet();
		for (Iterator<Color> iterator = colors.iterator(); iterator.hasNext();) {
			PointRoi componentRoi = generateROI4component(components.get(iterator.next()));
			double[] componentResult = calculateComponentResults(componentRoi, statistics);
			resultsMatrix[index] = componentResult;
			index++;
		}
		this.resultsMatrix = resultsMatrix;
	}

	public PointRoi convertRoi2PointRoi(Roi roi) {
		Point[] points = roi.getContainedPoints();
		PointRoi pointRoi = new PointRoi(points[0].x, points[0].y);
		for (int index = 1; index < points.length; index++) {
			pointRoi.addPoint(points[index].x, points[index].y);
		}
		return pointRoi;
	}

	public void displayAndPlotGivedRoiResults(Roi givedRoi) {
		PointRoi roi = convertRoi2PointRoi(givedRoi);
		ROI_Statistics statistics = new ROI_Statistics();
		double[] roiResults = calculateComponentResults(roi, statistics);

		ResultsTable resultsTable = new ResultsTable();
		for (int column = 0; column < this.ATTRIBUTES - 1; column++) {
			resultsTable.addValue(this.HEADINGS[column], roiResults[column]);
		}
		resultsTable.setLabel("ROI", 0);
		resultsTable.showRowNumbers(true);
		// resultsTable.setPrecision(4);
		resultsTable.save("C://Users//luana//Downloads//UFPE//PDI//saved-files/roi-statistics.csv");
		resultsTable.show("Selected ROI Statistics");

	}

	public void displayAndPlotComponentsResults(double[][] resultsMatrix) {
		ResultsTable resultsTable = new ResultsTable();
		for (int column = 0; column < this.ATTRIBUTES - 1; column++) {
			for (int row = 0; row < this.numberOfComponents; row++) {
				resultsTable.addValue(this.HEADINGS[column], resultsMatrix[row][column]);
			}
		}
		for (int index = 0; index < numberOfComponents; index++) {
			resultsTable.addValue(this.HEADINGS[this.ATTRIBUTES], this.borderline.get(index));
		}
		for (int row = 0; row < this.numberOfComponents; row++) {
			resultsTable.setLabel("Componente " + (row + 1), row);
		}
		resultsTable.showRowNumbers(true);
		// resultsTable.setPrecision(4);
		resultsTable.save("C://Users//luana//Downloads//UFPE//PDI//saved-files/components-statistics.csv");
		resultsTable.show("Components Statistics");

	}

}