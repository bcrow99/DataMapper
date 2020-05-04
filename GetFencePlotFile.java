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

public class GetFencePlotFile 
{

	public static void main(String[] args)throws IOException 
	{
		if(args.length != 2)
		{
			System.out.println("Usage: GetFencePlotFile <start index> <stop index> ");
			System.exit(0);
		}
		int    start     = Integer.valueOf(args[0]);
        int    stop      = Integer.valueOf(args[1]);
       
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
        
        for(int i = start; i < stop; i++)
        {
            Sample sample = (Sample)complete_sample_list.get(i);
            sample_list.add(sample);
        }

        String filestring = new String("C:/Users/Brian Crowley/Desktop/fenceplot.txt");
        
        Sample  first_sample  = (Sample)sample_list.get(2);
        Sample  second_sample = (Sample)sample_list.get(7);
        boolean isAscending   = true; 
        if(first_sample.y > second_sample.y)
        	isAscending = false;
         
        try(PrintWriter output = new PrintWriter(filestring))
        {
            if(isAscending)
            {
                for(int i = 0; i < sample_list.size(); i += 5)
                {
                	output.print("0.0 0.25 0.5 0.75 1.0 ");
                	Sample middle_sample = (Sample)sample_list.get(i + 2);
                	output.print(middle_sample.y);
            	    for(int j = 0; j < 5; j++)
            	    {
            	    	Sample current_sample = (Sample)sample_list.get(i + j);
            	    	output.print(" " + current_sample.intensity);
            	    }
            	    output.println();
            	}
            }
            else
            {
            	for(int i = sample_list.size() - 1; i > 0; i -= 5)
                {
            		output.print("0.0 0.25 0.5 0.75 1.0 ");
            		Sample middle_sample = (Sample)sample_list.get(i - 2);
                	output.print(middle_sample.y);
            	    for(int j = 0; j < 5; j++)
            	    {
            	    	Sample current_sample = (Sample)sample_list.get(i - j);	
            	    	output.print(" " + current_sample.intensity);	
            	    }
            	    output.println();
            	}       	
            }
            output.close();
	    }     
	}
}
