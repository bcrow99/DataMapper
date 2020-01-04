import java.io.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class GetDataMap
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
        
     
    
        //A parameter we can adjust to increase our resolution.
        int   resolution = 8;
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
            	    	System.out.println("The number of samples is " + sample_list.size()); 	
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
        xdim *= resolution;
        ydim *= resolution;
    
        cell_intensity = new double[ydim][xdim];
        for(int i = 0; i < ydim; i++)
        {
        	for(int j = 0; j < xdim; j++)
        	{
        		cell_intensity[i][j] = minimum_intensity;
        	}
        }
        ArrayList data_array = new ArrayList(xdim * ydim);
        for(int i = 0; i < xdim * ydim; i++)
        { 
        	ArrayList cell_list = new ArrayList();
        	data_array.add(cell_list);
        }
        	   	
        for(int i = 156570; i < sample_list_size; i++)
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
            int data_index = y_index * xdim + x_index;
            ArrayList cell_list = (ArrayList)data_array.get(data_index);
            cell_list.add(current_sample);
        }
        
        long start_time = System.nanoTime();
        for(int i = 0; i < ydim; i++)
        {  
        	double ycenter = y_origin + i * increment + increment * .5;
        	for(int j = 0; j < xdim; j++)
        	{
        		double xcenter       = j * increment + increment / 2. + x_origin;
        		int data_index       = i * xdim + j;
        		ArrayList cell_list  = (ArrayList)data_array.get(data_index);
        		if(cell_list.size() != 0)
        		{
        			double total_intensity = 0;
        			for(int k = 0; k < cell_list.size(); k++)
        			{
        				Sample sample = (Sample)cell_list.get(k);
        				sample.setDistance(xcenter, ycenter);
        				total_intensity += sample.intensity;
        			}
        			average_intensity = total_intensity / cell_list.size();
        			cell_intensity[i][j] = average_intensity;
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
                gray_value = 255 - gray_value;
            	int rgb_value = ((gray_value&0x0ff)<<16)|((gray_value&0x0ff)<<8)|(gray_value&0x0ff);
            	data_image.setRGB(j, i, rgb_value);  	
            }
        }
        
        try 
        {  
            ImageIO.write(data_image, "jpg", new File("C:/Users/Brian Crowley/Desktop/data_map.jpg")); 
        } 
        catch(IOException e) 
        {  
            e.printStackTrace(); 
        }  
	}
}
