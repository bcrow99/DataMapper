import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;
import java.util.zip.*;
import java.lang.Math.*;

public class CorrelationFinder extends Frame
{
	public CorrelationFinder(String filename, int xdim, int ydim)
    {
	    WindowHandler window_handler = new WindowHandler();
	    addWindowListener(window_handler);
	    CorrelationPanel correlation_panel = new CorrelationPanel(filename, xdim, ydim);
		add(correlation_panel);
	    setSize(xdim, ydim);
	    setLocation(800, 400);
	    setVisible(true);
	    
	    Graphics graphics    = getGraphics();
	    Rectangle visible_area = graphics.getClipBounds();
    	int clipped_xdim       = (int) visible_area.getWidth();
		System.out.println("The width of the visible area is " + clipped_xdim);
		int clipped_ydim = (int) visible_area.getHeight();
		System.out.println("The height of the visible area is " + clipped_ydim);
	    
	    /*
	    Graphics2D graphics    = (Graphics2D)this.getGraphics();
	    Rectangle visible_area = graphics.getClipBounds();
    	int clipped_xdim       = (int) visible_area.getWidth();
		System.out.println("The width of the visible area is " + clipped_xdim);
		int clipped_ydim = (int) visible_area.getHeight();
		System.out.println("The height of the visible area is " + clipped_ydim);
		*/
		
		
    }
	
	
	
	//The following listener is used to terminate the 
    // program when the user closes the Frame object.
    class WindowHandler extends WindowAdapter
    {
        public void windowClosing(WindowEvent e)
        {
            System.exit(0);
        }
    }   
	
	public class CorrelationPanel extends Panel
	{
	    public CorrelationPanel(String filename, int xdim, int ydim)
	    {
	    	File data_file = new File(filename);
	    }
	}

	public static void main(String[] args)
    {
        CorrelationFinder correlation_finder;

        if(args.length != 3)
        {
            System.out.println("Usage: java CorrelationFinder <filename> <xdim> <ydim>");
            System.exit(0);
        }
        else
        {
            int xdim  = Integer.parseInt(args[1]);
            int ydim  = Integer.parseInt(args[2]);
            correlation_finder = new CorrelationFinder(args[0], xdim, ydim);
        }
    }
}