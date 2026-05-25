package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.function.Predicate;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

class Rectangle{
	int x,y,width,height;
	
	public Rectangle(int x, int y, int width, int height) {
		this.x=x;
		this.width=width;
		this.y=y;
		this.height=height;
	}
	
	@Override
	public String toString() {
		return x+", "+y+", "+width+", "+height;
	}
}


interface Drawable{
	static int BOTTOM_LAYER = 0,
			TOP_LAYER = Integer.MAX_VALUE;
	void draw(Rectangle bounds, Graphics2D g2d);
	int layer();
	Position getPosition();
}

public class Grid extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 249850776205643471L;
	private final int N1, N2;
	private int tileWidth, tileHeight, widthRest, heightRest;
	private final ArrayList<Layer> layers = new ArrayList<Grid.Layer>();	
	private BufferedImage screenBuffer;
	
	public Grid(final int N1, final int N2, final int layers) {
		this.N1=N1;
		this.N2=N2;
		for(int i=0;i<layers;i++) {
			this.layers.add(new Layer(null, i));
		}
	}

	@Override
	public void setSize(Dimension d) {
		setSize(d.width, d.height);
	}
	
	@Override
	public void setSize(int width, int height) {
		for(Layer l : layers)
			if(width > 0 && height > 0 && ((l.getImg()==null) || (width<l.getImg().getWidth()/3 && height<l.getImg().getHeight()/3)  || (width>l.getImg().getWidth() || height>l.getImg().getHeight())))
				l.setImg(new BufferedImage((int) (width*1.25), (int)(height*1.25), BufferedImage.TYPE_INT_ARGB));
		if(width > 0 && height > 0 && ((screenBuffer==null) || (width<screenBuffer.getWidth()/3 && height<screenBuffer.getHeight()/3)  || (width>screenBuffer.getWidth() || height>screenBuffer.getHeight())))
			screenBuffer = new BufferedImage((int) (width*1.25), (int)(height*1.25), BufferedImage.TYPE_INT_ARGB);
		for(Layer l : layers)
			l.setRepaint(true);
		
		tileWidth=width/N1;
		tileHeight=height/N2;
		widthRest=width%N1;
		heightRest=height%N2;
		super.setSize(width, height);
	}
	
	protected Position getCoords(int x, int y) {
		int xn = x * N1/getWidth();
		int yn = y * N2/getHeight();
		if(xn<0 || yn<0 || xn>=N1 || yn>=N2) return null;
		else return new Position(xn, yn);
	}
	
	private Rectangle getBounds(Position r) {
		if(r.x==N1-1 && r.y==N2-1) 
			return new Rectangle(tileWidth*r.x,
					tileHeight*r.y,
					tileWidth+widthRest-1,
					tileHeight+heightRest-1
					);
		if(r.x == N1-1)
			return new Rectangle(tileWidth*r.x,
					tileHeight*r.y,
					tileWidth+widthRest-1,
					tileHeight-1
					);
		if(r.y == N2-1)
			return new Rectangle(tileWidth*r.x,
					tileHeight*r.y,
					tileWidth-1,
					tileHeight+heightRest-1
					);
		return new Rectangle(tileWidth*r.x,
				tileHeight*r.y,
				tileWidth-1,
				tileHeight-1
				);
	}
	
	public void addDrawable(Drawable d, Integer layer) {
		LinkedList<Drawable> draw = layers.get(layer).getDraw();
		draw.add(d);
	}
	
	public void removeDrawable(Drawable d, int layer) {
		layers.get(layer).remove(d);
	}
	
	public void addRepaintRequest(int layer) {
		layers.get(layer).setRepaint(true);
	}
	
	@Override
	final protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		Graphics2D bufferg2d = screenBuffer.createGraphics();
		g2d.setRenderingHint(
			    RenderingHints.KEY_INTERPOLATION ,
			    RenderingHints.VALUE_INTERPOLATION_BICUBIC
			);
		for(Layer l : layers) {
			if(l.isRepaint()) {
				Graphics2D lg2d = l.getImg().createGraphics();
				lg2d.setBackground(new Color(255,0,0,0));
				lg2d.clearRect(0, 0, getWidth(), getHeight());
				for(Drawable d : l.getDraw()) {
					if(d.getPosition() != null) {
						Rectangle bounds = getBounds(d.getPosition());
						d.draw(bounds, lg2d);
					}
				}
				l.setRepaint(false);
				lg2d.dispose();
			}
			bufferg2d.drawImage(l.getImg(), 0, 0, null);
		}
		g2d.drawImage(screenBuffer, 0, 0, null);
		bufferg2d.dispose();
		g2d.dispose();
	}
	
	public int getN1() {
		return N1;
	}

	public int getN2() {
		return N2;
	}
	
	private class Layer{
		private BufferedImage img;
		private Integer layer;
		private boolean repaint=false;
		
		//private final Drawable[][] draw = new Drawable[N1][N2];//założenie jest, że ta lista jest posortowana wg layer
		private final LinkedList<Drawable> draw = new LinkedList<Drawable>();
		private Layer(BufferedImage img, Integer layer) {
			this.img=img;
			this.layer=layer;
		}
		
		public void remove(Drawable d) {
			draw.remove(d);
		}

		public BufferedImage getImg() {
			return img;
		}
		public void setImg(BufferedImage img) {
			this.img = img;
		}
		public Integer getLayer() {
			return layer;
		}
		public void setLayer(Integer layer) {
			this.layer = layer;
		}
		public boolean isRepaint() {
			return repaint;
		}
		public void setRepaint(boolean repaint) {
			this.repaint = repaint;
		}
		
		public LinkedList<Drawable> getDraw() {
			return draw;
		}
		
		
	}
	
	public void clearLayer(int layer) {
		layers.get(layer).getDraw().clear();
		layers.get(layer).setRepaint(true);
		repaint();	
	}
}