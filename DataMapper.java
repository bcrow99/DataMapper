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
	
	public static double getLength(double x1, double y1, double x2, double y2)
	{
	    double length = StrictMath.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	    return(length);
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
		{
			System.out.println("Not a triangle: x-values collinear.");
		    return(0);
		}
		if((y1 == y2) && (y2 == y3))
		{
			System.out.println("Not a triangle: y-values collinear.");
		    return(0);
		}
		if((x1 == x2 && y1 == y2) || (x1 == x3 && y1 == y3) || (x1 == x3 && y1 == y3))
		{
			System.out.println("Not a triangle: duplicate points.");
		    return(0);   	
		}
		
	
		
		
		double a      = DataMapper.getDistance(x1, y1, x2, y2);
		double b      = DataMapper.getDistance(x2, y2, x3, y3);
		double c      = DataMapper.getDistance(x3, y3, x1, y1);
		double s      = (a + b + c) / 2;
		double square = s * (s - a) * (s - b) * (s - c); 
		double area   = Math.sqrt(square);
		
		
		
		// The values produced by Heron's formula and the bisecting formula are slightly different.
		// The result from Heron's formula does not
		// look like a natural number but has been well tested, to say the least.
		// The bisecting formula is subject to constraints that are probably best
		// satisfied by rotating the points to a standard orientation, not yet implemented.
		
		//The bisector is useful because it sometimes produces a non-zero result when
		// the formula goes to 0 and it might be more accurate.  
		// For now, sticking with Heron's formula and seeing if the interpolation 
		// can work.
	    
		/*
		double         width    = 0;
		double         height   = 0;
		Point2D.Double location = new Point2D.Double();
		
		// Find a point that has a perpendicular bisector.
		
		
		
		if(x1 != x2)
		{
		    if(x1 > x3 && x2 > x3)
		    {
		        if(x1 < x2)	
		        {
		        	location          = DataMapper.getBisectingPoint(x3, y3, x2, y2, base1);
			        double location_x = location.getX();
					double location_y = location.getY();
					height            = DataMapper.getDistance(x1, y1, location_x, location_y);	
					width             = DataMapper.getDistance(x2, y2, x3, y3);
		        }
		        else
		        {
		        	location          = DataMapper.getBisectingPoint(x3, y3, x1, y1, base2);
			        double location_x = location.getX();
					double location_y = location.getY();
					height            = DataMapper.getDistance(x2, y2, location_x, location_y); 
					width             = DataMapper.getDistance(x1, y1, x3, y3);
		        }
		    }
		    else if (x1 < x3 && x2 < x3)
			{
		    	if(x1 > x2)	
		        {
		    		location          = DataMapper.getBisectingPoint(x3, y3, x2, y2, base1);
		    		double location_x = location.getX();
					double location_y = location.getY();
					height            = DataMapper.getDistance(x1, y1, location_x, location_y);	
					width             = DataMapper.getDistance(x2, y2, x3, y3);
		        }
		        else
		        {
		        	location          = DataMapper.getBisectingPoint(x3, y3, x1, y1, base2);
			        double location_x = location.getX();
					double location_y = location.getY();
					height            = DataMapper.getDistance(x2, y2, location_x, location_y); 
					width             = DataMapper.getDistance(x1, y1, x3, y3);   	
		        }	
			}
		    else
		    { 
		        location          = DataMapper.getBisectingPoint(x1, y1, x2, y2, top);
		        double location_x = location.getX();
				double location_y = location.getY();
				height            = DataMapper.getDistance(x3, y3, location_x, location_y);
				width             = DataMapper.getDistance(x1, y1, x2, y2);
		    }
		    
		}
		else // y1 can't be equal to y2
		{
			if(y1 > y3 && y2 > y3)
		    {
		        if(y1 < y2)	
		        {
		        	location          = DataMapper.getBisectingPoint(x3, y3, x2, y2, base1);
		    		double location_x = location.getX();
					double location_y = location.getY();
					height            = DataMapper.getDistance(x1, y1, location_x, location_y);	
					width             = DataMapper.getDistance(x2, y2, x3, y3);	     	
		        }
		        else
		        {
		        	location          = DataMapper.getBisectingPoint(x3, y3, x1, y1, base2);
			        double location_x = location.getX();
					double location_y = location.getY();
					height            = DataMapper.getDistance(x2, y2, location_x, location_y); 
					width             = DataMapper.getDistance(x1, y1, x3, y3);   	
		        }
		    }
		    else if (y1 < y3 && y2 < y3)
			{
		    	if(y1 > y2)	
		        {
		    		location          = DataMapper.getBisectingPoint(x3, y3, x2, y2, base1);
		    		double location_x = location.getX();
					double location_y = location.getY();
					height            = DataMapper.getDistance(x1, y1, location_x, location_y);	
					width             = DataMapper.getDistance(x2, y2, x3, y3);		 	  	
		        }
		        else
		        {
		        	location          = DataMapper.getBisectingPoint(x3, y3, x1, y1, base2);
			        double location_x = location.getX();
					double location_y = location.getY();
					height            = DataMapper.getDistance(x2, y2, location_x, location_y); 
					width             = DataMapper.getDistance(x1, y1, x3, y3);   	
		        }	
			}
		    else
		    { 
		    	location          = DataMapper.getBisectingPoint(x1, y1, x2, y2, top);
		        double location_x = location.getX();
				double location_y = location.getY();
				height            = DataMapper.getDistance(x3, y3, location_x, location_y);
				width             = DataMapper.getDistance(x1, y1, x2, y2);	  	
		    }	
		}
		
		//System.out.println("Height is " + height);
		//System.out.println("Width is " + width);
		double area = height * width / 2;
		//System.out.println("The value produced using the bisecting location is " + area);
		
		if(area == 0)
		{
			System.out.println("Calculated a zero area.");
			System.out.println("x1 = " + x1 + ", y1 = " + y1);
			System.out.println("x2 = " + x2 + ", y2 = " + y2);
			System.out.println("x3 = " + x3 + ", y3 = " + y3);
			System.out.println();
		}
		if(Double.isNaN(area))
		{
			System.out.println("Result from bisecting location was not a number.");
		}
		*/
		
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
	    System.out.println("Top triangle area is " + area1);
	    System.out.println("Base 1 = " + upper_left + ", base 2 = " + lower_left + ", top = " + upper_right);
	    double area2 = getTriangleArea(upper_right, lower_right, lower_left);
	    System.out.println("Bottom triangle area is " + area2);
	    System.out.println("Base 1 = " + upper_right + ", base 2 = " + lower_right + ", top = " + lower_left);
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
    	
    	Point2D.Double top    = new Point2D.Double(x1, y2);
		Point2D.Double base1  = new Point2D.Double(x2, y2);
        Point2D.Double base2  = new Point2D.Double(x3, y3); 
        
        double area1 = DataMapper.getTriangleArea(interior_point, base1, base2);
        double area2 = DataMapper.getTriangleArea(top, interior_point, base2);
        double area3 = DataMapper.getTriangleArea(top, interior_point, base1);
        
        double total_area = area1 + area2 + area3;
        double weight1    = area1 / total_area;
        double weight2    = area2 / total_area;
        double weight3    = area3 / total_area;
        
        if(area1 == 0  || Double.isNaN(area1))
        {
        	System.out.println("Area 1 is not a triangle.");
        	System.out.println("Interior x = " + String.format("%.2f", interior.x) + ", y = " + String.format("%.2f", interior.y));
        	System.out.println("Corner 1 x = " + String.format("%.2f", x2) + ", y = " + String.format("%.2f", y2));
        	System.out.println("Corner 2 x = " + String.format("%.2f", x3) + ", y = " + String.format("%.2f", y3));
        	return(Double.NaN);
        }
        
        if(area2 == 0 || Double.isNaN(area2))
        {
        	System.out.println("Area 2 is not a triangle.");
        	System.out.println("Top x = " + String.format("%.2f", x1) + ", y = " + String.format("%.2f", y1));
        	System.out.println("Interior x = " + String.format("%.2f", interior.x) + ", y = " + String.format("%.2f", interior.y));
        	System.out.println("Corner 2 x = " + String.format("%.2f", x3) + ", y = " + String.format("%.2f", y3));
        	return(Double.NaN);
        }
        
        if(area3 == 0 || Double.isNaN(area3))
        {
        	System.out.println("Area 3 is not a triangle.");
        	System.out.println("Top x = " + String.format("%.2f", x2) + ", y = " + String.format("%.2f", y2));
        	System.out.println("Interior x = " + String.format("%.2f", interior.x) + ", y = " + String.format("%.2f", interior.y));
        	System.out.println("Corner 1 x = " + String.format("%.2f", x2) + ", y = " + String.format("%.2f", y2));
        	return(Double.NaN);
        }
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
        
        if(area1 == 0  || Double.isNaN(area1))
        {
        	System.out.println("Area 1 is 0.");
        	return(Double.NaN);
        }
        
        if(area2 == 0 || Double.isNaN(area2))
        {
        	System.out.println("Area 2 is 0.");
        	return(Double.NaN);
        }
        
        if(area3 == 0 || Double.isNaN(area3))
        {
        	System.out.println("Area 3 is 0.");
        	return(Double.NaN);
        }
        
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
	    	 	
	    boolean area1IsTriangle = false;
	    boolean area2IsTriangle = false;
	    boolean area3IsTriangle = false;
	    boolean area4IsTriangle = false;
	    
	    Point2D.Double middle_top    =  getBisectingPoint(x1, y1, x2, y2, point);
	    
	    double x = middle_top.getX();
	    double y = middle_top.getY(); 
	    if((x == x1 && y == y1) || (x == x2 && y == y2))
	    {
	    	//System.out.println("Bisecting point for top is actually an endpoint.");
	    	if(x == x1 && y == y1)
	    	{
	    		area1IsTriangle = true;
	    	}
	    	else
	    	{
	    		area2IsTriangle = true;	
	    	}
	    }
	    
	    Point2D.Double middle_left   =  getBisectingPoint(x4, y4, x1, y1, point);
	    x = middle_left.getX();
	    y = middle_left.getY(); 
	    
	    if((x == x1 && y == y1) || (x == x4 && y == y4))
	    {
	    	System.out.println("Bisecting point for left is actually an endpoint.");
	    	System.out.println("x1 = " + x4 + ", y1 = " + y4 + ", x2 = " + x1 + ", y2 = " + y1);
	   
	    	x = point.getX();
	    	y = point.getY();
	    	System.out.println("x = " + x + ", y = " + y);
	    	System.out.println();
	    	if(x == x1 && y == y1)
	    	    area1IsTriangle = true;
	    	else
	    	    area4IsTriangle = true;
	    }
	    
	    Point2D.Double middle_right  =  getBisectingPoint(x3, y3, x2, y2, point);
	    x = middle_right.getX();
	    y = middle_right.getY(); 
	    if((x == x2 && y == y2) || (x == x3 && y == y3))
	    {
	    	//System.out.println("Bisecting point for right is actually an endpoint.");
	    	if(x == x2 && y == y2)
	    	    area2IsTriangle = true;
	    	else
	    		area3IsTriangle = true;
	    }
	    
	    Point2D.Double middle_bottom =  getBisectingPoint(x4, y4, x3, y3, point);
	    x = middle_bottom.getX();
	    y = middle_bottom.getY(); 
	    if((x == x2 && y == y2) || (x == x4 && y == y4))
	    {
	    	//System.out.println("Bisecting point for bottom is actually an endpoint.");
	    	if(x == x2 && y == y2)
	    	    area2IsTriangle = true;
	    	else
	    		area4IsTriangle = true;
	    }
	    
	    	
	    double area1 = 0;
	    double area2 = 0;
	    double area3 = 0;
	    double area4 = 0;
	    double total_area = 0;
	    if(!area1IsTriangle && !area2IsTriangle && !area3IsTriangle && !area4IsTriangle)
	    {
	        area1 = getQuadrilateralArea(middle_left, upper_left, middle_top, point);
	        area2 = getQuadrilateralArea(point, middle_top, upper_right, middle_right);
	        area3 = getQuadrilateralArea(middle_bottom, point, middle_right, lower_right);
	        area4 = getQuadrilateralArea(lower_left, middle_left, point, middle_bottom);
	        
	        total_area =  getQuadrilateralArea(lower_left, upper_left, upper_right, lower_right);
		    System.out.println("Total area calculated from cell directly is " + total_area);
		    System.out.println("Total area calculated by summing seperate areas is " + (area1 + area2 + area3 + area4));
		    System.out.println("Area 1 = " + area1 + ", area2 = " + area2 + ", area3 = " + area3 + ", area4 is " + area4);
		    System.out.println();
	    }
	    else if(area1IsTriangle)
	    {
	    	x = middle_top.getX();
	 	    y = middle_top.getY(); 
	 	    if(x == x1 && y == y1) 
	 	    {
	 	    	area1 = DataMapper.getTriangleArea(middle_left, upper_left, point); 
	 	    	area2 = DataMapper.getQuadrilateralArea(point, upper_left, upper_right, middle_right);
	 	    }
	 	    else
	 	    {
	 	    	area1 = DataMapper.getTriangleArea(middle_top, upper_left, point);
	 	    	area4 = DataMapper.getQuadrilateralArea(lower_left, upper_left, point, middle_bottom);
	 	    }
	    }
	    else if(area2IsTriangle)
	    {
	    	x = middle_top.getX();
	 	    y = middle_top.getY(); 
	 	    if(x == x2 && y == y2) 
	 	    {
	 	    	area2 = DataMapper.getTriangleArea(middle_right, upper_right, point);
	 	    	area1 = DataMapper.getQuadrilateralArea(middle_left, upper_left, upper_right, point);
	 	    	
	 	    }
	 	    else
	 	    {
	 	    	area2 = DataMapper.getTriangleArea(middle_top, upper_right, point);
	 	    	area4 = DataMapper.getQuadrilateralArea(lower_left, upper_left, point, middle_bottom);
	 	    }	
	    }
	    else if(area3IsTriangle)
	    {
	    	x = middle_bottom.getX();
	 	    y = middle_bottom.getY(); 
	 	    if(x == x3 && y == y3)
	 	    {
	 	        area3 = DataMapper.getTriangleArea(middle_right, lower_right, point);
	 	        area4 = DataMapper.getQuadrilateralArea(lower_left, middle_left, point, lower_right);
	 	    }
	 	    else
	 	    {
	 	    	area3 = DataMapper.getTriangleArea(middle_bottom, lower_right, point);	
	 	    	area2 = DataMapper.getQuadrilateralArea(point, middle_top, upper_right, lower_right);
	 	    }
	    }
	    else if(area4IsTriangle)
	    {
	    	x = middle_bottom.getX();
	 	    y = middle_bottom.getY();	
	 	    if(x == x4 && y == y4)
	 	    {
	 	    	area4 = DataMapper.getTriangleArea(lower_left, lower_right, point);	
	 	    	area3 = DataMapper.getQuadrilateralArea(lower_left, point, middle_right, lower_right);
	 	    }
	 	    else
	 	    {
	 	    	area4 = DataMapper.getTriangleArea(lower_left, lower_right, point);
	 	    	area1 = DataMapper.getQuadrilateralArea(lower_left, upper_left, middle_top, point);
	 	    }
	    }
	    
	  
	    total_area =  DataMapper.getQuadrilateralArea(lower_left, upper_left, upper_right, lower_right);
	    /*
	    System.out.println("Total area calculated from cell directly is " + total_area);
	    System.out.println("Total area calculated by summing seperate areas is " + (area1 + area2 + area3 + area4));
	    System.out.println();
	    */
	    	    		
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
		if(distance == length1 || distance == length2) // No bisecting line, return not a number.
		{
			/*
			if(distance == length1)
				return(sample1.intensity);
			else
				return(sample2.intensity);
			*/
			// It might be useful to return the value of the near endpoint.
			// Returning not a number makes it easier to debug programs.
			return(Double.NaN);
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
		    
		    double weight1 = (length - segment_length) / length;
		    double weight2 = segment_length /length;
		    double value = weight1 * sample1.intensity + weight2 * sample2.intensity;
		    return(value);
		}
	}
	
	// This returns the bisecting point from a line determined by two samples, or the nearest endpoint location if no bisecting line from the point exists.
	public static Point2D.Double getBisectingPoint(Sample sample1, Sample sample2, Point2D.Double point)
	{ 
	    double x1                    = sample1.x;
	    double y1                    = sample1.y;
	    double x2                    = sample2.x;
		double y2                    = sample2.y;
		double x3                    = point.getX();
		double y3                    = point.getY();
		Line2D.Double line           = new Line2D.Double(x1, y1, x2, y2);
		Line2D.Double endpoint_line1 = new Line2D.Double(x1, y1, x3, y3);
		Line2D.Double endpoint_line2 = new Line2D.Double(x2, y2, x3, y3);
		double        length1        = DataMapper.getLength(endpoint_line1);
		double        length2        = DataMapper.getLength(endpoint_line2);
		
		if((((y1 < y3) && (y3 < y2)) || ((y1 > y3) && (y3 > y2))) || 
		(((x1 < x3) && (x3 < x2)) || ((x1 > x3) && (x3 > x2))))
		{
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
				double        a_radians          = DataMapper.getSlopeRadians(line);   
			    Line2D.Double hypotenuse         = new Line2D.Double(x1, y1, x3, y3);
			    double        hypotenuse_length  = DataMapper.getLength(hypotenuse);
			    double        b_radians          = 0;
			    
			    if(x1 == x3)
			        b_radians = Math.PI / 2;
		        else if(y1 == y3)
				    b_radians = 0;
			    else
				    b_radians = DataMapper.getSlopeRadians(hypotenuse);	             
			    double  c_radians = 0;
			    if(b_radians < a_radians)
				    c_radians = a_radians - b_radians;
			    else
				    c_radians = b_radians - a_radians;   
			    double d_radians      = Math.PI / 2 - c_radians;    
			    double segment_length = hypotenuse_length * Math.sin(d_radians);
			    double x4             = Math.cos(a_radians) * segment_length + x1;
			    double y4             = Math.sin(a_radians) * segment_length + y1;
			               
			    Point2D.Double bisecting_point = new  Point2D.Double(x4, y4);
			    return(bisecting_point); 
			}
		}
        else
        {
            if(length1 < length2)
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
	}
	
	public static Point2D.Double getBisectingPoint(double x1, double y1, double x2, double y2, Point2D.Double point)
	{ 
		double        x3             = point.getX();
		double        y3             = point.getY();
		Line2D.Double line           = new Line2D.Double(x1, y1, x2, y2);
		
		double        length1        = DataMapper.getLength(x1, y1, x3, y3);
		double        length2        = DataMapper.getLength(x2, y2, x3, y3);
		
		double        distance1  = line.ptSegDist(x3, y3);
		
		double xshift = x1;
		double yshift = y1;
		
		
		
		// This is sometimes a false equality with very small differences between numbers.  
		// Better to screen the non-bisecting points another way.
	    if(distance1 == length1)
	    {
	    	Point2D.Double end_point = new  Point2D.Double(x1, y1);	
	    	return(end_point);
	    }
	    else if(distance1 == length2)
	    {
	    	Point2D.Double end_point = new  Point2D.Double(x2, y2);	
	    	return(end_point);	
	    }
	    
	    double a_radians = 0.;
	    if(x1 == x2)
	    {
	    	if((y1 <= y3 && y3 <= y2) || (y2 <= y3 && y3 <= y1))
	    	{
	    	    Point2D.Double bisecting_point = new  Point2D.Double(x1, y3);	
	    	    return(bisecting_point);
	    	}
	    	else
	    		a_radians = Math.PI / 2;
	    }
	    else if(y1 == y2)
	    {
	    	if((x1 <= x3 && x3 <= x2) || (x2 <= x3 && x3 <= x1))
    	    {
    	        Point2D.Double bisecting_point = new  Point2D.Double(x3, y1);	
    	        return(bisecting_point);
    	    }
	    	else
	    	    a_radians = 0;
	    }
	    else
	        a_radians = DataMapper.getSlopeRadians(line);   
	    
	    x1 -= xshift;
	    y1 -= yshift;
	    x2 -= xshift;
	    y2 -= yshift;
	    x3 -= xshift;
	    y3 -= yshift;
	    
	    Line2D.Double hypotenuse1         = new Line2D.Double(x1, y1, x3, y3);
	    double        hypotenuse_length1  = DataMapper.getLength(hypotenuse1);
	    
	    double        b_radians          = 0;
	    
	    if(x1 == x3)
	        b_radians = Math.PI / 2;
        else if(y1 == y3)
		    b_radians = 0;
	    else
		    b_radians = DataMapper.getSlopeRadians(hypotenuse1);	             
	    double  c_radians = 0;
	    if(b_radians < a_radians)
		    c_radians = a_radians - b_radians;
	    else
		    c_radians = b_radians - a_radians;   
	    
	    double d_radians      = Math.PI / 2 - c_radians;    
	    double segment_length = hypotenuse_length1 * Math.sin(d_radians);
	    double x4             = Math.cos(a_radians) * segment_length + x1;
	    double y4             = Math.sin(a_radians) * segment_length + y1;
	               
	    Point2D.Double bisecting_point = new  Point2D.Double(x4 + xshift, y4 + yshift);
	    
	    double distance2 = getLength(x3, y3, x4, y4);
	    
	    //System.out.println("The distance returned by line.ptSegDist() = " + distance1);
	    //System.out.println("The distance calculated with the law of sines = " + distance2);
	    double difference = Math.abs(distance1 - distance2);
	    if(difference > .1)
	    {
	    	System.out.println("Discrepancy in calculating bisecting point.");
	    	System.out.println("x1 = " + x1 + ", y1 = " + y1);
	    	System.out.println("x2 = " + x2 + ", y2 = " + y2);
	    	System.out.println("x3 = " + x3 + ", y3 = " + y3);
	    }
	    return(bisecting_point); 
	}
	
	public static Point2D.Double getNearestPoint(double x1, double y1, double x2, double y2, Point2D.Double point)
	{ 
		double         x3       = point.getX();
		double         y3       = point.getY();
		Line2D.Double line      = new Line2D.Double(x1, y1, x2, y2);
		double        distance1 = line.ptSegDist(x3, y3);
		double        distance2 = getLength(x1, y1, x3, y3);
		double        distance3 = getLength(x2, y2, x3, y3);
		if(distance1 == distance2)
	    {
	    	Point2D.Double end_point = new  Point2D.Double(x1, y1);	
	    	return(end_point);
	    }
	    else if(distance1 == distance3)
	    {
	    	Point2D.Double end_point = new  Point2D.Double(x2, y2);	
	    	return(end_point);	
	    }
	    else
	    {
	    	double theta1 = 0;
	    	if(x1 == x2)
	    	{
	    		if(y1 < y2)
	    		    theta1 = Math.PI / 2;
	    		else
	    			theta1 = -Math.PI / 2;	
	    	}
	    	else
	    	    theta1 = DataMapper.getSlopeRadians(line);
	    	
	    	Line2D.Double hypotenuse = new Line2D.Double(x1, y1, x3, y3);
	    	double theta2 = 0;
	    	if(x1 == x3)
	    	{
	    		if(y1 < y2)
	    		    theta2 = Math.PI / 2;
	    		else
	    			theta2 = -Math.PI / 2;	
	    	}
	    	else
	    	    theta2 = DataMapper.getSlopeRadians(hypotenuse);
	    	
	    	double r1     = getLength(x1, y1, x3, y3);
	    	double r2     = getLength(x1, y1, x2, y2);
	    	double _x1    = 0;
	    	double _y1    = 0;
	    	double _x2    = 0;
	    	double _y2    = 0;
	    	double _x3    = 0;
	    	double _y3    = 0;
	    	double xshift = 0;
	    	double yshift = 0;
	    	if(theta1 == 0)
	    	{
	    		_x1 = x1;
	    		_y1 = y1;
	    		_x2 = x2;
	    		_y2 = y2;
	    		_x3 = x3;
	    		_y3 = y3;
	    	}
	    	else
	    	{
	    		_x1 = 0;
	    		_y1 = 0;
	    		_x2 = r2;
	    		_y2 = 0;
	    		_x3 = r1 * Math.cos(theta2 - theta1);
	    		_y3 = r1 * Math.sin(theta2 - theta1);
	    		xshift = x1;
	    		yshift = y1;
	    	}
	    	
	    	hypotenuse       = new Line2D.Double(_x1, _y1, _x3, _y3);
	    	double a_radians = 0;
		    if(_x1 == _x3)
			    a_radians = Math.PI / 2;
		    else if(_y1 == _y3)
				a_radians = 0;
			else
				a_radians = DataMapper.getSlopeRadians(hypotenuse);
		    double b_radians      = Math.PI / 2 - a_radians;    
			double segment_length = r1 * Math.sin(b_radians);
			double x4             = segment_length * Math.cos(theta1) + xshift;
			double y4             = segment_length * Math.sin(theta1) + yshift;
		
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