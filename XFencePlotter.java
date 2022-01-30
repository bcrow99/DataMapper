import java.awt.*;
import java.awt.Color.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.*;

import java.awt.print.*;
import javax.print.*;

public class XFencePlotter
{
	double  offset         = 15;
	double  range          = 60;
	int     init_line      = 9;
	boolean autoscale      = false;
   
    boolean raster_overlay = false;
    boolean persistent_data = false;
	boolean relative_mode  = true;
    boolean reverse_view   = false;
    boolean data_scaled    = false;
    boolean data_clipped   = false;
    boolean color_key      = false;
    boolean show_id        = true;
    boolean show_label     = true;
    boolean in_order       = false;
    double  scale_factor   = 1.;
    double  normal_xstep   = 0.5;
	double  normal_ystep   = 0.5;
	double  sort_location  = 0.5;
	double  xlocation      = 0.5;
	double  ylocation      = 0.5;	
    double  minimum_y      = 0;
    double  maximum_y      = 0;
    int     smooth         = 0;
    boolean config_file_exists = false;
    String  config_filename;
    int     gui_index          = 0;
    
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
	int                startpoint_index = 0;
	boolean            startpoint_set;
	
	double             midpoint_x = 0;
	double             midpoint_y = 0;
	double             midpoint_intensity = 0;
	int                midpoint_x_position = 0;
	int                midpoint_y_position = 0;
	int                midpoint_line = 0;
	int                midpoint_sensor = 0;
	int                midpoint_index;
	boolean            midpoint_set;
	
	double             endpoint_x = 0;
	double             endpoint_y = 0;
	double             endpoint_intensity = 0;
	int                endpoint_x_position = 0;
	int                endpoint_y_position = 0;
	int                endpoint_line = 0;
	int                endpoint_sensor = 0;
	int                endpoint_index = 0;
	boolean            endpoint_set;
	
    ArrayList     sensor_id   = new ArrayList();
	ArrayList     data        = new ArrayList();
	ArrayList     sensor_data = new ArrayList();
	ArrayList     xlist       = new ArrayList();
	ArrayList[][] grid_data;
	ArrayList[][] image_grid_data;
			
	public JFrame             frame;
	public LineCanvas         data_canvas;
	public JScrollBar         range_scrollbar;
	public RangeSlider        range_slider;
	public RangeSlider        dynamic_range_slider;
	public LineImageCanvas    line_image_canvas;
	public DynamicRangeCanvas dynamic_range_canvas;
	public SegmentImageCanvas segment_image_canvas;
    public SortCanvas         sort_canvas;
    public OrderCanvas        order_canvas;
	public JTextField         load_input;
	public JTextField         graph_input;
	public JTextField         load_config_input;
	public JTextField         save_config_input;
	public JTextField         lower_bound;
	public JTextField         upper_bound;
	public JTextArea          order_information;
	public JScrollPane        order_scrollpane;
	public BufferedImage      buffered_image;

	public JDialog load_dialog;
	public JDialog save_dialog;
	public JDialog placement_dialog;
	public JDialog sort_dialog;
	public JDialog segment_image_dialog;
	public JDialog scale_dialog;
	public JDialog dynamic_range_dialog;
	public JDialog smooth_dialog;
	public JDialog location_dialog;
	public JDialog graph_dialog;
	public JDialog load_config_dialog;
	public JDialog save_config_dialog;
	public JDialog save_graph_dialog;
	
	public JDialog line_image_dialog;
	public JDialog information_dialog;
	public JDialog slope_dialog;
	public JDialog range_dialog;

	// Shared by line canvas and mouse handler.
	int    top_margin    = 20;
	int    right_margin  = 100;
	int    left_margin   = 90;
	int    bottom_margin = 80;
	
	//Shared by segment image canvas and XFencePlotter
	int     image_xdim     = 670;
	int     image_ydim     = 640;
	
	int     line_image_xdim = 870;
	int     line_image_ydim = 940;
	
	// Variables determined when reading in data.
	double global_xmin, global_xmax, global_ymin, global_ymax, intensity_min, intensity_max;

	// Updated by Range Slider, Range Scrollbar, and Range Button
	JTextField offset_information, range_information;

	// Updated by MouseMotionHandler
	JTextArea sample_information;
	JTextArea slope_output;
	// Fired by the range scrollbar and slider and adjust button and XFencePlotter
	public JMenuItem apply_item;
	
	// Fired by the config file reloader.
	public JCheckBoxMenuItem view_item;
	public JCheckBoxMenuItem number_mode_item;
	public JCheckBoxMenuItem overlay_item;
	public JCheckBoxMenuItem show_id_item;
	public JCheckBoxMenuItem color_key_item;
	public JToggleButton     autoscale_button;
	public JSlider           smooth_slider;
	public JSlider           factor_slider;
	public JScrollBar        xstep_scrollbar;
	public JScrollBar        ystep_scrollbar;
	// Shared by the range scrollbar and slider and adjust button.
	boolean range_slider_changing    = false;
	boolean range_scrollbar_changing = false;
	boolean range_button_changing    = false;

	// Shared by dynamic range slider and adjust buttons.
	boolean dynamic_slider_changing = false;
	boolean dynamic_button_changing = false;

	// X and Y Step Handlers and View Handler call repaint()
	PlacementCanvas placement_canvas;
	
	// X and Y Location Handlers call repaint()
	LocationCanvas  location_canvas;
	
	// Shared by dynamic range control and autoscale control.
	JButton reset_bounds_button;
	
	
	
	// Referenced by location canvas and modified by location scrollbars.
	boolean location_changing;
	
	// Shared by line canvas and placement_canvas.
	JTextField[] sensor        = new JTextField[10];
	Canvas[]     sensor_canvas = new SensorCanvas[10];
	int[]        sensor_state  = new int[10];
	boolean[]    visible       = new boolean[10];
	boolean[]    transparent   = new boolean[10];
	Color[]      outline_color = new Color[10];
	Color[]      fill_color    = new Color[10];
	
	public static void main(String[] args)
	{
		if(args.length != 1)
		{
			System.out.println("Usage: XFencePlotter <data file>");
			System.exit(0);
		} 
		else
		{
			System.out.println("This is version 3 of fence.");
			try
			{
				try
				{
					XFencePlotter window = new XFencePlotter(args[0]);
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

	public XFencePlotter(String filename)
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
			String config_filename = new String(directory + "fence.cfg");
			File config_file = new File(config_filename);
			if(config_file.exists())
			{
				//System.out.println("Loading config file.");
				config_file_exists = true;
				try
				{
				    BufferedReader config_reader   = new BufferedReader(new InputStreamReader(new FileInputStream(config_file)));
				    
				    String line;
				    StringTokenizer config_tokenizer;
				    
				    // Get line sensor pairs.
				    line                 = config_reader.readLine();
				    config_tokenizer     = new StringTokenizer(line);
				    int number_of_tokens = config_tokenizer.countTokens();
				    String token         = config_tokenizer.nextToken();
				    number_of_tokens--;
				    for(int i = 0; i < number_of_tokens; i++)
				    {
				    	token = config_tokenizer.nextToken();
				    	// Cannot instantiate arrays of non-primitive types statically,
				    	// so we have to save the id and assign it later.
				    	sensor_id.add(token);
				    }
				    
				    // Get and set visibility.
				    line = config_reader.readLine();
				    config_tokenizer = new StringTokenizer(line);
				    number_of_tokens = config_tokenizer.countTokens();
				    token = config_tokenizer.nextToken();
				    number_of_tokens--;
				    for(int i = 0; i < number_of_tokens; i++)
				    {
				    	token = config_tokenizer.nextToken();
				    	if(token.equals("true"))
				    		visible[i] = true;
				    	else
				    		visible[i] = false;
				    }
				    
				    // Get and set transparency.
				    line = config_reader.readLine();
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
				    
				    // Skip line.
				    line = config_reader.readLine();
				    
				    while(line != null)
				    {
				        line             = config_reader.readLine();
				        if(line != null)
				        {
				            config_tokenizer = new StringTokenizer(line);
				            String key       = config_tokenizer.nextToken();
				            String value     = config_tokenizer.nextToken();
				            if(key.equals("Offset"))
				    	        offset = Double.valueOf(value);
				            else if(key.equals("Range")) 
				        	    range = Double.valueOf(value);
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
				            else if(key.equals("Autoscale")) 
					        {
					        	if(value.equals("true"))
					        		autoscale = true;
					        	else
					        		autoscale = false;
					        } 
				            else if(key.equals("ShowID")) 
					        {
					        	if(value.equals("true"))
					        		show_id = true;
					        	else
					        		show_id = false;
					        } 
				            else if(key.equals("ColorKey")) 
					        {
					        	if(value.equals("true"))
					        		color_key = true;
					        	else
					        		color_key = false;
					        } 
				            else if(key.equals("Smooth")) 
				        	    smooth = Integer.parseInt(value);
				            else if(key.equals("XStep"))
					    	    normal_xstep = Double.valueOf(value);
					        else if(key.equals("YStep")) 
					        	normal_ystep = Double.valueOf(value);
					        else if(key.equals("SortLocation")) 
					        	sort_location = Double.valueOf(value); 
					        else if(key.equals("XLocation"))
					    	    xlocation = Double.valueOf(value);
					        else if(key.equals("YLocation")) 
					        	ylocation = Double.valueOf(value);
					        else if(key.equals("InOrder")) 
					        {
					        	if(value.equals("true"))
					        		in_order = true;
					        	else
					        		in_order = false;	
					        }
					        else if(key.equals("Scaling")) 
					        {
					        	if(value.equals("true"))
					        		data_scaled = true;
					        	else
					        		data_scaled = false;
					        } 
					        else if(key.equals("ScaleFactor")) 
					        	scale_factor = Double.valueOf(value);
					        else if(key.equals("Scaling")) 
					        {
					        	if(value.equals("true"))
					        		data_scaled = true;
					        	else
					        		data_scaled = false;
					        } 
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
				}
				catch(Exception e)
				{
					System.out.println("Exception reading config file.");
					//System.out.println(e.toString());
					// Could reset defaults here.
					config_file_exists = false;
				}
			}
			
			ArrayList original_data = new ArrayList();
			global_xmin = Double.MAX_VALUE;
			global_xmax = Double.MIN_VALUE;
			global_ymin = Double.MAX_VALUE;
			global_ymax = Double.MIN_VALUE;
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
					if (x < global_xmin)
						global_xmin = x;
					else if (x > global_xmax)
						global_xmax = x;
					if (y < global_ymin)
						global_ymin = y;
					else if (y > global_ymax)
						global_ymax = y;
					double intensity = Double.valueOf(number_tokens.nextToken());
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
								if (x < global_xmin)
									global_xmin = x;
								else if (x > global_xmax)
									global_xmax = x;
								if (y < global_ymin)
									global_ymin = y;
								else if (y > global_ymax)
									global_ymax = y;
								double intensity = Double.valueOf(number_tokens.nextToken());
								Sample current_sample = new Sample(x, y, intensity);
								original_data.add(current_sample);
							}
						}
					} 
					catch (IOException e)
					{
						System.out.println("Unexpected error " + e.toString());
					}
				}
				reader.close();
				
				for (int i = 0; i < original_data.size(); i++)
				{
					Sample original_sample = (Sample) original_data.get(i);
					Sample sample = new Sample(original_sample.x, original_sample.y, original_sample.intensity);
					sample.x -= global_xmin;
					sample.y -= global_ymin;
					sample.index = i;
					data.add(sample);
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

		frame = new JFrame("XFence Plotter");
		WindowAdapter window_handler = new WindowAdapter()
	    {
	        public void windowClosing(WindowEvent event)
	        {
	        	try
	            {
	            	PrintWriter output  = new PrintWriter("fence.cfg");	
	            	String id           = new String("");
	            	String _visible     = new String("");
	            	String _transparent = new String("");
	            	for(int i = 0; i < 10; i++)
	            	{
	            	    id = new String(id + sensor[i].getText() + "\t");
	            	    if(visible[i])
	            	    	_visible = new String(_visible + "true\t");
	            	    else
	            	    	_visible = new String(_visible + "false\t");
	            	    if(transparent[i])
	            	    	_transparent = new String(_transparent + "true\t");
	            	    else
	            	    	_transparent = new String(_transparent + "false\t");	  
	            	}
	            	output.write("SensorID\t" + id + "\n");
	            	output.write("Visible\t\t" + _visible + "\n");
	            	output.write("Transparent\t" + _transparent + "\n\n");
	            	output.write("Offset\t\t\t" + String.format("%,.4f", offset) + "\n");
	            	output.write("Range\t\t\t" + String.format("%,.4f", range) + "\n");
	            	output.write("SortLocation\t" + String.format("%,.3f", sort_location) + "\n");
	            	
	            	//Don't think we need this to start up program, but might be useful to know.
	            	if(in_order)
	            		output.write("InOrder\t\t\ttrue\n");
	            	else
	            		output.write("InOrder\t\t\tfalse\n");
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
	            	if(autoscale)
	            		output.write("Autoscale\t\ttrue\n");
	            	else
	            		output.write("Autoscale\t\tfalse\n");
	            	if(show_id)
	            		output.write("ShowID\t\t\ttrue\n");
	            	else
	            		output.write("ShowID\t\t\tfalse\n");
	            	if(color_key)
	            		output.write("ColorKey\t\ttrue\n");
	            	else
	            		output.write("ColorKey\t\tfalse\n");
	            	if(data_scaled)
	            		output.write("Scaling\t\t\ttrue\n");
	            	else
	            		output.write("Scaling\t\t\tfalse\n");
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
	            	output.write("AppendY\t\t\t" + String.format("%,.2f", append_y) + "\n");
	            	output.write("AppendIntensity\t" + String.format("%,.2f", append_intensity) + "\n");
	            	
	            	String decimal_string = String.format("%,.2f", append_x_abs);
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
	        		System.out.println(e.toString());
	        	}
	            System.exit(0);
	        }
	    };
	    frame.addWindowListener(window_handler);
	    
		data_canvas = new LineCanvas();
		data_canvas.setSize(900, 700);
		MouseHandler mouse_handler = new MouseHandler();
		data_canvas.addMouseListener(mouse_handler);
		MouseMotionHandler mouse_motion_handler = new MouseMotionHandler();
		data_canvas.addMouseMotionListener(mouse_motion_handler);
		JPanel canvas_panel = new JPanel(new BorderLayout());
		canvas_panel.add(data_canvas, BorderLayout.CENTER);
		
		range_scrollbar        = new JScrollBar(JScrollBar.HORIZONTAL, 2000, 3, 0, 2003);
		double normal_start    = (offset - 15) / 60;
		double normal_stop     = (offset + range - 15) / 60;
		double normal_range    = normal_stop - normal_start;
		
		RangeScrollbarHandler range_scrollbar_handler = new RangeScrollbarHandler();
		range_scrollbar.addAdjustmentListener(range_scrollbar_handler);	
		
		
		// Taking advantage of an implementation detail and setting a semaphore
		// so the scrollbar handler doesn't do anything when starting up.
		range_slider_changing = true;
		
		double normal_position = normal_start + normal_range / 2;
		normal_position       *= 2000;
		range_scrollbar.setValue((int)normal_position);
		range_slider_changing = false;
		
		range_slider = new RangeSlider();
		range_slider.setMinimum(0);
		range_slider.setMaximum(2000);
		RangeSliderHandler range_slider_handler = new RangeSliderHandler();
		range_slider.addChangeListener(range_slider_handler);
		
		range_scrollbar_changing = true;
		normal_position  = (offset + range - 15) / 60;
		normal_position *= 2000;
		range_slider.setUpperValue((int)normal_position);
		normal_position  = (offset - 15) / 60;
		normal_position *= 2000;
		range_slider.setValue((int)normal_position);
		range_scrollbar_changing = false;
	
		JPanel segment_panel = new JPanel(new BorderLayout());
		segment_panel.add(range_scrollbar, BorderLayout.NORTH);
		segment_panel.add(range_slider, BorderLayout.SOUTH);
		canvas_panel.add(segment_panel, BorderLayout.SOUTH);
		frame.getContentPane().add(canvas_panel, BorderLayout.CENTER);

		JPanel sensor_panel = new JPanel(new GridLayout(2, 10));
		for (int i = 0; i < 10; i++)
		{
			sensor[i]       = new JTextField();
			sensor_state[i] = 0;
			sensor[i].setHorizontalAlignment(JTextField.CENTER);
			sensor_panel.add(sensor[i]);
			
			if(!config_file_exists)  
			{
				//Default for visibility and transparency.
			    visible[i]     = true;
			    transparent[i] = false;
			}
			else
			{
				// Config file exists so we should have sensor ids.
				if(i < sensor_id.size())
				{
				    String line_sensor = (String)sensor_id.get(i);
				    sensor[i].setText(line_sensor);
				    if(visible[i] == true && transparent[i] == false)
				    	sensor_state[i] = 0;
				    else if(visible[i] == true && transparent[i] == true)
					    sensor_state[i] = 1;
				    else
				    	sensor_state[i] = 2;
				}
				else // There may be less than 10 sensor_ids.
				{
					sensor[i].setText("");
					sensor_state[i] = 2;
				}
			}
			ActionListener sensor_handler = new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					apply_item.doClick();
				}
			};
			sensor[i].addActionListener(sensor_handler);
		}

		if(!config_file_exists)
		{
			// Initialize sensor ids using init_line
			for (int i = 0; i < 5; i++)
			{
				if (init_line % 2 == 1)
				{
					String line_sensor = new String(init_line + ":" + i);
					sensor[i].setText(line_sensor);
				} 
				else
				{
					String line_sensor = new String(init_line + ":" + (4 - i));
					sensor[i].setText(line_sensor);
				}
			}
		    for (int i = 0; i < 5; i++)
		    {
		    	
			    if((init_line + 1) % 2 == 1)
			    {
				    String line_sensor = new String((init_line + 1) + ":" + i);
				    sensor[i + 5].setText(line_sensor);
			    } 
			    else
			    {
				    String line_sensor = new String((init_line + 1) + ":" + (4 - i));
				    sensor[i + 5].setText(line_sensor);
			    }
		    }
		}
		
		for (int i = 0; i < 10; i++)
		{
			sensor_canvas[i] = new SensorCanvas(i);
			SensorCanvasMouseHandler sensor_canvas_mouse_handler = new SensorCanvasMouseHandler(i);
			sensor_canvas[i].addMouseListener(sensor_canvas_mouse_handler);
			sensor_panel.add(sensor_canvas[i]);
		}
		
		lower_bound = new JTextField();
		upper_bound = new JTextField();
		if(config_file_exists)
		{
			String lower_bound_string = String.format("%.4f", minimum_y);
			String upper_bound_string = String.format("%.4f", maximum_y);
			lower_bound.setText(lower_bound_string);
			upper_bound.setText(upper_bound_string); 
		}
		JMenuBar menu_bar = new JMenuBar();

		JMenu     file_menu  = new JMenu("File");
		
		apply_item           = new JMenuItem("Apply");
		JMenuItem print_item = new JMenuItem("Print");
		JMenuItem save_item  = new JMenuItem("Save");
		JMenuItem load_item  = new JMenuItem("Load Segment");
		ActionListener load_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();

				x += 830;

				load_dialog.setLocation(x, y);
				load_dialog.pack();
				load_dialog.setVisible(true);
			}
		};
		load_item.addActionListener(load_handler);
		
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
		
		ApplyHandler apply_handler = new ApplyHandler();
		apply_item.addActionListener(apply_handler);
		
		PrintHandler print_handler = new PrintHandler();
		print_item.addActionListener(print_handler);
		
		SaveHandler  save_handler = new SaveHandler();
		save_item.addActionListener(save_handler);

		file_menu.add(load_item);
		
		// This is almost obsolete, but still helps when 
		// graphing non-sequential segments created by
		// editing sensor entries by hand.
		// All other changes are automatically applied.
		file_menu.add(apply_item);
		
		// File->Print works but produces a low quality print.
		// Not worth trying to make it look better when
		// we can just print from Photoshop or Gimp instead.
		// Still leaving the hook in here.  We might want 
		// to print text for one thing.
		file_menu.add(print_item);
		
		file_menu.add(save_item);
        
		file_menu.add(load_config_item);
        
		file_menu.add(save_config_item);
        
        JMenu format_menu = new JMenu("Format");
		
		// A modeless dialog box that shows up if Format->Show Data is selected.
		JPanel information_panel = new JPanel(new BorderLayout());
		sample_information = new JTextArea(8, 17);
		information_panel.add(sample_information);
		information_dialog = new JDialog(frame);
		information_dialog.add(information_panel);		
		JMenuItem data_item = new JMenuItem("Show Data");
		ActionListener data_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();
						
				Dimension canvas_dimension = data_canvas.getSize();
				double    canvas_xdim      = canvas_dimension.getWidth();
						
				x += canvas_xdim;
				information_dialog.setLocation(x, y);
				information_dialog.pack();
				information_dialog.setVisible(true);
			}
		};
		data_item.addActionListener(data_handler);
		format_menu.add(data_item);
		
		JMenuItem place_item = new JMenuItem("Placement");
		ActionListener placement_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();

				x += 830;
				y += 95;

				if (y < 0)
					y = 0;

				placement_dialog.setLocation(x, y);
				placement_dialog.pack();
				placement_dialog.setVisible(true);
			}
		};
		place_item.addActionListener(placement_handler);
		
		JMenuItem sort_item = new JMenuItem("Sort");
		ActionListener sort_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();

				x += 830;

				sort_dialog.setLocation(x, y);
				sort_dialog.pack();
				sort_dialog.setVisible(true);
			}
		};
		sort_item.addActionListener(sort_handler);

		
		JMenuItem label_item  = new JMenuItem("Graph Label");
		ActionListener label_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();

				x += 830;

				graph_dialog.setLocation(x, y);
				graph_dialog.pack();
				graph_dialog.setVisible(true);
			}
		};
		label_item.addActionListener(label_handler);
		
		JPanel     range_panel = new JPanel(new BorderLayout());
		JTextField range_input = new JTextField();
		range_input.setHorizontalAlignment(JTextField.CENTER);
		range_input.setText("");
		ActionListener range_input_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
			    String input = range_input.getText();
			    double input_range = Double.valueOf(input);
			    double range_delta = 0;
			    if(input_range <= 0 || input_range > 60)
			    {
			        System.out.println("Range must be more than 0 and less than 60.");
			        return;
			    }
			    else
			    {	
			    	range_delta = input_range - range;
			    	range = input_range;
			    }
			    double normal_range = range / 60.;
			    
			    int scrollbar_value = range_scrollbar.getValue();
			    
			    double normal_position = scrollbar_value;
			    normal_position /= 2000;
			    
			    // Using existing semaphores instead of implementing
			    // a separate semaphore for the text input.  Otherwise,
			    // we need to go back and check for that semaphore in
			    // other parts of the program.
			    if(normal_position - normal_range / 2 < 0)
			    {
			    	range_slider_changing = true;
			    	range_scrollbar.setValue(0);
			    	offset = 15;
			    	range_slider_changing = false;	
			    }
			    else if(normal_position + normal_range / 2 > 1)
			    {
			    	range_slider_changing = true;
			    	range_scrollbar.setValue(2000);
			    	offset = 60 - range + 15;
			    	range_slider_changing = false;   	
				}
			    else
			    	offset -= range_delta / 2;
			    range_scrollbar_changing = true;
			    int lower_value = (int) ((offset - 15) / 60 * 2000);
			    int upper_value = (int) ((offset + range - 15) / 60 * 2000);
			    range_slider.setValue(lower_value);
		    	range_slider.setUpperValue(upper_value);
			    range_scrollbar_changing = false;
			    apply_item.doClick();
			    
			}
		};
        range_input.addActionListener(range_input_handler);
        range_panel.add(range_input);		
        range_dialog = new JDialog(frame, "Range");
        range_dialog.add(range_panel);
		
		JMenuItem range_item = new JMenuItem("Set Range");
		ActionListener range_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();

				Dimension canvas_dimension = data_canvas.getSize();
				double    canvas_xdim      = canvas_dimension.getWidth();
				
				x += canvas_xdim;
				
				y += 400;

				range_dialog.setLocation(x, y);
				range_dialog.pack();
				range_dialog.setVisible(true);
			}
		};
		range_item.addActionListener(range_handler);
		
		
		format_menu.add(place_item);
		format_menu.add(sort_item);
		format_menu.add(label_item);
		format_menu.add(range_item);
		
		JMenu adjustments_menu = new JMenu("Adjustments");
		JMenu slope_menu       = new JMenu("Slope");

		JMenuItem scaling_item = new JMenuItem("Scaling");
		ActionListener scale_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();

				x += 830;
				y += 540;

				scale_dialog.setLocation(x, y);
				scale_dialog.pack();
				scale_dialog.setVisible(true);
			}
		};
		scaling_item.addActionListener(scale_handler);
		
		JMenuItem smoothing_item = new JMenuItem("Smoothing");
		ActionListener smooth_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();
				x += 830;
				y += 640;
				smooth_dialog.setLocation(x, y);
				smooth_dialog.pack();
				smooth_dialog.setVisible(true);
			}
		};
		smoothing_item.addActionListener(smooth_handler);
		

		JMenuItem dynamic_range_item = new JMenuItem("Dynamic Range");
		ActionListener dynamic_range_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();

				x -= 150;
				y += 35;

				double seg_min = (double) sensor_data.get(0);
				double seg_max = (double) sensor_data.get(1);
				double line_min = (double) sensor_data.get(2);
				double line_max = (double) sensor_data.get(3);
                if(!data_clipped)
                {
				    if (autoscale)
				    {
					    lower_bound.setText(String.format("%,.2f", seg_min));
					    upper_bound.setText(String.format("%,.2f", seg_max));
				    }
				    else
				    {
					    lower_bound.setText(String.format("%,.2f", line_min));
					    upper_bound.setText(String.format("%,.2f", line_max));
				    }
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

		adjustments_menu.add(smoothing_item);
		adjustments_menu.add(scaling_item);
		adjustments_menu.add(dynamic_range_item);
		
		
		JMenu     location_menu  = new JMenu("Location");
		JMenuItem location_item = new JMenuItem("Show Map");
		ActionListener location_handler = new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
			    Point location_point = frame.getLocation();
			    int x = (int) location_point.getX();
			    int y = (int) location_point.getY();

			    x += 830;
			    location_dialog.setLocation(x, y);
			    location_dialog.pack();
			    location_dialog.setVisible(true);
		    }
		};
		location_item.addActionListener(location_handler);
		
		JMenuItem segment_image_item = new JMenuItem("Show Segment Image");
		ActionListener segment_image_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();

				x += 830;
				segment_image_dialog.setLocation(x, y);
				segment_image_dialog.pack();
				segment_image_dialog.setVisible(true);
			}
		};
		segment_image_item.addActionListener(segment_image_handler);
		
		JMenuItem line_image_item = new JMenuItem("Show Line Image");
		ActionListener line_image_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				Point location_point = frame.getLocation();
				int x = (int) location_point.getX();
				int y = (int) location_point.getY();

				x += 830;
				line_image_dialog.setLocation(x, y);
				line_image_dialog.pack();
				line_image_dialog.setVisible(true);
			}
		};
		line_image_item.addActionListener(line_image_handler);
		
		location_menu.add(location_item);
		location_menu.add(segment_image_item);
		location_menu.add(line_image_item);
		
		JMenu settings_menu = new JMenu("Settings");
		view_item = new JCheckBoxMenuItem("Reverse View");
		ActionListener view_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
            {
            	JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
            	if(reverse_view == true)
				{
            		reverse_view = false;
					item.setState(false);
					placement_canvas.repaint();
				}
				else
				{
					reverse_view = true;
					item.setState(true);
					placement_canvas.repaint();
				}
            	if(append_data)
				{
            		int number_of_segments = sensor_data.size() - 4;
					Dimension canvas_dimension = data_canvas.getSize();
					int       canvas_xdim      = (int)canvas_dimension.getWidth();
					int       canvas_ydim      = (int)canvas_dimension.getHeight();
					
					double    max_xstep        = (canvas_xdim - (left_margin + right_margin)) / number_of_segments;
					int       xstep            = (int) (max_xstep * normal_xstep);
					
					double    max_ystep        = (canvas_ydim - (top_margin + bottom_margin)) / number_of_segments;
					int       ystep            = (int) (max_ystep * normal_ystep);
					
					int delta_x = 0;
					int delta_y = 0;
						
					if(number_of_segments % 2 == 1)
					{
						for(int i = 0; i < number_of_segments / 2; i++)
						{
							int k = (number_of_segments - 1) / (i + 1);
							if(gui_index == i)
							{
							    if(reverse_view)
							    {
							        delta_x =  k * xstep;
							        delta_y = -k * ystep;
							    }
							    else
							    {
							    	delta_x = -k * xstep;
							        delta_y =  k * ystep;   	
							    }
							}
							else if(gui_index == number_of_segments - 1 - i)
							{
								if(reverse_view)
							    {
							        delta_x = -k * xstep;
							        delta_y =  k * ystep;
							    }
							    else
							    {
							    	delta_x =  k * xstep;
							        delta_y = -k * ystep;   	
							    }	
							}
						}
					}
					else
					{
						for(int i = 0; i < number_of_segments / 2; i++)
						{
							int k = number_of_segments - (2 * i) - 1;
							if(gui_index == i)
							{
							    if(reverse_view)
							    {
							        delta_x =  k * xstep;
							        delta_y = -k * ystep;
							    }
							    else
							    {
							    	
							    	delta_x = -k * xstep;
							        delta_y =  k * ystep;  
							    }
							}
							else if(gui_index == number_of_segments - 1 - i)
							{
								if(reverse_view)
							    {
							        delta_x = -k * xstep;
							        delta_y =  k * ystep;
							    }
							    else
							    {
							    	delta_x =  k * xstep;
							        delta_y = -k * ystep;   
							    }	
							}
						}        
					}
					
					append_x_position += delta_x;
				    append_y_position += delta_y;
				}
		        data_canvas.repaint();
            }   	
		};
		view_item.addActionListener(view_handler);
		if(reverse_view)
			view_item.setState(true);
        
		number_mode_item = new JCheckBoxMenuItem("Relative Mode");
		if(relative_mode)
		    number_mode_item.setState(true);
		number_mode_item.addActionListener(new ActionListener() 
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
        });
		
        overlay_item = new JCheckBoxMenuItem("Raster Overlay");
        if(raster_overlay == true)
        	overlay_item.setState(true);	
        overlay_item.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) 
            {
            	JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
            	if(raster_overlay == true)
				{
					raster_overlay = false;
					item.setState(false);
				}
				else
				{
					raster_overlay = true;
					item.setState(true);
				}
		        data_canvas.repaint();
            }
        });
		
		
		color_key_item = new JCheckBoxMenuItem("Color Key");
		ActionListener key_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
            {
            	JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
            	if(color_key == true)
				{
            		color_key = false;
					item.setState(false);
					data_canvas.repaint();;
				}
				else
				{
					color_key = true;
					item.setState(true);
					data_canvas.repaint();
				}
            }   	
		};
		color_key_item.addActionListener(key_handler);
		if(color_key)
			color_key_item.setState(true);
		else
			color_key_item.setState(false);
		
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
					item.setState(true);
				}
		        data_canvas.repaint();
            }   	
		};
		show_id_item.addActionListener(show_id_handler);
		if(show_id)
			show_id_item.setState(true);
		
		
		settings_menu.add(view_item);
		settings_menu.add(overlay_item);
		settings_menu.add(number_mode_item);
		settings_menu.add(show_id_item);
		settings_menu.add(color_key_item);
		
		
		
	
		menu_bar.add(file_menu);
		menu_bar.add(format_menu);
		menu_bar.add(adjustments_menu);
		menu_bar.add(slope_menu);
		menu_bar.add(location_menu);
		menu_bar.add(settings_menu);

		// A modeless dialog box that shows up if File->Load is selected.
		JPanel load_panel = new JPanel(new GridLayout(2, 1));
		load_input             = new JTextField();
		load_input.setHorizontalAlignment(JTextField.CENTER);
		load_input.setText("");

		JButton load_button = new JButton("Load Segment");
		LoadInputHandler load_input_handler = new LoadInputHandler();
		load_button.addActionListener(load_input_handler);

		load_panel.add(load_input);
		load_panel.add(load_button);

		load_dialog = new JDialog(frame);
		load_dialog.add(load_panel);

		// A modeless dialog box that shows up if File->Load Config is selected.
		JPanel load_config_panel = new JPanel(new GridLayout(2, 1));
		load_config_input        = new JTextField(30);
		load_config_input.setHorizontalAlignment(JTextField.CENTER);
		load_config_input.setText("");
		load_config_panel.add(load_config_input);
		JButton load_config_button = new JButton("Load Config");
		ActionListener load_config_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String filename = load_config_input.getText();
				
				String suffix = new String(".cfg");
				System.out.println("Loading " + filename + suffix);
				File config_file = new File(filename + suffix);
				if(config_file.exists())
				{
					try
					{
					    BufferedReader config_reader   = new BufferedReader(new InputStreamReader(new FileInputStream(config_file)));
					    String line;
					    StringTokenizer config_tokenizer;
					    
					    // Get line sensor pairs.
					    line                  = config_reader.readLine();
					    config_tokenizer      = new StringTokenizer(line);
					    int number_of_tokens  = config_tokenizer.countTokens();
					    int number_of_sensors = number_of_tokens - 1;
					    // Key
					    String token          = config_tokenizer.nextToken();
					    
					    number_of_tokens--;
					    for(int i = 0; i < number_of_tokens; i++)
					    {
					    	// Values
					    	token = config_tokenizer.nextToken();
					    	// Cannot instantiate arrays of non-primitive types statically at startup,
					    	// so we have to save the id at startup and assign it later in the initialization process.
					    	// Sensor_id is an array of strings that is only used starting up from config file.
					    	// sensor_id.add(token);
					    	
					    	//Should work now that program is initialized.
					    	sensor[i].setText(token);
					    }
					    // Set empty string sensor id's if number of sensors is less than 10.
					    if(number_of_sensors < 10)
					    {
					    	for(int i = number_of_sensors; i < 10; i++)
					    		sensor[i].setText("");
					    }
					    //System.out.println("Reassigned sensor id's.");
				        
					    line = config_reader.readLine();
					    config_tokenizer = new StringTokenizer(line);
					    number_of_tokens = config_tokenizer.countTokens();
					    // Key
					    token = config_tokenizer.nextToken();
					    number_of_tokens--;
					    for(int i = 0; i < number_of_tokens; i++)
					    {
					    	// Values
					    	token = config_tokenizer.nextToken();
					    	//System.out.println("Token is " + token);
					    	if(token.equals("true"))
					    		visible[i] = true;
					    	else
					    		visible[i] = false;
					    }
					    
					    line = config_reader.readLine();
					    config_tokenizer = new StringTokenizer(line);
					    number_of_tokens = config_tokenizer.countTokens();
					    // Key
					    token = config_tokenizer.nextToken();
					    number_of_tokens--;
					    for(int i = 0; i < number_of_tokens; i++)
					    {
					    	// Values.
					    	token = config_tokenizer.nextToken();
					    	//System.out.println("Token is " + token);
					    	if(token.equals("true"))
					    		transparent[i] = true;
					    	else
					    		transparent[i] = false;
					    }
					    
					    //System.out.println("Reassigned visibility and transparency.");
					    // For convenience, the gui references a state table
					    // that combines visible/transparent information.
					    for(int i = 0; i < 10; i++)
					    {
					    	if(visible[i])
					    	{
					    		if(transparent[i])
					    			sensor_state[i] = 1;
					    		else
					    			sensor_state[i] = 0;
					    	}
					    	else
					    		sensor_state[i] = 2;	
					    	sensor_canvas[i].repaint();
					    }
					    
					    // Skip blank line.
					    line = config_reader.readLine();
					    
					    
					    while(line != null)
					    {
					        line             = config_reader.readLine();
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
					    	        offset = Double.valueOf(value);
					            else if(key.equals("Range")) 
					        	    range = Double.valueOf(value);
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
						        			number_mode_item.doClick();
						        	}
						        	else
						        	{
						        		if(relative_mode)
						        			number_mode_item.doClick();	
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
					            else if(key.equals("Autoscale")) 
						        {
						        	if(value.equals("true"))
						        	{
						        		if(!autoscale)
						        			autoscale_button.doClick();
						        	}
						        	else
						        	{
						        		if(autoscale)
						        			autoscale_button.doClick();
						        	}
						        } 
					            else if(key.equals("Smooth")) 
					            {
					        	    smooth = Integer.parseInt(value);
					        	    smooth_slider.setValue(smooth);
					            }
					            // Need to reset gui but need current values
					            // for both scaling and scale factor.
					            // Process at end.
					            else if(key.equals("Scaling")) 
						        {
						        	if(value.equals("true"))
						        		data_scaled = true;
						        	else
						        		data_scaled = false;
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
						        else if(key.equals("SortLocation")) 
						        	sort_location = Double.valueOf(value); 
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
					    
					    
					    // We now have current setting for all parameters and can make the gui consistent.
					    
					    // Reset scaling slider to current settings.  
					    if(data_scaled)
					    {
					        int value = (int)((scale_factor - 1.) * 100.);	
					        factor_slider.setValue(value);
					    }
					    // Make it consistent with boolean.
					    else
					    {
					    	factor_slider.setValue(0);
					    	scale_factor = 1.0;
					    }
					    
					    // So dynamic range comes up right.
					    if(data_clipped)
					    {
					    	lower_bound.setText(String.format("%,.2f", minimum_y));	
		                	upper_bound.setText(String.format("%,.2f", maximum_y));	
					    }
					    
					    // So placement dialog comes up right.
					    xstep_scrollbar.setValue((int)normal_xstep * 100);
					    ystep_scrollbar.setValue((int)normal_ystep * 100);
					    
					    // Reset scrollbar and slider to current settings. 
					    double normal_range           = range / 60.;
					    double normal_start_position  = (offset - 15) /60;
					    double normal_end_position    = normal_start_position + normal_range;
					    double normal_center_position = normal_start_position + normal_range / 2;
					    
					    // The slider will fire the scrollbar and ad infinitum, 
					    // unless it knows it's being fired by the scrollar and not the user.
					    range_scrollbar_changing = true;
					    
					    // We have to know the current state of the slider to reset it correctly;
					    // This should be hidden in the implementation of the range slider but
					    // we can deal with it.
					    
					    int slider_lower_value = range_slider.getValue();
					    int slider_upper_value = range_slider.getUpperValue();
					    
					    double current_start_position = slider_lower_value;
					    double current_end_position   = slider_upper_value;
					    current_start_position /= 2000;
					    current_end_position   /= 2000;
					    
					    slider_lower_value = (int)(normal_start_position * 2000.);
					    slider_upper_value = (int)(normal_end_position * 2000.);
					    
					    if(current_end_position > normal_start_position)
					    {
					        range_slider.setValue(slider_lower_value);
					        range_slider.setUpperValue(slider_upper_value);
					    }
					    else
					    {
					        range_slider.setUpperValue(slider_upper_value);	
					        range_slider.setValue(slider_lower_value);
					    }
					    
					    range_scrollbar_changing = false;
					    
					    //Likewise with the scrollbar--setting semaphores to prevent oscillation.
					    range_slider_changing = true;
					    
					    int scrollbar_value = (int)(normal_center_position * 2000.);
					    range_scrollbar.setValue(scrollbar_value);
					    
					    range_slider_changing = false;
					    
					    System.out.println("Finished resetting parameters.");
					    
					    // If we change any parameter that affects what
					    // data segment we're looking at, we need to
					    // resegment the data besides repainting the data canvas.
					    // The apply item handler -- which does the segmenting
					    // in the fence program but not the wand program,
					    // will repaint the data canvas.
					    // Might want to check if we actually need to resegment
					    // data and just call data_canvas.repaint().
					    apply_item.doClick();  
					}
					catch(Exception e2)
					{
						// Could try loading standard config file.
					    System.out.println("Exception reading config file.");
					}
				}
			}
		};
		load_config_button.addActionListener(load_config_handler);
		load_config_panel.add(load_config_button);
		load_config_dialog = new JDialog(frame, "Load Config");
		load_config_dialog.add(load_config_panel, BorderLayout.CENTER);
		
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
				String filename = save_config_input.getText();
				System.out.println("Saving to " + filename);
			}
		};
		save_config_button.addActionListener(save_config_handler);
		save_config_panel.add(save_config_button);
		save_config_dialog = new JDialog(frame, "Save Config");
		save_config_dialog.add(save_config_panel, BorderLayout.CENTER);
		
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

		
		JPanel slope_panel = new JPanel(new BorderLayout());
		slope_output = new JTextArea(22, 10);
		JPanel slope_button_panel = new JPanel(new GridLayout(2,3));
		
		
		JButton        start_button       = new JButton("Start");
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
				startpoint_set = true;
				append_data    = false;
				persistent_data = false;
				sample_information.setText("");
				data_canvas.repaint();
			}
		};
		start_button.addActionListener(startpoint_handler);
		
		JButton midpoint_button  = new JButton("Midpoint");
		ActionListener midpoint_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				midpoint_x = append_x;
				midpoint_y = append_y;
				midpoint_intensity = append_intensity;
				midpoint_intensity = append_intensity;
				midpoint_x_position = append_x_position;
				midpoint_y_position = append_y_position;
				midpoint_line       = append_line;
				midpoint_sensor     = append_sensor;
				midpoint_index    = append_index;
				midpoint_set = true;
				append_data  = false;
				persistent_data = false;
				sample_information.setText("");
				data_canvas.repaint();
			}
		};
		midpoint_button.addActionListener(midpoint_handler);
		
		JButton end_button       = new JButton("End");
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
				endpoint_set        = true;
				append_data         = false;
				persistent_data     = false;
				sample_information.setText("");
				data_canvas.repaint();
			}
		};
		end_button.addActionListener(endpoint_handler);
		
		
		slope_button_panel.add(start_button);
		slope_button_panel.add(midpoint_button);
		slope_button_panel.add(end_button);
		
		JButton slope_apply_button = new JButton("Apply");
		ActionListener slope_apply_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(startpoint_set && midpoint_set && endpoint_set)
				{
	            	slope_output.append("start_intensity " + String.format("%.2f",startpoint_intensity) + " nT\n");
	            	slope_output.append("start_x " + String.format("%.2f", startpoint_x) + " m\n");
	            	slope_output.append("start_y " + String.format("%.2f", startpoint_y) + " m\n");
	            	slope_output.append("start_line_sensor " + startpoint_line + ":" + startpoint_sensor + "\n");
	            	
	            	slope_output.append("mid_intensity " + String.format("%.2f", midpoint_intensity) +  " nT\n");
	            	slope_output.append("mid_x " + String.format("%.2f", midpoint_x) + " m\n");
	            	slope_output.append("mid_y " + String.format("%.2f", midpoint_y) + " m\n");
	            	slope_output.append("mid_line_sensor " + midpoint_line + ":" + midpoint_sensor + "\n");
	            	
	            	slope_output.append("end_intensity " + String.format("%.2f", endpoint_intensity) + " nT\n");
	            	slope_output.append("end_x " + String.format("%.2f", endpoint_x) + " m\n");
	            	slope_output.append("end_y " + String.format("%.2f", endpoint_y) + " m\n");
	            	slope_output.append("end_line_sensor " + endpoint_line + ":" + endpoint_sensor + "\n\n");
	        
	            	double amplitude1 = midpoint_intensity - startpoint_intensity;
				    double width1  = getDistance(startpoint_x, startpoint_y, midpoint_x, midpoint_y);
				    double start_slope = amplitude1 / width1;
				    
				    
				    double amplitude2 = endpoint_intensity - midpoint_intensity;
				    double width2  = getDistance(endpoint_x, endpoint_y, midpoint_x, midpoint_y);
				    double end_slope = amplitude2 / width2;
				    
				  
				    slope_output.append("amplitude 1 " + String.format("%.2f", amplitude1) + " nT\n");
				    slope_output.append("width1 " + String.format("%.2f", width1) + " m\n");
				    slope_output.append("start_slope " + String.format("%.2f", start_slope) + " nT/m\n");
				    
				    slope_output.append("amplitude 2 " + String.format("%.2f", amplitude2) + " nT\n");
				    slope_output.append("width2 " + String.format("%.2f", width2) + " m\n");
				    slope_output.append("end_slope " + String.format("%.2f", end_slope) + " nT/m\n");
				    
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
					slope_output.append("nearest_target_id " + (closest_target + 1) + "\n");
					slope_output.append("nearest_target_distance " + String.format("%.2f", previous_distance) + " m\n");
				}
				else
				{
					if(!startpoint_set)
						System.out.println("Start point is not set.");
					if(!midpoint_set)
						System.out.println("Mid point is not set.");
					if(!endpoint_set)
						System.out.println("End point is not set.");
					
				}
			}
		};
		slope_apply_button.addActionListener(slope_apply_handler);
		
		JButton slope_clear_button = new JButton("Clear");
		ActionListener slope_clear_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				startpoint_set = false;
				midpoint_set   = false;
				endpoint_set   = false;
				
				slope_output.setText("");
				data_canvas.repaint();
			}
		};
		slope_clear_button.addActionListener(slope_clear_handler);
		
		
		JButton slope_save_button = new JButton("Save");
		ActionListener slope_save_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
			
				//System.out.println("Saving points of interest.");
				try
	            {
	            	FileWriter output  = new FileWriter("points.txt", true);
	            	
	            	output.write("start_index " + startpoint_index + "\n");
	            	output.write("start_intensity " + String.format("%.2f",startpoint_intensity) + " nT\n");
	            	output.write("start_x " + String.format("%.2f", startpoint_x) + " m\n");
	            	output.write("start_y " + String.format("%.2f", startpoint_y) + " m\n");
	            	output.write("start_line_sensor " + startpoint_line + ":" + startpoint_sensor + "\n");
	            	output.write("mid_index " + midpoint_index + "\n");
	            	output.write("mid_intensity " + String.format("%.2f", midpoint_intensity) + " nT\n");
	            	output.write("mid_x " + String.format("%.2f", midpoint_x) + " m\n");
	            	output.write("mid_y " + String.format("%.2f", midpoint_y) + " m\n");
	            	output.write("mid_line_sensor " + midpoint_line + ":" + midpoint_sensor + "\n");
	            	output.write("end_index " + endpoint_index + "\n");
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
	            	//System.out.println("Current time is " + current_time.toString());
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
		
		slope_panel.add(slope_output, BorderLayout.CENTER);
		slope_button_panel.add(slope_apply_button);
		slope_button_panel.add(slope_clear_button);
		slope_button_panel.add(slope_save_button);
		slope_panel.add(slope_button_panel, BorderLayout.SOUTH);
		
		slope_dialog = new JDialog(frame, "Get Slope/Amplitude");
		slope_dialog.add(slope_panel);
		JMenuItem slope_item = new JMenuItem("Get Slope/Amplitude");
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
				
				y += 200;

				slope_dialog.setLocation(x, y);
				slope_dialog.pack();
				slope_dialog.setVisible(true);
			}
		};
		slope_item.addActionListener(slope_handler);
		slope_menu.add(slope_item);
		
		// A modeless dialog box that shows up if Format->Sort is selected.
		JPanel sort_panel = new JPanel(new BorderLayout());
		JPanel sort_canvas_panel = new JPanel(new BorderLayout());
		sort_canvas       = new SortCanvas();
		sort_canvas.setSize(600, 400);
		JScrollBar sort_scrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, 0, 501);

		sort_canvas_panel.add(sort_canvas, BorderLayout.CENTER);
		sort_canvas_panel.add(sort_scrollbar, BorderLayout.SOUTH);
		sort_panel.add(sort_canvas_panel, BorderLayout.CENTER);
				
		JPanel      sort_button_panel = new JPanel(new BorderLayout());
		order_information             = new JTextArea(10, 10);
		order_information.setEditable(false);
		order_scrollpane  = new JScrollPane(order_information, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); 
		JPanel      order_panel       = new JPanel((new GridLayout(2, 1)));
		order_canvas                  = new OrderCanvas();
		order_canvas.setSize(20, 20);
		JButton     sort_button       = new JButton("Sort");
		SortButtonHandler sort_button_handler = new SortButtonHandler();
		sort_button.addActionListener(sort_button_handler);
		order_panel.add(order_canvas);
		order_panel.add(sort_button);
		sort_button_panel.add(order_scrollpane, BorderLayout.CENTER);
		sort_button_panel.add(order_panel, BorderLayout.SOUTH);
				
		sort_panel.add(sort_button_panel, BorderLayout.EAST);
		SortLocationHandler sort_location_handler = new SortLocationHandler();
		sort_scrollbar.addAdjustmentListener(sort_location_handler);
		value = (int) (500 * sort_location);
		sort_scrollbar.setValue(value);
		sort_dialog = new JDialog(frame, "Sort");
		sort_dialog.add(sort_panel);
		
		// A modeless dialog box that shows up if Settings->Show Segment Image is selected.
		segment_image_canvas = new SegmentImageCanvas();
		segment_image_canvas.setSize(image_xdim, image_ydim);
		JScrollPane segment_image_scrollpane = new JScrollPane();
		segment_image_scrollpane.setSize(400, 200);
		segment_image_scrollpane.add(segment_image_canvas);
		segment_image_scrollpane.setViewportView(segment_image_canvas);
		
		segment_image_dialog = new JDialog(frame);
		segment_image_dialog.add(segment_image_scrollpane);
		
		// A modeless dialog box that shows up if Settings->Show Line Image is selected.
		line_image_canvas = new LineImageCanvas();
		line_image_canvas.setSize(line_image_xdim, line_image_ydim);
		
		ImageMouseHandler image_mouse_handler = new ImageMouseHandler();
		line_image_canvas.addMouseListener(image_mouse_handler);
		JScrollPane line_image_scrollpane = new JScrollPane();
		line_image_scrollpane.setSize(400, 200);
		line_image_scrollpane.add(line_image_canvas);
		line_image_scrollpane.setViewportView(line_image_canvas);		
		line_image_dialog = new JDialog(frame);
		line_image_dialog.add(line_image_scrollpane);
		
		// A modeless dialog box that shows up if Settings->Scaling is selected.
		JPanel scale_panel = new JPanel((new GridLayout(2, 1)));
		if(autoscale)
		    autoscale_button = new JToggleButton("Autoscale", true);
		else
			autoscale_button = new JToggleButton("Autoscale", false);
		ItemListener autoscale_handler = new ItemListener()
		{
			public void itemStateChanged(ItemEvent itemEvent)
			{
				int state = itemEvent.getStateChange();

				if (state == ItemEvent.SELECTED)
				{
					autoscale = true;
					data_canvas.repaint();
				} 
				else
				{
					autoscale = false;
					data_canvas.repaint();
				}
				reset_bounds_button.doClick();
			}
		};
		autoscale_button.addItemListener(autoscale_handler);
		

	    factor_slider = new JSlider(0, 200, 0);
		ChangeListener factor_handler = new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				JSlider slider = (JSlider) e.getSource();
				if (slider.getValueIsAdjusting() == false)
				{
					int value = factor_slider.getValue();
					
					if(value == 0)
						data_scaled = false;
					else
						data_scaled = true;
					scale_factor = (double) value / 100 + 1.;
					data_canvas.repaint();
				}
			}
		};
		factor_slider.addChangeListener(factor_handler);
		scale_panel.add(autoscale_button);
		scale_panel.add(factor_slider);
		if(data_scaled)
	    {
	        int current_value = (int)((scale_factor - 1.) * 100.);	
	        factor_slider.setValue(current_value);
	    }
	    // Make it consistent with the boolean if it isn't in the config file.
	    else
	    {
	    	factor_slider.setValue(0);
	    	scale_factor = 1.0;
	    }
		scale_dialog = new JDialog(frame);
		scale_dialog.add(scale_panel);

		// A modeless dialog box that shows up if Settings->Dynamic Range is selected.
		lower_bound.setHorizontalAlignment(JTextField.CENTER);
		upper_bound.setHorizontalAlignment(JTextField.CENTER);
		JPanel bounds_panel = new JPanel(new GridLayout(2, 2));
		bounds_panel.add(lower_bound);
		bounds_panel.add(upper_bound);
		bounds_panel.add(new JLabel("Lower", JLabel.CENTER));
		bounds_panel.add(new JLabel("Upper", JLabel.CENTER));
		JPanel bounds_button_panel = new JPanel(new GridLayout(1, 2));
		JButton adjust_bounds_button = new JButton("Adjust");
		reset_bounds_button = new JButton("Reset");
		bounds_button_panel.add(adjust_bounds_button);
		ActionListener adjust_range_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				double seg_min      = (double) sensor_data.get(0);
				double seg_max      = (double) sensor_data.get(1);
				double line_min     = (double) sensor_data.get(2);
				double line_max     = (double) sensor_data.get(3);
				String bound_string = lower_bound.getText();
				double min          = Double.valueOf(bound_string);
				bound_string        = upper_bound.getText();
				double max          = Double.valueOf(bound_string);
				double global_min   = 0;
				double global_max   = 0;
				
				if(autoscale)
				{
					global_min = seg_min;
					global_max = seg_max;
				}
				else
				{
					global_min = line_min;
					global_max = line_max;	
				}
				data_clipped = true;
				if(global_min < min)
					intensity_min = global_min;
				else
					intensity_min = min;
				if(global_max > max)
					intensity_max= global_max;
				else
					intensity_max = max;
				apply_item.doClick();
				dynamic_range_canvas.repaint();
				dynamic_button_changing = true;
				double current_range = intensity_max - intensity_min;
				int min_value = 0;
				int max_value = 0;
				if(min > global_min)
				{
					min_value = (int) ((min - global_min) / current_range * 100);
				    if(max < global_max)
				    	max_value = (int) ((max - global_min) / current_range * 100);	
				    else
				    	max_value = (int) ((global_max - global_min) / current_range * 100);    
				}
				else
				{
					min_value = (int) ((global_min - min) / current_range * 100);
					if(max < global_max)
				    	max_value = (int) ((max - min) / current_range * 100);	
				    else
				    	max_value = (int) ((global_max - min) / current_range * 100);
				}
				dynamic_range_slider.setValue(min_value);
				dynamic_range_slider.setUpperValue(max_value);
				dynamic_button_changing = false;	
			}
		};
		adjust_bounds_button.addActionListener(adjust_range_handler);
		bounds_button_panel.add(reset_bounds_button);
		ActionListener reset_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				double seg_min = (double) sensor_data.get(0);
				double seg_max = (double) sensor_data.get(1);
				double line_min = (double) sensor_data.get(2);
				double line_max = (double) sensor_data.get(3);
				if(autoscale)
				{
					String lower_bound_string = String.format("%,.2f", seg_min);
					String upper_bound_string = String.format("%,.2f", seg_max);
					lower_bound.setText(lower_bound_string);
					upper_bound.setText(upper_bound_string);
					dynamic_button_changing = true;
					dynamic_range_slider.setValue(0);
					dynamic_range_slider.setUpperValue(100);
					dynamic_button_changing = false;
				} 
				else
				{
					String lower_bound_string = String.format("%,.2f", line_min);
					String upper_bound_string = String.format("%,.2f", line_max);
					lower_bound.setText(lower_bound_string);
					upper_bound.setText(upper_bound_string);

					dynamic_button_changing = true;
					dynamic_range_slider.setValue(0);
					dynamic_range_slider.setUpperValue(100);
					dynamic_button_changing = false;
				}
				data_clipped = false;
				apply_item.doClick();
				dynamic_range_canvas.repaint();
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
		dynamic_range_canvas.setSize(100, 520);
		JPanel dynamic_range_canvas_panel = new JPanel(new BorderLayout());
		dynamic_range_canvas_panel.add(dynamic_range_slider, BorderLayout.WEST);
		dynamic_range_canvas_panel.add(dynamic_range_canvas, BorderLayout.CENTER);
		JPanel dynamic_range_panel = new JPanel(new BorderLayout());
		dynamic_range_panel.add(dynamic_range_canvas_panel, BorderLayout.CENTER);
		dynamic_range_panel.add(dynamic_range_button_panel, BorderLayout.SOUTH);
		dynamic_range_dialog = new JDialog(frame);
		dynamic_range_dialog.add(dynamic_range_panel);

		// A modeless dialog box that shows up if Settings->Smoothing is selected.
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

		// A modeless dialog box that shows up if Settings->Location is selected.
		JPanel location_panel = new JPanel(new BorderLayout());
		offset_information = new JTextField();
		offset_information.setHorizontalAlignment(JTextField.CENTER);
		String string = String.format("%,.2f", offset);
		offset_information.setText(string);
		JPanel location_canvas_panel = new JPanel(new BorderLayout());
		location_canvas = new LocationCanvas();
		location_canvas.setSize(240, 360);
		
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
		// Adjust for coordinate systems.
		value = (int)(2000. - ylocation * 2000.);
		ylocation_scrollbar.setValue(value);
		
		location_canvas_panel.add(location_canvas, BorderLayout.CENTER);
		location_canvas_panel.add(xlocation_scrollbar, BorderLayout.SOUTH);
		location_canvas_panel.add(ylocation_scrollbar, BorderLayout.EAST);
		
		JPanel parameter_panel = new JPanel(new GridLayout(2, 2));
		parameter_panel.add(offset_information);
		range_information = new JTextField();
		range_information.setHorizontalAlignment(JTextField.CENTER);
		string = String.format("%,.2f", range);
		range_information.setText(string);
		parameter_panel.add(range_information);
		parameter_panel.add(new JLabel("Offset", JLabel.CENTER));
		parameter_panel.add(new JLabel("Range", JLabel.CENTER));
		JButton adjust_button = new JButton("Adjust");
		RangeButtonHandler adjust_handler = new RangeButtonHandler();
		adjust_button.addActionListener(adjust_handler);
	    JPanel bottom_panel = new JPanel(new BorderLayout());
	    bottom_panel.add(parameter_panel, BorderLayout.NORTH);
	    bottom_panel.add(adjust_button, BorderLayout.SOUTH);
		location_panel.add(location_canvas_panel, BorderLayout.CENTER);
		location_panel.add(bottom_panel, BorderLayout.SOUTH);
		location_dialog = new JDialog(frame, "Location");
		location_dialog.add(location_panel);

		// A modeless dialog box that shows up if Settings->Graph Label is selected.
		JPanel graph_panel = new JPanel(new BorderLayout());
		graph_input        = new JTextField(30);
		graph_input.setHorizontalAlignment(JTextField.CENTER);
        graph_input.setText("");
		graph_panel.add(graph_input);
		graph_dialog = new JDialog(frame, "Label");
		graph_dialog.add(graph_panel, BorderLayout.CENTER);
		graph_dialog.addWindowListener(new WindowAdapter() 
		{
		  public void windowClosing(WindowEvent e)
		  {
		    data_canvas.repaint();
		  }
		});
		
		
		
		
		frame.setJMenuBar(menu_bar);

		// Cursor cursor = new Cursor(Cursor.DEFAULT_CURSOR);
		Cursor cursor = new Cursor(Cursor.HAND_CURSOR);
		// Cursor cursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
		frame.setCursor(cursor);
		frame.getContentPane().add(sensor_panel, BorderLayout.SOUTH);
		frame.pack();
		frame.setLocation(400, 200);
		apply_item.doClick();
	}

	class LineCanvas extends Canvas
	{
		public void paint(Graphics g)
		{
			Rectangle visible_area     = g.getClipBounds();
			int       xdim             = (int) visible_area.getWidth();
			int       ydim             = (int) visible_area.getHeight();
			double    clipped_area     = xdim * ydim;
			Dimension canvas_dimension = this.getSize();
			double    canvas_xdim      = canvas_dimension.getWidth();
			double    canvas_ydim      = canvas_dimension.getHeight();
			double    entire_area      = canvas_xdim * canvas_ydim;
			int       x_remainder      = 0;
			int       y_remainder      = 0;

			if(clipped_area != entire_area)
			{
				if(buffered_image != null)
					g.drawImage(buffered_image, 0, 0, null);	
			} 
			else
			{
				grid_data = new ArrayList[ydim][xdim];
				for (int i = 0; i < ydim; i++)
					for (int j = 0; j < xdim; j++)
						grid_data[i][j] = new ArrayList();
				buffered_image             = new BufferedImage(xdim, ydim, BufferedImage.TYPE_INT_RGB);
				Graphics2D graphics_buffer = (Graphics2D) buffered_image.getGraphics();
				graphics_buffer.setColor(java.awt.Color.WHITE);
				graphics_buffer.fillRect(0, 0, xdim, ydim);
				int size = sensor_data.size();
				if (size > 0)
				{
					int    number_of_segments = size - 4;
					double seg_min           = (double) sensor_data.get(0);
					double seg_max           = (double) sensor_data.get(1);
					double line_min          = (double) sensor_data.get(2);
					double line_max          = (double) sensor_data.get(3);
					double max_xstep         = (xdim - (left_margin + right_margin)) / number_of_segments;
					int    xstep             = (int) (max_xstep * normal_xstep);
					int   graph_xdim         = xdim - (left_margin + right_margin) - (number_of_segments - 1) * xstep;
					double max_ystep = (ydim - (top_margin + bottom_margin)) / number_of_segments;
					int    ystep = (int) (max_ystep * normal_ystep);
					int    graph_ydim = ydim - (top_margin + bottom_margin) - (number_of_segments - 1) * ystep;

					// So that graphs are not butted together.
					if(xstep == max_xstep && ystep == 0)
					{
						graph_xdim -= 20;
						x_remainder = 20;
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
					

					double minimum_x = offset;
					double maximum_x = offset + range;
					minimum_y = 0;
					maximum_y = 0;
					if(data_clipped == true)
					{
						String bound_string = lower_bound.getText();
						minimum_y = Double.valueOf(bound_string);
						bound_string = upper_bound.getText();
						maximum_y = Double.valueOf(bound_string);
					} 
					else
					{
						if(!autoscale)
						{
							minimum_y = line_min;
							maximum_y = line_max;
						} 
						else
						{
							minimum_y = seg_min;
							maximum_y = seg_max;
						}
					}
                    
					if(data_scaled)
					{
						minimum_y /= scale_factor;
						maximum_y /= scale_factor;
					}
					
					double      xrange        = range;
					double      yrange        = maximum_y - minimum_y;
					Font        current_font  = graphics_buffer.getFont();
					FontMetrics font_metrics  = graphics_buffer.getFontMetrics(current_font);
					int         string_height = font_metrics.getAscent();
					
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
                        
						double current_range           = b1 - b2;
						double current_position        = b2;
						double current_value           = maximum_y;
						double current_intensity_range = maximum_y - minimum_y;
						

						if(xstep != 0 && xstep != max_xstep && ystep != 0 && ystep != max_xstep && i != number_of_segments - 1)
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
					    int string_width = 0;
				        if(relative_mode)
				            string_width = font_metrics.stringWidth("77.7");
				        else
					        string_width    = font_metrics.stringWidth("7,777,777");
				        int    number_of_units            = (int) (graph_xdim / (string_width + 4));  
				        double current_position_increment = graph_xdim;
				        current_position_increment        /= number_of_units;
				        
				        if(i == 0 || (xstep == max_xstep && ystep == 0))
				        {
				        	//Put down lines on the frontmost graph where we can hang location information.
				            graphics_buffer.drawLine((int) current_position, b1, (int) current_position, b1 + 10); 
				            for(int j = 0; j < number_of_units; j++)
				            {
					            current_position += current_position_increment;
					            graphics_buffer.drawLine((int) current_position, b1, (int) current_position, b1 + 10);
				            }
				        }
				        else
				        {
				        	if(ystep != 0)
				        	{
				        	    graphics_buffer.drawLine((int) current_position, b1 + y_remainder, (int) current_position - xstep, b1 + ystep); 
				        	    for(int j = 0; j < number_of_units; j++)
				        	    {
				        	    	current_position += current_position_increment;
				        		    graphics_buffer.drawLine((int) current_position, b1 + y_remainder, (int) current_position - xstep, b1 + ystep);
				        		    // At the end of a graph, put down a line where we can hang a line id or location information.
				        		    // It also helps define the isometric space.
				        		    if(j == number_of_units - 1)
				        		    	graphics_buffer.drawLine((int) current_position, b1 + y_remainder, (int) current_position, b1 + 10);  	
				        	    }
				            }
				        }	
                        current_position         = b2;
                        current_range            = b1 - b2;
					    number_of_units          = (int) (current_range / (2 * (string_height)));
					    double current_increment = current_range / number_of_units;
					    
					    if(i == 0)
					    {
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
				            if(xstep == 0 && ystep == 0  || xstep == 0 && ystep == max_ystep)
				            {
				            	current_position = b2;
					    	    for(int j = 0; j < number_of_units; j++)
				                {
				    			    graphics_buffer.drawLine(a1, (int)current_position, a2, (int)current_position);
					                current_position += current_increment;
				                }
					    	    current_position  = a1;
					    	    number_of_units   = (int)(graph_xdim / (string_width + 4));  
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
					    }
					    else
					    {
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
				    	    
					    }
					    
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
					    	number_of_units   = (int)(graph_xdim / (string_width + 4));  
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
					    	graphics_buffer.setColor(new Color(196, 196, 196));
				    	    graphics_buffer.setStroke(new BasicStroke(1));
				    	    graphics_buffer.drawLine(a1, b1, a2, b1);
					    	if(!(xstep == max_xstep && ystep == 0) || xstep == 0)
					    	{
					    		// Creating grid on rear of data space.
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
					    	    number_of_units   = (int)(graph_xdim / (string_width + 4));  
					    	    current_increment = graph_xdim;
					    	    current_increment /= number_of_units;
					    	    
					    	    
					    	    for(int j = 0; j < number_of_units; j++)
				                {
				    			    graphics_buffer.drawLine((int)current_position, b1, (int)current_position, b2);
				    			    current_position += current_increment;
				    			}
						    }
						    
						}
					    
					    if((xstep == max_xstep && ystep == 0))
			            {
					    	graphics_buffer.setColor(new Color(196, 196, 196));
				    	    graphics_buffer.setStroke(new BasicStroke(1));
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

					ArrayList plot_data = new ArrayList();
					ArrayList sample_data = new ArrayList();

					for (int i = 4; i < size; i++)
					{
						ArrayList sensor_list = (ArrayList) sensor_data.get(i);
						int       length      = sensor_list.size();
						ArrayList plot_list   = new ArrayList();
						ArrayList sample_list = new ArrayList();

						// This is to help filter out vertical jitter but not sure that's what we want
						// to do.
						/*
						 * int j = 4; Sample sample = (Sample) sensor_list.get(j); Point2D.Double point
						 * = new Point2D.Double(); point.x = sample.y; point.y = sample.intensity;
						 * point.y *= scale_factor; plot_list.add(point); double previous_x = sample.x;
						 * double current_intensity_max = sample.intensity; double current_intensity_min
						 * = sample.intensity; for (j = 5; j < length; j++)
						 */
						for (int j = 4; j < length; j++)
						{
							Sample sample = (Sample) sensor_list.get(j);
							// Will revisit this later.
							// Need to keep track of new dynamic range after subsampling.
							/*
							 * if (sample.x > previous_x + quantum_distance) { point = new Point2D.Double();
							 * point.x = sample.y; point.y = sample.intensity; point.y *= scale_factor; if
							 * (point.y < minimum_y) point.y = minimum_y; else if (point.y > maximum_y)
							 * point.y = maximum_y; previous_x = sample.x; if(sample.intensity <
							 * current_intensity_min) current_intensity_min = sample.intensity; else
							 * if(sample.intensity > current_intensity_max) current_intensity_max =
							 * sample.intensity; }
							 */

							// Using all the points, even the ones that map to the
							// same x coordinate on the display and vertical lines in the curve.
							Point2D.Double point = new Point2D.Double();
							point.x = sample.y;
							point.y = sample.intensity;
							point.y *= scale_factor;
							if (point.y < minimum_y)
								point.y = minimum_y;
							else if (point.y > maximum_y)
								point.y = maximum_y;
							plot_list.add(point);
							sample_list.add(sample);
						}
						int plot_length = plot_list.size();

						if (smooth == 0)
						{
							plot_data.add(plot_list);
							sample_data.add(sample_list);
						} 
						else
						{
							double x[] = new double[plot_length];
							double y[] = new double[plot_length];
							for (int j = 0; j < plot_length; j++)
							{
								Point2D.Double point = (Point2D.Double) plot_list.get(j);
								x[j] = point.getX();
								y[j] = point.getY();
							}
							double[] smooth_x = smooth(x, smooth);
							double[] smooth_y = smooth(y, smooth);

							Point2D.Double start_point = (Point2D.Double) plot_list.get(0);
							Point2D.Double end_point = (Point2D.Double) plot_list.get(plot_length - 1);
							plot_list.clear();

							plot_length = smooth_x.length;
							plot_list.add(start_point);
							for (int j = 0; j < plot_length; j++)
							{
								Point2D.Double smooth_point = new Point2D.Double(smooth_x[j], smooth_y[j]);
								plot_list.add(smooth_point);
							}
							plot_list.add(end_point);
							plot_data.add(plot_list);
							length = sample_list.size();
							Sample sample = (Sample) sample_list.get(length - 1);

							// Add a duplicate sample so the lengths of the plot list and sample_list are
							// the same.
							// Actually more accurately correlated than the sample being duplicated since we
							// are matching original data.
							Sample extra_sample = new Sample();
							extra_sample.intensity = sample.intensity;
							extra_sample.x = sample.x;
							extra_sample.y = sample.y;
							extra_sample.index = sample.index;
							sample_list.add(extra_sample);
							sample_data.add(sample_list);
						}
					}

					Polygon[] polygon = new Polygon[number_of_segments];
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

						ArrayList segment;
						ArrayList sample_segment;
						ArrayList sensor_list;
						int current_line;
						int current_sensor;
						if (!reverse_view)
						{
							sensor_list    = (ArrayList) sensor_data.get(i + 4);
							current_line   = (int) sensor_list.get(0);
							current_sensor = (int) sensor_list.get(1);
							segment        = (ArrayList) plot_data.get(i);
							sample_segment = (ArrayList) sample_data.get(i);
						} 
						else
						{
							sensor_list    = (ArrayList) sensor_data.get((number_of_segments - 1) - i + 4);
							current_line   = (int) sensor_list.get(0);
							current_sensor = (int) sensor_list.get(1);
							segment        = (ArrayList) plot_data.get((number_of_segments - 1) - i);
							sample_segment = (ArrayList) sample_data.get((number_of_segments - 1) - i);
						}

						int n   = segment.size() + 3;
						int[] x = new int[n];
						int[] y = new int[n];
						
						int m = 0;
                        yrange = maximum_y - minimum_y;
                        xrange = range;
                        
                        double this_minimum_y = maximum_y;
                        double this_maximum_y = minimum_y;
                        double init_point     = 0;
						for(int k = 0; k < segment.size(); k++)
						{
							Point2D.Double point = (Point2D.Double) segment.get(k);
							Sample sample = (Sample) sample_segment.get(k);

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

							// Associate this point with sample information.
							// Where endpoints overlap, there should be multiple samples.
							ArrayList grid_list = grid_data[(int) current_y][(int) current_x];
							if (grid_list.size() == 0)
							{
								grid_list.add(sensor_list.get(0));
								grid_list.add(sensor_list.get(1));
								grid_list.add(sample);
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
								int this_line = (int) sensor_list.get(0);
								int this_sensor = (int) sensor_list.get(1);
								ArrayList new_grid_list = new ArrayList();
								new_grid_list.add(sensor_list.get(0));
								new_grid_list.add(sensor_list.get(1));
								new_grid_list.add(sample);
								for (int p = 0; p < grid_list.size(); p += 3)
								{
									int line = (int) grid_list.get(p);
									int sensor = (int) grid_list.get(p + 1);

									if (this_line != line || this_sensor != sensor)
									{
										Sample previous_sample = (Sample) grid_list.get(p + 2);
										new_grid_list.add(line);
										new_grid_list.add(sensor);
										new_grid_list.add(previous_sample);
									}
								}
								grid_data[(int) current_y][(int) current_x] = new_grid_list;
							}

							// Assigning neighbor pixels if they are unassigned so that
							// it isn't hard for the mouse to find an assigned pixel.
							grid_list = grid_data[(int) current_y - 1][(int) current_x];
							if (grid_list.size() == 0)
							{
								grid_list.add(sensor_list.get(0));
								grid_list.add(sensor_list.get(1));
								grid_list.add(sample);
							}

							grid_list = grid_data[(int) current_y - 1][(int) current_x - 1];
							if (grid_list.size() == 0)
							{
								grid_list.add(sensor_list.get(0));
								grid_list.add(sensor_list.get(1));
								grid_list.add(sample);
							}

							grid_list = grid_data[(int) current_y - 1][(int) current_x + 1];
							if (grid_list.size() == 0)
							{
								grid_list.add(sensor_list.get(0));
								grid_list.add(sensor_list.get(1));
								grid_list.add(sample);
							}

							grid_list = grid_data[(int) current_y][(int) current_x - 1];
							if (grid_list.size() == 0)
							{
								grid_list.add(sensor_list.get(0));
								grid_list.add(sensor_list.get(1));
								grid_list.add(sample);
							}

							grid_list = grid_data[(int) current_y][(int) current_x + 1];
							if (grid_list.size() == 0)
							{
								grid_list.add(sensor_list.get(0));
								grid_list.add(sensor_list.get(1));
								grid_list.add(sample);
							}

							grid_list = grid_data[(int) current_y + 1][(int) current_x];
							if (grid_list.size() == 0)
							{
								grid_list.add(sensor_list.get(0));
								grid_list.add(sensor_list.get(1));
								grid_list.add(sample);
							}

							grid_list = grid_data[(int) current_y + 1][(int) current_x - 1];
							if (grid_list.size() == 0)
							{
								grid_list.add(sensor_list.get(0));
								grid_list.add(sensor_list.get(1));
								grid_list.add(sample);
							}

							grid_list = grid_data[(int) current_y + 1][(int) current_x + 1];
							if (grid_list.size() == 0)
							{
								grid_list.add(sensor_list.get(0));
								grid_list.add(sensor_list.get(1));
								grid_list.add(sample);
							}
							m++;
						}

						double local_min = this_minimum_y;
						local_min -= minimum_y;
						local_min /= yrange;
						local_min *= graph_ydim;
						local_min = (graph_ydim + y_remainder) - local_min;
						local_min+= top_margin + (number_of_segments - 1) * ystep;
						local_min -= yaddend;
						
						double local_max = this_maximum_y;
						local_max -= minimum_y;
						local_max /= yrange;
						local_max *= graph_ydim;
						local_max  = (graph_ydim + y_remainder) - local_max;
						local_max += top_margin + (number_of_segments - 1) * ystep;
						local_max -= yaddend;
						
						x[m] = a2;
						//y[m] = (int)local_min;
						y[m] = b1;
						m++;

						x[m] = a1;
						//y[m] = (int)local_min;
						y[m] = b1;
						m++;
						
						x[m] = a1;
						y[m] = (int)init_point;
						graphics_buffer.setColor(Color.BLACK);
					    graphics_buffer.setStroke(new BasicStroke(1));
					    graphics_buffer.drawLine(a2, (int)local_min, a2, b1);
					    
						//graphics_buffer.setColor(Color.RED);
					    if(reverse_view)
					    	graphics_buffer.setColor(fill_color[number_of_segments - 1 - i]);
					    else
					    	graphics_buffer.setColor(fill_color[i]);
					    graphics_buffer.setStroke(new BasicStroke(3));
					    graphics_buffer.drawLine(a1, (int)local_min, a1, (int)local_max);
						java.awt.Polygon sensor_polygon = new Polygon(x, y, n);
						polygon[i]  = sensor_polygon;
					}
					
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
               
						if(reverse_view)
						{
						    if (visible[(number_of_segments - 1) - i] == true)
						    {
							    if (transparent[(number_of_segments - 1) - i] == false)
							    {
								    graphics_buffer.setColor(fill_color[(number_of_segments - 1) - i]);
								    graphics_buffer.fillPolygon(polygon[i]);
							    }
							    graphics_buffer.setColor(java.awt.Color.BLACK);
							    graphics_buffer.setStroke(new BasicStroke(2));
							    //graphics_buffer.setColor(outline_color[(number_of_segments - 1) - i]);
								graphics_buffer.drawPolygon(polygon[i]);
							}
							
							if (minimum_y < 0 && visible[(number_of_segments - 1) - i] == true)
							{
								ArrayList plot_list = (ArrayList) plot_data.get((number_of_segments - 1) - i);
	
								Point2D.Double first = (Point2D.Double) plot_list.get(0);
								int plot_length = plot_list.size();
								Point2D.Double last = (Point2D.Double) plot_list.get(plot_length - 1);

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
								graphics_buffer.drawLine((int) a1, (int) zero_y, (int) a2, (int) zero_y);
								graphics_buffer.setStroke(new BasicStroke(2));
							}
						}
						else
						{
							if (visible[i] == true)
						    {
							    if (transparent[i] == false)
							    {
								    graphics_buffer.setColor(fill_color[i]);
								    graphics_buffer.fillPolygon(polygon[i]);
							    }
							    graphics_buffer.setColor(java.awt.Color.BLACK);
							    graphics_buffer.setStroke(new BasicStroke(2));
								graphics_buffer.drawPolygon(polygon[i]);
							}
							
							if (minimum_y < 0 && visible[i] == true)
							{
								ArrayList plot_list = (ArrayList) plot_data.get(i);
	
								Point2D.Double first = (Point2D.Double) plot_list.get(0);
								int plot_length = plot_list.size();
								Point2D.Double last = (Point2D.Double) plot_list.get(plot_length - 1);

								double zero_y = Math.abs(minimum_y);
								zero_y /=  maximum_y - minimum_y;
								zero_y *= graph_ydim;
								zero_y = (graph_ydim + y_remainder) - zero_y;
								zero_y += top_margin + (number_of_segments - 1) * ystep;
								zero_y -= yaddend;

								float[] dash ={ 2f, 0f, 2f };
								BasicStroke basic_stroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, dash, 2f);
								graphics_buffer.setStroke(basic_stroke);
								graphics_buffer.setColor(java.awt.Color.RED);
								graphics_buffer.drawLine((int) a1, (int) zero_y, (int) a2, (int) zero_y);
								graphics_buffer.setStroke(new BasicStroke(2));
							}	
						}
					}
					
					graphics_buffer.setColor(java.awt.Color.BLACK);
					double current_value    = offset;
					double current_position = left_margin;
					int a1                  = left_margin;
					int b1                  = ydim - bottom_margin;
					int a2                  = a1 + graph_xdim;
					int b2                  = b1 - graph_ydim;
					
					String position_string;
				    int    string_width;
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
					        if(i != number_of_segments - 1)
					        {
					    	    //Side--seems like it just gets in the way.
				                //graphics_buffer.drawLine(a1, b1, a2, b1); 
					    	    if(ystep != 0)
					    	    {
					    	    	//graphics_buffer.setColor(Color.GRAY);
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
						
					    
					    current_value    = offset;
					    current_position = a1;
					    
				        if (relative_mode)
				        {
				            string_width = font_metrics.stringWidth("77.7");
				            position_string = String.format("%,.0f", current_value);
				        }
				        else
				        {
					        string_width    = font_metrics.stringWidth("7,777,777");
					        position_string = String.format("%,.0f", current_value + global_ymin);
				        }
				        int    number_of_units            = (int) (graph_xdim / (string_width + 4));
				        double current_position_increment = graph_xdim;
				        current_position_increment        /= number_of_units;
				        if(i == 0  || (xstep == max_xstep && ystep == 0))
				        {
				        	// Hanging numbers on frontmost xaxis.
				        	graphics_buffer.drawString(position_string, (int) current_position - string_width / 2, ydim + string_height + 12 - bottom_margin);
				            double current_value_increment = range / number_of_units;
				            for(int j = 0; j < number_of_units; j++)
				            {
					            current_value += current_value_increment;
					            current_position += current_position_increment;
					            if(relative_mode)
					                position_string = String.format("%,.0f", current_value);
					            else
						            position_string = String.format("%,.0f", current_value + global_ymin);	
					            graphics_buffer.drawString(position_string, (int) current_position - string_width / 2, ydim + string_height + 12 - bottom_margin);
				            }
				        }
				    }
					
					position_string = new String("meters");
					string_width = font_metrics.stringWidth(position_string);
					graphics_buffer.drawString(position_string, left_margin + (xdim - right_margin - left_margin) / 2 - string_width / 2, ydim - bottom_margin / 4);
                    
					// Placing numbers on the intensity axis.
					double current_intensity_range = maximum_y - minimum_y;
					double current_range = b1 - b2;
					int number_of_units = (int) (current_range / (2 * string_height));
					double current_increment = current_range / number_of_units;
					double current_value_increment = current_intensity_range / number_of_units;
					current_position = b2;
					String intensity_string = String.format("%,.0f", current_value);
					string_width = font_metrics.stringWidth(intensity_string);
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
						    
						    current_value = maximum_y;
						    current_position = b2;
						    
						    if(i == 0  || (xstep == 0 && ystep == max_ystep))
						    {
					            for(int j = 0; j < number_of_units; j++)
					            {
						            intensity_string = String.format("%,.0f", current_value);
						            string_width     = font_metrics.stringWidth(intensity_string);
						            graphics_buffer.drawString(intensity_string, a1 - (string_width + 14), (int) (current_position + string_height / 2));
						            current_position += current_increment;
						            current_value    -= current_value_increment;
						            
						            if(j == number_of_units - 1)
						            {
										
										ArrayList sensor_list;
						            	if(reverse_view)
						            	    sensor_list = (ArrayList) sensor_data.get(((number_of_segments - 1) - i) + 4);
						            	else
						            		sensor_list = (ArrayList) sensor_data.get(i + 4);      	
						            	int line   = (int)sensor_list.get(0);
						            	int sensor = (int)sensor_list.get(1);
										size = sensor_list.size();
										Sample sample = (Sample)sensor_list.get(size - 1);
										String line_id = new String(line + ":" + sensor);
										if(ystep != 0  && show_id)
										    graphics_buffer.drawString(line_id, a2 + 6, (int) current_position + ( 3 * string_height / 4));
						            }
					            }
					            intensity_string = String.format("%,.0f", current_value);
					            string_width = font_metrics.stringWidth(intensity_string);
								graphics_buffer.drawString(intensity_string, a1 - (string_width + 14), (int) (current_position + string_height / 2));
						    }
						    else
						    {
						    	if(ystep != 0)
						    	{
						    		for(int j = 0; j < number_of_units; j++)
						            {
							            current_position += current_increment;
							            if(j == number_of_units - 1)
							            {
							            	ArrayList sensor_list;
							            	if(reverse_view)
							            	    sensor_list = (ArrayList) sensor_data.get(((number_of_segments - 1) - i) + 4);
							            	else
							            		sensor_list = (ArrayList) sensor_data.get(i + 4);      	
							            	int line   = (int)sensor_list.get(0);
							            	int sensor = (int)sensor_list.get(1);
							            	
							            	// Some other possibilities besides the line id:
							            	// the actual locations that might reflect any out of
							            	// orders segments, and the ideal location that we
							            	// might want to compare it to.
							            	/*
							            	double ideal_x = line * 2;
											if (line % 2 == 0)
												ideal_x += ((4 - sensor) * .5); 
											else
												ideal_x += sensor * .5;
											String ideal_string = String.format("%,.1f", ideal_x);
											
							            	
											size = sensor_list.size();
											Sample sample = (Sample)sensor_list.get(size - 1);
											String actual_string = String.format("%, .2f", sample.x);
											*/
											String line_id = new String(line + ":" + sensor);
											if(show_id)
											    graphics_buffer.drawString(line_id, a2 + 6, (int) current_position + ( 3 * string_height / 4));
							          }
						         }
						     }
						}
					}
					intensity_string = new String("nT");
					string_width = font_metrics.stringWidth(intensity_string);
					graphics_buffer.drawString(intensity_string, string_width / 2, top_margin + (ydim - top_margin - bottom_margin) / 2);
					
					if(startpoint_set)
					{
						graphics_buffer.setColor(Color.BLUE);
						graphics_buffer.drawOval((int)startpoint_x_position - 2, (int)startpoint_y_position - 2, 5, 5);
						graphics_buffer.fillOval((int)startpoint_x_position - 2, (int)startpoint_y_position - 2, 5, 5);
						graphics_buffer.setColor(Color.BLACK);	
					}
					if(midpoint_set)
					{
						graphics_buffer.setColor(Color.BLUE);
						graphics_buffer.drawOval((int)midpoint_x_position - 2, (int)midpoint_y_position - 2, 5, 5);
						graphics_buffer.fillOval((int)midpoint_x_position - 2, (int)midpoint_y_position - 2, 5, 5);
						graphics_buffer.setColor(Color.BLACK);	
					}
					if(endpoint_set)
					{
						graphics_buffer.setColor(Color.BLUE);
						graphics_buffer.drawOval((int)endpoint_x_position - 2, (int)endpoint_y_position - 2, 5, 5);
						graphics_buffer.fillOval((int)endpoint_x_position - 2, (int)endpoint_y_position - 2, 5, 5);
						graphics_buffer.setColor(Color.BLACK);	
					}
					
					if(append_data)
					{
						graphics_buffer.setColor(Color.RED);
						graphics_buffer.drawOval((int)append_x_position - 2, (int)append_y_position - 2, 5, 5);
						graphics_buffer.fillOval((int)append_x_position - 2, (int)append_y_position - 2, 5, 5);
						graphics_buffer.setColor(Color.BLACK);
						
						int current_y = string_height + 2;
						int current_x = 2;
						String information_string = new String("  Line:         " + append_line);
						graphics_buffer.drawString(information_string, current_x, current_y);
						
						current_y += string_height + 2;
						information_string = new String("  Sensor:     " + append_sensor);
						graphics_buffer.drawString(information_string, current_x, current_y);
						
						current_y += string_height + 2;
						information_string = new String("  Intensity:   " + append_intensity + "\n");	
						graphics_buffer.drawString(information_string, current_x, current_y);
						
						current_y           += string_height + 2;
						String number_string = String.format("%,.2f", append_x);
						information_string   = new String("  Relative x: " + number_string);
						graphics_buffer.drawString(information_string, current_x, current_y);
						
						current_y           += string_height + 2;
						number_string        = String.format("%,.2f", append_y);
						information_string   = new String("  Relative y: " + number_string);
						graphics_buffer.drawString(information_string, current_x, current_y);
						
						current_y           += string_height + 2;
						number_string = String.format("%,.2f", append_x_abs);
						information_string   = new String("  Absolute x: " + number_string);
						graphics_buffer.drawString(information_string, current_x, current_y);
						
						current_y           += string_height + 2;
						number_string        = String.format("%,.2f", append_y_abs);
						information_string   = new String("  Absolute y: " + number_string);
						graphics_buffer.drawString(information_string, current_x, current_y);
					}
					
					String graph_label = graph_input.getText();					
					if(show_label && !graph_label.equals(""))
					{
						string_width = font_metrics.stringWidth(graph_label); 
						graphics_buffer.drawString(graph_label, xdim / 2 - string_width / 2, top_margin - 5);
					}
					
					if(color_key)
					{
						string_width  = font_metrics.stringWidth("xx:x");
						ArrayList   sensor_id     = new ArrayList();
						for(int i = 0; i < 10; i++)
						{
							String _sensor_id = sensor[i].getText();
							if(!_sensor_id.equals(""))
								sensor_id.add(_sensor_id);
						}
						number_of_segments = sensor_id.size();
						
						int x = xdim - 3 * string_width - 5;
						int y = ydim - (2 * number_of_segments * string_height) + 5;
						graphics_buffer.setStroke(new BasicStroke(3));
						for(int i = 0; i < number_of_segments; i++)
						{
							String id = (String)sensor_id.get(i);
							graphics_buffer.setColor(Color.BLACK);
							graphics_buffer.drawString(id, x, y);
							graphics_buffer.setColor(fill_color[i]);
							graphics_buffer.fillRect(x + 2 * string_width, y - string_height, string_width, string_height);
							y += 2 * string_height;
						}
					}
				}
				g.drawImage(buffered_image, 0, 0, null);
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
			}
			if(button == 3)
			{
				persistent_data = false;
				sample_information.setText("");
				if(append_data)
				{
				    append_data = false;
				    data_canvas.repaint();
				}
			}
		}
	}
				
	class MouseMotionHandler extends MouseMotionAdapter
	{
		public void mouseMoved(MouseEvent event)
		{
			if(persistent_data || grid_data == null)
				return;
			
			int x = event.getX();
			int y = event.getY();
			int xdim = grid_data[0].length;
			int ydim = grid_data.length;

			if (x > left_margin && x < xdim - right_margin && y > top_margin && y < ydim - bottom_margin)
			{
				int current_line, current_sensor;
				double current_intensity, current_x, current_y;
				ArrayList sample_list = grid_data[y][x];
				int size = sample_list.size();
				outer: if (size == 0)
				{
					sample_list = grid_data[y - 1][x];
					size = sample_list.size();
					if (size != 0)
						break outer;
					sample_list = grid_data[y + 1][x];
					size = sample_list.size();
					if (size != 0)
						break outer;
					sample_list = grid_data[y][x - 1];
					size = sample_list.size();
					if (size != 0)
						break outer;
					sample_list = grid_data[y][x + 1];
					size = sample_list.size();
					if (size != 0)
					break outer;
					sample_list = grid_data[y - 1][x - 1];
					size = sample_list.size();
					if (size != 0)
						break outer;
					sample_list = grid_data[y + 1][x - 1];
					size = sample_list.size();
					sample_list = grid_data[y - 1][x + 1];
					size = sample_list.size();
					if (size != 0)
						break outer;
					sample_list = grid_data[y + 1][x + 1];
					size = sample_list.size();
				}

				if (size != 0)
				{
					sample_information.setText("");
					current_line = (int) sample_list.get(0);
					current_sensor = (int) sample_list.get(1);
					Sample sample = (Sample) sample_list.get(2);
					current_intensity = sample.intensity;
					current_x = sample.x;
					current_y = sample.y;
					
					append_line       = current_line;
					append_sensor     = current_sensor;
					append_intensity  = current_intensity;
					append_x          = current_x;
					append_y          = current_y;
					append_x_abs      = current_x + global_xmin;
					append_y_abs      = current_y + global_ymin;
					append_x_position = x;
					append_y_position = y;
					append_index = sample.index;
					
					gui_index = 0;
					String append_id = new String(current_line + ":" + current_sensor);
					
					for(int i = 0; i < 10; i++)
					{
					    String this_id = sensor[i].getText();
					    if(this_id.equals(append_id))
					    	break;
					    else
					    	gui_index++;
					}

					// Minimal filtering--not showing data for points that aren't plotted.
					// Also want to resolve multiple points assigned to the same location and filter occluded points.
					// Not a critical issue at the moment, but can lead to errors without careful attention by the user.
					if(visible[gui_index])
					{
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
				}
				else
					// Blank information panel when a pixel is traversed that is not associated with data.
					sample_information.setText("");
			}
		}
	}
	
	class ApplyHandler implements ActionListener
	{
		int[][] line_array;
		int[] current_line;
		int[] current_sensor;

		ApplyHandler()
		{
			line_array = ObjectMapper.getUnclippedLineArray();
			current_line = new int[10];
			current_sensor = new int[10];
		}

		public void actionPerformed(ActionEvent event)
		{
			for (int i = 0; i < 10; i++)
			{
				try
				{
					String line_sensor_pair = sensor[i].getText();
					if(line_sensor_pair.equals(""))
					{
					    if(i < 9)
					    {
					    	line_sensor_pair = sensor[i + 1].getText();
					    	sensor[i + 1].setText("");
					    	sensor[i].setText(line_sensor_pair);
					    }
					}
						
					StringTokenizer tokenizer = new StringTokenizer(line_sensor_pair, ":");
					int number_of_tokens = tokenizer.countTokens();
					if (number_of_tokens == 2)
					{
						String line_string = tokenizer.nextToken(":");
						int next_line = Integer.parseInt(line_string);
						String sensor_string = tokenizer.nextToken(":");
						int next_sensor = Integer.parseInt(sensor_string);

						// Do a check for valid input
						if (next_line >= 0 && next_line < 30 && next_sensor >= 0 && next_sensor < 5)
						{
							current_line[i] = next_line;
							current_sensor[i] = next_sensor;
						} 
						else
						{
							current_line[i] = -1;
							current_sensor[i] = -1;
						}
					} 
					else
					{
						current_line[i] = -1;
						current_sensor[i] = -1;
					}
				} 
				catch (Exception exception)
				{
					current_line[i] = -1;
					current_sensor[i] = -1;
				}
			}

			// Do a first pass thru the data to get the segment and line extrema.
			// This is so we can prepend it to the segment data for the line canvas
			// to use to implement autoscaling.
			double seg_min  = Double.MAX_VALUE;
			double seg_max  = -Double.MAX_VALUE;
			double line_min = Double.MAX_VALUE;
			double line_max = -Double.MAX_VALUE;

			for (int i = 0; i < 10; i++)
			{
				if (current_line[i] != -1)
				{
					int start = line_array[current_line[i]][0];
					int stop = line_array[current_line[i]][1];

					if (current_line[i] % 2 == 0)
					{
						for (int j = start + current_sensor[i]; j < stop; j += 5)
						{
							Sample sample = (Sample) data.get(j);
							if (sample.y >= offset && sample.y < (offset + range))
							{
								if (seg_min > sample.intensity)
									seg_min = sample.intensity;
								if (seg_max < sample.intensity)
									seg_max = sample.intensity;
							}
							if (sample.y >= 15 && sample.y < 75)
							{
								if (line_min > sample.intensity)
									line_min = sample.intensity;
								if (line_max < sample.intensity)
									line_max = sample.intensity;
							}
						}
					} 
					else
					{
						for (int j = stop - (1 + (4 - current_sensor[i])); j >= start; j -= 5)
						{
							Sample sample = (Sample) data.get(j);
							if (sample.y >= offset && sample.y < (offset + range))
							{
								if (seg_min > sample.intensity)
									seg_min = sample.intensity;
								if (seg_max < sample.intensity)
									seg_max = sample.intensity;
							}
							if (sample.y >= 15 && sample.y < 75)
							{
								if (line_min > sample.intensity)
									line_min = sample.intensity;
								if (line_max < sample.intensity)
									line_max = sample.intensity;
							}
						}
					}
				}
			}

			sensor_data.clear();
			sensor_data.add(seg_min);
			sensor_data.add(seg_max);
			sensor_data.add(line_min);
			sensor_data.add(line_max);
			for (int i = 0; i < 10; i++)
			{
				if (current_line[i] != -1)
				{
					ArrayList segment_data = new ArrayList();
					segment_data.add(current_line[i]);
					segment_data.add(current_sensor[i]);

					if (visible[i] == true)
						segment_data.add(new String("yes"));
					else
						segment_data.add(new String("no"));
					if (transparent[i] == true)
						segment_data.add(new String("yes"));
					else
						segment_data.add(new String("no"));

					int start = line_array[current_line[i]][0];
					int stop = line_array[current_line[i]][1];
					if (current_line[i] % 2 == 0)
					{
						for (int j = start + current_sensor[i]; j < stop; j += 5)
						{
							Sample sample = (Sample) data.get(j);
							if (sample.y >= offset && sample.y < (offset + range))
								segment_data.add(sample);
						}
					} 
					else
					{
						for (int j = stop - (1 + (4 - current_sensor[i])); j >= start; j -= 5)
						{
							Sample sample = (Sample) data.get(j);
							if (sample.y >= offset && sample.y < (offset + range))
								segment_data.add(sample);
						}
					}
					sensor_data.add(segment_data);
				}
			}
			data_canvas.repaint();
			placement_canvas.repaint();
			location_canvas.repaint();
			segment_image_canvas.repaint();
			order_canvas.repaint();
		}
	}

	class RangeScrollbarHandler implements AdjustmentListener
	{
		public void adjustmentValueChanged(AdjustmentEvent event)
		{
			JScrollBar scrollbar   = (JScrollBar) event.getSource();
			double normal_position = (double) event.getValue();
			normal_position       /= 2000;
			double normal_start    = (offset - 15) / 60;
			double normal_stop     = (offset + range - 15) / 60;
			double normal_range    = normal_stop - normal_start;
			double normal_min      = normal_range / 2;
			double normal_max      = 1 - normal_range + normal_range / 2;
			
			if (scrollbar.getValueIsAdjusting() == false)
			{
				if (range_slider_changing == false && range_button_changing == false)
				{
					range_scrollbar_changing = true;

					double previous_offset = offset;
					if(normal_position <= normal_min)
					{
						offset = 15;
					}
					else if(normal_position >= normal_max)
					{
						offset = 60 - range + 15;
					}
					else
					{
						normal_start = normal_position - normal_range / 2;
						offset       = normal_start * 60 + 15;
					}
					
					boolean moving_down = true;
					if(previous_offset < offset)
						moving_down = false;
					String string = String.format("%,.4f", offset);
					if(offset_information != null)
					    offset_information.setText(string);

					normal_position  = (offset - 15.)/ 60.;
					normal_position *= 2000;
					int lower_value  = (int)normal_position;
					normal_position = (offset + range - 15) / 60.;
					normal_position *= 2000;
					int upper_value  = (int)normal_position;
					
					if(moving_down)
					{
					    range_slider.setValue(lower_value);
					    range_slider.setUpperValue(upper_value);
					}
					else
					{
						range_slider.setUpperValue(upper_value);
						range_slider.setValue(lower_value);
					}
					range_scrollbar_changing = false;

					append_data = false;
					persistent_data = false;
					sample_information.setText("");
					
					slope_output.setText("");
					startpoint_set = false;
					midpoint_set = false;
					endpoint_set = false;
					
					// Resegment the data.
					apply_item.doClick();
				}
			}
		}
	}

	class RangeButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			String offset_string = offset_information.getText();
			String range_string = range_information.getText();
			double current_offset = Double.valueOf(offset_string);
			double current_range = Double.valueOf(range_string);

			if (current_offset < 15 || current_offset > (75 - current_range) || current_range < 0 || current_range > (75 - current_offset))
			{
				offset_string = String.format("%,.4f", offset);
				range_string = String.format("%,.4f", range);
				System.out.println("Invalid input: offset = " + offset_string + " range = " + range_string);
				offset_string = String.format("%,.4f", offset);
				range_string = String.format("%,.4f", range);
				offset_information.setText(offset_string);
				range_information.setText(range_string);
			} 
			else if (range_slider_changing == false && range_scrollbar_changing == false)
			{
				range_button_changing = true;
				offset = current_offset;
				range  = current_range;

				// Reset the scrollbar.
				double normal_start    = (offset - 15) / 60;
				double normal_stop     = (offset + range - 15) / 60;
				double normal_range    = normal_stop - normal_start;
				double normal_position = normal_start + normal_range / 2;
				normal_position       *= 2000;
				range_scrollbar.setValue((int)normal_position);

				// Reset the slider.
				normal_start *= 2000;
				normal_stop  *= 2000;
				range_slider.setValue((int) normal_start);
				range_slider.setUpperValue((int)normal_stop);
				range_button_changing = false;

				// Resegment the data.
				apply_item.doClick();
				
			}
		}
	};
	
	class RangeSliderHandler implements ChangeListener
	{
		public void stateChanged(ChangeEvent e)
		{
			if (range_scrollbar_changing == false && range_button_changing == false)
			{
				range_slider_changing = true;
				RangeSlider slider = (RangeSlider) e.getSource();
				if (slider.getValueIsAdjusting() == false)
				{
					int value = slider.getValue();
					int upper_value = slider.getUpperValue();
					
					// Check to see lower and upper values are the same.
					// Cleaner to change this in the range slider implementation.
					if(value == upper_value)
					{
						upper_value++;
						slider.setUpperValue(upper_value);
					}
					double lower_normal_position = (double) value;
					lower_normal_position /= 2000.;
					
					double upper_normal_position = (double) upper_value;
					upper_normal_position /= 2000;
					
					double normal_range = upper_normal_position - lower_normal_position;
					
					double normal_scrollbar_position = lower_normal_position + normal_range / 2;
					normal_scrollbar_position *= 2000;
					range_scrollbar.setValue((int)normal_scrollbar_position);
					
					lower_normal_position *= 60.;
					offset = lower_normal_position + 15;
					upper_normal_position *= 60.;
					upper_normal_position += 15.;
					range = upper_normal_position - offset;
					
					String string = String.format("%,.2f", offset);
					offset_information.setText(string);
					string = String.format("%,.2f", range);
					range_information.setText(string);
					range_slider_changing = false;

					append_data = false;
					persistent_data = false;
					sample_information.setText("");
					
					slope_output.setText("");
					startpoint_set = false;
					midpoint_set = false;
					endpoint_set = false;
					
					// Resegment the data.
					apply_item.doClick();
				}
			}
		}
	}

	class LoadInputHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			String input_string = load_input.getText();
			StringTokenizer input_tokenizer = new StringTokenizer(input_string, ":./");

			String line_string = input_tokenizer.nextToken();
			int current_line = Integer.parseInt(line_string);

			String sensor_string = input_tokenizer.nextToken();
			int current_sensor = Integer.parseInt(sensor_string);

			int number_of_pairs = 10;
			if (input_tokenizer.hasMoreTokens())
			{
				String number_of_pairs_string = input_tokenizer.nextToken();
				number_of_pairs = Integer.parseInt(number_of_pairs_string);
			}

			String[] line_sensor_pair = new String[10];

			int current_pair = 0;

			if (current_line % 2 == 1)
			{
				for (int i = current_sensor; i < 5; i++)
				{
					String current_string = new String(current_line + ":" + i);
					line_sensor_pair[current_pair] = current_string;
					current_pair++;
				}
			} 
			else
			{
				for (int i = current_sensor; i >= 0; i--)
				{
					String current_string = new String(current_line + ":" + i);
					line_sensor_pair[current_pair] = current_string;
					current_pair++;
				}
			}
			current_line++;
			if (current_line % 2 == 1)
			{
				for (int i = 0; i < 5; i++)
				{
					String current_string = new String(current_line + ":" + i);
					line_sensor_pair[current_pair] = current_string;
					current_pair++;
				}
			} 
			else
			{
				for (int i = 4; i >= 0; i--)
				{
					String current_string = new String(current_line + ":" + i);
					line_sensor_pair[current_pair] = current_string;
					current_pair++;
				}
			}
			current_line++;
			outer: if (current_pair < 10)
			{
				if (current_line % 2 == 1)
				{
					for (int i = 0; i < 5; i++)
					{
						String current_string = new String(current_line + ":" + i);
						line_sensor_pair[current_pair] = current_string;
						current_pair++;
						if (current_pair == 10)
							break outer;
					}
				} 
				else
				{
					for (int i = 4; i >= 0; i--)
					{
						String current_string = new String(current_line + ":" + i);
						line_sensor_pair[current_pair] = current_string;
						current_pair++;
						if (current_pair == 10)
							break outer;
					}
				}
			}

			for (int i = 0; i < 10; i++)
			{
				sensor[i].setText("");
			}
			for (int i = 0; i < number_of_pairs; i++)
			{
				sensor[i].setText(line_sensor_pair[i]);
			}
			// Resegment the data.
			apply_item.doClick();
		}
	}

	class PrintHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
		    try
		    {
			    PrintService service = PrintServiceLookup.lookupDefaultPrintService();
			    DocPrintJob  job     = service.createPrintJob();
			    DocFlavor    flavor  = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
			    SimpleDoc    doc     = new SimpleDoc(new GraphPrintable(), flavor, null);
			    job.print(doc, null);
		    }
		    catch(Exception e)
		    {
		    	System.out.println(e.toString());
		    }
		}
	}
	
	class GraphPrintable implements Printable 
	{
		 public int print(Graphics g, PageFormat pf, int pageIndex) 
		 {
		      Graphics2D graphics = (Graphics2D) g;
		      g.translate((int) (pf.getImageableX()), (int) (pf.getImageableY()));
		      if (pageIndex == 0) 
		      {
		          double pageWidth   = pf.getImageableWidth();
		          double pageHeight  = pf.getImageableHeight();
		          double imageWidth  = buffered_image.getWidth();
		          double imageHeight = buffered_image.getHeight();
		          double scaleX = pageWidth / imageWidth;
		          double scaleY = pageHeight / imageHeight;
		          double scaleFactor = Math.min(scaleX, scaleY);
		          graphics.scale(scaleFactor, scaleFactor);
		          g.drawImage(buffered_image, 0, 0, null);
		          return Printable.PAGE_EXISTS;
		      }
		      return Printable.NO_SUCH_PAGE;
		 }
	}
	
	class SaveHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ev)
		{
			int size = sensor_data.size();
			if (size == 0)
			{
				System.out.println("No data to save.");
				return;
			} 
			else
			{
				FileDialog file_dialog = new FileDialog(frame, "Save Segment", FileDialog.SAVE);
				file_dialog.setVisible(true);
				String filename = file_dialog.getFile();
				if (filename != "")
				{
					String current_directory = file_dialog.getDirectory();
					StringTokenizer filename_tokenizer = new StringTokenizer(filename, ".");
					int number_of_tokens = filename_tokenizer.countTokens();
					if(number_of_tokens != 2)
					{
					    System.out.println("Filename requires .txt or .fp or .png extension.");
					    return;
					}
					String name = filename_tokenizer.nextToken();
					String extension = filename_tokenizer.nextToken();
					if(!extension.equals("txt") && !extension.equals("fp")&& !extension.equals("png"))
					{
						System.out.println("Filename requires .txt or .fp or .png extension.");
					    return;	
					}
					if(extension.equals("txt"))
					{
						// General purpose text file to use with gnuplot.
						System.out.println("Writing text file.");
					    try (PrintWriter output = new PrintWriter(current_directory + filename))
					    {
						    // The first 4 elements in the sensor data are local min, local max, global min,
					        // and global max.
					        // The rest are array lists of samples, with some information prepended.
					    	double intensity_min = (double) sensor_data.get(0);
							String intensity_min_string = String.format("%.2f", intensity_min);
							for (int i = 4; i < size; i++)
							{
								// The first 4 elements are flight line, sensor, visibility, and transparency.
								// The last two are yes/no strings.
								ArrayList segment_list = (ArrayList) sensor_data.get(i);
								int line = (int) segment_list.get(0);
								int sensor = (int) segment_list.get(1);

								output.println("#Sensor " + sensor + ", Line " + line);

								double ideal_x = line * 2;
								if (line % 2 == 0)
								{
									ideal_x += ((4 - sensor) * .5);
								} else
								{
									ideal_x += sensor * .5;
								}
								// Keeping track of how much deviation there is in raster.
								String ideal_string = String.format("%.2f", ideal_x);
	                            
								// Plot individual sensor curves.
								String xstring, ystring;
								for (int j = 4; j < segment_list.size(); j++)
								{
									Sample sample = (Sample) segment_list.get(j);
									xstring = String.format("%.2f", sample.x);
									ystring = String.format("%.2f", sample.y);
									String intensity_string = String.format("%.2f", sample.intensity);
									output.println(xstring + " " + ystring + " " + intensity_string + " " + ideal_string);
								}
								
								// Seperate this sensor curve from the next one with an extra line feed.
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
					else if(extension.equals("fp"))
					{
						// Special format to generate fence plots in gnuplot.
						System.out.println("Writing fence plot file.");
					    try (PrintWriter output = new PrintWriter(current_directory + filename))
					    {
						    // The first 4 elements in the sensor data are local min, local max, global min,
						    // and global max.
						    // The rest are array lists of samples, with some information prepended.
						    double intensity_min = (double) sensor_data.get(0);
						    String intensity_min_string = String.format("%.2f", intensity_min);
						    for (int i = 4; i < size; i++)
						    {
							    // The first 4 elements are flight line, sensor, visibility, and transparency.
							    // The last two are yes/no strings.
							    ArrayList segment_list = (ArrayList) sensor_data.get(i);
							    int line = (int) segment_list.get(0);
							    int sensor = (int) segment_list.get(1);

							    output.println("#Sensor " + sensor + ", Line " + line);

							    double ideal_x = line * 2;
							    if (line % 2 == 0)
								  ideal_x += ((4 - sensor) * .5);
							    else
								    ideal_x += sensor * .5;
							    // Keeping track of how much deviation there is in raster.
							    String ideal_string = String.format("%.2f", ideal_x);

							    // Make the first bottom corner of our fence plot.
							    Sample init_sample = (Sample) segment_list.get(4);
							    String xstring = String.format("%.2f", init_sample.x);
							    String ystring = String.format("%.2f", init_sample.y);
							    output.println(xstring + " " + ystring + " " + intensity_min_string + " " + ideal_string);
							
							    // Plot actual data points.
							    for (int j = 4; j < segment_list.size(); j++)
							    {
							        Sample sample = (Sample) segment_list.get(j);
							        xstring = String.format("%.2f", sample.x);
							        ystring = String.format("%.2f", sample.y);
							        String intensity_string = String.format("%.2f", sample.intensity);
							        output.println(xstring + " " + ystring + " " + intensity_string + " " + ideal_string);
							    }
							
							    // Add the second bottom corner to our fence plot.
							    output.println(xstring + " " + ystring + " " + intensity_min_string + " " + ideal_string);
							
							    // Connect it to our first bottom corner to get our polygon.
							    xstring = String.format("%.2f", init_sample.x);
							    ystring = String.format("%.2f", init_sample.y);
							    output.println(xstring + " " + ystring + " " + intensity_min_string + " " + ideal_string);
							
							    // Seperate this sensor polygon from the next one with an extra line feed.
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
					else if(extension.equals("png"))
					{
						int           image_xdim   = buffered_image.getWidth();
						int           image_ydim   = buffered_image.getHeight();
						BufferedImage print_image  = new BufferedImage(image_xdim, image_ydim, BufferedImage.TYPE_INT_RGB);
						Graphics2D    print_buffer = (Graphics2D) print_image.getGraphics(); 
						print_buffer.drawImage(buffered_image, 0, 0,  null);	
						try 
	        	        {  
	        	            ImageIO.write(print_image, "png", new File(current_directory + filename)); 
	        	        } 
	        	        catch(IOException e) 
	        	        {  
	        	            e.printStackTrace(); 
	        	        }  
					}
				}
			}
		}
	}

	class OrderCanvas extends Canvas
	{
		public void paint(Graphics g)
		{
			Rectangle visible_area = g.getClipBounds();
			int xdim = (int) visible_area.getWidth();
			int ydim = (int) visible_area.getHeight();
			Graphics2D g2 = (Graphics2D) g;
            
			if(in_order)
			{
				Color color = new Color(0, 175, 0);
				g2.setColor(color);
			}
			else
			{
				Color color = new Color(175, 0, 0);
				g2.setColor(color);	
			}
			
		    g2.fillRect(0, 0, xdim, ydim);
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
			else if (button == 3)
			{
				if (index == 9)
					sensor[index].setText("");
				else
				{
					int current_index = index + 1;
					while(current_index < 10)
					{ 
						String line_sensor_pair = sensor[current_index].getText();
						sensor[current_index - 1].setText(line_sensor_pair);
						current_index ++;
					}
					sensor[9].setText("");
				}
				
				// Resegment the data.
				apply_item.doClick();
			}
		}
	}

	class SegmentImageCanvas extends Canvas
	{
		int top_margin    = 10;
		int bottom_margin = 30;
		int left_margin   = 50;
		int right_margin  = 20;
		int xdim          = 600;
		int ydim          = 600;
		int image_xdim    = xdim + left_margin + right_margin;
		int image_ydim    = ydim + top_margin  + bottom_margin;
		
		public void paint(Graphics g)
		{
			int size = sensor_data.size();
			if(size < 5)
				return;
			//BufferedImage buffered_image       = new BufferedImage(image_xdim, image_ydim, BufferedImage.TYPE_INT_RGB);
			buffered_image       = new BufferedImage(image_xdim, image_ydim, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics_buffer = (Graphics2D) buffered_image.getGraphics();
			FontMetrics font_metrics   = graphics_buffer.getFontMetrics();
			
			graphics_buffer.setColor(java.awt.Color.WHITE);
			graphics_buffer.fillRect(0, 0, image_xdim, image_ydim);
			
			int a1 = left_margin;
			int a2 = left_margin + 600;
			int b1 = top_margin;
			int b2 = top_margin + 600;
			
			graphics_buffer.setColor(java.awt.Color.BLACK);
			graphics_buffer.drawRect(a1, b1, xdim, ydim);
			int string_width  = font_metrics.stringWidth("77");
			int string_height = font_metrics.getAscent();
			int position      = b2;
			for(int i = 0; i < 12; i++)
			{
				graphics_buffer.drawLine(a1, position, a2, position);
				String string;
				int j = i * 5;
				if(j < 10)
					string = new String(" " + j);
				else
				    string = new String("" + j);
				graphics_buffer.drawString(string, a1 - (12 + string_width), position + (string_height / 2));
				position -= 50;
			}
			graphics_buffer.drawLine(a1, position, a2, position);
			String string = new String("60");
			graphics_buffer.drawString(string, a1 - (12 + string_width), position + (string_height / 2));
			int number_of_segments = size - 4;
			double xmin = Double.MAX_VALUE;
			for (int i = 0; i < number_of_segments; i++)
			{
				ArrayList sensor_list = (ArrayList) sensor_data.get(i + 4);
				size = sensor_list.size();
				for(int j = 4; j < size; j++)
				{
					Sample sample = (Sample) sensor_list.get(j);
					double x = sample.x;
					if(xmin > x)
						xmin = x;
				}
			}
			position      = a1;
			double  value = xmin;
			String  value_string = String.format("%.1f", value);
			string_width  = font_metrics.stringWidth(value_string);
			for(int i = 0; i < 12; i++)
			{
				graphics_buffer.drawLine(position, b2, position, b1);
				graphics_buffer.drawString(value_string, position - (string_width / 2), b2 + (12 + string_height));
				position += 50;
				value    += .5;
				value_string = String.format("%.1f", value);
				string_width  = font_metrics.stringWidth(value_string);
			}
			graphics_buffer.drawLine(position, b2, position, b1);
			graphics_buffer.drawString(value_string, position - (string_width / 2), b2 + (12 + string_height));
			
			double min_intensity   = (double)sensor_data.get(0);
			double max_intensity   = (double)sensor_data.get(1);
			double intensity_range = max_intensity - min_intensity;
			
			
			for (int i = 0; i < number_of_segments; i++)
			{
				ArrayList sensor_list = (ArrayList) sensor_data.get(i + 4);
				size = sensor_list.size();
				for(int j = 4; j < size; j++)
				{
					Sample sample = (Sample) sensor_list.get(j);
					double x = sample.x;
					double y = sample.y;
					x -= xmin;
					x *= 100;
					x += left_margin;
					y *= 10;
					y -= 15;
					y  = ydim - y;
					y += top_margin;
					
					double intensity = sample.intensity - min_intensity;
					intensity /= intensity_range;
					int rgb_value = (int)(intensity * 255.);
					Color color = new Color(rgb_value, rgb_value, rgb_value);
					graphics_buffer.setColor(color);
					graphics_buffer.fillRect((int)x, (int)y, 40, 1);
					
				}
			}
			g.drawImage(buffered_image, 0, 0,  this);
		}
	}
	
	class LineImageCanvas extends Canvas
	{
		int top_margin    = 10;
		int bottom_margin = 30;
		int left_margin   = 50;
		int right_margin  = 20;
		int xdim          = 800;
		int ydim          = 900;
		int image_xdim    = xdim + left_margin + right_margin;
		int image_ydim    = ydim + top_margin  + bottom_margin;
		
		int[][] line_array;
		int[] current_line;
		int[] current_sensor;

		LineImageCanvas()
		{
			line_array = ObjectMapper.getUnclippedLineArray();
			current_line = new int[10];
			current_sensor = new int[10];
			image_grid_data = new ArrayList[image_ydim][image_xdim];
			for (int i = 0; i < image_ydim; i++)
				for (int j = 0; j < image_xdim; j++)
					image_grid_data[i][j] = new ArrayList();
		}
		
		public void paint(Graphics g)
		{
			for (int i = 0; i < image_ydim; i++)
			{
				for (int j = 0; j < image_xdim; j++)
				{
					ArrayList data_list = image_grid_data[i][j];
					data_list.clear();
				}
			}
			
			BufferedImage buffered_image = new BufferedImage(image_xdim, image_ydim, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics_buffer   = (Graphics2D) buffered_image.getGraphics();
			FontMetrics font_metrics     = graphics_buffer.getFontMetrics();
			
			graphics_buffer.setColor(java.awt.Color.WHITE);
			graphics_buffer.fillRect(0, 0, image_xdim, image_ydim);
			
			int a1 = left_margin;
			int a2 = left_margin + 800;
			int b1 = top_margin;
			int b2 = top_margin + 900;
			
			graphics_buffer.setColor(java.awt.Color.BLACK);
			graphics_buffer.drawRect(a1, b1, xdim, ydim);
			int string_width  = font_metrics.stringWidth("77");
			int string_height = font_metrics.getAscent();
			int position      = b2;
			for(int i = 0; i < 18; i++)
			{
				graphics_buffer.drawLine(a1, position, a2, position);
				String string;
				int j = i * 5;
				if(j < 10)
					string = new String(" " + j);
				else
				    string = new String("" + j);
				graphics_buffer.drawString(string, a1 - (12 + string_width), position + (string_height / 2));
				position -= 50;
			}
			graphics_buffer.drawLine(a1, position, a2, position);
			String string = new String("90");
			graphics_buffer.drawString(string, a1 - (12 + string_width), position + (string_height / 2));
			
			
			for (int i = 0; i < 10; i++)
			{
				try
				{
					String line_sensor_pair = sensor[i].getText();
					if(line_sensor_pair.equals(""))
					{
					    if(i < 9)
					    {
					    	line_sensor_pair = sensor[i + 1].getText();
					    	sensor[i + 1].setText("");
					    	sensor[i].setText(line_sensor_pair);
					    }
					}
						
					StringTokenizer tokenizer = new StringTokenizer(line_sensor_pair, ":");
					int number_of_tokens = tokenizer.countTokens();
					if (number_of_tokens == 2)
					{
						String line_string = tokenizer.nextToken(":");
						int next_line = Integer.parseInt(line_string);
						String sensor_string = tokenizer.nextToken(":");
						int next_sensor = Integer.parseInt(sensor_string);

						// Do a check for valid input
						if (next_line >= 0 && next_line < 30 && next_sensor >= 0 && next_sensor < 5)
						{
							current_line[i] = next_line;
							current_sensor[i] = next_sensor;
						} 
						else
						{
							current_line[i] = -1;
							current_sensor[i] = -1;
						}
					} 
					else
					{
						current_line[i] = -1;
						current_sensor[i] = -1;
					}
				} 
				catch (Exception exception)
				{
					current_line[i] = -1;
					current_sensor[i] = -1;
				}
			}
			
			
			// Do a first pass thru the data to get the line max / min.
			// This is so we can prepend it so the line image canvas
			// can use to implement autoscaling.
			double line_min = Double.MAX_VALUE;
			double line_max = -Double.MAX_VALUE;
			double xmin     = Double.MAX_VALUE;
			double xmax     = 0;
			for (int i = 0; i < 10; i++)
			{
				if (current_line[i] != -1)
				{
					int start = line_array[current_line[i]][0];
					int stop = line_array[current_line[i]][1];

					if (current_line[i] % 2 == 0)
					{
						for (int j = start + current_sensor[i]; j < stop; j += 5)
						{
							Sample sample = (Sample) data.get(j);
							if (line_min > sample.intensity)
								line_min = sample.intensity;
							if (line_max < sample.intensity)
								line_max = sample.intensity;
							if(sample.x < xmin)
								xmin = sample.x;
						}
					} 
					else
					{
						for (int j = stop - (1 + (4 - current_sensor[i])); j >= start; j -= 5)
						{
							Sample sample = (Sample) data.get(j);
							if (line_min > sample.intensity)
								line_min = sample.intensity;
							if (line_max < sample.intensity)
								line_max = sample.intensity;
							if(sample.x < xmin)
								xmin = sample.x;
						}
					}
				}
			}
			xmax = xmin + 8;
			
		    ArrayList line_sensor_data = new ArrayList();
			for (int i = 0; i < 10; i++)
			{
				if (current_line[i] != -1)
				{
					ArrayList segment_data = new ArrayList();
					segment_data.add(current_line[i]);
					segment_data.add(current_sensor[i]);


					int start = line_array[current_line[i]][0];
					int stop = line_array[current_line[i]][1];
					if (current_line[i] % 2 == 0)
					{
						for (int j = start + current_sensor[i]; j < stop; j += 5)
						{
							Sample sample = (Sample) data.get(j);
					       segment_data.add(sample);
						}
					} 
					else
					{
						for (int j = stop - (1 + (4 - current_sensor[i])); j >= start; j -= 5)
						{
							Sample sample = (Sample) data.get(j);
						    segment_data.add(sample);
						}
					}
					line_sensor_data.add(segment_data);
				}
			}
			
			position      = a1;
			double  value = xmin;
			String  value_string = String.format("%.1f", value);
			string_width  = font_metrics.stringWidth(value_string);
			for(int i = 0; i < 16; i++)
			{
				graphics_buffer.drawLine(position, b2, position, b1);
				graphics_buffer.drawString(value_string, position - (string_width / 2), b2 + (12 + string_height));
				position += 50;
				value    += .5;
				value_string = String.format("%.1f", value);
				string_width  = font_metrics.stringWidth(value_string);
			}
			graphics_buffer.drawLine(position, b2, position, b1);
			graphics_buffer.drawString(value_string, position - (string_width / 2), b2 + (12 + string_height));
			double min_intensity   = line_min;
			double max_intensity   = line_max;
			double intensity_range = max_intensity - min_intensity;
			
			int number_of_segments = line_sensor_data.size();	
			for (int i = 0; i < number_of_segments; i++)
			{
				ArrayList line_data_list = (ArrayList) line_sensor_data.get(i);
				int size = line_data_list.size();
				
				int current_line   = (int)line_data_list.get(0);
				int current_sensor = (int)line_data_list.get(1);
				for(int j = 2; j < size; j++)
				{
					Sample sample = (Sample) line_data_list.get(j);
					double x = sample.x;
					double y = sample.y;
					x -= xmin;
					x *= 100;
					x += left_margin;
					y *= 10;
					y  = ydim - y;
					y += top_margin;
					double intensity = sample.intensity - min_intensity;
					intensity /= intensity_range;
					int rgb_value = (int)(intensity * 255.);
					Color color = new Color(rgb_value, rgb_value, rgb_value);
					graphics_buffer.setColor(color);
					int start_x = (int)x - 10;
					if(start_x < left_margin)
						start_x = left_margin;
					int end_x = (int)x + 10;
					if(end_x > right_margin)
						end_x = right_margin;
					graphics_buffer.fillRect(start_x, (int)y, end_x, 1);
					
					int x_index = (int)x;
					int y_index = (int)y;
					
					for(int k = 0; k < 10; k++)
					{
						ArrayList data_list = image_grid_data[y_index][x_index + k];
						data_list.add(current_line);
						data_list.add(current_sensor);
						data_list.add(sample);
						if(k != 0)
						{
						    data_list = image_grid_data[y_index][x_index - k];
						    data_list.add(current_line);
							data_list.add(current_sensor);
							data_list.add(sample);
						}
						
					}
				}
			}
			
			double [][] location_array = ObjectMapper.getObjectLocationArray();
			int length = location_array.length;
			graphics_buffer.setColor(java.awt.Color.RED);	
			for(int i = 0; i < length; i++)
			{
				double x = location_array[i][0];
				double y = location_array[i][1];
				x -= global_xmin;
				y -= global_ymin;
				if(x >= xmin && x <= xmax)
				{
					x -= xmin;
					x *= 100;
					x += left_margin;
					
					y *= 10;
					y = ydim - y;
					y += top_margin;
					
				    graphics_buffer.fillOval((int)(x - 1), (int)(y - 1), 3, 3);
				    // If we want targent numbers.
				    String object_string = Integer.toString(i + 1); 
				    graphics_buffer.drawString(object_string, (int)(x + 2), (int)y); 
				}
			}
			g.drawImage(buffered_image, 0, 0,  this);
		}
	}
	
	class ImageMouseHandler extends MouseAdapter
	{
		boolean persistent_sample_information = false;

		public void mouseClicked(MouseEvent event)
		{
			int button = event.getButton();
			if (button == 3)
			{
				if (persistent_sample_information == false)
					persistent_sample_information = true;
				else
					persistent_sample_information = false;
			}
		}

		public void mousePressed(MouseEvent event)
		{
			int button = event.getButton();

			if (button == 1)
			{
				Point location_point = frame.getLocation();
				int frame_x = (int) location_point.getX();
				frame_x += 830;
				int frame_y = (int) location_point.getY();

				information_dialog.setLocation(frame_x, frame_y);
				information_dialog.pack();
				information_dialog.setVisible(true);
				
				// Blank information panel when a pixel is traversed that is not associated with data.
				sample_information.setText("");
				
				if(image_grid_data != null)
				{
					int       x         = event.getX();
					int       y         = event.getY();
					ArrayList data_list = image_grid_data[y][x];
					int       size      = data_list.size();
					int       index     = 0;
					while(size > 0)
					{
						int current_line  = (int) data_list.get(index + 0);
						int current_sensor = (int) data_list.get(index + 1);
						Sample sample = (Sample) data_list.get(index + 2);
						double current_intensity = sample.intensity;
						double current_x = sample.x;
						double current_y = sample.y;

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
						information_string = new String("  Absolute y: " + number_string + "\n\n");
						sample_information.append(information_string);
						
						size  -= 3;
						index += 3;
					}
				}
			}
		}

		public void mouseReleased(MouseEvent event)
		{
			int button = event.getButton();
			if (button == 1 && persistent_sample_information == false)
				information_dialog.setVisible(false);
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

			int size = sensor_data.size();
			int number_of_segments = 1;
			if (size > 0)
			{
				number_of_segments = size - 4;
			}
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
						g2.setColor(outline_color[i]);
						g2.drawRect(a1, b2, graph_xdim, graph_ydim);
					} 
					else
					{
						if (!transparent[i])
						{
							g2.setColor(fill_color[(number_of_segments - 1) - i]);
							g2.fillRect(a1, b2, graph_xdim, graph_ydim);
						}
						g2.setColor(outline_color[(number_of_segments - 1) - i]);
						g2.drawRect(a1, b2, graph_xdim, graph_ydim);
					}
				}
			}
		}
	}

	class SortCanvas extends Canvas
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
			int bottom_margin = 10;
			int left_margin   = 40;
			int right_margin  = 10;
			int graph_xdim    = xdim - (left_margin + right_margin);
			int graph_ydim    = ydim - (top_margin + bottom_margin);
			
			double a1 = left_margin;
			double a2 = left_margin + graph_xdim;
			double b1 = top_margin;
			double b2 = top_margin + graph_ydim;
			
			double xmin = Double.MAX_VALUE;
			double xmax = Double.MIN_NORMAL;
			for(int i = 4; i < sensor_data.size(); i++)
			{
			    ArrayList sensor_list = (ArrayList)sensor_data.get(i);
			    for(int j = 4; j < sensor_list.size(); j++)
			    {
			    	Sample sample = (Sample)sensor_list.get(j);
			    	if(sample.x < xmin)
			    		xmin = sample.x;
			    	if(sample.x > xmax)
			    		xmax = sample.x;
			    }
			}
			
			double xrange = range;
			double yrange = xmax - xmin;
			graphics_buffer.setStroke(new BasicStroke(3));
			for(int i = 4; i < sensor_data.size(); i++)
			{
			    ArrayList sensor_list = (ArrayList)sensor_data.get(i);
			    Sample previous_sample = (Sample)sensor_list.get(4);
			    
			    // Switching x and y to match orientation of graphs
			    double x = previous_sample.y;
			    double y = previous_sample.x;
			    
			    x -= offset;
			    x /= xrange;
        	    x *= graph_xdim;
        	    x += left_margin;
        	    
        	    y -= xmin;
        	    y /= yrange;
        	    y *= graph_ydim;
        	    y = graph_ydim - y;
        	    y += top_margin;
			  
        	    int    line     = (int)sensor_list.get(0);
        	    int    sensor   = (int)sensor_list.get(1);
        	    String line_sensor_pair = new String(line + ":" + sensor);
        	    string_width = font_metrics.stringWidth(line_sensor_pair);
        	    graphics_buffer.setColor(java.awt.Color.BLACK);
			    graphics_buffer.drawString(line_sensor_pair, (int)x - (string_width + 5), (int)y);
			    graphics_buffer.setColor(fill_color[i - 4]);
			    Point2D.Double previous = new Point2D.Double(x, y);
			    for(int j = 5; j < sensor_list.size(); j++)
				{
					Sample sample = (Sample)sensor_list.get(j);
					x = sample.y;
				    y = sample.x;
				    
				    x -= offset;
				    x /= xrange;
	        	    x *= graph_xdim;
	        	    x += left_margin;
	        	    
	        	    y -= xmin;
	        	    y /= yrange;
	        	    y *= graph_ydim;
	        	    y = graph_ydim - y;
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
			
			double current_position = graph_xdim;
			current_position        *= sort_location;
			current_position        += left_margin;
			graphics_buffer.setColor(java.awt.Color.BLUE);
			graphics_buffer.drawLine((int) current_position, (int)b1, (int) current_position, (int)b2);
			double current_ylocation = range * sort_location + offset;
			String position_string = String.format("%,.2f",  current_ylocation); 
			String string = new String("Y = " + position_string);
			string_width = font_metrics.stringWidth(string);
			if(current_ylocation < 45)
			    graphics_buffer.drawString(string, (int)current_position + 10, graph_ydim / 2 + top_margin);
			else
				graphics_buffer.drawString(string, (int)current_position - 10 - string_width, graph_ydim / 2 + top_margin);
				
			graphics.drawImage(buffered_image, 0, 0, null);
		}   	
	}
	
	class SortLocationHandler implements AdjustmentListener
	{
		public void adjustmentValueChanged(AdjustmentEvent event)
		{
			int location = event.getValue();
			sort_location = (double)location / 500.;
			
			double current_position = range * sort_location + offset;
			xlist.clear();
			
			int size = sensor_data.size();
			int number_of_segments = size - 4;
			order_information.append("\n");
			double previous_x = 0;
			in_order = true;
			for(int i = 0; i < number_of_segments; i++)
			{
				ArrayList sensor_list    = (ArrayList) sensor_data.get(i + 4);
				int       current_line   = (int)sensor_list.get(0);
				int       current_sensor = (int)sensor_list.get(1);
				size                     = sensor_list.size();
				
				Sample sample = (Sample)sensor_list.get(4);
				int j = 5;
				while(sample.y < current_position && j < size)
				{
					sample = (Sample)sensor_list.get(j);
					j++;
				}
				xlist.add(sample.x);
				
				if(i == 0)
				{
					previous_x = sample.x;  	
				}
				else
				{
				    if(sample.x < previous_x)
				    	in_order = false;
				    previous_x = sample.x;
				}
				
				String data_string       = new String(current_line + ":" + current_sensor);
				String xstring           = String.format("%,.2f", sample.x);
				String order_string = new String(data_string + "   " + xstring);
				if(order_information != null)
					order_information.append(order_string + "\n");	
				JScrollBar this_scrollbar = order_scrollpane.getVerticalScrollBar();
				int max = this_scrollbar.getMaximum();
				this_scrollbar.setValue(max);
			}
			if(sort_canvas != null)
			    sort_canvas.repaint();
			if(order_canvas != null)
			    order_canvas.repaint();
		}
	}
	
	class SortButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			int number_of_segments     = sensor_data.size() - 4;
			ArrayList line_sensor_list = new ArrayList();               
			Hashtable sensor_table     = new Hashtable();
	
			
			for(int i = 0; i < number_of_segments; i++)
			{
				String line_id = sensor[i].getText();
				line_sensor_list.add(line_id);
			}
			// We're using x values as keys to a hashtable,
			// so we need to make sure none of them are equal.
			// We'll add a small value to subsequent numbers,
			// so when values are equal the order remains the same.
			for(int i = 0; i < number_of_segments; i++)
			{
				double addend = .000001;
				double previous_x = (double)xlist.get(i);
				for(int j = i + 1; j < 10; j++)
				{
					double x = (double)xlist.get(j);
					if(previous_x == x)
					{
						x += addend;
						xlist.set(j, x);
						addend *= 2;
					}
				}
			}
			for(int i = 0; i < number_of_segments; i++)
			{
				// Modified data.
				double    key         = (double)xlist.get(i); 
				String    line_id     = (String)line_sensor_list.get(i);
				sensor_table.put(key, line_id);
			}
			Collections.sort(xlist);
			order_information.append("\n");
			for(int i = 0; i < number_of_segments; i++)
			{
			    double x       = (double)xlist.get(i);
			    String xstring = String.format("%,.2f", x);
			    String line_id = (String)sensor_table.get(x);
			    sensor[i].setText(line_id);
			    order_information.append(line_id + "  " + xstring + "\n");
			}
			in_order = true;
			order_canvas.repaint();
			apply_item.doClick();	
		}
	}
	
	class LocationCanvas extends Canvas
	{
		public void paint(Graphics g)
		{
			Rectangle visible_area = g.getClipBounds();

			int xdim = (int) visible_area.getWidth();
			int ydim = (int) visible_area.getHeight();
			
			double xrange = global_xmax - global_xmin;
			double yrange = global_ymax - global_ymin;
			
			double xfactor = (double)xdim / xrange;
			double yfactor = (double)ydim / yrange;

			int size = sensor_data.size();
			int number_of_segments = 0;
			if (size > 0)
			{
				number_of_segments = size - 4;
			}
			
			boolean lineIncluded[] = new boolean[30];
			for(int i = 0; i < 30; i++)
				lineIncluded[i]	= false;
			
			for(int i = 0; i < number_of_segments; i++)
			{
				ArrayList sensor_list = (ArrayList)sensor_data.get(i + 4);
				int included_line = (int)sensor_list.get(0);
				lineIncluded[included_line] = true;
			}
			
			int number_of_lines_included = 0;
			for(int i = 0; i < 30; i++)
			{
				if(lineIncluded[i] == true)
					number_of_lines_included++;
			}
			
			ArrayList segment = new ArrayList();
			int[][] line_array = ObjectMapper.getUnclippedLineArray();


			for (int i = 0; i < 30; i++)
			{
				if(lineIncluded[i] == true)
				{
				    int start = line_array[i][0];
				    int stop = line_array[i][1];
				    segment.add(start);
				    segment.add(stop);
				}
			}
			
			Image buffered_image = new BufferedImage(xdim, ydim, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics_buffer = (Graphics2D) buffered_image.getGraphics();
			Font current_font = graphics_buffer.getFont();
			FontMetrics font_metrics = graphics_buffer.getFontMetrics(current_font);
			graphics_buffer.setColor(java.awt.Color.WHITE);
			graphics_buffer.fillRect(0, 0, xdim, ydim);
			graphics_buffer.setColor(java.awt.Color.BLACK);
			
			Sample sample = (Sample) data.get(2);
			int previous_x = (int)(sample.x * xfactor);
		    int previous_y = (int)(sample.y * yfactor);
		    previous_y     = ydim - previous_y;
		    for(int i = 7; i < data.size(); i += 5)
		    {
		    	sample = (Sample) data.get(i); 
			    if(sample.y >= offset && sample.y < offset + range)
			    {
			    	boolean isSegmented = false;
			    	for(int j = 0; j < segment.size(); j += 2)
			    	{
			    	    int start = (int)segment.get(j);
			    	    int stop  = (int)segment.get(j + 1);
			    	    if(i >= start && i < stop)
			    	        isSegmented = true;
			    	}
			    	if(isSegmented)
			    	{
			    		graphics_buffer.setColor(java.awt.Color.GREEN);
			    	    graphics_buffer.setStroke(new BasicStroke(3));
			    	}
			    	else
			    	{
			    		graphics_buffer.setColor(java.awt.Color.GRAY);
			    		graphics_buffer.setStroke(new BasicStroke(1));
			    	}
			    	int current_x = (int)(sample.x * xfactor);
				    int current_y = (int)(sample.y * yfactor);
				    current_y     = ydim - current_y;
			    	graphics_buffer.drawLine(previous_x,  previous_y, current_x, current_y);
			    	previous_x = current_x;
				    previous_y = current_y;
			    }
			    else
			    {
			    	graphics_buffer.setColor(java.awt.Color.GRAY);
			    	graphics_buffer.setStroke(new BasicStroke(1));
			    	int current_x = (int)(sample.x * xfactor);
				    int current_y = (int)(sample.y * yfactor);
				    current_y     = ydim - current_y;
			    	graphics_buffer.drawLine(previous_x,  previous_y, current_x, current_y);
			        previous_x = current_x;
				    previous_y = current_y;
			    }   	
		    }
			
		    double [][] location_array = ObjectMapper.getObjectLocationArray();
			int length = location_array.length;
			
			
			for(int i = 0; i < length; i++)
			{
				double x = location_array[i][0];
				double y = location_array[i][1];
				x -= global_xmin;
				y -= global_ymin;
				x *= xfactor;
				y *= yfactor;
				y = ydim - y;
				graphics_buffer.setColor(java.awt.Color.RED);
				graphics_buffer.fillOval((int)(x - 1), (int)(y - 1), 3, 3);
				// If we want targent numbers.
				graphics_buffer.setColor(java.awt.Color.BLACK);
				String object_string = Integer.toString(i + 1); 
				graphics_buffer.drawString(object_string, (int)(x + 2), (int)y); 
			}
			
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
			g.drawImage(buffered_image, 0, 0, null);
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
			double seg_min = (double) sensor_data.get(0);
			double seg_max = (double) sensor_data.get(1);
			double line_min = (double) sensor_data.get(2);
			double line_max = (double) sensor_data.get(3);
			double current_range = 0;
			int size = sensor_data.size();
			if (size > 0)
			{
				if(data_clipped)
				{
					String lower_bound_string = lower_bound.getText();
					String upper_bound_string = upper_bound.getText();
					min = Double.valueOf(lower_bound_string);
					max = Double.valueOf(upper_bound_string);
					current_range = max - min;
					
					double global_max = 0;
					double global_min = 0;
					
					if(autoscale)
					{
						global_max = seg_max;
						global_min = seg_min;
					}
					else
					{
						global_max = line_max;
						global_min = line_min;
					}
					
					if(max > global_max)
					{
						String intensity_string = String.format("%,.2f", max);
						int string_width = font_metrics.stringWidth(intensity_string);
						graphics.drawString(intensity_string, xdim / 2 - (string_width + 15), top_margin + string_height / 2);	
					}
					else
					{
						String intensity_string = String.format("%,.2f", global_max);
						int string_width = font_metrics.stringWidth(intensity_string);
						graphics.drawString(intensity_string, xdim / 2 - (string_width + 15), top_margin + string_height / 2);		
					}
					
					if(min < global_min)
					{
						String intensity_string = String.format("%,.2f", min);
						int string_width = font_metrics.stringWidth(intensity_string);
						graphics.drawString(intensity_string, xdim / 2 - (string_width + 15), ydim - bottom_margin);	
					}
					else
					{
						String intensity_string = String.format("%,.2f", global_min);
						int string_width = font_metrics.stringWidth(intensity_string);
						graphics.drawString(intensity_string, xdim / 2 - (string_width + 15), ydim - bottom_margin);	
					}
					
					current_range = 0;
					if(min < global_min)
					{
					    if(max > global_max)
					    	current_range = max - min;
					    else
					    	current_range = global_max - min;
					}
					else
					{
						if(max > global_max)
					    	current_range = max - global_min;
					    else
					    	current_range = global_max - global_min;   	
					}
					
					if(min > global_min)
					{
						double min_delta = global_min - min;
						min_delta /= current_range;
						double delta = min_delta * graph_ydim;
						graphics.drawLine(xdim / 2, ydim - ((int) -delta + bottom_margin), xdim / 2 + 10, ydim - ((int) -delta + bottom_margin));
						String intensity_string = String.format("%,.2f", min);
						graphics.drawString(intensity_string, xdim / 2 + 15, ydim - ((int) -delta + bottom_margin));		
					}
					else if(min < global_min)
					{
						double min_delta = min - global_min;
						min_delta /= current_range;
						double delta = min_delta * graph_ydim;
						graphics.drawLine(xdim / 2, ydim - ((int) -delta + bottom_margin), xdim / 2 + 10, ydim - ((int) -delta + bottom_margin));
						String intensity_string = String.format("%,.2f", global_min);
						graphics.drawString(intensity_string, xdim / 2 + 15, ydim - ((int) -delta + bottom_margin));	 	
					}
					else if(min == global_min) 
					{
						graphics.drawLine(xdim / 2 + 10, ydim - bottom_margin, xdim / 2, ydim - bottom_margin);	
					}
					
					if(max < global_max)
					{
						double max_delta = global_max - max;
						max_delta       /= current_range;
						double delta     = max_delta * graph_ydim;
						graphics.drawLine(xdim / 2, top_margin + (int) delta, xdim / 2 + 10, top_margin + (int) delta);
						String intensity_string = String.format("%,.2f", max);
						graphics.drawString(intensity_string, xdim / 2 + 15, top_margin + (int) delta);		
					}
					else if(max > global_max)
					{
						double max_delta = max - global_max;
						max_delta       /= current_range;
						double delta     = max_delta * graph_ydim;
						graphics.drawLine(xdim / 2, top_margin + (int) delta, xdim / 2 + 10, top_margin + (int) delta);
						String intensity_string = String.format("%,.2f", global_max);
						graphics.drawString(intensity_string, xdim / 2 + 15, top_margin + (int) delta);		
					}
					else if(max == global_max)
					{
						graphics.drawLine(xdim / 2 + 10, top_margin, xdim / 2, top_margin);	
					}
					
					if(min < 0  || global_min < 0)
					{
						current_range = 0;
						if(min < global_min)
						{
						    if(max > global_max)
						    	current_range = max - min;
						    else
						    	current_range = global_max - min;
						}
						else
						{
							if(max > global_max)
						    	current_range = max - global_min;
						    else
						    	current_range = global_max - global_min;   	
						}
						
						double zero_point = 0;
						if(max > global_max)
						    zero_point = max / current_range;
						else
							zero_point = global_max / current_range;
						zero_point *= graph_ydim;
						graphics.setColor(java.awt.Color.RED);
						graphics.drawLine(xdim / 2 - 10, top_margin + (int) zero_point, xdim / 2, top_margin + (int) zero_point);
						graphics.setColor(java.awt.Color.BLACK);
					}
				}
				else
				{
					if(autoscale)
					{
					    min = seg_min;
					    max = seg_max;
					}
					else
					{
						min = line_min;
						max = line_max;   	
					}
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
	}

	class DynamicRangeSliderHandler implements ChangeListener
	{
		public void stateChanged(ChangeEvent e)
		{
			RangeSlider slider = (RangeSlider) e.getSource();
			int lower = slider.getValue();
			int upper = slider.getUpperValue();
			if (dynamic_button_changing == false)
			{
				double min = 0;
				double max = 0;
				if(data_clipped)
				{
				    min = intensity_min;
				    max = intensity_max;
				}
				else if(autoscale)
				{
					min = (double) sensor_data.get(0);
					max = (double) sensor_data.get(1);
				} 
				else
				{
					min = (double) sensor_data.get(2);
					max = (double) sensor_data.get(3);
				}
				double current_range      = max - min;
				double fraction           = (double) lower / 100;
				double lower_value        = (fraction * current_range) + min;
				String lower_bound_string = String.format("%,.2f", lower_value);
				fraction                  = (double) upper / 100;
				double upper_value        = (fraction * current_range) + min;
				String upper_bound_string = String.format("%,.2f", upper_value);
				lower_bound.setText(lower_bound_string);
				upper_bound.setText(upper_bound_string);
			}
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