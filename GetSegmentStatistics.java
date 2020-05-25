import java.io.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class GetSegmentStatistics
{
	public static void main(String[] args)throws IOException, FileNotFoundException  
	{	
        int object_id = 0;
		if(args.length != 1)
		{
			System.out.println("Usage: GetSegmentStatistics <object id>");
			System.exit(0);
		}
		else
		{
			object_id = Integer.parseInt(args[0]);	
		}
		Hashtable object_table         = ObjectMapper.getObjectTable();
	 	int [][]  object_array         = (int [][])object_table.get(object_id);
		ArrayList complete_sample_list = new ArrayList();
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
        
        
        String file_string = new String("C:/Users/Brian Crowley/Desktop/foo.jpg");
        
        // Load all the clipped data to get global statistics.
        int[][] line_array = ObjectMapper.getLineArray();
        int length         = line_array.length;
        int init_index     = line_array[0][0];
        Sample init_sample = (Sample)complete_sample_list.get(init_index); 
        double global_minimum_x = init_sample.x;
        double global_maximum_x = init_sample.x;
        double global_minimum_y = init_sample.y;
        double global_maximum_y = init_sample.y;
        double global_minimum_intensity = init_sample.intensity;
        double global_maximum_intensity = init_sample.intensity;
        for(int i = 0; i < length; i++)
        {
        	int start    = line_array[i][0];
        	int stop     = line_array[i][1];
        	for(int j = start; j < stop; j++)
        	{
        		Sample sample = (Sample)complete_sample_list.get(j);	
        		if(sample.x < global_minimum_x)
        			global_minimum_x = sample.x;
        		else if(sample.x > global_maximum_x)
        			global_maximum_x = sample.x;
        		if(sample.y < global_minimum_y)
        			global_minimum_y = sample.y;
        		else if(sample.y > global_maximum_y)
        			global_maximum_y = sample.y;
        		if(sample.intensity < global_minimum_intensity)
        			global_minimum_y = sample.y;
        		else if(sample.y > global_maximum_y)
        			global_maximum_intensity = sample.intensity;
        	}	
        }
        
        // Load object segments.
        length           = object_array.length;
        init_index       = object_array[0][0];
        init_sample      = (Sample)complete_sample_list.get(init_index);
        double minimum_x = init_sample.x;
        double maximum_x = init_sample.x;
        double minimum_y = init_sample.y;
        double maximum_y = init_sample.y;
        double minimum_intensity = init_sample.intensity;
        double maximum_intensity = init_sample.intensity;
        for(int i = 0; i < length; i++)
        {
        	int start    = object_array[i][0];
        	int stop     = object_array[i][1];
        	for(int j = start; j < stop; j++)
        	{
        		Sample sample = (Sample)complete_sample_list.get(j);	
        		if(sample.x < minimum_x)
        			minimum_x = sample.x;
        		else if(sample.x > maximum_x)
        			maximum_x = sample.x;
        		if(sample.y < minimum_y)
        			minimum_y = sample.y;
        		else if(sample.y > maximum_y)
        			maximum_y = sample.y;
        		if(sample.intensity < minimum_intensity)
        			minimum_intensity = sample.intensity;
        		else if(sample.intensity > maximum_intensity)
        			maximum_intensity = sample.intensity;
        	}	
        }
        
        // Second pass to collect histogram.
        double intensity_range = maximum_intensity - minimum_intensity;
        double interval        = intensity_range / 255.;
        int histogram[]        = new int[256];
        for(int i = 0; i < length; i++)
        {
        	int start    = object_array[i][0];
        	int stop     = object_array[i][1];
        	for(int j = start; j < stop; j++)
        	{
        		Sample sample    = (Sample)complete_sample_list.get(j);	
                double intensity = sample.intensity - minimum_intensity;
                
                double index     = intensity / interval;
                int    k         = (int)Math.floor(index);
                histogram[k]++; 
        	}	
        }
        
        double relative_x = minimum_x - global_minimum_x;
        relative_x /= (global_maximum_x - global_minimum_x);
        String xstring   = String.format("%.2f", relative_x);
        
        double relative_y = minimum_y - global_minimum_y;
        relative_y /= (global_maximum_y - global_minimum_y);
        String ystring   = String.format("%.2f", relative_y);
        
        System.out.println("The relative position of the segment in the data space is " + xstring + "," + ystring + ".");
        double segment_area = (maximum_x - minimum_x) * (maximum_y - minimum_y);
        String area_string   = String.format("%.2f", segment_area);
        System.out.println("The segment area is " + area_string + " m^2.");
        
        
        String filestring = new String("C:/Users/Brian Crowley/Desktop/Object" + object_id + "_histogram.txt");
        double current_intensity = minimum_intensity + interval / 2;
        
        try(PrintWriter output = new PrintWriter(filestring))
        {
        	output.println("# Object " + object_id + " histogram");
            for(int i = 0; i < 256; i++)
            {
            	String intensity_string   = String.format("%.2f", current_intensity);
            	output.println(intensity_string + " " + histogram[i]);
            	current_intensity += interval;
            }
            output.close();
        }
	}
}