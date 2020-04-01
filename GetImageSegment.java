import java.io.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class GetImageSegment
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
        
        
        String file_string = new String("C:/Users/Brian Crowley/Desktop/obj21.jpg");
            
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
        */
        
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
    	    		    //The two intensity values are different so I'm not
    	    		    //adding the same sample twice.  
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
    	    	//Keep this information handy so we have an alternative to nearest neighbors later.
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
        	    	
        	    	// These should be the four closest points.  Worth double checking.
        	    	// Looks like.  Still should write a test program.
        	    	Sample first_sample  = (Sample)first_list.get(0);
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
        	    	if(slope == 0)
        	    	{
        	    	    y5 = y1;	
        	    	}
        	    	else
        	    	{
        	    	    double y_intercept = DataMapper.getYIntercept(upper_left, slope);
        	    	    y5 = slope * xcenter + y_intercept;
        	    	}
        	    	slope = DataMapper.getSlope(bottom);
        	    	if(slope == 0)
        	    	{
        	    	    y6 = y4;	
        	    	}
        	    	else
        	    	{
        	    	    double y_intercept = DataMapper.getYIntercept(lower_left, slope);
        	    	    y6 = slope * xcenter + y_intercept;
        	    	}
        	    	    			    
        	    	if(x2 != x3)
        	    	{
        	    	    slope = DataMapper.getSlope(right);
        	    	    double y_intercept = DataMapper.getYIntercept(upper_right, slope);
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
        	    	    double y_intercept = DataMapper.getYIntercept(lower_left, slope);
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
        	    	if(Double.isNaN(intensity))
    		        	System.out.println("Intensity value is not a number.");
        	    	if(intensity <= minimum_intensity)
        	    		System.out.println("Intensity value is less than or equal to the minimum_intensity.");
        	    	cell_intensity[i][j] = intensity;
        	    	//cell_intensity[i][j] = maximum_intensity;
        	    	isInterpolated[i][j] = true;	  
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
        	    	
        	    	// These should be the four closest points.  Worth double checking.
        	    	// Looks like.  Still should write a test program.
        	    	Sample first_sample  = (Sample)first_list.get(0);
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
        	    	if(slope == 0)
        	    	{
        	    		y5 = y1;
        	    	}
        	    	else
        	    	{
        	    	    double y_intercept = DataMapper.getYIntercept(upper_left, slope);
        	    	    y5 = slope * xcenter + y_intercept;
        	    	}
        	    	slope = DataMapper.getSlope(bottom);
        	    	if(slope == 0)
        	    	{
        	    		y6 = y4;
        	    	}
        	    	else
        	    	{
        	    	    double y_intercept = DataMapper.getYIntercept(lower_left, slope);
        	    	    y6 = slope * xcenter + y_intercept;
        	    	}
        	    	    			    
        	    	if(x2 != x3)
        	    	{
        	    	    slope = DataMapper.getSlope(right);
        	    	    double y_intercept = DataMapper.getYIntercept(upper_right, slope);
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
        	    	    double y_intercept = DataMapper.getYIntercept(lower_left, slope);
        	    	    x6 = (ycenter - y_intercept) / slope;
        	    	}
        	    	else
        	    	{
        	    	    double distance = left.ptSegDist(origin);
        	    	    x6              = xcenter - distance;
        	    	}
        	    	    			      
        	    	Point2D.Double middle_top    = new Point2D.Double(xcenter, y5);
        	    	Point2D.Double middle_bottom = new Point2D.Double(xcenter, y6);
        	    	Point2D.Double middle_right  = new Point2D.Double(x5, ycenter);
        	    	Point2D.Double middle_left   = new Point2D.Double(x6, ycenter); 
        	    	    		        
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
        	    	//cell_intensity[i][j] = maximum_intensity;
        	    	isInterpolated[i][j] = true;	   	
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
        	    	
        	    	boolean foundTriangle = false;
        	    	outer: for(int m = 0; m < first_list.size(); m++)
        	    	{
        	    	    for(int n = 0; n < second_list.size(); n++)
        	    	    {
        	    	    	for(int p = 0; p < third_list.size(); p++)
        	    	    	{ 
        	    	    		Point2D.Double origin        = new Point2D.Double(xcenter, ycenter);
        	    	    		Sample         first_sample  = (Sample)first_list.get(m);
        	    	    		Sample         second_sample = (Sample)second_list.get(n);
        	    	    		Sample         third_sample  = (Sample)third_list.get(p);
        	    	    		boolean        isContained   = DataMapper.containsPoint(origin, first_sample, second_sample, third_sample);
        	        	    	
        	    	    		if(isContained) 
        	    	    		{
        	    	    			foundTriangle = true;
        	    	    			
        	    	    			double x1 = first_sample.x;
            	        	    	double y1 = first_sample.y; 
            	        	    	
            	        	    	double x2 = second_sample.x;
            	        	    	double y2 = second_sample.y;
            	        	    	
            	        	    	double x3 = third_sample.x;
            	        	    	double y3 = third_sample.y;
        	    	    			
        	    	    			Point2D.Double base1  = new Point2D.Double(x1, y1);
    	    	    		        Point2D.Double top    = new Point2D.Double(x2, y2);
    	    	    		        Point2D.Double base2  = new Point2D.Double(x3, y3); 
    	    	    		        origin                = new Point2D.Double(xcenter, ycenter);
    	    	    		        
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
    	    	    		        //cell_intensity[i][j] = maximum_intensity;
        	    	    			isInterpolated[i][j] = true;
    	    	    		        break outer;
        	    	    		}
        	    	    	}
        	    	    }
        	    	} 
        	    	if(!foundTriangle)
        	    	{
        	            //This would be a weighted average of the nearest neighbors.
        	    		
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
        	        	
        	    		
        	    		//This is probably more accurate in the sense that we're doing something
        	    		//that involves information about where the other points are in relation 
        	    		//to each other, which nearest neighbors does not do.
        	    		Sample first_sample = (Sample)first_list.get(0);
        	    	    double x1           = first_sample.x;
        	    	    double y1           = first_sample.y;
        	    	    
        	    	    Sample second_sample  = (Sample)second_list.get(0);
        	    	    double x2             = second_sample.x;
        	    	    double y2             = second_sample.y;
        	    	    
        	    	    Sample third_sample = (Sample)third_list.get(0);
        	    	    double x3           = third_sample.x;
        	    	    double y3           = third_sample.y;
    	    		    
        	    	    Point2D.Double origin     = new Point2D.Double(xcenter, ycenter);
        	    	    Line2D.Double first_line  = new Line2D.Double(x1, y1, x2, y2);
        	    	    Line2D.Double second_line = new Line2D.Double(x2, y2, x3, y3);
        	    	    Line2D.Double third_line  = new Line2D.Double(x3, y3, x1, y1);
        	    	    
        	    	    double first_length       = first_line.ptSegDist(xcenter, ycenter);	
        	    	    double second_length      = second_line.ptSegDist(xcenter, ycenter);
        	    	    double third_length       = third_line.ptSegDist(xcenter, ycenter);
        	    	    if(first_length < second_length && first_length < third_length)
        	    	    {
        	    	    	// Get bisecting average from first line--using x1, y1 and x2, y2
        	    	    	cell_intensity[i][j] = DataMapper.getBisectingAverage(first_sample, second_sample, origin);
        	    			isInterpolated[i][j] = true;
        	    	    }
        	    	    else if(second_length < first_length && second_length < third_length)
        	    	    {
        	    	    	// Get bisecting average from second line--using x2, y2 and x3, y3
        	    	    	cell_intensity[i][j] = DataMapper.getBisectingAverage(second_sample, third_sample, origin);
        	    			isInterpolated[i][j] = true;
        	    	    }
        	    	    else if(third_length < first_length && third_length < second_length)
        	    	    {
        	    	    	// Get bisecting average from third line--using x3, y3, x1, y1
        	    	    	cell_intensity[i][j] = DataMapper.getBisectingAverage(third_sample, second_sample, origin);
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
        	    	    //cell_intensity[i][j] = maximum_intensity;
        	    	}
        	    }
        	    
        	    // All these scenarios can be processed with the same code.
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
        	    	boolean foundTriangle = false;
        	    	outer: for(int m = 0; m < first_list.size(); m++)
        	    	{
        	    	    for(int n = 0; n < second_list.size(); n++)
        	    	    {
        	    	    	for(int p = 0; p < third_list.size(); p++)
        	    	    	{
        	    	    		Point2D.Double origin        = new Point2D.Double(xcenter, ycenter);
        	    	    		Sample         first_sample  = (Sample)first_list.get(m);
        	    	    		Sample         second_sample = (Sample)second_list.get(n);
        	    	    		Sample         third_sample  = (Sample)third_list.get(p);
        	    	    		boolean        isContained   = DataMapper.containsPoint(origin, first_sample, second_sample, third_sample);
        	    	    		if(isContained) 
        	    	    		{
        	    	    			foundTriangle = true;
        	    	    			
        	    	    			double x1 = first_sample.x;
            	        	    	double y1 = first_sample.y; 
            	        	    	
            	        	    	double x2 = second_sample.x;
            	        	    	double y2 = second_sample.y;
            	        	    	
            	        	    	double x3 = third_sample.x;
            	        	    	double y3 = third_sample.y;
        	    	    			
        	    	    			Point2D.Double base1  = new Point2D.Double(x1, y1);
    	    	    		        Point2D.Double top    = new Point2D.Double(x2, y2);
    	    	    		        Point2D.Double base2  = new Point2D.Double(x3, y3); 
    	    	    		        
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
    	    	    		        //cell_intensity[i][j] = maximum_intensity;
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
        	    		
        	    		//Doing something different because it might be more accurate.
        	    		Sample first_sample  = (Sample)first_list.get(0);
        	    	    double x1            = first_sample.x;
        	    	    double y1            = first_sample.y;
        	    	    
        	    	    Sample second_sample = (Sample)second_list.get(0);
        	    	    double x2            = second_sample.x;
        	    	    double y2            = second_sample.y;
        	    	    
        	    	    Sample third_sample  = (Sample)third_list.get(0);
        	    	    double x3            = third_sample.x;
        	    	    double y3            = third_sample.y;
    	    		    
        	    	    Point2D.Double origin     = new Point2D.Double(xcenter, ycenter);
        	    	    Line2D.Double first_line  = new Line2D.Double(x1, y1, x2, y2);
        	    	    Line2D.Double second_line = new Line2D.Double(x2, y2, x3, y3);
        	    	    Line2D.Double third_line  = new Line2D.Double(x3, y3, x1, y1);
        	    	    
        	    	    double first_length       = first_line.ptSegDist(xcenter, ycenter);	
        	    	    double second_length      = second_line.ptSegDist(xcenter, ycenter);
        	    	    double third_length       = third_line.ptSegDist(xcenter, ycenter);
        	    	    if(first_length < second_length && first_length < third_length)
        	    	    {
        	    	    	// Get bisecting average from first line--using x1, y1 and x2, y2
        	    	    	cell_intensity[i][j] = DataMapper.getBisectingAverage(first_sample, second_sample, origin);
        	    			isInterpolated[i][j] = true;
        	    	    }
        	    	    else if(second_length < first_length && second_length < third_length)
        	    	    {
        	    	    	// Get bisecting average from second line--using x2, y2 and x3, y3
        	    	    	cell_intensity[i][j] = DataMapper.getBisectingAverage(second_sample, third_sample, origin);
        	    			isInterpolated[i][j] = true;
        	    	    }
        	    	    else if(third_length < first_length && third_length < second_length)
        	    	    {
        	    	    	// Get bisecting average from third line--using x3, y3, x1, y1
        	    	    	cell_intensity[i][j] = DataMapper.getBisectingAverage(third_sample, first_sample, origin);
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
        	    	    //cell_intensity[i][j] = maximum_intensity;
	
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
      
        	    	
        	    	Sample first_sample  = (Sample)first_list.get(0);
        	    	double x1            = first_sample.x;
        	    	double y1            = first_sample.y;
                    if(first_list.size() == 1)
                    {
                    	Sample second_sample = (Sample)second_list.get(0);
                    	double x2            = second_sample.x;
                    	double y2            = second_sample.y;
                    	if(second_list.size() == 1)
                    	{
                    		Point2D.Double origin      = new Point2D.Double(xcenter, ycenter);
                    		first_sample  = (Sample)first_list.get(0);
            	    		second_sample = (Sample)second_list.get(0);
            	    		
            	    		// Nearest neighbors--let's try using bisecting average instead.
            	    		/*
    	        	    	double total_distance = 0.;
    	        	        total_distance += first_sample.distance;
    	        	        total_distance += second_sample.distance;
    	        	        double weight1 = first_sample.distance / total_distance;
    	        	        double weight2 = second_sample.distance / total_distance; 
    	        	        double intensity = first_sample.intensity * weight1 + second_sample.intensity * weight2;
    	        	        cell_intensity[i][j] = intensity;
    	        	        */
            	    		//cell_intensity[i][j] = maximum_intensity;
            	    		cell_intensity[i][j] = DataMapper.getBisectingAverage(first_sample, second_sample, origin);
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
                    		    Sample third_sample        = (Sample)second_list.get(m);
                    		    Point2D.Double origin      = new Point2D.Double(xcenter, ycenter);
        	    	    		boolean        isContained = DataMapper.containsPoint(origin, first_sample, second_sample, third_sample);
                    		    
                    		  
        	    	    		if(isContained) 
        	    	    		{
        	    	    			double x3           = third_sample.x;
                        		    double y3           = third_sample.y;
        	    	    			Point2D.Double base1  = new Point2D.Double(x1, y1);
    	    	    		        Point2D.Double top    = new Point2D.Double(x2, y2);
    	    	    		        Point2D.Double base2  = new Point2D.Double(x3, y3); 
    	    	    		        
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
    	    	    		        //cell_intensity[i][j] = maximum_intensity;
    	    	    		        cell_intensity[i][j] = intensity;
        	    	    			isInterpolated[i][j] = true;
        	    	    			foundBoundingTriangle = true;
    	    	    		        break outer;
        	    	    		}
                    		}
                    		if(!foundBoundingTriangle)
                    		{
                    			// Need to compare this at some point with bisecting average.
                    			// Just assuming any thing is better than weighted nearest neighbors.
                    			
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
                	    	    double x3           = third_sample.x;
                	    	    double y3           = third_sample.y;
            	    		    
                	    	    Point2D.Double origin     = new Point2D.Double(xcenter, ycenter);
                	    	    Line2D.Double first_line  = new Line2D.Double(x1, y1, x2, y2);
                	    	    Line2D.Double second_line = new Line2D.Double(x2, y2, x3, y3);
                	    	    Line2D.Double third_line  = new Line2D.Double(x3, y3, x1, y1);
                	    	    
                	    	    double first_length       = first_line.ptSegDist(xcenter, ycenter);	
                	    	    double second_length      = second_line.ptSegDist(xcenter, ycenter);
                	    	    double third_length       = third_line.ptSegDist(xcenter, ycenter);
                	    	    if(first_length < second_length && first_length < third_length)
                	    	    {
                	    	    	// Get bisecting average from first line--using x1, y1 and x2, y2
                	    	    	cell_intensity[i][j] = DataMapper.getBisectingAverage(first_sample, second_sample, origin);
                	    			isInterpolated[i][j] = true;
                	    	    }
                	    	    else if(second_length < first_length && second_length < third_length)
                	    	    {
                	    	    	cell_intensity[i][j] = DataMapper.getBisectingAverage(second_sample, third_sample, origin);
                	    			isInterpolated[i][j] = true;
                	    	    }
                	    	    else if(third_length < first_length && third_length < second_length)
                	    	    {
                	    	    	// Get bisecting average from third line--using x3, y3, x1, y1
                	    	    	cell_intensity[i][j] = DataMapper.getBisectingAverage(third_sample, first_sample, origin);
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
                	    	    //cell_intensity[i][j] = maximum_intensity;
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
                        	Point2D.Double origin         = new Point2D.Double(xcenter, ycenter);
                        	third_sample                  = (Sample)second_list.get(0);
                		    boolean foundBoundingTriangle = false;
                    		outer: for(int m = 1; m < first_list.size(); m++)
                    		{
                    			second_sample       = (Sample)first_list.get(m);
                    			boolean isContained = DataMapper.containsPoint(origin, first_sample, second_sample, third_sample);
        	    	    		if(isContained) 
        	    	    		{
        	    	    		    double x2 = second_sample.x;
                    		        double y2 = second_sample.y;
                    			    double x3           = third_sample.x;
                    		        double y3           = third_sample.y;
        	    	    			//Do a linear interpolation
        	    	    			Point2D.Double base1  = new Point2D.Double(x1, y1);
    	    	    		        Point2D.Double top    = new Point2D.Double(x2, y2);
    	    	    		        Point2D.Double base2  = new Point2D.Double(x3, y3); 
    	    	    		        
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
    	    	    		        
    	    	    		        //cell_intensity[i][j] = maximum_intensity;
    	    	    		        cell_intensity[i][j] = intensity;
        	    	    			isInterpolated[i][j] = true;
        	    	    			foundBoundingTriangle = true;
        	    	    			
    	    	    		        break outer;
        	    	    		}
                    		}
                    		if(!foundBoundingTriangle)
                    		{
                    		    //Weighted average of nearest nearest neighbors.
                    			//first_sample  = (Sample)first_list.get(0);
                	    		//second_sample = (Sample)second_list.get(0);
        	        	    	//double total_distance = 0.;
        	        	        //total_distance += first_sample.distance;
        	        	        //total_distance += second_sample.distance;
        	        	        //double weight1 = first_sample.distance / total_distance;
        	        	        //double weight2 = second_sample.distance / total_distance;    
        	        	        //double intensity = first_sample.intensity * weight1 + second_sample.intensity * weight2;
                    			
                    			//Something different.
                    			first_sample = (Sample)first_list.get(0);
                	    	    x1           = first_sample.x;
                	    	    y1           = first_sample.y;
                	    	    
                	    	    second_sample  = (Sample)second_list.get(0);
                	    	    double x2             = second_sample.x;
                	    	    double y2             = second_sample.y;
                	    	    
                	    	    third_sample = (Sample)second_list.get(0);
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
                	    	    	cell_intensity[i][j] = DataMapper.getBisectingAverage(first_sample, second_sample, origin);
                	    			isInterpolated[i][j] = true;
                	    	    }
                	    	    else if(second_length < first_length && second_length < third_length)
                	    	    {
                	    	    	// Get bisecting average from second line--using x2, y2 and x3, y3
                	    	    	cell_intensity[i][j] = DataMapper.getBisectingAverage(second_sample, third_sample, origin);
                	    			isInterpolated[i][j] = true;
                	    	    }
                	    	    else if(third_length < first_length && third_length < second_length)
                	    	    {
                	    	    	// Get bisecting average from third line--using x3, y3, x1, y1
                	    	    	cell_intensity[i][j] = DataMapper.getBisectingAverage(third_sample, first_sample, origin);
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
                	    	    //cell_intensity[i][j] = maximum_intensity;
                    		}
                        }
                        else
                        { 
                        	/*
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
        	    	    		        if(Double.isNaN(area1))
        	    	    		        	System.out.println("Area 1 is not a number.");
        	    	    		        
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
                    		*/
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
                        		
                        	    // Would probably improve image accuracy by looking for bounding boxes first.
                        	    // Assuming this is better than weighted average of nearest neighbors.
                        		first_sample  = (Sample)first_list.get(0);
                        		second_sample = (Sample)first_list.get(1);
                        		third_sample  = (Sample)second_list.get(0);
                        		Sample fourth_sample = (Sample)second_list.get(1);
                        		x1 = first_sample.x;
                        		y1 = first_sample.y;
                        		double x2 = second_sample.x;
                        		double y2 = second_sample.y;
                        		double x3 = third_sample.x;
                        		double y3 = third_sample.y;
                        		double x4 = fourth_sample.x;
                        		double y4 = fourth_sample.y;
                        		Point2D.Double origin = new Point2D.Double(xcenter, ycenter);
                        		Line2D.Double first_line  = new Line2D.Double(x1, y1, x2, y2);
                        		Line2D.Double second_line = new Line2D.Double(x2, y2, x3, y3);
                        		Line2D.Double third_line  = new Line2D.Double(x3, y3, x4, y4);
                        		Line2D.Double fourth_line = new Line2D.Double(x4, y4, x1, y1);
                        	    double first_distance  = first_line.ptSegDist(xcenter, ycenter);
                        	    double second_distance = second_line.ptSegDist(xcenter, ycenter);
                        	    double third_distance  = third_line.ptSegDist(xcenter, ycenter);
                        	    double fourth_distance = fourth_line.ptSegDist(xcenter, ycenter);
                        	    
                        	    if(first_distance <= second_distance && first_distance <= third_distance && first_distance <= third_distance)
                        	    {
                        	    	cell_intensity[i][j] = DataMapper.getBisectingAverage(first_sample, second_sample, origin);
                    	    		isInterpolated[i][j] = true;	
                        	    }
                        	    else if(second_distance <= first_distance && second_distance <= third_distance && second_distance <= fourth_distance)
                        	    {
                        	    	cell_intensity[i][j] = DataMapper.getBisectingAverage(second_sample, third_sample, origin);
            	    		        isInterpolated[i][j] = true;
                        	    }
                        	    else if(third_distance <= first_distance && third_distance <= second_distance && third_distance <= fourth_distance)
                        	    {
                        	    	cell_intensity[i][j] = DataMapper.getBisectingAverage(third_sample, fourth_sample, origin);
    	    		                isInterpolated[i][j] = true;
                        	    }
                        	    else 
                        	    {
                        	    	cell_intensity[i][j] = DataMapper.getBisectingAverage(fourth_sample, first_sample, origin);
    	    		                isInterpolated[i][j] = true;	
                        	    }
                        	    //cell_intensity[i][j] = maximum_intensity;
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
        	    		    			 Sample first_sample   = (Sample)first_list.get(0);
        	        	    	         Sample second_sample  = (Sample)second_list.get(0);
        	        	    	         Point2D.Double origin = new Point2D.Double(xcenter, ycenter);
        	        	    	         cell_intensity[i][j]  = DataMapper.getBisectingAverage(first_sample, second_sample, origin);
                      	    			 isInterpolated[i][j]  = true; 
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
	        	    	                  Point2D.Double origin = new Point2D.Double(xcenter, ycenter);
	        	    	                  Line2D.Double first_line = new Line2D.Double(x1, y1, x2, y2);
	        	    	                  Line2D.Double second_line = new Line2D.Double(x2, y2, x3, y3);
	        	    	                  Line2D.Double third_line = new Line2D.Double(x3, y3, x1, y1);
	                              	      double first_distance  = first_line.ptSegDist(xcenter, ycenter);
	                              	      double second_distance = second_line.ptSegDist(xcenter, ycenter);
	                              	      double third_distance  = third_line.ptSegDist(xcenter, ycenter);
	                              	    
	                              	      
	                              	      if(first_distance < second_distance && first_distance < third_distance)
	                              	      {
	                              	    	  cell_intensity[i][j]  = DataMapper.getBisectingAverage(first_sample, second_sample, origin);
	                              	    	  isInterpolated[i][j] = true;
	                              	    	  break outer;
	                              	      }
	                              	      else if(second_distance < first_distance && second_distance < third_distance)
	                              	      {
	                              	    	  cell_intensity[i][j]  = DataMapper.getBisectingAverage(second_sample, third_sample, origin);
                    	    	              isInterpolated[i][j] = true;	
                    	    	              break outer;
	                              	      }
	                              	      else 
	                              	      {
	                              	    	  cell_intensity[i][j]  = DataMapper.getBisectingAverage(third_sample, first_sample, origin);
             	    	                      isInterpolated[i][j] = true;
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
                        	              Point2D.Double origin = new Point2D.Double(xcenter, ycenter);
                        	           
                        	             int line = 0;
                        	             if(first_distance < second_distance && first_distance < third_distance && first_distance < third_distance)
                                 	    {
                        	            	cell_intensity[i][j]  = DataMapper.getBisectingAverage(first_sample, second_sample, origin);
                     	    	            isInterpolated[i][j] = true;
                              	            break outer;	
                                 	    }
                                 	    else if(second_distance < first_distance && second_distance < third_distance && second_distance < fourth_distance)
                                 	    {
                                 	    	cell_intensity[i][j]  = DataMapper.getBisectingAverage(second_sample, third_sample, origin);
                     	    	            isInterpolated[i][j] = true;
                              	            break outer;		
                                 	    }
                                 	    else if(third_distance < first_distance && third_distance < second_distance && third_distance < fourth_distance)
                                 	    {
                                 	    	cell_intensity[i][j]  = DataMapper.getBisectingAverage(third_sample, fourth_sample, origin);
                     	    	            isInterpolated[i][j] = true;
                              	            break outer;
                                 	    }
                                 	    else 
                                 	    {
                                 	    	cell_intensity[i][j]  = DataMapper.getBisectingAverage(fourth_sample, first_sample, origin);
                     	    	            isInterpolated[i][j] = true;
                              	            break outer;   
                                 	    }
                        	             
                                 	}
		    		            }
		    	            }
	                    //}
        	    	}
                    //cell_intensity[i][j] = maximum_intensity;
                    if(isInterpolated[i][j] == false)
                    {
                        System.out.println("Odd cell was not assigned.");
                        System.out.println("The number of neighbors was " + number_of_neighbors);
                        System.out.println("");
                        double total_distance = 0.;
                        
                        if(number_of_neighbors != 0)
                        {
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
            	    	    //cell_intensity[i][j] = maximum_intensity;
            	    	    cell_intensity[i][j] = intensity;
            	    	    isInterpolated[i][j] = true;
                        }	
                    }
        	    }
        	}
        }
        
        double gray1[]               = new double[xdim * ydim];
        double gray2[]               = new double[xdim * ydim];
        double src[];
        double dst[];
        boolean isAssigned[]     = new boolean[xdim * ydim];
        double intensity_range   = maximum_intensity - minimum_intensity;
        BufferedImage data_image = new BufferedImage(xdim, ydim, BufferedImage.TYPE_INT_RGB);
        
        //Reformat the data to use single digit indices
        for(int i = 0; i < ydim; i++)
        {
            for(int j= 0; j < xdim; j++)
            {
            	int k                = i * xdim + j;
            	double current_value = cell_intensity[i][j];
            	current_value       -= minimum_intensity;
            	//current_value       /= intensity_range;
            	//current_value       *= 255.;
                gray1[k]              = current_value;	
                isAssigned[k]        = isInterpolated[i][j];
            }
        }
        
        //Check to see if pixels are unassigned because of a sparse data set.
        int number_of_uninterpolated_cells = 0;
        for(int i = 0; i < ydim; i++)
        {
            for(int j= 0; j < xdim; j++)
            {
            	if(isInterpolated[i][j] == false)
            		number_of_uninterpolated_cells++;
            		
            }
        }
        boolean even = true;
        System.out.println("The image has " + number_of_uninterpolated_cells + " uninterpolated cells.");
        if(number_of_uninterpolated_cells == 0) //Go ahead and generate image
        {	
            for(int i = 0; i < ydim; i++)
            {
                for(int j = 0; j < xdim; j++)
                {
            	    int k                 = i * xdim + j;	
                    double current_value  = gray1[k];
                    current_value        /= intensity_range;
                    current_value        *= 255.;
                    int gray_value        = (int)current_value;
            	    int rgb_value = ((gray_value&0x0ff)<<16)|((gray_value&0x0ff)<<8)|(gray_value&0x0ff);
            	    data_image.setRGB(j, i, rgb_value);  
                }
            }
        }
        else
        {
            while(number_of_uninterpolated_cells != 0)  //Do an image dilation.
            {
            	if(even == true)
            	{
            		src = gray1;
            		dst = gray2;
            		even = false;
            	}
            	else
            	{
            		src = gray2;
            		dst = gray1;
            		even = true;
            	}
            	DataMapper.getImageDilation(src, isAssigned, xdim, ydim, dst);
            	
            	number_of_uninterpolated_cells = 0;
            	int i = xdim * ydim;
            	for(int j = 0; j < i; j++)
            		if(isAssigned[j] = false)
            			number_of_uninterpolated_cells++;
            }
            System.out.println("The image has been dilated so there are no uninterpolated cells.");
            if(even == true)
                src = gray1;
            else
            	src = gray2;
            for(int i = 0; i < ydim; i++)
            {
                for(int j = 0; j < xdim; j++)
                {
            	    int k                = i * xdim + j;
            	    double current_value = src[k];
            	    current_value       /= intensity_range;
            	    current_value       *= 255.;
                    int gray_value       = (int)current_value;
            	    int rgb_value        = ((gray_value&0x0ff)<<16)|((gray_value&0x0ff)<<8)|(gray_value&0x0ff);
            	    data_image.setRGB(j, i, rgb_value);  
                }
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