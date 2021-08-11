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
	public ArrayList   data                 = new ArrayList();
	public ArrayList   adjusted_data        = new ArrayList();
	public ArrayList   index                = new ArrayList();
	public double      data_offset          = .25;
	public double      data_range           = .5;
	
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
					data.add(sample);
				}
				
				Sample previous_sample = (Sample)data.get(2);
				double total_distance  = 0;
				for(int i = 7; i < data.size(); i += 5)
				{
					Sample sample   = (Sample) data.get(i);
					//double distance = Math.abs(sample.y - previous_sample.y);
					double distance = getDistance(sample.x, sample.y, previous_sample.x, previous_sample.y);
					total_distance += distance;
					previous_sample = sample;
				}
				
				System.out.println("The total distance traveled by the drone was " + String.format("%.2f", total_distance) + " meters.");
				data_length = (int)total_distance;
				System.out.println("Data length is " + data_length);
				slider_resolution = data_length;
				scrollbar_resolution = data_length;
				
				int _index = 0;
				index.add(_index);
				total_distance  = 0;
				for(int i = 1; i < data_length; i++)
				{
					previous_sample = (Sample)data.get(_index + 2);
					
					for(int j = _index + 7; j < data.size(); j += 5)
					{
						Sample sample   = (Sample) data.get(j);	
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
				
				adjusted_data   = new ArrayList();
				previous_sample = (Sample)data.get(2);
				for(int i = 7; i < data.size(); i += 5)
				{
					Sample sample   = (Sample) data.get(i);	
					double axis     = getDistance(sample.x, sample.y, previous_sample.x, previous_sample.y);
					total_distance += axis;
					
					Sample previous_set     = (Sample) data.get(i - 7);
					Sample current_set      = (Sample) data.get(i - 2);
					double current_distance =  getDistance(current_set.x, current_set.y, previous_set.x, previous_set.y);
					Sample adjusted_sample  = previous_set;
					adjusted_sample.y       = total_distance - axis;
					if(current_distance > axis)
					    adjusted_sample.intensity = ((current_distance - axis) / current_distance) * current_set.intensity + (axis / current_distance) * previous_set.intensity;    
					else if(current_distance < axis)
						adjusted_sample.intensity = ((axis - current_distance) / axis) * current_set.intensity + (current_distance / axis) * previous_set.intensity;   	
					adjusted_data.add(adjusted_sample);
					
					previous_set     = (Sample) data.get(i - 6);
					current_set      = (Sample) data.get(i - 1);
					current_distance =  getDistance(current_set.x, current_set.y, previous_set.x, previous_set.y);
					adjusted_sample  = previous_set;
					adjusted_sample.y       = total_distance - axis;
					if(current_distance > axis)
					    adjusted_sample.intensity = ((current_distance - axis) / current_distance) * current_set.intensity + (axis / current_distance) * previous_set.intensity;    
					else if(current_distance < axis)
						adjusted_sample.intensity = ((axis - current_distance) / axis) * current_set.intensity + (current_distance / axis) * previous_set.intensity;   	
					adjusted_data.add(adjusted_sample);
					
					previous_set      = (Sample) data.get(i - 5);
					adjusted_sample   = previous_set;
					adjusted_sample.y = total_distance - axis;
					adjusted_data.add(adjusted_sample);
					
					previous_set     = (Sample) data.get(i - 4);
					current_set      = (Sample) data.get(i + 1);
					current_distance =  getDistance(current_set.x, current_set.y, previous_set.x, previous_set.y);
					adjusted_sample  = previous_set;
					adjusted_sample.y       = total_distance - axis;
					if(current_distance > axis)
					    adjusted_sample.intensity = ((current_distance - axis) / current_distance) * current_set.intensity + (axis / current_distance) * previous_set.intensity;    
					else if(current_distance < axis)
						adjusted_sample.intensity = ((axis - current_distance) / axis) * current_set.intensity + (current_distance / axis) * previous_set.intensity;   	
					adjusted_data.add(adjusted_sample);
					
					previous_set     = (Sample) data.get(i - 4);
					current_set      = (Sample) data.get(i + 1);
					current_distance =  getDistance(current_set.x, current_set.y, previous_set.x, previous_set.y);
					adjusted_sample  = previous_set;
					adjusted_sample.y       = total_distance - axis;
					if(current_distance > axis)
					    adjusted_sample.intensity = ((current_distance - axis) / current_distance) * current_set.intensity + (axis / current_distance) * previous_set.intensity;    
					else if(current_distance < axis)
						adjusted_sample.intensity = ((axis - current_distance) / axis) * current_set.intensity + (current_distance / axis) * previous_set.intensity;   	
					adjusted_data.add(adjusted_sample);
				}
				
				// Add last set of adjusted data.
				int size = data.size();
				size = adjusted_data.size();
				System.out.println("Adjusted data size is " + size);
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

		data_canvas = new Canvas();
		data_canvas.setSize(600, 400);
		
		int thumb_size        = 3;
		data_scrollbar        = new JScrollBar(JScrollBar.HORIZONTAL, scrollbar_resolution / 2, thumb_size, 0, scrollbar_resolution + thumb_size);
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
        
        position = slider_resolution * .75;
        value = (int) position;
		//System.out.println("Setting upper range slider value to " + value);
		data_slider.setUpperValue((int)position);
		
		position = slider_resolution * .25;
		value = (int)position;
		//System.out.println("Setting lower range slider value to " + value);
		data_slider.setValue((int)position);

		data_scrollbar_changing = false;
		
		frame.getContentPane().add(data_panel, BorderLayout.CENTER);
		frame.pack();
		frame.setLocation(400, 200);
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
					double start = (double) value;
					start /= slider_resolution;
					double stop = (double) upper_value;
					stop /= slider_resolution;	
					double range = stop - start;
					data_offset = start;
					data_range  = range;
					double scrollbar_position = start + range / 2;
					scrollbar_position *= scrollbar_resolution;
					data_scrollbar.setValue((int)scrollbar_position);
					data_slider_changing = false;
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
					//System.out.println("Normal position is " + String.format("%.3f", normal_position));
					//System.out.println("Normal range is " + String.format("%.3f", data_range));
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
					
					boolean moving_down = true;
					if(normal_start >  data_offset)
						moving_down = false;
					
					data_offset   = normal_start;
					normal_start *= slider_resolution;
					normal_stop  *= slider_resolution;
					int start     = (int)normal_start;
					int stop      = (int)normal_stop;
					
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