import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;
import java.util.zip.*;
import java.lang.Math.*;

public class CorrelationFinder extends Frame
{
	ArrayList line   = new ArrayList();
	ArrayList sensor = new ArrayList();
	ArrayList offset = new ArrayList();
	ArrayList range  = new ArrayList();
	ArrayList xshift = new ArrayList();
	ArrayList yshift = new ArrayList();
	
	
	int   resolution = 100;
	int   reduction  = 2;
	
	ArrayList samples_per_res = new ArrayList();
	ArrayList delta_impulse   = new ArrayList();
	ArrayList delta_delta     = new ArrayList();
	
	
	public CorrelationFinder(String filename, int xdim, int ydim)
    {
	    WindowHandler window_handler = new WindowHandler();
	    addWindowListener(window_handler);
	    setSize(xdim, ydim);
	    setLocation(800, 400);
	    Panel main_panel = new Panel(new BorderLayout());
		add(main_panel);
	    LineCanvas plot_canvas = new LineCanvas();
	    plot_canvas.setSize(xdim, ydim / 2);
	    main_panel.add(plot_canvas, "Center");
	    Panel parameter_panel = new Panel(new BorderLayout());
	    main_panel.add(parameter_panel, "South");
	    setVisible(true);
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
    
    class LineCanvas extends Canvas
    {
    	public void paint(Graphics g) 
        {
    		Rectangle visible_area = g.getClipBounds();
        	int clipped_xdim = (int) visible_area.getWidth();
        	System.out.println("The width of the visible area is " + clipped_xdim);
    		int clipped_ydim = (int) visible_area.getHeight();
    		System.out.println("The height of the visible area is " + clipped_ydim);
    		Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(1));
            g2.setColor(java.awt.Color.BLACK);
            g2.drawLine(1, 1, 1, clipped_ydim - 2);
            g2.drawLine(1, 1, clipped_xdim - 2, 1);
            g2.drawLine(1, clipped_ydim - 2, clipped_xdim - 2, clipped_ydim - 2);
            g2.drawLine(clipped_xdim - 2, clipped_ydim - 2, clipped_xdim - 2, 1);
            
            
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