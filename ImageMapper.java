import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.awt.Point;

public class ImageMapper
{
	public static void smoothImage(int src[], int xdim, int ydim, double smooth_factor, int number_of_iterations, int dst[])
    {
        double even[]    = new double[xdim * ydim];
        double odd[]     = new double[xdim * ydim];
        double weight[]  = new double[xdim * ydim];
        double product[] = new double[xdim * ydim];
        double current_src[];
        double current_dst[];
        double dx, dy, dxy, sum, factor; 
        double total_weights;
        int    index;
        int    i, j, k;
    
        factor = 1.0 / ( 2 * smooth_factor * smooth_factor);
        current_src = odd;
        current_dst = even;

        for(i = 0; i < xdim * ydim; i++)
            current_src[i] = current_dst[i] = (double)src[i];

        for(i = 0; i < number_of_iterations; i++)
        {
            if(i % 2 == 0)
            {
                current_src = even;
                current_dst = odd;
            }
            else
            {
                current_src = odd;
                current_dst = even;
            }

            for(j = 1; j < ydim - 1; j++)
            {
                index = j * xdim;
                for(k = 1; k < xdim - 1; k++)
                {
                    index++;
                    dx = (current_src[index - 1] - current_src[index + 1]) / 2.; 
                    dy = (current_src[index - xdim] - current_src[index + xdim]) / 2.;
                    dxy = dx * dx + dy * dy;
                    weight[index] = java.lang.Math.exp(-dxy * factor);
                    product[index] = weight[index] * current_src[index];
                }
            }

            for(j = 2; j < ydim - 2; j++)
            {
                index = j * xdim + 2;
                total_weights = weight[index - xdim - 1] + weight[index - xdim] + weight[index - xdim + 1]
                + weight[index - 1] + weight[index] + weight[index + 1]
                + weight[index + xdim - 1] + weight[index + xdim] + weight[index + xdim + 1];
                sum = product[index - xdim - 1]
                + product[index - xdim]
                + product[index - xdim + 1]
                + product[index - 1]
                + product[index]
                + product[index + 1]
                + product[index + xdim - 1]
                + product[index + xdim]
                + product[index + xdim + 1];

                for(k = 2; k < xdim - 2; k++)
                {
                    current_dst[index] = sum / total_weights;

                    total_weights += weight[index + xdim + 2] 
                    + weight[index + 2] 
		    + weight[index - xdim + 2]
                    - weight[index - xdim - 1] 
		    - weight[index - 1] 
		    - weight[index + xdim - 1];

                    sum += product[index - xdim + 2]
                    + product[index + 2]
                    + product[index + xdim + 2]
                    - product[index - xdim - 1]
                    - product[index - 1]
                    - product[index + xdim - 1];
                    index++;
                }
            }
        }
        for(i = 0; i < xdim * ydim; i++)
            dst[i] = (int)current_dst[i];
    }
    
	public static int getLocationType(int xindex, int yindex, int xdim, int ydim)
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
	
	//Seems like this should be based on a generic get neighbors function
	//but this offers a handle to customizing the way we collect variances.
	public static void getPixelVariance(int src[], int xdim, int ydim, int dst[])
	{	
		for(int i = 0; i < ydim; i++)
		{
			for(int j = 0; j < xdim; j++)
			{
				int location_type = getLocationType(j, i, xdim, ydim);
				int variance;
				int k;
				
				switch(location_type)
				{
				case 1: variance = 0;
				        variance += Math.abs(src[0] - src[1]);
				        variance += Math.abs(src[0] - src[xdim]);
				        variance += Math.abs(src[0] - src[xdim + 1]);
				        dst[0]    = variance;
				        break;
				        
				case 2: variance = 0;
				        variance += Math.abs(src[j] - src[j - 1]);
				        variance += Math.abs(src[j] - src[j + 1]);
				        variance += Math.abs(src[j] - src[j + xdim - 1]);
				        variance += Math.abs(src[j] - src[j + xdim]);
				        variance += Math.abs(src[j] - src[j + xdim + 1]);
				        dst[j]    = variance;
				        break;
				
				case 3: variance = 0;
				        variance += Math.abs(src[j] - src[j - 1]);
				        variance += Math.abs(src[j] - src[j + xdim - 1]);
				        variance += Math.abs(src[j] - src[j + xdim]);
				        dst[j] = variance;
				        break;
					
				case 4: k = i * xdim + j;
				        variance = 0;
				        variance += Math.abs(src[k] - src[k - xdim]);
				        variance += Math.abs(src[k] - src[k - xdim + 1]);
				        variance += Math.abs(src[k] - src[k + 1]);
				        variance += Math.abs(src[k] - src[k + xdim]);
				        variance += Math.abs(src[k] - src[k + xdim + 1]);
				        dst[k] = variance;
				        break;

				case 5: k = i * xdim + j;
				        variance = 0;
				        variance += Math.abs(src[k] - src[k - xdim - 1]);
				        variance += Math.abs(src[k] - src[k - xdim]);
				        variance += Math.abs(src[k] - src[k - xdim + 1]);
				        variance += Math.abs(src[k] - src[k - 1]);
				        variance += Math.abs(src[k] - src[k + 1]);
				        variance += Math.abs(src[k] - src[k + xdim - 1]);
				        variance += Math.abs(src[k] - src[k + xdim]);
				        variance += Math.abs(src[k] - src[k + xdim + 1]);
				        dst[k] = variance;
					    break;
				
				
				case 6: k = i * xdim + j;
				        variance = 0;
				        variance += Math.abs(src[k] - src[k - xdim]);
				        variance += Math.abs(src[k] - src[k - xdim - 1]);
				        variance += Math.abs(src[k] - src[k - 1]);
				        variance += Math.abs(src[k] - src[k + xdim]);
				        variance += Math.abs(src[k] - src[k + xdim - 1]);
				        dst[k]= variance;
				        break;
				
				case 7: k = i * xdim + j;
		                variance = 0;
				        variance += Math.abs(src[k] - src[k - xdim]);
				        variance += Math.abs(src[k] - src[k - xdim + 1]);
				        variance += Math.abs(src[k] - src[k + 1]);
			            dst[k] = variance;
				        break;
				        
				case 8: k = i * xdim + j;
		                variance = 0;
				        variance += Math.abs(src[k] - src[k - xdim - 1]);
				        variance += Math.abs(src[k] - src[k - xdim]);
				        variance += Math.abs(src[k] - src[k - xdim + 1]);
				        variance += Math.abs(src[k] - src[k - 1]);
				        variance += Math.abs(src[k] - src[k + 1]);
				        dst[k] = variance;
				        break;
				        
				case 9: k = i * xdim + j;
				        variance = 0;
				        variance += Math.abs(src[k] - src[k - xdim]);
		                variance += Math.abs(src[k] - src[k - xdim - 1]);
		                variance += Math.abs(src[k] - src[k - 1]);
		                dst[k] = variance;
				        break; 
				        
				default: System.out.println("Location type is " + location_type);
				}
			}
		}
	}
	
	public static void getImageDilation(double src[][], boolean isInterpolated[][], double dst[][])
	{
	    int ydim = src.length;
	    int xdim = src[0].length;
	    
	    double source[];
        double dest[];
    	double gray1[]                     = new double[xdim * ydim];
        double gray2[]                     = new double[xdim * ydim];
        boolean isAssigned[]               = new boolean[xdim * ydim];
        int number_of_uninterpolated_cells = 0;
        int number_of_iterations           = 0;
        for(int i = 0; i < ydim; i++)
        {
            for(int j= 0; j < xdim; j++)
            {
            	int k                = i * xdim + j;
                gray1[k]             = src[i][j];	
                isAssigned[k]        = isInterpolated[i][j];
                if(isAssigned[k] == false)
                	number_of_uninterpolated_cells++;	
            }
        }
        //System.out.println("Origninal number of uninterpolated_cells is " + number_of_uninterpolated_cells);
        boolean even = true;  // Keep track of which buffer is the source and which is the destination.
        while(number_of_uninterpolated_cells != 0)
        {
        	number_of_iterations++;
            if(even == true)
    	    {
    		    source = gray1;
    		    dest   = gray2;
    		    even   = false;
    	    }
    	    else
    	    {
    		    source = gray2;
    		    dest   = gray1;
    		    even   = true;
    	    }
    	    dilateImage(source, isAssigned, xdim, ydim, dest);
    	    number_of_uninterpolated_cells = 0;
    	    for(int i = 0; i < xdim * ydim; i++)
    	    {
    		    if(isAssigned[i] == false)
    			    number_of_uninterpolated_cells++;
    	    }
    	    //System.out.println("The number of uninterpolated cells after dilation was " + number_of_uninterpolated_cells);
        }
        
        if(even == true)
        {
        	int k = 0;
        	for(int i = 0; i < ydim; i++)
        	{
        		for(int j = 0; j < xdim; j++)
        		{
        			dst[i][j] = gray1[k++];
        		}
        	}   
        }
        else
        {
        	int k = 0;
        	for(int i = 0; i < ydim; i++)
        	{
        		for(int j = 0; j < xdim; j++)
        		{
        			dst[i][j] = gray2[k++];
        		}
        	}
        }
        System.out.println("The number of iterations was " + number_of_iterations);
	}
	
	
	// This function modifies values in isInterpolated and dst, and can be called multiple times
	// until all the values in isInterpolated are true.
	// Also, using single index into image to keep low level code simple--will have to reformat
	// data for processing--see getVarianceImage.
	public static void dilateImage(double src[], boolean isInterpolated[], int xdim, int ydim, double dst[])
	{	
		boolean wasInterpolated[] = new boolean[xdim * ydim];
		for(int i = 0; i < ydim; i++)
		{
			for(int j = 0; j < xdim; j++)
			{
				int k = i * xdim + j;
				if(isInterpolated[k])
				{
					dst[k] = src[k];
					wasInterpolated[k] = true;
				}
				else
				{
					double diagonal_weight     = 0.7071;  // Orthogonal weight is 1.
					double total_weight        = 0;
					double value               = 0.;
					int    number_of_neighbors = 0;
				    int location_type          = getLocationType(j, i, xdim, ydim);
				    switch(location_type)
				    {
				        case 1: //Orthogonal
				        	    if(isInterpolated[k + 1])     
				                {
				        	        number_of_neighbors++;
				        	        total_weight += 1.;
				        	        value += src[k + 1];
				                }
				        	    
				                if(isInterpolated[k + xdim])
				                {
				                	number_of_neighbors++;
				        	        total_weight += 1.;
				        	        value += src[k + xdim];    	
				                }
				                
				                //Diagonal
				                if(isInterpolated[k + xdim + 1]) 
				                {
				                	number_of_neighbors++;
				        	        total_weight += diagonal_weight;
				        	        value += diagonal_weight * src[k + xdim + 1];
				                }

				                break;
				
				        case 2: //Orthogonal
				        	    if(isInterpolated[k - 1])
				                {
				        	        number_of_neighbors++;
		        	                total_weight += 1.;
		        	                value        += src[k - 1];    
				                }
				             
				                if(isInterpolated[k + 1])
				                {
				                	number_of_neighbors++;
				                	total_weight += 1.;
				                	value        += src[k + 1];
				                }
				                
				                if(isInterpolated[k + xdim])
				                {
				                	number_of_neighbors++;
				                	total_weight += 1.;
				                	value        += src[k + xdim];   	
				                }
				             
				                //Diagonal
				                if(isInterpolated[k + xdim - 1])
				                {
				                	number_of_neighbors++;
				                	total_weight += diagonal_weight;
				                	value        += diagonal_weight * src[k + xdim - 1];	
				                }
				                
				                if(isInterpolated[k + xdim + 1])
				                {
				                	number_of_neighbors++;
				                	total_weight += diagonal_weight;
				                	value        += diagonal_weight * src[k + xdim +  1];	
				                }
				                
				                break;
				       
				        case 3: // Orthogonal
				        	    if(isInterpolated[k - 1])      
				                {
				        	        number_of_neighbors++;
        	                        total_weight += 1.;
        	                        value        += src[k - 1];    
				                }
				                
				                if(isInterpolated[k + xdim])
				                {
				                	number_of_neighbors++;
        	                        total_weight += 1.;
        	                        value        += src[k - 1];	
				                }
				                
				                // Diagonal
				                if(isInterpolated[k + xdim - 1]) 
				                {
				                	number_of_neighbors++;
				                	total_weight += diagonal_weight;
				                	value        += diagonal_weight * src[k + xdim - 1];
				                }
				                
				                break;
					
				        case 4: //Orthogonal
				        	    if(isInterpolated[k - xdim])
			                    {
			                	    number_of_neighbors++;
			                	    total_weight += 1.;
			                	    value        += src[k - xdim];   	
			                    }
				        	    if(isInterpolated[k + xdim])
			                    {
			                	    number_of_neighbors++;
			                	    total_weight += 1.;
			                	    value        += src[k + xdim];   	
			                    }
				        	    
				        	    if(isInterpolated[k + 1])
			                    {
			                	    number_of_neighbors++;
			                	    total_weight += 1.;
			                	    value        += src[k + 1];   	
			                    }
				        	    
				        	    //Diagonal 
				        	    if(isInterpolated[k - xdim + 1])
		                        {
		                	        number_of_neighbors++;
		                	        total_weight += diagonal_weight;
		                	        value        += diagonal_weight * src[k - xdim + 1];   	
		                        }
				        	    if(isInterpolated[k + xdim + 1])
		                        {
		                	        number_of_neighbors++;
		                	        total_weight += diagonal_weight;
		                	        value        += diagonal_weight * src[k + xdim + 1];   	
		                        }
				        	    
				                break;

				        case 5: //Orthogonal
				        	    if(isInterpolated[k - xdim])
		                        {
		                	        number_of_neighbors++;
		                	        total_weight += 1.;
		                	        value        += src[k - xdim];   	
		                        }
			        	        if(isInterpolated[k + xdim])
		                        {
		                	        number_of_neighbors++;
		                	        total_weight += 1.;
		                	        value        += src[k + xdim];   	
		                        }
			        	        
			        	        if(isInterpolated[k - 1])
		                        {
		                	        number_of_neighbors++;
		                	        total_weight += 1.;
		                	        value        += src[k - 1];   	
		                        }
			        	    
			        	        if(isInterpolated[k + 1])
		                        {
		                	        number_of_neighbors++;
		                	        total_weight += 1.;
		                	        value        += src[k + 1];   	
		                        }
			        	        
			        	        //Diagonal
			        	        if(isInterpolated[k - xdim - 1])
		                        {
		                	        number_of_neighbors++;
		                	        total_weight += diagonal_weight;
		                	        value        += diagonal_weight * src[k - xdim - 1];   	
		                        }
				        	    if(isInterpolated[k + xdim - 1])
		                        {
		                	        number_of_neighbors++;
		                	        total_weight += diagonal_weight;
		                	        value        += diagonal_weight * src[k + xdim - 1];   	
		                        }
			        	        
			        	        if(isInterpolated[k - xdim + 1])
		                        {
		                	        number_of_neighbors++;
		                	        total_weight += diagonal_weight;
		                	        value        += diagonal_weight * src[k - xdim + 1];   	
		                        }
				        	    if(isInterpolated[k + xdim + 1])
		                        {
		                	        number_of_neighbors++;
		                	        total_weight += diagonal_weight;
		                	        value        += diagonal_weight * src[k + xdim + 1];   	
		                        }
			        	   
					            break;
				
				        case 6: //Orthogonal
				        	    if(isInterpolated[k - xdim])
	                            {
	                	            number_of_neighbors++;
	                	            total_weight += 1.;
	                	            value        += src[k - xdim];   	
	                            }
		        	            if(isInterpolated[k + xdim])
	                            {
	                	            number_of_neighbors++;
	                	            total_weight += 1.;
	                	            value        += src[k + xdim];   	
	                            }
		        	        
		        	            if(isInterpolated[k - 1])
	                            {
	                	            number_of_neighbors++;
	                	            total_weight += 1.;
	                	            value        += src[k - 1];   	
	                            }
		        	            
		        	            //Diagonal
		        	            if(isInterpolated[k - xdim - 1])
		                        {
		                	        number_of_neighbors++;
		                	        total_weight += diagonal_weight;
		                	        value        += diagonal_weight * src[k - xdim - 1];   	
		                        }
		        	            
				        	    if(isInterpolated[k + xdim - 1])
		                        {
		                	        number_of_neighbors++;
		                	        total_weight += diagonal_weight;
		                	        value        += diagonal_weight * src[k + xdim - 1];   	
		                        }
				        	   
				                break;
				
				        case 7: //Orthogonal
				        	    if(isInterpolated[k - xdim])
	                            {
	                	            number_of_neighbors++;
	                	            total_weight += 1.;
	                	            value        += src[k - xdim];   	
	                            }
				        	    if(isInterpolated[k + 1])
	                            {
	                	            number_of_neighbors++;
	                	            total_weight += 1.;
	                	            value        += src[k + 1];   	
	                            }
				        	    
				        	    //Diagonal
				        	    if(isInterpolated[k - xdim + 1])
		                        {
		                	        number_of_neighbors++;
		                	        total_weight += diagonal_weight;
		                	        value        += diagonal_weight * src[k - xdim + 1];   	
		                        }
				
				                break;
				        
				        case 8: //Orthogonal
				        	    if(isInterpolated[k - xdim])
                                {
                	                number_of_neighbors++;
                	                total_weight += 1.;
                	                value        += src[k - xdim];   	
                                }
			        	        if(isInterpolated[k - 1])
                                {
                	                number_of_neighbors++;
                	                total_weight += 1.;
                	                value        += src[k - 1];   	
                                }
			        	        if(isInterpolated[k + 1])
                                {
                	                number_of_neighbors++;
                	                total_weight += 1.;
                	                value        += src[k + 1];   	
                                }
			        	        
			        	        //Diagonal
			        	        if(isInterpolated[k - xdim - 1])
		                        {
		                	        number_of_neighbors++;
		                	        total_weight += diagonal_weight;
		                	        value        += diagonal_weight * src[k - xdim - 1];   	
		                        }
			        	        if(isInterpolated[k - xdim + 1])
		                        {
		                	        number_of_neighbors++;
		                	        total_weight += diagonal_weight;
		                	        value        += diagonal_weight * src[k - xdim + 1];   	
		                        }
				                break;
				        
				        case 9: //Orthogonal
				        	    if(isInterpolated[k - xdim])
                                {
            	                    number_of_neighbors++;
            	                    total_weight += 1.;
            	                    value        += src[k - xdim];   	
                                }
				        	    if(isInterpolated[k - 1])
                                {
            	                    number_of_neighbors++;
            	                    total_weight += 1.;
            	                    value        += src[k - 1];   	
                                }
				        	    
				        	    //Diagonal
				        	    if(isInterpolated[k - xdim - 1])
		                        {
		                	        number_of_neighbors++;
		                	        total_weight += diagonal_weight;
		                	        value        += diagonal_weight * src[k - xdim - 1];   	
		                        }
				                break; 
				        
				       default: break;
				    }
				    
				    if(number_of_neighbors > 0)               // Found a neighbor this iteration, set value.
                    {
                	    value             /= total_weight;
                	    dst[k]             = (int) value;
                	    wasInterpolated[k] = true;
                	    //System.out.println("Number of neighbors was " + number_of_neighbors);
                    }
                    else
                    {
                	    dst[k] = 0;
                	    wasInterpolated[k] = false;
                	    //System.out.println("Found a cell with no neighbors.");
                	    //System.out.println("Set boolean to false.");
                	    // No neighbors, set value to zero.
                    }
				}
			}
		}
		for(int i = 0; i < xdim * ydim; i++)
		{
			isInterpolated[i] = wasInterpolated[i];
		}
	}
	
    public static void avgAreaXTransform(int src[], int xdim, int ydim, int dst[], int new_xdim, int start_fraction[], int end_fraction[], int number_of_pixels[])
    {
        int    i, j, k, x, y;
        int    weight, current_whole_number, previous_whole_number;
        int    total, factor;
        double real_position, differential, previous_position;
    
        differential = (double)xdim / (double)new_xdim;
        weight       = (int)(differential * xdim);
        weight       *= 1000;
        factor       = 1000 * xdim;

        real_position = 0.;
        current_whole_number = 0;
        for(i = 0; i < new_xdim; i++)
        {
            previous_position     = real_position;
            previous_whole_number = current_whole_number;
            real_position        += differential;
            current_whole_number  = (int)(real_position);
            number_of_pixels[i]   = current_whole_number - previous_whole_number;
            start_fraction[i]     = (int)(1000. * (1. - (previous_position - (double)(previous_whole_number)))); 
            end_fraction[i]       = (int)(1000. * (real_position - (double)(current_whole_number)));
        }

        for(y = 0; y < ydim; y++)
        {
            i = y * new_xdim;
            j = y * xdim;
            for(x = 0; x < new_xdim - 1; x++)
            {
                if(number_of_pixels[x] == 0)
                {
                    dst[i] = src[j];
                    i++;
                }
                else
                {
                    total = start_fraction[x] * xdim * src[j];
                    j++;
                    k = number_of_pixels[x] - 1;
                    while(k > 0)
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
            if(number_of_pixels[x] == 0)
                dst[i] = src[j];
            else
            {
                total = start_fraction[x] * xdim * src[j];
                j++;
                k = number_of_pixels[x] - 1;
                while(k > 0)
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

    public static void avgAreaYTransform(int src[], int xdim, int ydim, int dst[], int new_ydim, int start_fraction[], int end_fraction[], int number_of_pixels[])
    {
        int    i, j, k, x, y;
        int    weight, current_whole_number, previous_whole_number;
        int    total, factor;
        double real_position, differential, previous_position;
    
        differential = (double)ydim / (double)new_ydim;
        weight       = (int)(differential * ydim);
        weight       *= 1000;
        factor       = ydim * 1000;
        
        real_position = 0.;
        current_whole_number = 0;
        for(i = 0; i < new_ydim; i++)
        {
            previous_position     = real_position;
            previous_whole_number = current_whole_number;
            real_position        += differential;
            current_whole_number  = (int)(real_position);
            number_of_pixels[i]   = current_whole_number - previous_whole_number;
            start_fraction[i]     = (int) (1000. * (1. - (previous_position - (double)(previous_whole_number)))); 
            end_fraction[i]       = (int) (1000. * (real_position - (double)(current_whole_number)));
        }

        for(x = 0; x < xdim; x++)
        {
            i = j = x;
            for(y = 0; y < new_ydim - 1; y++)
            {
                if(number_of_pixels[y] == 0)
                {
                    dst[i] = src[j];
                    i += xdim;
                }
                else
                {
                    total    = start_fraction[y] * ydim * src[j];
                    j       += xdim;
                    k        = number_of_pixels[y] - 1;
                    while(k > 0)
                    {
                        total += factor * src[j];
                        j += xdim;
                        k--;
                    }
                    total   += end_fraction[y] * ydim * src[j];
                    total   /= weight;
                    dst[i]   = total;
                    i       += xdim;
                }
            }
            if(number_of_pixels[y] == 0)
                dst[i] = src[j];
            else
            {
                total    = start_fraction[y] * ydim * src[j];
                j       += xdim;
                k        = number_of_pixels[y] - 1;
                while(k > 0)
                {
                    total += factor * src[j];
                    j += xdim;
                    k--;
                }
                total /= weight - end_fraction[y] * ydim;
                dst[i]   = total;
            }
        }
    }

    public void avgAreaTransform(int src[], int xdim, int ydim, int dst[], int new_xdim, int new_ydim, int workspace[], int start_fraction[], int end_fraction[], int number_of_pixels[])
    {
        avgAreaXTransform(src, xdim, ydim, workspace, new_xdim, start_fraction, end_fraction, number_of_pixels);
        avgAreaYTransform(workspace, new_xdim, ydim, dst, new_ydim, start_fraction, end_fraction, number_of_pixels);
    }
    
   
    
    
    /*
    public int[][] translate(int[][] source, double x, double y)
    {
        int ydim = source.length;
        int xdim = source[0].length;
        
        int    xshift = (int)x;
        double xdiff  = x - xshift; 
        
        int    yshift = (int)y;
        double ydiff  = y - yshift;
        
        int[][] dest = new int[ydim][xdim];
        
        if(xshift != 0 && yshift != 0)
    	{
    		if(x > 0 && y > 0)
    		{
    			int i      = 0; 
    			int j      = xshift + yshift * xdim;
    			int xend   = xdim - xshift - 1;
    			int yend   = ydim - yshift - 1;
    			int addend = xdim - xend;
    			if(xdiff > 0 && ydiff > 0)
    			{
    				int    rowlength     = xdim;
    				double left_factor   = 1. - xdiff;
    				double right_factor  = xdiff;
    				double top_factor    = 1. - ydiff;
    				double bottom_factor = ydiff;
    				for(y = 0; y < yend; y++)
    				{
    					for(x = 0; x < xend; x++)
    					{
    						northwest = src[i];
    						northeast = src[i + 1];
    						southwest = src[i + rowlength];
    						southeast = src[i + rowlength + 1];
    						value     = top_factor * (left_factor * northwest +  right_factor * northeast) +
    									bottom_factor * (left_factor * southwest +  right_factor * southeast) + .5;
                            dst[j++]  = value;
        					i++;
    					}
    					dst[j++] = src[i++];
    					i += addend;
    					j += addend;
    				}
    				for(x = 0; x < xend + 1; x++)
    					dst[j++] = src[i++];
    			}
    			else
    			{
    				if(xdiff > 0)
    				{
    					left_factor  = 1. - xdiff;
    					right_factor = xdiff;
    					for(y = 0; y < yend + 1; y++)
    					{
    						for(x = 0; x < xend; x++)
    						{
    						    value  = (left_factor * src[i]) + (right_factor * src[i + 1]) + .5;
    						    dst[j++] = value;
    							i++;
    						}
    						dst[j++] = src[i++];
    						i += addend;
    						j += addend;
    					}
    				}
    				else
    				{
    					if(ydiff > 0)
    					{
    						top_factor    = 1. - ydiff;
    						bottom_factor = ydiff;
    						for(y = 0; y < yend; y++)
    						{
    							for(x = 0; x < xend + 1; x++)
    							{
    								value     = (top_factor * src[i]) + (bottom_factor  * src[i + rowlength]) + .5;
    						        dst[j++] = value;
    							    i++;
    							}
    							i += addend;
    							j += addend;
    						}
    						for(x = 0; x < xend + 1; x++)
    							dst[j++] = src[i++];
    					}
    					else
    					{
    						
    						//Do a shift with no interpolation. 
    						
    						addend--;
    						for(y = 0; y < yend + 1; y++)
    						{
    							for(x = 0; x < xend + 1; x++)
    								dst[j++] = src[i++];
    							i += addend;
    							j += addend;
    						}
    					}
    				}
    			}
    		}
    		else
    		{
    		    if(xtrans < 0 && ytrans < 0)
    			{
    				xstart = -xshift;
    				ystart = -yshift;
    			    i      = xstart - yshift * xdim; 
    			    j      = 0;
    			    xend   = xdim + xshift - 1;
    			    yend   = ydim + yshift - 1;
    			    addend = xdim - xend;
    				if(xdiff < 0 && ydiff < 0)
    				{
    				    rowlength     = xdim;
    				    left_factor   = -xdiff;
    				    right_factor  = 1. + xdiff;
    				    top_factor    = -ydiff;
    				    bottom_factor = 1. + ydiff;
    					for(x = 0; x < xend + 1; x++)
    						dst[j++] = src[i++];
    					i += addend - 1;
    					j += addend - 1;
    					for(y = 0; y < yend; y++)
    					{
    						dst[j++] = src[i++];
    						for(x = 0; x < xend; x++)
    						{
                                northwest = src[i - rowlength - 1];
                                northeast = src[i - rowlength];
                                southwest = src[i - 1];
                                southeast = src[i];
    							value     = top_factor * (left_factor * northwest + right_factor * northeast) +
    							bottom_factor * (left_factor * southwest + right_factor * southeast) + .5;
    							dst[j++]  = value;
    							i++;
    						}
    						i += addend;
    						j += addend;
    					}
    				}
    				else
    				{
    					if(xdiff < 0)
    					{
    					    left_factor  = -xdiff;
    					    right_factor = 1. + xdiff;
        					for(y = 0; y < yend + 1; y++)
        					{
        						dst[j++] = src[i++];
        						for(x = 0; x < xend; x++)
        						{
        						    value  = (left_factor * src[i]) + (right_factor * src[i + 1]) + .5;
        						    dst[j++] = value;
        							i++;
        						}
        						i += addend;
        						j += addend;
        					}
    					}
    					else
    					{
    						if(ydiff < 0)
    						{
    							top_factor    = -ydiff;
    							bottom_factor = 1. + ydiff;
    						    for(y = 0; y < yend; y++)
    						    {
    						    	for(x = 0; x < xend + 1; x++)
    								{
    								    value = (top_factor * src[i]) + (bottom_factor  * src[i + rowlength]) + .5;
    									i++;
    									dst[j++] = value;
    								}
    						    	i += addend;
    						    	j += addend;
    						    }
    						    for(x = 0; x < xend + 1; x++)
    							    dst[j++] = src[i++];
    						}
    						else
    						{
    						    
    						    //Do a shift with no interpolation. 
    						    
    							addend--;
    						    for(y = 0; y < yend + 1; y++)
    						    {
    						    	for(x = 0; x < xend + 1; x++)
    						    		dst[j++] = src[i++];
    						    	i += addend;
    						    	j += addend;
    						    }
    						}
    					}
    				}
    			}
    			else
    			{
    		        if(xtrans > 0 && ytrans < 0)
    				{
    				    xstart = 0;
    				    ystart = -yshift;
    			        i      = -yshift * xdim; 
    			        j      = 0;
    			        xend   = xdim - xshift - 1;
    			        yend   = ydim + yshift - 1;
    			        addend = xdim - xend;
    			        if(xdiff > 0 && ydiff < 0)
    					{
    				        rowlength     = xdim;
    				        left_factor   = 1. - xdiff;
    				        right_factor  = xdiff;
    				        top_factor    = 1. + ydiff;
    				        bottom_factor = -ydiff;
    				        for(x = 0; x < xend + 1; x++)
    					        dst[j++] = src[i++];
    						i += addend - 1;
    						j += addend - 1;
    				        for(y = 0; y < yend; y++)
    				        {
    					        for(x = 0; x < xend; x++)
    					        {
    						        northwest = src[i];
    						        northeast = src[i + 1];
    						        southwest = src[i - rowlength];
    						        southeast = src[i - rowlength + 1];
    						        value     = top_factor * (left_factor * northwest +  right_factor * northeast) +
    									        bottom_factor * (left_factor * southwest +  right_factor * southeast) + .5;
                                    dst[j++]    = value;
        					        i++;
    					        }
    					        dst[j++] = src[i++];
    					        i += addend;
    					        j += addend;
    				        }
    					}
    					else
    					{
    						if(xdiff > 0)
    						{
    					        left_factor  = 1. - xdiff;
    					        right_factor = xdiff;
        					    for(y = 0; y < yend + 1; y++)
        					    {
        					    	for(x = 0; x < xend; x++)
        					    	{
        					    	    value  = (left_factor * src[i]) + (right_factor * src[i + 1]) + .5;
        					    	    dst[j++] = value;
        					    		i++;
        					    	}
        					    	dst[j++] = src[i++];
        					    	i += addend;
        					    	j += addend;
        					    }
    						}
    						else
    						{
    							if(ydiff < 0)
    							{
    							    top_factor    = -ydiff;
    							    bottom_factor = 1. + ydiff;
    						        for(y = 0; y < yend; y++)
    						        {
    						        	for(x = 0; x < xend + 1; x++)
    							    	{
    							    	    value = (top_factor * src[i]) + (bottom_factor  * src[i + rowlength]) + .5;
    							    		i++;
    							    		dst[j++] = value;
    							    	}
    						        	i += addend;
    						        	j += addend;
    						        }
    						        for(x = 0; x < xend + 1; x++)
    							        dst[j++] = src[i++];
    							}
    							else
    							{
    							    addend--;
    						        for(y = 0; y < yend + 1; y++)
    						        {
    						    	    for(x = 0; x < xend + 1; x++)
    						    	    	dst[j++] = src[i++];
    						    	    i += addend;
    						    	    j += addend;
    						        }
    							}
    						}
    					}
    				}
    				else
    				
    				//xtrans < 0 && ytrans > 0      
    				
    				{
    				    xstart = -xshift;
    				    ystart = 0;
    			        i      = xstart + yshift * xdim; 
    			        j      = 0;
    			        xend   = xdim + xshift - 1;
    			        yend   = ydim - yshift - 1;
    			        addend = xdim - xend;
    			        if(xdiff < 0 && ydiff > 0)
    					{
    				        rowlength     = xdim;
    				        left_factor   = xdiff;
    				        right_factor  = 1. + xdiff;
    				        top_factor    = 1. - ydiff;
    				        bottom_factor = ydiff;
    					    for(y = 0; y < yend; y++)
    					    {
    					    	dst[j++] = src[i++];
    					    	for(x = 0; x < xend; x++)
    					    	{
                                    northwest = src[i];
                                    northeast = src[i - 1];
                                    southwest = src[i + rowlength - 1];
                                    southeast = src[i + rowlength];
    							    value     = top_factor * (left_factor * northwest + right_factor * northeast) +
    							    bottom_factor * (left_factor * southwest + right_factor * southeast) + .5;
    							    dst[j++]  = value;
    							    i++;
    						    }
    						    i += addend;
    						    j += addend;
    					    }
    					    for(x = 0; x < xend + 1; x++)
    						    dst[j++] = src[i++];
    					}
    					else
    					{
    						if(xdiff < 0)
    						{
    					        left_factor  = 1. + xdiff;
    					        right_factor = -xdiff;
        					    for(y = 0; y < yend + 1; y++)
        					    {
        					    	dst[j++] = src[i++];
        					    	for(x = 0; x < xend; x++)
        					    	{
        					    	    value  = (left_factor * src[i]) + (right_factor * src[i + 1]) + .5;
        					    	    dst[j++] = value;
        					    		i++;
        					    	}
        					    	i += addend;
        					    	j += addend;
        					    }
    						}
    						else
    						{
    							if(ydiff > 0)
    							{
    						        top_factor    = 1. - ydiff;
    						        bottom_factor = ydiff;
    						        for(y = 0; y < yend; y++)
    						        {
    							        for(x = 0; x < xend + 1; x++)
    							        {
    								        value     = (top_factor * src[i]) + (bottom_factor  * src[i + rowlength]) + .5;
    						                dst[j++] = value;
    							            i++;
    							        }
    							        i += addend - 1;
    							        j += addend - 1;
    						        }
    						        for(x = 0; x < xend + 1; x++)
    							        dst[j++] = src[i++];
    							}
    							else
    							{
    							    addend--;
    						        for(y = 0; y < yend + 1; y++)
    						        {
    						    	    for(x = 0; x < xend + 1; x++)
    						    	    	dst[j++] = src[i++];
    						    	    i += addend;
    						    	    j += addend;
    						        }
    							}
    						}
    					}
    				}
    			}
    		}
    	}
    	else
    	{
    		i         = 0;
    		xend      = xdim - 1;
    		yend      = ydim - 1;
    		rowlength = xdim;
    		if(xdiff != 0 && ydiff !=0)
    		{
    			if(xdiff > 0 && ydiff > 0)
    			{
    				left_factor   = 1. - xdiff;
    				right_factor  = xdiff;
    				top_factor    = 1. - ydiff;
    				bottom_factor = ydiff;
    				for(y = 0; y < yend; y++)
    				{
    					for(x = 0; x < xend; x++)
    					{
    						northwest = src[i];
    						northeast = src[i + 1];
    						southwest = src[i + rowlength];
    						southeast = src[i + rowlength + 1];
    						value     = top_factor * (left_factor * northwest +  right_factor * northeast) +
    									bottom_factor * (left_factor * southwest +  right_factor * southeast) + .5;
                            dst[i++]    = value;
    					}
    					dst[i] = src[i];
    					i++;
    				}
    				for(x = 0; x < xend + 1; x++)
    				{
    					dst[i] = src[i];
    				    i++;
    				}
    			}
    			else
    			{
    				if(xdiff > 0 || ydiff > 0)
    				{
    					if(xdiff > 0)
    					{
    				        left_factor   = 1. - xdiff;
    				        right_factor  = xdiff;
    				        top_factor    = -ydiff;
    				        bottom_factor = 1. + ydiff;
            				for(x = 0; x < xend + 1; x++)
            				{
            					dst[i] = src[i];
            				    i++;
            				}
            				for(y = 0; y < yend; y++)
            				{
            					for(x = 0; x < xend; x++)
            					{
            						northwest = src[i];
            						northeast = src[i + 1];
            						southwest = src[i + rowlength];
            						southeast = src[i + rowlength + 1];
            						value     = top_factor * (left_factor * northwest +  right_factor * northeast) +
            									bottom_factor * (left_factor * southwest +  right_factor * southeast) + .5;
                                    dst[i++]    = value;
            					}
            					dst[i] = src[i];
            					i++;
            				}
    					}
    					else
    					{
    				        left_factor   = -xdiff;
    				        right_factor  = 1. + xdiff;
    				        top_factor    = 1. - ydiff;
    				        bottom_factor = ydiff;
            				for(y = 0; y < yend; y++)
            				{
            					dst[i] = src[i];
            					i++;
            					for(x = 0; x < xend; x++)
            					{
            						northwest = src[i];
            						northeast = src[i + 1];
            						southwest = src[i + rowlength];
            						southeast = src[i + rowlength + 1];
            						value     = top_factor * (left_factor * northwest +  right_factor * northeast) +
            									bottom_factor * (left_factor * southwest +  right_factor * southeast) + .5;
                                    dst[i++]    = value;
            					}
            				}
            				for(x = 0; x < xend + 1; x++)
            				{
            					dst[i] = src[i];
            				    i++;
            				}
    					}
    				}
    				else
    				{
    				    left_factor   = -xdiff;
    				    right_factor  = 1. + xdiff;
    				    top_factor    = -ydiff;
    				    bottom_factor = 1. + ydiff;
        				for(x = 0; x < xend + 1; x++)
        				{
        					dst[i] = src[i];
        				    i++;
        				}
        				for(y = 0; y < yend; y++)
        				{
        					dst[i] = src[i];
        					i++;
        					for(x = 0; x < xend; x++)
        					{
        						northwest = src[i];
        						northeast = src[i + 1];
        						southwest = src[i + rowlength];
        						southeast = src[i + rowlength + 1];
        						value     = top_factor * (left_factor * northwest +  right_factor * northeast) +
        									bottom_factor * (left_factor * southwest +  right_factor * southeast) + .5;
                                dst[i++]    = value;
        					}
        				}
    				}
    			}
    		}
    		else
    		{
    			if(xdiff != 0 || ydiff != 0)
    			{
    				if(xdiff != 0)
    				{
    					if(xdiff > 0)
    					{
    					   left_factor  = 1. - xdiff;
    					   right_factor = xdiff;
    					   xend         = xdim - 1;
    					   yend         = ydim;
    					   i            = 0;
        				   for(y = 0; y < yend; y++)
        				   {
        					   for(x = 0; x < xend; x++)
        					   {
        					       value  = (left_factor * src[i]) + (right_factor * src[i + 1]) + .5;
        					       dst[i++] = value;
        					   }
        					   dst[i] = src[i];
    						   i++;
        				   }
    					}
    					else
    					{
    					    left_factor  = -xdiff;
    					    right_factor = 1. + xdiff;
    						xend         = xdim - 1;
    						yend         = ydim;
    						i            = 0;
        					for(y = 0; y < yend; y++)
        					{
        						dst[i] = src[i];
        						for(x = 0; x < xend; x++)
        						{
        						    value  = (left_factor * src[i]) + (right_factor * src[i + 1]) + .5;
        						    dst[i++] = value;
        						}
        					}
    					}
    				}
    				else
    				{
    					if(ydiff > 0)
    					{
    						top_factor    = 1. - ydiff;
    						bottom_factor = ydiff;
    						xend          = xdim;
    						yend          = ydim - 1;
    						i             = 0;
    						for(y = 0; y < yend; y++)
    						{
    							for(x = 0; x < xend; x++)
    							{
    								value     = (top_factor * src[i]) + (bottom_factor  * src[i + rowlength]) + .5;
    						        dst[i++] = value;
    							}
    						}
    						for(x = 0; x < xend; x++)
    						{
    							dst[i] = src[i];
    							i++;
    						}
    					}
    					else
    					{
    					    top_factor    = -ydiff;
    					    bottom_factor = 1. + ydiff;
    					    for(i = 0; i < xdim; i++)
    							dst[i] = src[i];
    					    for(y = 0; y < yend; y++)
    					    {
    					       	for(x = 0; x < xdim; x++)
    					    	{
    					    	    value = (top_factor * src[i]) + (bottom_factor  * src[i + rowlength]) + .5;
    					    		dst[i++] = value;
    					    	}
    					    }
    					}
    				}
    			}
    			else
    			{
    				for(i = 0; i < xdim * ydim; i++)
    					dest[i] = source[i];
    			}
    		}
    	}
        
        
        
        return(dest);
    }
    */
    
    
    
    /*
    void translate(src, dst, xdim, ydim, xtrans, ytrans)
    unsigned char *src, *dst;
    int    xdim, ydim; 
    double xtrans, ytrans; 
    {
    	register int    i, j, x, y, rowlength;
    	register int    xend, yend, addend;
        register double northwest,northeast,southwest,southeast;
    	register double left_factor, right_factor, top_factor, bottom_factor;
        register double value;
    	int    xshift, yshift;
    	int    xstart, ystart;
        double xdiff,  ydiff;

    	xshift = (int)xtrans;
    	yshift = (int)ytrans;
    	xdiff  = xtrans - xshift;
    	ydiff  = ytrans - yshift;

    	if(xshift != 0 && yshift != 0)
    	{
    		if(xtrans > 0 && ytrans > 0)
    		{
    			i      = 0; 
    			j      = xshift + yshift * xdim;
    			xend   = xdim - xshift - 1;
    			yend   = ydim - yshift - 1;
    			addend = xdim - xend;
    			if(xdiff > 0 && ydiff > 0)
    			{
    				rowlength     = xdim;
    				left_factor   = 1. - xdiff;
    				right_factor  = xdiff;
    				top_factor    = 1. - ydiff;
    				bottom_factor = ydiff;
    				for(y = 0; y < yend; y++)
    				{
    					for(x = 0; x < xend; x++)
    					{
    						northwest = src[i];
    						northeast = src[i + 1];
    						southwest = src[i + rowlength];
    						southeast = src[i + rowlength + 1];
    						value     = top_factor * (left_factor * northwest +  right_factor * northeast) +
    									bottom_factor * (left_factor * southwest +  right_factor * southeast) + .5;
                            dst[j++]  = value;
        					i++;
    					}
    					dst[j++] = src[i++];
    					i += addend;
    					j += addend;
    				}
    				for(x = 0; x < xend + 1; x++)
    					dst[j++] = src[i++];
    			}
    			else
    			{
    				if(xdiff > 0)
    				{
    					left_factor  = 1. - xdiff;
    					right_factor = xdiff;
    					for(y = 0; y < yend + 1; y++)
    					{
    						for(x = 0; x < xend; x++)
    						{
    						    value  = (left_factor * src[i]) + (right_factor * src[i + 1]) + .5;
    						    dst[j++] = value;
    							i++;
    						}
    						dst[j++] = src[i++];
    						i += addend;
    						j += addend;
    					}
    				}
    				else
    				{
    					if(ydiff > 0)
    					{
    						top_factor    = 1. - ydiff;
    						bottom_factor = ydiff;
    						for(y = 0; y < yend; y++)
    						{
    							for(x = 0; x < xend + 1; x++)
    							{
    								value     = (top_factor * src[i]) + (bottom_factor  * src[i + rowlength]) + .5;
    						        dst[j++] = value;
    							    i++;
    							}
    							i += addend;
    							j += addend;
    						}
    						for(x = 0; x < xend + 1; x++)
    							dst[j++] = src[i++];
    					}
    					else
    					{
    						
    						//Do a shift with no interpolation. 
    						
    						addend--;
    						for(y = 0; y < yend + 1; y++)
    						{
    							for(x = 0; x < xend + 1; x++)
    								dst[j++] = src[i++];
    							i += addend;
    							j += addend;
    						}
    					}
    				}
    			}
    		}
    		else
    		{
    		    if(xtrans < 0 && ytrans < 0)
    			{
    				xstart = -xshift;
    				ystart = -yshift;
    			    i      = xstart - yshift * xdim; 
    			    j      = 0;
    			    xend   = xdim + xshift - 1;
    			    yend   = ydim + yshift - 1;
    			    addend = xdim - xend;
    				if(xdiff < 0 && ydiff < 0)
    				{
    				    rowlength     = xdim;
    				    left_factor   = -xdiff;
    				    right_factor  = 1. + xdiff;
    				    top_factor    = -ydiff;
    				    bottom_factor = 1. + ydiff;
    					for(x = 0; x < xend + 1; x++)
    						dst[j++] = src[i++];
    					i += addend - 1;
    					j += addend - 1;
    					for(y = 0; y < yend; y++)
    					{
    						dst[j++] = src[i++];
    						for(x = 0; x < xend; x++)
    						{
                                northwest = src[i - rowlength - 1];
                                northeast = src[i - rowlength];
                                southwest = src[i - 1];
                                southeast = src[i];
    							value     = top_factor * (left_factor * northwest + right_factor * northeast) +
    							bottom_factor * (left_factor * southwest + right_factor * southeast) + .5;
    							dst[j++]  = value;
    							i++;
    						}
    						i += addend;
    						j += addend;
    					}
    				}
    				else
    				{
    					if(xdiff < 0)
    					{
    					    left_factor  = -xdiff;
    					    right_factor = 1. + xdiff;
        					for(y = 0; y < yend + 1; y++)
        					{
        						dst[j++] = src[i++];
        						for(x = 0; x < xend; x++)
        						{
        						    value  = (left_factor * src[i]) + (right_factor * src[i + 1]) + .5;
        						    dst[j++] = value;
        							i++;
        						}
        						i += addend;
        						j += addend;
        					}
    					}
    					else
    					{
    						if(ydiff < 0)
    						{
    							top_factor    = -ydiff;
    							bottom_factor = 1. + ydiff;
    						    for(y = 0; y < yend; y++)
    						    {
    						    	for(x = 0; x < xend + 1; x++)
    								{
    								    value = (top_factor * src[i]) + (bottom_factor  * src[i + rowlength]) + .5;
    									i++;
    									dst[j++] = value;
    								}
    						    	i += addend;
    						    	j += addend;
    						    }
    						    for(x = 0; x < xend + 1; x++)
    							    dst[j++] = src[i++];
    						}
    						else
    						{
    						    
    						    //Do a shift with no interpolation. 
    						    
    							addend--;
    						    for(y = 0; y < yend + 1; y++)
    						    {
    						    	for(x = 0; x < xend + 1; x++)
    						    		dst[j++] = src[i++];
    						    	i += addend;
    						    	j += addend;
    						    }
    						}
    					}
    				}
    			}
    			else
    			{
    		        if(xtrans > 0 && ytrans < 0)
    				{
    				    xstart = 0;
    				    ystart = -yshift;
    			        i      = -yshift * xdim; 
    			        j      = 0;
    			        xend   = xdim - xshift - 1;
    			        yend   = ydim + yshift - 1;
    			        addend = xdim - xend;
    			        if(xdiff > 0 && ydiff < 0)
    					{
    				        rowlength     = xdim;
    				        left_factor   = 1. - xdiff;
    				        right_factor  = xdiff;
    				        top_factor    = 1. + ydiff;
    				        bottom_factor = -ydiff;
    				        for(x = 0; x < xend + 1; x++)
    					        dst[j++] = src[i++];
    						i += addend - 1;
    						j += addend - 1;
    				        for(y = 0; y < yend; y++)
    				        {
    					        for(x = 0; x < xend; x++)
    					        {
    						        northwest = src[i];
    						        northeast = src[i + 1];
    						        southwest = src[i - rowlength];
    						        southeast = src[i - rowlength + 1];
    						        value     = top_factor * (left_factor * northwest +  right_factor * northeast) +
    									        bottom_factor * (left_factor * southwest +  right_factor * southeast) + .5;
                                    dst[j++]    = value;
        					        i++;
    					        }
    					        dst[j++] = src[i++];
    					        i += addend;
    					        j += addend;
    				        }
    					}
    					else
    					{
    						if(xdiff > 0)
    						{
    					        left_factor  = 1. - xdiff;
    					        right_factor = xdiff;
        					    for(y = 0; y < yend + 1; y++)
        					    {
        					    	for(x = 0; x < xend; x++)
        					    	{
        					    	    value  = (left_factor * src[i]) + (right_factor * src[i + 1]) + .5;
        					    	    dst[j++] = value;
        					    		i++;
        					    	}
        					    	dst[j++] = src[i++];
        					    	i += addend;
        					    	j += addend;
        					    }
    						}
    						else
    						{
    							if(ydiff < 0)
    							{
    							    top_factor    = -ydiff;
    							    bottom_factor = 1. + ydiff;
    						        for(y = 0; y < yend; y++)
    						        {
    						        	for(x = 0; x < xend + 1; x++)
    							    	{
    							    	    value = (top_factor * src[i]) + (bottom_factor  * src[i + rowlength]) + .5;
    							    		i++;
    							    		dst[j++] = value;
    							    	}
    						        	i += addend;
    						        	j += addend;
    						        }
    						        for(x = 0; x < xend + 1; x++)
    							        dst[j++] = src[i++];
    							}
    							else
    							{
    							    addend--;
    						        for(y = 0; y < yend + 1; y++)
    						        {
    						    	    for(x = 0; x < xend + 1; x++)
    						    	    	dst[j++] = src[i++];
    						    	    i += addend;
    						    	    j += addend;
    						        }
    							}
    						}
    					}
    				}
    				else
    				
    				//xtrans < 0 && ytrans > 0      
    				
    				{
    				    xstart = -xshift;
    				    ystart = 0;
    			        i      = xstart + yshift * xdim; 
    			        j      = 0;
    			        xend   = xdim + xshift - 1;
    			        yend   = ydim - yshift - 1;
    			        addend = xdim - xend;
    			        if(xdiff < 0 && ydiff > 0)
    					{
    				        rowlength     = xdim;
    				        left_factor   = xdiff;
    				        right_factor  = 1. + xdiff;
    				        top_factor    = 1. - ydiff;
    				        bottom_factor = ydiff;
    					    for(y = 0; y < yend; y++)
    					    {
    					    	dst[j++] = src[i++];
    					    	for(x = 0; x < xend; x++)
    					    	{
                                    northwest = src[i];
                                    northeast = src[i - 1];
                                    southwest = src[i + rowlength - 1];
                                    southeast = src[i + rowlength];
    							    value     = top_factor * (left_factor * northwest + right_factor * northeast) +
    							    bottom_factor * (left_factor * southwest + right_factor * southeast) + .5;
    							    dst[j++]  = value;
    							    i++;
    						    }
    						    i += addend;
    						    j += addend;
    					    }
    					    for(x = 0; x < xend + 1; x++)
    						    dst[j++] = src[i++];
    					}
    					else
    					{
    						if(xdiff < 0)
    						{
    					        left_factor  = 1. + xdiff;
    					        right_factor = -xdiff;
        					    for(y = 0; y < yend + 1; y++)
        					    {
        					    	dst[j++] = src[i++];
        					    	for(x = 0; x < xend; x++)
        					    	{
        					    	    value  = (left_factor * src[i]) + (right_factor * src[i + 1]) + .5;
        					    	    dst[j++] = value;
        					    		i++;
        					    	}
        					    	i += addend;
        					    	j += addend;
        					    }
    						}
    						else
    						{
    							if(ydiff > 0)
    							{
    						        top_factor    = 1. - ydiff;
    						        bottom_factor = ydiff;
    						        for(y = 0; y < yend; y++)
    						        {
    							        for(x = 0; x < xend + 1; x++)
    							        {
    								        value     = (top_factor * src[i]) + (bottom_factor  * src[i + rowlength]) + .5;
    						                dst[j++] = value;
    							            i++;
    							        }
    							        i += addend - 1;
    							        j += addend - 1;
    						        }
    						        for(x = 0; x < xend + 1; x++)
    							        dst[j++] = src[i++];
    							}
    							else
    							{
    							    addend--;
    						        for(y = 0; y < yend + 1; y++)
    						        {
    						    	    for(x = 0; x < xend + 1; x++)
    						    	    	dst[j++] = src[i++];
    						    	    i += addend;
    						    	    j += addend;
    						        }
    							}
    						}
    					}
    				}
    			}
    		}
    	}
    	else
    	{
    		i         = 0;
    		xend      = xdim - 1;
    		yend      = ydim - 1;
    		rowlength = xdim;
    		if(xdiff != 0 && ydiff !=0)
    		{
    			if(xdiff > 0 && ydiff > 0)
    			{
    				left_factor   = 1. - xdiff;
    				right_factor  = xdiff;
    				top_factor    = 1. - ydiff;
    				bottom_factor = ydiff;
    				for(y = 0; y < yend; y++)
    				{
    					for(x = 0; x < xend; x++)
    					{
    						northwest = src[i];
    						northeast = src[i + 1];
    						southwest = src[i + rowlength];
    						southeast = src[i + rowlength + 1];
    						value     = top_factor * (left_factor * northwest +  right_factor * northeast) +
    									bottom_factor * (left_factor * southwest +  right_factor * southeast) + .5;
                            dst[i++]    = value;
    					}
    					dst[i] = src[i];
    					i++;
    				}
    				for(x = 0; x < xend + 1; x++)
    				{
    					dst[i] = src[i];
    				    i++;
    				}
    			}
    			else
    			{
    				if(xdiff > 0 || ydiff > 0)
    				{
    					if(xdiff > 0)
    					{
    				        left_factor   = 1. - xdiff;
    				        right_factor  = xdiff;
    				        top_factor    = -ydiff;
    				        bottom_factor = 1. + ydiff;
            				for(x = 0; x < xend + 1; x++)
            				{
            					dst[i] = src[i];
            				    i++;
            				}
            				for(y = 0; y < yend; y++)
            				{
            					for(x = 0; x < xend; x++)
            					{
            						northwest = src[i];
            						northeast = src[i + 1];
            						southwest = src[i + rowlength];
            						southeast = src[i + rowlength + 1];
            						value     = top_factor * (left_factor * northwest +  right_factor * northeast) +
            									bottom_factor * (left_factor * southwest +  right_factor * southeast) + .5;
                                    dst[i++]    = value;
            					}
            					dst[i] = src[i];
            					i++;
            				}
    					}
    					else
    					{
    				        left_factor   = -xdiff;
    				        right_factor  = 1. + xdiff;
    				        top_factor    = 1. - ydiff;
    				        bottom_factor = ydiff;
            				for(y = 0; y < yend; y++)
            				{
            					dst[i] = src[i];
            					i++;
            					for(x = 0; x < xend; x++)
            					{
            						northwest = src[i];
            						northeast = src[i + 1];
            						southwest = src[i + rowlength];
            						southeast = src[i + rowlength + 1];
            						value     = top_factor * (left_factor * northwest +  right_factor * northeast) +
            									bottom_factor * (left_factor * southwest +  right_factor * southeast) + .5;
                                    dst[i++]    = value;
            					}
            				}
            				for(x = 0; x < xend + 1; x++)
            				{
            					dst[i] = src[i];
            				    i++;
            				}
    					}
    				}
    				else
    				{
    				    left_factor   = -xdiff;
    				    right_factor  = 1. + xdiff;
    				    top_factor    = -ydiff;
    				    bottom_factor = 1. + ydiff;
        				for(x = 0; x < xend + 1; x++)
        				{
        					dst[i] = src[i];
        				    i++;
        				}
        				for(y = 0; y < yend; y++)
        				{
        					dst[i] = src[i];
        					i++;
        					for(x = 0; x < xend; x++)
        					{
        						northwest = src[i];
        						northeast = src[i + 1];
        						southwest = src[i + rowlength];
        						southeast = src[i + rowlength + 1];
        						value     = top_factor * (left_factor * northwest +  right_factor * northeast) +
        									bottom_factor * (left_factor * southwest +  right_factor * southeast) + .5;
                                dst[i++]    = value;
        					}
        				}
    				}
    			}
    		}
    		else
    		{
    			if(xdiff != 0 || ydiff != 0)
    			{
    				if(xdiff != 0)
    				{
    					if(xdiff > 0)
    					{
    					   left_factor  = 1. - xdiff;
    					   right_factor = xdiff;
    					   xend         = xdim - 1;
    					   yend         = ydim;
    					   i            = 0;
        				   for(y = 0; y < yend; y++)
        				   {
        					   for(x = 0; x < xend; x++)
        					   {
        					       value  = (left_factor * src[i]) + (right_factor * src[i + 1]) + .5;
        					       dst[i++] = value;
        					   }
        					   dst[i] = src[i];
    						   i++;
        				   }
    					}
    					else
    					{
    					    left_factor  = -xdiff;
    					    right_factor = 1. + xdiff;
    						xend         = xdim - 1;
    						yend         = ydim;
    						i            = 0;
        					for(y = 0; y < yend; y++)
        					{
        						dst[i] = src[i];
        						for(x = 0; x < xend; x++)
        						{
        						    value  = (left_factor * src[i]) + (right_factor * src[i + 1]) + .5;
        						    dst[i++] = value;
        						}
        					}
    					}
    				}
    				else
    				{
    					if(ydiff > 0)
    					{
    						top_factor    = 1. - ydiff;
    						bottom_factor = ydiff;
    						xend          = xdim;
    						yend          = ydim - 1;
    						i             = 0;
    						for(y = 0; y < yend; y++)
    						{
    							for(x = 0; x < xend; x++)
    							{
    								value     = (top_factor * src[i]) + (bottom_factor  * src[i + rowlength]) + .5;
    						        dst[i++] = value;
    							}
    						}
    						for(x = 0; x < xend; x++)
    						{
    							dst[i] = src[i];
    							i++;
    						}
    					}
    					else
    					{
    					    top_factor    = -ydiff;
    					    bottom_factor = 1. + ydiff;
    					    for(i = 0; i < xdim; i++)
    							dst[i] = src[i];
    					    for(y = 0; y < yend; y++)
    					    {
    					       	for(x = 0; x < xdim; x++)
    					    	{
    					    	    value = (top_factor * src[i]) + (bottom_factor  * src[i + rowlength]) + .5;
    					    		dst[i++] = value;
    					    	}
    					    }
    					}
    				}
    			}
    			else
    			{
    				for(i = 0; i < xdim * ydim; i++)
    					dst[i] = src[i];
    			}
    		}
    	}
    }

    
    //The workspace should be equal to twice the size of one of the image files. 
    //Note: if the mallocing is done from within the function there is a bug     
    //even if it is properly freed at the end of the function (besides being     
    //slower as well).  This may have something to do with how fast and how many 
    //times the function gets called over again. The function returns three      
    //result codes: 0 - the optimal result, the estimate converged to a local    
    //minimum after the typical number of iterations; 1 - possibly a somewhat    
    //accurate result but the estimate did not converge within a typical number  
    //of iterations; 2 - the estimate may be accurate but it has reached the     
    //limit of precision, 3 pixels or the width of the gradient filter. In the   
    //third case, it may be possible to extend the precision by resampling and   
    //correlating them from different offsets(possibly with                      
    //get_translation_with_resample()) or comparing them at a smaller scale      
    //(possibly with subsample() or shrinkavg() in conjunction with this         
    //function).                                                                 
    
    int get_translation(first, second, xdim, ydim, xtrans, ytrans, number_of_iterations, workspace)
    unsigned char *first, *second, *workspace;
    int            xdim, ydim; 
    double        *xtrans, *ytrans; 
    int           *number_of_iterations; 
    {
        register double a1,a2;

        double        increment;
        double        first_xincrement, first_yincrement;    
        double        xincrement_max, yincrement_max;
        double        xdiff, ydiff;
        double        xsize_cutoff, ysize_cutoff;

        int           xshift, yshift, maximum_shift;
        int           result;
        int           maximum_iterations;
        int           current_iteration;

        register int           delta, xpos, ypos, addend;
        register int           xgradient_sum, ygradient_sum;
    	register int           rowlength;
        register int           least_x, greatest_x, least_y, greatest_y;

        register int           i;
        register double        w,x,z;
        register double        b1,b2;
        register double        xx, xy, yy, xdelta, ydelta;
        register double        xgradient, ygradient;
        register double        xincrement, yincrement;    

    	unsigned char *shift, *intermediate;

        maximum_shift      = 3;
        maximum_iterations = 4; 
    	rowlength          = xdim;
    	intermediate       = workspace + xdim * ydim;

        xsize_cutoff = ysize_cutoff = 0.;
        least_x      = least_y      = 0;
        a1 = 0.;
        a2 = 0.;
        current_iteration = 0;
        greatest_x = xdim -1;
        greatest_y = ydim -1;
    	shift = second;

        while(1)
        {
            current_iteration++;
            if(current_iteration  > maximum_iterations)
            {
                result = 1;
                *xtrans = a1;
                *ytrans = a2;
                *number_of_iterations = current_iteration;
                break;
            }
            w = x = z = b1 = b2 = 0.;
    		i = least_x + 1 + (least_y + 1) * xdim;
    		addend = least_x + 1;
    		addend += (xdim - 1) - greatest_x + 1;
            for(ypos = least_y + 1; ypos < greatest_y; ypos++)
    		{
    		    for(xpos = least_x + 1; xpos < greatest_x; xpos++)
                {
    				xgradient_sum = (shift[i+1] - shift[i-1]) + (shift[i-rowlength+1] - shift[i-rowlength-1]) + (shift[i+rowlength+1] - shift[i+rowlength-1]);  
    				ygradient_sum = (shift[i+rowlength] - shift[i-rowlength]) + (shift[i+rowlength-1] - shift[i-rowlength-1]) + (shift[i+rowlength+1] - shift[i-rowlength+1]); 
                    xgradient =  xgradient_sum;
                    ygradient =  ygradient_sum;
                    xgradient /= 3.;
                    ygradient /= 3.;
                    xx        =  xgradient * xgradient;
                    xy        =  xgradient * ygradient;
                    yy        =  ygradient * ygradient;
                    delta     =  first[i] - shift[i];
                    xdelta    =  xgradient * delta;
                    ydelta    =  ygradient * delta;
                    w         += xx;
                    x         += xy;
                    z         += yy;
                    b1        += xdelta; 
                    b2        += ydelta;
    				i++;
                }
    			i += addend;
            }
        
            yincrement = (b2 - x * b1/ w)/(z - x * x / w);
            xincrement = (b1 - x * b2/ z)/(w - x * x / z);
        
            if((xincrement <= xsize_cutoff && xincrement >= -xsize_cutoff) && (yincrement <= ysize_cutoff && yincrement >= -ysize_cutoff))
            {
                *xtrans = a1;
                *ytrans = a2;
                *number_of_iterations = current_iteration;
                result = 0;
                break;
            }

            if(current_iteration == 1)
            {
                if(xincrement == 0. && yincrement == 0.)
                {
                    result = 0;
                    *xtrans = 0.;
                    *ytrans = 0.;
                    *number_of_iterations = current_iteration;
                    break;
                }
    	        shift    = workspace;

                xincrement_max = fabs(xincrement);
                xsize_cutoff   = xincrement_max / 100.;
        
                yincrement_max = fabs(yincrement);
                ysize_cutoff   = yincrement_max / 100.;
                 

                first_xincrement = xincrement;
                if(xincrement < 0.)
                    a1 = -1. - xincrement;
                else
                {
                    if(xincrement != 0.)
                        a1 = 1. - xincrement;
                    else
                        a1 = 0.;
                }

                first_yincrement = yincrement;
                if(yincrement < 0.)
                    a2 = -1. - yincrement;
                else
                {
                    if(yincrement != 0.)
                        a2 = 1. - yincrement;
                    else
                        a2 = 0.;
                }
            }
            else
            {
                increment = fabs(xincrement);
                if(xincrement_max < increment)
                {
                    xincrement_max    = increment;
                    xsize_cutoff      = increment / 100.; 
                }

                increment = fabs(yincrement);
                if(yincrement_max < increment)
                {
                    yincrement_max = increment;
                    ysize_cutoff   = increment / 100.;
                }

                if(current_iteration == 2)
                {
                    if(((xincrement <= 0 && first_xincrement <= 0) || 
                        (xincrement >= 0 && first_xincrement >= 0)))
                    {
                        if(a1 < 0.)
                            a1 = -2.0 - xincrement;
                        else
                            a1 =  2.0 - xincrement;
                        
                        first_xincrement = xincrement;
                    }
                    else
                    {
                        if(fabs(first_xincrement) < fabs(xincrement))
                        {
                            a1 = 0.;
                            xincrement = first_xincrement;    
                        }
                    }

                    if(((yincrement <= 0 && first_yincrement <= 0) ||
                        (yincrement >= 0 && first_yincrement >= 0)))
                    {
                        if(a2 < 0.)
                            a2 = -2.0 - yincrement;
                        else
                            a2 =  2.0 - yincrement;
                        
                        first_yincrement = yincrement;
                    }
                    else
                    {
                        if(fabs(first_yincrement) < fabs(yincrement))
                        {
                            a2 = 0.;
                            yincrement = first_yincrement;
                        }
                    }
                }

                if(current_iteration == 3)
                {
                    if(fabs(a1) == 2.)
                    {
                        if(((xincrement <= 0 && first_xincrement <= 0) || 
                            (xincrement >= 0 && first_xincrement >= 0)))
                        {
                            if(a1 < 0.)
                                a1 = -3.0 - xincrement;
                            else
                                a1 =  3.0 - xincrement;
                        }
                        else
                        {
                            if(fabs(first_xincrement) < fabs(xincrement))
                            {
                                if(a1 > 0.)
                                    a1 = 1.;
                                else
                                    a1 = -1.;
                                xincrement = first_xincrement;    
                            }
                        }
                    }

                    if(fabs(a2) == 2.)
                    {
                        if(((yincrement <= 0 && first_yincrement <= 0) ||
                            (yincrement >= 0 && first_yincrement >= 0)))
                        {
                            if(a2 < 0.)
                                a2 = -3.0 - yincrement;
                            else
                                a2 =  3.0 - yincrement;
                        }
                        else
                        {
                            if(fabs(first_yincrement) < fabs(yincrement))
                            {
                                if(a2 > 0.)
                                    a2 = 1.;
                                else
                                    a2 = -1.;
                                yincrement = first_yincrement;
                            }
                        }
                    }
                }
            }
        
            a1 += xincrement;
            xshift = -a1;

            a2 += yincrement;
            yshift = -a2;

            if((abs(yshift) >= maximum_shift) || (abs(xshift) >= maximum_shift))
            {
                result  = 2;
                *xtrans = a1;
                *ytrans = a2;
                *number_of_iterations = current_iteration;
                break;
            }
        
            if(xshift != 0)
            {
                if(xshift < 0)
                {
                    least_x    = 0;
                    greatest_x = xdim -1 + xshift;
                }
                else
                {
                    least_x    = xshift;
                    greatest_x = xdim -1;
                }
            }
            else
            {
                least_x    = 0;
                greatest_x = xdim -1;
            }
        
            if(yshift != 0)
            {
                if(yshift < 0)
                {
                    least_y    = 0;
                    greatest_y = ydim -1 + yshift;
                }
                else
                {
                    least_y    = yshift;
                    greatest_y = ydim -1;
                }
            }
            else
            {
                least_y    = 0;
                greatest_y = ydim -1;
            }
            translate(second, shift, xdim, ydim, -a1, -a2);

        }
        return(result);
    }
    */

}