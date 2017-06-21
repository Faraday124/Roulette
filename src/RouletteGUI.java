import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class RouletteGUI extends JFrame {

	private static final int BOARD_SIZE = 600;
	ScheduledThreadPoolExecutor executor;

	public static void main(String[] args) {
		new RouletteGUI();
	}

	public RouletteGUI() {

		this.setSize(BOARD_SIZE, BOARD_SIZE);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setTitle("Roulette");
		this.setBackground(Color.BLACK);
		this.setResizable(false);

		RouletteDisk disk = new RouletteDisk();
		JPanel panel = new JPanel();
		JLabel currentNumber = new JLabel(Double.toString(disk.getAngle()));
		currentNumber.setHorizontalAlignment(SwingConstants.CENTER);
		currentNumber.setVerticalAlignment(SwingConstants.TOP);
		Font numberFont = new Font("Serif", Font.BOLD, 36);
		currentNumber.setFont(numberFont);
		panel.setLayout(new BorderLayout());
		panel.add(disk, BorderLayout.CENTER);
		this.add(panel);

		executor = new ScheduledThreadPoolExecutor(5);
		executor.scheduleAtFixedRate(new Rotate(this), 0L, 20L,
				TimeUnit.MILLISECONDS);
		
		this.setFocusable(true);
		this.requestFocusInWindow();
		this.addKeyListener(disk.getKeyListener());
		this.setVisible(true);

	}

	private class RouletteDisk extends JComponent {
		private BufferedImage image;
		private double angle;

		private int locationX;
		private int locationY;
		private int[] rouletteNumbers = new int[] { 0, 26, 3, 35, 12, 28, 7,
				29, 18, 22, 9, 31, 14, 20, 1, 33, 16, 24, 5, 10, 23, 8, 30, 11,
				36, 13, 27, 6, 34, 17, 25, 2, 21, 4, 19, 15, 32 };

		public RouletteDisk() {
			setOpaque(true);
			try {
				image = ImageIO.read(getClass().getResource(
						"/image/roulette.png"));

				locationX = (BOARD_SIZE - image.getWidth()) / 2;
				locationY = (BOARD_SIZE - image.getHeight()) / 2;
			} catch (IOException ioe) {
				System.out.println("Unable to fetch image.");
				ioe.printStackTrace();
			}

			this.addMouseListener(new DiskMouseListener());	
		
		}
		
		public DiskKeyListener getKeyListener(){
			return new DiskKeyListener();
		}

		@Override
		public Dimension getPreferredSize() {

			return new Dimension(image.getWidth(), image.getHeight());
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D graphics = (Graphics2D) g;
			setGraphicsConfig(graphics);			
			drawCurrentNumber(g, graphics);
			drawDisk(graphics);
			drawInformationSubtitle(g, graphics);		
		}

		private void drawInformationSubtitle(Graphics g, Graphics2D graphics) {
			Font subtitleFont = new Font("Verdana", Font.ITALIC, 17);
			g.setColor(Color.WHITE);
			g.setFont(subtitleFont);
			
			String information = "Use your arrows <- -> to rotate"; 
			graphics.drawChars(information.toCharArray(), 0, information.length() ,160 , 500);
			
			information = "Use your mouse to spin the disk";
			graphics.drawChars(information.toCharArray(), 0, information.length() ,160 , 550);
		}

		private void drawDisk(Graphics2D graphics) {
			AffineTransform at = new AffineTransform();
			at.translate(getWidth() / 2, getHeight() / 2);
			at.rotate(Math.toRadians(getAngle()));
			at.translate(-image.getWidth() / 2, -image.getHeight() / 2);
			graphics.drawImage(image, at, null);
		}

		private void setGraphicsConfig(Graphics2D graphics) {
			graphics.setComposite(AlphaComposite.Src);
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		}

		private char[] drawCurrentNumber(Graphics g, Graphics2D graphics) {
			Font numberFont = new Font("Serif", Font.BOLD, 60);
			g.setColor(Color.ORANGE);
			g.setFont(numberFont);
			char[] currentNumber = getNumberFromAngle(getAngle()).toCharArray();
			graphics.drawChars(currentNumber, 0, currentNumber.length,
					BOARD_SIZE / 2 - 35, 60);
			return currentNumber;
		}

		public double getAngle() {
			return angle;
		}

		private String getNumberFromAngle(double angle) {

			int result = 0;
			if (angle != 0.0) {
				int index = (int) (angle / 9.73);
				result = rouletteNumbers[index];
			}

			return Integer.toString(result);
		}

		class ChangeAngleMouse implements Runnable {
			double moveLength;
			boolean isPositive;

			public ChangeAngleMouse(double moveLength, boolean isPositive) {
				this.moveLength = moveLength;
				this.isPositive = isPositive;
			}

			@Override
			public void run() {
				for (double i = 0; i < Math.abs(moveLength); i++) {

					changeTheAngle();
					delayDiskMovement(i);
				}
			}

			private void delayDiskMovement(double i) {
				int timeToSleep = 3;
				if (i != 0) {
					if (Math.abs(moveLength) / i > 0.99) {
						timeToSleep = (int) (i * 10 / Math.abs(moveLength));
					}
				}

				try {
					Thread.sleep(timeToSleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			private void changeTheAngle() {
				if (isPositive) {
					if (angle == 360)
						angle = -1;
					angle++;
				} else {
					if (angle == 0)
						angle = 361;
					angle--;
				}
			}		
		}
	
		private class DiskKeyListener implements KeyListener {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
						if(angle == 360){
							angle = -1;
						}
						angle++;
				}
				if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					if(angle == 0){
						angle = 361;
					}
					angle--;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

		}

		private class DiskMouseListener implements MouseListener {
			boolean isCorrectRange = false;
			private int startPositionY;
			private int endPositionY;
			private int startPositionX;
			private int endPositionX;

			@Override
			public void mouseReleased(MouseEvent e) {
				if (isCorrectRange) {
					endPositionY = e.getY();
					endPositionX = e.getX();
					if (checkIfInTheMiddle(endPositionX, endPositionY)) {
						return;
					}
					double moveY = endPositionY - startPositionY;
					double moveX = endPositionX - startPositionX;
					if (moveY == 0.0 && moveX == 0.0)
						return;
					boolean isPositive = isClockwise(moveX, moveY,
							startPositionX, startPositionY);
					double moveLength = calculateLength(moveX, moveY);
					new Thread(new ChangeAngleMouse(moveLength, isPositive)).start();
				}
			}

			private boolean checkIfInTheMiddle(int endPositionX,
					int endPositionY) {

				return Math.sqrt(Math.abs(Math.pow(endPositionX - 300, 2)
						+ Math.pow(endPositionY - 300, 2))) < 90;
			}

			private double calculateLength(double moveX, double moveY) {
				// __________________
				// |AB|= √(x2−x1)^2+(y2−y1)^2
				return Math.sqrt(Math.abs(Math.pow(moveX, 2)
						+ Math.pow(moveY, 2)));
			}

			private boolean isClockwise(double moveX, double moveY,
					int startPositionX, int startPositionY) {
				boolean result;
				boolean isRightSide = startPositionX > BOARD_SIZE / 2;
				boolean isDownSide = startPositionY > BOARD_SIZE / 2;
				if (isRightSide) {
					if (isDownSide) {
						result = moveX < 0 && moveY > 0;
					} else {
						result = moveX > 0 || (moveY > 0 && moveX < 0);
					}
				} else {
					if (isDownSide) {
						result = moveX < 0 || (moveX > 0 && moveY < 0);
					} else {
						result = moveX > 0 || (moveX < 0 && moveY < 0);
					}
				}
				return result;
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (clickedInsideRouletteDisk(e)) {
					startPositionY = e.getY();
					startPositionX = e.getX();
				}
			}

			private boolean clickedInsideRouletteDisk(MouseEvent e) {
				boolean horizontally = e.getX() > locationX
						&& e.getX() < locationX + image.getWidth();
				boolean vertically = e.getY() > locationY
						&& e.getY() < locationY + image.getHeight();
				isCorrectRange = horizontally && vertically;
				return isCorrectRange;

			}
		}
	}
}

class Rotate implements Runnable {

	RouletteGUI gui;

	public Rotate(RouletteGUI gui) {
		this.gui = gui;
	}

	@Override
	public void run() {
		gui.repaint();
	}
}