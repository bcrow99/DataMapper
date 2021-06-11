import java.awt.*;
import java.awt.Color.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
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

	// Sensor data segmented at start of program and by File->Apply and accessed by
	// LineCanvas.paint()
	ArrayList sensor_data = new ArrayList();

	// Shared interface componants.
	public JFrame      frame;
	public LineCanvas  canvas;
	public JScrollBar  scrollbar;
	public RangeSlider range_slider;
	public JTextField  input;
	public JTable      option_table;
	public JTextField  lower_bound;
	public JTextField  upper_bound;
	
	// Pop-ups associated with the menu bar.  File->Apply does not have a pop-up.
	
	// File
	public JDialog load_dialog;
	public JDialog save_dialog;
	
	//Format
	public JDialog placement_dialog;
	public JDialog view_dialog;
	
	// Settings
	public JDialog scale_dialog;
	public JDialog dynamic_range_dialog;
	public JDialog smooth_dialog;
	public JDialog location_dialog;
	
	// Shared program variables.
	double offset = 45.82;
	double range  = 14.;
	
	// Updated by Slider, Scrollbar, and Button
	JTextField offset_information;
	JTextField range_information;
	
	// Fired by the scrollbar and range slider and adjust button.
	public JMenuItem apply_item;
	
	// Shared by the scrollbar and range slider and adjust button.
	boolean slider_changing     = false;
	boolean scrollbar_changing  = false;
	boolean button_changing     = false;
	
	// Shared by the dynamic range adjust and reset handlers.
	// Referenced by canvas.paint().
	boolean data_clipped        = false;
	
	// X and Y Step Handlers call repaint()
	PlacementCanvas placement_canvas;
	
    // Referenced by pop-ups and canvas paint().
	boolean autoscale    = false;
	double  scale_factor = 1.;
	int     smoothing    = 0;
	double  normal_xstep = 0.;
	double  normal_ystep = 0.;
	String  view         = new String("East");

	public static void main(String[] args)
	{
		String prefix = new String("C:/Users/Brian Crowley/Desktop/");
		//String prefix = new String("");
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
		Color[] fill_color = new Color[10];

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

		// System.out.println(System.getProperty("java.version"));
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
					} catch (IOException e)
					{
						System.out.println("Unexpected error " + e.toString());
					}
				}
				reader.close();
				double range_of_data = ymax - ymin;
				//System.out.println("Range of data is " + range_of_data);
				for (int i = 0; i < original_data.size(); i++)
				{
					Sample sample = (Sample) original_data.get(i);
					sample.x -= xmin;
					sample.y -= ymin;
					data.add(sample);
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		} 
		else
		{
			System.out.println("File not found.");
			System.exit(0);
		}

		frame = new JFrame("Fence Plotter");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		canvas = new LineCanvas();
		canvas.setSize(800, 600);
		JPanel canvas_panel = new JPanel(new BorderLayout());
		canvas_panel.add(canvas, BorderLayout.CENTER);

		scrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 50, 3, 0, 103);
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

		option_table = new JTable(5, 11)
		{
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
			{
				Component c = super.prepareRenderer(renderer, row, column);
				if (row == 3 && column > 0)
					c.setBackground(fill_color[column - 1]);
				else
					c.setBackground(java.awt.Color.WHITE);
				/*
				 * if (column == 0) c.setFont(c.getFont().deriveFont(Font.BOLD));
				 */
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

		int init_line = 9;

		for (int i = 0; i < 5; i++)
		{
			if (init_line % 2 == 1)
			{
				String line_sensor = new String(init_line + ":" + i);
				option_table.setValueAt(line_sensor, 0, i + 1);
			} else
			{
				String line_sensor = new String(init_line + ":" + (4 - i));
				option_table.setValueAt(line_sensor, 0, i + 1);
			}
		}

		for (int i = 0; i < 5; i++)
		{
			if ((init_line + 1) % 2 == 1)
			{
				String line_sensor = new String((init_line + 1) + ":" + i);
				option_table.setValueAt(line_sensor, 0, i + 6);
			} else
			{
				String line_sensor = new String((init_line + 1) + ":" + (4 - i));
				option_table.setValueAt(line_sensor, 0, i + 6);
			}
		}

		for (int i = 1; i < 11; i++)
		{
			option_table.setValueAt("yes", 1, i);
			option_table.setValueAt("no", 2, i);
		}

		JPanel option_panel = new JPanel(new BorderLayout());
		option_panel.add(option_table, BorderLayout.CENTER);
		frame.getContentPane().add(option_panel, BorderLayout.NORTH);

		JMenuBar menu_bar = new JMenuBar();

		JMenu file_menu = new JMenu("File");
		JMenuItem load_item = new JMenuItem("Load");
		apply_item = new JMenuItem("Apply");
		JMenuItem save_item = new JMenuItem("Save");

		LoadHandler load_handler = new LoadHandler();
		load_item.addActionListener(load_handler);

		ApplyHandler apply_handler = new ApplyHandler();
		apply_item.addActionListener(apply_handler);

		SaveHandler save_handler = new SaveHandler();
		save_item.addActionListener(save_handler);

		file_menu.add(load_item);
		file_menu.add(apply_item);
		file_menu.add(save_item);

		JMenu format_menu = new JMenu("Format");
		
		JMenuItem place_item = new JMenuItem("Placement");
		PlacementHandler placement_handler = new PlacementHandler();
		place_item.addActionListener(placement_handler);

		JMenuItem view_item = new JMenuItem("View");
		ViewHandler view_handler = new ViewHandler();
		view_item.addActionListener(view_handler);

		format_menu.add(place_item);
		format_menu.add(view_item);

		JMenu settings_menu = new JMenu("Settings");

		JMenuItem scaling_item = new JMenuItem("Scaling");
		ScaleHandler scale_handler = new ScaleHandler();
		scaling_item.addActionListener(scale_handler);
		
		JMenuItem dynamic_range_item = new JMenuItem("Dynamic Range");
		DynamicRangeHandler dynamic_range_handler = new DynamicRangeHandler();
		dynamic_range_item.addActionListener(dynamic_range_handler);
		
		JMenuItem smoothing_item = new JMenuItem("Smoothing");
		SmoothHandler smooth_handler = new SmoothHandler();
		smoothing_item.addActionListener(smooth_handler);

		JMenuItem location_item = new JMenuItem("Location");
		LocationHandler location_handler = new LocationHandler();
		location_item.addActionListener(location_handler);
		
		settings_menu.add(scaling_item);
		settings_menu.add(dynamic_range_item);
		settings_menu.add(smoothing_item);
		settings_menu.add(location_item);

		menu_bar.add(file_menu);
		menu_bar.add(format_menu);
		menu_bar.add(settings_menu);
		
		// A modeless dialog box that shows up if File->Load is selected.
		JPanel load_panel = new JPanel(new GridLayout(2, 1));
		input = new JTextField();
		input.setHorizontalAlignment(JTextField.CENTER);
		input.setText("9:3");

		JButton load_button = new JButton("Load");
		InputHandler input_handler = new InputHandler();
		load_button.addActionListener(input_handler);

		load_panel.add(input);
		load_panel.add(load_button);

		load_dialog = new JDialog(frame);
		load_dialog.add(load_panel);

		// A modeless dialog box that shows up if Format->Placement is selected.
		JPanel placement_panel = new JPanel(new BorderLayout());

		placement_canvas = new PlacementCanvas();

		placement_canvas.setSize(100, 100);

		JScrollBar xstep_scrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, 0, 101);
		XStepHandler xstep_handler = new XStepHandler();
		xstep_scrollbar.addAdjustmentListener(xstep_handler);
		xstep_scrollbar.setValue(0);
		normal_xstep = 0;

		JScrollBar ystep_scrollbar = new JScrollBar(JScrollBar.VERTICAL, 0, 1, 0, 101);
		YStepHandler ystep_handler = new YStepHandler();
		ystep_scrollbar.addAdjustmentListener(ystep_handler);
		ystep_scrollbar.setValue(100);
		normal_ystep = 0;

		placement_panel.add(placement_canvas, BorderLayout.CENTER);
		placement_panel.add(xstep_scrollbar, BorderLayout.SOUTH);
		placement_panel.add(ystep_scrollbar, BorderLayout.EAST);

		placement_dialog = new JDialog(frame, "Placement");
		placement_dialog.add(placement_panel);
		
		// A modeless dialog box that shows up if Format->View is selected.
	    JPanel view_panel = new JPanel((new GridLayout(2, 1)));
	    JTextField user_information = new JTextField();
	    user_information.setText("East");
	    user_information.setHorizontalAlignment(JTextField.CENTER);
	    user_information.setEditable(false);
	    
	    JButton  view_button = new JButton("Switch View");
	    ActionListener button_handler = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
			    if(view.equals("East"))	
			    	view = new String("West");
			    else
			    	view = new String("East");
			    canvas.repaint();
			    placement_canvas.repaint();
			    user_information.setText(view);
			}
		};
		view_button.addActionListener(button_handler);
		view_panel.add(user_information);
		view_panel.add(view_button);
		view_dialog = new JDialog(frame);
		view_dialog.add(view_panel);

		// A modeless dialog box that shows up if Settings->Scaling is selected.
		JPanel scale_panel = new JPanel((new GridLayout(2, 1)));
		JToggleButton autoscale_button = new JToggleButton("Autoscale");
		ItemListener autoscale_handler = new ItemListener()
		{
			public void itemStateChanged(ItemEvent itemEvent)
			{
				int state = itemEvent.getStateChange();

				if (state == ItemEvent.SELECTED)
				{
					autoscale = true;
					canvas.repaint();
				} else
				{
					autoscale = false;
					canvas.repaint();
				}
			}
		};
		autoscale_button.addItemListener(autoscale_handler);

		JSlider factor_slider = new JSlider(0, 200, 0);
		ChangeListener factor_handler = new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				JSlider slider = (JSlider) e.getSource();
				if (slider.getValueIsAdjusting() == false)
				{
					int value = factor_slider.getValue();
					scale_factor = (double) value / 100 + 1.;
					canvas.repaint();
				}
			}
		};
		factor_slider.addChangeListener(factor_handler);

		scale_panel.add(autoscale_button);
		scale_panel.add(factor_slider);
		scale_dialog = new JDialog(frame);
		scale_dialog.add(scale_panel);
		
		// A modeless dialog box that shows up if Settings->Dynamic Range is selected.
		JPanel dynamic_range_panel = new JPanel(new BorderLayout());
		
		
		lower_bound = new JTextField();
		lower_bound.setHorizontalAlignment(JTextField.CENTER);
		upper_bound = new JTextField();
		upper_bound.setHorizontalAlignment(JTextField.CENTER);
		
		JPanel bounds_panel = new JPanel(new GridLayout(2,2));
		bounds_panel.add(lower_bound);
		bounds_panel.add(upper_bound);
		bounds_panel.add(new JLabel("Lower", JLabel.CENTER));
		bounds_panel.add(new JLabel("Upper", JLabel.CENTER));
		
		JPanel bounds_button_panel = new JPanel(new GridLayout(1,2));
		JButton adjust_bounds_button = new JButton("Adjust");
		JButton reset_bounds_button  = new JButton("Reset");
		bounds_button_panel.add(adjust_bounds_button);
		AdjustRangeHandler adjust_range_handler = new AdjustRangeHandler();
		adjust_bounds_button.addActionListener(adjust_range_handler);
		bounds_button_panel.add(reset_bounds_button);
		ResetRangeHandler reset_handler = new ResetRangeHandler();
		reset_bounds_button.addActionListener(reset_handler);
		
		dynamic_range_panel.add(bounds_panel, BorderLayout.CENTER);
		dynamic_range_panel.add(bounds_button_panel, BorderLayout.SOUTH);
		dynamic_range_dialog = new JDialog(frame);
		dynamic_range_dialog.add(dynamic_range_panel);
		
		// A modeless dialog box that shows up if Settings->Smoothing is selected.
		JPanel smooth_panel = new JPanel(new BorderLayout());

		JSlider smooth_slider = new JSlider(0, 100, 0);
		ChangeListener smooth_slider_handler = new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				JSlider slider = (JSlider) e.getSource();
				if (slider.getValueIsAdjusting() == false)
				{
					int value = slider.getValue();
					smoothing = value;
					canvas.repaint();
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
		
		JPanel parameter_panel = new JPanel(new GridLayout(2,2));
		parameter_panel.add(offset_information);
		range_information  = new JTextField();
		range_information.setHorizontalAlignment(JTextField.CENTER);
		string = String.format("%,.2f", range);
		range_information.setText(string);
		parameter_panel.add(range_information);
		parameter_panel.add(new JLabel("Offset", JLabel.CENTER));
		parameter_panel.add(new JLabel("Range", JLabel.CENTER));
		
		JButton adjust_button = new JButton("Adjust");
		ButtonHandler adjust_handler = new ButtonHandler();
		adjust_button.addActionListener(adjust_handler);
		
		location_panel.add(parameter_panel, BorderLayout.CENTER);
		location_panel.add(adjust_button, BorderLayout.SOUTH);
        location_dialog = new JDialog(frame, "Location");
        location_dialog.add(location_panel);
        
		frame.setJMenuBar(menu_bar);
		
		Cursor cursor = new Cursor(Cursor.DEFAULT_CURSOR);
		//Cursor cursor = new Cursor(Cursor.HAND_CURSOR);
		//Cursor cursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
		//Cursor cursor = new Cursor(Cursor.E_RESIZE_CURSOR);
		
        frame.setCursor(cursor);
		frame.pack();
		frame.setLocation(300, 300);
		apply_item.doClick();
	}

	class PlacementCanvas extends Canvas
	{
		Color[] outline_color;
		Color[] fill_color;
		
		int top_margin    = 2;
		int bottom_margin = 4;
		int left_margin   = 4;
		int right_margin  = 2;

		public PlacementCanvas()
		{
			outline_color = new Color[10];
			fill_color = new Color[10];

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

			//System.out.println("Inside placement canvas paint()");
			int xdim = (int) visible_area.getWidth();
			int ydim = (int) visible_area.getHeight();
			
			//System.out.println("xdim = " + xdim + ", ydim = " + ydim);

			Graphics2D g2 = (Graphics2D) g;
			Font current_font = g2.getFont();
			FontMetrics font_metrics = g2.getFontMetrics(current_font);
			g2.setColor(java.awt.Color.WHITE);
			g2.fillRect(0, 0, xdim, ydim);
			
			int size = sensor_data.size();
			//System.out.println("Sensor data size is " + size);
            int number_of_segments = 1;
			if (size > 0)
			{
				number_of_segments = size - 4;
			}
			//System.out.println("number of segments is " + number_of_segments);
			
			double max_xstep = xdim / number_of_segments;
			int xstep = (int) (max_xstep * normal_xstep);
			//System.out.println("Xstep is " + xstep);
			
			int graph_xdim = xdim  - (left_margin + right_margin) - (number_of_segments - 1) * xstep;

			// Separate graphs instead of butting them.
			if (xstep == max_xstep)
			{
				graph_xdim -= 2;
			}
			
			double max_ystep  = ydim / number_of_segments;
			int    ystep      = (int) (max_ystep * normal_ystep);
			int    graph_ydim = ydim -(top_margin + bottom_margin) - (number_of_segments - 1) * ystep;

			
			if (ystep == max_ystep)
			{
				graph_ydim -= 2;
			}
			
			//System.out.println("graph xdim = " + graph_xdim + ", graph ydim = " + graph_ydim);
			
			Rectangle [] rectangle   = new Rectangle[number_of_segments];
			boolean   [] visible     = new boolean[number_of_segments];
			boolean   [] transparent = new boolean[number_of_segments];
			
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
	             
	             //System.out.println("a1 is " + a1 + ", a2 is " + a2);
	             //System.out.println("b1 is " + b1 + ", b2 is " + b2);
	             rectangle[i] = new Rectangle(a2, b2, graph_xdim, graph_ydim);
	             String visible_string = (String) option_table.getValueAt(1, i + 1);
	             if(visible_string.equals("yes"))
	                 visible[i] = true;	 
	             else
	            	 visible[i] = false;
	             String transparent_string = (String) option_table.getValueAt(2, i + 1);
	             if(transparent_string.equals("yes"))
		                 transparent[i] = true;	 
		             else
		            	 transparent[i] = false;
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
				if(visible[i])
				{
					if(view.equals("East"))
					{
						if(!transparent[i])
						{
						    g2.setColor(fill_color[i]);  
						    g2.fillRect(a1, b2, graph_xdim, graph_ydim);
						}
						g2.setColor(outline_color[i]);  
					    g2.drawRect(a1, b2, graph_xdim, graph_ydim);
					}
					else
					{
						if(!transparent[i])
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

	class XStepHandler implements AdjustmentListener
	{
		public void adjustmentValueChanged(AdjustmentEvent event)
		{
			int xstep = event.getValue();
			normal_xstep = (double) xstep / 100;
			// System.out.println("Normal xstep is now " + xstep);
			if (event.getValueIsAdjusting() == false)
			{
				canvas.repaint();
				placement_canvas.repaint();
			}
		}
	}

	class YStepHandler implements AdjustmentListener
	{
		public void adjustmentValueChanged(AdjustmentEvent event)
		{
			int ystep = 100 - event.getValue();
			normal_ystep = (double) ystep / 100;
			// System.out.println("Normal ystep is now " + normal_ystep);
			if (event.getValueIsAdjusting() == false)
			{
				canvas.repaint();
				placement_canvas.repaint();
			}
		}
	}

	
	
	
	
	
	
	
	class LineCanvas extends Canvas
	{
		Color[] outline_color;
		Color[] fill_color;
		int top_margin    = 10;
		int right_margin  = 10;
		int left_margin   = 60;
		int bottom_margin = 60;
		int original_xdim = 0;
		int original_ydim = 0;

		public LineCanvas()
		{
			outline_color = new Color[10];
			fill_color = new Color[10];

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
            
			Graphics2D g2 = (Graphics2D)g;
			Image buffered_image = new BufferedImage(xdim, ydim, BufferedImage.TYPE_INT_RGB);
			Graphics2D buffered_g = (Graphics2D) buffered_image.getGraphics();
			buffered_g.setColor(java.awt.Color.WHITE);
			buffered_g.fillRect(0, 0, xdim, ydim);
			
			int size = sensor_data.size();

			if (size > 0)
			{
				int number_of_segments = size - 4;

				double seg_min = (double) sensor_data.get(0);
				double seg_max = (double) sensor_data.get(1);
				double line_min = (double) sensor_data.get(2);
				double line_max = (double) sensor_data.get(3);

				int max_length = 0;
				for (int i = 0; i < number_of_segments; i++)
				{
					ArrayList list = (ArrayList) sensor_data.get(i + 4);
					int current_length = list.size();
					if ((current_length - 4) > max_length)
						max_length = current_length - 4;
				}

				double max_xstep = (xdim - (left_margin + right_margin)) / number_of_segments;
				int xstep = (int) (max_xstep * normal_xstep);
				int graph_xdim = xdim - (left_margin + right_margin) - (number_of_segments - 1) * xstep;

				if (xstep == max_xstep)
				{
					graph_xdim -= number_of_segments;
				}
				
				double max_ystep  = (ydim - (top_margin + bottom_margin)) / number_of_segments;
				int    ystep      = (int) (max_ystep * normal_ystep);
				int    graph_ydim = ydim - (top_margin + bottom_margin) - (number_of_segments - 1) * ystep;

				
				if (ystep == max_ystep)
				{
					graph_ydim -= number_of_segments;
				}

				double quantum_distance = range / graph_xdim;
				//System.out.println("The quantum distance for a pixel is " + quantum_distance);

				/*
				System.out.println("The number of segments being plotted is " + number_of_segments);
				System.out.println("Minimum value in the segments is " + seg_min);
				System.out.println("Maximum value in the segments is " + seg_max);
				System.out.println("Minimum value in the lines is " + line_min);
				System.out.println("Maximum value in the lines is " + line_max);
				*/

				// System.out.println("Canvas xdim is " + xdim + ", ydim is " + ydim);
				// System.out.println("Graph xdim is " + graph_xdim + ", ydim is " + graph_ydim);

				double minimum_x = offset;
				double maximum_x = offset + range;
				double minimum_y = 0;
				double maximum_y = 0;
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
                
				double xrange = range;
				double yrange = maximum_y - minimum_y;

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
		             
		             if(view.equals("East"))
		                 buffered_g.setColor(outline_color[i]);
		             else
		            	 buffered_g.setColor(outline_color[(number_of_segments - 1) - i]);
		             
		             buffered_g.setStroke(new BasicStroke(2));
		             buffered_g.drawLine((int) a1, (int) b1, (int) a1, (int) b2); 
		             buffered_g.drawLine((int) a1, (int) b2, (int) a1 + 5, (int) b2);
		             
		             // If plots directly overlap, we only need one line to show the y extent.
				     if (ystep == 0 && xstep == 0) 
					     break; 
				 }
				 

				ArrayList plot_data = new ArrayList();
				for (int i = 4; i < size; i++)
				{
					ArrayList sensor_list = (ArrayList) sensor_data.get(i);
					int length = sensor_list.size();
					ArrayList plot_list = new ArrayList();
					
                    // This is to help filter out vertical jitter but not sure that's what we want to do.
					/*
					int j = 4;
					Sample sample = (Sample) sensor_list.get(j);
					Point2D.Double point = new Point2D.Double();
					point.x = sample.y;
					point.y = sample.intensity;
					point.y *= scale_factor;
					plot_list.add(point);
					double previous_x = sample.x;
					double current_intensity_max = sample.intensity;
					double current_intensity_min = sample.intensity;
					for (j = 5; j < length; j++)
					*/
					for (int j = 4; j < length; j++)
					{
						Sample sample = (Sample) sensor_list.get(j);
						// Will revisit this later.
						// Need to keep track of new dynamic range after subsampling.
						/*
						if (sample.x > previous_x + quantum_distance)
						{
							point = new Point2D.Double();
							point.x = sample.y;
							point.y = sample.intensity;
                            point.y *= scale_factor;
                            if (point.y < minimum_y)
                                point.y = minimum_y;
                            else if (point.y > maximum_y)
                                point.y = maximum_y;
							previous_x = sample.x;
							if(sample.intensity < current_intensity_min)
							    current_intensity_min = sample.intensity;
							 else if(sample.intensity > current_intensity_max)
							    current_intensity_max = sample.intensity;
						}
						*/
						
						// Using all the points, even the ones that map to the
						// same x coordinate on the display.
						Point2D.Double point = new Point2D.Double();
						point.x = sample.y;
						point.y = sample.intensity; 
						point.y *= scale_factor;
						if (point.y < minimum_y)
                            point.y = minimum_y;
                        else if (point.y > maximum_y)
                            point.y = maximum_y;
						plot_list.add(point);
						
					}
					int plot_length = plot_list.size();
                     
					//System.out.println("The plot list is " + plot_length + " points long");
					//System.out.println();
					if (smoothing == 0)
						plot_data.add(plot_list);
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
						double[] smooth_x = smooth(x, smoothing);
						double[] smooth_y = smooth(y, smoothing);

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
					if(view.equals("East"))
					{
					    segment = (ArrayList) plot_data.get(i);
					}
					else
					{
						segment = (ArrayList) plot_data.get((number_of_segments - 1) - i);   	
					}

					int n   = segment.size() + 3;
					int[] x = new int[n];
					int[] y = new int[n];
					x[0]    = a1;
					y[0]    = b1;

					
					int m = 1;

					for (int k = 0; k < segment.size(); k++)
					{ 
						Point2D.Double point = (Point2D.Double) segment.get(k);
						double current_x = point.getX();
						current_x -= minimum_x;
						current_x /= xrange;
						current_x *= graph_xdim;
						current_x += left_margin;
						current_x += xaddend;
						
						double current_y = point.getY();
						current_y -= minimum_y;
						if(current_y < 0)
						{
							System.out.println("Current y is " + current_y);					
						}
						/*
						if(autoscale)
							current_y -= line_min;
						else
							current_y -= seg_min;
						*/
						current_y /= yrange;
						current_y *= graph_ydim;
						current_y = graph_ydim - current_y;
						current_y += top_margin + (number_of_segments - 1) * ystep;
						current_y -= yaddend;
					
						x[m] = (int) current_x;
						y[m] = (int) current_y;
						m++;
					}

					x[m] = a2;
					y[m] = b1;
					m++;

					x[m] = a1;
					y[m] = b1;

					java.awt.Polygon sensor_polygon = new Polygon(x, y, n);
					polygon[i] = sensor_polygon;

					ArrayList sensor_list = (ArrayList) sensor_data.get(i + 4);
					String visible = (String) sensor_list.get(2);
					String transparent = (String) sensor_list.get(3);

					if (visible.equals("yes"))
					{
						
						if (transparent.equals("no"))
						{
							if(view.equals("East"))
							    buffered_g.setColor(fill_color[i]);
							else
								buffered_g.setColor(fill_color[(number_of_segments - 1) - i]);
							buffered_g.fillPolygon(polygon[i]);
						}
						if(view.equals("East"))
						    buffered_g.setColor(outline_color[i]);
						else
							buffered_g.setColor(outline_color[(number_of_segments - 1) - i]);
						buffered_g.drawPolygon(polygon[i]);
                        
						if (minimum_y < 0)
						{
							ArrayList plot_list = (ArrayList) plot_data.get(i);
							Point2D.Double first = (Point2D.Double) plot_list.get(0);
							int plot_length = plot_list.size();
							Point2D.Double last = (Point2D.Double) plot_list.get(plot_length - 1);

							double x1 = first.getX();
							double x2 = last.getX();
							double zero_y = Math.abs(minimum_y);

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

							zero_y /= yrange;
							zero_y *= graph_ydim;
							zero_y = graph_ydim - zero_y;
							zero_y += top_margin + (number_of_segments - 1) * ystep;
							zero_y -= yaddend;

							float[] dash = { 2f, 0f, 2f }; 
							BasicStroke basic_stroke = new BasicStroke(1,BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, dash, 2f);
							buffered_g.setStroke(basic_stroke); 
							buffered_g.setColor(java.awt.Color.BLACK);
							buffered_g.drawLine((int)x1, (int)zero_y, (int)x2, (int)zero_y); 
							buffered_g.setStroke(new BasicStroke(2));
						}
					}
				}
				g2.drawImage(buffered_image, 0, 0, null);
				g2.setColor(java.awt.Color.BLACK); 
				double stop = offset + range; 
				String position_string = String.format("%,.2f", offset);
				g2.drawString(position_string, left_margin, ydim - bottom_margin / 2);
				
				position_string = String.format("%,.2f", stop); 
				Font current_font = g2.getFont(); 
				FontMetrics font_metrics = g2.getFontMetrics(current_font);
				int string_width = font_metrics.stringWidth(position_string);
				g2.drawString(position_string, graph_xdim + left_margin - string_width, ydim - bottom_margin / 2);
				
				position_string = new String("meters"); 
				string_width = font_metrics.stringWidth(position_string); 
				g2.drawString(position_string, left_margin + (graph_xdim + left_margin - string_width) / 2 - string_width / 2, ydim - bottom_margin / 3);
				
				String intensity_string = String.format("%,.2f", minimum_y); 
				string_width = font_metrics.stringWidth(intensity_string); 
				g2.drawString(intensity_string, left_margin / 2 - string_width / 2, ydim - bottom_margin); 
				
				intensity_string = String.format("%,.2f", maximum_y); 
				string_width = font_metrics.stringWidth(intensity_string); 
				int string_height = font_metrics.getAscent(); 
				g2.drawString(intensity_string, left_margin / 2 - string_width / 2, top_margin + (number_of_segments - 1) * ystep + string_height);
				
				
				intensity_string =new String("nT"); 
				string_width = font_metrics.stringWidth(intensity_string);
				g2.drawString(intensity_string, string_width / 2, top_margin + (number_of_segments - 1) * ystep + graph_ydim / 2);
				
				double zero_position = Math.abs(minimum_y); 
				zero_position /= yrange;
				zero_position *= graph_ydim; 
				zero_position = graph_ydim - zero_position;
				zero_position += top_margin + (number_of_segments - 1) * ystep; 
				String zero_string = new String("0.0"); 
				string_width = font_metrics.stringWidth(zero_string);
				g2.drawString(zero_string, left_margin - (string_width + 5), (int)zero_position);
				
			}
		}
	}

	class ApplyHandler implements ActionListener
	{
		int[][] line_array;
		int[] line;
		int[] sensor;

		ApplyHandler()
		{
			line_array = ObjectMapper.getUnclippedLineArray();
			line = new int[10];
			sensor = new int[10];
		}

		public void actionPerformed(ActionEvent event)
		{
			//System.out.println("Offset is " + offset);
			//System.out.println("Range is " + range);
			for (int i = 0; i < 10; i++)
			{
				try
				{
					String line_sensor_pair = (String) option_table.getValueAt(0, i + 1);
					StringTokenizer tokenizer = new StringTokenizer(line_sensor_pair, ":");
					int number_of_tokens = tokenizer.countTokens();
					if (number_of_tokens == 2)
					{
						String line_string = tokenizer.nextToken(":");
						int current_line = Integer.parseInt(line_string);
						String sensor_string = tokenizer.nextToken(":");
						int current_sensor = Integer.parseInt(sensor_string);

						// Do a check for valid input
						if (current_line >= 0 && current_line < 30 && current_sensor >= 0 && current_sensor < 5)
						{
							line[i] = current_line;
							sensor[i] = current_sensor;
						} else
						{
							line[i] = -1;
							sensor[i] = -1;
						}
					} else
					{
						line[i] = -1;
						sensor[i] = -1;
					}
				} catch (Exception exception)
				{
					line[i] = -1;
					sensor[i] = -1;
				}
			}

			// Do a first pass thru the data to get the segment and line extrema.
			// This is so we can prepend it to the segment data for the line canvas
			// to use to implement autoscaling.
			double seg_min = Double.MAX_VALUE;
			double seg_max = -Double.MAX_VALUE;

			double line_min = Double.MAX_VALUE;
			double line_max = -Double.MAX_VALUE;

			for (int i = 0; i < 10; i++)
			{
				if (line[i] != -1)
				{
					int current_line = line[i];
					int current_sensor = sensor[i];
					int start = line_array[current_line][0];
					int stop = line_array[current_line][1];

					if (current_line % 2 == 0)
					{
						for (int j = start + current_sensor; j < stop; j += 5)
						{
							Sample sample = (Sample) data.get(j);
							if (sample.y >= offset && sample.y < (offset + range))
							{
								if (seg_min > sample.intensity)
								{
									seg_min = sample.intensity;
								}
								if (seg_max < sample.intensity)
								{
									seg_max = sample.intensity;
								}
							}
							if (sample.y >= 15 && sample.y < 75)
							{
								if (line_min > sample.intensity)
								{
									line_min = sample.intensity;
								}
								if (line_max < sample.intensity)
								{
									line_max = sample.intensity;
								}
							}
						}
					} else
					{
						for (int j = stop - (1 + (4 - current_sensor)); j >= start; j -= 5)
						{
							Sample sample = (Sample) data.get(j);
							if (sample.y >= offset && sample.y < (offset + range))
							{
								if (seg_min > sample.intensity)
								{
									seg_min = sample.intensity;
								}
								if (seg_max < sample.intensity)
								{
									seg_max = sample.intensity;
								}
							}
							if (sample.y >= 15 && sample.y < 75)
							{
								if (line_min > sample.intensity)
								{
									line_min = sample.intensity;
								}
								if (line_max < sample.intensity)
								{
									line_max = sample.intensity;
								}
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
				if (line[i] != -1)
				{
					int current_line = line[i];
					int current_sensor = sensor[i];
					ArrayList segment_data = new ArrayList();
					segment_data.add(current_line);
					segment_data.add(current_sensor);

					String visible = (String) option_table.getValueAt(1, i + 1);
					segment_data.add(visible);
					String transparent = (String) option_table.getValueAt(2, i + 1);
					segment_data.add(transparent);

					int start = line_array[current_line][0];
					int stop = line_array[current_line][1];
					if (current_line % 2 == 0)
					{
						for (int j = start + current_sensor; j < stop; j += 5)
						{
							Sample sample = (Sample) data.get(j);
							if (sample.y >= offset && sample.y < (offset + range))
							{
								segment_data.add(sample);
							}
						}
					} 
					else
					{
						for (int j = stop - (1 + (4 - current_sensor)); j >= start; j -= 5)
						{
							Sample sample = (Sample) data.get(j);
							if (sample.y >= offset && sample.y < (offset + range))
							{
								segment_data.add(sample);
							}
						}
					}
					sensor_data.add(segment_data);
				}
			}
			canvas.repaint();
		}
	}

	class ScrollbarHandler implements AdjustmentListener
	{
		public void adjustmentValueChanged(AdjustmentEvent event)
		{
			JScrollBar scrollbar = (JScrollBar) event.getSource();
			Double current_offset = (double) event.getValue();
			current_offset /= 100.;
			current_offset *= (60. - range);
			current_offset += 15.;
			String string = String.format("%,.2f", current_offset);
			offset_information.setText(string);
			if (scrollbar.getValueIsAdjusting() == false)
			{
				if (slider_changing == false  && button_changing == false)
				{
					scrollbar_changing = true; 
					
					// Reset the offset.
					offset = (double) event.getValue();
					offset /= 100.;
					offset *= (60. - range);
					offset += 15.;
					string = String.format("%,.2f", offset);
					offset_information.setText(string);
					
					// Reset the slider.
					int value = (int) offset;
					int upper_value = (int) (offset + range);
					range_slider.setValue(value);
					range_slider.setUpperValue(upper_value);

					scrollbar_changing = false;
					
					// Resegment the data.
					apply_item.doClick();
				}
			}
		}
	}
    
	class ButtonHandler  implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			String offset_string = offset_information.getText();
		    String range_string  = range_information.getText();
		    double current_offset = Double.valueOf(offset_string);
		    double current_range  = Double.valueOf(range_string);
		    
		    if(current_offset < 15 || current_offset > (75 - current_range) || current_range < 0 || current_range > 59)
		    {
		    	System.out.println("Invalid input.");
		    	offset_string = String.format("%,.2f", offset);
		    	range_string = String.format("%,.2f", range);
		    	offset_information.setText(offset_string);
		    	range_information.setText(range_string);
		    }
		    else if(slider_changing == false && scrollbar_changing == false)
			{
				button_changing = true;
				
				offset = current_offset;
				range  = current_range;
				
				// Reset the scrollbar.
				double position = offset - 15.;
				position *= 100. / (60 - range);
				int value = (int) position;
				scrollbar.setValue(value);
				
				// Reset the slider.
				range_slider.setValue((int)offset);
				range_slider.setValue((int)(offset + range));
				
				
				button_changing = false;
				
				// Resegment the data.
				apply_item.doClick();
			}
		}
	};
	
	class SliderHandler implements ChangeListener
	{
		public void stateChanged(ChangeEvent e)
		{
			if (scrollbar_changing == false && button_changing == false)
			{
				slider_changing = true;
				RangeSlider slider = (RangeSlider) e.getSource();
				if (slider.getValueIsAdjusting() == false)
				{
					// Set the new offset and range.
					offset = (double) slider.getValue();
					double stop = (double) slider.getUpperValue();
					range = stop - offset;
					if (range == 0)
						range = 1; 
					String string = String.format("%,.2f", offset);
					offset_information.setText(string);
					string = String.format("%,.2f", range);
					range_information.setText(string);
					
					// Reset the scrollbar.
					double position = offset - 15.;
					position *= 100. / (60 - range);
					int value = (int) position;
					scrollbar.setValue(value);
					slider_changing = false;
					
					// Resegment the data.
					apply_item.doClick();
				}
			}
		}
	}

	class ScaleHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			Point location_point = frame.getLocation();
			int x = (int) location_point.getX();
			int y = (int) location_point.getY();

			x += 800;
			y -= 100;

			if (y < 0)
				y = 0;

			scale_dialog.setLocation(x, y);
			scale_dialog.pack();
			scale_dialog.setVisible(true);
		}
	}

	class DynamicRangeHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			Point location_point = frame.getLocation();
			int x = (int) location_point.getX();
			int y = (int) location_point.getY();

			x += 800;
			y -= 100;

			if(y < 0)
				y = 0;
			
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
			    
			}
			else
			{
				String lower_bound_string = String.format("%,.2f", line_min);
			    String upper_bound_string = String.format("%,.2f", line_max);
			    lower_bound.setText(lower_bound_string);
			    upper_bound.setText(upper_bound_string);   	
			}
			dynamic_range_dialog.setLocation(x, y);
			dynamic_range_dialog.pack();
			dynamic_range_dialog.setVisible(true);
		}
	}
	
	class SmoothHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			Point location_point = frame.getLocation();
			int x = (int) location_point.getX();
			int y = (int) location_point.getY();

			x += 800;
			y -= 100;

			if (y < 0)
				y = 0;

			smooth_dialog.setLocation(x, y);
			smooth_dialog.pack();
			smooth_dialog.setVisible(true);
		}
	}

	class PlacementHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			Point location_point = frame.getLocation();
			int x = (int) location_point.getX();
			int y = (int) location_point.getY();

			x += 800;
			y -= 100;

			if (y < 0)
				y = 0;

			placement_dialog.setLocation(x, y);
			placement_dialog.pack();
			placement_dialog.setVisible(true);
		}
	}

	class LocationHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			Point location_point = frame.getLocation();
			int x = (int) location_point.getX();
			int y = (int) location_point.getY();

			x += 800;
			y -= 100;

			if (y < 0)
				y = 0;

			location_dialog.setLocation(x, y);
			location_dialog.pack();
			location_dialog.setVisible(true);
		}
	}
	
	class ViewHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			Point location_point = frame.getLocation();
			int x = (int) location_point.getX();
			int y = (int) location_point.getY();

			x += 800;
			y -= 100;

			if (y < 0)
				y = 0;

			view_dialog.setLocation(x, y);
			view_dialog.pack();
			view_dialog.setVisible(true);
		}
	}
	
	class LoadHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			Point location_point = frame.getLocation();
			int x = (int) location_point.getX();
			int y = (int) location_point.getY();

			x -= 100;
			y -= 100;

			if (x < 0)
				x = 0;
			if (y < 0)
				y = 0;

			load_dialog.setLocation(x, y);
			load_dialog.pack();
			load_dialog.setVisible(true);
		}
	}
	
	class AdjustRangeHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			double seg_min = (double) sensor_data.get(0);
			double seg_max = (double) sensor_data.get(1);
			double line_min = (double) sensor_data.get(2);
			double line_max = (double) sensor_data.get(3);
			
			String lower_bound_string = lower_bound.getText();
			String upper_bound_string = upper_bound.getText();
			double current_min = Double.valueOf(lower_bound_string);
			double current_max = Double.valueOf(upper_bound_string);
			System.out.println("Current min is " + current_min);
			System.out.println("Current max is " + current_max);
			System.out.println("Seg min is " + seg_min);
			System.out.println("Seg max is " + seg_max);
			System.out.println("Line min is " + line_min);
			System.out.println("Line max is " + line_max);
			
			if(autoscale)
			{
				if(current_min >= seg_min && current_max <= seg_max)
				{
					data_clipped = true;
					apply_item.doClick();
				}
				else
				{
			        lower_bound_string = String.format("%,.2f", seg_min);
			        upper_bound_string = String.format("%,.2f", seg_max);
			        lower_bound.setText(lower_bound_string);
			        upper_bound.setText(upper_bound_string);
			        System.out.println("Clipping values are outside of current dynamic range.");
				}
			    
			}
			else
			{
				if(current_min >= line_min && current_max <= line_max)
				{
					data_clipped = true;
					apply_item.doClick();
				}
				else
				{
					lower_bound_string = String.format("%,.2f", line_min);
			        upper_bound_string = String.format("%,.2f", line_max);
			        lower_bound.setText(lower_bound_string);
			        upper_bound.setText(upper_bound_string);
			        System.out.println("Clipping values are outside of current dynamic range.");   
				}
			}
		}
	}
	
	class ResetRangeHandler implements ActionListener
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
			    
			}
			else
			{
				String lower_bound_string = String.format("%,.2f", line_min);
			    String upper_bound_string = String.format("%,.2f", line_max);
			    lower_bound.setText(lower_bound_string);
			    upper_bound.setText(upper_bound_string);   	
			}
			data_clipped = false;
			apply_item.doClick();
		}
	}
	
	class InputHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			String input_string = input.getText();
			StringTokenizer input_tokenzer = new StringTokenizer(input_string);
			String line_string = input_tokenzer.nextToken(":");
			int current_line = Integer.parseInt(line_string);
			String sensor_string = input_tokenzer.nextToken(":");
			int current_sensor = Integer.parseInt(sensor_string);

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
			} else
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
			} else
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
				} else
				{
					for (int i = 4; i >= 0; i--)
					{
						String current_string = new String(current_line + ":" + i);
						line_sensor_pair[current_pair] = current_string;
						current_pair++;
						if(current_pair == 10)
							break outer;
					}
				}
			}

			for (int i = 0; i < 10; i++)
			{
				option_table.setValueAt(line_sensor_pair[i], 0, i + 1);
			}
		}
	}

	class SaveHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ev)
		{
			int size = sensor_data.size();
			if(size == 0)
			{
				System.out.println("No data to save.");
			}
			else
			{
				System.out.println("Data to save.");
				FileDialog file_dialog = new FileDialog(frame, "Save Segment", FileDialog.SAVE);
				file_dialog.setVisible(true);
				String filename = file_dialog.getFile();
				if(filename != "")
				{
					String current_directory = file_dialog.getDirectory();
					System.out.println("Current directory is " + current_directory);
					System.out.println("File name is " + filename);
					try(PrintWriter output = new PrintWriter(current_directory + filename))
			        {
						// The first 4 elements in the sensor data are local min, local max, global min, and global max.
						// The rest are array lists of samples, with some information prepended.  
						
						double intensity_min = (double)sensor_data.get(0);
						String intensity_min_string = String.format("%.2f", intensity_min);
					    for(int i = 4; i < size; i++)
					    {
					    	// The first 4 elements are flight line, sensor, visibility, and transparency.
					    	// The last two are yes/no strings.
					        ArrayList segment_list = (ArrayList)sensor_data.get(i);
					        int       line         = (int)segment_list.get(0);
					        int       sensor       = (int)segment_list.get(1);
					        
					        
					        
					        output.println("#Sensor " + sensor + ", Line " + line);
					        
			      			
					        double ideal_x = line * 2;
					        if(line % 2 == 0)
					        {
					        	ideal_x += ((4 - sensor)* .5);
					        }
					        else
					        {
					        	ideal_x += sensor * .5;
					        }
					        
					        String ideal_string          = String.format("%.2f", ideal_x);
					        
					        Sample init_sample = (Sample)segment_list.get(4);
					        String xstring          = String.format("%.2f", init_sample.x);	
			      			String ystring          = String.format("%.2f", init_sample.y);
			      			output.println(xstring + " " + ystring + " " + intensity_min_string + " " + ideal_string);
					        
					        
					        for(int j = 4; j < segment_list.size(); j++)
					        {
					            Sample sample = (Sample)segment_list.get(j);
					            xstring          = String.format("%.2f", sample.x);	
				      			ystring          = String.format("%.2f", sample.y);
				      			String intensity_string = String.format("%.2f", sample.intensity);
				      			output.println(xstring + " " + ystring + " " + intensity_string + " " + ideal_string);
					        }
					        
					        Sample end_sample  = (Sample)segment_list.get(size - 1);
					        xstring          = String.format("%.2f", end_sample.x);	
			      			ystring          = String.format("%.2f", end_sample.y);
			      			output.println(xstring + " " + ystring + " " + intensity_min_string + " " + ideal_string);
			      			
			      			xstring          = String.format("%.2f", init_sample.x);	
			      			ystring          = String.format("%.2f", init_sample.y);
			      			output.println(xstring + " " + ystring + " " + intensity_min_string + " " + ideal_string);
			      			
					        output.println();
					        output.println();
					    }
					    output.close();
			        }
					catch(Exception ex)
					{
						System.out.println(ex.toString());
					}
				}		
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
}
