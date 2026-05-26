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
	
	public void setValues(int x, int y){
		this.x=x;
		this.y=y;
	}
	
	public void setValues(Position p) {
		this.x=p.x;
		this.y=p.y;
	}
	
	static public String encode(Position p) {
		return p.x+","+p.y;
	}
	
	static public Position decode(String str) {
		String[] prts = str.split(",");
		return new Position(Integer.parseInt(prts[0]), Integer.parseInt(prts[1]));
	}
	
	public boolean checkIfContained(int N1, int N2) {
		return x>=0&&y>=0&&x<N1&&y<N2;
	}
}
