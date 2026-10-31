import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.awt.Point;
import java.math.BigDecimal;

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
		// Intersection of the diagonals lower_left->upper_right and
		// upper_left->lower_right, by solving the two parametric line
		// equations with cross products. The previous version subtracted
		// angles from atan2() without wrapping them, and was badly wrong for
		// cells rotated so a diagonal angle crossed +/- pi.
		double d1x = upper_right.x - lower_left.x;
		double d1y = upper_right.y - lower_left.y;
		double d2x = lower_right.x - upper_left.x;
		double d2y = lower_right.y - upper_left.y;

		double denominator = d1x * d2y - d1y * d2x;
		if(denominator == 0)
		{
			System.out.println("Diagonals are parallel: no intersection.");
			return(new Point2D.Double(Double.NaN, Double.NaN));
		}

		double t = ((upper_left.x - lower_left.x) * d2y - (upper_left.y - lower_left.y) * d2x) / denominator;
		return(new Point2D.Double(lower_left.x + t * d1x, lower_left.y + t * d1y));
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
		// Nearest point to 'point' on the segment (x1, y1)-(x2, y2): project onto
		// the line, then clamp to the segment. When the projection falls outside
		// the segment the endpoint itself is returned, with exactly the same
		// coordinates, so callers can test for it with ==.
		double dx      = x2 - x1;
		double dy      = y2 - y1;
		double length2 = dx * dx + dy * dy;
		if(length2 == 0)
			return(new Point2D.Double(x1, y1));

		double t = ((point.x - x1) * dx + (point.y - y1) * dy) / length2;
		if(t <= 0)
			return(new Point2D.Double(x1, y1));
		else if(t >= 1)
			return(new Point2D.Double(x2, y2));
		else
			return(new Point2D.Double(x1 + t * dx, y1 + t * dy));
	}

	// Relative error the fast double-precision path of getCrossProduct() is
	// allowed to have before it falls back to exact BigDecimal arithmetic.
	public static final double TRIANGLE_AREA_TOLERANCE = 1e-12;

	// Shewchuk's bound on the absolute rounding error of a 2x2 cross product
	// of coordinate differences computed in doubles: (3 + 16 * eps) * eps,
	// with eps = 2^-53.
	private static final double CROSS_ERROR_BOUND = (3.0 + 16.0 * 0x1p-53) * 0x1p-53;

	// Returns (b - a) x (d - c) = (bx - ax) * (dy - cy) - (by - ay) * (dx - cx),
	// accurate to TRIANGLE_AREA_TOLERANCE relative error. It is computed in
	// doubles when that is provably accurate enough, and otherwise exactly with
	// BigDecimal (correctly rounded). Returns NaN for non-finite input.
	public static double getCrossProduct(double ax, double ay, double bx, double by,
	                                     double cx, double cy, double dx, double dy)
	{
		if(!Double.isFinite(ax) || !Double.isFinite(ay) || !Double.isFinite(bx) || !Double.isFinite(by) ||
		   !Double.isFinite(cx) || !Double.isFinite(cy) || !Double.isFinite(dx) || !Double.isFinite(dy))
			return(Double.NaN);

		// Fast path.
		double left  = (bx - ax) * (dy - cy);
		double right = (by - ay) * (dx - cx);
		double cross = left - right;

		double error_bound = CROSS_ERROR_BOUND * (Math.abs(left) + Math.abs(right));
		if(error_bound <= TRIANGLE_AREA_TOLERANCE * Math.abs(cross))
			return(cross);

		// Slow path: the conversions, subtractions and multiplications are all
		// exact, so the only rounding is the final doubleValue().
		BigDecimal exact = new BigDecimal(bx).subtract(new BigDecimal(ax)).multiply(new BigDecimal(dy).subtract(new BigDecimal(cy)))
		         .subtract(new BigDecimal(by).subtract(new BigDecimal(ay)).multiply(new BigDecimal(dx).subtract(new BigDecimal(cx))));
		return(exact.doubleValue());
	}

	// Twice the signed area of triangle a, b, c: positive when the vertices
	// are counter-clockwise (with y increasing upward), negative when clockwise.
	private static double getSignedDoubleArea(Point2D.Double a, Point2D.Double b, Point2D.Double c)
	{
		return(getCrossProduct(a.x, a.y, b.x, b.y, a.x, a.y, c.x, c.y));
	}

	public static double getTriangleArea(Point2D.Double point1, Point2D.Double point2, Point2D.Double point3)
	{
		// Put the vertices in a fixed (lexicographic) order so every permutation
		// of the input performs exactly the same floating-point operations and
		// returns a bit-identical result.
		Point2D.Double[] p = {point1, point2, point3};
		java.util.Arrays.sort(p, (u, v) -> u.x != v.x ? Double.compare(u.x, v.x)
		                                               : Double.compare(u.y, v.y));

		double area = Math.abs(getSignedDoubleArea(p[0], p[1], p[2])) / 2;
		if(Double.isNaN(area))
		{
			System.out.println("Triangle has a non-finite coordinate.");
			System.out.println("x1 = " + point1.x + ", y1 = " + point1.y);
			System.out.println("x2 = " + point2.x + ", y2 = " + point2.y);
			System.out.println("x3 = " + point3.x + ", y3 = " + point3.y);
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
		// For a simple quadrilateral p1 p2 p3 p4 (vertices in cyclic order,
		// convex or concave), the area is half the cross product of the
		// diagonals: |(p3 - p1) x (p4 - p2)| / 2. Unlike splitting into two
		// triangles, this is correct for concave quads, and the result is
		// identical whichever corner the vertices start from and in either
		// direction. A repeated vertex reduces it to the triangle's area.
		double cross = getCrossProduct(lower_left.x,  lower_left.y,  upper_right.x, upper_right.y,
		                               upper_left.x,  upper_left.y,  lower_right.x, lower_right.y);
		double area  = Math.abs(cross) / 2;
		if(Double.isNaN(area))
			System.out.println("Quadrilateral has a non-finite coordinate.");
		return(area);
	}

	public static boolean containsPoint(Point2D.Double point, Sample ... sample)
	{
		// True if the point is inside the polygon formed by the samples, or on
		// its boundary (edges and corners included). Fewer than three samples
		// do not form a polygon, so the result is false.
		if(sample == null || sample.length < 3)
			return(false);

		Path2D.Double path = new Path2D.Double();
		path.moveTo(sample[0].x, sample[0].y);
		for(int i = 1; i < sample.length; i++)
			path.lineTo(sample[i].x, sample[i].y);
		path.closePath();
		if(path.contains(point))
			return(true);

		// Path2D.contains() leaves out some boundary points (e.g. the right and
		// top edges), so test the edges directly with an exact collinearity test.
		for(int i = 0; i < sample.length; i++)
		{
			Sample a = sample[i];
			Sample b = sample[(i + 1) % sample.length];
			if(getCrossProduct(a.x, a.y, b.x, b.y, a.x, a.y, point.x, point.y) == 0 &&
			   point.x >= Math.min(a.x, b.x) && point.x <= Math.max(a.x, b.x) &&
			   point.y >= Math.min(a.y, b.y) && point.y <= Math.max(a.y, b.y))
				return(true);
		}
		return(false);
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
		// Finds the value at 'point' such that linear interpolation over the
		// triangle point, corner1, corner2 reproduces interior.intensity at
		// the interior sample. Uses signed (barycentric) areas, so it also
		// works when the interior sample is not strictly inside the triangle.
		Point2D.Double p  = point;
		Point2D.Double c1 = new Point2D.Double(corner1.x, corner1.y);
		Point2D.Double c2 = new Point2D.Double(corner2.x, corner2.y);
		Point2D.Double q  = new Point2D.Double(interior.x, interior.y);

		double total = getSignedDoubleArea(p, c1, c2);
		if(total == 0 || Double.isNaN(total))
		{
			System.out.println("Extrapolation triangle is degenerate.");
			System.out.println("Point x = " + String.format("%.2f", p.x) + ", y = " + String.format("%.2f", p.y));
			System.out.println("Corner 1 x = " + String.format("%.2f", c1.x) + ", y = " + String.format("%.2f", c1.y));
			System.out.println("Corner 2 x = " + String.format("%.2f", c2.x) + ", y = " + String.format("%.2f", c2.y));
			return(Double.NaN);
		}

		// Barycentric weights of the interior sample.
		double weight_point = getSignedDoubleArea(q, c1, c2) / total;
		double weight1      = getSignedDoubleArea(p, q,  c2) / total;
		double weight2      = getSignedDoubleArea(p, c1, q)  / total;

		if(weight_point == 0)
		{
			System.out.println("Interior sample lies on the line through the corners; cannot extrapolate.");
			System.out.println("Interior x = " + String.format("%.2f", q.x) + ", y = " + String.format("%.2f", q.y));
			return(Double.NaN);
		}

		double value = (interior.intensity - weight1 * corner1.intensity - weight2 * corner2.intensity) / weight_point;
		return(value);
	}


	// Barycentric weights of 'point' in triangle a, b, c, from signed areas.
	// Inside the triangle (including its edges) all three are >= 0; outside it
	// at least one is negative. Returns null for a degenerate triangle.
	private static double[] getBarycentricWeights(Point2D.Double point, Point2D.Double a, Point2D.Double b, Point2D.Double c)
	{
		double total = getSignedDoubleArea(a, b, c);
		if(total == 0 || Double.isNaN(total))
			return(null);
		double[] weight = new double[3];
		weight[0] = getSignedDoubleArea(point, b, c) / total;
		weight[1] = getSignedDoubleArea(a, point, c) / total;
		weight[2] = getSignedDoubleArea(a, b, point) / total;
		return(weight);
	}

	public static double getLinearInterpolation(Point2D.Double point, Sample sample1, Sample sample2, Sample sample3)
	{
		// Barycentric (linear) interpolation over the triangle of samples.
		// Signed areas make this exact linear interpolation inside the
		// triangle, including on its edges and corners, and linear
		// extrapolation outside it.
		Point2D.Double a = new Point2D.Double(sample1.x, sample1.y);
		Point2D.Double b = new Point2D.Double(sample2.x, sample2.y);
		Point2D.Double c = new Point2D.Double(sample3.x, sample3.y);

		double[] weight = getBarycentricWeights(point, a, b, c);
		if(weight == null)
		{
			System.out.println("Interpolation triangle is degenerate.");
			return(Double.NaN);
		}

		double value = sample1.intensity * weight[0] +
		               sample2.intensity * weight[1] +
		               sample3.intensity * weight[2];
		return(value);
	}

	// How far outside a cell (as a barycentric weight, i.e. a fraction of the
	// triangle's size) a point may be before the 4-sample interpolation warns.
	public static final double OUTSIDE_TOLERANCE = 1e-9;

	public static double getLinearInterpolation(Point2D.Double point, Sample sample1, Sample sample2, Sample sample3, Sample sample4)
	{
		// sample1..sample4 are the upper left, upper right, lower right and
		// lower left corners of the cell.
		//
		// The cell is split into two triangles along a diagonal and the point is
		// interpolated linearly in the triangle that contains it. The result
		// equals each sample's intensity at its own corner, and along each side
		// of the cell depends only on that side's two corners, so neighboring
		// cells agree along the edges they share.
		Point2D.Double ul = new Point2D.Double(sample1.x, sample1.y);
		Point2D.Double ur = new Point2D.Double(sample2.x, sample2.y);
		Point2D.Double lr = new Point2D.Double(sample3.x, sample3.y);
		Point2D.Double ll = new Point2D.Double(sample4.x, sample4.y);

		// A diagonal can be used only if it lies inside the cell, i.e. the two
		// corners not on it are strictly on opposite sides of it. For a convex
		// cell both diagonals qualify; for a concave cell only the one through
		// the reflex corner does.
		double ul_side = Math.signum(getSignedDoubleArea(ll, ur, ul));
		double lr_side = Math.signum(getSignedDoubleArea(ll, ur, lr));
		double ur_side = Math.signum(getSignedDoubleArea(ul, lr, ur));
		double ll_side = Math.signum(getSignedDoubleArea(ul, lr, ll));
		boolean ll_ur_ok = ul_side * lr_side < 0;
		boolean ul_lr_ok = ur_side * ll_side < 0;

		boolean use_ll_ur;
		if(ll_ur_ok && ul_lr_ok)
		{
			// Convex: use the shorter diagonal, which gives better-shaped
			// triangles. Ties go to lower left - upper right.
			double ll_ur_length2 = (ur.x - ll.x) * (ur.x - ll.x) + (ur.y - ll.y) * (ur.y - ll.y);
			double ul_lr_length2 = (lr.x - ul.x) * (lr.x - ul.x) + (lr.y - ul.y) * (lr.y - ul.y);
			use_ll_ur = ll_ur_length2 <= ul_lr_length2;
		}
		else if(ll_ur_ok)
			use_ll_ur = true;
		else if(ul_lr_ok)
			use_ll_ur = false;
		else
		{
			System.out.println("Interpolation cell is degenerate or self-intersecting.");
			return(Double.NaN);
		}

		// Pick the triangle on the point's side of the diagonal. A point on the
		// diagonal itself gets the same value from either triangle.
		Sample s1, s2, s3;
		if(use_ll_ur)
		{
			double point_side = Math.signum(getSignedDoubleArea(ll, ur, point));
			if(point_side == 0 || point_side == ul_side)
			{ s1 = sample1; s2 = sample2; s3 = sample4; }   // upper left, upper right, lower left
			else
			{ s1 = sample2; s2 = sample3; s3 = sample4; }   // upper right, lower right, lower left
		}
		else
		{
			double point_side = Math.signum(getSignedDoubleArea(ul, lr, point));
			if(point_side == 0 || point_side == ur_side)
			{ s1 = sample1; s2 = sample2; s3 = sample3; }   // upper left, upper right, lower right
			else
			{ s1 = sample1; s2 = sample3; s3 = sample4; }   // upper left, lower right, lower left
		}

		Point2D.Double a = new Point2D.Double(s1.x, s1.y);
		Point2D.Double b = new Point2D.Double(s2.x, s2.y);
		Point2D.Double c = new Point2D.Double(s3.x, s3.y);
		double[] weight = getBarycentricWeights(point, a, b, c);
		if(weight == null)
		{
			System.out.println("Interpolation triangle is degenerate.");
			return(Double.NaN);
		}

		// The point is already on the correct side of the diagonal, so a
		// negative weight means it is outside one of the cell's sides. Outside
		// points get a linear extrapolation from the nearer triangle. A small
		// tolerance keeps points that are on a side but have been rounded a
		// hair outside it from producing warnings.
		if(weight[0] < -OUTSIDE_TOLERANCE || weight[1] < -OUTSIDE_TOLERANCE || weight[2] < -OUTSIDE_TOLERANCE)
		{
			System.out.println("Point is outside the interpolation cell; extrapolating.");
			System.out.println("Point x = " + point.x + ", y = " + point.y);
		}

		double value = s1.intensity * weight[0] + s2.intensity * weight[1] + s3.intensity * weight[2];
		return(value);
	}

	public static Point[]  getOrderedPositionList(int xdimension, int ydimension, int direction)
	{
		if(xdimension < 1 || ydimension < 1)
			throw new IllegalArgumentException("Grid dimensions must be at least 1, got " + xdimension + " x " + ydimension + ".");
		if(direction < 1 || direction > 4)
			throw new IllegalArgumentException("Direction must be 1, 2, 3 or 4, got " + direction + ".");

		int number_of_positions = xdimension * ydimension;
		Point[] position_list = new Point[number_of_positions];

		// The diagonal-crawling algorithm below assumes a grid at least
		// 2x2: before it ever checks a bound, it unconditionally seeds
		// points one column and one row past the origin. A single row or
		// column has no diagonal to crawl, so handle it directly here
		// (this also covers the degenerate 1x1 case) rather than letting
		// the general algorithm run off the end of position_list.
		if(xdimension == 1 || ydimension == 1)
		{
			int current_index = 0;
			for(int row_index = 0; row_index < ydimension; row_index++)
			{
				for(int column_index = 0; column_index < xdimension; column_index++)
				{
					if(direction == 1 || direction == 3)
						position_list[current_index++] = new Point(column_index, row_index);
					else
					{
						int reverse_index = ydimension - row_index - 1;
						position_list[current_index++] = new Point(column_index, reverse_index);
					}
				}
			}
			if(direction == 1 || direction == 4)
				return(position_list);
			else if(direction == 2 || direction == 3)
			{
				Point[] reverse_position_list = new Point[number_of_positions];
				for(int i = 0; i < number_of_positions; i++)
				{
					int j = number_of_positions - i - 1;
					reverse_position_list[j] = position_list[i];
				}
				return(reverse_position_list);
			}
			else
				return(position_list);
		}

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
	// Returns a list of eight sample lists for the neighbors of cell
	// (xindex, yindex), in the order NW, N, NE, W, E, SW, S, SE (y index
	// increasing southward). A neighbor that is outside the grid or not
	// populated gets an empty list, so the position is implicit in the order.
	// Works for any grid size, including grids one cell wide or tall.
	public static ArrayList getNeighborList(int xindex, int yindex, int xdim, int ydim, ArrayList close_data_array, boolean isPopulated[][])
	{
		if(xindex < 0 || xindex >= xdim || yindex < 0 || yindex >= ydim)
			throw new IllegalArgumentException("Cell (" + xindex + ", " + yindex + ") is outside the " + xdim + " x " + ydim + " grid.");

		int[][] offset = {{-1, -1}, {0, -1}, {1, -1},
		                  {-1,  0},          {1,  0},
		                  {-1,  1}, {0,  1}, {1,  1}};

		ArrayList neighbor_list = new ArrayList();
		for(int k = 0; k < offset.length; k++)
		{
			int x = xindex + offset[k][0];
			int y = yindex + offset[k][1];
			if(x >= 0 && x < xdim && y >= 0 && y < ydim && isPopulated[y][x])
				neighbor_list.add(close_data_array.get(y * xdim + x));
			else
				neighbor_list.add(new ArrayList());
		}
		return(neighbor_list);
	}
}
