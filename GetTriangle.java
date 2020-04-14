import java.io.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class GetTriangle
{
	public static void main(String[] args)throws IOException, FileNotFoundException  
	{	 
        //Information that we want to stay global for the rest of the program.
		
		//The raw data.
		ArrayList complete_sample_list = new ArrayList();
		
		//Segmented data.
		ArrayList sample_list = new ArrayList();
		
		//Information we'll collect about the data at the onset of the program.
		double minimum_x, maximum_x;
        double minimum_y, maximum_y;
        double minimum_intensity, maximum_intensity;
        
       
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
        
        // Setting our gray scale from values in entire
        // data set so segmented images are all using
        // the same scale and can be compared.
        
        // The signal for the samll features we are looking
        // for disappears when we do this.  It might be 
        // useful looking for larger features.
        
        /*
        int sample_list_size = complete_sample_list.size();
        Sample init_sample = (Sample)complete_sample_list.get(0);
        minimum_intensity = maximum_intensity = init_sample.intensity;
        for(int i = 1; i < sample_list_size; i++)
        {
        	Sample current_sample = (Sample)complete_sample_list.get(i);
        	double intensity = current_sample.intensity;
        	if(intensity < minimum_intensity)
        		minimum_intensity = intensity;
        	else if(intensity > maximum_intensity)
        		maximum_intensity = intensity;		
        }
        */
        
        
        String file_string = new String("C:/Users/Brian Crowley/Desktop/foo.jpg");
            
        // Obj 21
        for(int i = 226375; i < 227780; i++)
        {
        	Sample sample = (Sample)complete_sample_list.get(i);
        	sample_list.add(sample);
        }
        
       
        for(int i = 237900; i < 239290; i++)
        {
        	Sample sample = (Sample)complete_sample_list.get(i);
        	sample_list.add(sample);
        }
        
        for(int i = 253660; i < 254810; i++)
        {
        	Sample sample = (Sample)complete_sample_list.get(i);
        	sample_list.add(sample);
        }
        
        
        for(int i = 265450; i < 266840; i++)
        {
        	Sample sample = (Sample)complete_sample_list.get(i);
        	sample_list.add(sample);
        }
        
        for(int i = 281030; i < 282565; i++)
        {
        	Sample sample = (Sample)complete_sample_list.get(i);
        	sample_list.add(sample);
        }
        
        // Obj 8
        /*
        for(int i = 127850; i < 129090; i++)
        {
        	Sample sample = (Sample)complete_sample_list.get(i);
        	sample_list.add(sample);
        } 
      
        
        for(int i = 143605; i < 144870; i++)
        {
        	Sample sample = (Sample)complete_sample_list.get(i);
        	sample_list.add(sample);
        }
       
        
        for(int i = 155625; i < 156950; i++)
        {
        	Sample sample = (Sample)complete_sample_list.get(i);
        	sample_list.add(sample);
        }
       
        
        /*
        for(int i = 182975; i < 183640; i++)
        {
        	Sample sample = (Sample)complete_sample_list.get(i);
        	sample_list.add(sample);
        }
        
       
        for(int i = 280820; i < 282770; i++)
        {
        	Sample sample = (Sample)complete_sample_list.get(i);
        	sample_list.add(sample);
        }
        
       
        for(int i = 292520; i < 294660; i++)
        {
        	Sample sample = (Sample)complete_sample_list.get(i);
        	sample_list.add(sample);
        }
        */
        
        
        
        
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
        	Sample current_sample = (Sample)sample_list.get(i);
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
        xdim *= resolution;
        ydim *= resolution;
        //System.out.println("The xdim of the data grid is " + xdim);
        //System.out.println("The ydim of the data grid is " + ydim);
        //Now that we know the size of our raster, lets allocate memory for our information.
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
            Sample current_sample = (Sample)sample_list.get(i);
            double current_x = current_sample.x;
            double current_y = current_sample.y;
            current_x -= x_origin;
            current_y -= y_origin;
            
            
            
            // Tried to flip the coordinate system but it scrambes the neighbor lists.
            // Some subtlety here that I'm not getting but shouldn't have to do it anyway.
            // Just try and remember that the original data is oriented the opposite direction from the individual data
            // (but only when you have to).  I'll try to figure this out someday.
            // double reverse_y = y_range - current_y;
            // reverse_y *= resolution;
            current_x *= resolution;
            current_y *= resolution;
    
            int x_index       = (int) Math.floor(current_x);
            int y_index       = (int)Math.floor(current_y);
         
            
            isPopulated[y_index][x_index] = true;
            number_of_samples[y_index][x_index]++;
            
            int data_index   = y_index * xdim + x_index;
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
        	    	for(int k = 0; k < current_cell_list.size(); k++)
        	    	{
        	    		Sample current_sample = (Sample)current_cell_list.get(k);
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
        	    
    	    	//Now we're going to order the neighbor lists based on how close they are to the center
    	    	//of the cell.	We're doing this so that we check for smaller polygons first.  It's
        	    //possible that's not the ideal polygon if it's irregular enough--that is, a large
        	    //perimeter compared to the area.  We might want to go through the entire list and see
        	    //if some polygons with points further from the center have a smaller product of the 
        	    //perimeter and area.  Good experiment once everything is working.  Right now we'll
        	    //settle for the bounding polygon with the closest points.
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
    	    		    //Data from the distance list are not always unique.
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
    	    	data_array.add(list); 
        	}
        }
        
        // I'm seeing some alternating light and dark rows I don't like.
        // Trying to see if I can pin it on one or the other of the interpolation methods.
        // Can't really pin in it on one or the other.
        // Will try doing things with quadrilaterals to see if it's smoother.
        int number_of_linear_interpolations = 0;
        int number_of_bisecting_averages    = 0;
        
        // We have the information we need and we can start polygon searching.
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
        	    
        	    // To cut down on searching later.  
        	    // This lets us know where the populated quadrants are.
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
        	    // List of actual samples that we get from a list of lists.
        	    ArrayList current_neighbor_list;
        	    Sample    northwest_sample, north_sample,  northeast_sample, west_sample, east_sample;
        	    Sample    southwest_sample, south_sample,  southeast_sample;
        	    
        	    ArrayList first_list, second_list, third_list, fourth_list;
	    	    Sample    first_sample, second_sample, third_sample, fourth_sample;
	    	    int       first_index, second_index, third_index, fourth_index;
	    	    double    first_distance, second_distance, third_distance;
	    	    ArrayList possible_set_list;
	    	    Hashtable actual_set_table;
        	      
        	    switch(number_of_quadrants)
        	    {
        	    // There is no quadrant (NW, N, NE, W, E, SW, S, SE) that contains a sample.
        	    // We expect to assign these pixels later with an image dilation or some other strategy.
        	    case 0:  break;  
        	    
        	    // One quadrant contains one or more samples.
        	    case 1:  int index             = (int)neighbor_index_list.get(0);      // Get the index for the populated quadrant.
        	             current_neighbor_list = (ArrayList)neighbor_list.get(index);  // Get the list of actual samples.
        	             int sample_size = current_neighbor_list.size();
        	             
        	             if(sample_size == 1)
        	             {
        	            	 Sample sample = (Sample)current_neighbor_list.get(0);
        	            	 cell_intensity[i][j] = sample.intensity;
        	            	 isInterpolated[i][j] = true;
        	             }
        	             else if(sample_size >= 2)  // For now we'll just use the two closest points.
        	             {
        	            	 // Simple bisecting average with no dx and dy.
        	            	 // Try implementing bisecting average with dx and dy.
        	            	 first_sample  = (Sample)current_neighbor_list.get(0);
        	            	 second_sample = (Sample)current_neighbor_list.get(1); 
        	            	 cell_intensity[i][j] = DataMapper.getBisectingAverage(first_sample, second_sample, origin);
        	            	 number_of_bisecting_averages++;
         	    			 isInterpolated[i][j] = true;
        	             }
        	             /*
        	             else if(sample_size >= 3)  
        	             {
        	                  // Find line with nearest bisecting point and do above. 
        	            	  // Might want to factor in distance from endpoints.
        	             }
        	             */
        	             break;
        	             
        	    case 2:  first_index = (int)neighbor_index_list.get(0); 
        	             second_index = (int)neighbor_index_list.get(1); 
        	             first_list   = (ArrayList)neighbor_list.get(first_index);
        	             second_list  = (ArrayList)neighbor_list.get(second_index);
        	             first_sample  = (Sample)first_list.get(0);
    	            	 second_sample = (Sample)second_list.get(0); 
    	            	 cell_intensity[i][j] = DataMapper.getBisectingAverage(first_sample, second_sample, origin);
    	            	 number_of_bisecting_averages++;
     	    			 isInterpolated[i][j] = true; 
        	    	     break;
        	    // The interpolation for cases 1 and 2 works reasonably well.  The value we assign has some error built-in
        	    // that might be minimized.  Probably not important if we do most of the interpolating with bounding polygons.
        	    // Something to check later.  Should keep track of how many times getBisectingAverage gets used.
        	    // Could use the bisecting average of two samples adjusted by a delta x and a delta y calculated from a
        	    // third sample that doesn't actually contain the center of the cell.  Could try adjusting nearest neighbor same 
        	    // way.
        	    
        	    
        	    
        	   default: possible_set_list = QuadrantMapper.getPossibleContainingSets(neighbor_index_list);
        	            actual_set_table  = QuadrantMapper.getActualQuadrantSetTable(possible_set_list, neighbor_list, origin);
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
                            /*
                            System.out.println("Sample " + first_sample_index + " in quadrant "  + first_quadrant_index + 
                            		           " and sample " + second_sample_index + " in quadrant " + second_quadrant_index +
                            		           " and sample " + third_sample_index + " in quadrant " + third_quadrant_index  +
                            		           " produced the smallest and most regular bounding triangle.");
                            */	
        	            }
        	            else
        	            {
        	            	Hashtable distance_table = new Hashtable();
        	            	ArrayList distance_list  = new ArrayList();
        	            	for(int k = 0; k < neighbor_index_list.size(); k++)
        	            	{
        	            		int current_index       = (int)neighbor_index_list.get(k);
        	            		ArrayList current_list  = (ArrayList)neighbor_list.get(current_index);
        	            		Sample current_sample   = (Sample)current_list.get(0);
        	            		double current_distance = current_sample.distance;	
        	            		distance_list.add(current_distance);
        	            		distance_table.put(current_distance, current_index);
        	            	}
        	            	Collections.sort(distance_list);
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
        	    			isInterpolated[i][j] = true; 
        	            }
        	             
        	    /*
        	    // This pretty much works and helps show when some other interpolation methods are flawed.
        	    // Picks up about half the pixels in the image.
        	    default: if(neighborPopulated[0] && neighborPopulated[2] && neighborPopulated[7] && neighborPopulated[5])
	                     {
       		                 first_list  = (ArrayList)neighbor_list.get(0);
       	                     second_list = (ArrayList)neighbor_list.get(2);
       	                     third_list  = (ArrayList)neighbor_list.get(7); 
       	                     fourth_list = (ArrayList)neighbor_list.get(5); 
       	                     first_sample = (Sample)first_list.get(0);
        	    	         second_sample = (Sample)second_list.get(0);
        	    	         third_sample  = (Sample)third_list.get(0);
        	    	         fourth_sample = (Sample)fourth_list.get(0);
        	    	         cell_intensity[i][j] = DataMapper.getLinearInterpolation(origin, first_sample, second_sample, third_sample, fourth_sample);
        	    	         isInterpolated[i][j] = true;
        	            }
	                    else if(neighborPopulated[1] && neighborPopulated[4] && neighborPopulated[6] && neighborPopulated[3])
	                    {
	                	    first_list  = (ArrayList)neighbor_list.get(1);
     	                    second_list = (ArrayList)neighbor_list.get(4);
     	                    third_list  = (ArrayList)neighbor_list.get(6); 
     	                    fourth_list = (ArrayList)neighbor_list.get(3); 
     	                    first_sample = (Sample)first_list.get(0);
      	    	            second_sample = (Sample)second_list.get(0);
      	    	            third_sample  = (Sample)third_list.get(0);
      	    	            fourth_sample = (Sample)fourth_list.get(0);
      	    	            cell_intensity[i][j] = DataMapper.getLinearInterpolation(origin, first_sample, second_sample, third_sample, fourth_sample);
      	    	            isInterpolated[i][j] = true;   
	                    }
        	            */
        	       }
              }
        }
   
            	
        //System.out.println("Ydim is " + ydim);
        System.out.println("The number of cells was " + (xdim * ydim));
        for(int i = 0; i < 9; i++)
        {
        	System.out.println(neighbor_histogram[i] + " cells had " + i + " quadrants with samples.");
        }
        //Check how many pixels got assigned.
        int number_of_interpolated_cells = 0;
        for(int i = 0; i < ydim; i++)
        {
            for(int j= 0; j < xdim; j++)
            {
            	if(isInterpolated[i][j] == true)
            		number_of_interpolated_cells++;
            		
            }
        }  
        System.out.println("The number of interpolated cells was " + number_of_interpolated_cells);
        System.out.println("The number of linear interpolations was " + number_of_linear_interpolations);
        System.out.println("The number of bisecting averages was " + number_of_bisecting_averages);
        double intensity_range = maximum_intensity - minimum_intensity;
        BufferedImage data_image = new BufferedImage(xdim, ydim, BufferedImage.TYPE_INT_RGB);
        for(int i = 0; i < ydim; i++)
        {
            for(int j = 0; j < xdim; j++)
            {  	
                double current_value  = cell_intensity[i][j];
                current_value        -= minimum_intensity;
                current_value        /= intensity_range;
                current_value        *= 255.;
                int gray_value        = (int)current_value;
        	    int rgb_value = ((gray_value&0x0ff)<<16)|((gray_value&0x0ff)<<8)|(gray_value&0x0ff);
        	    data_image.setRGB(j, i, rgb_value);  
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