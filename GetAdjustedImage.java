import java.io.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class GetAdjustedImage
{
	public static void main(String[] args)throws IOException, FileNotFoundException  
	{	
        int object_id = 0;
		if(args.length != 1)
		{
			System.out.println("Usage: GetImage <object id>");
			System.exit(0);
		}
		else
		{
			object_id = Integer.parseInt(args[0]);	
		}
		Hashtable object_table         = ObjectMapper.getObjectTable();
	 	int [][]  object_array         = (int [][])object_table.get(object_id);
		ArrayList complete_sample_list = new ArrayList();
		ArrayList sample_list          = new ArrayList();
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
            	complete_sample_list.add(current_sample);
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
                        	complete_sample_list.add(current_sample);	     
                        }
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
        
        
        String file_string = new String("C:/Users/Brian Crowley/Desktop/foo.jpg");
        
        
        Sample previous_sample, current_sample, next_sample;
        // Load object segments and also get samples before and after segment to help adjust location.
        int length        = object_array.length;
        for(int i = 0; i < length; i++)
        {
        	int start = object_array[i][0];  
        	int stop  = object_array[i][1];
        	
        	boolean isAscending;
        	
        	previous_sample = (Sample)complete_sample_list.get(start - 5);
    		current_sample  = (Sample)complete_sample_list.get(start);
    		if(current_sample.y > previous_sample.y)
    			isAscending = true;
    		else
    			isAscending = false;	
        	for(int j = start; j < stop; j++)
        	{
        		previous_sample = (Sample)complete_sample_list.get(j - 5);
        		current_sample  = (Sample)complete_sample_list.get(j);
        		next_sample     = (Sample)complete_sample_list.get(j + 5);
        		
        		Sample sample = new Sample(current_sample.x, current_sample.y, current_sample.intensity);
        		
        		boolean doAjustement = true;
        		// We are adjusting an offset between different directions for unknown reasons.
        		if(doAjustement)
        		{
        		if(isAscending)
        		{
        			sample.x += next_sample.x;
        			sample.x /= 2.;
        			sample.y += previous_sample.y;
        			sample.y /= 2;
        			sample.y += 0.75;
        		}
        		else
        		{
        			sample.x += previous_sample.x;
        			sample.x /= 2.;
        			sample.y += previous_sample.y;
        			sample.y /= 2;
        			sample.y -= 0.75;
        		}
        		}
        		sample_list.add(sample);
        	}	
        }
          
      	double minimum_x, maximum_x;
        double minimum_y, maximum_y;
        double minimum_intensity, maximum_intensity;
                 
        boolean isPopulated[][];          
        boolean hasNeighbors[][];  
        boolean isInterpolated[][];
        int     number_of_samples[][];   

        int   resolution = 4;
        double increment = 1. / resolution;
              
        //A value we can set from our data to produce an image.
        double  cell_intensity[][];
              
        // Now get the information specific to our segmented data.
        int sample_list_size = sample_list.size();
        //sample_list_size = sample_list.size();
        Sample init_sample = (Sample)sample_list.get(0);
        //init_sample = (Sample)sample_list.get(0);
        minimum_x = maximum_x = init_sample.x;
        minimum_y = maximum_y = init_sample.y;
        minimum_intensity = maximum_intensity = init_sample.intensity;
        for(int i = 1; i < sample_list_size; i++)
        {
        	current_sample = (Sample)sample_list.get(i);
        	double x = current_sample.x;
        	double y = current_sample.y;
        	if(x < minimum_x)
        		minimum_x = x;
        	else if(x > maximum_x)
        		maximum_x = x;
        	if(y < minimum_y)
        		minimum_y = y;
        	else if(y > maximum_y)
        		maximum_y = y;
        	if(current_sample.intensity < minimum_intensity)
        		minimum_intensity = current_sample.intensity;
        	else if(current_sample.intensity > maximum_intensity)
        		maximum_intensity = current_sample.intensity;		
        }
        double range = maximum_intensity - minimum_intensity;
        double average_intensity = minimum_intensity + range / 2;
        
        //Now set up a raster.
        double x_range = maximum_x - minimum_x;
        double x_origin = minimum_x;
        double y_range = maximum_y - minimum_y;
        double y_origin = minimum_y;
        
        String range_double = String.format("%.2f", x_range);
        System.out.println("The x range is " + range_double);
        range_double = String.format("%.2f", y_range);
        System.out.println("The y range is " + range_double);
            
        int xdim = (int)Math.round(x_range + .5);
        
        int ydim = (int)Math.round(y_range + .5);
        
        
        
        
        // This is meters and we want quarter meters, the resolution of our sensors.
        xdim *= resolution;
        ydim *= resolution;
        
        
        /*
        System.out.println("Xdim is "      + xdim);
        System.out.println("Ydim is "      + ydim);
        System.out.println("Minimum x is " + minimum_x);
        System.out.println("Maximum x is " + maximum_x);
        System.out.println("Minimum y is " + minimum_y);
        System.out.println("Maximum y is " + maximum_y);
        */
        
        System.out.println("Minimum intensity is " + minimum_intensity);
        System.out.println("Maximum intensity is " + maximum_intensity);
        
        isPopulated       = new boolean[ydim][xdim];
        hasNeighbors      = new boolean[ydim][xdim];
        isInterpolated    = new boolean[ydim][xdim];  
        number_of_samples = new int[ydim][xdim];
        cell_intensity    = new double[ydim][xdim];
          
        for(int i = 0; i < ydim; i++)
        {
        	for(int j = 0; j < xdim; j++)
        	{
        		isPopulated[i][j]  = false;
        		hasNeighbors[i][j] = false;
        		number_of_samples[i][j] = 0;
        		cell_intensity[i][j] = minimum_intensity;
        		isInterpolated[i][j] = false;
        	}
        }  
        int neighbor_histogram[] = new int[9];
        for(int i = 0; i < 9; i++)
        	neighbor_histogram[i] = 0;	
        ArrayList close_data_array = new ArrayList(xdim * ydim);
        ArrayList data_array = new ArrayList(xdim * ydim);
        for(int i = 0; i < xdim * ydim; i++)
        {
        	ArrayList cell_list = new ArrayList();
        	close_data_array.add(cell_list);	
        }	   	
        for(int i = 0; i < sample_list_size; i++)
        {
            current_sample = (Sample)sample_list.get(i);
            double current_x = current_sample.x;
            double current_y = current_sample.y;
            current_x -= x_origin;
            current_y -= y_origin; 
            // Tried to flip the coordinate system but it scrambes the neighbor lists.
            // Some subtlety here that I'm not getting but shouldn't have to do it anyway.
            // Just try and remember that the original data is oriented the opposite direction from the individual data
            // (but only when you have to).  I'll try to figure this out someday.
            // This doesn't work:
            // double reverse_y = y_range - current_y;
            // reverse_y *= resolution;
            current_x  *= resolution;
            current_y  *= resolution;
            int x_index = (int)Math.floor(current_x);
            int y_index = (int)Math.floor(current_y);
            isPopulated[y_index][x_index] = true;
            number_of_samples[y_index][x_index]++;
            int data_index   = y_index * xdim + x_index;
            ArrayList current_cell_list = (ArrayList)close_data_array.get(data_index);
            current_cell_list.add(current_sample);
        }
        
        for(int i = 0; i < ydim; i++)
        {  
        	double ycenter = y_origin + i * increment + increment * .5;
        	for(int j = 0; j < xdim; j++)
        	{
        		double xcenter = j * increment + increment / 2. + x_origin;
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
        	    	for(int k = 0; k < current_cell_list.size(); k++)
        	    	{
        	    		current_sample = (Sample)current_cell_list.get(k);
        	    		current_sample.setDistance(xcenter, ycenter);
    	    		    if(current_sample.x < xcenter)
    	    		    {
    	    		    	if(current_sample.y < ycenter)
    	    		    	{
    	    		    		// SW quadrant
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
        	        		            south_list.add(current_sample);	
        	        		        }
    	        		            else
    	        		            	southwest_list.add(current_sample);
    	        		       }	
    	    		    	}
    	    		    	else
    	    		    	{
    	    		    	    // NW quadrant
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
        	        		            north_list.add(current_sample);	
        	        		        }
    	        		            else
    	        		            	northwest_list.add(current_sample);
    	        		        }
    	    		    	}
    	    		    } 
    	    		    else
    	    		    {
    	    		    	if(current_sample.y < ycenter)
    	    		    	{
    	    		    	    // SE quadrant
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
        	        		            south_list.add(current_sample);	
        	        		        }
    	        		            else
    	        		            	southeast_list.add(current_sample);
    	        		        }
    	    		    		
    	    		    	}
	    		    	    else
	    		    	    {
	    		    	    	// NE quadrant
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
        	        		            north_list.add(current_sample);	
        	        		        }
    	        		            else
    	        		            	northeast_list.add(current_sample);
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
        	    			    case 0:  southwest_list.add(sample);
	    		                         break; 
        	    			    case 1:  south_list.add(sample);
   	    		                         break;
   	    		                case 2:  southeast_list.add(sample);
   	    		                         break;
   	    		                case 3:  west_list.add(sample);
   	    		                         break;
   	    		                case 4:  east_list.add(sample);
   	    		                         break;
   	    		                case 5:  northwest_list.add(sample);
   	    		                         break;
   	    		                case 6:  north_list.add(sample);
   	    		                         break;
   	    		                case 7:  northeast_list.add(sample);
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
        	    
    	    	for(int m = 0; m < 8; m++)
    	    	{
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
    	    		    //Entries from the distance list are not always unique.
    	    		    //Don't understand how it can happen so often.
    	    		    //We need to go through the list and
    	    		    //change the value by a small enough amount that its
    	    		    //different from the real value but unlikely to affect
    	    		    //any result significantly.  This is so when we
    	    		    //use the distance as a key to a hashtable, samples
    	    		    //don't start stepping on each other.
    	    		    
    	    		    //Kludgy, but any workaround that doesn't corrupt
    	    		    //the actual data will be tedious and add a lot
    	    		    //of code and alter basic data structures.
    	    		    double previous_distance = (double)distance_list.get(0);
    	    		    for(int n = 1; n < distance_list.size(); n++)
    	    		    {
    	    		    	double current_distance = (double)distance_list.get(n);
    	    		    	if(current_distance == previous_distance)
    	    		    	{
    	    		    		current_sample  = (Sample)unsorted_list.get(n);
    	    		    		previous_sample = (Sample)unsorted_list.get(n - 1);
    	    		    		
    	    		    		//Minimum possible floating point value.
    	    		    		current_distance       += 0.0000000000000001; 
    	    		    		current_sample.distance = current_distance;
    	    		    		distance_list.set(n, current_distance);
    	    		    	}  
    	    		    }
    	    		    Collections.sort(distance_list);
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
        	            list.set(m, sorted_list);
    	    		}
    	        }
    	    	data_array.add(list); 
        	}
        }
        
        int number_of_linear_interpolations = 0;
        int number_of_bisecting_averages    = 0;
        int number_of_weighted_averages     = 0;
        int number_of_nearest_neighbors     = 0;
        for(int i = 0; i < ydim; i++)
        {
        	double ycenter = i * increment + increment * .5 + y_origin;
        	for(int j = 0; j < xdim; j++)
        	{ 
        		double xcenter = j * increment + increment / 2. + x_origin;
        		int data_index = i * xdim + j;
        		ArrayList neighbor_list = (ArrayList)data_array.get(data_index);
        	    boolean neighborPopulated[]   = new boolean[8];
        	    int number_of_quadrants = 0;
        	    for(int k = 0; k < 8; k++)
        	    	neighborPopulated[k] = false; 
        	    ArrayList neighbor_index_list = new ArrayList();    
        	    for(int k = 0; k < neighbor_list.size(); k++)
        	    {
        	    	ArrayList current_neighbor_list = (ArrayList)neighbor_list.get(k);
        	    	if(current_neighbor_list.size() > 0)
        	    	{
        	    		neighborPopulated[k] = true;
        	    		neighbor_index_list.add(k);
        	    		number_of_quadrants++;
        	    	}
        	    } 
        	    neighbor_histogram[number_of_quadrants]++;
        	    Point2D.Double origin = new Point2D.Double(xcenter, ycenter);
        	    ArrayList current_neighbor_list;
        	    ArrayList first_list, second_list, third_list, fourth_list;
	    	    Sample    first_sample, second_sample, third_sample, fourth_sample;
	    	    int       first_index, second_index, third_index, fourth_index;
	    	    double    first_distance, second_distance, third_distance;
	    	    ArrayList possible_set_list;
	    	    Hashtable actual_set_table;
        	      
	    	    if(number_of_quadrants == 1)
	    	    {
	    	    	int index             = (int)neighbor_index_list.get(0);      // Get the index for the populated quadrant.
   	                current_neighbor_list = (ArrayList)neighbor_list.get(index);  // Get the list of actual samples.  
   	                if(current_neighbor_list.size() > 1)
   	                {
   	                    first_sample  = (Sample)current_neighbor_list.get(0);
   	                    second_sample = (Sample)current_neighbor_list.get(1);
   	                    cell_intensity[i][j] = DataMapper.getBisectingAverage(first_sample, second_sample, origin);
   	                    number_of_bisecting_averages++;
   	                    isInterpolated[i][j] = true;
   	                }
   	                else
   	                {
   	                    Sample sample = (Sample)current_neighbor_list.get(0);
   	                    cell_intensity[i][j] = sample.intensity;
            	        isInterpolated[i][j] = true;
            	        number_of_nearest_neighbors++;
   	                }
	    	    }
	    	    else if(number_of_quadrants == 2)
	    	    {
	    	    	first_index = (int)neighbor_index_list.get(0); 
   	                second_index = (int)neighbor_index_list.get(1); 
   	                first_list   = (ArrayList)neighbor_list.get(first_index);
   	                second_list  = (ArrayList)neighbor_list.get(second_index);
   	                first_sample  = (Sample)first_list.get(0);
	            	second_sample = (Sample)second_list.get(0); 
	            	
	            	/*
	            	// Weighted average--open question whether it might be better than bisecting average.
	            	double total_distance = first_sample.distance + second_sample.distance;
	            	cell_intensity[i][j] = first_sample.intensity * first_sample.distance / total_distance + 
	            			               second_sample.intensity * second_sample.distance / total_distance; */
	            	
	            	
	            	cell_intensity[i][j] = DataMapper.getBisectingAverage(first_sample, second_sample, origin);
	            	number_of_bisecting_averages++;
	    			isInterpolated[i][j] = true; 	
	    	    }
	    	    else if(neighborPopulated[0] && neighborPopulated[2] && neighborPopulated[7] && neighborPopulated[5]) 
	    	    {
	    	    	first_list    = (ArrayList)neighbor_list.get(0);
                    second_list   = (ArrayList)neighbor_list.get(2);
                    third_list    = (ArrayList)neighbor_list.get(7);
                    fourth_list   = (ArrayList)neighbor_list.get(5);
                    
                    first_sample  = (Sample)first_list.get(0);
                    second_sample = (Sample)second_list.get(0);
                    third_sample  = (Sample)third_list.get(0);
                    fourth_sample = (Sample)fourth_list.get(0);
                    cell_intensity[i][j] = DataMapper.getLinearInterpolation(origin, first_sample, second_sample, third_sample, fourth_sample);
                    isInterpolated[i][j] = true;
                    number_of_linear_interpolations++;	
	    	    }
	    	    else if(neighborPopulated[1] && neighborPopulated[4] && neighborPopulated[6] && neighborPopulated[3]) 
	    	    {
	    	    	first_list    = (ArrayList)neighbor_list.get(1);
                    second_list   = (ArrayList)neighbor_list.get(4);
                    third_list    = (ArrayList)neighbor_list.get(6);
                    fourth_list   = (ArrayList)neighbor_list.get(3);
                    
                    first_sample  = (Sample)first_list.get(0);
                    second_sample = (Sample)second_list.get(0);
                    third_sample  = (Sample)third_list.get(0);
                    fourth_sample = (Sample)fourth_list.get(0);
                    cell_intensity[i][j] = DataMapper.getLinearInterpolation(origin, first_sample, second_sample, third_sample, fourth_sample);
                    isInterpolated[i][j] = true;
                    number_of_linear_interpolations++;	
	    	    }
	    	    else if(number_of_quadrants > 2)
	    	    {
	    	    	possible_set_list = QuadrantMapper.getPossibleTriangleSets(neighbor_index_list);
    	            actual_set_table  = QuadrantMapper.getActualTriangleSetTable(possible_set_list, neighbor_list, origin);
    	            if(!actual_set_table.isEmpty())
    	            {
                        Enumeration keys   = actual_set_table.keys();
                        ArrayList key_list = new ArrayList();
                        while(keys.hasMoreElements())
                        {
                            double key = (double)keys.nextElement();	
                            key_list.add(key);
                        }
                        Collections.sort(key_list);
                        
                        double key                 = (double)key_list.get(0);
                        int sample_space[][]       = (int[][])actual_set_table.get(key);
                        int first_quadrant_index   = sample_space[0][0];
                        int second_quadrant_index  = sample_space[1][0];
                        int third_quadrant_index   = sample_space[2][0];
                        int first_sample_index     = sample_space[0][1];
                        int second_sample_index    = sample_space[1][1];
                        int third_sample_index     = sample_space[2][1];
                        
                        first_list    = (ArrayList)neighbor_list.get(first_quadrant_index);
                        second_list   = (ArrayList)neighbor_list.get(second_quadrant_index);
                        third_list    = (ArrayList)neighbor_list.get(third_quadrant_index);
                        first_sample  = (Sample)first_list.get(first_sample_index);
                        second_sample = (Sample)second_list.get(second_sample_index);
                        third_sample  = (Sample)third_list.get(third_sample_index);
                        cell_intensity[i][j] = DataMapper.getLinearInterpolation(origin, first_sample, second_sample, third_sample);
  	    	            isInterpolated[i][j] = true;
  	    	            number_of_linear_interpolations++;
    	            }
    	            else
    	            {
    	            	Hashtable distance_table = new Hashtable();
    	            	ArrayList distance_list  = new ArrayList();
    	            	for(int k = 0; k < neighbor_index_list.size(); k++)
    	            	{
    	            		int current_index       = (int)neighbor_index_list.get(k);
    	            		ArrayList current_list  = (ArrayList)neighbor_list.get(current_index);
    	            		current_sample   = (Sample)current_list.get(0);
    	            		double current_distance = current_sample.distance;	
    	            		distance_list.add(current_distance);
    	            		distance_table.put(current_distance, current_index);
    	            	}
    	            	Collections.sort(distance_list);
    	            	/*
    	            	// Open question whether this is better than weighted nearest neighbors.
    	            	double first_key = (double)distance_list.get(0);
    	            	double second_key = (double)distance_list.get(1);
    	            	first_index = (int)distance_table.get(first_key);
    	            	second_index = (int)distance_table.get(second_key);
       	                first_list   = (ArrayList)neighbor_list.get(first_index);
       	                second_list  = (ArrayList)neighbor_list.get(second_index);
       	                first_sample  = (Sample)first_list.get(0);
   	            	    second_sample = (Sample)second_list.get(0); 
   	            	    cell_intensity[i][j] = DataMapper.getBisectingAverage(first_sample, second_sample, origin);
   	            	    number_of_bisecting_averages++;
   	            	    */
    	            	// Try weighted average to reduce noise.
    	            	double total_distance = 0;
    	            	for(int k = 0; k < distance_list.size(); k++)
    	            	    total_distance += (double) distance_list.get(k);
    	            	double weighted_average = 0;
    	            	for(int k = 0; k < neighbor_index_list.size(); k++)
    	            	{
    	            		int current_index       = (int)neighbor_index_list.get(k);
    	            		ArrayList current_list  = (ArrayList)neighbor_list.get(current_index);
    	            		current_sample   = (Sample)current_list.get(0);
    	            		double current_distance = current_sample.distance;
    	            		weighted_average += current_sample.intensity * current_distance / total_distance;
    	            	}
    	            	cell_intensity[i][j] = weighted_average;
   	            	    number_of_weighted_averages++;
    	    			isInterpolated[i][j] = true; 
    	            }  	
	    	    }
            }
        }
   
        
        for(int i = 0; i < 9; i++)
        	System.out.println(neighbor_histogram[i] + " cells had " + i + " quadrants with samples.");
        int number_of_interpolated_cells = 0;
        for(int i = 0; i < ydim; i++)
        {
            for(int j= 0; j < xdim; j++)
            {
            	if(isInterpolated[i][j] == true)
            		number_of_interpolated_cells++;
            		
            }
        } 
        int number_of_cells = xdim * ydim;
        System.out.println("The number of cells was "                 + number_of_cells);
        System.out.println("The number of interpolated cells was "    + number_of_interpolated_cells);
        System.out.println("The number of linear interpolations was " + number_of_linear_interpolations);
        System.out.println("The number of bisecting averages was "    + number_of_bisecting_averages);
        System.out.println("The number of weighted averages was "     + number_of_weighted_averages);
        System.out.println("The number of nearest neighbors was "     + number_of_nearest_neighbors);
        double intensity_range = maximum_intensity - minimum_intensity;
        BufferedImage data_image = new BufferedImage(xdim, ydim, BufferedImage.TYPE_INT_RGB);
        
        
        
        double interpolated_min = maximum_intensity;
        double interpolated_max = minimum_intensity;
        for(int i = 0; i < ydim; i++)
        {
            for(int j = 0; j < xdim; j++)
            {
                if(isInterpolated[i][j] == true)
                {
                	if(cell_intensity[i][j] < interpolated_min)
                		interpolated_min = cell_intensity[i][j];
                	if(cell_intensity[i][j] > interpolated_max)
                		interpolated_max = cell_intensity[i][j];	
                }
            }
        }
        
        System.out.println("The minimum intensity in the interpolated values was " + interpolated_min);
        System.out.println("The maximum intensity in the interpolated values was " + interpolated_max);
        
        if(number_of_cells == number_of_interpolated_cells)
        {
        	double interpolated_range = interpolated_max - interpolated_min;
            for(int i = 0; i < ydim; i++)
            {
                for(int j = 0; j < xdim; j++)
                {  	
                    double current_value  = cell_intensity[i][j];
                    current_value        -= interpolated_min;
                    current_value        /= interpolated_range;
                    current_value        *= 255.;
                    int gray_value        = (int)current_value;
        	        int rgb_value = ((gray_value&0x0ff)<<16)|((gray_value&0x0ff)<<8)|(gray_value&0x0ff);
        	        data_image.setRGB(j, i, rgb_value);  
                }
            }
        }
        else
        {
            double dilated_image[][] = new double[ydim][xdim]; 
            ImageMapper.getImageDilation(cell_intensity, isInterpolated, dilated_image);
            int number_of_cells_assigned_by_dilation = number_of_cells - number_of_interpolated_cells;
            System.out.println("The number of cells assigned by dilation was " + number_of_cells_assigned_by_dilation);
            double max_value = 0;
            
            interpolated_min = maximum_intensity;
            interpolated_max = minimum_intensity;
            for(int i = 0; i < ydim; i++)
            {
                for(int j = 0; j < xdim; j++)
                {
                    if(isInterpolated[i][j] == true)
                    {
                    	if(cell_intensity[i][j] < interpolated_min)
                    		interpolated_min = cell_intensity[i][j];
                    	if(cell_intensity[i][j] > interpolated_max)
                    		interpolated_max = cell_intensity[i][j];	
                    }
                }
            }
            
            System.out.println("The minimum intensity in the interpolated values after dilating was " + interpolated_min);
            System.out.println("The maximum intensity in the interpolated values after dilating was " + interpolated_max);
            double interpolated_range = interpolated_max - interpolated_min;
            for(int i = 0; i < ydim; i++)
            {
                for(int j = 0; j < xdim; j++)
                {  	
                    double current_value  = dilated_image[i][j];
                    current_value        -= interpolated_min;
                    current_value        /= interpolated_range;
                    current_value        *= 255.;
                    int gray_value        = (int)current_value;
        	        int rgb_value = ((gray_value&0x0ff)<<16)|((gray_value&0x0ff)<<8)|(gray_value&0x0ff);
        	        data_image.setRGB(j, i, rgb_value);  
                }
            }
            
            String filename = new String("C:/Users/Brian Crowley/Desktop/foo.txt");
            try(PrintWriter output = new PrintWriter(filename))
            {
                for(int i = 0; i < xdim; i++)
                {
                	for(int j = 0; j < ydim; j++)
                	{
                	    String intensity = String.valueOf(dilated_image[j][i]);
                	    output.println(i +" " + j + " " + intensity);
                	}
                }
                output.close();
    	    }
        }
        try 
        {  
            ImageIO.write(data_image, "jpg", new File(file_string)); 
        } 
        catch(IOException e) 
        {  
            e.printStackTrace(); 
        }        
	}
}