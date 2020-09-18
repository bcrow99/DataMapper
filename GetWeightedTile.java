import java.io.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class GetWeightedTile
{
	public static void main(String[] args)throws IOException, FileNotFoundException  
	{	
        int tile_id = 100;
        int resolution = 12;
        int tile_xmin, tile_xmax, tile_ymin, tile_ymax;
		int column, start_line, stop_line;
		double increment  = 1. / resolution;
		ArrayList complete_sample_list = new ArrayList();
		
        if(args.length == 5)
        {
        	tile_xmin = Integer.parseInt(args[0]);
        	tile_xmax = Integer.parseInt(args[1]);
        	tile_ymin = Integer.parseInt(args[2]);
        	tile_ymax = Integer.parseInt(args[3]);
			resolution = Integer.parseInt(args[4]);	
			increment  = 1. / resolution;
			
			column = tile_xmin / 2;
			if(column > 0)
			  column--;
			start_line = column;
			column = tile_xmax / 2;
			if(column > 28)
				  column = 29;
			else
				column++;
			stop_line = column;
        	File file  = new File("C:/Users/Brian Crowley/Desktop/CleanData.txt");
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
          
    		ArrayList sample_list          = new ArrayList();
    	    String file_string = new String("C:/Users/Brian Crowley/Desktop/WTile" + tile_id + ".png");
    	    Sample previous_sample, current_sample, next_sample;
    	        
    	    double global_minimum_x = 539080;
    	    double global_minimum_y = 5823110;
    	    int [][] line_array     = ObjectMapper.getLineArray();
    	    double [] offset_array  = ObjectMapper.getOffsetArray();
    	    for(int i = start_line; i <= stop_line; i++)
    		{
    			int start     = line_array[i][0];
    			int stop      = line_array[i][1];
    			double offset = offset_array[i];
    			//Adjusting data according to flight line to make a coherent data set.
    			for(int j = start; j < stop; j++)
    		    {
    		    	Sample sample = (Sample)complete_sample_list.get(j);
    		    	sample.x -= global_minimum_x;
    		    	sample.y -= global_minimum_y;
    		    	sample.y += offset;
    		    	if(sample.y >= tile_ymin && sample.y < tile_ymax)
    		    	{	
    		    		if(sample.x >= tile_xmin && sample.x < tile_xmax)
    		    		{
    		    			sample_list.add(sample);
    		    		}
    		    	}	
    		    }	
    		}
    		double minimum_x, maximum_x;
    	    double minimum_y, maximum_y;
    	    double minimum_intensity, maximum_intensity;             
    	    boolean isPopulated[][];          
    	    boolean hasNeighbors[][];  
    	    boolean isInterpolated[][];
    	    int     number_of_samples[][];   
    	    double  cell_intensity[][];
    	              
    	    int sample_list_size = sample_list.size();
    	    Sample init_sample = (Sample)sample_list.get(0);
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
    	        double intensity_range = maximum_intensity - minimum_intensity;
    	        double average_intensity = minimum_intensity + intensity_range / 2;
    	        double x_range = maximum_x - minimum_x;
    	        double x_origin = minimum_x;
    	        double y_range = maximum_y - minimum_y;
    	        double y_origin = minimum_y;
    	        String range_double = String.format("%.2f", x_range);
    	        range_double = String.format("%.2f", y_range);
    	        int xdim = (int)Math.round(x_range + .5);
    	        int ydim = (int)Math.round(y_range + .5);
    	        // This is meters and we probably want a finer resolution.
    	        xdim *= resolution;
    	        ydim *= resolution;
    	        System.out.println("Xdim is "      + xdim);
    	        System.out.println("Ydim is "      + ydim);
    	        String xstring   = String.format("%.2f", minimum_x);
    	        xstring   = String.format("%.2f", maximum_x);
    	        String ystring = String.format("%.2f", minimum_y);
    	        ystring = String.format("%.2f", maximum_y);
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
                        //Now sort samples from surrounding tiles.
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
    	    	    		    //Could happen when the drone is flying slow enough that
    	    	    		    //it is quantizing values into limited precision bins.
    	    	    		    //This is an opportunity to adjust the data to improve
    	    	    		    //the accuracy.
    	    	    		    
    	    	    		    //We need to go through the list and
    	    	    		    //change the value by a small enough amount that its
    	    	    		    //different from the real value but unlikely to affect
    	    	    		    //any result significantly.  This is so when we
    	    	    		    //use the distance as a key to a hashtable, samples
    	    	    		    //don't start stepping on each other.
    	    	    		    
    	    	    		    //The data would probably be more accurate if we kept 
    	    	    		    //looking for a different value, then used that interval
    	    	    		    //to calculate the increment.
    	    	    		    
    	    	    		    //Kludgy, but any workaround that doesn't change
    	    	    		    //the actual data will be tedious and add a lot
    	    	    		    //of code and alter basic data structures.
    	    	    		    
    	    	    		    //Minimum possible floating point value.
    	    	    		    double current_increment = 0.0000000000000001; 
    	    	    		    double previous_distance = (double)distance_list.get(0);
    	    	    		    for(int n = 1; n < distance_list.size(); n++)
    	    	    		    {
    	    	    		    	double current_distance = (double)distance_list.get(n);
    	    	    		    	if(current_distance == previous_distance)
    	    	    		    	{
    	    	    		    		current_sample  = (Sample)unsorted_list.get(n);
    	    	    		    		previous_sample = (Sample)unsorted_list.get(n - 1);
    	    	    		    		current_distance  += current_increment; 
    	    	    		    		current_increment += 0.0000000000000001;
    	    	    		    		current_sample.distance = current_distance;
    	    	    		    		distance_list.set(n, current_distance);
    	    	    		    	}  
    	    	    		    }
    	    	    		    Collections.sort(distance_list);
    	    	    		    Hashtable <Double, Integer> location = new Hashtable <Double, Integer>();
    	        	    	    for(int n = 0; n < unsorted_list.size(); n++)
    	        	    		    location.put((double)distance_list.get(n), n);	
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
    	   	                Sample sample = (Sample)current_neighbor_list.get(0);
	   	                    cell_intensity[i][j] = sample.intensity;
	            	        isInterpolated[i][j] = true;
	            	        number_of_nearest_neighbors++;
    		    	    }
    		    	    else if(number_of_quadrants == 2)
    		    	    {
    		    	    	first_index = (int)neighbor_index_list.get(0); 
    	   	                second_index = (int)neighbor_index_list.get(1); 
    	   	                first_list   = (ArrayList)neighbor_list.get(first_index);
    	   	                second_list  = (ArrayList)neighbor_list.get(second_index);
    	   	                first_sample  = (Sample)first_list.get(0);
    		            	second_sample = (Sample)second_list.get(0); 
    		            	double total_distance = first_sample.distance + second_sample.distance;
    		            	cell_intensity[i][j] = first_sample.intensity * first_sample.distance / total_distance + 
    		            			               second_sample.intensity * second_sample.distance / total_distance; 
    		            	number_of_weighted_averages++;
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
    	        //System.out.println("The number of cells was "                 + number_of_cells);
    	        //System.out.println("The number of interpolated cells was "    + number_of_interpolated_cells);
    	        //System.out.println("The number of linear interpolations was " + number_of_linear_interpolations);
    	        //System.out.println("The number of bisecting averages was "    + number_of_bisecting_averages);
    	        //System.out.println("The number of weighted averages was "     + number_of_weighted_averages);
    	        //System.out.println("The number of nearest neighbors was "     + number_of_nearest_neighbors);
    	        intensity_range = maximum_intensity - minimum_intensity;
    	        BufferedImage data_image = new BufferedImage(xdim, ydim, BufferedImage.TYPE_USHORT_GRAY);
    	        WritableRaster data_raster = data_image.getRaster();
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
    	        
    	        String string = String.format("%.2f", interpolated_min);
    	        System.out.println("The minimum intensity in the interpolated values was " + string);
    	        string = String.format("%.2f", interpolated_max);
    	        System.out.println("The maximum intensity in the interpolated values was " + string); 
    	        boolean wasDilated = false;
    	        if(number_of_cells == number_of_interpolated_cells)
    	        {
    	        	double interpolated_range = interpolated_max - interpolated_min;
    	        	int    gray_value[] = new int[xdim * ydim];
    	        	for(int i = ydim - 1; i >= 0; i--)
    	            {
    	                for(int j = 0; j < xdim; j++)
    	                { 
    	                	int k = i * xdim + j;
    	                	int m = ydim - 1 - i;
    	                	double current_value  = cell_intensity[i][m];
    	                	current_value        -= interpolated_min;
    	                	/* 
    	                	This preserves precision for image processing by keeping 
    	                	what looks like 14-bit data. 
    	                	current_value        *= 100; 
    	                	gray_value[k]        = (int)current_value;
    	                	*/
    	                
    	                	// This gives us something we can easily look at on a 3 x 8 bit display.
    	                	// The division and multiplication reduce the accuracy even while it increases the range.
    	                	current_value        /= interpolated_range;
    	                	current_value        *= 65535;
    	                	gray_value[k]        = (int)current_value;
    	                	k++;
    	                }
    	            }
    	        	data_raster.setPixels(0, 0, xdim, ydim, gray_value);
    	            
    	            double xaddend = tile_xmax - tile_xmin;
    	            xaddend /= xdim;
    	            double yaddend = tile_ymax - tile_ymin;
    	            yaddend /= ydim;
    	            String filename = new String("C:/Users/Brian Crowley/Desktop/PTile" + tile_id + ".txt");
    	            //System.out.println("Filename is " + filename);
    	            try(PrintWriter output = new PrintWriter(filename))
    	            {
    	            	double xposition = tile_xmin;
    	                for(int i = 0; i < xdim; i++)
    	                {
    	                	xstring = String.format("%.2f", xposition);
    	                	double yposition = tile_ymin;
    	                	for(int j = 0; j < ydim; j++)
    	                	{
    	                		ystring = String.format("%.2f", yposition);
    	                	    String intensity = String.format("%.2f", cell_intensity[j][i]);
    	                	    //The decimal format maintains the nanotesla units although the value
    	                	    //can be accurately represented by an integer.
    	                	    output.println(xstring + " " + ystring + " " + intensity);
    	                	    yposition += yaddend;
    	                	}
    	                	xposition += xaddend;
    	                }
    	                output.close();
    	    	    }  
    	            
        	        try 
        	        {  
        	            ImageIO.write(data_image, "png", new File(file_string)); 
        	        } 
        	        catch(IOException e) 
        	        {  
        	            e.printStackTrace(); 
        	        }  
    	            
    	        }
    	        else
    	        {
    	        	wasDilated = true;
    	            double dilated_image[][] = new double[ydim][xdim]; 
    	            ImageMapper.getImageDilation(cell_intensity, isInterpolated, dilated_image);
    	            int number_of_cells_assigned_by_dilation = number_of_cells - number_of_interpolated_cells;
    	            System.out.println("The number of cells assigned by dilation was " + number_of_cells_assigned_by_dilation);
    	            System.out.println("The number of total cells  was " + (xdim * ydim));
    	            
    	            double max_value = 0;
    	            interpolated_min = maximum_intensity;
    	            interpolated_max = minimum_intensity;
    	            for(int i = 0; i < ydim; i++)
    	            {
    	                for(int j = 0; j < xdim; j++)
    	                {
    	                    if(dilated_image[i][j] < interpolated_min)
    	                        interpolated_min = dilated_image[i][j];
    	                    if(dilated_image[i][j] > interpolated_max)
    	                    	interpolated_max = dilated_image[i][j];	
    	                }
    	            }
    	            
    	            string = String.format("%.2f", interpolated_min);
        	        System.out.println("The minimum intensity in the interpolated values after dilation was " + string);
        	        string = String.format("%.2f", interpolated_max);
        	        System.out.println("The maximum intensity in the interpolated values after dilation was " + string);
    	            double interpolated_range = interpolated_max - interpolated_min;
    	            int    gray_value[] = new int[xdim * ydim];
    	        	//int k = 0;
    	        	int current_max = 0;
    	        	int current_min = Integer.MAX_VALUE;
    	        	for(int i = 0; i < ydim; i++)
    	            {
    	                for(int j = 0; j < xdim; j++)
    	                { 
    	                	// Flipping the image to orient it north-south.
    	                	int k = i * xdim + j;
    	                	int m = ydim - 1 - i;
    	                	double current_value  = dilated_image[m][j];
    	 
    	                	current_value        -= interpolated_min;
    	                	/*
    	                	Preserves precision for extended processing.
    	                	current_value        *= 100; 
    	                	gray_value[k]        = (int)current_value;
    	                	*/
    	                	
    	                	//Easy to look at on 3 x 8 bit display.
    	                	current_value        /= interpolated_range;
    	                	current_value        *= 65535;
    	                	gray_value[k]        = (int)current_value;
    	                	if(gray_value[k] > current_max)
    	                		current_max = gray_value[k];
    	                	if(gray_value[k] < current_min)
	                		    current_min = gray_value[k];
    	                	k++;
    	                }
    	        }
    	        System.out.println("Minimum pixel value is " + current_min);
    	        System.out.println("Maximum pixel value is " + current_max);
    	        data_raster.setPixels(0, 0, xdim, ydim, gray_value);   
    	        double xaddend = tile_xmax - tile_xmin;
    	        xaddend       /= xdim;
    	        double yaddend = tile_ymax - tile_ymin;
    	        yaddend       /= ydim;            
    	        String filename = new String("C:/Users/Brian Crowley/Desktop/PTile" + tile_id + ".txt");
    	        try(PrintWriter output = new PrintWriter(filename))
    	        {
    	            double xposition = tile_xmin;
    	            for(int i = 0; i < xdim; i++)
    	            {
    	                xstring = String.format("%.2f", xposition);
    	                double yposition = tile_ymin;
    	                for(int j = 0; j < ydim; j++)
    	                {
    	                	ystring = String.format("%.2f", yposition);
    	                	String intensity = String.format("%.2f", dilated_image[j][i]);
    	                	output.println(xstring + " " + ystring + " " + intensity);
    	                	yposition += yaddend;
    	                }
    	                xposition += xaddend;
    	            }
    	            output.close();
    	    	}
    	        try 
    	        {  
    	            ImageIO.write(data_image, "png", new File(file_string)); 
    	        } 
    	        catch(IOException e) 
    	        {  
    	            e.printStackTrace(); 
    	        }  
    	    }
        }
        else
        {
        	System.out.println("Usage: GetWeightedTile <xmin> <xmax> <ymin> <ymax> <resolution>");
			System.exit(0);	
        }
	}
}