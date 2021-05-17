import java.awt.*;
import java.awt.Color.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.*;

public class SensorPathPlotter
{
	// Interface componants.
	public JFrame frame;
	public Canvas canvas;
	public JScrollBar scrollbar;
	
	ArrayList original_data = new ArrayList();
	ArrayList data = new ArrayList();
	
	int line   = 9;
	int sensor = 3;
	
	double offset = 15;
	double range  = 60;
	double position = 45;
	
	/*
	outline_color[0] = new Color(0, 0, 0);
	outline_color[1] = new Color(0, 0, 75);
	outline_color[2] = new Color(0, 75, 0);
	outline_color[3] = new Color(75, 0, 0);
	outline_color[4] = new Color(0, 75, 75);
	outline_color[5] = new Color(75, 0, 75);
	outline_color[6] = new Color(75, 75, 0);
	outline_color[7] = new Color(75, 75, 75);
	outline_color[8] = new Color(75, 75, 150);
	outline_color[9] = new Color(75, 150, 75);
    
	fill_color[0] = new Color(196, 196, 196);
	fill_color[1] = new Color(196, 196, 224);
	fill_color[2] = new Color(196, 224, 196);
	fill_color[3] = new Color(224, 196, 196);
	fill_color[4] = new Color(196, 224, 255);
	fill_color[5] = new Color(224, 196, 224);
	fill_color[6] = new Color(224, 224, 196);
	fill_color[7] = new Color(224, 224, 224);
	fill_color[8] = new Color(224, 224, 255);
	fill_color[9] = new Color(224, 255, 224);
	*/
	
	
	public static void main(String[] args)
	{
		String prefix = new String("C:/Users/Brian Crowley/Desktop/");
		//String prefix = new String("");
		if (args.length != 1)
		{
			System.out.println("Usage: SensorPathPlotter <data file>");
			System.exit(0);
		} 
		else
		{
			try
			{
				String filename = prefix + args[0];
				try
				{
					SensorPathPlotter window = new SensorPathPlotter(filename);
					window.frame.setVisible(true);
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public SensorPathPlotter(String filename)
	{
		//System.out.println(System.getProperty("java.version"));
		File file = new File(filename);
		if (file.exists())
		{
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

				System.out.println("Xmin is " + xmin);
				System.out.println("Ymin is " + ymin);
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
				
				//ArrayList data = new ArrayList();
				for (int i = 0; i < original_data.size(); i++)
				{
					Sample sample = (Sample) original_data.get(i);
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
		else
		{
			System.out.println("File not found.");
			System.exit(0);
		}

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		canvas = new PathCanvas();
		canvas.setSize(850, 700);

		scrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 100, 3, 0, 203);
		ScrollbarHandler scrollbar_handler = new ScrollbarHandler();
		scrollbar.addAdjustmentListener(scrollbar_handler);
		

		frame.getContentPane().add(canvas, BorderLayout.CENTER);
		frame.getContentPane().add(scrollbar, BorderLayout.SOUTH);
		frame.pack();
		
	}
	
	class PathCanvas extends Canvas
	{
		int bottom_margin = 60;
		int left_margin   = 60;
		int top_margin    = 10;
		int right_margin  = 120;
		
		public void paint(Graphics g)
		{	
			Color[] outline_color = new Color[10];
			Color[] fill_color    = new Color[10];
			
			outline_color[0] = new Color(0, 0, 0);
			outline_color[1] = new Color(0, 0, 75);
			outline_color[2] = new Color(0, 75, 0);
			outline_color[3] = new Color(75, 0, 0);
			outline_color[4] = new Color(0, 75, 75);
			outline_color[5] = new Color(75, 0, 75);
			outline_color[6] = new Color(75, 75, 0);
			outline_color[7] = new Color(75, 75, 75);
			outline_color[8] = new Color(75, 75, 150);
			outline_color[9] = new Color(75, 150, 75);
		    
			fill_color[0] = new Color(196, 196, 196);
			fill_color[1] = new Color(196, 196, 224);
			fill_color[2] = new Color(196, 224, 196);
			fill_color[3] = new Color(224, 196, 196);
			fill_color[4] = new Color(196, 224, 255);
			fill_color[5] = new Color(224, 196, 224);
			fill_color[6] = new Color(224, 224, 196);
			fill_color[7] = new Color(224, 224, 224);
			fill_color[8] = new Color(224, 224, 255);
			fill_color[9] = new Color(224, 255, 224);
			
			Rectangle visible_area = g.getClipBounds();

			int xdim = (int) visible_area.getWidth();
			int ydim = (int) visible_area.getHeight();
			
			Graphics2D g2 = (Graphics2D) g;
			Font current_font = g2.getFont();
			FontMetrics font_metrics = g2.getFontMetrics(current_font);
			g2.setColor(java.awt.Color.WHITE);
			g2.fillRect(0, 0, xdim, ydim);
			g2.setColor(java.awt.Color.BLACK);
			
			int[][] line_array = ObjectMapper.getUnclippedLineArray();
			
			int current_line   = line;
			int current_sensor = sensor;
			int current_pair   = 0;
			
			ArrayList line_data = new ArrayList();
			int start = line_array[current_line][0];
			int stop  = line_array[current_line][1];
			
			if (current_line % 2 == 0)
			{
				for (int i = start; i < stop; i++)
				{
					Sample sample = (Sample) data.get(i);
					line_data.add(sample);
				}
			} 
			else
			{
				for (int i = stop - 1; i >= start; i--)
				{
					Sample sample = (Sample) data.get(i);
					line_data.add(sample);
				}
			}
			//System.out.println("Current line:sensor is " + current_line + ":" + current_sensor);
			
			double ideal_position[] = new double[10];
			String[] pair_string    = new String[10];
			
			
			ArrayList sensor_data = new ArrayList();
			while(current_pair < 10)
			{
				String line_sensor_pair = new String(current_line + ":" + current_sensor);
		    	pair_string[current_pair] = line_sensor_pair;
		        //System.out.println("Current line:sensor is " + line_sensor_pair);
				
				double ideal_x_position = current_line * 2 + 1;
				if(current_line % 2 == 0)
			    {
					ideal_x_position += .5 * (4 - current_sensor);
			    }
				else
				{
					ideal_x_position += .5 * current_sensor;	
				}
				ideal_position[current_pair] = ideal_x_position;
			    if(current_line % 2 == 0)
			    {
			    	ArrayList sample_list = new ArrayList();
			    	for (int i = current_sensor; i < line_data.size(); i += 5)
					{
						Sample sample = (Sample) line_data.get(i);
						if(sample.y >= offset && sample.y < (offset + range))
						    sample_list.add(sample);
					}
			    	sensor_data.add(sample_list);
			        current_sensor--;
			        if(current_sensor < 0)
			        {
			        	current_line++;
			        	
			        	start = line_array[current_line][0];
						stop  = line_array[current_line][1];
						
						line_data.clear();
						
						for (int i = stop - 1; i >= start; i--)
						{
							Sample sample = (Sample) data.get(i);
							line_data.add(sample);
						}
			        	current_sensor = 0;
			        }
			    }
			    else 
			    {
			    	ArrayList sample_list = new ArrayList();
			    	for (int i = 4 - current_sensor; i < line_data.size(); i += 5)
					{
						Sample sample = (Sample) line_data.get(i);
						if(sample.y >= offset && sample.y < (offset + range))
						    sample_list.add(sample);
					}
			    	sensor_data.add(sample_list);
			        current_sensor++;
			        if(current_sensor > 4)
			        {
			        	current_line++;
			        	
			        	start = line_array[current_line][0];
						stop  = line_array[current_line][1];
						
						line_data.clear();
						
						for (int i = start; i < stop; i++)
						{
							Sample sample = (Sample) data.get(i);
							line_data.add(sample);
						}
			        	current_sensor = 4;
			        }
			    }
			    current_pair++;
			}
			
			//System.out.println("Sensor data is composed of " + sensor_data.size() + " lists.");
			
			double data_xmin = Double.MAX_VALUE;
			double data_xmax = Double.MIN_VALUE;
			for(int i = 0; i < sensor_data.size(); i++)
			{
			    ArrayList sample_list = (ArrayList)sensor_data.get(i);
			    int list_length = sample_list.size();
			    //System.out.println("List " + i + " has " + list_length + " samples.");
			    
			    double xmin = Double.MAX_VALUE;
				double xmax = Double.MIN_VALUE;
				for(int j = 0; j < list_length; j++)
				{
					Sample sample = (Sample)sample_list.get(j);
					if(sample.x < xmin)
						xmin = sample.x;
					if(sample.x > xmax)
						xmax = sample.x;
				}
				if(data_xmin > xmin)
					data_xmin = xmin;
				if(data_xmax < xmax)
					data_xmax = xmax;
				//System.out.println("Minimum x value is " + xmin);
				//System.out.println("Maximum x value is " + xmax);
				//System.out.println("Ideal x value is "   + ideal_position[i]);	
			}
			
			
			
			// Switching x and y to make orientation the same as fence plotter program.
			double xrange = range;
			
			double global_xmin = data_xmin;
			double global_xmax = data_xmax;
			if(global_xmin > (ideal_position[0] - .25))
				global_xmin = ideal_position[0] - .25;
			if(global_xmax < (ideal_position[9] + .25))
				global_xmax = ideal_position[9] + .25;
			
			
			double yrange = global_xmax - global_xmin;
			
			int graph_xdim = xdim - (left_margin + right_margin);
			int graph_ydim = ydim - (top_margin + bottom_margin);
			
			
			double a1 = 0;
			double a2 = range;
			double b1 = 0;
			double b2 = data_xmax - data_xmin;
			
			/*
			a1 /= xrange;
    	    a1 *= graph_xdim;
    	    a1 += left_margin;
    	    
    	    a2 /= xrange;
    	    a2 *= graph_xdim;
    	    a2 += left_margin;
			
			b1 /= yrange;
    	    b1 *= graph_ydim;
    	    b1 = graph_ydim - b1;
    	    b1 += top_margin;
    	    
    	    b2 /= yrange;
    	    b2 *= graph_ydim;
    	    b2 = graph_ydim - b2;
    	    b2 += top_margin;
    	    
    	    g2.setColor(java.awt.Color.RED);
    	    g2.drawLine((int) a1, (int)b1, (int) a1, (int)b2);
    	    g2.drawLine((int) a2, (int)b1, (int) a2, (int)b2);
    	    g2.drawLine((int) a1, (int)b1, (int) a2, (int)b1);
    	    g2.drawLine((int) a1, (int)b2, (int) a2, (int)b2);
			*/
			
			
			for(int i = 0; i < sensor_data.size(); i++)
			{
			    ArrayList sample_list = (ArrayList)sensor_data.get(i);
			    int list_length = sample_list.size();
			    
			    Sample previous_sample = (Sample)sample_list.get(0);
			    
			    double x = previous_sample.y;
			    double y = previous_sample.x;
			    
			    x -= offset;
			    x /= xrange;
        	    x *= graph_xdim;
        	    x += left_margin;
        	    
        	    y -= global_xmin;
        	    y /= yrange;
        	    y *= graph_ydim;
        	    y = graph_ydim - y;
        	    y += top_margin;
			  
			    Point2D.Double previous = new Point2D.Double(x, y);
				
			    g2.setColor(fill_color[i]);
			    g2.setStroke(new BasicStroke(3));
			    for(int j = 1; j < list_length; j++)
				{
					Sample sample = (Sample)sample_list.get(j);
					x = sample.y;
				    y = sample.x;
				    
				    x -= offset;
				    x /= xrange;
	        	    x *= graph_xdim;
	        	    x += left_margin;
	        	    
	        	    y -= global_xmin;
	        	    y /= yrange;
	        	    y *= graph_ydim;
	        	    y = graph_ydim - y;
	        	    y += top_margin;
	        	    
	        	    Point2D.Double current = new Point2D.Double(x, y);
	        	    
	        	    double x1 = previous.getX();
					double y1 = previous.getY();
					double x2 = current.getX();
					double y2 = current.getY();
					
					g2.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
	        	    previous = current;
				}
			}
			
			
			//g2.setColor(java.awt.Color.BLUE);
			//g2.setStroke(new BasicStroke(2));
			a1 = 0;
			a1 /= xrange;
	    	a1 *= graph_xdim;
	    	a1 += left_margin;
	    	a2 = range;   
	    	a2 /= xrange;
	    	a2 *= graph_xdim;
	    	a2 += left_margin;
			for(int i = 0; i < ideal_position.length; i++)
			{
				b1 = ideal_position[i];
				b1 -= global_xmin;
	    	    b1 /= yrange;
	    	    b1 *= graph_ydim;
	    	    b1 = graph_ydim - b1;
	    	    b1 += top_margin;
	    	    
	    	    
				int string_width;
				
				if(i < 9)
				{ 
				    if(ideal_position[i] == ideal_position[i + 1])
				    {
				    	String cat_string = new String(pair_string[i] + "," + pair_string[i + 1]);
				    	string_width = font_metrics.stringWidth(cat_string);
				    	g2.setColor(java.awt.Color.BLACK);
						g2.drawString(cat_string, left_margin / 2 - string_width / 2, (int)b1);
						g2.setStroke(new BasicStroke(2));
						g2.setColor(fill_color[i]);
						g2.drawLine((int) a1, (int)b1 + 2, (int) a2, (int)b1 + 2);
						g2.setColor(fill_color[i + 1]);
						g2.drawLine((int) a1, (int)b1 - 2, (int) a2, (int)b1 - 2);
						i++;
						
				    }
				    else
				    {
				        string_width = font_metrics.stringWidth(pair_string[i]);
				        g2.setColor(java.awt.Color.BLACK);
				        g2.drawString(pair_string[i], left_margin / 2 - string_width / 2, (int)b1);
				        g2.setStroke(new BasicStroke(3));
				        
				        g2.setColor(fill_color[i]);
				        g2.drawLine((int) a1, (int)b1, (int) a2, (int)b1);
				    }
				}
				else
				{
					g2.setColor(java.awt.Color.BLACK);
					string_width = font_metrics.stringWidth(pair_string[i]);
			        g2.drawString(pair_string[i], left_margin / 2 - string_width / 2, (int)b1);
			        
			        g2.setColor(fill_color[i]);
			        g2.setStroke(new BasicStroke(2));
		    	    g2.drawLine((int) a1, (int)b1, (int) a2, (int)b1);
				}
			}
			
			b1 = data_xmin;
			b1 -= global_xmin;
			b1 /= yrange;
    	    b1 *= graph_ydim;
    	    b1 = graph_ydim - b1;
    	    b1 += top_margin;
    	    
    	    b2 = data_xmax;
    	    b2 -= global_xmin;
    	    b2 /= yrange;
    	    b2 *= graph_ydim;
    	    b2 = graph_ydim - b2;
    	    b2 += top_margin;
 
    	    g2.setColor(java.awt.Color.RED);
    	    g2.drawLine((int) a1, (int)b1, (int) a1, (int)b2);
    	    g2.drawLine((int) a2, (int)b1, (int) a2, (int)b2);
    	    g2.drawLine((int) a1, (int)b1, (int) a2, (int)b1);
    	    g2.drawLine((int) a1, (int)b2, (int) a2, (int)b2);
    	    
    	    
    	    g2.drawLine((int) a2 + 10, (int)b1, (int) a2 + 10, (int)b2);
    	    g2.drawLine((int) a2 + 5, (int)b1, (int) a2 + 10, (int)b1);
    	    g2.drawLine((int) a2 + 5, (int)b2, (int) a2 + 10, (int)b2);
    	    
    	    
    	    g2.setColor(java.awt.Color.BLACK);
    	    
    	    String position_string = String.format("%,.2f", data_xmin);
    	    g2.drawString(position_string, (int)a2 + 15, (int) b1);
    	    
    	    position_string = String.format("%,.2f", data_xmax);
    	    //int string_height = font_metrics.getHeight();
    	    //g2.drawString(position_string, (int)a2 + 15, (int) b2 + string_height);
    	    g2.drawString(position_string, (int)a2 + 15, (int) b2 + 8);
    	    
    	   
    	    b1 =  ideal_position[0] - .25;
			b1 -= global_xmin;
    	    b1 /= yrange;
    	    b1 *= graph_ydim;
    	    b1 =  graph_ydim - b1;
    	    b1 += top_margin;
    	    
    	    b2 =  ideal_position[9] + .25;
			b2 -= global_xmin;
    	    b2 /= yrange;
    	    b2 *= graph_ydim;
    	    b2 =  graph_ydim - b2;
    	    b2 += top_margin;
    	    
    	    g2.setColor(java.awt.Color.BLACK);
    	    g2.drawLine((int) a1, (int)b1, (int) a1, (int)b2);
    	    g2.drawLine((int) a2, (int)b1, (int) a2, (int)b2);
    	    g2.drawLine((int) a1, (int)b1, (int) a2, (int)b1);
    	    g2.drawLine((int) a1, (int)b2, (int) a2, (int)b2);
    	    
    	    g2.drawLine((int) a2 + 60, (int)b1, (int) a2 + 60, (int)b2);
    	    g2.drawLine((int) a2 + 55, (int)b1, (int) a2 + 60, (int)b1);
    	    g2.drawLine((int) a2 + 55, (int)b2, (int) a2 + 60, (int)b2);
    	    
    	    position_string = String.format("%,.2f", ideal_position[0] - .25);
    	    g2.drawString(position_string, (int)a2 + 65, (int) b1);
    	    position_string = String.format("%,.2f", ideal_position[9] + .25);
    	    g2.drawString(position_string, (int)a2 + 65, (int) b2 + 8);
    	    
    	    b1 = graph_ydim + top_margin + 40;
    	    g2.drawLine((int) a1, (int)b1, (int) a2, (int)b1);
    	    g2.drawLine((int) a1, (int)b1, (int) a1, (int)b1 - 5);
    	    g2.drawLine((int) a2, (int)b1, (int) a2, (int)b1 - 5);
    	    position_string = String.format("%,.2f", offset);
    	    int string_height = font_metrics.getHeight();
    	    g2.drawString(position_string, (int)a1, (int) b1 + string_height);
    	    position_string = String.format("%,.2f", offset + range);
    	    int string_width = font_metrics.stringWidth(position_string);
    	    g2.drawString(position_string, (int)a2 - string_width, (int) b1 + string_height);
    	    
    	    g2.setColor(java.awt.Color.BLUE);
    	    a1 = position - offset;
			a1 /= xrange;
	    	a1 *= graph_xdim;
	    	a1 += left_margin;
	    	
	    	b1 =  data_xmin;
			b1 -= global_xmin;
    	    b1 /= yrange;
    	    b1 *= graph_ydim;
    	    b1 =  graph_ydim - b1;
    	    b1 += top_margin;
    	    
    	    b2 =  data_xmax;
			b2 -= global_xmin;
    	    b2 /= yrange;
    	    b2 *= graph_ydim;
    	    b2 =  graph_ydim - b2;
    	    b2 += top_margin;
    	    
    	    g2.drawLine((int) a1, (int)b1, (int) a1, (int)b2);
    	    position_string = String.format("%,.2f", position);
    	    string_width = font_metrics.stringWidth(position_string);
    	    g2.drawString(position_string, (int)a1 - string_width / 2, (int) b1 + string_height);
    	    
    	    ArrayList position_list = new ArrayList();
    	    Hashtable <Double, String> position_table = new Hashtable <Double, String>();
    	    for(int i = 0; i < sensor_data.size(); i++)
			{
			    ArrayList sample_list = (ArrayList)sensor_data.get(i);
			    int list_length = sample_list.size();
			    outer: for(int j = 0; j < list_length; j++)
				{
					Sample sample = (Sample)sample_list.get(j);
					if(sample.y >= position)
					{
						//System.out.println("The x position of " + pair_string[i] + " is " + sample.x);
						position_list.add(sample.x);
						position_table.put(sample.x, pair_string[i]);
						break outer;
					}
					
				}
			}
    	    Collections.sort(position_list);
    	    System.out.println("Ordered line sensor list:");
    	    for(int i = 0; i < position_list.size(); i++)
    	    {
    	    	String current_pair_string = position_table.get(position_list.get(i));
    	    	System.out.println(i + " " + current_pair_string);
    	    }
    	    
    	    
		}
	}
	
	class ScrollbarHandler implements AdjustmentListener
	{
		public void adjustmentValueChanged(AdjustmentEvent event)
		{
			int current_value = event.getValue();
				
			position = current_value;
			position /= 200;
			position *= range;
			position += offset;
			if(event.getValueIsAdjusting() == false)
			    canvas.repaint();
		}
	}
}
