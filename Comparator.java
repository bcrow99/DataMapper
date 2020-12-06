import java.io.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class Comparator
{
	public static void main(String[] args) throws IOException, FileNotFoundException
	{
		if (args.length == 7)
		{
			Comparator comparator = new Comparator(Integer.valueOf(args[0]), Integer.valueOf(args[1]),
					Integer.valueOf(args[2]), Double.valueOf(args[3]), Double.valueOf(args[4]), Double.valueOf(args[5]),
					Integer.valueOf(args[6]));
		} 
		else
		{
			System.out.println(
					"Usage: Comparator <line #> <line #> <resolution> <start y> <y range> <offset> <reduction>");
			System.exit(0);
		}
	}

	public Comparator(int line1, int line2, int resolution, double start_y, double range, double offset, int reduction)
	{
		ArrayList complete_sample_list = new ArrayList();
		double xmin = 0;
		double xmax = 0;
		double ymin = 0;
		double ymax = 0;
		File file = new File("C:/Users/Brian Crowley/Desktop/CleanData.txt");
		if (file.exists())
		{
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				String line = reader.readLine();
				StringTokenizer number_tokens = new StringTokenizer(line, " ");
				int length = number_tokens.countTokens();
				int number_of_entries = length / 3;

				double x = Double.valueOf(number_tokens.nextToken());
				double y = Double.valueOf(number_tokens.nextToken());
				double intensity = Double.valueOf(number_tokens.nextToken());
				Sample init_sample = new Sample(x, y, intensity);
				xmin = xmax = x;
				ymin = ymax = y;
				complete_sample_list.add(init_sample);
				for (int i = 1; i < number_of_entries; i++)
				{
					x = Double.valueOf(number_tokens.nextToken());
					y = Double.valueOf(number_tokens.nextToken());
					if (x < xmin)
						xmin = x;
					else if (x > xmax)
						xmax = x;
					if (y < ymin)
						ymin = y;
					else if (y > ymax)
						ymax = y;
					intensity = Double.valueOf(number_tokens.nextToken());
					Sample current_sample = new Sample(x, y, intensity);
					complete_sample_list.add(current_sample);
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
								x = Double.valueOf(number_tokens.nextToken());
								y = Double.valueOf(number_tokens.nextToken());
								if (x < xmin)
									xmin = x;
								else if (x > xmax)
									xmax = x;
								if (y < ymin)
									ymin = y;
								else if (y > ymax)
									ymax = y;
								intensity = Double.valueOf(number_tokens.nextToken());
								Sample current_sample = new Sample(x, y, intensity);
								complete_sample_list.add(current_sample);
							}
						}
					} catch (IOException e)
					{
						System.out.println("Unexpected error " + e.toString());
					}
				}
				reader.close();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		} else
		{
			System.out.println("File not found.");
			System.exit(0);
		}

		int[][] line_array = ObjectMapper.getLineArray();
		int start = line_array[line1][0];
		int stop = line_array[line1][1];
		ArrayList line1_sample_list = new ArrayList();
		for (int i = start; i < stop; i++)
		{
			Sample sample = (Sample) complete_sample_list.get(i);
			sample.x -= xmin;
			sample.y -= ymin;
			line1_sample_list.add(sample);
		}
		start = line_array[line2][0];
		stop = line_array[line2][1];
		ArrayList line2_sample_list = new ArrayList();
		for (int i = start; i < stop; i++)
		{
			Sample sample = (Sample) complete_sample_list.get(i);
			sample.x -= xmin;
			sample.y -= ymin;
			line2_sample_list.add(sample);
		}
		
		double increment = range / resolution;
		double current_y = start_y;
		double[] line1_interpolation = new double[resolution];
		double[] line2_interpolation = new double[resolution];
		double[] x1_interpolation = new double[resolution];
		double[] x2_interpolation = new double[resolution];
		double[] xdelta = new double[resolution];
		double[] difference = new double[resolution];
		double[] line1_delta = new double[resolution - 1];
		double[] line2_delta = new double[resolution - 1];
		double[] line1_reduction = new double[resolution - 1];
		double[] line2_reduction = new double[resolution - 1];

		if (line1 % 2 == 0)
		{
		    int sensor = 0;
		    
		    Sample current_sample  = (Sample) line1_sample_list.get(sensor);
		    Sample previous_sample = current_sample;
		    int i = 1;
		    while(current_sample.y < start_y)
		    { 
		    	previous_sample = current_sample;
		    	current_sample = (Sample) line1_sample_list.get(sensor + i * 5);
		    	i++;
		    }
		    current_y = start_y;
		    int number_of_samples = 1;
		    for(int j = 0; j < resolution; j++)
		    {
		    	if (current_sample.y == current_y)
				{
					line1_interpolation[j] = current_sample.intensity;
					x1_interpolation[j]    = current_sample.x;
					System.out.println("Exact match for line 1.");
				} 
				else
				{
					// System.out.println("The index of a bounding sample was " + (sensor + i * 5));
					double distance1 = Math.abs(current_y - previous_sample.y);
					double distance2 = Math.abs(current_y - current_sample.y);
					double total_distance = distance1 + distance2;
					line1_interpolation[j] = previous_sample.intensity * (distance2 / total_distance);
					line1_interpolation[j] += current_sample.intensity * (distance1 / total_distance);

					double xdifference = Math.abs(previous_sample.x - current_sample.x);
					if (previous_sample.x < current_sample.x)
						x1_interpolation[j] = previous_sample.x + xdifference * (distance2 / total_distance);
					else
						x1_interpolation[j] = current_sample.x + xdifference * (distance1 / total_distance);
					if(j == 0)
						number_of_samples++;
				}
		    	current_y += increment;
		    	if(current_sample.y < current_y)
		    		number_of_samples++;
		    	while(current_sample.y < current_y)
			    { 
			    	previous_sample = current_sample;
			    	current_sample = (Sample) line1_sample_list.get(sensor + i * 5);
			    	i++;
			    }
		    }  
		    System.out.println("Number of samples from line 1 was " + number_of_samples);
		    
		    current_sample  = (Sample) line2_sample_list.get(sensor);
		    previous_sample = current_sample;
		    i = 1;
		    while(current_sample.y > current_y)
		    { 
		    	previous_sample = current_sample;
		    	current_sample = (Sample) line2_sample_list.get(sensor + i * 5);
		    	i++;
		    }
		    number_of_samples = 1;
		    for(int j = 0; j < resolution; j++)
		    {
		    	if (current_sample.y + offset == current_y)
				{
					line2_interpolation[resolution - 1 - j] = current_sample.intensity;
					x2_interpolation[resolution - 1 - j]    = current_sample.x;
					System.out.println("Exact match for line 2.");
				} 
				else
				{
					//System.out.println("The index of a bounding sample was " + (sensor + i * 5));
					double distance1 = Math.abs(current_y - (previous_sample.y + offset));
					double distance2 = Math.abs(current_y - (current_sample.y + offset));
					double total_distance = distance1 + distance2;
					line2_interpolation[resolution - 1 - j] = previous_sample.intensity * (distance2 / total_distance);
					line2_interpolation[resolution - 1 - j] += current_sample.intensity * (distance1 / total_distance);

					double xdifference = Math.abs(previous_sample.x - current_sample.x);
					if (previous_sample.x < current_sample.x)
					    x2_interpolation[resolution - 1 - j] = previous_sample.x + xdifference * (distance2 / total_distance);
					else
						x2_interpolation[resolution - 1 - j] = current_sample.x + xdifference * (distance1 / total_distance);
					if(j == 0)
						number_of_samples++;
				}
		    	current_y -= increment;
		    	if(current_sample.y + offset > current_y)
		    		number_of_samples++;
		    	while(current_sample.y + offset > current_y)
			    { 
			    	previous_sample = current_sample;
			    	current_sample = (Sample) line2_sample_list.get(sensor + i * 5);
			    	i++;
			    }
		    } 
		    System.out.println("Number of samples from line 2 was " + number_of_samples);
		}
		else  // line1 % 2 == 1
		{
            int sensor = 4;
		    
		    Sample current_sample  = (Sample) line1_sample_list.get(sensor);
		    Sample previous_sample = current_sample;
		    int i = 1;
		    current_y = start_y + resolution * increment;
		    while(current_sample.y > current_y)
		    { 
		    	previous_sample = current_sample;
		    	current_sample = (Sample) line1_sample_list.get(sensor + i * 5);
		    	i++;
		    }
		    int number_of_samples = 1;
		    for(int j = 0; j < resolution; j++)
		    {
		    	if (current_sample.y == current_y)
				{
					line1_interpolation[resolution - 1 - j] = current_sample.intensity;
					x1_interpolation[resolution - 1 - j]    = current_sample.x;
					System.out.println("Exact match for line 1.");
				} 
				else
				{
					// System.out.println("The index of a bounding sample was " + j);
					//previous_sample = (Sample) line1_sample_list.get(sensor + i * 5 - 5);
					double distance1 = Math.abs(current_y - previous_sample.y);
					double distance2 = Math.abs(current_y - current_sample.y);
					double total_distance = distance1 + distance2;
					line1_interpolation[resolution - 1 - j] = previous_sample.intensity * (distance2 / total_distance);
					line1_interpolation[resolution - 1 - j] += current_sample.intensity * (distance1 / total_distance);

					double xdifference = Math.abs(previous_sample.x - current_sample.x);
					if (previous_sample.x < current_sample.x)
						x1_interpolation[j] = previous_sample.x + xdifference * (distance2 / total_distance);
					else
						x1_interpolation[j] = current_sample.x + xdifference * (distance1 / total_distance);
					if(j == 0)
						number_of_samples++;
				}
		    	current_y -= increment;
		    	if(current_sample.y > current_y)
		    		number_of_samples++;
		    	while(current_sample.y > current_y)
			    { 
			    	previous_sample = current_sample;
			    	current_sample = (Sample) line1_sample_list.get(sensor + i * 5);
			    	i++;
			    }
		    }  
		    System.out.println("Number of samples from line 1 was " + number_of_samples);
		    
		    current_y       = start_y;
		    current_sample  = (Sample) line2_sample_list.get(sensor);
		    previous_sample = current_sample;
		    i               = 1;
		    while(current_sample.y < current_y)
		    { 
		    	previous_sample = current_sample;
		    	current_sample  = (Sample) line2_sample_list.get(sensor + i * 5);
		    	i++;
		    }
		    number_of_samples = 1;
		    for(int j = 0; j < resolution; j++)
		    {
		    	if (current_sample.y + offset == current_y)
				{
					line2_interpolation[resolution - 1 - j] = current_sample.intensity;
					x2_interpolation[resolution - 1 - j]    = current_sample.x;
					System.out.println("Exact match for line 2.");
				} 
				else
				{
					//System.out.println("The index of a bounding sample was " + (sensor + i * 5));
					//previous_sample = (Sample) line1_sample_list.get(sensor + i * 5 - 5);
					double distance1 = Math.abs(current_y - (previous_sample.y + offset));
					double distance2 = Math.abs(current_y - (current_sample.y + offset));
					double total_distance = distance1 + distance2;
					line2_interpolation[resolution - 1 - j] = previous_sample.intensity * (distance2 / total_distance);
					line2_interpolation[resolution - 1 - j] += current_sample.intensity * (distance1 / total_distance);

					double xdifference = Math.abs(previous_sample.x - current_sample.x);
					if (previous_sample.x < current_sample.x)
					    x2_interpolation[resolution - 1 - j] = previous_sample.x + xdifference * (distance2 / total_distance);
					else
						x2_interpolation[resolution - 1 - j] = current_sample.x + xdifference * (distance1 / total_distance);
					if(j == 0)
						number_of_samples++;
				}
		    	current_y += increment;
		    	if(current_sample.y + offset < current_y)
		    		number_of_samples++;
		    	while(current_sample.y + offset < current_y)
			    { 
			    	previous_sample = current_sample;
			    	current_sample = (Sample) line2_sample_list.get(sensor + i * 5);
			    	i++;
			    }
		    } 
		    System.out.println("Number of samples from line 2 was " + number_of_samples);
		    
		}
		
		double min_delta = Double.MAX_VALUE;
		double max_delta = 0;
		for (int i = 0; i < resolution; i++)
		{
			xdelta[i] = Math.abs(x1_interpolation[i] - x2_interpolation[i]);
			if (xdelta[i] < min_delta)
				min_delta = xdelta[i];
			else if (xdelta[i] > max_delta)
				max_delta = xdelta[i];
		}
		System.out.println("Min delta was " + min_delta + " and max delta was " + max_delta);

		for (int i = 0; i < resolution - 1; i++)
		{
			line1_delta[i] = (line1_interpolation[i] - line1_interpolation[i + 1]);
			line2_delta[i] = (line2_interpolation[i] - line2_interpolation[i + 1]);
		}
      
		if (reduction == 0)
		{
			double total_difference = 0;
			for (int i = 0; i < xdelta.length; i++)
			{
				if (xdelta[i] < .25)
				{
					difference[i] = Math.abs(line1_interpolation[i] - line2_interpolation[i]);
					if (xdelta[i] == 0)
						total_difference += difference[i];
					else
						total_difference += difference[i] * ((.25 - xdelta[i]) / .25);
				} 
				/*
				else
					System.out.println("X delta out of bounds.");
				*/
			}
			System.out.println("Total difference in intensity is " + total_difference);

			total_difference = 0;
			for (int i = 0; i < line1_delta.length; i++)
			{
				total_difference += Math.abs(line1_delta[i] - line2_delta[i]);
			}
			System.out.println("Total difference in deltas is " + total_difference);

			ArrayList line1_list = new ArrayList();
			for (int i = 0; i < line1_interpolation.length; i++)
			{
				Point2D.Double point = new Point2D.Double();
				point.x = i * increment;
				point.y = line1_interpolation[i];
				line1_list.add(point);
			}

			ArrayList line2_list = new ArrayList();
			for (int i = 0; i < line2_interpolation.length; i++)
			{
				Point2D.Double point = new Point2D.Double();
				point.x = i * increment;
				point.y = line2_interpolation[i];
				line2_list.add(point);
			}

			ArrayList[] line_vector = new ArrayList[2];
			line_vector[0] = line1_list;
			line_vector[1] = line2_list;
			PlotCanvas lines = new PlotCanvas(400, 200, line_vector);
		} 
		else
		{
			double[] xdelta_reduction = reduce(xdelta, 1);

			line1_reduction = reduce(line1_interpolation, 1);
			double[] line1_delta_reduction = reduce(line1_delta, 1);

			line2_reduction = reduce(line2_interpolation, 1);
			double[] line2_delta_reduction = reduce(line2_delta, 1);

			double total_difference = 0;
			for (int i = 0; i < xdelta_reduction.length; i++)
			{
				if (xdelta_reduction[i] < .25)
				{
					difference[i] = Math.abs(line1_reduction[i] - line2_reduction[i]);
					if (xdelta_reduction[i] == 0)
						total_difference += difference[i];
					else
						total_difference += difference[i] * ((.25 - xdelta_reduction[i]) / .25);
				} 
				else
					System.out.println("X delta out of bounds.");
			}
			System.out.println("Total difference in intensity is " + total_difference);

			total_difference = 0;
			for (int i = 0; i < line1_delta_reduction.length; i++)
			{
				total_difference += Math.abs(line1_delta_reduction[i] - line2_delta_reduction[i]);
			}
			System.out.println("Total difference in deltas is " + total_difference);

			ArrayList line1_list = new ArrayList();
			for (int i = 0; i < line1_reduction.length; i++)
			{
				Point2D.Double point = new Point2D.Double();
				point.x = i * increment;
				point.y = line1_reduction[i];
				line1_list.add(point);
			}

			ArrayList line2_list = new ArrayList();
			for (int i = 0; i < line2_reduction.length; i++)
			{
				Point2D.Double point = new Point2D.Double();
				point.x = i * increment;
				point.y = line2_reduction[i];
				line2_list.add(point);
			}

			ArrayList[] line_vector = new ArrayList[2];
			line_vector[0] = line1_list;
			line_vector[1] = line2_list;
			PlotCanvas lines = new PlotCanvas(400, 200, line_vector);
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
}