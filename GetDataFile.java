import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Hashtable;

public class GetDataFile
{
	public static void main(String[] args)throws IOException 
	{
		if(args.length != 1)
		{
			System.out.println("Usage: GetOffsetPlotFile <object id> ");
			System.exit(0);
		}
		
		int object_id          = 0; // So the compiler doesn't complain.
		object_id              = Integer.parseInt(args[0]);
		Hashtable object_table = ObjectMapper.getObjectTable();
	 	int [][]  object_array = (int [][])object_table.get(object_id);
       
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
        
        String filestring = new String("C:/Users/Brian Crowley/Desktop/Object" + object_id + ".txt");
        Sample previous_sample, current_sample, next_sample;
        int length        = object_array.length;
        
        // Values we collect from the segmented data that we can use so we don't have unneccessarily 
        // large or negative numbers to graph.
        double minimum_x, maximum_x;
        double minimum_y, maximum_y;
        double minimum_intensity, maximum_intensity;
        
        // Get a segmented sample list to collect statistics.
        // All indices later refer to the complete sample list.
        for(int i = 0; i < length; i++)
        {
        	int start = object_array[i][0];  
        	int stop  = object_array[i][1];
        	
        	for(int j = start; j < stop; j++)
        	{
        		Sample sample = (Sample)complete_sample_list.get(j);
        		sample_list.add(sample);
        	}	
        }
        
        Sample init_sample    = (Sample)sample_list.get(0);
        minimum_x = maximum_x = init_sample.x;
        minimum_y = maximum_y = init_sample.y;
        minimum_intensity = maximum_intensity = init_sample.intensity;
        
        for(int i = 1; i < sample_list.size(); i++)
        {
        	Sample sample = (Sample)sample_list.get(i);
        	if(sample.x <  minimum_x)
        		minimum_x = sample.x;
        	else if(sample.x > maximum_x)
        		maximum_x = sample.x;
        	if(sample.y <  minimum_y)
        		minimum_y = sample.y;
        	else if(sample.y > maximum_y)
        		maximum_y = sample.y;
        	if(sample.intensity <  minimum_intensity)
        		minimum_intensity = sample.intensity;
        	else if(sample.intensity > maximum_intensity)
        		maximum_intensity = sample.intensity;
        }
          
      	System.out.println("Minimum y is " + minimum_y);
      	System.out.println("Minimum intensity is " + minimum_intensity);
        
        try(PrintWriter output = new PrintWriter(filestring))
        {
        	int current_row = ObjectMapper.getInitRow(object_id);
            for(int i = 0; i < length; i++)
            {
        	    int start = object_array[i][0];  
        	    int stop  = object_array[i][1];
        	    boolean isAscending = true;  // Keep the compiler happy.
        	
        	    // Get direction of flight line from previous and next values of center sensor.
        	    previous_sample = (Sample)complete_sample_list.get(start - 3);
    		    current_sample  = (Sample)complete_sample_list.get(start + 2);
    		    next_sample     = (Sample)complete_sample_list.get(start + 7);
    		
    		    if(current_sample.y > previous_sample.y && next_sample.y > current_sample.y)
    		    {
    			    isAscending = true;
    			    System.out.println("Flight line is ascending.");
    		    }
    		    else if(current_sample.y < previous_sample.y && next_sample.y < current_sample.y)
    		    {
    			    isAscending = false;
    			    System.out.println("Flight line is descending.");
    		    }
    		    else
    		    {
    			    // The curve is not monotonic across three points.
    			    // Resolve this by checking the next 3 samples in the flight line.
    			    System.out.println("Non monotonic.");
    			    previous_sample = (Sample)complete_sample_list.get(start + 12);
        		    current_sample  = (Sample)complete_sample_list.get(start + 17);
        		    next_sample     = (Sample)complete_sample_list.get(start + 23);
        		    if(current_sample.y > previous_sample.y && next_sample.y > current_sample.y)
        		    {
        			    // Double check
        			    isAscending = true;
        		    }
        		    else if(current_sample.y < previous_sample.y && next_sample.y < current_sample.y)
        		    {
        			    isAscending = false;	
        		    }	
    		    }
    		    
    		    boolean doAdjustment = false;
    		    double  xlocation, ylocation, intensity;
    		    if(isAscending)
    		    {
    		    	output.println("#Sensor 5, Line " + current_row);
    			    for(int j = start; j < stop; j += 5)
    			    {
    				    Sample sample    = (Sample)complete_sample_list.get(j + 4);
    				    if(doAdjustment)
    				        xlocation = sample.y + 0.75 - minimum_y;
    				    else
    				    	xlocation = sample.y - minimum_y;
    				    String xstring   = String.format("%.2f", xlocation);
    				    
    				    ylocation = sample.x - minimum_x;
    				    String ystring   = String.format("%.2f", ylocation);
    				    
    				    intensity = sample.intensity;
    				    String intensity_string   = String.format("%.2f", intensity);
    				    output.println(xstring + " " + ystring + " " + intensity_string);
    			    }
    			    output.println();
    			    output.println();
    		        
    			    
    			    
    			    output.println("#Sensor 4, Line " + current_row);
    			    for(int j = start; j < stop; j += 5)
    			    {
    				    Sample sample    = (Sample)complete_sample_list.get(j + 3);
    				    if(doAdjustment)
    				        xlocation = sample.y + 0.75 - minimum_y;
    				    else
    				    	xlocation = sample.y - minimum_y;
    				    String xstring   = String.format("%.2f", xlocation);
    				    
    				    ylocation = sample.x - minimum_x;
    				    String ystring   = String.format("%.2f", ylocation);
    				    
    				    intensity = sample.intensity;
    				    String intensity_string   = String.format("%.2f", intensity);
    				    output.println(xstring + " " + ystring + " " + intensity_string);
    			    }
    			    output.println();
    			    output.println();
    			    
    			    output.println("#Sensor 3, Line " + current_row);
    			    for(int j = start; j < stop; j += 5)
    			    {
    				    Sample sample    = (Sample)complete_sample_list.get(j + 2);
    				    if(doAdjustment)
    				        xlocation = sample.y + 0.75 - minimum_y;
    				    else
    				    	xlocation = sample.y - minimum_y;
    				    String xstring   = String.format("%.2f", xlocation);
    				    
    				    ylocation = sample.x - minimum_x;
    				    String ystring   = String.format("%.2f", ylocation);
    				    
    				    intensity = sample.intensity;
    				    String intensity_string   = String.format("%.2f", intensity);
    				    output.println(xstring + " " + ystring + " " + intensity_string);
    			    }
    		        output.println();
    		        output.println();
    		        
    		        output.println("#Sensor 2, Line " + current_row);
    			    for(int j = start; j < stop; j += 5)
    			    {
    				    Sample sample    = (Sample)complete_sample_list.get(j + 1);
    				    if(doAdjustment)
    				        xlocation = sample.y + 0.75 - minimum_y;
    				    else
    				    	xlocation = sample.y - minimum_y;
    				    String xstring   = String.format("%.2f", xlocation);
    				    
    				    ylocation = sample.x - minimum_x;
    				    String ystring   = String.format("%.2f", ylocation);
    				    
    				    intensity = sample.intensity;
    				    String intensity_string   = String.format("%.2f", intensity);
    				    output.println(xstring + " " + ystring + " " + intensity_string);
    			    }
    		        output.println();
    		        output.println();
    		        
    		        output.println("#Sensor 1, Line " + current_row);
    			    for(int j = start; j < stop; j += 5)
    			    {
    				    Sample sample    = (Sample)complete_sample_list.get(j);
    				    if(doAdjustment)
    				        xlocation = sample.y + 0.75 - minimum_y;
    				    else
    				    	xlocation = sample.y - minimum_y;
    				    String xstring   = String.format("%.2f", xlocation);
    				    
    				    ylocation = sample.x - minimum_x;
    				    String ystring   = String.format("%.2f", ylocation);
    				    
    				    intensity = sample.intensity;
    				    String intensity_string   = String.format("%.2f", intensity);
    				    output.println(xstring + " " + ystring + " " + intensity_string);
    			    }
    		        output.println();
    		        output.println();
    		        
    		        current_row++;
    		    }
    		    else  // Flight line is descending.
    		    {
    		    	output.println("#Sensor 1, Line " + current_row);
    			    for(int j = stop - 1; j > start; j -= 5)
    			    {
    				    Sample sample = (Sample)complete_sample_list.get(j - 4);
    				    if(doAdjustment)
    				        xlocation = sample.y + 0.75 - minimum_y;
    				    else
    				    	xlocation = sample.y - minimum_y;
    				    String xstring   = String.format("%.2f", xlocation);
    				    
    				    ylocation = sample.x - minimum_x;
    				    String ystring   = String.format("%.2f", ylocation);
    				    
    				    intensity = sample.intensity;
    				    String intensity_string   = String.format("%.2f", intensity);
    				    output.println(xstring + " " + ystring + " " + intensity_string);
    				    
    			    }
    			    output.println();
        		    output.println();
        		    
        		    output.println("#Sensor 2, Line " + current_row);
    			    for(int j = stop - 1; j > start; j -= 5)
    			    {
    				    Sample sample = (Sample)complete_sample_list.get(j - 3);
    				    if(doAdjustment)
    				        xlocation = sample.y + 0.75 - minimum_y;
    				    else
    				    	xlocation = sample.y - minimum_y;
    				    String xstring   = String.format("%.2f", xlocation);
    				    
    				    ylocation = sample.x - minimum_x;
    				    String ystring   = String.format("%.2f", ylocation);
    				    
    				    intensity = sample.intensity;
    				    String intensity_string   = String.format("%.2f", intensity);
    				    output.println(xstring + " " + ystring + " " + intensity_string);
    				    
    			    }
    			    output.println();
        		    output.println();
        		    
        		    output.println("#Sensor 3, Line " + current_row);
    			    for(int j = stop - 1; j > start; j -= 5)
    			    {
    				    Sample sample = (Sample)complete_sample_list.get(j - 2);
    				    if(doAdjustment)
    				        xlocation = sample.y + 0.75 - minimum_y;
    				    else
    				    	xlocation = sample.y - minimum_y;
    				    String xstring   = String.format("%.2f", xlocation);
    				    
    				    ylocation = sample.x - minimum_x;
    				    String ystring   = String.format("%.2f", ylocation);
    				    
    				    intensity = sample.intensity;
    				    String intensity_string   = String.format("%.2f", intensity);
    				    output.println(xstring + " " + ystring + " " + intensity_string);
    				    
    			    }
    			    output.println();
        		    output.println();
        		    
        		    output.println("#Sensor 4, Line " + current_row);
    			    for(int j = stop - 1; j > start; j -= 5)
    			    {
    				    Sample sample = (Sample)complete_sample_list.get(j - 1);
    				    if(doAdjustment)
    				        xlocation = sample.y + 0.75 - minimum_y;
    				    else
    				    	xlocation = sample.y - minimum_y;
    				    String xstring   = String.format("%.2f", xlocation);
    				    
    				    ylocation = sample.x - minimum_x;
    				    String ystring   = String.format("%.2f", ylocation);
    				    
    				    intensity = sample.intensity;
    				    String intensity_string   = String.format("%.2f", intensity);
    				    output.println(xstring + " " + ystring + " " + intensity_string);
    				    
    			    }
    			    output.println();
        		    output.println();
        		    
        		    output.println("#Sensor 5, Line " + current_row);
        		    for(int j = stop - 1; j > start; j -= 5)
        		    {
        			    Sample sample = (Sample)complete_sample_list.get(j);
        			    if(doAdjustment)
    				        xlocation = sample.y + 0.75 - minimum_y;
    				    else
    				    	xlocation = sample.y - minimum_y;
    				    String xstring   = String.format("%.2f", xlocation);
    				    
    				    ylocation = sample.x - minimum_x;
    				    String ystring   = String.format("%.2f", ylocation);
    				    
    				    intensity = sample.intensity;
    				    String intensity_string   = String.format("%.2f", intensity);
    				    output.println(xstring + " " + ystring + " " + intensity_string);
        			    
        		    }
        		    output.println();
        		    output.println();
        		    
        		    current_row++;
        		    
    		    }	
            }
            output.close();
        }
	}
}
