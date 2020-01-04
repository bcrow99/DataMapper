import java.io.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class GetSmoothImage
{
	public static void main(String[] args)throws IOException, FileNotFoundException  
	{	 
        //Information that we want to stay global for the rest of the program.
		
		//The raw data.
		ArrayList sample_list = new ArrayList();
		
		//Information we'll collect about the data at the beginning of the program.
		double minimum_x, maximum_x;
        double minimum_y, maximum_y;
        double minimum_intensity, maximum_intensity;
        
        //Information we'll collect about the data during the program.
        //We're assigning values using three different algorithms:
        //bilinear, nearest line, and nearest point.
        //That means we can assign values without finding larger 
        //bounding polygons for cells that don't have one in
        //their local neighborhood.
        
        boolean isPopulated[][];          
        boolean hasNeighbors[][];  
        boolean isInterpolated[][];
        int     number_of_samples[][];   
          
        
        //A parameter we can adjust to increase our resolution.
        int   resolution = 4;
        double increment = 1. / resolution;
        
        //A value we can set from our data to produce an image.
        double  cell_intensity[][];
        
        File file = new File("C:/Users/Brian Crowley/Desktop/CleanData.txt");
        if(file.exists())
        {       
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line = reader.readLine();
            StringTokenizer number_tokens = new StringTokenizer(line, " ");
            int length = number_tokens.countTokens();  
            int number_of_entries = length / 3;
            for(int i = 0; i < number_of_entries; i++)
            {
            	double x = Double.valueOf(number_tokens.nextToken());
            	double y = Double.valueOf(number_tokens.nextToken());
            	double intensity = Double.valueOf(number_tokens.nextToken()); 
            	Sample current_sample = new Sample(x, y, intensity);
            	sample_list.add(current_sample);
            }
            
            while (line != null)
            {
            	try
                {
            	    line = reader.readLine();
            	    if(line != null)
            	    {
            	    	number_tokens = new StringTokenizer(line, " ");
            	        for(int i = 0; i < number_of_entries; i++)
                        {
                        	double x = Double.valueOf(number_tokens.nextToken());
                        	double y = Double.valueOf(number_tokens.nextToken());
                        	double intensity = Double.valueOf(number_tokens.nextToken()); 
                        	Sample current_sample = new Sample(x, y, intensity);
                        	sample_list.add(current_sample);	     
                        }
            	    }
            	    else
            	    {
            	    	//System.out.println("The number of lines in the data was " + number_of_lines);
            	    	//System.out.println("The number of samples is " + sample_list.size()); 	
            	    }
                }
            	catch(IOException e)
                {
                	System.out.println("Unexpected error " + e.toString());	
                }	
            }
            reader.close();
        }
        else
        {
        	System.out.println("File not found.");
        	System.exit(0);
        }
        
        
        //Get some information about our samples.
        int sample_list_size = sample_list.size();
        Sample init_sample = (Sample)sample_list.get(0);
        minimum_x = maximum_x = init_sample.x;
        minimum_y = maximum_y = init_sample.y;
        minimum_intensity = maximum_intensity = init_sample.intensity;
        for(int i = 1; i < sample_list_size; i++)
        {
        	Sample current_sample = (Sample)sample_list.get(i);
        	double x = current_sample.x;
        	double y = current_sample.y;
        	double intensity = current_sample.intensity;
        	if(x < minimum_x)
        		minimum_x = x;
        	else if(x > maximum_x)
        		maximum_x = x;
        	if(y < minimum_y)
        		minimum_y = y;
        	else if(y > maximum_y)
        		maximum_y = y;
        	if(intensity < minimum_intensity)
        		minimum_intensity = intensity;
        	else if(intensity > maximum_intensity)
        		maximum_intensity = intensity;		
        }
        
        //Now set up a raster.
        double x_range = maximum_x - minimum_x;
        // We'll pad the range so no data points fall on a boundary line.
        //x_range += .5;
        //double x_origin = minimum_x - .25;
        double x_origin = minimum_x;
        
        double y_range = maximum_y - minimum_y;
        //y_range += .5;
        //double y_origin = minimum_y - .25;
        double y_origin = minimum_y;
        
        String range_double = String.format("%.2f", x_range);
        //System.out.println("The x range is " + range_double);
        range_double = String.format("%.2f", y_range);
        //System.out.println("The y range is " + range_double);
            
        int xdim = (int)Math.round(x_range + .5);
        //System.out.println("Xdim is " + xdim);
        int ydim = (int)Math.round(y_range + .5);
        //System.out.println("Ydim is " + ydim);
        
        
        // This is meters and we want quarter meters, the resolution of our sensors.
        xdim *= resolution;
        ydim *= resolution;
        System.out.println("The xdim of the data grid is " + xdim);
        System.out.println("The ydim of the data grid is " + ydim);
        //Now that we know the size of our raster, lets allocate memory for our information.
        isPopulated       = new boolean[ydim][xdim];
        hasNeighbors      = new boolean[ydim][xdim];
        number_of_samples = new int[ydim][xdim];
        cell_intensity    = new double[ydim][xdim];
        isInterpolated     = new boolean[ydim][xdim];
          
        for(int i = 0; i < ydim; i++)
        {
        	for(int j = 0; j < xdim; j++)
        	{
        		isPopulated[i][j]  = false;
        		hasNeighbors[i][j] = false;
        		number_of_samples[i][j] = 0;
        		cell_intensity[i][j] = 0;
        	}
        }
        ArrayList close_data_array = new ArrayList(xdim * ydim);
        ArrayList data_array = new ArrayList(xdim * ydim);
        for(int i = 0; i < xdim * ydim; i++)
        {
        	ArrayList cell_list = new ArrayList();
        	close_data_array.add(cell_list);	
        }
        	   	
        for(int i = 0; i < sample_list_size; i++)
        {
            Sample current_sample = (Sample)sample_list.get(i);
            double current_x = current_sample.x;
            double current_y = current_sample.y;
            current_x -= x_origin;
            current_y -= y_origin;
            current_x *= resolution;
            current_y *= resolution;
            int x_index = (int) Math.floor(current_x);
            int y_index = (int)Math.floor(current_y);
           
            isPopulated[y_index][x_index] = true;
            number_of_samples[y_index][x_index]++;
            int data_index = y_index * xdim + x_index;
            ArrayList current_cell_list = (ArrayList)close_data_array.get(data_index);
            current_cell_list.add(current_sample);
        }
       
        System.out.println("Starting...");

        long start_time = System.nanoTime();
        
        for(int i = 0; i < ydim; i++)
        {  
        	double ycenter = i * increment + increment * .5 + y_origin;
        	for(int j = 0; j < xdim; j++)
        	{
        		double xcenter = j * increment + increment / 2. + x_origin;
        		//We'll make separate lists of samples based on location
    	    	//to facilitate the search for bounding triangles
        		//or quadrilaterals.
        		ArrayList northwest_list = new ArrayList();
    	    	ArrayList north_list     = new ArrayList();
    	    	ArrayList northeast_list = new ArrayList();
    	    	ArrayList west_list      = new ArrayList();
    	    	ArrayList east_list      = new ArrayList();
    	    	ArrayList southwest_list = new ArrayList();
    	    	ArrayList south_list     = new ArrayList();
    	    	ArrayList southeast_list = new ArrayList(); 
    	    	
        	    if(isPopulated[i][j] == true)
        	    {  
        	    	int       data_index        = i * xdim + j;
        	    	ArrayList current_cell_list = (ArrayList)close_data_array.get(data_index);
        	    	
        	    	//Sort interior points.
        	    	//Originally I thought that simply assigning everything to the corner lists
        	    	//would work but it creates problems somehow that I don't understand.
        	    	//For whatever reasons, this works better.
        	    	for(int k = 0; k < current_cell_list.size(); k++)
        	    	{
        	    		Sample current_sample = (Sample)current_cell_list.get(k);
        	    		current_sample.setDistance(xcenter, ycenter);
        	    		//Make it consistent with xy system
        	    		
    	    		    if(current_sample.x < xcenter)
    	    		    {
    	    		    	if(current_sample.y < ycenter)
    	    		    	{
    	    		    		// NW quadrant
    	    		    		double x = current_sample.x;
    	    		    		double y = current_sample.y;
    	    		    		
    	    		    		double x1 = xcenter;
    	    		    		double x2 = xcenter - increment / 2;
    	    		    		
    	    		    		double y1 = ycenter;
    	    		    		double y2 = ycenter - increment / 4;
    	    		    		double y3 = ycenter - increment / 2;
    	    		    		
    	    		    		Path2D.Double path = new Path2D.Double();
    	        		        path.moveTo(x1, y1);
    	        		        path.lineTo(x2,  y2);
    	        		        path.lineTo(x2,  y1);
    	        		        path.closePath();
    	        		        if(path.contains(x,y))
    	        		        {
    	        		            west_list.add(current_sample);	
    	        		        }
    	        		        else 
    	        		        {
    	        		            path = 	new Path2D.Double();
    	        		            path.moveTo(x1, y2);
    	        		            path.lineTo(x1, y3);
    	        		            path.lineTo(x2,  y3);
    	        		            path.closePath();
    	        		            if(path.contains(x,y))
        	        		        {
        	        		            north_list.add(current_sample);	
        	        		        }
    	        		            else
    	        		            	northwest_list.add(current_sample);
    	        		       }	
    	    		    	}
    	    		    	else
    	    		    	{
    	    		    	    // SW quadrant
    	    		    		double x = current_sample.x;
    	    		    		double y = current_sample.y;
    	    		    		
    	    		    		double x1 = xcenter;
    	    		    		double x2 = xcenter - increment / 2;
    	    		    		
    	    		    		double y1 = ycenter;
    	    		    		double y2 = ycenter + increment / 4;
    	    		    		double y3 = ycenter + increment / 2;
    	    		    		
    	    		    		Path2D.Double path = new Path2D.Double();
    	        		        path.moveTo(x1, y1);
    	        		        path.lineTo(x2,  y2);
    	        		        path.lineTo(x2,  y1);
    	        		        path.closePath();
    	        		        if(path.contains(x,y))
    	        		        {
    	        		            west_list.add(current_sample);	
    	        		        }
    	        		        else 
    	        		        {
    	        		            path = 	new Path2D.Double();
    	        		            path.moveTo(x1, y2);
    	        		            path.lineTo(x1, y3);
    	        		            path.lineTo(x2,  y3);
    	        		            path.closePath();
    	        		            if(path.contains(x,y))
        	        		        {
        	        		            south_list.add(current_sample);	
        	        		        }
    	        		            else
    	        		            	southwest_list.add(current_sample);
    	        		        }
    	    		    	}
    	    		    } 
    	    		    else
    	    		    {
    	    		    	if(current_sample.y < ycenter)
    	    		    	{
    	    		    	    // NE quadrant
    	    		    		double x = current_sample.x;
    	    		    		double y = current_sample.y;
    	    		    		
    	    		    		double x1 = xcenter;
    	    		    		double x2 = xcenter + increment / 2;
    	    		    		
    	    		    		double y1 = ycenter;
    	    		    		double y2 = ycenter - increment / 4;
    	    		    		double y3 = ycenter - increment / 2;
    	    		    		
    	    		    		Path2D.Double path = new Path2D.Double();
    	        		        path.moveTo(x1, y1);
    	        		        path.lineTo(x2,  y2);
    	        		        path.lineTo(x2,  y1);
    	        		        path.closePath();
    	        		        if(path.contains(x,y))
    	        		        {
    	        		            east_list.add(current_sample);	
    	        		        }
    	        		        else 
    	        		        {
    	        		            path = 	new Path2D.Double();
    	        		            path.moveTo(x1, y2);
    	        		            path.lineTo(x1, y3);
    	        		            path.lineTo(x2,  y3);
    	        		            path.closePath();
    	        		            if(path.contains(x,y))
        	        		        {
        	        		            north_list.add(current_sample);	
        	        		        }
    	        		            else
    	        		            	northeast_list.add(current_sample);
    	        		        }
    	    		    		
    	    		    	}
	    		    	    else
	    		    	    {
	    		    	    	// SE quadrant
	    		    	    	double x = current_sample.x;
    	    		    		double y = current_sample.y;
    	    		    		
    	    		    		double x1 = xcenter;
    	    		    		double x2 = xcenter + increment / 2;
    	    		    		
    	    		    		double y1 = ycenter;
    	    		    		double y2 = ycenter + increment / 4;
    	    		    		double y3 = ycenter + increment / 2;
    	    		    		
    	    		    		Path2D.Double path = new Path2D.Double();
    	        		        path.moveTo(x1, y1);
    	        		        path.lineTo(x2,  y2);
    	        		        path.lineTo(x2,  y1);
    	        		        path.closePath();
    	        		        if(path.contains(x,y))
    	        		        {
    	        		            east_list.add(current_sample);	
    	        		        }
    	        		        else 
    	        		        {
    	        		            path = 	new Path2D.Double();
    	        		            path.moveTo(x1, y2);
    	        		            path.lineTo(x1, y3);
    	        		            path.lineTo(x2,  y3);
    	        		            path.closePath();
    	        		            if(path.contains(x,y))
        	        		        {
        	        		            south_list.add(current_sample);	
        	        		        }
    	        		            else
    	        		            	southeast_list.add(current_sample);
    	        		        }
	    		    	    }
    	    		    }
        	    	} 
        	    } 

        	    //Getting surrounding samples, sorted by quadrant.
        	    ArrayList neighbor_list  = DataMapper.getNeighborList(j, i, xdim, ydim, close_data_array, isPopulated);
        	    for(int k = 0; k < 8; k++)
        	    {
        	    	ArrayList list = (ArrayList)neighbor_list.get(k);
        	    	if(list.size() != 0)
        	    	{
        	    		hasNeighbors[i][j] = true;
        	    		for(int m = 0; m < list.size(); m++)
        	    		{
        	    			Sample sample = (Sample)list.get(m);
        	    			sample.setDistance(xcenter, ycenter);
        	    			switch(k)
        	    			{
        	    			    case 0:  northwest_list.add(sample);
	    		                         break; 
        	    			    case 1:  north_list.add(sample);
   	    		                         break;
   	    		                case 2:  northeast_list.add(sample);
   	    		                         break;
   	    		                case 3:  west_list.add(sample);
   	    		                         break;
   	    		                case 4:  east_list.add(sample);
   	    		                         break;
   	    		                case 5:  southwest_list.add(sample);
   	    		                         break;
   	    		                case 6:  south_list.add(sample);
   	    		                         break;
   	    		                case 7:  southeast_list.add(sample);
        	    			}
        	    		}
        	    	}
        	    }
       
        	    ArrayList list = new ArrayList();
        	    list.add(northwest_list);
        	    list.add(north_list);
        	    list.add(northeast_list);
        	    list.add(west_list);
        	    list.add(east_list);
        	    list.add(southwest_list);
        	    list.add(south_list);
        	    list.add(southeast_list);
        	    
    	    	//Now we're going to order the neighbor lists based on how close they are to the center
    	    	//of the cell.	
    	    	for(int m = 0; m < 8; m++)
    	    	{
    	    		//Get a list of samples.
    	    		//Some confusing semantics here--this starts out
    	    		//unsorted by distance from the center of the cell
    	    		//but then gets sorted that way.
    	    		ArrayList sorted_list = (ArrayList)list.get(m);
    	    		if(sorted_list.size() != 0)
    	    		{
    	    		    //Make a copy.
    	    		    ArrayList unsorted_list = new ArrayList();
    	    		    for(int n = 0; n < sorted_list.size(); n++)
    	    			    unsorted_list.add(sorted_list.get(n));
    	    		    //Get a list of distances.
    	    		    ArrayList distance_list = new ArrayList();
    	    		    for(int n = 0; n < sorted_list.size(); n++)
    	    		    {
    	    			    Sample sample = (Sample)unsorted_list.get(n);
    	    			    distance_list.add(sample.distance);
    	    		    }	
    	    		    //My original supposition that the entries in the 
    	    		    //distance list would be unique turns out to be
    	    		    //incorrect.  We need to go through the list and
    	    		    //change the value by a small enough amount that its
    	    		    //different from the real value but unlikely to affect
    	    		    //the result significantly.  This is so when we
    	    		    //use the distance as a key to a hashtable, samples
    	    		    //don't start stepping on each other.
    	    		    //Kludgy, but any workaround that doesn't corrupt
    	    		    //the actual data will be tedious and add a lot
    	    		    //of code and alter basic data structures.
    	    		    //Something noisy in the data--same location with different value.

    	    		    double previous_distance = (double)distance_list.get(0);
    	    		    //System.out.print(previous_distance + "  ");
    	    		    for(int n = 1; n < distance_list.size(); n++)
    	    		    {
    	    		    	double current_distance = (double)distance_list.get(n);
    	    		    	if(current_distance == previous_distance)
    	    		    	{
    	    		    		//System.out.print(" duplicate distance ");
    	    		    		Sample current_sample  = (Sample)unsorted_list.get(n);
    	    		    		Sample previous_sample = (Sample)unsorted_list.get(n - 1);
    	    		    		//System.out.println(" previous x = " + previous_sample.x + " y = " + previous_sample.y + " current x = " + current_sample.x + "  y = " + current_sample.y);
    	    		    		//System.out.println("previous intensity was " + previous_sample.intensity + " current intensity is " + current_sample.intensity);
    	    		    		//Minimum possible floating point value.
    	    		    		current_distance       += 0.0000000000000001; 
    	    		    		current_sample.distance = current_distance;
    	    		    		distance_list.set(n, current_distance);
    	    		    	}
    	    		    	//System.out.print(current_distance + "  ");   
    	    		    }
    	    		    Collections.sort(distance_list);
    	    			
    	    		    //Could be some built-in facility in java to do this, but
    	    		    //it's likely to slow the program down.
    	    		    Hashtable <Double, Integer> location = new Hashtable <Double, Integer>();
        	    	    for(int n = 0; n < sorted_list.size(); n++)
        	    		    location.put((double)distance_list.get(n), n);	
        	    	    //Now order the list.
        	            for(int n = 0;  n < sorted_list.size(); n++)
    	    		    {
    	    		        Sample sample = (Sample)unsorted_list.get(n);
    	    			
    	    		        int index = location.get(sample.distance);
    	    		        try
    	    		        {
    	    		            sorted_list.set(index, sample);
    	    		        }
    	    		        catch(Exception e)
    	    		        {
    	    		            System.out.println("Error adding sample to array: " + e.toString());	
    	    			    }
    	    		    }
        	            //This was a direct reference to a list in neighbor list, I think.
        	            //Should test this if we go back and get the list from the neighbor list and see if it stays sorted.
    	    		}
    	        }
    	    	//Keep this information handy so we can search for bounding polygons later.
    	    	data_array.add(list);
        	}
        }
        
        // We have the information we need and we can start polygon searching.
        for(int i = 0; i < ydim; i++)
        {
        	//double ycenter = (y_origin + y_range) - (i * increment + increment / 2.);
        	double ycenter = i * increment + increment * .5 + y_origin;
        	for(int j = 0; j < xdim; j++)
        	{ 
        		
        		double xcenter = j * increment + increment / 2. + x_origin;
        		int data_index = i * xdim + j;
        		ArrayList neighbor_list = (ArrayList)data_array.get(data_index);
        		int location_type = DataMapper.getLocationType(j, i, xdim, ydim);
        	    boolean neighborPopulated[]   = new boolean[8];
        	    for(int m = 0; m < 8; m++)
        	    	neighborPopulated[m] = false; 
        	    
        	    for(int m = 0; m < neighbor_list.size(); m++)
        	    {
        	    	ArrayList current_neighbor_list = (ArrayList)neighbor_list.get(m);
        	    	if(current_neighbor_list.size() > 0)
        	    		neighborPopulated[m] = true;
        	    }
        	   
        	    //Now go look for polygons.
        	    if((neighborPopulated[0] && neighborPopulated[2] && neighborPopulated[5] && neighborPopulated[7]))
        	    	
        	    {	
        	    	//System.out.println("Found four corners.");
        	    	//This is the only time our indices are in a different order from the arguments, along with four sides.
        	    	int first_index  = 0;
        	    	int second_index = 2;
        	    	int third_index  = 7;
        	    	int fourth_index = 5;
        	    	
        	    	ArrayList first_list  = (ArrayList)neighbor_list.get(first_index);
        	    	ArrayList second_list = (ArrayList)neighbor_list.get(second_index);
        	    	ArrayList third_list  = (ArrayList)neighbor_list.get(third_index);
        	    	ArrayList fourth_list = (ArrayList)neighbor_list.get(fourth_index);
        	    	
        	    	double x1,y1,x2,y2,x3,y3,x4,y4 = 0;
        	    	outer: for(int m = 0; m < first_list.size(); m++)
        	    	{
        	    	    for(int n = 0; n < second_list.size(); n++)
        	    	    {
        	    	    	for(int p = 0; p < third_list.size(); p++)
        	    	    	{
        	    	    		for(int q = 0; q < fourth_list.size(); q++)
        	    	    		{
        	    	    	        Sample first_sample  = (Sample)first_list.get(m);
        	    	    			x1 = first_sample.x;
        	        	    	    y1 = first_sample.y;
        	        	    	    Sample second_sample = (Sample)second_list.get(n);
        	        	    	    x2 = second_sample.x;
        	        	    		y2 = second_sample.y;
        	        	    		Sample third_sample  = (Sample)third_list.get(p);
        	        	    		x3 = third_sample.x;
        	        	    		y3 = third_sample.y;
        	        	    		Sample fourth_sample = (Sample)fourth_list.get(q);
        	        	    		x4 = fourth_sample.x;
        	        	    		y4 = fourth_sample.y;
        	        	    		Path2D.Double path = new Path2D.Double();
        	        		    	path.moveTo(x1, y1);
        	        		    	path.lineTo(x2, y2);
        	        		    	path.lineTo(x3, y3);
        	        		    	path.lineTo(x4, y4);
        	        		    	path.closePath();
        	    	    		    if(path.contains(xcenter, ycenter)) 
        	    	    		    {
        	    	    		    	Point2D.Double upper_left  = new Point2D.Double(x1, y1);
        	    	    		        Point2D.Double upper_right = new Point2D.Double(x2, y2);
        	    	    		        Point2D.Double lower_right = new Point2D.Double(x3, y3);
        	    	    		        Point2D.Double lower_left  = new Point2D.Double(x4, y4);  
        	    	    		        Point2D.Double origin      = new Point2D.Double(xcenter, ycenter);
        	    	    		          
        	    	    			    Line2D.Double top      = new Line2D.Double(upper_left, upper_right);
        	    	    			    Line2D.Double left     = new Line2D.Double(upper_left, lower_left);
        	    	    			    Line2D.Double bottom   = new Line2D.Double(lower_left, lower_right);
        	    	    			    Line2D.Double right    = new Line2D.Double(lower_right, upper_right);
        	    	    		    	
        	    	                    //We need to get four more points to do our bilinear interpolation.
        	    	    			    double x5, y5, x6, y6;
        	    
        	    	    			    double slope = DataMapper.getSlope(top);
        	    	    			    double y_intercept = DataMapper.getYIntercept(upper_left, slope);
        	    	    			    y5 = slope * xcenter + y_intercept;
        	    	    			    slope = DataMapper.getSlope(bottom);
        	    	    			    y_intercept = DataMapper.getYIntercept(lower_left, slope);
        	    	    			    y6 = slope * xcenter + y_intercept;
        	    	    			    
        	    	    			    if(x2 != x3)
        	    	    			    {
        	    	    			        slope = DataMapper.getSlope(right);
        	    	    			        y_intercept = DataMapper.getYIntercept(upper_right, slope);
        	    	    			        x5 = (ycenter - y_intercept) / slope;
        	    	    			    }
        	    	    			    else
        	    	    			    {
        	    	    			    	double distance = right.ptSegDist(origin);
        	    	    			    	x5              = xcenter + distance;
        	    	    			    }
        	    	    			    
        	    	    			    if(x1 != x4)
        	    	    			    {
        	    	    			        slope = DataMapper.getSlope(left);
        	    	    			        y_intercept = DataMapper.getYIntercept(lower_left, slope);
        	    	    			        x6 = (ycenter - y_intercept) / slope;
        	    	    			    }
        	    	    			    else
        	    	    			    {
        	    	    			    	double distance = left.ptSegDist(origin);
        	    	    			    	x6              = xcenter - distance;
        	    	    			    }
        	    	    			      
        	    	    			    Point2D.Double middle_top  = new Point2D.Double(xcenter, y5);
        	    	    		        Point2D.Double middle_bottom = new Point2D.Double(xcenter, y6);
        	    	    		        Point2D.Double middle_right = new Point2D.Double(x5, ycenter);
        	    	    		        Point2D.Double middle_left  = new Point2D.Double(x6, ycenter); 
        	    	    		        
        	    	    		        double area1 = DataMapper.getQuadrilateralArea(middle_left, upper_left, middle_top, origin);
        	    	    		        double area2 = DataMapper.getQuadrilateralArea(origin, middle_top, upper_right, middle_right);
        	    	    		        double area3 = DataMapper.getQuadrilateralArea(middle_bottom, origin, middle_right, lower_right);
        	    	    		        double area4 = DataMapper.getQuadrilateralArea(lower_left, middle_left, origin, middle_bottom);
        	    	    		        double total_area = area1 + area2 + area3 + area4;
        	    	    		        
        	    	    		        total_area =  DataMapper.getQuadrilateralArea(lower_left, upper_left, upper_right, lower_right);
        	    	    		
        	    	    		        double weight1 = area3 / total_area;
        	    	    		        double weight2 = area4 / total_area;
        	    	    		        double weight3 = area1 / total_area;
        	    	    		        double weight4 = area2 / total_area;
        	    	    		        
        	    	    		        double intensity = first_sample.intensity * weight1 + 
        	    	    		        		           second_sample.intensity * weight2 + 
        	    	    		        		           third_sample.intensity * weight3 +
        	    	    		        		           fourth_sample.intensity * weight4;
        	    	    		        cell_intensity[i][j] = intensity;
        	    	    		      
        	    	    		        isInterpolated[i][j] = true;		
        	    	    		        break outer;
        	    	    		    }
        	    	    		    else
        	    	    		    {
        	    	    		    	/*
        	    	    		        System.out.println("Found a non-bounding box.");
        	    	    		    	System.out.println("");
        	    	    		    	String xstring =  String.format("%.2f", xcenter);
        	    	    		    	String ystring =  String.format("%.2f", ycenter);
        	    	    		    	System.out.println("The center of the cell is " + xstring + "  " + ystring);
        	    	    		    	xstring =  String.format("%.2f", x1);
        	    	    		    	ystring =  String.format("%.2f", y1);
        	    	    		    	System.out.println("");
        	    	    		    	System.out.println("Upper left of the cell is  " + xstring + "  " + ystring);
        	    	    		    	xstring =  String.format("%.2f", x2);
        	    	    		    	ystring =  String.format("%.2f", y2);
        	    	    		    	System.out.println("Upper right of the cell is " + xstring + "  " + ystring);
        	    	    		    	xstring =  String.format("%.2f", x3);
        	    	    		    	ystring =  String.format("%.2f", y3);
        	    	    		    	System.out.println("");
        	    	    		    	System.out.println("Lower left of the cell is  " + xstring + "  " + ystring);
        	    	    		    	xstring =  String.format("%.2f", x4);
        	    	    		    	ystring =  String.format("%.2f", y4);
        	    	    		    	System.out.println("Lower right of the cell is " + xstring + "  " + ystring);
        	    	    		    	
        	    	    		    	
        	    	    		    	
        	    	    		    	boolean 
        	    	    		    	keyPressed = false;
        	    	        		    while(!keyPressed)
        	    	        		    {
        	    	        		    	try 
        	    	        		        {	
        	    	        		            int key = System.in.read();
        	    	        		            keyPressed = true;
        	    	        		        } 
        	    	        		        catch (java.io.IOException ioex) 
        	    	        		    	{
        	    	        		                System.out.println("IO Exception");
        	    	        		        }
        	    	        		    }
        	    	        		    */
        	    	        		    
        	    	    		    }
        	    	    		}
        	    	    	}
        	    	    }
        	    	}   
        	    }
        	    else if((neighborPopulated[0] && neighborPopulated[2] && neighborPopulated[5]) || 
        	            (neighborPopulated[2] && neighborPopulated[5] && neighborPopulated[7]) ||
        	    		(neighborPopulated[0] && neighborPopulated[2] && neighborPopulated[6]) ||
        	    		(neighborPopulated[7] && neighborPopulated[0] && neighborPopulated[2]) ||
        	    		(neighborPopulated[0] && neighborPopulated[2] && neighborPopulated[6]) ||
        	    		(neighborPopulated[0] && neighborPopulated[5] && neighborPopulated[4]) ||
        	    		(neighborPopulated[2] && neighborPopulated[7] && neighborPopulated[3]) ||
        	    		(neighborPopulated[5] && neighborPopulated[7] && neighborPopulated[1]))
        	    {
        	    	
        	    	int first_index  = 0;
        	    	int second_index = 0;
        	    	int third_index  = 0;
        	    	int current_case = 0;
        	    	
        	    	if(neighborPopulated[0] && neighborPopulated[2] && neighborPopulated[5])
        	    	{
        	    		//System.out.println("Found three corners.");
        	    		first_index  = 0;
        	    		second_index = 2;
        	    		third_index  = 5;
        	    		current_case = 1;
        	    	}
        	    	else if(neighborPopulated[2] && neighborPopulated[5] && neighborPopulated[7])
        	    	{
        	    		//System.out.println("Found three corners.");
        	    		first_index  = 2;
        	    		second_index = 5;
        	    		third_index  = 7;
        	    		current_case = 2;
        	    	}
        	    	else if(neighborPopulated[0] && neighborPopulated[2] && neighborPopulated[6]) 
        	    	{
        	    		//System.out.println("Found three corners.");
        	    		first_index  = 0;
        	    		second_index = 2;
        	    		third_index  = 6;
        	    		current_case = 3;
        	    	}
        	    	else if(neighborPopulated[7] && neighborPopulated[0] && neighborPopulated[2])
        	    	{
        	    		//System.out.println("Found three corners.");
        	    		first_index  = 7;
        	    		second_index = 0;
        	    		third_index  = 2;
        	    		current_case = 4;
        	    	}
        	    	else if(neighborPopulated[0] && neighborPopulated[2] && neighborPopulated[6])
        	    	{
        	    		//System.out.println("Found two corners and a side.");
        	    		first_index  = 0;
        	    		second_index = 2;
        	    		third_index  = 6;
        	    		current_case = 5;
        	    	}
        	    	else if(neighborPopulated[0] && neighborPopulated[5] && neighborPopulated[4])
        	    	{
        	    		//System.out.println("Found two corners and a side.");
        	    		first_index  = 0;
        	    		second_index = 5;
        	    		third_index  = 4;	
        	    		current_case = 6;
        	    	}
        	    	else if(neighborPopulated[2] && neighborPopulated[7] && neighborPopulated[3])
        	    	{
        	    		//System.out.println("Found two corners and a side.");
        	    		first_index  = 2;
        	    		second_index = 7;
        	    		third_index  = 3;
        	    		current_case = 7;
        	    	}
        	    	else if(neighborPopulated[5] && neighborPopulated[7] && neighborPopulated[1])
        	    	{
        	    		//System.out.println("Found two corners and a side.");
        	    		first_index  = 5;
        	    		second_index = 7;
        	    		third_index  = 1;
        	    		current_case = 8;
        	    	}
        	    	ArrayList first_list  = (ArrayList)neighbor_list.get(first_index);
        	    	ArrayList second_list = (ArrayList)neighbor_list.get(second_index);
        	    	ArrayList third_list  = (ArrayList)neighbor_list.get(third_index);
        	    	double x1,y1,x2,y2,x3,y3 = 0;
        	    	boolean foundTriangle = false;
        	    	outer: for(int m = 0; m < first_list.size(); m++)
        	    	{
        	    	    for(int n = 0; n < second_list.size(); n++)
        	    	    {
        	    	    	for(int p = 0; p < third_list.size(); p++)
        	    	    	{
        	    	    		Sample first_sample  = (Sample)first_list.get(m);
        	    	    		x1 = first_sample.x;
        	        	    	y1 = first_sample.y; 
        	        	    	Sample second_sample = (Sample)second_list.get(n);
        	        	    	x2 = second_sample.x;
        	        	    	y2 = second_sample.y;
        	        	    	Sample third_sample  = (Sample)third_list.get(p);
        	        	    	x3 = third_sample.x;
        	        	    	y3 = third_sample.y;
        	        	    	Path2D.Double path = new Path2D.Double();
        	        		    path.moveTo(x1, y1);
        	        		    path.lineTo(x2, y2);
        	        		    path.lineTo(x3, y3);
        	        		    path.closePath();
        	    	    		if(path.contains(xcenter, ycenter)) 
        	    	    		{
        	    	    			foundTriangle = true;
        	    	    			
        	    	    			Point2D.Double base1  = new Point2D.Double(x1, y1);
    	    	    		        Point2D.Double top    = new Point2D.Double(x2, y2);
    	    	    		        Point2D.Double base2  = new Point2D.Double(x3, y3); 
    	    	    		        Point2D.Double origin = new Point2D.Double(xcenter, ycenter);
    	    	    		        
    	    	    		        double area1 = DataMapper.getTriangleArea(origin, base2, top);
    	    	    		        double area2 = DataMapper.getTriangleArea(base1, base2, origin);
    	    	    		        double area3 = DataMapper.getTriangleArea(base1, origin, top);
    	    	    		        
    	    	    		        double total_area = area1 + area2 + area3;
    	    	    		        double weight1    = area1 / total_area;
    	    	    		        double weight2    = area2 / total_area;
    	    	    		        double weight3    = area3 / total_area;
    	    	    		        
    	    	    		        double intensity = first_sample.intensity * weight1 + 
    	    	    		        		           second_sample.intensity * weight2 + 
    	    	    		        		           third_sample.intensity * weight3;
    	    	    		        cell_intensity[i][j] = intensity;
        	    	    			isInterpolated[i][j] = true;
    	    	    		        break outer;
        	    	    		}
        	    	    	}
        	    	    }
        	    	} 
        	    	if(!foundTriangle)
        	    	{
        	            //Do a weighted average of the nearest neighbors.
                	    //Sample first_sample  = (Sample)first_list.get(0);
                	    //Sample second_sample = (Sample)second_list.get(0);
        	        	//Sample third_sample  = (Sample)third_list.get(0);
        	        	//double total_distance = 0.;
        	        	//total_distance += first_sample.distance;
        	        	//total_distance += second_sample.distance;
        	        	//total_distance += third_sample.distance;
        	        	//double weight1 = first_sample.distance / total_distance;
        	        	//double weight2 = second_sample.distance / total_distance;
        	        	//double weight3 = third_sample.distance / total_distance;
        	        	//double intensity = first_sample.intensity * weight1 + second_sample.intensity * weight2 + third_sample.intensity * weight3;
        	        	    
        	    		Sample first_sample = (Sample)first_list.get(0);
        	    	    x1           = first_sample.x;
        	    	    y1           = first_sample.y;
        	    	    
        	    	    Sample second_sample  = (Sample)second_list.get(0);
        	    	    x2             = second_sample.x;
        	    	    y2             = second_sample.y;
        	    	    
        	    	    Sample third_sample = (Sample)third_list.get(0);
        	    	    x3           = third_sample.x;
        	    	    y3           = third_sample.y;
    	    		    
        	    	    
        	    	    Line2D.Double first_line  = new Line2D.Double(x1, y1, x2, y2);
        	    	    Line2D.Double second_line = new Line2D.Double(x2, y2, x3, y3);
        	    	    Line2D.Double third_line  = new Line2D.Double(x3, y3, x1, y1);
        	    	    
        	    	    double first_length       = first_line.ptSegDist(xcenter, ycenter);	
        	    	    double second_length      = second_line.ptSegDist(xcenter, ycenter);
        	    	    double third_length       = third_line.ptSegDist(xcenter, ycenter);
        	    	    if(first_length < second_length && first_length < third_length)
        	    	    {
        	    	    	// Get bisecting average from first line--using x1, y1 and x2, y2
        	    	    	Line2D.Double reference = new Line2D.Double(x1, y1, xcenter, ycenter);
        	    	    	double        reference_length     = DataMapper.getLength(reference);
        	    			double        first_slope          = Math.abs(DataMapper.getSlope(first_line));          //B
        	    			double        second_slope         = Math.abs(DataMapper.getSlope(reference));
        	    			
        	    			// Get the degrees.
        	    			double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
        	    			double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
        	    			double        third_degrees        = 0;                                                        //c
        	    			if(first_degrees < second_degrees)
        	    			   third_degrees = second_degrees - first_degrees;
        	    			else
        	    				third_degrees = first_degrees - second_degrees;
        	    			double fourth_degrees =  90 - third_degrees;                                                      //d
        	    			double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
        	    			double side_length = DataMapper.getLength(first_line); 
        	    			double weight1 = segment_length / side_length;
        	    			double weight2 = (side_length - segment_length) / side_length;
        	    			cell_intensity[i][j] = weight1 * first_sample.intensity + weight2 * second_sample.intensity;
        	    			isInterpolated[i][j] = true;
        	    	    }
        	    	    else if(second_length < first_length && second_length < third_length)
        	    	    {
        	    	    	// Get bisecting average from second line--using x2, y2 and x3, y3
        	    	    	Line2D.Double reference = new Line2D.Double(x2, y2, xcenter, ycenter);
        	    	    	double        reference_length     = DataMapper.getLength(reference);
        	    			double        first_slope          = Math.abs(DataMapper.getSlope(first_line));          //B
        	    			double        second_slope         = Math.abs(DataMapper.getSlope(reference));
        	    			
        	    			// Get the degrees.
        	    			double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
        	    			double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
        	    			double        third_degrees        = 0;                                                        //c
        	    			if(first_degrees < second_degrees)
        	    			   third_degrees = second_degrees - first_degrees;
        	    			else
        	    				third_degrees = first_degrees - second_degrees;
        	    			double fourth_degrees =  90 - third_degrees;                                                      //d
        	    			double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
        	    			double side_length = DataMapper.getLength(second_line); 
        	    			double weight1 = segment_length / side_length;
        	    			double weight2 = (side_length - segment_length) / side_length;
        	    			cell_intensity[i][j] = weight1 * second_sample.intensity + weight2 * third_sample.intensity;
        	    			isInterpolated[i][j] = true;
        	    	    }
        	    	    else if(third_length < first_length && third_length < second_length)
        	    	    {
        	    	    	// Get bisecting average from third line--using x3, y3, x1, y1
        	    	    	Line2D.Double reference = new Line2D.Double(x3, y3, xcenter, ycenter);
        	    	    	double        reference_length     = DataMapper.getLength(reference);
        	    			double        first_slope          = Math.abs(DataMapper.getSlope(third_line));          //B
        	    			double        second_slope         = Math.abs(DataMapper.getSlope(reference));
        	    			
        	    			// Get the degrees.
        	    			double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
        	    			double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
        	    			double        third_degrees        = 0;                                                        //c
        	    			if(first_degrees < second_degrees)
        	    			   third_degrees = second_degrees - first_degrees;
        	    			else
        	    				third_degrees = first_degrees - second_degrees;
        	    			double fourth_degrees =  90 - third_degrees;                                                      //d
        	    			double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
        	    			double side_length = DataMapper.getLength(third_line); 
        	    			double weight1 = segment_length / side_length;
        	    			double weight2 = (side_length - segment_length) / side_length;
        	    			cell_intensity[i][j] = weight1 * second_sample.intensity + weight2 * third_sample.intensity;
        	    			isInterpolated[i][j] = true;
        	    	    } 
        	    	    else if(first_length == second_length)
        	    	    {
        	    	    	// Get x2, y2
        	    	    	cell_intensity[i][j] = second_sample.intensity;
        	    	    	isInterpolated[i][j] = true;
        	    	    }
        	    	    else if(second_length == third_length)
        	    	    {
        	    	    	// Get x3, y3
        	    	    	cell_intensity[i][j] = third_sample.intensity;
        	    	    	isInterpolated[i][j] = true;
        	    	    }
        	    	    else if(third_length == first_length)
        	    	    {
        	    	    	// Get x1, y1
        	    	    	cell_intensity[i][j] = first_sample.intensity;
        	    	    	isInterpolated[i][j] = true;
        	    	    }
        	    	}
        	    }
        	    //Test for this after looking for corner cases.  Same code as four corners.
        	    else if(neighborPopulated[1] && neighborPopulated[3] && neighborPopulated[4] && neighborPopulated[6])
        	    {
        	    	//System.out.println("Found four sides.");
        	    	int first_index  = 1;
        	    	int second_index = 3;
        	    	int third_index  = 6;
        	    	int fourth_index = 4;
        	    	ArrayList first_list  = (ArrayList)neighbor_list.get(first_index);
        	    	ArrayList second_list = (ArrayList)neighbor_list.get(second_index);
        	    	ArrayList third_list  = (ArrayList)neighbor_list.get(third_index);
        	    	ArrayList fourth_list = (ArrayList)neighbor_list.get(fourth_index);
        	    	
        	    	double x1,y1,x2,y2,x3,y3,x4,y4 = 0;
        	    	boolean foundBox = false;
        	    	outer: for(int m = 0; m < first_list.size(); m++)
     	    	    {
     	    	    	for(int n = 0; n < second_list.size(); n++)
     	    	    	{
     	    	    		for(int p = 0; p < third_list.size(); p++)
     	    	    		{
     	    	    			for(int q = 0; q < fourth_list.size(); q++)
     	    	    			{
     	    	    			    Sample first_sample  = (Sample)first_list.get(m);
     	    	    			    x1 = first_sample.x;
     	        	    	        y1 = first_sample.y;
     	        	    	        Sample second_sample = (Sample)second_list.get(n);
     	        	    	        x2 = second_sample.x;
     	        	    		    y2 = second_sample.y;
     	        	    		    Sample third_sample  = (Sample)third_list.get(p);
     	        	    		    x3 = third_sample.x;
     	        	    		    y3 = third_sample.y;
     	        	    		    Sample fourth_sample = (Sample)fourth_list.get(q);
     	        	    		    x4 = fourth_sample.x;
     	        	    		    y4 = fourth_sample.y;
     	        	    		    Path2D.Double path = new Path2D.Double();
     	        		    	    path.moveTo(x1, y1);
     	        		    	    path.lineTo(x2, y2);
     	        		    	    path.lineTo(x3, y3);
     	        		    	    path.lineTo(x4, y4);
     	        		    	    path.closePath();
     	    	    		        if(path.contains(xcenter, ycenter)) 
     	    	    		        {
     	    	    		        	foundBox = true;
     	    	    		        	Point2D.Double upper_left  = new Point2D.Double(x1, y1);
        	    	    		        Point2D.Double upper_right = new Point2D.Double(x2, y2);
        	    	    		        Point2D.Double lower_right = new Point2D.Double(x3, y3);
        	    	    		        Point2D.Double lower_left  = new Point2D.Double(x4, y4);  
        	    	    		        Point2D.Double origin      = new Point2D.Double(xcenter, ycenter);
        	    	    		          
        	    	    			    Line2D.Double top      = new Line2D.Double(upper_left, upper_right);
        	    	    			    Line2D.Double left     = new Line2D.Double(upper_left, lower_left);
        	    	    			    Line2D.Double bottom   = new Line2D.Double(lower_left, lower_right);
        	    	    			    Line2D.Double right    = new Line2D.Double(lower_right, upper_right);
        	    	    		    	
        	    	                    //We need to get four more points to do our bilinear interpolation.
        	    	    			    double x5, y5, x6, y6;
        	    	    			    double slope = DataMapper.getSlope(top);
        	    	    			    double y_intercept = DataMapper.getYIntercept(upper_left, slope);
        	    	    			    y5 = slope * xcenter + y_intercept;
        	    	    			    slope = DataMapper.getSlope(bottom);
        	    	    			    y_intercept = DataMapper.getYIntercept(lower_left, slope);
        	    	    			    y6 = slope * xcenter + y_intercept;
        	    	    			    
        	    	    			    if(x2 != x3)
        	    	    			    {
        	    	    			        slope = DataMapper.getSlope(right);
        	    	    			        y_intercept = DataMapper.getYIntercept(upper_right, slope);
        	    	    			        x5 = (ycenter - y_intercept) / slope;
        	    	    			    }
        	    	    			    else
        	    	    			    {
        	    	    			    	double distance = right.ptSegDist(origin);
        	    	    			    	x5              = xcenter + distance;
        	    	    			    }
        	    	    			    
        	    	    			    if(x1 != x4)
        	    	    			    {
        	    	    			        slope = DataMapper.getSlope(left);
        	    	    			        y_intercept = DataMapper.getYIntercept(lower_left, slope);
        	    	    			        x6 = (ycenter - y_intercept) / slope;
        	    	    			    }
        	    	    			    else
        	    	    			    {
        	    	    			    	double distance = left.ptSegDist(origin);
        	    	    			    	x6              = xcenter - distance;
        	    	    			    }
        	    	    			    
        	    	    			    
        	    	    			    Point2D.Double middle_top  = new Point2D.Double(xcenter, y5);
        	    	    		        Point2D.Double middle_bottom = new Point2D.Double(xcenter, y6);
        	    	    		        Point2D.Double middle_right = new Point2D.Double(x5, ycenter);
        	    	    		        Point2D.Double middle_left  = new Point2D.Double(x6, ycenter); 
        	    	    		        
        	    	    		        double area1 = DataMapper.getQuadrilateralArea(middle_left, upper_left, middle_top, origin);
        	    	    		        double area2 = DataMapper.getQuadrilateralArea(origin, middle_top, upper_right, middle_right);
        	    	    		        double area3 = DataMapper.getQuadrilateralArea(middle_bottom, origin, middle_right, lower_right);
        	    	    		        double area4 = DataMapper.getQuadrilateralArea(lower_left, middle_left, origin, middle_bottom);
        	    	    		        double total_area = area1 + area2 + area3 + area4;
        	    	    		        
        	    	    		        total_area =  DataMapper.getQuadrilateralArea(lower_left, upper_left, upper_right, lower_right);
        	    	    		
        	    	    		        double weight1 = area3 / total_area;
        	    	    		        double weight2 = area4 / total_area;
        	    	    		        double weight3 = area1 / total_area;
        	    	    		        double weight4 = area2 / total_area;
        	    	    		        
        	    	    		        double intensity = first_sample.intensity * weight1 + 
        	    	    		        		           second_sample.intensity * weight2 + 
        	    	    		        		           third_sample.intensity * weight3 +
        	    	    		        		           fourth_sample.intensity * weight4;
        	    	    		        cell_intensity[i][j] = intensity;
        	    	    		      
     	    	    		        	isInterpolated[i][j] = true;
        	    	    		        break outer;	
     	    	    		        }
     	    	    			}
     	    	    		}
     	    	    	}
     	    	    }
        	    	if(!foundBox)
        	    	{
        	    		//We should really throw an exception, like we more or less do in 4 corners.
        	    		//We won't bother for now.
        	    	}
        	    }
        	    // This is a small number of samples with the current set, and is also 
        	    // more complex to process.  The payoff would be with a sparser data set.
        	    // Check for three sides after checking four sides.
        	    // Mostly the same code as three corners and two corners and a side, but we wanted to
        	    // check for four sides first.
        	    else if((neighborPopulated[1] && neighborPopulated[3] && neighborPopulated[4]) ||
        	    		(neighborPopulated[3] && neighborPopulated[4] && neighborPopulated[6]) ||
        	    		(neighborPopulated[4] && neighborPopulated[6] && neighborPopulated[1]) ||
        	    		(neighborPopulated[6] && neighborPopulated[1] && neighborPopulated[3]))
        	    {
        	    	//System.out.println("Found three sides.");
        	    	int first_index  = 0;
        	    	int second_index = 0;
        	    	int third_index  = 0;
        	    	
        	    	if(neighborPopulated[1] && neighborPopulated[3] && neighborPopulated[4])
        	    	{
        	    		first_index  = 1;
        	    		second_index = 3;
        	    		third_index  = 4;
        	    	}
        	    	else if(neighborPopulated[3] && neighborPopulated[4] && neighborPopulated[6])
        	    	{
        	    		first_index  = 3;
        	    		second_index = 4;
        	    		third_index  = 6;
        	    	}
        	    	else if(neighborPopulated[4] && neighborPopulated[6] && neighborPopulated[1])
        	    	{
        	    		first_index  = 4;
        	    		second_index = 6;
        	    		third_index  = 1;
        	    	}
        	    	else if(neighborPopulated[6] && neighborPopulated[1] && neighborPopulated[3])
        	    	{
        	    		first_index  = 6;
        	    		second_index = 1;
        	    		third_index  = 3;
        	    	}
        	    	ArrayList first_list  = (ArrayList)neighbor_list.get(first_index);
        	    	ArrayList second_list = (ArrayList)neighbor_list.get(second_index);
        	    	ArrayList third_list  = (ArrayList)neighbor_list.get(third_index);
        	    	double x1,y1,x2,y2,x3,y3 = 0;
        	    	boolean foundTriangle = false;
        	    	outer: for(int m = 0; m < first_list.size(); m++)
        	    	{
        	    	    for(int n = 0; n < second_list.size(); n++)
        	    	    {
        	    	    	for(int p = 0; p < third_list.size(); p++)
        	    	    	{
        	    	    		Sample first_sample  = (Sample)first_list.get(m);
        	    	    		x1 = first_sample.x;
        	        	    	y1 = first_sample.y; 
        	        	    	Sample second_sample = (Sample)second_list.get(n);
        	        	    	x2 = second_sample.x;
        	        	    	y2 = second_sample.y;
        	        	    	Sample third_sample  = (Sample)third_list.get(p);
        	        	    	x3 = third_sample.x;
        	        	    	y3 = third_sample.y;
        	        	    	Path2D.Double path = new Path2D.Double();
        	        		    path.moveTo(x1, y1);
        	        		    path.lineTo(x2, y2);
        	        		    path.lineTo(x3, y3);
        	        		    path.closePath();
        	    	    		if(path.contains(xcenter, ycenter)) 
        	    	    		{
        	    	    			foundTriangle = true;
        	    	    			Point2D.Double base1  = new Point2D.Double(x1, y1);
    	    	    		        Point2D.Double top    = new Point2D.Double(x2, y2);
    	    	    		        Point2D.Double base2  = new Point2D.Double(x3, y3); 
    	    	    		        Point2D.Double origin = new Point2D.Double(xcenter, ycenter);
    	    	    		        
    	    	    		        double area1 = DataMapper.getTriangleArea(origin, base2, top);
    	    	    		        double area2 = DataMapper.getTriangleArea(base1, base2, origin);
    	    	    		        double area3 = DataMapper.getTriangleArea(base1, origin, top);
    	    	    		        
    	    	    		        double total_area = area1 + area2 + area3;
    	    	    		        double weight1    = area1 / total_area;
    	    	    		        double weight2    = area2 / total_area;
    	    	    		        double weight3    = area3 / total_area;
    	    	    		        
    	    	    		        double intensity = first_sample.intensity * weight1 + 
    	    	    		        		           second_sample.intensity * weight2 + 
    	    	    		        		           third_sample.intensity * weight3;
    	    	    		        cell_intensity[i][j] = intensity;
        	    	    			isInterpolated[i][j] = true;
    	    	    		        break outer;
        	    	    		}
        	    	    	}
        	    	    }
        	    	}
        	    	if(!foundTriangle)
        	    	{
        	    		//Do a weighted average of the nearest neighbors.
                	    //Sample first_sample  = (Sample)first_list.get(0);
                	    //Sample second_sample = (Sample)second_list.get(0);
        	        	//Sample third_sample  = (Sample)third_list.get(0);
        	        	//double total_distance = 0.;
        	        	//total_distance += first_sample.distance;
        	        	//total_distance += second_sample.distance;
        	        	//total_distance += third_sample.distance;
        	        	//double weight1 = first_sample.distance / total_distance;
        	        	//double weight2 = second_sample.distance / total_distance;
        	        	//double weight3 = third_sample.distance / total_distance;
        	        	//double intensity = first_sample.intensity * weight1 + second_sample.intensity * weight2 + third_sample.intensity * weight3;
        	    		Sample first_sample = (Sample)first_list.get(0);
        	    	    x1           = first_sample.x;
        	    	    y1           = first_sample.y;
        	    	    
        	    	    Sample second_sample  = (Sample)second_list.get(0);
        	    	    x2             = second_sample.x;
        	    	    y2             = second_sample.y;
        	    	    
        	    	    Sample third_sample = (Sample)third_list.get(0);
        	    	    x3           = third_sample.x;
        	    	    y3           = third_sample.y;
    	    		    
        	    	    
        	    	    Line2D.Double first_line  = new Line2D.Double(x1, y1, x2, y2);
        	    	    Line2D.Double second_line = new Line2D.Double(x2, y2, x3, y3);
        	    	    Line2D.Double third_line  = new Line2D.Double(x3, y3, x1, y1);
        	    	    
        	    	    double first_length       = first_line.ptSegDist(xcenter, ycenter);	
        	    	    double second_length      = second_line.ptSegDist(xcenter, ycenter);
        	    	    double third_length       = third_line.ptSegDist(xcenter, ycenter);
        	    	    if(first_length < second_length && first_length < third_length)
        	    	    {
        	    	    	// Get bisecting average from first line--using x1, y1 and x2, y2
        	    	    	Line2D.Double reference = new Line2D.Double(x1, y1, xcenter, ycenter);
        	    	    	double        reference_length     = DataMapper.getLength(reference);
        	    			double        first_slope          = Math.abs(DataMapper.getSlope(first_line));          //B
        	    			double        second_slope         = Math.abs(DataMapper.getSlope(reference));
        	    			
        	    			// Get the degrees.
        	    			double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
        	    			double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
        	    			double        third_degrees        = 0;                                                        //c
        	    			if(first_degrees < second_degrees)
        	    			   third_degrees = second_degrees - first_degrees;
        	    			else
        	    				third_degrees = first_degrees - second_degrees;
        	    			double fourth_degrees =  90 - third_degrees;                                                      //d
        	    			double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
        	    			double side_length = DataMapper.getLength(first_line); 
        	    			double weight1 = segment_length / side_length;
        	    			double weight2 = (side_length - segment_length) / side_length;
        	    			cell_intensity[i][j] = weight1 * first_sample.intensity + weight2 * second_sample.intensity;
        	    			isInterpolated[i][j] = true;
        	    	    }
        	    	    else if(second_length < first_length && second_length < third_length)
        	    	    {
        	    	    	// Get bisecting average from second line--using x2, y2 and x3, y3
        	    	    	Line2D.Double reference = new Line2D.Double(x2, y2, xcenter, ycenter);
        	    	    	double        reference_length     = DataMapper.getLength(reference);
        	    			double        first_slope          = Math.abs(DataMapper.getSlope(first_line));          //B
        	    			double        second_slope         = Math.abs(DataMapper.getSlope(reference));
        	    			
        	    			// Get the degrees.
        	    			double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
        	    			double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
        	    			double        third_degrees        = 0;                                                        //c
        	    			if(first_degrees < second_degrees)
        	    			   third_degrees = second_degrees - first_degrees;
        	    			else
        	    				third_degrees = first_degrees - second_degrees;
        	    			double fourth_degrees =  90 - third_degrees;                                                      //d
        	    			double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
        	    			double side_length = DataMapper.getLength(second_line); 
        	    			double weight1 = segment_length / side_length;
        	    			double weight2 = (side_length - segment_length) / side_length;
        	    			cell_intensity[i][j] = weight1 * second_sample.intensity + weight2 * third_sample.intensity;
        	    			isInterpolated[i][j] = true;
        	    	    }
        	    	    else if(third_length < first_length && third_length < second_length)
        	    	    {
        	    	    	// Get bisecting average from third line--using x3, y3, x1, y1
        	    	    	Line2D.Double reference = new Line2D.Double(x3, y3, xcenter, ycenter);
        	    	    	double        reference_length     = DataMapper.getLength(reference);
        	    			double        first_slope          = Math.abs(DataMapper.getSlope(third_line));          //B
        	    			double        second_slope         = Math.abs(DataMapper.getSlope(reference));
        	    			
        	    			// Get the degrees.
        	    			double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
        	    			double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
        	    			double        third_degrees        = 0;                                                        //c
        	    			if(first_degrees < second_degrees)
        	    			   third_degrees = second_degrees - first_degrees;
        	    			else
        	    				third_degrees = first_degrees - second_degrees;
        	    			double fourth_degrees =  90 - third_degrees;                                                      //d
        	    			double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
        	    			double side_length = DataMapper.getLength(third_line); 
        	    			double weight1 = segment_length / side_length;
        	    			double weight2 = (side_length - segment_length) / side_length;
        	    			cell_intensity[i][j] = weight1 * second_sample.intensity + weight2 * third_sample.intensity;
        	    			isInterpolated[i][j] = true;
        	    	    } 
        	    	    else if(first_length == second_length)
        	    	    {
        	    	    	// Get x2, y2
        	    	    	cell_intensity[i][j] = second_sample.intensity;
        	    	    	isInterpolated[i][j] = true;
        	    	    }
        	    	    else if(second_length == third_length)
        	    	    {
        	    	    	// Get x3, y3
        	    	    	cell_intensity[i][j] = third_sample.intensity;
        	    	    	isInterpolated[i][j] = true;
        	    	    }
        	    	    else if(third_length == first_length)
        	    	    {
        	    	    	// Get x1, y1
        	    	    	cell_intensity[i][j] = first_sample.intensity;
        	    	    	isInterpolated[i][j] = true;
        	    	    }
	
        	    	}
        	    }
        	    else if((neighborPopulated[1] && neighborPopulated[6]) ||
        	    		(neighborPopulated[3] && neighborPopulated[4]))
        	    {
        	    	//System.out.println("Found two opposing sides.");
        	    	int first_index  = 0;
        	    	int second_index = 0;
        	    	boolean isVertical = false;
        	    	
        	    	if(neighborPopulated[1] && neighborPopulated[6])
        	    	{
        	    		first_index = 1;
        	    		second_index = 6;
        	    		isVertical   = true;
        	    	}
        	    	else if(neighborPopulated[3] && neighborPopulated[4])
        	    	{
        	    		first_index = 3;
        	    		second_index = 4;
        	    	}
        	    		
        	    	
        	    	ArrayList first_list  = (ArrayList)neighbor_list.get(first_index);
        	    	ArrayList second_list = (ArrayList)neighbor_list.get(second_index);
        	    	double x1,y1,x2,y2,x3,y3,x4,y4 = 0;
        	    	
        	    	Sample first_sample  = (Sample)first_list.get(0);
        	    	x1                   = first_sample.x;
        	    	y1                   = first_sample.y;
                    if(first_list.size() == 1)
                    {
                    	Sample second_sample = (Sample)second_list.get(0);
                    	x2 = second_sample.x;
                    	y2 = second_sample.y;
                    	if(second_list.size() == 1)
                    	{
                    		first_sample  = (Sample)first_list.get(0);
            	    		second_sample = (Sample)second_list.get(0);
    	        	    	double total_distance = 0.;
    	        	        total_distance += first_sample.distance;
    	        	        total_distance += second_sample.distance;
    	        	        double weight1 = first_sample.distance / total_distance;
    	        	        double weight2 = second_sample.distance / total_distance; 
    	        	        double intensity = first_sample.intensity * weight1 + second_sample.intensity * weight2;
    	        	        cell_intensity[i][j] = intensity;
                    		isInterpolated[i][j] = true;
                    	}
                    	else
                    	{
                    		boolean foundBoundingTriangle = false;
                    		//Try triangles.
                    		//A little different from our previous cases
                    		//because we're working with two lists, not three.
                    		outer: for(int m = 1; m < second_list.size(); m++)
                    		{
                    		    Sample third_sample = (Sample)second_list.get(m);
                    		    x3                  = third_sample.x;
                    		    y3                  = third_sample.y;
                    		    Path2D.Double path = new Path2D.Double();
        	        		    path.moveTo(x1, y1);
        	        		    path.lineTo(x2, y2);
        	        		    path.lineTo(x3, y3);
        	        		    path.closePath();
        	    	    		if(path.contains(xcenter, ycenter)) 
        	    	    		{
        	    	    			Point2D.Double base1  = new Point2D.Double(x1, y1);
    	    	    		        Point2D.Double top    = new Point2D.Double(x2, y2);
    	    	    		        Point2D.Double base2  = new Point2D.Double(x3, y3); 
    	    	    		        Point2D.Double origin = new Point2D.Double(xcenter, ycenter);
    	    	    		        
    	    	    		        double area1 = DataMapper.getTriangleArea(origin, base2, top);
    	    	    		        double area2 = DataMapper.getTriangleArea(base1, base2, origin);
    	    	    		        double area3 = DataMapper.getTriangleArea(base1, origin, top);
    	    	    		        
    	    	    		        double total_area = area1 + area2 + area3;
    	    	    		        double weight1    = area1 / total_area;
    	    	    		        double weight2    = area2 / total_area;
    	    	    		        double weight3    = area3 / total_area;
    	    	    		        
    	    	    		        double intensity = first_sample.intensity * weight1 + 
    	    	    		        		           second_sample.intensity * weight2 + 
    	    	    		        		           third_sample.intensity * weight3;
    	    	    		        
    	    	    		        cell_intensity[i][j] = intensity;
        	    	    			isInterpolated[i][j] = true;
        	    	    			foundBoundingTriangle = true;
    	    	    		        break outer;
        	    	    		}
                    		}
                    		if(!foundBoundingTriangle)
                    		{
                    			//first_sample  = (Sample)first_list.get(0);
                	    		//second_sample = (Sample)second_list.get(0);
        	        	    	//double total_distance = 0.;
        	        	        //total_distance += first_sample.distance;
        	        	        //total_distance += second_sample.distance;
        	        	        //double weight1 = first_sample.distance / total_distance;
        	        	        //double weight2 = second_sample.distance / total_distance; 
        	        	        //double intensity = first_sample.intensity * weight1 + second_sample.intensity * weight2;
                    			first_sample = (Sample)first_list.get(0);
                	    	    x1           = first_sample.x;
                	    	    y1           = first_sample.y;
                	    	    
                	    	    second_sample  = (Sample)second_list.get(0);
                	    	    x2             = second_sample.x;
                	    	    y2             = second_sample.y;
                	    	    
                	    	    Sample third_sample = (Sample)second_list.get(1);
                	    	    x3           = third_sample.x;
                	    	    y3           = third_sample.y;
            	    		    
                	    	    
                	    	    Line2D.Double first_line  = new Line2D.Double(x1, y1, x2, y2);
                	    	    Line2D.Double second_line = new Line2D.Double(x2, y2, x3, y3);
                	    	    Line2D.Double third_line  = new Line2D.Double(x3, y3, x1, y1);
                	    	    
                	    	    double first_length       = first_line.ptSegDist(xcenter, ycenter);	
                	    	    double second_length      = second_line.ptSegDist(xcenter, ycenter);
                	    	    double third_length       = third_line.ptSegDist(xcenter, ycenter);
                	    	    if(first_length < second_length && first_length < third_length)
                	    	    {
                	    	    	// Get bisecting average from first line--using x1, y1 and x2, y2
                	    	    	Line2D.Double reference = new Line2D.Double(x1, y1, xcenter, ycenter);
                	    	    	double        reference_length     = DataMapper.getLength(reference);
                	    			double        first_slope          = Math.abs(DataMapper.getSlope(first_line));          //B
                	    			double        second_slope         = Math.abs(DataMapper.getSlope(reference));
                	    			
                	    			// Get the degrees.
                	    			double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
                	    			double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
                	    			double        third_degrees        = 0;                                                        //c
                	    			if(first_degrees < second_degrees)
                	    			   third_degrees = second_degrees - first_degrees;
                	    			else
                	    				third_degrees = first_degrees - second_degrees;
                	    			double fourth_degrees =  90 - third_degrees;                                                      //d
                	    			double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
                	    			double side_length = DataMapper.getLength(first_line); 
                	    			double weight1 = segment_length / side_length;
                	    			double weight2 = (side_length - segment_length) / side_length;
                	    			cell_intensity[i][j] = weight1 * first_sample.intensity + weight2 * second_sample.intensity;
                	    			isInterpolated[i][j] = true;
                	    	    }
                	    	    else if(second_length < first_length && second_length < third_length)
                	    	    {
                	    	    	// Get bisecting average from second line--using x2, y2 and x3, y3
                	    	    	Line2D.Double reference = new Line2D.Double(x2, y2, xcenter, ycenter);
                	    	    	double        reference_length     = DataMapper.getLength(reference);
                	    			double        first_slope          = Math.abs(DataMapper.getSlope(first_line));          //B
                	    			double        second_slope         = Math.abs(DataMapper.getSlope(reference));
                	    			
                	    			// Get the degrees.
                	    			double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
                	    			double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
                	    			double        third_degrees        = 0;                                                        //c
                	    			if(first_degrees < second_degrees)
                	    			   third_degrees = second_degrees - first_degrees;
                	    			else
                	    				third_degrees = first_degrees - second_degrees;
                	    			double fourth_degrees =  90 - third_degrees;                                                      //d
                	    			double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
                	    			double side_length = DataMapper.getLength(second_line); 
                	    			double weight1 = segment_length / side_length;
                	    			double weight2 = (side_length - segment_length) / side_length;
                	    			cell_intensity[i][j] = weight1 * second_sample.intensity + weight2 * third_sample.intensity;
                	    			isInterpolated[i][j] = true;
                	    	    }
                	    	    else if(third_length < first_length && third_length < second_length)
                	    	    {
                	    	    	// Get bisecting average from third line--using x3, y3, x1, y1
                	    	    	Line2D.Double reference = new Line2D.Double(x3, y3, xcenter, ycenter);
                	    	    	double        reference_length     = DataMapper.getLength(reference);
                	    			double        first_slope          = Math.abs(DataMapper.getSlope(third_line));          //B
                	    			double        second_slope         = Math.abs(DataMapper.getSlope(reference));
                	    			
                	    			// Get the degrees.
                	    			double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
                	    			double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
                	    			double        third_degrees        = 0;                                                        //c
                	    			if(first_degrees < second_degrees)
                	    			   third_degrees = second_degrees - first_degrees;
                	    			else
                	    				third_degrees = first_degrees - second_degrees;
                	    			double fourth_degrees =  90 - third_degrees;                                                      //d
                	    			double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
                	    			double side_length = DataMapper.getLength(third_line); 
                	    			double weight1 = segment_length / side_length;
                	    			double weight2 = (side_length - segment_length) / side_length;
                	    			cell_intensity[i][j] = weight1 * second_sample.intensity + weight2 * third_sample.intensity;
                	    			isInterpolated[i][j] = true;
                	    	    } 
                	    	    else if(first_length == second_length)
                	    	    {
                	    	    	// Get x2, y2
                	    	    	cell_intensity[i][j] = second_sample.intensity;
                	    	    	isInterpolated[i][j] = true;
                	    	    }
                	    	    else if(second_length == third_length)
                	    	    {
                	    	    	// Get x3, y3
                	    	    	cell_intensity[i][j] = third_sample.intensity;
                	    	    	isInterpolated[i][j] = true;
                	    	    }
                	    	    else if(third_length == first_length)
                	    	    {
                	    	    	// Get x1, y1
                	    	    	cell_intensity[i][j] = first_sample.intensity;
                	    	    	isInterpolated[i][j] = true;
                	    	    }
                    		}
                    	}
                    }
                    else
                    {
                    	//Try triangles. First list has at least two samples.
                    	Sample second_sample;
                    	Sample third_sample;
                        if(second_list.size() == 1)	
                        {
                        	third_sample = (Sample)second_list.get(0);
                		    x3 = third_sample.x;
                		    y3 = third_sample.y;
                		    boolean foundBoundingTriangle = false;
                    		//Try triangles.
                    		outer: for(int m = 1; m < first_list.size(); m++)
                    		{
                    		    second_sample = (Sample)first_list.get(m);
                    		    x2 = second_sample.x;
                    		    y2 = second_sample.y;
                    		    Path2D.Double path = new Path2D.Double();
        	        		    path.moveTo(x1, y1);
        	        		    path.lineTo(x2, y2);
        	        		    path.lineTo(x3, y3);
        	        		    path.closePath();
        	    	    		if(path.contains(xcenter, ycenter)) 
        	    	    		{
        	    	    			Point2D.Double base1  = new Point2D.Double(x1, y1);
    	    	    		        Point2D.Double top    = new Point2D.Double(x2, y2);
    	    	    		        Point2D.Double base2  = new Point2D.Double(x3, y3); 
    	    	    		        Point2D.Double origin = new Point2D.Double(xcenter, ycenter);
    	    	    		        
    	    	    		        double area1 = DataMapper.getTriangleArea(origin, base2, top);
    	    	    		        double area2 = DataMapper.getTriangleArea(base1, base2, origin);
    	    	    		        double area3 = DataMapper.getTriangleArea(base1, origin, top);
    	    	    		        
    	    	    		        double total_area = area1 + area2 + area3;
    	    	    		        double weight1    = area1 / total_area;
    	    	    		        double weight2    = area2 / total_area;
    	    	    		        double weight3    = area3 / total_area;
    	    	    		        
    	    	    		        double intensity = first_sample.intensity * weight1 + 
    	    	    		        		           second_sample.intensity * weight2 + 
    	    	    		        		           third_sample.intensity * weight3;
    	    	    		        
    	    	    		        cell_intensity[i][j] = intensity;
        	    	    			isInterpolated[i][j] = true;
        	    	    			foundBoundingTriangle = true;
        	    	    			//Do a linear interpolation
    	    	    		        break outer;
        	    	    		}
                    		}
                    		if(!foundBoundingTriangle)
                    		{
                    		    //Do the weighted average of the two closest samples and call it interpolated.
                    			//first_sample  = (Sample)first_list.get(0);
                	    		//second_sample = (Sample)second_list.get(0);
        	        	    	//double total_distance = 0.;
        	        	        //total_distance += first_sample.distance;
        	        	        //total_distance += second_sample.distance;
        	        	        //double weight1 = first_sample.distance / total_distance;
        	        	        //double weight2 = second_sample.distance / total_distance;    
        	        	        //double intensity = first_sample.intensity * weight1 + second_sample.intensity * weight2;
                    			first_sample = (Sample)first_list.get(0);
                	    	    x1           = first_sample.x;
                	    	    y1           = first_sample.y;
                	    	    
                	    	    second_sample  = (Sample)second_list.get(0);
                	    	    x2             = second_sample.x;
                	    	    y2             = second_sample.y;
                	    	    
                	    	    third_sample = (Sample)second_list.get(0);
                	    	    x3           = third_sample.x;
                	    	    y3           = third_sample.y;
            	    		    
                	    	    
                	    	    Line2D.Double first_line  = new Line2D.Double(x1, y1, x2, y2);
                	    	    Line2D.Double second_line = new Line2D.Double(x2, y2, x3, y3);
                	    	    Line2D.Double third_line  = new Line2D.Double(x3, y3, x1, y1);
                	    	    
                	    	    double first_length       = first_line.ptSegDist(xcenter, ycenter);	
                	    	    double second_length      = second_line.ptSegDist(xcenter, ycenter);
                	    	    double third_length       = third_line.ptSegDist(xcenter, ycenter);
                	    	    if(first_length < second_length && first_length < third_length)
                	    	    {
                	    	    	// Get bisecting average from first line--using x1, y1 and x2, y2
                	    	    	Line2D.Double reference = new Line2D.Double(x1, y1, xcenter, ycenter);
                	    	    	double        reference_length     = DataMapper.getLength(reference);
                	    			double        first_slope          = Math.abs(DataMapper.getSlope(first_line));          //B
                	    			double        second_slope         = Math.abs(DataMapper.getSlope(reference));
                	    			
                	    			// Get the degrees.
                	    			double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
                	    			double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
                	    			double        third_degrees        = 0;                                                        //c
                	    			if(first_degrees < second_degrees)
                	    			   third_degrees = second_degrees - first_degrees;
                	    			else
                	    				third_degrees = first_degrees - second_degrees;
                	    			double fourth_degrees =  90 - third_degrees;                                                      //d
                	    			double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
                	    			double side_length = DataMapper.getLength(first_line); 
                	    			double weight1 = segment_length / side_length;
                	    			double weight2 = (side_length - segment_length) / side_length;
                	    			cell_intensity[i][j] = weight1 * first_sample.intensity + weight2 * second_sample.intensity;
                	    			isInterpolated[i][j] = true;
                	    	    }
                	    	    else if(second_length < first_length && second_length < third_length)
                	    	    {
                	    	    	// Get bisecting average from second line--using x2, y2 and x3, y3
                	    	    	Line2D.Double reference = new Line2D.Double(x2, y2, xcenter, ycenter);
                	    	    	double        reference_length     = DataMapper.getLength(reference);
                	    			double        first_slope          = Math.abs(DataMapper.getSlope(first_line));          //B
                	    			double        second_slope         = Math.abs(DataMapper.getSlope(reference));
                	    			
                	    			// Get the degrees.
                	    			double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
                	    			double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
                	    			double        third_degrees        = 0;                                                        //c
                	    			if(first_degrees < second_degrees)
                	    			   third_degrees = second_degrees - first_degrees;
                	    			else
                	    				third_degrees = first_degrees - second_degrees;
                	    			double fourth_degrees =  90 - third_degrees;                                                      //d
                	    			double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
                	    			double side_length = DataMapper.getLength(second_line); 
                	    			double weight1 = segment_length / side_length;
                	    			double weight2 = (side_length - segment_length) / side_length;
                	    			cell_intensity[i][j] = weight1 * second_sample.intensity + weight2 * third_sample.intensity;
                	    			isInterpolated[i][j] = true;
                	    	    }
                	    	    else if(third_length < first_length && third_length < second_length)
                	    	    {
                	    	    	// Get bisecting average from third line--using x3, y3, x1, y1
                	    	    	Line2D.Double reference = new Line2D.Double(x3, y3, xcenter, ycenter);
                	    	    	double        reference_length     = DataMapper.getLength(reference);
                	    			double        first_slope          = Math.abs(DataMapper.getSlope(third_line));          //B
                	    			double        second_slope         = Math.abs(DataMapper.getSlope(reference));
                	    			
                	    			// Get the degrees.
                	    			double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
                	    			double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
                	    			double        third_degrees        = 0;                                                        //c
                	    			if(first_degrees < second_degrees)
                	    			   third_degrees = second_degrees - first_degrees;
                	    			else
                	    				third_degrees = first_degrees - second_degrees;
                	    			double fourth_degrees =  90 - third_degrees;                                                      //d
                	    			double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
                	    			double side_length = DataMapper.getLength(third_line); 
                	    			double weight1 = segment_length / side_length;
                	    			double weight2 = (side_length - segment_length) / side_length;
                	    			cell_intensity[i][j] = weight1 * second_sample.intensity + weight2 * third_sample.intensity;
                	    			isInterpolated[i][j] = true;
                	    	    } 
                	    	    else if(first_length == second_length)
                	    	    {
                	    	    	// Get x2, y2
                	    	    	cell_intensity[i][j] = second_sample.intensity;
                	    	    	isInterpolated[i][j] = true;
                	    	    }
                	    	    else if(second_length == third_length)
                	    	    {
                	    	    	// Get x3, y3
                	    	    	cell_intensity[i][j] = third_sample.intensity;
                	    	    	isInterpolated[i][j] = true;
                	    	    }
                	    	    else if(third_length == first_length)
                	    	    {
                	    	    	// Get x1, y1
                	    	    	cell_intensity[i][j] = first_sample.intensity;
                	    	    	isInterpolated[i][j] = true;
                	    	    }
                    		}
                        }
                        else
                        {
                        	boolean foundBoundingBox = false;
                        	//Need this information when we break out of loop.
                        	//Previously declared someplace.
                        	//int first_index;
            		        //int second_index;
            		        int third_index;
            		        int fourth_index;
            		        
                        	//Try boxes.  Both lists have at least two samples.
                        	second_sample = (Sample)second_list.get(0);
                        	x2                   = second_sample.x;
                        	y2                   = second_sample.y;
                        	// We're always using these points.
                        	// Could try using all possibilities, including without these points.
                        	// At least they're the closest points.
                        	outer: for(int m = 1; m < first_list.size(); m++)
                    		{
                        		Sample fourth_sample = (Sample)first_list.get(m);
                        		x4                   = fourth_sample.x;
                        		y4                   = fourth_sample.y;
                        		for(int n = 1; n < second_list.size(); n++)
                        		{
                    		        third_sample        = (Sample)second_list.get(n);
                    		        x3                  = third_sample.x;
                    		        y3                  = third_sample.y;
                    		        Path2D.Double path  = new Path2D.Double();
                    		        //Previously declared somewhere.  Might want to clean it up.
                    		        
                    		        //We didn't need to do this kind of sorting previously
                    		        //because our lists all came from different quadrants.
                    		        //All we know about x1, y1 and x4, y4 is that x1, y1 is
                    		        //closer to the origin.
                    		        if(isVertical)
                    		        {
                    		            if(x1 < x4)
                    		            {
                    		            	//Make it the upper left corner.
                    		            	first_index = 1;
                    		            	fourth_index = 4;
                    		        	    path.moveTo(x1, y1); 
                    		        	    
                    		        	    if(y2 > y3)
                    		        	    { 
                    		        	    	//Make it the upper right corner.
                    		        	    	second_index = 2;
                    		        	    	third_index  = 3;
                    		        		    path.lineTo(x2, y2);
                    		        		    path.lineTo(x3, y3);
                    		        	    }
                    		        	    else
                    		        	    {
                    		        	    	//Make it the upper right corner.
                    		        	    	second_index = 3;
                    		        	    	third_index  = 2;
                    		        		    path.lineTo(x3, y3);
                    		        		    path.lineTo(x4, y4);
                    		        	    }
                    		            }
                    		            else
                    		            { 
                    		            	//Make it the upper left corner.
                    		            	first_index = 4;
                    		            	fourth_index = 1;
                    		        	    path.moveTo(x4, y4);
                    		        	    if(y2 > y3)
                    		        	    { 
                    		        	    	//Make it the upper right corner.
                    		        	    	second_index = 2;
                    		        	    	third_index  = 3;
                    		        		    path.lineTo(x2, y2);
                    		        		    path.lineTo(x3, y3);
                    		        	    }
                    		        	    else
                    		        	    {
                    		        	    	//Make it the upper right corner.
                    		        	    	second_index = 3;
                    		        	    	third_index  = 2;
                    		        		    path.lineTo(x3, y3);
                    		        		    path.lineTo(x2, y2);
                    		        	    }
                    		            }
                    		        }
                    		        // We're going from side to side.
                    		        else
                    		        {
                    		        	if(y1 > y4)
                        		        {
                    		        		//Make it the upper left corner.
                    		        		first_index  = 1;
                    		        		fourth_index = 4;
                        		        	path.moveTo(x1, y1); 
                        		   
                        		        	if(y2 > y3)
                        		        	{ 
                        		        		//Make it the upper right corner.
                        		        		second_index = 2;
                        		        		third_index  = 3;
                        		        		path.lineTo(x2, y2);
                        		        		path.lineTo(x3, y3);
                        		        	}
                        		        	else
                        		        	{
                        		        		//Make it the upper right corner.
                        		        		second_index = 3;
                        		        		third_index  = 2;
                        		        		path.lineTo(x3, y3);
                        		        		path.lineTo(x2, y2);
                        		        	}
                        		        }
                        		        else
                        		        { 
                        		        	//Make it the upper left corner.
                        		        	path.moveTo(x4, y4);
                        		        	first_index = 4;
                        		        	fourth_index = 1;
                        		        	if(y2 > y3)
                        		        	{ 
                        		        		//Make it the upper right corner.
                        		        		second_index = 2;
                        		        		third_index  = 3;
                        		        		path.lineTo(x2, y2);
                        		        		path.lineTo(x3, y3);
                        		        	}
                        		        	else
                        		        	{
                        		        		//Make it the upper right corner.
                        		        		second_index = 3;
                        		        		third_index  = 2;
                        		        		path.lineTo(x3, y3);
                        		        		path.lineTo(x2, y2);
                        		        	}
                        		        }
                    		        }
                    		        path.closePath();
                    		        if(path.contains(xcenter, ycenter)) 
            	    	    		{
                    		        	foundBoundingBox           = true;
                    		        	Point2D.Double origin      = new Point2D.Double(xcenter, ycenter);
                    		        	Point2D.Double upper_left;
        	    	    		        Point2D.Double upper_right;
        	    	    		        Point2D.Double lower_right;
        	    	    		        Point2D.Double lower_left;
                    		        	
                    		        	if(first_index == 1)
                    		        	{
                    		        		upper_left  = new Point2D.Double(x1, y1);
                    		        		lower_left = new Point2D.Double(x4, y4);
                    		        	}
                    		        	else
                    		        	{
                    		        		upper_left  = new Point2D.Double(x4, y4);
                    		        		lower_left = new Point2D.Double(x1, y1);
                    		        	}
                    		        	if(second_index == 2)
                    		        	{
            	    	    		        upper_right = new Point2D.Double(x2, y2);
            	    	    		        lower_right = new Point2D.Double(x3, y3);
                    		        	}
                    		        	else
                    		        	{
                    		        		upper_right = new Point2D.Double(x3, y3);
            	    	    		        lower_right = new Point2D.Double(x2, y2);
                    		        	}
                    		        	  
        	    	    			    Line2D.Double top      = new Line2D.Double(upper_left, upper_right);
        	    	    			    Line2D.Double left     = new Line2D.Double(upper_left, lower_left);
        	    	    			    Line2D.Double bottom   = new Line2D.Double(lower_left, lower_right);
        	    	    			    Line2D.Double right    = new Line2D.Double(lower_right, upper_right);
        	    	    		    	
        	    	                    //We need to get four more points to do our bilinear interpolation.
        	    	    			    double x5, y5, x6, y6;
        	    	    			    
        	    	    			    //Thought the natural variation in data would eliminate the need for checking
        	    	    			    //for infinite slopes but it appears not.  Probably should look over other places
        	    	    			    //where I use getSlope.
        	    	    			    
        	    	    			    //Don't think I have to worry about zero.
        	    	    			    double slope = DataMapper.getSlope(top);
        	    	    			    double y_intercept = DataMapper.getYIntercept(upper_left, slope);
        	    	    			    y5 = slope * xcenter + y_intercept;
        	    	    			    slope = DataMapper.getSlope(bottom);
        	    	    			    y_intercept = DataMapper.getYIntercept(lower_left, slope);
        	    	    			    y6 = slope * xcenter + y_intercept;
        	    	    			    
        	    	    			    if(x2 != x3)
        	    	    			    {
        	    	    			        slope = DataMapper.getSlope(right);
        	    	    			        y_intercept = DataMapper.getYIntercept(upper_right, slope);
        	    	    			        x5 = (ycenter - y_intercept) / slope;
        	    	    			    }
        	    	    			    else
        	    	    			    {
        	    	    			    	double distance = right.ptSegDist(origin);
        	    	    			    	x5              = xcenter + distance;
        	    	    			    }
        	    	    			    
        	    	    			    if(x1 != x4)
        	    	    			    {
        	    	    			        slope = DataMapper.getSlope(left);
        	    	    			        y_intercept = DataMapper.getYIntercept(lower_left, slope);
        	    	    			        x6 = (ycenter - y_intercept) / slope;
        	    	    			    }
        	    	    			    else
        	    	    			    {
        	    	    			    	double distance = left.ptSegDist(origin);
        	    	    			    	x6              = xcenter - distance;
        	    	    			    }
        	    	    			    
        	    	    			    
        	    	    			    Point2D.Double middle_top  = new Point2D.Double(xcenter, y5);
        	    	    		        Point2D.Double middle_bottom = new Point2D.Double(xcenter, y6);
        	    	    		        Point2D.Double middle_right = new Point2D.Double(x5, ycenter);
        	    	    		        Point2D.Double middle_left  = new Point2D.Double(x6, ycenter); 
        	    	    		        
        	    	    		        double area1 = DataMapper.getQuadrilateralArea(middle_left, upper_left, middle_top, origin);
        	    	    		        double area2 = DataMapper.getQuadrilateralArea(origin, middle_top, upper_right, middle_right);
        	    	    		        double area3 = DataMapper.getQuadrilateralArea(middle_bottom, origin, middle_right, lower_right);
        	    	    		        double area4 = DataMapper.getQuadrilateralArea(lower_left, middle_left, origin, middle_bottom);
        	    	    		        double total_area = area1 + area2 + area3 + area4;
        	    	    		        double weight1 = area3 / total_area;
        	    	    		        double weight2 = area4 / total_area;
        	    	    		        double weight3 = area1 / total_area;
        	    	    		        double weight4 = area2 / total_area;
        	    	    		        
        	    	    		        double intensity = 0;
        	    	    		        
        	    	    		        if(first_index == 1)
        	    	    		        	intensity += first_sample.intensity * weight1 + fourth_sample.intensity * weight4;
        	    	    		        else
        	    	    		        	intensity += first_sample.intensity * weight4 + fourth_sample.intensity * weight1;
        	    	    		        if(second_index == 2)
        	    	    		        	intensity += second_sample.intensity * weight2 + third_sample.intensity * weight3;
        	    	    		        else
        	    	    		        	intensity += second_sample.intensity * weight3 + third_sample.intensity * weight2;
        	    	    		        
        	    	    		        cell_intensity[i][j] = intensity;
            	    	    			isInterpolated[i][j] = true;
            	    	    			foundBoundingBox = true;
        	    	    		        break outer;
            	    	    		}  
                        		}
                    		}
                        	if(!foundBoundingBox)
                    		{
                    		    //Do the weighted average of the two closest samples and call it interpolated.	
                        		//Its possible that all the samples were on one side of the cell center so
                        		//its not cause for an exception.
                        		//first_sample  = (Sample)first_list.get(0);
                	    		//second_sample = (Sample)second_list.get(0);
        	        	    	//double total_distance = 0.;
        	        	        //total_distance += first_sample.distance;
        	        	        //total_distance += second_sample.distance;
        	        	        //double weight1 = first_sample.distance / total_distance;
        	        	        //double weight2 = second_sample.distance / total_distance; 
        	        	        //double intensity = first_sample.intensity * weight1 + second_sample.intensity * weight2;
                        		
                        		//Since the origin lies outside the box we dont have to
                        		//worry about sorting points to make a quadrilateral.
                        		first_sample  = (Sample)first_list.get(0);
                        		second_sample = (Sample)first_list.get(1);
                        		third_sample  = (Sample)second_list.get(0);
                        		Sample fourth_sample = (Sample)second_list.get(1);
                        		x1 = first_sample.x;
                        		y1 = first_sample.y;
                        		x2 = second_sample.x;
                        		y2 = second_sample.y;
                        		x3 = third_sample.x;
                        		y3 = third_sample.y;
                        		x4 = fourth_sample.x;
                        		y4 = fourth_sample.y;
                        		Line2D.Double first_line  = new Line2D.Double(x1, y1, x2, y2);
                        		Line2D.Double second_line = new Line2D.Double(x2, y2, x3, y3);
                        		Line2D.Double third_line  = new Line2D.Double(x3, y3, x4, y4);
                        		Line2D.Double fourth_line = new Line2D.Double(x4, y4, x1, y1);
                        	    double first_distance  = first_line.ptSegDist(xcenter, ycenter);
                        	    double second_distance = second_line.ptSegDist(xcenter, ycenter);
                        	    double third_distance  = third_line.ptSegDist(xcenter, ycenter);
                        	    double fourth_distance = fourth_line.ptSegDist(xcenter, ycenter);
                        	    
                        	    // Assuming identical distances are impossible.
                        	    int line = 0;
                        	    if(first_distance < second_distance && first_distance < third_distance && first_distance < third_distance)
                        	    {
                        	        line = 1;	
                        	    }
                        	    else if(second_distance < first_distance && second_distance < third_distance && second_distance < fourth_distance)
                        	    {
                        	    	line = 2;
                        	    }
                        	    else if(third_distance < first_distance && third_distance < second_distance && third_distance < fourth_distance)
                        	    {
                        	        line = 3;
                        	    }
                        	    else 
                        	    {
                        	        line = 4;	
                        	    }
                        	    switch(line)
                        	    {
                        	    case 1: Line2D.Double first_endpoint   = new Line2D.Double(x1, y1, xcenter, ycenter);
                        	            Line2D.Double second_endpoint  = new Line2D.Double(x2, y2, xcenter, ycenter);
                        	            double first_length = DataMapper.getLength(first_endpoint);
                        	            double second_length = DataMapper.getLength(second_endpoint);
                        	            if(first_length == first_distance)
                        	            {
                        	                cell_intensity[i][j] = first_sample.intensity;	
                        	                isInterpolated[i][j] = true;
                        	            }
                        	            else if(second_length == first_distance)
                        	            {
                        	                cell_intensity[i][j] = second_sample.intensity;	
                        	                isInterpolated[i][j] = true;
                        	            }
                        	            else
                        	            {
                        	            	// Get bisecting average from first line--using x1, y1 and x2, y2
                        	    	    	Line2D.Double reference = new Line2D.Double(x1, y1, xcenter, ycenter);
                        	    	    	double        reference_length     = DataMapper.getLength(reference);
                        	    			double        first_slope          = Math.abs(DataMapper.getSlope(first_line));          //B
                        	    			double        second_slope         = Math.abs(DataMapper.getSlope(reference));
                        	    			
                        	    			// Get the degrees.
                        	    			double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
                        	    			double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
                        	    			double        third_degrees        = 0;                                                        //c
                        	    			if(first_degrees < second_degrees)
                        	    			   third_degrees = second_degrees - first_degrees;
                        	    			else
                        	    				third_degrees = first_degrees - second_degrees;
                        	    			double fourth_degrees =  90 - third_degrees;                                                      //d
                        	    			double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
                        	    			double side_length = DataMapper.getLength(first_line); 
                        	    			double weight1 = segment_length / side_length;
                        	    			double weight2 = (side_length - segment_length) / side_length;
                        	    			cell_intensity[i][j] = weight1 * first_sample.intensity + weight2 * second_sample.intensity;
                        	    			isInterpolated[i][j] = true;	
                        	            }
                        	            break;
                        	    
                        	    case 2: first_endpoint   = new Line2D.Double(x2, y2, xcenter, ycenter);
                	                    second_endpoint  = new Line2D.Double(x3, y3, xcenter, ycenter);
                	                    first_length = DataMapper.getLength(first_endpoint);
                	                    second_length = DataMapper.getLength(second_endpoint);
                	                    if(first_length == first_distance)
                	                    {
                	                        cell_intensity[i][j] = second_sample.intensity;	
                	                        isInterpolated[i][j] = true;
                	                    }
                	                    else if(second_length == first_distance)
                	                    {
                	                        cell_intensity[i][j] = second_sample.intensity;	
                	                        isInterpolated[i][j] = true;
                	                    }
                	                    else
                	                    {
                	            	        // Get bisecting average from second line--using x2, y2 and x3, y3
                	    	    	        Line2D.Double reference = new Line2D.Double(x2, y2, xcenter, ycenter);
                	    	    	        double        reference_length     = DataMapper.getLength(reference);
                	    			        double        first_slope          = Math.abs(DataMapper.getSlope(second_line));          //B
                	    			        double        second_slope         = Math.abs(DataMapper.getSlope(reference));
                	    			
                	    			        // Get the degrees.
                	    			        double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
                	    			        double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
                	    			        double        third_degrees        = 0;                                                        //c
                	    			        if(first_degrees < second_degrees)
                	    			            third_degrees = second_degrees - first_degrees;
                	    			        else
                	    				        third_degrees = first_degrees - second_degrees;
                	    			        double fourth_degrees =  90 - third_degrees;                                                      //d
                	    			        double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
                	    			        double side_length = DataMapper.getLength(first_line); 
                	    			        double weight1 = segment_length / side_length;
                	    			        double weight2 = (side_length - segment_length) / side_length;
                	    			        cell_intensity[i][j] = weight1 * second_sample.intensity + weight2 * third_sample.intensity;
                	    			        isInterpolated[i][j] = true;	
                	                   }
                	                   break;
                	                   
                        	    case 3: first_endpoint   = new Line2D.Double(x3, y3, xcenter, ycenter);      
                        	            second_endpoint  = new Line2D.Double(x4, y4, xcenter, ycenter);
                        	            first_length = DataMapper.getLength(first_endpoint);
                	                    second_length = DataMapper.getLength(second_endpoint);
                	                    if(first_length == first_distance)
                	                    {
                	                        cell_intensity[i][j] = second_sample.intensity;	
                	                        isInterpolated[i][j] = true;
                	                    }
                	                    else if(second_length == first_distance)
                	                    {
                	                        cell_intensity[i][j] = third_sample.intensity;	
                	                        isInterpolated[i][j] = true;
                	                    }
                	                    else
                	                    {
                	            	        // Get bisecting average from third line--using x3, y3 and x4, y4
                	    	    	        Line2D.Double reference = new Line2D.Double(x3, y3, xcenter, ycenter);
                	    	    	        double        reference_length     = DataMapper.getLength(reference);
                	    			        double        first_slope          = Math.abs(DataMapper.getSlope(second_line));          //B
                	    			        double        second_slope         = Math.abs(DataMapper.getSlope(reference));
                	    			
                	    			        // Get the degrees.
                	    			        double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
                	    			        double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
                	    			        double        third_degrees        = 0;                                                        //c
                	    			        if(first_degrees < second_degrees)
                	    			            third_degrees = second_degrees - first_degrees;
                	    			        else
                	    				        third_degrees = first_degrees - second_degrees;
                	    			        double fourth_degrees =  90 - third_degrees;                                                      //d
                	    			        double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
                	    			        double side_length = DataMapper.getLength(first_line); 
                	    			        double weight1 = segment_length / side_length;
                	    			        double weight2 = (side_length - segment_length) / side_length;
                	    			        cell_intensity[i][j] = weight1 * second_sample.intensity + weight2 * third_sample.intensity;
                	    			        isInterpolated[i][j] = true;	
                	                    }
                	                    break;
                	                   
                        	    case 4: first_endpoint   = new Line2D.Double(x4, y4, xcenter, ycenter); 
                        	            second_endpoint  = new Line2D.Double(x1, y1, xcenter, ycenter);
                	                    first_length = DataMapper.getLength(first_endpoint);
        	                            second_length = DataMapper.getLength(second_endpoint);
        	                            if(first_length == first_distance)
        	                            {
                	                        cell_intensity[i][j] = third_sample.intensity;	
                	                        isInterpolated[i][j] = true;
                	                    }
                	                    else if(second_length == first_distance)
                	                    {
                	                        cell_intensity[i][j] = fourth_sample.intensity;	
                	                        isInterpolated[i][j] = true;
                	                    }
                	                    else
                	                    {
                	                    	// Get bisecting average from fourth line--using x4, y4 and x1, y1
                	    	    	        Line2D.Double reference = new Line2D.Double(x4, y4, xcenter, ycenter);
                	    	    	        double        reference_length     = DataMapper.getLength(reference);
                	    			        double        first_slope          = Math.abs(DataMapper.getSlope(second_line));          //B
                	    			        double        second_slope         = Math.abs(DataMapper.getSlope(reference));
                	    			
                	    			        // Get the degrees.
                	    			        double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
                	    			        double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
                	    			        double        third_degrees        = 0;                                                        //c
                	    			        if(first_degrees < second_degrees)
                	    			            third_degrees = second_degrees - first_degrees;
                	    			        else
                	    				        third_degrees = first_degrees - second_degrees;
                	    			        double fourth_degrees =  90 - third_degrees;                                                      //d
                	    			        double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
                	    			        double side_length = DataMapper.getLength(first_line); 
                	    			        double weight1 = segment_length / side_length;
                	    			        double weight2 = (side_length - segment_length) / side_length;
                	    			        cell_intensity[i][j] = weight1 * second_sample.intensity + weight2 * third_sample.intensity;
                	    			        isInterpolated[i][j] = true;		
                	                    }
        	                            break;
                        	    }
        	        	       
                    		}
                        }
                    }
        	    }
        	    else if(neighborPopulated[0] || neighborPopulated[1] || neighborPopulated[2] || neighborPopulated[3] || 
        	    		neighborPopulated[4] || neighborPopulated[5] ||  neighborPopulated[6] || neighborPopulated[7])
        	    {
        	    	// The simple approach conceptually is to sort the points and find the nearest line, but it involves a lot of coding.
        	    	// Lets try walking through the simple cases first, and use the other algorithm for complex cases.
        	    	int number_of_neighbors = 0;
        	    	for(int m = 0; m < 8; m++)
                    {
        	    		if(neighborPopulated[m])
        	    			number_of_neighbors++;
                    }
        	    	outer: switch(number_of_neighbors)
        	    	{
        	    	case 1 :  for(int m = 0; m < 8; m++)
        	    	          {
        	    		          if(neighborPopulated[m])
        	    		          {
        	    		        	  ArrayList list   = (ArrayList)neighbor_list.get(m);	
        	        	    	      Sample    sample = (Sample)list.get(0);
        	        	    	      cell_intensity[i][j] = sample.intensity;
        	              	    	  isInterpolated[i][j] = true;
        	              	    	  break outer;    
        	    		          }	  
        	    	          }
        	    	case 2 :  int current_neighbors = 0;
        	    	          ArrayList first_list = new ArrayList();
        	    	          ArrayList second_list = new ArrayList();
        	    		      for(int m = 0; m < 8; m++) 
        	    	          {
        	    		    	  if(neighborPopulated[m])
        	    		    	  {
        	    		    		  ArrayList list   = (ArrayList)neighbor_list.get(m);  
        	    		    		  if(current_neighbors == 0)
        	    		    			  first_list = list;
        	    		    		  else
        	    		    			  second_list = list;
        	    		    		  current_neighbors++;
        	    		    		  if(current_neighbors == 2)
        	    		    		  {
        	    		    			 Sample first_sample = (Sample)first_list.get(0);
        	        	    	         Sample second_sample = (Sample)second_list.get(0);
        	        	    	         double x1 = first_sample.x;
        	        	    	         double y1 = first_sample.y;
        	        	    	         double x2 = second_sample.x;
        	        	    	         double y2 = second_sample.y;
        	        	    	         Line2D.Double first_line = new Line2D.Double(x1, y1, x2, y2);
        	    		    			// Get bisecting average from first line--using x1, y1 and x2, y2
                      	    	    	Line2D.Double reference = new Line2D.Double(x1, y1, xcenter, ycenter);
                      	    	    	double        reference_length     = DataMapper.getLength(reference);
                      	    			double        first_slope          = Math.abs(DataMapper.getSlope(first_line));          //B
                      	    			double        second_slope         = Math.abs(DataMapper.getSlope(reference));
                      	    			
                      	    			// Get the degrees.
                      	    			double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
                      	    			double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
                      	    			double        third_degrees        = 0;                                                        //c
                      	    			if(first_degrees < second_degrees)
                      	    			   third_degrees = second_degrees - first_degrees;
                      	    			else
                      	    				third_degrees = first_degrees - second_degrees;
                      	    			double fourth_degrees =  90 - third_degrees;                                                      //d
                      	    			double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
                      	    			double side_length = DataMapper.getLength(first_line); 
                      	    			double weight1 = segment_length / side_length;
                      	    			double weight2 = (side_length - segment_length) / side_length;
                      	    			cell_intensity[i][j] = weight1 * first_sample.intensity + weight2 * second_sample.intensity;
                      	    			isInterpolated[i][j] = true; 
                      	    			break outer;
        	    		    		  }
        	    		    	  }
        	    	          }
        	    	case 3 :  current_neighbors = 0;
        	    	          first_list = new ArrayList();
	    	                  second_list = new ArrayList();
	    	                  ArrayList third_list  = new ArrayList();
	    		              for(int m = 0; m < 8; m++) 
	    	                  {
	    		    	          if(neighborPopulated[m])
	    		    	          {
	    		    		          ArrayList list   = (ArrayList)neighbor_list.get(m);  
	    		    		          if(current_neighbors == 0)
	    		    			          first_list = list;
	    		    		          else if(current_neighbors == 1)
	    		    			          second_list = list;
	    		    		          else
	    		    		        	  third_list = list;
	    		    		          current_neighbors++;
	    		    		          if(current_neighbors == 3)
	    		    		          {
	    		    			          Sample first_sample = (Sample)first_list.get(0);
	        	    	                  Sample second_sample = (Sample)second_list.get(0);
	        	    	                  Sample third_sample  = (Sample)third_list.get(0);
	        	    	                  double x1 = first_sample.x;
	        	    	                  double y1 = first_sample.y;
	        	    	                  double x2 = second_sample.x;
	        	    	                  double y2 = second_sample.y;
	        	    	                  double x3 = third_sample.x;
	        	    	                  double y3 = third_sample.y;
	        	    	                  Line2D.Double first_line = new Line2D.Double(x1, y1, x2, y2);
	        	    	                  Line2D.Double second_line = new Line2D.Double(x2, y2, x3, y3);
	        	    	                  Line2D.Double third_line = new Line2D.Double(x3, y3, x1, y1);
	                              	      double first_distance  = first_line.ptSegDist(xcenter, ycenter);
	                              	      double second_distance = second_line.ptSegDist(xcenter, ycenter);
	                              	      double third_distance  = third_line.ptSegDist(xcenter, ycenter);
	                              	    
	                              	      // Assuming identical distances are impossible.
	                              	      int line = 0;
	                              	      if(first_distance < second_distance && first_distance < third_distance)
	                              	      {
	                              	          line = 1;	
	                              	      }
	                              	      else if(second_distance < first_distance && second_distance < third_distance)
	                              	      {
	                              	    	  line = 2;
	                              	      }
	                              	      else 
	                              	      {
	                              	          line = 3;
	                              	      }
	                              	      switch(line)
	                              	      {
	                              	      case 1: Line2D.Double first_endpoint   = new Line2D.Double(x1, y1, xcenter, ycenter);
	                              	              Line2D.Double second_endpoint  = new Line2D.Double(x2, y2, xcenter, ycenter);
	                              	              double first_length = DataMapper.getLength(first_endpoint);
	                              	              double second_length = DataMapper.getLength(second_endpoint);
	                              	              if(first_length == first_distance)
	                              	              {
	                              	                  cell_intensity[i][j] = first_sample.intensity;	
	                              	                  isInterpolated[i][j] = true;
	                              	              }
	                              	              else if(second_length == first_distance)
	                              	              { 
	                              	                  cell_intensity[i][j] = second_sample.intensity;	
	                              	                  isInterpolated[i][j] = true;
	                              	              }
	                              	              else
	                              	              {
	                              	            	// Get bisecting average from first line--using x1, y1 and x2, y2
	                              	    	    	Line2D.Double reference = new Line2D.Double(x1, y1, xcenter, ycenter);
	                              	    	    	double        reference_length     = DataMapper.getLength(reference);
	                              	    			double        first_slope          = Math.abs(DataMapper.getSlope(first_line));          //B
	                              	    			double        second_slope         = Math.abs(DataMapper.getSlope(reference));
	                              	    			
	                              	    			// Get the degrees.
	                              	    			double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
	                              	    			double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
	                              	    			double        third_degrees        = 0;                                                        //c
	                              	    			if(first_degrees < second_degrees)
	                              	    			   third_degrees = second_degrees - first_degrees;
	                              	    			else
	                              	    				third_degrees = first_degrees - second_degrees;
	                              	    			double fourth_degrees =  90 - third_degrees;                                                      //d
	                              	    			double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
	                              	    			double side_length = DataMapper.getLength(first_line); 
	                              	    			double weight1 = segment_length / side_length;
	                              	    			double weight2 = (side_length - segment_length) / side_length;
	                              	    			cell_intensity[i][j] = weight1 * first_sample.intensity + weight2 * second_sample.intensity;
	                              	    			isInterpolated[i][j] = true;	
	                              	            }
	                              	            break outer;
	                              	    
	                              	    case 2: first_endpoint   = new Line2D.Double(x2, y2, xcenter, ycenter);
	                      	                    second_endpoint  = new Line2D.Double(x3, y3, xcenter, ycenter);
	                      	                    first_length = DataMapper.getLength(first_endpoint);
	                      	                    second_length = DataMapper.getLength(second_endpoint);
	                      	                    if(first_length == first_distance)
	                      	                    {
	                      	                        cell_intensity[i][j] = second_sample.intensity;	
	                      	                        isInterpolated[i][j] = true;
	                      	                    }
	                      	                    else if(second_length == first_distance)
	                      	                    {
	                      	                        cell_intensity[i][j] = second_sample.intensity;	
	                      	                        isInterpolated[i][j] = true;
	                      	                    }
	                      	                    else
	                      	                    {
	                      	            	        // Get bisecting average from second line--using x2, y2 and x3, y3
	                      	    	    	        Line2D.Double reference = new Line2D.Double(x2, y2, xcenter, ycenter);
	                      	    	    	        double        reference_length     = DataMapper.getLength(reference);
	                      	    			        double        first_slope          = Math.abs(DataMapper.getSlope(second_line));          //B
	                      	    			        double        second_slope         = Math.abs(DataMapper.getSlope(reference));
	                      	    			
	                      	    			        // Get the degrees.
	                      	    			        double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
	                      	    			        double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
	                      	    			        double        third_degrees        = 0;                                                        //c
	                      	    			        if(first_degrees < second_degrees)
	                      	    			            third_degrees = second_degrees - first_degrees;
	                      	    			        else
	                      	    				        third_degrees = first_degrees - second_degrees;
	                      	    			        double fourth_degrees =  90 - third_degrees;                                                      //d
	                      	    			        double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
	                      	    			        double side_length = DataMapper.getLength(first_line); 
	                      	    			        double weight1 = segment_length / side_length;
	                      	    			        double weight2 = (side_length - segment_length) / side_length;
	                      	    			        cell_intensity[i][j] = weight1 * second_sample.intensity + weight2 * third_sample.intensity;
	                      	    			        isInterpolated[i][j] = true;	
	                      	                   }
	                      	                   break;
	                      	                   
	                              	    case 3: first_endpoint   = new Line2D.Double(x3, y3, xcenter, ycenter);      
	                              	            second_endpoint  = new Line2D.Double(x1, y1, xcenter, ycenter);
	                              	            first_length = DataMapper.getLength(first_endpoint);
	                      	                    second_length = DataMapper.getLength(second_endpoint);
	                      	                    if(first_length == first_distance)
	                      	                    {
	                      	                        cell_intensity[i][j] = third_sample.intensity;	
	                      	                        isInterpolated[i][j] = true;
	                      	                    }
	                      	                    else if(second_length == first_distance)
	                      	                    {
	                      	                        cell_intensity[i][j] = first_sample.intensity;	
	                      	                        isInterpolated[i][j] = true;
	                      	                    }
	                      	                    else
	                      	                    {
	                      	            	        // Get bisecting average from third line--using x3, y3 and x1, y1
	                      	    	    	        Line2D.Double reference = new Line2D.Double(x3, y3, xcenter, ycenter);
	                      	    	    	        double        reference_length     = DataMapper.getLength(reference);
	                      	    			        double        first_slope          = Math.abs(DataMapper.getSlope(third_line));          //B
	                      	    			        double        second_slope         = Math.abs(DataMapper.getSlope(reference));
	                      	    			
	                      	    			        // Get the degrees.
	                      	    			        double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
	                      	    			        double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
	                      	    			        double        third_degrees        = 0;                                                        //c
	                      	    			        if(first_degrees < second_degrees)
	                      	    			            third_degrees = second_degrees - first_degrees;
	                      	    			        else
	                      	    				        third_degrees = first_degrees - second_degrees;
	                      	    			        double fourth_degrees =  90 - third_degrees;                                                      //d
	                      	    			        double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
	                      	    			        double side_length = DataMapper.getLength(first_line); 
	                      	    			        double weight1 = segment_length / side_length;
	                      	    			        double weight2 = (side_length - segment_length) / side_length;
	                      	    			        cell_intensity[i][j] = weight1 * second_sample.intensity + weight2 * third_sample.intensity;
	                      	    			        isInterpolated[i][j] = true;	
	                      	                    }
	                      	                    break outer;
	                      	                   
	                              	    
	                              	    }
	    		    		        }
	    		    	        }
	    		    		  
	    		    	   }
        	    	case 4 :  current_neighbors = 0;
	    	                  first_list = new ArrayList();
	                          second_list = new ArrayList();
	                          third_list  = new ArrayList();
	                          ArrayList fourth_list = new ArrayList();
		                      for(int m = 0; m < 8; m++) 
	                          {
		    	                  if(neighborPopulated[m])
		    	                  {
		    		                  ArrayList list   = (ArrayList)neighbor_list.get(m);  
		    		                  if(current_neighbors == 0)
		    			                 first_list = list;
		    		                  else if(current_neighbors == 1)
		    			                  second_list = list;
		    		                  else if(current_neighbors == 2)
		    			                  third_list = list;
		    		                  else
		    		        	          fourth_list = list;
		    		                  current_neighbors++;
		    		                  if(current_neighbors == 4)
		    		                  {
		    			                  Sample first_sample = (Sample)first_list.get(0);
  	    	                              Sample second_sample = (Sample)second_list.get(0);
  	    	                              Sample third_sample  = (Sample)third_list.get(0);
  	    	                              Sample fourth_sample = (Sample)fourth_list.get(0);
  	    	                              double x1 = first_sample.x;
  	    	                              double y1 = first_sample.y;
  	    	                              double x2 = second_sample.x;
  	    	                              double y2 = second_sample.y;
  	    	                              double x3 = third_sample.x;
  	    	                              double y3 = third_sample.y;
  	    	                              double x4 = fourth_sample.x;
  	    	                              double y4 = fourth_sample.y;
  	    	                              Line2D.Double first_line = new Line2D.Double(x1, y1, x2, y2);
  	    	                              Line2D.Double second_line = new Line2D.Double(x2, y2, x3, y3);
  	    	                              Line2D.Double third_line = new Line2D.Double(x3, y3, x4, y4);
  	    	                              Line2D.Double fourth_line = new Line2D.Double(x4, y4, x1, y1);
                        	              double first_distance  = first_line.ptSegDist(xcenter, ycenter);
                        	              double second_distance = second_line.ptSegDist(xcenter, ycenter);
                        	              double third_distance  = third_line.ptSegDist(xcenter, ycenter);
                        	              double fourth_distance = fourth_line.ptSegDist(xcenter, ycenter);
                        	    
                        	             // Assuming identical distances are (nearly) impossible.
                        	             int line = 0;
                        	             if(first_distance < second_distance && first_distance < third_distance && first_distance < third_distance)
                                 	    {
                                 	        line = 1;	
                                 	    }
                                 	    else if(second_distance < first_distance && second_distance < third_distance && second_distance < fourth_distance)
                                 	    {
                                 	    	line = 2;
                                 	    }
                                 	    else if(third_distance < first_distance && third_distance < second_distance && third_distance < fourth_distance)
                                 	    {
                                 	        line = 3;
                                 	    }
                                 	    else 
                                 	    {
                                 	        line = 4;	
                                 	    }
                                 	    switch(line)
                                 	    {
                                 	    case 1: Line2D.Double first_endpoint   = new Line2D.Double(x1, y1, xcenter, ycenter);
                                 	            Line2D.Double second_endpoint  = new Line2D.Double(x2, y2, xcenter, ycenter);
                                 	            double first_length = DataMapper.getLength(first_endpoint);
                                 	            double second_length = DataMapper.getLength(second_endpoint);
                                 	            if(first_length == first_distance)
                                 	            {
                                 	                cell_intensity[i][j] = first_sample.intensity;	
                                 	                isInterpolated[i][j] = true;
                                 	            }
                                 	            else if(second_length == first_distance)
                                 	            {
                                 	                cell_intensity[i][j] = second_sample.intensity;	
                                 	                isInterpolated[i][j] = true;
                                 	            }
                                 	            else
                                 	            {
                                 	            	// Get bisecting average from first line--using x1, y1 and x2, y2
                                 	    	    	Line2D.Double reference = new Line2D.Double(x1, y1, xcenter, ycenter);
                                 	    	    	double        reference_length     = DataMapper.getLength(reference);
                                 	    			double        first_slope          = Math.abs(DataMapper.getSlope(first_line));          //B
                                 	    			double        second_slope         = Math.abs(DataMapper.getSlope(reference));
                                 	    			
                                 	    			// Get the degrees.
                                 	    			double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
                                 	    			double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
                                 	    			double        third_degrees        = 0;                                                        //c
                                 	    			if(first_degrees < second_degrees)
                                 	    			   third_degrees = second_degrees - first_degrees;
                                 	    			else
                                 	    				third_degrees = first_degrees - second_degrees;
                                 	    			double fourth_degrees =  90 - third_degrees;                                                      //d
                                 	    			double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
                                 	    			double side_length = DataMapper.getLength(first_line); 
                                 	    			double weight1 = segment_length / side_length;
                                 	    			double weight2 = (side_length - segment_length) / side_length;
                                 	    			cell_intensity[i][j] = weight1 * first_sample.intensity + weight2 * second_sample.intensity;
                                 	    			isInterpolated[i][j] = true;	
                                 	            }
                                 	            break outer;
                                 	    
                                 	    case 2: first_endpoint   = new Line2D.Double(x2, y2, xcenter, ycenter);
                         	                    second_endpoint  = new Line2D.Double(x3, y3, xcenter, ycenter);
                         	                    first_length = DataMapper.getLength(first_endpoint);
                         	                    second_length = DataMapper.getLength(second_endpoint);
                         	                    if(first_length == first_distance)
                         	                    {
                         	                        cell_intensity[i][j] = second_sample.intensity;	
                         	                        isInterpolated[i][j] = true;
                         	                    }
                         	                    else if(second_length == first_distance)
                         	                    {
                         	                        cell_intensity[i][j] = second_sample.intensity;	
                         	                        isInterpolated[i][j] = true;
                         	                    }
                         	                    else
                         	                    {
                         	            	        // Get bisecting average from second line--using x2, y2 and x3, y3
                         	    	    	        Line2D.Double reference = new Line2D.Double(x2, y2, xcenter, ycenter);
                         	    	    	        double        reference_length     = DataMapper.getLength(reference);
                         	    			        double        first_slope          = Math.abs(DataMapper.getSlope(second_line));          //B
                         	    			        double        second_slope         = Math.abs(DataMapper.getSlope(reference));
                         	    			
                         	    			        // Get the degrees.
                         	    			        double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
                         	    			        double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
                         	    			        double        third_degrees        = 0;                                                        //c
                         	    			        if(first_degrees < second_degrees)
                         	    			            third_degrees = second_degrees - first_degrees;
                         	    			        else
                         	    				        third_degrees = first_degrees - second_degrees;
                         	    			        double fourth_degrees =  90 - third_degrees;                                                      //d
                         	    			        double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
                         	    			        double side_length = DataMapper.getLength(first_line); 
                         	    			        double weight1 = segment_length / side_length;
                         	    			        double weight2 = (side_length - segment_length) / side_length;
                         	    			        cell_intensity[i][j] = weight1 * second_sample.intensity + weight2 * third_sample.intensity;
                         	    			        isInterpolated[i][j] = true;	
                         	                   }
                         	                   break outer;
                         	                   
                                 	    case 3: first_endpoint   = new Line2D.Double(x3, y3, xcenter, ycenter);      
                                 	            second_endpoint  = new Line2D.Double(x4, y4, xcenter, ycenter);
                                 	            first_length = DataMapper.getLength(first_endpoint);
                         	                    second_length = DataMapper.getLength(second_endpoint);
                         	                    if(first_length == first_distance)
                         	                    {
                         	                        cell_intensity[i][j] = second_sample.intensity;	
                         	                        isInterpolated[i][j] = true;
                         	                    }
                         	                    else if(second_length == first_distance)
                         	                    {
                         	                        cell_intensity[i][j] = third_sample.intensity;	
                         	                        isInterpolated[i][j] = true;
                         	                    }
                         	                    else
                         	                    {
                         	            	        // Get bisecting average from third line--using x3, y3 and x4, y4
                         	    	    	        Line2D.Double reference = new Line2D.Double(x3, y3, xcenter, ycenter);
                         	    	    	        double        reference_length     = DataMapper.getLength(reference);
                         	    			        double        first_slope          = Math.abs(DataMapper.getSlope(second_line));          //B
                         	    			        double        second_slope         = Math.abs(DataMapper.getSlope(reference));
                         	    			
                         	    			        // Get the degrees.
                         	    			        double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
                         	    			        double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
                         	    			        double        third_degrees        = 0;                                                        //c
                         	    			        if(first_degrees < second_degrees)
                         	    			            third_degrees = second_degrees - first_degrees;
                         	    			        else
                         	    				        third_degrees = first_degrees - second_degrees;
                         	    			        double fourth_degrees =  90 - third_degrees;                                                      //d
                         	    			        double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
                         	    			        double side_length = DataMapper.getLength(first_line); 
                         	    			        double weight1 = segment_length / side_length;
                         	    			        double weight2 = (side_length - segment_length) / side_length;
                         	    			        cell_intensity[i][j] = weight1 * second_sample.intensity + weight2 * third_sample.intensity;
                         	    			        isInterpolated[i][j] = true;	
                         	                    }
                         	                    break outer;
                         	                   
                                 	    case 4: first_endpoint   = new Line2D.Double(x4, y4, xcenter, ycenter); 
                                 	            second_endpoint  = new Line2D.Double(x1, y1, xcenter, ycenter);
                         	                    first_length = DataMapper.getLength(first_endpoint);
                 	                            second_length = DataMapper.getLength(second_endpoint);
                 	                            if(first_length == first_distance)
                 	                            {
                         	                        cell_intensity[i][j] = third_sample.intensity;	
                         	                        isInterpolated[i][j] = true;
                         	                    }
                         	                    else if(second_length == first_distance)
                         	                    {
                         	                        cell_intensity[i][j] = fourth_sample.intensity;	
                         	                        isInterpolated[i][j] = true;
                         	                    }
                         	                    else
                         	                    {
                         	                    	// Get bisecting average from fourth line--using x4, y4 and x1, y1
                         	    	    	        Line2D.Double reference = new Line2D.Double(x4, y4, xcenter, ycenter);
                         	    	    	        double        reference_length     = DataMapper.getLength(reference);
                         	    			        double        first_slope          = Math.abs(DataMapper.getSlope(second_line));          //B
                         	    			        double        second_slope         = Math.abs(DataMapper.getSlope(reference));
                         	    			
                         	    			        // Get the degrees.
                         	    			        double        first_degrees        = DataMapper.getDegrees(first_slope);                       //a
                         	    			        double        second_degrees       = DataMapper.getDegrees(second_slope);                      //b
                         	    			        double        third_degrees        = 0;                                                        //c
                         	    			        if(first_degrees < second_degrees)
                         	    			            third_degrees = second_degrees - first_degrees;
                         	    			        else
                         	    				        third_degrees = first_degrees - second_degrees;
                         	    			        double fourth_degrees =  90 - third_degrees;                                                      //d
                         	    			        double segment_length  = reference_length * DataMapper.sin(fourth_degrees) / DataMapper.sin(90);  //C
                         	    			        double side_length = DataMapper.getLength(first_line); 
                         	    			        double weight1 = segment_length / side_length;
                         	    			        double weight2 = (side_length - segment_length) / side_length;
                         	    			        cell_intensity[i][j] = weight1 * second_sample.intensity + weight2 * third_sample.intensity;
                         	    			        isInterpolated[i][j] = true;		
                         	                    }
                 	                            break outer;
                                 	      }
		    		                  }
		    	                  }
	                          }
        	    	}
                    if(isInterpolated[i][j] == false)
                    {
                        System.out.println("Odd cell was not assigned.");
                        System.out.println("The number of neighbors was " + number_of_neighbors);
                        System.out.println("");
                        double total_distance = 0.;
            	    	for(int m = 0; m < 8; m++)
            	    	{
            	    		if(neighborPopulated[m])
            	    		{
            	    			ArrayList current_list   = (ArrayList)neighbor_list.get(m);	
            	    			Sample    current_sample = (Sample)current_list.get(0);
            	    			total_distance           += current_sample.distance;
            	    		}
            	    	}
                        double intensity = 0.;
            	    	for(int m = 0; m < 8; m++)
            	    	{
            	    		if(neighborPopulated[m])
            	    		{
            	    			ArrayList current_list   = (ArrayList)neighbor_list.get(m);	
            	    			Sample    current_sample = (Sample)current_list.get(0);
            	    			intensity               += (current_sample.distance / total_distance) * current_sample.intensity;
            	    		}
            	    	}
            	    	cell_intensity[i][j] = intensity;
            	    	isInterpolated[i][j] = true;
                    }
        	    }
        	}
        }
        long finish_time = System.nanoTime();
	    System.out.println("...finished.");
	    long time = finish_time - start_time;
	    System.out.println("It took " + (time  / 1000000 ) + " ms.");
	    System.out.println(" ");
        
        int number_of_populated_cells      = 0;
        int number_of_cells_with_neighbors = 0;
        int number_of_interpolated_cells   = 0;              
        for(int i = 0; i < ydim; i++)
        {
        	for(int j = 0; j < xdim; j++)
        	{
        		if(isPopulated[i][j])
        			number_of_populated_cells++;
        		if(hasNeighbors[i][j])
        			number_of_cells_with_neighbors++;
        		if(isInterpolated[i][j])
        			number_of_interpolated_cells++;
        	}
        }
        
	    System.out.println("The total number of cells is          " + (xdim * ydim)); 
	    System.out.println("The number of populated cells is      " + number_of_populated_cells); 
	    System.out.println("The number of cells with neighbors is " + number_of_cells_with_neighbors); 
	    System.out.println("The number of interpolated cells is    " + number_of_interpolated_cells);
	    System.out.println("Got here.");
	    double intensity_range = maximum_intensity - minimum_intensity;
        BufferedImage data_image = new BufferedImage(xdim, ydim, BufferedImage.TYPE_INT_RGB);
        
        int k = 0;
        int src[] = new int[xdim * ydim];
        for(int i = 0; i < ydim; i++)
        {
            for(int j= 0; j < xdim; j++)
            {
            	double current_value = cell_intensity[i][j];
            	current_value -= minimum_intensity;
            	current_value /= intensity_range;
            	current_value *= 255.;
                int gray_value = (int)current_value;
                src[k++] = gray_value; 
                //int rgb_value = ((gray_value&0x0ff)<<16)|((gray_value&0x0ff)<<8)|(gray_value&0x0ff);
                //data_image.setRGB(j, i, rgb_value);
            }
        }
        
        
        int dst[] = new int[xdim * ydim];  
        DataMapper.smooth(src, xdim, ydim, 4, 2, dst);
        k = 0;
        for(int i = 0; i < ydim; i++)
        {
            for(int j= 0; j < xdim; j++)
            {
            	int gray_value = dst[k++];
            	int rgb_value = ((gray_value&0x0ff)<<16)|((gray_value&0x0ff)<<8)|(gray_value&0x0ff);
            	data_image.setRGB(j, i, rgb_value);  	
            }
        }
        
        
        
        
        try 
        {  
            ImageIO.write(data_image, "jpg", new File("C:/Users/Brian Crowley/Desktop/smooth.jpg")); 
        } 
        catch(IOException e) 
        {  
            e.printStackTrace(); 
        }  
	}
}