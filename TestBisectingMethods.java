import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.io.*;

public class TestBisectingMethods
{
    public static void main(String[] args)
    {
    	
    	/*
    	Sample sample1 = new Sample(0, 0, 100);
    	Sample sample2 = new Sample(10, 12, 50);
    	Point2D.Double point = new Point2D.Double(1, 1);
    	Point2D.Double bisecting_point = DataMapper.getBisectingPoint(sample2, sample1, point);
    	double x = bisecting_point.getX();
    	double y = bisecting_point.getY();
    	double value = DataMapper.getBisectingAverage(sample1, sample2, point);
    	//System.out.println("Value at " + x + ", " + y + " is " + value);	
    	*/
    	
    	/*
    	Point2D.Double upper_left = new Point2D.Double(4, 99);
    	Point2D.Double upper_right = new Point2D.Double(100, 100);
    	Point2D.Double lower_left = new Point2D.Double(0, 0);
    	Point2D.Double lower_right = new Point2D.Double(100, 3);
    	
    	Point2D.Double intersecting_point = DataMapper.getIntersection(upper_left, upper_right, lower_right, lower_left);
    	double x = intersecting_point.getX();
    	double y = intersecting_point.getY();
    	System.out.println("Intersection at " + x + ", " + y);	
    	*/
    	
    	double rise  =  1;
    	double run   =  1;
    	double theta = Math.atan2(rise, run);
    	double degrees = Math.toDegrees(theta);
    	System.out.println("Rise = " + rise + ", run = " + run + ", theta = " + degrees);
    	
    	rise  =  1;
    	run   = -1;
    	theta = Math.atan2(rise, run);
    	degrees = Math.toDegrees(theta);
    	System.out.println("Rise = " + rise + ", run = " + run + ", theta = " + degrees);

    	rise  = -1;
    	run   =  1;
    	theta = Math.atan2(rise, run);
    	degrees = Math.toDegrees(theta);
    	System.out.println("Rise = " + rise + ", run = " + run + ", theta = " + degrees);
    	

    	rise  = -1;
    	run   = -1;
    	theta = Math.atan2(rise, run);
    	degrees = Math.toDegrees(theta);
    	System.out.println("Rise = " + rise + ", run = " + run + ", theta = " + degrees);
    	
    	
    }
}
