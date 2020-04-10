import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.awt.Point;

public class DataMapper
{
	public static double getDistance(double x, double y, double x_origin, double y_origin)
	{
	    double distance  = Math.sqrt((x - x_origin) * (x - x_origin) + (y - y_origin) * (y - y_origin));
	    return(distance);
	}
	
	public static double sin(double degrees)
	{
	    double radians = StrictMath.toRadians(degrees);
	    double sin     = StrictMath.sin(radians);
	    return(sin);
	}

	
	public static double getSlope(Line2D.Double line)
	{
	    
		double x1 = line.getX1();
	    double y1 = line.getY1();
	    double x2 = line.getX2();
	    double y2 = line.getY2();  
	    double slope;
	    
	    if(y1 == y2)
	    	return(Double.NaN);
	    else if(x1 == x2)
	    	return(0);
	    else
	    	return(slope = (y2 - y1) / (x2 - x1));
	}
	
	public static double getDegrees(double radians)
	{
		double degrees = radians / 0.0174533;
		return(degrees);
	}
	
	public static double getLength(Line2D.Double line)
	{
	    double x1     = line.getX1();
	    double y1     = line.getY1();
	    double x2     = line.getX2();
	    double y2     = line.getY2(); 
	    double length = StrictMath.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	    return(length);
	}
	
	public static double getYIntercept(Point2D.Double point, double slope)
	{
		double x           = point.getX();
		double y           = point.getY();
		double y_intercept = y - slope * x;
		return (y_intercept);
	}
	
	//Broken
	public static Point2D.Double getIntersectPoint(Point2D.Double upper_left, Point2D.Double upper_right, Point2D.Double lower_right, Point2D.Double lower_left)
	{
		Line2D.Double first_diagonal       = new Line2D.Double(lower_left,  upper_right);
		Line2D.Double second_diagonal      = new Line2D.Double(lower_right, upper_left);
		Line2D.Double reference            = new Line2D.Double(lower_left, upper_left);
		double        reference_length     = getLength(reference);
		double        first_slope          = Math.abs(getSlope(first_diagonal));
		double        second_slope         = Math.abs(getSlope(second_diagonal));
		
		// Get the degrees.
		double        first_degrees        = getDegrees(first_slope);                  //a
		double        second_degrees       = getDegrees(second_slope);                 //b
		double        third_degrees        = 90 - first_degrees;                       //c
		double        fourth_degrees       = 90 - third_degrees;                       //d
		double        reference_degrees    = second_degrees + fourth_degrees;          //e
		double        determinant_degrees  = 90 - second_degrees;                      //f
		
		// Start the calculations.
		double        determinant_length   = sin(determinant_degrees) / sin(reference_degrees) * reference_length;
		double        determinant          = determinant_length  / sin(90);
		double        delta_y              = determinant * sin(first_degrees);
		double        delta_x              = determinant * sin(fourth_degrees);	
		double        x1                   = lower_left.getX();
		double        y1                   = lower_left.getY();
		double        x2                   = x1 + delta_x;
		double        y2                   = y1 + delta_y;
		
		Point2D.Double intersect_point     = new Point2D.Double(x2, y2);
		return(intersect_point);
	}
	
	public static double getTrianglePerimeter(Point2D.Double base1, Point2D.Double base2, Point2D.Double top)
	{
		Line2D.Double first_side    = new Line2D.Double(base1,  top);	
		Line2D.Double second_side   = new Line2D.Double(base2,  top);
		Line2D.Double third_side    = new Line2D.Double(base1,  base2);	
		double        first_length  = getLength(first_side);
		double        second_length = getLength(second_side);
		double        third_length  = getLength(third_side);
		double        perimeter     = first_length + second_length + third_length;
		return(perimeter);	
	}
	
	// The orientation of the points is not important, just a convenience to understand the code.
	public static double getTriangleArea(Point2D.Double base1, Point2D.Double base2, Point2D.Double top)
	{
		double x1 = base1.getX();
		double y1 = base1.getY();
		double x2 = base2.getX();
		double y2 = base2.getY();
		double x3 = top.getX();
		double y3 = top.getY();
		
		// Return 0 if the points are on a line.	
		if((x1 == x2) && (x2 == x3))
		    return(0);
		if((y1 == y2) && (y2 == y3))
			return(0);

		Line2D.Double base = new Line2D.Double(base1, base2);
		Line2D.Double height;
		double base_slope, base_intercept, perpendicular_slope, area, base_length, height_length;
		
		if(x1 == x2)
		{
			//Base slope is undefined.  
		    double x_intersect           = x1;	
		    double y_intersect           = top.getY();
		    Point2D.Double top_intersect = new Point2D.Double(x_intersect, y_intersect);
		    height                       = new Line2D.Double(top, top_intersect); 
		}
		else
		{ 
			base_slope = (y2 - y1)/(x2 - x1);
			if(base_slope != 0.)
			{	
				//Base slope is defined and non-zero.
				base_intercept               = getYIntercept(base1, base_slope);		
			    perpendicular_slope          = -1. / base_slope;
		        double top_intercept         = getYIntercept(top, perpendicular_slope);
		        double x_intersect           = (base_intercept - top_intercept)/(perpendicular_slope - base_slope);
		        double y_intersect           = base_slope * x_intersect + base_intercept;
		        Point2D.Double top_intersect = new Point2D.Double(x_intersect, y_intersect);
		        height                       = new Line2D.Double(top, top_intersect);  
			}
			else
			{
				//Base slope is zero.
				double y_intersect           = base1.getY();
				double x_intersect           = top.getX();
				Point2D.Double top_intersect = new Point2D.Double(x_intersect, y_intersect);
				height                       = new Line2D.Double(top, top_intersect);
			}	
		}
		base_length   = getLength(base);
        height_length = getLength(height);
        area          = base_length * height_length * .5;
	    return(area);
	}	
	
	public static double getQuadrilateralPerimeter(Point2D.Double lower_left, Point2D.Double upper_left, Point2D.Double upper_right, Point2D.Double lower_right)
	{
		Line2D.Double first_side    = new Line2D.Double(lower_left,  upper_left);	
		Line2D.Double second_side   = new Line2D.Double(upper_left,  upper_right);
		Line2D.Double third_side    = new Line2D.Double(upper_right, lower_right);	
		Line2D.Double fourth_side   = new Line2D.Double(lower_right, lower_left);
		double        first_length  = getLength(first_side);
		double        second_length = getLength(second_side);
		double        third_length  = getLength(third_side);
		double        fourth_length = getLength(fourth_side);
		double        perimeter     = first_length + second_length + third_length + fourth_length;
		return(perimeter);	
	}
	
	public static double getQuadrilateralArea(Point2D.Double lower_left, Point2D.Double upper_left, Point2D.Double upper_right, Point2D.Double lower_right)
	{
		double x1 = upper_left.getX();
		double y1 = upper_left.getY();
		double x2 = upper_right.getX();
		double y2 = upper_right.getY();
		double x3 = lower_right.getX();
		double y3 = lower_right.getY();
		double x4 = lower_left.getX();
		double y4 = lower_left.getY();
		
		/*
		System.out.println("x1 = " + x1 + ", y1 = " + y1);
		System.out.println("x2 = " + x2 + ", y2 = " + y2);
		System.out.println("x3 = " + x3 + ", y3 = " + y3);
		System.out.println("x4 = " + x4 + ", y4 = " + y4);
		
		*/
	 
	    double area1 = getTriangleArea(upper_left, lower_left, upper_right);
	    if(Double.isNaN(area1))
	    {
	        //System.out.println("Area 1 is not a number.");	
	    }
	    double area2 = getTriangleArea(lower_left, lower_right, upper_right);
	    if(Double.isNaN(area2))
	    {
	    	//System.out.println("Area 2 is not a number.");	
	    }
	    double area = area1 + area2;
	    return(area);
	}

	public static void smooth(int src[], int xdim, int ydim, double smooth_factor, int number_of_iterations, int dst[])
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
	
	// This function modifies values in isInterpolated and dst, and can be called multiple times
	// until all the values in isInterpolated are true.
	// Could be rewritten to do iterations inside function.
	// Also, using single index into image to keep low level code simple--will have to reformat
	// data for processing--see getVarianceImage.
	public static void getImageDilation(double src[], boolean isInterpolated[], int xdim, int ydim, double dst[])
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

    public static boolean containsPoint(Point2D.Double point, Sample ... sample)
    {
    	boolean contains      = false;
    	Path2D.Double path    = new Path2D.Double();
    	Sample current_sample = sample[0]; 
	    path.moveTo(current_sample.x, current_sample.y);
	    for(int i = 0; i < sample.length; i++)
	    {
	    	current_sample = sample[i];
	    	path.lineTo(current_sample.x, current_sample.y);
	    }
	    path.closePath();
		if(path.contains(point))
    	    contains = true;
    	return(contains);
    }
    
    public static double getLinearInterpolation(Point2D.Double point, Sample ... sample)
    {
    	if(sample.length == 3)
    	{
    		double x1 = sample[0].x;
	    	double y1 = sample[0].y; 
	    	
	    	double x2 = sample[1].x;
	    	double y2 = sample[1].y;
	    	
	    	double x3 = sample[2].x;
	    	double y3 = sample[2].y;
			
			Point2D.Double base1  = new Point2D.Double(x1, y1);
	        Point2D.Double top    = new Point2D.Double(x2, y2);
	        Point2D.Double base2  = new Point2D.Double(x3, y3); 
	        
	        
	        double area1 = DataMapper.getTriangleArea(point, base2, top);
	        double area2 = DataMapper.getTriangleArea(base1, base2, point);
	        double area3 = DataMapper.getTriangleArea(base1, point, top);
	        
	        double total_area = area1 + area2 + area3;
	        double weight1    = area1 / total_area;
	        double weight2    = area2 / total_area;
	        double weight3    = area3 / total_area;
	        
	        double value = sample[0].intensity * weight1 + 
	        		       sample[1].intensity * weight2 + 
	        		       sample[2].intensity * weight3;	
	        return(value);
    	}
    	// Implement this later.
    	else if(sample.length == 4)
    	{
    		double x1 = sample[0].x;
	    	double y1 = sample[0].y; 
	    	
	    	double x2 = sample[1].x;
	    	double y2 = sample[1].y;
	    	
	    	double x3 = sample[2].x;
	    	double y3 = sample[2].y;
	    	
	    	double x4 = sample[3].x;
	    	double y4 = sample[3].y;
	    	
	    	Point2D.Double upper_left  = new Point2D.Double(x1, y1);
	    	Point2D.Double upper_right = new Point2D.Double(x2, y2);
	    	Point2D.Double lower_right = new Point2D.Double(x3, y3);
	    	Point2D.Double lower_left  = new Point2D.Double(x4, y4);  
	    	
	    	    		          
	    	Line2D.Double top      = new Line2D.Double(upper_left, upper_right);
	    	Line2D.Double left     = new Line2D.Double(upper_left, lower_left);
	    	Line2D.Double bottom   = new Line2D.Double(lower_left, lower_right);
	    	Line2D.Double right    = new Line2D.Double(lower_right, upper_right);
	    	    		    	
	    	//We need to get four more points to do our bilinear interpolation.
	    	double x5, y5, x6, y6;
	    
	    	double slope = DataMapper.getSlope(top);
	    	if(slope == 0)
	    	{
	    		y5 = y1;
	    	}
	    	else
	    	{
	    	    double y_intercept = DataMapper.getYIntercept(upper_left, slope);
	    	    y5 = slope * point.getX() + y_intercept;
	    	}
	    	slope = DataMapper.getSlope(bottom);
	    	if(slope == 0)
	    	{
	    		y6 = y4;
	    	}
	    	else
	    	{
	    	    double y_intercept = DataMapper.getYIntercept(lower_left, slope);
	    	    y6 = slope * point.getX() + y_intercept;
	    	}
	    	    			    
	    	if(x2 != x3)
	    	{
	    	    slope = DataMapper.getSlope(right);
	    	    double y_intercept = DataMapper.getYIntercept(upper_right, slope);
	    	    x5 = (point.getY() - y_intercept) / slope;
	    	}
	    	else
	    	{
	    	    double distance = right.ptSegDist(point);
	    	    x5              = point.getY() + distance;
	    	}
	    	    			    
	    	if(x1 != x4)
	    	{
	    	    slope = DataMapper.getSlope(left);
	    	    double y_intercept = DataMapper.getYIntercept(lower_left, slope);
	    	    x6 = (point.getY() - y_intercept) / slope;
	    	}
	    	else
	    	{
	    	    double distance = left.ptSegDist(point);
	    	    x6              = point.getX() - distance;
	    	}
	    	    			      
	    	Point2D.Double middle_top    = new Point2D.Double(point.getX(), y5);
	    	Point2D.Double middle_bottom = new Point2D.Double(point.getX(), y6);
	    	Point2D.Double middle_right  = new Point2D.Double(x5, point.getY());
	    	Point2D.Double middle_left   = new Point2D.Double(x6, point.getY()); 
	    	    		        
	    	double area1 = DataMapper.getQuadrilateralArea(middle_left, upper_left, middle_top, point);
	    	double area2 = DataMapper.getQuadrilateralArea(point, middle_top, upper_right, middle_right);
	    	double area3 = DataMapper.getQuadrilateralArea(middle_bottom, point, middle_right, lower_right);
	    	double area4 = DataMapper.getQuadrilateralArea(lower_left, middle_left, point, middle_bottom);
	    	double total_area = area1 + area2 + area3 + area4;
	    	    		        
	    	total_area =  DataMapper.getQuadrilateralArea(lower_left, upper_left, upper_right, lower_right);
	    	    		
	    	double weight1 = area3 / total_area;
	    	double weight2 = area4 / total_area;
	    	double weight3 = area1 / total_area;
	    	double weight4 = area2 / total_area;
	    	    		        
	    	double value = sample[0].intensity * weight1 + 
	    	    		   sample[1].intensity * weight2 + 
	    	    		   sample[2].intensity * weight3 +
	    	    		   sample[3].intensity * weight4;
	    	return(value);
	    	
    	}
    	else
    		return(0.);
    }
    
    // This returns the bisecting average from a line determined by two samples, or the nearest endpoint sample value if no bisecting line from the point exists.
	public static double getBisectingAverage(Sample sample1, Sample sample2, Point2D.Double point)
	{ 
		double x1 = sample1.x;
		double y1 = sample1.y;
		double x2 = sample2.x;
		double y2 = sample2.y;
		double x3 = point.getX();
		double y3 = point.getY();
		
        
		Line2D.Double determinant_line     = new Line2D.Double(x1, y1, x2, y2);
		double        determinant_distance = determinant_line.ptSegDist(x3, y3);
		double        determinant_length   = DataMapper.getLength(determinant_line);
		// Find out if there's a bisecting line.
		Line2D.Double endpoint_line1 = new Line2D.Double(x1, y1, x3, y3);
		Line2D.Double endpoint_line2 = new Line2D.Double(x2, y2, x3, y3);
		double length1 = DataMapper.getLength(endpoint_line1);
		double length2 = DataMapper.getLength(endpoint_line2);
		if(determinant_distance == length1 || determinant_distance == length2) // No biscecting line, return one of the endpoint values.
		{
			if(determinant_distance == length1)
				return(sample1.intensity);
			else
				return(sample2.intensity);
		}

		if(x1 == x2)
		{
		    double segment_length = Math.abs(y2 - y3);
		    double weight1        = segment_length / determinant_length;
		    double weight2 = (determinant_length - segment_length) / determinant_length;
		    double value = weight1 * sample1.intensity + weight2 * sample2.intensity;
		    return(value);
		    
		}
		else if(y1 == y2)
		{
			double segment_length = Math.abs(x2 - x3);
		    double weight1        = segment_length / determinant_length;
		    double weight2 = (determinant_length - segment_length) / determinant_length; 
		    double value = weight1 * sample1.intensity + weight2 * sample2.intensity;
		    return(value);
		}
		else
		{
			// We know the slope is neither 0 nor infinity.
			double        determinant_slope = Math.abs(DataMapper.getSlope(determinant_line));          //B  
			
			// Get degrees of significant angles and apply the law of sines.
		    double        first_degrees        = DataMapper.getDegrees(determinant_slope);                 //a
			
		    Line2D.Double reference_line    = new Line2D.Double(x1, y1, x3, y3);
		    double        reference_length  = DataMapper.getLength(reference_line);
		    double        second_degrees    = 0.0;   
		    if(x1 == x3)
		    {
		    	second_degrees = 90.;
		    }
		    else if(y1 == y3)
		    {
		    	second_degrees = 0;
		    }
		    else
		    {
		    	double reference_slope = Math.abs(DataMapper.getSlope(reference_line));	
		    	second_degrees       = DataMapper.getDegrees(reference_slope);                   //b
		    }
		       
		    double        third_degrees        = 0;                                                        //c
		    if(first_degrees < second_degrees)
		       third_degrees = second_degrees - first_degrees;
		    else
			   third_degrees = first_degrees - second_degrees;
		    double fourth_degrees =  90 - third_degrees;                                                      //d
		    double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
		    double weight1 = segment_length / determinant_length;
		    double weight2 = (determinant_length - segment_length) / determinant_length;
		    double value = weight1 * sample1.intensity + weight2 * sample2.intensity;
		    return(value);
		}
	}
	
	// This returns the bisecting point from a line determined by two samples, or the nearest endpoint location if no bisecting line from the point exists.
	public static Point2D.Double getBisectingPoint(Sample sample1, Sample sample2, Point2D.Double point)
	{ 
	    double x1 = sample1.x;
	    double y1 = sample1.y;
	    double x2 = sample2.x;
		double y2 = sample2.y;
		double x3 = point.getX();
		double y3 = point.getY();
			
	        
		Line2D.Double determinant_line     = new Line2D.Double(x1, y1, x2, y2);
		double        determinant_distance = determinant_line.ptSegDist(x3, y3);
		double        determinant_length   = DataMapper.getLength(determinant_line);
		// Find out if there's a bisecting line.
		Line2D.Double endpoint_line1 = new Line2D.Double(x1, y1, x3, y3);
		Line2D.Double endpoint_line2 = new Line2D.Double(x2, y2, x3, y3);
		double length1 = DataMapper.getLength(endpoint_line1);
		double length2 = DataMapper.getLength(endpoint_line2);
		if(determinant_distance == length1 || determinant_distance == length2) // No biscecting line, return one of the endpoint values.
		{
			if(determinant_distance == length1)
			{
				Point2D.Double bisecting_point = new  Point2D.Double(x1, y1);
				return(bisecting_point);
			}
			else
			{
				Point2D.Double bisecting_point = new  Point2D.Double(x2, y2);
				return(bisecting_point);  
			}
		}

		if(x1 == x2)
		{
			Point2D.Double bisecting_point = new  Point2D.Double(x1, y3);
			return(bisecting_point);    
		}
		else if(y1 == y2)
		{
			Point2D.Double bisecting_point = new  Point2D.Double(x3, y1);
			return(bisecting_point);  	
		}
		else
		{
				// We know the slope is neither 0 nor infinity.
				double        determinant_slope = Math.abs(DataMapper.getSlope(determinant_line));          //B  
				System.out.println("Determinant slope in radians is " + determinant_slope);
				
				// Get degrees of significant angles and apply the law of sines.
			    double        first_degrees        = DataMapper.getDegrees(determinant_slope);                 //a
			    
			    System.out.println("Determinant slope in degrees is " + first_degrees );
				
			    Line2D.Double reference_line    = new Line2D.Double(x1, y1, x3, y3);
			    double        reference_length  = DataMapper.getLength(reference_line);
			    double        second_degrees    = 0.0;   
			    if(x1 == x3)
			    {
			    	second_degrees = 90.;
			    }
			    else if(y1 == y3)
			    {
			    	second_degrees = 0;
			    }
			    else
			    {
			    	double reference_slope = Math.abs(DataMapper.getSlope(reference_line));	
			    	second_degrees         = DataMapper.getDegrees(reference_slope);                   //b
			    	System.out.println("Reference slope is " + second_degrees + " degrees.");
			    }
			    double        third_degrees        = 0;                                                        //c
			    if(first_degrees < second_degrees)
			       third_degrees = second_degrees - first_degrees;
			    else
				   third_degrees = first_degrees - second_degrees;
			    double fourth_degrees =  90 - third_degrees;                                                      //d    
			    double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
			    
			    double determinant    = DataMapper.sin(90) / segment_length;
			    double fifth_degrees  = 90 - first_degrees;
			    double delta_x        = DataMapper.sin(fifth_degrees) / determinant;
			    double delta_y        = DataMapper.sin(first_degrees) / determinant;  
			    double x4 = 0.;
			    double y4 = 0.;
			    if(x1 < x2)
			    	x4 = x1 + delta_x;
			    else
			    	x4 = x1 - delta_x;
			    if(y1 < y2)
			    	y4 = y1 + delta_y;
			    else
			    	x4 = y1 - delta_y;	 
			    Point2D.Double bisecting_point = new  Point2D.Double(x4, y4);
				return(bisecting_point); 
		}
	}
		
	public static Point[]  getOrderedPositionList(int xdimension, int ydimension, int direction)
	{
		int number_of_positions = xdimension * ydimension;
		Point[] position_list = new Point[number_of_positions];
		
		int diagonal_type;
		int even = 0;
		int odd  = 1;
		
		int close_index, column_index, row_index;
		int small_index, large_index;
		int current_index = 0;
		
		int dimension;
		if(xdimension < ydimension)
			dimension = xdimension;
		else
			dimension = ydimension;
		
		row_index = column_index = 0;
		if(direction == 1 || direction == 3)
		    position_list[current_index++] = new Point(column_index, row_index);
		else
		{
			int reverse_index = ydimension - row_index - 1;
			position_list[current_index++] = new Point(column_index, reverse_index);
		}
			
		
		column_index++;
		//position_list[current_index++] = new Point(column_index, row_index);
		if(direction == 1 || direction == 3)
		    position_list[current_index++] = new Point(column_index, row_index);
		else
		{
			int reverse_index = ydimension - row_index - 1;
			position_list[current_index++] = new Point(column_index, reverse_index);
		}
		
		row_index++;
		column_index--;
		//position_list[current_index++] = new Point(column_index, row_index);
		if(direction == 1 || direction == 3)
		    position_list[current_index++] = new Point(column_index, row_index);
		else
		{
			int reverse_index = ydimension - row_index - 1;
			position_list[current_index++] = new Point(column_index, reverse_index);
		}
		
		close_index = 1;
		diagonal_type = odd;
		while(close_index < dimension)
		{
		    if(diagonal_type == even)
		    {
		    	column_index = close_index;
		    	row_index    = close_index - 1;
		    	//position_list[current_index++] = new Point(column_index, row_index);
		    	if(direction == 1 || direction == 3)
				    position_list[current_index++] = new Point(column_index, row_index);
				else
				{
					int reverse_index = ydimension - row_index - 1;
					position_list[current_index++] = new Point(column_index, reverse_index);
				}
		    	
				column_index = close_index - 1;
				row_index    = close_index;
				//position_list[current_index++] = new Point(column_index, row_index);
				if(direction == 1 || direction == 3)
				    position_list[current_index++] = new Point(column_index, row_index);
				else
				{
					int reverse_index = ydimension - row_index - 1;
					position_list[current_index++] = new Point(column_index, reverse_index);
				}
				
				small_index = close_index - 2;
				large_index = close_index + 1;
				
				while(small_index >= 0 && large_index < dimension)
				{ 
					column_index = large_index;
				    row_index    = small_index;
				    //position_list[current_index++] = new Point(column_index, row_index);
				    if(direction == 1 || direction == 3)
					    position_list[current_index++] = new Point(column_index, row_index);
					else
					{
						int reverse_index = ydimension - row_index - 1;
						position_list[current_index++] = new Point(column_index, reverse_index);
					}
					
					column_index = small_index;
					row_index    = large_index;
					//position_list[current_index++] = new Point(column_index, row_index);
					if(direction == 1 || direction == 3)
					    position_list[current_index++] = new Point(column_index, row_index);
					else
					{
						int reverse_index = ydimension - row_index - 1;
						position_list[current_index++] = new Point(column_index, reverse_index);
					}
					
					small_index--;
					large_index++;
				}
				
				if(xdimension > ydimension)
				{
					while(small_index >= 0 && large_index < xdimension)
					{
						column_index = large_index;
					    row_index    = small_index;
					    //position_list[current_index++] = new Point(column_index, row_index);
					    if(direction == 1 || direction == 3)
						    position_list[current_index++] = new Point(column_index, row_index);
						else
						{
							int reverse_index = ydimension - row_index - 1;
							position_list[current_index++] = new Point(column_index, reverse_index);
						}
						
						small_index--;
						large_index++;
					}
				}
				else if(ydimension > xdimension)
				{
					while(small_index >= 0 && large_index < ydimension)
					{
						column_index = small_index;
					    row_index    = large_index;
					    //position_list[current_index++] = new Point(column_index, row_index);
					    if(direction == 1 || direction == 3)
						    position_list[current_index++] = new Point(column_index, row_index);
						else
						{
							int reverse_index = ydimension - row_index - 1;
							position_list[current_index++] = new Point(column_index, reverse_index);
						}
						
						small_index--;
						large_index++;
					}	
				}
				
		    	diagonal_type = odd;
		    }
		    else
		    {
		    	row_index = column_index = close_index;
		    	//position_list[current_index++] = new Point(column_index, row_index);
		    	if(direction == 1 || direction == 3)
				    position_list[current_index++] = new Point(column_index, row_index);
				else
				{
					int reverse_index = ydimension - row_index - 1;
					position_list[current_index++] = new Point(column_index, reverse_index);
				}
		    		
		    	small_index = close_index - 1;
		    	large_index = close_index + 1;
		    	while(small_index >= 0 && large_index < dimension)
				{
				    column_index = large_index;
				    row_index    = small_index;
				    //position_list[current_index++] = new Point(column_index, row_index);
				    if(direction == 1 || direction == 3)
					    position_list[current_index++] = new Point(column_index, row_index);
					else
					{
						int reverse_index = ydimension - row_index - 1;
						position_list[current_index++] = new Point(column_index, reverse_index);
					}
					
					column_index = small_index;
					row_index    = large_index;
					//position_list[current_index++] = new Point(column_index, row_index);
					if(direction == 1 || direction == 3)
					    position_list[current_index++] = new Point(column_index, row_index);
					else
					{
						int reverse_index = ydimension - row_index - 1;
						position_list[current_index++] = new Point(column_index, reverse_index);
					}
					
					small_index--;
					large_index++;
				} 	
		    	
		    	if(xdimension > ydimension)
				{
					while(small_index >= 0 && large_index < xdimension)
					{
						column_index = large_index;
					    row_index    = small_index;
					    //position_list[current_index++] = new Point(column_index, row_index);
					    if(direction == 1 || direction == 3)
						    position_list[current_index++] = new Point(column_index, row_index);
						else
						{
							int reverse_index = ydimension - row_index - 1;
							position_list[current_index++] = new Point(column_index, reverse_index);
						}
						
						small_index--;
						large_index++;
					}
				}
		    	else if(ydimension > xdimension)
				{
					while(small_index >= 0 && large_index < ydimension)
					{
						column_index = small_index;
					    row_index    = large_index;
					    //position_list[current_index++] = new Point(column_index, row_index);
					    if(direction == 1 || direction == 3)
						    position_list[current_index++] = new Point(column_index, row_index);
						else
						{
							int reverse_index = ydimension - row_index - 1;
							position_list[current_index++] = new Point(column_index, reverse_index);
						}
						
						small_index--;
						large_index++;
					}	
				}
		    	
		    	diagonal_type = even;
		    	close_index++;
		    }
		}
		
		if(xdimension > ydimension)
		{
			while(close_index < xdimension)
			{
				row_index = ydimension - 1;
				column_index = close_index;
				//position_list[current_index++] = new Point(column_index, row_index);
				if(direction == 1 || direction == 3)
				    position_list[current_index++] = new Point(column_index, row_index);
				else
				{
					int reverse_index = ydimension - row_index - 1;
					position_list[current_index++] = new Point(column_index, reverse_index);
				}
				column_index++;
				row_index--;
				while(column_index < xdimension  && row_index >= 0)
				{
					//position_list[current_index++] = new Point(column_index, row_index);
					if(direction == 1 || direction == 3)
					    position_list[current_index++] = new Point(column_index, row_index);
					else
					{
						int reverse_index = ydimension - row_index - 1;
						position_list[current_index++] = new Point(column_index, reverse_index);
					}
					column_index++;
					row_index--;
				}
				close_index++;
			}
		}
		else if(ydimension > xdimension)
		{
			while(close_index < ydimension)
			{
				row_index = close_index;
				column_index = xdimension - 1;
				//position_list[current_index++] = new Point(column_index, row_index);
				if(direction == 1 || direction == 3)
				    position_list[current_index++] = new Point(column_index, row_index);
				else
				{
					int reverse_index = ydimension - row_index - 1;
					position_list[current_index++] = new Point(column_index, reverse_index);
				}
				column_index--;
				row_index++;
				while(row_index < ydimension  && column_index >= 0)
				{
					//position_list[current_index++] = new Point(column_index, row_index);
					if(direction == 1 || direction == 3)
					    position_list[current_index++] = new Point(column_index, row_index);
					else
					{
						int reverse_index = ydimension - row_index - 1;
						position_list[current_index++] = new Point(column_index, reverse_index);
					}
					column_index--;
					row_index++;
				}
				close_index++;
			}
		}
		if(direction == 1 || direction == 4)
		    return(position_list);
		else if(direction == 2 || direction == 3)
		{
			Point[] reverse_position_list = new Point[number_of_positions]; 
			for(int i = 0; i < number_of_positions; i++)
			{
				int j                    = number_of_positions - i - 1;
				double x                 = position_list[i].getX();
				double y                 = position_list[i].getY();
				reverse_position_list[j] = new Point((int)x, (int) y);
			}
			return(reverse_position_list);
		}
		else
			return(position_list);
	}


	
	public static ArrayList getNeighborCellList(int xindex, int yindex, int xdim, int ydim, ArrayList neighbor_array_list)
	{
		//Dont really need ydim but we'll leave it in there.
		int data_index = yindex * xdim + xindex;
		ArrayList neighbor_data = (ArrayList) neighbor_array_list.get(data_index);
        return(neighbor_data);
	}
	
	// The cell list is a list of eight sample lists.
	public static ArrayList getNeighborList(int xindex, int yindex, int xdim, int ydim, ArrayList close_data_array, boolean isPopulated[][])
	{
		ArrayList neighbor_list = new ArrayList(); 
		int       location_type = getLocationType(xindex, yindex, xdim, ydim);
		
		//Let's handle the main case first.
		if(location_type == 5)
		{
		    int northwest_index =  (yindex - 1) * xdim + xindex - 1;
		    int i               =  northwest_index / xdim;
		    int j               =  northwest_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(northwest_index);
		        neighbor_list.add(current_list);  //1
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    
		    int north_index =  (yindex - 1) * xdim + xindex;
		    i               =  north_index / xdim;
		    j               =  north_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(north_index);
		        neighbor_list.add(current_list);//2
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    
		    int northeast_index =  (yindex - 1) * xdim + xindex + 1;
		    i                   =  northeast_index / xdim;
		    j                   =  northeast_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(northeast_index);
		        neighbor_list.add(current_list);//3
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    
		    int west_index  =  yindex * xdim + xindex - 1;
		    i               =  west_index / xdim;
		    j               =  west_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(west_index);
		        neighbor_list.add(current_list);//4
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    
		    int east_index  =  yindex * xdim + xindex + 1;
		    i               =  east_index / xdim;
		    j               =  east_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(east_index);
		        neighbor_list.add(current_list);//5
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    int southwest_index =  (yindex + 1) * xdim + xindex - 1;
		    i                   =  southwest_index / xdim;
		    j                   =  southwest_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(southwest_index);
		        neighbor_list.add(current_list);//6
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    
		    int south_index =  (yindex + 1) * xdim + xindex;
		    i               =  south_index / xdim;
		    j               =  south_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(south_index);
		        neighbor_list.add(current_list);//7
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		       
		    int southeast_index =  (yindex + 1) * xdim + xindex + 1;
		    i                   =  southeast_index / xdim;
		    j                   =  southeast_index % xdim;
		    if(isPopulated[i][j])
		    { 
		    	ArrayList current_list = (ArrayList)close_data_array.get(southeast_index);
		        neighbor_list.add(current_list);//8
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		}
		else if(location_type == 1)
		{
			//Let's go ahead and add empty lists so we have 8 lists later.
		    //The position information is now implicit in the order.
		    //The first four lists--NW, N, NE, W--are empty.
		    for(int m = 0; m < 4; m++)
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);	//4
		    }
			
			int east_index      =  1;
		    int i               =  0;
		    int j               =  1;
		    if(isPopulated[i][j])
		    {
			    ArrayList current_list = (ArrayList)close_data_array.get(east_index);
			    neighbor_list.add(current_list); //5
			}
			else
			{
			    ArrayList empty_list = new ArrayList();
			    neighbor_list.add(empty_list);
			}
		    
		    //SW is empty
		    ArrayList empty_list = new ArrayList();
	    	neighbor_list.add(empty_list); //6
		    
		    int south_index =  (yindex + 1) * xdim + xindex;
		    i               =  south_index / xdim;
		    j               =  south_index % xdim;
		    if(isPopulated[i][j])
		    { 
		    	ArrayList current_list = (ArrayList)close_data_array.get(south_index);
		        neighbor_list.add(current_list); //7	
		    }
		    else
			{
			    empty_list = new ArrayList();
			    neighbor_list.add(empty_list);
			}
		    
		    int southeast_index =  (yindex + 1) * xdim + xindex + 1;
		    i                   =  southeast_index / xdim;
		    j                   =  southeast_index % xdim;
		    if(isPopulated[i][j])
		    { 
		    	ArrayList current_list = (ArrayList)close_data_array.get(southeast_index);
		        neighbor_list.add(current_list);	//8
		    }
		    else
			{
			    empty_list = new ArrayList();
			    neighbor_list.add(empty_list);
			}
		    
		}
		else if(location_type == 2)
		{
			//First three lists--NW, N, NE--are empty;
			for(int m = 0; m < 3; m++)
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);	//3
		    }
			
			int west_index  =  xindex - 1;
		    int i           =  0;
		    int j           =  west_index;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(west_index); 
		        neighbor_list.add(current_list); //4
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    
		    int east_index  =  xindex + 1;
		    i               =  0;
		    j               =  east_index;
		    if(isPopulated[i][j])
		    {
			    ArrayList current_list = (ArrayList)close_data_array.get(east_index);
			    neighbor_list.add(current_list); //5
			}
			else
			{
			    ArrayList empty_list = new ArrayList();
			    neighbor_list.add(empty_list);
			}
		    
		    int southwest_index =  xdim + xindex - 1;
		    i                   =  southwest_index / xdim;
		    j                   =  southwest_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(southwest_index);
		        neighbor_list.add(current_list);  //6
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    

		    int south_index =  xdim + xindex;
		    i               =  south_index / xdim;
		    j               =  south_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(south_index);
		        neighbor_list.add(current_list); //7
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    
		    int southeast_index =  (yindex + 1) * xdim + xindex + 1;
		    i                   =  southeast_index / xdim;
		    j                   =  southeast_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(southeast_index);
		        neighbor_list.add(current_list); //8
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }

		}
		else if(location_type == 3)
		{ 
			//First three lists--NW, N, NE--are empty;
			for(int m = 0; m < 3; m++)
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);	//3
		    }
		    	
			int west_index      =  xindex - 1;
		    int i               =  0;
		    int j               =  west_index;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(west_index);
		        neighbor_list.add(current_list);  //4
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    
		    //E is empty;
		    ArrayList empty_list = new ArrayList();
	    	neighbor_list.add(empty_list); //5
		    
	    	int southwest_index =  xdim + xindex - 1;
		    i                   =  1;
		    j                   =  southwest_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(southwest_index);
		        neighbor_list.add(current_list); //6
		    }
		    else
		    {
		    	empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
	    
		    int south_index =  xdim + xindex;
		    i               =  1;
		    j               =  xindex;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(south_index);
		        neighbor_list.add(current_list);  //7
		    }
		    else
		    {
		    	empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		       
		    //SE is empty.
		    empty_list = new ArrayList();
	    	neighbor_list.add(empty_list); //8
		}
		else if(location_type == 4)
		{
			//NW is empty
			ArrayList empty_list = new ArrayList();
	    	neighbor_list.add(empty_list); //1
			
			int north_index     =  (yindex - 1) * xdim + xindex;
		    int i               =  north_index / xdim;
		    int j               =  north_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(north_index);
		        neighbor_list.add(current_list);//2
		    }
		    else
		    {
		    	empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    
		    int northeast_index =  (yindex - 1) * xdim + xindex + 1;
		    i                   =  northeast_index / xdim;
		    j                   =  northeast_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(northeast_index);
		        neighbor_list.add(current_list);//3
		    }
		    else
		    {
		    	empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    
		    //W is empty
		    empty_list = new ArrayList();
	    	neighbor_list.add(empty_list);//4
		    	
			int east_index  =  yindex * xdim + xindex + 1;
		    i               =  east_index / xdim;
		    j               =  east_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(east_index);
		        neighbor_list.add(current_list);//5
		    }
		    else
		    {
		    	empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    
		    //SW is empty
		    empty_list = new ArrayList();
	    	neighbor_list.add(empty_list);//6
		    
		    int south_index =  (yindex + 1) * xdim + xindex;
		    i               =  south_index / xdim;
		    j               =  south_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(south_index);
		        neighbor_list.add(current_list);//7
		    }
		    else
		    {
		    	empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		       
		    int southeast_index =  (yindex + 1) * xdim + xindex + 1;
		    i                   =  southeast_index / xdim;
		    j                   =  southeast_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(southeast_index);
		        neighbor_list.add(current_list);//8
		    }
		    else
		    {
		    	empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }   	
		}
		else if(location_type == 6)
		{
			int northwest_index =  (yindex - 1) * xdim + xindex - 1;
		    int i               =  northwest_index / xdim;
		    int j               =  northwest_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(northwest_index);
		        neighbor_list.add(current_list);//1
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }  
		    
		    int north_index =  (yindex - 1) * xdim + xindex;
		    i               =  north_index / xdim;
		    j               =  north_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(north_index);
		        neighbor_list.add(current_list);//2
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    } 
		    
		    //NE is empty
		    ArrayList empty_list = new ArrayList();
		    neighbor_list.add(empty_list);//3
		    
		    int west_index  =  yindex * xdim + xindex - 1;
		    i               =  west_index / xdim;
		    j               =  west_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(west_index);
		        neighbor_list.add(current_list);//4
		    }
		    else
		    {
		    	empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    
		    //E is empty
		    empty_list = new ArrayList();
		    neighbor_list.add(empty_list);//5
		    
		    int southwest_index =  (yindex + 1) * xdim + xindex - 1;
		    i                   =  southwest_index / xdim;
		    j                   =  southwest_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(southwest_index);
		        neighbor_list.add(current_list);//6
		    }
		    else
		    {
		    	empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    
		    int south_index =  (yindex + 1) * xdim + xindex;
		    i               =  south_index / xdim;
		    j               =  south_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(south_index);
		        neighbor_list.add(current_list);//7
		    }
		    else
		    {
		    	empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    
		    //SE is empty
		    empty_list = new ArrayList();
	    	neighbor_list.add(empty_list);//8
		}
		else if(location_type == 7)
		{
			//NW is empty
			ArrayList empty_list = new ArrayList();
	    	neighbor_list.add(empty_list);//1
	    	
		    int north_index     =  (yindex - 1) * xdim + xindex;
		    int i               =  north_index / xdim;
		    int j               =  north_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(north_index);
		        neighbor_list.add(current_list);//2
		    }
		    else
		    {
		    	empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    
		    int northeast_index =  (yindex - 1) * xdim + xindex + 1;
		    i                   =  northeast_index / xdim;
		    j                   =  northeast_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(northeast_index);
		        neighbor_list.add(current_list);//3
		    }
		    else
		    {
		    	empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    
		    //W is empty
		    empty_list = new ArrayList();
	    	neighbor_list.add(empty_list);//4
	    	
	    	int east_index  =  yindex * xdim + xindex + 1;
		    i               =  east_index / xdim;
		    j               =  east_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(east_index);
		        neighbor_list.add(current_list);//5
		    }
		    else
		    {
		    	empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    //Next three--SW, S, SE--are empty
		    for(int m = 0; m < 3; m++)
		    {
		    	empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);//8	
		    }
		    
		}
		else if(location_type == 8)
		{
			int northwest_index =  (yindex - 1) * xdim + xindex - 1;
		    int i               =  northwest_index / xdim;
		    int j               =  northwest_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(northwest_index);
		        neighbor_list.add(current_list);//1
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    
		    int north_index =  (yindex - 1) * xdim + xindex;
		    i               =  north_index / xdim;
		    j               =  north_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(north_index);
		        neighbor_list.add(current_list);//2
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    
		    int northeast_index =  (yindex - 1) * xdim + xindex + 1;
		    i                   =  northeast_index / xdim;
		    j                   =  northeast_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(northeast_index);
		        neighbor_list.add(current_list);//3
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		      
		    int west_index  =  yindex * xdim + xindex - 1;
		    i               =  west_index / xdim;
		    j               =  west_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(west_index);
		        neighbor_list.add(current_list);//4
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		      
		    int east_index  =  yindex * xdim + xindex + 1;
		    i               =  east_index / xdim;
		    j               =  east_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(east_index);
		        neighbor_list.add(current_list);//5
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    //Next three--SW, S, SE--are empty
		    for(int m = 0; m < 3; m++)
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);	//8
		    }
		}
		else if(location_type == 9)
		{
			int northwest_index =  (yindex - 1) * xdim + xindex - 1;
		    int i               =  northwest_index / xdim;
		    int j               =  northwest_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(northwest_index);
		        neighbor_list.add(current_list);  //1
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    
		    int north_index =  (yindex - 1) * xdim + xindex;
		    i               =  north_index / xdim;
		    j               =  north_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(north_index);
		        neighbor_list.add(current_list);//2
		    }
		    else
		    {
		    	ArrayList empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    
		    //NE is empty
		    ArrayList empty_list = new ArrayList();
	    	neighbor_list.add(empty_list);//3	
	    	
		    int west_index  =  yindex * xdim + xindex - 1;
		    i               =  west_index / xdim;
		    j               =  west_index % xdim;
		    if(isPopulated[i][j])
		    { 
		        ArrayList current_list = (ArrayList)close_data_array.get(west_index);
		        neighbor_list.add(current_list);//4
		    }
		    else
		    {
		    	empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);
		    }
		    //Next four--E, SW, S, SE--are empty
		    for(int m = 0; m < 4; m++)
		    {
		    	empty_list = new ArrayList();
		    	neighbor_list.add(empty_list);	//8
		    }
		}
	    return(neighbor_list);
	}	
}