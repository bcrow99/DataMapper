import java.awt.*;
import java.awt.Color.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;

public class YFencePlotter
{
	// Entire data set in interleaved format.
	public ArrayList   data          = new ArrayList(); // Adjusted sample data for wand plot.
	public ArrayList   relative_data = new ArrayList(); // Unadjusted sample data for calculations.
	
	// Data segments segmented by sensor and range, attached to these arrays as lists.
	ArrayList data_array = new ArrayList();          // Adjusted sample data for wand plot, from which smoothed segments are extracted.
	ArrayList relative_data_array = new ArrayList(); // Unadjusted and possibly smoothed sample data for calculations.
	
	// Entire data set segmented by sensor
	ArrayList set_array = new ArrayList();
	ArrayList relative_set_array = new ArrayList();
	
	// Information we collect at start up.
	public double      global_xmin, global_xmax;
	public double      global_ymin, global_ymax;
	public double      global_intensity_min, global_intensity_max;
	
	// Values that determine a data segment.
	public double      data_offset  = .0;
	public double      data_range   = .004;
	
	// Local min/max for current data segment.
	public double      seg_min, seg_max;
	
	// Arbitrary values when not graphing all values and/or not using entire display area.
	public double      clipped_min, clipped_max;
	
	// An array that keeps track of what data is associated 
	// with what part of the graph.
	ArrayList[][]      pixel_data;
	
	// An array of indices to object locations.
	public ArrayList   object_index         = new ArrayList();
	
	// An index of the endpoints of the unclipped lines.
	int[][] line_index;
	
	public PlotCanvas  data_canvas;
	public JScrollBar  data_scrollbar;
	public RangeSlider dynamic_range_slider;
	public DynamicRangeCanvas dynamic_range_canvas;
	public JFrame      frame;
	public LocationCanvas location_canvas;
	
	
	public int         slider_resolution    = 2640;
	public int         scrollbar_resolution = 2640;
	public int         data_length          = 2640;
	public Color[]     fill_color           = new Color[10];
	public Color[]     outline_color        = new Color[10];
	
	public double      normal_xstep         = .5;
	public double      normal_ystep         = .5;
	public double      xlocation            = .5;
	public double      ylocation            = .5;	
	public int         x_remainder          = 0;
	public int         y_remainder          = 0;
	
	int                start_flight_line    = 0;
	int                stop_flight_line     = 0;
	int                start_index          = 0;
	int                stop_index           = 0;
	
	boolean            raster_overlay       = false;
	boolean            reverse_view         = false;
	boolean            persistent_data      = false;
	boolean            show_id              = true;
	boolean            show_position        = false;
	boolean            show_label           = false;
	boolean            show_slope           = false;
	boolean            show_data            = false;
	boolean            color_key            = false;
	boolean            relative_mode        = true;
	boolean            location_changing    = false;
	boolean            data_clipped         = false;
	boolean            config_file_exists   = false;
	
	boolean            dynamic_slider_changing = false;
	boolean            dynamic_button_changing = false;
	
	boolean            location_slider_changing = false;
	boolean            range_button_changing    = false;
	boolean            object_button_changing   = false;
	boolean            scrollbar_changing       = false;
	//boolean            text_field_changing      = false;
	
	
	int     append_gui_index     = 0;
    int     startpoint_gui_index = 0;
    int     midpoint_gui_index   = 0;
    int     endpoint_gui_index   = 0;
	
	
	boolean            append_data          = false;
	int                append_line          = 0;
	int                append_sensor        = 0;
	double             append_x             = 0;
	double             append_y             = 0;
	double             append_intensity     = 0;
	double             append_x_abs         = 0;
	double             append_y_abs         = 0;
	int                append_x_position    = 0;
	int                append_y_position    = 0;
	int                append_index         = -1;
	
	double             startpoint_x = 0;
	double             startpoint_y = 0;
	int                startpoint_line = 0;
	int                startpoint_sensor = 0;
	double             startpoint_intensity = 0;
	int                startpoint_x_position = 0;
	int                startpoint_y_position = 0;
	boolean            startpoint_set;
	int                startpoint_index;
	
	double             midpoint_x = 0;
	double             midpoint_y = 0;
	int                midpoint_line = 0;
	int                midpoint_sensor = 0;
	double             midpoint_intensity = 0;
	int                midpoint_x_position = 0;
	int                midpoint_y_position = 0;
	boolean            midpoint_set;
	int                midpoint_index;
	
	double             endpoint_x = 0;
	double             endpoint_y = 0;
	int                endpoint_line = 0;
	int                endpoint_sensor = 0;
	double             endpoint_intensity = 0;
	int                endpoint_x_position = 0;
	int                endpoint_y_position = 0;
	boolean            endpoint_set;
	int                endpoint_index;
	
	String             graph_label = new String("");
	
	int                smooth               = 5;
	double             scale_factor         = 1.;
	double             minimum_y            = 0;
	double             maximum_y            = 0;
	
	
	Canvas[]           sensor_canvas        = new SensorCanvas[10];
	int[]              sensor_state         = new int[10];
	boolean[]          visible              = new boolean[5];
	boolean[]          transparent          = new boolean[5];
	
	public JDialog     save_dialog;
	public JDialog     information_dialog;
	public JDialog     placement_dialog;
	public JDialog     sensor_dialog;
	public JDialog     smooth_dialog;
	public JDialog     scale_dialog;
	public JDialog     location_dialog;
	public JDialog     set_location_dialog;
	public JDialog     set_object_dialog;
	public JDialog     dynamic_range_dialog;
	public JDialog     segment_image_dialog;
	
	public JDialog     label_dialog;
	public JDialog     range_dialog;
	public JDialog     load_config_dialog;
	public JDialog     save_config_dialog;
	
	public PlacementCanvas placement_canvas;
	public SegmentImageCanvas segment_image_canvas;
	public SegmentImage segment_image;
	public SegmentMap  segment_map;
	public JTextField  load_config_input;
	public JTextField  save_config_input;
	public JTextField  range_information;
	public JTextField  offset_information;
	public JTextArea   sample_information;
	
	public JTextArea slope_output;
	public JDialog   slope_dialog;
	public JDialog   segment_map_dialog;
	
	
	// Another text box that shows the offset
	// in a normalized form.
	// An alterntive would be using the middle
	// of the range for the location.
	public JTextField  location_information;
	
	
	public JSlider     location_slider;
	
	boolean flight_line_odd = false;
	
	// Gui components that are fired by load handler.
	public JCheckBoxMenuItem view_item;
	public JCheckBoxMenuItem mode_item;
	public JCheckBoxMenuItem overlay_item;
	
	public JCheckBoxMenuItem color_key_item;
	
	public JCheckBoxMenuItem show_id_item;
	public JCheckBoxMenuItem show_position_item;
	public JCheckBoxMenuItem show_data_item;
	public JSlider           smooth_slider;
	public JSlider           factor_slider;
	public JScrollBar        xstep_scrollbar;
	public JScrollBar        ystep_scrollbar;
	public JTextField        lower_bound;
	public JTextField        upper_bound;
	
	int       left_margin        = 70;
	int       right_margin       = 70;
	int       top_margin         = 30;
	int       bottom_margin      = 70;
	
	BufferedImage buffered_image;
	
	public static void main(String[] args)
	{
		if(args.length != 1)
		{
			System.out.println("Usage: YFencePlotter <data file>");
			System.exit(0);
		} 
		else
		{
			System.out.println("This is version 4.1.7 of wand.");
			String version = System.getProperty("java.version");
			//System.out.println("Current java version is " + version);
			
			try
			{
				try
				{
					YFencePlotter window = new YFencePlotter(args[0]);
					window.frame.setVisible(true);
				} 
				catch(Exception e)
				{
					e.printStackTrace();
				}
			} 
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public YFencePlotter(String filename)
	{
		outline_color[0] = new Color(0, 0, 0);
		outline_color[1] = new Color(0, 0, 75);
		outline_color[2] = new Color(0, 75, 0);
		outline_color[3] = new Color(75, 0, 0);
		outline_color[4] = new Color(0, 75, 75);
		outline_color[5] = new Color(75, 0, 75);
		outline_color[6] = new Color(75, 75, 0);
		outline_color[7] = new Color(75, 75, 75);
		outline_color[8] = new Color(75, 75, 150);
		outline_color[9] = new Color(75, 150, 75);

		fill_color[0]    = new Color(196, 196, 196);
		fill_color[1]    = new Color(196, 196, 224);
		fill_color[2]    = new Color(196, 224, 196);
		fill_color[3]    = new Color(224, 196, 196);
		fill_color[4]    = new Color(196, 224, 255);
		fill_color[5]    = new Color(224, 196, 224);
		fill_color[6]    = new Color(224, 224, 196);
		fill_color[7]    = new Color(224, 224, 224);
		fill_color[8]    = new Color(224, 224, 255);
		fill_color[9]    = new Color(224, 255, 224);

		// Start file input.
		
		File file = new File(filename);
		if (file.exists())
		{
			// Get the current directory and see if it contains a config file.
			StringTokenizer tokenizer = new StringTokenizer(filename, "/");
			String current_directory = new String("");
			String directory         = new String("");
			while(tokenizer.hasMoreTokens())
			{
				directory = new String(current_directory);
				String  next_string = tokenizer.nextToken();
				current_directory   = new String(current_directory + next_string + "/");
			}
			
			String config_filename = new String(directory + "wand.cfg");
			File config_file = new File(config_filename);
			if(config_file.exists())
			{
			    System.out.println("Loading config file.");
				config_file_exists = true;
				try
				{
			        BufferedReader config_reader   = new BufferedReader(new InputStreamReader(new FileInputStream(config_file)));
					String line;
					StringTokenizer config_tokenizer;
					line = config_reader.readLine();  // Sensor id's--we already know they're 0-4.		    
					line = config_reader.readLine();  // Visibiltiy
					config_tokenizer = new StringTokenizer(line);
					int number_of_tokens = config_tokenizer.countTokens();
					String token = config_tokenizer.nextToken();
					number_of_tokens--;
					for(int i = 0; i < number_of_tokens; i++)
					{
					    token = config_tokenizer.nextToken();
						if(token.equals("true"))
						    visible[i] = true;
						else
							visible[i] = false;
					}
				    line = config_reader.readLine();  // Transparency
				    config_tokenizer = new StringTokenizer(line);
				    number_of_tokens = config_tokenizer.countTokens();
				    token = config_tokenizer.nextToken();
				    number_of_tokens--;
				    for(int i = 0; i < number_of_tokens; i++)
				    {
				    	token = config_tokenizer.nextToken();
				    	if(token.equals("true"))
				    		transparent[i] = true;
				    	else
				    		transparent[i] = false;
				    }		    
				    line = config_reader.readLine(); 
				    while(line != null)
				    {
				    	// Skip line.
				        line             = config_reader.readLine();
				        if(line != null)
				        {
				            config_tokenizer = new StringTokenizer(line);
				            String key       = config_tokenizer.nextToken();
				            String value     = config_tokenizer.nextToken();
				            
				            //System.out.println("Key is " + key);
				            //System.out.println("Value is " + value);
				            if(key.equals("Offset"))
				    	        data_offset = Double.valueOf(value);
				            else if(key.equals("Range")) 
				        	    data_range = Double.valueOf(value);
				            else if(key.equals("ReverseView")) 
					        {
					        	if(value.equals("true"))
					        		reverse_view = true;
					        	else
					        		reverse_view = false;
					        }
				            else if(key.equals("RelativeMode")) 
					        {
					        	if(value.equals("true"))
					        		relative_mode = true;
					        	else
					        		relative_mode = false;
					        }
				            else if(key.equals("RasterOverlay")) 
					        {
					        	if(value.equals("true"))
					        		raster_overlay = true;
					        	else
					        		raster_overlay = false;
					        }
				            else if(key.equals("Smooth")) 
				        	    smooth = Integer.parseInt(value);
				            else if(key.equals("XStep"))
					    	    normal_xstep = Double.valueOf(value);
					        else if(key.equals("YStep")) 
					        	normal_ystep = Double.valueOf(value);
					        else if(key.equals("XLocation"))
					    	    xlocation = Double.valueOf(value);
					        else if(key.equals("YLocation")) 
					        	ylocation = Double.valueOf(value);
					        else if(key.equals("ScaleFactor")) 
					        	scale_factor = Double.valueOf(value);
				            else if(key.equals("Clipping")) 
					        {
					        	if(value.equals("true"))
					        		data_clipped = true;
					        	else
					        		data_clipped = false;
					        } 
				            else if(key.equals("Label")) 
					        {
				            	show_label = true;
				            	graph_label = value;
					        } 
				            else if(key.equals("ShowID")) 
					        {
					        	if(value.equals("true"))
					        		show_id = true;
					        	else
					        		show_id = false;
					        } 
				            else if(key.equals("ShowPosition")) 
					        {
					        	if(value.equals("true"))
					        		show_position = true;
					        	else
					        		show_position = false;
					        } 
				            else if(key.equals("Maximum")) 
					        	maximum_y = Double.valueOf(value);
				            else if(key.equals("Minimum")) 
					        	minimum_y = Double.valueOf(value);
				            else if(key.equals("AppendData")) 
					        {
					        	if(value.equals("true"))
					        		append_data = true;
					        	else
					        		append_data = false;
					        } 
				            else if(key.equals("AppendLine")) 
				            	append_line = Integer.parseInt(value);	
				            else if(key.equals("AppendSensor")) 
				            	append_sensor = Integer.parseInt(value);
				            else if(key.equals("AppendIntensity")) 
					        	append_intensity = Double.valueOf(value);
				            else if(key.equals("AppendX")) 
					        	append_x = Double.valueOf(value);
				            else if(key.equals("AppendY")) 
					        	append_y = Double.valueOf(value);
				            else if(key.equals("AppendXAbs")) 
					        	append_x_abs = Double.valueOf(value);
				            else if(key.equals("AppendYAbs")) 
					        	append_y_abs = Double.valueOf(value);
				            else if(key.equals("AppendXPosition")) 
				            	append_x_position = Integer.parseInt(value);
				            else if(key.equals("AppendYPosition")) 
				            	append_y_position = Integer.parseInt(value);
				            else if(key.equals("AppendIndex")) 
				            	append_index = Integer.parseInt(value);
				            else if(key.equals("StartSet")) 
					        {
					        	if(value.equals("true"))
					        		startpoint_set = true;
					        	else
					        		startpoint_set = false;
					        } 
				            else if(key.equals("StartLine")) 
				            	startpoint_line = Integer.parseInt(value);	
				            else if(key.equals("StartSensor")) 
				            	startpoint_sensor = Integer.parseInt(value);
				            else if(key.equals("StartX")) 
				            	startpoint_x = Double.valueOf(value);
				            else if(key.equals("StartY")) 
				            	startpoint_y = Double.valueOf(value);
				            else if(key.equals("StartIntensity")) 
				            	startpoint_intensity = Double.valueOf(value);
				            else if(key.equals("StartXPosition")) 
				            	startpoint_x_position = Integer.parseInt(value);
				            else if(key.equals("StartYPosition")) 
				            	startpoint_y_position = Integer.parseInt(value);
				            else if(key.equals("StartIndex")) 
				            	startpoint_index = Integer.parseInt(value);
				            else if(key.equals("MidSet")) 
					        {
					        	if(value.equals("true"))
					        		midpoint_set = true;
					        	else
					        		midpoint_set = false;
					        } 
				            else if(key.equals("MidLine")) 
				            	midpoint_line = Integer.parseInt(value);	
				            else if(key.equals("MidSensor")) 
				            	midpoint_sensor = Integer.parseInt(value);
				            else if(key.equals("MidX")) 
				            	midpoint_x = Double.valueOf(value);
				            else if(key.equals("MidY")) 
				            	midpoint_y = Double.valueOf(value);
				            else if(key.equals("MidIntensity")) 
				            	midpoint_intensity = Double.valueOf(value);
				            else if(key.equals("MidXPosition")) 
				            	midpoint_x_position = Integer.parseInt(value);
				            else if(key.equals("MidYPosition")) 
				            	midpoint_y_position = Integer.parseInt(value);
				            else if(key.equals("MidIndex")) 
				            	midpoint_index = Integer.parseInt(value);
				            else if(key.equals("EndSet")) 
					        {
					        	if(value.equals("true"))
					        		endpoint_set = true;
					        	else
					        		endpoint_set = false;
					        } 
				            else if(key.equals("EndLine")) 
				            	endpoint_line = Integer.parseInt(value);	
				            else if(key.equals("EndSensor")) 
				            	endpoint_sensor = Integer.parseInt(value);
				            else if(key.equals("EndX")) 
				            	endpoint_x = Double.valueOf(value);
				            else if(key.equals("EndY")) 
				            	endpoint_y = Double.valueOf(value);
				            else if(key.equals("EndIntensity")) 
				            	endpoint_intensity = Double.valueOf(value);
				            else if(key.equals("EndXPosition")) 
				            	endpoint_x_position = Integer.parseInt(value);
				            else if(key.equals("EndYPosition")) 
				            	endpoint_y_position = Integer.parseInt(value);
				            else if(key.equals("EndIndex")) 
				            	endpoint_index = Integer.parseInt(value); 
				            if(data_offset + data_range >= 1)
				            {
				            	data_offset -= data_range;
				            }
				        }
				    }
				    //System.out.println("Finished reading config file.");
					config_reader.close();  
				}
				catch(Exception e)
				{
					System.out.println("Exception trying to read config file.");
					System.out.println(e.toString());
					System.out.println("Setting defaults.");
					data_offset    = .0;
					data_range     = .004;
					
				    raster_overlay = false;
				    persistent_data = false;
					relative_mode  = true;
				    reverse_view   = false;
				    data_clipped   = false;
				    color_key      = false;
				    show_id        = true;
				    show_label     = false;
				    show_data      = false;
				    scale_factor   = 1.;
				    normal_xstep   = 0.5;
					normal_ystep   = 0.5;
					xlocation      = 0.5;
					ylocation      = 0.5;	
				    smooth         = 0;
				    append_data    = false;
				    startpoint_set = false;
				    midpoint_set   = false;
				    endpoint_set   = false;
					
					// Only important if clipping is true,
				    // then they need to be set to actual values.
					minimum_y      = 0;
					maximum_y      = 0;
					
					// Only important if show_label is true,
					// although there might be some code that
					// checks for an empty string.
					graph_label = new String("");
					config_file_exists = false;
				}
			}
			else
				System.out.println("Loading defaults.");
			
			ArrayList original_data = new ArrayList();
			global_xmin          = Double.MAX_VALUE;
			global_xmax          = 0;
			global_ymin          = Double.MAX_VALUE;
			global_ymax          = 0;
			global_intensity_min = Double.MAX_VALUE;
			global_intensity_max = -Double.MAX_VALUE;
			
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				String line = reader.readLine();
				StringTokenizer number_tokens = new StringTokenizer(line, " ");
				int length = number_tokens.countTokens();
				int number_of_entries = length / 3;
				for (int i = 0; i < number_of_entries; i++)
				{
					double x = Double.valueOf(number_tokens.nextToken());
					double y = Double.valueOf(number_tokens.nextToken());
					double intensity = Double.valueOf(number_tokens.nextToken());
					if (x < global_xmin)
						global_xmin = x;
					else if (x > global_xmax)
						global_xmax = x;
					if (y < global_ymin)
						global_ymin = y;
					else if (y > global_ymax)
						global_ymax = y;
					if (intensity < global_intensity_min)
						global_intensity_min = intensity;
					else if (intensity > global_intensity_max)
						global_intensity_max = intensity;
					Sample current_sample = new Sample(x, y, intensity);
					original_data.add(current_sample);
				}
				
				while(line != null)
				{
					try
					{
						line = reader.readLine();
						if (line != null)
						{
							number_tokens = new StringTokenizer(line, " ");
							for (int i = 0; i < number_of_entries; i++)
							{
								double x = Double.valueOf(number_tokens.nextToken());
								double y = Double.valueOf(number_tokens.nextToken());
								double intensity = Double.valueOf(number_tokens.nextToken());
								if (x < global_xmin)
									global_xmin = x;
								else if (x > global_xmax)
									global_xmax = x;
								if (y < global_ymin)
									global_ymin = y;
								else if (y > global_ymax)
									global_ymax = y;
								if (intensity < global_intensity_min)
									global_intensity_min = intensity;
								else if (intensity > global_intensity_max)
									global_intensity_max = intensity;
								Sample current_sample = new Sample(x, y, intensity);
								original_data.add(current_sample);
							}
						}
					} 
					catch (IOException e)
					{
						System.out.println("Exception trying to read data file.");
						System.out.println("Unexpected error " + e.toString());
					}
				}
				reader.close();
				
				//System.out.println("Data set contains " + original_data.size() + " samples.");
				for(int i = 0; i < original_data.size(); i++)
				{
					Sample sample = (Sample) original_data.get(i);
					Sample new_sample = new Sample();
					new_sample.x = sample.x - global_xmin;
					new_sample.y = sample.y - global_ymin;
					new_sample.intensity = sample.intensity;
					new_sample.index     = i;
					relative_data.add(new_sample);
				}
				
				Sample previous_sample = (Sample)relative_data.get(2);
				double total_distance  = 0;
				for(int i = 7; i < relative_data.size(); i += 5)
				{
					Sample sample   = (Sample) relative_data.get(i);
					double distance = getDistance(sample.x, sample.y, previous_sample.x, previous_sample.y);
					total_distance += distance;
					previous_sample = sample;
				}
				
				data_length          = (int)total_distance;
				scrollbar_resolution = data_length;
				
				Sample init_sample = (Sample)relative_data.get(2);
				double init_x      = init_sample.x;
				double init_y      = init_sample.y;
				
				double [][] object_location = ObjectMapper.getObjectLocationArray();
				int number_of_objects       = object_location.length;
				for(int i = 0; i < number_of_objects; i++)
				{
					object_location[i][0] -= global_xmin;
					object_location[i][1] -= global_ymin;
				}
				
				for(int i = 0; i < number_of_objects; i++)
				{
					double distance = getDistance(init_x, init_y, object_location[i][0], object_location[i][1]);
					int    index    = 0;
					for(int j = 7; j < relative_data.size(); j += 5)
					{
						Sample sample = (Sample)relative_data.get(j);
						double current_x = sample.x;
						double current_y = sample.y;
						double current_distance = getDistance(current_x, current_y, object_location[i][0], object_location[i][1]);
						if(current_distance < distance)
						{
							distance = current_distance;
							index = j;
						}	
					}
					// Reset index from center to initial sensor.
					//index -= 2;
					object_index.add(index);
				}
				
				data           = new ArrayList();
				total_distance = 0;
				
				for(int i = 0; i < 5; i++)
				{
					Sample sample          = (Sample) relative_data.get(i);
				    Sample adjusted_sample = new Sample(sample.x, total_distance, sample.intensity);
				    adjusted_sample.index = sample.index;
				    data.add(adjusted_sample);
				}
				
				for(int i = 7; i < relative_data.size(); i += 5)
				{
					Sample current_sample   = (Sample) relative_data.get(i);
					previous_sample         = (Sample) relative_data.get(i - 5);
					double axis             = getDistance(current_sample.x, current_sample.y, previous_sample.x, previous_sample.y);
					total_distance          += axis;
					
					current_sample          = (Sample) relative_data.get(i - 2);
					Sample adjusted_sample  = new Sample(current_sample.x, total_distance, current_sample.intensity);
					adjusted_sample.index   = current_sample.index;
					data.add(adjusted_sample);
					
					
					current_sample      = (Sample) relative_data.get(i - 1);
					adjusted_sample  = new Sample(current_sample.x, total_distance, current_sample.intensity);
					adjusted_sample.index   = current_sample.index;
					data.add(adjusted_sample);
					
					current_sample      = (Sample) relative_data.get(i);
					adjusted_sample  = new Sample(current_sample.x, total_distance, current_sample.intensity);
					adjusted_sample.index   = current_sample.index;
					data.add(adjusted_sample);
					
					current_sample      = (Sample) relative_data.get(i + 1);
					adjusted_sample  = new Sample(current_sample.x, total_distance, current_sample.intensity);
					adjusted_sample.index   = current_sample.index;
					data.add(adjusted_sample);
					
					current_sample      = (Sample) relative_data.get(i + 2);
					adjusted_sample  = new Sample(current_sample.x, total_distance, current_sample.intensity);
					adjusted_sample.index   = current_sample.index;
					data.add(adjusted_sample);
				}
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		} 
		else
		{
			System.out.println("File not found.");
			System.exit(0);
		}
		
		// Segment the data set by sensor so we don't have
		// to do it over and over again in the rest of the program.
		for(int i = 0; i < 5; i++)
		{
			ArrayList data_list = new ArrayList();
			set_array.add(data_list);
			ArrayList relative_data_list = new ArrayList();
			relative_set_array.add(relative_data_list);
		}
		
		for(int i = 0; i < 5; i++)
		{
			ArrayList data_list = (ArrayList)set_array.get(i);
			ArrayList relative_data_list = (ArrayList)relative_set_array.get(i);
			for(int j = i; j < data.size(); j += 5)
			{
				Sample sample = (Sample)data.get(j);
				data_list.add(sample);
				Sample relative_sample = (Sample)relative_data.get(j);
				relative_data_list.add(relative_sample);
			}
		}
		
		// Get an idex of endpoints by checking when the order of the x-coordinates
		// changes.
		int    first_index  = 0;
		int    last_index   = 4;
		Sample first_sample = (Sample)relative_data.get(first_index);
		Sample last_sample  = (Sample)relative_data.get(last_index);
		
		boolean init_direction_north = true;
		boolean headed_north         = true;
		if(first_sample.x < last_sample.x)
		{
			//System.out.println("Flight path is not headed north.");
			headed_north        = false;
			init_direction_north = false;
		}
		else
		{
			//System.out.println("Flight path is headed north.");
		}
		
		ArrayList end_point_index = new ArrayList();
		
		while(last_index < relative_data.size() - 5)
		{
		    first_index += 5;
		    last_index  += 5;
		    first_sample = (Sample)relative_data.get(first_index);
		    last_sample  = (Sample)relative_data.get(last_index);
		    if(headed_north)
		    {
		    	if(first_sample.x < last_sample.x)
		    	{
		    		end_point_index.add(first_index);
		    	    headed_north = false;
		    	}
		    }
		    else
		    {
		    	if(first_sample.x > last_sample.x)
		    	{
		    	    end_point_index.add(first_index);
		    	    headed_north = true;
		    	}	
		    }
		}
		
        int number_of_lines = end_point_index.size() + 1;
		line_index = new int[number_of_lines][2];
		line_index[0][0] = 0;
		for(int i = 0; i < number_of_lines - 1; i++)
		{
			int index = (int)end_point_index.get(i);
			line_index[i][1] = index;
			line_index[i + 1][0] = index;
		}
		line_index[number_of_lines - 1][1] = data.size() - 1;
	    
		// End file input.
		
		// Start gui.
		
		// Start out with the maximum area we intend to stake out on the display at startup.
		// This can be arbitrarily increased afterwards, to any aspect ratio.
		int data_canvas_xdim = 1000;
		int data_canvas_ydim = 800;
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screen_ydim = (int)screenSize.getHeight();
		int screen_xdim = (int)screenSize.getWidth();
		
		// Mostly we're just concerned with the y dimension.
		// Our default x dimension works on both laptop and desktops, y is too much.
		if(screen_ydim - 125 < data_canvas_ydim)
		{
			data_canvas_ydim = screen_ydim - 125;
			
			// Keep the aspect ratio, although it's not necessary.
			double value = data_canvas_ydim;
			value *= 1.25;
			data_canvas_xdim = (int) value;
		}
		
				
		frame = new JFrame("Wand Plotter");
		
		WindowAdapter window_handler = new WindowAdapter()
	    {
	        public void windowClosing(WindowEvent event)
	        {
	        	{
		        	try
		            {
		            	PrintWriter output  = new PrintWriter("wand.cfg");	
		            	
		            	String _id          = new String("");
		            	String _visible     = new String("");
		            	String _transparent = new String("");
		            	String _show_id     = new String("");
		            	for(int i = 0; i < 5; i++)
		            	{
		            	    _id = new String(_id + i + "\t\t");
		            	    if(visible[i])
		            	    	_visible = new String(_visible + "true\t");
		            	    else
		            	    	_visible = new String(_visible + "false\t");
		            	    if(transparent[i])
		            	    	_transparent = new String(_transparent + "true\t");
		            	    else
		            	    	_transparent = new String(_transparent + "false\t");	
		            	}
		            	output.write("SensorID\t" + _id + "\n");
		            	output.write("Visible\t\t" + _visible + "\n");
		            	output.write("Transparent\t" + _transparent + "\n\n");
		            	output.write("Offset\t\t\t" + String.format("%,.4f", data_offset) + "\n");
		            	output.write("Range\t\t\t" + String.format("%,.4f", data_range) + "\n");
		            	output.write("XLocation\t\t" + String.format("%,.4f", xlocation) + "\n");
		            	output.write("YLocation\t\t" + String.format("%,.4f", ylocation) + "\n");
		            	output.write("XStep\t\t\t" + String.format("%,.2f", normal_xstep) + "\n");
		            	output.write("YStep\t\t\t" + String.format("%,.2f", normal_ystep) + "\n");
		            	if(show_label)
		            		output.write("Label\t\t\t " + graph_label + "\n");
		            	if(show_id)
		            		output.write("ShowID\t\t\ttrue\n");
		            	else
		            		output.write("ShowID\t\t\tfalse\n");
		            	if(show_position)
		            		output.write("ShowPosition\ttrue\n");
		            	else
		            		output.write("ShowPosition\tfalse\n");
		            	if(reverse_view)
		            		output.write("ReverseView\t\ttrue\n");
		            	else
		            		output.write("ReverseView\t\tfalse\n");
		            	if(relative_mode)
		            		output.write("RelativeMode\ttrue\n");
		            	else
		            		output.write("RelativeMode\tfalse\n");
		            	if(raster_overlay)
		            		output.write("RasterOverlay\ttrue\n");
		            	else
		            		output.write("RasterOverlay\tfalse\n");
		            	output.write("ScaleFactor\t\t" + String.format("%,.2f", scale_factor) + "\n");
		            	if(data_clipped)
		            		output.write("Clipping\t\ttrue\n");
		            	else
		            		output.write("Clipping\t\tfalse\n");
		            	output.write("Maximum\t\t\t" + String.format("%,.2f", maximum_y) + "\n");
		            	output.write("Minimum\t\t\t" + String.format("%,.2f", minimum_y) + "\n");
		            	output.write("Smooth\t\t\t" + smooth + "\n");
		            	if(append_data)
		            		output.write("AppendData\t\ttrue\n");
		            	else
		            		output.write("AppendData\t\tfalse\n");
		            	output.write("AppendLine\t\t" + append_line + "\n");
		            	output.write("AppendSensor\t" + append_sensor + "\n");
		            	output.write("AppendX\t\t\t" + String.format("%,.2f", append_x) + "\n");
		            	String decimal_string = String.format("%,.2f", append_y);
		            	output.write("AppendY\t\t\t" + decimal_string.replaceAll(",", "") + "\n");
		            	output.write("AppendIntensity\t" + String.format("%,.2f", append_intensity) + "\n");
		            	
		            	decimal_string = String.format("%,.2f", append_x_abs);
		            	output.write("AppendXAbs\t\t" + decimal_string.replaceAll(",", "") + "\n");
		            	
		            	decimal_string = String.format("%,.2f", append_y_abs);
		            	output.write("AppendYAbs\t\t" + decimal_string.replaceAll(",", "") + "\n");
		            	
		            	
		            	output.write("AppendXPosition\t" + append_x_position + "\n");
		            	output.write("AppendYPosition\t" + append_y_position + "\n");
		            	output.write("AppendIndex\t\t" + append_index + "\n");
		            	
		            	if(startpoint_set)
		            		output.write("StartSet\t\ttrue\n");
		            	else
		            		output.write("StartSet\t\tfalse\n");
		            	output.write("StartLine\t\t" + startpoint_line + "\n");
		            	output.write("StartSensor\t\t" + startpoint_sensor + "\n");
		            	output.write("StartX\t\t\t" + String.format("%,.2f", startpoint_x) + "\n");
		            	output.write("StartY\t\t\t" + String.format("%,.2f", startpoint_y) + "\n");
		            	output.write("StartIntensity\t" + String.format("%,.2f", startpoint_intensity) + "\n");
		            	output.write("StartXPosition\t" + startpoint_x_position + "\n");
		            	output.write("StartYPosition\t" + startpoint_y_position + "\n");
		            	output.write("StartIndex\t\t" + startpoint_index + "\n");
		            	
		            	if(midpoint_set)
		            		output.write("MidSet\t\t\ttrue\n");
		            	else
		            		output.write("MidSet\t\t\tfalse\n");
		            	output.write("MidLine\t\t\t" + midpoint_line + "\n");
		            	output.write("MidSensor\t\t" + midpoint_sensor + "\n");
		            	output.write("MidX\t\t\t" + String.format("%,.2f", midpoint_x) + "\n");
		            	output.write("MidY\t\t\t" + String.format("%,.2f", midpoint_y) + "\n");
		            	output.write("MidIntensity\t" + String.format("%,.2f", midpoint_intensity) + "\n");
		            	output.write("MidXPosition\t" + midpoint_x_position + "\n");
		            	output.write("MidYPosition\t" + midpoint_y_position + "\n");
		            	output.write("MidIndex\t\t" + midpoint_index + "\n");
		            	
		            	if(endpoint_set)
		            		output.write("EndSet\t\t\ttrue\n");
		            	else
		            		output.write("EndSet\t\t\tfalse\n");
		            	output.write("EndLine\t\t\t" + endpoint_line + "\n");
		            	output.write("EndSensor\t\t" + endpoint_sensor + "\n");
		            	output.write("EndX\t\t\t" + String.format("%,.2f", endpoint_x) + "\n");
		            	output.write("EndY\t\t\t" + String.format("%,.2f", endpoint_y) + "\n");
		            	output.write("EndIntensity\t" + String.format("%,.2f", endpoint_intensity) + "\n");
		            	output.write("EndXPosition\t" + endpoint_x_position + "\n");
		            	output.write("EndYPosition\t" + endpoint_y_position + "\n");
		            	output.write("EndIndex\t\t" + endpoint_index + "\n");
		            	
		            	output.close();	
		            }
		        	catch(Exception e)
		        	{
		        		System.out.println("Exception writing config file.");
		        		System.out.println(e.toString());
		        	}
		            System.exit(0);
		        }    	
	        }
	    };
	    frame.addWindowListener(window_handler);
		Cursor cursor = new Cursor(Cursor.HAND_CURSOR);
		frame.setCursor(cursor);

		// A modeless dialog box that shows up if Settings->Dynamic Range is selected.
		lower_bound = new JTextField();
		upper_bound = new JTextField();
		lower_bound.setHorizontalAlignment(JTextField.CENTER);
		upper_bound.setHorizontalAlignment(JTextField.CENTER);
		
		String lower_string = String.format("%.2f", minimum_y);
		String upper_string = String.format("%.2f", maximum_y);
		
		lower_bound.setText(lower_string);
		upper_bound.setText(upper_string);
		
		JPanel bounds_panel = new JPanel(new GridLayout(2, 2));
		bounds_panel.add(lower_bound);
		bounds_panel.add(upper_bound);
		bounds_panel.add(new JLabel("Lower", JLabel.CENTER));
		bounds_panel.add(new JLabel("Upper", JLabel.CENTER));
		JPanel bounds_button_panel = new JPanel(new GridLayout(1, 2));
		JButton adjust_bounds_button = new JButton("Adjust");
		JButton reset_bounds_button = new JButton("Reset");
		bounds_button_panel.add(adjust_bounds_button);
		
		data_canvas = new PlotCanvas();
		data_canvas.setSize(data_canvas_xdim, data_canvas_ydim);
		MouseHandler mouse_handler = new MouseHandler();
		data_canvas.addMouseListener(mouse_handler);
		MouseMotionHandler mouse_motion_handler = new MouseMotionHandler();
		data_canvas.addMouseMotionListener(mouse_motion_handler);
		
		int thumb_size        = 3;
		int scrollbar_position = (int) (data_offset * scrollbar_resolution + data_range * scrollbar_resolution / 2);
		data_scrollbar        = new JScrollBar(JScrollBar.HORIZONTAL, scrollbar_position, thumb_size, 0, scrollbar_resolution + thumb_size);
		DataScrollbarHandler data_scrollbar_handler = new DataScrollbarHandler();
		data_scrollbar.addAdjustmentListener(data_scrollbar_handler);	
		
		JPanel data_panel = new JPanel(new BorderLayout());
		data_panel.add(data_canvas, BorderLayout.CENTER);
		data_panel.add(data_scrollbar, BorderLayout.SOUTH);
		
		for(int i = 0; i < 5; i++)
		{
			visible[i] = true;
			transparent[i] = false;
		}
		JMenuBar menu_bar = new JMenuBar();
		
		//System.out.println("Finished constructing gui.");
		
		// End gui.
		
		// Start file menu.
		
		JMenu     file_menu      = new JMenu("File");
		
		JMenuItem    save_graph_item    = new JMenuItem("Save Graph");
		ActionListener save_graph_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
		        FileDialog file_dialog = new FileDialog(frame, "Save Graph", FileDialog.SAVE);
		        file_dialog.setVisible(true);
		        String filename = file_dialog.getFile();
		        int           image_xdim   = buffered_image.getWidth();
				int           image_ydim   = buffered_image.getHeight();
				BufferedImage print_image  = new BufferedImage(image_xdim, image_ydim, BufferedImage.TYPE_INT_RGB);
				Graphics2D    print_buffer = (Graphics2D) print_image.getGraphics(); 
				print_buffer.drawImage(buffered_image, 0, 0,  null);	
				String current_directory = file_dialog.getDirectory();
				try 
    	        {  
    	            ImageIO.write(print_image, "png", new File(current_directory + filename + ".png")); 
    	        } 
    	        catch(IOException e2) 
    	        {  
    	            //e2.printStackTrace(); 
    	            e2.toString();
    	        }  
			}
		};
		save_graph_item.addActionListener(save_graph_handler);
		file_menu.add(save_graph_item);
		
		JMenuItem    save_segment_item    = new JMenuItem("Save Segment");
		ActionListener save_segment_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// Line indices to help us figure out where an index falls.
				//int[][] line = ObjectMapper.getUnclippedLineArray();
		        
				
				FileDialog file_dialog = new FileDialog(frame, "Save Segment", FileDialog.SAVE);
		        file_dialog.setVisible(true);
		        String filename = file_dialog.getFile();
		    
				String current_directory = file_dialog.getDirectory();
				
				// General purpose text file to use with gnuplot and as a source of segmented data.
				
				System.out.println("Writing text file.");
				
				
				try (PrintWriter output = new PrintWriter(current_directory + filename + ".txt"))
			    {
				    // Separate the sensor information into separate blocks.
				    // This is a format that makes gnuplot easier to use,
				    // especially making fence plots.
				    int number_of_sensors = 5;
				    for(int i = 0; i < number_of_sensors; i++)
				    {
				    	ArrayList relative_data_list = (ArrayList)relative_data_array.get(i);
				    	output.println("#Sensor " + i + ", Line " + start_flight_line);
				        for(int j = 0; j < relative_data_list.size(); j++)
				        {
						        Sample sample    = (Sample)relative_data_list.get(j);
						        String xlocation = String.format("%.2f", sample.x);
						        String ylocation = String.format("%.2f", sample.y);
						        String intensity = String.format("%.2f", sample.intensity);
						        output.println(xlocation + " " + ylocation + " " + intensity);
						}
				        output.println("#Sensor " + i + ", Line " + stop_flight_line);
					    output.println();
					    output.println();
				    } 
				    output.close();
				} 
			    catch (Exception ex)
			    {
				    System.out.println(ex.toString());
			    }
			}
		};
		save_segment_item.addActionListener(save_segment_handler);
		file_menu.add(save_segment_item);
		
		// A modeless dialog box that shows up if File->Load Config is selected.
		JPanel load_config_panel = new JPanel(new GridLayout(2, 1));
		load_config_input        = new JTextField(30);
		load_config_input.setHorizontalAlignment(JTextField.CENTER);
		load_config_input.setText("");
		load_config_panel.add(load_config_input);
		JButton load_config_button = new JButton("Load Config");
		
		// This reads in non-default config files, where the default
		// is the configuration saved the last time the program
		// exited.
		ActionListener load_config_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String filename = load_config_input.getText();
				String suffix = new String(".cfg");
				System.out.println("Loading configuration from " + filename + suffix);
				File config_file = new File(filename + suffix);
				if(config_file.exists())
				{
					try
					{
						BufferedReader config_reader   = new BufferedReader(new InputStreamReader(new FileInputStream(config_file)));
						String line;
						StringTokenizer config_tokenizer;
						line = config_reader.readLine();  // Sensor id's--we already know they're 0-4.		    
						line = config_reader.readLine();  // Visibiltiy
						config_tokenizer = new StringTokenizer(line);
						int number_of_tokens = config_tokenizer.countTokens();
						String token = config_tokenizer.nextToken();
						number_of_tokens--;
						for(int i = 0; i < number_of_tokens; i++)
						{
						    token = config_tokenizer.nextToken();
							if(token.equals("true"))
							    visible[i] = true;
							else
								visible[i] = false;
						}
					    line = config_reader.readLine();  // Transparency
					    config_tokenizer = new StringTokenizer(line);
					    number_of_tokens = config_tokenizer.countTokens();
					    token = config_tokenizer.nextToken();
					    number_of_tokens--;
					    for(int i = 0; i < number_of_tokens; i++)
					    {
					        token = config_tokenizer.nextToken();
							if(token.equals("true"))
							     transparent[i] = true;
							else
							     transparent[i] = false;
						}	
					    
					    // A graph label in the config file is optional,
					    // so we set the defaults first. 
					   show_label = false;
					   graph_label = new String("");
					  
					   line = config_reader.readLine(); 
					   while(line != null)
					   {
					        line = config_reader.readLine();
					        if(line != null)
					        {
					            config_tokenizer = new StringTokenizer(line);
					            String key       = config_tokenizer.nextToken();
					            String value     = config_tokenizer.nextToken();
					            
					            // We have to adjust the scrollbar and slider
					            // when these values change.
					            // Will do that at the end when we have both
					            // values.
					            if(key.equals("Offset"))
					    	        data_offset = Double.valueOf(value);
					            else if(key.equals("Range")) 
					        	    data_range = Double.valueOf(value);
					            else if(key.equals("Label")) 
						        {
					            	show_label = true;
					            	graph_label = value;
						        } 
					            else if(key.equals("ShowID")) 
						        {
						        	if(value.equals("true"))
						        		show_id = true;
						        	else
						        		show_id = false;
						        }
					            else if(key.equals("ShowPosition")) 
						        {
						        	if(value.equals("true"))
						        		show_position = true;
						        	else
						        		show_position = false;
						        }
					            else if(key.equals("ReverseView")) 
						        {
						        	if(value.equals("true"))
						        	{
						        		// Instead of setting values directly,
						        		// let gui components do it and 
						        		//  keep gui state accurate.
						        		if(!reverse_view)
						        		{
						        		    //reverse_view = true;
						        			view_item.doClick();
						        		}
						        	}
						        	else
						        	{
						        		if(reverse_view) 
						        		{
						        			//reverse_view = false;	
						        			view_item.doClick();
						        		}
						        	}
						        }
					            else if(key.equals("RelativeMode")) 
						        {
						        	if(value.equals("true"))
						        	{
						        		if(!relative_mode)
						        			mode_item.doClick();
						        	}
						        	else
						        	{
						        		if(relative_mode)
						        			mode_item.doClick();	
						        	}
						        }
					            else if(key.equals("RasterOverlay")) 
						        {
						        	if(value.equals("true"))
						        	{
						        		if(!raster_overlay)
						        			overlay_item.doClick();
						        	}
						        	else
						        	{
						        		if(raster_overlay)
						        			overlay_item.doClick();
						        	}
						        }
					            else if(key.equals("ColorKey")) 
						        {
						        	if(value.equals("true"))
						        	{
						        		if(!color_key)
						        			color_key_item.doClick();
						        	}
						        	else
						        	{
						        		if(color_key)
						        			color_key_item.doClick();
						        	}
						        } 
					            else if(key.equals("Smooth")) 
					            {
					        	    smooth = Integer.parseInt(value);
					        	    smooth_slider.setValue(smooth);
					            }
						        else if(key.equals("ScaleFactor")) 
						        {
						        	scale_factor = Double.valueOf(value);
						        }
					            // We need to reset the bounds text in the dynamic range dialog
					            // if there is clipping.  Process at the end when we have
					            // current max/min values.
						        else if(key.equals("Clipping")) 
						        {
						        	if(value.equals("true"))
						        		data_clipped = true;
						        	else
						        		data_clipped = false;
						        } 
					            else if(key.equals("Maximum")) 
						        	maximum_y = Double.valueOf(value);
					            else if(key.equals("Minimum")) 
						        	minimum_y = Double.valueOf(value);
					            else if(key.equals("XStep"))
						    	    normal_xstep = Double.valueOf(value);
						        else if(key.equals("YStep")) 
						        	normal_ystep = Double.valueOf(value);
						        else if(key.equals("XLocation"))
						    	    xlocation = Double.valueOf(value);
						        else if(key.equals("YLocation")) 
						        	ylocation = Double.valueOf(value);
					            else if(key.equals("AppendData")) 
						        {
						        	if(value.equals("true"))
						        		append_data = true;
						        	else
						        		append_data = false;
						        } 
					            else if(key.equals("AppendLine")) 
					            	append_line = Integer.parseInt(value);	
					            else if(key.equals("AppendSensor")) 
					            	append_sensor = Integer.parseInt(value);
					            else if(key.equals("AppendX")) 
						        	append_x = Double.valueOf(value);
					            else if(key.equals("AppendY")) 
						        	append_y = Double.valueOf(value);
					            else if(key.equals("AppendIntensity")) 
						        	append_intensity = Double.valueOf(value);
					            else if(key.equals("AppendXAbs")) 
						        	append_x_abs = Double.valueOf(value);
					            else if(key.equals("AppendYAbs")) 
						        	append_y_abs = Double.valueOf(value);
					            else if(key.equals("AppendXPosition")) 
					            	append_x_position = Integer.parseInt(value);
					            else if(key.equals("AppendYPosition")) 
					            	append_y_position = Integer.parseInt(value);
					            else if(key.equals("AppendIndex")) 
					            	append_index = Integer.parseInt(value);
					            else if(key.equals("StartSet")) 
						        {
						        	if(value.equals("true"))
						        		startpoint_set = true;
						        	else
						        		startpoint_set = false;
						        } 
					            else if(key.equals("StartLine")) 
					            	startpoint_line = Integer.parseInt(value);	
					            else if(key.equals("StartSensor")) 
					            	startpoint_sensor = Integer.parseInt(value);
					            else if(key.equals("StartX")) 
					            	startpoint_x = Double.valueOf(value);
					            else if(key.equals("StartY")) 
					            	startpoint_y = Double.valueOf(value);
					            else if(key.equals("StartIntensity")) 
					            	startpoint_intensity = Double.valueOf(value);
					            else if(key.equals("StartXPosition")) 
					            	startpoint_x_position = Integer.parseInt(value);
					            else if(key.equals("StartYPosition")) 
					            	startpoint_y_position = Integer.parseInt(value);
					            else if(key.equals("StartIndex")) 
					            	startpoint_index = Integer.parseInt(value);
					            else if(key.equals("MidSet")) 
						        {
						        	if(value.equals("true"))
						        		midpoint_set = true;
						        	else
						        		midpoint_set = false;
						        } 
					            else if(key.equals("MidLine")) 
					            	midpoint_line = Integer.parseInt(value);	
					            else if(key.equals("MidSensor")) 
					            	midpoint_sensor = Integer.parseInt(value);
					            else if(key.equals("MidX")) 
					            	midpoint_x = Double.valueOf(value);
					            else if(key.equals("MidY")) 
					            	midpoint_y = Double.valueOf(value);
					            else if(key.equals("MidIntensity")) 
					            	midpoint_intensity = Double.valueOf(value);
					            else if(key.equals("MidXPosition")) 
					            	midpoint_x_position = Integer.parseInt(value);
					            else if(key.equals("MidYPosition")) 
					            	midpoint_y_position = Integer.parseInt(value);
					            else if(key.equals("MidIndex")) 
					            	midpoint_index = Integer.parseInt(value);
					            else if(key.equals("EndSet")) 
						        {
						        	if(value.equals("true"))
						        		endpoint_set = true;
						        	else
						        		endpoint_set = false;
						        } 
					            else if(key.equals("EndLine")) 
					            	endpoint_line = Integer.parseInt(value);	
					            else if(key.equals("EndSensor")) 
					            	endpoint_sensor = Integer.parseInt(value);
					            else if(key.equals("EndX")) 
					            	endpoint_x = Double.valueOf(value);
					            else if(key.equals("EndY")) 
					            	endpoint_y = Double.valueOf(value);
					            else if(key.equals("EndIntensity")) 
					            	endpoint_intensity = Double.valueOf(value);
					            else if(key.equals("EndXPosition")) 
					            	endpoint_x_position = Integer.parseInt(value);
					            else if(key.equals("EndYPosition")) 
					            	endpoint_y_position = Integer.parseInt(value);
					            else if(key.equals("EndIndex")) 
					            	endpoint_index = Integer.parseInt(value);
					        }
					    }
					    config_reader.close();
					    
					    
					    // We now have current settings for all parameters with dependencies and can make the gui consistent.
					    // We already reset booleans without dependencies.
					    
					    
					    // Reset placement scrollbars.
					    int value = (int)(100. * normal_xstep);
					    xstep_scrollbar.setValue(value);
					    value = (int)(100. * (1 - normal_ystep));
						ystep_scrollbar.setValue(value);
					    
					    // Reset scaling slider to current settings.  
						value = (int)((scale_factor - 1.) * 100.);	
				        factor_slider.setValue(value);
				       
					    
				        // So dynamic range comes up right.
					    if(data_clipped)
					    {
					    	lower_bound.setText(String.format("%.2f", minimum_y));	
		                	upper_bound.setText(String.format("%.2f", maximum_y));	
					    }
					    
					    int scrollbar_position = (int) (data_offset * scrollbar_resolution + data_range * scrollbar_resolution / 2);
						data_scrollbar.setValue(scrollbar_position);
					
						// Data canvas paint() function does data segmentation.
					    data_canvas.repaint();
					     
						//System.out.println("Finished resetting parameters.");
						
					}
					catch(Exception e2)
					{
						// Could try loading standard config file instead of
						// continuing with possible corrupted parameters.
					    System.out.println("Exception reading config file.");
					}
				}
			}
		};
		load_config_button.addActionListener(load_config_handler);
		load_config_panel.add(load_config_button);
		load_config_dialog = new JDialog(frame, "Load Config");
		load_config_dialog.add(load_config_panel, BorderLayout.CENTER);		
		
		JMenuItem load_config_item  = new JMenuItem("Load Config");
		ActionListener load_config_dialog_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();

				x += 830;

				load_config_dialog.setLocation(x, y);
				load_config_dialog.pack();
				load_config_dialog.setVisible(true);
			}
		};
		load_config_item.addActionListener(load_config_dialog_handler);
		file_menu.add(load_config_item);
		
		
		
		// A modeless dialog box that shows up if File->Save Config is selected.
		JPanel save_config_panel = new JPanel(new GridLayout(2, 1));
		save_config_input        = new JTextField(30);
		save_config_input.setHorizontalAlignment(JTextField.CENTER);
		save_config_input.setText("");
		save_config_panel.add(save_config_input);
		JButton save_config_button = new JButton("Save Config");
		ActionListener save_config_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String suffix = new String(".cfg");
				String filename = save_config_input.getText();
				System.out.println("Saving configuration to " + filename + suffix);
				
				try
	            {
	            	PrintWriter output  = new PrintWriter(filename + suffix);	
	            	String _id          = new String("");
	            	String _visible     = new String("");
	            	String _transparent = new String("");
	            	
	            	for(int i = 0; i < 5; i++)
	            	{
	            	    _id = new String(_id + i + "\t\t");
	            	    if(visible[i])
	            	    	_visible = new String(_visible + "true\t");
	            	    else
	            	    	_visible = new String(_visible + "false\t");
	            	    if(transparent[i])
	            	    	_transparent = new String(_transparent + "true\t");
	            	    else
	            	    	_transparent = new String(_transparent + "false\t");	
	            	}
	            	output.write("SensorID\t" + _id + "\n");
	            	output.write("Visible\t\t" + _visible + "\n");
	            	output.write("Transparent\t" + _transparent + "\n\n");
	            	output.write("Offset\t\t\t" + String.format("%,.4f", data_offset) + "\n");
	            	output.write("Offset\t\t\t" + String.format("%,.4f", data_offset) + "\n");
	            	output.write("Range\t\t\t" + String.format("%,.4f", data_range) + "\n");
	                output.write("XLocation\t\t" + String.format("%,.4f", xlocation) + "\n");
	            	output.write("YLocation\t\t" + String.format("%,.4f", ylocation) + "\n");
	            	output.write("XStep\t\t\t" + String.format("%,.2f", normal_xstep) + "\n");
	            	output.write("YStep\t\t\t" + String.format("%,.2f", normal_ystep) + "\n");
	                
	            	if(reverse_view)
	            		output.write("ReverseView\t\ttrue\n");
	            	else
	            		output.write("ReverseView\t\tfalse\n");
	            	if(relative_mode)
	            		output.write("RelativeMode\ttrue\n");
	            	else
	            		output.write("RelativeMode\tfalse\n");
	            	if(raster_overlay)
	            		output.write("RasterOverlay\ttrue\n");
	            	else
	            		output.write("RasterOverlay\tfalse\n");
	            	if(show_label)
	            		output.write("Label\t\t\t " + graph_label + "\n");
	            	if(show_id)
	            		output.write("ShowID\t\t\ttrue\n");
	            	else
	            		output.write("ShowID\t\t\tfalse\n");
	            	if(show_position)
	            		output.write("ShowPosition\ttrue\n");
	            	else
	            		output.write("ShowPosition\tfalse\n");
	            	if(color_key)
	            		output.write("ColorKey\t\ttrue\n");
	            	else
	            		output.write("ColorKey\t\tfalse\n");
	            	output.write("ScaleFactor\t\t" + String.format("%,.2f", scale_factor) + "\n");
	            	if(data_clipped)
	            		output.write("Clipping\t\ttrue\n");
	            	else
	            		output.write("Clipping\t\tfalse\n");
	            	output.write("Maximum\t\t\t" + String.format("%,.2f", maximum_y) + "\n");
	            	output.write("Minimum\t\t\t" + String.format("%,.2f", minimum_y) + "\n");
	            	output.write("Smooth\t\t\t" + smooth + "\n");
	            	if(append_data)
	            		output.write("AppendData\t\ttrue\n");
	            	else
	            		output.write("AppendData\t\tfalse\n");
	            	output.write("AppendLine\t\t" + append_line + "\n");
	            	output.write("AppendSensor\t" + append_sensor + "\n");
	            	output.write("AppendX\t\t\t" + String.format("%,.2f", append_x) + "\n");
	            	String decimal_string = String.format("%,.2f", append_y);
	            	output.write("AppendY\t\t\t" + decimal_string.replaceAll(",", "") + "\n");
	            	output.write("AppendIntensity\t" + String.format("%,.2f", append_intensity) + "\n");
	            	
	            	decimal_string = String.format("%,.2f", append_x_abs);
	            	output.write("AppendXAbs\t\t" + decimal_string.replaceAll(",", "") + "\n");
	            	
	            	decimal_string = String.format("%,.2f", append_y_abs);
	            	output.write("AppendYAbs\t\t" + decimal_string.replaceAll(",", "") + "\n");
	            	output.write("AppendXPosition\t" + append_x_position + "\n");
	            	output.write("AppendYPosition\t" + append_y_position + "\n");
	            	output.write("AppendIndex\t\t" + append_index + "\n");
	            	
	            	if(startpoint_set)
	            		output.write("StartSet\t\ttrue\n");
	            	else
	            		output.write("StartSet\t\tfalse\n");
	            	output.write("StartLine\t\t" + startpoint_line + "\n");
	            	output.write("StartSensor\t\t" + startpoint_sensor + "\n");
	            	output.write("StartX\t\t\t" + String.format("%,.2f", startpoint_x) + "\n");
	            	output.write("StartY\t\t\t" + String.format("%,.2f", startpoint_y) + "\n");
	            	output.write("StartIntensity\t" + String.format("%,.2f", startpoint_intensity) + "\n");
	            	output.write("StartXPosition\t" + startpoint_x_position + "\n");
	            	output.write("StartYPosition\t" + startpoint_y_position + "\n");
	            	output.write("StartIndex\t\t" + startpoint_index + "\n");
	            	
	            	if(midpoint_set)
	            		output.write("MidSet\t\t\ttrue\n");
	            	else
	            		output.write("MidSet\t\t\tfalse\n");
	            	output.write("MidLine\t\t\t" + midpoint_line + "\n");
	            	output.write("MidSensor\t\t" + midpoint_sensor + "\n");
	            	output.write("MidX\t\t\t" + String.format("%,.2f", midpoint_x) + "\n");
	            	output.write("MidY\t\t\t" + String.format("%,.2f", midpoint_y) + "\n");
	            	output.write("MidIntensity\t" + String.format("%,.2f", midpoint_intensity) + "\n");
	            	output.write("MidXPosition\t" + midpoint_x_position + "\n");
	            	output.write("MidYPosition\t" + midpoint_y_position + "\n");
	            	output.write("MidIndex\t\t" + midpoint_index + "\n");
	            	
	            	if(endpoint_set)
	            		output.write("EndSet\t\t\ttrue\n");
	            	else
	            		output.write("EndSet\t\t\tfalse\n");
	            	output.write("EndLine\t\t\t" + endpoint_line + "\n");
	            	output.write("EndSensor\t\t" + endpoint_sensor + "\n");
	            	output.write("EndX\t\t\t" + String.format("%,.2f", endpoint_x) + "\n");
	            	output.write("EndY\t\t\t" + String.format("%,.2f", endpoint_y) + "\n");
	            	output.write("EndIntensity\t" + String.format("%,.2f", endpoint_intensity) + "\n");
	            	output.write("EndXPosition\t" + endpoint_x_position + "\n");
	            	output.write("EndYPosition\t" + endpoint_y_position + "\n");
	            	output.write("EndIndex\t\t" + endpoint_index + "\n");
	            	
	            	output.close();	
	            }
	        	catch(Exception e2)
	        	{
	        		System.out.println(e2.toString());
	        	}
			}
		};
		save_config_button.addActionListener(save_config_handler);
		save_config_panel.add(save_config_button);
		save_config_dialog = new JDialog(frame, "Save Config");
		save_config_dialog.add(save_config_panel, BorderLayout.CENTER);		
		JMenuItem save_config_item  = new JMenuItem("Save Config");
		ActionListener save_config_dialog_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();

				x += 830;

				save_config_dialog.setLocation(x, y);
				save_config_dialog.pack();
				save_config_dialog.setVisible(true);
			}
		};
		save_config_item.addActionListener(save_config_dialog_handler);
		file_menu.add(save_config_item);
		menu_bar.add(file_menu);
		
		// End file menu.
		
		// Start format menu
		
		JMenu     format_menu  = new JMenu("Format");
		
		// A modeless dialog box that shows up if Format->Placement is selected.
		JPanel placement_panel = new JPanel(new BorderLayout());
		placement_canvas = new PlacementCanvas();
		placement_canvas.setSize(100, 100);
		xstep_scrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, 0, 101);
		AdjustmentListener xstep_handler = new AdjustmentListener()
		{
			public void adjustmentValueChanged(AdjustmentEvent event)
			{
				int xstep = event.getValue();
				normal_xstep = (double) xstep / 100;
				if (event.getValueIsAdjusting() == false)
				{
					data_canvas.repaint();
					placement_canvas.repaint();
				}
			}
		};
		xstep_scrollbar.addAdjustmentListener(xstep_handler);
		int value = (int)(100. * normal_xstep);
		xstep_scrollbar.setValue(value);
		ystep_scrollbar = new JScrollBar(JScrollBar.VERTICAL, 0, 1, 0, 101);
		AdjustmentListener ystep_handler = new AdjustmentListener()
		{
			public void adjustmentValueChanged(AdjustmentEvent event)
			{
				int ystep = 100 - event.getValue();
				normal_ystep = (double) ystep / 100;
				if (event.getValueIsAdjusting() == false)
				{
					data_canvas.repaint();
					placement_canvas.repaint();
				}
			}
		};
		ystep_scrollbar.addAdjustmentListener(ystep_handler);
		value = (int)(100. * (1 - normal_ystep));
		ystep_scrollbar.setValue(value);

		placement_panel.add(placement_canvas, BorderLayout.CENTER);
		placement_panel.add(xstep_scrollbar, BorderLayout.SOUTH);
		placement_panel.add(ystep_scrollbar, BorderLayout.EAST);

		placement_dialog = new JDialog(frame, "Placement");
		placement_dialog.add(placement_panel);
		
		JMenuItem place_item = new JMenuItem("Placement");
		ActionListener placement_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();

				Dimension canvas_dimension = data_canvas.getSize();
				double    canvas_xdim      = canvas_dimension.getWidth();
				double    canvas_ydim      = canvas_dimension.getHeight();
				
				y += canvas_ydim + 70;

				placement_dialog.setLocation(x, y);
				placement_dialog.pack();
				placement_dialog.setVisible(true);
			}
		};
		place_item.addActionListener(placement_handler);
		format_menu.add(place_item);
				
		JPanel sensor_panel = new JPanel(new GridLayout(1, 5));
		for (int i = 0; i < 5; i++)
		{
			sensor_canvas[i] = new SensorCanvas(i);
			sensor_canvas[i].setSize(20, 20);
			SensorCanvasMouseHandler sensor_canvas_mouse_handler = new SensorCanvasMouseHandler(i);
			sensor_canvas[i].addMouseListener(sensor_canvas_mouse_handler);
			sensor_panel.add(sensor_canvas[i]);
		}
		sensor_dialog = new JDialog(frame, "Sensors");
		sensor_dialog.add(sensor_panel);
		
		JMenuItem sensor_item = new JMenuItem("Sensors");
		ActionListener sensor_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();


				Dimension canvas_dimension = data_canvas.getSize();
				double    canvas_xdim      = canvas_dimension.getWidth();
				double    canvas_ydim      = canvas_dimension.getHeight();
				
				x += 120;
				
				y += canvas_ydim + 70;

				sensor_dialog.setLocation(x, y);
				sensor_dialog.pack();
				sensor_dialog.setVisible(true);
			}
		};
		sensor_item.addActionListener(sensor_handler);
		format_menu.add(sensor_item);
				
		JPanel     label_panel = new JPanel(new BorderLayout());
		JTextField label_input = new JTextField(30);
		label_input.setHorizontalAlignment(JTextField.CENTER);
		label_input.setText("");
		ActionListener label_input_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
			    graph_label = label_input.getText();
			    if(graph_label.equals(""))
			    	show_label = false;
			    else
			    	show_label = true;		
			    data_canvas.repaint();
			}
		};
        label_input.addActionListener(label_input_handler);
		label_panel.add(label_input);		
		label_dialog = new JDialog(frame, "Graph Label");
		label_dialog.add(label_panel);
		label_dialog.addWindowListener(new WindowAdapter() 
		{
		  public void windowClosing(WindowEvent e)
		  {
		    data_canvas.repaint();
		  }
		});
		
		JMenuItem label_item = new JMenuItem("Graph Label");
		ActionListener label_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();

				Dimension canvas_dimension = data_canvas.getSize();
				double    canvas_xdim      = canvas_dimension.getWidth();
				double    canvas_ydim      = canvas_dimension.getHeight();
				
				x += 240;
				
				y += canvas_ydim + 70;

				label_dialog.setLocation(x, y);
				label_dialog.pack();
				label_dialog.setVisible(true);
			}
		};
		label_item.addActionListener(label_handler);
		format_menu.add(label_item);
		
	
		menu_bar.add(format_menu);
		
		// End format menu.
		
		// Start adjustment_menu.
		
		JMenu     adjustment_menu  = new JMenu("Adjustments");
        
		// A modeless dialog box that shows up if Adjustments->Smoothing is selected.
		JPanel  smooth_panel  = new JPanel(new BorderLayout());
		smooth_slider = new JSlider(0, 100, smooth);
		ChangeListener smooth_slider_handler = new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				JSlider slider = (JSlider) e.getSource();
				if (slider.getValueIsAdjusting() == false)
				{
					int value = slider.getValue();
					smooth = value;
				    data_canvas.repaint();
				}
			}
		};
		smooth_slider.addChangeListener(smooth_slider_handler);
		smooth_panel.add(smooth_slider, BorderLayout.CENTER);
		smooth_dialog = new JDialog(frame, "Smoothing");
		smooth_dialog.add(smooth_panel);
		JMenuItem smoothing_item = new JMenuItem("Smoothing");
		ActionListener smooth_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();
				Dimension canvas_dimension = data_canvas.getSize();
				
				double    canvas_xdim      = canvas_dimension.getWidth();
				x += canvas_xdim;
				//y += 670;	
				y += 490;
				smooth_dialog.setLocation(x, y);
				smooth_dialog.pack();
				smooth_dialog.setVisible(true);
			}
		};
		smoothing_item.addActionListener(smooth_handler);
		adjustment_menu.add(smoothing_item);
		
		// A modeless dialog box that shows up if Adjustments->Scaling is selected.
		JPanel scale_panel = new JPanel(new BorderLayout());
	
		value = (int)((scale_factor - 1.) * 100.);	
		factor_slider = new JSlider(0, 200, value);
		ChangeListener factor_handler = new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				JSlider slider = (JSlider) e.getSource();
				if (slider.getValueIsAdjusting() == false)
				{
					int value = factor_slider.getValue();
					scale_factor = (double) value / 100 + 1.;
					data_canvas.repaint();
				}
			}
		};
		factor_slider.addChangeListener(factor_handler);
		scale_panel.add(factor_slider, BorderLayout.CENTER);
		scale_dialog = new JDialog(frame, "Scale Factor");
		scale_dialog.add(scale_panel);
		
		JMenuItem scaling_item = new JMenuItem("Scaling");
		ActionListener scale_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();
				Dimension canvas_dimension = data_canvas.getSize();
				double    canvas_xdim      = canvas_dimension.getWidth();
				x += canvas_xdim;
				//y += 720;	
				y += 540;	
				scale_dialog.setLocation(x, y);
				scale_dialog.pack();
				scale_dialog.setVisible(true);
			}
		};
		scaling_item.addActionListener(scale_handler);
		adjustment_menu.add(scaling_item);		
		
		ActionListener adjust_range_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String bound_string = lower_bound.getText();
				double min          = Double.valueOf(bound_string);
				bound_string        = upper_bound.getText();
				double max          = Double.valueOf(bound_string);
				
				minimum_y = min;
				maximum_y = max;
				data_clipped = true;
			
				dynamic_range_canvas.repaint();
				dynamic_button_changing = true;
				double current_range = max - min;
				//System.out.println("Current range is " + current_range);
				
				int min_value = 0;
				int max_value = 100;
				
				if(min > seg_min)
				{
					min -= seg_min;
					min *= 100;
					min /= current_range;
				    min_value = (int) min;
				    if(min_value < 0)
				    	min_value = - min_value;	
				}
				
				if(max < seg_max)
				{
					max -= seg_min;
					max *= 100;
					max /= current_range;
				    max_value = (int) max;
				    if(max_value < 0)
				    	max_value = - max_value;	
				}
				
				//int previous_min_value = dynamic_range_slider.getValue();	
				dynamic_range_slider.setValue(min_value);
				
				//int previous_max_value = dynamic_range_slider.getUpperValue();	
				dynamic_range_slider.setUpperValue(max_value);
				
				dynamic_button_changing = false;	
				data_canvas.repaint();
			}
		};
		
		adjust_bounds_button.addActionListener(adjust_range_handler);
		bounds_button_panel.add(reset_bounds_button);
		ActionListener reset_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String lower_bound_string = String.format("%,.2f", seg_min);
				String upper_bound_string = String.format("%,.2f", seg_max);
				data_clipped = false;
				
				minimum_y = seg_min;
				maximum_y = seg_max;
				
				lower_bound.setText(lower_bound_string);
				upper_bound.setText(upper_bound_string);
				dynamic_button_changing = true;
				dynamic_range_slider.setValue(0);
				dynamic_range_slider.setUpperValue(100);
				dynamic_button_changing = false;
				data_clipped = false;
				dynamic_range_canvas.repaint();
				data_canvas.repaint();
			}
		};
		reset_bounds_button.addActionListener(reset_handler);
		JPanel dynamic_range_button_panel = new JPanel(new BorderLayout());
		dynamic_range_button_panel.add(bounds_panel, BorderLayout.CENTER);
		dynamic_range_button_panel.add(bounds_button_panel, BorderLayout.SOUTH);
		dynamic_range_slider = new RangeSlider();
		dynamic_range_slider.setOrientation(JSlider.VERTICAL);
		dynamic_range_slider.setMinimum(0);
		dynamic_range_slider.setMaximum(100);
		dynamic_range_slider.setValue(0);
		dynamic_range_slider.setUpperValue(100);
		DynamicRangeSliderHandler dynamic_range_slider_handler = new DynamicRangeSliderHandler();
		dynamic_range_slider.addChangeListener(dynamic_range_slider_handler);
		dynamic_range_canvas = new DynamicRangeCanvas();
		dynamic_range_canvas.setSize(100, 300);
		JPanel dynamic_range_canvas_panel = new JPanel(new BorderLayout());
		dynamic_range_canvas_panel.add(dynamic_range_slider, BorderLayout.WEST);
		dynamic_range_canvas_panel.add(dynamic_range_canvas, BorderLayout.CENTER);
		JPanel dynamic_range_panel = new JPanel(new BorderLayout());
		dynamic_range_panel.add(dynamic_range_canvas_panel, BorderLayout.CENTER);
		dynamic_range_panel.add(dynamic_range_button_panel, BorderLayout.SOUTH);
		dynamic_range_dialog = new JDialog(frame);
		dynamic_range_dialog.add(dynamic_range_panel);
		
		JMenuItem dynamic_range_item = new JMenuItem("Dynamic Range");
		ActionListener dynamic_range_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();
				Dimension canvas_dimension = data_canvas.getSize();
				double    canvas_xdim      = canvas_dimension.getWidth();
				x += canvas_xdim;
				//y += 770;
				y += 590;
				
				if(!data_clipped)
                {
					lower_bound.setText(String.format("%,.2f", seg_min));
					upper_bound.setText(String.format("%,.2f", seg_max));
                }
                else
                {
                	lower_bound.setText(String.format("%,.2f", minimum_y));	
                	upper_bound.setText(String.format("%,.2f", maximum_y));
                }
               
				dynamic_range_dialog.setLocation(x, y);
				dynamic_range_dialog.pack();
				dynamic_range_dialog.setVisible(true);
			}
		};
		dynamic_range_item.addActionListener(dynamic_range_handler);
		adjustment_menu.add(dynamic_range_item);	
		menu_bar.add(adjustment_menu);
		// End adjustment menu.
		
		// Start slope menu.
	
		JMenu  slope_menu  = new JMenu("Slope");
		
		JPanel slope_panel = new JPanel(new BorderLayout());
		slope_output = new JTextArea(25, 10);
		JPanel slope_button_panel = new JPanel(new GridLayout(2,3));
		JButton   slope_start_button       = new JButton("Start");
		ActionListener startpoint_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				startpoint_x = append_x;
				startpoint_y = append_y;
				startpoint_intensity = append_intensity;
				startpoint_x_position = append_x_position;
				startpoint_y_position = append_y_position;
				startpoint_line       = append_line;
				startpoint_sensor     = append_sensor;
				startpoint_index      = append_index;
				startpoint_gui_index  = append_gui_index;
				
				startpoint_set = true;
				append_data    = false;
				persistent_data = false;
				
				slope_output.append(" start_intensity     " + String.format("%.2f",startpoint_intensity) + " nT\n");
            	slope_output.append(" start_x                 " + String.format("%.2f", startpoint_x) + "  m\n");
            	slope_output.append(" start_y                 " + String.format("%.2f", startpoint_y) + "  m\n");
            	slope_output.append(" start_line_sensor " + startpoint_line + ":" + startpoint_sensor + "\n\n");
				
				sample_information.setText("");
				data_canvas.repaint();
				segment_image.repaint();
				segment_map.repaint();
			}
		};
		slope_start_button.addActionListener(startpoint_handler);
		slope_button_panel.add(slope_start_button);
		
		JButton slope_midpoint_button  = new JButton("Midpoint");
		ActionListener midpoint_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				midpoint_x          = append_x;
				midpoint_y          = append_y;
				midpoint_intensity  = append_intensity;
				midpoint_intensity  = append_intensity;
				midpoint_x_position = append_x_position;
				midpoint_y_position = append_y_position;
				midpoint_line       = append_line;
				midpoint_sensor     = append_sensor;
				midpoint_index      = append_index;
				midpoint_gui_index  = append_gui_index;
				midpoint_set        = true;
				append_data         = false;
				persistent_data     = false;
				
				slope_output.append(" mid_intensity     " + String.format("%.2f", midpoint_intensity) +  " nT\n");
            	slope_output.append(" mid_x                 " + String.format("%.2f", midpoint_x) + "  m\n");
            	slope_output.append(" mid_y                 " + String.format("%.2f", midpoint_y) + "  m\n");
            	slope_output.append(" mid_line_sensor " + midpoint_line + ":" + midpoint_sensor + "\n\n");
				
				sample_information.setText("");
				data_canvas.repaint();
				//segment_image_canvas.repaint();
				segment_image.repaint();
				segment_map.repaint();
			}
		};
		slope_midpoint_button.addActionListener(midpoint_handler);
		slope_button_panel.add(slope_midpoint_button);
		
		
		JButton slope_end_button       = new JButton("End");
		ActionListener endpoint_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				endpoint_x          = append_x;
				endpoint_y          = append_y;
				endpoint_intensity  = append_intensity;
				endpoint_intensity  = append_intensity;
				endpoint_x_position = append_x_position;
				endpoint_y_position = append_y_position;
				endpoint_line       = append_line;
				endpoint_sensor     = append_sensor;
				endpoint_index      = append_index;
				endpoint_gui_index  = append_gui_index;
				endpoint_set        = true;
				append_data         = false;
				persistent_data     = false;
				
				slope_output.append(" end_intensity     " + String.format("%.2f", endpoint_intensity) + " nT\n");
            	slope_output.append(" end_x                 " + String.format("%.2f", endpoint_x) + "  m\n");
            	slope_output.append(" end_y                 " + String.format("%.2f", endpoint_y) + "  m\n");
            	slope_output.append(" end_line_sensor " + endpoint_line + ":" + endpoint_sensor + "\n\n");
            	
				sample_information.setText("");
				data_canvas.repaint();
				//segment_image_canvas.repaint();
				segment_image.repaint();
				segment_map.repaint();
			}
		};
		slope_end_button.addActionListener(endpoint_handler);
		slope_button_panel.add(slope_end_button);
		
		JButton slope_apply_button = new JButton("Apply");
		ActionListener triple_apply_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(startpoint_set && midpoint_set && endpoint_set)
				{
	            	double amplitude1 = midpoint_intensity - startpoint_intensity;
				    double width1  = getDistance(startpoint_x, startpoint_y, midpoint_x, midpoint_y);
				    double start_slope = amplitude1 / width1;
				    
				    
				    double amplitude2 = endpoint_intensity - midpoint_intensity;
				    double width2  = getDistance(endpoint_x, endpoint_y, midpoint_x, midpoint_y);
				    double end_slope = amplitude2 / width2;
				    
				    slope_output.append(" amplitude1  " + String.format("%.2f", amplitude1) + " nT\n");
				    slope_output.append(" width1          " + String.format("%.2f", width1) + "  m\n");
				    slope_output.append(" start_slope  " + String.format("%.2f", start_slope) + " nT/m\n\n");
				    
				    slope_output.append(" amplitude2  " + String.format("%.2f", amplitude2) + " nT\n");
				    slope_output.append(" width2         " + String.format("%.2f", width2) + "  m\n");
				    slope_output.append(" end_slope   " + String.format("%.2f", end_slope) + " nT/m\n\n");
				    
				     
				    double [][] location_array = ObjectMapper.getObjectLocationArray();
					int length = location_array.length;
					for(int i = 0; i < length; i++)
					{
						location_array[i][0] -= global_xmin;
						location_array[i][1] -= global_ymin;
					}
					double previous_distance = getDistance(midpoint_x, midpoint_y, location_array[0][0], location_array[0][1]);
					int    closest_target    = 0;
					for(int i = 1; i < location_array.length; i++)
					{
						double current_distance = getDistance(midpoint_x, midpoint_y, location_array[i][0], location_array[i][1]);
						if(current_distance < previous_distance)
						{
							previous_distance = current_distance;
							closest_target = i;
						}
					}
					slope_output.append(" nearest_target_id " + (closest_target + 1) + "\n");
					slope_output.append(" nearest_target_distance " + String.format("%.2f", previous_distance) + "\n");
				}
				else if(startpoint_set && endpoint_set)
				{ 
					
					slope_output.append(" start_intensity      "     + String.format("%.2f",startpoint_intensity) + " nT\n");
					slope_output.append(" start_x                  " + String.format("%.2f", startpoint_x) + "  m\n");
					slope_output.append(" start_y                  " + String.format("%.2f", startpoint_y) + "  m\n");
					slope_output.append(" start_line_sensor  "       + startpoint_line + ":" + startpoint_sensor + "\n\n");
	            	
					slope_output.append(" end_intensity       "      + String.format("%.2f", endpoint_intensity) + " nT\n");
					slope_output.append(" end_x                   " + String.format("%.2f", endpoint_x) + "  m\n");
					slope_output.append(" end_y                   " + String.format("%.2f", endpoint_y) + "  m\n");
					slope_output.append(" end_line_sensor   "       + endpoint_line + ":" + endpoint_sensor + "\n\n");
	                
	            	double amplitude = endpoint_intensity - startpoint_intensity;
				    double width  = getDistance(startpoint_x, startpoint_y, endpoint_x, endpoint_y);
				    double slope = amplitude / width;
				     
				    slope_output.append(" amplitude             " + String.format("%.2f", amplitude) + " nT\n");
				    slope_output.append(" width                    " + String.format("%.2f", width) + "   m\n");
				    slope_output.append(" slope                    " + String.format("%.2f", slope) + " nT/m\n\n");
				    
				    double [][] location_array = ObjectMapper.getObjectLocationArray();
					int length = location_array.length;
					for(int i = 0; i < length; i++)
					{
						location_array[i][0] -= global_xmin;
						location_array[i][1] -= global_ymin;
					}
					double previous_distance = getDistance(endpoint_x, endpoint_y, location_array[0][0], location_array[0][1]);
					int    closest_target    = 0;
					for(int i = 1; i < location_array.length; i++)
					{
						double current_distance = getDistance(endpoint_x, endpoint_y, location_array[i][0], location_array[i][1]);
						if(current_distance < previous_distance)
						{
							previous_distance = current_distance;
							closest_target = i;
						}
					}
					slope_output.append(" nearest_target_id            " + (closest_target + 1) + "\n");
					slope_output.append(" nearest_target_distance " + String.format("%.2f", previous_distance) + " m\n");
				}
				else
				{
					if(!startpoint_set)
						System.out.println("Start point is not set.");
					if(!endpoint_set)
						System.out.println("End point is not set.");
					
				}
			}
		};
		slope_apply_button.addActionListener(triple_apply_handler);
		slope_button_panel.add(slope_apply_button);
		
		JButton slope_clear_button = new JButton("Clear");
		ActionListener triple_clear_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				startpoint_set = false;
				midpoint_set   = false;
				endpoint_set   = false;
				
				slope_output.setText("");
				data_canvas.repaint();
				//segment_image_canvas.repaint();
				segment_image.repaint();
				segment_map.repaint();
			}
		};
		slope_clear_button.addActionListener(triple_clear_handler);
		slope_button_panel.add(slope_clear_button);
		
		
		JButton slope_save_button = new JButton("Save");
		ActionListener slope_save_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
			
				//System.out.println("Saving points of interest.");
				try
	            {
	            	FileWriter output  = new FileWriter("fpoints.txt", true);
	            	
	            	//output.write("start_index " + startpoint_index + "\n");
	            	output.write("start_intensity " + String.format("%.2f",startpoint_intensity) + " nT\n");
	            	output.write("start_x " + String.format("%.2f", startpoint_x) + " m\n");
	            	output.write("start_y " + String.format("%.2f", startpoint_y) + " m\n");
	            	output.write("start_line_sensor " + startpoint_line + ":" + startpoint_sensor + "\n");
	            	
	            	output.write("mid_index " + midpoint_index + "\n");
	            	output.write("mid_intensity " + String.format("%.2f", midpoint_intensity) + " nT\n");
	            	output.write("mid_x " + String.format("%.2f", midpoint_x) + " m\n");
	            	output.write("mid_y " + String.format("%.2f", midpoint_y) + " m\n");
	            	output.write("mid_line_sensor " + midpoint_line + ":" + midpoint_sensor + "\n");
	            	//output.write("end_index " + endpoint_index + "\n");
	            	output.write("end_intensity " + String.format("%.2f", endpoint_intensity) + " nT\n");
	            	output.write("end_x " + String.format("%.2f", endpoint_x) + " m m\n");
	            	output.write("end_y " + String.format("%.2f", endpoint_y) + "\n");
	            	output.write("end_line_sensor " + endpoint_line + ":" + endpoint_sensor + "\n");
	            	
	            	double amplitude1 = midpoint_intensity - startpoint_intensity;
				    double width1  = getDistance(startpoint_x, startpoint_y, midpoint_x, midpoint_y);
				    double start_slope = amplitude1 / width1;
				    output.write("amplitude1 " + String.format("%.2f", amplitude1) + " nT\n");
				    output.write("width1 " + String.format("%.2f", width1) + " m\n");
				    output.write("start_slope " + String.format("%.2f", start_slope) + " nT/m\n");
				    
				    double amplitude2 = endpoint_intensity - midpoint_intensity;
				    double width2  = getDistance(endpoint_x, endpoint_y, midpoint_x, midpoint_y);
				    double end_slope = amplitude2 / width2;
				    output.write("amplitude2 " + String.format("%.2f", amplitude2) + " nT\n");
				    output.write("width2 " + String.format("%.2f", width2) + " m\n");
				    output.write("end_slope " + String.format("%.2f", end_slope) + " nT/m\n");
				  
				    double [][] location_array = ObjectMapper.getObjectLocationArray();
					int length = location_array.length;
					for(int i = 0; i < length; i++)
					{
						location_array[i][0] -= global_xmin;
						location_array[i][1] -= global_ymin;
					}
					double previous_distance = getDistance(midpoint_x, midpoint_y, location_array[0][0], location_array[0][1]);
					int    closest_target    = 0;
					for(int i = 1; i < location_array.length; i++)
					{
						double current_distance = getDistance(midpoint_x, midpoint_y, location_array[i][0], location_array[i][1]);
						if(current_distance < previous_distance)
						{
							previous_distance = current_distance;
							closest_target = i;
						}
					}
					output.write("nearest_target_id " + (closest_target + 1) + "\n");
					output.write("nearest_target_distance " + String.format("%.2f", previous_distance) + " m\n");
					
	            	output.write("\n");
	            	Date current_time = new Date();
	            	output.write(current_time.toString());
	            	output.write("\n");
	            	output.write("\n");
	            	output.write("\n");
	            	output.close();
	            }
				catch(Exception ex)
				{
				    System.out.println(ex.toString());	
				}
			}
		};
		slope_save_button.addActionListener(slope_save_handler);
		slope_button_panel.add(slope_save_button);
		
		slope_panel.add(slope_output, BorderLayout.CENTER);
		slope_panel.add(slope_button_panel, BorderLayout.SOUTH);
		
		slope_dialog = new JDialog(frame, "Get Slope");
		slope_dialog.add(slope_panel);
		JMenuItem slope_item = new JMenuItem("Get Slope");
		ActionListener slope_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();

				Dimension canvas_dimension = data_canvas.getSize();
				double    canvas_xdim      = canvas_dimension.getWidth();
				
				x += canvas_xdim;
				


				slope_dialog.setLocation(x, y);
				slope_dialog.pack();
				slope_dialog.setVisible(true);
			}
		};
		slope_item.addActionListener(slope_handler);
		slope_menu.add(slope_item);
		
		// A modeless dialog box that shows up if Slope->Show Segment Image is selected.
		//segment_image_canvas = new SegmentImageCanvas();
		//segment_image_canvas.setSize(600, 400);
		segment_image = new SegmentImage();
		segment_image.setSize(700, 460);
		segment_image_dialog = new JDialog(frame);
		//segment_image_dialog.add(segment_image_canvas);
		segment_image_dialog.add(segment_image);

		JMenuItem segment_image_item = new JMenuItem("Show Segment Image");
		ActionListener segment_image_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();

				Dimension canvas_dimension = data_canvas.getSize();
				double    canvas_xdim      = canvas_dimension.getWidth();
				
				x += canvas_xdim;
				y += 490;
				segment_image_dialog.setLocation(x, y);
				segment_image_dialog.pack();
				segment_image_dialog.setVisible(true);
			}
		};
		segment_image_item.addActionListener(segment_image_handler);
		
		slope_menu.add(segment_image_item);
				
		// A modeless dialog box that shows up if Slope->Show Segment Image is selected.
		segment_map = new SegmentMap();
		segment_map.setSize(600, 400);
		segment_map_dialog = new JDialog(frame);
		segment_map_dialog.add(segment_map);

		JMenuItem segment_map_item = new JMenuItem("Show Segment Map");
		ActionListener segment_map_handler = new ActionListener()
		{
		    public void actionPerformed(ActionEvent event)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();

				Dimension canvas_dimension = data_canvas.getSize();
				double    canvas_xdim      = canvas_dimension.getWidth();
						
				x += canvas_xdim + 250;
				segment_map_dialog.setLocation(x, y);
				segment_map_dialog.pack();
				segment_map_dialog.setVisible(true);
			}
		};
		segment_map_item.addActionListener(segment_map_handler);
		slope_menu.add(segment_map_item);
		
		menu_bar.add(slope_menu);
	
		// End slope menu.
		
		
		// Start location menu.
		
		JMenu     location_menu  = new JMenu("Location");
		
		JPanel location_canvas_panel = new JPanel(new BorderLayout());
		location_canvas = new LocationCanvas();
		location_canvas.setSize(470, 330);
		
		// Not clear if this helps.
		/*
		JScrollBar xlocation_scrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, 0, 2001);
		AdjustmentListener xlocation_handler = new AdjustmentListener()
		{
		    public void adjustmentValueChanged(AdjustmentEvent event)
			{
			    int location = event.getValue();
			    xlocation = (double)location;
			    xlocation /= 2000.;
			    location_changing = event.getValueIsAdjusting();
				if(location_canvas != null)
				    location_canvas.repaint();
			}
		};		
		xlocation_scrollbar.addAdjustmentListener(xlocation_handler);
		value = (int)(xlocation * 2000.);
		xlocation_scrollbar.setValue(value);
		
		JScrollBar ylocation_scrollbar = new JScrollBar(JScrollBar.VERTICAL, 0, 1, 0, 2001);
		AdjustmentListener ylocation_handler = new AdjustmentListener()
		{
		    public void adjustmentValueChanged(AdjustmentEvent event)
			{
			    int location = event.getValue();
			    location     = 2000 - location;
			    ylocation    = (double)location;
			    ylocation    /= 2000.;
			    location_changing = event.getValueIsAdjusting();
				if(location_canvas != null)
				    location_canvas.repaint();
			}
		};		
		ylocation_scrollbar.addAdjustmentListener(ylocation_handler);
		value = (int)(2000. - ylocation * 2000.);
		ylocation_scrollbar.setValue(value);
		*/
		
		location_canvas_panel.add(location_canvas, BorderLayout.CENTER);
		//location_canvas_panel.add(xlocation_scrollbar, BorderLayout.SOUTH);
		//location_canvas_panel.add(ylocation_scrollbar, BorderLayout.EAST);
		location_dialog = new JDialog(frame, "Location");
		location_dialog.add(location_canvas_panel);
		
		JMenuItem map_item = new JMenuItem("Show Location Map");
		ActionListener map_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();

				Dimension canvas_dimension = data_canvas.getSize();
				double    canvas_xdim      = canvas_dimension.getWidth();
				
				x += canvas_xdim + 250;
				location_dialog.setLocation(x, y);
				location_dialog.pack();
				location_dialog.setVisible(true);
			}
		};
		map_item.addActionListener(map_handler);
		location_menu.add(map_item);
		

		JPanel offset_range_panel = new JPanel(new BorderLayout());
		JPanel parameter_panel = new JPanel(new GridLayout(2, 2));
		parameter_panel.add(new JLabel("Offset", JLabel.CENTER));
		parameter_panel.add(new JLabel("Range", JLabel.CENTER));
		
	    offset_information = new JTextField();
	    offset_information.setHorizontalAlignment(JTextField.CENTER);
	    offset_information.setText(String.format("%,.4f", data_offset));
		parameter_panel.add(offset_information);
		
		range_information = new JTextField();
		range_information.setHorizontalAlignment(JTextField.CENTER);
		range_information.setText(String.format("%.2f", data_range * data_length));
		parameter_panel.add(range_information);
		
		offset_range_panel.add(parameter_panel, BorderLayout.CENTER);
		JButton adjust_button = new JButton("Adjust");
		
		class RangeButtonHandler implements ActionListener
		{
			public void actionPerformed(ActionEvent e)
			{
				String offset_string  = offset_information.getText();
				String range_string   = range_information.getText();
				double current_offset = Double.valueOf(offset_string);
				double current_range  = Double.valueOf(range_string);
				current_range /= data_length;

				if (current_offset < 0 || current_offset > (1 - data_range) || current_range < 0 || current_range > (1 - current_offset))
				{
					if(current_offset < 0 || current_offset > .9999)
						System.out.println("Invalid input: offset = " + offset_string);
					else if(current_range < .998 || current_range > (1 - current_offset))
						System.out.println("Invalid input: range = " + range_string);
					offset_information.setText(String.format("%,.4f", data_offset));
					range_information.setText(String.format("%.2f", data_range * data_length));
				} 
				else if(location_slider_changing == false)
				{
					range_button_changing = true;
					//data_offset = current_offset;
					data_range  = current_range;
					
					int current_value = (int)(data_offset * 100);
					location_slider.setValue(current_value);
					location_information.setText(String.format("%.4f", data_offset));
                    
					// Clear data since we're at a new position.
					append_data     = false;
					persistent_data = false;
					startpoint_set  = false;
					midpoint_set    = false;
					endpoint_set    = false;
					
					sample_information.setText("");	
					slope_output.setText("");
					
					// Reset scrollbar.
					int scrollbar_position = (int) (data_offset * scrollbar_resolution + data_range * scrollbar_resolution / 2);
					data_scrollbar.setValue(scrollbar_position);
                    
					range_button_changing = false;
                    
                    // Resegment the data.
					append_data    = false;
					startpoint_set = false;
					midpoint_set   = false;
				    endpoint_set   = false;
					data_canvas.repaint();
					location_canvas.repaint();
					
					segment_image.repaint();
					segment_map.repaint();				}
			}
		};
		RangeButtonHandler adjust_handler = new RangeButtonHandler();
		adjust_button.addActionListener(adjust_handler);
		offset_range_panel.add(adjust_button, BorderLayout.SOUTH);
		range_dialog = new JDialog(frame, "Offset/Range");
	    range_dialog.add(offset_range_panel);
		JMenuItem range_item = new JMenuItem("Set Offset/Range");
		ActionListener range_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();

				Dimension canvas_dimension = data_canvas.getSize();
				double    canvas_xdim      = canvas_dimension.getWidth();
	            double    canvas_ydim      = canvas_dimension.getHeight();
				
				x += 520;
				
				y += canvas_ydim + 70;

				range_dialog.setLocation(x, y);
				range_dialog.pack();
				range_dialog.setVisible(true);
			}
		};
		range_item.addActionListener(range_handler);
		
		location_menu.add(range_item);
		
	    class LocationSliderHandler implements ChangeListener
	    {
		    public void stateChanged(ChangeEvent e)
		    {
		        if(range_button_changing == false)
		        {
		            location_slider_changing = true;
			        JSlider slider = (JSlider) e.getSource();
			        if(slider.getValueIsAdjusting() == false)
			        {
				        int value = slider.getValue();
				        double new_offset = value;
				        new_offset /= 100;
				        double new_range  = data_range;
				        if(new_range + new_offset > 1)
				            new_range = 1 - new_offset;
				        data_offset = new_offset;
				        data_range = new_range;
				        
				        offset_information.setText(String.format("%.4f", data_offset));
				        range_information.setText(String.format("%.2f", data_range * data_length));
				        location_information.setText(String.format("%.4f", data_offset));
				        
						// Clear data since we're at a new position.
						append_data     = false;
						persistent_data = false;
						startpoint_set  = false;
						midpoint_set    = false;
						endpoint_set    = false;
						
						sample_information.setText("");	
						slope_output.setText("");
						
						// Reset scrollbar.
						int scrollbar_position = (int) (data_offset * scrollbar_resolution + data_range * scrollbar_resolution / 2);
						data_scrollbar.setValue(scrollbar_position);
				        
						append_data    = false;
						startpoint_set = false;
						midpoint_set   = false;
					    endpoint_set   = false;
						data_canvas.repaint();
						location_canvas.repaint();
						
						segment_image.repaint();  
						segment_map.repaint();				    }
				        location_slider_changing = false;
				 }
			}	
	    }
	    
		JPanel  location_slider_panel  = new JPanel(new BorderLayout());
		int     current_value          = (int)(data_offset * 100);
	
		location_slider = new JSlider(0, 98, current_value);
		
		//System.out.println("Setting location slider value to " + current_value);
		
		LocationSliderHandler location_slider_handler = new LocationSliderHandler();
		
		location_slider.addChangeListener(location_slider_handler);
		
		location_information = new JTextField();
		location_information.setHorizontalAlignment(JTextField.CENTER);
		location_information.setColumns(7);
		//System.out.println("Setting location information to " + data_offset);
		location_information.setText(" " + String.format("%.4f", data_offset) + " ");
		location_slider_panel.add(location_slider, BorderLayout.CENTER);
		location_slider_panel.add(location_information, BorderLayout.EAST);
		JDialog	location_dialog = new JDialog(frame, "Set Location");
		location_dialog.add(location_slider_panel);
		JMenuItem location_item = new JMenuItem("Set Location");
		ActionListener location_item_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();
				
				Dimension canvas_dimension = data_canvas.getSize();
				double    canvas_xdim      = canvas_dimension.getWidth();
				double    canvas_ydim      = canvas_dimension.getHeight();
					
			    x += 650;
			    y += canvas_ydim + 70;
			    
				location_dialog.setLocation(x, y);
				location_dialog.pack();
				location_dialog.setVisible(true);
			}
		};
		location_item.addActionListener(location_item_handler);
		location_menu.add(location_item);		
		
		
		
		// A modeless dialog box that shows up if Location->Set Object is selected.		
		set_object_dialog = new JDialog(frame, "Set Object");
		JMenuItem set_object_item = new JMenuItem("Set Object");
		ActionListener set_object_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();

				Dimension canvas_dimension = data_canvas.getSize();
				double    canvas_xdim      = canvas_dimension.getWidth();
				double    canvas_ydim      = canvas_dimension.getHeight();
				
			    x += 650;
			    y += canvas_ydim + 70;

				set_object_dialog.setLocation(x, y);
				set_object_dialog.pack();
				set_object_dialog.setVisible(true);
			}
		};
		set_object_item.addActionListener(set_object_handler);
		JPanel set_object_panel = new JPanel(new GridLayout(2, 1));
		JTextField set_object_input             = new JTextField();
		set_object_input.setHorizontalAlignment(JTextField.CENTER);
		set_object_input.setText("");
		JButton set_object_button = new JButton("Set Object");
		ActionListener set_object_button_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String object_string = set_object_input.getText();
				int object_id = Integer.parseInt(object_string);
						
				if(object_id >= 1 && object_id < 54)
				{
					int    index          = (int)object_index.get(object_id - 1);
					Sample current_sample = (Sample)data.get(index);
					double current_offset = current_sample.y;
					
					data_offset = current_offset / data_length - data_range / 2;
					
					// Clear data since we're at a new position.
					append_data     = false;
					persistent_data = false;
					startpoint_set  = false;
					midpoint_set    = false;
					endpoint_set    = false;	
					sample_information.setText("");	
					slope_output.setText("");
					
					
					
					// Reset scrollbar.
					object_button_changing = true;
					int scrollbar_position = (int) (data_offset * scrollbar_resolution + data_range * scrollbar_resolution / 2);
					data_scrollbar.setValue(scrollbar_position);
					object_button_changing = false;
					
					// Redraw and resegment data.
					data_canvas.repaint();
					location_canvas.repaint();
					//segment_image_canvas.repaint();
					segment_image.repaint();
					segment_map.repaint();
				}
				else
				{
					System.out.println("Object id must be from 1 to 53.");	
				}
			}
		};
		set_object_button.addActionListener(set_object_button_handler);
		set_object_panel.add(set_object_input);
		set_object_panel.add(set_object_button);
		set_object_dialog.add(set_object_panel);
		set_object_dialog.add(set_object_panel);
		location_menu.add(set_object_item);
		menu_bar.add(location_menu);
		
		// End location menu.
		
		// Start settings menu.
		
		JMenu     settings_menu  = new JMenu("Settings");
		view_item = new JCheckBoxMenuItem("Reverse View");
		ActionListener view_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
            {
            	JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
            	
            	
            	Dimension canvas_dimension = data_canvas.getSize();
				int       canvas_xdim      = (int)canvas_dimension.getWidth();
				int       canvas_ydim      = (int)canvas_dimension.getHeight();
				
				double    max_xstep        = (canvas_xdim - (left_margin + right_margin)) / 5;
				int       xstep            = (int) (max_xstep * normal_xstep);
				
				double    max_ystep        = (canvas_ydim - (top_margin + bottom_margin)) / 5;
				int       ystep            = (int) (max_ystep * normal_ystep);
            	
				int delta_x = 0;
				int delta_y = 0;
				
            	if(append_data)
				{
					if(append_sensor == 0)
					{
						if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
						{
						    delta_x = 4 * xstep;
						    delta_y = -4 * ystep;
						}
						else
						{
							delta_x = -4 * xstep;
							delta_y = 4 * ystep;
						}	
					}
					else if(append_sensor == 4)
					{
						if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
						{
						    delta_x = -4 * xstep;
						    delta_y = 4 * ystep;
						}
						else
						{
							delta_x = 4 * xstep;
							delta_y =  -4 * ystep;
						} 
					}
					else if(append_sensor == 1)
					{
						if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
						{
						    delta_x = 2 * xstep;
						    delta_y = -2 * ystep;
						}
						else
						{
							delta_x = -2 * xstep;
							delta_y = 2 * ystep;
						}	
					}
					else if(append_sensor == 3)
					{
						if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
						{
						    delta_x = -2 * xstep;
						    delta_y = 2 * ystep;
						}
						else
						{
							delta_x = 2 * xstep;
							delta_y =  -2 * ystep;
						} 
					}
					
					append_x_position += delta_x;
				    append_y_position += delta_y;
				}
            	
            	if(startpoint_set)
				{
					if(startpoint_sensor == 0)
					{
						if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
						{
						    delta_x = 4 * xstep;
						    delta_y = -4 * ystep;
						}
						else
						{
							delta_x = -4 * xstep;
							delta_y = 4 * ystep;
						}	
					}
					else if(startpoint_sensor == 4)
					{
						if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
						{
						    delta_x =  -4 * xstep;
						    delta_y = 4 * ystep;
						}
						else
						{
							delta_x = 4 * xstep;
							delta_y =  -4 * ystep;
						} 
					}
					else if(startpoint_sensor == 1)
					{
						if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
						{
						    delta_x = 2 * xstep;
						    delta_y =  -2 * ystep;
						}
						else
						{
							delta_x =  -2 * xstep;
							delta_y = 2 * ystep;
						}  
					}
					else if(startpoint_sensor == 3)
					{
						if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
						{
						    delta_x =  -2 * xstep;
						    delta_y = 2 * ystep;
						}
						else
						{
							delta_x = 2 * xstep;
							delta_y =  -2 * ystep;
						} 
					}
					
					startpoint_x_position += delta_x;
				    startpoint_y_position += delta_y;
				}
            	
            	if(midpoint_set)
				{
					if(midpoint_sensor == 0)
					{
						if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
						{
						    delta_x = 4 * xstep;
						    delta_y =  -4 * ystep;
						}
						else
						{
							delta_x =  -4 * xstep;
							delta_y = 4 * ystep;
						}	
					}
					else if(midpoint_sensor == 4)
					{
						if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
						{
						    delta_x =  -4 * xstep;
						    delta_y = 4 * ystep;
						}
						else
						{
							delta_x = 4 * xstep;
							delta_y =  -4 * ystep;
						} 
					}
					else if(midpoint_sensor == 1)
					{
						if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
						{
						    delta_x = 2 * xstep;
						    delta_y =  -2 * ystep;
						}
						else
						{
							delta_x =  -2 * xstep;
							delta_y = 2 * ystep;
						}  
					}
					else if(midpoint_sensor == 3)
					{
						if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
						{
						    delta_x =  -2 * xstep;
						    delta_y = 2 * ystep;
						}
						else
						{
							delta_x = 2 * xstep;
							delta_y =  -2 * ystep;
						} 
					}
					
					midpoint_x_position += delta_x;
				    midpoint_y_position += delta_y;
				}
            	

            	if(endpoint_set)
				{
					if(endpoint_sensor == 0)
					{
						if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
						{
						    delta_x = 4 * xstep;
						    delta_y =  -4 * ystep;
						}
						else
						{
							delta_x =  -4 * xstep;
							delta_y = 4 * ystep;
						}	
					}
					else if(endpoint_sensor == 4)
					{
						if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
						{
						    delta_x =  -4 * xstep;
						    delta_y = 4 * ystep;
						}
						else
						{
							delta_x = 4 * xstep;
							delta_y =  -4 * ystep;
						} 
					}
					else if(endpoint_sensor == 1)
					{
						if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
						{
						    delta_x = 2 * xstep;
						    delta_y =  -2 * ystep;
						}
						else
						{
							delta_x =  -2 * xstep;
							delta_y = 2 * ystep;
						}  
					}
					else if(endpoint_sensor == 3)
					{
						if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
						{
						    delta_x =  -2 * xstep;
						    delta_y = 2 * ystep;
						}
						else
						{
							delta_x = 2 * xstep;
							delta_y =  -2 * ystep;
						} 
					}
					
					endpoint_x_position += delta_x;
				    endpoint_y_position += delta_y;
				}
            	
            	if(reverse_view == true)
				{
            		reverse_view = false;
					item.setState(false);
				}
				else
				{
					reverse_view = true;
					item.setState(true);
				}
            	
            	placement_canvas.repaint();
				data_canvas.repaint();
				location_canvas.repaint();
				segment_image.repaint();
				segment_map.repaint();
            }   	
		};
		view_item.addActionListener(view_handler);
		if(reverse_view)
			view_item.setState(true);
		settings_menu.add(view_item);
		
		overlay_item = new JCheckBoxMenuItem("Raster Overlay");
		ActionListener overlay_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
            {
            	JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
            	if(raster_overlay == true)
				{
            		raster_overlay = false;
					item.setState(false);
					placement_canvas.repaint();
				}
				else
				{
					raster_overlay = true;
					item.setState(true);
					placement_canvas.repaint();
				}
		        data_canvas.repaint();
            }   	
		};
		overlay_item.addActionListener(overlay_handler);
		if(raster_overlay)
			overlay_item.setState(true);
		settings_menu.add(overlay_item);
		
		mode_item = new JCheckBoxMenuItem("Relative Mode");
		ActionListener mode_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
            {
            	JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
            	if(relative_mode == true)
				{
            		relative_mode = false;
					item.setState(false);
				}
				else
				{
					relative_mode = true;
					item.setState(true);
				}
		        data_canvas.repaint();
            }   	
		};
		mode_item.addActionListener(mode_handler);
		if(relative_mode)
			mode_item.setState(true);
		settings_menu.add(mode_item);
		
		show_id_item = new JCheckBoxMenuItem("Show ID");
		ActionListener show_id_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
            {
            	JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
            	if(show_id == true)
				{
            		show_id = false;
					item.setState(false);
				}
				else
				{
					show_id = true;
					show_position = false;
					show_position_item.setState(false);
					item.setState(true);
				}
		        data_canvas.repaint();
            }   	
		};
		show_id_item.addActionListener(show_id_handler);
		if(show_id)
			show_id_item.setState(true);
		settings_menu.add(show_id_item);
		
		show_position_item = new JCheckBoxMenuItem("Show Position");
		ActionListener show_position_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
            {
            	JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
            	if(show_position == true)
				{
            		show_position = false;
					item.setState(false);
				}
				else
				{
					show_position = true;
					show_id       = false;
					show_id_item.setState(false);
					item.setState(true);
				}
		        data_canvas.repaint();
            }   	
		};
		show_position_item.addActionListener(show_position_handler);
		if(show_position)
			show_position_item.setState(true);
		settings_menu.add(show_position_item);
		
		color_key_item = new JCheckBoxMenuItem("Color Key");
		ActionListener color_key_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
            {
            	JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
            	if(color_key == true)
				{
            		color_key = false;
					item.setState(false);
				}
				else
				{
					color_key = true;
					item.setState(true);
				}
		        data_canvas.repaint();
            }   	
		};
		color_key_item.addActionListener(color_key_handler);
		if(color_key)
			color_key_item.setState(true);
		settings_menu.add(color_key_item);
		
		// A modeless dialog box that shows up if Settings->Show Data is selected.
		JPanel information_panel = new JPanel(new BorderLayout());
		sample_information = new JTextArea(8, 17);
		information_panel.add(sample_information);
		information_dialog = new JDialog(frame);
		information_dialog.add(information_panel);			
		show_data_item = new JCheckBoxMenuItem("Show Data");
		ActionListener show_data_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
            {
            	JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
            	if(show_data == true)
				{
            		show_data = false;
            		information_dialog.setVisible(false);
					item.setState(false);
					
				}
				else
				{
					Point location_point = frame.getLocation();
					int x = (int) location_point.getX();
					int y = (int) location_point.getY();
								
					Dimension canvas_dimension = data_canvas.getSize();
					double    canvas_xdim      = canvas_dimension.getWidth();
					double    canvas_ydim      = canvas_dimension.getHeight();
					
					x += canvas_xdim - 155;;
					y += canvas_ydim - 110;
					information_dialog.setLocation(x, y);
					information_dialog.pack();
					information_dialog.setVisible(true);
					show_data = true;
					item.setState(true);
				}
            }   	
		};
		show_data_item.addActionListener(show_data_handler);
		if(show_data)
			show_data_item.setState(true);
		else
			show_data_item.setState(false);
		settings_menu.add(show_data_item);		
		menu_bar.add(settings_menu);
		// End settings menu.
		
		frame.setJMenuBar(menu_bar);
		frame.getContentPane().add(data_panel, BorderLayout.CENTER);
		frame.pack();
		frame.setLocation(50, 10);
	    data_canvas.repaint();
	    show_data_item.doClick();
		//System.out.println("Finished Y Fence constructor.");
		// End constructor.
	}
	
	class PlotCanvas extends Canvas
	{
		PlotCanvas()
		{
			// The data array is what we use to construct the graph,
			// and the relative data array is the information we display in the graph.
			// Add empty lists at startup.
			
		
			for(int i = 0; i < 5; i++)
			{
				ArrayList data_list = new ArrayList();
				data_array.add(data_list);
				ArrayList relative_data_list = new ArrayList(); 
				relative_data_array.add(relative_data_list); 
			}
		}
		
		public void paint(Graphics g)
		{
			Rectangle  visible_area = g.getClipBounds();
			int        xdim         = (int) visible_area.getWidth();
			int        ydim         = (int) visible_area.getHeight();
			
			double    clipped_area     = xdim * ydim;
			Dimension canvas_dimension = this.getSize();
			double    canvas_xdim      = canvas_dimension.getWidth();
			double    canvas_ydim      = canvas_dimension.getHeight();
			double    entire_area      = canvas_xdim * canvas_ydim;
			

			if(clipped_area != entire_area)
			{
				if(buffered_image != null)
					g.drawImage(buffered_image, 0, 0, null);
				return;
			} 
			
			// Reallocate the memory every time because the canvas might get resized.
			pixel_data = new ArrayList[ydim][xdim];
			for (int i = 0; i < ydim; i++)
				for (int j = 0; j < xdim; j++)
					pixel_data[i][j] = new ArrayList();
			
			double[] end_position = new double[5];
			// Remember to clear any previous segments.
			int    number_of_segments = 5;
			for(int i = 0; i < number_of_segments; i++)
			{
				ArrayList data_list = (ArrayList)data_array.get(i);
				data_list.clear();
				ArrayList relative_data_list = (ArrayList)relative_data_array.get(i);
				relative_data_list.clear();
			}
			
			buffered_image  = new BufferedImage(xdim, ydim, BufferedImage.TYPE_INT_RGB);
			Graphics2D    graphics_buffer = (Graphics2D) buffered_image.getGraphics();
			graphics_buffer.setColor(java.awt.Color.WHITE);
			graphics_buffer.fillRect(0, 0, xdim, ydim);
			FontMetrics font_metrics = graphics_buffer.getFontMetrics();
			
			// This doesn't change so we can just set it once.
			// We also use string_width which varies.
			int string_height        = font_metrics.getAscent();
			
			// Get the flight lines to label the graph.
			// Assume a flight line is 1/30 of the data, 
			// which is only approximately true.
			double start_location = data_offset * 30.;
			double stop_location  = (data_offset + data_range) * 30;
			start_flight_line = (int)Math.floor(start_location);
			stop_flight_line = (int)Math.floor(stop_location);
			flight_line_odd = true;
	        if(start_flight_line % 2 == 0)
	            flight_line_odd = false;
			
			// Start data segmentation.
			
			// Get start and stop locations in terms of the entire data set.
			start_location = data_offset * data_length;
			stop_location  = (data_offset + data_range) * data_length;
			
			//System.out.println("Start location is " + start_location);
			//System.out.println("Stop location is " + stop_location);
			
			seg_min         = Double.MAX_VALUE;
			seg_max         = -Double.MAX_VALUE;
			double seg_xmin = Double.MAX_VALUE;
			double seg_xmax = 0; 
			
			
			if(smooth != 0) // Initialize data lists from smoothed data
			{
				// First get indices for the segment from the center sensor.
				start_index           = 0;
				stop_index            = 0;
				boolean start_set     = false;
				boolean stop_set      = false;
				boolean setting_index = true;
				while(setting_index)
				{
				    ArrayList source = (ArrayList)set_array.get(2);
				    int size         = source.size();
				    double[] y       = new double[size];
					for(int j = 0; j < size; j++)
					{
					    Sample sample = (Sample)source.get(j);
					    y[j] = sample.y;
					}
					double[] smooth_y = smooth(y, smooth);
					start_index = 0;
					stop_index  = 0;
					for(int j = 0; j < smooth_y.length; j++)
					{
					    if(y[j] >= start_location && !start_set)
					    {
					    	start_index = j;
					    	start_set   = true;
					    }
					    if(y[j] >= stop_location && !stop_set)
					    {
					    	stop_index = j;
					    	stop_set   = true;
					    	stop_index = j;
					    }
					}
				    setting_index = false;
				}
				
				
				for(int i = 0; i < 5; i++)
				{
					ArrayList source = (ArrayList)set_array.get(i);
					ArrayList dest   = (ArrayList)data_array.get(i);
					dest.clear();
					
					// Smooth the entire set of adjusted points.
					// We don't want to shrink the segment by itself because the range contracts,
					// and we'll need to use a few points outside the range to re-expand it.
					// Lot of unnecessary points being processed--could check that with
					// some extra code.
					
					int size = source.size();
					
					double[] x = new double[size];
					double[] y = new double[size];
					double[] z = new double[size];
					
					for(int j = 0; j < size; j++)
					{
					    Sample sample = (Sample)source.get(j);
					    x[j] = sample.x;
					    y[j] = sample.y;
					    z[j] = sample.intensity;
					}
					
					double[] smooth_x = smooth(x, smooth);
					//double[] smooth_x = x;
					double[] smooth_y = smooth(y, smooth);
					double[] smooth_z = smooth(z, smooth);

					for(int j = start_index; j < stop_index; j++)
					{
					    Sample sample = new Sample(smooth_x[j], smooth_y[j], smooth_z[j]);
					    dest.add(sample);
					    
					    // Some tricky stuff here--use the y value for x and the z value
						// for y in the wand plot.
					    
					    // Y
					    if (seg_min > sample.intensity)
							seg_min = sample.intensity;
						if (seg_max < sample.intensity)
							seg_max = sample.intensity;	
						
						// X
						if(seg_xmin > sample.y)
							seg_xmin = sample.y;
						if(seg_xmax < sample.y)
							seg_xmax = sample.y; 
					}
					
					// Now do a similar thing to the unadjusted data,
					// using the same indices.
					
					source = (ArrayList)relative_set_array.get(i);
					dest   = (ArrayList)relative_data_array.get(i);
					
                    size   = source.size();
					
					x = new double[size];
					y = new double[size];
					z = new double[size];
					
					for(int j = 0; j < size; j++)
					{
					    Sample sample = (Sample)source.get(j);
					    x[j] = sample.x;
					    y[j] = sample.y;
					    z[j] = sample.intensity;
					}
					
					smooth_x = smooth(x, smooth);
					smooth_y = smooth(y, smooth);
					smooth_z = smooth(z, smooth);
					
					for(int j = start_index; j < stop_index; j++)
					{
					    Sample sample = new Sample(smooth_x[j], smooth_y[j], smooth_z[j]);
					    dest.add(sample);
					}
				}
			}
			else // Initialize data lists from unsmoothed data.
			{
				start_index = 0;
				stop_index  = 0;
				
				boolean start_set = false;
				boolean stop_set  = false;
				
				ArrayList sample_list = (ArrayList)set_array.get(2);
				for(int i = 0; i < sample_list.size(); i++)
				{
				    Sample sample = (Sample)sample_list.get(i);
				    if(sample.y >= start_location && !start_set)
				    {
				    	start_index = i;
				    	start_set   = true;
				    }
				    if(sample.y >= stop_location && !stop_set)
				    {
				    	stop_index = i;
				    	stop_set   = true;
				    }
				}
				
				for(int i = 0; i < 5; i++)
				{
					ArrayList source = (ArrayList)set_array.get(i);
					ArrayList dest   = (ArrayList)data_array.get(i);
					dest.clear();
					for(int j = start_index; j < stop_index; j++)
					{
					    Sample sample = (Sample)source.get(j);
					    dest.add(sample);
					    
					    if (seg_min > sample.intensity)
							seg_min = sample.intensity;
						if (seg_max < sample.intensity)
							seg_max = sample.intensity;	

						if(seg_xmin > sample.y)
							seg_xmin = sample.y;
						if(seg_xmax < sample.y)
							seg_xmax = sample.y; 
					}
					
					source = (ArrayList)relative_set_array.get(i);
					dest   = (ArrayList)relative_data_array.get(i);
					dest.clear();
					for(int j = start_index; j < stop_index; j++)
					{
					    Sample sample = (Sample)source.get(j);
					    dest.add(sample);
					}
				}
			}
			
			//System.out.println("Finished segmenting data.");
			
			for(int i = 0; i < number_of_segments; i++)
			{
				ArrayList sample_list = (ArrayList)relative_data_array.get(i);
			    Sample end_sample = (Sample)sample_list.get(sample_list.size() - 1);	
			    end_position[i] = end_sample.x;
			}
			
			
			boolean data_increasing = true;
			ArrayList init_list     = (ArrayList)relative_data_array.get(2);
            if(init_list.size() == 0)
            {
	            System.out.println("No samples in data space.");
	            return;
            }
            	
			Sample init_sample      = (Sample)init_list.get(0);
			int size = init_list.size();
			Sample end_sample       = (Sample)init_list.get(size - 1);
			if(end_sample.y < init_sample.y)
			    data_increasing = false;
			double relative_start_y = 0;
			double relative_end_y   = 0;
			
			
			if(data_increasing)
			{
				relative_start_y = init_sample.y;
				relative_end_y   = end_sample.y;	
			}
			else
			{
				relative_start_y = end_sample.y;
				relative_end_y   = init_sample.y;   	
			}
			if(!data_increasing)
			{
				ArrayList reverse_array = new ArrayList();
				ArrayList reverse_relative_array = new ArrayList();
				
				init_list = (ArrayList)data_array.get(2);
				init_sample = (Sample)init_list.get(0);
				size = init_list.size();
				end_sample       = (Sample)init_list.get(size - 1);
				
				double ymin = init_sample.y;
				double ymax = end_sample.y;
				
				double[] position = new double[5];
				for(int i = 0; i < 5; i++)
				{
					ArrayList list         = (ArrayList)data_array.get(i);
					
					ArrayList reverse_list = new ArrayList();
					
					for(int j = list.size() - 1; j >= 0; j--)
					{
					    Sample sample = (Sample)list.get(j);
					    sample.y = (ymax - sample.y) + ymin;
					    reverse_list.add(sample);
					}
					reverse_array.add(reverse_list);
					
					list = (ArrayList)relative_data_array.get(i);
					reverse_list = new ArrayList();
					for(int j = list.size() - 1; j >= 0; j--)
					{
					    Sample sample = (Sample)list.get(j);
					    reverse_list.add(sample);
					}
					reverse_relative_array.add(reverse_list);
				}
				
				data_array.clear();
				relative_data_array.clear();
				for(int i = 0; i < 5; i++)
				{
					ArrayList list = (ArrayList)reverse_array.get(i);
					data_array.add(list);
					list = (ArrayList)reverse_relative_array.get(i);
					relative_data_array.add(list);
					
				}
			}
			
			if(!data_clipped)
			{
				minimum_y = seg_min;
				maximum_y = seg_max;
			}
			
			// End data segmentation.
			
			
			// Start graph.
			
			double max_xstep         = (xdim - (left_margin + right_margin)) / number_of_segments;
			int    xstep             = (int) (max_xstep * normal_xstep);
			int    graph_xdim        = xdim - (left_margin + right_margin) - (number_of_segments - 1) * xstep;
			
			double max_ystep         = (ydim - (top_margin + bottom_margin)) / number_of_segments;
			int    ystep             = (int) (max_ystep * normal_ystep);
			int    graph_ydim        = ydim - (top_margin + bottom_margin) - (number_of_segments - 1) * ystep;
			

			// So that graphs are not butted together.
			if (xstep == max_xstep && ystep == 0)
			{
				graph_xdim -= 25;
				x_remainder = 25;
			}	
			else
				x_remainder = 0;

			if(ystep == max_ystep && xstep == 0)
			{
			    graph_ydim -= 20;
			    y_remainder = 20;
			}
			else
				y_remainder = 0;
			
		
			double minimum_x = seg_xmin;
			double maximum_x = seg_xmax;
			
			
			if(!data_clipped)
			{
				minimum_y = seg_min;
				maximum_y = seg_max;
			}
			
			// Modify our bounding values if we're scaling data.
			// If we aren't, we are doing a divide by one.
			minimum_y /= scale_factor;
			maximum_y /= scale_factor;
			
			// Layout the isometric space.
			for (int i = 0; i < number_of_segments; i++)
			{
				int a1      = left_margin;
				int b1      = ydim - bottom_margin;
				int a2      = a1 + graph_xdim;
				int b2      = b1 - graph_ydim;
				int xaddend = i * xstep;
				int yaddend = i * ystep;
				a1         += xaddend;
				b1         -= yaddend;
				a2         += xaddend;
				b2         -= yaddend;
                
				double current_range    = b1 - b2;
				double current_position = b2;
			
				if(xstep != 0 && xstep != max_xstep && ystep != 0 && ystep != max_xstep  && i != (number_of_segments - 1))
				{
					double zero_y = Math.abs(minimum_y);
					zero_y /= maximum_y - minimum_y;
					zero_y *= graph_ydim;
					zero_y = (graph_ydim + y_remainder) - zero_y;
					zero_y += top_margin + (number_of_segments - 1) * ystep;
					zero_y -= yaddend;

					float[] dash ={ 2f, 0f, 2f };
					BasicStroke basic_stroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, dash, 2f);
					graphics_buffer.setStroke(basic_stroke);
					graphics_buffer.setColor(java.awt.Color.RED);
					graphics_buffer.drawLine((int) a1, (int) zero_y, (int) a1 + xstep, (int) zero_y - ystep);
					graphics_buffer.setStroke(new BasicStroke(2));	
				}
				
				graphics_buffer.setColor(java.awt.Color.BLACK);
			    graphics_buffer.setStroke(new BasicStroke(1));
			    current_position = a1;
			    String width_string;
			    double xrange = maximum_x - minimum_x;
			    if(relative_mode)
			    {
			        width_string = String.format("%.2f", maximum_x);
			    }    
			    else
			    	width_string = String.format("%.0f", global_ymin);
			    
			    	
			    int    string_width               = font_metrics.stringWidth(width_string);
			    int    number_of_units            = graph_xdim / (string_width + 6);  
		        double current_position_increment = graph_xdim;
		        current_position_increment        /= number_of_units;
		       
		        if(i == 0  || (xstep == max_xstep && ystep == 0))
		        {
		        	//Put down lines on the frontmost graph where we can hang location information.
		            graphics_buffer.drawLine((int) current_position, b1, (int) current_position, b1 + 10); 
		            for(int j = 0; j < number_of_units; j++)
		            {
			            current_position += current_position_increment;
			            graphics_buffer.drawLine((int) current_position, b1, (int) current_position, b1 + 10);
		            }
		            current_position         = b2;
	                current_range            = b1 - b2;
				    number_of_units          = (int) (current_range / (2 * (string_height)));
				    double current_increment = current_range;
				    current_increment       /= number_of_units;
				    graphics_buffer.setColor(Color.BLACK);
			    	graphics_buffer.setStroke(new BasicStroke(2));
			    	graphics_buffer.drawLine(a1, b1, a1, b2);
			    	
			    	graphics_buffer.setStroke(new BasicStroke(1));
			    	graphics_buffer.setColor(new Color(196, 196, 196));
			    	graphics_buffer.drawLine(a1, b1, a2, b1);
		            for(int j = 0; j < number_of_units; j++)
		            {
		            	graphics_buffer.drawLine(a1, (int)current_position, a1 - 10, (int)current_position);
			            current_position += current_increment;
		            }
		            graphics_buffer.drawLine(a1, (int)current_position, a1 - 10, (int)current_position);
		           
		           
		            if(ystep != 0  && (show_id || show_position))
		            {
		            	graphics_buffer.setColor(Color.BLACK);
		            	String line_id = new String("foo");
		            	String end_position_string = new String("foo");
		            	
		            	if(!reverse_view)
		            		end_position_string = String.format("%.2f", end_position[i]);
		            	else
		            		end_position_string = String.format("%.2f", end_position[4 - i]);
		            	if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
		            	{
		            		end_position_string = String.format("%.2f", end_position[i]);
		            		if(start_flight_line == stop_flight_line)
		            		{
        		    	        line_id = new String(start_flight_line + ":" + i);
        		    	        
		            		}
        		    	    else
        		    	    {
        		    		    line_id = new String(start_flight_line + "/" + stop_flight_line + ":" + i);
        		    	    }
		            	}
		            	else
		            	{
		            	    end_position_string = String.format("%.2f", end_position[4 - i]);
		            		if(start_flight_line == stop_flight_line)
	        		    	    line_id = new String(start_flight_line + ":" + (4 - i));
	        		    	else
	        		    		line_id = new String(start_flight_line + "/" + stop_flight_line + ":" + (4 - i));
		            	}
		            	if(show_id)
					        graphics_buffer.drawString(line_id, a2 + 10, (int) current_position + ( 3 * string_height / 4));
		            	else if(show_position)
		            		graphics_buffer.drawString(end_position_string, a2 + 10, (int) current_position + ( 3 * string_height / 4));
					    graphics_buffer.setColor(new Color(196, 196, 196));
		            }
		            if((xstep == 0 && ystep == 0) || (xstep == 0 && ystep == max_ystep) || (xstep == max_xstep && ystep == 0) || (xstep == max_xstep && ystep == max_ystep))
		            {
		            	current_position = b2;
			    	    for(int j = 0; j < number_of_units; j++)
		                {
		    			    graphics_buffer.drawLine(a1, (int)current_position, a2, (int)current_position);
			                current_position += current_increment;
		                }
			    	    current_position  = a1;
			    	    number_of_units   = (int)(graph_xdim / (string_width + 6));  
			    	    current_increment = graph_xdim;
			    	    current_increment /= number_of_units;
			    	    // Creating grid on rear of data space on frontmost graph panel.
			    	    for(int j = 0; j < number_of_units; j++)
		                {
		    			    graphics_buffer.drawLine((int)current_position, b1, (int)current_position, b2);
		    			    current_position += current_increment;
		    			}	
			    	    graphics_buffer.drawLine((int)a2, b1, (int)a2, b2);
			    	    
			    	    graphics_buffer.drawLine(a1, b1, a2, b1); 
		            }
		            // If plots directly overlap, we only need one set of axes.
					if (ystep == 0 && xstep == 0)
						break;
		        }
		        else
		        {
		        	if(xstep == max_xstep && ystep == 0)
		        	{
		        	    //Put down lines on all the graphs where we can hang location information since they
		        		// are all laid out in a line.
		                graphics_buffer.drawLine((int) current_position, b1, (int) current_position, b1 + 10); 
		                for(int j = 0; j < number_of_units; j++)
			            {
				            current_position += current_position_increment;
				            graphics_buffer.drawLine((int) current_position, b1, (int) current_position, b1 + 10);
			            }
		            }
		        	if(!(ystep == 0 && xstep == 0))
		        	{
		        		graphics_buffer.drawLine((int) current_position, b1 + y_remainder, (int) current_position - xstep, b1 + ystep);
		        	    for(int j = 0; j < number_of_units; j++)
		        	    {
		        	    	current_position += current_position_increment;
		        		    graphics_buffer.drawLine((int) current_position, b1 + y_remainder, (int) current_position - xstep, b1 + ystep);
		        		    // At the end of a graph, put down a line where we can hang a line id or location information.
		        		    // It also helps define the isometric space.
		        		    if(j == number_of_units - 1)
		        		    {
		        		    	graphics_buffer.drawLine((int) current_position, b1, (int) current_position, b1 + 10); 
		        		    	String line_id = new String("foo");
		        		    	String line_slope = new String("foo");
		        		    	String end_position_string = new String("foo");
		        		    	if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))	
		        		    	{ 
		        		    		end_position_string = String.format("%.2f", end_position[i]);
		        		    		if(start_flight_line == stop_flight_line)
		        		    	        line_id = new String(start_flight_line + ":" + i);
		        		    	    else
		        		    		    line_id = new String(start_flight_line + "/" + stop_flight_line + ":" + i);
		        		    	}
		        		    	else
		        		    	{
		        		    		end_position_string = String.format("%.2f", end_position[4 - i]);
		        		    		if(start_flight_line == stop_flight_line)
		        		    	        line_id = new String(start_flight_line + ":" + (4 - i));
		        		    	    else
		        		    		    line_id = new String(start_flight_line + "/" + stop_flight_line + ":" + (4 - i));
		        		    	}
								if(show_id)
								    graphics_buffer.drawString(line_id,  (int) current_position + 10, b1 + 10);
								else if(show_position)
									graphics_buffer.drawString(end_position_string,  (int) current_position + 10, b1 + 10);
		        		    }
		        	    }
		            }
		        	
		        	if(xstep == 0 && ystep == max_ystep)
		        	{
		        		//Put down lines where we can hang intensity information since graphs are laid out
		        		//in a column.
		        		current_position         = b2;
		                current_range            = b1 - b2;
					    number_of_units          = (int) (current_range / (2 * (string_height)));
					    double current_increment = current_range;
					    current_increment       /= number_of_units;
					    graphics_buffer.setColor(Color.BLACK);
				    	graphics_buffer.setStroke(new BasicStroke(2));
				    	graphics_buffer.drawLine(a1, b1, a1, b2);
				    	
				    	graphics_buffer.setStroke(new BasicStroke(1));
				    	graphics_buffer.setColor(new Color(196, 196, 196));
				    	graphics_buffer.drawLine(a1, b1, a2, b1);
			            for(int j = 0; j < number_of_units; j++)
			            {
			            	graphics_buffer.drawLine(a1, (int)current_position, a1 - 10, (int)current_position);
				            current_position += current_increment;
			            }
			            graphics_buffer.drawLine(a1, (int)current_position, a1 - 10, (int)current_position);
		        	}
		        	
		        	current_position         = b2;
	                current_range            = b1 - b2;
				    number_of_units          = (int) (current_range / (2 * (string_height)));
				    double current_increment = current_range;
				    current_increment       /= number_of_units;
				    if(ystep != 0)
			    	{
			    		graphics_buffer.setColor(Color.BLACK);
			    		graphics_buffer.setStroke(new BasicStroke(2));
			    		graphics_buffer.drawLine(a1, b1, a1, b2);
				    	graphics_buffer.setColor(new Color(196, 196, 196));
				    	graphics_buffer.setStroke(new BasicStroke(1));
			    		for(int j = 0; j < number_of_units; j++)
			            {
			    			graphics_buffer.drawLine(a1, (int)current_position, a1 - xstep, (int)current_position + ystep);
				            current_position += current_increment;
			            }
			    		graphics_buffer.drawLine(a1, (int)current_position, a1 - xstep, (int)current_position + ystep);
			    	}
			    	if(ystep == max_ystep)
			    	{
			    		graphics_buffer.setColor(new Color(196, 196, 196));
			    		graphics_buffer.setStroke(new BasicStroke(1));
			    		current_position = b2;
			    	    for(int j = 0; j < number_of_units; j++)
		                {
		    			    graphics_buffer.drawLine(a1, (int)current_position, a2, (int)current_position);
			                current_position += current_increment;
		                }	
			    	}
			    	graphics_buffer.setColor(new Color(196, 196, 196));
		    	    graphics_buffer.setStroke(new BasicStroke(1));
		    	    graphics_buffer.drawLine(a1, b1, a2, b1);
		    	    
		    	    if(xstep == max_xstep && ystep == 0)
		    	        graphics_buffer.drawLine(a1, b1, a1, b2);
		    	    if(xstep > (max_xstep - 2)  && ystep > (max_ystep - 2))
			    	{
			    		graphics_buffer.drawLine(a1, b1, a2, b1); 
				    	graphics_buffer.drawLine(a1, b2, a2, b2);
				    	graphics_buffer.drawLine(a2, b1, a2, b2);   
				    	current_position = b2;
				    	for(int j = 0; j < number_of_units; j++)
			            {
			    			graphics_buffer.drawLine(a1, (int)current_position, a2, (int)current_position);
				            current_position += current_increment;
			            }
				    	current_position  = a1;
				    	number_of_units   = (int)(graph_xdim / (string_width + 6));  
				    	current_increment = graph_xdim;
				    	current_increment /= number_of_units;
				    	for(int j = 0; j < number_of_units; j++)
			            {
			    			graphics_buffer.drawLine((int)current_position, b1, (int)current_position, b2);
			    			current_position += current_increment;
			    	    }	
			    	}
				     
				    if(i == number_of_segments - 1 && (ystep != max_ystep || xstep != max_xstep))
				    {
				    	if(xstep == 0 && ystep == max_ystep)
				    		graphics_buffer.setColor(Color.BLACK);
				    	else
				    	graphics_buffer.setColor(new Color(196, 196, 196));
			    	    graphics_buffer.setStroke(new BasicStroke(1));
			    	    graphics_buffer.drawLine(a1, b1, a2, b1);
				    	if(!(xstep == max_xstep && ystep == 0) || xstep == 0)
				    	{
				    	    graphics_buffer.drawLine(a1, b1, a2, b1); 
				    	    graphics_buffer.drawLine(a1, b2, a2, b2);
				    	    graphics_buffer.drawLine(a2, b1, a2, b2);
				    	    
				    	    current_position = b2;
				    	    for(int j = 0; j < number_of_units; j++)
			                {
			    			    graphics_buffer.drawLine(a1, (int)current_position, a2, (int)current_position);
				                current_position += current_increment;
			                }
				    	    current_position  = a1;
				    	    number_of_units   = (int)(graph_xdim / (string_width + 6));  
				    	    current_increment = graph_xdim;
				    	    current_increment /= number_of_units;
				    	    
				    	    // Creating grid on rear of data space.
				    	    for(int j = 0; j < number_of_units; j++)
			                {
			    			    graphics_buffer.drawLine((int)current_position, b1, (int)current_position, b2);
			    			    current_position += current_increment;
			    			}
				    	}
			        }
		        }
			}
			
			ArrayList plot_data = new ArrayList();
			double smooth_maximum_y = -Double.MAX_VALUE;
			double smooth_minimum_y = Double.MAX_VALUE;
			
			for(int i = 0; i < number_of_segments; i++)
			{
				int a1      = left_margin;
				int b1      = ydim - bottom_margin;
				int a2      = a1 + graph_xdim;
				int b2      = b1 - graph_ydim;
				int xaddend = i * xstep;
				int yaddend = i * ystep;
				a1         += xaddend;
				b1         -= yaddend;
				a2         += xaddend;
				b2         -= yaddend;
                
				double current_range           = b1 - b2;
				double current_position        = b2;
				double current_value           = maximum_y;
				double current_intensity_range = maximum_y - minimum_y;
				
				ArrayList data_list = (ArrayList)data_array.get(i);
				ArrayList relative_data_list = (ArrayList)relative_data_array.get(i);
				
				if(data_list.size() == 0)
				{
					System.out.println("No samples in sample space.");
					return;
				}
				ArrayList plot_list = new ArrayList();
				
				for(int j = 0; j < data_list.size(); j++)
				{
					Sample sample = (Sample)data_list.get(j);
					Point2D.Double point = new Point2D.Double();
					point.x              = sample.y;
					point.y              = sample.intensity;
					point.y *= scale_factor;
					if (point.y < minimum_y)
						point.y = minimum_y;
					else if (point.y > maximum_y)
						point.y = maximum_y;
					plot_list.add(point);
				}
				plot_data.add(plot_list);
			}
			

			Polygon[] polygon               = new Polygon[number_of_segments];
			boolean[] polygon_zero_crossing = new boolean[number_of_segments];
			double[]  polygon_min           = new double[number_of_segments];
			double[]  polygon_max           = new double[number_of_segments];
			
			ArrayList data_list;
			ArrayList relative_data_list;
			for (int i = 0; i < number_of_segments; i++)
			{
				// Get the data we used to generate the graph so we can append it later.
				if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
				{
				    data_list = (ArrayList)data_array.get(i);
				    relative_data_list = (ArrayList)relative_data_array.get(i);
				}
				else
				{
					data_list = (ArrayList)data_array.get(4 - i);
				    relative_data_list = (ArrayList)relative_data_array.get(4 - i);    	
				}
				
				Sample    previous_sample = (Sample)data_list.get(0);
				polygon_zero_crossing[i]  = false;
				
				for(int j = 1; j < data_list.size(); j++)
				{
				    Sample current_sample = (Sample)data_list.get(j);
				    if((previous_sample.intensity <= 0 && current_sample.intensity >= 0) ||
				    (previous_sample.intensity >= 0 && current_sample.intensity <= 0))
				    {
				    	polygon_zero_crossing[i]  = true;	
				    	break;
				    }
				}
				
				int a1 = left_margin;
				int b1 = ydim - bottom_margin;

				int a2 = a1 + graph_xdim;
				int b2 = b1 - graph_ydim;

				int xaddend = i * xstep;
				int yaddend = i * ystep;

				a1 += xaddend;
				b1 -= yaddend;

				a2 += xaddend;
				b2 -= yaddend;

				ArrayList segment;
				if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
				    segment = (ArrayList)plot_data.get(i);
				else
					segment = (ArrayList)plot_data.get(4 - i);
		
				int n   = segment.size() + 3;
				int[] x = new int[n];
				int[] y = new int[n];
				
				int m         = 0;
                double yrange = maximum_y - minimum_y;
                double xrange = maximum_x - minimum_x;
                
                double this_minimum_y = maximum_y;
                double this_maximum_y = minimum_y;
                double init_point     = 0;
                
                double zero_y = Math.abs(minimum_y);
				zero_y /= yrange;
				zero_y *= graph_ydim;
				zero_y = graph_ydim - zero_y;
				zero_y += top_margin + (number_of_segments - 1) * ystep;
				zero_y -= yaddend;
				double previous_y = 0;
				for(int k = 0; k < segment.size(); k++)
				{
					Point2D.Double point = (Point2D.Double) segment.get(k);
					double current_x = point.getX();
					current_x -= minimum_x;
					current_x /= xrange;
					current_x *= graph_xdim;
					current_x += left_margin;
					current_x += xaddend;

					double current_y = point.getY();
					if(current_y < this_minimum_y)
						this_minimum_y = current_y;	
					if(current_y > this_maximum_y)
						this_maximum_y = current_y;
					current_y -= minimum_y;
					current_y /= yrange;
					current_y *= graph_ydim;
					current_y = (graph_ydim + y_remainder) - current_y;
					current_y += top_margin + (number_of_segments - 1) * ystep;
					current_y -= yaddend;
					
					if(k == 0)
						init_point = current_y;

					x[m] = (int) current_x;
					y[m] = (int) current_y;

					m++;
					
					// Associate this point with sample information.
					// Where endpoints overlap, there should be multiple samples.
					ArrayList pixel_data_list = pixel_data[(int) current_y][(int) current_x];
					Sample sample;
					
					if(k < relative_data_list.size())
					    sample = (Sample)relative_data_list.get(k);
					else
						sample = (Sample)relative_data_list.get(relative_data_list.size() - 1);
					
					if (pixel_data_list.size() == 0)
					{
						pixel_data_list.add(start_flight_line);
						
						if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
						{
							pixel_data_list.add(4 - i);   	
						}
						else
						{
						    pixel_data_list.add(i);
						}
						pixel_data_list.add(sample);
					} 
					else
					{
						// Saving previous entries in cases where
						// two points in the data map to one point in graph.
						// Throwing out entries that were generated
						// by a neighboring pixel.  For now we are just
						// pulling out the most recent entry and the
						// user has to check pixel information panel
						// to see if they are getting information
						// about the curve they want.
						
						ArrayList new_pixel_list = new ArrayList();
						new_pixel_list.add(start_flight_line);
						
						
						if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
						{
							new_pixel_list.add(4 - i);   	
						}
						else
						{
							new_pixel_list.add(i);
						}
						
						new_pixel_list.add(sample);
						for (int p = 0; p < pixel_data_list.size(); p += 3)
						{
							int line = (int) pixel_data_list.get(p);
							int sensor = (int) pixel_data_list.get(p + 1);

							if(start_flight_line != line || i != sensor)
							{
								previous_sample = (Sample) pixel_data_list.get(p + 2);
								new_pixel_list.add(line);
								new_pixel_list.add(sensor);
								new_pixel_list.add(previous_sample);
							}
						}
						pixel_data[(int) current_y][(int) current_x] = new_pixel_list;
					}
					
					// Assigning neighbor pixels if they are unassigned so that
					// it isn't hard for the mouse to find an assigned pixel.
					ArrayList pixel_list = pixel_data[(int) current_y - 1][(int) current_x];
					int j = 4 - i;
					if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
						j = i;
							
					if (pixel_list.size() == 0)
					{
						pixel_list.add(start_flight_line);
						
						pixel_list.add(j);
						pixel_list.add(sample);
					}

					pixel_list = pixel_data[(int) current_y - 1][(int) current_x - 1];
					if (pixel_list.size() == 0)
					{
						pixel_list.add(start_flight_line);
						pixel_list.add(j);
						pixel_list.add(sample);
					}

					pixel_list = pixel_data[(int) current_y - 1][(int) current_x + 1];
					if (pixel_list.size() == 0)
					{
						pixel_list.add(start_flight_line);
						pixel_list.add(j);
						pixel_list.add(sample);
					}

					pixel_list = pixel_data[(int) current_y][(int) current_x - 1];
					if (pixel_list.size() == 0)
					{
						pixel_list.add(start_flight_line);
						pixel_list.add(j);
						pixel_list.add(sample);
					}

					pixel_list = pixel_data[(int) current_y][(int) current_x + 1];
					if (pixel_list.size() == 0)
					{
						pixel_list.add(start_flight_line);
						pixel_list.add(j);
						pixel_list.add(sample);
					}

					pixel_list = pixel_data[(int) current_y + 1][(int) current_x];
					if (pixel_list.size() == 0)
					{
						pixel_list.add(start_flight_line);
						pixel_list.add(j);
						pixel_list.add(sample);
					}

					pixel_list = pixel_data[(int) current_y + 1][(int) current_x - 1];
					if (pixel_list.size() == 0)
					{
						pixel_list.add(start_flight_line);
						pixel_list.add(j);
						pixel_list.add(sample);
					}

					pixel_list = pixel_data[(int) current_y + 1][(int) current_x + 1];
					if (pixel_list.size() == 0)
					{
						pixel_list.add(start_flight_line);
						pixel_list.add(j);
						pixel_list.add(sample);
					}
				}
				
				double local_min = this_minimum_y;
				local_min -= minimum_y;
				local_min /= yrange;
				local_min *= graph_ydim;
				local_min = (graph_ydim + y_remainder) - local_min;
				local_min += top_margin + (number_of_segments - 1) * ystep;
				local_min -= yaddend;
				
				double local_max = this_maximum_y;
				local_max -= minimum_y;
				local_max /= yrange;
				local_max *= graph_ydim;
				local_max  = (graph_ydim + y_remainder) - local_max;
				local_max += top_margin + (number_of_segments - 1) * ystep;
				local_max -= yaddend;
				
				x[m] = a2;
				
				// If we want to set the base to the local min.
				// y[m] = (int)local_min;
				
				// Setting base to global_min.
				y[m] = b1;
				m++;

				x[m] = a1;
				
				// If we want to set the base to the local min.
				// y[m] = (int)local_min;
				
				// Setting base to global_min.
				y[m] = b1;
				m++;
				
				x[m] = a1;
				y[m] = (int)init_point;
				
				
				// Color y axis.
				
				if(reverse_view)
				{
					if(visible[number_of_segments - 1 - i])
					{
					    graphics_buffer.setColor(fill_color[number_of_segments - 1 - i]);
				        graphics_buffer.setStroke(new BasicStroke(3)); 
				        graphics_buffer.drawLine(a1, (int)local_min, a1, (int)local_max);
				        
					}	
				}
				else
				{
					if(visible[i])
					{
				    	graphics_buffer.setColor(fill_color[i]);
			        	graphics_buffer.setStroke(new BasicStroke(3)); 
			        	graphics_buffer.drawLine(a1, (int)local_min, a1, (int)local_max);
					}
				}
			    
			    polygon_min[i] = local_min;
			    polygon_max[i] = local_max;
				java.awt.Polygon sensor_polygon = new Polygon(x, y, n);
				polygon[i]  = sensor_polygon;
			}
			
			double xrange = maximum_x - minimum_x;
			double yrange = maximum_y - minimum_y;
			
			for (int i = number_of_segments - 1; i >= 0; i--)
			{
				int a1 = left_margin;
				int b1 = ydim - bottom_margin;

				int a2 = a1 + graph_xdim;
				int b2 = b1 - graph_ydim;

				int xaddend = i * xstep;
				int yaddend = i * ystep;

				a1 += xaddend;
				b1 -= yaddend;

				a2 += xaddend;
				b2 -= yaddend;
       
				
				// Draw polygon.
				if(reverse_view)
				{
					graphics_buffer.setColor(fill_color[4 - i]);
					if(!transparent[4 - i])
				        graphics_buffer.fillPolygon(polygon[i]);
					graphics_buffer.setStroke(new BasicStroke(2));
				    graphics_buffer.setColor(java.awt.Color.BLACK);
				    if(visible[4 - i])
				        graphics_buffer.drawPolygon(polygon[i]);
				    
				    // Anchor polygon to isometric grid.
				    graphics_buffer.setColor(Color.DARK_GRAY);
				    graphics_buffer.setStroke(new BasicStroke(1)); 
				    if(visible[4 - i])
				        graphics_buffer.drawLine(a2, (int)polygon_min[i], a2, b1);
				}
				else
				{
					graphics_buffer.setColor(fill_color[i]);
					if(!transparent[i])
				        graphics_buffer.fillPolygon(polygon[i]);
				    graphics_buffer.setStroke(new BasicStroke(2));
				    graphics_buffer.setColor(java.awt.Color.BLACK);
				    if(visible[i])
				        graphics_buffer.drawPolygon(polygon[i]);
				    
				    // Anchor polygon to isometric grid.
				    graphics_buffer.setColor(Color.DARK_GRAY);
				    graphics_buffer.setStroke(new BasicStroke(1)); 
				    if(visible[i])
				        graphics_buffer.drawLine(a2, (int)polygon_min[i], a2, b1);
				}
				
				if(reverse_view)
				{
					if(visible[number_of_segments - 1 - i])
					{
						ArrayList plot_list = (ArrayList) plot_data.get(number_of_segments - 1 - i);
						Point2D.Double first = (Point2D.Double) plot_list.get(0);
						int plot_length = plot_list.size();
						Point2D.Double last = (Point2D.Double) plot_list.get(plot_length - 1);
						double x1 = first.getX();
						double x2 = last.getX();
						
						x1 -= minimum_x;
						x1 /= xrange;
						x1 *= graph_xdim;
						x1 += left_margin;
						x1 += xaddend;

						x2 -= minimum_x;
						x2 /= xrange;
						x2 *= graph_xdim;
						x2 += left_margin;
						x2 += xaddend;
						
						double zero_y = Math.abs(minimum_y);
						zero_y /= yrange;
						zero_y *= graph_ydim;
						zero_y = (graph_ydim + y_remainder) - zero_y;
						zero_y += top_margin + (number_of_segments - 1) * ystep;
						zero_y -= yaddend;

						float[] dash ={ 2f, 0f, 2f };
						BasicStroke basic_stroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, dash, 2f);
						graphics_buffer.setStroke(basic_stroke);
						graphics_buffer.setColor(java.awt.Color.RED);
						graphics_buffer.drawLine((int) x1, (int) zero_y, (int) x2, (int) zero_y);
						graphics_buffer.setStroke(new BasicStroke(2));
							
					}	
				}
				else
				{
					if(visible[i])
					{
						ArrayList plot_list = (ArrayList) plot_data.get(i);
						Point2D.Double first = (Point2D.Double) plot_list.get(0);
						int plot_length = plot_list.size();
						Point2D.Double last = (Point2D.Double) plot_list.get(plot_length - 1);
						double x1 = first.getX();
						double x2 = last.getX();
						
						x1 -= minimum_x;
						x1 /= xrange;
						x1 *= graph_xdim;
						x1 += left_margin;
						x1 += xaddend;

						x2 -= minimum_x;
						x2 /= xrange;
						x2 *= graph_xdim;
						x2 += left_margin;
						x2 += xaddend;
						
						double zero_y = Math.abs(minimum_y);
						zero_y /= yrange;
						zero_y *= graph_ydim;
						zero_y = (graph_ydim + y_remainder) - zero_y;
						zero_y += top_margin + (number_of_segments - 1) * ystep;
						zero_y -= yaddend;

						float[] dash ={ 2f, 0f, 2f };
						BasicStroke basic_stroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, dash, 2f);
						graphics_buffer.setStroke(basic_stroke);
						graphics_buffer.setColor(java.awt.Color.RED);
						graphics_buffer.drawLine((int) x1, (int) zero_y, (int) x2, (int) zero_y);
						graphics_buffer.setStroke(new BasicStroke(2));	
					}
				}
			}
			
			// Add numbers and labels.
			graphics_buffer.setColor(java.awt.Color.BLACK);
			double current_value    = data_offset;
			double current_position = left_margin;
			int a1                  = left_margin;
			int b1                  = ydim - bottom_margin;
			int a2                  = a1 + graph_xdim;
			int b2                  = b1 - graph_ydim;
			
			for(int i = 0; i < number_of_segments; i++)
		    {
			    a1 = left_margin;
			    b1 = ydim - bottom_margin;

			    a2 = a1 + graph_xdim;
			    b2 = b1 - graph_ydim;

			    int xaddend = i * xstep;
			    int yaddend = i * ystep;

			    a1 += xaddend;
			    b1 -= yaddend;

			    a2 += xaddend;
			    b2 -= yaddend;
			    
			    //Create a top on the frame around each graph to help evaluate relative y dimensions accurately.
			    if(raster_overlay)
			    {
			        if(i != number_of_segments - 1 && visible[i])
			        {
			    	    //Side--seems like it just gets in the way.
		                //graphics_buffer.drawLine(a1, b1, a2, b1); 
			    	    if(ystep != 0)
			    	    {
			    	    	graphics_buffer.setColor(new Color(196, 196, 196));
				    	    graphics_buffer.setStroke(new BasicStroke(1));
	    	                graphics_buffer.drawLine(a1, b2, a2, b2);
	    	                graphics_buffer.drawLine(a2, b2, a2 + xstep, b2 - ystep);
	    	                graphics_buffer.setColor(Color.BLACK);
			    	    }
			        }
			    }
			    
			    if(xstep != 0 && xstep != max_xstep && ystep != 0 && ystep != max_xstep  && i != (number_of_segments - 1))
				{
					double zero_y = Math.abs(minimum_y);
					zero_y /= maximum_y - minimum_y;
					zero_y *= graph_ydim;
					zero_y = (graph_ydim + y_remainder) - zero_y;
					zero_y += top_margin + (number_of_segments - 1) * ystep;
					zero_y -= yaddend;

					float[] dash ={ 2f, 0f, 2f };
					BasicStroke basic_stroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, dash, 2f);
					graphics_buffer.setStroke(basic_stroke);
					graphics_buffer.setColor(java.awt.Color.RED);
					graphics_buffer.drawLine((int) a2, (int) zero_y, (int) a2 + xstep, (int) zero_y - ystep);
					graphics_buffer.setStroke(new BasicStroke(1));
					graphics_buffer.setColor(new Color(196, 196, 196));
					graphics_buffer.drawLine((int) a2, (int) zero_y, (int) a2, (int) b1);
					graphics_buffer.setColor(Color.BLACK);
				}
			 
			    current_value    = 0;
			    String width_string;
			   
			    xrange = maximum_x - minimum_x;
			    if(relative_mode)
			    {
			    	width_string = String.format("%.2f", maximum_x);
			    }    
			    else
			    	width_string = String.format("%.0f", global_ymin);
			    
			    	
			    int string_width                  = font_metrics.stringWidth(width_string);
			    int    number_of_units            = (int) (graph_xdim / (string_width + 6));  
		        double current_position_increment = graph_xdim;
		        current_position_increment       /= number_of_units;
		        
		        String position_string;
		        xrange = maximum_x - minimum_x;
		        if(relative_mode)
		        {
		        	position_string = String.format("%.2f", relative_start_y);
		            current_value = relative_start_y;
		        }
		        else
		        {
		            position_string = String.format("%.0f", relative_start_y + global_ymin);	
		        	current_value = relative_start_y + global_ymin;
		        }
		        current_position               = a1;
		        double current_value_increment = xrange;
		        current_value_increment       /= number_of_units;	
		        
		        if(i == 0 || (xstep == max_xstep && ystep == 0) )
		        {
		        	// Hanging locations on frontmost graph or all the graphs if they are laid out in a row.
		        	graphics_buffer.drawString(position_string, (int) current_position - string_width / 2, ydim + string_height + 12 - bottom_margin);
		        	current_position += current_position_increment;
		        	current_value += current_value_increment;
		            for(int j = 0; j < number_of_units; j++)
		            {
		            	if(relative_mode)
		            	    position_string = String.format("%.2f", current_value);
		            	else
		            		position_string = String.format("%.0f", current_value);
		            	graphics_buffer.drawString(position_string, (int) current_position - string_width / 2, ydim + string_height + 12 - bottom_margin);
		            	current_position += current_position_increment;
		            	current_value += current_value_increment;
		            }
		        }
		        
		        if(i == 0 || (xstep == 0 && ystep == max_ystep))
		        {
		        	double current_intensity_range = maximum_y - minimum_y;
				    double current_range     = b1 - b2;
				    number_of_units          = (int) (current_range / (2 * string_height));
				    double current_increment = current_range; 
				    current_increment       /= number_of_units;
				    current_value_increment  = current_intensity_range;
				    current_value_increment /= number_of_units;
				    current_position = b2;
				    current_value = maximum_y;
				    String intensity_string;
				    for(int j = 0; j < number_of_units; j++)
		            {
				    	intensity_string = String.format("%.1f", current_value);
			            string_width     = font_metrics.stringWidth(intensity_string);
			            graphics_buffer.drawString(intensity_string, a1 - (string_width + 14), (int) (current_position + string_height / 2));
			            current_position += current_increment;
			            current_value    -= current_value_increment;
		            }
				    intensity_string = String.format("%.1f", current_value);
		            string_width     = font_metrics.stringWidth(intensity_string);
		            graphics_buffer.drawString(intensity_string, a1 - (string_width + 14), (int) (current_position + string_height / 2));	
		        }
		    }
			
			String position_string = new String("meters");
			int string_width = font_metrics.stringWidth(position_string);
			graphics_buffer.drawString(position_string, left_margin + (xdim - right_margin - left_margin) / 2 - string_width / 2, ydim - bottom_margin / 6);
			
		    String intensity_string = new String("nT");
			string_width = font_metrics.stringWidth(intensity_string);
			graphics_buffer.drawString(intensity_string, string_width / 2, top_margin + (ydim - top_margin - bottom_margin) / 2);
			
			if(!graph_label.equals(""))
			{
				string_width = font_metrics.stringWidth(graph_label); 
				graphics_buffer.drawString(graph_label, xdim / 2 - string_width / 2, top_margin - 5);
			}
			
			if(color_key)
			{
				graphics_buffer.setStroke(new BasicStroke(3));
				for(int i = 0; i < 5; i++)
				{
					String sensor_id = new String("foo");
					if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
					{
					    if(stop_flight_line != start_flight_line)
						    sensor_id = new String(start_flight_line + "/" + stop_flight_line + ":" + i);
					    else
						    sensor_id = new String(start_flight_line + ":" + i);
					}
					else
					{
						if(stop_flight_line != start_flight_line)
						    sensor_id = new String(start_flight_line + "/" + stop_flight_line + ":" + (4 - i));
					    else
						    sensor_id = new String(start_flight_line + ":" + (4 - i));	
					}
					string_width = font_metrics.stringWidth(sensor_id);
					int x = xdim - (3 * string_width + 10);
					//int y = ydim - (2 * i * string_height) - bottom_margin;
					int y = ydim - (2 * i * string_height) - 5;
					graphics_buffer.setColor(Color.BLACK);
					graphics_buffer.drawString(sensor_id, x, y);
					if((!reverse_view && flight_line_odd) || (reverse_view && !flight_line_odd))
					    graphics_buffer.setColor(fill_color[4 - i]);
					else
						graphics_buffer.setColor(fill_color[i]);
					graphics_buffer.fillRect(x + 2 * string_width, y - string_height, string_width, string_height);
				}
			}
			
			if(startpoint_set)
			{
				graphics_buffer.setColor(Color.BLACK);	
				graphics_buffer.drawOval((int)startpoint_x_position - 3, (int)startpoint_y_position - 3, 7, 7);
				graphics_buffer.fillOval((int)startpoint_x_position - 3, (int)startpoint_y_position - 3, 7, 7);
				graphics_buffer.setColor(Color.GREEN);
				graphics_buffer.drawOval((int)startpoint_x_position - 2, (int)startpoint_y_position - 2, 5, 5);
				graphics_buffer.fillOval((int)startpoint_x_position - 2, (int)startpoint_y_position - 2, 5, 5);
				graphics_buffer.setColor(Color.BLACK);	
			}
			if(midpoint_set)
			{
				graphics_buffer.setColor(Color.BLACK);	
				graphics_buffer.drawOval((int)midpoint_x_position - 3, (int)midpoint_y_position - 3, 7, 7);
				graphics_buffer.fillOval((int)midpoint_x_position - 3, (int)midpoint_y_position - 3, 7, 7);
				graphics_buffer.setColor(Color.GREEN);
				graphics_buffer.drawOval((int)midpoint_x_position - 2, (int)midpoint_y_position - 2, 5, 5);
				graphics_buffer.fillOval((int)midpoint_x_position - 2, (int)midpoint_y_position - 2, 5, 5);
				graphics_buffer.setColor(Color.BLACK);	
			}
			if(endpoint_set)
			{
				graphics_buffer.setColor(Color.BLACK);	
				graphics_buffer.drawOval((int)endpoint_x_position - 3, (int)endpoint_y_position - 3, 7, 7);
				graphics_buffer.fillOval((int)endpoint_x_position - 3, (int)endpoint_y_position - 3, 7, 7);
				graphics_buffer.setColor(Color.GREEN);
				graphics_buffer.drawOval((int)endpoint_x_position - 2, (int)endpoint_y_position - 2, 5, 5);
				graphics_buffer.fillOval((int)endpoint_x_position - 2, (int)endpoint_y_position - 2, 5, 5);
				graphics_buffer.setColor(Color.BLACK);		
			}
			if(append_data)
			{
				graphics_buffer.setColor(Color.BLACK);
				graphics_buffer.drawOval((int)append_x_position - 3, (int)append_y_position - 3, 7, 7);
				graphics_buffer.fillOval((int)append_x_position - 3, (int)append_y_position - 3, 7, 7);
				graphics_buffer.setColor(Color.RED);
				graphics_buffer.drawOval((int)append_x_position - 2, (int)append_y_position - 2, 5, 5);
				graphics_buffer.fillOval((int)append_x_position - 2, (int)append_y_position - 2, 5, 5);
				graphics_buffer.setColor(Color.BLACK);
				
				
				int current_y = string_height + 2;
				int current_x = 2;
				String information_string = new String("  Line:            " + append_line);
				graphics_buffer.drawString(information_string, current_x, current_y);
				
				current_y += string_height + 2;
				information_string = new String("  Sensor:       " + append_sensor);
				graphics_buffer.drawString(information_string, current_x, current_y);
				
				current_y += string_height + 2;
				
				information_string = new String("  Intensity:      " + String.format("%.2f", append_intensity) + "\n");	
				graphics_buffer.drawString(information_string, current_x, current_y);
				
				current_y           += string_height + 2;
				String number_string = String.format("%.2f", append_x);
				information_string   = new String("  Relative x:   " + number_string);
				graphics_buffer.drawString(information_string, current_x, current_y);
				
				current_y           += string_height + 2;
				number_string        = String.format("%.2f", append_y);
				information_string   = new String("  Relative y:   " + number_string);
				graphics_buffer.drawString(information_string, current_x, current_y);
				
				current_y           += string_height + 2;
				number_string = String.format("%.2f", append_x_abs);
				information_string   = new String("  Absolute x:  " + number_string);
				graphics_buffer.drawString(information_string, current_x, current_y);
				
				current_y           += string_height + 2;
				number_string        = String.format("%.2f", append_y_abs);
				information_string   = new String("  Absolute y:  " + number_string);
				graphics_buffer.drawString(information_string, current_x, current_y);
				
				current_y           += string_height + 2;
				information_string   = new String("  Smoothing: " + smooth);
				graphics_buffer.drawString(information_string, current_x, current_y);
			}
			g.drawImage(buffered_image, 0, 0, null);
			
			// Restore our bounding values.
			// This is important if we're doing any clipping so we save the right
			// information to the config file--otherwise it gets reinitialized
			// every time we do a repaint() and we don't have to worry.
			// If we weren't doing any scaling, it's a multiply by one.
			minimum_y *= scale_factor;
			maximum_y *= scale_factor;
		
			location_canvas.repaint();
		}
	}
	
	// End PlotCanvas
	
	class DataScrollbarHandler implements AdjustmentListener
	{
		public void adjustmentValueChanged(AdjustmentEvent event)
		{
			if(object_button_changing == false && location_slider_changing == false && range_button_changing == false)
			{
				scrollbar_changing = true;
				if (data_scrollbar.getValueIsAdjusting() == false)
				{
					JScrollBar scrollbar    = (JScrollBar) event.getSource();
					
					// Calculate the new offset.
					double normal_position  = (double) event.getValue();
					normal_position         /= scrollbar_resolution;
					double normal_start     = normal_position - data_range / 2;
					if(normal_start < 0)
						normal_start = 0;
					double normal_stop      = normal_start + data_range; 
					if(normal_stop > 1)
					{
						normal_stop = 1;
						normal_start = 1 - data_range;
					}
					
					data_offset   = normal_start;
					
					// Make sure the last segment contains at least two points.
					if(data_offset + data_range > 1 - .002)
					{
						data_offset = 1 - .002;
						data_range  = .998;
					}
					
					// Clear data since we're at a new position.
					append_data = false;
					persistent_data = false;
					sample_information.setText("");	

					slope_output.setText("");
					startpoint_set = false;
					midpoint_set = false;
					endpoint_set = false;
					
					// Update offset/range information.
					offset_information.setText(String.format("%,.4f", data_offset));
					range_information.setText(String.format("%.2f", data_range * data_length));
					
                    // Update location slider.
					int current_value = (int)(data_offset * 100);
					location_slider.setValue(current_value);
					location_information.setText(String.format("%.4f", data_offset));
					
					// Update location map.
					location_canvas.repaint();
					
					// Redraw data.
					data_canvas.repaint();
					segment_image.repaint();
					segment_map.repaint();
					
				}
				scrollbar_changing = false;
			}
		}
	}
	
	class MouseHandler extends MouseAdapter
	{
		public void mouseClicked(MouseEvent event)
		{
			int button = event.getButton();
			if(button == 1)
			{
			    persistent_data = true;
			    append_data     = true;
			    data_canvas.repaint();
				segment_image.repaint();
			    segment_map.repaint();
			}
			if(button == 3)
			{
				persistent_data = false;
				sample_information.setText("");
				if(append_data)
				{
				    append_data = false;
				    data_canvas.repaint();
					segment_image.repaint();
				    segment_map.repaint();
				}
			}
		}
	}

	class MouseMotionHandler extends MouseMotionAdapter
	{
		public void mouseMoved(MouseEvent event)
		{
			if(pixel_data == null)
				return;
			
			int x = event.getX();
			int y = event.getY();
			int xdim = pixel_data[0].length;
			int ydim = pixel_data.length;

			if (x > left_margin && x < xdim - right_margin && y > top_margin && y < ydim - bottom_margin)
			{
				int current_line, current_sensor;
				double current_intensity, current_x, current_y;
				ArrayList sample_list = pixel_data[y][x];
				int size = sample_list.size();
				outer: if (size == 0)
				{
					sample_list = pixel_data[y - 1][x];
					size = sample_list.size();
					if (size != 0)
						break outer;
					sample_list = pixel_data[y + 1][x];
					size = sample_list.size();
					if (size != 0)
						break outer;
					sample_list = pixel_data[y][x - 1];
					size = sample_list.size();
					if (size != 0)
						break outer;
					sample_list = pixel_data[y][x + 1];
					size = sample_list.size();
					if (size != 0)
					break outer;
					sample_list = pixel_data[y - 1][x - 1];
					size = sample_list.size();
					if (size != 0)
						break outer;
					sample_list = pixel_data[y + 1][x - 1];
					size = sample_list.size();
					sample_list = pixel_data[y - 1][x + 1];
					size = sample_list.size();
					if (size != 0)
						break outer;
					sample_list = pixel_data[y + 1][x + 1];
					size = sample_list.size();
				}

				if (size != 0)
				{
					sample_information.setText("");
					
					// We would like to filter invisible and occluded points here 
					// (and resolve multiple points that project to the same point) .
					// The method for filtering invisible points in the fence program
					// does not work here because it references a gui feature not 
					// included in the wand program.
					current_line = (int) sample_list.get(0);
					current_sensor = (int) sample_list.get(1);
					Sample sample = (Sample) sample_list.get(2);
					current_intensity = sample.intensity;
					current_x = sample.x;
					current_y = sample.y;
					
					if(!persistent_data)
					{
					    append_line       = current_line;
					    append_sensor     = current_sensor;
					    append_intensity  = current_intensity;
					    append_x          = current_x;
					    append_y          = current_y;
					    append_x_abs      = current_x + global_xmin;
					    append_y_abs      = current_y + global_ymin;
					    append_x_position = x;
					    append_y_position = y;
					    append_index      = sample.index;
					}

					String information_string = new String("  Line:         " + current_line + "\n");
					sample_information.append(information_string);
					information_string = new String("  Sensor:     " + current_sensor + "\n");
					sample_information.append(information_string);
					String number_string = String.format("%,.2f", current_intensity);
					information_string = new String("  Intensity:   " + number_string + "\n");
					sample_information.append(information_string);

					number_string = String.format("%,.2f", current_x);
					information_string = new String("  Relative x: " + number_string + "\n");
					sample_information.append(information_string);
					number_string = String.format("%,.2f", current_y);
					information_string = new String("  Relative y: " + number_string + "\n");
					sample_information.append(information_string);

					number_string = String.format("%,.2f", (current_x + global_xmin));
					information_string = new String("  Absolute x: " + number_string + "\n");
					sample_information.append(information_string);
					number_string = String.format("%,.2f", (current_y + global_ymin));
					information_string = new String("  Absolute y: " + number_string + "\n");
					sample_information.append(information_string);
				}
				else
					// Blank information panel when a pixel is traversed that is not associated with data.
					sample_information.setText("");
			}
		}
	}
	
	class CompassCanvas extends Canvas
	{
		int top_margin    = 2;
		int bottom_margin = 4;
		int left_margin   = 4;
		int right_margin  = 2;
		public void paint(Graphics g)
		{
			Rectangle visible_area = g.getClipBounds();
			int xdim = (int) visible_area.getWidth();
			int ydim = (int) visible_area.getHeight();
			Graphics2D g2 = (Graphics2D) g;
			Font current_font = g2.getFont();
			FontMetrics font_metrics = g2.getFontMetrics(current_font);
			g2.setColor(java.awt.Color.WHITE);
			g2.fillRect(0, 0, xdim, ydim);

		}
	}
	
	class PlacementCanvas extends Canvas
	{
		int top_margin    = 2;
		int bottom_margin = 4;
		int left_margin   = 4;
		int right_margin  = 2;
		public void paint(Graphics g)
		{
			Rectangle visible_area = g.getClipBounds();
			int xdim = (int) visible_area.getWidth();
			int ydim = (int) visible_area.getHeight();
			Graphics2D g2 = (Graphics2D) g;
			Font current_font = g2.getFont();
			FontMetrics font_metrics = g2.getFontMetrics(current_font);
			g2.setColor(java.awt.Color.WHITE);
			g2.fillRect(0, 0, xdim, ydim);

			int number_of_segments = 5;
			// System.out.println("number of segments is " + number_of_segments);

			double max_xstep = xdim / number_of_segments;
			int xstep = (int) (max_xstep * normal_xstep);
			// System.out.println("Xstep is " + xstep);

			int graph_xdim = xdim - (left_margin + right_margin) - (number_of_segments - 1) * xstep;

			// Separate graphs instead of butting them.
			if (xstep == max_xstep)
			{
				graph_xdim -= 2;
			}

			double max_ystep = ydim / number_of_segments;
			int ystep = (int) (max_ystep * normal_ystep);
			int graph_ydim = ydim - (top_margin + bottom_margin) - (number_of_segments - 1) * ystep;

			if (ystep == max_ystep)
			{
				graph_ydim -= 2;
			}

			Rectangle[] rectangle = new Rectangle[number_of_segments];

			for (int i = 0; i < number_of_segments; i++)
			{
				int a1 = left_margin;
				int b1 = ydim - bottom_margin;
				int a2 = a1 + graph_xdim;
				int b2 = b1 - graph_ydim;

				int xaddend = i * xstep;
				int yaddend = i * ystep;
				a1 += xaddend;
				b1 -= yaddend;
				a2 += xaddend;
				b2 -= yaddend;

				rectangle[i] = new Rectangle(a2, b2, graph_xdim, graph_ydim);
			}

			for (int i = (number_of_segments - 1); i >= 0; i--)
			{
				int a1 = 0;
				int b1 = ydim;
				int a2 = a1 + graph_xdim;
				int b2 = b1 - graph_ydim;

				int xaddend = i * xstep;
				int yaddend = i * ystep;
				a1 += xaddend;
				b1 -= yaddend;
				a2 += xaddend;
				b2 -= yaddend;
				if (visible[i])
				{
					if (!reverse_view)
					{
						if (!transparent[i])
						{
							g2.setColor(fill_color[i]);
							g2.fillRect(a1, b2, graph_xdim, graph_ydim);
						}
						g2.setColor(Color.BLACK);
						g2.drawRect(a1, b2, graph_xdim, graph_ydim);
					} 
					else
					{
						if (!transparent[i])
						{
							g2.setColor(fill_color[(number_of_segments - 1) - i]);
							g2.fillRect(a1, b2, graph_xdim, graph_ydim);
						}
						g2.setColor(Color.BLACK);
						g2.drawRect(a1, b2, graph_xdim, graph_ydim);
					}
				}
			}
		}
	}
	
	class LocationCanvas extends Canvas
	{
		public void paint(Graphics g)
		{
			Rectangle visible_area = g.getClipBounds();

			int xdim = (int) visible_area.getWidth();
			int ydim = (int) visible_area.getHeight();
			
			int left_margin = 40;
			int bottom_margin = 40;
			
			int right_margin = 10;
			int top_margin   = 10;
			
			// Swapping x and y to create a horizontal display.
			double xrange = global_ymax - global_ymin;
			double yrange = global_xmax - global_xmin;
			
			int graph_xdim = xdim - (left_margin + right_margin);
			int graph_ydim = ydim - (top_margin + bottom_margin);
			
			double xfactor = graph_xdim;
			xfactor       /= xrange;
			double yfactor = graph_ydim; 
			yfactor       /= yrange;

			int size = data_array.size();
			int number_of_segments = 5;
			
			
			Image buffered_image = new BufferedImage(xdim, ydim, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics_buffer = (Graphics2D) buffered_image.getGraphics();
			Font current_font = graphics_buffer.getFont();
			FontMetrics font_metrics = graphics_buffer.getFontMetrics(current_font);
			graphics_buffer.setColor(java.awt.Color.WHITE);
			graphics_buffer.fillRect(0, 0, xdim, ydim);
			graphics_buffer.setColor(java.awt.Color.BLACK);
			
	    	graphics_buffer.setColor(java.awt.Color.BLACK);
	    	graphics_buffer.setStroke(new BasicStroke(2));
	    	graphics_buffer.drawLine(left_margin - 5, top_margin, left_margin - 5, ydim - bottom_margin);
	    	graphics_buffer.drawLine(left_margin - 5, top_margin, left_margin - 1, top_margin);
	    	graphics_buffer.drawLine(left_margin - 5, ydim - bottom_margin, left_margin - 1, ydim - bottom_margin);
	    	
	    	
	    	graphics_buffer.setFont(new Font("default", Font.BOLD, 12));
	    	String stop_string = String.format("%.1f", global_xmax - global_xmin);
	    	if(reverse_view)
	    		graphics_buffer.drawString("0", 20, top_margin + 10);	
	    	else
	    		graphics_buffer.drawString(stop_string, 5, top_margin + 10);
	    	
	    	if(reverse_view)
	    		graphics_buffer.drawString(stop_string, 5, ydim - bottom_margin);	
	    	else
	    		graphics_buffer.drawString("0", 20, ydim - bottom_margin);
	    	
	    	graphics_buffer.drawLine(left_margin, ydim - bottom_margin + 20, xdim - right_margin, ydim - bottom_margin + 20);
	    	graphics_buffer.drawLine(left_margin, ydim - bottom_margin + 20, left_margin, ydim - bottom_margin + 16);
	    	graphics_buffer.drawLine(xdim - right_margin, ydim - bottom_margin + 20, xdim - right_margin, ydim - bottom_margin + 16);
	    	graphics_buffer.drawString("0", left_margin , ydim - bottom_margin + 35);
	    	stop_string = String.format("%.1f", global_ymax - global_ymin);
	    	graphics_buffer.drawString(stop_string, xdim - right_margin - 25 , ydim - bottom_margin + 35);
	    	
	    	ArrayList data_list = (ArrayList)data_array.get(0);
		
			Sample sample = (Sample) relative_data.get(2);
			int previous_x = (int)(sample.y * xfactor);
		    int previous_y = (int)(sample.x * yfactor);
		    if(!reverse_view)
		    	previous_y = graph_ydim - previous_y;
		    previous_x += left_margin;
		    previous_y += top_margin;
		    
		    for(int i = 7; i < relative_data.size(); i += 5)
		    {
		    	sample = (Sample) relative_data.get(i); 
		    	if(i >= (start_index * 5) && i < (stop_index * 5))
		    	{
		    		graphics_buffer.setColor(java.awt.Color.GREEN);
		    	    graphics_buffer.setStroke(new BasicStroke(3));
		    	}
		    	else
		    	{
		    		graphics_buffer.setColor(java.awt.Color.LIGHT_GRAY);
		    		graphics_buffer.setStroke(new BasicStroke(3));
		    	}
			    
			    int current_x = (int)(sample.y * xfactor);	
				int current_y = (int)(sample.x * yfactor);
				if(!reverse_view)
				    current_y = graph_ydim - current_y;
				current_y += top_margin;
				current_x += left_margin;
				
				
			    graphics_buffer.drawLine(previous_x,  previous_y, current_x, current_y);
			    previous_x = current_x;
			    previous_y = current_y;
		    }
		    
		    double [][] location_array = ObjectMapper.getObjectLocationArray();
			int length = location_array.length;
			
			
			for(int i = 0; i < length; i++)
			{
				double y = location_array[i][0];
				double x = location_array[i][1];
				y -= global_xmin;
				x -= global_ymin;
				x *= xfactor;
				y *= yfactor;
				if(!reverse_view)
				    y = graph_ydim - y;
				y += top_margin;
				x += left_margin;
				
				
				graphics_buffer.setColor(java.awt.Color.BLACK);
				graphics_buffer.drawOval((int)(x - 3), (int)(y - 3), 7, 7);
				graphics_buffer.fillOval((int)(x - 3), (int)(y - 3), 7, 7);
				graphics_buffer.setColor(java.awt.Color.RED);
				graphics_buffer.drawOval((int)(x - 1), (int)(y - 1), 3, 3);
				graphics_buffer.fillOval((int)(x - 1), (int)(y - 1), 3, 3);
				
				// Target numbers.
				graphics_buffer.setColor(java.awt.Color.BLACK);
				String object_string = Integer.toString(i + 1); 
				graphics_buffer.drawString(object_string, (int)(x + 5), (int) (y - 1)); 
				
			}
			
			
			// Scrollbars that find exact locations in location map.
			// Not used much, and gets in the way of the display.
			// Might be useful at some point.
			/*
			int current_xlocation       = (int)(xlocation * (xdim - 1));
			int current_ylocation       = (int)(ylocation * (ydim - 1));
			current_ylocation           = ydim - current_ylocation;
			
			graphics_buffer.setColor(java.awt.Color.BLUE);
	    	graphics_buffer.setStroke(new BasicStroke(1));
			graphics_buffer.drawLine(0, current_ylocation, xdim - 1, current_ylocation);
			graphics_buffer.drawLine(current_xlocation, 0, current_xlocation, ydim - 1);
			
			if(!location_changing)
			{
			    double normalized_x = xrange;
			    normalized_x       *= xlocation;
			    String xstring, ystring;
			    if(relative_mode)
			        xstring = String.format("%,.2f", normalized_x);
			    else
				    xstring = String.format("%,.0f", normalized_x + global_xmin);
			    double normalized_y = yrange;
			    normalized_y       *= ylocation;
			    
			    if(relative_mode)
			        ystring = String.format("%,.2f", normalized_y);
			    else
				    ystring = String.format("%,.0f", normalized_y + global_ymin);
			    String location_string = new String("x = " + xstring + ", y = " + ystring);
			    int string_width       = font_metrics.stringWidth(location_string);
			    int string_height      = font_metrics.getAscent();
			
			    if(current_xlocation > xdim / 2)
				    current_xlocation -= string_width + 3; 
			    else
				    current_xlocation += 3; 
			    if(current_ylocation < ydim / 2)
				    current_ylocation += string_height + 1;
			    else
				    current_ylocation -= 3;
			    graphics_buffer.setColor(java.awt.Color.WHITE);
			    graphics_buffer.fillRect(current_xlocation, current_ylocation  - string_height + 1, string_width, string_height);
			    graphics_buffer.setColor(java.awt.Color.BLACK);
			    graphics_buffer.drawString(location_string, current_xlocation, current_ylocation); 
			}
			*/
			g.drawImage(buffered_image, 0, 0, null);
		}
	}
	
	class SensorCanvas extends Canvas
	{
		int index;
		SensorCanvas(int index)
		{
			this.index = index;
		}

		public void paint(Graphics g)
		{
			Rectangle visible_area = g.getClipBounds();
			int xdim = (int) visible_area.getWidth();
			int ydim = (int) visible_area.getHeight();
			Graphics2D g2 = (Graphics2D) g;

			int state = sensor_state[index];
			if (state == 0)
			{
				g2.setColor(fill_color[index]);
				g2.fillRect(0, 0, xdim, ydim);
				g2.setColor(java.awt.Color.BLACK);
				g2.setStroke(new BasicStroke(3));
				g2.drawRect(0, 0, xdim, ydim);
			} 
			else if (state == 1)
			{
				g2.setColor(java.awt.Color.WHITE);
				g2.fillRect(0, 0, xdim, ydim);
				g2.setColor(java.awt.Color.BLACK);
				g2.setStroke(new BasicStroke(3));
				g2.drawRect(0, 0, xdim, ydim);
			} 
			else if (state == 2)
			{
				g2.setColor(java.awt.Color.WHITE);
				g2.fillRect(0, 0, xdim, ydim);
			}
		}
	}
	
	class SensorCanvasMouseHandler extends MouseAdapter
	{
		int index;
		SensorCanvasMouseHandler(int index)
		{
			this.index = index;
		}

		public void mouseClicked(MouseEvent event)
		{
			int button = event.getButton();
			if (button == 1)
			{
				if (sensor_state[index] == 2)
				{
					sensor_state[index] = 0;
					visible[index] = true;
					transparent[index] = false;
				} 
				else
				{
					sensor_state[index]++;
					if (sensor_state[index] == 1)
					{
						visible[index] = true;
						transparent[index] = true;
					} 
					else
						visible[index] = false;
				}
				sensor_canvas[index].repaint();
				data_canvas.repaint();
				placement_canvas.repaint();
			} 
		}
	}

	class DynamicRangeCanvas extends Canvas
	{
		public void paint(Graphics g)
		{
			Rectangle visible_area = g.getClipBounds();

			int xdim = (int) visible_area.getWidth();
			int ydim = (int) visible_area.getHeight();

			Graphics2D graphics = (Graphics2D) g;
			Font current_font = graphics.getFont();
			FontMetrics font_metrics = graphics.getFontMetrics(current_font);
			int string_height = font_metrics.getAscent();

			graphics.setColor(java.awt.Color.WHITE);
			graphics.fillRect(0, 0, xdim, ydim);

			
			int top_margin = 5;
			int bottom_margin = 5;
			int graph_ydim = ydim - (top_margin + bottom_margin);
			graphics.setColor(java.awt.Color.BLACK);
			graphics.setStroke(new BasicStroke(2));
			graphics.drawLine(xdim / 2, top_margin, xdim / 2, ydim - bottom_margin);
			graphics.drawLine(xdim / 2 - 10, top_margin, xdim / 2, top_margin);
			graphics.drawLine(xdim / 2 - 10, ydim - bottom_margin, xdim / 2, ydim - bottom_margin);

			double min = 0;
			double max = 0;
			double current_range = 0;
			if(data_clipped)
			{
				min = minimum_y;
			    max = maximum_y;
				
				//Since we're limiting min and max to two decimal places,
				//we have to do the same thing to our global min/max to
				//make our logic work.
				String global_max_string = String.format("%.2f", seg_max);
				double adjusted_global_max = Double.valueOf(global_max_string);
				String global_min_string = String.format("%.2f", seg_min);
				double adjusted_global_min = Double.valueOf(global_min_string);
				
				
				String intensity_string = new String("foo");
				if(max > adjusted_global_max)
					intensity_string = String.format("%,.2f", max);
				else
					intensity_string = String.format("%,.2f", adjusted_global_max);
				int string_width = font_metrics.stringWidth(intensity_string);
				graphics.drawString(intensity_string, xdim / 2 - (string_width + 15), top_margin + string_height);
					
				if(min < adjusted_global_min)
					intensity_string = String.format("%,.2f", min);
				else
					intensity_string = String.format("%,.2f", adjusted_global_min);
				string_width = font_metrics.stringWidth(intensity_string);
				graphics.drawString(intensity_string, xdim / 2 - (string_width + 15), ydim - bottom_margin);	
					
				current_range = 0;
				if(min <= adjusted_global_min)
				{
					if(max > adjusted_global_max)
					    current_range = max - min;
					else
					    current_range = seg_max - min;
				}
				else
				{
					if(max > adjusted_global_max)
					    current_range = max - seg_min;
					else
					    current_range = seg_max - seg_min;   	
				}
					
				if(min > adjusted_global_min)
				{
					double min_delta = seg_min - min;
					min_delta /= current_range;
					double delta = min_delta * graph_ydim;
					graphics.drawLine(xdim / 2, ydim - ((int) -delta + bottom_margin), xdim / 2 + 10, ydim - ((int) -delta + bottom_margin));
					intensity_string = String.format("%,.2f", min);
					graphics.drawString(intensity_string, xdim / 2 + 15, ydim - ((int) -delta + bottom_margin));		
				}
				else if(min < adjusted_global_min)
				{
					double min_delta = min - seg_min;
					min_delta /= current_range;
					double delta = min_delta * graph_ydim;
					graphics.drawLine(xdim / 2, ydim - ((int) -delta + bottom_margin), xdim / 2 + 10, ydim - ((int) -delta + bottom_margin));
					intensity_string = String.format("%,.2f", adjusted_global_min);
				    graphics.drawString(intensity_string, xdim / 2 + 15, ydim - ((int) -delta + bottom_margin));	 	
				}
				else if(min == adjusted_global_min) 
				{
					graphics.drawLine(xdim / 2 + 10, ydim - bottom_margin, xdim / 2, ydim - bottom_margin);	
				}
					
				if(max < adjusted_global_max)
				{
					double max_delta = seg_max - max;
					max_delta       /= current_range;
					double delta     = max_delta * graph_ydim;
					graphics.drawLine(xdim / 2, top_margin + (int) delta, xdim / 2 + 10, top_margin + (int) delta);
					intensity_string = String.format("%,.2f", max);
					graphics.drawString(intensity_string, xdim / 2 + 15, top_margin + (int) delta);		
				}
				else if(max > adjusted_global_max)
				{
					double max_delta = max - seg_max;
					max_delta       /= current_range;
					double delta     = max_delta * graph_ydim;
					graphics.drawLine(xdim / 2, top_margin + (int) delta, xdim / 2 + 10, top_margin + (int) delta);
					intensity_string = String.format("%,.2f", seg_max);
					graphics.drawString(intensity_string, xdim / 2 + 15, top_margin + (int) delta);
				}
				else if(max == adjusted_global_max)
				{
					graphics.drawLine(xdim / 2 + 10, top_margin, xdim / 2, top_margin);	
				}
					
				if(min < 0  || adjusted_global_min < 0)
				{
					current_range = 0;
					if(min < adjusted_global_min)
					{
						if(max > adjusted_global_max)
						    current_range = max - min;
						else
						    current_range = seg_max - min;
					}
					else
					{
						if(max > adjusted_global_max)
						    current_range = max - seg_min;
						else
						    current_range = seg_max - seg_min;   	
					}
						
					double zero_point = 0;
					if(max > adjusted_global_max)
						zero_point = max / current_range;
					else
						zero_point = seg_max / current_range;
					zero_point *= graph_ydim;
					graphics.setColor(java.awt.Color.RED);
					graphics.drawLine(xdim / 2 - 10, top_margin + (int) zero_point, xdim / 2, top_margin + (int) zero_point);
					graphics.setColor(java.awt.Color.BLACK);
				}
			}
			else
			{
				min = seg_min;
				max = seg_max;
				current_range = max - min; 
				String intensity_string = String.format("%,.2f", max);
				int string_width = font_metrics.stringWidth(intensity_string);
				graphics.drawString(intensity_string, xdim / 2 - (string_width + 15), top_margin + string_height / 2);
				intensity_string = String.format("%,.2f", min);
				string_width = font_metrics.stringWidth(intensity_string);
				graphics.drawString(intensity_string, xdim / 2 - (string_width + 15), ydim - bottom_margin);
				if (min < 0)
				{
					double zero_point = max / current_range;
					zero_point *= graph_ydim;
					graphics.setColor(java.awt.Color.RED);
					graphics.drawLine(xdim / 2 - 10, top_margin + (int) zero_point, xdim / 2, top_margin + (int) zero_point);
					graphics.setColor(java.awt.Color.BLACK);
				}
			} 
		}
	}
	
	class DynamicRangeSliderHandler implements ChangeListener
	{
		public void stateChanged(ChangeEvent e)
		{
			RangeSlider slider = (RangeSlider) e.getSource();
			int lower = slider.getValue();
			int upper = slider.getUpperValue();
			double min = 0;
			double max = 0;
			
			if (dynamic_button_changing == false)
			{
				if(data_clipped)
				{
				    min = minimum_y;
				    max = maximum_y;
				}
				else
				{
					min = seg_min;
				    max = seg_max;  	
				}
				
				double current_range      = max - min;
				
				double fraction           = (double) lower / 100;
				double lower_value        = (fraction * current_range) + min;
				String lower_bound_string = String.format("%,.2f", lower_value);
				lower_bound.setText(lower_bound_string);
				
				fraction                  = (double) upper / 100;
				double upper_value        = (fraction * current_range) + min;
				String upper_bound_string = String.format("%,.2f", upper_value);
				upper_bound.setText(upper_bound_string);
			}
		}
	}
	
	class SegmentImageCanvas extends Canvas
	{
		public void paint(Graphics g)
		{
			int number_of_segments = 5;
			
			Rectangle visible_area = g.getClipBounds();

			int xdim = (int) visible_area.getWidth();
			int ydim = (int) visible_area.getHeight();
			
			int top_margin    = 10;
			int bottom_margin = 50;
			int left_margin   = 50;
			int right_margin  = 20;
			
			int image_xdim = xdim - (left_margin + right_margin);
			int image_ydim = ydim - (top_margin + bottom_margin);
			
			BufferedImage buffered_image       = new BufferedImage(xdim, ydim, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics_buffer = (Graphics2D) buffered_image.getGraphics();
			FontMetrics font_metrics   = graphics_buffer.getFontMetrics();
			
			graphics_buffer.setColor(java.awt.Color.WHITE);
			graphics_buffer.fillRect(0, 0, xdim, ydim);
			
			int a1 = left_margin;
			int a2 = left_margin + image_xdim;
			int b1 = top_margin;
			int b2 = top_margin + image_ydim;
			
			graphics_buffer.setColor(java.awt.Color.BLACK);
			graphics_buffer.drawRect(a1, b1, image_xdim, image_ydim);
			
			double xmin = Double.MAX_VALUE;
			double xmax = 0;
			double ymin = Double.MAX_VALUE;
			double ymax = 0;
			double min_intensity = Double.MAX_VALUE;
			double max_intensity  = 0;
			
			for (int i = 0; i < number_of_segments; i++)
			{
				ArrayList sample_list = (ArrayList) relative_data_array.get(i);
				int size = sample_list.size();
				for(int j = 0; j < size; j++)
				{
					Sample sample = (Sample) sample_list.get(j);
					double x = sample.y;
					double y = sample.x;
					
					if(xmin > x)
						xmin = x;
					if(xmax < x)
						xmax = x;
					
					if(ymin > y)
						ymin = y;
					if(ymax < y)
						ymax = y;
					
					if(sample.intensity < min_intensity)
						min_intensity = sample.intensity;
					if(sample.intensity > max_intensity)
						max_intensity = sample.intensity;
				}
			}
			double intensity_range = max_intensity - min_intensity;
			
			int string_width  = font_metrics.stringWidth("77.7");
			int string_height = font_metrics.getAscent();
			
			int number_of_units = 10;
			double xrange = xmax - xmin;
			double value_increment = xrange / number_of_units;
			int    position_increment = image_xdim / number_of_units;
			int    position = a1;
			
			double value = xmin;
			
			for(int i = 0; i < (number_of_units + 1); i++)
			{
				graphics_buffer.drawLine(position, ydim - bottom_margin, position, top_margin);
				
				String value_string = String.format("%.1f", value);
				graphics_buffer.drawString(value_string, position - string_width / 2, ydim - bottom_margin + 20 + string_height);
			    value += value_increment;
				position += position_increment;
			}
			
			double yrange      = ymax - ymin;
			value_increment    = yrange / number_of_segments;
			position_increment = image_ydim / number_of_segments;
			position           = b1;
			
			if(reverse_view)
				value = ymin;
			else
				value = ymax;
			for(int i = 0; i < (number_of_segments + 1); i++)
			{
				graphics_buffer.drawLine(a1, position, a2, position);
				
				String value_string = String.format("%.1f", value);
				graphics_buffer.drawString(value_string, a1 - (12 + string_width), position + (string_height / 2));
				if(reverse_view)
					value += value_increment;
				else
					value -= value_increment;	
				position += position_increment;
			}
			
			for (int i = 0; i < number_of_segments; i++)
			{
				ArrayList sample_list = (ArrayList)relative_data_array.get(i);
				int size = sample_list.size();
				for(int j = 0; j < size; j++)
				{
					Sample sample = (Sample) sample_list.get(j);
					double y = sample.x;
					double x = sample.y;
					
					x -= xmin;
					x /= xrange;
					x *= image_xdim;
					x += left_margin;
					
					y -= ymin;
					y /= yrange;
					y *= image_ydim;
					if(!reverse_view)
					   y = image_ydim - y;
					y += top_margin;
					
					double intensity = sample.intensity - min_intensity;
					intensity /= intensity_range;
					int rgb_value = (int)(intensity * 255.);
					Color color = new Color(rgb_value, rgb_value, rgb_value);
					graphics_buffer.setColor(color);
					if(sample_list.size() > image_xdim)
					    graphics_buffer.fillRect((int)x, (int)(y - 10), 1, 20);
					else
					{
						int width = image_xdim / sample_list.size();
						width += 2;
						graphics_buffer.fillRect((int)x, (int)(y - 10), width, 20);
					}	
				}
			}
			
			if(append_data)
			{
				double x_position = append_y;
				x_position -= xmin;
				x_position /= xrange;
				x_position *= image_xdim;
				x_position += left_margin;
				
				double y_position = append_x;
				y_position -= ymin;
				y_position /= yrange;
				y_position *= image_ydim;
				if(!reverse_view)
				    y_position  = image_ydim - y_position;
				y_position += top_margin;
			
				graphics_buffer.setColor(Color.BLACK);
				graphics_buffer.drawOval((int)x_position - 3, (int)y_position - 3, 7, 7);
				graphics_buffer.fillOval((int)x_position - 3, (int)y_position - 3, 7, 7);
				graphics_buffer.setColor(Color.RED);
				graphics_buffer.drawOval((int)x_position - 2, (int)y_position - 2, 5, 5);
				graphics_buffer.fillOval((int)x_position - 2, (int)y_position - 2, 5, 5);
				graphics_buffer.setColor(Color.BLACK);	   	
			}
			
			if(startpoint_set)
			{
				double x_position = startpoint_y;
				
				x_position -= xmin;
				x_position /= xrange;
				x_position *= image_xdim;
				x_position += left_margin;
				
				double y_position = startpoint_x;
				y_position -= ymin;
				y_position /= yrange;
				y_position *= image_ydim;
				if(!reverse_view)
				    y_position  = image_ydim - y_position;
				y_position += top_margin;
				
				graphics_buffer.setColor(Color.BLACK);
				graphics_buffer.drawOval((int)x_position - 3, (int)y_position - 3, 7, 7);
				graphics_buffer.fillOval((int)x_position - 3, (int)y_position - 3, 7, 7);
				
				graphics_buffer.setColor(Color.GREEN);
				graphics_buffer.drawOval((int)x_position - 2, (int)y_position - 2, 5, 5);
				graphics_buffer.fillOval((int)x_position - 2, (int)y_position - 2, 5, 5);
				graphics_buffer.setColor(Color.BLACK);		
			}
			
			if(midpoint_set)
			{
				double x_position = midpoint_y;
				
				x_position -= xmin;
				x_position /= xrange;
				x_position *= image_xdim;
				x_position += left_margin;
				
				double y_position = midpoint_x;
				y_position -= ymin;
				y_position /= yrange;
				y_position *= image_ydim;
				if(!reverse_view)
				    y_position  = image_ydim - y_position;
				y_position += top_margin;
				
				graphics_buffer.setColor(Color.BLACK);
				graphics_buffer.drawOval((int)x_position - 3, (int)y_position - 3, 7, 7);
				graphics_buffer.fillOval((int)x_position - 3, (int)y_position - 3, 7, 7);
				graphics_buffer.setColor(Color.GREEN);
				graphics_buffer.drawOval((int)x_position - 2, (int)y_position - 2, 5, 5);
				graphics_buffer.fillOval((int)x_position - 2, (int)y_position - 2, 5, 5);
				graphics_buffer.setColor(Color.BLACK);	
			}
			
			if(endpoint_set)
			{
				double x_position = endpoint_y;

				x_position -= xmin;
				x_position /= xrange;
				x_position *= image_xdim;
				x_position += left_margin;
				
				double y_position = endpoint_x;
				y_position -= ymin;
				y_position /= yrange;
				y_position *= image_ydim;
				if(!reverse_view)
				    y_position  = image_ydim - y_position;
				y_position += top_margin;
			
				graphics_buffer.setColor(Color.BLACK);
				graphics_buffer.drawOval((int)x_position - 3, (int)y_position - 3, 7, 7);
				graphics_buffer.fillOval((int)x_position - 3, (int)y_position - 3, 7, 7);
				graphics_buffer.setColor(Color.GREEN);
				graphics_buffer.drawOval((int)x_position - 2, (int)y_position - 2, 5, 5);
				graphics_buffer.fillOval((int)x_position - 2, (int)y_position - 2, 5, 5);
				graphics_buffer.setColor(Color.BLACK);		
			}
			
			g.drawImage(buffered_image, 0, 0,  this);
		}
	}
	
	class SegmentImage extends Canvas
	{
		public void paint(Graphics g)
		{
			int number_of_segments = 5;
			
			Rectangle visible_area = g.getClipBounds();

			int xdim = (int) visible_area.getWidth();
			int ydim = (int) visible_area.getHeight();
			
			int top_margin    = 10;
			int bottom_margin = 50;
			int left_margin   = 50;
			int right_margin  = 50;
			
			int image_xdim = xdim - (left_margin + right_margin);
			int image_ydim = ydim - (top_margin + bottom_margin);
			
			BufferedImage buffered_image       = new BufferedImage(xdim, ydim, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics_buffer = (Graphics2D) buffered_image.getGraphics();
			FontMetrics font_metrics   = graphics_buffer.getFontMetrics();
			
			graphics_buffer.setColor(java.awt.Color.WHITE);
			graphics_buffer.fillRect(0, 0, xdim, ydim);
			
			int a1 = left_margin;
			int a2 = left_margin + image_xdim;
			int b1 = top_margin;
			int b2 = top_margin + image_ydim;
			
			graphics_buffer.setColor(java.awt.Color.BLACK);
			graphics_buffer.drawRect(a1, b1, image_xdim, image_ydim);
			
			double xmin = Double.MAX_VALUE;
			double xmax = 0;
			double ymin = Double.MAX_VALUE;
			double ymax = 0;
			double min_intensity = Double.MAX_VALUE;
			double max_intensity  = 0;
			
			double [] start_y = new double[5];
			double [] end_y   = new double[5];
			for (int i = 0; i < number_of_segments; i++)
			{
				ArrayList sample_list = (ArrayList) relative_data_array.get(i);
				int size = sample_list.size();
				Sample start_sample = (Sample)sample_list.get(0);
				Sample end_sample   = (Sample)sample_list.get(size - 1);
				
				start_y[i] = start_sample.x;
				end_y[i]   = end_sample.x;
				
				for(int j = 0; j < size; j++)
				{
					Sample sample = (Sample) sample_list.get(j);
					double x = sample.y;
					double y = sample.x;
					
					if(xmin > x)
						xmin = x;
					if(xmax < x)
						xmax = x;
					
					if(ymin > y)
						ymin = y;
					if(ymax < y)
						ymax = y;
					
					if(sample.intensity < min_intensity)
						min_intensity = sample.intensity;
					if(sample.intensity > max_intensity)
						max_intensity = sample.intensity;
				}
			}
			double intensity_range = max_intensity - min_intensity;
			
			int string_width  = font_metrics.stringWidth("77.7");
			int string_height = font_metrics.getAscent();
			
			int number_of_units = 10;
			double xrange = xmax - xmin;
			double value_increment = xrange / number_of_units;
			int    position_increment = image_xdim / number_of_units;
			int    position = a1;
			
			double value = xmin;
			
			for(int i = 0; i < (number_of_units + 1); i++)
			{
				graphics_buffer.drawLine(position, ydim - bottom_margin, position, top_margin);
				
				String value_string = String.format("%.1f", value);
				graphics_buffer.drawString(value_string, position - string_width / 2, ydim - bottom_margin + 20 + string_height);
			    value += value_increment;
				position += position_increment;
			}
			
			
			
			double yrange      = ymax - ymin;
			value_increment    = yrange / number_of_segments;
			position_increment = image_ydim / number_of_segments;
			position           = b1;
			
			if(reverse_view)
				value = ymin;
			else
				value = ymax;
			for(int i = 0; i < (number_of_segments + 1); i++)
			{
				graphics_buffer.drawLine(a1, position, a2, position);
				
				String value_string = String.format("%.1f", value);
				if(reverse_view)
					value += value_increment;
				else
					value -= value_increment;	
				position += position_increment;
			}
			
			for (int i = 0; i < number_of_segments; i++)
			{
				double y = start_y[i];
				String start_string = String.format("%.2f", y);
				y -= ymin;
				y /= yrange;
				y *= image_ydim;
				if(!reverse_view)
				   y = image_ydim - y;
				y += top_margin;
				
				
				string_width = font_metrics.stringWidth(start_string);
				graphics_buffer.drawString(start_string, a1 - (12 + string_width), (int)y + (string_height / 2));
				
				y = end_y[i];
				String end_string = String.format("%.2f", y);
				y -= ymin;
				y /= yrange;
				y *= image_ydim;
				if(!reverse_view)
				   y = image_ydim - y;
				y += top_margin;
				
				//string_width = font_metrics.stringWidth(start_string);
				graphics_buffer.drawString(end_string, a2 + 12, (int)y + (string_height / 2));
				
				ArrayList sample_list = (ArrayList)relative_data_array.get(i);
				int size = sample_list.size();
				for(int j = 0; j < size; j++)
				{
					Sample sample = (Sample) sample_list.get(j);
					y = sample.x;
					double x = sample.y;
					
					x -= xmin;
					x /= xrange;
					x *= image_xdim;
					x += left_margin;
					
					y -= ymin;
					y /= yrange;
					y *= image_ydim;
					if(!reverse_view)
					   y = image_ydim - y;
					y += top_margin;
					
					double intensity = sample.intensity - min_intensity;
					intensity /= intensity_range;
					int rgb_value = (int)(intensity * 255.);
					Color color = new Color(rgb_value, rgb_value, rgb_value);
					graphics_buffer.setColor(color);
					if(sample_list.size() > image_xdim)
					    graphics_buffer.fillRect((int)x, (int)(y - 10), 1, 20);
					else
					{
						int width = image_xdim / sample_list.size();
						width += 2;
						graphics_buffer.fillRect((int)x, (int)(y - 10), width, 20);
					}	
				}
			}
			
			if(append_data)
			{
				double x_position = append_y;
				x_position -= xmin;
				x_position /= xrange;
				x_position *= image_xdim;
				x_position += left_margin;
				
				double y_position = append_x;
				y_position -= ymin;
				y_position /= yrange;
				y_position *= image_ydim;
				if(!reverse_view)
				    y_position  = image_ydim - y_position;
				y_position += top_margin;
			
				graphics_buffer.setColor(Color.BLACK);
				graphics_buffer.drawOval((int)x_position - 3, (int)y_position - 3, 7, 7);
				graphics_buffer.fillOval((int)x_position - 3, (int)y_position - 3, 7, 7);
				graphics_buffer.setColor(Color.RED);
				graphics_buffer.drawOval((int)x_position - 2, (int)y_position - 2, 5, 5);
				graphics_buffer.fillOval((int)x_position - 2, (int)y_position - 2, 5, 5);
				graphics_buffer.setColor(Color.BLACK);	   	
			}
			
			if(startpoint_set)
			{
				double x_position = startpoint_y;
				
				x_position -= xmin;
				x_position /= xrange;
				x_position *= image_xdim;
				x_position += left_margin;
				
				double y_position = startpoint_x;
				y_position -= ymin;
				y_position /= yrange;
				y_position *= image_ydim;
				if(!reverse_view)
				    y_position  = image_ydim - y_position;
				y_position += top_margin;
				
				graphics_buffer.setColor(Color.BLACK);
				graphics_buffer.drawOval((int)x_position - 3, (int)y_position - 3, 7, 7);
				graphics_buffer.fillOval((int)x_position - 3, (int)y_position - 3, 7, 7);
				
				graphics_buffer.setColor(Color.GREEN);
				graphics_buffer.drawOval((int)x_position - 2, (int)y_position - 2, 5, 5);
				graphics_buffer.fillOval((int)x_position - 2, (int)y_position - 2, 5, 5);
				graphics_buffer.setColor(Color.BLACK);		
			}
			
			if(midpoint_set)
			{
				double x_position = midpoint_y;
				
				x_position -= xmin;
				x_position /= xrange;
				x_position *= image_xdim;
				x_position += left_margin;
				
				double y_position = midpoint_x;
				y_position -= ymin;
				y_position /= yrange;
				y_position *= image_ydim;
				if(!reverse_view)
				    y_position  = image_ydim - y_position;
				y_position += top_margin;
				
				graphics_buffer.setColor(Color.BLACK);
				graphics_buffer.drawOval((int)x_position - 3, (int)y_position - 3, 7, 7);
				graphics_buffer.fillOval((int)x_position - 3, (int)y_position - 3, 7, 7);
				graphics_buffer.setColor(Color.GREEN);
				graphics_buffer.drawOval((int)x_position - 2, (int)y_position - 2, 5, 5);
				graphics_buffer.fillOval((int)x_position - 2, (int)y_position - 2, 5, 5);
				graphics_buffer.setColor(Color.BLACK);	
			}
			
			if(endpoint_set)
			{
				double x_position = endpoint_y;

				x_position -= xmin;
				x_position /= xrange;
				x_position *= image_xdim;
				x_position += left_margin;
				
				double y_position = endpoint_x;
				y_position -= ymin;
				y_position /= yrange;
				y_position *= image_ydim;
				if(!reverse_view)
				    y_position  = image_ydim - y_position;
				y_position += top_margin;
			
				graphics_buffer.setColor(Color.BLACK);
				graphics_buffer.drawOval((int)x_position - 3, (int)y_position - 3, 7, 7);
				graphics_buffer.fillOval((int)x_position - 3, (int)y_position - 3, 7, 7);
				graphics_buffer.setColor(Color.GREEN);
				graphics_buffer.drawOval((int)x_position - 2, (int)y_position - 2, 5, 5);
				graphics_buffer.fillOval((int)x_position - 2, (int)y_position - 2, 5, 5);
				graphics_buffer.setColor(Color.BLACK);		
			}
			
			g.drawImage(buffered_image, 0, 0,  this);
		}
	}
	
	
	
	class SegmentMap extends Canvas
	{
		public void paint(Graphics g)
		{
			Graphics2D graphics       = (Graphics2D)g;
			Rectangle  visible_area   = graphics.getClipBounds();
			int xdim                  = (int) visible_area.getWidth();
			int ydim                  = (int) visible_area.getHeight();
			
			Image buffered_image = new BufferedImage(xdim, ydim, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics_buffer = (Graphics2D) buffered_image.getGraphics();
			Font        current_font  = graphics_buffer.getFont();
			FontMetrics font_metrics  = graphics_buffer.getFontMetrics(current_font);
			int         string_height = font_metrics.getAscent();
			int         string_width;
			
			graphics_buffer.setColor(java.awt.Color.WHITE);
			graphics_buffer.fillRect(0, 0, xdim, ydim);
			
			int top_margin    = 10;
			int bottom_margin = 40;
			int left_margin   = 40;
			int right_margin  = 40;
			int graph_xdim    = xdim - (left_margin + right_margin);
			int graph_ydim    = ydim - (top_margin + bottom_margin);
			
			double a1 = left_margin;
			double a2 = left_margin + graph_xdim;
			double b1 = top_margin;
			double b2 = top_margin + graph_ydim;
			
			double xmin = Double.MAX_VALUE;
			double xmax = 0;
			double ymin = Double.MAX_VALUE;
			double ymax = 0;
			
			int number_of_segments = 5;
			for (int i = 0; i < number_of_segments; i++)
			{
				ArrayList sample_list = (ArrayList) relative_data_array.get(i);
				int size = sample_list.size();
				for(int j = 0; j < size; j++)
				{
					Sample sample = (Sample) sample_list.get(j);
					double x = sample.y;
					double y = sample.x;
					
					if(xmin > x)
						xmin = x;
					if(xmax < x)
						xmax = x;
					
					if(ymin > y)
						ymin = y;
					if(ymax < y)
						ymax = y;
				}
			}
			
			double xrange = xmax - xmin;
			double yrange = ymax - ymin;
			graphics_buffer.setStroke(new BasicStroke(3));
			for(int i = 0; i < number_of_segments; i++)
			{
				ArrayList sample_list = (ArrayList) relative_data_array.get(i);
			    Sample previous_sample = (Sample)sample_list.get(0);
			    
			    // Switching x and y to match orientation of graphs
			    double x = previous_sample.y;
			    double y = previous_sample.x;
			    
			    x -= xmin;
			    x /= xrange;
        	    x *= graph_xdim;
        	    x += left_margin;
        	    
        	    y -= ymin;
        	    y /= yrange;
        	    y *= graph_ydim;
        	    if(!reverse_view)
        	    {
        	    	y = graph_ydim - y;
        	    }
        	    y += top_margin;
        	    
        	    int line   = start_flight_line;
        	    int sensor = i;
        	    if(reverse_view)
        	    	sensor = 4 - i;
        	    String line_sensor_pair = new String(line + ":" + sensor);
        	    string_width = font_metrics.stringWidth(line_sensor_pair);
        	    graphics_buffer.setColor(java.awt.Color.BLACK);
			    graphics_buffer.drawString(line_sensor_pair, (int)x - (string_width + 10), (int)(y + string_height / 2));
			    
			    if(reverse_view)
        	        graphics_buffer.setColor(fill_color[i]);
			    else
			    	graphics_buffer.setColor(fill_color[4 - i]);
			    Point2D.Double previous = new Point2D.Double(x, y);
			    for(int j = 1; j < sample_list.size(); j++)
				{
					Sample sample = (Sample)sample_list.get(j);
					x = sample.y;
				    y = sample.x;
				    
				    x -= xmin;
				    x /= xrange;
	        	    x *= graph_xdim;
	        	    x += left_margin;
	        	    
	        	    y -= ymin;
	        	    y /= yrange;
	        	    y *= graph_ydim;
	        	    if(!reverse_view)
	        	    {
	        	    	y = graph_ydim - y;
	        	    }
	        	    y += top_margin;
	        	    
	        	    Point2D.Double current = new Point2D.Double(x, y);
	        	    
	        	    double x1 = previous.getX();
					double y1 = previous.getY();
					double x2 = current.getX();
					double y2 = current.getY();
					
					graphics_buffer.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
	        	    previous = current;
				}
			}
			
			graphics_buffer.setStroke(new BasicStroke(1));
			graphics_buffer.setColor(java.awt.Color.BLACK);
			graphics_buffer.drawLine((int) a1, (int)b1, (int)a1, (int)b2);
			graphics_buffer.drawLine((int) a2, (int)b1, (int) a2, (int)b2);
			graphics_buffer.drawLine((int) a1, (int)b1, (int) a2, (int)b1);
			graphics_buffer.drawLine((int) a1, (int)b2, (int) a2, (int)b2);
			
			
			
			int number_of_units = 10;
			double value_increment = xrange / number_of_units;
			int    position_increment = graph_xdim / number_of_units;
			int    position = (int)a1;
			
			double value = xmin;
			
			for(int i = 0; i < (number_of_units + 1); i++)
			{
				graphics_buffer.drawLine(position, ydim - bottom_margin, position, top_margin);
				
				String value_string = String.format("%.1f", value);
				string_width               = font_metrics.stringWidth(value_string);
				graphics_buffer.drawString(value_string, position - string_width / 2, ydim - bottom_margin + 20 + string_height);
			    value += value_increment;
				position += position_increment;
			}
			
			value_increment    = yrange / number_of_segments;
			position_increment = graph_ydim / number_of_segments;
			position           = (int)b1;
			
			if(!reverse_view)
				value = ymax;
			else
				value = ymin;
			for(int i = 0; i < (number_of_segments + 1); i++)
			{
				graphics_buffer.drawLine((int)a1, position, (int)a2, position);
				
				String value_string = String.format("%.1f", value);
				string_width               = font_metrics.stringWidth(value_string);
				graphics_buffer.drawString(value_string, (int)(a2 + 10), position + (string_height / 2));
				if(!reverse_view)
					value -= value_increment;
				else
					value += value_increment;	
				position += position_increment;
			}
			
			if(append_data == true)
			{
				graphics_buffer.setColor(java.awt.Color.RED);
				
				double x = append_y;
				x -= xmin;
				x /= xrange;
	        	x *= graph_xdim;
	        	x += left_margin;
	        	
	        	double y = append_x;
	        	y -= ymin;
	        	y /= yrange;
	        	y *= graph_ydim;
	        	if(!reverse_view)
	        	{
	        	    y = graph_ydim - y;
	        	}
	        	y += top_margin;
	        	    
				graphics_buffer.setColor(Color.BLACK);
				graphics_buffer.drawOval((int)x - 3, (int)y - 3, 7, 7);
				graphics_buffer.fillOval((int)x - 3, (int)y - 3, 7, 7);
				
				graphics_buffer.setColor(Color.RED);
				graphics_buffer.drawOval((int)x - 2, (int)y - 2, 5, 5);
				graphics_buffer.fillOval((int)x - 2, (int)y - 2, 5, 5);
			}
			

			if(startpoint_set == true)
			{
				double x = startpoint_y;
				x -= xmin;
			    x /= xrange;
        	    x *= graph_xdim;
        	    x += left_margin;
        	    
        	    double y = startpoint_x;
        	    y -= ymin;
        	    y /= yrange;
        	    y *= graph_ydim;
        	    if(!reverse_view)
        	    	y = graph_ydim - y;
        	    y += top_margin;
        	    
				int x_position = (int)x;
				int y_position = (int)y;
				
				graphics_buffer.setColor(Color.BLACK);
				graphics_buffer.drawOval(x_position - 3, y_position - 3, 7, 7);
				graphics_buffer.fillOval(x_position - 3, y_position - 3, 7, 7);
				
				graphics_buffer.setColor(Color.GREEN);
				graphics_buffer.drawOval(x_position - 2, y_position - 2, 5, 5);
				graphics_buffer.fillOval(x_position - 2, y_position - 2, 5, 5);
				
			}
			
			if(midpoint_set == true)
			{
				double x = midpoint_y;
				x -= xmin;
			    x /= xrange;
        	    x *= graph_xdim;
        	    x += left_margin;
        	    
        	    double y = midpoint_x;
        	    y -= ymin;
        	    y /= yrange;
        	    y *= graph_ydim;
        	    if(!reverse_view)
        	    	y = graph_ydim - y;
        	    y += top_margin;
        	    
				int x_position = (int)x;
				int y_position = (int)y;
				
				graphics_buffer.setColor(Color.BLACK);
				graphics_buffer.drawOval(x_position - 3, y_position - 3, 7, 7);
				graphics_buffer.fillOval(x_position - 3, y_position - 3, 7, 7);
				
				graphics_buffer.setColor(Color.GREEN);
				graphics_buffer.drawOval(x_position - 2, y_position - 2, 5, 5);
				graphics_buffer.fillOval(x_position - 2, y_position - 2, 5, 5);
				
			}
			
			if(endpoint_set == true)
			{
				double x = endpoint_y;
				x -= xmin;
			    x /= xrange;
        	    x *= graph_xdim;
        	    x += left_margin;
        	    
        	    double y = endpoint_x;
        	    y -= ymin;
        	    y /= yrange;
        	    y *= graph_ydim;
        	    if(!reverse_view)
        	    	y = graph_ydim - y;
        	    y += top_margin;
        	    
				int x_position = (int)x;
				int y_position = (int)y;
				
				graphics_buffer.setColor(Color.BLACK);
				graphics_buffer.drawOval(x_position - 3, y_position - 3, 7, 7);
				graphics_buffer.fillOval(x_position - 3, y_position - 3, 7, 7);
				
				graphics_buffer.setColor(Color.GREEN);
				graphics_buffer.drawOval(x_position - 2, y_position - 2, 5, 5);
				graphics_buffer.fillOval(x_position - 2, y_position - 2, 5, 5);
				
			}
			
			graphics.drawImage(buffered_image, 0, 0, null);	
		}   	
	}
	
	public double[] smooth(double[] source, int iterations)
	{
		int dst_length = source.length - 1;
		double[] src = source;
		double[] dst = new double[dst_length];
		while (dst_length >= source.length - iterations)
		{
			for (int i = 0; i < dst_length; i++)
			{
				dst[i] = (src[i] + src[i + 1]) / 2;
			}
			src = dst;
			dst_length--;
			if (dst_length >= source.length - iterations)
				dst = new double[dst_length];
		}
		return (dst);
	}
	
	
	public double getDistance(double x, double y, double x_origin, double y_origin)
	{
	    double distance  = Math.sqrt((x - x_origin) * (x - x_origin) + (y - y_origin) * (y - y_origin));
	    return(distance);
	}
}