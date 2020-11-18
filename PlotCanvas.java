import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Frame; 
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;

public class PlotCanvas extends Canvas
{
    int xdim = 0;
    int ydim = 0;
    double minimum_x = Double.MAX_VALUE;
    double maximum_x = Double.MIN_VALUE; 
    double minimum_y = Double.MAX_VALUE; 
    double maximum_y = Double.MIN_VALUE;
    double xrange, yrange;
    ArrayList[] line;
    Frame frame;
    				
    public PlotCanvas(int xdim, int ydim, ArrayList[] line)
    {
    	this.xdim              = xdim;
    	this.ydim              = ydim;
    	this.line              = line;
    	ArrayList current_line = line[0];
    	Point2D.Double point   = (Point2D.Double)current_line.get(0); 
    	double x               = point.getX();
    	double y               = point.getY();
    	minimum_x = maximum_x  = x;
    	minimum_y = maximum_y  = y;
    	for(int i = 0; i < line.length; i++)
    	{
    		current_line = line[i];
    		for(int j = 0; j < current_line.size(); j++)
    		{
    			point = (Point2D.Double)current_line.get(j); 
    			x     = point.getX();
            	y     = point.getY();
            	if(x < minimum_x)
            		minimum_x = x;
            	else if(x > maximum_x)
            		maximum_x = x;
            	if(y < minimum_y)
            		minimum_y = y;
            	else if(y > maximum_x)
            		maximum_y = y;
    		}
    	}
    	xrange = maximum_x - minimum_x;
    	yrange = maximum_y - minimum_y;
    	
    	setSize(xdim, ydim);
    	frame = new Frame();
    	frame.setSize(xdim, ydim);
    	frame.add(this);
    	frame.addWindowListener(new WindowAdapter() 
    	{
    	    public void windowClosing(WindowEvent we) 
    	    {
    	        System.exit(0);
    	    }
    	});
    	frame.setVisible(true);
    }
    
    public void paint(Graphics g) 
    {
    	for(int i = 0; i < line.length; i++)
    	{
    		ArrayList current_line  = line[i];
            Point2D.Double previous = (Point2D.Double)current_line.get(0);
            for(int j = 1; j < current_line.size(); j++)
            {
            	double x1 = previous.getX();
        	    x1 -= minimum_x;
        	    x1 /= xrange;
        	    x1 *= xdim;
        	    double y1 = previous.getY();
        	    y1 -= minimum_y;
        	    y1 /= yrange;
        	    y1 *= ydim;
            	
            	Point2D.Double next = (Point2D.Double)current_line.get(j);
        	    double x2 = next.getX();
        	    x2 -= minimum_x;
        	    x2 /= xrange;
        	    x2 *= xdim;
        	    double y2 = next.getY();
        	    y2 -= minimum_y;
        	    y2 /= yrange;
        	    y2 *= ydim;
        	    
        	    g.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
        	    next = previous;
            }
        }
    }   
}
