import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Frame; 
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.*;

public class DataCanvas extends Canvas
{
    int xdim = 0;
    int ydim = 0;
    BufferedImage data_image;
    Frame frame;
    				
    public DataCanvas(int xdim, int ydim, BufferedImage data_image)
    {
    	this.xdim = xdim;
    	this.ydim = ydim;
    	this.data_image = data_image;
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
        g.drawImage(data_image, 0, 0, frame);	    	
    }   
}
