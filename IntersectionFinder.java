import java.awt.*;
import java.awt.Color.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
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

public class IntersectionFinder
{
	// Interface componants.
	public JFrame frame;
	public Canvas canvas;

	ArrayList original_data = new ArrayList();
	ArrayList data = new ArrayList();

	int line = 9;
	int sensor = 3;

	double offset = 15;
	double range = 60;
	double position = 45;

	Polygon[] polygon = new Polygon[30];
	Polygon[] scaled_polygon = new Polygon[30];
	Area[] area = new Area[30];
	Area[] scaled_area = new Area[30];

	double xmin = Double.MAX_VALUE;
	double xmax = Double.MIN_VALUE;
	double ymin = Double.MAX_VALUE;
	double ymax = Double.MIN_VALUE;
	
	public static void main(String[] args)
	{
		String prefix = new String("C:/Users/Brian Crowley/Desktop/");
		// String prefix = new String("");
		if (args.length != 1)
		{
			System.out.println("Usage: IntersectionFinder <data file>");
			System.exit(0);
		} else
		{
			try
			{
				String filename = prefix + args[0];
				try
				{
					IntersectionFinder window = new IntersectionFinder(filename);
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

	public IntersectionFinder(String filename)
	{
		// System.out.println(System.getProperty("java.version"));
		File file = new File(filename);
		if (file.exists())
		{
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

				// ArrayList data = new ArrayList();
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
		
		// Segment and smooth the data set by sensor.
		
		ArrayList segment_array = new ArrayList();
		
		for(int i = 0; i < 5; i++)
		{
			ArrayList data_list = new ArrayList();
			for(int j = i; j < data.size(); j += 5)
			{
				Sample sample = (Sample)data.get(j);
				data_list.add(sample);
			}
			segment_array.add(data_list);
		}
		
		for(int i = 0; i < 5; i++)
		{
			ArrayList data_list = (ArrayList)segment_array.get(i);
			int       size      = data_list.size();
			double    x[]       = new double[size];
			double    y[]       = new double[size];
			double    z[]       = new double[size];
			
			for(int j = 0; j < size; j++)
			{
				Sample sample = (Sample)data_list.get(j);
				x[j] = sample.x;
				y[j] = sample.y;
				z[j] = sample.intensity;
			}
			
			double smooth_x[] = smooth(x, 2);
    		double smooth_y[] = smooth(y, 2);
    		double smooth_z[] = smooth(z, 2);
    		
    		// Replace the original data with wiht a shorter list of smoothed data.
    		data_list.clear();
    		int length = smooth_x.length;
    		for(int j = 0; j < length; j++)
    		{  
    			Sample sample = new Sample(smooth_x[j], smooth_y[j], smooth_z[j]);	
    			data_list.add(sample);
    		}	
		}
		
		data.clear();
		ArrayList sensor_0 = (ArrayList)segment_array.get(0);
		ArrayList sensor_1 = (ArrayList)segment_array.get(1);
		ArrayList sensor_2 = (ArrayList)segment_array.get(2);
		ArrayList sensor_3 = (ArrayList)segment_array.get(3);
		ArrayList sensor_4 = (ArrayList)segment_array.get(4);
		
		//All the lists should be the same size.
		int size = sensor_0.size();
		for(int i = 0; i < size; i++)
		{
			Sample sample = (Sample)sensor_0.get(i);
			data.add(sample);
			sample = (Sample)sensor_1.get(i);
			data.add(sample);
			sample = (Sample)sensor_2.get(i);
			data.add(sample);
			sample = (Sample)sensor_3.get(i);
			data.add(sample);
			sample = (Sample)sensor_4.get(i);
			data.add(sample);
		}
		

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		canvas = new PathCanvas();
		canvas.setSize(650, 900);

		frame.getContentPane().add(canvas, BorderLayout.CENTER);
		frame.pack();

	}

	class PathCanvas extends Canvas
	{
		int bottom_margin = 60;
		int left_margin = 60;
		int top_margin = 10;
		int right_margin = 10;

		public void paint(Graphics g)
		{
			Color[] outline_color = new Color[10];
			Color[] fill_color = new Color[10];

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
			
			Image buffered_image = new BufferedImage(xdim, ydim, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics_buffer = (Graphics2D) buffered_image.getGraphics();
			graphics_buffer.setColor(java.awt.Color.WHITE);
			graphics_buffer.fillRect(0, 0, xdim, ydim);

			int[][] line_array = ObjectMapper.getUnclippedLineArray();
			
			line_array[29][1] -= 10;

			ArrayList line_data = new ArrayList();

			for (int i = 0; i < 30; i++)
			{
				ArrayList line_list = new ArrayList();
				int start = line_array[i][0];
				int stop = line_array[i][1];

				if (i % 2 == 0)
				{
					for (int j = start; j < stop; j++)
					{
						Sample sample = (Sample) data.get(j);
						line_list.add(sample);
					}
				} else
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
			for (int i = 0; i < 30; i++)
			{
				ArrayList line_list = (ArrayList) line_data.get(i);
				ArrayList polygon_list = new ArrayList();

				if (i % 2 == 0)
				{

					boolean not_started = true;
					Sample init_sample = new Sample();

					int current_sensor = 4;
					for (int j = current_sensor; j < line_list.size(); j += 5)
					{
						Sample sample = (Sample) line_list.get(j);
						if (sample.y >= offset && sample.y < (offset + range))
						{
							polygon_list.add(sample);
							if (not_started)
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
						if (sample.y >= offset && sample.y < (offset + range))
						{
							polygon_list.add(sample);
						}
					}
					polygon_list.add(init_sample);
				} 
				else
				{
					boolean not_started = true;
					Sample init_sample = new Sample();

					int current_sensor = 0;
					for (int j = current_sensor; j < line_list.size(); j += 5)
					{
						Sample sample = (Sample) line_list.get(j);
						if (sample.y >= offset && sample.y < (offset + range))
						{
							polygon_list.add(sample);
							if (not_started)
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
						if (sample.y >= offset && sample.y < (offset + range))
						{
							polygon_list.add(sample);
						}
					}
					polygon_list.add(init_sample);
				}
				polygon_data.add(polygon_list);
			}

			double yrange = ymax - ymin;
			AffineTransform t1 = AffineTransform.getScaleInstance(.1, .1);
			AffineTransform t2 = AffineTransform.getTranslateInstance(10, 0);
			for (int i = 0; i < 30; i++)
			{
				ArrayList polygon_list = (ArrayList) polygon_data.get(i);
				int length = polygon_list.size();
				int[] x = new int[length];
				int[] y = new int[length];
				for (int j = 0; j < length; j++)
				{
					Sample sample = (Sample) polygon_list.get(j);

					x[j] = Math.round((float) (sample.x * 100));
                    double reverse_y = sample.y;
                    reverse_y = yrange - reverse_y;
					//y[j] = Math.round((float) (sample.y * 100));
                    y[j] = Math.round((float) (reverse_y * 100));
				}

				polygon[i] = new Polygon(x, y, length);
				area[i] = new Area(polygon[i]);
				scaled_area[i] = (Area) area[i].clone();
				scaled_area[i].transform(t1);
				scaled_area[i].transform(t2);
			}
            /*
			g2.setColor(java.awt.Color.LIGHT_GRAY);
			for (int i = 0; i < 30; i++)
			{
				g2.fill(scaled_area[i]);	
			}
			*/
			graphics_buffer.setColor(java.awt.Color.LIGHT_GRAY);
			for (int i = 0; i < 30; i++)
			{
				graphics_buffer.fill(scaled_area[i]);	
			}
			
			
			for (int i = 0; i < 29; i++)
			{
				Area intersecting_area = (Area) area[i].clone();
				intersecting_area.intersect(area[i + 1]);
				Area scaled_intersecting_area = (Area) scaled_area[i].clone();
				scaled_intersecting_area.intersect(scaled_area[i + 1]);
				if (intersecting_area.isEmpty())
				{
					System.out.println("Flight line " + i + " does not intersect flight line " + (i + 1));
				} 
				else
				{
					PathIterator path_iterator = intersecting_area.getPathIterator(null);
					ArrayList line_list = (ArrayList) line_data.get(i);
					int size = line_list.size();
					int number_of_samples = 0;
					for (int j = 0; j < size; j++)
					{
						Sample sample = (Sample) line_list.get(j);
						if (intersecting_area.contains(sample.x * 100, sample.y * 100))
						{
							number_of_samples++;
						}
					}
					System.out.println("Number of samples in intersecting area in line " + i + " is " + number_of_samples);
					line_list = (ArrayList) line_data.get(i + 1);
					size = line_list.size();
					number_of_samples = 0;
					for(int j = 0; j < size; j++)
					{
						Sample sample = (Sample) line_list.get(j);
						if (intersecting_area.contains(sample.x * 100, sample.y * 100))
						{
							number_of_samples++;
						}
					}
					System.out.println("Number of samples in intersecting area in line " + (i + 1) + " is " + number_of_samples);

					double perimeter = getPerimeter(path_iterator);
					String perimeter_string = String.format("%,.2f", perimeter);
					System.out.println("Perimeter of intersecting area is " + perimeter_string);
					System.out.println();
					/*
					if(perimeter > 8000)
					    g2.setColor(java.awt.Color.BLUE);
					else
						g2.setColor(java.awt.Color.RED);
					
					g2.setColor(java.awt.Color.BLUE);
					g2.fill(scaled_intersecting_area);
					*/
					graphics_buffer.setColor(java.awt.Color.BLUE);
					graphics_buffer.fill(scaled_intersecting_area);
					
					Rectangle2D bounding_box = intersecting_area.getBounds2D();
					/*
					double xmin = bounding_box.getMinX();
					double ymin = bounding_box.getMinY();
					double xmax = bounding_box.getMaxX();
					double ymax = bounding_box.getMaxY();
					*/

					// g2.setColor(java.awt.Color.RED);
					// g2.draw(bounding_box);

				}
			}
			/*
			g2.setColor(java.awt.Color.BLACK);
			for (int i = 0; i < 30; i++)
			{
				g2.draw(scaled_area[i]);	
			}
			*/
			
			graphics_buffer.setColor(java.awt.Color.BLACK);
			for (int i = 0; i < 30; i++)
			{
				graphics_buffer.draw(scaled_area[i]);	
			}
			g2.drawImage(buffered_image, 0, 0, null);
			
			double [][] location_array = ObjectMapper.getObjectLocationArray();
			int length = location_array.length;
			g2.setColor(java.awt.Color.RED);
			//double yrange = ymax - ymin;
			for(int i = 0; i < length; i++)
			{
				double x = location_array[i][0];
				double y = location_array[i][1];
				x -= xmin;
				y -= ymin;
				y = yrange - y;
				x *= 10;
				y *= 10;
				x += 10;
				//y -= 90;
				g2.fillOval((int)(x - 1), (int)(y - 1), 3, 3);
				String object_string = Integer.toString(i + 1); 
				g2.drawString(object_string, (int)(x + 2), (int)y); 
			}
		}
	}

	double getDistance(double x1, double y1, double x2, double y2)
	{
		double distance = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
		return (distance);
	}

	double getPerimeter(PathIterator path_iterator)
	{
		double[] coords = new double[6];
		double perimeter = 0;
		double previous_x = 0;
		double previous_y = 0;
		double current_x = 0;
		double current_y = 0;

		int segment_type = path_iterator.currentSegment(coords);
		if (segment_type == PathIterator.SEG_MOVETO)
		{
			//System.out.println("Coordinates start at " + coords[0] + ", " + coords[1]);
			previous_x = coords[0];
			previous_y = coords[1];
			path_iterator.next();
		} else
		{
			System.out.println("Unexpected format.");
			return (perimeter);
		}

		
		while (path_iterator.isDone() == false)
		{

			segment_type = path_iterator.currentSegment(coords);
			if (segment_type == PathIterator.SEG_LINETO)
			{
				current_x = coords[0];
				current_y = coords[1];

				double length = getDistance(previous_x, previous_y, current_x, current_y);
				perimeter += length;

				previous_x = current_x;
				previous_y = current_y;
			}
			path_iterator.next();
		}
		
		return (perimeter);
	}

	public double[] smooth(double[] source, int iterations)
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
		return(dst);
	}
	
}
