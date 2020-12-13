import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class CorrelationFinder
{
	ArrayList line          = new ArrayList();
	ArrayList sensor        = new ArrayList();
	ArrayList offset        = new ArrayList();
	ArrayList range         = new ArrayList();
	ArrayList xshift        = new ArrayList();
	ArrayList yshift        = new ArrayList();
	ArrayList resolution    = new ArrayList();
	ArrayList reduction     = new ArrayList();
	ArrayList samples       = new ArrayList();
	ArrayList delta_impulse = new ArrayList();
	ArrayList delta_delta   = new ArrayList();
	
	ArrayList data = new ArrayList();
	double[] line1_interpolation;
	double[] line2_interpolation;
	double[] x1_interpolation;
	double[] x2_interpolation;
	double[] xdelta;
	double[] difference;
	double[] line1_delta;
	double[] line2_delta;
	double[] line1_reduction;
	double[] line2_reduction;
	
	private JFrame frame;
	public  JTable table;
	public  LineCanvas canvas;
	
	

	public static void main(String[] args)
	{
		String prefix = new String("C:/Users/Brian Crowley/Desktop/");
		if (args.length != 1)
		{
			System.out.println("Usage: CorrelationFinder <data file>");
			System.exit(0);
		}
		else
		{
		    try
		    {
		        String filename = prefix + args[0];
		        try
				{
					CorrelationFinder window = new CorrelationFinder(filename);
					window.frame.setVisible(true);
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}
		    }
		    catch(Exception e)
		    {
		    	e.printStackTrace();	
		    }
		}
	}

	public double[] reduce(double[] source, int iterations)
	{
		int src_length = source.length;
		int dst_length = source.length - 1;
		double[] src = source;
		double[] dst = new double[dst_length];
		;
		while (dst_length >= source.length - iterations)
		{
			for (int i = 0; i < dst_length; i++)
			{
				dst[i] = (src[i] + src[i + 1]) / 2;
			}
			src = dst;
			dst_length--;
			if (dst_length >= source.length - iterations)
				dst = new double[dst_length];
		}
		return (dst);
	}
	
	public CorrelationFinder(String filename)
	{
		int    _line, _sensor, _resolution, _reduction; 
		double _offset, _range, _xshift, _yshift, _samples, _delta_impulse, _delta_delta;
		
		
		File file = new File(filename);
		if (file.exists())
		{
			ArrayList original_data = new ArrayList();
			double xmin = Double.MAX_VALUE;
			double xmax = Double.MIN_VALUE;
			double ymin = Double.MAX_VALUE;
			double ymax = Double.MIN_VALUE;
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				String line = reader.readLine();
				StringTokenizer number_tokens = new StringTokenizer(line, " ");
				int length = number_tokens.countTokens();
				int number_of_entries = length / 3;
				for (int i = 0; i < number_of_entries; i++)
				{
					double x = Double.valueOf(number_tokens.nextToken());
					double y = Double.valueOf(number_tokens.nextToken());
					if (x < xmin)
						xmin = x;
					else if (x > xmax)
						xmax = x;
					if (y < ymin)
						ymin = y;
					else if (y > ymax)
						ymax = y;
					double intensity = Double.valueOf(number_tokens.nextToken());
					Sample current_sample = new Sample(x, y, intensity);
					original_data.add(current_sample);
				}
				
				while (line != null)
				{
					try
					{
						line = reader.readLine();
						if (line != null)
						{
							number_tokens = new StringTokenizer(line, " ");
							for (int i = 0; i < number_of_entries; i++)
							{
								double x = Double.valueOf(number_tokens.nextToken());
								double y = Double.valueOf(number_tokens.nextToken());
								if (x < xmin)
									xmin = x;
								else if (x > xmax)
									xmax = x;
								if (y < ymin)
									ymin = y;
								else if (y > ymax)
									ymax = y;
								double intensity = Double.valueOf(number_tokens.nextToken());
								Sample current_sample = new Sample(x, y, intensity);
								original_data.add(current_sample);
							}
						}
					} 
					catch (IOException e)
					{
						System.out.println("Unexpected error " + e.toString());
					}
				}
				reader.close();
				for(int i = 0; i < original_data.size(); i++)
				{
					Sample sample = (Sample)original_data.get(i);
					sample.x -= xmin;
					sample.y -= ymin;
					data.add(sample);
				}
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	
		_line = 0;
		line.add(_line);
		_line = 1;
		line.add(_line);
		
		_sensor = 4;
		sensor.add(_sensor);
		sensor.add(_sensor);
		
		_offset = 55.;
		offset.add(_offset);
		offset.add(_offset);
		
		_range = 15.;
		range.add(_range);
		range.add(_range);
		
		_xshift = 0.;
		xshift.add(_xshift);
		xshift.add(_xshift);
		
		_yshift = 0.;
		yshift.add(_yshift);
		_yshift = -0.8;
		yshift.add(_yshift);
		
		_resolution = 100;
		resolution.add(_resolution);
		resolution.add(_resolution);
		
		_reduction = 2;
		reduction.add(_reduction);
		reduction.add(_reduction);
		
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		canvas = new LineCanvas();
		canvas.setSize(400, 350);
		frame.getContentPane().add(canvas, BorderLayout.CENTER);
		
		table = new JTable(3, 11);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		for (int column = 0; column < 11; column++)
        {
            table.getColumnModel().getColumn(column).setCellRenderer(centerRenderer);
        }
		String header;
		header = new String("Line");
		table.setValueAt(header, 0, 0);
		header = new String("Sensor");
		table.setValueAt(header, 0, 1);
		header = new String("Offset");
		table.setValueAt(header, 0, 2);
		header = new String("Range");
		table.setValueAt(header, 0, 3);
		header = new String("Xshift");
		table.setValueAt(header, 0, 4);
		header = new String("Yshift");
		table.setValueAt(header, 0, 5);
		header = new String("Resolution");
		table.setValueAt(header, 0, 6);
		header = new String("Reduction");
		table.setValueAt(header, 0, 7);
		header = new String("Samples");
		table.setValueAt(header, 0, 8);
		header = new String("Delta(i)");
		table.setValueAt(header, 0, 9);
		header = new String("Delta(d)");
		table.setValueAt(header, 0, 10);
		
		int rows = 2;
		
		for(int i = 0; i < rows; i++)
		{
			table.setValueAt((int)line.get(i), i + 1, 0);
			table.setValueAt((int)sensor.get(i), i + 1, 1);
			table.setValueAt((double)offset.get(i), i + 1, 2);
			table.setValueAt((double)range.get(i), i + 1, 3);
			table.setValueAt((double)xshift.get(i), i + 1, 4);
			table.setValueAt((double)yshift.get(i), i + 1, 5);
			table.setValueAt((int)resolution.get(i), i + 1, 6);
			table.setValueAt((int)reduction.get(i), i + 1, 7);
		}
		
		JPanel bottom_panel = new JPanel(new BorderLayout());
		bottom_panel.add(table, BorderLayout.CENTER);
		JButton apply_button = new JButton("Apply params.");
		ApplyHandler handler = new ApplyHandler();
		apply_button.addActionListener(handler);
		bottom_panel.add(apply_button, BorderLayout.EAST);
	
		frame.getContentPane().add(bottom_panel, BorderLayout.SOUTH);
		
		frame.pack();
	}

	class LineCanvas extends Canvas
    {
		public void paint(Graphics g) 
        {
    		Rectangle visible_area = g.getClipBounds();
        	int clipped_xdim = (int) visible_area.getWidth();
    		int clipped_ydim = (int) visible_area.getHeight();
    		Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(1));
            g2.setColor(java.awt.Color.BLACK);
            g2.drawLine(1, 1, 1, clipped_ydim - 2);
            g2.drawLine(1, 1, clipped_xdim - 2, 1);
            g2.drawLine(1, clipped_ydim - 2, clipped_xdim - 2, clipped_ydim - 2);
            g2.drawLine(clipped_xdim - 2, clipped_ydim - 2, clipped_xdim - 2, 1);   
        }
    }
	
	class ApplyHandler implements ActionListener
	{
		int[][] line_array;
		
		ApplyHandler()
		{
			line_array = ObjectMapper.getLineArray();    	
		}
		
		public void actionPerformed(ActionEvent e)
        {
			System.out.println("Getting entire line data.");
			int    line1   = (int)table.getValueAt(1,0);
			int    line2   = (int)table.getValueAt(2,0);
			int start = line_array[line1][0];
			int stop = line_array[line1][1];
			ArrayList line1_sample_list = new ArrayList();
			for (int i = start; i < stop; i++)
			{
				Sample sample = (Sample) data.get(i);
				line1_sample_list.add(sample);
			}
			start = line_array[line2][0];
			stop = line_array[line2][1];
			ArrayList line2_sample_list = new ArrayList();
			for (int i = start; i < stop; i++)
			{
				Sample sample = (Sample) data.get(i);
				line2_sample_list.add(sample);
			}
			System.out.println("Done.");
			
			
			
			int    sensor1 = (int)table.getValueAt(1,1);
			int    sensor2 = (int)table.getValueAt(2,1);
			double offset1 = (double)table.getValueAt(1,2);
			double offset2 = (double)table.getValueAt(2,2);
			double range1  = (double)table.getValueAt(1,3);
			double range2  = (double)table.getValueAt(2,3);
			
			
			double xshift1 = (double)table.getValueAt(1,4);
			double xshift2 = (double)table.getValueAt(2,4);
			double yshift1  = (double)table.getValueAt(1,5);
			double yshift2  = (double)table.getValueAt(2,5);
			
			int    resolution1 = (int)table.getValueAt(1,6);
			int    resolution2 = (int)table.getValueAt(2,6);
			int    reduction1 = (int)table.getValueAt(1,7);
			int    reduction2 = (int)table.getValueAt(2,7);
			
			double increment1 = range1 / resolution1;
			line1_interpolation = new double[resolution1];
			line2_interpolation = new double[resolution2];
			x1_interpolation = new double[resolution1];
			x2_interpolation = new double[resolution2];
			xdelta = new double[resolution1];
			difference = new double[resolution1];
			line1_delta = new double[resolution1 - reduction1];
			line2_delta = new double[resolution2 - reduction2];
			line1_reduction = new double[resolution1 - reduction1];
			line2_reduction = new double[resolution2 - reduction2];
			
			
			
			if (line1 % 2 == 0)
			{
				
			}
			else
			{
				
			}
			if (line2 % 2 == 0)
			{
				
			}
			else
			{
				
			}
			
			canvas.repaint();
        }	
	} 
}
