package LAB_02;

import java.awt.Point;

import ij.gui.Roi;
import ij.process.ImageStatistics;

public class ROI_Statistics {

	private double min;
	private double max;
	private double mean;
	private double median;
	private double stdDev;
	private double var;
	private double mode;
	private double perimeter;
	private double area;
	private String shape;
	private double xCentroid;
	private double yCentroid;
	private double heigth;
	private double width;
	private double xstart;
	private double ystart;
	private long[] hist;
	private Point[] points;
	private double circularity;
	private Boolean isBorderline;
	
	public ROI_Statistics() {
		// TODO Auto-generated constructor stub
	}
	
	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public double getMean() {
		return mean;
	}

	public void setMean(double mean) {
		this.mean = mean;
	}

	public double getMedian() {
		return median;
	}

	public void setMedian(double median) {
		this.median = median;
	}

	public double getStdDev() {
		return stdDev;
	}

	public void setStdDev(double stdDev) {
		this.stdDev = stdDev;
	}

	public double getVar() {
		return var;
	}

	public void setVar(double var) {
		this.var = var;
	}

	public double getMode() {
		return mode;
	}

	public void setMode(double mode) {
		this.mode = mode;
	}

	public double getPerimeter() {
		return perimeter;
	}

	public void setPerimeter(double perimeter) {
		this.perimeter = perimeter;
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	public String getShape() {
		return shape;
	}

	public void setShape(String shape) {
		this.shape = shape;
	}

	public double getxCentroid() {
		return xCentroid;
	}

	public void setxCentroid(double xCentroid) {
		this.xCentroid = xCentroid;
	}

	public double getyCentroid() {
		return yCentroid;
	}

	public void setyCentroid(double yCentroid) {
		this.yCentroid = yCentroid;
	}
	
	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}	
	
	public double getHeigth() {
		return heigth;
	}

	public void setHeigth(double heigth) {
		this.heigth = heigth;
	}
	
	public double getXstart() {
		return xstart;
	}

	public void setXstart(double xstart) {
		this.xstart = xstart;
	}

	public double getYstart() {
		return ystart;
	}

	public void setYstart(double ystart) {
		this.ystart = ystart;
	}
	
	public long[] getHist() {
		return hist;
	}

	public void setHist(long[] hist) {
		this.hist = hist;
	}
	
	public Point[] getPoints() {
		return points;
	}

	public void setPoints(Point[] points) {
		this.points = points;
	}
	
	public double getCircularity() {
		return circularity;
	}

	public void setCircularity(double circularity) {
		this.circularity = circularity;
	}
	
	public Boolean getIsBorderline() {
		return isBorderline;
	}

	public void setIsBorderline(Boolean isBorderline) {
		this.isBorderline = isBorderline;
	}

	public void calculateROIStatistics(Roi roi) {
		//ImageStatistics is = new ImageStatistics(); 
		//roi.setImage(imp);
		//ImageProcessor ip = imp.getProcessor();
		ImageStatistics is = roi.getStatistics();
		setMin(is.min); 
		setMax(is.max);
		setMean(is.mean);
		setMedian(is.median);
		setStdDev(is.stdDev);
		setVar(Math.pow(stdDev, 2));
		setMode(is.mode);
		setPerimeter(roi.getLength());
		setArea(is.area);
		setShape(roi.getTypeAsString());
		setxCentroid(is.xCentroid);
		setyCentroid(is.yCentroid);
		setHeigth(is.roiHeight);
		setWidth(is.roiWidth);
		setXstart(is.xstart);
		setYstart(is.ystart);
		setHist(is.getHistogram());	
		setPoints(roi.getContainedPoints());
		setCircularity(4*Math.PI*area/Math.pow(perimeter,2));
		//setMargin(isMargin); it will be implemented yet
	}

}
