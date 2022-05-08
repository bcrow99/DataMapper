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

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.*;

public class Interpolater
{
	ArrayList original_data = new ArrayList();
	ArrayList data = new ArrayList();

	int line = 9;
	int sensor = 3;

	double offset = 15;
	double range = 60;
	double position = 45;

	double xmin = Double.MAX_VALUE;
	double xmax = Double.MIN_VALUE;
	double ymin = Double.MAX_VALUE;
	double ymax = Double.MIN_VALUE;

	public static void main(String[] args)
	{
		String prefix = new String("C:/Users/Brian Crowley/Desktop/");
		// String prefix = new String("");
		if (args.length != 2)
		{
			System.out.println("Usage: Interpolater <data file> <line number>");
			System.exit(0);
		} 
		else
		{
			try
			{
				String filename    = prefix + args[0];
				int    line_number = Integer.parseInt(args[1]);
				try
				{
					Interpolater interpolater = new Interpolater(filename, line_number);
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

	public Interpolater(String filename, int line_number)
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

				// System.out.println("Xmin is " + xmin);
				// System.out.println("Ymin is " + ymin);
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

		for (int i = 0; i < 5; i++)
		{
			ArrayList data_list = new ArrayList();
			for (int j = i; j < data.size(); j += 5)
			{
				Sample sample = (Sample) data.get(j);
				data_list.add(sample);
			}
			segment_array.add(data_list);
		}

		for (int i = 0; i < 5; i++)
		{
			ArrayList data_list = (ArrayList) segment_array.get(i);
			int size = data_list.size();
			double x[] = new double[size];
			double y[] = new double[size];
			double z[] = new double[size];

			for (int j = 0; j < size; j++)
			{
				Sample sample = (Sample) data_list.get(j);
				x[j] = sample.x;
				y[j] = sample.y;
				z[j] = sample.intensity;
			}

			double smooth_x[] = smooth(x, 2);
			double smooth_y[] = smooth(y, 2);
			double smooth_z[] = smooth(z, 2);

			data_list.clear();
			int length = smooth_x.length;
			for (int j = 0; j < length; j++)
			{
				Sample sample = new Sample(smooth_x[j], smooth_y[j], smooth_z[j]);
				data_list.add(sample);
			}
		}

		ArrayList sensor_0 = (ArrayList) segment_array.get(0);
		ArrayList sensor_1 = (ArrayList) segment_array.get(1);
		ArrayList sensor_2 = (ArrayList) segment_array.get(2);
		ArrayList sensor_3 = (ArrayList) segment_array.get(3);
		ArrayList sensor_4 = (ArrayList) segment_array.get(4);

		// All the lists should be the same size.
		int size = sensor_0.size();

		// Replace the interleaved relative data with a shorter list of smoothed data.
		data.clear();
		for (int i = 0; i < size; i++)
		{
			Sample sample = (Sample) sensor_0.get(i);
			data.add(sample);
			sample = (Sample) sensor_1.get(i);
			data.add(sample);
			sample = (Sample) sensor_2.get(i);
			data.add(sample);
			sample = (Sample) sensor_3.get(i);
			data.add(sample);
			sample = (Sample) sensor_4.get(i);
			data.add(sample);
		}

		// Get an index of endpoints by checking when the order of the x-coordinates
		// changes.
		int first_index = 0;
		int last_index = 4;
		Sample init_sample = (Sample) data.get(first_index);
		Sample last_sample = (Sample) data.get(last_index);

		boolean init_direction_north = true;
		boolean headed_north = true;
		if (init_sample.x < last_sample.x)
		{
			headed_north = false;
			init_direction_north = false;
		}

		ArrayList endpoint_index = new ArrayList();

		while (last_index < data.size() - 5)
		{
			first_index += 5;
			last_index += 5;
			init_sample = (Sample) data.get(first_index);
			last_sample = (Sample) data.get(last_index);
			if (headed_north)
			{
				if (init_sample.x < last_sample.x)
				{
					endpoint_index.add(first_index);
					headed_north = false;
				}
			} 
			else
			{
				if (init_sample.x > last_sample.x)
				{
					endpoint_index.add(first_index);
					headed_north = true;
				}
			}
		}

		int number_of_lines = endpoint_index.size() + 1;
		System.out.println("The number of lines in the data set is " + number_of_lines);
		int[][] line_index = new int[number_of_lines][2];
		line_index[0][0] = 0;
		for (int i = 0; i < number_of_lines - 1; i++)
		{
			int index = (int) endpoint_index.get(i);
			line_index[i][1] = index;
			line_index[i + 1][0] = index;
		}
		line_index[number_of_lines - 1][1] = data.size() - 1;
		
		
		
		double global_xmax     = -Double.MAX_VALUE;
		double global_xmin     = Double.MAX_VALUE;
		double global_ymax     = -Double.MAX_VALUE;
		double global_ymin     = Double.MAX_VALUE;
		ArrayList line_data    = new ArrayList();
		ArrayList clipped_data = new ArrayList();
		
		for (int i = 0; i < number_of_lines; i++)
		{
			int start              = line_index[i][0];
			int stop               = line_index[i][1];
			ArrayList line_list    = new ArrayList();
			ArrayList clipped_list = new ArrayList();
			if (i % 2 == 0)
			{
				for(int j = start; j < stop; j += 5)
				{
					for(int k = j + 4; k >= j; k--)
					{
					    Sample sample = (Sample) data.get(k);
					    line_list.add(sample);
					}
				}
			} 
			else
			{
				for(int j = stop - 1; j >= start; j -= 5)
				{
					for(int k = j - 4; k <= j; k++)
					{
					    Sample sample = (Sample) data.get(k);
					    line_list.add(sample);
					}
				}
			}
			line_data.add(line_list);
		}

		for(int i = 0; i < line_data.size(); i++)
		{
			ArrayList line_list    = (ArrayList) line_data.get(i);
			ArrayList clipped_list = new ArrayList();
			for(int j = 0; j < line_list.size(); j += 5)
			{
				Sample center_sample = (Sample) line_list.get(j + 2);
			    if(center_sample.y >= offset && center_sample.y < (offset + range)) 
			    {
			    	for(int k = j; k < j + 5; k++)
			    	{
			    		Sample sample = (Sample)line_list.get(k);
			    		clipped_list.add(sample);
			    		if(global_xmax < sample.x)
			    			global_xmax = sample.x;
			    		if(global_xmin > sample.x)
			    			global_xmin = sample.x;
			    		if(global_ymax < sample.y)
			    			global_ymax = sample.y;
			    		if(global_ymin > sample.y)
			    			global_ymin = sample.y;
			    	}
			    }	
			}
			clipped_data.add(clipped_list);
		}
		System.out.println("Xmax for clipped data = " + String.format("%.2f", global_xmax) + ", xmin = " + String.format("%.2f", global_xmin));
		System.out.println("Ymax for clipped data = " + String.format("%.2f", global_ymax) + ", ymin = " + String.format("%.2f", global_ymin)); 
	
		double xrange = global_xmax - global_xmin;
		double yrange = global_ymax - global_ymin;
			
		int global_xdim = (int)(xrange / .5);
		int global_ydim = (int)(yrange / .04);
		System.out.println("The ideal raster for this data set has xdim = " + global_xdim + ", ydim = " + global_ydim);
		System.out.println();
		
		xmax = -Double.MAX_VALUE;
		xmin = Double.MAX_VALUE;
		ymax = -Double.MAX_VALUE;
		ymin = Double.MAX_VALUE;
		
		ArrayList [][] global_raster = new ArrayList[global_ydim][global_xdim];
		for(int i = 0; i < global_ydim; i++)
		{
			for(int j = 0; j < global_xdim; j++)
			{
				global_raster[i][j] = new ArrayList();
			}
		}
		
		ArrayList clipped_list = (ArrayList)clipped_data.get(line_number);
		System.out.println("Size of clipped data list is " + clipped_list.size());
		int number_of_samples = 0;
		for(int i = 0; i < clipped_list.size(); i++)
		{
			Sample sample = (Sample) clipped_list.get(i);
			number_of_samples++;
			if(sample.x < xmin)
				xmin = sample.x;
			if(sample.x > xmax)
				xmax = sample.x;
			if (sample.y < ymin)
				ymin = sample.y;
			if (sample.y > ymax)
				ymax = sample.y;
		}
		System.out.println("Number of samples in unclipped area of line " + line_number + " is " + number_of_samples);
		System.out.println("Number of rows of data in unclipped area of line " + line_number + " is " + (number_of_samples / 5));
		
		System.out.println("Xmax = " + String.format("%.2f", xmax) + ", xmin = " + String.format("%.2f", xmin));
		System.out.println("Ymax = " + String.format("%.2f", ymax) + ", ymin = " + String.format("%.2f", ymin));

	    xrange = xmax - xmin;
		yrange = ymax - ymin;
			
		int line_xdim = (int)(xrange / .5);
		int line_ydim = (int)(yrange / .04);
		System.out.println("The ideal raster for this flight line has xdim = " + line_xdim + ", ydim = " + line_ydim);
		
		// Replace actual intensity values with a gray scale to help evaluate algorithms.
		for(int i = 0; i < clipped_list.size(); i++)
		{
			Sample sample = (Sample) clipped_list.get(i);
			double synthetic_intensity = (sample.y - ymin) / (ymax - ymin);
			synthetic_intensity       *= 200;
			synthetic_intensity       -= 100;
			sample.intensity = synthetic_intensity;
		}
		
		for(int i = 0; i < clipped_list.size() - 5; i += 5)
		{
		    for(int j = i; j < i + 4; j++)
		    {
	    	    Sample lower_left  = (Sample)clipped_list.get(j);
	    	    Sample lower_right = (Sample)clipped_list.get(j + 1);
	    	    Sample upper_left  = (Sample)clipped_list.get(j + 5);
	    	    Sample upper_right = (Sample)clipped_list.get(j + 6);
		    	
		    	Point2D.Double lower_left_point  = new Point2D.Double(lower_left.x, lower_left.y);
		    	Point2D.Double lower_right_point = new Point2D.Double(lower_right.x, lower_right.y);
		    	Point2D.Double upper_left_point  = new Point2D.Double(upper_left.x, upper_left.y);
		    	Point2D.Double upper_right_point = new Point2D.Double(upper_right.x, upper_right.y);
		    	
		    	Path2D.Double cell = new Path2D.Double();
		    	cell.moveTo(lower_left.x, lower_left.y);
				cell.lineTo(upper_left.x, upper_left.y);
				cell.lineTo(upper_right.x, upper_right.y);
				cell.lineTo(lower_right.x, lower_right.y);
				cell.closePath();
		    	
		    	if(lower_left.x < upper_left.x)
		    		xmin = lower_left.x;
		    	else
		    		xmin = upper_left.x;
		    	if(lower_right.x > upper_right.x)
		    		xmax = lower_right.x;
		    	else
		    		xmax = upper_right.x;
		    	if(upper_left.y > upper_right.y)
		    	    ymax = upper_left.y;
		    	else
		    		ymax = upper_right.y;
		    	if(lower_left.y < lower_right.y)
		    		ymin = lower_left.y;
		    	else
		    		ymin = lower_right.y;
		    	
		    	
		    	Point2D.Double location = getIdealLocation(xmin, ymin, global_xmin, global_ymin);
		    	double x_value = location.getX();
		    	double y_value = location.getY();
		    	if(x_value != xmin && y_value != ymin)
		    	{
		    	    if(cell.contains(x_value, y_value))
				    {
		    	    	/*
				        System.out.println("Cell contains calculated ideal location.");	
				        double area = DataMapper.getQuadrilateralArea(lower_left_point, upper_left_point, upper_right_point, lower_right_point);
			    		System.out.println("The area of the quadrilateral is " + String.format("%.2f", area));
			    		System.out.println();
			    		*/
				    }
				    else
				    {
					    //System.out.println("Calculated ideal location is not within cell at j = " + j);
					    // Try changing ymin.
					    if(lower_left.y < lower_right.y)
			    		    ymin = lower_right.y;
			    	    else
			    		    ymin = lower_left.y;
					    location = getIdealLocation(xmin, ymin, global_xmin, global_ymin);
			    	    x_value = location.getX();
			    	    y_value = location.getY();
			    	    if(cell.contains(x_value, y_value))
					    {
			    	    	/*
					        System.out.println("Cell contains calculated ideal location starting from larger ymin.");
					        double area = DataMapper.getQuadrilateralArea(lower_left_point, upper_left_point, upper_right_point, lower_right_point);
				    		System.out.println("The area of the quadrilateral is " + String.format("%.2f", area));
				    		System.out.println();
				    		*/
					    }
			    	    else
			    	    {
			    		    if(lower_left.x < upper_left.x)
				    		    xmin = upper_left.x;
				    	    else
				    		    xmin = lower_left.x;	
			    		    location = getIdealLocation(xmin, ymin, global_xmin, global_ymin);
				    	    x_value = location.getX();
				    	    y_value = location.getY();
				    	    if(cell.contains(x_value, y_value))
						    {
				    	    	/*
						        System.out.println("Cell contains calculated ideal location starting from larger xmin.");
						        double area = DataMapper.getQuadrilateralArea(lower_left_point, upper_left_point, upper_right_point, lower_right_point);
					    		System.out.println("The area of the quadrilateral is " + String.format("%.2f", area));
					    		System.out.println();
					    		*/
						    }
				    	    else
				    	    {
				    		    System.out.println("Did not find ideal location inside cell.");
				    		    if(x_value >= xmax)
				    		    {
				    		        System.out.println("X value is greater than or equal to xmax.")	;
				    		    }
				    		    if(y_value >= ymax)
				    		    {
				    		    	System.out.println("Y value is greater than or equal to yxmax.")	;   	
				    		    }
				    		    
				    		    if(y_value < ymax && x_value < xmax)
				    		    {
				    		    	System.out.println("Lower left x = "  + String.format("%.2f", lower_left.x)  + ", y = " + String.format("%.2f", lower_left.y));
						    		System.out.println("Upper left x = "  + String.format("%.2f", upper_left.x)  + ", y = " + String.format("%.2f", upper_left.y));
						    		System.out.println("Upper right x = " + String.format("%.2f", upper_right.x) + ", y = " + String.format("%.2f", upper_right.y));
						    		System.out.println("Lower right x = " + String.format("%.2f", lower_right.x) + ", y = " + String.format("%.2f", lower_right.y));
						    		
						    		System.out.println("Xmin = " + String.format("%.3f", xmin) + ", xmid = " + String.format("%.3f", x_value) + ", xmax = " + String.format("%.3f", xmax));
						    		System.out.println("Ymin = " + String.format("%.3f", ymin) + ", ymid = " + String.format("%.3f", y_value) + ", ymax = " + String.format("%.3f", ymax));
						    		double area = DataMapper.getQuadrilateralArea(lower_left_point, upper_left_point, upper_right_point, lower_right_point);
						    		System.out.println("The area of the quadrilateral is " + String.format("%.2f", area));	
				    		    }
				    		    
				    		    
				    		    //System.out.println("j = " + j);
				    		    System.out.println();
				    	    }
			    	    }
				    }
		    	}
		    	else if(x_value == xmin && y_value == ymin)
		    	{
		    	     // Find out if lower left hand value falls on an ideal location.
		    		 if(lower_left.x == xmin && lower_left.y == ymin)
		    		 {
		    			 //cell_list.add(lower_left);
		    		 }
		    		 else if(lower_left.x == xmin)
		    		 {
		    			 
		    		 }
		    		 else if(lower_left.y == ymin)
		    		 {
		    			 
		    		 }
		    	}
		    	else if(x_value == xmin)
		    	{
		    		
		    	}
		    	else if(y_value == ymin)
		    	{
		    		
		    	}
		    	
		    	/*
		    	if(lower_left.x < upper_left.x)
		    		xmin = upper_left.x;
		    	else
		    		xmin = lower_left.x;
		    	if(lower_right.x > upper_right.x)
		    		xmax = upper_right.x;
		    	else
		    		xmax = lower_right.x;
		    	if(upper_left.y > upper_right.y)
		    	    ymax = upper_right.y;
		    	else
		    		ymax = upper_left.y;
		    	if(lower_left.y < lower_right.y)
		    		ymin = lower_right.y;
		    	else
		    		ymin = lower_left.y;
		    	*/
		    	
		    	
		    	
		    	/*
		    	int start_whole_part = (int)Math.floor(xmin);
		    	double start_fractional_part = xmin - start_whole_part;
		    	
		    	if(start_fractional_part != .25 && start_fractional_part != .75)
		    	{
		    	    if(start_fractional_part < .25)
		    	    {
		    		    start_fractional_part = .25;
		    	    }
		    	    else if(start_fractional_part < .75)
		    	    {
		    		    start_fractional_part = .75;   
		    	    }
		    	    else
		    	    {
		    		    start_fractional_part = .25; 
		    		    start_whole_part++;
		    	    }
		    	}
		    	
		    	double x_value = start_whole_part + start_fractional_part;
		    	
		    	start_whole_part = (int)Math.floor(ymin);
		    	start_fractional_part = ymin - start_whole_part;
		    	if(start_fractional_part % .04 != 0)
		    	{
		    		double modulus         = start_fractional_part % .04;
	    	  	    double increment       = .04 - modulus;
	    	  	    start_fractional_part += increment;  
		    	}
		    	double y_value = start_whole_part + start_fractional_part;
		    	*/
		    	
		    	
		    	int x_index = 0;
		    	double current_location = global_xmin;
		    	while(current_location < x_value)
		    	{
		    		x_index++;
		    		current_location += .5;
		    	}
		    	
		    	int y_index = 0;
		    	current_location = global_ymin;
		    	while(current_location < y_value)
		    	{
		    		y_index++;
		    		current_location += .04;
		    	}
				
		    	ArrayList cell_list = global_raster[y_index][x_index];
		    	
		    	/*
		    	if(j == 3530)
		    	{
		    		System.out.println("Lower left x = "  + String.format("%.2f", lower_left.x)  + ", y = " + String.format("%.2f", lower_left.y));
		    		System.out.println("Upper left x = "  + String.format("%.2f", upper_left.x)  + ", y = " + String.format("%.2f", upper_left.y));
		    		System.out.println("Upper right x = " + String.format("%.2f", upper_right.x) + ", y = " + String.format("%.2f", upper_right.y));
		    		System.out.println("Lower right x = " + String.format("%.2f", lower_right.x) + ", y = " + String.format("%.2f", lower_right.y));
		    		
		    		System.out.println("Xmin = " + String.format("%.3f", xmin) + ", xmid = " + String.format("%.3f", x_value) + ", xmax = " + String.format("%.3f", xmax));
		    		System.out.println("Ymin = " + String.format("%.3f", ymin) + ", ymid = " + String.format("%.3f", y_value) + ", ymax = " + String.format("%.3f", ymax));
		    		double area = DataMapper.getQuadrilateralArea(lower_left_point, upper_left_point, upper_right_point, lower_right_point);
		    		System.out.println("The area of the quadrilateral is " + String.format("%.2f", area));
		    	}
		    	*/
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
			// System.out.println("Coordinates start at " + coords[0] + ", " + coords[1]);
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
		return (dst);
	}

	public ArrayList getNeighborList(int j, int k,  ArrayList data[][])
	{
		int ydim                = data.length;
		int xdim                = data[0].length;
		int location_type       = getLocationType(k, j, xdim, ydim);
	    
		ArrayList neighbor_list = new ArrayList();
	    
	    Sample    sample;
	    ArrayList sample_list;
	    int       size;
	    
	    if(location_type == 5)
	    {
	    	sample_list = data[j - 1][k - 1];
	    	size        = sample_list.size();
	    	if(size != 0)
	    	{
	    		sample = (Sample)sample_list.get(size - 1);
	    		neighbor_list.add(sample);
	    	}
	    	
	    	sample_list = data[j - 1][k];
	    	size        = sample_list.size();
	    	if(size != 0)
	    	{
	    		sample = (Sample)sample_list.get(size - 1);
	    		neighbor_list.add(sample);
	    	}
			
	    	sample_list = data[j - 1][k + 1];
	    	size        = sample_list.size();
	    	if(size != 0)
	    	{
	    		sample = (Sample)sample_list.get(size - 1);
	    		neighbor_list.add(sample);
	    	}
	    	
	    	sample_list = data[j][k - 1];
	    	size        = sample_list.size();
	    	if(size != 0)
	    	{
	    		sample = (Sample)sample_list.get(size - 1);
	    		neighbor_list.add(sample);
	    	}
	    	
	    	sample_list = data[j][k + 1];
	    	size        = sample_list.size();
	    	if(size != 0)
	    	{
	    		sample = (Sample)sample_list.get(size - 1);
	    		neighbor_list.add(sample);
	    	}
	    	
	    	sample_list = data[j + 1][k - 1];
	    	size        = sample_list.size();
	    	if(size != 0)
	    	{
	    		sample = (Sample)sample_list.get(size - 1);
	    		neighbor_list.add(sample);
	    	}
	    			
	    	sample_list = data[j + 1][k];
	    	size        = sample_list.size();
	    	if(size != 0)
	    	{
	    		sample = (Sample)sample_list.get(size - 1);
	    		neighbor_list.add(sample);
	    	}
			
	    	sample_list = data[j + 1][k + 1];
	    	size        = sample_list.size();
	    	if(size != 0)
	    	{
	    		sample = (Sample)sample_list.get(size - 1);
	    		neighbor_list.add(sample);
	    	}
	    }
	    return(neighbor_list);
	}
	
	public Point2D.Double getIdealLocation(double start_x, double start_y, double global_xmin, double global_ymin)
	{
		int start_whole_part = (int)Math.floor(start_x);
    	double start_fractional_part = start_x - start_whole_part;
    	
    	if(start_fractional_part != .25 && start_fractional_part != .75)
    	{
    	    if(start_fractional_part < .25)
    	    {
    		    start_fractional_part = .25;
    	    }
    	    else if(start_fractional_part < .75)
    	    {
    		    start_fractional_part = .75;   
    	    }
    	    else
    	    {
    		    start_fractional_part = .25; 
    		    start_whole_part++;
    	    }
    	}
    	
    	double x_value = start_whole_part + start_fractional_part;
    	
    	start_whole_part = (int)Math.floor(start_y);
    	start_fractional_part = start_y - start_whole_part;
    	if(start_fractional_part % .04 != 0)
    	{
    		double modulus         = start_fractional_part % .04;
	  	    double increment       = .04 - modulus;
	  	    start_fractional_part += increment;  
    	}
    	double y_value = start_whole_part + start_fractional_part;
    	
	    Point2D.Double location = new Point2D.Double(x_value, y_value);	
	    return(location);
	}
	
	public int getLocationType(int xindex, int yindex, int xdim, int ydim)
	{
		int location_type = 0;
		if (yindex == 0)
		{
			if (xindex == 0)
			{
				location_type = 1;
			} 
			else if (xindex % xdim != xdim - 1)
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
			if (xindex == 0)
			{
				location_type = 4;
			} 
			else if (xindex % xdim != xdim - 1)
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
			if (xindex == 0)
			{
				location_type = 7;
			} 
			else if (xindex % xdim != xdim - 1)
			{
				location_type = 8;
			} 
			else
			{
				location_type = 9;
			}
		}
		return (location_type);
	}
}
