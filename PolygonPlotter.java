import java.awt.*;
import java.awt.Color.*;
import java.awt.event.*;
import java.awt.geom.*;
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

public class PolygonPlotter
{
	// Interface componants.
	public JFrame frame;
	public Canvas canvas;
	
	
	ArrayList original_data = new ArrayList();
	ArrayList data = new ArrayList();
	
	int line   = 9;
	int sensor = 3;
	
	double offset = 15;
	double range  = 60;
	double position = 45;
	
	Polygon[] polygon = new Polygon[30];
	Polygon[] scaled_polygon = new Polygon[30];
	Area[]    area    = new Area[30];
	Area[]    scaled_area    = new Area[30];
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
			System.out.println("Usage: PolygonPlotter <data file>");
			System.exit(0);
		} 
		else
		{
			try
			{
				String filename = prefix + args[0];
				try
				{
					PolygonPlotter window = new PolygonPlotter(filename);
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

	public PolygonPlotter(String filename)
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
		canvas.setSize(650, 700);

		frame.getContentPane().add(canvas, BorderLayout.CENTER);
		frame.pack();
		
	}
	
	class PathCanvas extends Canvas
	{
		int bottom_margin = 60;
		int left_margin   = 60;
		int top_margin    = 10;
		int right_margin  = 10;
		
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
			
		
			ArrayList line_data = new ArrayList();
			
			
			for(int i = 0; i < 30; i++)
			{
				ArrayList line_list = new ArrayList();
				int start = line_array[i][0];
				int stop  = line_array[i][1];
			    
				if(i % 2 == 0)
			    {
				    for (int j = start; j < stop; j++)
				    {
					    Sample sample = (Sample) data.get(j);
					    line_list.add(sample);
				    }
			    } 
			    else
			    {
				    for (int j = stop - 1; j >= start; j--)
				    {
					    Sample sample = (Sample) data.get(j);
					    line_list.add(sample);
				    }
			    }
			    line_data.add(line_list);
			}
			
			ArrayList polygon_data = new ArrayList();
			for(int i = 0; i < 30; i++)
			{
				ArrayList line_list    = (ArrayList)line_data.get(i);
				ArrayList polygon_list = new ArrayList();
			
				if (i % 2 == 0)
				{ 
					
					boolean not_started    = true;
					Sample  init_sample = new Sample();
					
					int     current_sensor = 4;
					for (int j = current_sensor; j < line_list.size(); j += 5)
					{
						Sample sample = (Sample) line_list.get(j);
						if(sample.y >= offset && sample.y < (offset + range))
						{
						    polygon_list.add(sample);
						    if(not_started)
						    {
						    	not_started = false;
						    	init_sample = sample;
						    }
						}
					}
					current_sensor = 0;
					for (int j = line_list.size() - (5 - current_sensor); j >= 0; j -= 5)
					{
						Sample sample = (Sample) line_list.get(j);
						if(sample.y >= offset && sample.y < (offset + range))
						{
						    polygon_list.add(sample);
						}      	
					}
					polygon_list.add(init_sample);
				} 
				else
				{
					boolean not_started    = true;
					Sample  init_sample = new Sample();
					
					int     current_sensor = 0;
					for (int j = current_sensor; j < line_list.size(); j += 5)
					{
						Sample sample = (Sample) line_list.get(j);
						if(sample.y >= offset && sample.y < (offset + range))
						{
						    polygon_list.add(sample);
						    if(not_started)
						    {
						    	not_started = false;
						    	init_sample = sample;
						    	
						    }
						}
					}
					current_sensor = 4;
					for (int j = line_list.size() - (5 - current_sensor); j >= 0; j -= 5)
					{
						Sample sample = (Sample) line_list.get(j);
						if(sample.y >= offset && sample.y < (offset + range))
						{
						    polygon_list.add(sample);
						}      	
					}
					polygon_list.add(init_sample);   	
				}
				polygon_data.add(polygon_list);
			}
			
			AffineTransform t1 = AffineTransform.getScaleInstance(.1, .1);
			AffineTransform t2 = AffineTransform.getTranslateInstance(10, -90);
			for(int i = 0; i < 30; i++)
			{
				ArrayList polygon_list = (ArrayList)polygon_data.get(i);
				int length = polygon_list.size();
				int[] x = new int[length];
				int[] y = new int[length];
				for(int j = 0; j < length; j++)
				{
				    Sample sample = (Sample)polygon_list.get(j);	
				    
				    x[j] = Math.round((float)(sample.x * 100));
				 
				    y[j] = Math.round((float)(sample.y * 100));
				}
				
				polygon[i] = new Polygon(x, y, length);
				area[i]    = new Area(polygon[i]);
				scaled_area[i] = (Area) area[i].clone();
				scaled_area[i].transform(t1);
				scaled_area[i].transform(t2);
			}
			
			for(int i = 0; i < 29; i++)
			{
				Area intersecting_area = (Area)area[i].clone();;
			    intersecting_area.intersect(area[i + 1]);
			    Area scaled_intersecting_area = (Area)scaled_area[i].clone();;
			    scaled_intersecting_area.intersect(scaled_area[i + 1]);
                
			    g2.setColor(java.awt.Color.BLACK);
			    g2.draw(scaled_area[i]);
			    if(intersecting_area.isEmpty())
			    {
				    //System.out.println("Flight line " + i + " does not intersect flight line " + (i + 1));
			    }
			    else
			    {
			    	//System.out.println("Flight line " + i + " does intersect flight line " + (i + 1));
			    	if(i == 22)
			    	{
			    		PathIterator path_iterator = intersecting_area.getPathIterator(null);
			    		int winding_rule = path_iterator.getWindingRule();
			    		if(winding_rule == PathIterator.WIND_EVEN_ODD)
			    		{
			    		    System.out.println("Rule for determining insideness is even odd.");	
			    		}
			    		else if(winding_rule == PathIterator.WIND_NON_ZERO)
			    		{
			    			System.out.println("Rule for determining insideness is non zero.");	
			    		}
			    		double [] coords = new double[6];
			    		while(path_iterator.isDone() == false)
			    		{
			    		    int segment_type = path_iterator.currentSegment(coords);
			    		    if(segment_type == PathIterator.SEG_MOVETO)
			    		    {
			    		        //System.out.println("Segment type is move to");	
			    		        System.out.println("Coordinates start at " + coords[0] + ", " + coords[1]); 
			    		    }
			    		    else if(segment_type == PathIterator.SEG_LINETO)
			    		    {
			    		    	//System.out.println("Segment type is line to");
			    		    	//System.out.println("Coordinates are " + coords[0] + ", " + coords[1]);
			    		    }
			    		    else if(segment_type == PathIterator.SEG_QUADTO)
			    		    {
			    		    	System.out.println("Segment type is quadratic parametric");		 
			    		    }
			    		    else if(segment_type == PathIterator.SEG_CUBICTO)
			    		    {
			    		    	System.out.println("Segment type is cubic parametric");		
			    		    }
			    		    else if(segment_type == PathIterator.SEG_CLOSE)
			    		    {
			    		    	//System.out.println("Segment type is close");
			    		    	System.out.println("Coordinates end at " + coords[0] + ", " + coords[1]);
			    		    }  
			    		    path_iterator.next();
			    		}
			    		ArrayList line_list    = (ArrayList)line_data.get(22);
			    		int size = line_list.size();
			    		int number_of_samples = 0;
			    		for(int j = 0; j < size; j++)
			    		{
			    			Sample sample = (Sample)line_list.get(j);
			    			if(intersecting_area.contains(sample.x * 100, sample.y * 100))
			    			{
			    			     number_of_samples++;   	
			    			}
			    		}
			    		System.out.println("Number of samples in intersecting area in line 22 is " + number_of_samples);
			    		line_list    = (ArrayList)line_data.get(23);
			    		size = line_list.size();
			    		number_of_samples = 0;
			    		for(int j = 0; j < size; j++)
			    		{
			    			Sample sample = (Sample)line_list.get(j);
			    			if(intersecting_area.contains(sample.x * 100, sample.y * 100))
			    			{
			    			     number_of_samples++;   	
			    			}
			    		}
			    		System.out.println("Number of samples in intersecting area in line 23 is " + number_of_samples);
			    		g2.setColor(java.awt.Color.BLUE);
			    		g2.fill(scaled_intersecting_area);
			    		Rectangle2D bounding_box =  intersecting_area.getBounds2D();
			    		double xmin = bounding_box.getMinX();
			    		double ymin = bounding_box.getMinY();
			    		double xmax = bounding_box.getMaxX();
			    		double ymax = bounding_box.getMaxY();
			    		
			    		System.out.println("Xmin is " + xmin);
			    		System.out.println("Ymin is " + ymin);
			    		System.out.println("Xmax is " + xmax);
			    		System.out.println("Ymax is " + ymax);
			    		
			    		
				        g2.setColor(java.awt.Color.RED);
				        //g2.draw(bounding_box);
			    		
			    	}
			    	else
			    	{
				        g2.setColor(java.awt.Color.RED);
				        g2.fill(scaled_intersecting_area);
			    	}
			    }
			}
			g2.setColor(java.awt.Color.BLACK);
			g2.draw(scaled_area[29]);
		}
	}
}
