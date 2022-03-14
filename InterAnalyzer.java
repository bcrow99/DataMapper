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
			
		    
			
		    double yrange = ymax - ymin;
		    double xrange = xmax - xmin;
		    
		    double ratio              = 0;
		    int    number_of_sections = 0;
		    if(yrange > xrange)
		    {
		    	ratio              = yrange / xrange;
		    	number_of_sections = (int)ratio;
		    	number_of_sections++;
		    }
		    else if(xrange > yrange)
			{
			    ratio              = xrange / yrange;
			    number_of_sections = (int)ratio;
			    number_of_sections++;
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
	    	for(int i = 0; i < size; i++)
		    {
		    	Sample    sample = (Sample)first_intersecting_data_list.get(i);
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
	    	for(int i = 0; i < size; i++)
		    {
		    	Sample    sample = (Sample)next_intersecting_data_list.get(i);
		    	int       index  = (int)Math.floor((float)((sample.y - ymin) / yrange) * number_of_sections);
		    	if(index == number_of_sections)
		    	{
		    		//If this is our max value (1), stick it in the final bin.
		    		index--;
		    	}
		    	
		    	lower_bound = index * y_increment + ymin;
		    	upper_bound = lower_bound + y_increment;
		    	
		    	/*
		    	System.out.println("Lower bound is " + String.format("%.4f", lower_bound));
		    	System.out.println("Value is " + String.format("%.4f", sample.y));
		    	System.out.println("Upper bound is " + String.format("%.4f", upper_bound));
		    	System.out.println();
		    	*/
		    	
		    	
		    	
		    	ArrayList list   = (ArrayList)segment_data[1][index];
		    	list.add(sample);
		    }
	    	
	    	
	    	for(int i = 0; i < number_of_sections; i++)
	    	{
	    		xmax = -Double.MAX_VALUE;
				xmin = Double.MAX_VALUE;
				ymax = -Double.MAX_VALUE;
				ymin = Double.MAX_VALUE;
				
	    		ArrayList first_list = segment_data[0][i];
	    		size                 = first_list.size();
	    		for(int j = 0; j < size; j++)
	    		{
	    			Sample sample = (Sample)first_list.get(j);
	    			if(sample.x < xmin)
	    				xmin = sample.x;
	    			if(sample.x > xmax)
	    				xmax = sample.x;
	    			if(sample.y < ymin)
	    				ymin = sample.y;
	    			if(sample.y > ymax)
	    				ymax = sample.y;
	    		}
	    		
	    		ArrayList next_list = segment_data[1][i];
	    		size                = next_list.size();
	    		for(int j = 0; j < size; j++)
	    		{
	    			Sample sample = (Sample)next_list.get(j);
	    			if(sample.x < xmin)
	    				xmin = sample.x;
	    			if(sample.x > xmax)
	    				xmax = sample.x;
	    			if(sample.y < ymin)
	    				ymin = sample.y;
	    			if(sample.y > ymax)
	    				ymax = sample.y;
	    		}
	    		
	    		System.out.println("Xmax for section " + i + " is " + String.format("%.2f", xmax));
			    System.out.println("Xmin for section " + i + " is " + String.format("%.2f", xmin));
			    System.out.println("Ymax for section " + i + " is " + String.format("%.2f", ymax));
			    System.out.println("Ymin for section " + i + " is " + String.format("%.2f", ymin));
			    
			    double x_range = xmax - xmin;
			    
			    double xcell_width = .125;
			    double init_value = xmin - xcell_width / 2;
			    double end_value  = xmax + xcell_width / 2;
			    int number_of_cells = 0;
			   
			    while(init_value < end_value)		
			    {
			        number_of_cells++;
			        init_value += xcell_width;
			    }
			    
			    System.out.println("Raster xdim is " + number_of_cells);
			    
			    int raster_xdim = number_of_cells;
			    
			    double ycell_width = .025;
			    init_value = ymin - ycell_width / 2;
			    end_value  = ymax + ycell_width / 2;
			    number_of_cells = 0;
			    
			    while(init_value < end_value)		
			    {
			        number_of_cells++;
			        init_value += ycell_width;
			    }
			    
			    System.out.println("Raster ydim is " + number_of_cells);
			    
			    int raster_ydim = number_of_cells;
			    
			    double raster_xmin = xmin - xcell_width / 2;
				double raster_xmax = xmax + xcell_width / 2;
				double raster_ymin = ymin - ycell_width / 2;
				double raster_ymax = ymax + ycell_width / 2;
				
				ArrayList[][] segment1_data = new ArrayList[raster_ydim][raster_xdim];
				ArrayList[][] segment2_data = new ArrayList[raster_ydim][raster_xdim];
				boolean[][] isPopulated1  = new boolean[raster_ydim][raster_xdim];
				boolean[][] isPopulated2  = new boolean[raster_ydim][raster_xdim];
				int[][] sampleNumber1 = new int[raster_ydim][raster_xdim];
				int [][] sampleNumber2 = new int[raster_ydim][raster_xdim];
				
				for (int j = 0; j < raster_ydim; j++)
				{
					for (int k = 0; k < raster_xdim; k++)
					{
						segment1_data[j][k] = new ArrayList();
						segment2_data[j][k] = new ArrayList();
						isPopulated1[j][k]  = false;
						isPopulated2[j][k]  = false;
						sampleNumber1[j][k] = 0;
						sampleNumber2[j][k] = 0;
					}
				}
				
				System.out.println("The number of cells in the raster are " + (raster_xdim * raster_ydim));
				ArrayList segment_list = segment_data[0][i];
				size = segment_list.size();
				System.out.println("The number of samples being assigned to the raster from the first segment is " + size);
				for(int j = 0; j < size; j++)
				{
					Sample sample = (Sample)segment_list.get(j);
					double current_location = raster_xmin + xcell_width;
			    	int    x_index          = 0;
			    	while(sample.x >= current_location)
			    	{
			    		x_index++;
			    		current_location += xcell_width;
			    	}
			    	double cell_lower_bound  = x_index * xcell_width + raster_xmin;
			    	double cell_upper_bound  = cell_lower_bound + xcell_width;
			    	current_location = raster_ymin + xcell_width;
			    	int    y_index = 0;
			    	while(sample.y > current_location)
			    	{
			    		y_index++;
			    		current_location += ycell_width;
			    	}
			    	
			    	cell_lower_bound  = y_index * ycell_width + raster_ymin;
			    	cell_upper_bound  = cell_lower_bound + ycell_width;
			    	
			    	//System.out.println("Cell lower bound is " + String.format("%.5f", cell_lower_bound));
			    	//System.out.println("Cell upper bound is " + String.format("%.5f", cell_upper_bound));
			    	//System.out.println("Sample is           " + String.format("%.5f", sample.y));
			    	//System.out.println();
			    	
			    	ArrayList sample_list = segment1_data[y_index][x_index];
			    	sample_list.add(sample);
			    	isPopulated1[y_index][x_index] = true;
			    	sampleNumber1[y_index][x_index]++;
				}
				
				int number_of_populated_cells = 0;
				
				for(int j = 0; j < raster_ydim; j++)
				{
				    for(int k = 0; k < raster_xdim; k++)
				    {
				        if(isPopulated1[j][k])
				        	number_of_populated_cells++;
				    }
				}
				System.out.println("The number of populated cells in the first raster is " + number_of_populated_cells);
				
				
				segment_list = segment_data[1][i];
				size = segment_list.size();
				System.out.println("The number of samples being assigned to the raster from the second segment is " + size);
				for(int j = 0; j < size; j++)
				{
					Sample sample = (Sample)segment_list.get(j);
					double current_location = raster_xmin + xcell_width;
			    	int    x_index          = 0;
			    	while(sample.x >= current_location)
			    	{
			    		x_index++;
			    		current_location += xcell_width;
			    	}
			    	double cell_lower_bound  = x_index * xcell_width + raster_xmin;
			    	double cell_upper_bound  = cell_lower_bound + xcell_width;
			    	current_location = raster_ymin + ycell_width;
			    	int    y_index = 0;
			    	while(sample.y > current_location)
			    	{
			    		y_index++;
			    		current_location += ycell_width;
			    	}
			    	
			    	cell_lower_bound  = y_index * ycell_width + raster_ymin;
			    	cell_upper_bound  = cell_lower_bound + ycell_width;
			    	ArrayList sample_list = segment2_data[y_index][x_index];
			    	sample_list.add(sample);
			    	isPopulated2[y_index][x_index] = true;
			    	sampleNumber2[y_index][x_index]++;
				}
				

				number_of_populated_cells = 0;
				
				for(int j = 0; j < raster_ydim; j++)
				{
				    for(int k = 0; k < raster_xdim; k++)
				    {
				        if(isPopulated2[j][k])
				        	number_of_populated_cells++;
				    }
				}
				System.out.println("The number of populated cells in the second raster is " + number_of_populated_cells);
				
				int number_of_unpopulated_cells_without_neighbors = 0;
				double ycenter = ymin;
				for(int j = 0; j < raster_ydim; j++)
				{
					double xcenter = xmin;
					for(int k = 0; k < raster_xdim; k++)
					{
					     
						int       location_type = getLocationType(k, j, raster_xdim, raster_ydim);
						boolean[] isPopulated   = new boolean[9];
						
						for(int m = 0; m < 9; m++)
						{
							isPopulated[m] = false;
						}
						
						ArrayList sample_list = (ArrayList)segment1_data[j][k];  
					     
					    if(sample_list.size() == 0)
					    {
					    	// If the cell is unpopulated, we want to find the two closest
					    	// samples from cells in opposing directions.
					        if(location_type == 5)
					        {
					            ArrayList neighbor_list = new ArrayList();
					            
					            ArrayList list          = (ArrayList)segment1_data[j - 1][k - 1]; 
					            neighbor_list.add(list);
					            list          = (ArrayList)segment1_data[j - 1][k]; 
					            neighbor_list.add(list);
					            list          = (ArrayList)segment1_data[j - 1][k + 1]; 
					            neighbor_list.add(list);
					            list          = (ArrayList)segment1_data[j][k - 1]; 
					            neighbor_list.add(list);
					            // Include the list from this cell.
					            neighbor_list.add(sample_list);
					            list          = (ArrayList)segment1_data[j][k + 1]; 
					            neighbor_list.add(list);
					            list          = (ArrayList)segment1_data[j + 1][k - 1]; 
					            neighbor_list.add(list);
					            list          = (ArrayList)segment1_data[j + 1][k]; 
					            neighbor_list.add(list);
					            list          = (ArrayList)segment1_data[j + 1][k + 1];
					            neighbor_list.add(list);
					            
					            for(int m = 0; m < 9; m++)
					            {
					            	list = (ArrayList)neighbor_list.get(m);
					            	if(list.size() > 0)
					            		isPopulated[m] = true;
					            }
					            
					            double current_distance = -1;
					            
					            if(!isPopulated[1] && !isPopulated[7])
					            	number_of_unpopulated_cells_without_neighbors++;
					            else if(isPopulated[1] && isPopulated[7])
					            {
					            	ArrayList north_list = (ArrayList)segment1_data[j - 1][k];
					            	ArrayList south_list = (ArrayList)segment1_data[j + 1][k];
					            	// Set the boolean as if we assigned a value;
					            	isPopulated1[j][k] = true;
					            	
					            	
					            }
					            if(isPopulated[3] || isPopulated[5])
					            {
					            	//System.out.println("Neighborhood contains samples in west or east direction.");	
					            	isPopulated1[j][k] = true;
					            }
					            /*
					            if(isPopulated[0] && isPopulated[8])
					            {
					            	System.out.println("Neighborhood contains samples in northwest and southeast direction.");
					            }
					            if(isPopulated[1] && isPopulated[7])
					            {
					            	System.out.println("Neighborhood contains samples in north and south direction.");	
					            }
					            if(isPopulated[2] && isPopulated[6])
					            {
					            	System.out.println("Neighborhood contains samples in northwest and southeast direction.");	
					            }
					            if(isPopulated[3] && isPopulated[5])
					            {
					            	System.out.println("Neighborhood contains samples in west and east direction.");	
					            }
					            */
					        }
					     }
					     else if(sample_list.size() == 1)
					     {
					    	Sample sample   = (Sample)sample_list.get(0);
					    	int    quadrant = 0;
					        if(sample.y < ycenter)
					        {
					             if(sample.x < xcenter)
					             {
					                 quadrant = 1;	 
					             }
					             else
					             {
					                 quadrant = 2;	 
					             }
					        }
					        else 
					        {
					        	if(sample.x < xcenter)
					            {
					                 quadrant = 3;	 
					            }
					            else
					            {
					                quadrant = 4;	 
					            }   	 
					        }
					         
					        if(location_type == 5)
					        {
					        	if(quadrant == 1)
					        	{
					        	    // We want an opposite sample from SE neighbor.
					        	}
					        	else if(quadrant == 2)
					        	{
					        	    // We want an opposite sample from SW neighbor.
					        	}
					        	else if(quadrant == 3)
					        	{
					        	    // We want an opposite sample from NE neighbor.	
					        	}
					        	else if(quadrant == 4)
					        	{
					        		// We want an opposite sample from NW neighbor.	
					        	}
					        
					        }
					        
					     }
					     else if(sample_list.size() > 1)
					     {
					    	 
					     }
					     
					     xcenter += xcell_width;
					}
					
					ycenter += ycell_width;
				}
				System.out.println("Number of unpopulated cells without neighbors is " + number_of_unpopulated_cells_without_neighbors);
                number_of_populated_cells = 0;
				
				for(int j = 0; j < raster_ydim; j++)
				{
				    for(int k = 0; k < raster_xdim; k++)
				    {
				        if(isPopulated1[j][k])
				        	number_of_populated_cells++;
				    }
				}
				System.out.println("The number of populated cells in the first raster after assigning intermediate values is " + number_of_populated_cells);
				
				System.out.println();
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
	
	
	public int getLocationType(int xindex, int yindex, int xdim, int ydim)
	{ 
		int location_type = 0;
		if(yindex == 0)
		{
		    if(xindex == 0) 
		    {
		        location_type = 1;
		    }
		    else if(xindex % xdim != xdim - 1)
		    {
		        location_type = 2;
		    }
		    else
		    {
		        location_type = 3;
		    }
		}
		else if(yindex % ydim != ydim - 1)
		{
			if(xindex == 0) 
		    {
		        location_type = 4;
		    }
		    else if(xindex % xdim != xdim - 1)
		    {
		    	location_type = 5;
		    }
		    else
		    {
		        location_type = 6;
		    }
		}
		else
		{
	        if(xindex == 0) 
		    {
		        location_type = 7;
		    }
		    else if(xindex % xdim != xdim - 1)
		    {
		        location_type = 8;
		    }
		    else
		    {
		    	location_type = 9;
		    }   
		}
		return(location_type);
	}
}
