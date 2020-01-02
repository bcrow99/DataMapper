import java.io.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class GetPointImage
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
     
          
        for(int i = 0; i < ydim; i++)
        {
        	for(int j = 0; j < xdim; j++)
        	{
        		isPopulated[i][j]       = false;
        		hasNeighbors[i][j]      = false;
        		number_of_samples[i][j] = 0;
        		cell_intensity[i][j]    = minimum_intensity;
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
        	double ycenter = i * increment + increment * .5 + y_origin;
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
        	    
    	    	//Now we're going to order the neighbor lists of points based on how close they are to the center
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
    	    	//Keep this information handy so we can find nearest points later.
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
        		
        		double total_distance = 0;
        		
        		for(int k = 0; k < 8; k++)
        		{
        			ArrayList array_list = (ArrayList)neighbor_list.get(k);
        			if(array_list.size() != 0)
        			{
        			    Sample sample        = (Sample)array_list.get(0);
        			    total_distance      += sample.distance;
        			}
        		}
        		
        		double intensity = 0;
        		
        		for(int k = 0; k < 8; k++)
        		{
        			ArrayList array_list     = (ArrayList)neighbor_list.get(k);
        			if(array_list.size() != 0)
        			{
        			    Sample sample            = (Sample)array_list.get(0);
        			    double current_intensity = sample.intensity;
        			    double current_weight    = sample.distance / total_distance;
        			    intensity               += sample.intensity * current_weight;
        			}
        		}	
        		cell_intensity[i][j] = intensity;
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
            ImageIO.write(data_image, "jpg", new File("C:/Users/Brian Crowley/Desktop/point.jpg")); 
        } 
        catch(IOException e) 
        {  
            e.printStackTrace(); 
        }     
	}
}