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
	// Input
	ArrayList data = new ArrayList();
	ArrayList line_data = new ArrayList();
	ArrayList sensor_data = new ArrayList();
	ArrayList modified_data = new ArrayList();
	ArrayList interpolated_data = new ArrayList();
	

	// Output
	ArrayList sample_ratio  = new ArrayList();
	ArrayList delta_impulse = new ArrayList();
	ArrayList delta_delta   = new ArrayList();

	private JFrame frame;
	public JTable table;
	public LineCanvas canvas;

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
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public CorrelationFinder(String filename)
	{
		int _line, _sensor, _resolution, _reduction;
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
					} catch (IOException e)
					{
						System.out.println("Unexpected error " + e.toString());
					}
				}
				reader.close();
				for (int i = 0; i < original_data.size(); i++)
				{
					Sample sample = (Sample) original_data.get(i);
					sample.x -= xmin;
					sample.y -= ymin;
					data.add(sample);
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}

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
		header = new String("Sample Ratio");
		table.setValueAt(header, 0, 8);
		header = new String("Delta(i)");
		table.setValueAt(header, 0, 9);
		header = new String("Delta(d)");
		table.setValueAt(header, 0, 10);

		int rows = 2;

		for (int i = 0; i < rows; i++)
		{
			table.setValueAt((int) i, i + 1, 0);
			table.setValueAt((int) 4, i + 1, 1);
			table.setValueAt((double) 55, i + 1, 2);
			table.setValueAt((double) 15, i + 1, 3);
			table.setValueAt((double) 0, i + 1, 4);
			table.setValueAt((double) 0, i + 1, 5);
			table.setValueAt((int) 100, i + 1, 6);
			table.setValueAt((int) 0, i + 1, 7);
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

			int number_of_rows = table.getRowCount();
			System.out.println("Getting and reordering entire line data.");
			
			line_data.clear();
			for (int i = 1; i < number_of_rows; i++)
			{
				int current_line = (int) table.getValueAt(i, 0);
				int start = line_array[current_line][0];
				int stop = line_array[current_line][1];
				ArrayList sample_list = new ArrayList();
				if (current_line % 2 == 0)
				{
					for (int j = start; j < stop; j++)
					{
						Sample sample = (Sample) data.get(j);
						sample_list.add(sample);
					}
				}
				else
				{
					for (int j = stop - 1; j <= start; j--)
					{
						Sample sample = (Sample) data.get(j);
						sample_list.add(sample);
					}	
				}
				line_data.add(sample_list);
			}

			System.out.println("Extracting specific sensor data.");
			
			sensor_data.clear();
			for (int i = 1; i < number_of_rows; i++)
			{
				int j = i - 1;
				ArrayList line_list = (ArrayList) line_data.get(j);
				int current_line = (int) table.getValueAt(i, 0);
				int current_sensor = (int) table.getValueAt(i, 1);
				ArrayList sample_list = new ArrayList();

				if (current_line % 2 == 0)
				{
                    for(j = current_sensor; j < line_list.size(); j += 5)
                    {
                    	Sample sample = (Sample) line_list.get(j);  
                    	sample_list.add(sample);
                    }
				} 
				else
				{
					for(j = 5 - current_sensor; j < line_list.size(); j += 5)
                    {
                    	Sample sample = (Sample) line_list.get(j);  
                    	sample_list.add(sample);
                    }
				}
				sensor_data.add(sample_list);
			}

			modified_data.clear();
			for (int i = 1; i < number_of_rows; i++)
			{
				int j = i - 1;
				ArrayList sensor_list = (ArrayList) sensor_data.get(j);
				double current_offset = (double) table.getValueAt(i, 2);
				double current_range  = (double) table.getValueAt(i, 3);
				double current_xshift = (double) table.getValueAt(i, 4);
				double current_yshift = (double) table.getValueAt(i, 5);
				ArrayList sample_list = new ArrayList();
				boolean first_sample = true;
				for(j = 0; j <sensor_list.size(); j++)
				{
				    Sample sample = (Sample)sensor_list.get(j);
				    sample.y += current_yshift;
				    sample.x += current_xshift;
				    // Add previous sample as well unless location is the same as the offset.
				    if(sample.y >=  current_offset && sample.y < (current_offset + current_range))
				    {	
				    	if(first_sample && sample.y > current_offset)
				    	{
				    		Sample previous_sample = (Sample)sensor_list.get(j - 1);
				    		previous_sample.y += current_yshift;
				    		previous_sample.x += current_yshift; 
				    		sample_list.add(previous_sample);
				    		first_sample = false;
				    	}
				    	sample_list.add(sample);
				    }
				}
				modified_data.add(sample_list);
			}
			
			interpolated_data.clear();
			for (int i = 1; i < number_of_rows; i++)
			{
				int j = i - 1;
				ArrayList data_list = (ArrayList) modified_data.get(j);
				double current_offset = (double) table.getValueAt(i, 2);
				double current_range = (double) table.getValueAt(i, 3);
				int current_resolution = (int) table.getValueAt(i, 6);
				ArrayList sample_list = new ArrayList();
				double increment = current_range / current_resolution;
				double current_y = current_offset;
				Sample init_sample = (Sample)data_list.get(0);
				int number_of_samples = 0;
				int index             = 0;
				if(init_sample.y < current_y)
				{
					 // Could use x position as well, but keeping it simple.
				     Sample next_sample = (Sample)data_list.get(1); 
				     double distance1 = Math.abs(current_y - init_sample.y);
				     double distance2 = Math.abs(current_y - next_sample.y);
					 double total_distance = distance1 + distance2;
					 
					 
					 Sample sample = new Sample();
					 sample.intensity  = init_sample.intensity * (distance2 / total_distance);
					 sample.intensity += next_sample.intensity * (distance1 / total_distance);
					 sample.x          = init_sample.x * (distance2 / total_distance);
					 sample.x         += next_sample.x * (distance1 / total_distance);
					 sample.y          = init_sample.y * (distance2 / total_distance);
					 sample.y         += next_sample.y * (distance1 / total_distance);
					 number_of_samples = 2;
					 index             = 1;
				}
				else  // init sample y exactly equals offset
				{
				     sample_list.add(init_sample);	
				     number_of_samples = 1;
				     index             = 1;
				}
				current_y += increment;
				
				for(j = 0; j < current_resolution; j++)
				{
				    Sample sample = (Sample) data_list.get(index);
				    if(sample.y > current_y)
				    {
				    	
				    }
				    else if(sample.y == current_y)
				    {
				    	
				    }
				    else  // sample.y < current_y
				    {
				    	
				    }
				}
				
				
				
			}	
			
			
			
			
			// table.setValueAt(0, 1, 8);
			table.setValueAt(0, 1, 9);
			table.setValueAt(0, 1, 10);

			canvas.repaint();
		}
	}

	public double[] reduce(double[] source, int iterations)
	{
		int dst_length = source.length - 1;
		double[] src = source;
		double[] dst = new double[dst_length];
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

}
