import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.awt.Point;

public class DataMapper
{
	public static double getDistance(double x1, double y1, double x2, double y2)
	{
	    double distance  = StrictMath.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	    return(distance);
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
	
	public static double getSlope(Line2D.Double line)
	{
		double x1 = line.getX1();
	    double y1 = line.getY1();
	    double x2 = line.getX2();
	    double y2 = line.getY2();  
	    
	    if(x1 == x2)
	    {
	    	if(y1 < y2)
	    		return(Double.POSITIVE_INFINITY);
	    	else if(y1 > y2)
	    		return(Double.NEGATIVE_INFINITY);
	    	else
	    		return(Double.NaN);
	    }
	    else
	    	return((y2 - y1) / (x2 - x1));
	}
	
	public static double getSlope(double x1, double y1, double x2, double y2)
	{
	    if(x1 == x2)
	    {
	    	if(y1 < y2)
	    		return(Double.POSITIVE_INFINITY);
	    	else if(y1 > y2)
	    		return(Double.NEGATIVE_INFINITY);
	    	else
	    		return(Double.NaN);
	    }
	    else
	    	return((y2 - y1) / (x2 - x1));
	}
	
	public static double getAngleRadians(Line2D.Double line)
	{
		double x1 = line.getX1();
	    double y1 = line.getY1();
	    double x2 = line.getX2();
	    double y2 = line.getY2();  
	    
	    if(x1 == x2 && y1 == y2)
	    	return(Double.NaN);
	    else if(y1 == y2)
	    {
	    	if(x1 < x2)
	    	    return(0);
	    	else
	    		return(Math.PI);
	    }
	    else if(x1 == x2)
	    {
	    	if(y1 < y2)
	    	    return(Math.PI / 2);
	    	else
	    		return(-Math.PI / 2);
	    }
	    else
	    {
	    	double rise    = y2 - y1;
	    	double run     = x2 - x1;
	    	double radians = Math.atan2(rise, run);
	    	return(radians);
	    }
	}
	
	public static double getAngleRadians(double x1, double y1, double x2, double y2)
	{
	    if(x1 == x2 && y1 == y2)
	    	return(Double.NaN);
	    else if(y1 == y2)
	    {
	    	if(x1 < x2)
	    	    return(0);
	    	else
	    		return(Math.PI);
	    }
	    else if(x1 == x2)
	    {
	    	if(y1 < y2)
	    	    return(Math.PI / 2);
	    	else
	    		return(-Math.PI / 2);
	    }
	    else
	    {
	    	double rise    = y2 - y1;
	    	double run     = x2 - x1;
	    	double radians = Math.atan2(rise, run);
	    	return(radians);
	    }
	}
	
	public static Point2D.Double getIntersection(Point2D.Double upper_left, Point2D.Double upper_right, Point2D.Double lower_right, Point2D.Double lower_left)
	{
		Line2D.Double base        = new Line2D.Double(lower_left,  lower_right);
		Line2D.Double diagonal1   = new Line2D.Double(lower_left,  upper_right);
		Line2D.Double diagonal2   = new Line2D.Double(upper_left,  lower_right);
		
		double base_radians       = getAngleRadians(base);
		double diagonal1_radians  = getAngleRadians(diagonal1);
		double diagonal2_radians  = getAngleRadians(diagonal2);
		
		double theta1             = diagonal1_radians - base_radians;
		double theta2             = -diagonal2_radians + base_radians;
		double theta3             = Math.PI - (theta1 + theta2);
		
		double base_length        = getLength(base);
		double determinant_length = Math.sin(theta2) / Math.sin(theta3) * base_length;
		double delta_x            = determinant_length * Math.cos(theta1);
		double delta_y            = determinant_length * Math.sin(theta1);
		
		double x1                 = lower_left.getX();
		double y1                 = lower_left.getY();
		double x2                 = x1 + delta_x;
		double y2                 = y1 + delta_y;
		
		Point2D.Double intersection_point = new Point2D.Double(x2, y2);
		return(intersection_point);
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
			//System.out.println("No perpendicular bisector.");
    	    Point2D.Double end_point = new  Point2D.Double(x1, y1);	
    	    return(end_point);
	    }
		else if(distance1 == distance3)
	    {
			
			//System.out.println("No perpendicular bisector.");
    	    Point2D.Double end_point = new  Point2D.Double(x2, y2);	
    	    return(end_point);
	    }
	    else if(x1 == x2)
		{
	    	//System.out.println("Simple perpendicular_bisector.");
			Point2D.Double perpendicular_bisector = new  Point2D.Double(x1, y3);	
	    	return(perpendicular_bisector);   	
		}
		else if(y1 == y2)
		{
			//System.out.println("Simple perpendicular_bisector.");
			Point2D.Double perpendicular_bisector = new  Point2D.Double(x3, y1);	
	    	return(perpendicular_bisector);	
		} 
	    else
	    {
	    	double        theta1     = getAngleRadians(x1, y1, x2, y2);
	    	Line2D.Double hypotenuse = new Line2D.Double(x1, y1, x3, y3);
	    	double        theta2     = getAngleRadians(hypotenuse);
	    	double        _x1        = 0;
	    	double        _y1        = 0;
	    	double        _x3        = 0;
	    	double        _y3        = 0;
	    	double        xshift     = 0;
	    	double        yshift     = 0;
	    	
	    	if(theta1 == 0)
	    	{
	    		_x1 = x1;
	    		_y1 = y1;
	    		_x3 = x3;
	    		_y3 = y3;
	    	}
	    	else
	    	{
	    		_x1    = 0;
	    		_y1    = 0;
	    		_x3    = distance2 * Math.cos(theta2 - theta1);
	    		_y3    = distance2 * Math.sin(theta2 - theta1);
	    		xshift = x1;
	    		yshift = y1;
	    	}
	    	
	    	hypotenuse         = new Line2D.Double(_x1, _y1, _x3, _y3);
	    	double theta3      = DataMapper.getAngleRadians(hypotenuse);
		    double theta4      = Math.PI / 2 - theta3;    
			double base_length = distance2 * Math.sin(theta4);
			double x4          = base_length * Math.cos(theta1) + xshift;
			double y4          = base_length * Math.sin(theta1) + yshift;
			
			double distance4   = getLength(x4, y4, x3, y3);
		    double difference  = Math.abs(distance1 - distance4);
		    if(difference > .1)
		    {
		    	System.out.println("Discrepancy in calculating nearest_location.");
		    	System.out.println("The distance returned by line.ptSegDist() = " + distance1);
			    System.out.println("The distance calculated with the law of sines = " + distance4);
		    	System.out.println("x1 = " + x1 + ", y1 = " + y1);
		    	System.out.println("x2 = " + x2 + ", y2 = " + y2);
		    	System.out.println("x3 = " + x3 + ", y3 = " + y3);
		    	System.out.println();
		    }
			
			Point2D.Double perpendicular_bisector = new  Point2D.Double(x4, y4);
			return(perpendicular_bisector);
	    }
	}
	
	public static double getTriangleArea(Point2D.Double point1, Point2D.Double point2, Point2D.Double point3)
	{
		double x1 = point1.getX();
		double y1 = point1.getY();
		double x2 = point2.getX();
		double y2 = point2.getY();
		double x3 = point3.getX();
		double y3 = point3.getY();
		
		// Return 0 if the points are on a line.
		if((x1 == x2 && y1 == y2) || (x1 == x3 && y1 == y3) || (x1 == x3 && y1 == y3))
		{
			
			System.out.println("Not a triangle: duplicate points.");
			System.out.println("x1 = " + x1 + ", y1 = " + y1 + " x2 = " + x2 + ", y2 = " + y2 + " x3 = " + x3 + ", y3 = " + y3);
		    return(0);   	
		}
		else if((x1 == x2) && (x2 == x3))
		{
			System.out.println("Not a triangle: x-values collinear.");
			System.out.println("x1 = " + x1 + ", y1 = " + y1 + " x2 = " + x2 + ", y2 = " + y2 + " x3 = " + x3 + ", y3 = " + y3);
		    return(0);
		}
		else if((y1 == y2) && (y2 == y3))
		{
			System.out.println("Not a triangle: y-values collinear.");
			System.out.println("x1 = " + x1 + ", y1 = " + y1 + " x2 = " + x2 + ", y2 = " + y2 + " x3 = " + x3 + ", y3 = " + y3);
		    return(0);
		}
		
		
		double a      = DataMapper.getDistance(x1, y1, x2, y2);
		double b      = DataMapper.getDistance(x2, y2, x3, y3);
		double c      = DataMapper.getDistance(x3, y3, x1, y1);
	
		double s      = (a + b + c) / 2;
		double square = s * (s - a) * (s - b) * (s - c); 
		double h_area = Math.sqrt(square);
		// This turns out to be zero with our data set, when it should be non-zero.
		//System.out.println("The value produced using Heron's formula is " + h_area);
	 
		// The values produced by dropping an altitude and Heron's formula are very
		// similar, but the formula goes to zero before the algorithm.
		// The formula also produces floating point numbers at times with lots of empty space in
		// the fractional part, with numbers in the least significant places, which looks like noise.
		double         width    = 0;
		double         height   = 0;
		
		double length1 = DataMapper.getLength(x1, y1, x2, y2);
		double length2 = DataMapper.getLength(x2, y2, x3, y3);
		double length3 = DataMapper.getLength(x3, y3, x1, y1);
		
		// Find a side of the triangle that has a 
		// perpendicular line to the opposing vertex.
		Point2D.Double location = DataMapper.getNearestPoint(x1, y1, x2, y2, point3);
		double x4 = location.getX();
		double y4 = location.getY();
		double distance = DataMapper.getDistance(x4, y4, x3, y3);
		if(distance != length2 && distance != length3)
		{
			//System.out.println("Dropping an altitude to side 1.");
			height            = DataMapper.getDistance(x3, y3, x4, y4);	
			width             = DataMapper.getDistance(x1, y1, x2, y2);	
		}
		else
		{
			location = DataMapper.getNearestPoint(x2, y2, x3, y3, point1);
			x4 = location.getX();
			y4 = location.getY();
			distance = DataMapper.getDistance(x4, y4, x1, y1);
			if(distance != length1 && distance != length3)
			{
				//System.out.println("Dropping an altitude to side 2.");
				height            = DataMapper.getDistance(x1, y1, x4, y4);	
				width             = DataMapper.getDistance(x2, y2, x3, y3);	
			}
			else
			{
				location = DataMapper.getNearestPoint(x3, y3, x1, y1, point2);
				x4 = location.getX();
				y4 = location.getY();   
				distance = DataMapper.getDistance(x4, y4, x2, y2);
				if(distance != length1 && distance != length3)
				{
					//System.out.println("Dropping an altitude to side 3.");
					height            = DataMapper.getDistance(x2, y2, x4, y4);	
					width             = DataMapper.getDistance(x3, y3, x1, y1);	
				}
				else
				{
					// In fact, there still might be a perpendicular bisector in the case of right angles,
					// as well as triangles that are extremely close to right angles because
					// of a false equivalence in the calculations.  In that case, the area is
					// an approximation that might be improved but the error might not be significant
					// for our purposes.  For example, x = 74.92 and x = 74.92000000015832
					// produce identical results.
					
					// Find out which side of the triangle is longer than the others, and then
					// use the two other ones for height and width.
					if((length1 > length2) && (length1 > length3))
					{
						// The hypotenuse is formed by x1, y1, and x2, y2.	
						//System.out.println("Found a right triangle.");
						height = length2;
						width  = length3;
					}
					else if((length2 > length1) && (length2 > length3))
					{
						// The hypotenuse is formed by x2, y2,  and x3, y3.
						//System.out.println("Found a right triangle.");
						height = length1;
						width  = length3;
					}
					else if((length3 > length1) && (length3 > length2))
					{
						// The hypotenuse is formed by x3, y3,  and x1, y1.
						//System.out.println("Found a right triangle.");
						height = length1;
						width  = length2;
					}
					else
					{
						// Could be a false equivalence because one side is so much 
						// shorter than the others.  Find the shortest side, and 
						// find the distance from the opposing vertex as the height
						// and use the shortest side as the width.
						//System.out.println("Assuming a false equivalence.");
						if((length1 < length2) && (length1 < length3))
						{
							// The short side is formed by x1, y1, and x2, y2.	
							location = DataMapper.getNearestPoint(x1, y1, x2, y2, point3);
							x4 = location.getX();
							y4 = location.getY(); 
							height = getDistance(x3, y3, x4, y4);
							width  = length1;
						}
						else if((length2 < length1) && (length2 < length3))
						{
							// The  short side is formed by x2, y2, and x3, y3.
							location = DataMapper.getNearestPoint(x2, y2, x3, y3, point1);
							x4 = location.getX();
							y4 = location.getY(); 
							height = getDistance(x1, y1, x4, y4);
							width  = length2;
						}
						else if((length3 < length1) && (length3 < length2))
						{
							// The  short side is formed by x3, y3,  and x1, y1.
							location = DataMapper.getNearestPoint(x3, y3, x1, y1, point2);
							x4 = location.getX();
							y4 = location.getY(); 
							height = getDistance(x2, y2, x4, y4);
							width  = length3;
						}
					}
				}
			}
		}
		
		double area = height * width / 2;
		
		double difference = Math.abs(h_area - area);
	    //System.out.println("The difference between Heron's formula and dropping an altitude is " + difference);
		//System.out.println();
		
		
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
			System.out.println("Result was not a number.");
			System.out.println("x1 = " + x1 + ", y1 = " + y1);
			System.out.println("x2 = " + x2 + ", y2 = " + y2);
			System.out.println("x3 = " + x3 + ", y3 = " + y3);
			System.out.println();
		}
		
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
		
		//System.out.println("Doing top triangle");
	    double area1 = getTriangleArea(upper_left, lower_left, upper_right);
	    //System.out.println("Doing bottom triangle");
	    double area2 = getTriangleArea(upper_right, lower_right, lower_left);
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
	    
	    int number_of_triangles = 0;
	    
	    Point2D.Double middle_top    =  getNearestPoint(x1, y1, x2, y2, point);
	    
	    double x = middle_top.getX();
	    double y = middle_top.getY(); 
	    //System.out.println("Checking midpoints: ");
	    if((x == x1 && y == y1) || (x == x2 && y == y2))
	    {
	    	//System.out.println("Middle top is an endpoint.");
	    	number_of_triangles++;
	    	if(x == x1 && y == y1)
	    	{
	    		//System.out.println("Area 1 is a triangle.");
	    		area1IsTriangle = true;
	    		
	    	}
	    	else
	    	{
	    		//System.out.println("Area 2 is a triangle.");
	    		area2IsTriangle = true;	
	    	}
	    }
	    
	    Point2D.Double middle_left   =  getNearestPoint(x4, y4, x1, y1, point);
	    x = middle_left.getX();
	    y = middle_left.getY(); 
	    
	    if((x == x1 && y == y1) || (x == x4 && y == y4))
	    {
	    	//System.out.println("Middle left is an endpoint.");
	    	number_of_triangles++;
	    	if(x == x1 && y == y1)
	    	{
	    	    area1IsTriangle = true;
	    	    //System.out.println("Area 1 is a triangle.");
	    	}
	    	else
	    	{
	    	    area4IsTriangle = true;
	    	    //System.out.println("Area 4 is a triangle.");
	    	}
	    }
	    
	    Point2D.Double middle_right  =  getNearestPoint(x3, y3, x2, y2, point);
	    x = middle_right.getX();
	    y = middle_right.getY(); 
	    
	    if((x == x2 && y == y2) || (x == x3 && y == y3))
	    {
	    	//System.out.println("Middle right is an endpoint.");
	    	number_of_triangles++;
	    	if(x == x2 && y == y2)
	    	{
	    	    area2IsTriangle = true;
	    	    //System.out.println("Area 2 is a triangle.");
	    	}
	    	else
	    	{
	    		area3IsTriangle = true;
	    		//System.out.println("Area 3 is a triangle.");
	    	}
	    }
	    
	    Point2D.Double middle_bottom =  getNearestPoint(x4, y4, x3, y3, point);
	    x = middle_bottom.getX();
	    y = middle_bottom.getY(); 
	    if((x == x3 && y == y3) || (x == x4 && y == y4))
	    {
	    	//System.out.println("Middle bottom is an endpoint.");
	    	number_of_triangles++;
	    	if(x == x3 && y == y3)
	    	{
	    	    area3IsTriangle = true;
	    	    //System.out.println("Area 3 is a triangle.");
	    	}
	    	else
	    	{
	    		area4IsTriangle = true;
	    		//System.out.println("Area 4 is a triangle with 3.");
	    	}
	    }
	    
	    //System.out.println("Finished checking midpoints.");
	    //System.out.println();
	    //System.out.println("Number of triangle areas in quadrilateral are " + number_of_triangles);
	    //System.out.println();
	    	
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
	    }
	    else if(area1IsTriangle)
	    {
	    	x = middle_top.getX();
	 	    y = middle_top.getY(); 
	 	    
	 	    if(x == x1 && y == y1) 
	 	    {
	 	    	// We have a middle left but no middle top.
	 	    	area1 = DataMapper.getTriangleArea(middle_left, upper_left, point); 
	 	    	
	 	    	if(area2IsTriangle)
	 	    	{
	 	    		// Only two areas can be a triangle at a time, so go ahead and
	 	    		// get area 4.
	 	    		area4 = DataMapper.getQuadrilateralArea(lower_left, middle_left, point, middle_bottom);
	 	    	    
	 	    		// If area2 is not an odd shaped quad it must be lacking a middle right.
	 	    		area2 = DataMapper.getTriangleArea(upper_left, upper_right, point);
	 	    		
	 	    		// Get the odd shape quad formed by area3.
		 	    	area3 = DataMapper.getQuadrilateralArea(middle_bottom, point, upper_right, lower_right);
	 	    		
	 	    	}
	 	    	else if(area4IsTriangle)
	 	    	{
	 	    	    // If area4 is a triangle it must be lacking a middle bottom.
	 	    		area4 = DataMapper.getTriangleArea(lower_left, middle_left, point);	
	 	    		
	 	    		// Get the odd shaped quads formed by areas 2 and 3.
	 	    		area2 = DataMapper.getQuadrilateralArea(upper_left, point, middle_right, upper_right);
	 	    		area3 = DataMapper.getQuadrilateralArea(lower_left, point, middle_right, lower_right);
	 	    	}
	 	    	else if(area3IsTriangle)
	 	    	{
	 	    	    // In this case, we need to find out if we lack a middle bottom or a middle right.
	 	    		x = middle_right.getX();
	 	    		y = middle_right.getY();
	 	    		if(x == x3 && y == y3)
	 	    		{
	 	    			// We lack a middle right.
	 	    			area3 = DataMapper.getTriangleArea(middle_bottom, lower_right, point);
	 	    			area2 = DataMapper.getQuadrilateralArea(point, upper_left, upper_right, lower_right);
	 	    		    // Only two areas can be a triangle at a time, so go ahead and
		 	    		// get the quad formed by area 4.
		 	    		area4 = DataMapper.getQuadrilateralArea(lower_left, middle_left, point, middle_bottom);
	 	    		}
	 	    		else
	 	    		{
	 	    		    // We lack a middle bottom.
	 	    			area3 = DataMapper.getTriangleArea(lower_right, point, middle_right);
	 	    			area2 = DataMapper.getQuadrilateralArea(point, upper_left, upper_right, middle_right);
	 	    		    
	 	    			// Only two areas can be a triangle at a time, so go ahead and
		 	    		// get the quad formed by area 4.
		 	    		area4 = DataMapper.getQuadrilateralArea(lower_left, middle_left, point, lower_right);
	 	    		}		
	 	    	}
	 	    	else
	 	    	{
	 	    	    // Get odd shaped quad.
	 	    		area2 = getQuadrilateralArea(point, upper_left, upper_right, middle_right);
	 	    		
	 	    		area3 = getQuadrilateralArea(middle_bottom, point, middle_right, lower_right);	
	 	    		area4 = DataMapper.getQuadrilateralArea(lower_left, middle_left, point, middle_bottom);	
	 	    	}
	 	    }
	 	    else
	 	    { 
	 	    	// We have a middle top but no middle left.
	 	    	area1 = DataMapper.getTriangleArea(middle_top, upper_left, point);
	 	    	
	 	    	if(area2IsTriangle)
	 	    	{
	 	    		// Only two areas can be a triangle at a time, so go ahead and
	 	    		// get the odd shape quad formed by area 4.
	 	    		area4 = DataMapper.getQuadrilateralArea(lower_left, upper_left, point, middle_bottom);
	 	    	    
	 	    		// We know there's a middle top, so area2 must be lacking a middle right.
	 	    		area2 = DataMapper.getTriangleArea(middle_top, upper_right, point);
	 	    		
	 	    		// Get the odd shape quad formed by area3.
		 	    	area3 = DataMapper.getQuadrilateralArea(middle_bottom, point, upper_right, lower_right);
	 	    		
	 	    	}
	 	    	else if(area4IsTriangle)
	 	    	{
	 	    	    // We know the left side corners form a side, so area4 must be lacking a middle bottom.
	 	    		area4 = DataMapper.getTriangleArea(lower_left, upper_left, point);
	 	    		
	 	    		// We have a middle top and a middle right.
	 	    		// Get the odd shaped quads formed by area2 and area3.
	 	    		area2 = DataMapper.getQuadrilateralArea(point, middle_top, upper_right, middle_right);
	 	    		area3 = DataMapper.getQuadrilateralArea(lower_left, point, middle_right, lower_right);	
	 	    	}
	 	    	else if(area3IsTriangle)
	 	    	{
	 	    		// In this case, we need to find out if we lack a middle bottom or a middle right.
	 	    		x = middle_right.getX();
	 	    		y = middle_right.getY();
	 	    		if(x == x3 && y == y3)
	 	    		{
	 	    			// We lack a middle right.
	 	    			area3 = DataMapper.getTriangleArea(middle_bottom, lower_right, point);
	 	    			area2 = DataMapper.getQuadrilateralArea(point, middle_top, upper_right, lower_right);
	 	    			// Only two areas can be a triangle at a time, so go ahead and
		 	    		// get the odd shape quad formed by area 4.
		 	    		area4 = DataMapper.getQuadrilateralArea(lower_left, upper_left, point, middle_bottom);
	 	    		}
	 	    		else
	 	    		{
	 	    		    // We lack a middle bottom.
	 	    			area3 = DataMapper.getTriangleArea(lower_right, point, middle_right);
	 	    			area2 = DataMapper.getQuadrilateralArea(point, middle_top, upper_right, middle_right);
	 	    		    // Only two areas can be triangles at a time, so go ahead and
		 	    		// get the odd shape quad formed by area 4.
		 	    		area4 = DataMapper.getQuadrilateralArea(lower_left, upper_left, point, lower_right);
	 	    		}	
	 	    	}
	 	    	else
	 	    	{
	 	    		area2 = getQuadrilateralArea(point, middle_top, upper_right, middle_right);
	 	    		area3 = getQuadrilateralArea(middle_bottom, point, middle_right, lower_right);	
	 	    		
	 	    		// Get odd shaped quad.
	 	    		area4 = DataMapper.getQuadrilateralArea(lower_left, upper_left, point, middle_bottom);
	 	    	}
	 	    }
	    }
	    else if(area2IsTriangle)
	    {
	    	x = middle_top.getX();
	 	    y = middle_top.getY(); 
	 	    
	 	    if(x == x2 && y == y2) 
	 	    {
	 	    	// Area 2 lacks a middle top but has a middle right.
	 	    	area2 = DataMapper.getTriangleArea(middle_right, upper_right, point);
	 	    	
	 	    	if(area3IsTriangle)
	 	    	{
	 	    		//System.out.println("Areas 2 and 3 are triangles.");
	 	    		x = middle_bottom.getX();
	 		 	    y = middle_bottom.getY(); 
	 		 	    
	 		 	    if(x == x3 && y == y3) 
	 		 	    {
	 		 	    	// Area 3 lacks a middle bottom.
	 	    		    area3 = DataMapper.getTriangleArea(lower_left, middle_right, point);
	 	    		    
	 	    		    // Get odd shaped quads for areas 1 and 4.
	 	    		    area1 = DataMapper.getQuadrilateralArea(lower_left, middle_left, point, lower_right);
		 	    	    area4 = DataMapper.getQuadrilateralArea(lower_left, middle_left, point, lower_right);
	 		 	    }
	 		 	    else
	 		 	    {
	 		 	        // Area 3 lacks a middle right.
	 	    		    area3 = DataMapper.getTriangleArea(lower_left, middle_bottom, point);
	 	    		    
	 	    		    // Get odd shaped quad for area 1.
	 	    		    area1 = DataMapper.getQuadrilateralArea(lower_left, middle_left, point, lower_right);
	 	    		    // Get regular quad for area 4.
	 	    		    area4 = getQuadrilateralArea(lower_left, middle_left, point, middle_bottom); 
	 		 	    }	
	 	    	}
	 	    	else if(area4IsTriangle)
	 	    	{
	 	    		System.out.println("Areas 2 and 4 are triangles.");
	 	    		x = middle_bottom.getX();
	 		 	    y = middle_bottom.getY(); 
	 		 	    
	 		 	    if(x == x4 && y == y4)
	 		 	    {
	 		 	    	// Area 4 lacks a middle bottom.
	 		 	    	area4 = DataMapper.getTriangleArea(lower_left, middle_left, point);
	 		 	    	
	 		 	        // Get odd shaped quads for areas 1 and 3.
	 		 	    	area1 = getQuadrilateralArea(middle_left, upper_left, upper_right, point);
		 		        area3 = getQuadrilateralArea(lower_left, point, middle_right, lower_right);
	 		 	    }
	 		 	    else
	 		 	    {
	 		 	        // Area 4 lacks a middle left.
	 		 	    	area4 = DataMapper.getTriangleArea(lower_left, middle_bottom, point);
	 		 	    	
	 		 	    	// Get odd shaped quad for area 1.
	 		 	    	area1 = getQuadrilateralArea(lower_left, upper_left, upper_right, point);
	 		 	    	
	 		 	    	area3 = getQuadrilateralArea(middle_bottom, point, middle_right, lower_right);
	 		 	    }	
	 	    	}
	 	    	else
	 	    	{
	 	    		//System.out.println("Area 2 is a triangle.");
	 	    	    // Get odd shaped quad for area1, which we know is not a triangle.
		 	    	area1 = getQuadrilateralArea(middle_left, upper_left, upper_right, point);	
		 	    	
		 	    	area3 = getQuadrilateralArea(middle_bottom, point, middle_right, lower_right);
	 		        area4 = getQuadrilateralArea(lower_left, middle_left, point, middle_bottom);	
	 	    	}
	 	    }
	 	    else
	 	    {
	 	    	// Area2 lacks a middle right.
	 	    	area2 = DataMapper.getTriangleArea(middle_top, upper_right, point);
	 	    	if(area4IsTriangle)
	 	    	{
	 	    		
	 	    		x = middle_bottom.getX();
	 		 	    y = middle_bottom.getY(); 
	 		 	    //System.out.println("Areas 2 and 4 are triangles.");
	 		 	    if(x == x4 && y == y4)
	 		 	    {
	 		 	    	
	 		 	    	// Area 4 lacks a middle bottom.
	 		 	    	area4 = DataMapper.getTriangleArea(lower_left, middle_left, point);
	 		 	        // Get quad for area1.
			 	    	area1 = getQuadrilateralArea(middle_left, upper_left, middle_top, point);
			 	    	// Get odd shaped quad for  area3.
			 	    	area3 = DataMapper.getQuadrilateralArea(lower_left, point, upper_right, lower_right);
	 		 	    }
	 		 	    else
	 		 	    {
	 		 	        // Area 4 lacks a middle left.
	 		 	    	area4 = DataMapper.getTriangleArea(lower_left, middle_bottom, point); 
	 		 	        // Get odd shaped quad for area1.
			 	    	area1 = getQuadrilateralArea(lower_left, upper_left, upper_right, point);
			 	        // Get quad for area3.
			 	    	area3 = DataMapper.getQuadrilateralArea(middle_bottom, point, upper_right, lower_right);
			 	    	
	 		 	    }
	 	    	}
	 	    	else
	 	    	{
	 	    		// No middle right.
	 	    		//System.out.println("Area 2 is a triangle.");
	 	    		
	 	    		// Get odd shaped shaped quad for area3.
	 	    		area3 = getQuadrilateralArea(middle_bottom, point, upper_right, lower_right);
	 	    		
	 	    		area1 = getQuadrilateralArea(middle_left, upper_left, middle_top, point);
	 	    		area4 = getQuadrilateralArea(lower_left, middle_left, point, middle_bottom);	
	 	    	}
	 	    	
	 	    }	
	    }
	    else if(area3IsTriangle)
	    {
	    	x = middle_bottom.getX();
	 	    y = middle_bottom.getY(); 
	 	  
	 	    
	 	    if(x == x3 && y == y3)
	 	    {
	 	    	// Area3 lacks a middle bottom.
	 	    	
	 	        area3 = DataMapper.getTriangleArea(middle_right, lower_right, point);
	 	        
	 	        if(area4IsTriangle)
	 	        {
	 	        	//System.out.println("Area 3 and 4 are triangles.");
	 	        	
	 	        	// We already know it uses the lower right, so it must be lacking
	 	        	// a middle left.
	 	        	area4 = DataMapper.getTriangleArea(lower_left, lower_right, point);
	 	        	
	 	        	// Get the odd shaped quad for area1.
	 	        	area1 = getQuadrilateralArea(lower_left, upper_left, middle_top, point);
	 	        	
	 	        	area2 = getQuadrilateralArea(point, middle_top, upper_right, middle_right);
	 	        }
	 	        else
	 	        {
	 	        	//System.out.println("Area 3 is a triangle.");
	 	        	// Get the odd shaped quad for area4.
	 	        	area4 = getQuadrilateralArea(lower_left, middle_left, point, lower_right);
	 	        	
                    area1 = getQuadrilateralArea(middle_left, upper_left, middle_top, point);
	 	        	area2 = getQuadrilateralArea(point, middle_top, upper_right, middle_right);
	 	        }
	 	    }
	 	    else
	 	    {
	 	        // Area3 lacks a middle right.
                area3 = DataMapper.getTriangleArea(middle_bottom, lower_right, point);
	 	        
	 	        if(area4IsTriangle)
	 	        {
	 	        	//System.out.println("Area 3 and 4 are triangles.");
	 	        	if(x == x4 && y == y4)
	 	        	{
	 	        		// No middle right or bottom.
	 	        		area4 = DataMapper.getTriangleArea(lower_left, middle_left, point);
	 	        		
	 	        	    // Get the odd shaped quad for area2.
	 	        		area2 = getQuadrilateralArea(lower_right, upper_right, middle_top, point);
	 	        		
	 	        		area1 = getQuadrilateralArea(middle_left, upper_left, middle_top, point);
	 	        	}
	 	        	else
	 	        	{
	 	        		// No middle right or middle left.
	 	        		area4 = DataMapper.getTriangleArea(lower_left, middle_bottom, point);
	 	        		
	 	        	    // Get the odd shaped quads for area1 and area2.
	 	        		area1 = getQuadrilateralArea(lower_left, upper_left, middle_top, point);
	 	        		area2 = getQuadrilateralArea(lower_right, upper_right, middle_top, point);
	 	        	}
	 	        }
	 	        else
	 	        {
	 	        	//System.out.println("Area 3 is a triangle.");
	 	        	// Get the odd shaped quad for area4.
	 	        	area4 = getQuadrilateralArea(lower_left, middle_left, point, lower_right);
	 	        	
                    area1 = getQuadrilateralArea(middle_left, upper_left, middle_top, point);
	 	        	area2 = getQuadrilateralArea(point, middle_top, upper_right, middle_right);
	 	        }

	 	    }
	    }
	    else if(area4IsTriangle)
	    {
	    	//System.out.println("Area 4 is a triangle.");
	    	x = middle_bottom.getX();
	 	    y = middle_bottom.getY();
	 	 
	 	    if(x == x4 && y == y4)
	 	    {
	 	    	// Area4 lacks a middle bottom.
	 	    	area4 = DataMapper.getTriangleArea(lower_left, middle_left, point);	
	 	    	
	 	    	// Get the odd shaped quad for area3.
	 	    	area3 = DataMapper.getQuadrilateralArea(lower_left, point, middle_right, lower_right);
	 	    	
	 	    	area1 = getQuadrilateralArea(middle_left, upper_left, middle_top, point);
	 	        area2 = getQuadrilateralArea(point, middle_top, upper_right, middle_right);
	 	    }
	 	    else
	 	    {
	 	        // Area4 lacks a middle right.
	 	    	area4 = DataMapper.getTriangleArea(lower_right, middle_bottom, point);
	 	    	
	 	    	// Get the odd shaped quad for area1.
	 	    	area1 = DataMapper.getQuadrilateralArea(lower_left, upper_left, middle_top, point);

	 	    	area2 = getQuadrilateralArea(point, middle_top, upper_right, middle_right);
		        area3 = getQuadrilateralArea(middle_bottom, point, middle_right, lower_right);	
	 	    }
	    }
	    
	  
	    total_area =  DataMapper.getQuadrilateralArea(lower_left, upper_left, upper_right, lower_right);
	    
	    //System.out.println("Total area calculated from cell directly is " + total_area);
	    //System.out.println("Total area calculated by summing seperate areas is " + (area1 + area2 + area3 + area4));
	    //System.out.println();
	   
	    	    		
	    double weight1 = area3 / total_area;
	    double weight2 = area4 / total_area;
	    double weight3 = area1 / total_area;
	    double weight4 = area2 / total_area;
	    	    		        
	    double value = sample1.intensity * weight1 + sample2.intensity * weight2 + sample3.intensity * weight3 + sample4.intensity * weight4;
	    return(value);
    }
    
    
 
    /*
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
			double        a_radians = DataMapper.getAngleRadians(line);       
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
		    	b_radians = DataMapper.getAngleRadians(hypotenuse);	
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
	*/
	
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