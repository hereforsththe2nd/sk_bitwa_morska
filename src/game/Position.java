package game;

public class Position {
	public int x, y;
	@Override
	public boolean equals(Object obj) {
		if(obj==null) return false;
		if(obj.getClass() == Position.class) {
			Position pos = (Position)obj;
			if(pos.x == this.x && pos.y == this.y)
				return true;
		}
		return super.equals(obj);
	}
	
	public Position(int x, int y) {
		this.x=x;
		this.y=y;
	}
	
	@Override
	public String toString() {
		return "x: "+x+", y: "+y;
	}
}
