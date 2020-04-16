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
	    
	    System.out.println("Xdim is " + xdim + ", ydim is " + ydim);
	    double source[];
        double dest[];
    	double gray1[]       = new double[xdim * ydim];
        double gray2[]       = new double[xdim * ydim];
        boolean isAssigned[] = new boolean[xdim * ydim];
        int number_of_uninterpolated_cells = 0;
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
        boolean even = true;  // Keep track of which buffer is the source and which is the destination.
        while(number_of_uninterpolated_cells != 0)
        {
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
    		    if(isAssigned[i] = false)
    			    number_of_uninterpolated_cells++;
    	    }
        }
        //System.out.println("Got here.");
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
	}
	
	
	// This function modifies values in isInterpolated and dst, and can be called multiple times
	// until all the values in isInterpolated are true.
	// Also, using single index into image to keep low level code simple--will have to reformat
	// data for processing--see getVarianceImage.
	public static void dilateImage(double src[], boolean isInterpolated[], int xdim, int ydim, double dst[])
	{	
		for(int i = 0; i < ydim; i++)
		{
			for(int j = 0; j < xdim; j++)
			{
				int k = i * xdim + j;
				if(isInterpolated[k])
					dst[k] = src[k];
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
                	    value            /= total_weight;
                	    dst[k]            = (int) value;
                	    isInterpolated[k] = true;
                    }
                    else
                	    dst[k] = 0;                           // No neighbors, set value to zero.
				}
			}
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
}