package application;

public class Pixel {

	/* Cor deste Pixel */
	public double r;
	public double g;
	public double b;

	/* Posição deste Pixel */
	public int x;
	public int y;

	/* Vizinhos deste Pixel p/ cada tipo de eliminação de ruídos */
	public Pixel[] vizX = new Pixel[4];
	public Pixel[] vizC = new Pixel[4];
	public Pixel[] viz3 = new Pixel[8];

	/* Construtor */
	public Pixel(double r, double g, double b, int x, int y) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.x = x;
		this.y = y;
	}

}
