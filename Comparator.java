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
					x1_interpolation[i]    = previous_sample.x * (distance1 / total_distance);
					x1_interpolation[i]    += current_sample.intensity * (distance2 / total_distance);
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
					double distance1 = Math.abs(current_sample.y - previous_sample.y);
					double distance2 = Math.abs(current_y - current_sample.y);
					double total_distance = distance1 + distance2;
					line1_interpolation[i] = previous_sample.intensity * (distance1 / total_distance);
					line1_interpolation[i] += current_sample.intensity * (distance2 / total_distance);
					x1_interpolation[i]    = previous_sample.x * (distance1 / total_distance);
					x1_interpolation[i]    += current_sample.x * (distance2 / total_distance);
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
				    double distance1 = Math.abs(current_sample.y - (previous_sample.y + offset));
				    double distance2 = Math.abs(current_y - (current_sample.y + offset));
				    double total_distance = distance1 + distance2;
				    line2_interpolation[i] = previous_sample.intensity * (distance1 / total_distance);
				    line2_interpolation[i] += current_sample.intensity * (distance2 / total_distance);
				    x2_interpolation[i]    = previous_sample.x * (distance1 / total_distance);
					x2_interpolation[i]    += current_sample.x * (distance2 / total_distance);
				}
			} 
			else // sample.y == current_y
			{
				line2_interpolation[i] = current_sample.intensity;
				x2_interpolation[i]    = current_sample.x;
			}
			current_y += increment;
		}
		
		for(int i = 0; i < resolution; i++)
		{
		    xdelta[i] =  Math.abs(line1_interpolation[i] - line2_interpolation[i]);	
		}
		
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
		}
		System.out.println("Total difference is " + total_difference);
		
		ArrayList line1_list = new ArrayList();
		for(int i = 0; i < resolution; i++)
		{
			Point2D.Double point  = new Point2D.Double();
			point.x               = start_y + i * increment;
			point.y               = line1_interpolation[i];
			line1_list.add(point);
		}
		ArrayList line2_list = new ArrayList();
		for(int i = 0; i < resolution; i++)
		{
			Point2D.Double point  = new Point2D.Double();
			point.x               = start_y + i * increment;
			point.y               = line2_interpolation[i];
			line2_list.add(point);
		}
		ArrayList[] line_vector = new ArrayList[2];
		line_vector[0] = line1_list;
		line_vector[1] = line2_list;
		PlotCanvas linew = new PlotCanvas(200, 400, line_vector);
	}
}