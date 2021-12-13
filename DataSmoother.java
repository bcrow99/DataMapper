import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class DataSmoother
{
	public static void main(String[] args) throws IOException
	{
		if (args.length != 2)
		{
			System.out.println("Usage: DataSmoother <filename> <# of iterations>");
			System.exit(0);
		}
		else
		{
			String filename   = args[0];
			int    iterations = Integer.parseInt(args[1]);
			DataSmoother smoother = new DataSmoother(filename, iterations);
		}
	}
	
	public DataSmoother(String filename, int iterations)
	{
	    System.out.println("Filename is " + filename);
	    System.out.println("Number of iterations is " + iterations);
	    
	    File file = new File(filename);
		if (file.exists())
		{
			double global_xmin          = Double.MAX_VALUE;
			double global_xmax          = 0;
			double global_ymin          = Double.MAX_VALUE;
			double global_ymax          = 0;
			double global_intensity_min = Double.MAX_VALUE;
			double global_intensity_max = -Double.MAX_VALUE;
			ArrayList original_data = new ArrayList();
			
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
					double intensity = Double.valueOf(number_tokens.nextToken());
					if (x < global_xmin)
						global_xmin = x;
					else if (x > global_xmax)
						global_xmax = x;
					if (y < global_ymin)
						global_ymin = y;
					else if (y > global_ymax)
						global_ymax = y;
					if (intensity < global_intensity_min)
						global_intensity_min = intensity;
					else if (intensity > global_intensity_max)
						global_intensity_max = intensity;
					Sample current_sample = new Sample(x, y, intensity);
					original_data.add(current_sample);
				}
				
				while(line != null)
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
								double intensity = Double.valueOf(number_tokens.nextToken());
								if (x < global_xmin)
									global_xmin = x;
								else if (x > global_xmax)
									global_xmax = x;
								if (y < global_ymin)
									global_ymin = y;
								else if (y > global_ymax)
									global_ymax = y;
								if (intensity < global_intensity_min)
									global_intensity_min = intensity;
								else if (intensity > global_intensity_max)
									global_intensity_max = intensity;
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
			} 
			catch (IOException e)
			{
				System.out.println("Unexpected error " + e.toString());
			}
			
			System.out.println("Data size is " + original_data.size());
			ArrayList relative_data = new ArrayList();
			for(int i = 0; i < original_data.size(); i++)
			{
				Sample sample = (Sample) original_data.get(i);
				Sample new_sample = new Sample();
				new_sample.x = sample.x - global_xmin;
				new_sample.y = sample.y - global_ymin;
				new_sample.intensity = sample.intensity;
				relative_data.add(new_sample);
			}
			
			ArrayList data           = new ArrayList();
			double    total_distance = 0;
			
			
			for(int i = 0; i < 5; i++)
			{
				Sample sample          = (Sample) relative_data.get(i);
			    Sample adjusted_sample = new Sample(sample.x, total_distance, sample.intensity);
			    data.add(adjusted_sample);
			}
			
			for(int i = 7; i < relative_data.size(); i += 5)
			{
				Sample current_sample   = (Sample) relative_data.get(i);
				Sample previous_sample  = (Sample) relative_data.get(i - 5);
				double axis             = getDistance(current_sample.x, current_sample.y, previous_sample.x, previous_sample.y);
				total_distance         += axis;
				
				current_sample          = (Sample) relative_data.get(i - 2);
				Sample adjusted_sample  = new Sample(current_sample.x, total_distance, current_sample.intensity);
				data.add(adjusted_sample);
				
				current_sample   = (Sample) relative_data.get(i - 1);
				adjusted_sample  = new Sample(current_sample.x, total_distance, current_sample.intensity);
				data.add(adjusted_sample);
				
				current_sample   = (Sample) relative_data.get(i);
				adjusted_sample  = new Sample(current_sample.x, total_distance, current_sample.intensity);
				data.add(adjusted_sample);
				
				current_sample   = (Sample) relative_data.get(i + 1);
				adjusted_sample  = new Sample(current_sample.x, total_distance, current_sample.intensity);
				data.add(adjusted_sample);

				current_sample      = (Sample) relative_data.get(i + 2);
				adjusted_sample  = new Sample(current_sample.x, total_distance, current_sample.intensity);
				data.add(adjusted_sample);
			}
			
			ArrayList sensor_data = new ArrayList();
			for(int i = 0; i < 5; i++)
			{
				ArrayList sensor_list = new ArrayList();
				sensor_data.add(sensor_list);
			}
		
			for(int i = 0; i < data.size(); i += 5)
			{
				int       k           = 0;
			    for(int j = i; j < i + 5; j++)	
			    {
			    	Sample    sample      = (Sample)relative_data.get(j);
			    	ArrayList sensor_list = (ArrayList)sensor_data.get(k++);
			    	sensor_list.add(sample);
			    }
			}
			
			ArrayList point_data = new ArrayList();
			for(int i = 0; i < 5; i++)
			{
				ArrayList point_list = new ArrayList();
				point_data.add(point_list);
			}
			
			for(int i = 0; i < 5; i++)
			{
				ArrayList sample_list = (ArrayList)sensor_data.get(i);
				System.out.println("Sensor " + i + " has " + sample_list.size() + " pieces of data.");
				ArrayList point_list  = (ArrayList)point_data.get(i);
				for(int j = 0; j < sample_list.size(); j++)
				{
					Sample sample        = (Sample)sample_list.get(j);
					
					double point[] = new double[3];
					
					point[0]       = sample.x;
					point[1]       = sample.y;
					point[2]       = sample.intensity;
					point_list.add(point);
				}
			}
			
			ArrayList segmented_data = new ArrayList();
			
			for(int i = 0; i < 5; i++)
			{
				ArrayList point_list = (ArrayList)point_data.get(i);
				ArrayList segmented_list = new ArrayList();
				int       size       = point_list.size();
				
				double [] x = new double[size];
				double [] y = new double[size];
				double [] z = new double[size];
				for(int j = 0; j < size; j++)
				{
					double[] point = (double[])point_list.get(j);
					x[j] = point[0];
					y[j] = point[1];
					z[j] = point[2];
				}
				double [] smooth_x = smooth(x, iterations);
				double [] smooth_y = smooth(y, iterations);
				double [] smooth_z = smooth(z, iterations);
				
				double  start_value    = smooth_z[0];
				int     index          = 1;
				int     length         = smooth_z.length;
				
				boolean increasing    = false;
				if(smooth_z[1] > smooth_z[0])
					increasing = true;
				
				double [] initial_point = new double[3];
				initial_point[0] = smooth_x[0];
				initial_point[1] = smooth_y[0];
				initial_point[2] = smooth_z[0];
				
				
				// Add initial point to use as reference to calculate slope.
				segmented_list.add(initial_point);
				
				while(index < length)
				{
					if(smooth_z[index] < (smooth_z[index - 1] - 1) && increasing)
					{
						double [] point = new double[3];
						point[0]        = smooth_x[index - 1];
						point[1]        = smooth_y[index - 1];
						point[2]        = smooth_z[index - 1];
						segmented_list.add(point);
						
						
						start_value    = smooth_z[index - 1];
						increasing = false;
					}	
					else if(smooth_z[index] > (smooth_z[index - 1] + 1) && !increasing)
					{
						double [] point = new double[3];
						point[0]        = smooth_x[index - 1];
						point[1]        = smooth_y[index - 1];
						point[2]        = smooth_z[index - 1];
						segmented_list.add(point);
						
						
						start_value    = smooth_z[index - 1];
						increasing = true;    	
					}
					// Add final point even if slope is not changing sign so it can serve as a reference.
					else if(index == length - 1) 
					{
						double [] point = new double[3];
						point[0]        = smooth_x[index - 1];
						point[1]        = smooth_y[index - 1];
						point[2]        = smooth_z[index - 1];
						segmented_list.add(point);	
					}
						
					index++;
				}
				segmented_data.add(segmented_list);
			}
			int number_of_lists = segmented_data.size();
			System.out.println("Number of lists is " + number_of_lists);
			for(int i = 0; i < number_of_lists; i++)
			{
				ArrayList segmented_list      = (ArrayList)segmented_data.get(i);
				int       number_of_entries = segmented_list.size();
				System.out.println("List " + i + " has " + number_of_entries + " entries.");
			}
		}
		else
		{
			System.out.println("Could not find file.");
		}
	}
	
	public double getDistance(double x, double y, double x_origin, double y_origin)
	{
	    double distance  = Math.sqrt((x - x_origin) * (x - x_origin) + (y - y_origin) * (y - y_origin));
	    return(distance);
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
}