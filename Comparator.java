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
		if (args.length == 6)
		{
			Comparator comparator = new Comparator(Integer.valueOf(args[0]), Integer.valueOf(args[1]),
					Integer.valueOf(args[2]), Double.valueOf(args[3]), Double.valueOf(args[4]), Double.valueOf(args[5]));
		} else
		{
			System.out.println("Usage: Comparator <line #> <line #> <resolution> <start y> <y range> <offset>");
			System.exit(0);
		}
	}

	public Comparator(int line1, int line2, int resolution, double start_y, double range, double offset)
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
		//double range = 5;
		double increment = range / resolution;
		//double current_y = 20;
		double current_y = start_y;
		double[] line1_interpolation = new double[resolution];
		double[] line2_interpolation = new double[resolution];
		double[] x1_interpolation    = new double[resolution];
		double[] x2_interpolation    = new double[resolution];
		double[] xdelta              = new double[resolution];
		double[] difference          = new double[resolution];
		double[] line1_delta         = new double[resolution - 1];
		double[] line2_delta         = new double[resolution - 1];
		double[] delta_difference    = new double[resolution - 1];
		double[] line1_reduction     = new double[resolution - 1];
		double[] line2_reduction     = new double[resolution - 1];
		
		
		//double offset =  0.;

		int sensor = 0;
		if(line1 % 2 == 1)
			sensor = 4;
		for (int i = 0; i < resolution; i++)
		{
			Sample current_sample = (Sample) line1_sample_list.get(sensor);
			if (current_sample.y < current_y)
			{
				int j = sensor + 5;
				while (current_sample.y < current_y)
				{
					current_sample = (Sample) line1_sample_list.get(j);
					j += 5;
				}

				if (current_sample.y == current_y)
				{
					line1_interpolation[i] = current_sample.intensity;
					x1_interpolation[i] = current_sample.x;
					System.out.println("Exact match.");
				} 
				else
				{
					System.out.println("The index of a bounding sample was " + j);
					Sample previous_sample = (Sample) line1_sample_list.get(j - 5);
					double distance1 = Math.abs(current_y - previous_sample.y);
					double distance2 = Math.abs(current_y - current_sample.y);
					double total_distance = distance1 + distance2;
					line1_interpolation[i] = previous_sample.intensity * (distance1 / total_distance);
					line1_interpolation[i] += current_sample.intensity * (distance2 / total_distance);
					
					double xdifference = Math.abs(previous_sample.x - current_sample.x);
					if(previous_sample.x < current_sample.x)
					    x1_interpolation[i]    =  previous_sample.x + xdifference / (distance1 / total_distance);
					else
						x1_interpolation[i]    =  current_sample.x + xdifference / (distance2 / total_distance);
				}
			} 
			else if (current_sample.y > current_y)
			{
				int j = sensor + 5;
				while (current_sample.y > current_y)
				{
					current_sample = (Sample) line1_sample_list.get(j);
					j += 5;
				}
				if (current_sample.y == current_y)
				{
					line1_interpolation[i] = current_sample.intensity;
					x1_interpolation[i] = current_sample.x;
					System.out.println("Exact match.");
				} 
				else
				{
					System.out.println("The index of a bounding sample was " + j);
					Sample previous_sample = (Sample) line1_sample_list.get(j - 5);
					//double distance1 = Math.abs(current_sample.y - previous_sample.y);
					double distance1 = Math.abs(current_y - previous_sample.y);
					double distance2 = Math.abs(current_y - current_sample.y);
					double total_distance = distance1 + distance2;
					line1_interpolation[i] = previous_sample.intensity * (distance1 / total_distance);
					line1_interpolation[i] += current_sample.intensity * (distance2 / total_distance);
					
					double xdifference = Math.abs(previous_sample.x - current_sample.x);
					if(previous_sample.x < current_sample.x)
					    x1_interpolation[i]    =  previous_sample.x + xdifference / (distance1 / total_distance);
					else
						x1_interpolation[i]    =  current_sample.x + xdifference / (distance2 / total_distance);
				}
			} 
			else // sample.y == current_y
			{
				line1_interpolation[i] = current_sample.intensity;
				x1_interpolation[i]    = current_sample.x;
				System.out.println("Exact match.");
			}

			current_sample = (Sample) line2_sample_list.get(sensor);
			if ((current_sample.y + offset) < current_y)
			{
				int j = sensor + 5;
				while ((current_sample.y + offset) < current_y)
				{
					current_sample = (Sample) line2_sample_list.get(j);
					j += 5;
				}
				if ((current_sample.y + offset) == current_y)
				{
					line2_interpolation[i] = current_sample.intensity;
					x2_interpolation[i]    = current_sample.x;
				} 
				else
				{
				    // System.out.println("The index of a bounding sample was " + j);
				    Sample previous_sample = (Sample) line2_sample_list.get(j - 5);
				    double distance1 = Math.abs(current_sample.y - (previous_sample.y + offset));
				    double distance2 = Math.abs(current_y - (current_sample.y + offset));
				    double total_distance = distance1 + distance2;
				    line2_interpolation[i] = previous_sample.intensity * (distance1 / total_distance);
				    line2_interpolation[i] += current_sample.intensity * (distance2 / total_distance);
				    
				    double xdifference = Math.abs(previous_sample.x - current_sample.x);
					if(previous_sample.x < current_sample.x)
					    x2_interpolation[i]    =  previous_sample.x + xdifference / (distance1 / total_distance);
					else
						x2_interpolation[i]    =  current_sample.x + xdifference / (distance2 / total_distance);
				    
				}
			} 
			else if ((current_sample.y + offset) > current_y)
			{
				int j = sensor + 5;
				while ((current_sample.y + offset) > current_y)
				{
					current_sample = (Sample) line2_sample_list.get(j);
					j += 5;
				}
				if ((current_sample.y + offset) == current_y)
				{
					line2_interpolation[i] = current_sample.intensity;
					x2_interpolation[i]    = current_sample.x;
				} 
				else
				{
				    //System.out.println("The index of a bounding sample was " + j);
				    Sample previous_sample = (Sample) line2_sample_list.get(j - 5);
				    double distance1 = Math.abs(current_y - (previous_sample.y + offset));
				    double distance2 = Math.abs(current_y - (current_sample.y + offset));
				    double total_distance = distance1 + distance2;
				    line2_interpolation[i] = previous_sample.intensity * (distance1 / total_distance);
				    line2_interpolation[i] += current_sample.intensity * (distance2 / total_distance);
				    
				    double xdifference = Math.abs(previous_sample.x - current_sample.x);
					if(previous_sample.x < current_sample.x)
					    x2_interpolation[i]    =  previous_sample.x + xdifference / (distance1 / total_distance);
					else
						x2_interpolation[i]    =  current_sample.x + xdifference / (distance2 / total_distance);
				   
				}
			} 
			else // sample.y == current_y
			{
				line2_interpolation[i] = current_sample.intensity;
				x2_interpolation[i]    = current_sample.x;
			}
			current_y += increment;
		}
		
		
		
		
		
		/*
		double total_difference = 0;
		for(int i = 0; i < resolution; i++)
		{
			
			if(xdelta[i] < .25)
			{
			    difference[i]     = Math.abs(line1_interpolation[i] - line2_interpolation[i]);
			    
			    if(xdelta[i] == 0)
			        total_difference += difference[i];
			    else
			    	total_difference += difference[i] * ((.25 - xdelta[i]) / .25);
			}
			else
				System.out.println("X delta out of bounds.");	
		}
		System.out.println("Total difference in intensity is " + total_difference);
		
		total_difference = 0;
		for(int i = 0; i < resolution - 2; i++)
		{
		    line1_delta[i]      = (line1_interpolation[i] - line1_interpolation[i + 1]);
		    line2_delta[i]      = (line2_interpolation[i] - line2_interpolation[i + 1]);
		    line1_reduction[i]  = (line1_interpolation[i] + line1_interpolation[i + 1]) / 2;
		    line2_reduction[i]  = (line2_interpolation[i] + line2_interpolation[i + 1]) / 2;
		    delta_difference[i] = line1_delta[i]          - line2_delta[i];
		    total_difference   += Math.abs(delta_difference[i]);
		}
		System.out.println("Total difference in deltas is " + total_difference);
		
		*/
		
		double min_delta = Double.MAX_VALUE;
		double max_delta = 0;
		for(int i = 0; i < resolution; i++)
		{
		    xdelta[i] =  Math.abs(x1_interpolation[i] - x2_interpolation[i]);
		    if(xdelta[i] < min_delta)
		    	min_delta = xdelta[i];
		    else if(xdelta[i] > max_delta)
                max_delta = xdelta[i];
		}
		System.out.println("Min delta was " + min_delta + " and max delta was " + max_delta);
		
		for(int i = 0; i < resolution - 1; i++)
		{
		    line1_delta[i]      = (line1_interpolation[i] - line1_interpolation[i + 1]);
		    line2_delta[i]      = (line2_interpolation[i] - line2_interpolation[i + 1]);
		}
		
		double[] xdelta_reduction = reduce(xdelta, 2);
		
		line1_reduction = reduce(line1_interpolation, 2);
		double[] line1_delta_reduction = reduce(line1_delta, 2);
		
		line2_reduction = reduce(line2_interpolation, 2);
		double[] line2_delta_reduction = reduce(line2_delta, 2);
	
		double total_difference = 0;
		for(int i = 0; i < xdelta_reduction.length; i++)
		{
			if(xdelta_reduction[i] < .25)
			{
			    difference[i]     = Math.abs(line1_reduction[i] - line2_reduction[i]);  
			    if(xdelta_reduction[i] == 0)
			        total_difference += difference[i];
			    else
			    	total_difference += difference[i] * ((.25 - xdelta_reduction[i]) / .25);
			}
			else
				System.out.println("X delta out of bounds.");	
		}
		System.out.println("Total difference in intensity is " + total_difference);
		
		total_difference = 0;
		for(int i = 0; i < line1_delta_reduction.length; i++)
		{
		    total_difference   += Math.abs(line1_delta_reduction[i] - line2_delta_reduction[i]);
		}
		System.out.println("Total difference in deltas is " + total_difference);
		
		ArrayList line1_list = new ArrayList();
		for(int i = 0; i < line1_reduction.length; i++)
		{
			Point2D.Double point  = new Point2D.Double();
			point.x               = i * increment;
			point.y               = line1_reduction[i];
			line1_list.add(point);
		}

		ArrayList line2_list = new ArrayList();
		for(int i = 0; i < line2_reduction.length; i++)
		{
			Point2D.Double point  = new Point2D.Double();
			point.x               = i * increment;
			point.y               = line2_reduction[i];
			line2_list.add(point);
		}
		
		ArrayList[] line_vector = new ArrayList[2];
		line_vector[0] = line1_list;
		line_vector[1] = line2_list;
		PlotCanvas lines = new PlotCanvas(400, 200, line_vector);
	}
	
	public double[] reduce(double[] source, int iterations)
	{
	    int src_length = source.length;
	    int dst_length = source.length - 1;
	    double[] src = source;
		double[] dst = new double[dst_length];;
	    while(dst_length >= source.length - iterations)
	    {
	        for(int i = 0; i < dst_length; i++)
	        {
	        	dst[i] = (src[i] + src[i + 1]) / 2;
	        }
	        src = dst;
	        dst_length--;
	        if(dst_length >= source.length - iterations)
	        	dst = new double[dst_length];	
	    }
	    return(dst);
	}
	
}