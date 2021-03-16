import java.awt.*;
import java.awt.Color.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
	double max_delta;
	//double shift, start, range;

	boolean slider_changed = false;
	boolean scrollbar_changed = false;

	// Plotting parameters
	Color[] outline_color = new Color[5];
	Color[] fill_color = new Color[5];
	int number_of_sensors = 5;

	int bottom_margin = 60;
	int left_margin = 60;
	int top_margin = 10;
	int right_margin = 10;
	int xstep = 10;
	int ystep = 20;
	int xdim = 0;
	int ydim = 0;
	int graph_xdim = 0;
	int graph_ydim = 0;

	// Interface componants.
	private JFrame frame;
	public JTable table;
	public LineCanvas canvas;
	public JScrollBar scrollbar;
	public JButton apply_button;
	public RangeSlider range_slider;
	public JTextField input;
	
	public static void main(String[] args)
	{
		String prefix = new String("C:/Users/Brian Crowley/Desktop/");
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
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public FencePlotter(String filename)
	{
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

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		canvas = new LineCanvas();
		canvas.setSize(1000, 1000);

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

		table = new JTable(6, 11)
		{
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
			{
				Component c = super.prepareRenderer(renderer, row, column);
				if (column == 10 && row > 0)
					c.setBackground(outline_color[row - 1]);
				else
					c.setBackground(java.awt.Color.WHITE);
				return c;
			}
		};

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);

		for (int column = 0; column < 11; column++)
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
		header = new String("Shift");
		table.setValueAt(header, 0, 4);
		header = new String("Resolution");
		table.setValueAt(header, 0, 5);
		header = new String("Smoothing");
		table.setValueAt(header, 0, 6);
		header = new String("Data Usage");
		table.setValueAt(header, 0, 7);
		header = new String("Visible");
		table.setValueAt(header, 0, 8);
		header = new String("Transparent");
		table.setValueAt(header, 0, 9);
		header = new String("Key");
		table.setValueAt(header, 0, 10);

		int     rows  = 5;
		int     line  = 4;
		double start  = 32;
		double range  = 6;
		
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
			table.setValueAt((String) "0", i + 1, 4);
			table.setValueAt((String) "100", i + 1, 5);
			table.setValueAt((String) "0", i + 1, 6);
			table.setValueAt((String) "0", i + 1, 7);
			table.setValueAt((String) "yes", i + 1, 8);
			table.setValueAt((String) "no", i + 1, 9);
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
						double shift = start - (45. - (range / 2));
						String start_string = String.format("%,.2f", start);
						String range_string = String.format("%,.2f", range);
						String shift_string = String.format("%,.2f", shift);
						for (int i = 1; i < 6; i++)
						{
							table.setValueAt(start_string, i, 2);
							table.setValueAt(range_string, i, 3);
							table.setValueAt(shift_string, i, 4);
						}
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
		
		
		JPanel load_panel   = new JPanel(new BorderLayout());
		JButton load_button = new JButton("Load");
		LoadHandler load_handler = new LoadHandler();
		load_button.addActionListener(load_handler);
		input.setHorizontalAlignment(JTextField.CENTER);
		load_panel.add(load_button, BorderLayout.SOUTH);
		load_panel.add(input, BorderLayout.NORTH);

		JPanel canvas_panel = new JPanel(new BorderLayout());
		Canvas apply_canvas = new Canvas();
		apply_canvas.setSize(40, 40);

		JScrollBar xstep_scrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, 0, 190);
		XStepHandler xstep_handler = new XStepHandler();
		xstep_scrollbar.addAdjustmentListener(xstep_handler);
		xstep_scrollbar.setValue(0);
		xstep = 0;

		JScrollBar ystep_scrollbar = new JScrollBar(JScrollBar.VERTICAL, 0, 1, 0, 190);
		YStepHandler ystep_handler = new YStepHandler();
		ystep_scrollbar.addAdjustmentListener(ystep_handler);
		ystep_scrollbar.setValue(189);
		ystep = 0;

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

			graph_xdim = xdim - (left_margin + right_margin) - (number_of_sensors - 1) * xstep;
			graph_ydim = ydim - (left_margin + right_margin) - (number_of_sensors - 1) * ystep;

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
					} else if (point.x > maximum_x)
					{
						maximum_x = point.x;
					}
					if (point.y < minimum_y)
					{
						minimum_y = point.y;
					} else if (point.y > maximum_y)
					{
						maximum_y = point.y;
					}
				}
			}
			xrange = maximum_x - minimum_x;

			// Set labels.
			double start = Double.valueOf((String) table.getValueAt(1, 2));
			double current_range = Double.valueOf((String) table.getValueAt(1, 3));
			double shift = Double.valueOf((String) table.getValueAt(1, 4));
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

			// Construct polygons in second pass.
			Polygon[] polygon = new Polygon[5];
			for (int i = 0; i < 5; i++)
			{
				// int j = order[i];

				int a1 = left_margin;
				int b1 = ydim - bottom_margin;

				int a2 = a1 + graph_xdim;
				int b2 = b1 - graph_ydim;

				// int xaddend = j * xstep;
				int xaddend = i * xstep;
				// int yaddend = j * ystep;
				int yaddend = i * ystep;
				a1 += xaddend;
				b1 -= yaddend;

				a2 += xaddend;
				b2 -= yaddend;

				// ArrayList current_line = (ArrayList) plot_data.get(j);
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
				String visibility = (String) table.getValueAt(i + 1, 8);
				String transparency = (String) table.getValueAt(i + 1, 9);
				if (visibility.equals("yes"))
				{
					if (transparency.equals("no"))
					{
						g2.setColor(fill_color[i]);
						g2.fillPolygon(polygon[i]);
					}
					g2.setColor(outline_color[i]);
					g2.drawPolygon(polygon[i]);

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
				double shift = current_value;
				shift /= 200;
				shift *= difference;
				double start = 45. - (range / 2);
			
				start += shift;
				String start_string = String.format("%,.2f", start);
				String shift_string = String.format("%,.2f", shift);
				for (int i = 1; i < 6; i++)
				{
					table.setValueAt(start_string, i, 2);
					table.setValueAt(shift_string, i, 4);
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
					shift_string = String.format("%,.2f", shift);
					for (int i = 1; i < 6; i++)
					{
						table.setValueAt(start_string, i, 2);
						table.setValueAt(shift_string, i, 4);
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
			xstep = event.getValue();
			System.out.println("Xstep is now " + xstep);
			if (event.getValueIsAdjusting() == false)
				apply_button.doClick(0);
		}
	}

	class YStepHandler implements AdjustmentListener
	{
		public void adjustmentValueChanged(AdjustmentEvent event)
		{
			ystep = 189 - event.getValue();
			System.out.println("Ystep is now " + ystep);
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
				current_shift = Double.valueOf((String) table.getValueAt(1, 4));
				
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
				current_shift = Double.valueOf((String) table.getValueAt(1, 4));
				
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
			// an incorrect result when only do it in the critical section.
			// This seems kludgy.
			current_start = Double.valueOf((String) table.getValueAt(1, 2));
			current_range = Double.valueOf((String) table.getValueAt(1, 3));
			current_shift = Double.valueOf((String) table.getValueAt(1, 4));
			
			value = (int) current_start;
			upper_value = (int) (current_start + current_range);
			
			difference = upper_value - value;
			range_slider.setValue(value);
			range_slider.setUpperValue(upper_value);
			
			int number_of_rows = table.getRowCount();
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
				} else
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
			double baseline_range = Double.valueOf((String) table.getValueAt(1, 3));

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
				double current_yshift = Double.valueOf((String) table.getValueAt(i, 4));

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
				System.out.println("The number of samples in the region of interest is " + sample_list.size());
			}

			int baseline_resolution = Integer.parseInt((String) table.getValueAt(1, 5));
			int baseline_reduction = Integer.parseInt((String) table.getValueAt(1, 6));
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
				double current_offset = Double.valueOf((String) table.getValueAt(i, 2));
				current_range = Double.valueOf((String) table.getValueAt(i, 3));

				int current_resolution = Integer.parseInt((String) table.getValueAt(i, 5));
				if (current_resolution != baseline_resolution)
				{
					String new_resolution = (String) table.getValueAt(1, 5);
					table.setValueAt(new_resolution, i, 5);
					current_resolution = baseline_resolution;
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
					sample.x = init_sample.x * (distance2 / total_distance);
					sample.x += next_sample.x * (distance1 / total_distance);
					sample.y = init_sample.y * (distance2 / total_distance);
					sample.y += next_sample.y * (distance1 / total_distance);
					sample_list.add(sample);
					index = 2;
					used_sample[0] = true;
					used_sample[1] = true;
				} else // init sample y exactly equals offset
				{
					Sample sample = new Sample();
					sample.x = init_sample.x;
					sample.y = init_sample.y;
					sample.intensity = init_sample.intensity;
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
						new_sample.x = previous_sample.x * (distance2 / total_distance);
						new_sample.x += sample.x * (distance1 / total_distance);
						new_sample.y = previous_sample.y * (distance2 / total_distance);
						new_sample.y += sample.y * (distance1 / total_distance);
						sample_list.add(new_sample);
					} else // sample.y == current_y
					{
						Sample new_sample = new Sample();
						new_sample.intensity = sample.intensity;
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

				System.out.println("Number of samples used was " + number_of_samples_used);
				double sample_ratio = number_of_samples_used;
				sample_ratio /= number_of_samples;
				String ratio_string = String.format("%,.2f", sample_ratio);
				table.setValueAt(ratio_string, i, 7);
				interpolated_data.add(sample_list);
			}

			reduced_data.clear();
			plot_data.clear();

			for (int i = 1; i < number_of_rows; i++)
			{
				int current_resolution = Integer.parseInt((String) table.getValueAt(i, 5));
				int current_reduction = Integer.parseInt((String) table.getValueAt(i, 6));
				if (current_reduction != baseline_reduction)
				{
					String new_reduction = (String) table.getValueAt(1, 6);
					table.setValueAt(new_reduction, i, 6);
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
				} else // Reducing data.
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
						sample.y = reduced_x[j];
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
