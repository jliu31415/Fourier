import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class Fourier extends JPanel{
	private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int FREQUENCY = 20;
    private static final int TRACE_SIZE = 250;
    private static JFrame frame;
    private static Fourier panel;
    private static LinkedList<Point> outline;
    private static LinkedList<Point2D.Double> fourierOutline, coefficients;	//Fourier transform requires higher precision
	private static double realTime, adjustedTime;
    private static double speedFactor = 1;
	
	public Fourier() {
		this.setBackground(Color.black);
        outline = new LinkedList<Point>();
        fourierOutline = new LinkedList<Point2D.Double>();
        coefficients = new LinkedList<Point2D.Double>();
	}
	
	public static void main(String args[]) {
		frame = new JFrame();
		panel = new Fourier();
        DrawListener drawListener = new DrawListener();
        ReleaseListener releaseListener = new ReleaseListener();
        frame.add(panel);
        frame.addMouseMotionListener(drawListener);
        frame.addMouseListener(releaseListener);
        initialize(frame);   //initialize frame
	}
	
	public static void initialize(JFrame frame){
        frame.setTitle("Fourier Series");
        frame.setSize(WIDTH, HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
	
	public void paintComponent(Graphics g){
        Graphics2D gg = (Graphics2D) g; //cast to 2D graphics to handle doubles

        //Enable anti-aliasing and pure stroke
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gg.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        super.paintComponent(gg);
        
        if(coefficients.size() == 2 * FREQUENCY + 1) {
        	Point2D.Double previous = new Point2D.Double();
        	Point2D.Double next = new Point2D.Double();
        	Color traceColor = Color.gray;
        	adjustedTime += speedFactor * (System.currentTimeMillis() / 1000.0 - realTime);
        	realTime = System.currentTimeMillis() / 1000.0;
        	int index = 0;
        	
        	for(int f = 0; f <= FREQUENCY; f++) {
    			do {
    				next = multiply(coefficients.get(index++), 2 * Math.PI * f * adjustedTime);
    				double radius = Math.hypot(next.x, next.y);	//compute radius before translation
    				next.x += previous.x;
    				next.y += previous.y;
    				if(f != 0) {
    					if(Math.abs(f) < 2) gg.setPaint(traceColor);
    					traceColor = traceColor.darker();
    					gg.draw(new Line2D.Double(previous.x, previous.y, next.x, next.y));
    					gg.draw(new Ellipse2D.Double(previous.x - radius, previous.y - radius, 2 * radius, 2 * radius));
    				}
    				previous.setLocation(next);
    			} while((f *= -1) < 0);	//repeat for negative frequencies
    		}
        	
        	fourierOutline.add(new Point2D.Double(next.x, next.y));
        	if(fourierOutline.size() > TRACE_SIZE) fourierOutline.remove();
        }
        
        gg.setColor(Color.blue);
        for(int i = outline.size() - 1; i > 0; i--) {
        	gg.drawLine(outline.get(i - 1).x, outline.get(i - 1).y,
        			outline.get(i).x, outline.get(i).y);
        }
        
        Color fourierTraceColor = Color.cyan;
        for(int i = fourierOutline.size() - 1; i > 0; i--) {
        	gg.setColor(fourierTraceColor);
        	if(i % 50 == 0) fourierTraceColor = fourierTraceColor.darker();
        	gg.draw(new Line2D.Double(fourierOutline.get(i - 1).x, fourierOutline.get(i - 1).y,
        			fourierOutline.get(i).x, fourierOutline.get(i).y));
        }

        frame.repaint();
    }
	
	public void transform() {
		realTime = System.currentTimeMillis() / 1000.0;
		adjustedTime = 0;
		Point integral = new Point();
		for(int f = 0; f <= FREQUENCY; f++) {
			integral.setLocation(0, 0);
			do {
				for(int j = 0; j < outline.size(); j++) {
					Point2D.Double outlinePoint = new Point2D.Double(outline.get(j).getX(), outline.get(j).y);
					Point2D.Double riemann = multiply(outlinePoint, -2 * Math.PI * f * j / (double) outline.size());
					integral.x += riemann.x;
					integral.y += riemann.y;
				}
				integral.x /= outline.size();
				integral.y /= outline.size();
				coefficients.add(new Point2D.Double(integral.x, integral.y));
			} while((f *= -1) < 0);	//repeat for negative frequencies
		}
	}
	
	public Point2D.Double multiply(Point2D.Double p, double power) {
		//return product of Point p and e^i*power
		return new Point2D.Double(p.x * Math.cos(power) - p.y * Math.sin(power), 
				p.x * Math.sin(power) + p.y * Math.cos(power));
	}
	
	public static class DrawListener implements MouseMotionListener{

		@Override
		public void mouseDragged(MouseEvent e) {
			Point cursor = e.getPoint();
			if(!cursor.equals(outline.peekLast())) {
				outline.add(cursor);
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			speedFactor = 2 * ((double) e.getPoint().x / WIDTH);
		}
	}
	
	public static class ReleaseListener implements MouseListener{

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			outline.clear();
			fourierOutline.clear();
			coefficients.clear();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if(outline.size() > 0)
				panel.transform();
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
