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

public class Analyzer
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
			System.out.println("Usage: Analyzer <data file> <line number>");
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
					Analyzer interpolater = new Analyzer(filename, line_number);
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

	public Analyzer(String filename, int line_number)
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
		//System.out.println("Xmax for clipped data = " + String.format("%.2f", global_xmax) + ", xmin = " + String.format("%.2f", global_xmin));
		//System.out.println("Ymax for clipped data = " + String.format("%.2f", global_ymax) + ", ymin = " + String.format("%.2f", global_ymin)); 
	
		double global_xrange = global_xmax - global_xmin;
		double global_yrange = global_ymax - global_ymin;
			
		int global_xdim = (int)(global_xrange / .5 + 2);
		int global_ydim = (int)(global_yrange / .04 + 2);
		System.out.println("The ideal raster for this data set has xdim = " + global_xdim + ", ydim = " + global_ydim);
		System.out.println();
		
		ArrayList list =  getIndex(global_xmax, global_ymax, global_xmin, global_ymin);
	        
	    int _xmax = (int)list.get(0);
	    int _ymax = (int)list.get(1);
	    //System.out.println("The max xindex = " + _xmax + ", yindex = " + _ymax);
		
		xmax = -Double.MAX_VALUE;
		xmin = Double.MAX_VALUE;
		ymax = -Double.MAX_VALUE;
		ymin = Double.MAX_VALUE;
		
		
		ArrayList [][] global_raster = new ArrayList[global_ydim][global_xdim];
		ArrayList [][] seg_raster    = new ArrayList[global_ydim][global_xdim];
		ArrayList [][] seg_raster1   = new ArrayList[global_ydim][global_xdim];
		ArrayList [][] seg_raster2   = new ArrayList[global_ydim][global_xdim];
		for(int i = 0; i < global_ydim; i++)
		{
			for(int j = 0; j < global_xdim; j++)
			{
				global_raster[i][j] = new ArrayList();
				seg_raster[i][j]    = new ArrayList();
				seg_raster1[i][j]   = new ArrayList();
				seg_raster2[i][j]   = new ArrayList();
			}
		}
		
		
		
		
		
		
		
		
		
		
		for(int i = 0; i < number_of_lines; i++)
		{
		    ArrayList clipped_list = (ArrayList)clipped_data.get(i);
		    // System.out.println("Size of clipped data list is " + clipped_list.size());
		   
		    for(int j = 0; j < clipped_list.size(); j++)
		    {
			    Sample sample = (Sample) clipped_list.get(j);
			    
			    if(sample.x < xmin)
				    xmin = sample.x;
			    if(sample.x > xmax)
			        xmax = sample.x;
			    if (sample.y < ymin)
			        ymin = sample.y;
			    if (sample.y > ymax)
			        ymax = sample.y;
			}
		    //System.out.println("Number of samples in unclipped area of line " + line_number + " is " + number_of_samples);
		    //System.out.println("Number of rows of data in unclipped area of line " + line_number + " is " + (number_of_samples / 5));
		    //System.out.println("Xmax = " + String.format("%.2f", xmax) + ", xmin = " + String.format("%.2f", xmin));
		    //System.out.println("Ymax = " + String.format("%.2f", ymax) + ", ymin = " + String.format("%.2f", ymin));

	        double xrange = xmax - xmin;
	        double yrange = ymax - ymin;
			
	        //int line_xdim = (int)(xrange / .5 + 2);
	        //int line_ydim = (int)(yrange / .04 + 2);
	        //System.out.println("The ideal raster for this flight line has xdim = " + line_xdim + ", ydim = " + line_ydim);
	      
		    /*
	        // Replace actual intensity values with a gray scale to help evaluate algorithms.
	        for(int j = 0; j < clipped_list.size(); j++)
	        {
	            //Sample sample              = (Sample) clipped_list.get(j);
	            //double synthetic_intensity = (sample.y - global_ymin) / global_yrange;
	            //synthetic_intensity       *= 255;
	            //sample.intensity           = synthetic_intensity;
	            //System.out.println("Sample x = " + sample.x + ", y = " + sample.y + ", intensity = " + sample.intensity);
	        }
	        */
		
	        for(int j = 0; j < clipped_list.size() - 5; j += 5)
	        {
	            for(int k = j; k < j + 4; k++)
	            {
	                Sample lower_left  = (Sample)clipped_list.get(k);
	                Sample lower_right = (Sample)clipped_list.get(k + 1);
	                Sample upper_left  = (Sample)clipped_list.get(k + 5);
	                Sample upper_right = (Sample)clipped_list.get(k + 6);
	    	    
	                if(lower_left.x == upper_left.x && lower_left.y == upper_left.y)
	                {
	                    System.out.println("Different samples have the same location");
	                }
		    	
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
		    	            double intensity     = DataMapper.getLinearInterpolation(location, upper_left, upper_right, lower_right, lower_left);
		    	        	//double intensity = ((y_value - global_ymin) / global_yrange) * 255.;
		    	        	//System.out.println("Lower left y = " + lower_left.y + ", upper left y = " + upper_left.y);
		    	        	//System.out.println("Y value is " + y_value);
		    	        	//System.out.println();
		    	        	
		    	            Sample sample        = new Sample(x_value, y_value, intensity);
		    	            ArrayList index_list = getIndex(x_value, y_value, global_xmin, global_ymin);
		    	            int x_index          = (int)index_list.get(0);
		    	            int y_index          = (int)index_list.get(1);
		    	            ArrayList cell_list  = global_raster[y_index][x_index];
		    	            cell_list.add(sample);
		    	            if(i >= line_number && i < line_number + 2)
		    	            {
		    	            	cell_list  = seg_raster[y_index][x_index];
		    	            	cell_list.add(sample);
		    	            }
		    	            if(i == line_number)
		    	            {
		    	            	cell_list  = seg_raster1[y_index][x_index];
		    	            	cell_list.add(sample);	
		    	            }
		    	            else if(i == line_number + 1)
		    	            {
		    	            	cell_list  = seg_raster2[y_index][x_index];
		    	            	cell_list.add(sample);	
		    	            }
				        }
		    	        else
		    	        {
		    	            if(lower_left.y < lower_right.y)
		    	                ymin = lower_right.y;
		    	            else
		    	                ymin = lower_left.y;
		    	            location = getIdealLocation(xmin, ymin, global_xmin, global_ymin);
		    	            x_value = location.getX();
		    	            y_value = location.getY();
		    	            if(cell.contains(x_value, y_value))
		    	            {
			    	    	    double intensity     = DataMapper.getLinearInterpolation(location, upper_left, upper_right, lower_right, lower_left);
			    	    	    //double intensity = ((y_value - global_ymin) / global_yrange) * 255.;
		    	            	
			    	    	    Sample sample        = new Sample(intensity, x_value, y_value);
			    	    	    ArrayList index_list = getIndex(x_value, y_value, global_xmin, global_ymin);
			    	    	    int x_index          = (int)index_list.get(0);
			    	    	    int y_index          = (int)index_list.get(1);
			    	    	    ArrayList cell_list  = global_raster[y_index][x_index];
			    	    	    cell_list.add(lower_left);
			    	    	    if(i >= line_number && i < line_number + 2)
			    	            {
			    	            	cell_list  = seg_raster[y_index][x_index];
			    	            	cell_list.add(sample);
			    	            }
			    	    	    if(i == line_number)
			    	            {
			    	            	cell_list  = seg_raster1[y_index][x_index];
			    	            	cell_list.add(sample);	
			    	            }
			    	            else if(i == line_number + 1)
			    	            {
			    	            	cell_list  = seg_raster2[y_index][x_index];
			    	            	cell_list.add(sample);	
			    	            }
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
				    	    	    double intensity     = DataMapper.getLinearInterpolation(location, upper_left, upper_right, lower_right, lower_left);
				    	    	    //double intensity = ((y_value - global_ymin) / global_yrange) * 255.;
				    	    	    
				    	    	    Sample sample        = new Sample(intensity, x_value, y_value);
				    	    	    ArrayList index_list = getIndex(x_value, y_value, global_xmin, global_ymin);
				    	    	    int x_index          = (int)index_list.get(0);
				    	    	    int y_index          = (int)index_list.get(1);
				    	    	    ArrayList cell_list  = global_raster[y_index][x_index];
				    	    	    cell_list.add(sample);
				    	    	    if(i >= line_number && i < line_number + 2)
				    	            {
				    	            	cell_list  = seg_raster[y_index][x_index];
				    	            	cell_list.add(sample);
				    	            }
				    	    	    if(i == line_number)
				    	            {
				    	            	cell_list  = seg_raster1[y_index][x_index];
				    	            	cell_list.add(sample);	
				    	            }
				    	            else if(i == line_number + 1)
				    	            {
				    	            	cell_list  = seg_raster2[y_index][x_index];
				    	            	cell_list.add(sample);	
				    	            }
				    	    	}
		    	            }
				        }
		    	    }
		    	    else if(x_value == xmin && y_value == ymin)
		    	    {
		    		    if(lower_left.x == xmin && lower_left.y == ymin)
		    		    {
		    			    //System.out.println("Lower left sample is at ideal location.");
		    			    ArrayList index_list = getIndex(x_value, y_value, global_xmin, global_ymin);
		    			    int x_index = (int)index_list.get(0);
		    		        int y_index = (int)index_list.get(1);
		    		        ArrayList cell_list = global_raster[y_index][x_index];
		    		        cell_list.add(lower_left);
		    		        if(i >= line_number && i < line_number + 2)
		    	            {
		    	            	cell_list  = seg_raster[y_index][x_index];
		    	            	cell_list.add(lower_left);
		    	            }
		    		        if(i == line_number)
		    	            {
		    	            	cell_list  = seg_raster1[y_index][x_index];
		    	            	cell_list.add(lower_left);	
		    	            }
		    	            else if(i == line_number + 1)
		    	            {
		    	            	cell_list  = seg_raster2[y_index][x_index];
		    	            	cell_list.add(lower_left);	
		    	            }
		    		    }
		    	    }
		    	}
		    }
		}
		
		int    number_of_interpolated_values = 0;
		int    number_of_occupied_cells      = 0;
		double intensity_min                 = Double.MAX_VALUE;
		double intensity_max                 = -Double.MAX_VALUE;
		
		for(int i = 0; i < global_ydim; i++)
		{
			for(int j = 0; j < global_xdim; j++)
			{
				ArrayList cell_list  = seg_raster[i][j];
				if(cell_list.size() != 0)
				{
					number_of_interpolated_values++;
					number_of_occupied_cells++;
					Sample sample = (Sample)cell_list.get(0);
					if(sample.intensity < intensity_min)
						intensity_min = sample.intensity;
					if(sample.intensity > intensity_max)
						intensity_max = sample.intensity;
					if(cell_list.size() == 2)
					{
						number_of_interpolated_values++;
						sample = (Sample)cell_list.get(1);
						if(sample.intensity < intensity_min)
							intensity_min = sample.intensity;
						if(sample.intensity > intensity_max)
							intensity_max = sample.intensity;	
					}
				}
			}
		}
		
		System.out.println("Number of interpolated values is " + number_of_interpolated_values);
		System.out.println("Number of occupied cells is " + number_of_occupied_cells);
		int number_of_overlapping_cells = number_of_interpolated_values - number_of_occupied_cells;
		//int total_number_of_cells = global_xdim * global_ydim;
		System.out.println("Number of overlapping values is " + number_of_overlapping_cells);
		
		//System.out.println("Intensity min = " + intensity_min + " , intensity max = " + intensity_max);
	    //double intensity_range = intensity_max - intensity_min;
	    //System.out.println("Intensity range = " + intensity_range);
	 
		ArrayList seg_index_list = new ArrayList();
		for(int i = 0; i < global_ydim; i++)
		{
			for(int j = 0; j < global_xdim; j++)
			{
				ArrayList cell_list  = seg_raster[i][j];
				if(cell_list.size() == 2)
				{
					int index[] = new int[2];
					index[0]    = i;
					index[1]    = j;
					seg_index_list.add(index);
				}
			}
		}
		
		int[] index    = (int[])seg_index_list.get(0);
		int   seg_xmin = index[1];
		int   seg_xmax = index[1];
		int   seg_ymin = index[0];
		int   seg_ymax = index[0];
		
		for(int i = 1; i < seg_index_list.size(); i++)
		{
			index = (int[])seg_index_list.get(i);
			if(seg_xmin > index[1])
			    seg_xmin = index[1];
			if(seg_xmax < index[1])
			    seg_xmax = index[1];
			if(seg_ymin > index[0])
			    seg_ymin = index[0];
			if(seg_ymax < index[0])
			    seg_ymax = index[0];
		}
		
		int seg_xdim = seg_xmax - seg_xmin + 1;
		int seg_ydim = seg_ymax - seg_ymin + 1;
		
		System.out.println("Segment xdim = " + seg_xdim + ", ydim = " + seg_ydim);
		//System.out.println("Origin is x = " + seg_xmin + ", y = " + seg_ymin);
		
		double data1[][]            = new double[seg_ydim][seg_xdim];
		double data2[][]            = new double[seg_ydim][seg_xdim];
		int data_image1[][]         = new int[seg_ydim][seg_xdim];
		int data_image2[][]         = new int[seg_ydim][seg_xdim];
		boolean isInterpolated1[][] = new boolean[seg_ydim][seg_xdim];
		boolean isInterpolated2[][] = new boolean[seg_ydim][seg_xdim];
		
		for(int i = 0; i < seg_ydim; i++)
		{
			for(int j = 0; j < seg_xdim; j++)
			{
				isInterpolated1[i][j] = false;
				isInterpolated2[i][j] = false;
			}
		}
		
		for(int i = seg_ymin; i < (seg_ymax + 1); i++)
        {
            for(int j = seg_xmin; j < (seg_xmax + 1); j++)
            {  	
                ArrayList cell_list = global_raster[i][j];
                if(cell_list.size() == 0)
                {
                    data1[i - seg_ymin][j - seg_xmin] = 0;
                    data2[i - seg_ymin][j - seg_xmin] = 0;
                    data_image1[i - seg_ymin][j - seg_xmin] = 0;
                    data_image2[i - seg_ymin][j - seg_xmin] = 0;
                }
                else if(cell_list.size() == 1)
                {
                	cell_list = seg_raster1[i][j];
                	if(cell_list.size() == 0)
                	{
                		data1[i - seg_ymin][j - seg_xmin]       = 0;
                		data_image1[i - seg_ymin][j - seg_xmin] = 0;
                		
                	}
                	else
                	{
                		Sample sample                           = (Sample)cell_list.get(0);	
                		data1[i - seg_ymin][j - seg_xmin]       = sample.intensity;
                		data_image1[i - seg_ymin][j - seg_xmin] = (int)(sample.intensity * 100);
                		isInterpolated1[i - seg_ymin][j - seg_xmin] = true;
                	}
                	cell_list = seg_raster2[i][j];
                	if(cell_list.size() == 0)
                	{
                		data2[i - seg_ymin][j - seg_xmin]       = 0;
                		data_image2[i - seg_ymin][j - seg_xmin] = 0;
                	}
                	else
                	{
                		Sample sample                           = (Sample)cell_list.get(0);	
                		data2[i - seg_ymin][j - seg_xmin]       = sample.intensity;
                		data_image2[i - seg_ymin][j - seg_xmin] = (int)(sample.intensity * 100);
                		isInterpolated2[i - seg_ymin][j - seg_xmin] = true;
                	}
                }
                else if(cell_list.size() == 2)
                {
                	Sample sample1 = (Sample)cell_list.get(0);
                	Sample sample2 = (Sample)cell_list.get(1);
                	data1[i - seg_ymin][j - seg_xmin]       = sample1.intensity;
                    data_image1[i - seg_ymin][j - seg_xmin] = (int)(sample1.intensity * 100);
                    data2[i - seg_ymin][j - seg_xmin]       = sample2.intensity;
                    data_image2[i - seg_ymin][j - seg_xmin] = (int)(sample2.intensity * 100);	
                    isInterpolated1[i - seg_ymin][j - seg_xmin] = true;
                    isInterpolated2[i - seg_ymin][j - seg_xmin] = true;
                }	
            }
        }
		
		double[][] dilated_data1     = ImageMapper.getImageDilation(data1, isInterpolated1);
		double[][] dilated_data2     = ImageMapper.getImageDilation(data2, isInterpolated1);
		
		for(int i = 0; i < seg_ydim; i++)
		{
			for(int j = 0; j < seg_xdim; j++) 
			{
				data_image1[i][j] = (int)(dilated_data1[i][j] * 100);
				data_image2[i][j] = (int)(dilated_data1[i][j] * 100);
			}
		}
	
		
		double[][] expanded_data1 = ImageMapper.expandX(data1, 12);
		double[][] expanded_data2 = ImageMapper.expandX(data2, 12);
		double[][] shrunken_expanded_data1 = ImageMapper.shrinkAvg(expanded_data1);
		double[][] shrunken_expanded_data2 = ImageMapper.shrinkAvg(expanded_data2);
		
		int _ydim = shrunken_expanded_data1.length;
		int _xdim = shrunken_expanded_data1[0].length;
		
		int[][] _data1 = new int[_ydim][_xdim];
		int[][] _data2 = new int[_ydim][_xdim];
		
		for(int i = 0; i < _ydim; i++)
		{
			for(int j = 0; j < _xdim; j++)
			{
				_data1[i][j] = (int) (shrunken_expanded_data1[i][j] * 100);
				_data2[i][j] = (int) (shrunken_expanded_data2[i][j] * 100);
			}
		}
		
		double[] result              = ImageMapper.getTranslation(_data1, _data2);
		//double[] result              = ImageMapper.getTranslation(data_image1, data_image2);
		
		System.out.println("Result was type " + result[0]);
		System.out.println("Xshift = " + result[1] + ", yshift = " + result[2]);
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

	
    public ArrayList getIndex(double x_value, double y_value, double global_xmin, double global_ymin)
    {
    	ArrayList index_list = new ArrayList();
    	int x_index = 0;
    	double current_location = global_xmin;
    	while(current_location < x_value)
    	{
    		x_index++;
    		current_location += .5;
    		
    		//System.out.println("x index = " + x_index);
    		//System.out.println("location = " + current_location);
    		//System.out.println();
    	}
    	index_list.add(x_index);
    	
    	int y_index = 0;
    	current_location = global_ymin;
    	while(current_location < y_value)
    	{
    		y_index++;
    		current_location += .04;
    	}
    	index_list.add(y_index);
    	//System.out.println("x location = " + x_value + ", y location = " + y_value);
    	//System.out.println("x index = " + x_index + ", y index = " + y_index);
    	//System.out.println("x min = " + global_xmin + ", y min = " + global_ymin);
    	//System.out.println();
    	return(index_list);
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
    	
    	//System.out.println("Start x = " + start_x + ", start y = " + start_y);
    	//System.out.println("Ideal x = " + x_value + ", ideal y = " + y_value);
    	//System.out.println();
	    Point2D.Double location = new Point2D.Double(x_value, y_value);	
	    return(location);
	}
}
