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

	public static double cos(double degrees)
	{
	    double radians = StrictMath.toRadians(degrees);
	    double sin     = StrictMath.cos(radians);
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
	
	public static double getSlopeRadians(Line2D.Double line)
	{
		double x1 = line.getX1();
	    double y1 = line.getY1();
	    double x2 = line.getX2();
	    double y2 = line.getY2();  
	    double slope;
	    
	    if(y1 == y2)
	    	return(0);
	    else if(x1 == x2)
	    	return(Double.NaN);
	    else
	    {
	    	double rise = y2 - y1;
	    	double run  = x2 - x1;
	    	
	    	double radians       = Math.atan2(rise, run);
	    	return(radians);
	    }
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
	
	
	public static Point2D.Double getIntersection(Point2D.Double upper_left, Point2D.Double upper_right, Point2D.Double lower_right, Point2D.Double lower_left)
	{
		Line2D.Double base     = new Line2D.Double(lower_left,  lower_right);
		
		Line2D.Double diagonal1 = new Line2D.Double(lower_left,  upper_right);
		
		Line2D.Double diagonal2 = new Line2D.Double(upper_left,  lower_right);
		
		double base_radians     = getSlopeRadians(base);
		double diagonal1_radians = getSlopeRadians(diagonal1);
		double diagonal2_radians = getSlopeRadians(diagonal2);
		
		double base_degrees     = getDegrees(base_radians);
		double diagonal1_degrees = getDegrees(diagonal1_radians);
		double diagonal2_degrees = getDegrees(diagonal2_radians);
		
		double a_degrees = diagonal1_degrees - base_degrees;
		double b_degrees = -diagonal2_degrees + base_degrees;
		double c_degrees = 180 - (a_degrees + b_degrees);
		
		double base_length     = getLength(base);
		double determinant_length = sin(b_degrees) / sin(c_degrees) * base_length;
		double delta_x = determinant_length * cos(a_degrees);
		double delta_y = determinant_length * sin(a_degrees);
		
		double x1 = lower_left.getX();
		double y1 = lower_left.getY();
		double x2 = x1 + delta_x;
		double y2 = y1 + delta_y;
		
		Point2D.Double intersect_point  = new Point2D.Double(x2, y2);
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
		
		
	    double area1 = getTriangleArea(upper_left, lower_left, upper_right);
	    double area2 = getTriangleArea(lower_left, lower_right, upper_right);
	    double area = area1 + area2;
	    return(area);
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
    
	public static double getLinearExtrapolation(Point2D.Double point, Sample interior, Sample corner1, Sample corner2)
    {
		double x1 = point.x;
    	double y1 = point.y; 
    	
    	double x2 = corner1.x;
    	double y2 = corner1.y;
    	
    	double x3 = corner2.x;
    	double y3 = corner2.y;  
    	
    	Point2D.Double interior_point = new Point2D.Double(interior.x, interior.y); 
    	
		Point2D.Double base1  = new Point2D.Double(x1, y1);
        Point2D.Double top    = new Point2D.Double(x2, y2);
        Point2D.Double base2  = new Point2D.Double(x3, y3); 
        
        double area1 = DataMapper.getTriangleArea(interior_point, base2, top);
        double area2 = DataMapper.getTriangleArea(base1, base2, interior_point);
        double area3 = DataMapper.getTriangleArea(base1, interior_point, top);
        
        double total_area = area1 + area2 + area3;
        double weight1    = area1 / total_area;
        double weight2    = area2 / total_area;
        double weight3    = area3 / total_area;
        
        double value = (total_area * interior.intensity - (corner1.intensity * area2 + corner2.intensity * area3)) / area1;
        return(value);    	
    }
	
	
	public static double getLinearInterpolation(Point2D.Double point, Sample sample1, Sample sample2, Sample sample3)
    {
		double x1 = sample1.x;
    	double y1 = sample1.y; 
    	
    	double x2 = sample2.x;
    	double y2 = sample2.y;
    	
    	double x3 = sample3.x;
    	double y3 = sample3.y;  
    	
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
        
        double value = sample1.intensity * weight1 + 
        		       sample2.intensity * weight2 + 
        		       sample3.intensity * weight3;	
        return(value);    	
    }
	
	public static double getLinearInterpolation(Point2D.Double point, Sample sample1, Sample sample2, Sample sample3, Sample sample4)
    {
    	double x1 = sample1.x;
	    double y1 = sample1.y; 
	    	
	    double x2 = sample2.x;
	    double y2 = sample2.y;
	    	
	    double x3 = sample3.x;
	    double y3 = sample3.y;
	    	
	    double x4 = sample4.x;
	    double y4 = sample4.y;
	    	
	    Point2D.Double upper_left    = new Point2D.Double(x1, y1);
	    Point2D.Double upper_right   = new Point2D.Double(x2, y2);
	    Point2D.Double lower_right   = new Point2D.Double(x3, y3);
	    Point2D.Double lower_left    = new Point2D.Double(x4, y4);  
	    	    		          
	    Line2D.Double top            = new Line2D.Double(upper_left, upper_right);
	    Line2D.Double left           = new Line2D.Double(upper_left, lower_left);
	    Line2D.Double bottom         = new Line2D.Double(lower_left, lower_right);
	    Line2D.Double right          = new Line2D.Double(lower_right, upper_right);
	    	 	
	    Point2D.Double middle_top    =  getBisectingPoint(x1, y1, x2, y2, point);
	    Point2D.Double middle_left   =  getBisectingPoint(x4, y4, x1, y1, point);
	    Point2D.Double middle_right  =  getBisectingPoint(x3, y3, x2, y2, point);
	    Point2D.Double middle_bottom =  getBisectingPoint(x4, y4, x2, y2, point);
	    	
	    // We need to get four more points to do our bilinear interpolation.
  	    // We're using the perpendicular bisector instead of finding the point on the line orthogonal to the center.
  	    // Seems like this should produce a consistent result regardless of the orientation of the coordinate system.	 	
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
	    	    		        
	    double value = sample1.intensity * weight1 + 
	    	    	   sample2.intensity * weight2 + 
	    	    	   sample3.intensity * weight3 +
	    	    	   sample4.intensity * weight4;
	    return(value);
    }
    
    
    public static boolean areSameSide(Line2D.Double line, double x1, double y1, double x2, double y2)
    {
        double a = DataMapper.getSlope(line);
        if(Double.isNaN(a))
        {
        	//Get an endpoint and check x1 and x2.
        	double endpoint = line.getX1();
        	if((x1 < endpoint && x2 < endpoint) || (x1 > endpoint && x2 > endpoint))
        		return true;
        	else
        		return false;
        }
        else if(a == 0)
        {
        	//Get an endpoint and check y1 and y2.
        	double endpoint = line.getY1();
        	if((y1 < endpoint && y2 < endpoint) || (y1 > endpoint && y2 > endpoint))
            	return true;
            else
            	return false;
        }
        else
        {
            double b = -1;
            double x3 = line.getX1();
            double y3 = line.getY1();
            Point2D.Double endpoint = new Point2D.Double(x3, y3);
            double c = getYIntercept(endpoint, a);
            c = -c;
            double fx1 = a * x1 + b * y1 - c; 
            double fx2 = a * x2 + b * y2 - c; 
            if((fx1 * fx2) > 0) 
                return true; 
            return false;
        }
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
		
        
		Line2D.Double line     = new Line2D.Double(x1, y1, x2, y2);
		double        distance = line.ptSegDist(x3, y3);
		double        length   = DataMapper.getLength(line);
		
		// Find out if there's a bisecting line.
		Line2D.Double endpoint_line1 = new Line2D.Double(x1, y1, x3, y3);
		Line2D.Double endpoint_line2 = new Line2D.Double(x2, y2, x3, y3);
		double        length1 = DataMapper.getLength(endpoint_line1);
		double        length2 = DataMapper.getLength(endpoint_line2);
		if(distance == length1 || distance == length2) // No biscecting line, return one of the endpoint values.
		{
			if(distance == length1)
				return(sample1.intensity);
			else
				return(sample2.intensity);
		}

		if(x1 == x2)
		{
		    double segment_length = Math.abs(y2 - y3);
		    double weight1        = segment_length / length;
		    double weight2        = (length - segment_length) / length;
		    double value          = weight1 * sample1.intensity + weight2 * sample2.intensity;
		    return(value);
		    
		}
		else if(y1 == y2)
		{
			double segment_length = Math.abs(x2 - x3);
		    double weight1        = segment_length / length;
		    double weight2        = (length - segment_length) / length; 
		    double value          = weight1 * sample1.intensity + weight2 * sample2.intensity;
		    return(value);
		}
		else
		{
			// We know the slope is neither 0 nor infinity.
			double        a_radians = DataMapper.getSlopeRadians(line);       
		    double        a_degrees  = DataMapper.getDegrees(a_radians);           
			
		    Line2D.Double hypotenuse    = new Line2D.Double(x1, y1, x3, y3);
		    double        hypotenuse_length  = DataMapper.getLength(hypotenuse);
		    double        b_degrees    = 0.0;  
		    double        b_radians    = 0.0;
		    if(x1 == x3)
		    {
		    	b_degrees = 90.;
		    	b_radians = Math.PI / 2;
		    }
		    else if(y1 == y3)
		    {
		    	b_degrees = 0;
		    	b_radians = 0;
		    }
		    else
		    {
		    	b_radians = DataMapper.getSlopeRadians(hypotenuse);	
		    	b_degrees = DataMapper.getDegrees(b_radians); 
		    }
		       
		    double  c_degrees        = 0; 
		    double  c_radians        = 0;
		    if(b_degrees < a_degrees)
		    {
		       c_degrees = a_degrees - b_degrees;
		       c_radians = a_radians - b_radians;
		    }
		    else
		    {
			   c_degrees = b_degrees - a_degrees;
			   c_radians = b_radians - a_radians;
		    }
		    double d_degrees =  90 - c_degrees; 
		    double d_radians = Math.PI / 2 - c_radians; 
		    double segment_length = hypotenuse_length * Math.sin(d_radians);
		    /*
		    double weight1 = segment_length /length;
		    double weight2 = (length - segment_length) / length;
		    */
		    double weight1 = (length - segment_length) / length;
		    double weight2 = segment_length /length;
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
		
		
		double xmin = Double.MAX_VALUE;
		double ymin = Double.MAX_VALUE;
		
		if(x1 < xmin)
		{
			xmin = x1;
		}
		if(x2 < xmin)
		{
			xmin = x2;
		}
		if(x3 < xmin)
		{
			xmin = x3;
		}
		
		if(y1 < ymin)
		{
			ymin = y1;
		}
		if(y2 < ymin)
		{
			ymin = y2;
		}
		if(y3 < ymin)
		{
			ymin = y3;
		}
		
		x1 -= xmin;
		x2 -= xmin;
		x3 -= xmin;
		
		y1 -= ymin;
		y2 -= ymin;
		y3 -= ymin;
		
		Line2D.Double line     = new Line2D.Double(x1, y1, x2, y2);
		double        distance = line.ptSegDist(x3, y3);
		double        length   = DataMapper.getLength(line);
	
		Line2D.Double endpoint_line1 = new Line2D.Double(x1, y1, x3, y3);
		Line2D.Double endpoint_line2 = new Line2D.Double(x2, y2, x3, y3);
		double length1 = DataMapper.getLength(endpoint_line1);
		double length2 = DataMapper.getLength(endpoint_line2);
		if(distance == length1 || distance == length2) 
		{
			if(distance == length1)
			{
				Point2D.Double bisecting_point = new  Point2D.Double(x1 + xmin, y1 + ymin);
				return(bisecting_point);
			}
			else
			{
				Point2D.Double bisecting_point = new  Point2D.Double(x2 + xmin, y2 + ymin);
				return(bisecting_point);  
			}
		}

		if(x1 == x2)
		{
			Point2D.Double bisecting_point = new  Point2D.Double(x1 + xmin, y3 + ymin);
			return(bisecting_point);    
		}
		else if(y1 == y2)
		{
			Point2D.Double bisecting_point = new  Point2D.Double(x3 + xmin, y1 + ymin);
			return(bisecting_point);  	
		}
		else
		{
			double        a_radians = DataMapper.getSlopeRadians(line);   
			double        a_degrees        = DataMapper.getDegrees(a_radians); 	
			Line2D.Double hypotenuse = new Line2D.Double(x1, y1, x3, y3);
			double        hypotenuse_length  = DataMapper.getLength(hypotenuse);
			double        b_degrees    = 0.0; 
			double        b_radians    = 0;
			if(x1 == x3)
			{
			    b_degrees = 90.;
			    b_radians = Math.PI / 2;
			}
			else if(y1 == y3)
			{
			    b_degrees = 0;
			    b_radians = 0;
			}
			else
			{
			   b_radians = DataMapper.getSlopeRadians(hypotenuse);	
			   b_degrees = DataMapper.getDegrees(b_radians);               
			}
			   
			double  c_degrees        = 0; 
			double  c_radians        = 0;
			if(b_degrees < a_degrees)
			{
			    c_degrees = a_degrees - b_degrees;
			    c_radians = a_radians - b_radians;
			}
			else
			{
				c_degrees = b_degrees - a_degrees;
				c_radians = b_radians - a_radians;
			}
			    
			double d_degrees      =  90 - c_degrees; 
			double d_radians      = Math.PI / 2 - c_radians;    
			double segment_length = hypotenuse_length * Math.sin(d_radians);
		    double x4             = Math.cos(a_radians) * segment_length;
			double y4             = Math.sin(a_radians) * segment_length;
			x4                   += xmin;
			y4                   += ymin;
			Point2D.Double bisecting_point = new  Point2D.Double(x4, y4);
			return(bisecting_point); 
		}
	}
	
	// This returns the bisecting point from a line determined by two samples, or the nearest endpoint location if no bisecting line from the point exists.
	public static Point2D.Double getBisectingPoint(double x1, double y1, double x2, double y2, Point2D.Double point)
	{ 
		double x3 = point.getX();
		double y3 = point.getY();
				
		double xmin = Double.MAX_VALUE;
		double ymin = Double.MAX_VALUE;
			
		if(x1 < xmin)
		{
			xmin = x1;
		}
		if(x2 < xmin)
		{
			xmin = x2;
		}
		if(x3 < xmin)
		{
			xmin = x3;
		}
			
		if(y1 < ymin)
		{
			ymin = y1;
		}
		if(y2 < ymin)
		{
			ymin = y2;
		}
		if(y3 < ymin)
		{
			ymin = y3;
		}
			
		x1 -= xmin;
		x2 -= xmin;
		x3 -= xmin;
			
		y1 -= ymin;
		y2 -= ymin;
		y3 -= ymin;
			
		Line2D.Double line     = new Line2D.Double(x1, y1, x2, y2);
		double        distance = line.ptSegDist(x3, y3);
		double        length   = DataMapper.getLength(line);
		
		Line2D.Double endpoint_line1 = new Line2D.Double(x1, y1, x3, y3);
		Line2D.Double endpoint_line2 = new Line2D.Double(x2, y2, x3, y3);
		double length1 = DataMapper.getLength(endpoint_line1);
		double length2 = DataMapper.getLength(endpoint_line2);
		if(distance == length1 || distance == length2) 
		{
			if(distance == length1)
			{
				Point2D.Double bisecting_point = new  Point2D.Double(x1 + xmin, y1 + ymin);
				return(bisecting_point);
			}
			else
			{
				Point2D.Double bisecting_point = new  Point2D.Double(x2 + xmin, y2 + ymin);
				return(bisecting_point);  
			}
		}

		if(x1 == x2)
		{
			Point2D.Double bisecting_point = new  Point2D.Double(x1 + xmin, y3 + ymin);
			return(bisecting_point);    
		}
		else if(y1 == y2)
		{
			Point2D.Double bisecting_point = new  Point2D.Double(x3 + xmin, y1 + ymin);
			return(bisecting_point);  	
		}
		else
		{
			double        a_radians = DataMapper.getSlopeRadians(line);   
			double        a_degrees        = DataMapper.getDegrees(a_radians); 	
			Line2D.Double hypotenuse = new Line2D.Double(x1, y1, x3, y3);
			double        hypotenuse_length  = DataMapper.getLength(hypotenuse);
			double        b_degrees    = 0.0; 
			double        b_radians    = 0;
			if(x1 == x3)
			{
				b_degrees = 90.;
			    b_radians = Math.PI / 2;
			}
		    else if(y1 == y3)
		    {
				b_degrees = 0;
				b_radians = 0;
			}
			else
			{
				b_radians = DataMapper.getSlopeRadians(hypotenuse);	
				b_degrees = DataMapper.getDegrees(b_radians);               
			}
				   
			double  c_degrees        = 0; 
			double  c_radians        = 0;
			if(b_degrees < a_degrees)
			{
				c_degrees = a_degrees - b_degrees;
				c_radians = a_radians - b_radians;
			}
			else
			{
				c_degrees = b_degrees - a_degrees;
				c_radians = b_radians - a_radians;
			}
				    
			double d_degrees      =  90 - c_degrees; 
			double d_radians      = Math.PI / 2 - c_radians;    
			double segment_length = hypotenuse_length * Math.sin(d_radians);
			double x4             = Math.cos(a_radians) * segment_length;
			double y4             = Math.sin(a_radians) * segment_length;
			x4                   += xmin;
			y4                   += ymin;
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