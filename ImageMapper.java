import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.awt.Point;

public class ImageMapper
{
	public static void smoothImage(int src[], int xdim, int ydim, double smooth_factor, int number_of_iterations,
			int dst[])
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
		while (number_of_uninterpolated_cells != 0)
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

	public static void avgAreaXTransform(int src[], int xdim, int ydim, int dst[], int new_xdim, int start_fraction[],
			int end_fraction[], int number_of_pixels[])
	{
		int i, j, k, x, y;
		int weight, current_whole_number, previous_whole_number;
		int total, factor;
		double real_position, differential, previous_position;

		differential = (double) xdim / (double) new_xdim;
		weight = (int) (differential * xdim);
		weight *= 1000;
		factor = 1000 * xdim;

		real_position = 0.;
		current_whole_number = 0;
		for (i = 0; i < new_xdim; i++)
		{
			previous_position = real_position;
			previous_whole_number = current_whole_number;
			real_position += differential;
			current_whole_number = (int) (real_position);
			number_of_pixels[i] = current_whole_number - previous_whole_number;
			start_fraction[i] = (int) (1000. * (1. - (previous_position - (double) (previous_whole_number))));
			end_fraction[i] = (int) (1000. * (real_position - (double) (current_whole_number)));
		}

		for (y = 0; y < ydim; y++)
		{
			i = y * new_xdim;
			j = y * xdim;
			for (x = 0; x < new_xdim - 1; x++)
			{
				if (number_of_pixels[x] == 0)
				{
					dst[i] = src[j];
					i++;
				} else
				{
					total = start_fraction[x] * xdim * src[j];
					j++;
					k = number_of_pixels[x] - 1;
					while (k > 0)
					{
						total += factor * src[j];
						j++;
						k--;
					}
					total += end_fraction[x] * xdim * src[j];
					total /= weight;
					dst[i] = total;
					i++;
				}
			}
			if (number_of_pixels[x] == 0)
				dst[i] = src[j];
			else
			{
				total = start_fraction[x] * xdim * src[j];
				j++;
				k = number_of_pixels[x] - 1;
				while (k > 0)
				{
					total += factor * src[j];
					j++;
					k--;
				}
				total /= weight - end_fraction[x] * xdim;
				dst[i] = total;
			}
		}
	}

	public static void avgAreaYTransform(int src[], int xdim, int ydim, int dst[], int new_ydim, int start_fraction[],
			int end_fraction[], int number_of_pixels[])
	{
		int i, j, k, x, y;
		int weight, current_whole_number, previous_whole_number;
		int total, factor;
		double real_position, differential, previous_position;

		differential = (double) ydim / (double) new_ydim;
		weight = (int) (differential * ydim);
		weight *= 1000;
		factor = ydim * 1000;

		real_position = 0.;
		current_whole_number = 0;
		for (i = 0; i < new_ydim; i++)
		{
			previous_position = real_position;
			previous_whole_number = current_whole_number;
			real_position += differential;
			current_whole_number = (int) (real_position);
			number_of_pixels[i] = current_whole_number - previous_whole_number;
			start_fraction[i] = (int) (1000. * (1. - (previous_position - (double) (previous_whole_number))));
			end_fraction[i] = (int) (1000. * (real_position - (double) (current_whole_number)));
		}

		for (x = 0; x < xdim; x++)
		{
			i = j = x;
			for (y = 0; y < new_ydim - 1; y++)
			{
				if (number_of_pixels[y] == 0)
				{
					dst[i] = src[j];
					i += xdim;
				} else
				{
					total = start_fraction[y] * ydim * src[j];
					j += xdim;
					k = number_of_pixels[y] - 1;
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
			if (number_of_pixels[y] == 0)
				dst[i] = src[j];
			else
			{
				total = start_fraction[y] * ydim * src[j];
				j += xdim;
				k = number_of_pixels[y] - 1;
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
	}

	public void avgAreaTransform(int src[], int xdim, int ydim, int dst[], int new_xdim, int new_ydim, int workspace[],
			int start_fraction[], int end_fraction[], int number_of_pixels[])
	{
		avgAreaXTransform(src, xdim, ydim, workspace, new_xdim, start_fraction, end_fraction, number_of_pixels);
		avgAreaYTransform(workspace, new_xdim, ydim, dst, new_ydim, start_fraction, end_fraction, number_of_pixels);
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

	public static int[][] shift(int[][] source, int x, int y)
	{
		int ydim = source.length;
		int xdim = source[0].length;

		if (x > 0)
		{
			if (y > 0)
			{
				int[][] dest = new int[ydim - y][xdim - x];
				for (int i = y; i < ydim; i++)
				{
					for (int j = x; j < xdim; j++)
					{
						dest[i - y][j - x] = source[i][j];
					}
				}
				return (dest);

			} else if (y < 0)
			{
				int[][] dest = new int[ydim + y][xdim - x];
				for (int i = 0; i < ydim + y; i++)
				{
					for (int j = x; j < xdim; j++)
					{
						dest[i][j - x] = source[i - y][j];
					}
				}
				return (dest);
			} else
			{
				int[][] dest = new int[ydim][xdim - x];
				for (int i = 0; i < ydim; i++)
				{
					for (int j = x; j < xdim; j++)
					{
						dest[i][j - x] = source[i][j];
					}
				}

				return (dest);
			}
		} else if (x < 0)
		{
			if (y > 0)
			{
				int[][] dest = new int[ydim - y][xdim + x];
				for (int i = y; i < ydim; i++)
				{
					for (int j = 0; j < xdim + x; j++)
					{
						dest[i - y][j] = source[i][j - x];
					}
				}
				return (dest);

			} else if (y < 0)
			{
				int[][] dest = new int[ydim + y][xdim + x];
				for (int i = 0; i < ydim + y; i++)
				{
					for (int j = 0; j < xdim + x; j++)
					{
						dest[i][j] = source[i - y][j - x];
					}
				}
				return (dest);
			} else
			{
				int[][] dest = new int[ydim][xdim - x];
				for (int i = 0; i < ydim; i++)
				{
					for (int j = 0; j < xdim + x; j++)
					{
						dest[i][j] = source[i][j - x];
					}
				}

				return (dest);
			}
		} else // x == 0
		{
			if (y > 0)
			{
				int[][] dest = new int[ydim - y][xdim];
				for (int i = y; i < ydim; i++)
				{
					for (int j = 0; i < xdim; i++)
					{
						dest[i - y][j] = source[i][j];
					}
				}
				return (dest);
			} else if (y < 0)
			{
				int[][] dest = new int[ydim + y][xdim];
				for (int i = 0; i < ydim + y; i++)
				{
					for (int j = 0; i < xdim; i++)
					{
						dest[i][j] = source[i - y][j];
					}
				}
				return (dest);
			} else
			{
				int[][] dest = new int[ydim][xdim];
				for (int i = 0; i < ydim; i++)
				{
					for (int j = 0; i < xdim; i++)
					{
						dest[i][j] = source[i][j];
					}
				}
				return (dest);
			}
		}
	}

	public static int[][] translate(int[][] source, double x, double y)
	{
		int ydim = source.length;
		int xdim = source[0].length;
		int[][] dest = new int[ydim][xdim];

		if (x > 0)
		{
			if (y > 0)
			{
				// Change the dimensions depending on the arguments.
				dest = new int[ydim - 1][xdim - 1];
				for (int i = 0; i < ydim - 1; i++)
				{
					for (int j = 0; j < xdim - 1; j++)
					{
						double a = (double) source[i][j] * (1. - x) + (double) source[i][j + 1] * x;
						double b = (double) source[i + 1][j] * (1. - x) + (double) source[i + 1][j + 1] * x;
						dest[i][j] = (int) (a * (1. - y) + b * y * .25);
					}
				}
			} 
			else if (y < 0)
			{
				dest = new int[ydim - 1][xdim - 1];
				for (int i = 0; i < ydim - 1; i++)
				{
					for (int j = 0; j < xdim - 1; j++)
					{
						double a = (double) source[i][j] * (1. - x) + (double) source[i][j + 1] * x;
						double b = (double) source[i + 1][j] * (1. - x) + (double) source[i + 1][j + 1] * x;
						dest[i][j] = (int) (a * -y + b * (1. + y) * .25);
					}
				}
			} 
			else
			{
				dest = new int[ydim][xdim - 1];
				for (int i = 0; i < ydim; i++)
				{
					for (int j = 0; j < xdim - 1; j++)
					{
						double a = (double) source[i][j] * (1. - x) + (double) source[i][j + 1] * x;
						dest[i][j] = (int) (a * .5);
					}
				}
			}
		} 
		else if (x < 0)
		{
			if (y > 0)
			{
				dest = new int[ydim - 1][xdim - 1];
				for (int i = 0; i < ydim - 1; i++)
				{
					for (int j = 0; j < xdim - 1; j++)
					{
						double a = (double) source[i][j] * -x + (double) source[i][j + 1] * (1. - x);
						double b = (double) source[i + 1][j] * -x + (double) source[i + 1][j] * (1. - x);
						dest[i][j] = (int) (a * (1. - y) + b * y * .25);
					}
				}
			} 
			else if (y < 0)
			{
				dest = new int[ydim - 1][xdim - 1];
				for (int i = 0; i < ydim - 1; i++)
				{
					for (int j = 0; j < xdim - 1; j++)
					{
						double a = (double) source[i][j] * -x + (double) source[i][j + 1] * (1. + x);
						double b = (double) source[i + 1][j] * -x + (double) source[i + 1][j + 1] * (1. + x);
						dest[i][j] = (int) (a * -y + b * (1. + y) * .25);
					}
				}
			} 
			else
			{
				dest = new int[ydim][xdim - 1];
				for (int i = 0; i < ydim; i++)
				{
					for (int j = 0; j < xdim - 1; j++)
					{
						double a = (double) source[i][j] * -x + (double) source[i][j + 1] * (1. + x);
						dest[i][j] = (int) (a * .5);
					}
				}
			}
		} 
		else
		{
			if (y > 0)
			{
				dest = new int[ydim - 1][xdim];
				for (int i = 0; i < ydim - 1; i++)
				{
					for (int j = 0; j < xdim; j++)
					{
						double a = (double) source[i][j] * (1. - x) + (double) source[i + 1][j] * x;
						dest[i][j] = (int) (a * .5);
					}
				}
			} 
			else if (y < 0)
			{
				dest = new int[ydim + 1][xdim];
				for (int i = 0; i < ydim + y; i++)
				{
					for (int j = 0; j < xdim; j++)
					{
						double a = (double) source[i][j] * -x + (double) source[i + 1][j] * (1. + x);
						dest[i][j] = (int) (a * .5);
					}
				}
			} 
			else
			{
				dest = new int[ydim][xdim];
				for (int i = 0; i < ydim; i++)
				{
					for (int j = 0; j < xdim; j++)
					{
						dest[i][j] = source[i][j];
					}
				}
			}
		}
		return (dest);
	}

	public static double[] getTranslation(int[][] source1, int[][] source2)
	{
		int ydim = source2.length;
		int xdim = source2[0].length;
		int[][] estimate = new int[xdim][ydim];

		for (int i = 0; i < ydim; i++)
		{
			for (int j = 0; j < xdim; j++)
			{
				estimate[i][j] = source2[i][j];
			}
		}

		double[] dest = new double[2];

		double w = 0;
		double x = 0;
		double z = 0;
		double a1 = 0;
		double a2 = 0;
		double b1 = 0;
		double b2 = 0;

		ArrayList[][] gradient = getGradient(estimate);
		for (int i = 1; i < ydim - 1; i++)
		{
			for (int j = 1; j < xdim - 1; j++)
			{
				ArrayList current_gradient = gradient[i][j];
				double xgradient = (double) current_gradient.get(0);
				double ygradient = (double) current_gradient.get(1);
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
		double xincrement = (b1 - x * b2 / z) / (w - x * x / z);
		double yincrement = (b2 - x * b1 / w) / (w - x * x / z);
		
		String string = String.format("%,.4f", xincrement);
		System.out.println("First x estimate is " + string);
		string = String.format("%,.4f", yincrement);
		System.out.println("First y estimate is " + string);
		System.out.println();
		
		if (xincrement == 0. && yincrement == 0.)
		{
			dest[0] = 0;
			dest[1] = 0;
			return (dest);
		}

		double increment_min = 0.;
		if(Math.abs(xincrement) > Math.abs(yincrement))
			increment_min = Math.abs(xincrement) / 100.;
		else
			increment_min = Math.abs(yincrement) / 100.;
		
		double previous_xincrement = xincrement;
		double previous_yincrement = yincrement;

		if (xincrement < 0)
			a1 = -1. - xincrement;
		else if (xincrement > 0)
			a1 = 1. - xincrement;
		else
			a1 = 0.;
		if (yincrement < 0)
			a2 = -1. - yincrement;
		else if (xincrement > 0)
			a2 = 1. - yincrement;
		else
			a2 = 0.;

		int xshift = (int) Math.floor(a1);
		int yshift = (int) Math.floor(a2);
		double xdiff = a1 - xshift;
		double ydiff = a2 - yshift;

		if (xshift != 0 && yshift != 0)
		{
			if (xdiff != 0 && ydiff != 0)
			{
				int[][] intermediate = shift(source2, xshift, yshift);
				estimate = translate(intermediate, xdiff, ydiff);
			} 
			else
			{
				estimate = shift(source2, xshift, yshift);
			}
		} 
		else
		{
			estimate = translate(source2, xdiff, ydiff);
		}

		double current = 2.;
		double maximum = 5.;
		while (current < maximum)
		{
			gradient = getGradient(estimate);
			ydim = estimate.length;
			xdim = estimate.length;
			for (int i = 1; i < ydim - 1; i++)
			{
				for (int j = 1; j < xdim - 1; j++)
				{
					ArrayList current_gradient = gradient[i][j];
					double xgradient = (double) current_gradient.get(0);
					double ygradient = (double) current_gradient.get(1);
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
			xincrement = (b1 - x * b2 / z) / (w - x * x / z);
			yincrement = (b2 - x * b1 / w) / (w - x * x / z);

			string = String.format("%,.4f", xincrement);
			System.out.println("Current x estimate is " + string);
			string = String.format("%,.4f", yincrement);
			System.out.println("Current y estimate is " + string);
			System.out.println();
			
			
			// Estimate is still moving in same direction.
			if ((xincrement <= 0 && previous_xincrement <= 0) || (xincrement >= 0 && previous_xincrement >= 0))
			{
				if (a1 < 0.)
					a1 = -current - xincrement;
				else
					a1 = current - xincrement;

				previous_xincrement = xincrement;
			} 
			else // New estimate in opposite direction.
			{
				if (Math.abs(previous_xincrement) < Math.abs(xincrement))
				{
					// If new estimate takes us past our starting point, reset.
					a1 = 0.;
					xincrement = previous_xincrement;
				} 
				else // Apparently passed a local minimum.
				{
					if (a1 < 0.)
						a1 = -current - xincrement;
					else
						a1 = current - xincrement;

					previous_xincrement = xincrement;
				}
			}

			// Estimate is still moving in same direction.
			if ((yincrement <= 0 && previous_yincrement <= 0) || (yincrement >= 0 && previous_yincrement >= 0))
			{
				if (a2 < 0.)
					a2 = -current - yincrement;
				else
					a2 = current - yincrement;

				previous_yincrement = yincrement;
			} 
			else // New estimate in opposite direction.
			{
				if (Math.abs(previous_yincrement) < Math.abs(yincrement))
				{
					// If new estimate takes us past our starting point, reset.
					a2 = 0.;
					yincrement = previous_yincrement;
				} 
				else // Apparently passed a local minimum.
				{
					if (a2 < 0.)
						a2 = -current - yincrement;
					else
						a2 = current - yincrement;
					previous_yincrement = yincrement;
				}
			}

			a1 += xincrement;
			a2 += yincrement;

			// If both of the increments are much smaller than the largest one, stop.
			if (xincrement < increment_min && yincrement < increment_min)
				break;
			else
			{
				xshift = (int) Math.floor(a1);
				yshift = (int) Math.floor(a2);
				xdiff = a1 - xshift;
				ydiff = a2 - yshift;

				if (xshift != 0 && yshift != 0)
				{
					if (xdiff != 0 && ydiff != 0)
					{
						int[][] intermediate = shift(source2, xshift, yshift);
						estimate = translate(intermediate, xdiff, ydiff);
					} else
					{
						estimate = shift(source2, xshift, yshift);
					}
				} 
				else
				{
					estimate = translate(source2, xdiff, ydiff);
				}
				current += 1.;
			}
		}
        dest[0] = a1;
        dest[1] = a2;
		return (dest);
	}

}