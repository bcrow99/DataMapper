import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Frame; 
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;

public class RouteCanvas extends Canvas
{
    int xdim = 0;
    int ydim = 0;
    ArrayList route;
    Frame frame;
    				
    public RouteCanvas(int xdim, int ydim, ArrayList route)
    {
    	this.xdim = xdim;
    	this.ydim = ydim;
    	this.route = route;
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
        Point2D.Double first = (Point2D.Double)route.get(0);
        for(int i = 1; i < route.size(); i++)
        {
        	Point2D.Double second = (Point2D.Double) route.get(i);
        	int x1 = (int)first.getX();
        	int y1 = (int)first.getY();
        	int x2 = (int)second.getX();
        	int y2 = (int)second.getY();
        	g.drawLine(x1, y1, x2, y2);
        	second = first;
        }
        
    }   
}
