import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;

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

		this.add(new RouletteDisk());

		executor = new ScheduledThreadPoolExecutor(5);
		executor.scheduleAtFixedRate(new Rotate(this), 0L, 20L,
				TimeUnit.MILLISECONDS);

		this.setVisible(true);

	}

	@SuppressWarnings("serial")
	private class RouletteDisk extends JComponent {
		private BufferedImage image;
		private double angle;

		private int locationX;
		private int locationY;

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

		@Override
		public Dimension getPreferredSize() {

			return new Dimension(image.getWidth(), image.getHeight());
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D graphics = (Graphics2D) g;

			AffineTransform at = new AffineTransform();
			at.translate(getWidth() / 2, getHeight() / 2);
			at.rotate(Math.toRadians(getAngle()));
			at.translate(-image.getWidth() / 2, -image.getHeight() / 2);

			graphics.drawImage(image, at, null);
			System.out.println("Here Repaint");
			graphics.setComposite(AlphaComposite.Src);

			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			// graphics.setPaint(Color.WHITE);
			// graphics.fillRect(20, 20, 80, 50);
		}

		public double getAngle() {
			return angle;
		}

		public void setAngle(double angle) {
			this.angle = angle;

		}

		class ChangeAngle implements Runnable {
			double moveLength;
			boolean isPositive;

			public ChangeAngle(double moveLength, boolean isPositive) {
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
					double moveY = endPositionY - startPositionY;
					double moveX = endPositionX - startPositionX;
					if(moveY == 0.0 && moveX == 0.0)
						return;
					boolean isPositive = isPositive(moveX, moveY,
							startPositionX);
					double moveLength = calculateLength(moveX, moveY);
					new Thread(new ChangeAngle(moveLength, isPositive)).start();
				}
			}

			private double calculateLength(double moveX, double moveY) {
				//        __________________
				// |AB|= √(x2−x1)^2+(y2−y1)^2
				return Math.sqrt(Math.abs(Math.pow(moveX, 2)
						- Math.pow(moveY, 2)));
			}

			private boolean isPositive(double moveX, double moveY,
					int startPositionX) {
				boolean result;
				if (startPositionX > BOARD_SIZE / 2) {
					result = moveY > 0;
				} else {
					result = moveY < 0;
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