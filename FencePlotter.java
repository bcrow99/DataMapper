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

public class FencePlotter
{
	// Input
	ArrayList data = new ArrayList();
	ArrayList line_data = new ArrayList();
	ArrayList sensor_data = new ArrayList();
	ArrayList modified_data = new ArrayList();
	ArrayList interpolated_data = new ArrayList();
	ArrayList reduced_data = new ArrayList();
	ArrayList plot_data = new ArrayList();
	ArrayList baseline_data = new ArrayList();
	//double max_delta;
	
	
    double  shift = 0;
	boolean slider_changed = false;
	boolean scrollbar_changed = false;
	double  line_imin = 0;
	double  line_imax = 0;
	double  segment_imin = 0;
	double  segment_imax = 0;

	// Plotting parameters
	Color[] outline_color = new Color[5];
	Color[] fill_color = new Color[5];
	Color[] zero_crossing_color = new Color[5];
	int number_of_sensors = 5;

	int bottom_margin = 60;
	int left_margin = 60;
	int top_margin = 10;
	int right_margin = 10;
	double  normal_xstep = 0;
	double  normal_ystep = 0;
	int xdim = 0;
	int ydim = 0;
	int graph_xdim = 0;
	int graph_ydim = 0;

	// Interface componants.
	public JFrame frame;
	public JTable table;
	public LineCanvas canvas;
	public JScrollBar scrollbar;
	public JButton apply_button;
	public RangeSlider range_slider;
	public JTextField input;
	
	public static void main(String[] args)
	{
		String prefix = new String("C:/Users/Brian Crowley/Desktop/");
		//String prefix = new String("");
		if (args.length != 1)
		{
			System.out.println("Usage: FencePlotter <data file>");
			System.exit(0);
		} 
		else
		{
			try
			{
				String filename = prefix + args[0];
				try
				{
					FencePlotter window = new FencePlotter(filename);
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

	public FencePlotter(String filename)
	{
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

		frame.getContentPane().add(canvas, BorderLayout.CENTER);

		outline_color[0] = new Color(0, 0, 0);
		outline_color[1] = new Color(0, 0, 150);
		outline_color[2] = new Color(150, 0, 0);
		outline_color[3] = new Color(0, 150, 0);
		outline_color[4] = new Color(150, 0, 150);
        
		fill_color[0] = new Color(196, 196, 196);
		fill_color[1] = new Color(196, 196, 255);
		fill_color[2] = new Color(255, 196, 196);
		fill_color[3] = new Color(196, 255, 196);
		fill_color[4] = new Color(255, 196, 255);
		
		zero_crossing_color[0] = new Color(60, 60, 60);
		zero_crossing_color[1] = new Color(60, 60, 0);
		zero_crossing_color[1] = new Color(0, 60, 60);
		zero_crossing_color[1] = new Color(60, 0, 60);
		zero_crossing_color[1] = new Color(0, 60, 0);

		table = new JTable(6, 14)
		{
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
			{
				Component c = super.prepareRenderer(renderer, row, column);
				if (column == 13 && row > 0)
					c.setBackground(fill_color[row - 1]);
				else
					c.setBackground(java.awt.Color.WHITE);
				return c;
			}
		};

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);

		for (int column = 0; column < 14; column++)
		{
			table.getColumnModel().getColumn(column).setCellRenderer(centerRenderer);
		}

		String header;
		header = new String("Line");
		table.setValueAt(header, 0, 0);
		header = new String("Sensor");
		table.setValueAt(header, 0, 1);
		header = new String("Start");
		table.setValueAt(header, 0, 2);
		header = new String("Range");
		table.setValueAt(header, 0, 3);
		header = new String("Resolution");
		table.setValueAt(header, 0, 4);
		header = new String("Smoothing");
		table.setValueAt(header, 0, 5);
		header = new String("Data Usage");
		table.setValueAt(header, 0, 6);
		header = new String("Autoscale");
		table.setValueAt(header, 0, 7);
		header = new String("Visible");
		table.setValueAt(header, 0, 8);
		header = new String("Transparent");
		table.setValueAt(header, 0, 9);
		header = new String("Imin");
		table.setValueAt(header, 0, 10);
		header = new String("Imax");
		table.setValueAt(header, 0, 11);
		header = new String("Scale");
		table.setValueAt(header, 0, 12);
		header = new String("Key");
		table.setValueAt(header, 0, 13);

		int     rows  = 5;
		int     line  = 9;
		double start  = 15;
		double range  = 60;
		
		double [] local_min_max = getMinMax(line, start, range, data);
		//System.out.println("Min intensity in segment is " + local_min_max[0]);
		//System.out.println("Max intensity in segment is " + local_min_max[1]);
		double [] global_min_max = getMinMax(line, 15, 60, data);
		//System.out.println("Min intensity in line is " + global_min_max[0]);
		//System.out.println("Max intensity in line is " + global_min_max[1]);
		line_imin = global_min_max[0];
		line_imax = global_min_max[1];
		
		input    = new JTextField();
		input.setText(Integer.toString(line));
		
		for (int i = 0; i < rows; i++)
		{
			table.setValueAt((String) Integer.toString(line), i + 1, 0);
			if(line % 2 == 1)
			    table.setValueAt((String) Integer.toString(i), i + 1, 1);
			else
				table.setValueAt((String) Integer.toString(4 - i), i + 1, 1);
			table.setValueAt(Double.toString(start), i + 1, 2);
			table.setValueAt(Double.toString(range), i + 1, 3);
			table.setValueAt((String) "100", i + 1, 4);
			table.setValueAt((String) "0", i + 1, 5);
			table.setValueAt((String) "0", i + 1, 6);
			table.setValueAt((String) "no", i + 1, 7);
			table.setValueAt((String) "yes", i + 1, 8);
			table.setValueAt((String) "no", i + 1, 9);	
			table.setValueAt(Double.toString(local_min_max[0]), i + 1, 10);
			table.setValueAt(Double.toString(local_min_max[1]), i + 1, 11);
			table.setValueAt((String) "1.0", i + 1, 12);
		}

		JPanel segment_panel = new JPanel(new BorderLayout());
		scrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 3, -100, 103);
		ShiftHandler shift_handler = new ShiftHandler();
		scrollbar.addAdjustmentListener(shift_handler);
		segment_panel.add(scrollbar, BorderLayout.NORTH);

		range_slider = new RangeSlider();
		range_slider.setMinimum(15);
		range_slider.setMaximum(75);
		range_slider.setValue(35);
		range_slider.setUpperValue(55);
		range_slider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				if (scrollbar_changed == false)
				{
					slider_changed = true;
					RangeSlider slider = (RangeSlider) e.getSource();
					if (slider.getValueIsAdjusting() == false)
					{
						double start = (double) slider.getValue();
						double stop = (double) slider.getUpperValue();
						double range = stop - start;
						if (range == 0)
							range = 1;
						shift = start - (45. - (range / 2));
						String start_string = String.format("%,.2f", start);
						String range_string = String.format("%,.2f", range);
						String shift_string = String.format("%,.2f", shift);
						for (int i = 1; i < 6; i++)
						{
							table.setValueAt(start_string, i, 2);
							table.setValueAt(range_string, i, 3);
						}
						
						int line = Integer.parseInt((String) table.getValueAt(1, 0));
						double min_max[] = getMinMax(line, start, range, data);
						for (int i = 1; i < 6; i++)
						{
						    table.setValueAt(Double.toString(min_max[0]), i, 10);
						    table.setValueAt(Double.toString(min_max[1]), i, 11);
						}
						segment_imin = min_max[0];
						segment_imax = min_max[1];
						apply_button.doClick(0);
					}
				}
			}
		});
		
		segment_panel.add(range_slider, BorderLayout.SOUTH);

		JPanel bottom_panel = new JPanel(new BorderLayout());
		bottom_panel.add(segment_panel, BorderLayout.NORTH);
		bottom_panel.add(table, BorderLayout.CENTER);

		JPanel apply_panel = new JPanel(new BorderLayout());
		apply_button = new JButton("Apply");
		ApplyHandler handler = new ApplyHandler();
		apply_button.addActionListener(handler);
		apply_panel.add(apply_button, BorderLayout.SOUTH);
		
		
		JButton load_button = new JButton("Load");
		LoadHandler load_handler = new LoadHandler();
		load_button.addActionListener(load_handler);
		JButton save_button = new JButton("Save");
		SaveHandler save_handler = new SaveHandler();
		save_button.addActionListener(save_handler);
		
		JPanel button_panel = new JPanel(new BorderLayout());
		button_panel.add(load_button, BorderLayout.NORTH);
		button_panel.add(save_button, BorderLayout.SOUTH);
		
		JPanel load_panel   = new JPanel(new BorderLayout());
		
		input.setHorizontalAlignment(JTextField.CENTER);
		load_panel.add(button_panel, BorderLayout.CENTER);
		load_panel.add(input, BorderLayout.NORTH);

		JPanel canvas_panel = new JPanel(new BorderLayout());
		Canvas apply_canvas = new Canvas();
		apply_canvas.setSize(40, 40);

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

		canvas_panel.add(apply_canvas, BorderLayout.CENTER);
		canvas_panel.add(xstep_scrollbar, BorderLayout.SOUTH);
		canvas_panel.add(ystep_scrollbar, BorderLayout.EAST);
		apply_panel.add(canvas_panel, BorderLayout.CENTER);
		
		bottom_panel.add(apply_panel, BorderLayout.EAST);
		bottom_panel.add(load_panel, BorderLayout.WEST);
		
		frame.getContentPane().add(bottom_panel, BorderLayout.SOUTH);
		frame.pack();
	}

	class LineCanvas extends Canvas
	{
		public void paint(Graphics g)
		{
			Rectangle visible_area = g.getClipBounds();

			xdim = (int) visible_area.getWidth();
			ydim = (int) visible_area.getHeight();

			double max_xstep = (xdim - (left_margin + right_margin)) / 5;
			int    xstep     = (int) (max_xstep * normal_xstep);
			graph_xdim = xdim - (left_margin + right_margin) - (number_of_sensors - 1) * xstep;
			if(xstep == max_xstep)
			{
			    graph_xdim -= 5;	
			}
			
			
			double max_ystep = (ydim - (top_margin + bottom_margin)) / 5;
			int    ystep     = (int) (max_ystep * normal_ystep);
			graph_ydim = ydim - (top_margin + bottom_margin) - (number_of_sensors - 1) * ystep;
			
			if(ystep == max_ystep)
			{
			    graph_ydim -= 5;	
			}

			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(2));
			g2.setColor(java.awt.Color.BLACK);

			int size = plot_data.size();

			double minimum_x = 0;
			double minimum_y = 0;
			double maximum_x = 0;
			double maximum_y = 0;
			double xrange = 0;
			double yrange = 0;

			if (size == 0)
				return;

			// Collecting statistics in first pass.
			ArrayList baseline = (ArrayList) plot_data.get(0);
			Point2D.Double init_point = (Point2D.Double) baseline.get(0);
			maximum_x = minimum_x = init_point.x;
			maximum_y = minimum_y = init_point.y;
			for (int i = 0; i < size; i++)
			{
				ArrayList current_line = (ArrayList) plot_data.get(i);
				Point2D.Double point = (Point2D.Double) current_line.get(0);

				for (int j = 0; j < current_line.size(); j++)
				{
					point = (Point2D.Double) current_line.get(j);
					if (point.x < minimum_x)
					{
						minimum_x = point.x;
					} 
					else if (point.x > maximum_x)
					{
						maximum_x = point.x;
					}
					if (point.y < minimum_y)
					{
						minimum_y = point.y;
					} 
					else if (point.y > maximum_y)
					{
						maximum_y = point.y;
					}
				}
			}
			
			String autoscale_string = (String) table.getValueAt(1, 7);
			if(autoscale_string.equals("no"))
			{
				minimum_y = line_imin;
				maximum_y = line_imax;
			}
			xrange = maximum_x - minimum_x;

			// Set labels.
			double start = Double.valueOf((String) table.getValueAt(1, 2));
			double current_range = Double.valueOf((String) table.getValueAt(1, 3));
			//double shift = Double.valueOf((String) table.getValueAt(1, 4));
			double stop = start + current_range;
			String position_string = String.format("%,.2f", start);
			g2.drawString(position_string, left_margin, ydim - bottom_margin / 2);
			position_string = String.format("%,.2f", stop);
			Font current_font = g2.getFont();
			FontMetrics font_metrics = g2.getFontMetrics(current_font);
			int string_width = font_metrics.stringWidth(position_string);
			g2.drawString(position_string, graph_xdim + left_margin - string_width, ydim - bottom_margin / 2);

			position_string = new String("meters");
			string_width = font_metrics.stringWidth(position_string);
			g2.drawString(position_string,
					left_margin + (graph_xdim + left_margin - string_width) / 2 - string_width / 2,
					ydim - bottom_margin / 3);

			yrange = maximum_y - minimum_y;

			String intensity_string = String.format("%,.2f", minimum_y);
			string_width = font_metrics.stringWidth(intensity_string);
			g2.drawString(intensity_string, left_margin / 2 - string_width / 2, ydim - bottom_margin);
			intensity_string = String.format("%,.2f", maximum_y);
			string_width = font_metrics.stringWidth(intensity_string);
			int string_height = font_metrics.getAscent();
			g2.drawString(intensity_string, left_margin / 2 - string_width / 2, top_margin + 4 * ystep + string_height);
			intensity_string = new String("nT");
			string_width = font_metrics.stringWidth(intensity_string);
			g2.drawString(intensity_string, string_width / 2, top_margin + 4 * ystep + graph_ydim / 2);

			double zero_position = Math.abs(minimum_y);
			zero_position /= yrange;
    	    zero_position *= graph_ydim;
    	    zero_position = graph_ydim - zero_position;
    	    zero_position += top_margin + 4 * ystep;
    	    String zero_string = new String("0.0");
    	    string_width = font_metrics.stringWidth(zero_string);
    	    //g2.drawString(zero_string, left_margin / 2 - string_width / 2, (int)zero_position);		
    	    g2.drawString(zero_string, left_margin - (string_width + 5), (int)zero_position);	
    	    
			// Draw line showing y extent and construct polygons in second pass.
			Polygon[] polygon = new Polygon[5];
			for (int i = 0; i < 5; i++)
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
				
				if(ystep == 0 && xstep == 0)
					g2.setColor(outline_color[0]);
				else
					g2.setColor(outline_color[i]);
					
				g2.setStroke(new BasicStroke(2));
        	    g2.drawLine((int)a1, (int)b1, (int)a1, (int)b2);
        	    g2.drawLine((int)a1, (int)b2, (int)a1 + 5, (int)b2);
        	    
				ArrayList current_line = (ArrayList) plot_data.get(i);

				int n = current_line.size() + 3;
				int[] x = new int[n];
				int[] y = new int[n];
				x[0] = a1;
				y[0] = b1;

				Point2D.Double previous = (Point2D.Double) current_line.get(0);
				double x1 = previous.getX();
				x1 -= minimum_x;
				x1 /= xrange;
				x1 *= graph_xdim;
				x1 += left_margin;
				x1 += xaddend;

				double y1 = previous.getY();
				y1 -= minimum_y;
				y1 /= yrange;
				y1 *= graph_ydim;
				y1 = graph_ydim - y1;
				y1 += top_margin + 4 * ystep;
				y1 -= yaddend;

				x[1] = (int) x1;
				y[1] = (int) y1;

				double x2 = 0;
				double y2 = 0;
				int m = 2;
				for (int k = 1; k < current_line.size(); k++)
				{
					x1 = previous.getX();
					x1 -= minimum_x;
					x1 /= xrange;
					x1 *= graph_xdim;
					x1 += left_margin;
					x1 += xaddend;

					y1 = previous.getY();
					y1 -= minimum_y;
					y1 /= yrange;
					y1 *= graph_ydim;
					y1 = graph_ydim - y1;
				    y1 += top_margin + 4 * ystep;
					y1 -= yaddend;

					Point2D.Double current = (Point2D.Double) current_line.get(k);

					x2 = current.getX();
					x2 -= minimum_x;
					x2 /= xrange;
					x2 *= graph_xdim;
					x2 += left_margin;
					x2 += xaddend;

					y2 = current.getY();
					y2 -= minimum_y;
					y2 /= yrange;
					y2 *= graph_ydim;
					y2 = graph_ydim - y2;
					y2 += top_margin + 4 * ystep;
					y2 -= yaddend;
					x[m] = (int) x2;
					y[m] = (int) y2;
					m++;
					previous = current;
				}

				x[m] = a2;
				y[m] = b1;
				m++;

				x[m] = a1;
				y[m] = b1;

				java.awt.Polygon sensor_polygon = new Polygon(x, y, n);
				polygon[i] = sensor_polygon;
			}

			for (int i = 4; i >= 0; i--)
			{
				//String zero_crossings = (String) table.getValueAt(i + 1, 7);
				String visibility     = (String) table.getValueAt(i + 1, 8);
				String transparency   = (String) table.getValueAt(i + 1, 9);
				int    xaddend = i * xstep;
				int    yaddend = i * ystep;
				if (visibility.equals("yes"))
				{
					if (transparency.equals("no"))
					{
						g2.setColor(fill_color[i]);
						g2.fillPolygon(polygon[i]);
					}
					g2.setColor(outline_color[i]);
					g2.drawPolygon(polygon[i]);
					
					if(minimum_y < 0)
					{
						ArrayList current_line  = (ArrayList)plot_data.get(i);	
						Point2D.Double first = (Point2D.Double)current_line.get(0);
						size = current_line.size();
						Point2D.Double last = (Point2D.Double)current_line.get(size - 1);
						
						double x1     = first.getX();
						double x2     = last.getX();
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
		        	    zero_y += top_margin + 4 * ystep;
		        	    //zero_y += top_margin;
		        	    zero_y -= yaddend;
		        	    
		        	    float[] dash = { 2f, 0f, 2f };
		        	    BasicStroke basic_stroke = new BasicStroke(1, 
		        	            BasicStroke.CAP_BUTT, 
		        	            BasicStroke.JOIN_ROUND, 
		        	            1.0f, 
		        	            dash,
		        	            2f);
		        	    g2.setStroke(basic_stroke);
		        	    g2.setColor(java.awt.Color.BLACK);
		        	    g2.drawLine((int)x1, (int)zero_y, (int)x2, (int)zero_y);
		        	    g2.setStroke(new BasicStroke(2));
					}
					
					/*
					if(zero_crossings.equals("yes"))
					{
						ArrayList current_line  = (ArrayList)plot_data.get(i);
						Point2D.Double previous = (Point2D.Double)current_line.get(0);
						for(int j = 1; j < current_line.size(); j++)
			            {
							Point2D.Double current = (Point2D.Double)current_line.get(j);
							double x1 = previous.getX();
							double y1 = previous.getY();
							double x2 = current.getX();
							double y2 = current.getY();
							if((y1 < 0 && y2 > 0) || (y1 > 0 && y2 < 0))
							    g2.setColor(java.awt.Color.RED);
							else
								g2.setColor(outline_color[i]);
							
			        	    x1 -= minimum_x;
			        	    x1 /= xrange;
			        	    x1 *= graph_xdim;
			        	    x1 += left_margin;
			        	    x1 += xaddend;
			        	    
			        	    y1 -= minimum_y;  
			        	    y1 /= yrange;
			        	    y1 *= graph_ydim;
			        	    y1 =  graph_ydim - y1;
			        	    y1 += top_margin + 4 * ystep;
			        	    y1 -= yaddend;
			        	    
			        	    x2 -= minimum_x;
			        	    x2 /= xrange;
			        	    x2 *= graph_xdim;
			        	    x2 += left_margin;
			        	    x2 += xaddend;
			        	    
			        	    y2 -= minimum_y;
			        	    y2 /= yrange;
			        	    y2 *= graph_ydim;
		            	    y2 =  graph_ydim - y2;
			        	    y2 += top_margin + 4 * ystep;
			        	    y2 -= yaddend;
			        	    
			        	    g2.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
			        	    previous = current;
			            }
					}
					*/
				}
			}
		}
	}

	class ShiftHandler implements AdjustmentListener
	{
		public void adjustmentValueChanged(AdjustmentEvent event)
		{
			if (slider_changed == false)
			{
				scrollbar_changed = true;
				double range = Double.valueOf((String) table.getValueAt(1, 3));
				double difference = 60 - range;

				int current_value = event.getValue();
				
				shift = current_value;
				shift /= 200;
				shift *= difference;
				double start = 45. - (range / 2);
			
				start += shift;
				String start_string = String.format("%,.2f", start);
				
				for (int i = 1; i < 6; i++)
				{
					table.setValueAt(start_string, i, 2);
				}

				if (event.getValueIsAdjusting() == false)
				{
					current_value = event.getValue();
					shift = current_value;
					shift /= 200;
					shift *= difference;
					start = 45. - (range / 2);
					
					start += shift;
					start_string = String.format("%,.2f", start);
					//shift_string = String.format("%,.2f", shift);
					for (int i = 1; i < 6; i++)
					{
						table.setValueAt(start_string, i, 2);
					}
					
					int line         = Integer.parseInt((String) table.getValueAt(1, 0));
					double min_max[] = getMinMax(line, start, range, data);
					segment_imin     = min_max[0];
					segment_imax     = min_max[1];
					for (int i = 1; i < 6; i++)
					{
					    table.setValueAt(Double.toString(min_max[0]), i, 10);
					    table.setValueAt(Double.toString(min_max[1]), i, 11);
					}
					apply_button.doClick(0);
				}
			}
		}
	}

	class XStepHandler implements AdjustmentListener
	{
		public void adjustmentValueChanged(AdjustmentEvent event)
		{
			int xstep = event.getValue();
			normal_xstep = (double)xstep / 100;
			//System.out.println("Normal xstep is now " + xstep);
			if (event.getValueIsAdjusting() == false)
				apply_button.doClick(0);
		}
	}

	class YStepHandler implements AdjustmentListener
	{
		public void adjustmentValueChanged(AdjustmentEvent event)
		{
			int ystep = 100 - event.getValue();
			normal_ystep = (double)ystep / 100;
			//System.out.println("Normal ystep is now " + normal_ystep);
			if (event.getValueIsAdjusting() == false)
				apply_button.doClick(0);
		}
	}

	class LoadHandler implements ActionListener
	{
		int rows = 5;
		public void actionPerformed(ActionEvent e)
		{
			String current_line = input.getText();
			int current_line_int = Integer.parseInt(current_line);
			System.out.println("Current line is " + current_line);
			for (int i = 0; i < rows; i++)
			{
				table.setValueAt(current_line, i + 1, 0);	
				if(current_line_int % 2 == 1)
					table.setValueAt((String) Integer.toString(i), i + 1, 1);	
				else
					table.setValueAt((String) Integer.toString(4 - i), i + 1, 1);
			}
			double current_start = Double.valueOf((String) table.getValueAt(1, 2));
			double current_range = Double.valueOf((String) table.getValueAt(1, 3));
			double [] min_max = getMinMax(current_line_int, current_start, current_range, data);
			for (int i = 0; i < rows; i++)
			{
				table.setValueAt(Double.toString(min_max[0]), i + 1, 10);	
				table.setValueAt(Double.toString(min_max[1]), i + 1, 11);
			}
			
			min_max = getMinMax(current_line_int, 15, 60, data);
			line_imin = min_max[0];
			line_imax = min_max[1];
		}
	}
	
	class SaveHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			FileDialog file_dialog = new FileDialog(frame, "Save Segment", FileDialog.SAVE);
			file_dialog.setVisible(true);
			String filename = file_dialog.getFile();
			if(filename != "")
			{
				String current_directory = file_dialog.getDirectory();
				System.out.println("Current directory is " + current_directory);
				System.out.println("File name is " + filename);
				int    line  = Integer.parseInt((String) table.getValueAt(1, 0));
				
				int[][] line_array = ObjectMapper.getUnclippedLineArray();
				int start = line_array[line][0];
				int stop = line_array[line][1];
				
				ArrayList sample_list = new ArrayList();
				if (line % 2 == 0)
				{
					for (int j = start; j < stop; j++)
					{
						Sample sample = (Sample) data.get(j);
						sample_list.add(sample);
					}
				} 
				else
				{
					for (int j = stop - 1; j >= start; j--)
					{
						Sample sample = (Sample) data.get(j);
						sample_list.add(sample);
					}
				}
				
				sensor_data.clear();
				for(int i = 0; i < 5; i++)
				{
					ArrayList sensor_list = new ArrayList();

					for (int j = 4 - i; j < sample_list.size(); j += 5)
					{
						Sample sample = (Sample) sample_list.get(j);
						sensor_list.add(sample);
					}
					sensor_data.add(sensor_list);
				}
				
				try(PrintWriter output = new PrintWriter(current_directory + filename))
		        {
					double start_y = Double.valueOf((String) table.getValueAt(1, 2));
					double range   = Double.valueOf((String) table.getValueAt(1, 3));
					double stop_y  = start_y + range;
					
					
				    // Get the min intensity for all sensors.
				    double min_intensity = Double.MAX_VALUE;
				    for(int i = 0; i < 5; i++)
				    {
				    	ArrayList sensor_list = (ArrayList)sensor_data.get(i);
				    	for(int j = 0; j < sensor_list.size(); j++)
				        {
				    		Sample sample = (Sample)sensor_list.get(j);
				            if(sample.y >= start_y && sample.y < stop_y)
				            {
				            	if(sample.intensity < min_intensity)
				            		min_intensity = sample.intensity;
				            }
				        }
				    }
				    String min_intensity_string   = String.format("%.2f", min_intensity);
				    
				    int current_sensor = 0;
				    if(line % 2 == 0)
				        current_sensor = 5;	
				    else
				    	current_sensor = 1;
				    
				    double x_start = line * 2.;
				    for(int i = 0; i < 5; i++)
				    {
				        output.println("#Sensor " + current_sensor + ", Line " + line);
				        ArrayList sensor_list = (ArrayList)sensor_data.get(i);
				        
				        int start_index = 0;
				        int stop_index  = 0;
				        
				        boolean not_started  = true;
				       
				        outer: for(int j = 0; j < sensor_list.size(); j++)
				        {
				        	Sample sample = (Sample)sensor_list.get(j);
				        	if(not_started)
				        	{ 
				        		System.out.println("Got here.");
				        		if(sample.y >= start_y)
				        		{
				        		    start_index = j;
				        		    not_started = false;
				        		}
				        	}
				        
				        	if(sample.y >= stop_y)
				        	{
				                stop_index = j;	
				                break outer;
				        	}  
				        }
				        
				        System.out.println("Start y is " + start_y);
				        System.out.println("Stop y is " + stop_y);

				        System.out.println("Start index is " + start_index);
				        System.out.println("Stop index is " + stop_index);
				        
				        Sample init_sample = (Sample) sensor_list.get(start_index);
				        String xstring          = String.format("%.2f", init_sample.x);	
		      			String ystring          = String.format("%.2f", init_sample.y);
		      			String intensity_string = String.format("%.2f", init_sample.intensity);
		      			//output.println(xstring + " " + ystring + " " + min_intensity_string + " " + (i + 1));
		      			output.println(xstring + " " + ystring + " " + min_intensity_string + " " + x_start);
				        
				        for(int j = start_index; j < stop_index; j++)
				        {
				            Sample sample    = (Sample)sensor_list.get(j);
				            xstring          = String.format("%.2f", sample.x);	
			      			ystring          = String.format("%.2f", sample.y);	
			    			intensity_string = String.format("%.2f", sample.intensity);
				    		output.println(xstring + " " + ystring + " " + intensity_string + " " + x_start);
				        }
				        
				        Sample end_sample = (Sample) sensor_list.get(stop_index - 1);
				        xstring          = String.format("%.2f", end_sample.x);	
		      			ystring          = String.format("%.2f", end_sample.y);
		      			//output.println(xstring + " " + ystring + " " + min_intensity_string + " " + (i + 1));
		      			output.println(xstring + " " + ystring + " " + min_intensity_string + " " + x_start);
		      			xstring          = String.format("%.2f", init_sample.x);	
		      			ystring          = String.format("%.2f", init_sample.y);
		      			
		      			//output.println(xstring + " " + ystring + " " + min_intensity_string + " " + (i + 1));
		      			output.println(xstring + " " + ystring + " " + min_intensity_string + " " + x_start);
				        output.println();
				        output.println();
				        
				        x_start += .5;
				        if(line % 2 == 0)
					        current_sensor--;	
					    else
					    	current_sensor++;		
				    }
				    output.close();
		        }
				catch(Exception e1)
				{
					e1.printStackTrace();      
				}
			}
		}
	}
	
	class ApplyHandler implements ActionListener
	{
		int[][] line_array;

		ApplyHandler()
		{
			line_array = ObjectMapper.getUnclippedLineArray();
		}

		public void actionPerformed(ActionEvent e)
		{
			double current_start, current_range, current_shift;
			int    value, upper_value, difference;
			
			if (slider_changed)
			{
				// Adjust scrollbar.
				current_start = Double.valueOf((String) table.getValueAt(1, 2));
				current_range = Double.valueOf((String) table.getValueAt(1, 3));
				current_shift = shift;
				
				double normalized_start = current_start - 15;
				normalized_start /= 60.;
				double normalized_stop = (current_start + current_range) - 15;
				normalized_stop /= 60.;
				double normalized_center = (normalized_start + normalized_stop) / 2.;
				normalized_center -= .5;
				normalized_center *= 200;

				value = (int) normalized_center;
				scrollbar.setValue(value);
				
				slider_changed = false;
			} 
			else if (scrollbar_changed)
			{
				//Adjust slider;
				current_start = Double.valueOf((String) table.getValueAt(1, 2));
				current_range = Double.valueOf((String) table.getValueAt(1, 3));
				//current_shift = Double.valueOf((String) table.getValueAt(1, 4));
				current_shift = shift;
				
				value = (int) current_start;
				upper_value = (int) (current_start + current_range);
				
				difference = upper_value - value;
				range_slider.setValue(value);
				range_slider.setUpperValue(upper_value);
				
				System.out.println("Range is " + current_range);
				System.out.println("Slider is " + difference);
			    
				scrollbar_changed = false;
				
			}
			
			// We're checking the slider everytime to
			// make it consistent with the table.   Sometimes we end up with
			// an incorrect result when we only do it in the critical section.
			// This seems kludgy.
			current_start = Double.valueOf((String) table.getValueAt(1, 2));
			current_range = Double.valueOf((String) table.getValueAt(1, 3));
			
			current_shift = shift;
			
			value = (int) current_start;
			upper_value = (int) (current_start + current_range);
			
			difference = upper_value - value;
			range_slider.setValue(value);
			range_slider.setUpperValue(upper_value);
			int number_of_rows = table.getRowCount();
			
			
			int baseline       = Integer.parseInt((String) table.getValueAt(1, 0));
			double line_min_max[] = getMinMax(baseline, 15, 75, data);
			double segment_min_max[] = getMinMax(baseline, current_start, current_start + current_range, data);
			
			
			line_imin = line_min_max[0];
			line_imax = line_min_max[1];
			segment_imin = segment_min_max[0];
			segment_imax = segment_min_max[1];
			
			
			
		    // Section to extend dynamic range across flight lines.
			
			for (int i = 2; i < number_of_rows; i++)
			{
				int current_line = Integer.parseInt((String) table.getValueAt(i, 0));
				if(current_line != baseline)
				{
					line_min_max = getMinMax(current_line, 15, 75, data);
					segment_min_max = getMinMax(current_line, current_start, current_start + current_range, data);
					if(line_min_max[0] < line_imin)
						line_imin = line_min_max[0];
					if(line_min_max[1] > line_imax)
						line_imax = line_min_max[1];
					if(segment_min_max[0] < segment_imin)
						segment_imin = segment_min_max[0];
					if(segment_min_max[1] > segment_imax)
						segment_imax = segment_min_max[1];
				}
			}
			
			
			
			for (int i = 1; i < number_of_rows; i++)
			{
				table.setValueAt(Double.toString(segment_imin), i, 10);	
				table.setValueAt(Double.toString(segment_imax), i, 11);
			}
			
			
			line_data.clear();
			for (int i = 1; i < number_of_rows; i++)
			{
				int current_line = Integer.parseInt((String) table.getValueAt(i, 0));
				int start = line_array[current_line][0];
				int stop = line_array[current_line][1];
				ArrayList sample_list = new ArrayList();
				if (current_line % 2 == 0)
				{
					for (int j = start; j < stop; j++)
					{
						Sample sample = (Sample) data.get(j);
						sample_list.add(sample);
					}
				} else
				{
					for (int j = stop - 1; j >= start; j--)
					{
						Sample sample = (Sample) data.get(j);
						sample_list.add(sample);
					}
				}
				line_data.add(sample_list);
			}

			sensor_data.clear();
			for (int i = 1; i < number_of_rows; i++)
			{
				int j = i - 1;
				ArrayList line_list = (ArrayList) line_data.get(j);
				int current_line = Integer.parseInt((String) table.getValueAt(i, 0));
				int current_sensor = Integer.parseInt((String) table.getValueAt(i, 1));
				ArrayList sample_list = new ArrayList();

				if (current_line % 2 == 0)
				{
					for (j = current_sensor; j < line_list.size(); j += 5)
					{
						Sample sample = (Sample) line_list.get(j);
						sample_list.add(sample);
					}
				} 
				else
				{
					for (j = 4 - current_sensor; j < line_list.size(); j += 5)
					{
						Sample sample = (Sample) line_list.get(j);
						sample_list.add(sample);
					}
				}
				sensor_data.add(sample_list);
			}

			System.out.println();
			modified_data.clear();

			double baseline_offset = Double.valueOf((String) table.getValueAt(1, 2));
			double baseline_range  = Double.valueOf((String) table.getValueAt(1, 3));
			double baseline_slope  = 1.;
			try
			{
				baseline_slope = Double.valueOf((String) table.getValueAt(1, 12));
				//System.out.println("Slope table type is string.");
			}
			catch(Exception e1)
			{
				baseline_slope = (double) table.getValueAt(1, 12);
				//System.out.println("Slope table type is double.");
			}
			
			for (int i = 1; i < number_of_rows; i++)
			{
				int j = i - 1;
				ArrayList sensor_list = (ArrayList) sensor_data.get(j);
				double current_offset = Double.valueOf((String) table.getValueAt(i, 2));
				if (current_offset != baseline_offset)
				{
					String new_offset = (String) table.getValueAt(1, 2);
					table.setValueAt(new_offset, i, 2);
					current_offset = baseline_offset;
				}
				current_range = Double.valueOf((String) table.getValueAt(i, 3));
				if (current_range != baseline_range)
				{
					String new_range = (String) table.getValueAt(1, 3);
					table.setValueAt(new_range, i, 3);
					current_range = baseline_range;
				}
				double current_slope = 1.;
				try
				{
					current_slope = Double.valueOf((String) table.getValueAt(i, 12));
					//System.out.println("Slope table type is string.");
				}
				catch(Exception e1)
				{
					current_slope = (double) table.getValueAt(i, 12);
					//System.out.println("Slope table type is double.");
				}
				if (current_slope != baseline_slope)
				{
					String slope_string = Double.toString(baseline_slope);
					table.setValueAt(slope_string, i, 12);
				}
				
				ArrayList sample_list = new ArrayList();
				boolean first_sample = true;
				boolean last_sample = false;
				for (j = 0; j < sensor_list.size(); j++)
				{
					Sample original_sample = (Sample) sensor_list.get(j);
					Sample sample = new Sample();
					sample.intensity = original_sample.intensity;
					sample.x = original_sample.x;
					sample.y = original_sample.y;
					if (sample.y >= current_offset && sample.y < (current_offset + current_range))
					{
						if (first_sample && sample.y > current_offset)
						{
							Sample previous_sample = (Sample) sensor_list.get(j - 1);
							Sample sample2 = new Sample();
							sample2.intensity = original_sample.intensity;
							sample2.x = original_sample.x;
							sample2.y = original_sample.y;
							sample_list.add(sample2);
							first_sample = false;
						}
						sample_list.add(sample);
					}
					if (!last_sample && sample.y >= (current_offset + current_range))
					{
						sample_list.add(sample);
						last_sample = true;
						break;
					}
				}
				modified_data.add(sample_list);
				//System.out.println("The number of samples in the region of interest is " + sample_list.size());
			}

			int baseline_resolution   = Integer.parseInt((String) table.getValueAt(1, 4));
			int baseline_reduction    = Integer.parseInt((String) table.getValueAt(1, 5));
			String baseline_autoscale = (String) table.getValueAt(1, 7);
			double baseline_imin      = Double.valueOf((String) table.getValueAt(1, 10));
			double baseline_imax      = Double.valueOf((String) table.getValueAt(1, 11));
			
			
			interpolated_data.clear();
			int number_of_samples, number_of_samples_used;
			for (int i = 1; i < number_of_rows; i++)
			{
				int j = i - 1;

				ArrayList data_list = (ArrayList) modified_data.get(j);
				number_of_samples = data_list.size();
				boolean[] used_sample = new boolean[number_of_samples];
				for (j = 0; j < number_of_samples; j++)
					used_sample[j] = false;
				
				// Better to always use the baseline value.
				double current_offset = Double.valueOf((String) table.getValueAt(i, 2));
				current_range = Double.valueOf((String) table.getValueAt(i, 3));

				int current_resolution = Integer.parseInt((String) table.getValueAt(i, 4));
				if (current_resolution != baseline_resolution)
				{
					String new_resolution = (String) table.getValueAt(1, 4);
					table.setValueAt(new_resolution, i, 4);
					current_resolution = baseline_resolution;
				}
				
				String current_autoscale = (String) table.getValueAt(i, 7);
				if(!current_autoscale.equals(baseline_autoscale))
				{
					table.setValueAt(baseline_autoscale, i, 7);	
				}
				
                double current_slope = 1.;
				
				try
				{
					current_slope = Double.valueOf((String) table.getValueAt(i, 12));
					//System.out.println("Slope table type is string.");
				}
				catch(Exception e1)
				{
					current_slope = (double) table.getValueAt(i, 12);
					//System.out.println("Slope table type is double.");
				}
				
				if (current_slope != baseline_slope)
				{
					String slope_string = Double.toString(baseline_slope);
					table.setValueAt(slope_string, i, 12);
					current_resolution = baseline_resolution;
				}
				
				double current_imin = Double.valueOf((String) table.getValueAt(i, 10));
				if (current_imin != baseline_imin)
				{
					String imin_string = Double.toString(baseline_imin);
					table.setValueAt(imin_string, i, 10);
					current_imin = baseline_imin;
				}
				
				double current_imax = Double.valueOf((String) table.getValueAt(i, 11));
				if (current_imax != baseline_imax)
				{
					String imax_string = Double.toString(baseline_imax);
					table.setValueAt(imax_string, i, 11);
					current_imax = baseline_imax;
				}
				
				ArrayList sample_list = new ArrayList();
				double increment = current_range / current_resolution;
				double current_y = current_offset;
				Sample init_sample = (Sample) data_list.get(0);
				int index = 0;
				if (init_sample.y < current_y)
				{
					// Could use x position as well, but keeping it simple.
					Sample next_sample = (Sample) data_list.get(1);
					double distance1 = Math.abs(current_y - init_sample.y);
					double distance2 = Math.abs(current_y - next_sample.y);
					double total_distance = distance1 + distance2;
					Sample sample = new Sample();
					sample.intensity = init_sample.intensity * (distance2 / total_distance);
					sample.intensity += next_sample.intensity * (distance1 / total_distance);
					
					if(sample.intensity < baseline_imin)
						sample.intensity = baseline_imin;
					else if(sample.intensity > baseline_imax)
						sample.intensity = baseline_imax;
					/*
					if(baseline_slope != 1)
					{
						if(sample.intensity < 0)
						{
						    double factor = Math.abs(baseline_imin);
						    double absolute_value  = Math.abs(sample.intensity);
						    sample.intensity = factor * Math.pow(absolute_value / factor, 1 / baseline_slope);
						    if(sample.intensity > 0)
						    {
						        sample.intensity = -sample.intensity;
						    }
						    else
						    	System.out.println("Slope function produced negative value from positive value.");
						}
						else if(sample.intensity > 0)
						{
							double factor = baseline_imax;
							double absolute_value  = sample.intensity;
							sample.intensity = factor * Math.pow(absolute_value / factor, 1 / baseline_slope);
						}	
					}
					*/
					
					/*
					double factor = 0;
					double absolute_value = 0;
					if(sample.intensity < -1 || sample.intensity > 1)
					{
						if(sample.intensity < 0)
						{
							factor = Math.abs(baseline_imin);
							absolute_value  = Math.abs(sample.intensity);
							sample.intensity = factor * Math.pow(absolute_value / factor, 1 / baseline_slope);
							sample.intensity = -sample.intensity;
						}
						else
						{
							factor = baseline_imax;
							absolute_value = sample.intensity;
							sample.intensity = factor * Math.pow(absolute_value / factor, 1 / baseline_slope);
						}
					}
					*/
					
					sample.intensity = sample.intensity * baseline_slope;
					
					if(sample.intensity < baseline_imin)
					{
						sample.intensity = baseline_imin;
					}
					else if(sample.intensity > baseline_imax)
					{
						sample.intensity = baseline_imax;	
					}
					
					sample.x = init_sample.x * (distance2 / total_distance);
					sample.x += next_sample.x * (distance1 / total_distance);
					sample.y = init_sample.y * (distance2 / total_distance);
					sample.y += next_sample.y * (distance1 / total_distance);
					sample_list.add(sample);
					index = 2;
					used_sample[0] = true;
					used_sample[1] = true;
				} 
				else // init sample y exactly equals offset
				{
					Sample sample = new Sample();
					sample.x = init_sample.x;
					sample.y = init_sample.y;
					sample.intensity = init_sample.intensity;
					if(sample.intensity < baseline_imin)
						sample.intensity = baseline_imin;
					else if(sample.intensity > baseline_imax)
						sample.intensity = baseline_imax;
					if(baseline_slope != 1)
					{
						/*
						if(sample.intensity < 0)
						{
						    double factor = Math.abs(baseline_imin);
						    double absolute_value  = Math.abs(sample.intensity);
						    sample.intensity = factor * Math.pow(absolute_value / factor, 1 / baseline_slope);
						    sample.intensity = -sample.intensity;
						}
						else if(sample.intensity > 0)
						{
							double factor = baseline_imax;
							double absolute_value  = sample.intensity;
							sample.intensity = factor * Math.pow(absolute_value / factor, 1 / baseline_slope);
						}
						*/
						sample.intensity = sample.intensity * baseline_slope;
						
						if(sample.intensity < baseline_imin)
						{
							sample.intensity = baseline_imin;
						}
						else if(sample.intensity > baseline_imax)
						{
							sample.intensity = baseline_imax;	
						}
					}
					
					sample_list.add(sample);
					index = 1;
					used_sample[0] = true;
				}
				current_y += increment;
				Sample sample = (Sample) data_list.get(index);
				index++;

				for (j = 1; j < current_resolution; j++)
				{
					while (sample.y < current_y)
						sample = (Sample) data_list.get(index++);
					if (sample.y > current_y)
					{
						used_sample[index - 1] = true;
						used_sample[index - 2] = true;
						Sample previous_sample = (Sample) data_list.get(index - 2);
						double distance1 = Math.abs(current_y - previous_sample.y);
						double distance2 = Math.abs(current_y - sample.y);
						double total_distance = distance1 + distance2;
						Sample new_sample = new Sample();
						new_sample.intensity = previous_sample.intensity * (distance2 / total_distance);
						new_sample.intensity += sample.intensity * (distance1 / total_distance);
						
						if(new_sample.intensity < baseline_imin)
							new_sample.intensity = baseline_imin;
						else if(new_sample.intensity > baseline_imax)
							new_sample.intensity = baseline_imax;
						if(baseline_slope != 1)
						{
							/*
							if(new_sample.intensity < 0)
							{
							    double factor = Math.abs(baseline_imin);
							    double absolute_value  = Math.abs(sample.intensity);
							    new_sample.intensity = factor * Math.pow(absolute_value / factor, 1 / baseline_slope);
							    new_sample.intensity = -sample.intensity;
							}
							else if(sample.intensity > 0)
							{
								double factor = baseline_imax;
								double absolute_value  = sample.intensity;
								new_sample.intensity = factor * Math.pow(absolute_value / factor, 1 / baseline_slope);
							}
							*/
							new_sample.intensity = new_sample.intensity * baseline_slope;
							
							if(new_sample.intensity < baseline_imin)
							{
								new_sample.intensity = baseline_imin;
							}
							else if(new_sample.intensity > baseline_imax)
							{
								new_sample.intensity = baseline_imax;	
							}
						}
						
						new_sample.x = previous_sample.x * (distance2 / total_distance);
						new_sample.x += sample.x * (distance1 / total_distance);
						new_sample.y = previous_sample.y * (distance2 / total_distance);
						new_sample.y += sample.y * (distance1 / total_distance);
						
						sample_list.add(new_sample);
					} 
					else // sample.y == current_y
					{
						Sample new_sample = new Sample();
						new_sample.intensity = sample.intensity;
						if(new_sample.intensity < baseline_imin)
							new_sample.intensity = baseline_imin;
						else if(new_sample.intensity > baseline_imax)
							new_sample.intensity = baseline_imax;
						
						if(baseline_slope != 1)
						{
							/*
							if(new_sample.intensity < 0)
							{
							    double factor = Math.abs(baseline_imin);
							    double absolute_value  = Math.abs(sample.intensity);
							    new_sample.intensity = factor * Math.pow(absolute_value / factor, 1 / baseline_slope);
							    new_sample.intensity = -sample.intensity;
							}
							else if(sample.intensity > 0)
							{
								double factor = baseline_imax;
								double absolute_value  = sample.intensity;
								new_sample.intensity = factor * Math.pow(absolute_value / factor, 1 / baseline_slope);
							}
							*/
							new_sample.intensity = new_sample.intensity * baseline_slope;
							if(new_sample.intensity < baseline_imin)
							{
								new_sample.intensity = baseline_imin;
							}
							else if(new_sample.intensity > baseline_imax)
							{
								new_sample.intensity = baseline_imax;	
							}
						}
						
						new_sample.y = sample.y;
						new_sample.x = sample.x;
						sample_list.add(new_sample);
						used_sample[index - 1] = true;
					}
					current_y += increment;
				}

				number_of_samples_used = 0;
				for (j = 0; j < number_of_samples; j++)
				{
					if (used_sample[j] == true)
						number_of_samples_used++;
				}

				//System.out.println("Number of samples used was " + number_of_samples_used);
				double sample_ratio = number_of_samples_used;
				sample_ratio /= number_of_samples;
				String ratio_string = String.format("%,.2f", sample_ratio);
				table.setValueAt(ratio_string, i, 6);
				interpolated_data.add(sample_list);
			}

			reduced_data.clear();
			plot_data.clear();

			for (int i = 1; i < number_of_rows; i++)
			{
				int current_resolution = Integer.parseInt((String) table.getValueAt(i, 4));
				int current_reduction = Integer.parseInt((String) table.getValueAt(i, 5));
				if (current_reduction != baseline_reduction)
				{
					String new_reduction = (String) table.getValueAt(1, 5);
					table.setValueAt(new_reduction, i, 5);
					current_reduction = baseline_reduction;
				}
				int j = i - 1;
				ArrayList sample_list = (ArrayList) interpolated_data.get(j);
				ArrayList plot_list = new ArrayList();
				if (current_reduction == 0)
				{
					for (j = 0; j < sample_list.size(); j++)
					{
						Sample sample = (Sample) sample_list.get(j);
						Point2D.Double point = new Point2D.Double();
						point.x = sample.y;
						point.y = sample.intensity;
						plot_list.add(point);
					}
					plot_data.add(plot_list);
				} 
				else // Reducing data.
				{
					int size = sample_list.size();
					double[] x = new double[size];
					double[] y = new double[size];
					double[] intensity = new double[size];
					for (j = 0; j < size; j++)
					{
						Sample sample = (Sample) sample_list.get(j);
						x[j] = sample.x;
						y[j] = sample.y;
						intensity[j] = sample.intensity;
					}

					double[] reduced_x = reduce(x, current_reduction);
					double[] reduced_y = reduce(y, current_reduction);
					double[] reduced_intensity = reduce(intensity, current_reduction);

					size = current_resolution - current_reduction;
					ArrayList reduced_list = new ArrayList();
					for (j = 0; j < size; j++)
					{
						Sample sample = new Sample();
						sample.x = reduced_x[j];
						sample.y = reduced_y[j];
						sample.intensity = reduced_intensity[j];
						reduced_list.add(sample);
					}

					for (j = 0; j < size; j++)
					{
						Point2D.Double point = new Point2D.Double();
						point.x = reduced_y[j];
						point.y = reduced_intensity[j];
						plot_list.add(point);
					}
					plot_data.add(plot_list);
				}
			}
			canvas.repaint();
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
