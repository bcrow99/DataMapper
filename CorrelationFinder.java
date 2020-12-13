import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class CorrelationFinder
{
	ArrayList line          = new ArrayList();
	ArrayList sensor        = new ArrayList();
	ArrayList offset        = new ArrayList();
	ArrayList range         = new ArrayList();
	ArrayList xshift        = new ArrayList();
	ArrayList yshift        = new ArrayList();
	ArrayList resolution    = new ArrayList();
	ArrayList reduction     = new ArrayList();
	ArrayList samples       = new ArrayList();
	ArrayList delta_impulse = new ArrayList();
	ArrayList delta_delta   = new ArrayList();
	
	ArrayList data          = new ArrayList();
	
	private JFrame frame;
	public  JTable table;
	public  LineCanvas canvas;

	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					CorrelationFinder window = new CorrelationFinder();
					window.frame.setVisible(true);
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	public CorrelationFinder()
	{
		int    _line, _sensor, _resolution, _reduction; 
		double _offset, _range, _xshift, _yshift, _samples, _delta_impulse, _delta_delta;
		
		_line = 0;
		line.add(_line);
		_line = 1;
		line.add(_line);
		
		_sensor = 4;
		sensor.add(_sensor);
		sensor.add(_sensor);
		
		_offset = 55.;
		offset.add(_offset);
		offset.add(_offset);
		
		_range = 15.;
		range.add(_range);
		range.add(_range);
		
		_xshift = 0.;
		xshift.add(_xshift);
		xshift.add(_xshift);
		
		_yshift = 0.;
		yshift.add(_yshift);
		_yshift = -0.8;
		yshift.add(_yshift);
		
		_resolution = 100;
		resolution.add(_resolution);
		resolution.add(_resolution);
		
		_reduction = 2;
		reduction.add(_reduction);
		reduction.add(_reduction);
		
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		canvas = new LineCanvas();
		canvas.setSize(400, 350);
		frame.getContentPane().add(canvas, BorderLayout.CENTER);
		
		table = new JTable(3, 11);
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
		header = new String("Offset");
		table.setValueAt(header, 0, 2);
		header = new String("Range");
		table.setValueAt(header, 0, 3);
		header = new String("Xshift");
		table.setValueAt(header, 0, 4);
		header = new String("Yshift");
		table.setValueAt(header, 0, 5);
		header = new String("Resolution");
		table.setValueAt(header, 0, 6);
		header = new String("Reduction");
		table.setValueAt(header, 0, 7);
		header = new String("Samples");
		table.setValueAt(header, 0, 8);
		header = new String("Delta(i)");
		table.setValueAt(header, 0, 9);
		header = new String("Delta(d)");
		table.setValueAt(header, 0, 10);
		
		int rows = 2;
		
		for(int i = 0; i < rows; i++)
		{
			table.setValueAt((int)line.get(i), i + 1, 0);
			table.setValueAt((int)sensor.get(i), i + 1, 1);
			table.setValueAt((double)offset.get(i), i + 1, 2);
			table.setValueAt((double)range.get(i), i + 1, 3);
			table.setValueAt((double)xshift.get(i), i + 1, 4);
			table.setValueAt((double)yshift.get(i), i + 1, 5);
			table.setValueAt((int)resolution.get(i), i + 1, 6);
			table.setValueAt((int)reduction.get(i), i + 1, 7);
		}
		
		JPanel bottom_panel = new JPanel(new BorderLayout());
		bottom_panel.add(table, BorderLayout.CENTER);
		JButton apply_button = new JButton();
		apply_button.setLabel("Apply params.");
        ApplyButtonHandler handler = new ApplyButtonHandler();
        apply_button.addActionListener(handler);
		bottom_panel.add(apply_button, BorderLayout.EAST);
	
		frame.getContentPane().add(bottom_panel, BorderLayout.SOUTH);
		
		frame.pack();
	}

	class LineCanvas extends Canvas
    {
		public void paint(Graphics g) 
        {
    		Rectangle visible_area = g.getClipBounds();
        	int clipped_xdim = (int) visible_area.getWidth();
    		int clipped_ydim = (int) visible_area.getHeight();
    		Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(1));
            g2.setColor(java.awt.Color.BLACK);
            g2.drawLine(1, 1, 1, clipped_ydim - 2);
            g2.drawLine(1, 1, clipped_xdim - 2, 1);
            g2.drawLine(1, clipped_ydim - 2, clipped_xdim - 2, clipped_ydim - 2);
            g2.drawLine(clipped_xdim - 2, clipped_ydim - 2, clipped_xdim - 2, 1);   
        }
    }
	
	class ApplyButtonHandler implements ActionListener
    {
		public void actionPerformed(ActionEvent e)
        {
			System.out.println("Got here.");
			canvas.repaint();
        }
    }    
}
