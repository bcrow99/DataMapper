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

public class InterAnalyzer
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
			System.out.println("Usage: InterAnalyzer <data file>");
			System.exit(0);
		} else
		{
			try
			{
				String filename = prefix + args[0];
				try
				{
					InterAnalyzer analyzer = new InterAnalyzer(filename);
					//window.frame.setVisible(true);
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public InterAnalyzer(String filename)
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

				//System.out.println("Xmin is " + xmin);
				//System.out.println("Ymin is " + ymin);
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

				// Relative coordinates
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
    		
    		
    		data_list.clear();
    		int length = smooth_x.length;
    		for(int j = 0; j < length; j++)
    		{  
    			Sample sample = new Sample(smooth_x[j], smooth_y[j], smooth_z[j]);	
    			data_list.add(sample);
    		}	
		}
		
		
		ArrayList sensor_0 = (ArrayList)segment_array.get(0);
		ArrayList sensor_1 = (ArrayList)segment_array.get(1);
		ArrayList sensor_2 = (ArrayList)segment_array.get(2);
		ArrayList sensor_3 = (ArrayList)segment_array.get(3);
		ArrayList sensor_4 = (ArrayList)segment_array.get(4);
		
		//All the lists should be the same size.
		int size = sensor_0.size();
		
		// Replace the interleaved relative data with a shorter list of smoothed data.
		data.clear();
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
	
		
		// Get an idex of endpoints by checking when the order of the x-coordinates
		// changes.
		int    first_index  = 0;
		int    last_index   = 4;
		Sample first_sample = (Sample)data.get(first_index);
		Sample last_sample  = (Sample)data.get(last_index);
		
		boolean init_direction_north = true;
		boolean headed_north         = true;
		if(first_sample.x < last_sample.x)
		{
			//System.out.println("Flight path is not headed north.");
			headed_north        = false;
			init_direction_north = false;
		}
		else
		{
			//System.out.println("Flight path is headed north.");
		}
		
		ArrayList endpoint_index = new ArrayList();
		
		while(last_index < data.size() - 5)
		{
		    first_index += 5;
		    last_index  += 5;
		    first_sample = (Sample)data.get(first_index);
		    last_sample  = (Sample)data.get(last_index);
		    if(headed_north)
		    {
		    	if(first_sample.x < last_sample.x)
		    	{
		    		endpoint_index.add(first_index);
		    	    headed_north = false;
		    	}
		    }
		    else
		    {
		    	if(first_sample.x > last_sample.x)
		    	{
		    	    endpoint_index.add(first_index);
		    	    headed_north = true;
		    	}	
		    }
		}
		
        int number_of_lines = endpoint_index.size() + 1;
        System.out.println("The number of lines in the data set is " + number_of_lines);
		int [][] line_index = new int[number_of_lines][2];
		line_index[0][0] = 0;
		for(int i = 0; i < number_of_lines - 1; i++)
		{
			int index = (int)endpoint_index.get(i);
			line_index[i][1] = index;
			line_index[i + 1][0] = index;
		}
		line_index[number_of_lines - 1][1] = data.size() - 1;
		
		ArrayList line_data = new ArrayList();
		for (int i = 0; i < number_of_lines; i++)
		{
			ArrayList line_list = new ArrayList();
			int       start     = line_index[i][0];
			int       stop      = line_index[i][1];

			if (i % 2 == 0)
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
		for (int i = 0; i < number_of_lines; i++)
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

		Polygon [] polygon = new Polygon[number_of_lines];
		Area    [] area    = new Area[number_of_lines];
		for (int i = 0; i < number_of_lines; i++)
		{
			ArrayList polygon_list = (ArrayList) polygon_data.get(i);
			int       length       = polygon_list.size();
			int[]     x            = new int[length];
			int[]     y            = new int[length];
			for (int j = 0; j < length; j++)
			{
				Sample sample = (Sample) polygon_list.get(j);
				x[j]          = Math.round((float) (sample.x * 100));
				y[j]          = Math.round((float) (sample.y * 100));
			}

			polygon[i] = new Polygon(x, y, length);
			area[i]    = new Area(polygon[i]);
		}
		
		int line_of_interest   = 22;
		Area intersecting_area = (Area) area[line_of_interest].clone();
		intersecting_area.intersect(area[line_of_interest + 1]);
		if (intersecting_area.isEmpty())
		{
			System.out.println("Line " + line_of_interest + " does not intersect line " + (line_of_interest + 1));
		} 
		else
		{
			double xmax = -Double.MAX_VALUE;
			double xmin = Double.MAX_VALUE;
			double ymax = -Double.MAX_VALUE;
			double ymin = Double.MAX_VALUE;
			
			ArrayList line_list = (ArrayList) line_data.get(line_of_interest);
			size                = line_list.size();
			
			ArrayList first_intersecting_data_list = new ArrayList();
			int       number_of_samples            = 0;
			for (int j = 0; j < size; j++)
			{
				Sample sample = (Sample) line_list.get(j);
				if(intersecting_area.contains(sample.x * 100, sample.y * 100))
				{
					number_of_samples++;
					first_intersecting_data_list.add(sample);
					if(sample.x < xmin)
						xmin = sample.x;
					else if(sample.x > xmax)
						xmax = sample.x;
					if(sample.y < ymin)
						ymin = sample.y;
					else if(sample.y > ymax)
						ymax = sample.y;
				}
			}
			System.out.println("Number of samples in intersecting area in line " + line_of_interest + " is " + number_of_samples);
			
			line_list = (ArrayList) line_data.get(line_of_interest + 1);
			ArrayList next_intersecting_data_list = new ArrayList();
			size = line_list.size();
			number_of_samples = 0;
			for (int j = 0; j < size; j++)
			{
				Sample sample = (Sample) line_list.get(j);
				if(intersecting_area.contains(sample.x * 100, sample.y * 100))
				{
					number_of_samples++;
					next_intersecting_data_list.add(sample);
					if(sample.x < xmin)
						xmin = sample.x;
					else if(sample.x > xmax)
						xmax = sample.x;
					if(sample.y < ymin)
						ymin = sample.y;
					else if(sample.y > ymax)
						ymax = sample.y;
				}
			}
			System.out.println("Number of samples in intersecting area in line " + (line_of_interest + 1) + " is " + number_of_samples);
			
		    System.out.println("Xmax for segments is " + String.format("%.2f", xmax));
		    System.out.println("Xmin for segments is " + String.format("%.2f", xmin));
		    System.out.println("Ymax for segments is " + String.format("%.2f", ymax));
		    System.out.println("Ymin for segments is " + String.format("%.2f", ymin));
		    
		    double x_range = xmax - xmin;
		    
		    double cell_width = .003;
		    double init_value = xmin - .0015;
		    double end_value  = xmax + .0015;
		    int number_of_cells = 0;
		    System.out.println("First bounding value is " + String.format("%.4f", init_value));
		    while(init_value < end_value)		
		    {
		        number_of_cells++;
		        init_value += cell_width;
		    }
		    System.out.println("Last bounding value is " + String.format("%.4f", init_value));
		    System.out.println("Raster xdim is " + number_of_cells);
		    
		    int raster_xdim = number_of_cells;
		    
		    init_value = ymin - .0015;
		    end_value  = ymax + .0015;
		    number_of_cells = 0;
		    System.out.println("First bounding value is " + String.format("%.4f", init_value));
		    while(init_value < end_value)		
		    {
		        number_of_cells++;
		        init_value += cell_width;
		    }
		    System.out.println("Last bounding value is " + String.format("%.4f", init_value));
		    System.out.println("Raster ydim is " + number_of_cells);
		    
		    int raster_ydim = number_of_cells;
		    
		    double raster_xmin = xmin - .0015;
			double raster_xmax = xmax + .0015;
			double raster_ymin = ymin - .0015;
			double raster_ymax = ymax + .0015;
			
			
			
			
			
			double raster_xlength = raster_xmax - raster_xmin;
			double raster_ylength = raster_ymax - raster_ymin;
		    
		    ArrayList [][] segment1_data = new ArrayList[raster_ydim][raster_xdim];
			ArrayList [][] segment2_data = new ArrayList[raster_ydim][raster_xdim];
			boolean   [][] isPopulated1  = new boolean[raster_ydim][raster_xdim];
			int       [][] sampleNumber1 = new int[raster_ydim][raster_xdim];
			boolean   [][] isPopulated2  = new boolean[raster_ydim][raster_xdim];
			int       [][] sampleNumber2 = new int[raster_ydim][raster_xdim];
			
			for (int i = 0; i < raster_ydim; i++)
			{
				for (int j = 0; j < raster_xdim; j++)
				{
					segment1_data[i][j] = new ArrayList();
					segment2_data[i][j] = new ArrayList();
					isPopulated1[i][j]  = false;
					isPopulated2[i][j]  = false;
					sampleNumber1[i][j] = 0;
					sampleNumber2[i][j] = 0;
				}
			}
			
		    double yrange = ymax - ymin;
		    double xrange = xmax - xmin;
		    
		    double ratio              = 0;
		    int    number_of_sections = 0;
		    if(yrange > xrange)
		    {
		    	ratio              = yrange / xrange;
		    	number_of_sections = (int)Math.floor((float)ratio);
		    }
		    else if(xrange > yrange)
			{
			    ratio              = xrange / yrange;
			    number_of_sections = (int)Math.floor((float)ratio);
			}
		    else 
		    	number_of_sections = 1;
		    
		    ArrayList [][] segment_data = new ArrayList[2][number_of_sections];
		    for(int i = 0; i < 2; i++)
		    	for(int j = 0; j < number_of_sections; j++)
		    		segment_data[i][j] = new ArrayList();
		    
		    
		    // We know our y dimension is always the major one.
		    /*
		    if(yrange > xrange)
		    {
		    	
		    }
		    else
		    {
		    	
		    }
		    */
		    
		    double y_increment = yrange / number_of_sections;
		    double lower_bound = ymin;
		    double upper_bound = ymin + y_increment;
		    
		    /*
		    System.out.println("Ymin is " + String.format("%.2f", ymin));
		    System.out.println();
		    for(int i = 0; i < number_of_sections; i++)
		    {
		    	System.out.println("Lower bound is " + String.format("%.2f", lower_bound));
		    	System.out.println("Upper bound is " + String.format("%.2f", upper_bound));
		    	System.out.println();
		    	lower_bound = upper_bound;
		    	upper_bound += y_increment;
		    }
		    System.out.println("Ymax is " + String.format("%.2f", ymax));
		    */
		    
		    size = first_intersecting_data_list.size();
	    	for(int j = 0; j < size; j++)
		    {
		    	Sample    sample = (Sample)first_intersecting_data_list.get(j);
		    	int       index  = (int)Math.floor((float)((sample.y - ymin) / yrange) * number_of_sections);
		    	if(index == number_of_sections)
		    	{
		    		//If this is our max value (1), stick it in the final bin.
		    		index--;
		    	}
		    	
		    	lower_bound = index * y_increment + ymin;
		    	upper_bound = lower_bound + y_increment;
		    	
		    	//System.out.println("Lower bound is " + String.format("%.4f", lower_bound));
		    	//System.out.println("Value is " + String.format("%.4f", sample.y));
		    	//System.out.println("Upper bound is " + String.format("%.4f", upper_bound));
		    	//System.out.println();
		    	
		    	
		    	ArrayList list   = (ArrayList)segment_data[0][index];
		    	list.add(sample);
		    }
		    
		    
	    	size = next_intersecting_data_list.size();
	    	for(int j = 0; j < size; j++)
		    {
		    	Sample    sample = (Sample)next_intersecting_data_list.get(j);
		    	int       index  = (int)Math.floor((float)((sample.y - ymin) / yrange) * number_of_sections);
		    	if(index == number_of_sections)
		    	{
		    		//If this is our max value (1), stick it in the final bin.
		    		index--;
		    	}
		    	
		    	lower_bound = index * y_increment + ymin;
		    	upper_bound = lower_bound + y_increment;
		    	
		    	System.out.println("Lower bound is " + String.format("%.4f", lower_bound));
		    	System.out.println("Value is " + String.format("%.4f", sample.y));
		    	System.out.println("Upper bound is " + String.format("%.4f", upper_bound));
		    	System.out.println();
		    	
		    	
		    	ArrayList list   = (ArrayList)segment_data[1][index];
		    	list.add(sample);
		    }
		    
		    
		    
		    
		    
		    
		    
		    /*
		    
			size = first_intersecting_data_list.size();
			for(int i = 0; i < size; i++)
		    {
		    	Sample sample = (Sample)first_intersecting_data_list.get(i);
		    	
		    	//double normal_x_location = (sample.x - raster_xmin) / raster_xlength;
		    	//double x_location        = normal_x_location * raster_xdim; 
		    	//int    x_index           = (int)Math.floor(x_location);
		    	
		    	double current_location = raster_xmin + cell_width;
		    	int    x_index          = 0;
		    	while(sample.x >= current_location)
		    	{
		    		x_index++;
		    		current_location += cell_width;
		    	}
		    	double cell_lower_bound  = x_index * .003 + raster_xmin;
		    	double cell_upper_bound  = cell_lower_bound + .003;
		    	
		    	// This breaks, even though it works with x.  A real puzzler, but 
		    	// there is another method that is just a little less efficient.
		    	
		    	
		    	//double normal_y_location = (sample.y - raster_ymin) / raster_ylength;
		    	//double y_location        = normal_y_location * raster_ydim; 
		    	//int    y_index           = (int)Math.floor(y_location);
		    
		    	
		    	current_location = raster_ymin + cell_width;
		    	int    y_index = 0;
		    	while(sample.y > current_location)
		    	{
		    		y_index++;
		    		current_location += cell_width;
		    	}
		    	
		    	cell_lower_bound  = y_index * .003 + raster_ymin;
		    	cell_upper_bound  = cell_lower_bound + .003;
		    	
		    	//System.out.println("Cell lower bound is " + String.format("%.5f", cell_lower_bound));
		    	//System.out.println("Cell upper bound is " + String.format("%.5f", cell_upper_bound));
		    	//System.out.println("Sample is           " + String.format("%.5f", sample.y));
		    	//System.out.println();
		    	
		    	ArrayList sample_list = segment1_data[y_index][x_index];
		    	sample_list.add(sample);
		    	isPopulated1[y_index][x_index] = true;
		    	sampleNumber1[y_index][x_index]++;
		    }
			
			size = next_intersecting_data_list.size();
			for(int i = 0; i < size; i++)
		    {
		    	Sample sample = (Sample)next_intersecting_data_list.get(i);
		    	double current_location = raster_xmin + cell_width;
		    	int    x_index          = 0;
		    	while(sample.x >= current_location)
		    	{
		    		x_index++;
		    		current_location += cell_width;
		    	}
		    	
		    	current_location = raster_ymin + cell_width;
		    	int    y_index = 0;
		    	while(sample.y > current_location)
		    	{
		    		y_index++;
		    		current_location += cell_width;
		    	}
		    	
		    	ArrayList sample_list = segment2_data[y_index][x_index];
		    	sample_list.add(sample);
		    	isPopulated2[y_index][x_index] = true;
		    	sampleNumber2[y_index][x_index]++;
		    }
			
			
			number_of_cells = raster_xdim * raster_ydim;
			System.out.println("The number of cells in the raster are " + number_of_cells);
			
			int number_of_populated_cells = 0;
			
			for (int i = 0; i < raster_ydim; i++)
			{
				for (int j = 0; j < raster_xdim; j++)
				{
					if(isPopulated1[i][j])
						number_of_populated_cells++;	
				}
			}
			System.out.println("The number of cells populated by the first segment are " + number_of_populated_cells);
			
			
            number_of_populated_cells = 0;
			
			for (int i = 0; i < raster_ydim; i++)
			{
				for (int j = 0; j < raster_xdim; j++)
				{
					if(isPopulated2[i][j])
						number_of_populated_cells++;	
				}
			}
			System.out.println("The number of cells populated by the next segment are " + number_of_populated_cells);
			
			*/
			
			
			
			
		}
		
		
		
		
		
		
		
		
		
		
		
        /*
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		canvas = new PathCanvas();
		canvas.setSize(650, 900);

		frame.getContentPane().add(canvas, BorderLayout.CENTER);
		frame.pack();
        */
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
					
					ArrayList first_intersecting_data_list = new ArrayList();
					int size = line_list.size();
					int number_of_samples = 0;
					for (int j = 0; j < size; j++)
					{
						Sample sample = (Sample) line_list.get(j);
						if (intersecting_area.contains(sample.x * 100, sample.y * 100))
						{
							number_of_samples++;
							first_intersecting_data_list.add(sample);
						}
					}
					//System.out.println("Number of samples in intersecting area in line " + i + " is " + number_of_samples);
					size = first_intersecting_data_list.size();
					//System.out.println("Size of sample list is " + size);
					
					double xmax = -Double.MAX_VALUE;
					double xmin = Double.MAX_VALUE;
					double ymax = -Double.MAX_VALUE;
					double ymin = Double.MAX_VALUE;
					if(i == 22)
					{
						try (PrintWriter output = new PrintWriter("first_segment.txt"))
					    {
						    for(int j = 0; j < size; j++)
						    {
						    	Sample sample = (Sample)first_intersecting_data_list.get(j);
						    	if(xmin > sample.x)
						    		xmin = sample.x;
						    	if(xmax < sample.x)
						    		xmax = sample.x;
						    	if(ymin > sample.y)
						    		ymin = sample.y;
						    	if(ymax < sample.y)
						    		ymax = sample.y;
						    	output.println(String.format("%.2f", sample.x) + " " + String.format("%.2f", sample.y) + " " + String.format("%.2f", sample.intensity));
						    }
						    output.close();
					    }
						catch (Exception ex)
					    {
						    System.out.println(ex.toString());
					    }
					}
					line_list = (ArrayList) line_data.get(i + 1);
					size = line_list.size();
					number_of_samples = 0;
					ArrayList last_intersecting_data_list = new ArrayList();
					for(int j = 0; j < size; j++)
					{
						
						Sample sample = (Sample) line_list.get(j);
						if (intersecting_area.contains(sample.x * 100, sample.y * 100))
						{
							number_of_samples++;
							last_intersecting_data_list.add(sample);
							
						}
					}
					//System.out.println("Number of samples in intersecting area in line " + (i + 1) + " is " + number_of_samples);
					size = last_intersecting_data_list.size();
					//System.out.println("Size of sample list is " + size);
					if(i == 22)
					{
						try (PrintWriter output = new PrintWriter("last_segment.txt"))
					    {
		
						    for(int j = 0; j < size; j++)
						    {
						    	Sample sample = (Sample)last_intersecting_data_list.get(j);
						    	if(xmin > sample.x)
						    		xmin = sample.x;
						    	if(xmax < sample.x)
						    		xmax = sample.x;
						    	if(ymin > sample.y)
						    		ymin = sample.y;
						    	if(ymax < sample.y)
						    		ymax = sample.y;
						    	output.println(String.format("%.2f", sample.x) + " " + String.format("%.2f", sample.y) + " " + String.format("%.2f", sample.intensity));
						    }
						    output.close();
						    System.out.println("Xmax for segments is " + String.format("%.2f", xmax));
						    System.out.println("Xmin for segments is " + String.format("%.2f", xmin));
						    System.out.println("Ymax for segments is " + String.format("%.2f", ymax));
						    System.out.println("Ymin for segments is " + String.format("%.2f", ymin));
						    
						    double x_range = xmax - xmin;
						    
						    double cell_width = .003;
						    double init_value = xmin - .0015;
						    double end_value  = xmax + .0015;
						    int number_of_cells = 0;
						    System.out.println("First bounding value is " + String.format("%.4f", init_value));
						    while(init_value < end_value)		
						    {
						        number_of_cells++;
						        init_value += cell_width;
						    }
						    System.out.println("Last bounding value is " + String.format("%.4f", init_value));
						    System.out.println("Raster xdim is " + number_of_cells);
						    
						    int raster_xdim = number_of_cells;
						    
						    init_value = ymin - .0015;
						    end_value  = ymax + .0015;
						    number_of_cells = 0;
						    System.out.println("First bounding value is " + String.format("%.4f", init_value));
						    while(init_value < end_value)		
						    {
						        number_of_cells++;
						        init_value += cell_width;
						    }
						    System.out.println("Last bounding value is " + String.format("%.4f", init_value));
						    System.out.println("Raster ydim is " + number_of_cells);
						    
						    int raster_ydim = number_of_cells;
						    
						    double raster_xmin = xmin - .0015;
							double raster_xmax = xmax + .0015;
							double raster_ymin = ymin - .0015;
							double raster_ymax = ymax + .0015;
							
							double raster_xlength = raster_xmax - raster_xmin;
							double raster_ylength = raster_ymax - raster_ymin;
						    
						    ArrayList [][] segment1_data = new ArrayList[raster_ydim][raster_xdim];
							for (int j = 0; j < raster_ydim; j++)
								for (int k = 0; k < raster_xdim; k++)
									segment1_data[j][k] = new ArrayList();	
							
							ArrayList [][] segment2_data = new ArrayList[raster_ydim][raster_xdim];
							for (int j = 0; j < raster_ydim; j++)
								for (int k = 0; k < raster_xdim; k++)
									segment2_data[j][k] = new ArrayList();	
							
							
							size = first_intersecting_data_list.size();
							for(int j = 0; j < size; j++)
						    {
						    	Sample sample = (Sample)first_intersecting_data_list.get(j);
						    	
						    	/*
						    	double normal_x_location = (sample.x - raster_xmin) / raster_xlength;
						    	double x_location        = normal_x_location * raster_xdim; 
						    	int    x_index           = (int)Math.floor(x_location);
						    	double cell_lower_bound  = x_index * .003 + raster_xmin;
						    	double cell_upper_bound  = cell_lower_bound + .003;
						    	*/
						    	
						    	double current_location = raster_xmin + cell_width;
						    	int    x_index          = 0;
						    	while(sample.x > current_location)
						    	{
						    		x_index++;
						    		current_location += cell_width;
						    	}
						    	double cell_lower_bound  = x_index * .003 + raster_xmin;
						    	double cell_upper_bound  = cell_lower_bound + .003;
						    	
						    	System.out.println("Cell lower bound is " + String.format("%.4f", cell_lower_bound));
						    	System.out.println("Cell upper bound is " + String.format("%.4f", cell_upper_bound));
						    	System.out.println("Sample is           " + String.format("%.4f", sample.x));
						    	System.out.println();
						    	
						    	// This breaks, even though it works with x.  A real puzzler, but 
						    	// there is another method that is just a little less efficient.
						    	
						    	/*
						    	double normal_y_location = (sample.y - raster_ymin) / raster_ylength;
						    	double y_location        = normal_y_location * raster_ydim; 
						    	int    y_index           = (int)Math.floor(y_location);
						    	cell_lower_bound  = y_index * .003 + raster_ymin;
						    	cell_upper_bound  = cell_lower_bound + .003;
						    	
						    	System.out.println("Cell lower bound is " + String.format("%.4f", cell_lower_bound));
						    	System.out.println("Cell upper bound is " + String.format("%.4f", cell_upper_bound));
						    	System.out.println("Sample is           " + String.format("%.4f", sample.y));
						    	System.out.println();
						    	*/
						    	
						    	current_location = raster_ymin + cell_width;
						    	int    y_index = 0;
						    	while(sample.y > current_location)
						    	{
						    		y_index++;
						    		current_location += cell_width;
						    	}
						    	
						    	
						    	
						       
						    	
						    	
						    }
						    
						    
					    }
						catch (Exception ex)
					    {
						    System.out.println(ex.toString());
					    }
					}
					
					double perimeter = getPerimeter(path_iterator);
					String perimeter_string = String.format("%,.2f", perimeter);
					//System.out.println("Perimeter of intersecting area is " + perimeter_string);
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
