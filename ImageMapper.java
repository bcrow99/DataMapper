import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.awt.Point;

public class ImageMapper
{
	public static void smooth(int src[], int xdim, int ydim, double smooth_factor, int number_of_iterations, int dst[])
	{
		double even[] = new double[xdim * ydim];
		double odd[] = new double[xdim * ydim];
		double weight[] = new double[xdim * ydim];
		double product[] = new double[xdim * ydim];
		double current_src[];
		double current_dst[];
		double dx, dy, dxy, sum, factor;
		double total_weights;
		int index;
		int i, j, k;

		factor = 1.0 / (2 * smooth_factor * smooth_factor);
		current_src = odd;
		current_dst = even;

		for (i = 0; i < xdim * ydim; i++)
			current_src[i] = current_dst[i] = (double) src[i];

		for (i = 0; i < number_of_iterations; i++)
		{
			if (i % 2 == 0)
			{
				current_src = even;
				current_dst = odd;
			} else
			{
				current_src = odd;
				current_dst = even;
			}

			for (j = 1; j < ydim - 1; j++)
			{
				index = j * xdim;
				for (k = 1; k < xdim - 1; k++)
				{
					index++;
					dx = (current_src[index - 1] - current_src[index + 1]) / 2.;
					dy = (current_src[index - xdim] - current_src[index + xdim]) / 2.;
					dxy = dx * dx + dy * dy;
					weight[index] = java.lang.Math.exp(-dxy * factor);
					product[index] = weight[index] * current_src[index];
				}
			}

			for (j = 2; j < ydim - 2; j++)
			{
				index = j * xdim + 2;
				total_weights = weight[index - xdim - 1] + weight[index - xdim] + weight[index - xdim + 1]
						+ weight[index - 1] + weight[index] + weight[index + 1] + weight[index + xdim - 1]
						+ weight[index + xdim] + weight[index + xdim + 1];
				sum = product[index - xdim - 1] + product[index - xdim] + product[index - xdim + 1] + product[index - 1]
						+ product[index] + product[index + 1] + product[index + xdim - 1] + product[index + xdim]
						+ product[index + xdim + 1];

				for (k = 2; k < xdim - 2; k++)
				{
					current_dst[index] = sum / total_weights;

					total_weights += weight[index + xdim + 2] + weight[index + 2] + weight[index - xdim + 2]
							- weight[index - xdim - 1] - weight[index - 1] - weight[index + xdim - 1];

					sum += product[index - xdim + 2] + product[index + 2] + product[index + xdim + 2]
							- product[index - xdim - 1] - product[index - 1] - product[index + xdim - 1];
					index++;
				}
			}
		}
		for (i = 0; i < xdim * ydim; i++)
			dst[i] = (int) current_dst[i];
	}

	public static int getLocationType(int xindex, int yindex, int xdim, int ydim)
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

	public static void getImageDilation(double src[][], boolean isInterpolated[][], double dst[][])
	{
		int ydim = src.length;
		int xdim = src[0].length;

		double source[];
		double dest[];
		double gray1[] = new double[xdim * ydim];
		double gray2[] = new double[xdim * ydim];
		boolean isAssigned[] = new boolean[xdim * ydim];
		int number_of_uninterpolated_cells = 0;
		int number_of_iterations = 0;
		for (int i = 0; i < ydim; i++)
		{
			for (int j = 0; j < xdim; j++)
			{
				int k = i * xdim + j;
				gray1[k] = src[i][j];
				isAssigned[k] = isInterpolated[i][j];
				if (isAssigned[k] == false)
					number_of_uninterpolated_cells++;
			}
		}
		// System.out.println("Origninal number of uninterpolated_cells is " +
		// number_of_uninterpolated_cells);
		boolean even = true; // Keep track of which buffer is the source and which is the destination.
		while(number_of_uninterpolated_cells != 0)
		{
			number_of_iterations++;
			if (even == true)
			{
				source = gray1;
				dest = gray2;
				even = false;
			} else
			{
				source = gray2;
				dest = gray1;
				even = true;
			}
			dilateImage(source, isAssigned, xdim, ydim, dest);
			number_of_uninterpolated_cells = 0;
			for (int i = 0; i < xdim * ydim; i++)
			{
				if (isAssigned[i] == false)
					number_of_uninterpolated_cells++;
			}
			// System.out.println("The number of uninterpolated cells after dilation was " +
			// number_of_uninterpolated_cells);
		}

		if (even == true)
		{
			int k = 0;
			for (int i = 0; i < ydim; i++)
			{
				for (int j = 0; j < xdim; j++)
				{
					dst[i][j] = gray1[k++];
				}
			}
		} else
		{
			int k = 0;
			for (int i = 0; i < ydim; i++)
			{
				for (int j = 0; j < xdim; j++)
				{
					dst[i][j] = gray2[k++];
				}
			}
		}
		System.out.println("The number of iterations was " + number_of_iterations);
	}

	// This function modifies values in isInterpolated and dst, and can be called
	// multiple times
	// until all the values in isInterpolated are true.
	// Also, using single index into image to keep low level code simple--will have
	// to reformat
	// data for processing--see getVarianceImage.
	public static void dilateImage(double src[], boolean isInterpolated[], int xdim, int ydim, double dst[])
	{
		boolean wasInterpolated[] = new boolean[xdim * ydim];
		for (int i = 0; i < ydim; i++)
		{
			for (int j = 0; j < xdim; j++)
			{
				int k = i * xdim + j;
				if (isInterpolated[k])
				{
					dst[k] = src[k];
					wasInterpolated[k] = true;
				} else
				{
					double diagonal_weight = 0.7071; // Orthogonal weight is 1.
					double total_weight = 0;
					double value = 0.;
					int number_of_neighbors = 0;
					int location_type = getLocationType(j, i, xdim, ydim);
					switch (location_type)
					{
					case 1: // Orthogonal
						if (isInterpolated[k + 1])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k + 1];
						}

						if (isInterpolated[k + xdim])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k + xdim];
						}

						// Diagonal
						if (isInterpolated[k + xdim + 1])
						{
							number_of_neighbors++;
							total_weight += diagonal_weight;
							value += diagonal_weight * src[k + xdim + 1];
						}

						break;

					case 2: // Orthogonal
						if (isInterpolated[k - 1])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k - 1];
						}

						if (isInterpolated[k + 1])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k + 1];
						}

						if (isInterpolated[k + xdim])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k + xdim];
						}

						// Diagonal
						if (isInterpolated[k + xdim - 1])
						{
							number_of_neighbors++;
							total_weight += diagonal_weight;
							value += diagonal_weight * src[k + xdim - 1];
						}

						if (isInterpolated[k + xdim + 1])
						{
							number_of_neighbors++;
							total_weight += diagonal_weight;
							value += diagonal_weight * src[k + xdim + 1];
						}

						break;

					case 3: // Orthogonal
						if (isInterpolated[k - 1])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k - 1];
						}

						if (isInterpolated[k + xdim])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k - 1];
						}

						// Diagonal
						if (isInterpolated[k + xdim - 1])
						{
							number_of_neighbors++;
							total_weight += diagonal_weight;
							value += diagonal_weight * src[k + xdim - 1];
						}

						break;

					case 4: // Orthogonal
						if (isInterpolated[k - xdim])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k - xdim];
						}
						if (isInterpolated[k + xdim])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k + xdim];
						}

						if (isInterpolated[k + 1])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k + 1];
						}

						// Diagonal
						if (isInterpolated[k - xdim + 1])
						{
							number_of_neighbors++;
							total_weight += diagonal_weight;
							value += diagonal_weight * src[k - xdim + 1];
						}
						if (isInterpolated[k + xdim + 1])
						{
							number_of_neighbors++;
							total_weight += diagonal_weight;
							value += diagonal_weight * src[k + xdim + 1];
						}

						break;

					case 5: // Orthogonal
						if (isInterpolated[k - xdim])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k - xdim];
						}
						if (isInterpolated[k + xdim])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k + xdim];
						}

						if (isInterpolated[k - 1])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k - 1];
						}

						if (isInterpolated[k + 1])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k + 1];
						}

						// Diagonal
						if (isInterpolated[k - xdim - 1])
						{
							number_of_neighbors++;
							total_weight += diagonal_weight;
							value += diagonal_weight * src[k - xdim - 1];
						}
						if (isInterpolated[k + xdim - 1])
						{
							number_of_neighbors++;
							total_weight += diagonal_weight;
							value += diagonal_weight * src[k + xdim - 1];
						}

						if (isInterpolated[k - xdim + 1])
						{
							number_of_neighbors++;
							total_weight += diagonal_weight;
							value += diagonal_weight * src[k - xdim + 1];
						}
						if (isInterpolated[k + xdim + 1])
						{
							number_of_neighbors++;
							total_weight += diagonal_weight;
							value += diagonal_weight * src[k + xdim + 1];
						}

						break;

					case 6: // Orthogonal
						if (isInterpolated[k - xdim])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k - xdim];
						}
						if (isInterpolated[k + xdim])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k + xdim];
						}

						if (isInterpolated[k - 1])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k - 1];
						}

						// Diagonal
						if (isInterpolated[k - xdim - 1])
						{
							number_of_neighbors++;
							total_weight += diagonal_weight;
							value += diagonal_weight * src[k - xdim - 1];
						}

						if (isInterpolated[k + xdim - 1])
						{
							number_of_neighbors++;
							total_weight += diagonal_weight;
							value += diagonal_weight * src[k + xdim - 1];
						}

						break;

					case 7: // Orthogonal
						if (isInterpolated[k - xdim])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k - xdim];
						}
						if (isInterpolated[k + 1])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k + 1];
						}

						// Diagonal
						if (isInterpolated[k - xdim + 1])
						{
							number_of_neighbors++;
							total_weight += diagonal_weight;
							value += diagonal_weight * src[k - xdim + 1];
						}

						break;

					case 8: // Orthogonal
						if (isInterpolated[k - xdim])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k - xdim];
						}
						if (isInterpolated[k - 1])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k - 1];
						}
						if (isInterpolated[k + 1])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k + 1];
						}

						// Diagonal
						if (isInterpolated[k - xdim - 1])
						{
							number_of_neighbors++;
							total_weight += diagonal_weight;
							value += diagonal_weight * src[k - xdim - 1];
						}
						if (isInterpolated[k - xdim + 1])
						{
							number_of_neighbors++;
							total_weight += diagonal_weight;
							value += diagonal_weight * src[k - xdim + 1];
						}
						break;

					case 9: // Orthogonal
						if (isInterpolated[k - xdim])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k - xdim];
						}
						if (isInterpolated[k - 1])
						{
							number_of_neighbors++;
							total_weight += 1.;
							value += src[k - 1];
						}

						// Diagonal
						if (isInterpolated[k - xdim - 1])
						{
							number_of_neighbors++;
							total_weight += diagonal_weight;
							value += diagonal_weight * src[k - xdim - 1];
						}
						break;

					default:
						break;
					}

					if (number_of_neighbors > 0) // Found a neighbor this iteration, set value.
					{
						value /= total_weight;
						dst[k] = (int) value;
						wasInterpolated[k] = true;
						// System.out.println("Number of neighbors was " + number_of_neighbors);
					} else
					{
						dst[k] = 0;
						wasInterpolated[k] = false;
						// System.out.println("Found a cell with no neighbors.");
						// System.out.println("Set boolean to false.");
						// No neighbors, set value to zero.
					}
				}
			}
		}
		for (int i = 0; i < xdim * ydim; i++)
		{
			isInterpolated[i] = wasInterpolated[i];
		}
	}

	public static int[] avgAreaXTransform(int source[], int xdim, int ydim, int new_xdim)
	{
		double differential         = (double) xdim / (double) new_xdim;
		int    weight               = (int)(differential * xdim) * 1000;
		int    factor               = xdim * 1000;
		double real_position        = 0.;
		int    current_whole_number = 0;

		int [] start_fraction   = new int[new_xdim];
		int [] end_fraction     = new int[new_xdim];
		int [] number_of_pixels = new int[new_xdim];
		for(int i = 0; i < new_xdim; i++)
		{
		    double  previous_position     = real_position;
		    int     previous_whole_number = current_whole_number;
		    
		    real_position       += differential;
		    current_whole_number = (int) (real_position);
			number_of_pixels[i]  = current_whole_number - previous_whole_number;
			start_fraction[i]    = (int) (1000. * (1. - (previous_position - (double) (previous_whole_number))));
			end_fraction[i]      = (int) (1000. * (real_position - (double) (current_whole_number)));	
		}
		
		int[] dest = new int[ydim * new_xdim];	
		for (int y = 0; y < ydim; y++)
		{
			int i = y * new_xdim;
			int j = y * xdim;
			for (int x = 0; x < new_xdim - 1; x++)
			{
				if (number_of_pixels[x] == 0)
				{
					dest[i] = source[j];
					i++;
				} 
				else
				{
					int total = start_fraction[x] * xdim * source[j];
					j++;
					int k = number_of_pixels[x] - 1;
					while (k > 0)
					{
						total += factor * source[j];
						j++;
						k--;
					}
					total += end_fraction[x] * xdim * source[j];
					total /= weight;
					dest[i] = total;
					i++;
				}
			}
			
			int x = new_xdim - 1;
			if (number_of_pixels[x] == 0)
				dest[i] = source[j];
			else
			{
				int total = start_fraction[x] * xdim * source[j];
				j++;
				int k = number_of_pixels[x] - 1;
				while (k > 0)
				{
					total += factor * source[j];
					j++;
					k--;
				}
				total /= weight - end_fraction[x] * xdim;
				dest[i] = total;
			}
		}
		return(dest);
	}
	
	public static int[][] avgAreaXTransform(int src[][], int new_xdim)
	{
		int ydim = src.length;
		int xdim = src[0].length;
		int[][] dst = new int[ydim][new_xdim];	
		
		int [] source = new int[xdim * ydim];
		int [] dest   = new int[new_xdim * ydim];
		
		// Changing from 2-d array to 1-d array to simplify processing.
		// The main issue is a 2-d format complicates making the transform
		// work in both directions.  On the other hand, the 2-d format
		// is occasionally convenient.  
		for (int i = 0; i < ydim; i++)
		{
			for (int j = 0; j < xdim; j++)
			{
				int k = i * xdim + j;
				source[k] = src[i][j];
			}
		}
		
		double differential         = (double) xdim / (double) new_xdim;
		int    weight               = (int)(differential * xdim) * 1000;
		int    factor               = xdim * 1000;
		double real_position        = 0.;
		int    current_whole_number = 0;

		int [] start_fraction   = new int[new_xdim];
		int [] end_fraction     = new int[new_xdim];
		int [] number_of_pixels = new int[new_xdim];
		
		for(int i = 0; i < new_xdim; i++)
		{
		    double  previous_position     = real_position;
		    int     previous_whole_number = current_whole_number;
		    
		    real_position       += differential;
		    current_whole_number = (int) (real_position);
			number_of_pixels[i]  = current_whole_number - previous_whole_number;
			start_fraction[i]    = (int) (1000. * (1. - (previous_position - (double) (previous_whole_number))));
			end_fraction[i]      = (int) (1000. * (real_position - (double) (current_whole_number)));	
		}
		
		for (int y = 0; y < ydim; y++)
		{
			int i = y * new_xdim;
			int j = y * xdim;
			for (int x = 0; x < new_xdim - 1; x++)
			{
				if (number_of_pixels[x] == 0)
				{
					dest[i] = source[j];
					i++;
				} 
				else
				{
					int total = start_fraction[x] * xdim * source[j];
					j++;
					int k = number_of_pixels[x] - 1;
					while (k > 0)
					{
						total += factor * source[j];
						j++;
						k--;
					}
					total += end_fraction[x] * xdim * source[j];
					total /= weight;
					dest[i] = total;
					i++;
				}
			}
			
			int x = new_xdim - 1;
			if (number_of_pixels[x] == 0)
				dest[i] = source[j];
			else
			{
				int total = start_fraction[x] * xdim * source[j];
				j++;
				int k = number_of_pixels[x] - 1;
				while (k > 0)
				{
					total += factor * source[j];
					j++;
					k--;
				}
				total /= weight - end_fraction[x] * xdim;
				dest[i] = total;
			}
		}
		
		// Back to 2-d.
		for (int i = 0; i < ydim; i++)
		{
			for (int j = 0; j < new_xdim; j++)
			{
				int k = i * new_xdim + j;
				dst[i][j] = dest[k];
			}
		}
		return(dst);
	}
	
	public static int [] avgAreaYTransform(int src[], int xdim, int ydim, int new_ydim)
	{
		double differential         = (double) ydim / (double) new_ydim;
		int    weight               = (int) (differential * ydim) * 1000;
		int    factor               = ydim * 1000;
		double real_position        = 0.;
		int    current_whole_number = 0;
		
		int [] start_fraction   = new int[new_ydim];
		int [] end_fraction     = new int[new_ydim];
		int [] number_of_pixels = new int[new_ydim];
		for (int i = 0; i < new_ydim; i++)
		{
			double previous_position     = real_position;
			int    previous_whole_number = current_whole_number;
			
			real_position       += differential;
			current_whole_number = (int) (real_position);
			number_of_pixels[i]  = current_whole_number - previous_whole_number;
			start_fraction[i]    = (int) (1000. * (1. - (previous_position - (double) (previous_whole_number))));
			end_fraction[i]      = (int) (1000. * (real_position - (double) (current_whole_number)));
		}

		int [] dst = new int[xdim * new_ydim];
		for (int x = 0; x < xdim; x++)
		{
			int i = x;
			int j = x;
			for (int y = 0; y < new_ydim - 1; y++)
			{
				if (number_of_pixels[y] == 0)
				{
					dst[i] = src[j];
					i += xdim;
				} 
				else
				{
					int total = start_fraction[y] * ydim * src[j];
					j += xdim;
					int k = number_of_pixels[y] - 1;
					while (k > 0)
					{
						total += factor * src[j];
						j += xdim;
						k--;
					}
					total += end_fraction[y] * ydim * src[j];
					total /= weight;
					dst[i] = total;
					i += xdim;
				}
			}
			int y = new_ydim - 1;
			if (number_of_pixels[y] == 0)
				dst[i] = src[j];
			else
			{
				int total = start_fraction[y] * ydim * src[j];
				j += xdim;
				int k = number_of_pixels[y] - 1;
				while (k > 0)
				{
					total += factor * src[j];
					j += xdim;
					k--;
				}
				total /= weight - end_fraction[y] * ydim;
				dst[i] = total;
			}
		}
		return(dst);
	}
	
	public static int[][] avgAreaYTransform(int src[][], int new_ydim)
	{
		int ydim = src.length;
		int xdim = src[0].length;
			
		// Changing from 2-d array to 1-d array to simplify processing.
		int [] source = new int[xdim * ydim];
		int [] dest   = new int[xdim * new_ydim];
		for (int i = 0; i < ydim; i++)
		{
			int k = 0;
			for (int j = 0; j < xdim; j++)
			{
				source[k] = src[i][j];
				k++;
			}
		}
		
		double differential         = (double) ydim / (double) new_ydim;
		int    weight               = (int) (differential * ydim) * 1000;
		int    factor               = ydim * 1000;
		double real_position        = 0.;
		int    current_whole_number = 0;
		
		int [] start_fraction   = new int[new_ydim];
		int [] end_fraction     = new int[new_ydim];
		int [] number_of_pixels = new int[new_ydim];
		for (int i = 0; i < new_ydim; i++)
		{
			double previous_position     = real_position;
			int    previous_whole_number = current_whole_number;
			
			real_position       += differential;
			current_whole_number = (int) (real_position);
			number_of_pixels[i]  = current_whole_number - previous_whole_number;
			start_fraction[i]    = (int) (1000. * (1. - (previous_position - (double) (previous_whole_number))));
			end_fraction[i]      = (int) (1000. * (real_position - (double) (current_whole_number)));
		}

		for (int x = 0; x < xdim; x++)
		{
			int i = x;
			int j = x;
			for (int y = 0; y < new_ydim - 1; y++)
			{
				if (number_of_pixels[y] == 0)
				{
					dest[i] = source[j];
					i += xdim;
				} 
				else
				{
					int total = start_fraction[y] * ydim * source[j];
					j += xdim;
					int k = number_of_pixels[y] - 1;
					while (k > 0)
					{
						total += factor * source[j];
						j += xdim;
						k--;
					}
					total += end_fraction[y] * ydim * source[j];
					total /= weight;
					dest[i] = total;
					i += xdim;
				}
			}
			int y = new_ydim - 1;
			if (number_of_pixels[y] == 0)
				dest[i] = source[j];
			else
			{
				int total = start_fraction[y] * ydim * source[j];
				j += xdim;
				int k = number_of_pixels[y] - 1;
				while (k > 0)
				{
					total += factor * source[j];
					j += xdim;
					k--;
				}
				total /= weight - end_fraction[y] * ydim;
				dest[i] = total;
			}
		}
		
		// Back to 2-d.
		int[][] dst = new int[new_ydim][xdim];
		for (int i = 0; i < new_ydim; i++)
		{
			int k = 0;
			for (int j = 0; j < xdim; j++)
			{
				dst[i][j] = dest[k];
				k++;
			}
		}

		return(dst);
		
	}
	
	public static int [] avgAreaTransform(int src[], int xdim, int ydim, int new_xdim, int new_ydim)
	{
		int [] intermediate = avgAreaXTransform(src, xdim, ydim, new_xdim);
	    int [] dst          = avgAreaYTransform(intermediate, new_xdim, ydim, new_ydim);
	    return(dst);
	}
	
	public static int [][] avgAreaTransform(int src[][], int new_xdim, int new_ydim)
	{
		int ydim = src.length;
		int xdim = src[0].length;
			
		// Changing from 2-d array to 1-d array to simplify processing.
		int [] source = new int[xdim * ydim];
		for (int i = 0; i < ydim; i++)
		{
			int k = 0;
			for (int j = 0; j < xdim; j++)
			{
				source[k] = src[i][j];
				k++;
			}
		}
		int [] intermediate = avgAreaXTransform(source, xdim, ydim, new_xdim);
		int [] dest         = avgAreaYTransform(intermediate, new_xdim, ydim, new_ydim);
		
		// Back to 2-d.
		int[][] dst = new int[new_ydim][new_xdim];
		for (int i = 0; i < new_ydim; i++)
		{
			int k = 0;
			for (int j = 0; j < xdim; j++)
			{
				dst[i][j] = dest[k];
				k++;
			}
		}

		return(dst);
	}
	
	public static ArrayList[][] getGradient(int src[][])
	{
		int ydim = src.length;
		int xdim = src[0].length;
		ArrayList[][] dst = new ArrayList[ydim][xdim];

		for (int i = 0; i < ydim; i++)
		{
			for (int j = 0; j < xdim; j++)
			{
				int type = getLocationType(j, i, xdim, ydim);
				double xgradient = 0;
				double ygradient = 0;
				if (type == 1)
				{
					xgradient = Double.NaN;
					ygradient = Double.NaN;

					ArrayList gradient_list = new ArrayList();
					gradient_list.add(xgradient);
					gradient_list.add(ygradient);
					dst[i][j] = gradient_list;
				} else if (type == 2)
				{
					xgradient = (src[i][j + 1] - src[i][j - 1]) + (src[i + 1][j + 1] - src[i + 1][j - 1]);
					xgradient /= 2;
					ygradient = Double.NaN;

					ArrayList gradient_list = new ArrayList();
					gradient_list.add(xgradient);
					gradient_list.add(ygradient);
					dst[i][j] = gradient_list;
				} else if (type == 3)
				{
					xgradient = Double.NaN;
					ygradient = Double.NaN;

					ArrayList gradient_list = new ArrayList();
					gradient_list.add(xgradient);
					gradient_list.add(ygradient);
					dst[i][j] = gradient_list;
				} else if (type == 4)
				{
					xgradient = Double.NaN;
					ygradient = (src[i + 1][j] - src[i - 1][j]) + (src[i + 1][j + 1] - src[i - 1][j + 1]);
					ygradient /= 2;

					ArrayList gradient_list = new ArrayList();
					gradient_list.add(xgradient);
					gradient_list.add(ygradient);
					dst[i][j] = gradient_list;
				} else if (type == 5)
				{
					xgradient = src[i - 1][j + 1] - src[i - 1][j - 1] + src[i][j + 1] - src[i][j - 1]
							+ src[i + 1][j + 1] - src[i + 1][j - 1];
					xgradient /= 3;
					ygradient = src[i + 1][j - 1] - src[i - 1][j - 1] + src[i + 1][j] - src[i - 1][j]
							+ src[i + 1][j + 1] - src[i - 1][j + 1];
					ygradient /= 3;

					ArrayList gradient_list = new ArrayList();
					gradient_list.add(xgradient);
					gradient_list.add(ygradient);
					dst[i][j] = gradient_list;
				} else if (type == 6)
				{
					xgradient = Double.NaN;
					ygradient = src[i + 1][j - 1] - src[i - 1][j - 1] + src[i + 1][j] - src[i - 1][j];
					ygradient /= 2;

					ArrayList gradient_list = new ArrayList();
					gradient_list.add(xgradient);
					gradient_list.add(ygradient);
					dst[i][j] = gradient_list;
				} else if (type == 7)
				{
					xgradient = Double.NaN;
					ygradient = Double.NaN;

					ArrayList gradient_list = new ArrayList();
					gradient_list.add(xgradient);
					gradient_list.add(ygradient);
					dst[i][j] = gradient_list;
				} else if (type == 8)
				{
					xgradient = (src[i - 1][j + 1] - src[i - 1][j - 1]) + (src[i][j + 1] - src[i][j - 1]);
					xgradient /= 2;
					ygradient = Double.NaN;

					ArrayList gradient_list = new ArrayList();
					gradient_list.add(xgradient);
					gradient_list.add(ygradient);
					dst[i][j] = gradient_list;
				} else if (type == 9)
				{
					xgradient = Double.NaN;
					ygradient = Double.NaN;

					ArrayList gradient_list = new ArrayList();
					gradient_list.add(xgradient);
					gradient_list.add(ygradient);
					dst[i][j] = gradient_list;
				}
			}
		}
		return (dst);
	}
    
	public static ArrayList[][] getSmoothGradient(int src[][])
	{
		int ydim = src.length;
		int xdim = src[0].length;
		ArrayList[][] dst = new ArrayList[ydim][xdim];

		for (int i = 0; i < ydim; i++)
		{
			for (int j = 0; j < xdim; j++)
			{
				int type = getLocationType(j, i, xdim, ydim);
				double xgradient = 0;
				double ygradient = 0;
				if (type == 1)
				{
					xgradient = Double.NaN;
					ygradient = Double.NaN;

					ArrayList gradient_list = new ArrayList();
					gradient_list.add(xgradient);
					gradient_list.add(ygradient);
					dst[i][j] = gradient_list;
				} 
				else if (type == 2)
				{
					xgradient = (src[i][j + 1] - src[i][j - 1]) + (src[i + 1][j + 1] - src[i + 1][j - 1]);
					xgradient /= 2;
					ygradient = Double.NaN;

					ArrayList gradient_list = new ArrayList();
					gradient_list.add(xgradient);
					gradient_list.add(ygradient);
					dst[i][j] = gradient_list;
				} 
				else if (type == 3)
				{
					xgradient = Double.NaN;
					ygradient = Double.NaN;

					ArrayList gradient_list = new ArrayList();
					gradient_list.add(xgradient);
					gradient_list.add(ygradient);
					dst[i][j] = gradient_list;
				} 
				else if (type == 4)
				{
					xgradient = Double.NaN;
					ygradient = (src[i + 1][j] - src[i - 1][j]) + (src[i + 1][j + 1] - src[i - 1][j + 1]);
					ygradient /= 2;

					ArrayList gradient_list = new ArrayList();
					gradient_list.add(xgradient);
					gradient_list.add(ygradient);
					dst[i][j] = gradient_list;
				} 
				else if (type == 5)
				{
					if(((src[i][j + 1] < src[i][j]) && (src[i][j] < src[i][j - 1]))  ||
					   ((src[i][j + 1] > src[i][j]) && (src[i][j] > src[i][j - 1])))	
					{
					    xgradient = src[i - 1][j + 1] - src[i - 1][j - 1] + 
							        src[i][j + 1] - src[i][j - 1] +
							        src[i + 1][j + 1] - src[i + 1][j - 1];
					    xgradient /= 3;
					    //System.out.println("Got here.");
					}
					else
					{
						xgradient = Double.NaN;	
					}
					
					
					if(((src[i + 1][j] < src[i][j]) && (src[i][j] < src[i - 1][j]))  ||
							   ((src[i + 1][j] > src[i][j]) && (src[i][j] > src[i - 1][j])))		
					{
					    ygradient = src[i + 1][j - 1] - src[i - 1][j - 1] + 
							    	src[i + 1][j] - src[i - 1][j] +
							    	src[i + 1][j + 1] - src[i - 1][j + 1];
					    ygradient /= 3;
					}
					else	
				        ygradient = Double.NaN;	

					
					ArrayList gradient_list = new ArrayList();
					gradient_list.add(xgradient);
					gradient_list.add(ygradient);
					dst[i][j] = gradient_list;
				} 
				else if (type == 6)
				{
					xgradient = Double.NaN;
					ygradient = src[i + 1][j - 1] - src[i - 1][j - 1] + src[i + 1][j] - src[i - 1][j];
					ygradient /= 2;

					ArrayList gradient_list = new ArrayList();
					gradient_list.add(xgradient);
					gradient_list.add(ygradient);
					dst[i][j] = gradient_list;
				} 
				else if (type == 7)
				{
					xgradient = Double.NaN;
					ygradient = Double.NaN;

					ArrayList gradient_list = new ArrayList();
					gradient_list.add(xgradient);
					gradient_list.add(ygradient);
					dst[i][j] = gradient_list;
				} 
				else if (type == 8)
				{
					xgradient = (src[i - 1][j + 1] - src[i - 1][j - 1]) + (src[i][j + 1] - src[i][j - 1]);
					xgradient /= 2;
					ygradient = Double.NaN;

					ArrayList gradient_list = new ArrayList();
					gradient_list.add(xgradient);
					gradient_list.add(ygradient);
					dst[i][j] = gradient_list;
				} 
				else if (type == 9)
				{
					xgradient = Double.NaN;
					ygradient = Double.NaN;

					ArrayList gradient_list = new ArrayList();
					gradient_list.add(xgradient);
					gradient_list.add(ygradient);
					dst[i][j] = gradient_list;
				}
			}
		}
		return (dst);
	}
	
	public static int[][] getVariance(int src[][])
	{
		int ydim = src.length;
		int xdim = src[0].length;
		int[][] dst = new int[ydim][xdim];

		for (int i = 0; i < ydim; i++)
		{
			for (int j = 0; j < xdim; j++)
			{
				int type = getLocationType(j, i, xdim, ydim);
				int variance = 0;
				if (type == 1)
				{
					variance += Math.abs(src[i][j] - src[i][j + 1]);
					variance += Math.abs(src[i][j] - src[i + 1][j]);
					variance += Math.abs(src[i][j] - src[i + 1][j + 1]);
					dst[i][j] = variance;
				} else if (type == 2)
				{
					variance += Math.abs(src[i][j] - src[i][j - 1]);
					variance += Math.abs(src[i][j] - src[i][j + 1]);
					variance += Math.abs(src[i][j] - src[i + 1][j - 1]);
					variance += Math.abs(src[i][j] - src[i + 1][j]);
					variance += Math.abs(src[i][j] - src[i + 1][j + 1]);
					dst[i][j] = variance;
				} else if (type == 3)
				{
					variance += Math.abs(src[i][j] - src[i][j - 1]);
					variance += Math.abs(src[i][j] - src[i + 1][j]);
					variance += Math.abs(src[i][j] - src[i + 1][j - 1]);
					dst[i][j] = variance;
				} else if (type == 4)
				{
					variance += Math.abs(src[i][j] - src[i - 1][j]);
					variance += Math.abs(src[i][j] - src[i - 1][j + 1]);
					variance += Math.abs(src[i][j] - src[i][j + 1]);
					variance += Math.abs(src[i][j] - src[i + 1][j]);
					variance += Math.abs(src[i][j] - src[i + 1][j + 1]);
					dst[i][j] = variance;
				} else if (type == 5)
				{
					variance += Math.abs(src[i][j] - src[i - 1][j - 1]);
					variance += Math.abs(src[i][j] - src[i - 1][j]);
					variance += Math.abs(src[i][j] - src[i - 1][j + 1]);
					variance += Math.abs(src[i][j] - src[i][j - 1]);
					variance += Math.abs(src[i][j] - src[i][j + 1]);
					variance += Math.abs(src[i][j] - src[i + 1][j - 1]);
					variance += Math.abs(src[i][j] - src[i + 1][j]);
					variance += Math.abs(src[i][j] - src[i + 1][j + 1]);
					dst[i][j] = variance;
				} else if (type == 6)
				{
					variance += Math.abs(src[i][j] - src[i - 1][j]);
					variance += Math.abs(src[i][j] - src[i - 1][j - 1]);
					variance += Math.abs(src[i][j] - src[i][j - 1]);
					variance += Math.abs(src[i][j] - src[i + 1][j]);
					variance += Math.abs(src[i][j] - src[i + 1][j - 1]);
					dst[i][j] = variance;
				} else if (type == 7)
				{
					variance += Math.abs(src[i][j] - src[i - 1][j]);
					variance += Math.abs(src[i][j] - src[i - 1][j + 1]);
					variance += Math.abs(src[i][j] - src[i][j + 1]);
					dst[i][j] = variance;
				} else if (type == 8)
				{
					variance += Math.abs(src[i][j] - src[i - 1][j - 1]);
					variance += Math.abs(src[i][j] - src[i - 1][j]);
					variance += Math.abs(src[i][j] - src[i - 1][j + 1]);
					variance += Math.abs(src[i][j] - src[i][j - 1]);
					variance += Math.abs(src[i][j] - src[i][j + 1]);
					dst[i][j] = variance;
				} else if (type == 9)
				{
					variance += Math.abs(src[i][j] - src[i - 1][j - 1]);
					variance += Math.abs(src[i][j] - src[i - 1][j]);
					variance += Math.abs(src[i][j] - src[i][j - 1]);
					dst[i][j] = variance;
				}
			}
		}
		return (dst);
	}

	public static int[][] extract(int[][] source, int xoffset, int yoffset, int xlength, int ylength)
	{
	    int src_ydim = source.length;
	    int src_xdim = source[0].length;
	    
	    int [][] dest = new int[ylength][xlength];
	    
	    for(int i = 0; i < ylength; i++)
	    {
	    	for(int j = 0; j < xlength; j++)
	    	{
	    	    dest[i][j] = source[i + yoffset][j + xoffset];	
	    	}
	    }
	    return(dest); 
	}
	
	public static int[][] shift(int[][] source, int x, int y)
	{
		int ydim = source.length;
		int xdim = source[0].length;
		
		int[][] dest = new int[ydim][xdim];
		
        if(x == 0 && y == 0)
        {
            for(int i = 0; i < ydim; i++)
            {
            	for(int j = 0; j < xdim; j++)
            	{
            		dest[i][j] = source[i][j];
            	}
            }
            return(dest);
        }
        else if(x == 0)
        {
        	int ydelta = Math.abs(y);
        	dest = new int[ydim - ydelta][xdim];
        	int k = 0;
        	if(y > 0)
        		k = y;
        	
        	for(int i = 0; i < ydim - ydelta; i++)
            {
            	for(int j = 0; j < xdim; j++)
            	{
            		dest[i][j] = source[i + k][j];
            	}
            }
        	return(dest);
        }
        else if(y == 0)
        {
        	int xdelta = Math.abs(x);
        	dest = new int[ydim][xdim - xdelta];
        	int k = 0;
        	if(x > 0)
        		k = x;
        	for(int i = 0; i < ydim; i++)
            {
            	for(int j = 0; j < xdim - xdelta; j++)
            	{
            		dest[i][j] = source[i][j + k];
            	}
            }
        	return(dest);
        }
        else  // x != 0 && y != 0
        {
        	
        	int xdelta = Math.abs(x);
    		int ydelta = Math.abs(y); 
    		
    		dest = new int[ydim - ydelta][xdim - xdelta];
    		int k = 0;
    		if(y > 0)
    			k = y;
    		int m = 0;
    		if(x > 0)
    			m = x;
    		for(int i = 0; i < ydim - ydelta; i++)
            {
            	for(int j = 0; j < xdim - xdelta; j++)
            	{
            		dest[i][j] = source[i + k][j + m];
            	}
            }
        	return(dest);
        }
	}
	
	public static int[][] contract(int[][] source)
	{
		int ydim = source.length;
		int xdim = source[0].length;
		
		int[][] dest = new int[ydim - 1][xdim - 1];
		
	    for(int i = 0; i < ydim - 1; i++)
		{
			for(int j = 0; j < xdim - 1; j++)
			{
				double a = (double) source[i][j] * .5 + (double) source[i][j + 1] * .5;
				double b = (double) source[i + 1][j] * .5  + (double) source[i + 1][j + 1] * .5;
				dest[i][j] = (int) ((a + b) * .25 + .5);
			}
		}
		return(dest);
	}

	
	// x and y should be some number from 1 to -1
	public static int[][] translate(int[][] source, double x, double y)
	{
		int ydim = source.length;
		int xdim = source[0].length;
		int[][] dest = new int[ydim - 1][xdim - 1];
		
		x += 1.;
		x *= .5;
		y += 1.;
		y *= .5;
		
		for (int i = 0; i < ydim - 1; i++)
		{
			for (int j = 0; j < xdim - 1; j++)
			{
				double a = (double) source[i][j] * (1. - x) + (double) source[i][j + 1] * x;
				double b = (double) source[i + 1][j] * (1. - x) + (double) source[i + 1][j + 1] * x;
				dest[i][j] = (int) ((a * (1. - y) + b * y) * .5 + .5);
			}
		}
		return(dest);
	}
	
	public static double[] getTranslation(int[][] source1, int[][] source2)
	{
		//Assumes source1 and source2 are same size.
		int src_ydim = source1.length;
		int src_xdim = source1[0].length;
		int[][] estimate = new int[src_ydim][src_xdim];

		
		for (int i = 0; i < src_ydim; i++)
		{
			for (int j = 0; j < src_xdim; j++)
			{
				estimate[i][j] = source2[i][j];
			}
		}

		double[] dest = new double[3];

		double w = 0;
		double x = 0;
		double z = 0;
		double b1 = 0;
		double b2 = 0;

		ArrayList[][] gradient = getSmoothGradient(estimate);
		for (int i = 1; i < src_ydim - 1; i++)
		{
			for (int j = 1; j < src_xdim - 1; j++)
			{
				ArrayList current_gradient = gradient[i][j];
				double xgradient = (double) current_gradient.get(0);
				double ygradient = (double) current_gradient.get(1);
				boolean xgradient_isNaN = Double.isNaN(xgradient);
				boolean ygradient_isNaN = Double.isNaN(ygradient);
				if(!xgradient_isNaN && !ygradient_isNaN)
				{
					if(Double.isNaN(xgradient))
						System.out.println("Xgradient is not a number.");
					if(Double.isNaN(xgradient))
						System.out.println("Ygradient is not a number.");
				    double xx = xgradient * xgradient;
				    double xy = xgradient * ygradient;
				    double yy = ygradient * ygradient;
				    double delta = source1[i][j] - estimate[i][j];
				    double xdelta = xgradient * delta;
				    double ydelta = ygradient * delta;

				    w += xx;
				    x += xy;
				    z += yy;
				    b1 += xdelta;
				    b2 += ydelta;
				}
			}
		}
		
		System.out.println("w = " + w);
		System.out.println("x = " + x);
		System.out.println("z = " + z);
		System.out.println("b1 = " + b1);
		System.out.println("b2 = " + b2);
		double xincrement = (b1 - x * b2 / z) / (w - x * x / z);
		double yincrement = (b2 - x * b1 / w) / (z - x * x / w);
		
		if (xincrement == 0. && yincrement == 0.)
		{
			//System.out.println("Produced zero increments.");
			dest[0] = 0;
			dest[1] = 0;
			dest[2] = 0;
			return (dest);
		}

		double increment_min = 0.;
		if(Math.abs(xincrement) > Math.abs(yincrement))
			increment_min = Math.abs(xincrement) / 100.;
		else
			increment_min = Math.abs(yincrement) / 100.;
		
		double previous_xincrement = xincrement;
		double previous_yincrement = yincrement;
		double xtranslation        = xincrement;
		double ytranslation        = yincrement;

		int xshift   = (int) (xtranslation);
		double xdiff = xtranslation - xshift;
		
		int yshift   = (int) (ytranslation);
		double ydiff = ytranslation - yshift;
		
		String string = String.format("%,.4f", xincrement);
		System.out.println("First x increment is " + string);
		string = String.format("%,.4f", yincrement);
		System.out.println("First y increment is " + string);
		System.out.println();
		
		if(Math.abs(xdiff) > .5)
		{
			if(xdiff < 0)
			{
			    xshift--;
			    xdiff += .5;
			    xdiff = -xdiff;
			}
		    else if(xdiff > 0)
		    {
			    xshift++;	
			    xdiff -= .5;
			    xdiff = -xdiff;
		    }
		}
		
		if(Math.abs(ydiff) > .5)
		{
			if(ydiff < 0)
			{
			    yshift--;
			    ydiff += .5;
			    ydiff = -ydiff;
			}
		    else if(ydiff > 0)
		    {
			    yshift++;	
			    ydiff -= .5;
			    ydiff = -ydiff;
		    }
		}
		
		// We only use the original source in the first iteration.
		// After that we need to extract and contract a region of interest
		// so that it registers with the estimate for our sub-pixel calculation.
		int [][] current_source;
		
		if (xshift != 0 || yshift != 0)
		{
			int [][] intermediate = shift(source1, -xshift, -yshift);
			//We do this to align the source and estimate. 
			current_source = translate(intermediate, 0, 0);	
			//current_source = contract(intermediate);
			intermediate   = shift(source2, xshift, yshift);
			estimate       = translate(intermediate, 2 * xdiff, 2 * ydiff);  
			
		}
		else
		{
			estimate = translate(source2, 2 * xdiff, 2 * ydiff);  
			current_source = translate(source1, 0, 0);
			//current_source = contract(source1);
		}

		int current = 1;
		int maximum = 5;

		while (current < maximum)
		{
			w            = 0;
			x            = 0;
			z            = 0;

			b1           = 0;
            b2           = 0;
        
			gradient     = getSmoothGradient(estimate);
			int est_ydim = estimate.length;
			int est_xdim = estimate[0].length;
			
			
			for (int i = 1; i < est_ydim - 1; i++)
			{
				for (int j = 1; j < est_xdim - 1; j++)
				{
					ArrayList current_gradient = gradient[i][j];
					double xgradient = (double) current_gradient.get(0);
					double ygradient = (double) current_gradient.get(1);
					boolean xgradient_isNaN = Double.isNaN(xgradient);
					boolean ygradient_isNaN = Double.isNaN(ygradient);
					if(!xgradient_isNaN && !ygradient_isNaN)
					{
					    double xx = xgradient * xgradient;
					    double xy = xgradient * ygradient;
					    double yy = ygradient * ygradient;
					    double delta = current_source[i][j] - estimate[i][j];
					    double xdelta = xgradient * delta;
					    double ydelta = ygradient * delta;

					    w += xx;
					    x += xy;
					    z += yy;
					    b1 += xdelta;
					    b2 += ydelta;
					}
				}
			}
			
			xincrement = (b1 - x * b2 / z) / (w - x * x / z);
			yincrement = (b2 - x * b1 / w) / (z - x * x / w);
			
			if(Math.abs(xincrement) < increment_min || Math.abs(yincrement) < increment_min)
			{
				//System.out.println("Produced an increment smaller than minimum.");
				//Converging.
				dest[0] = 1;
			    dest[1] = xtranslation;
		        dest[2] = ytranslation;
				return (dest);
			}
			
			if((xincrement < 0 && previous_xincrement > 0) || (xincrement > 0 && previous_xincrement < 0) 
				|| (yincrement < 0 && previous_yincrement > 0) || (yincrement > 0 && previous_yincrement < 0))
			{
				//System.out.println("Produced an increment in the opposite direction.");
				//Oscillating.
				xtranslation += xincrement;
				ytranslation += yincrement;
				
				dest[0] = 2;
				dest[1] = xtranslation;
		        dest[2] = ytranslation;
				return (dest);
			}
				
			xtranslation += xincrement;
		    previous_xincrement = xincrement;
			
			ytranslation += yincrement;
			previous_yincrement = yincrement;
			
			string = String.format("%,.4f", xincrement);
			System.out.println("Current x increment is " + string);
			string = String.format("%,.4f", yincrement);
			System.out.println("Current y increment is " + string);
			System.out.println();
			
			string = String.format("%,.4f", xtranslation);
			System.out.println("Current x estimate is " + string);
			string = String.format("%,.4f", ytranslation);
			System.out.println("Current y estimate is " + string);
			System.out.println();
			
		    // Create a new translated image for the next iteration.
			xshift = (int) xtranslation;
			yshift = (int) ytranslation;
			xdiff = xtranslation - xshift;
			ydiff = ytranslation - yshift;
			
            //Check to see if our translation passes pixel boundries
			//so we need to adjust our shift and diff values.
			if(Math.abs(xdiff) > .5)
			{
				if(xdiff < 0)
				{
				    xshift--;
				    xdiff += .5;
				    xdiff = -xdiff;
				}
			    else if(xdiff > 0)
			    {
				    xshift++;	
				    xdiff -= .5;
				    xdiff = -xdiff;
			    }
			}
			
			if(Math.abs(ydiff) > .5)
			{
				if(ydiff < 0)
				{
				    yshift--;
				    ydiff += .5;
				    ydiff = -ydiff;
				}
			    else if(ydiff > 0)
			    {
				    yshift++;	
				    ydiff -= .5;
				    ydiff = -ydiff;
			    }
			}
			
			if (xshift != 0 || yshift != 0)
			{
				int [][] intermediate = shift(source2, xshift, yshift);
				estimate = translate(intermediate, 2 * xdiff, 2 * ydiff);
				
				intermediate = shift(source1, -xshift, -yshift);
				current_source = translate(intermediate, 0, 0);
				//current_source = contract(intermediate);
			}
			else
			{
				estimate = translate(source2, 2 * xdiff, 2 * ydiff);
				current_source = translate(source1, 0, 0);
				//current_source = contract(source1);
			}
			current++;
		}
		
		//System.out.println("Reached maximum iterations.");
		dest[0] = 3;
        dest[1] = xtranslation;
        dest[2] = ytranslation;
		return (dest);
	}
	

	public ArrayList adaptive_interpolate(double[] x, double[] y, double[] z, double distance)
	{
		// We'll get three lists of original data with new interpolated values included.
		ArrayList interpolated_data = new ArrayList();
		
		// Use z as the discriminant, and process x and y accordingly.
		double[] src   = z;
		
		
		ArrayList x_dst = new ArrayList();
		ArrayList y_dst = new ArrayList();
		ArrayList z_dst = new ArrayList();
		
		
		for(int i = 0; i < src.length; i++)
		{
		    x_dst.add(x[i]);
		    y_dst.add(y[i]);
		    z_dst.add(z[i]);
		    
		    // If that was the last point, don't
		    // bother interpolating.
		    if(i != src.length - 1)
		    {
		    	// The range of the jitter is about 6 nT,
		    	// so we don't want to interpolate when points
		    	// are that close because it will affect
		    	// the result of the averaging we do later.
		    	// We'll try to interpolate a point at least every
		    	// 12 nT, so the gap we're looking for is
		    	// at least 24 nT.
		    	// That's being conservative, although it would
		    	// be even more conservative to only interpolate
		    	// one point.
		    	
		    	double discriminant = Math.abs(src[i + 1] - src[i]);
		    	if(discriminant > distance)
				{
		    		// This does something crazy.
		    		/*
		    		// Find out how large the gap is, and if we
		    		// want to interpolate multiple values.
		    		int number_of_points = (int)Math.floor(discriminant / distance);
		    		// Take care of the simple case.
		    		if(number_of_points == 1)
		    		{
		    		    x_dst.add((x[i] + x[i + 1]) / 2);
		    		    y_dst.add((y[i] + y[i + 1]) / 2);
		    		    z_dst.add((z[i] + z[i + 1]) / 2);
		    		}
		    		else
		    		{
		    			double increment = 1. / (number_of_points + 1);
		    			double factor    = increment;
		    		    for(int j = 0; j < number_of_points; j++)
		    		    {
		    		        double first_value  = (1 - factor) * x[i + 1];		
		    		        double second_value = factor * x[i];
		    		        x_dst.add((first_value + second_value) / 2);
		    		        
		    		        first_value  = (1 - factor) * y[i + 1];
		    		        second_value = factor * y[i];
		    		        y_dst.add((first_value + second_value) / 2);
		    		        
		    		        first_value  = (1 - factor) * z[i + 1];
		    		        second_value = factor * z[i];
		    		        z_dst.add((first_value + second_value) / 2);
		    		        
		    		        factor += increment;
		    		    }
		    		}
		    		*/
		    		
		    		// Try simple.
		    		x_dst.add((x[i] + x[i + 1]) / 2);
	    		    y_dst.add((y[i] + y[i + 1]) / 2);
	    		    z_dst.add((z[i] + z[i + 1]) / 2);
				}
		    }
		}
		
		interpolated_data.add(x_dst);
		interpolated_data.add(y_dst);
		interpolated_data.add(z_dst);
		
		return(interpolated_data);
	}
	
	
	public ArrayList adaptive_smooth(double[] x, double[] y, double[] z, int iterations, double amount)
	{
		ArrayList smooth_data = new ArrayList();
		
		// Use z as the discriminant, and process x and y accordingly.
		double[] src   = z;
		int dst_length = z.length - 1;
		int final_length = z.length - iterations;
		double[] x_dst = new double[dst_length];
		double[] y_dst = new double[dst_length];
		double[] z_dst = new double[dst_length];
		while (dst_length >= final_length)
		{
			for (int i = 0; i < dst_length; i++)
			{
				if(Math.abs(src[i + 1] - src[i]) > amount)
				{
				    x_dst[i] = (x[i] + x[i + 1]) / 2;
				    y_dst[i] = (y[i] + y[i + 1]) / 2;
				    z_dst[i] = (z[i] + z[i + 1]) / 2;
				}
				else
				{
				    x_dst[i] = x[i];
				    y_dst[i] = y[i];
				    z_dst[i] = z[i];
				}
			}
			src = z_dst;
			x   = x_dst;
			y   = y_dst;
			z   = z_dst;
			dst_length--;
			if(dst_length >= final_length)
			{
			    x_dst = new double[dst_length];
			    y_dst = new double[dst_length];
				z_dst = new double[dst_length];
			}
			else
			{
			    smooth_data.add(x);
			    smooth_data.add(y);
			    smooth_data.add(z);
			}
		}
		return(smooth_data);
	}
	
	
}