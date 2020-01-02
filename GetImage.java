import java.io.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class GetImage
{
	public static void main(String[] args)throws IOException, FileNotFoundException  
	{	 
        //Information that we want to stay global for the rest of the program.
		
		//The raw data.
		ArrayList sample_list = new ArrayList();
		
		//Information we'll collect about the data at the onset of the program.
		double minimum_x, maximum_x;
        double minimum_y, maximum_y;
        double minimum_intensity, maximum_intensity;
        
        //Information we collect during the course of the program.
        boolean isPopulated[][];          
        boolean hasNeighbors[][];  
        boolean isInterpolated[][];
        int     number_of_samples[][];   
        int     number_of_cells_with_four_corners  = 0;
        int     number_of_cells_with_four_corners_interpolated = 0;
        int     number_of_cells_with_three_corners = 0;
        int     number_of_cells_with_three_corners_interpolated = 0;
        int     number_of_cells_with_four_sides = 0;
        int     number_of_cells_with_four_sides_interpolated = 0;
        int     number_of_cells_with_three_sides = 0;
        int     number_of_cells_with_three_sides_interpolated = 0;
        int     number_of_cells_with_two_corners_and_a_side = 0;
        int     number_of_cells_with_two_corners_and_a_side_interpolated = 0;
        int     number_of_cells_with_two_sides_and_a_corner = 0;
        int     number_of_cells_with_two_sides_and_a_corner_interpolated = 0;
        int     number_of_cells_with_opposing_sides_or_corners = 0;
        int     number_of_cells_with_opposing_sides_or_corners_interpolated = 0;
        int     number_of_other_cells = 0;
        int     number_of_other_cells_interpolated = 0;

    
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
        
        double range = maximum_intensity + minimum_intensity;
        double average_intensity = minimum_intensity + range / 2;
        
        
        //Now set up a raster.
        double x_range = maximum_x - minimum_x;
        double x_origin = minimum_x;
        double y_range = maximum_y - minimum_y;
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
        // Could try playing around with this parameter.
        xdim *= resolution;
        ydim *= resolution;
        //System.out.println("The xdim of the data grid is " + xdim);
        //System.out.println("The ydim of the data grid is " + ydim);
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
        		isInterpolated[i][j] = false;
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
            
            int x_index       = (int) Math.floor(current_x);
            int y_index       = (int)Math.floor(current_y);
            isPopulated[y_index][x_index] = true;
            number_of_samples[y_index][x_index]++;
            int data_index = y_index * xdim + x_index;
            ArrayList current_cell_list = (ArrayList)close_data_array.get(data_index);
            current_cell_list.add(current_sample);
        }
        
        long start_time = System.nanoTime();
        for(int i = 0; i < ydim; i++)
        {  
        	double ycenter = y_origin + i * increment + increment * .5;
        	for(int j = 0; j < xdim; j++)
        	{
        		double xcenter = j * increment + increment / 2. + x_origin;
        		//We'll make separate lists of samples based on location
    	    	//to facilitate the search for nearest lines.
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
    	    	//of the cell.	We're doing this to make it easy to find the nearest lines.
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
    	    		    //System.out.println("");
    	    		    Collections.sort(distance_list);
    	    		    //System.out.println("Sorted distance list:");
    	    		    for(int n = 0; n < distance_list.size(); n++)
    	    		    {
    	    		    	double distance = (double)distance_list.get(n);
    	    		    	//System.out.print(distance + "  ");
    	    		    
    	    		    }
    	    		    //System.out.println("");
    	    		    //Could be some built-in facility in java to do this, but
    	    		    //it's likely to slow the program down.
    	    		    Hashtable <Double, Integer> location = new Hashtable <Double, Integer>();
        	    	    for(int n = 0; n < unsorted_list.size(); n++)
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
        	            //Lets do this for now to be safe.
        	            list.set(m, sorted_list);
    	    		}
    	        }
    	    	//Keep this information handy so we can find nearest lines later.
    	    	data_array.add(list); 
        	}
        }
        
        
        for(int i = 0; i < ydim; i++)
        {
        	for(int j = 0; j < xdim; j++)
        	{
        		int       data_index     = i * xdim + j;
        		ArrayList neighbor_list  = (ArrayList)data_array.get(data_index);
        		double    xcenter        = j * increment + increment / 2. + x_origin;
        		double    ycenter        = y_origin + i * increment + increment / 2.;	
        		ArrayList northwest_list = (ArrayList)neighbor_list.get(0);
        		ArrayList north_list     = (ArrayList)neighbor_list.get(1);
        		ArrayList northeast_list = (ArrayList)neighbor_list.get(2);
        		ArrayList west_list      = (ArrayList)neighbor_list.get(3);
        		ArrayList east_list      = (ArrayList)neighbor_list.get(4);
        		ArrayList southwest_list = (ArrayList)neighbor_list.get(5);
        		ArrayList south_list     = (ArrayList)neighbor_list.get(6);
        		ArrayList southeast_list = (ArrayList)neighbor_list.get(7);
        		
        		if(northwest_list.size() != 0 && northeast_list.size() != 0 && southwest_list.size() != 0 && southeast_list.size() != 0)
        		{			
        			double x1 = 0;
        			double y1 = 0;
        			double x2 = 0;
        			double y2 = 0;
        			double x3 = 0;
        			double y3 = 0;
        			double x4 = 0;
        			double y4 = 0;
        			
        			number_of_cells_with_four_corners++;
        			Sample first_sample = (Sample)southwest_list.get(0);
    	    	    x1 = first_sample.x;
    	    	    y1 = first_sample.y;
    	    	    
    	    	    Sample second_sample  = (Sample)southeast_list.get(0);
    	    	    x2 = second_sample.x;
    	    	    y2 = second_sample.y;
    	    	    
    	    	    Sample third_sample = (Sample)northeast_list.get(0);
    	    	    x3 = third_sample.x;
    	    	    y3 = third_sample.y;
	    		    
    	    	    Sample fourth_sample  = (Sample)northwest_list.get(0);
	    		    x4 = fourth_sample.x;
    	    	    y4 = fourth_sample.y; 
    	    	    
    	    	    Line2D.Double first_diagonal  = new Line2D.Double(x1, y1, x3, y3);
    	    	    Line2D.Double second_diagonal = new Line2D.Double(x4, y4, x2, y2);
    	    	    double first_length           = first_diagonal.ptSegDist(xcenter, ycenter);	
    	    	    double second_length          = second_diagonal.ptSegDist(xcenter, ycenter);
    	    	    if(first_length < second_length)
    	    	    {
    	    	    	//Get bisecting average from first diagonal
    	    	    	Line2D.Double reference = new Line2D.Double(x1, y1, xcenter, ycenter);
    	    	    	double        reference_length     = DataMapper.getLength(reference);
    	    			double        first_slope          = Math.abs(DataMapper.getSlope(first_diagonal));          //B
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
    	    			double diagonal_length = DataMapper.getLength(first_diagonal); 
    	    			double weight1 = segment_length / diagonal_length;
    	    			double weight2 = (diagonal_length - segment_length) / diagonal_length;
    	    			cell_intensity[i][j] = weight1 * first_sample.intensity + weight2 * third_sample.intensity;
    	    			isInterpolated[i][j] = true;
    	    			number_of_cells_with_four_corners_interpolated++;
    	    			
    	    	    }
    	    	    else
    	    	    {
    	    	        //Get bisecting average from second diagonal
    	    	    	Line2D.Double reference = new Line2D.Double(x4, y4, xcenter, ycenter);
    	    	    	double        reference_length     = DataMapper.getLength(reference);
    	    			double        first_slope          = Math.abs(DataMapper.getSlope(second_diagonal));          //B
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
    	    			double diagonal_length = DataMapper.getLength(second_diagonal); 
    	    			double weight1 = segment_length / diagonal_length;
    	    			double weight2 = (diagonal_length - segment_length) / diagonal_length;
    	    			cell_intensity[i][j] = weight1 * fourth_sample.intensity + weight2 * second_sample.intensity;
    	    			isInterpolated[i][j] = true;
    	    			number_of_cells_with_four_corners_interpolated++;
    	    			
    	    	    }
        		}
        		else if(north_list.size() != 0 && south_list.size() != 0 && west_list.size() != 0 && east_list.size() != 0 && isInterpolated[i][j] == false)
        		{
        			double x1 = 0;
        			double y1 = 0;
        			double x2 = 0;
        			double y2 = 0;
        			double x3 = 0;
        			double y3 = 0;
        			double x4 = 0;
        			double y4 = 0;
        			
        			number_of_cells_with_four_sides++; //That haven't been interpolated.
        			Sample first_sample = (Sample)north_list.get(0);
    	    	    x1 = first_sample.x;
    	    	    y1 = first_sample.y;
    	    	    
    	    	    Sample second_sample  = (Sample)east_list.get(0);
    	    	    x2 = second_sample.x;
    	    	    y2 = second_sample.y;
    	    	    
    	    	    Sample third_sample = (Sample)south_list.get(0);
    	    	    x3 = third_sample.x;
    	    	    y3 = third_sample.y;
	    		    
    	    	    Sample fourth_sample  = (Sample)west_list.get(0);
	    		    x4 = fourth_sample.x;
    	    	    y4 = fourth_sample.y; 
    	    	    
    	    	    Line2D.Double first_diagonal  = new Line2D.Double(x1, y1, x3, y3);
    	    	    Line2D.Double second_diagonal = new Line2D.Double(x4, y4, x2, y2);
    	    	    double first_length           = first_diagonal.ptSegDist(xcenter, ycenter);	
    	    	    double second_length          = second_diagonal.ptSegDist(xcenter, ycenter);
    	    	    if(first_length < second_length)
    	    	    {
    	    	    	//Get bisecting average from first diagonal
    	    	    	Line2D.Double reference = new Line2D.Double(x1, y1, xcenter, ycenter);
    	    	    	double        reference_length     = DataMapper.getLength(reference);
    	    			double        first_slope          = Math.abs(DataMapper.getSlope(first_diagonal));          //B
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
    	    			double diagonal_length = DataMapper.getLength(first_diagonal); 
    	    			double weight1 = segment_length / diagonal_length;
    	    			double weight2 = (diagonal_length - segment_length) / diagonal_length;
    	    			cell_intensity[i][j] = weight1 * first_sample.intensity + weight2 * third_sample.intensity;
    	    			isInterpolated[i][j] = true;
    	    			number_of_cells_with_four_sides_interpolated++;
    	    	    	
    	    	    }
    	    	    else
    	    	    {
    	    	        //Get bisecting average from second diagonal
    	    	    	Line2D.Double reference = new Line2D.Double(x4, y4, xcenter, ycenter);
    	    	    	double        reference_length     = DataMapper.getLength(reference);
    	    			double        first_slope          = Math.abs(DataMapper.getSlope(second_diagonal));          //B
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
    	    			double diagonal_length = DataMapper.getLength(second_diagonal); 
    	    			double weight1 = segment_length / diagonal_length;
    	    			double weight2 = (diagonal_length - segment_length) / diagonal_length;
    	    			cell_intensity[i][j] = weight1 * fourth_sample.intensity + weight2 * second_sample.intensity;
    	    			isInterpolated[i][j] = true;
    	    			number_of_cells_with_four_sides_interpolated++;
    	    			
    	    	    }
        		}
        			
        		int    current_case = 0;
        		if(northwest_list.size()      != 0 && northeast_list.size() != 0 && southwest_list.size() != 0 && southeast_list.size() == 0 && isInterpolated[i][j] == false)
        	        current_case = 1;
        		else if(northwest_list.size() != 0 && northeast_list.size() != 0 && southwest_list.size() == 0 && southeast_list.size() != 0 && isInterpolated[i][j] == false)
        		    current_case = 2;
        		else if(northwest_list.size() != 0 && northeast_list.size() == 0 && southwest_list.size() != 0 && southeast_list.size() != 0 && isInterpolated[i][j] == false)
        			current_case = 3;
        		else if(northwest_list.size() == 0 && northeast_list.size() != 0 && southwest_list.size() != 0 && southeast_list.size() != 0 && isInterpolated[i][j] == false)
        			current_case = 4;
        		if(current_case != 0)
        		{
        			number_of_cells_with_three_corners++;  
        			ArrayList first_list  = new ArrayList();
        	    	ArrayList second_list = new ArrayList();
        	    	ArrayList third_list  = new ArrayList();
        		    switch(current_case)
        		    {
        		    case 1:  first_list    = northwest_list;
        		             second_list   = northeast_list;
        		             third_list    = southwest_list;
        	        	     break;
       
        		    case 2:  first_list  = northwest_list;
		                     second_list = northeast_list;
		                     third_list  = southeast_list;
        	        	     break;
		                     
        		    case 3:  first_list  = northwest_list;
                             second_list = southwest_list;
                             third_list  = southeast_list;
        	        	     break;
             
        		    case 4:  first_list  = northeast_list;
                             second_list = southwest_list;
                             third_list  = southeast_list;
        	        	     break;
        		    } 
        			Sample first_sample = (Sample)first_list.get(0);
    	    	    double x1           = first_sample.x;
    	    	    double y1           = first_sample.y;
    	    	    
    	    	    Sample second_sample  = (Sample)second_list.get(0);
    	    	    double x2             = second_sample.x;
    	    	    double y2             = second_sample.y;
    	    	    
    	    	    Sample third_sample = (Sample)third_list.get(0);
    	    	    double x3           = third_sample.x;
    	    	    double y3           = third_sample.y;
	    		    
    	    	    
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
    	    			number_of_cells_with_three_corners_interpolated++;
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
    	    			number_of_cells_with_three_corners_interpolated++;
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
    	    			number_of_cells_with_three_corners_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    } 
    	    	    else if(first_length == second_length)
    	    	    {
    	    	    	// Get x2, y2
    	    	    	cell_intensity[i][j] = second_sample.intensity;
    	    	    	number_of_cells_with_three_corners_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    }
    	    	    else if(second_length == third_length)
    	    	    {
    	    	    	// Get x3, y3
    	    	    	cell_intensity[i][j] = third_sample.intensity;
    	    	    	number_of_cells_with_three_corners_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    }
    	    	    else if(third_length == first_length)
    	    	    {
    	    	    	// Get x1, y1
    	    	    	cell_intensity[i][j] = first_sample.intensity;
    	    	    	number_of_cells_with_three_corners_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    }
        		} 
        		
                //Cells with three sides.  Not getting much here, maybe because of the linear array.  Examined the implementation closely.
        		current_case = 0;
        		if(north_list.size()      != 0 && south_list.size() != 0 && east_list.size() != 0 && west_list.size() == 0 && isInterpolated[i][j] == false)
        	        current_case = 1;
        		else if(north_list.size() != 0 && south_list.size() != 0 && east_list.size() == 0 && west_list.size() != 0  && isInterpolated[i][j] == false)
        		    current_case = 2;
        		else if(north_list.size() != 0 && south_list.size() == 0 && east_list.size() != 0 && west_list.size() != 0 && isInterpolated[i][j] == false)
        		    current_case = 3;
        		else if(north_list.size() == 0 && south_list.size() != 0 && east_list.size() != 0 && west_list.size() != 0 && isInterpolated[i][j] == false)
        		    current_case = 4;
        		if(current_case != 0)
        		{
        			number_of_cells_with_three_sides++;
        			ArrayList first_list  = new ArrayList();
        	    	ArrayList second_list = new ArrayList();
        	    	ArrayList third_list  = new ArrayList();
        		    switch(current_case)
        		    {
        		    case 1:  first_list   = north_list;
        		             second_list  = south_list;
        		             third_list   = east_list;
        	        	     break;
       
        		    case 2:  first_list  = north_list;
		                     second_list = south_list;
		                     third_list  = west_list;
        	        	     break;
		                     
        		    case 3:  first_list  = north_list;
                             second_list = east_list;
                             third_list  = west_list;
        	        	     break;
             
        		    case 4:  first_list  = south_list;
                             second_list = east_list;
                             third_list  = west_list;
        	        	     break;
        		    } 
        		    
        		    Sample first_sample = (Sample)first_list.get(0);
    	    	    double x1           = first_sample.x;
    	    	    double y1           = first_sample.y;
    	    	    
    	    	    Sample second_sample  = (Sample)second_list.get(0);
    	    	    double x2             = second_sample.x;
    	    	    double y2             = second_sample.y;
    	    	    
    	    	    Sample third_sample = (Sample)third_list.get(0);
    	    	    double x3           = third_sample.x;
    	    	    double y3           = third_sample.y;
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
    	    			number_of_cells_with_three_sides_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    }
    	    	    else if(second_length < first_length && second_length < third_length)
    	    	    {
    	    	    	// Get bisecting average from second line
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
    	    			number_of_cells_with_three_sides_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    }
    	    	    else if(third_length < first_length && third_length < second_length)
    	    	    {
    	    	    	// Get bisecting average from third line
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
    	    			number_of_cells_with_three_sides_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    }  
    	    	    else if(first_length == second_length)
    	    	    {
    	    	    	// Get x2, y2
    	    	    	cell_intensity[i][j] = second_sample.intensity;
    	    	    	number_of_cells_with_three_sides_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    }
    	    	    else if(second_length == third_length)
    	    	    {
    	    	    	// Get x3, y3
    	    	    	cell_intensity[i][j] = third_sample.intensity;
    	    	    	number_of_cells_with_three_sides_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    }
    	    	    else if(third_length == first_length)
    	    	    {
    	    	    	// Get x1, y1
    	    	    	cell_intensity[i][j] = first_sample.intensity;
    	    	    	number_of_cells_with_three_sides_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    }
        		} 
        		
        		// Lets check opposing sides and opposing corners now that we're not using bounding polygons.
        		// Might save us from having to use a more complex random neighbor search later.
        		current_case = 0;
        		if(north_list.size()  != 0 && south_list.size() != 0 && isInterpolated[i][j] == false)
        	        current_case = 1;
        		else if(west_list.size() != 0 && east_list.size() != 0 && isInterpolated[i][j] == false)
        		    current_case = 2;
        		else if(northwest_list.size() != 0 && southeast_list.size() != 0 &&  isInterpolated[i][j] == false)
        		    current_case = 3;
        		else if(southwest_list.size() != 0 && northeast_list.size() != 0 && isInterpolated[i][j] == false)
        		    current_case = 4;
        		if(current_case != 0)
        		{
        			number_of_cells_with_opposing_sides_or_corners++;
        		    
        			ArrayList first_list  = new ArrayList();
        			ArrayList second_list = new ArrayList();
        		    switch(current_case)
        		    {
        		    case 1:  first_list = south_list;  
        		             second_list = north_list;
        		             break;
        		             
        		    case 2:  first_list = west_list;
        		             second_list = east_list;
        		             break;
        		             
        		    case 3:  first_list = southeast_list;
        		             second_list = northwest_list;
        		             break;
        		             
        		    case 4:  first_list = southwest_list;
        		             second_list = northeast_list;
        		             break;		             
        		    }
        		    //Keeping it simple for now, just making a line from the two closest points.
        		    Sample first_sample  = (Sample) first_list.get(0);
        		    Sample second_sample = (Sample) second_list.get(0);
        		    double x1 = first_sample.x;
        		    double y1 = first_sample.y;
        		    double x2 = second_sample.x;
        		    double y2 = second_sample.y;
        		    Line2D.Double reference = new Line2D.Double(x1, y1, x2, y2);
	        		Line2D.Double first_line = new Line2D.Double(x1, y1, xcenter, ycenter);
	        		Line2D.Double second_line = new Line2D.Double(x1, y1, x2, y2);
	        	    double reference_length = DataMapper.getLength(reference);
	        	    double first_length     = DataMapper.getLength(first_line);
	        	    double second_length    = DataMapper.getLength(second_line);
	        	    double reference_distance = reference.ptSegDist(xcenter, ycenter);
	        	    if(reference_distance == first_length)
	        	    {
	        	    	cell_intensity[i][j] = first_sample.intensity; 
	        	    	number_of_cells_with_opposing_sides_or_corners_interpolated++;
    	    			isInterpolated[i][j] = true;
	        	    }
	        	    else if(reference_distance == second_length)
	        	    {
	        	        cell_intensity[i][j] = second_sample.intensity;	
	        	        number_of_cells_with_opposing_sides_or_corners_interpolated++;
    	    			isInterpolated[i][j] = true;
	        	    }
	        	    else
	        	    {
	        	    	double   first_slope = Math.abs(DataMapper.getSlope(first_line));        
    	    			double   second_slope         = Math.abs(DataMapper.getSlope(reference));
    	    			
    	    			// Get the degrees.
    	    			double        first_degrees        = DataMapper.getDegrees(first_slope);                       
    	    			double        second_degrees       = DataMapper.getDegrees(second_slope); 
    	    			double third_degrees = second_degrees - first_degrees;
    	    			double fourth_degrees = 90 - third_degrees;
    	    			double segment_length  = reference_distance * DataMapper.sin(fourth_degrees) / DataMapper.sin(third_degrees);
    	    			double weight1 = segment_length / reference_length;
    	    			double weight2 = (reference_length - segment_length) / reference_length;
    	    			cell_intensity[i][j] = weight1 * first_sample.intensity + weight2 * second_sample.intensity;
    	    			number_of_cells_with_opposing_sides_or_corners_interpolated++;
    	    			isInterpolated[i][j] = true;
	        	    }
        		    
        		}
        		
        		
        		
        		//Looking for two corners and a side.
        		current_case = 0;
        		if(northwest_list.size()      != 0 && southwest_list.size() != 0 && east_list.size() != 0  && isInterpolated[i][j] == false)
        	        current_case = 1;
        		else if(northwest_list.size() != 0 && northeast_list.size() != 0 && south_list.size() != 0  && isInterpolated[i][j] == false)
        		    current_case = 2;
        		else if(northeast_list.size() != 0 && southeast_list.size() != 0 &&  west_list.size() != 0  && isInterpolated[i][j] == false)
        		    current_case = 3;
        		else if(southwest_list.size() != 0 && southeast_list.size() != 0 && north_list.size() != 0  && isInterpolated[i][j] == false)
        		    current_case = 4;
        		if(current_case != 0)
        		{
        			number_of_cells_with_two_corners_and_a_side++;
        			ArrayList first_list  = new ArrayList();
        	    	ArrayList second_list = new ArrayList();
        	    	ArrayList third_list  = new ArrayList();
        		    switch(current_case)
        		    {
        		    case 1:  first_list   = northwest_list;
        		             second_list  = southwest_list;
        		             third_list   = east_list;
        	        	     break;
       
        		    case 2:  first_list  = northwest_list;
		                     second_list = northeast_list;
		                     third_list  = south_list;
        	        	     break;
		                     
        		    case 3:  first_list  = northeast_list;
                             second_list = southeast_list;
                             third_list  = west_list;
        	        	     break;
             
        		    case 4:  first_list  = southwest_list;
                             second_list = southeast_list;
                             third_list  = north_list;
        	        	     break;
 
        		    } 
        		    Sample first_sample = (Sample)first_list.get(0);
    	    	    double x1           = first_sample.x;
    	    	    double y1           = first_sample.y;
    	    	    
    	    	    Sample second_sample  = (Sample)second_list.get(0);
    	    	    double x2             = second_sample.x;
    	    	    double y2             = second_sample.y;
    	    	    
    	    	    Sample third_sample = (Sample)third_list.get(0);
    	    	    double x3           = third_sample.x;
    	    	    double y3           = third_sample.y;
        		    Line2D.Double first_line  = new Line2D.Double(x1, y1, x2, y2);
    	    	    Line2D.Double second_line = new Line2D.Double(x2, y2, x3, y3);
    	    	    Line2D.Double third_line  = new Line2D.Double(x3, y3, x1, y1);
    	    	    
    	    	    double first_length       = first_line.ptSegDist(xcenter, ycenter);	
    	    	    double second_length      = second_line.ptSegDist(xcenter, ycenter);
    	    	    double third_length       = third_line.ptSegDist(xcenter, ycenter);
    	    	    if(first_length < second_length && first_length < third_length)
    	    	    {
    	    	    	// Get bisecting average from first line
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
    	    			number_of_cells_with_two_corners_and_a_side_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    }
    	    	    else if(second_length < first_length && second_length < third_length)
    	    	    {
    	    	    	// Get bisecting average from second line
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
    	    			number_of_cells_with_two_corners_and_a_side_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    }
    	    	    else if(third_length < first_length && third_length < second_length)
    	    	    {
    	    	    	// Get bisecting average from third line
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
    	    			number_of_cells_with_two_corners_and_a_side_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    }
    	    	    else if(first_length == second_length)
    	    	    {
    	    	    	// Get x2, y2
    	    	    	cell_intensity[i][j] = second_sample.intensity;
    	    	    	number_of_cells_with_two_corners_and_a_side_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    }
    	    	    else if(second_length == third_length)
    	    	    {
    	    	    	// Get x3, y3
    	    	    	cell_intensity[i][j] = third_sample.intensity;
    	    	    	number_of_cells_with_two_corners_and_a_side_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    }
    	    	    else if(third_length == first_length)
    	    	    {
    	    	    	// Get x1, y1
    	    	    	cell_intensity[i][j] = first_sample.intensity;
    	    	    	number_of_cells_with_two_corners_and_a_side_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    }
        		} 
        		
        		//Looking for two sides and a corner.
        		current_case = 0;
        		if(northwest_list.size()      != 0 && south_list.size() != 0 && east_list.size() != 0  && isInterpolated[i][j] == false)
        	        current_case = 1;
        		else if(northeast_list.size() != 0 && west_list.size() != 0 && south_list.size() != 0  && isInterpolated[i][j] == false)
        		    current_case = 2;
        		else if(southwest_list.size() != 0 && north_list.size() != 0 &&  east_list.size() != 0  && isInterpolated[i][j] == false)
        		    current_case = 3;
        		else if(southeast_list.size() != 0 && north_list.size() != 0 && west_list.size() != 0  && isInterpolated[i][j] == false)
        		    current_case = 4;
        		if(current_case != 0)
        		{
        			number_of_cells_with_two_sides_and_a_corner++;
        			ArrayList first_list  = new ArrayList();
        	    	ArrayList second_list = new ArrayList();
        	    	ArrayList third_list  = new ArrayList();
        		    switch(current_case)
        		    {
        		    case 1:  first_list   = northwest_list;
        		             second_list  = south_list;
        		             third_list   = east_list;
        	        	     break;
       
        		    case 2:  first_list  = northeast_list;
		                     second_list = west_list;
		                     third_list  = south_list;
        	        	     break;
		                     
        		    case 3:  first_list  = southwest_list;
                             second_list = north_list;
                             third_list  = east_list;
        	        	     break;
             
        		    case 4:  first_list  = southeast_list;
                             second_list = north_list;
                             third_list  = west_list;
        	        	     break;
 
        		    } 
        		    Sample first_sample = (Sample)first_list.get(0);
    	    	    double x1           = first_sample.x;
    	    	    double y1           = first_sample.y;
    	    	    
    	    	    Sample second_sample  = (Sample)second_list.get(0);
    	    	    double x2             = second_sample.x;
    	    	    double y2             = second_sample.y;
    	    	    
    	    	    Sample third_sample = (Sample)third_list.get(0);
    	    	    double x3           = third_sample.x;
    	    	    double y3           = third_sample.y;
        		    Line2D.Double first_line  = new Line2D.Double(x1, y1, x2, y2);
    	    	    Line2D.Double second_line = new Line2D.Double(x2, y2, x3, y3);
    	    	    Line2D.Double third_line  = new Line2D.Double(x3, y3, x1, y1);
    	    	    
    	    	    double first_length       = first_line.ptSegDist(xcenter, ycenter);	
    	    	    double second_length      = second_line.ptSegDist(xcenter, ycenter);
    	    	    double third_length       = third_line.ptSegDist(xcenter, ycenter);
    	    	    if(first_length < second_length && first_length < third_length)
    	    	    {
    	    	    	// Get bisecting average from first line
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
    	    			number_of_cells_with_two_sides_and_a_corner_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    }
    	    	    else if(second_length < first_length && second_length < third_length)
    	    	    {
    	    	    	// Get bisecting average from second line
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
    	    			number_of_cells_with_two_sides_and_a_corner_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    }
    	    	    else if(third_length < first_length && third_length < second_length)
    	    	    {
    	    	    	// Get bisecting average from third line
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
    	    			number_of_cells_with_two_sides_and_a_corner_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    }
    	    	    else if(first_length == second_length)
    	    	    {
    	    	    	// Get x2, y2
    	    	    	cell_intensity[i][j] = second_sample.intensity;
    	    	    	number_of_cells_with_two_sides_and_a_corner_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    }
    	    	    else if(second_length == third_length)
    	    	    {
    	    	    	// Get x3, y3
    	    	    	cell_intensity[i][j] = third_sample.intensity;
    	    	    	number_of_cells_with_two_sides_and_a_corner_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    }
    	    	    else if(third_length == first_length)
    	    	    {
    	    	    	// Get x1, y1
    	    	    	cell_intensity[i][j] = first_sample.intensity;
    	    	    	number_of_cells_with_two_sides_and_a_corner_interpolated++;
    	    			isInterpolated[i][j] = true;
    	    	    }
        		} 
        		
        		
        		
        		
        		
        		
        		// Check uninterpolated cells with more than one neighbor, and find the 
        		// line that comes closest to the center of the cell, and use bisecting average or nearest point.
        		// Assign the cells with one neighbor the neighbor value.
        		// Leave cells with no neighbors alone.
        		if((northwest_list.size() != 0 || north_list.size() != 0 || northeast_list.size() != 0 || west_list.size() != 0 ||
        			east_list.size() != 0 || southwest_list.size() != 0 || south_list.size() != 0 || southeast_list.size() != 0) && isInterpolated[i][j] == false)
        		{
        			number_of_other_cells++;
        			boolean notEmpty[] = new boolean[8];
        			for(int m = 0; m < 8; m++)
        				notEmpty[m] = false;
        		    //Find the number of neighbors
        			int number_of_neighbors = 0;
        			if(northwest_list.size() != 0)
        			{
        				notEmpty[0] = true;
        				number_of_neighbors++;
        			}
        			if(north_list.size() != 0)
        			{
        				notEmpty[1] = true;
        				number_of_neighbors++;
        			}
        			if(northeast_list.size() != 0)
        			{
        				notEmpty[2] = true;
        				number_of_neighbors++;
        			}
        			if(west_list.size() != 0)
        			{
        				notEmpty[3] = true;
        				number_of_neighbors++;
        			}
        			if(east_list.size() != 0)
        			{
        				notEmpty[4] = true;
        				number_of_neighbors++;
        			}
        			if(southwest_list.size() != 0)
        			{
        				notEmpty[5] = true;
        				number_of_neighbors++;
        			}
        			if(south_list.size() != 0)
        			{
        				notEmpty[6] = true;
        				number_of_neighbors++;
        			}
        			if(southeast_list.size() != 0)
        			{
        				notEmpty[7] = true;
        				number_of_neighbors++;
        			}
        			
        			
        			
        			if(number_of_neighbors == 1)
        			{
        			    outer: for(int m = 0; m < 8; m++)
        			    {
        			    	
        			       //Has to be a more elegant way to do this but
        			       //I just want to see the image first.
        			       if(notEmpty[m])
        			       {
        			    	   Sample sample;
        			    	   switch(m) 
        			    	   {
        			    	       case(0) : sample = (Sample)northwest_list.get(0);
        			    	                 cell_intensity[i][j] = sample.intensity;
        			    	                 isInterpolated[i][j] = true;
        			    	                 number_of_other_cells_interpolated++;
        			    	                 break outer;
        			    	                 
        			    	       case(1) : sample = (Sample)north_list.get(0);
        			    	                 cell_intensity[i][j] = sample.intensity;
        			    	                 isInterpolated[i][j] = true;
        			    	                 number_of_other_cells_interpolated++;
        			    	                 break outer;
        			    	                 
        			    	       case(2) : sample        = (Sample)northeast_list.get(0);
			    	                         cell_intensity[i][j] = sample.intensity;
			    	                         isInterpolated[i][j] = true;
			    	                         number_of_other_cells_interpolated++;
			    	                         break outer;
			    	                 
			    	               case(3) : sample = (Sample)west_list.get(0);
			    	                         cell_intensity[i][j] = sample.intensity;
			    	                         isInterpolated[i][j] = true;
			    	                         number_of_other_cells_interpolated++;
			    	                         break outer;        	 
        			    	        
        			    	       case(4) : sample = (Sample)east_list.get(0);
			    	                         cell_intensity[i][j] = sample.intensity;
			    	                         isInterpolated[i][j] = true;
			    	                         number_of_other_cells_interpolated++;
			    	                         break outer;
			    	                 
			    	               case(5) : sample = (Sample)southwest_list.get(0);
			    	                         cell_intensity[i][j] = sample.intensity;
			    	                         isInterpolated[i][j] = true;
			    	                         number_of_other_cells_interpolated++;
			    	                         break outer;
			    	                 
			    	               case(6) : sample        = (Sample)south_list.get(0);
	    	                                 cell_intensity[i][j] = sample.intensity;
	    	                                 isInterpolated[i][j] = true;
	    	                                 number_of_other_cells_interpolated++;
	    	                                 break outer;
	    	                 
	    	                       case(7) : sample = (Sample)southeast_list.get(0);
	    	                                 cell_intensity[i][j] = sample.intensity;
	    	                                 isInterpolated[i][j] = true;
	    	                                 number_of_other_cells_interpolated++;
	    	                                 break outer;               
        			    	   }
        			       }
        			    }
        			}
        			else if(number_of_neighbors == 2)
        			{
        			    int current_number_of_neighbors = 0;
        			    ArrayList first_list            = new ArrayList();
        			    ArrayList second_list           = new ArrayList();
        			    for(int m = 0; m < 8; m++)
        			    {
        			        if(notEmpty[m] == true)	
        			        {
        			        	current_number_of_neighbors++;
        			        	if(current_number_of_neighbors == 1)
        			        	{
        			        		switch(m)
        			        		{
        			        		
        			        		case 0 : first_list = northwest_list;
        			        		         break;
        			        		case 1 : first_list = north_list;
			        		                 break;
			        		        case 2 : first_list = northeast_list;
			        		                 break;
			        		        case 3 : first_list = west_list;
			        		                 break;
			        		        case 4 : first_list = east_list;
			        		                 break;
			        		        case 5 : first_list = southwest_list;
	        		                         break;
			        		        case 6 : first_list = south_list;
			        		                 break;
        			        		}    	
        			        	}
        			        	else
        			        	{
        			        		switch(m)
        			        		{
        			        		
        			        		case 1 : second_list = north_list;
        			        		         break;
        			        		case 2 : second_list = northeast_list;
			        		                 break;
			        		        case 3 : second_list = west_list;
			        		                 break;
			        		        case 4 : second_list = east_list;
			        		                 break;
			        		        case 5 : second_list = southwest_list;
			        		                 break;
			        		        case 6 : second_list = south_list;
	        		                         break;
			        		        case 7 : second_list = southeast_list;
			        		                 break;
        			        		}
        			        		
        			        		Sample first_sample = (Sample)first_list.get(0);
        			        		Sample second_sample = (Sample)second_list.get(0);
        			        		double x1 = first_sample.x;
        			        		double y1 = first_sample.y;
        			        		double x2 = second_sample.x;
        			        		double y2 = second_sample.y;
        			        		Line2D.Double reference = new Line2D.Double(x1, y1, x2, y2);
        			        		Line2D.Double first_line = new Line2D.Double(x1, y1, xcenter, ycenter);
        			        		Line2D.Double second_line = new Line2D.Double(x1, y1, x2, y2);
        			        	    double reference_length = DataMapper.getLength(reference);
        			        	    double first_length     = DataMapper.getLength(first_line);
        			        	    double second_length    = DataMapper.getLength(second_line);
        			        	    double reference_distance = reference.ptSegDist(xcenter, ycenter);
        			        	    if(reference_distance == first_length)
        			        	    {
        			        	    	cell_intensity[i][j] = first_sample.intensity;  
        			        	    	number_of_other_cells_interpolated++;
        		    	    			isInterpolated[i][j] = true;
        			        	    }
        			        	    else if(reference_distance == second_length)
        			        	    {
        			        	        cell_intensity[i][j] = second_sample.intensity;	
        			        	        number_of_other_cells_interpolated++;
        		    	    			isInterpolated[i][j] = true;
        			        	    }
        			        	    else
        			        	    {
        			        	    	double   first_slope = Math.abs(DataMapper.getSlope(first_line));        
        		    	    			double   second_slope         = Math.abs(DataMapper.getSlope(reference));
        		    	    			
        		    	    			// Get the degrees.
        		    	    			double        first_degrees        = DataMapper.getDegrees(first_slope);                       
        		    	    			double        second_degrees       = DataMapper.getDegrees(second_slope); 
        		    	    			double third_degrees = second_degrees - first_degrees;
        		    	    			double fourth_degrees = 90 - third_degrees;
        		    	    			double segment_length  = reference_distance * DataMapper.sin(fourth_degrees) / DataMapper.sin(third_degrees);
        		    	    			double weight1 = segment_length / reference_length;
        		    	    			double weight2 = (reference_length - segment_length) / reference_length;
        		    	    			cell_intensity[i][j] = weight1 * first_sample.intensity + weight2 * second_sample.intensity;
        		    	    			number_of_other_cells_interpolated++;
        		    	    			isInterpolated[i][j] = true;
        			        	    }
        			        		
        			        	}
        			        }
        			    }
        			}
        			else if(number_of_neighbors == 3)
        			{
        				int current_number_of_neighbors = 0;
        			    ArrayList first_list            = new ArrayList();
        			    ArrayList second_list           = new ArrayList();
        			    ArrayList third_list            = new ArrayList();
        			    for(int m = 0; m < 8; m++)
        			    {
        			        if(notEmpty[m] == true)	
        			        {
        			        	current_number_of_neighbors++;
        			        	if(current_number_of_neighbors == 1)
        			        	{
        			        		switch(m)
        			        		{
        			        		
        			        		case 0 : first_list = northwest_list;
        			        		         break;
        			        		case 1 : first_list = north_list;
			        		                 break;
			        		        case 2 : first_list = northeast_list;
			        		                 break;
			        		        case 3 : first_list = west_list;
			        		                 break;
			        		        case 4 : first_list = east_list;
			        		                 break;
			        		        case 5 : first_list = southwest_list;
	        		                         break;
        			        		}    	
        			        	}
        			        	else if(current_number_of_neighbors == 2)
        			        	{
        			        		switch(m)
        			        		{
        			        		
        			        		case 1 : second_list = north_list;
        			        		         break;
        			        		case 2 : second_list = northeast_list;
			        		                 break;
			        		        case 3 : second_list = west_list;
			        		                 break;
			        		        case 4 : second_list = east_list;
			        		                 break;
			        		        case 5 : second_list = southwest_list;
			        		                 break;
			        		        case 6 : second_list = south_list;
	        		                         break;
        			        		}
        			        	}
        			        	else
        			        	{
        			        		switch(m)
        			        		{
        			        		case 2 : third_list = northeast_list;
			        		                 break;
			        		        case 3 : third_list = west_list;
			        		                 break;
			        		        case 4 : third_list = east_list;
			        		                 break;
			        		        case 5 : third_list = southwest_list;
			        		                 break;
			        		        case 6 : third_list = south_list;
	        		                         break;
			        		        case 7 : third_list = southeast_list;
			        		                 break;
        			        		}
        			        		
        			        		Sample first_sample = (Sample)first_list.get(0);
        		    	    	    double x1           = first_sample.x;
        		    	    	    double y1           = first_sample.y;
        		    	    	    
        		    	    	    Sample second_sample  = (Sample)second_list.get(0);
        		    	    	    double x2             = second_sample.x;
        		    	    	    double y2             = second_sample.y;
        		    	    	    
        		    	    	    Sample third_sample = (Sample)third_list.get(0);
        		    	    	    double x3           = third_sample.x;
        		    	    	    double y3           = third_sample.y;
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
        		    	    			number_of_other_cells_interpolated++;
        		    	    			isInterpolated[i][j] = true;
        		    	    	    }
        		    	    	    else if(second_length < first_length && second_length < third_length)
        		    	    	    {
        		    	    	    	// Get bisecting average from second line
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
        		    	    			number_of_other_cells_interpolated++;
        		    	    			isInterpolated[i][j] = true;
        		    	    	    }
        		    	    	    else if(third_length < first_length && third_length < second_length)
        		    	    	    {
        		    	    	    	// Get bisecting average from third line
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
        		    	    			number_of_other_cells_interpolated++;
        		    	    			isInterpolated[i][j] = true;
        		    	    	    }  
        		    	    	    else if(first_length == second_length)
        		    	    	    {
        		    	    	    	// Get x2, y2
        		    	    	    	cell_intensity[i][j] = second_sample.intensity;
        		    	    	    	number_of_other_cells_interpolated++;
        		    	    			isInterpolated[i][j] = true;
        		    	    	    }
        		    	    	    else if(second_length == third_length)
        		    	    	    {
        		    	    	    	// Get x3, y3
        		    	    	    	cell_intensity[i][j] = third_sample.intensity;
        		    	    	    	number_of_other_cells_interpolated++;
        		    	    			isInterpolated[i][j] = true;
        		    	    	    }
        		    	    	    else if(third_length == first_length)
        		    	    	    {
        		    	    	    	// Get x1, y1
        		    	    	    	cell_intensity[i][j] = first_sample.intensity;
        		    	    	    	number_of_other_cells_interpolated++;
        		    	    			isInterpolated[i][j] = true;
        		    	    	    }
        			        		
        			        	}
        			        } 
        			    }
        			}
        			else if(number_of_neighbors == 4)
        			{
        				int current_number_of_neighbors = 0;
        			    ArrayList first_list            = new ArrayList();
        			    ArrayList second_list           = new ArrayList();
        			    ArrayList third_list            = new ArrayList();
        			    ArrayList fourth_list           = new ArrayList();
        			    
        			    for(int m = 0; m < 8; m++)
        			    {
        			        if(notEmpty[m] == true)	
        			        {
        			        	current_number_of_neighbors++;
        			        	if(current_number_of_neighbors == 1)
        			        	{
        			        		switch(m)
        			        		{
        			        		
        			        		case 0 : first_list = northwest_list;
        			        		         break;
        			        		case 1 : first_list = north_list;
			        		                 break;
			        		        case 2 : first_list = northeast_list;
			        		                 break;
			        		        case 3 : first_list = west_list;
			        		                 break;
			        		        case 4 : first_list = east_list;
			        		                 break;
			        		        case 5 : first_list = southwest_list;
			        		                 break;
			        		        case 6:  first_list = south_list;
	        		                         break;
			        		        case 7:  first_list = southeast_list;
   		                                     break;
        			        		}    	
        			        	}
        			        	else if(current_number_of_neighbors == 2)
        			        	{
        			        		switch(m)
        			        		{
        			        		 case 0 : second_list = northwest_list;
			        		                  break;
			        		         case 1 : second_list = north_list;
	        		                          break;
	        		                 case 2 : second_list = northeast_list;
	        		                          break;
	        		                 case 3 : second_list = west_list;
	        		                          break;
	        		                 case 4 : second_list = east_list;
	        		                          break;
	        		                 case 5 : second_list = southwest_list;
	        		                          break;
	        		                 case 6:  second_list = south_list;
   		                                      break;
	        		                 case 7:  second_list = southeast_list;
	                                          break;
        			        		}
        			        	}
        			        	else if(current_number_of_neighbors == 3)
        			        	{
        			        		switch(m)
        			        		{
        			        		case 0 : third_list = northwest_list;
        			        		         break;
        			        		case 1 : third_list = north_list;
        			        		         break;
        			        		case 2 : third_list = northeast_list;
			        		                 break;
			        		        case 3 : third_list = west_list;
			        		                 break;
			        		        case 4 : third_list = east_list;
			        		                 break;
			        		        case 5 : third_list = southwest_list;
			        		                 break;
			        		        case 6 : third_list = south_list;
	        		                         break;
			        		        case 7 : third_list = southeast_list;
			        		                 break;
        			        		}
        			        	}
        			        	else
        			        	{
        			        		switch(m)
        			        		{
        			        		case 0 : fourth_list = northwest_list;
        			        		         break;
        			        		case 1 : fourth_list = north_list;
        			        		         break;
        			        		case 2 : fourth_list = northwest_list;
			        		                 break;
        			        		case 3 : fourth_list = west_list;
			        		                 break;
			        		        case 4 : fourth_list = east_list;
			        		                 break;
			        		        case 5 : fourth_list = southwest_list;
			        		                 break;
			        		        case 6 : fourth_list = south_list;
			        		                 break;
			        		        case 7 : fourth_list = southeast_list;
	        		                         break;
        			        		} 
        			        		//Set the value.
        			        	
        		        			Sample first_sample = (Sample)first_list.get(0);
        		    	    	    double x1 = first_sample.x;
        		    	    	    double y1 = first_sample.y;
        		    	    	    
        		    	    	    Sample second_sample  = (Sample)second_list.get(0);
        		    	    	    double x2 = second_sample.x;
        		    	    	    double y2 = second_sample.y;
        		    	    	    
        		    	    	    Sample third_sample = (Sample)third_list.get(0);
        		    	    	    double x3 = third_sample.x;
        		    	    	    double y3 = third_sample.y;
        			    		    
        		    	    	    Sample fourth_sample  = (Sample)fourth_list.get(0);
        			    		    double x4 = fourth_sample.x;
        		    	    	    double y4 = fourth_sample.y; 
        		    	    	    
        		    	    	    Line2D.Double first_diagonal  = new Line2D.Double(x1, y1, x3, y3);
        		    	    	    Line2D.Double second_diagonal = new Line2D.Double(x4, y4, x2, y2);
        		    	    	    double first_length           = first_diagonal.ptSegDist(xcenter, ycenter);	
        		    	    	    double second_length          = second_diagonal.ptSegDist(xcenter, ycenter);
        		    	    	    if(first_length < second_length)
        		    	    	    {
        		    	    	    	//Get bisecting average from first diagonal
        		    	    	    	Line2D.Double reference = new Line2D.Double(x1, y1, xcenter, ycenter);
        		    	    	    	double        reference_length     = DataMapper.getLength(reference);
        		    	    			double        first_slope          = Math.abs(DataMapper.getSlope(first_diagonal));          //B
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
        		    	    			double diagonal_length = DataMapper.getLength(first_diagonal); 
        		    	    			double weight1 = segment_length / diagonal_length;
        		    	    			double weight2 = (diagonal_length - segment_length) / diagonal_length;
        		    	    			cell_intensity[i][j] = weight1 * first_sample.intensity + weight2 * third_sample.intensity;
        		    	    			isInterpolated[i][j] = true;
        		    	    			number_of_other_cells_interpolated++;
        		    	    	    	
        		    	    	    }
        		    	    	    else
        		    	    	    {
        		    	    	        //Get bisecting average from second diagonal
        		    	    	    	Line2D.Double reference = new Line2D.Double(x4, y4, xcenter, ycenter);
        		    	    	    	double        reference_length     = DataMapper.getLength(reference);
        		    	    			double        first_slope          = Math.abs(DataMapper.getSlope(second_diagonal));          //B
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
        		    	    			double diagonal_length = DataMapper.getLength(second_diagonal); 
        		    	    			double weight1 = segment_length / diagonal_length;
        		    	    			double weight2 = (diagonal_length - segment_length) / diagonal_length;
        		    	    			cell_intensity[i][j] = weight1 * fourth_sample.intensity + weight2 * second_sample.intensity;
        		    	    			isInterpolated[i][j] = true;
        		    	    			number_of_other_cells_interpolated++;
        		    	    			
        		    	    	    }
        		        		}	
        		            }   
        			    }        			
        			}
        			else
        			{
        				// Things get more complex here.
        				// Let's handle the easier cases and see how often this happens.
        			}
        				
        		}  		
        	}
	    }
        
        
        double intensity_range = maximum_intensity - minimum_intensity;
        BufferedImage data_image = new BufferedImage(xdim, ydim, BufferedImage.TYPE_INT_RGB);
        for(int i = 0; i < ydim; i++)
        {
            for(int j= 0; j < xdim; j++)
            {
            	double current_value = cell_intensity[i][j];
            	current_value -= minimum_intensity;
            	current_value /= intensity_range;
            	current_value *= 255.;
                int gray_value = (int)current_value;
            	//int gray_value = number_of_samples[i][j];
            	//gray_value *= 255;
            	//gray_value /= max_samples;
            	int rgb_value = ((gray_value&0x0ff)<<16)|((gray_value&0x0ff)<<8)|(gray_value&0x0ff);
            	data_image.setRGB(j, i, rgb_value);  	
            }
        }
        
        try 
        {  
            ImageIO.write(data_image, "jpg", new File("C:/Users/Brian Crowley/Desktop/bisector.jpg")); 
        } 
        catch(IOException e) 
        {  
            e.printStackTrace(); 
        }  
      
        int number_of_populated_cells      = 0;
        int number_of_cells_with_neighbors = 0;
        int number_of_interpolated_cells    = 0;              
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
	    
	    System.out.println("The number of cells with four corners is " + number_of_cells_with_four_corners);
	    System.out.println("The number of cells with four corners interpolated is " + number_of_cells_with_four_corners_interpolated); 
	    
	    System.out.println("The number of cells with four sides is " + number_of_cells_with_four_sides);
	    System.out.println("The number of cells with four sides interpolated is " + number_of_cells_with_four_sides_interpolated);
	    
	    System.out.println("The number of cells with three corners is " + number_of_cells_with_three_corners);
	    System.out.println("The number of cells with three corners interpolated is " + number_of_cells_with_three_corners_interpolated);
	    
	    System.out.println("The number of cells with three sides is " + number_of_cells_with_three_sides);
	    System.out.println("The number of cells with three sides interpolated is " + number_of_cells_with_three_sides_interpolated);
	    
	    System.out.println("The number of cells with opposing corners or sides is " + number_of_cells_with_opposing_sides_or_corners);
	    System.out.println("The number of cells with opposing corners or sides is interpolated " + number_of_cells_with_opposing_sides_or_corners_interpolated);
	    
	    System.out.println("The number of cells with two corners and a side is " + number_of_cells_with_two_corners_and_a_side);  
	    System.out.println("The number of cells with two corners and a side interpolated is " + number_of_cells_with_two_corners_and_a_side_interpolated);
	    
	    System.out.println("The number of cells with two sides and a corner is " + number_of_cells_with_two_sides_and_a_corner);
	    System.out.println("The number of cells with two sides and a corner interpolated is " + number_of_cells_with_two_sides_and_a_corner_interpolated);
	    
	    
	    

	    System.out.println("The number of other cells is " + number_of_other_cells);
	    System.out.println("The number of other cells interpolated is " + number_of_other_cells_interpolated);
	    
	}
}