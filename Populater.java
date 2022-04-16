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

public class Populater
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
			System.out.println("Usage: Populater <data file>");
			System.exit(0);
		} else
		{
			try
			{
				String filename = prefix + args[0];
				try
				{
					Populater interpolater = new Populater(filename);
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

	public Populater(String filename)
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
					} catch (IOException e)
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
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		} else
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

		// Get an idex of endpoints by checking when the order of the x-coordinates
		// changes.
		int first_index = 0;
		int last_index = 4;
		Sample first_sample = (Sample) data.get(first_index);
		Sample last_sample = (Sample) data.get(last_index);

		boolean init_direction_north = true;
		boolean headed_north = true;
		if (first_sample.x < last_sample.x)
		{
			headed_north = false;
			init_direction_north = false;
		}

		ArrayList endpoint_index = new ArrayList();

		while (last_index < data.size() - 5)
		{
			first_index += 5;
			last_index += 5;
			first_sample = (Sample) data.get(first_index);
			last_sample = (Sample) data.get(last_index);
			if (headed_north)
			{
				if (first_sample.x < last_sample.x)
				{
					endpoint_index.add(first_index);
					headed_north = false;
				}
			} else
			{
				if (first_sample.x > last_sample.x)
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

		ArrayList line_data = new ArrayList();
		for (int i = 0; i < number_of_lines; i++)
		{
			ArrayList line_list = new ArrayList();
			int start = line_index[i][0];
			int stop = line_index[i][1];

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
			} else
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

		Polygon[] polygon = new Polygon[number_of_lines];
		Area[] area = new Area[number_of_lines];
		for (int i = 0; i < number_of_lines; i++)
		{
			ArrayList polygon_list = (ArrayList) polygon_data.get(i);
			int length = polygon_list.size();
			int[] x = new int[length];
			int[] y = new int[length];
			for (int j = 0; j < length; j++)
			{
				Sample sample = (Sample) polygon_list.get(j);
				x[j] = Math.round((float) (sample.x * 100));
				y[j] = Math.round((float) (sample.y * 100));
			}

			polygon[i] = new Polygon(x, y, length);
			area[i] = new Area(polygon[i]);
		}

		int line_of_interest = 22;
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
			size = line_list.size();

			ArrayList first_intersecting_data_list = new ArrayList();
			int number_of_samples = 0;
			for (int j = 0; j < size; j++)
			{
				Sample sample = (Sample) line_list.get(j);
				if (intersecting_area.contains(sample.x * 100, sample.y * 100))
				{
					number_of_samples++;
					first_intersecting_data_list.add(sample);
					if (sample.x < xmin)
						xmin = sample.x;
					else if (sample.x > xmax)
						xmax = sample.x;
					if (sample.y < ymin)
						ymin = sample.y;
					else if (sample.y > ymax)
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
				if (intersecting_area.contains(sample.x * 100, sample.y * 100))
				{
					number_of_samples++;
					next_intersecting_data_list.add(sample);
					
					if (sample.x < xmin)
						xmin = sample.x;
					else if (sample.x > xmax)
						xmax = sample.x;
					if (sample.y < ymin)
						ymin = sample.y;
					else if (sample.y > ymax)
						ymax = sample.y;
					
				}
			}
			System.out.println("Number of samples in intersecting area in line " + (line_of_interest + 1) + " is "
					+ number_of_samples);

			double yrange = ymax - ymin;
			double xrange = xmax - xmin;

			double ratio = 0;
			int number_of_sections = 0;
			if (yrange > xrange)
			{
				ratio = yrange / xrange;
				number_of_sections = (int) ratio;
				number_of_sections++;
			} 
			else if (xrange > yrange)
			{
				ratio = xrange / yrange;
				number_of_sections = (int) ratio;
				number_of_sections++;
			} 
			else
				number_of_sections = 1;

			ArrayList[][] segment_data = new ArrayList[2][number_of_sections];
			for (int i = 0; i < 2; i++)
				for (int j = 0; j < number_of_sections; j++)
					segment_data[i][j] = new ArrayList();

			// We know our y dimension is always the major one.
			/*
			 * if(yrange > xrange) {
			 * 
			 * } else {
			 * 
			 * }
			 */

			double y_increment = yrange / number_of_sections;
			double lower_bound = ymin;
			double upper_bound = ymin + y_increment;

			/*
			 * System.out.println("Ymin is " + String.format("%.2f", ymin));
			 * System.out.println(); for(int i = 0; i < number_of_sections; i++) {
			 * System.out.println("Lower bound is " + String.format("%.2f", lower_bound));
			 * System.out.println("Upper bound is " + String.format("%.2f", upper_bound));
			 * System.out.println(); lower_bound = upper_bound; upper_bound += y_increment;
			 * } System.out.println("Ymax is " + String.format("%.2f", ymax));
			 */

			size = first_intersecting_data_list.size();
			for (int i = 0; i < size; i++)
			{
				Sample sample = (Sample) first_intersecting_data_list.get(i);
				int index = (int) Math.floor((float) ((sample.y - ymin) / yrange) * number_of_sections);
				if (index == number_of_sections)
				{
					// If this is our max value (1), stick it in the final bin.
					index--;
				}

				lower_bound = index * y_increment + ymin;
				upper_bound = lower_bound + y_increment;

				// System.out.println("Lower bound is " + String.format("%.4f", lower_bound));
				// System.out.println("Value is " + String.format("%.4f", sample.y));
				// System.out.println("Upper bound is " + String.format("%.4f", upper_bound));
				// System.out.println();

				ArrayList list = (ArrayList) segment_data[0][index];
				list.add(sample);
			}

			size = next_intersecting_data_list.size();
			for (int i = 0; i < size; i++)
			{
				Sample sample = (Sample) next_intersecting_data_list.get(i);
				int index = (int) Math.floor((float) ((sample.y - ymin) / yrange) * number_of_sections);
				if (index == number_of_sections)
				{
					// If this is our max value (1), stick it in the final bin.
					index--;
				}

				lower_bound = index * y_increment + ymin;
				upper_bound = lower_bound + y_increment;

				/*
				 * System.out.println("Lower bound is " + String.format("%.4f", lower_bound));
				 * System.out.println("Value is " + String.format("%.4f", sample.y));
				 * System.out.println("Upper bound is " + String.format("%.4f", upper_bound));
				 * System.out.println();
				 */

				ArrayList list = (ArrayList) segment_data[1][index];
				list.add(sample);
			}

			for (int i = 0; i < number_of_sections; i++)
			{
				xmax = -Double.MAX_VALUE;
				xmin = Double.MAX_VALUE;
				ymax = -Double.MAX_VALUE;
				ymin = Double.MAX_VALUE;

				ArrayList first_list = segment_data[0][i];
				size = first_list.size();
				for (int j = 0; j < size; j++)
				{
					Sample sample = (Sample) first_list.get(j);
					if (sample.x < xmin)
						xmin = sample.x;
					if (sample.x > xmax)
						xmax = sample.x;
					if (sample.y < ymin)
						ymin = sample.y;
					if (sample.y > ymax)
						ymax = sample.y;
				}

				ArrayList next_list = segment_data[1][i];
				size = next_list.size();
				for (int j = 0; j < size; j++)
				{
					Sample sample = (Sample) next_list.get(j);
					if (sample.x < xmin)
						xmin = sample.x;
					if (sample.x > xmax)
						xmax = sample.x;
					if (sample.y < ymin)
						ymin = sample.y;
					if (sample.y > ymax)
						ymax = sample.y;
				}

				System.out.println("Xmax for section " + i + " is " + String.format("%.2f", xmax));
				System.out.println("Xmin for section " + i + " is " + String.format("%.2f", xmin));
				System.out.println("Ymax for section " + i + " is " + String.format("%.2f", ymax));
				System.out.println("Ymin for section " + i + " is " + String.format("%.2f", ymin));

				double x_range = xmax - xmin;

				double xcell_width = .25;
				//double init_value = xmin - xcell_width / 2;
				double init_value = xmin;
				//double end_value = xmax + xcell_width / 2;
				double end_value = xmax;
				
				
				int number_of_cells = 0;

				while (init_value < end_value)
				{
					number_of_cells++;
					init_value += xcell_width;
				}

			
				System.out.println("Raster xdim is " + number_of_cells);

				int raster_xdim = number_of_cells;

				double ycell_width = .03;
				//init_value = ymin - ycell_width / 2;
				init_value = ymin;
				//end_value = ymax + ycell_width / 2;
				end_value = ymax;
				number_of_cells = 0;

				while (init_value < end_value)
				{
					number_of_cells++;
					init_value += ycell_width;
				}

				System.out.println("Raster ydim is " + number_of_cells);

				int raster_ydim = number_of_cells;
				
				if(raster_xdim < 3)
				{
				    System.out.println();	
				}
				else
				{
				/*
				double raster_xmin = xmin - xcell_width / 2;
				double raster_xmax = xmax + xcell_width / 2;
				double raster_ymin = ymin - ycell_width / 2;
				double raster_ymax = ymax + ycell_width / 2;
				*/
				double raster_xmin = xmin;
				double raster_xmax = xmax;
				double raster_ymin = ymin;
				double raster_ymax = ymax;
				

				ArrayList[][] segment1_data = new ArrayList[raster_ydim][raster_xdim];
				ArrayList[][] segment2_data = new ArrayList[raster_ydim][raster_xdim];
				boolean[][] isPopulated1 = new boolean[raster_ydim][raster_xdim];
				boolean[][] isPopulated2 = new boolean[raster_ydim][raster_xdim];
				boolean[][] isCentered1 = new boolean[raster_ydim][raster_xdim];
				boolean[][] isCentered2 = new boolean[raster_ydim][raster_xdim];
				int[][] sampleNumber1 = new int[raster_ydim][raster_xdim];
				int[][] sampleNumber2 = new int[raster_ydim][raster_xdim];

				for (int j = 0; j < raster_ydim; j++)
				{
					for (int k = 0; k < raster_xdim; k++)
					{
						segment1_data[j][k] = new ArrayList();
						segment2_data[j][k] = new ArrayList();
						isPopulated1[j][k] = false;
						isPopulated2[j][k] = false;
						isCentered1[j][k] = false;
						isCentered2[j][k] = false;
						sampleNumber1[j][k] = 0;
						sampleNumber2[j][k] = 0;
					}
				}

				System.out.println("The number of cells in the raster are " + (raster_xdim * raster_ydim));
				ArrayList segment_list = segment_data[0][i];
				size = segment_list.size();
				System.out.println("The number of samples being assigned to the raster from the first segment is " + size);
				System.out.println();
				
				for (int j = 0; j < size; j++)
				{
					Sample sample = (Sample) segment_list.get(j);

					int x_index = 0;
					double cell_lower_bound = x_index * xcell_width + raster_xmin;
					double cell_upper_bound = cell_lower_bound + xcell_width;

					while (sample.x > cell_upper_bound)
					{
						x_index++;
						cell_lower_bound = x_index * xcell_width + raster_xmin;
						cell_upper_bound = cell_lower_bound + xcell_width;
					}

					int y_index = 0;
					cell_lower_bound = y_index * ycell_width + raster_ymin;
					cell_upper_bound = cell_lower_bound + ycell_width;

					while (sample.y > cell_upper_bound)
					{
						y_index++;
						cell_lower_bound = y_index * ycell_width + raster_ymin;
						cell_upper_bound = cell_lower_bound + ycell_width;
					}

					ArrayList sample_list = segment1_data[y_index][x_index];
					sample_list.add(sample);
					isPopulated1[y_index][x_index] = true;
					sampleNumber1[y_index][x_index]++;
				}

				int number_of_populated_cells = 0;

				for (int j = 0; j < raster_ydim; j++)
				{
					for (int k = 0; k < raster_xdim; k++)
					{
						if (isPopulated1[j][k])
						{
							number_of_populated_cells++;
							if(i == 33)
							{
								if(isCentered1[j][k])
									System.out.print("+ ");
								else
									System.out.print("x ");
							}
						}
						else
						{
							if(i == 33)
							    System.out.print("o ");	
						}
					}
					if(i == 33)
					   System.out.println();
				}
				System.out.println("The number of populated cells in the first raster is " + number_of_populated_cells);
				System.out.println();

				double ycenter = ymin;

				int previous_number_of_populated_cells = 0;

				while (previous_number_of_populated_cells != number_of_populated_cells)
				{
					for (int j = 0; j < raster_ydim; j++)
					{
						double xcenter = xmin;
						for (int k = 0; k < raster_xdim; k++)
						{
							boolean[] neighbor_populated = new boolean[8];
							for (int m = 0; m < 8; m++)
								neighbor_populated[m] = false;
							int location_type = getLocationType(k, j, raster_xdim, raster_ydim);
							ArrayList sample_list = (ArrayList) segment1_data[j][k];

							if (isCentered1[j][k] == false)
							{
								int number_of_neighbors = 0;

								if (location_type == 5)
								{
									if (isPopulated1[j - 1][k - 1])
									{
										neighbor_populated[0] = true;
										number_of_neighbors++;
									}

									if (isPopulated1[j - 1][k])
									{
										neighbor_populated[1] = true;
										number_of_neighbors++;
									}

									if (isPopulated1[j - 1][k + 1])
									{
										neighbor_populated[2] = true;
										number_of_neighbors++;
									}

									if (isPopulated1[j][k - 1])
									{
										neighbor_populated[3] = true;
										number_of_neighbors++;
									}

									if (isPopulated1[j][k + 1])
									{
										neighbor_populated[4] = true;
										number_of_neighbors++;
									}

									if (isPopulated1[j + 1][k - 1])
									{
										neighbor_populated[5] = true;
										number_of_neighbors++;
									}

									if (isPopulated1[j + 1][k])
									{
										neighbor_populated[6] = true;
										number_of_neighbors++;
									}

									if (isPopulated1[j + 1][k + 1])
									{
										neighbor_populated[7] = true;
										number_of_neighbors++;
									}
								} 
								else if (location_type == 4)
								{
									if (isPopulated1[j - 1][k])
									{
										neighbor_populated[1] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j - 1][k + 1])
									{
										neighbor_populated[2] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j + 1][k])
									{
										neighbor_populated[6] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j + 1][k + 1])
									{
										neighbor_populated[7] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k + 1])
									{
										neighbor_populated[4] = true;
										number_of_neighbors++;
									}

								} 
								else if (location_type == 6)
								{
									if (isPopulated1[j - 1][k])
									{
										neighbor_populated[1] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j - 1][k - 1])
									{
										neighbor_populated[0] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j + 1][k])
									{
										neighbor_populated[6] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j + 1][k - 1])
									{
										neighbor_populated[5] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k - 1])
									{
										neighbor_populated[3] = true;
										number_of_neighbors++;
									}
								} 
								else if (location_type == 4)
								{
									if (isPopulated1[j - 1][k])
									{
										neighbor_populated[1] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j - 1][k + 1])
									{
										neighbor_populated[2] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j + 1][k])
									{
										neighbor_populated[6] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j + 1][k + 1])
									{
										neighbor_populated[7] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k + 1])
									{
										neighbor_populated[4] = true;
										number_of_neighbors++;
									}
								} 
								else if (location_type == 2)
								{
									if (isPopulated1[j + 1][k])
									{
										neighbor_populated[6] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j + 1][k - 1])
									{
										neighbor_populated[5] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j + 1][k + 1])
									{
										neighbor_populated[7] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k - 1])
									{
										neighbor_populated[3] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k + 1])
									{
										neighbor_populated[4] = true;
										number_of_neighbors++;
									}
								} 
								else if (location_type == 8)
								{
									if (isPopulated1[j - 1][k])
									{
										neighbor_populated[1] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j - 1][k - 1])
									{
										neighbor_populated[0] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j - 1][k + 1])
									{
										neighbor_populated[2] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k - 1])
									{
										neighbor_populated[3] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k + 1])
									{
										neighbor_populated[4] = true;
										number_of_neighbors++;
									}
								} 
								else if (location_type == 1)
								{
									if (isPopulated1[j + 1][k])
									{
										neighbor_populated[6] = true;
										number_of_neighbors++;
									}
									if(isPopulated1[j + 1][k + 1])
									{
										neighbor_populated[7] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k + 1])
									{
										neighbor_populated[4] = true;
										number_of_neighbors++;
									}
								} 
								else if (location_type == 3)
								{
									if (isPopulated1[j + 1][k])
									{
										neighbor_populated[6] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j + 1][k - 1])
									{
										neighbor_populated[5] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k - 1])
									{
										neighbor_populated[3] = true;
										number_of_neighbors++;
									}
								} 
								else if (location_type == 7)
								{
									if (isPopulated1[j - 1][k])
									{
										neighbor_populated[1] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j - 1][k + 1])
									{
										neighbor_populated[2] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k + 1])
									{
										neighbor_populated[4] = true;
										number_of_neighbors++;
									}
								} 
								else if (location_type == 9)
								{
									if (isPopulated1[j - 1][k])
									{
										neighbor_populated[1] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j - 1][k - 1])
									{
										neighbor_populated[0] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k - 1])
									{
										neighbor_populated[3] = true;
										number_of_neighbors++;
									}
								}

								ArrayList neighbor_list = new ArrayList();
								ArrayList list = new ArrayList();
								for (int m = 0; m < 8; m++)
								{
									if (neighbor_populated[m])
									{
										if (m == 0)
											list = segment1_data[j - 1][k - 1];
										else if (m == 1)
											list = segment1_data[j - 1][k];
										else if (m == 2)
											list = segment1_data[j - 1][k + 1];
										else if (m == 3)
											list = segment1_data[j][k - 1];
										else if (m == 4)
											list = segment1_data[j][k + 1];
										else if (m == 5)
											list = segment1_data[j + 1][k - 1];
										else if (m == 6)
											list = segment1_data[j + 1][k];
										else if (m == 7)
											list = segment1_data[j + 1][k + 1];
										Sample sample = (Sample) list.get(0);
										neighbor_list.add(sample);
									}
								}

								Point2D.Double origin = new Point2D.Double(xcenter, ycenter);

								Path2D.Double cell = new Path2D.Double();
								double x1 = xcenter - xcell_width / 2;
								double x2 = x1 + xcell_width;
								double y1 = ycenter - ycell_width / 2;
								double y2 = y1 + ycell_width;
								cell.moveTo(x1, y1);
								cell.lineTo(x1, y2);
								cell.lineTo(x2, y2);
								cell.lineTo(x2, y1);
								cell.closePath();

								if(number_of_neighbors == 2 && !isPopulated1[j][k])
								{
									first_sample = (Sample) neighbor_list.get(0);
									Sample second_sample = (Sample) neighbor_list.get(1);
									Point2D.Double location = DataMapper.getBisectingPoint(first_sample, second_sample,
											origin);
									double location_x = location.getX();
									double location_y = location.getY();
									if (cell.contains(location_x, location_y))
									{
										double intensity = DataMapper.getBisectingAverage(first_sample, second_sample,
												origin);
										Sample sample = new Sample(location_x, location_y, intensity);
										list = segment1_data[j][k];
										list.add(sample);
										isPopulated1[j][k] = true;
										// System.out.println("Populated a cell with 2 neighbors.");
									}
								} 
								else if (number_of_neighbors == 2 && isPopulated1[j][k])
								{
									first_sample = (Sample) sample_list.get(0);
									Sample second_sample = (Sample) neighbor_list.get(0);
									Sample third_sample = (Sample) neighbor_list.get(1);
									Path2D.Double triangle = new Path2D.Double();
									triangle.moveTo(first_sample.x, first_sample.y);
									triangle.lineTo(second_sample.x, second_sample.y);
									triangle.lineTo(third_sample.x, third_sample.y);
									triangle.closePath();
									if (triangle.contains(xcenter, ycenter))
									{
										// System.out.println("Triplet contained origin.");
										double intensity = DataMapper.getLinearInterpolation(origin, first_sample,
												second_sample, third_sample);
										Sample sample = new Sample(xcenter, ycenter, intensity);
										list = segment1_data[j][k];
										list.add(sample);

										isPopulated1[j][k] = true;
										isCentered1[j][k] = true;
									} 
									else
									{
										ArrayList triangle_list = new ArrayList();

										triangle = new Path2D.Double();
										triangle.moveTo(xcenter, ycenter);
										triangle.lineTo(first_sample.x, first_sample.y);
										triangle.lineTo(second_sample.x, second_sample.y);
										triangle.closePath();
										if (triangle.contains(third_sample.x, third_sample.y))
										{
											list = new ArrayList();
											list.add(third_sample);
											list.add(first_sample);
											list.add(second_sample);
											triangle_list.add(list);
										}

										triangle = new Path2D.Double();
										triangle.moveTo(xcenter, ycenter);
										triangle.lineTo(first_sample.x, first_sample.y);
										triangle.lineTo(third_sample.x, third_sample.y);
										triangle.closePath();
										if (triangle.contains(second_sample.x, second_sample.y))
										{
											list = new ArrayList();
											list.add(second_sample);
											list.add(first_sample);
											list.add(third_sample);
											triangle_list.add(list);
										}

										triangle = new Path2D.Double();
										triangle.moveTo(xcenter, ycenter);
										triangle.lineTo(second_sample.x, second_sample.y);
										triangle.lineTo(third_sample.x, third_sample.y);
										triangle.closePath();
										if (triangle.contains(first_sample.x, first_sample.y))
										{
											list = new ArrayList();
											list.add(first_sample);
											list.add(second_sample);
											list.add(third_sample);
											triangle_list.add(list);
										}

										if(triangle_list.size() == 1)
										{
											// System.out.println("Found a containing triangle.");
											list = (ArrayList) triangle_list.get(0);
											Sample interior = (Sample) list.get(0);
											Sample corner1 = (Sample) list.get(1);
											Sample corner2 = (Sample) list.get(2);
											double intensity = DataMapper.getLinearExtrapolation(origin, interior,
													corner1, corner2);

											Sample sample = new Sample(xcenter, ycenter, intensity);
											list = segment1_data[j][k];
											list.add(sample);

											isPopulated1[j][k] = true;
											isCentered1[j][k] = true;
										} 
										else if(triangle_list.size() > 1)
										{
											// System.out.println("Found a containing triangle.");
											size = triangle_list.size();
											double[] measure = new double[size];
											for (int m = 0; m < size; m++)
											{
												list = (ArrayList) triangle_list.get(m);
												first_sample = (Sample) list.get(0);
												second_sample = (Sample) list.get(1);
												third_sample = (Sample) list.get(2);

												Point2D.Double first_point = new Point2D.Double(xcenter, ycenter);
												Point2D.Double second_point = new Point2D.Double(second_sample.x,
														second_sample.y);
												Point2D.Double third_point = new Point2D.Double(third_sample.x,
														third_sample.y);

												double triangle_area = DataMapper.getTriangleArea(first_point,
														second_point, third_point);
												double perimeter = DataMapper.getTrianglePerimeter(first_point,
														second_point, third_point);
												measure[m] = triangle_area * perimeter;
											}

											int triangle_index = 0;
											double least_measure = measure[0];
											for (int m = 1; m < size; m++)
											{
												if (measure[m] < least_measure)
												{
													least_measure = measure[m];
													triangle_index = m;
												}
											}

											list = (ArrayList) triangle_list.get(triangle_index);
											first_sample = (Sample) list.get(0);
											second_sample = (Sample) list.get(1);
											third_sample = (Sample) list.get(2);
											double intensity = DataMapper.getLinearExtrapolation(origin, first_sample,
													second_sample, third_sample);
											Sample sample = new Sample(xcenter, ycenter, intensity);
											list = segment1_data[j][k];
											list.add(sample);

											isPopulated1[j][k] = true;
											isCentered1[j][k] = true;
										}
									}
								} 
								else if (number_of_neighbors == 3)
								{
									first_sample = (Sample) neighbor_list.get(0);
									Sample second_sample = (Sample) neighbor_list.get(1);
									Sample third_sample = (Sample) neighbor_list.get(2);
									Path2D.Double triangle = new Path2D.Double();
									triangle.moveTo(first_sample.x, first_sample.y);
									triangle.lineTo(second_sample.x, second_sample.y);
									triangle.lineTo(third_sample.x, third_sample.y);
									triangle.closePath();
									if (triangle.contains(xcenter, ycenter))
									{
										// System.out.println("Triplet contained origin.");
										double intensity = DataMapper.getLinearInterpolation(origin, first_sample,
												second_sample, third_sample);
										Sample sample = new Sample(xcenter, ycenter, intensity);
										list = segment1_data[j][k];
										list.add(sample);

										isPopulated1[j][k] = true;
										isCentered1[j][k] = true;
									} 
									else
									{
										ArrayList triangle_list = new ArrayList();

										triangle = new Path2D.Double();
										triangle.moveTo(xcenter, ycenter);
										triangle.lineTo(first_sample.x, first_sample.y);
										triangle.lineTo(second_sample.x, second_sample.y);
										triangle.closePath();
										if (triangle.contains(third_sample.x, third_sample.y))
										{
											list = new ArrayList();
											list.add(third_sample);
											list.add(first_sample);
											list.add(second_sample);
											triangle_list.add(list);
										}

										triangle = new Path2D.Double();
										triangle.moveTo(xcenter, ycenter);
										triangle.lineTo(first_sample.x, first_sample.y);
										triangle.lineTo(third_sample.x, third_sample.y);
										triangle.closePath();
										if (triangle.contains(second_sample.x, second_sample.y))
										{
											list = new ArrayList();
											list.add(second_sample);
											list.add(first_sample);
											list.add(third_sample);
											triangle_list.add(list);
										}

										triangle = new Path2D.Double();
										triangle.moveTo(xcenter, ycenter);
										triangle.lineTo(second_sample.x, second_sample.y);
										triangle.lineTo(third_sample.x, third_sample.y);
										triangle.closePath();
										if (triangle.contains(first_sample.x, first_sample.y))
										{
											list = new ArrayList();
											list.add(first_sample);
											list.add(second_sample);
											list.add(third_sample);
											triangle_list.add(list);
										}

										if (triangle_list.size() == 1)
										{
											// System.out.println("Found a containing triangle.");
											list = (ArrayList) triangle_list.get(0);
											Sample interior = (Sample) list.get(0);
											Sample corner1 = (Sample) list.get(1);
											Sample corner2 = (Sample) list.get(2);
											double intensity = DataMapper.getLinearExtrapolation(origin, interior,
													corner1, corner2);

											Sample sample = new Sample(xcenter, ycenter, intensity);
											list = segment1_data[j][k];
											list.add(sample);

											isPopulated1[j][k] = true;
											isCentered1[j][k] = true;
										} else if (triangle_list.size() > 1)
										{
											// System.out.println("Found a containing triangle.");
											size = triangle_list.size();
											double[] measure = new double[size];
											for (int m = 0; m < size; m++)
											{
												list = (ArrayList) triangle_list.get(m);
												first_sample = (Sample) list.get(0);
												second_sample = (Sample) list.get(1);
												third_sample = (Sample) list.get(2);

												Point2D.Double first_point = new Point2D.Double(xcenter, ycenter);
												Point2D.Double second_point = new Point2D.Double(second_sample.x,
														second_sample.y);
												Point2D.Double third_point = new Point2D.Double(third_sample.x,
														third_sample.y);

												double triangle_area = DataMapper.getTriangleArea(first_point,
														second_point, third_point);
												double perimeter = DataMapper.getTrianglePerimeter(first_point,
														second_point, third_point);
												measure[m] = triangle_area * perimeter;
											}

											int triangle_index = 0;
											double least_measure = measure[0];
											for (int m = 1; m < size; m++)
											{
												if (measure[m] < least_measure)
												{
													least_measure = measure[m];
													triangle_index = m;
												}
											}

											list = (ArrayList) triangle_list.get(triangle_index);
											first_sample = (Sample) list.get(0);
											second_sample = (Sample) list.get(1);
											third_sample = (Sample) list.get(2);
											double intensity = DataMapper.getLinearExtrapolation(origin, first_sample,
													second_sample, third_sample);
											Sample sample = new Sample(xcenter, ycenter, intensity);
											list = segment1_data[j][k];
											list.add(sample);

											isPopulated1[j][k] = true;
											isCentered1[j][k] = true;
										} 
										else if (triangle_list.size() == 0)
										{
											first_sample = (Sample) neighbor_list.get(0);
											second_sample = (Sample) neighbor_list.get(1);
											Point2D.Double location = DataMapper.getBisectingPoint(first_sample,
													second_sample, origin);
											double location_x = location.getX();
											double location_y = location.getY();
											if (cell.contains(location_x, location_y))
											{
												double intensity = DataMapper.getBisectingAverage(first_sample,
														second_sample, origin);
												Sample sample = new Sample(location_x, location_y, intensity);
												list = segment1_data[j][k];
												list.add(sample);
												isPopulated1[j][k] = true;

												// System.out.println("Populated a cell with 3 neighbors with a
												// bisecting value (1).");
											}

											first_sample = (Sample) neighbor_list.get(0);
											second_sample = (Sample) neighbor_list.get(2);
											location = DataMapper.getBisectingPoint(first_sample, second_sample,
													origin);
											location_x = location.getX();
											location_y = location.getY();
											if (cell.contains(location_x, location_y))
											{
												double intensity = DataMapper.getBisectingAverage(first_sample,
														second_sample, origin);
												Sample sample = new Sample(location_x, location_y, intensity);
												list = segment1_data[j][k];
												list.add(sample);
												isPopulated1[j][k] = true;

												// System.out.println("Populated a cell with 3 neighbors with a
												// bisecting value (2).");
											}

											first_sample = (Sample) neighbor_list.get(1);
											second_sample = (Sample) neighbor_list.get(2);
											location = DataMapper.getBisectingPoint(first_sample, second_sample,
													origin);
											location_x = location.getX();
											location_y = location.getY();
											if (cell.contains(location_x, location_y))
											{
												double intensity = DataMapper.getBisectingAverage(first_sample,
														second_sample, origin);
												Sample sample = new Sample(location_x, location_y, intensity);
												list = segment1_data[j][k];
												list.add(sample);
												isPopulated1[j][k] = true;

												// System.out.println("Populated a cell with 3 neighbors with a
												// bisecting value (3).");
											}
										}
									}
								} 
								else if(number_of_neighbors > 3)
								{
									Path2D.Double triangle = new Path2D.Double();
									ArrayList triangle_list = new ArrayList();

									// Initialize triangle list.
									for (int m = 0; m < number_of_neighbors; m++)
									{
										for (int n = 1; n < number_of_neighbors + 1; n++)
										{
											for (int p = 2; p < number_of_neighbors + 2; p++)
											{
												first_index = m;
												int second_index = n % number_of_neighbors;
												int third_index = p % number_of_neighbors;
												if ((first_index != second_index) && (first_index != third_index))
												{
													first_sample = (Sample) neighbor_list.get(first_index);
													Sample second_sample = (Sample) neighbor_list.get(second_index);
													Sample third_sample = (Sample) neighbor_list.get(third_index);
													triangle.moveTo(first_sample.x, first_sample.y);
													triangle.lineTo(second_sample.x, second_sample.y);
													triangle.lineTo(third_sample.x, third_sample.y);
													triangle.closePath();
													if (triangle.contains(xcenter, ycenter))
													{
														list = new ArrayList();
														list.add(first_sample);
														list.add(second_sample);
														list.add(third_sample);
														triangle_list.add(list);
													}
												}
											}
										}
									}

									size = triangle_list.size();
									// System.out.println(size + " containing triangle(s).");

									if (size != 0)
									{
										// Find the smallest containing triangle.
										double[] measure = new double[size];
										for (int m = 0; m < size; m++)
										{
											list = (ArrayList) triangle_list.get(m);
											first_sample = (Sample) list.get(0);
											Sample second_sample = (Sample) list.get(1);
											Sample third_sample = (Sample) list.get(2);

											Point2D.Double first_point = new Point2D.Double(first_sample.x,
													first_sample.y);
											Point2D.Double second_point = new Point2D.Double(second_sample.x,
													second_sample.y);
											Point2D.Double third_point = new Point2D.Double(third_sample.x,
													third_sample.y);

											double triangle_area = DataMapper.getTriangleArea(first_point, second_point,
													third_point);
											double perimeter = DataMapper.getTrianglePerimeter(first_point,
													second_point, third_point);
											measure[m] = triangle_area * perimeter;
										}
										int triangle_index = 0;
										double least_measure = measure[0];
										for (int m = 1; m < size; m++)
										{
											if (measure[m] < least_measure)
											{
												least_measure = measure[m];
												triangle_index = m;
											}
										}

										list = (ArrayList) triangle_list.get(triangle_index);
										first_sample = (Sample) list.get(0);
										Sample second_sample = (Sample) list.get(1);
										Sample third_sample = (Sample) list.get(2);
										double intensity = DataMapper.getLinearInterpolation(origin, first_sample,
												second_sample, third_sample);
										Sample sample = new Sample(xcenter, ycenter, intensity);
										list = segment1_data[j][k];
										list.add(sample);

										isPopulated1[j][k] = true;
										isCentered1[j][k] = true;
									} 
									else
									{
										// Look for a triangle that includes the origin and contains one of the
										// neighboring points.
										triangle_list.clear();
										for (int m = 0; m < number_of_neighbors; m++)
										{
											for (int n = 0; n < number_of_neighbors; n++)
											{
												if (m != n)
												{
													for (int p = 0; p < number_of_neighbors; p++)
													{
														if (p != m && p != n)
														{
															Sample corner1 = (Sample) neighbor_list.get(m);
															Sample corner2 = (Sample) neighbor_list.get(n);
															Sample interior = (Sample) neighbor_list.get(p);
															triangle.moveTo(xcenter, ycenter);
															triangle.lineTo(corner1.x, corner1.y);
															triangle.lineTo(corner2.x, corner2.y);
															triangle.closePath();
															if (triangle.contains(xcenter, ycenter))
															{
																list = new ArrayList();
																list.add(interior);
																list.add(corner1);
																list.add(corner2);
																triangle_list.add(list);
															}
														}
													}
												}
											}
										}

										size = triangle_list.size();
										if (size != 0)
										{
											double[] measure = new double[size];
											for (int m = 0; m < size; m++)
											{
												list = (ArrayList) triangle_list.get(m);
												first_sample = (Sample) list.get(0);
												Sample second_sample = (Sample) list.get(1);
												Sample third_sample = (Sample) list.get(2);

												Point2D.Double first_point = new Point2D.Double(xcenter, ycenter);
												Point2D.Double second_point = new Point2D.Double(second_sample.x,
														second_sample.y);
												Point2D.Double third_point = new Point2D.Double(third_sample.x,
														third_sample.y);

												double triangle_area = DataMapper.getTriangleArea(first_point,
														second_point, third_point);
												double perimeter = DataMapper.getTrianglePerimeter(first_point,
														second_point, third_point);
												measure[m] = triangle_area * perimeter;
											}

											int triangle_index = 0;
											double least_measure = measure[0];
											for (int m = 1; m < size; m++)
											{
												if (measure[m] < least_measure)
												{
													least_measure = measure[m];
													triangle_index = m;
												}
											}

											list = (ArrayList) triangle_list.get(triangle_index);
											first_sample = (Sample) list.get(0);
											Sample second_sample = (Sample) list.get(1);
											Sample third_sample = (Sample) list.get(2);
											double intensity = DataMapper.getLinearExtrapolation(origin, first_sample,
													second_sample, third_sample);
											Sample sample = new Sample(xcenter, ycenter, intensity);
											list = segment1_data[j][k];
											list.add(sample);

											isPopulated1[j][k] = true;
											isCentered1[j][k] = true;
										} else // If there is none, look for a bisecting value.
										{
											for (int m = 0; m < neighbor_list.size(); m++)
											{
												for (int n = 0; n < neighbor_list.size(); n++)
												{
													if (m != n)
													{
														first_sample = (Sample) neighbor_list.get(m);
														Sample second_sample = (Sample) neighbor_list.get(n);
														Point2D.Double location = DataMapper.getBisectingPoint(first_sample, second_sample, origin);
														double location_x = location.getX();
														double location_y = location.getY();
														if (cell.contains(location_x, location_y))
														{
															double intensity = DataMapper.getBisectingAverage(first_sample, second_sample, origin);
															Sample sample = new Sample(location_x, location_y, intensity);
															list = segment1_data[j][k];
															list.add(sample);
															isPopulated1[j][k] = true;
															// System.out.println("Populated a cell with more than 3
															// neighbors with a bisecting value.");
														}
													}
												}
											}
										}
									}
								}
							}
							xcenter += xcell_width;
						}
						ycenter += ycell_width;
					}

					previous_number_of_populated_cells = number_of_populated_cells;
					number_of_populated_cells = 0;
					for (int m = 0; m < raster_ydim; m++)
					{
						for (int n = 0; n < raster_xdim; n++)
						{
							if (isPopulated1[m][n])
								number_of_populated_cells++;
						}
					}
				}

				number_of_populated_cells = 0;
				int number_of_centered_cells = 0;

				for (int j = 0; j < raster_ydim; j++)
				{
					for (int k = 0; k < raster_xdim; k++)
					{
						if (isPopulated1[j][k])
						{
							number_of_populated_cells++;
						    if(isCentered1[j][k])
							    number_of_centered_cells++;
						    if(i == 33)
						    {
							    if(isCentered1[j][k])
								    System.out.print("+ ");
							    else
								    System.out.print("x ");
						    }
						}
						else if(i == 33)
							System.out.print("o ");
					}
					if(i == 33)
						System.out.println();
				}
				System.out.println("The number of populated cells in the first raster after assigning values with neighbors is " + number_of_populated_cells);
				System.out.println("The number of cells with centered samples is " + number_of_centered_cells);
				System.out.println();

				number_of_centered_cells = 0;

				ycenter = ymin;
				for (int j = 0; j < raster_ydim; j++)
				{
					for (int k = 0; k < raster_xdim; k++)
					{
						double xcenter = xmin;
						if (!isCentered1[j][k] && isPopulated1[j][k])
						{
							boolean[] neighbor_populated = new boolean[8];
							for (int m = 0; m < 8; m++)
								neighbor_populated[m] = false;
							int location_type = getLocationType(k, j, raster_xdim, raster_ydim);
							ArrayList cell_list = (ArrayList) segment1_data[j][k];
							int number_of_neighbors = 0;

							if (location_type == 5)
							{
								if (isPopulated1[j - 1][k - 1])
								{
									neighbor_populated[0] = true;
									number_of_neighbors++;
								}

								if (isPopulated1[j - 1][k])
								{
									neighbor_populated[1] = true;
									number_of_neighbors++;
								}

								if (isPopulated1[j - 1][k + 1])
								{
									neighbor_populated[2] = true;
									number_of_neighbors++;
								}

								if (isPopulated1[j][k - 1])
								{
									neighbor_populated[3] = true;
									number_of_neighbors++;
								}

								if (isPopulated1[j][k + 1])
								{
									neighbor_populated[4] = true;
									number_of_neighbors++;
								}

								if (isPopulated1[j + 1][k - 1])
								{
									neighbor_populated[5] = true;
									number_of_neighbors++;
								}

								if (isPopulated1[j + 1][k])
								{
									neighbor_populated[6] = true;
									number_of_neighbors++;
								}

								if (isPopulated1[j + 1][k + 1])
								{
									neighbor_populated[7] = true;
									number_of_neighbors++;
								}
							} else if (location_type == 4)
							{
								if (isPopulated1[j - 1][k])
								{
									neighbor_populated[1] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j - 1][k + 1])
								{
									neighbor_populated[2] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j + 1][k])
								{
									neighbor_populated[6] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j + 1][k + 1])
								{
									neighbor_populated[7] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j][k + 1])
								{
									neighbor_populated[4] = true;
									number_of_neighbors++;
								}

							} else if (location_type == 6)
							{
								if (isPopulated1[j - 1][k])
								{
									neighbor_populated[1] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j - 1][k - 1])
								{
									neighbor_populated[0] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j + 1][k])
								{
									neighbor_populated[6] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j + 1][k - 1])
								{
									neighbor_populated[5] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j][k - 1])
								{
									neighbor_populated[3] = true;
									number_of_neighbors++;
								}
							} else if (location_type == 4)
							{
								if (isPopulated1[j - 1][k])
								{
									neighbor_populated[1] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j - 1][k + 1])
								{
									neighbor_populated[2] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j + 1][k])
								{
									neighbor_populated[6] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j + 1][k + 1])
								{
									neighbor_populated[7] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j][k + 1])
								{
									neighbor_populated[4] = true;
									number_of_neighbors++;
								}
							} else if (location_type == 2)
							{
								if (isPopulated1[j + 1][k])
								{
									neighbor_populated[6] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j + 1][k - 1])
								{
									neighbor_populated[5] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j + 1][k + 1])
								{
									neighbor_populated[7] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j][k - 1])
								{
									neighbor_populated[3] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j][k + 1])
								{
									neighbor_populated[4] = true;
									number_of_neighbors++;
								}
							} else if (location_type == 8)
							{
								if (isPopulated1[j - 1][k])
								{
									neighbor_populated[1] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j - 1][k - 1])
								{
									neighbor_populated[0] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j - 1][k + 1])
								{
									neighbor_populated[2] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j][k - 1])
								{
									neighbor_populated[3] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j][k + 1])
								{
									neighbor_populated[4] = true;
									number_of_neighbors++;
								}
							} else if (location_type == 1)
							{
								if (isPopulated1[j + 1][k])
								{
									neighbor_populated[6] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j + 1][k + 1])
								{
									neighbor_populated[7] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j][k + 1])
								{
									neighbor_populated[4] = true;
									number_of_neighbors++;
								}
							} else if (location_type == 3)
							{
								if (isPopulated1[j + 1][k])
								{
									neighbor_populated[6] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j + 1][k - 1])
								{
									neighbor_populated[5] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j][k - 1])
								{
									neighbor_populated[3] = true;
									number_of_neighbors++;
								}
							} else if (location_type == 7)
							{
								if (isPopulated1[j - 1][k])
								{
									neighbor_populated[1] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j - 1][k + 1])
								{
									neighbor_populated[2] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j][k + 1])
								{
									neighbor_populated[4] = true;
									number_of_neighbors++;
								}
							} else if (location_type == 9)
							{
								if (isPopulated1[j - 1][k])
								{
									neighbor_populated[1] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j - 1][k - 1])
								{
									neighbor_populated[0] = true;
									number_of_neighbors++;
								}
								if (isPopulated1[j][k - 1])
								{
									neighbor_populated[3] = true;
									number_of_neighbors++;
								}
							}

							ArrayList neighbor_list = new ArrayList();
							ArrayList list = new ArrayList();
							for (int m = 0; m < 8; m++)
							{
								if (neighbor_populated[m])
								{
									if (m == 0)
										list = segment1_data[j - 1][k - 1];
									else if (m == 1)
										list = segment1_data[j - 1][k];
									else if (m == 2)
										list = segment1_data[j - 1][k + 1];
									else if (m == 3)
										list = segment1_data[j][k - 1];
									else if (m == 4)
										list = segment1_data[j][k + 1];
									else if (m == 5)
										list = segment1_data[j + 1][k - 1];
									else if (m == 6)
										list = segment1_data[j + 1][k];
									else if (m == 7)
										list = segment1_data[j + 1][k + 1];
									Sample sample = (Sample) list.get(0);
									neighbor_list.add(sample);
								}
							}

							Point2D.Double origin = new Point2D.Double(xcenter, ycenter);

							Path2D.Double cell = new Path2D.Double();
							double x1 = xcenter - xcell_width / 2;
							double x2 = x1 + xcell_width;
							double y1 = ycenter - ycell_width / 2;
							double y2 = y1 + ycell_width;
							cell.moveTo(x1, y1);
							cell.lineTo(x1, y2);
							cell.lineTo(x2, y2);
							cell.lineTo(x2, y1);
							cell.closePath();

							int cell_list_size = cell_list.size();
							int neighbor_list_size = neighbor_list.size();
							ArrayList triangle_list = new ArrayList();

							// Look for extrapolating triangles to center samples that were
							// original data or obtained using a bisecting value.
							for (int m = 0; m < cell_list_size; m++)
							{
								Sample interior = (Sample) cell_list.get(m);
								for (int n = 0; n < neighbor_list_size; n++)
								{
									for (int p = 0; p < neighbor_list_size; p++)
									{
										if (n != p)
										{
											Sample corner1 = (Sample) neighbor_list.get(n);
											Sample corner2 = (Sample) neighbor_list.get(p);
											Path2D.Double triangle = new Path2D.Double();
											triangle.moveTo(xcenter, ycenter);
											triangle.lineTo(corner1.x, corner1.y);
											triangle.lineTo(corner2.x, corner2.y);
											triangle.closePath();
											if (triangle.contains(interior.x, interior.y))
											{
												list = new ArrayList();
												list.add(interior);
												list.add(corner1);
												list.add(corner2);
												triangle_list.add(list);
											}
										}
									}
								}
							}
							size = triangle_list.size();
							if (size != 0)
							{
								double[] measure = new double[size];
								for (int m = 0; m < size; m++)
								{
									list = (ArrayList) triangle_list.get(m);
									first_sample = (Sample) list.get(0);
									Sample second_sample = (Sample) list.get(1);
									Sample third_sample = (Sample) list.get(2);

									Point2D.Double first_point = new Point2D.Double(xcenter, ycenter);
									Point2D.Double second_point = new Point2D.Double(second_sample.x, second_sample.y);
									Point2D.Double third_point = new Point2D.Double(third_sample.x, third_sample.y);

									double triangle_area = DataMapper.getTriangleArea(first_point, second_point,
											third_point);
									double perimeter = DataMapper.getTrianglePerimeter(first_point, second_point,
											third_point);
									measure[m] = triangle_area * perimeter;
								}

								int triangle_index = 0;
								double least_measure = measure[0];
								for (int m = 1; m < size; m++)
								{
									if (measure[m] < least_measure)
									{
										least_measure = measure[m];
										triangle_index = m;
									}
								}

								list = (ArrayList) triangle_list.get(triangle_index);
								first_sample = (Sample) list.get(0);
								Sample second_sample = (Sample) list.get(1);
								Sample third_sample = (Sample) list.get(2);
								double intensity = DataMapper.getLinearExtrapolation(origin, first_sample,
										second_sample, third_sample);
								Sample sample = new Sample(xcenter, ycenter, intensity);
								list = segment1_data[j][k];
								list.add(sample);

								isPopulated1[j][k] = true;
								isCentered1[j][k] = true;
							}
						}
						xcenter += xcell_width;
					}
					ycenter += ycell_width;
				}

				number_of_centered_cells = 0;
				number_of_populated_cells = 0;
				for (int j = 0; j < raster_ydim; j++)
				{
					for (int k = 0; k < raster_xdim; k++)
					{
						if(isPopulated1[j][k])
						{
							number_of_populated_cells++;
							if(isCentered1[j][k])
							{
								number_of_centered_cells++;
								if(i == 33)
								    System.out.print("+ ");
							}
							else if(i == 33)
								System.out.print("x ");
						}
						else if(i == 33)
							System.out.print("o ");
					}
					if(i == 33)
						System.out.println();
				}
				System.out.println("The number of cells with centered samples after second pass is " + number_of_centered_cells);
				System.out.println();

				// Now do a third pass and interpolate values for remaining cells.
				previous_number_of_populated_cells = 0;
				int previous_number_of_centered_cells = 0;
				int iteration = 1;
				while (previous_number_of_populated_cells != number_of_populated_cells || previous_number_of_centered_cells != number_of_centered_cells)
				{
					System.out.println("Iteration " + iteration);
					iteration++;
					ycenter = ymin;
					for (int j = 0; j < raster_ydim; j++)
					{
						double xcenter = xmin;
						for (int k = 0; k < raster_xdim; k++)
						{
							if (!isCentered1[j][k])
							{
								boolean[] neighbor_populated = new boolean[8];
								for (int m = 0; m < 8; m++)
									neighbor_populated[m] = false;
								int location_type = getLocationType(k, j, raster_xdim, raster_ydim);
								ArrayList cell_list = (ArrayList) segment1_data[j][k];
								int number_of_neighbors = 0;

								if (location_type == 5)
								{
									if (isPopulated1[j - 1][k - 1])
									{
										neighbor_populated[0] = true;
										number_of_neighbors++;
									}

									if (isPopulated1[j - 1][k])
									{
										neighbor_populated[1] = true;
										number_of_neighbors++;
									}

									if (isPopulated1[j - 1][k + 1])
									{
										neighbor_populated[2] = true;
										number_of_neighbors++;
									}

									if (isPopulated1[j][k - 1])
									{
										neighbor_populated[3] = true;
										number_of_neighbors++;
									}

									if (isPopulated1[j][k + 1])
									{
										neighbor_populated[4] = true;
										number_of_neighbors++;
									}

									if (isPopulated1[j + 1][k - 1])
									{
										neighbor_populated[5] = true;
										number_of_neighbors++;
									}

									if (isPopulated1[j + 1][k])
									{
										neighbor_populated[6] = true;
										number_of_neighbors++;
									}

									if (isPopulated1[j + 1][k + 1])
									{
										neighbor_populated[7] = true;
										number_of_neighbors++;
									}
								} else if (location_type == 4)
								{
									if (isPopulated1[j - 1][k])
									{
										neighbor_populated[1] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j - 1][k + 1])
									{
										neighbor_populated[2] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j + 1][k])
									{
										neighbor_populated[6] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j + 1][k + 1])
									{
										neighbor_populated[7] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k + 1])
									{
										neighbor_populated[4] = true;
										number_of_neighbors++;
									}

								} else if (location_type == 6)
								{
									if (isPopulated1[j - 1][k])
									{
										neighbor_populated[1] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j - 1][k - 1])
									{
										neighbor_populated[0] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j + 1][k])
									{
										neighbor_populated[6] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j + 1][k - 1])
									{
										neighbor_populated[5] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k - 1])
									{
										neighbor_populated[3] = true;
										number_of_neighbors++;
									}
								} else if (location_type == 4)
								{
									if (isPopulated1[j - 1][k])
									{
										neighbor_populated[1] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j - 1][k + 1])
									{
										neighbor_populated[2] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j + 1][k])
									{
										neighbor_populated[6] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j + 1][k + 1])
									{
										neighbor_populated[7] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k + 1])
									{
										neighbor_populated[4] = true;
										number_of_neighbors++;
									}
								} else if (location_type == 2)
								{
									if (isPopulated1[j + 1][k])
									{
										neighbor_populated[6] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j + 1][k - 1])
									{
										neighbor_populated[5] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j + 1][k + 1])
									{
										neighbor_populated[7] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k - 1])
									{
										neighbor_populated[3] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k + 1])
									{
										neighbor_populated[4] = true;
										number_of_neighbors++;
									}
								} else if (location_type == 8)
								{
									if (isPopulated1[j - 1][k])
									{
										neighbor_populated[1] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j - 1][k - 1])
									{
										neighbor_populated[0] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j - 1][k + 1])
									{
										neighbor_populated[2] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k - 1])
									{
										neighbor_populated[3] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k + 1])
									{
										neighbor_populated[4] = true;
										number_of_neighbors++;
									}
								} else if (location_type == 1)
								{
									if (isPopulated1[j + 1][k])
									{
										neighbor_populated[6] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j + 1][k + 1])
									{
										neighbor_populated[7] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k + 1])
									{
										neighbor_populated[4] = true;
										number_of_neighbors++;
									}
								} else if (location_type == 3)
								{
									if (isPopulated1[j + 1][k])
									{
										neighbor_populated[6] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j + 1][k - 1])
									{
										neighbor_populated[5] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k - 1])
									{
										neighbor_populated[3] = true;
										number_of_neighbors++;
									}
								} else if (location_type == 7)
								{
									if (isPopulated1[j - 1][k])
									{
										neighbor_populated[1] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j - 1][k + 1])
									{
										neighbor_populated[2] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k + 1])
									{
										neighbor_populated[4] = true;
										number_of_neighbors++;
									}
								} else if (location_type == 9)
								{
									if (isPopulated1[j - 1][k])
									{
										neighbor_populated[1] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j - 1][k - 1])
									{
										neighbor_populated[0] = true;
										number_of_neighbors++;
									}
									if (isPopulated1[j][k - 1])
									{
										neighbor_populated[3] = true;
										number_of_neighbors++;
									}
								}

								ArrayList neighbor_list = new ArrayList();
								ArrayList list = new ArrayList();
								for (int m = 0; m < 8; m++)
								{
									if (neighbor_populated[m])
									{
										if (m == 0)
											list = segment1_data[j - 1][k - 1];
										else if (m == 1)
											list = segment1_data[j - 1][k];
										else if (m == 2)
											list = segment1_data[j - 1][k + 1];
										else if (m == 3)
											list = segment1_data[j][k - 1];
										else if (m == 4)
											list = segment1_data[j][k + 1];
										else if (m == 5)
											list = segment1_data[j + 1][k - 1];
										else if (m == 6)
											list = segment1_data[j + 1][k];
										else if (m == 7)
											list = segment1_data[j + 1][k + 1];
										size = list.size();
										Sample sample = (Sample) list.get(size - 1);
										neighbor_list.add(sample);
									}
								}

								Point2D.Double origin = new Point2D.Double(xcenter, ycenter);

								Path2D.Double cell = new Path2D.Double();
								double x1 = xcenter - xcell_width / 2;
								double x2 = x1 + xcell_width;
								double y1 = ycenter - ycell_width / 2;
								double y2 = y1 + ycell_width;
								cell.moveTo(x1, y1);
								cell.lineTo(x1, y2);
								cell.lineTo(x2, y2);
								cell.lineTo(x2, y1);
								cell.closePath();

								int cell_list_size = cell_list.size();
								
								ArrayList triangle_list = new ArrayList();

								// We'll pick up some cells on this pass because some of
								// the cells now have centered samples on opposite sides
								// and we can use the simple average but we need to extend
								// the neighbor lists to pick up the remaining ones with
								// interpolating or extrapolating triangles.  We'll also
								// assign remaining corner values with a bisecting value
								// is there are no containing triangles.

								// Extending neighbor lists.
								if (location_type == 4)
								{
									if(k < raster_xdim - 2)
									{
									    list = segment1_data[j + 1][k + 2];
									    size = list.size();
									    if (size != 0)
										   first_sample = (Sample) list.get(size - 1);
									    neighbor_list.add(first_sample);
									
									    list = segment1_data[j][k + 2];
									    size = list.size();
									    if (size != 0)
										    first_sample = (Sample) list.get(size - 1);
									    neighbor_list.add(first_sample);
									
									    list = segment1_data[j - 1][k + 2];
									    size = list.size();
									    if (size != 0)
										    first_sample = (Sample) list.get(size - 1);
									    neighbor_list.add(first_sample);
									}
									
									if (j > 2)
									{
										list = segment1_data[j - 2][k];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										list = segment1_data[j - 2][k + 1];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										if (k < raster_xdim - 2)
										{
											list = segment1_data[j - 2][k + 2];
											size = list.size();
											if (size != 0)
												first_sample = (Sample) list.get(size - 1);
											neighbor_list.add(first_sample);
											
											list = segment1_data[j][k + 2];
											size = list.size();
											if (size != 0)
												first_sample = (Sample) list.get(size - 1);
											neighbor_list.add(first_sample);
										}
										
									}
									if (j < raster_ydim - 2)
									{
										list = segment1_data[j + 2][k];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										list = segment1_data[j + 2][k + 1];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										if (k < raster_xdim - 2)
										{
											list = segment1_data[j + 2][k + 2];
											size = list.size();
											if (size != 0)
												first_sample = (Sample) list.get(size - 1);
											neighbor_list.add(first_sample);
										}
										
									}
									
								} 
								else if (location_type == 6)
								{
									if (k > 2)
									{
										list = segment1_data[j - 1][k - 2];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										list = segment1_data[j][k - 2];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										list = segment1_data[j + 1][k - 2];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
									}

									if (j > 2)
									{
										list = segment1_data[j - 2][k];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										list = segment1_data[j - 2][k - 1];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										if(k > 2)
										{
											list = segment1_data[j - 2][k - 2];
											size = list.size();
											if (size != 0)
												first_sample = (Sample) list.get(size - 1);
											neighbor_list.add(first_sample);	
										}
									}
									if (j < raster_ydim - 2)
									{
										list = segment1_data[j + 2][k];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										list = segment1_data[j + 2][k - 1];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										if(k > 2)
										{
											list = segment1_data[j + 2][k - 2];
											size = list.size();
											if (size != 0)
												first_sample = (Sample) list.get(size - 1);
											neighbor_list.add(first_sample);	
										}
										
									}
								} 
								else if (location_type == 2)
								{
									if (j < raster_ydim - 2)
									{
										list = segment1_data[j + 2][k - 1];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										list = segment1_data[j + 2][k];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										list = segment1_data[j + 2][k + 1];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
									}
									if (k > 2)
									{
										list = segment1_data[j][k - 2];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										list = segment1_data[j + 1][k - 2];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										if(j < raster_ydim - 2)
										{
											list = segment1_data[j + 2][k - 2];
											size = list.size();
											if (size != 0)
												first_sample = (Sample) list.get(size - 1);
											neighbor_list.add(first_sample);	
										}
									}
									
									if (k < raster_xdim - 2)
									{
										list = segment1_data[j][k + 2];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										list = segment1_data[j + 1][k + 2];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										if(j < raster_ydim - 2)
										{
											list = segment1_data[j + 2][k + 2];
											size = list.size();
											if (size != 0)
												first_sample = (Sample) list.get(size - 1);
											neighbor_list.add(first_sample);	
										}
									}
								} 
								else if (location_type == 8)
								{
									if (j < 2)
									{
										list = segment1_data[j - 2][k - 1];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										list = segment1_data[j - 2][k];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										list = segment1_data[j - 2][k + 1];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
									}
									if (k > 2)
									{
										list = segment1_data[j][k - 2];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										list = segment1_data[j - 1][k - 2];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										if(j > 2)
										{
											list = segment1_data[j - 2][k - 2];
											size = list.size();
											if (size != 0)
												first_sample = (Sample) list.get(size - 1);
											neighbor_list.add(first_sample);	
										}
									}
									
									if (k < raster_xdim - 2)
									{
										list = segment1_data[j][k + 2];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										list = segment1_data[j - 1][k + 2];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										if(j > 2)
										{
											list = segment1_data[j - 2][k + 2];
											size = list.size();
											if (size != 0)
												first_sample = (Sample) list.get(size - 1);
											neighbor_list.add(first_sample);	
										}
									}	
								}
								else if(location_type == 5) 
								{ 
									if (j > 2)
									{
										list = segment1_data[j - 2][k];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										if(k > 2)
										{
											list = segment1_data[j - 2][k - 2];
											size = list.size();
											if (size != 0)
												first_sample = (Sample) list.get(size - 1);
											neighbor_list.add(first_sample);	
										}
										
										if(k < raster_xdim - 2)
										{
											list = segment1_data[j - 2][k + 2];
											size = list.size();
											if (size != 0)
												first_sample = (Sample) list.get(size - 1);
											neighbor_list.add(first_sample);		
										}
									}
									if (j < raster_ydim - 2)
									{
										list = segment1_data[j + 2][k];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										if(k > 2)
										{
											list = segment1_data[j + 2][k - 2];
											size = list.size();
											if (size != 0)
												first_sample = (Sample) list.get(size - 1);
											neighbor_list.add(first_sample);	
										}
										
										if(k < raster_xdim - 2)
										{
											list = segment1_data[j + 2][k + 2];
											size = list.size();
											if (size != 0)
												first_sample = (Sample) list.get(size - 1);
											neighbor_list.add(first_sample);		
										}
										
									}
									if (k > 2)
									{
										list = segment1_data[j][k - 2];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);	
										
										if(j > 2)
										{
											list = segment1_data[j - 2][k - 2];
											size = list.size();
											if (size != 0)
												first_sample = (Sample) list.get(size - 1);
											neighbor_list.add(first_sample);	
										}
										
										if(j < raster_ydim - 2)
										{
											list = segment1_data[j + 2][k - 2];
											size = list.size();
											if (size != 0)
												first_sample = (Sample) list.get(size - 1);
											neighbor_list.add(first_sample);	
										}
									}
									if (k < raster_xdim - 2)
									{
										list = segment1_data[j][k + 2];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										if(j > 2)
										{
											list = segment1_data[j - 2][k + 2];
											size = list.size();
											if (size != 0)
												first_sample = (Sample) list.get(size - 1);
											neighbor_list.add(first_sample);	
										}
										
										if(j < raster_ydim - 2)
										{
											list = segment1_data[j + 2][k + 2];
											size = list.size();
											if (size != 0)
												first_sample = (Sample) list.get(size - 1);
											neighbor_list.add(first_sample);	
										}
									}
								}
								else if(location_type == 7)
								{
									if(j > raster_ydim - 2)
									{
										list = segment1_data[j - 2][k];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										list = segment1_data[j - 2][k + 1];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										if(k < raster_xdim - 2)
										{
											list = segment1_data[j - 2][k + 2];
											size = list.size();
											if (size != 0)
												first_sample = (Sample) list.get(size - 1);
											neighbor_list.add(first_sample);	
										}
										
									}
									if(k < raster_xdim - 2)
									{
										list = segment1_data[j][k + 2];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);
										
										list = segment1_data[j - 1][k + 2];
										size = list.size();
										if (size != 0)
											first_sample = (Sample) list.get(size - 1);
										neighbor_list.add(first_sample);	
									}
								}
                                
								int neighbor_list_size = neighbor_list.size();
								if (location_type == 4 || location_type == 6)
								{
									if(isCentered1[j - 1][k] && isCentered1[j + 1][k])
									{
										//System.out.println("Found centered samples north and south.");
										if(!isPopulated1[j][k])
										{
											list = segment1_data[j - 1][k];
											size = list.size();
											first_sample = (Sample) list.get(size - 1);
											list = segment1_data[j + 1][k];
											size = list.size();
											Sample second_sample = (Sample) list.get(size - 1);

											double intensity = (first_sample.intensity + second_sample.intensity) / 2;
											Sample sample = new Sample(xcenter, ycenter, intensity);
											list = segment1_data[j][k];
											list.add(sample);
											isPopulated1[j][k] = true;
									    }
										else
										{
											first_sample = (Sample) cell_list.get(0);
											list = segment1_data[j - 1][k];
											size = list.size();
											Sample second_sample = (Sample) list.get(size - 1);
											list = segment1_data[j + 1][k];
											size = list.size();
											Sample third_sample = (Sample) list.get(size - 1);
											double intensity = DataMapper.getLinearInterpolation(origin, first_sample, second_sample, third_sample);
											Sample sample = new Sample(xcenter, ycenter, intensity);
											list = segment1_data[j][k];
											list.add(sample);
									    }
									    isCentered1[j][k] = true;
									} 
									else
									{
										if(isPopulated1[j][k])
										{
											// To a limited extent, the quadrant where the sample is
											// determines whether an interpolating or extrapolating
											// triangle will produce a value for the center--
											// depending on where neighboring samples are located.
											
											Sample sample = (Sample)cell_list.get(0);
											
											if(sample.x > xcenter && sample.y > ycenter)
											{
												//System.out.println("Sample is in fourth quadrant.");     	
											}
											else if(sample.x < xcenter && sample.y > ycenter)
											{
												//System.out.println("Sample is in third quadrant.");	
											}
											else if(sample.x < xcenter && sample.y < ycenter)
											{
												//System.out.println("Sample is in second quadrant.");	
											}
											else if(sample.x > xcenter && sample.y < ycenter)
											{
												//System.out.println("Sample is in first quadrant.");
											}
											
											for (int m = 0; m < cell_list_size; m++)
											{
												Sample interior = (Sample) cell_list.get(m);
												for (int n = 0; n < neighbor_list_size; n++)
												{
													for (int p = 0; p < neighbor_list_size; p++)
													{
														if (n != p)
														{
															Sample corner1 = (Sample) neighbor_list.get(n);
															Sample corner2 = (Sample) neighbor_list.get(p);
															Path2D.Double triangle = new Path2D.Double();
															triangle.moveTo(xcenter, ycenter);
															triangle.lineTo(corner1.x, corner1.y);
															triangle.lineTo(corner2.x, corner2.y);
															triangle.closePath();
															if (triangle.contains(interior.x, interior.y))
															{
																list = new ArrayList();
																list.add(interior);
																list.add(corner1);
																list.add(corner2);
																triangle_list.add(list);
															}
														}
													}
												}
											}
											size = triangle_list.size();
											if (size != 0)
											{
												//System.out.println("Found extrapolating triangles.");
												//System.out.println();
												double[] measure = new double[size];
												for (int m = 0; m < size; m++)
												{
													list = (ArrayList) triangle_list.get(m);
													first_sample = (Sample) list.get(0);
													Sample second_sample = (Sample) list.get(1);
													Sample third_sample = (Sample) list.get(2);

													Point2D.Double first_point = new Point2D.Double(xcenter, ycenter);
													Point2D.Double second_point = new Point2D.Double(second_sample.x, second_sample.y);
													Point2D.Double third_point = new Point2D.Double(third_sample.x, third_sample.y);

													double triangle_area = DataMapper.getTriangleArea(first_point, second_point,
															third_point);
													double perimeter = DataMapper.getTrianglePerimeter(first_point, second_point,
															third_point);
													measure[m] = triangle_area * perimeter;
												}

												int triangle_index = 0;
												double least_measure = measure[0];
												for (int m = 1; m < size; m++)
												{
													if (measure[m] < least_measure)
													{
														least_measure = measure[m];
														triangle_index = m;
													}
												}

												list = (ArrayList) triangle_list.get(triangle_index);
												first_sample = (Sample) list.get(0);
												Sample second_sample = (Sample) list.get(1);
												Sample third_sample = (Sample) list.get(2);
												double intensity = DataMapper.getLinearExtrapolation(origin, first_sample, second_sample, third_sample);
												sample = new Sample(xcenter, ycenter, intensity);
												list = segment1_data[j][k];
												list.add(sample);

												isCentered1[j][k] = true;	
											}
											else
											{
												//System.out.println("Did not find extrapolating triangles.");
												
												// Look for interpolating triangles.
												for (int m = 0; m < cell_list_size; m++)
												{
													first_sample = (Sample) cell_list.get(m);
													for (int n = 0; n < neighbor_list_size; n++)
													{
														for (int p = 0; p < neighbor_list_size; p++)
														{
															if (n != p)
															{
																Sample second_sample = (Sample) neighbor_list.get(n);
																Sample third_sample  = (Sample) neighbor_list.get(p);
																Path2D.Double triangle = new Path2D.Double();
																triangle.moveTo(first_sample.x, first_sample.y);
																triangle.lineTo(second_sample.x, second_sample.y);
																triangle.lineTo(third_sample.x, third_sample.y);
																triangle.closePath();
																if (triangle.contains(xcenter, ycenter))
																{
																	list = new ArrayList();
																	list.add(first_sample);
																	list.add(second_sample);
																	list.add(third_sample);
																	triangle_list.add(list);
																}
															}
														}
													}
												}
												if(triangle_list.size() != 0)
												{
												
														System.out.println("Found interpolating triangles.");	
														size = triangle_list.size();
														double[] measure = new double[size];
														for (int m = 0; m < size; m++)
														{
															list = (ArrayList) triangle_list.get(m);
															first_sample = (Sample) list.get(0);
															Sample second_sample = (Sample) list.get(1);
															Sample third_sample = (Sample) list.get(2);

															Point2D.Double first_point = new Point2D.Double(first_sample.x, first_sample.y);
															Point2D.Double second_point = new Point2D.Double(second_sample.x, second_sample.y);
															Point2D.Double third_point = new Point2D.Double(third_sample.x, third_sample.y);

															double triangle_area = DataMapper.getTriangleArea(first_point, second_point,
																	third_point);
															double perimeter = DataMapper.getTrianglePerimeter(first_point, second_point,
																	third_point);
															measure[m] = triangle_area * perimeter;
														}

														int triangle_index = 0;
														double least_measure = measure[0];
														for (int m = 0; m < size; m++)
														{
															if (measure[m] < least_measure)
															{
																least_measure = measure[m];
																triangle_index = m;
															}
														}

														list = (ArrayList) triangle_list.get(triangle_index);
														first_sample = (Sample) list.get(0);
														Sample second_sample = (Sample) list.get(1);
														Sample third_sample = (Sample) list.get(2);
														double intensity = DataMapper.getLinearInterpolation(origin, first_sample, second_sample, third_sample);
														sample = new Sample(xcenter, ycenter, intensity);
														list = segment1_data[j][k];
														list.add(sample);

														isCentered1[j][k] = true;	
												}
												
											}
										} 
										else
										{
											//System.out.println("Cell is not populated.");
										}	
									}
								}
								else if(location_type == 2 || location_type == 8)
								{
									if(isCentered1[j][k - 1] && isCentered1[j][k + 1])
									{
										//System.out.println("Found centered samples west and east.");
										//System.out.println();
										if(!isPopulated1[j][k])
										{
											list = segment1_data[j][k - 1];
											size = list.size();
											first_sample = (Sample) list.get(size - 1);
											list = segment1_data[j][k + 1];
											size = list.size();
											Sample second_sample = (Sample) list.get(size - 1);

											double intensity = (first_sample.intensity + second_sample.intensity) / 2;
											Sample sample = new Sample(xcenter, ycenter, intensity);
											list = segment1_data[j][k];
											list.add(sample);
											isPopulated1[j][k] = true;
									    }
										else
										{
											first_sample = (Sample) cell_list.get(0);
											list = segment1_data[j][k - 1];
											size = list.size();
											Sample second_sample = (Sample) list.get(size - 1);
											list = segment1_data[j][k + 1];
											size = list.size();
											Sample third_sample = (Sample) list.get(size - 1);
											double intensity = DataMapper.getLinearInterpolation(origin, first_sample, second_sample, third_sample);
											Sample sample = new Sample(xcenter, ycenter, intensity);
											list = segment1_data[j][k];
											list.add(sample);
									    }
									    isCentered1[j][k] = true;
									} 
									else
									{
										if(isPopulated1[j][k])
										{
											//System.out.println("Cell has " + cell_list_size + " sample(s).");
											
											Sample sample = (Sample)cell_list.get(0);
											
											if(sample.x > xcenter && sample.y > ycenter)
											{
												//System.out.println("Sample is in first quadrant.");     	
											}
											else if(sample.x < xcenter && sample.y > ycenter)
											{
												//System.out.println("Sample is in second quadrant.");	
											}
											else if(sample.x < xcenter && sample.y < ycenter)
											{
												//System.out.println("Sample is in third quadrant.");	
											}
											else if(sample.x > xcenter && sample.y < ycenter)
											{
												//System.out.println("Sample is in fourth quadrant.");
											}
											
											for (int m = 0; m < cell_list_size; m++)
											{
												Sample interior = (Sample) cell_list.get(m);
												for (int n = 0; n < neighbor_list_size; n++)
												{
													for (int p = 0; p < neighbor_list_size; p++)
													{
														if (n != p)
														{
															Sample corner1 = (Sample) neighbor_list.get(n);
															Sample corner2 = (Sample) neighbor_list.get(p);
															Path2D.Double triangle = new Path2D.Double();
															triangle.moveTo(xcenter, ycenter);
															triangle.lineTo(corner1.x, corner1.y);
															triangle.lineTo(corner2.x, corner2.y);
															triangle.closePath();
															if (triangle.contains(interior.x, interior.y))
															{
																list = new ArrayList();
																list.add(interior);
																list.add(corner1);
																list.add(corner2);
																triangle_list.add(list);
															}
														}
													}
												}
											}
											size = triangle_list.size();
											if (size != 0)
											{
												//System.out.println("Found " + size + " extrapolating triangle(s).");
												double[] measure = new double[size];
												for (int m = 0; m < size; m++)
												{
													list = (ArrayList) triangle_list.get(m);
													first_sample = (Sample) list.get(0);
													Sample second_sample = (Sample) list.get(1);
													Sample third_sample = (Sample) list.get(2);

													Point2D.Double first_point = new Point2D.Double(xcenter, ycenter);
													Point2D.Double second_point = new Point2D.Double(second_sample.x, second_sample.y);
													Point2D.Double third_point = new Point2D.Double(third_sample.x, third_sample.y);

													double triangle_area = DataMapper.getTriangleArea(first_point, second_point,
															third_point);
													double perimeter = DataMapper.getTrianglePerimeter(first_point, second_point,
															third_point);
													measure[m] = triangle_area * perimeter;
												}

												int triangle_index = 0;
												double least_measure = measure[0];
												for (int m = 1; m < size; m++)
												{
													if (measure[m] < least_measure)
													{
														least_measure = measure[m];
														triangle_index = m;
													}
												}

												list = (ArrayList) triangle_list.get(triangle_index);
												first_sample = (Sample) list.get(0);
												Sample second_sample = (Sample) list.get(1);
												Sample third_sample = (Sample) list.get(2);
												double intensity = DataMapper.getLinearExtrapolation(origin, first_sample, second_sample, third_sample);
												sample = new Sample(xcenter, ycenter, intensity);
												list = segment1_data[j][k];
												list.add(sample);

												isCentered1[j][k] = true;	
											}
											else
											{
                                                //System.out.println("Did not find extrapolating triangles.");
												// Look for interpolating triangles.
												for (int m = 0; m < cell_list_size; m++)
												{
													first_sample = (Sample) cell_list.get(m);
													for (int n = 0; n < neighbor_list_size; n++)
													{
														for (int p = 0; p < neighbor_list_size; p++)
														{
															if (n != p)
															{
																Sample second_sample = (Sample) neighbor_list.get(n);
																Sample third_sample  = (Sample) neighbor_list.get(p);
																Path2D.Double triangle = new Path2D.Double();
																triangle.moveTo(first_sample.x, first_sample.y);
																triangle.lineTo(second_sample.x, second_sample.y);
																triangle.lineTo(third_sample.x, third_sample.y);
																triangle.closePath();
																if (triangle.contains(xcenter, ycenter))
																{
																	list = new ArrayList();
																	list.add(first_sample);
																	list.add(second_sample);
																	list.add(third_sample);
																	triangle_list.add(list);
																}
															}
														}
													}
												}
												if(triangle_list.size() != 0)
												{
												
														System.out.println("Found interpolating triangles.");	
														size = triangle_list.size();
														double[] measure = new double[size];
														for (int m = 0; m < size; m++)
														{
															list = (ArrayList) triangle_list.get(m);
															first_sample = (Sample) list.get(0);
															Sample second_sample = (Sample) list.get(1);
															Sample third_sample = (Sample) list.get(2);

															Point2D.Double first_point = new Point2D.Double(first_sample.x, first_sample.y);
															Point2D.Double second_point = new Point2D.Double(second_sample.x, second_sample.y);
															Point2D.Double third_point = new Point2D.Double(third_sample.x, third_sample.y);

															double triangle_area = DataMapper.getTriangleArea(first_point, second_point,
																	third_point);
															double perimeter = DataMapper.getTrianglePerimeter(first_point, second_point,
																	third_point);
															measure[m] = triangle_area * perimeter;
														}

														int triangle_index = 0;
														double least_measure = measure[0];
														for (int m = 0; m < size; m++)
														{
															if (measure[m] < least_measure)
															{
																least_measure = measure[m];
																triangle_index = m;
															}
														}

														list = (ArrayList) triangle_list.get(triangle_index);
														first_sample = (Sample) list.get(0);
														Sample second_sample = (Sample) list.get(1);
														Sample third_sample = (Sample) list.get(2);
														double intensity = DataMapper.getLinearInterpolation(origin, first_sample, second_sample, third_sample);
														sample = new Sample(xcenter, ycenter, intensity);
														list = segment1_data[j][k];
														list.add(sample);

														isCentered1[j][k] = true;	
												}
											}
										} 
										else
										{
											//System.out.println("Cell is not populated.");
										}	
									}	
								}
								else if(location_type == 5) 
								{ 
									if(!isCentered1[j][k])
									{
									    if(isCentered1[j - 1][k] && isCentered1[j + 1][k])
									    {
										    //System.out.println("Found centered samples north and south.");
										    if(!isPopulated1[j][k])
										    {
											    list = segment1_data[j - 1][k];
											    size = list.size();
											    first_sample = (Sample) list.get(size - 1);
											    list = segment1_data[j + 1][k];
											    size = list.size();
											    Sample second_sample = (Sample) list.get(size - 1);

											    double intensity = (first_sample.intensity + second_sample.intensity) / 2;
											    Sample sample = new Sample(xcenter, ycenter, intensity);
											    list = segment1_data[j][k];
											    list.add(sample);
											    isPopulated1[j][k] = true;
									        }
										    else
										    {
											    first_sample = (Sample) cell_list.get(0);
											    list = segment1_data[j - 1][k];
											    size = list.size();
											    Sample second_sample = (Sample) list.get(size - 1);
											    list = segment1_data[j + 1][k];
											    size = list.size();
											    Sample third_sample = (Sample) list.get(size - 1);
											    double intensity = DataMapper.getLinearInterpolation(origin, first_sample, second_sample, third_sample);
											    Sample sample = new Sample(xcenter, ycenter, intensity);
											    list = segment1_data[j][k];
											    list.add(sample);
									        }
									        isCentered1[j][k] = true;
									    } 
									    else if(isCentered1[j][k - 1] && isCentered1[j][k + 1])
										{
											//System.out.println("Found centered samples west and east.");
											//System.out.println();
											if(!isPopulated1[j][k])
											{
												list = segment1_data[j][k - 1];
												size = list.size();
												first_sample = (Sample) list.get(size - 1);
												list = segment1_data[j][k + 1];
												size = list.size();
												Sample second_sample = (Sample) list.get(size - 1);

												double intensity = (first_sample.intensity + second_sample.intensity) / 2;
												Sample sample = new Sample(xcenter, ycenter, intensity);
												list = segment1_data[j][k];
												list.add(sample);
												isPopulated1[j][k] = true;
										    }
											else
											{
												first_sample = (Sample) cell_list.get(0);
												list = segment1_data[j][k - 1];
												size = list.size();
												Sample second_sample = (Sample) list.get(size - 1);
												list = segment1_data[j][k + 1];
												size = list.size();
												Sample third_sample = (Sample) list.get(size - 1);
												double intensity = DataMapper.getLinearInterpolation(origin, first_sample, second_sample, third_sample);
												Sample sample = new Sample(xcenter, ycenter, intensity);
												list = segment1_data[j][k];
												list.add(sample);
										    }
										    isCentered1[j][k] = true;
										} 
									    else if(!isPopulated1[j][k])
									    {
									        neighbor_list_size = neighbor_list.size();
									        if(neighbor_list_size >= 3)
									        {
									        	Path2D.Double triangle = new Path2D.Double();
												triangle_list = new ArrayList();

												// Initialize triangle list.
												for (int m = 0; m < number_of_neighbors; m++)
												{
													for (int n = 1; n < number_of_neighbors + 1; n++)
													{
														for (int p = 2; p < number_of_neighbors + 2; p++)
														{
															first_index = m;
															int second_index = n % number_of_neighbors;
															int third_index = p % number_of_neighbors;
															if ((first_index != second_index) && (first_index != third_index))
															{
																first_sample = (Sample) neighbor_list.get(first_index);
																Sample second_sample = (Sample) neighbor_list.get(second_index);
																Sample third_sample = (Sample) neighbor_list.get(third_index);
																triangle.moveTo(first_sample.x, first_sample.y);
																triangle.lineTo(second_sample.x, second_sample.y);
																triangle.lineTo(third_sample.x, third_sample.y);
																triangle.closePath();
																if (triangle.contains(xcenter, ycenter))
																{
																	list = new ArrayList();
																	list.add(first_sample);
																	list.add(second_sample);
																	list.add(third_sample);
																	triangle_list.add(list);
																}
															}
														}
													}
												}

												size = triangle_list.size();
												// System.out.println(size + " containing triangle(s).");

												if (size != 0)
												{
													// Find the smallest containing triangle.
													double[] measure = new double[size];
													for (int m = 0; m < size; m++)
													{
														list = (ArrayList) triangle_list.get(m);
														first_sample = (Sample) list.get(0);
														Sample second_sample = (Sample) list.get(1);
														Sample third_sample = (Sample) list.get(2);

														Point2D.Double first_point = new Point2D.Double(first_sample.x,
																first_sample.y);
														Point2D.Double second_point = new Point2D.Double(second_sample.x,
																second_sample.y);
														Point2D.Double third_point = new Point2D.Double(third_sample.x,
																third_sample.y);

														double triangle_area = DataMapper.getTriangleArea(first_point, second_point,
																third_point);
														double perimeter = DataMapper.getTrianglePerimeter(first_point,
																second_point, third_point);
														measure[m] = triangle_area * perimeter;
													}
													int triangle_index = 0;
													double least_measure = measure[0];
													for (int m = 1; m < size; m++)
													{
														if (measure[m] < least_measure)
														{
															least_measure = measure[m];
															triangle_index = m;
														}
													}

													list = (ArrayList) triangle_list.get(triangle_index);
													first_sample = (Sample) list.get(0);
													Sample second_sample = (Sample) list.get(1);
													Sample third_sample = (Sample) list.get(2);
													double intensity = DataMapper.getLinearInterpolation(origin, first_sample,
															second_sample, third_sample);
													Sample sample = new Sample(xcenter, ycenter, intensity);
													list = segment1_data[j][k];
													list.add(sample);

													isPopulated1[j][k] = true;
													isCentered1[j][k] = true;
												} 
												else
												{
													// Look for a triangle that includes the origin and contains one of the
													// neighboring points.
													triangle_list.clear();
													for (int m = 0; m < number_of_neighbors; m++)
													{
														for (int n = 0; n < number_of_neighbors; n++)
														{
															if (m != n)
															{
																for (int p = 0; p < number_of_neighbors; p++)
																{
																	if (p != m && p != n)
																	{
																		Sample corner1 = (Sample) neighbor_list.get(m);
																		Sample corner2 = (Sample) neighbor_list.get(n);
																		Sample interior = (Sample) neighbor_list.get(p);
																		triangle.moveTo(xcenter, ycenter);
																		triangle.lineTo(corner1.x, corner1.y);
																		triangle.lineTo(corner2.x, corner2.y);
																		triangle.closePath();
																		if (triangle.contains(interior.x, interior.y))
																		{
																			list = new ArrayList();
																			list.add(interior);
																			list.add(corner1);
																			list.add(corner2);
																			triangle_list.add(list);
																		}
																	}
																}
															}
														}
													}

													size = triangle_list.size();
													if (size != 0)
													{
														double[] measure = new double[size];
														for (int m = 0; m < size; m++)
														{
															list = (ArrayList) triangle_list.get(m);
															first_sample = (Sample) list.get(0);
															Sample second_sample = (Sample) list.get(1);
															Sample third_sample = (Sample) list.get(2);

															Point2D.Double first_point = new Point2D.Double(xcenter, ycenter);
															Point2D.Double second_point = new Point2D.Double(second_sample.x,
																	second_sample.y);
															Point2D.Double third_point = new Point2D.Double(third_sample.x,
																	third_sample.y);

															double triangle_area = DataMapper.getTriangleArea(first_point,
																	second_point, third_point);
															double perimeter = DataMapper.getTrianglePerimeter(first_point,
																	second_point, third_point);
															measure[m] = triangle_area * perimeter;
														}

														int triangle_index = 0;
														double least_measure = measure[0];
														for (int m = 1; m < size; m++)
														{
															if (measure[m] < least_measure)
															{
																least_measure = measure[m];
																triangle_index = m;
															}
														}

														list = (ArrayList) triangle_list.get(triangle_index);
														first_sample = (Sample) list.get(0);
														Sample second_sample = (Sample) list.get(1);
														Sample third_sample = (Sample) list.get(2);
														double intensity = DataMapper.getLinearExtrapolation(origin, first_sample,
																second_sample, third_sample);
														Sample sample = new Sample(xcenter, ycenter, intensity);
														list = segment1_data[j][k];
														list.add(sample);

														isPopulated1[j][k] = true;
														isCentered1[j][k] = true;
													} 
												}
									        }
									    }
									}
								}
								else if(location_type == 7)
								{
									System.out.println("Proccessing lower corner cell in section " + i);
									//System.out.println("Neighbor list size is " + neighbor_list_size);
									//System.out.println("Neighbor list size is " + neighbor_list.size());
									System.out.println("Cell list size is " + cell_list.size());
									
									
									if(isCentered1[j][k + 1] && isCentered1[j - 1][k])
									{
									    //System.out.println("North and east neighbors are centered.");	
									    if(isPopulated1[j][k])
									    {
									    	list = segment1_data[j][k];
									    	size = list.size();
									    	first_sample = (Sample) list.get(size - 1);
									    	System.out.println("Xcenter = " + String.format("%.3f", xcenter) + ", ycenter = " + String.format("%.3f", ycenter));
									    	//System.out.println("Cell sample is located at x = " + String.format("%.3f", first_sample.x) + ", y = " + String.format("%.3f", first_sample.y));
									    	
									    	list = segment1_data[j][k + 1];
									    	size = list.size();
									    	Sample second_sample = (Sample) list.get(size - 1);
									    	//System.out.println("East sample is located at x = " + String.format("%.3f", second_sample.x) + ", y = " + String.format("%.3f", second_sample.y));
									    	
									    	list = segment1_data[j - 1][k];
									    	size = list.size();
									    	Sample third_sample = (Sample) list.get(size - 1);
									    	//System.out.println("North sample is located at x = " + String.format("%.3f", third_sample.x) + ", y = " + String.format("%.3f", third_sample.y));
									    	
									    	Path2D.Double triangle = new Path2D.Double();
											triangle.moveTo(xcenter, ycenter);
											triangle.lineTo(second_sample.x, second_sample.y);
											triangle.lineTo(third_sample.x, third_sample.y);
											triangle.closePath();
											if (triangle.contains(first_sample.x, first_sample.y))
											{
												double intensity = DataMapper.getLinearExtrapolation(origin, first_sample,second_sample, third_sample);
												Sample sample = new Sample(xcenter, ycenter, intensity);
												list = segment1_data[j][k];
												isCentered1[j][k] = true;	
												//System.out.println("Found extrapolating triangle.");
											}
											else
											{
											    triangle.reset();
											    triangle.moveTo(first_sample.x, first_sample.y);
											    triangle.lineTo(second_sample.x, second_sample.y);
												triangle.lineTo(third_sample.x, third_sample.y);
												triangle.closePath();
												
												if (triangle.contains(xcenter, ycenter))
												{
													double intensity = DataMapper.getLinearInterpolation(origin, first_sample,second_sample, third_sample);
													Sample sample = new Sample(xcenter, ycenter, intensity);
													list = segment1_data[j][k];
													list.add(sample);
													isCentered1[j][k] = true;
													//System.out.println("Found interpolating triangle.");
												}
												else
												{
													//System.out.println("Did not find extrapolating or interpolating triangle.");
													// If there are no triangles, get the bisecting average between the cell sample
													// and the north sample, and then do a simple extrapolation from the 
													// bisecting average and the east sample.
													Point2D.Double location = DataMapper.getBisectingPoint(first_sample, third_sample, origin);
													double location_x = location.getX();
													double location_y = location.getY();
													double intensity = DataMapper.getBisectingAverage(first_sample, third_sample, origin);
													
													double intensity_delta = (intensity - second_sample.intensity) / (second_sample.x - location_x);
													double intensity_increment = (xcenter - location_x) * intensity_delta;
													intensity += intensity_increment;
													Sample sample = new Sample(xcenter, ycenter, intensity);
                                                    list = segment1_data[j][k];
												    list.add(sample);
													isCentered1[j][k] = true;
													//System.out.println("Centered a corner cell with an adjusted bisecting value.");
												}
											}
									    }
									}
									
									else if(isPopulated1[j][k])
									{
										//System.out.println("Did not find centered neighbors.");
										Sample sample = (Sample)cell_list.get(0);
										
										if(sample.x > xcenter && sample.y > ycenter)
										{
											System.out.println("Sample is in fourth quadrant.");     	
										}
										else if(sample.x < xcenter && sample.y > ycenter)
										{
											System.out.println("Sample is in third quadrant.");	
										}
										else if(sample.x < xcenter && sample.y < ycenter)
										{
											System.out.println("Sample is in second quadrant.");	
										}
										else if(sample.x > xcenter && sample.y < ycenter)
										{
											System.out.println("Sample is in first quadrant.");
										}
									} 
									else
									{
										//System.out.println("Cell is not populated.");
									}	
								}
								else if(location_type == 3)
								{
									System.out.println("Processing upper right corner cell in section " + i);
									//System.out.println("Neighbor list size is " + neighbor_list_size);
									//System.out.println("Neighbor list size is " + neighbor_list.size());
									System.out.println("Cell list size is " + cell_list.size());
									
									if(isCentered1[j][k - 1] && isCentered1[j + 1][k])
									{
									    System.out.println("South and west neighbors are centered.");	
									    if(isPopulated1[j][k])
									    {
									    	list = segment1_data[j][k];
									    	size = list.size();
									    	first_sample = (Sample) list.get(size - 1);
									    	System.out.println("Xcenter = " + String.format("%.3f", xcenter) + ", ycenter = " + String.format("%.3f", ycenter));
									    	System.out.println("Cell sample is located at x = " + String.format("%.3f", first_sample.x) + ", y = " + String.format("%.3f", first_sample.y));
									    	
									    	list = segment1_data[j][k - 1];
									    	size = list.size();
									    	Sample second_sample = (Sample) list.get(size - 1);
									    	System.out.println("West sample is located at x = " + String.format("%.3f", second_sample.x) + ", y = " + String.format("%.3f", second_sample.y));
									    	
									    	list = segment1_data[j + 1][k];
									    	size = list.size();
									    	Sample third_sample = (Sample) list.get(size - 1);
									    	System.out.println("South sample is located at x = " + String.format("%.3f", third_sample.x) + ", y = " + String.format("%.3f", third_sample.y));
									    	
									    	Path2D.Double triangle = new Path2D.Double();
											triangle.moveTo(xcenter, ycenter);
											triangle.lineTo(second_sample.x, second_sample.y);
											triangle.lineTo(third_sample.x, third_sample.y);
											triangle.closePath();
											if (triangle.contains(first_sample.x, first_sample.y))
											{
												double intensity = DataMapper.getLinearExtrapolation(origin, first_sample,second_sample, third_sample);
												Sample sample = new Sample(xcenter, ycenter, intensity);
												list = segment1_data[j][k];
												isCentered1[j][k] = true;	
												System.out.println("Found extrapolating triangle.");
											}
											else
											{
											    triangle.reset();
											    triangle.moveTo(first_sample.x, first_sample.y);
											    triangle.lineTo(second_sample.x, second_sample.y);
												triangle.lineTo(third_sample.x, third_sample.y);
												triangle.closePath();
												
												if (triangle.contains(xcenter, ycenter))
												{
													double intensity = DataMapper.getLinearInterpolation(origin, first_sample,second_sample, third_sample);
													Sample sample = new Sample(xcenter, ycenter, intensity);
													list = segment1_data[j][k];
													list.add(sample);
													isCentered1[j][k] = true;
													System.out.println("Found interpolating triangle.");
												}
												else
												{
													System.out.println("Did not find extrapolating or interpolating triangle.");
													// If there are no triangles, get the bisecting average between the cell sample
													// and the north sample, and then do a simple extrapolation from the 
													// bisecting average and the east sample.
													Point2D.Double location = DataMapper.getBisectingPoint(first_sample, third_sample, origin);
													double location_x = location.getX();
													double location_y = location.getY();
													double intensity = DataMapper.getBisectingAverage(first_sample, third_sample, origin);
													
													double intensity_delta = (intensity - second_sample.intensity) / (location_x - second_sample.x);
													double intensity_increment = (location_x - xcenter) * intensity_delta;
													intensity += intensity_increment;
													Sample sample = new Sample(xcenter, ycenter, intensity);
                                                    list = segment1_data[j][k];
												    list.add(sample);
													isCentered1[j][k] = true;
													System.out.println("Centered a corner cell with an adjusted bisecting value.");
												}
											}
									    }
									}
									else if(isCentered1[j][k - 1] && isCentered1[j + 1][k - 1])
									{
									    System.out.println("West and southwest neighbors are centered.");	
									    if(isPopulated1[j][k])
									    {
									    	list = segment1_data[j][k];
									    	size = list.size();
									    	first_sample = (Sample) list.get(size - 1);
									    	System.out.println("Xcenter = " + String.format("%.3f", xcenter) + ", ycenter = " + String.format("%.3f", ycenter));
									    	System.out.println("Cell sample is located at x = " + String.format("%.3f", first_sample.x) + ", y = " + String.format("%.3f", first_sample.y));
									    	
									    	list = segment1_data[j][k - 1];
									    	size = list.size();
									    	Sample second_sample = (Sample) list.get(size - 1);
									    	System.out.println("West sample is located at x = " + String.format("%.3f", second_sample.x) + ", y = " + String.format("%.3f", second_sample.y));
									    	
									    	list = segment1_data[j + 1][k - 1];
									    	size = list.size();
									    	Sample third_sample = (Sample) list.get(size - 1);
									    	System.out.println("Southwest sample is located at x = " + String.format("%.3f", third_sample.x) + ", y = " + String.format("%.3f", third_sample.y));
									    	
									    	Path2D.Double triangle = new Path2D.Double();
											triangle.moveTo(xcenter, ycenter);
											triangle.lineTo(second_sample.x, second_sample.y);
											triangle.lineTo(third_sample.x, third_sample.y);
											triangle.closePath();
											if (triangle.contains(first_sample.x, first_sample.y))
											{
												double intensity = DataMapper.getLinearExtrapolation(origin, first_sample,second_sample, third_sample);
												Sample sample = new Sample(xcenter, ycenter, intensity);
												list = segment1_data[j][k];
												isCentered1[j][k] = true;	
												//System.out.println("Found extrapolating triangle.");
											}
											else
											{
											    triangle.reset();
											    triangle.moveTo(first_sample.x, first_sample.y);
											    triangle.lineTo(second_sample.x, second_sample.y);
												triangle.lineTo(third_sample.x, third_sample.y);
												triangle.closePath();
												
												if (triangle.contains(xcenter, ycenter))
												{
													double intensity = DataMapper.getLinearInterpolation(origin, first_sample,second_sample, third_sample);
													Sample sample = new Sample(xcenter, ycenter, intensity);
													list = segment1_data[j][k];
													list.add(sample);
													isCentered1[j][k] = true;
													//System.out.println("Found interpolating triangle.");
												}
												else
												{
													//System.out.println("Did not find extrapolating or interpolating triangle.");
													// If there are no triangles, get the bisecting average between the cell sample
													// and the north sample, and then do a simple extrapolation from the 
													// bisecting average and the east sample.
													Point2D.Double location = DataMapper.getBisectingPoint(first_sample, third_sample, origin);
													double location_x = location.getX();
													double location_y = location.getY();
													double intensity = DataMapper.getBisectingAverage(first_sample, third_sample, origin);
													
													double intensity_delta = (intensity - second_sample.intensity) / (second_sample.x - location_x);
													double intensity_increment = (xcenter - location_x) * intensity_delta;
													intensity += intensity_increment;
													Sample sample = new Sample(xcenter, ycenter, intensity);
                                                    list = segment1_data[j][k];
												    list.add(sample);
													isCentered1[j][k] = true;
													//System.out.println("Centered a corner cell with an adjusted bisecting value.");
												}
											}
									    }
									}
									else if(isPopulated1[j][k])
									{
										System.out.println("Did not find centered neighbors.");
										Sample sample = (Sample)cell_list.get(0);
										
										if(sample.x > xcenter && sample.y > ycenter)
										{
											System.out.println("Sample is in fourth quadrant.");     	
										}
										else if(sample.x < xcenter && sample.y > ycenter)
										{
											System.out.println("Sample is in third quadrant.");	
										}
										else if(sample.x < xcenter && sample.y < ycenter)
										{
											System.out.println("Sample is in second quadrant.");	
										}
										else if(sample.x > xcenter && sample.y < ycenter)
										{
											System.out.println("Sample is in first quadrant.");
										}
									} 
									else
									{
										System.out.println("Cell is not populated.");
									}	
								}
							}
							xcenter += xcell_width;
						}
						ycenter += ycell_width;
					}
					previous_number_of_populated_cells = number_of_populated_cells;
					previous_number_of_centered_cells = number_of_centered_cells;
					number_of_populated_cells = 0;
					number_of_centered_cells = 0;
					for (int m = 0; m < raster_ydim; m++)
					{
						for (int n = 0; n < raster_xdim; n++)
						{
							if (isPopulated1[m][n])
								number_of_populated_cells++;
							if (isCentered1[m][n])
								number_of_centered_cells++;
						}
					}
				}

				number_of_centered_cells = 0;
				number_of_populated_cells = 0;
				for (int j = 0; j < raster_ydim; j++)
				{
					for (int k = 0; k < raster_xdim; k++)
					{
						if(isPopulated1[j][k])
						{
							number_of_populated_cells++;
							if(isCentered1[j][k])
							{
							    number_of_centered_cells++;
							    if(i == 33)
							       System.out.print("+ ");
							}
							else if(i == 33)
								System.out.print("x ");	
						}
						else if(i == 33)
							System.out.print("o ");	
					}
					if(i == 33)
					    System.out.println();
				}

				System.out.println("The number of populated cells after third pass is " + number_of_populated_cells);
				System.out.println(
						"The number of cells with centered samples after third pass is " + number_of_centered_cells);
				System.out.println();

				segment_list = segment_data[1][i];
				size = segment_list.size();
				// System.out.println("The number of samples being assigned to the raster from
				// the second segment is " + size);
				for (int j = 0; j < size; j++)
				{
					Sample sample = (Sample) segment_list.get(j);
					double current_location = raster_xmin + xcell_width;
					int x_index = 0;
					while (sample.x >= current_location)
					{
						x_index++;
						current_location += xcell_width;
					}
					double cell_lower_bound = x_index * xcell_width + raster_xmin;
					double cell_upper_bound = cell_lower_bound + xcell_width;
					current_location = raster_ymin + ycell_width;
					int y_index = 0;
					while (sample.y > current_location)
					{
						y_index++;
						current_location += ycell_width;
					}

					cell_lower_bound = y_index * ycell_width + raster_ymin;
					cell_upper_bound = cell_lower_bound + ycell_width;
					ArrayList sample_list = segment2_data[y_index][x_index];
					sample_list.add(sample);
					isPopulated2[y_index][x_index] = true;
					sampleNumber2[y_index][x_index]++;
				}

				number_of_populated_cells = 0;

				for (int j = 0; j < raster_ydim; j++)
				{
					for (int k = 0; k < raster_xdim; k++)
					{
						if (isPopulated2[j][k])
							number_of_populated_cells++;
					}
				}
				// System.out.println("The number of populated cells in the second raster is " +
				// number_of_populated_cells);

			}
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

	public int getLocationType(int xindex, int yindex, int xdim, int ydim)
	{
		int location_type = 0;
		if (yindex == 0)
		{
			if (xindex == 0)
			{
				location_type = 1;
			} else if (xindex % xdim != xdim - 1)
			{
				location_type = 2;
			} else
			{
				location_type = 3;
			}
		} else if (yindex % ydim != ydim - 1)
		{
			if (xindex == 0)
			{
				location_type = 4;
			} else if (xindex % xdim != xdim - 1)
			{
				location_type = 5;
			} else
			{
				location_type = 6;
			}
		} else
		{
			if (xindex == 0)
			{
				location_type = 7;
			} else if (xindex % xdim != xdim - 1)
			{
				location_type = 8;
			} else
			{
				location_type = 9;
			}
		}
		return (location_type);
	}
}
