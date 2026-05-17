package game;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;

public class BoardLayout implements LayoutManager2 {
	
	private Component center;
	private Component left;
	private Component top;
	
	int topH, leftW, width, height;
	
	static enum Location{
		CENTER,
		LEFT,
		TOP
	}
	
	private static final int min(int x, int y) {
		return x<y ? x : y;
	}
	
	private static final Dimension min(Dimension a, Dimension b) {
		return new Dimension(min(a.width,b.width), min(a.height, b.height));
	}
	
	@Override
	public void addLayoutComponent(String name, Component comp) {	}

	@Override
	public void removeLayoutComponent(Component comp) {	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return new Dimension(width+leftW, height+topH);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return new Dimension(leftW, topH);
	}

	@Override
	public void layoutContainer(Container parent) {	
		center.setSize(min(new Dimension(parent.getWidth()-leftW, parent.getHeight()-topH), center.getMaximumSize()));
		left.setSize(new Dimension(leftW, center.getHeight()));
		top.setSize(new Dimension(center.getWidth(), topH));
		left.setLocation(0, topH);
		top.setLocation(leftW, 0);
		center.setLocation(leftW, topH);
	}

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		switch(constraints) {
		case Location.LEFT:
			left=comp;
			leftW=left.getMinimumSize().width;
			break;
		case Location.TOP:
			top=comp;
			topH=top.getMinimumSize().height;
			break;
		default:
			center=comp;
			width=comp.getPreferredSize().width;
			height=comp.getPreferredSize().height;
			break;
		}
	}

	@Override
	public Dimension maximumLayoutSize(Container target) {
		return preferredLayoutSize(target);
	}

	@Override
	public float getLayoutAlignmentX(Container target) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getLayoutAlignmentY(Container target) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void invalidateLayout(Container target) {
		// TODO Auto-generated method stub

	}

}
