import java.io.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class PlotCanvasTester
{
	public static void main(String[] args) throws IOException, FileNotFoundException
	{
		int resolution = 100;
		int increment  = 1;
		ArrayList line1 = new ArrayList();
		for(int i = 0; i < resolution; i++)
		{
			Point2D.Double point  = new Point2D.Double();
			point.x               = i * increment;
			point.y               = i * increment;
			line1.add(point);
		}
		ArrayList line2 = new ArrayList();
		for(int i = 0; i < resolution; i++)
		{
			Point2D.Double point  = new Point2D.Double();
			point.x               = i * increment;
			point.y               = i;
			line2.add(point);
		}
		ArrayList[] line_vector = new ArrayList[1];
		line_vector[0] = line1;
		PlotCanvas lines = new PlotCanvas(400, 200, line_vector);	
	}
}