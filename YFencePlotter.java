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
import javax.swing.table.*;
import java.lang.*;

public class YFencePlotter
{
	public Canvas      data_canvas;
	public JScrollBar  data_scrollbar;
	public RangeSlider data_slider;
	public JFrame      frame;
	public boolean     data_scrollbar_changing, data_slider_changing;
	public double      global_xmin, global_xmax, global_ymin, global_ymax, global_intensity_min, global_intensity_max;
	public int         slider_resolution    = 2640;
	public int         scrollbar_resolution = 2640;
	public int         data_length          = 2640;
	public Color[]     fill_color           = new Color[10];
	public ArrayList   relative_data        = new ArrayList();
	public ArrayList   data                 = new ArrayList();
	public ArrayList   index                = new ArrayList();
	public double      data_offset          = .5;
	public double      data_range           = .0005;
	public double      normal_xstep         = .5;
	public double      normal_ystep         = .5;
	public int         x_remainder          = 0;
	public int         y_remainder          = 0;
	ArrayList[][]      pixel_data;
	
	boolean            raster_overlay       = false;
	
	public static void main(String[] args)
	{
		if(args.length != 1)
		{
			System.out.println("Usage: YFencePlotter <data file>");
			System.exit(0);
		} 
		else
		{
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
						System.out.println("Unexpected error " + e.toString());
					}
				}
				reader.close();
				for(int i = 0; i < original_data.size(); i++)
				{
					Sample sample = (Sample) original_data.get(i);
					sample.x -= global_xmin;
					sample.y -= global_ymin;
					relative_data.add(sample);
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
				slider_resolution    = data_length;
				scrollbar_resolution = data_length;
				
				int _index = 0;
				index.add(_index);
				total_distance  = 0;
				for(int i = 1; i < data_length; i++)
				{
					previous_sample = (Sample)relative_data.get(_index + 2);
					
					for(int j = _index + 7; j < relative_data.size(); j += 5)
					{
						Sample sample   = (Sample) relative_data.get(j);	
						double distance = getDistance(sample.x, sample.y, previous_sample.x, previous_sample.y);
						total_distance += distance;
						previous_sample = sample;
						if((int) total_distance >= i)
						{
							_index = j - 2;
							index.add(_index);
							break;
						}
					}
				}
				_index = relative_data.size() - 1;
				index.add(_index);
				
				data   = new ArrayList();
				previous_sample = (Sample)relative_data.get(2);
				int previous_index = 0;
				total_distance = 0;
				for(int i = 7; i < relative_data.size(); i += 5)
				{
					Sample sample   = (Sample) relative_data.get(i);	
					double axis     = getDistance(sample.x, sample.y, previous_sample.x, previous_sample.y);
					total_distance += axis;
					
					Sample previous_set     = (Sample) relative_data.get(i - 7);
					Sample current_set      = (Sample) relative_data.get(i - 2);
					double current_distance =  getDistance(current_set.x, current_set.y, previous_set.x, previous_set.y);
					Sample adjusted_sample  = previous_set;
					adjusted_sample.y       = total_distance - axis;
					if(current_distance > axis)
					    adjusted_sample.intensity = ((current_distance - axis) / current_distance) * current_set.intensity + (axis / current_distance) * previous_set.intensity;    
					else if(current_distance < axis)
						adjusted_sample.intensity = ((axis - current_distance) / axis) * current_set.intensity + (current_distance / axis) * previous_set.intensity;   	
					data.add(adjusted_sample);
					
					previous_set     = (Sample) relative_data.get(i - 6);
					current_set      = (Sample) relative_data.get(i - 1);
					current_distance =  getDistance(current_set.x, current_set.y, previous_set.x, previous_set.y);
					adjusted_sample  = previous_set;
					adjusted_sample.y       = total_distance - axis;
					if(current_distance > axis)
					    adjusted_sample.intensity = ((current_distance - axis) / current_distance) * current_set.intensity + (axis / current_distance) * previous_set.intensity;    
					else if(current_distance < axis)
						adjusted_sample.intensity = ((axis - current_distance) / axis) * current_set.intensity + (current_distance / axis) * previous_set.intensity;   	
					data.add(adjusted_sample);
					
					previous_set      = (Sample) relative_data.get(i - 5);
					adjusted_sample   = previous_set;
					adjusted_sample.y = total_distance - axis;
					data.add(adjusted_sample);
					
					previous_set     = (Sample) relative_data.get(i - 4);
					current_set      = (Sample) relative_data.get(i + 1);
					current_distance =  getDistance(current_set.x, current_set.y, previous_set.x, previous_set.y);
					adjusted_sample  = previous_set;
					adjusted_sample.y       = total_distance - axis;
					if(current_distance > axis)
					    adjusted_sample.intensity = ((current_distance - axis) / current_distance) * current_set.intensity + (axis / current_distance) * previous_set.intensity;    
					else if(current_distance < axis)
						adjusted_sample.intensity = ((axis - current_distance) / axis) * current_set.intensity + (current_distance / axis) * previous_set.intensity;   	
					data.add(adjusted_sample);
					
					previous_set     = (Sample) relative_data.get(i - 4);
					current_set      = (Sample) relative_data.get(i + 1);
					current_distance =  getDistance(current_set.x, current_set.y, previous_set.x, previous_set.y);
					adjusted_sample  = previous_set;
					adjusted_sample.y       = total_distance - axis;
					if(current_distance > axis)
					    adjusted_sample.intensity = ((current_distance - axis) / current_distance) * current_set.intensity + (axis / current_distance) * previous_set.intensity;    
					else if(current_distance < axis)
						adjusted_sample.intensity = ((axis - current_distance) / axis) * current_set.intensity + (current_distance / axis) * previous_set.intensity;   	
					data.add(adjusted_sample);
					previous_index = i - 2;
					previous_sample = sample;
				}
				
				// Add last set of adjusted data.
				for(int i = previous_index; i < previous_index + 5; i++)
				{
					// Not bothering to adjust intensity for 4 samples out of 400000+
				    Sample sample = (Sample)relative_data.get(i);
				    sample.y      = total_distance;
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

		frame = new JFrame("YFence Plotter");
		WindowAdapter window_handler = new WindowAdapter()
	    {
	        public void windowClosing(WindowEvent event)
	        {
	        	try
	            {
	            	PrintWriter output  = new PrintWriter("yfp.cfg");	
	            	String      string  = new String("foo");
	            	output.write(string);
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
		Cursor cursor = new Cursor(Cursor.HAND_CURSOR);
		frame.setCursor(cursor);

		data_canvas = new PlotCanvas();
		data_canvas.setSize(1000, 800);
		
		int thumb_size        = 3;
		int scrollbar_position = (int) (data_offset * scrollbar_resolution + data_range * scrollbar_resolution / 2);
		data_scrollbar        = new JScrollBar(JScrollBar.HORIZONTAL, scrollbar_position, thumb_size, 0, scrollbar_resolution + thumb_size);
		DataScrollbarHandler data_scrollbar_handler = new DataScrollbarHandler();
		data_scrollbar.addAdjustmentListener(data_scrollbar_handler);	
		
		data_slider = new RangeSlider();
		data_slider.setMinimum(0);
		data_slider.setMaximum(slider_resolution);
		DataSliderHandler data_slider_handler = new DataSliderHandler();
		data_slider.addChangeListener(data_slider_handler);

		JPanel data_panel = new JPanel(new BorderLayout());
		JPanel segment_panel = new JPanel(new BorderLayout());
		segment_panel.add(data_scrollbar, BorderLayout.NORTH);
		segment_panel.add(data_slider, BorderLayout.SOUTH);
		data_panel.add(data_canvas, BorderLayout.CENTER);
		data_panel.add(segment_panel, BorderLayout.SOUTH);
		
        data_scrollbar_changing = true;

        double position;
        int    value;
        
        position = slider_resolution * data_offset;
        value = (int) position;
		data_slider.setUpperValue((int)position);
		
		position = slider_resolution * data_offset + slider_resolution * data_range;
		value = (int)position;
		
		data_slider.setValue((int)position);

		data_scrollbar_changing = false;
		
		frame.getContentPane().add(data_panel, BorderLayout.CENTER);
		frame.pack();
		frame.setLocation(400, 200);
	}
	
	class PlotCanvas extends Canvas
	{
		ArrayList data_array;
		int       left_margin        = 70;
		int       right_margin       = 20;
		int       top_margin         = 10;
		int       bottom_margin      = 70;
		int       number_of_segments = 5;
		
		PlotCanvas()
		{
			// We can just do this once and save the garbage collector some work.
			data_array = new ArrayList();
			for(int i = 0; i < number_of_segments; i++)
			{
				ArrayList data_list = new ArrayList();
				data_array.add(data_list);
			}
		}
		
		public void paint(Graphics g)
		{
			Rectangle  visible_area = g.getClipBounds();
			int        xdim         = (int) visible_area.getWidth();
			int        ydim         = (int) visible_area.getHeight();
			
			// Reallocate the memory every time because the canvas might get resized.
			pixel_data = new ArrayList[ydim][xdim];
			for (int i = 0; i < ydim; i++)
				for (int j = 0; j < xdim; j++)
					pixel_data[i][j] = new ArrayList();
			
			// Remember to clear any previous segments.
			for(int i = 0; i < number_of_segments; i++)
			{
				ArrayList data_list = (ArrayList)data_array.get(i);
				data_list.clear();
			}
			
			BufferedImage buffered_image  = new BufferedImage(xdim, ydim, BufferedImage.TYPE_INT_RGB);
			Graphics2D    graphics_buffer = (Graphics2D) buffered_image.getGraphics();
			graphics_buffer.setColor(java.awt.Color.WHITE);
			graphics_buffer.fillRect(0, 0, xdim, ydim);
			
			
			FontMetrics font_metrics = graphics_buffer.getFontMetrics();
			// This doesn't change so we can just set it once.
			// We also use string_width which varies.
			int string_height        = font_metrics.getAscent();
			
			// Get the indices for the segmented data.
			double data_location  = data_offset * data_length;
			int    start_location = (int)data_location;
			int    start_index    = (int)index.get(start_location);
			data_location         += data_range * data_length;
			int    stop_location  = (int)data_location;
			int    stop_index     = (int)index.get(stop_location);
			double seg_min        = Double.MAX_VALUE;
			double seg_max        = -Double.MAX_VALUE;
			double seg_xmin       = Double.MAX_VALUE;
			double seg_xmax       = 0;
			
			for(int i = start_index; i < stop_index; i++)
			{
				Sample sample = (Sample) data.get(i);
				int j = i % 5;
			    ArrayList data_list = (ArrayList)data_array.get(j);
			    data_list.add(sample);
				if (seg_min > sample.intensity)
					seg_min = sample.intensity;
				if (seg_max < sample.intensity)
					seg_max = sample.intensity;	
				if(seg_xmin > sample.y)
					seg_xmin = sample.y;
				if(seg_xmax < sample.y)
					seg_xmax = sample.y;
			}
			
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
			
			double minimum_y = seg_min;
			double maximum_y = seg_max;
			double minimum_x = seg_xmin;
			double maximum_x = seg_xmax;
			
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
			
				graphics_buffer.setColor(java.awt.Color.BLACK);
			    graphics_buffer.setStroke(new BasicStroke(1));
			    current_position = a1;
			    int string_width = font_metrics.stringWidth("77.7");
			    int    number_of_units            = (int) (graph_xdim / (string_width + 6));  
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
				    double current_increment = current_range / number_of_units;
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
		        	
		        	if(ystep != 0)
		        	{
		        		graphics_buffer.drawLine((int) current_position, b1 + y_remainder, (int) current_position - xstep, b1 + ystep);
		        	    for(int j = 0; j < number_of_units; j++)
		        	    {
		        	    	current_position += current_position_increment;
		        		    graphics_buffer.drawLine((int) current_position, b1 + y_remainder, (int) current_position - xstep, b1 + ystep);
		        		    // At the end of a graph, put down a line where we can hang a line id or location information.
		        		    // It also helps define the isometric space.
		        		    if(j == number_of_units - 1 && xstep != 0)
		        		    	graphics_buffer.drawLine((int) current_position, b1, (int) current_position, b1 + 10);  	
		        	    }
		            }
		        	
		        	if(xstep == 0 && ystep == max_ystep)
		        	{
		        		//Put down lines where we can hang intensity information since graphs are laid out
		        		//in a column.
		        		current_position         = b2;
		                current_range            = b1 - b2;
					    number_of_units          = (int) (current_range / (2 * (string_height)));
					    double current_increment = current_range / number_of_units;
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
				    double current_increment = current_range / number_of_units;
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
				ArrayList plot_list = new ArrayList();
				for(int j = 0; j < data_list.size(); j++)
				{
					// We probably want to save pixel data here,
					// but since we can't test whether its working
					// until we have graphs, we'll just start
					// constructing the graph.  Recall that
					// the data we are working with has both
					// an adjusted y and intensity value, so we
					// would get something from relative_data.
					Sample sample = (Sample)data_list.get(j);
					Point2D.Double point = new Point2D.Double();
					point.x              = sample.y;
					point.y              = sample.intensity;
					plot_list.add(point);
				}
				plot_data.add(plot_list);
			}
			
			Polygon[] polygon              = new Polygon[number_of_segments];
			boolean[] polygon_zero_crossing = new boolean[number_of_segments];
			double[]  polygon_min           = new double[number_of_segments];
			for (int i = 0; i < number_of_segments; i++)
			{
				ArrayList data_list       = (ArrayList)data_array.get(i);
				Sample    previous_sample = (Sample)data_list.get(0);
				polygon_zero_crossing[i]  = false;
				for(int j = 1; j < data_list.size(); j++)
				{
				    Sample current_sample = (Sample)data_list.get(j);
				    if((previous_sample.intensity <= 0 && current_sample.intensity >= 0) ||
				    (previous_sample.intensity >= 0 && current_sample.intensity <= 0))
				    {
				    	polygon_zero_crossing[i]  = true;	
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

				ArrayList segment = (ArrayList)plot_data.get(i);

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
				y[m] = (int)local_min;
				m++;

				x[m] = a1;
				y[m] = (int)local_min;
				m++;
				
				x[m] = a1;
				y[m] = (int)init_point;
				
				
				// Color y axis.
				graphics_buffer.setColor(fill_color[i]);
			    graphics_buffer.setStroke(new BasicStroke(3)); 
			    graphics_buffer.drawLine(a1, (int)local_min, a1, (int)local_max);
			    
			    polygon_min[i] = local_min;
			    
			   
			    
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
       
				graphics_buffer.setColor(fill_color[i]);
			    graphics_buffer.fillPolygon(polygon[i]);
			    graphics_buffer.setStroke(new BasicStroke(2));
			    graphics_buffer.setColor(java.awt.Color.BLACK);
			    graphics_buffer.drawPolygon(polygon[i]);
			    
			    // Anchor polygon to isometric grid.
			    graphics_buffer.setColor(Color.DARK_GRAY);
			    graphics_buffer.setStroke(new BasicStroke(1)); 
			    graphics_buffer.drawLine(a2, (int)polygon_min[i], a2, b1);
				
				//if (minimum_y < 0  && maximum_y > 0  && polygon_zero_crossing[i] == true)
			    if(polygon_zero_crossing[i]  || (xstep == max_xstep  || ystep == max_ystep))
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
			        if(i != number_of_segments - 1)
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
			 
			    current_value    = 0;
			    current_position = a1;
			    int string_width = font_metrics.stringWidth("77.7");
			    xrange           = maximum_x - minimum_x;
		        int    number_of_units            = (int) (graph_xdim / (string_width + 6));
		        double current_position_increment = graph_xdim;
		        current_position_increment        /= number_of_units;
		        
		        String position_string;
		        if(xrange < 1.)
		            position_string = String.format("%,.1f", current_value);
		        else
		        	position_string = String.format("%,.1f", current_value);
		        
		        if(i == 0 || (xstep == max_xstep && ystep == 0) )
		        {
		        	// Hanging locations on frontmost graph or all the graphs if they are laid out in a row.
		        	graphics_buffer.drawString(position_string, (int) current_position - string_width / 2, ydim + string_height + 12 - bottom_margin);
		            double current_value_increment = xrange / number_of_units;	            
		            for(int j = 0; j < number_of_units; j++)
		            {
			            current_value += current_value_increment;
			            current_position += current_position_increment;
			            if(xrange < 1.)
			                position_string = String.format("%,.1f", current_value);
			            else
			            	position_string = String.format("%,.1f", current_value);
			            graphics_buffer.drawString(position_string, (int) current_position - string_width / 2, ydim + string_height + 12 - bottom_margin);
		            }
		        }
		        
		        if(i == 0 || (xstep == 0 && ystep == max_ystep))
		        {
		        	double current_intensity_range = maximum_y - minimum_y;
				    double current_range = b1 - b2;
				    number_of_units = (int) (current_range / (2 * string_height));
				    double current_increment = current_range / number_of_units;
				    double current_value_increment = current_intensity_range / number_of_units;
				    current_position = b2;
				    current_value = maximum_y;
				    String intensity_string;
				    for(int j = 0; j < number_of_units; j++)
		            {
				    	if(current_intensity_range > 20)
					        intensity_string = String.format("%,.0f", current_value);
					    else
					    	intensity_string = String.format("%,.1f", current_value);
			            string_width     = font_metrics.stringWidth(intensity_string);
			            graphics_buffer.drawString(intensity_string, a1 - (string_width + 14), (int) (current_position + string_height / 2));
			            current_position += current_increment;
			            current_value    -= current_value_increment;
		            }
				    if(current_intensity_range > 20)
				        intensity_string = String.format("%,.0f", current_value);
				    else
				    	intensity_string = String.format("%,.1f", current_value);
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
			
			g.drawImage(buffered_image, 0, 0, null);
		}
	}
	
	class DataSliderHandler implements ChangeListener
	{
		public void stateChanged(ChangeEvent e)
		{
			if (data_scrollbar_changing == false)
			{
				data_slider_changing = true;
				RangeSlider slider = (RangeSlider) e.getSource();
				if(slider.getValueIsAdjusting() == false)
				{
					int value = slider.getValue();
					int upper_value = slider.getUpperValue();
					if(value == upper_value)
					{
						if(value == slider.getMaximum())
						{
							value--;
							slider.setValue(value);
						}
						else
						{
							upper_value++;
							slider.setUpperValue(upper_value);
						}
					}
					double start = (double) value;
					double stop = (double) upper_value;
					double range = (stop - start) / slider_resolution;
					start /= slider_resolution;
					stop /= slider_resolution;	
					data_offset = start;
					data_range  = range;
					double scrollbar_position = start + range / 2;
					scrollbar_position *= scrollbar_resolution;
					data_scrollbar.setValue((int)scrollbar_position);
					data_slider_changing = false;
					data_canvas.repaint();
				}
			}
		}
	}
	
	class DataScrollbarHandler implements AdjustmentListener
	{
		public void adjustmentValueChanged(AdjustmentEvent event)
		{
			if (data_scrollbar.getValueIsAdjusting() == false)
			{
				if (data_slider_changing == false)
				{
					data_scrollbar_changing = true;
					
					JScrollBar scrollbar    = (JScrollBar) event.getSource();
					double normal_position  = (double) event.getValue();
					normal_position         /= scrollbar_resolution;
					double normal_start     = data_offset; 
					double normal_stop      = normal_start + data_range; 
					normal_start = normal_position - data_range / 2;
					normal_stop = normal_start + data_range;
					if(normal_start < 0)
					{
						normal_start = 0;
						normal_stop  = data_range;
					}
					else if(normal_stop > 1)
					{
						normal_stop = 1;
						normal_start = 1 - data_range;
					}
		
					// If you set the lower value to a value more
					// than the upper value, or the upper value
					// to a value less than the lower value,
					// we get an incorrect result.
					// This manifests as a rangeslider 
					// with one thumb and zero extent, or
					// a range larger than intended.
					// Maybe one of the reasons it's not in the regular API.
					// If we figure out which way we're moving the slider
					// range we can avoid that problem.
					boolean moving_down = true;
					if(normal_start >  data_offset)
						moving_down = false;
					data_offset   = normal_start;
					normal_start *= slider_resolution;
					normal_stop  *= slider_resolution;
					int start     = (int)normal_start;
					int stop      = (int)normal_stop;
					int error = 0;
					if(moving_down)
					{
					    data_slider.setValue(start);
					    data_slider.setUpperValue(stop);
					}
					else
					{
					    data_slider.setUpperValue(stop);
					    data_slider.setValue(start);
					}
					data_scrollbar_changing = false;
					data_canvas.repaint();
				}
			}
		}
	}
	
	public double getDistance(double x, double y, double x_origin, double y_origin)
	{
	    double distance  = Math.sqrt((x - x_origin) * (x - x_origin) + (y - y_origin) * (y - y_origin));
	    return(distance);
	}
}