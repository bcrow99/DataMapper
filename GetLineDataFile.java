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

public class GetLineDataFile
{
	public static void main(String[] args)throws IOException 
	{
		if(args.length != 1)
		{
			System.out.println("Usage: GetLineDataFile <line #> ");
			System.exit(0);
		}
		
		int index = 0; // So the compiler doesn't complain.
		index     = Integer.parseInt(args[0]);
		index--;  //Counting from zero to index the array.  
		          //Convention for lines is counting from one, 1->30.
	
		int [][] line_array = ObjectMapper.getLineArray();
		int start           = line_array[index][0];
		int stop            = line_array[index][1];
		
		boolean isAscending = false;
		if(index % 2 == 1)
			isAscending = true;
		
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
        
        
        
        
        
        
        String filestring = new String("C:/Users/Brian Crowley/Desktop/Line" + (index + 1) + ".txt");
        for(int i = start; i < stop; i++)
    	{
    		Sample sample = (Sample)complete_sample_list.get(i);
    		sample_list.add(sample);
    	}	
        
        Sample init_sample = (Sample)sample_list.get(0);
        double minimum_x   = init_sample.x;
        double minimum_y   = init_sample.y;
    
        for(int i = 1; i < sample_list.size(); i++)
        {
            Sample sample = (Sample)sample_list.get(i); 
            if(sample.x < minimum_x)
            	minimum_x = sample.x;
            if(sample.y < minimum_y)
            	minimum_y = sample.y;
        }
        
        try(PrintWriter output = new PrintWriter(filestring))
        {
        	int sample_list_size = sample_list.size();
    		if(isAscending)
    		{
    		     output.println("#Sensor 5, Line " + (index + 1));
    			 //for(int i = 0; i < sample_list_size; i += 5)
    		     for(int i = sample_list_size - 5; i >= 0; i -= 5)
    			 {
    				  Sample sample    = (Sample)sample_list.get(i + 4);
    				  double xlocation = sample.y - minimum_y;
    				  String xstring   = String.format("%.2f", xlocation);
    				    
    				  double ylocation = sample.x - minimum_x;
    				  String ystring   = String.format("%.2f", ylocation);
    				    
    				  double intensity = sample.intensity;
    				  String intensity_string   = String.format("%.2f", intensity);
    				  output.println(xstring + " " + ystring + " " + intensity_string);
    			 }
    			 output.println();
    			 output.println();
    		        
    			     
    			 output.println("#Sensor 4, Line " + (index + 1));
    			 //for(int i = 0; i < sample_list_size; i += 5)
    			 for(int i = sample_list_size - 5; i >= 0; i -= 5)
    			 {
    				    Sample sample    = (Sample)sample_list.get(i + 3);
    				    double xlocation = sample.y - minimum_y;
      				    String xstring   = String.format("%.2f", xlocation);
      				    
      				    double ylocation = sample.x - minimum_x;
      				    String ystring   = String.format("%.2f", ylocation);
      				    
      				    double intensity = sample.intensity;
      				    String intensity_string   = String.format("%.2f", intensity);
      				    output.println(xstring + " " + ystring + " " + intensity_string);
    			 }
    			 output.println();
    			 output.println();
    			    
    			    output.println("#Sensor 3, Line " + (index + 1));
    			    //for(int i = 0; i < sample_list_size; i += 5)
    			    for(int i = sample_list_size - 5; i >= 0; i -= 5)
    			    {
    				    Sample sample    = (Sample)sample_list.get(i + 2);
    				    double xlocation = sample.y - minimum_y;
      				    String xstring   = String.format("%.2f", xlocation);
      				    
      				    double ylocation = sample.x - minimum_x;
      				    String ystring   = String.format("%.2f", ylocation);
      				    
      				    double intensity = sample.intensity;
      				    String intensity_string   = String.format("%.2f", intensity);
    				    output.println(xstring + " " + ystring + " " + intensity_string);
    			    }
    		        output.println();
    		        output.println();
    		        
    		        output.println("#Sensor 2, Line " + (index + 1));
    		        //for(int i = 0; i < sample_list_size; i += 5)
    		        for(int i = sample_list_size - 5; i >= 0; i -= 5)
    			    {
    				    Sample sample    = (Sample)sample_list.get(i + 1);
    				    double xlocation = sample.y - minimum_y;
      				    String xstring   = String.format("%.2f", xlocation);
      				    
      				    double ylocation = sample.x - minimum_x;
      				    String ystring   = String.format("%.2f", ylocation);
      				    
      				    double intensity = sample.intensity;
    				    String intensity_string   = String.format("%.2f", intensity);
    				    output.println(xstring + " " + ystring + " " + intensity_string);
    			    }
    		        output.println();
    		        output.println();
    		        
    		        output.println("#Sensor 1, Line " + (index + 1));
    		        //for(int i = 0; i < sample_list.size(); i += 5)
    		        for(int i = sample_list_size - 5; i >= 0; i -= 5)
    			    {
    				    Sample sample    = (Sample)sample_list.get(i);
    				    double xlocation = sample.y - minimum_y;
      				    String xstring   = String.format("%.2f", xlocation);
      				    
      				    double ylocation = sample.x - minimum_x;
      				    String ystring   = String.format("%.2f", ylocation);
      				    
      				    double intensity = sample.intensity;
    				    String intensity_string   = String.format("%.2f", intensity);
    				    output.println(xstring + " " + ystring + " " + intensity_string);
    			    }
    		        output.println();
    		        output.println();
    		    }
    		    else  // Flight line is descending.
    		    {
    		    	output.println("#Sensor 1, Line " + (index + 1));
    			    //for(int j = stop - 1; j > start; j -= 5)
    		    	//for(int i = sample_list_size - 1; i > 0; i -= 5)
    		    	for(int i = 4; i < sample_list_size; i += 5)
    			    {
    				    Sample sample = (Sample)sample_list.get(i - 4);
    				    double xlocation = sample.y - minimum_y;
      				    String xstring   = String.format("%.2f", xlocation);
      				    
      				    double ylocation = sample.x - minimum_x;
      				    String ystring   = String.format("%.2f", ylocation);
      				    
      				    double intensity = sample.intensity;
    				    String intensity_string   = String.format("%.2f", intensity);
    				    output.println(xstring + " " + ystring + " " + intensity_string);
    				    
    			    }
    			    output.println();
        		    output.println();
        		    
        		    output.println("#Sensor 2, Line " + (index + 1));
    			    
        		    //for(int i = sample_list_size - 1; i > 0; i -= 5)
        		    for(int i = 4; i < sample_list_size; i += 5)
    			    {
    				    Sample sample = (Sample)sample_list.get(i - 3);
    				    double xlocation = sample.y - minimum_y;
      				    String xstring   = String.format("%.2f", xlocation);
      				    
      				    double ylocation = sample.x - minimum_x;
      				    String ystring   = String.format("%.2f", ylocation);
      				    
      				    double intensity = sample.intensity;
    				    String intensity_string   = String.format("%.2f", intensity);
    				    output.println(xstring + " " + ystring + " " + intensity_string);
    			    }
    			    output.println();
        		    output.println();
        		    
        		    output.println("#Sensor 3, Line " + (index + 1));
    			    //for(int j = stop - 1; j > start; j -= 5)
        		    //for(int i = sample_list_size - 1; i > 0; i -= 5)
        		    for(int i = 4; i < sample_list_size; i += 5)
    			    {
    				    Sample sample = (Sample)sample_list.get(i - 2);
    				    double xlocation = sample.y - minimum_y;
      				    String xstring   = String.format("%.2f", xlocation);
      				    
      				    double ylocation = sample.x - minimum_x;
      				    String ystring   = String.format("%.2f", ylocation);
      				    
      				    double intensity = sample.intensity;
    				    String intensity_string   = String.format("%.2f", intensity);
    				    output.println(xstring + " " + ystring + " " + intensity_string);
    			    }
    			    output.println();
        		    output.println();
        		    
        		    output.println("#Sensor 4, Line " + (index + 1));
    			    //for(int j = stop - 1; j > start; j -= 5)
        		    //for(int i = sample_list_size - 1; i > 0; i -= 5)
        		    for(int i = 4; i < sample_list_size; i += 5)
    			    {
    				    Sample sample = (Sample)sample_list.get(i - 1);
    				    double xlocation = sample.y - minimum_y;
      				    String xstring   = String.format("%.2f", xlocation);
      				    
      				    double ylocation = sample.x - minimum_x;
      				    String ystring   = String.format("%.2f", ylocation);
      				    
      				    double intensity = sample.intensity;
    				    String intensity_string   = String.format("%.2f", intensity);
    				    output.println(xstring + " " + ystring + " " + intensity_string);
    			    }
    			    output.println();
        		    output.println();
        		    
        		    output.println("#Sensor 5, Line " + (index + 1));
        		    //for(int j = stop - 1; j > start; j -= 5)
        		    //for(int i = sample_list_size - 1; i > 0; i -= 5)
        		    for(int i = 4; i < sample_list_size; i += 5)
        		    {
        			    Sample sample = (Sample)sample_list.get(i);
        			    double xlocation = sample.y - minimum_y;
      				    String xstring   = String.format("%.2f", xlocation);
      				    
      				    double ylocation = sample.x - minimum_x;
      				    String ystring   = String.format("%.2f", ylocation);
      				    
      				    double intensity = sample.intensity;
    				    String intensity_string   = String.format("%.2f", intensity);
    				    output.println(xstring + " " + ystring + " " + intensity_string);
        			    
        		    }
        		    output.println();
        		    output.println();
    		 }
    		 output.close();
         }
            
     }
}
