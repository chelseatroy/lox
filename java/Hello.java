class BobBox extends Box {
	private int x1;
	private int y1;
	private int width;
	private int height;

	public BobBox(int x1, int width, int y1, int height) {
		this.x1 = x1;
		this.y1 = y1;
		this.width = width;
		this.height = height;
	}

	@Override
	void accept(BoxVisitor visitor) {
		visitor.visitBob(this);
	}
}

class AliceBox extends Box {
	private int x1;
	private int x2;
	private int y1;
	private int y2;

	public AliceBox(int x1, int x2, int y1, int y2) {
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
	}

	@Override
	void accept(BoxVisitor visitor) {
		visitor.visitAlice(this);
	}
}
