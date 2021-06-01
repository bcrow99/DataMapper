import java.awt.*;
import java.awt.Color.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.*;

public class XFencePlotter
{
	// Input
	ArrayList data = new ArrayList();

	// Shared interface componants.
	public JFrame      frame;
	public LineCanvas  canvas;
	public JScrollBar  scrollbar;
	public RangeSlider range_slider;
	public JTextField  input;
	public JTable      option_table;
	public JDialog     load_dialog;
	
	// Shared program variables.
	boolean slider_changed    = false;
	boolean scrollbar_changed = false;
	double  start             = 35;
	double  range             = 20;
	int     line              = 9;
	int     sensor            = 0;
	
	boolean autoscale    = true;
	double  scale_factor = 1.;
	int     smoothing    = 0;
	int     resolution   = 300;
	double  normal_xstep = 0;
	double  normal_ystep = 0;
	
	public static void main(String[] args)
	{
		//String prefix = new String("C:/Users/Brian Crowley/Desktop/");
		String prefix = new String("");
		if (args.length != 1)
		{
			System.out.println("Usage: XFencePlotter <data file>");
			System.exit(0);
		} 
		else
		{
			try
			{
				String filename = prefix + args[0];
				try
				{
					XFencePlotter window = new XFencePlotter(filename);
					window.frame.setVisible(true);
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public XFencePlotter(String filename)
	{
		Color[] outline_color = new Color[10];
		Color[] fill_color    = new Color[10];
		
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
	    
		fill_color[0] = new Color(196, 196, 196);
		fill_color[1] = new Color(196, 196, 224);
		fill_color[2] = new Color(196, 224, 196);
		fill_color[3] = new Color(224, 196, 196);
		fill_color[4] = new Color(196, 224, 255);
		fill_color[5] = new Color(224, 196, 224);
		fill_color[6] = new Color(224, 224, 196);
		fill_color[7] = new Color(224, 224, 224);
		fill_color[8] = new Color(224, 224, 255);
		fill_color[9] = new Color(224, 255, 224);
		
		//System.out.println(System.getProperty("java.version"));
		File file = new File(filename);
		if (file.exists())
		{
			ArrayList original_data = new ArrayList();
			double xmin = Double.MAX_VALUE;
			double xmax = Double.MIN_VALUE;
			double ymin = Double.MAX_VALUE;
			double ymax = Double.MIN_VALUE;
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
					if (x < xmin)
						xmin = x;
					else if (x > xmax)
						xmax = x;
					if (y < ymin)
						ymin = y;
					else if (y > ymax)
						ymax = y;
					double intensity = Double.valueOf(number_tokens.nextToken());
					Sample current_sample = new Sample(x, y, intensity);
					original_data.add(current_sample);
				}

				while (line != null)
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
								if (x < xmin)
									xmin = x;
								else if (x > xmax)
									xmax = x;
								if (y < ymin)
									ymin = y;
								else if (y > ymax)
									ymax = y;
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
					Sample sample = (Sample) original_data.get(i);
					sample.x -= xmin;
					sample.y -= ymin;
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

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		canvas = new LineCanvas();
		canvas.setSize(800, 600);
		JPanel canvas_panel = new JPanel(new BorderLayout());
		canvas_panel.add(canvas, BorderLayout.CENTER);
		
		scrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 3, -100, 103);
		ScrollbarHandler scrollbar_handler = new ScrollbarHandler();
		scrollbar.addAdjustmentListener(scrollbar_handler);
		
		range_slider = new RangeSlider();
		range_slider.setMinimum(15);
		range_slider.setMaximum(75);
		range_slider.setValue(35);
		range_slider.setUpperValue(55);
		SliderHandler slider_handler = new SliderHandler();
		range_slider.addChangeListener(slider_handler);
		
		JPanel segment_panel = new JPanel(new BorderLayout());
		segment_panel.add(scrollbar, BorderLayout.NORTH);
		segment_panel.add(range_slider, BorderLayout.SOUTH);
		canvas_panel.add(segment_panel, BorderLayout.SOUTH);
		
		frame.getContentPane().add(canvas_panel, BorderLayout.CENTER);

		option_table = new JTable(4, 11)
		{
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
			{
				Component c = super.prepareRenderer(renderer, row, column);
				if (row == 3 && column > 0)
					c.setBackground(fill_color[column - 1]);
				else
					c.setBackground(java.awt.Color.WHITE);
				if(column == 0)
					c.setFont(c.getFont().deriveFont(Font.BOLD));
				return c;
			}
		};
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		for (int i = 0; i < 11; i++)
		{
			option_table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
		}
		
		String header;
		header = new String("Line:Sensor");
		option_table.setValueAt(header, 0, 0);
		header = new String("Visible");
		option_table.setValueAt(header, 1, 0);
		header = new String("Transparent");
		option_table.setValueAt(header, 2, 0);
		header = new String("Key");
		option_table.setValueAt(header, 3, 0);
		
		for (int i = 0; i < 5; i++)
		{
			if(line % 2 == 1)
			{
				String line_sensor = new String(line + ":" + i);
			    option_table.setValueAt(line_sensor, 0, i + 1);
			}
			else
			{
				String line_sensor = new String(line + ":" + (4 - i)); 
				option_table.setValueAt(line_sensor, 0, i + 1);
			}
		}
		
		for (int i = 0; i < 5; i++)
		{
			if((line + 1) % 2 == 1)
			{
				String line_sensor = new String((line + 1) + ":" + i);
			    option_table.setValueAt(line_sensor, 0, i + 6);
			}
			else
			{
				String line_sensor = new String((line + 1) + ":" + (4 - i)); 
				option_table.setValueAt(line_sensor, 0, i + 6);
			}
		}
		
		for(int i = 1; i < 11; i++)
		{
			option_table.setValueAt("Yes", 1, i); 
			option_table.setValueAt("No", 2, i);
		}
		
		frame.getContentPane().add(option_table, BorderLayout.SOUTH);
		
		JMenuBar menu_bar    = new JMenuBar();
		
		JMenu     file_menu  =  new JMenu("File");
		JMenuItem load_item  = new JMenuItem("Load");
		JMenuItem apply_item = new JMenuItem("Apply");
		JMenuItem save_item  = new JMenuItem("Save");
		
		LoadItemHandler load_item_handler = new LoadItemHandler();
		load_item.addActionListener(load_item_handler);
		
		file_menu.add(load_item);
		file_menu.add(apply_item);
		file_menu.add(save_item);
		
		JMenu     format_menu     = new JMenu("Format");
		JMenuItem view_item       = new JMenuItem("View");
		JMenuItem place_item      = new JMenuItem("Placement");
		
		format_menu.add(view_item);
		format_menu.add(place_item);
		
		JMenu     settings_menu   = new JMenu("Settings");
		JMenuItem location_item   = new JMenuItem("Location");
		JMenuItem resolution_item = new JMenuItem("Resolution");
		JMenuItem scaling_item    = new JMenuItem("Scaling");
		
		settings_menu.add(location_item);
		settings_menu.add(resolution_item);
		settings_menu.add(scaling_item);
		
		menu_bar.add(file_menu);
		menu_bar.add(format_menu);
		menu_bar.add(settings_menu);
		
		input    = new JTextField();
		input.setHorizontalAlignment(JTextField.CENTER);
		input.setText(line + ":" + sensor);
		
		JButton load_button = new JButton("Load");
		LoadHandler load_handler = new LoadHandler();
		load_button.addActionListener(load_handler);
		
		JPanel load_panel = new JPanel(new GridLayout(2, 1));
		load_panel.add(input);
		load_panel.add(load_button);
		
		// A modeless dialog box that only shows up if File->Load is selected.
		load_dialog = new JDialog(frame);
		load_dialog.add(load_panel);
	
		frame.setJMenuBar(menu_bar);
		frame.pack();
	}

	class LineCanvas extends Canvas
	{
		Color[] outline_color;
		Color[] fill_color;
		int top_margin    = 10;
		int right_margin  = 10;
		int left_margin   = 60;
		int bottom_margin = 60;
		
		public LineCanvas()
		{
			outline_color = new Color[10];
			fill_color    = new Color[10];
			
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
		    
			fill_color[0] = new Color(196, 196, 196);
			fill_color[1] = new Color(196, 196, 224);
			fill_color[2] = new Color(196, 224, 196);
			fill_color[3] = new Color(224, 196, 196);
			fill_color[4] = new Color(196, 224, 255);
			fill_color[5] = new Color(224, 196, 224);
			fill_color[6] = new Color(224, 224, 196);
			fill_color[7] = new Color(224, 224, 224);
			fill_color[8] = new Color(224, 224, 255);
			fill_color[9] = new Color(224, 255, 224);   	
		}
		
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

	class ScrollbarHandler implements AdjustmentListener
	{
		public void adjustmentValueChanged(AdjustmentEvent event)
		{
			if (slider_changed == false)
			{
				scrollbar_changed = true;
				
				// Get the new starting point.
				double difference = 60 - range;
                int current_value = event.getValue();
				double shift = current_value;
				shift /= 200;
				shift *= difference;
				start = 45. - (range / 2);
				start += shift;
				
				// Reset the slider.
				int value = (int) start;
				int upper_value = (int) (start + range);
				range_slider.setValue(value);
				range_slider.setUpperValue(upper_value);
				
				scrollbar_changed = false;
			}
		}
	}
    
	class SliderHandler implements ChangeListener
	{
		public void stateChanged(ChangeEvent e)
		{
			if (scrollbar_changed == false)
			{
				slider_changed = true;
				RangeSlider slider = (RangeSlider) e.getSource();
				if (slider.getValueIsAdjusting() == false)
				{
					// Set the new offset and range.
					double current_start = (double) slider.getValue();
					double current_stop  = (double) slider.getUpperValue();
					range = current_stop - current_start;
					if (range == 0)
						range = 1;
					start = current_start;
					//System.out.println("Start is " + start);
					//System.out.println("Range is " + range);
					
					// Reset the scrollbar.
					double normalized_start = start - 15;
					normalized_start /= 60.;
					double normalized_stop = (start + range) - 15;
					normalized_stop /= 60.;
					double normalized_center = (normalized_start + normalized_stop) / 2.;
					normalized_center -= .5;
					normalized_center *= 200;

					int value = (int) normalized_center;
					scrollbar.setValue(value);
					
					slider_changed = false;
				}
			}
		}
	}
	
	class LoadItemHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			load_dialog.setLocation(300, 300);
			load_dialog.pack();
			load_dialog.setVisible(true);
		}
	}
	 
	class LoadHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			String input_string = input.getText();
			StringTokenizer input_tokenzer = new StringTokenizer(input_string);
			String line_string = input_tokenzer.nextToken(":");
			line = Integer.parseInt(line_string);
			String sensor_string = input_tokenzer.nextToken(":");
			sensor = Integer.parseInt(sensor_string);
			//System.out.println("Current line is " + line);
			//System.out.println("Current sensor is " + sensor);
			
			String[] line_sensor_pair = new String[10];
			
			int current_pair   = 0;
			int current_line   = line;
			int current_sensor = sensor;
			
			if(current_line % 2 == 1)
			{
			    for(int i = current_sensor; i < 5; i++)
			    {
			        String current_string = new String(current_line + ":" + i);	
			        line_sensor_pair[current_pair] = current_string;
			        current_pair++;
			    }
			}
			else
			{
				for(int i = current_sensor; i >= 0; i--)
			    {
			        String current_string = new String(current_line + ":" + i);	
			        line_sensor_pair[current_pair] = current_string;
			        current_pair++;
			    }	
			}
			current_line++;
			if(current_line % 2 == 1)
			{
			    for(int i = 0; i < 5; i++)
			    {
			        String current_string = new String(current_line + ":" + i);	
			        line_sensor_pair[current_pair] = current_string;
			        current_pair++;
			    }
			}
			else
			{
				for(int i = 4; i >= 0; i--)
			    {
			        String current_string = new String(current_line + ":" + i);	
			        line_sensor_pair[current_pair] = current_string;
			        current_pair++;
			    }	
			}
		    current_line++;
		    outer: if(current_pair < 10)
		    {
		    	if(current_line % 2 == 1)
				{
				    for(int i = 0; i < 5; i++)
				    {
				        String current_string = new String(current_line + ":" + i);	
				        line_sensor_pair[current_pair] = current_string;
				        current_pair++;
				        if(current_pair == 10)
				        	break outer;
				    }
				}
				else
				{
					for(int i = 4; i >= 0; i--)
				    {
				        String current_string = new String(current_line + ":" + i);	
				        line_sensor_pair[current_pair] = current_string;
				        current_pair++;
				        if(current_pair == 10)
				        	break outer;
				    }	
				}	
		    }

			for(int i = 0; i < 10; i++)
			{
				option_table.setValueAt(line_sensor_pair[i], 0, i + 1);	
			}
		}
	}
	
	public double[] getMinMax(int line, double start, double range, ArrayList data)
	{
	    double [] min_max = new double[2];
	    int[][] line_array = ObjectMapper.getUnclippedLineArray();
		int start_index = line_array[line][0];
		int stop_index  = line_array[line][1];
		
		double min_intensity = Double.MAX_VALUE;
		double max_intensity = -Double.MAX_VALUE;
		
		for(int i = start_index; i < stop_index; i++)
		{
			Sample sample = (Sample)data.get(i);
			if(sample.y >= start && sample.y < (start + range))
			{
				if(sample.intensity < min_intensity)
					min_intensity = sample.intensity;
				if(sample.intensity > max_intensity)
					max_intensity = sample.intensity;
			}
		}
	    
		min_max[0] = min_intensity;
		min_max[1] = max_intensity;
	    return(min_max);
	}
	
	public double[] reduce(double[] source, int iterations)
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
}
