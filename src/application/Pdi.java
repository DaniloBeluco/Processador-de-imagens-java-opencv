package application;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Pdi {

	private static final int RED = 1;
	private static final int GREEN = 2;
	private static final int BLUE = 3;

	public static Image cinzaMediaAritmetica(Image imagem, int pcR, int pcG, int pcB) { // Recebe uma imagem e os
																						// percentuais
		try {
			int w = (int) imagem.getWidth();
			int h = (int) imagem.getHeight();

			PixelReader pr = imagem.getPixelReader(); // le cada pixel da imagem e pega informações das cores atuais
			WritableImage wi = new WritableImage(w, h);
			PixelWriter pw = wi.getPixelWriter(); // cria a imagem nova

			for (int i = 0; i < w; i++) { // percorre todas linhas e colunas
				for (int j = 0; j < h; j++) {
					Color corA = pr.getColor(i, j); // usa o 'pixel reader' para pegar a cor do pixel atual
					double media = (corA.getRed() + corA.getGreen() + corA.getBlue()) / 3; // pega os canais R G B do
																							// pixel e divide por 3

					if (pcR != 0 || pcG != 0 || pcB != 0) // se algum percentual for diferente de 0, significa que a
															// média é ponderada
						media = (corA.getRed() * pcR + corA.getGreen() * pcG + corA.getBlue() * pcB) / 100; // faz o
																											// calculo
																											// da media
																											// ponderada
					Color corN = new Color(media, media, media, corA.getOpacity()); // cria a nova cor no pixel,
																					// passando os valores calculados e
																					// a opacidade da imagem anterior
					pw.setColor(i, j, corN); // usa o 'pixel writer' para escrever a nova cor no pixel atual
				}
			}
			return wi;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static Image limiarizacao(Image imagem, double limiar) { // recebe a imagem e a limiar
		try {
			int w = (int) imagem.getWidth(); // pega a largura
			int h = (int) imagem.getHeight(); // pega a altura

			PixelReader pr = imagem.getPixelReader();
			WritableImage wi = new WritableImage(w, h);
			PixelWriter pw = wi.getPixelWriter();

			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					Color corA = pr.getColor(i, j); // pega a cor atual
					Color corN; // cor nova

					if (corA.getRed() >= limiar) // se o canal atual for maior que o limiar
						corN = new Color(1, 1, 1, corA.getOpacity()); // o rgb da imagem fica tudo branco
					else // senão
						corN = new Color(0, 0, 0, corA.getOpacity()); // fica preto

					pw.setColor(i, j, corN);
				}
			}
			return wi;

		} catch (Exception e) {
			return null;
		}

	}

	public static Image negativa(Image imagem) {
		try {
			int w = (int) imagem.getWidth();
			int h = (int) imagem.getHeight();
			System.out.println(w);
			System.out.println(h);

			PixelReader pr = imagem.getPixelReader();
			WritableImage wi = new WritableImage(w, h);
			PixelWriter pw = wi.getPixelWriter();

			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					Color corA = pr.getColor(i, j);
					Color corN = new Color(1 - corA.getRed(), // diminui 1 resultando na negativa
							1 - corA.getGreen(), 1 - corA.getBlue(), corA.getOpacity());
					pw.setColor(i, j, corN);

				}
			}
			return wi;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static Image ruidos(Image imagem, int tipoVizinhos) {

		try {
			int w = (int) imagem.getWidth();
			int h = (int) imagem.getHeight();

			PixelReader pr = imagem.getPixelReader();
			WritableImage wi = new WritableImage(w, h);
			PixelWriter pw = wi.getPixelWriter();

			for (int i = 1; i < w - 1; i++) {
				for (int j = 1; j < h - 1; j++) {

					Color corA = pr.getColor(i, j);

					/*
					 * Instancia o Pixel e Envia para o Construtor da Classe, armazendo informações
					 * do pixel atual na variável dessa instância da Classe Pixel
					 */
					Pixel p = new Pixel(corA.getRed(), corA.getGreen(), corA.getBlue(), i, j);

					/*
					 * Busca os vizinhos do Pixel atual e coloca eles nas arrays vizX, vizC, e viz3
					 * da instância da classe Pixel
					 */
					buscaVizinhos(imagem, p);

					Pixel vetor[] = null; // cria um vetor de pixels

					if (tipoVizinhos == Constantes.VIZINHOS3x3) // se tiver definido como busca de vizinhos em 3X3

						vetor = p.viz3; // usa esse vetor de 8 posições da classe Pixel, que ja esta com todos os
										// vizinhos do Pixel atual

					if (tipoVizinhos == Constantes.VIZINHOSCRUZ) // se tiver definido como busca de vizinhos em Cruz

						vetor = p.vizC; // usa esse vetor de 4 posições da classe Pixel, que ja esta com todos os
										// vizinhos do Pixel atual

					if (tipoVizinhos == Constantes.VIZINHOSX) // se tiver definido como busca de vizinhos em X

						vetor = p.vizX; // usa esse vetor de 4 posições da classe Pixel, que ja esta com todos os
										// vizinhos do Pixel atual

					double red = mediana(vetor, Constantes.CANALR); // calcula a mediana do r

					double green = mediana(vetor, Constantes.CANALG); // calcula a mediana do g

					double blue = mediana(vetor, Constantes.CANALB); // calcula a mediana do b

					Color corN = new Color(red, green, blue, corA.getOpacity()); // o pixel recebe a nova cor :)

					pw.setColor(i, j, corN);
				}
			}

			return wi;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	private static void buscaVizinhos(Image imagem, Pixel p) {
		p.vizX = buscaVizinhosX(imagem, p); // busca vizinho em X, chamando o metodo que busca e retorna uma array
		p.vizC = buscaVizinhosC(imagem, p); // busca vizinho em Cruz, chamando o metodo que busca e retorna uma array
		p.viz3 = buscaVizinhos3(imagem, p); // busca vizinho em 3X3, chamando o metodo que busca e retorna uma array
	}

	private static Pixel[] buscaVizinhosX(Image imagem, Pixel p) {

		Pixel[] vizX = new Pixel[5]; // cria uma matriz de pixels

		PixelReader pr = imagem.getPixelReader(); // vai usar o leitor de pixels

		/* Lê a cor de cada Pixel vizinho do Pixel atual - Formato X */

		Color cor = pr.getColor(p.x - 1, p.y + 1);
		vizX[0] = new Pixel(cor.getRed(), cor.getGreen(), cor.getBlue(), p.x - 1, p.y + 1);

		cor = pr.getColor(p.x + 1, p.y - 1);
		vizX[1] = new Pixel(cor.getRed(), cor.getGreen(), cor.getBlue(), p.x + 1, p.y - 1);

		cor = pr.getColor(p.x - 1, p.y - 1);
		vizX[2] = new Pixel(cor.getRed(), cor.getGreen(), cor.getBlue(), p.x - 1, p.y - 1);

		cor = pr.getColor(p.x + 1, p.y + 1);
		vizX[3] = new Pixel(cor.getRed(), cor.getGreen(), cor.getBlue(), p.x + 1, p.y + 1);

		vizX[4] = p; // na ultima posição coloca o proprio pixel

		return vizX; // Retorna a array com todos os Pixels vizinhos

	}

	private static Pixel[] buscaVizinhosC(Image imagem, Pixel p) {

		Pixel[] vizC = new Pixel[5]; // cria uma matriz de pixels

		PixelReader pr = imagem.getPixelReader(); // vai usar o leitor de pixels

		/* Lê a cor de cada Pixel vizinho do Pixel atual - Formato Cruz */

		Color cor = pr.getColor(p.x, p.y - 1);
		vizC[0] = new Pixel(cor.getRed(), cor.getGreen(), cor.getBlue(), p.x, p.y - 1);

		cor = pr.getColor(p.x, p.y + 1);
		vizC[1] = new Pixel(cor.getRed(), cor.getGreen(), cor.getBlue(), p.x, p.y + 1);

		cor = pr.getColor(p.x - 1, p.y);
		vizC[2] = new Pixel(cor.getRed(), cor.getGreen(), cor.getBlue(), p.x - 1, p.y);

		cor = pr.getColor(p.x + 1, p.y);
		vizC[3] = new Pixel(cor.getRed(), cor.getGreen(), cor.getBlue(), p.x + 1, p.y);

		vizC[4] = p;

		return vizC;

	}

	private static Pixel[] buscaVizinhos3(Image imagem, Pixel p) {

		Pixel[] viz3 = new Pixel[9]; // cria uma matriz de pixels

		PixelReader pr = imagem.getPixelReader(); // vai usar o leitor de pixels

		/* Lê a cor de cada Pixel vizinho do Pixel atual - Formato 3X3 */

		Color cor = pr.getColor(p.x, p.y - 1);
		viz3[0] = new Pixel(cor.getRed(), cor.getGreen(), cor.getBlue(), p.x, p.y - 1);

		cor = pr.getColor(p.x, p.y + 1);
		viz3[1] = new Pixel(cor.getRed(), cor.getGreen(), cor.getBlue(), p.x, p.y + 1);

		cor = pr.getColor(p.x - 1, p.y);
		viz3[2] = new Pixel(cor.getRed(), cor.getGreen(), cor.getBlue(), p.x - 1, p.y);

		cor = pr.getColor(p.x + 1, p.y);
		viz3[3] = new Pixel(cor.getRed(), cor.getGreen(), cor.getBlue(), p.x + 1, p.y);

		cor = pr.getColor(p.x - 1, p.y + 1);
		viz3[4] = new Pixel(cor.getRed(), cor.getGreen(), cor.getBlue(), p.x - 1, p.y + 1);

		cor = pr.getColor(p.x + 1, p.y - 1);

		/*
		 * System.out.println("tamanho da array: "+viz3.length);
		 * System.out.println("cor.getRed(): "+cor.getRed());
		 * System.out.println("cor.getGreen(): "+cor.getGreen());
		 * System.out.println("cor.getBlue(): "+cor.getBlue()); System.out.println(p.x +
		 * 1); System.out.println(p.x - 1);
		 */

		viz3[5] = new Pixel(cor.getRed(), cor.getGreen(), cor.getBlue(), p.x + 1, p.y - 1);

		cor = pr.getColor(p.x - 1, p.y - 1);
		viz3[6] = new Pixel(cor.getRed(), cor.getGreen(), cor.getBlue(), p.x - 1, p.y - 1);

		cor = pr.getColor(p.x + 1, p.y + 1);
		viz3[7] = new Pixel(cor.getRed(), cor.getGreen(), cor.getBlue(), p.x + 1, p.y + 1);

		viz3[8] = p;

		return viz3;

	}

	public static double mediana(Pixel[] pixels, int canal) { // recebe os pixels da vizinhança e o canal

		double v[] = new double[pixels.length]; // cria um novo vetor com o tamanho da vizinhança

		for (int i = 0; i < pixels.length; i++) { // para cada pixel da vizinhança

			if (canal == Constantes.CANALR) { // Se o canal for igual a r, ou 1
				v[i] = pixels[i].r; // preenche nessa posição o vetor com o r do pixel atual
			}
			if (canal == Constantes.CANALG) { // Se o canal for igual a g, ou 2
				v[i] = pixels[i].g; // preenche nessa posição o vetor com g do pixel atual
			}
			if (canal == Constantes.CANALB) { // Se o canal for igual a b, ou 3
				v[i] = pixels[i].b; // preenche nessa posição o vetor com o b do pixel atual
			}

		}

		v = ordenaVetor(v); // ordena o vetor

		return v[v.length / 2]; // retorna o pixel do centro do vetor
	}

	public static double[] ordenaVetor(double[] v) {

		Arrays.sort(v);

		return v;
	}

	public static Image adicao(Image img1, Image img2, double ti1, double ti2) {

		/* Pega a largura e altura de cada imagem */
		int w1 = (int) img1.getWidth();
		int h1 = (int) img1.getHeight();
		int w2 = (int) img2.getWidth();
		int h2 = (int) img2.getHeight();

		/* Pega a largura e altura MÍNIMAS de cada imagem */
		int w = Math.min(w1, w2);
		int h = Math.min(h1, h2);

		PixelReader pr1 = img1.getPixelReader();
		PixelReader pr2 = img2.getPixelReader();

		WritableImage wi = new WritableImage(w, h);
		PixelWriter pw = wi.getPixelWriter();

		for (int i = 1; i < w; i++) {
			for (int j = 1; j < h; j++) {

				/* Pega a cor do Pixel de cada imagem */
				Color corImg1 = pr1.getColor(i, j);
				Color corImg2 = pr2.getColor(i, j);

				/*
				 * Pega o vermelho multiplica pelo indice de ponderação da imagem 1, soma com
				 * vermelho da imagem 2 e multiplica pelo indice de ponderação da imagem 2
				 */
				double r = (corImg1.getRed() * ti1) + (corImg2.getRed() * ti2);
				double g = (corImg1.getGreen() * ti1) + (corImg2.getGreen() * ti2);
				double b = (corImg1.getBlue() * ti1) + (corImg2.getBlue() * ti2);

				/*
				 * Se o resultado for maior que 1, significa que estrapolou o 255, ai po~e como
				 * 255
				 */
				r = r > 1 ? 1 : r;
				g = g > 1 ? 1 : g;
				b = b > 1 ? 1 : b;

				/* Cria a nova cor no pixel da nova imagem */
				Color newCor = new Color(r, g, b, 1);
				pw.setColor(i, j, newCor);

			}
		}
		return wi;

	}

	public static Image subtracao(Image img1, Image img2) {

		/* Pega a largura e altura de cada imagem */
		int w1 = (int) img1.getWidth();
		int h1 = (int) img1.getHeight();
		int w2 = (int) img2.getWidth();
		int h2 = (int) img2.getHeight();

		/* Pega a largura e altura MÍNIMAS de cada imagem */
		int w = Math.min(w1, w2);
		int h = Math.min(h1, h2);

		PixelReader pr1 = img1.getPixelReader();
		PixelReader pr2 = img2.getPixelReader();

		WritableImage wi = new WritableImage(w, h);
		PixelWriter pw = wi.getPixelWriter();

		for (int i = 1; i < w; i++) {
			for (int j = 1; j < h; j++) {

				/* Pega a cor do Pixel de cada imagem */
				Color oldCor1 = pr1.getColor(i, j);
				Color oldCor2 = pr2.getColor(i, j);

				double r = (oldCor1.getRed()) - (oldCor2.getRed());
				double g = (oldCor1.getGreen()) - (oldCor2.getGreen());
				double b = (oldCor1.getBlue()) - (oldCor2.getBlue());

				/*
				 * Se o resultado for maior que 1, significa que estrapolou o 255, ai po~e como
				 * 255
				 */
				r = r < 0 ? 0 : r;
				g = g < 0 ? 0 : g;
				b = b < 0 ? 0 : b;

				/* Cria a nova cor no pixel da nova imagem */
				Color newCor = new Color(r, g, b, oldCor1.getOpacity());
				pw.setColor(i, j, newCor);

			}
		}
		return wi;

	}

	public static Image marca(Image imagem, double x1, double y1, double x2, double y2) {

		try {
			int w = (int) imagem.getWidth();
			int h = (int) imagem.getHeight();

			PixelReader pr = imagem.getPixelReader();
			WritableImage wi = new WritableImage(w, h);
			PixelWriter pw = wi.getPixelWriter();

			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					Color corA = pr.getColor(i, j);
					double media = (corA.getRed() + corA.getGreen() + corA.getBlue()) / 3;
					double r, g, b;

					/* Caso aponte Embaixo para direita */

					if (x2 > x1 && y2 > y1) {

						if (i > x1 && i < x2 && j == y1) {

							r = 0.0;
							g = 0.0;
							b = 0.0;

						} else if (i == x1 && j > y1 && j < y2) {

							r = 0.0;
							g = 0.0;
							b = 0.0;

						} else if (i == x2 && j > y1 && j < y2) {

							r = 0.0;
							g = 0.0;
							b = 0.0;

						} else if (i > x1 && i < x2 && j == y2) {

							r = 0.0;
							g = 0.0;
							b = 0.0;

						} else {
							r = corA.getRed();
							g = corA.getGreen();
							b = corA.getBlue();
						}

						/* Caso aponte Embaixo para Esquerda */

					} else if (x2 < x1 && y2 > y1) {

						if (i < x1 && i > x2 && j == y1) {
							r = 0.0;
							g = 0.0;
							b = 0.0;

						} else if (i == x1 && j > y1 && j < y2) {

							r = 0.0;
							g = 0.0;
							b = 0.0;

						} else if (i == x2 && j > y1 && j < y2) {

							r = 0.0;
							g = 0.0;
							b = 0.0;

						} else if (i < x1 && i > x2 && j == y2) {

							r = 0.0;
							g = 0.0;
							b = 0.0;

						} else {
							r = corA.getRed();
							g = corA.getGreen();
							b = corA.getBlue();
						}

						/* Caso aponte Emcima para Direita */

					} else if (x2 > x1 && y2 < y1) {

						if (i > x1 && i < x2 && j == y1) {
							r = 0.0;
							g = 0.0;
							b = 0.0;

						} else if (i == x1 && j < y1 && j > y2) {

							r = 0.0;
							g = 0.0;
							b = 0.0;

						} else if (i == x2 && j < y1 && j > y2) {

							r = 0.0;
							g = 0.0;
							b = 0.0;

						} else if (i > x1 && i < x2 && j == y2) {

							r = 0.0;
							g = 0.0;
							b = 0.0;

						} else {
							r = corA.getRed();
							g = corA.getGreen();
							b = corA.getBlue();
						}

						/* Caso aponte Emcima para Esquerda */
					} else if (x2 < x1 && y2 < y1) {

						if (i < x1 && i > x2 && j == y1) {
							r = 0.0;
							g = 0.0;
							b = 0.0;

						} else if (i == x1 && j < y1 && j > y2) {

							r = 0.0;
							g = 0.0;
							b = 0.0;

						} else if (i == x2 && j < y1 && j > y2) {

							r = 0.0;
							g = 0.0;
							b = 0.0;

						} else if (i < x1 && i > x2 && j == y2) {

							r = 0.0;
							g = 0.0;
							b = 0.0;

						} else {
							r = corA.getRed();
							g = corA.getGreen();
							b = corA.getBlue();
						}

					} else {

						r = corA.getRed();
						g = corA.getGreen();
						b = corA.getBlue();

					}

					Color corN = new Color(r, g, b, corA.getOpacity());
					pw.setColor(i, j, corN);
				}
			}
			return wi;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	// @SupressWarnings({ "rawtypes", "unchecked" })
	/* Monta o grafico, parametro de imagem, e o BarChart do grafico */
	public static void getGrafico(Image img, BarChart<String, Number> grafico) {

		/* Vetor de 255 posições recebe a quantidade de pixels de cada cor da imagem */
		int[] hist = histogramaUnico(img);
		// int[] histR = histograma(img, 1);
		// int[] histG = histograma(img, 2);
		// int[] histB = histograma(img, 3);
		XYChart.Series vlr = new XYChart.Series();
		// XYChart.Series vlrR = new XYChart.Series();
		// XYChart.Series vlrG = new XYChart.Series();
		// XYChart.Series vlrB = new XYChart.Series();

		/* Monta o grafico */
		for (int i = 0; i < hist.length; i++) {
			vlr.getData().add(new XYChart.Data(i + "", hist[i]));
			// vlrR.getData().add(new XYChart.Data(i+"", histR[i]));
			// vlrG.getData().add(new XYChart.Data(i+"", histG[i]));
			// vlrB.getData().add(new XYChart.Data(i+"", histB[i]));
		}
		grafico.getData().addAll(vlr);

		for (Node n : grafico.lookupAll(".default-color0.chart-bar")) {
			n.setStyle("-fx-bar-fill: red;");
		}
	}

	/* Metodo que retorna a quantidade de pixels de cada cor da imagem */
	public static int[] histogramaUnico(Image img) {

		int[] qt = new int[256];
		PixelReader pr = img.getPixelReader();
		int w = (int) img.getWidth();
		int h = (int) img.getHeight();
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				qt[(int) (pr.getColor(i, j).getRed() * 255)]++;
				qt[(int) (pr.getColor(i, j).getGreen() * 255)]++;
				qt[(int) (pr.getColor(i, j).getBlue() * 255)]++;

			}
		}
		return qt;

	}

	/* Metodo que retorna a quantidade de pixels de cada cor da imagem */
	public static int[] histograma(Image img, int canal) {

		int[] qt = new int[256];
		PixelReader pr = img.getPixelReader();
		int w = (int) img.getWidth();
		int h = (int) img.getHeight();

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {

				if (canal == 1) {
					qt[(int) (pr.getColor(i, j).getRed() * 255)]++;

				}
				if (canal == 2) {
					qt[(int) (pr.getColor(i, j).getGreen() * 255)]++;

				}
				if (canal == 3) {
					qt[(int) (pr.getColor(i, j).getBlue() * 255)]++;

				}

			}
		}

		return qt;

	}

	/* True conidera todos pixels, false considera só os válidos */
	public static Image equalizacaoHistograma(Image img, boolean todos) {
		int w = (int) img.getWidth();
		int h = (int) img.getHeight();

		PixelReader pr = img.getPixelReader();
		WritableImage wi = new WritableImage(w, h);
		PixelWriter pw = wi.getPixelWriter();

		/*
		 * Tenho 3 vetores, um para cada histograma, chama o metodo do histograma que ja
		 * temos
		 */
		int[] hR = histograma(img, RED);
		int[] hG = histograma(img, GREEN);
		int[] hB = histograma(img, BLUE);

		/* Metodo a criar, Histograma acumulado */
		int[] histAcR = histogramaAc(hR);
		int[] histAcG = histogramaAc(hG);
		int[] histAcB = histogramaAc(hB);

		/* Recebe a quantidade de tons vermelho, verde e azul */
		/* Recebe apenas os validos, que nao sao 0 de 255 */
		/* Faz contagem de quantos sao 0 nesse histograma menos 255 */
		int qtTonsRed = qtTons(hR);

		int qtTonsGreen = qtTons(hG);

		int qtTonsBlue = qtTons(hB);

		/* Valor/Ponto minimo de cada histograma */
		double minR = pontoMin(hR);

		double minG = pontoMin(hG);

		double minB = pontoMin(hB);

		System.out.println("minimoR " + minR);
		System.out.println("minimoG " + minG);
		System.out.println("minimoB " + minB);

		System.out.println("qtTonsR " + qtTonsRed);
		System.out.println("qtTonsG " + qtTonsGreen);
		System.out.println("qtTonsB " + qtTonsBlue);

		/* se é pra considerar todos os pixels */
		if (todos) {

			qtTonsRed = 255;
			qtTonsGreen = 255;
			qtTonsBlue = 255;
			minR = 0;
			minG = 0;
			minB = 0;
		}

		/* Quantidade de pixels total da imagem */
		double n = w * h;

		/* aplica a formula percorrendo todos pixels */
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {

				/* Cor antiga */
				Color oldCor = pr.getColor(i, j);

				/*
				 * Pega o valor de um histograma acumulado de determinado pixel, multiplica por
				 * 255
				 */
				/* e usa esse valor pra ir na posição do histograma acumulado */
				double acR = histAcR[(int) (oldCor.getRed() * 255)];
				double acG = histAcG[(int) (oldCor.getGreen() * 255)];
				double acB = histAcB[(int) (oldCor.getBlue() * 255)];

				/* Quantidade pixels validos para cada canal */
				/*
				 * Pega a quantidade de tons - 1, e divide pela quantidade de pixels, e
				 * multiplica pelo histograma acumulado
				 */
				double pxR = ((qtTonsRed - 1) / n) * acR;
				double pxG = ((qtTonsGreen - 1) / n) * acG;
				double pxB = ((qtTonsBlue - 1) / n) * acB;
				// agora ja tenho o novo valor do pixel

				/* Para evitar erro deve-se partir do ponto minimo */
				double corR = (minR + pxR) / 255;
				double corG = (minG + pxG) / 255;
				double corB = (minB + pxB) / 255;

				Color newCor = new Color(corR, corG, corB, oldCor.getOpacity());
				pw.setColor(i, j, newCor);

			}
		}
		return wi;

	}

	/* Retorna o Histograma Acumulado */
	public static int[] histogramaAc(int[] hist) {

		int histAc[] = new int[hist.length];

		for (int i = 0; i < hist.length; i++) {
			if (i > 0) {
				histAc[i] = hist[i] + histAc[i - 1];
			} else {
				histAc[i] = hist[i];
			}
		}

		for (int i = 0; i < histAc.length; i++) {
		}

		return histAc;

	}

	/* Retorna a quantidade de tons validos do canal, (quantos nao sao 0) */
	public static int qtTons(int[] h) {

		int qt = 0;

		for (int i = 0; i < h.length; i++) {
			if (h[i] == 0) {
				qt++;
			}
		}
		System.out.println("quantidade de tons aqui: " + qt);
		return 255 - qt;
	}

	/*
	 * Metodo que percorre todo o histograma, se o valor for mair que 0 retorna o i,
	 * que é o primeiro valor ( ponto minimo do histograma )
	 */
	public static int pontoMin(int[] hist) {
		for (int i = 0; i < hist.length; i++) {
			if (hist[i] > 0) {
				System.out.println("ponto minimo aqui: " + hist[i]);
				return i;
			}
		}
		return 0;
	}

	public static Image blur(Image imagem) {

		try {

			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

			int width = (int) imagem.getWidth();
			int height = (int) imagem.getHeight();
			byte[] buffer = new byte[width * height * 4];

			PixelReader reader = imagem.getPixelReader();
			WritablePixelFormat<ByteBuffer> format = WritablePixelFormat.getByteBgraInstance();
			reader.getPixels(0, 0, width, height, format, buffer, 0, width * 4);

			Mat mat = image2Mat(imagem);

			// remove some noise
			Imgproc.blur(mat, mat, new Size(7, 7));

			imagem = mat2Image(mat);

			return imagem;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	public static Image blurGaussiano(Image imagem) {

		try {

			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

			int width = (int) imagem.getWidth();
			int height = (int) imagem.getHeight();
			byte[] buffer = new byte[width * height * 4];

			PixelReader reader = imagem.getPixelReader();
			WritablePixelFormat<ByteBuffer> format = WritablePixelFormat.getByteBgraInstance();
			reader.getPixels(0, 0, width, height, format, buffer, 0, width * 4);

			Mat mat = image2Mat(imagem);

			// g
			Imgproc.GaussianBlur(mat, mat, new Size(11, 11), 0);

			imagem = mat2Image(mat);

			return imagem;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static Image canny(Image imagem) {

		try {

			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

			Mat mat = image2Mat(imagem);
			Mat cannyEdges = new Mat();

			//Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
			// Imgproc.blur(mat, mat, new Size(7, 7));

			Imgproc.Canny(mat, cannyEdges, -1, 256);

			imagem = mat2Image(cannyEdges);

			return imagem;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static Image sobel(Image imagem) {

		try {

			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

			Mat mat = image2Mat(imagem);
			Mat nova = new Mat();

			Imgproc.Sobel(mat, mat, -1, 0, 255);

			imagem = mat2Image(nova);

			return imagem;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static Image prewitt(Image imagem) {

		try {

			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

			Mat mat = image2Mat(imagem);
			int kernelSize = 9;

			Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);

			Mat destination = new Mat(mat.rows(), mat.cols(), mat.type());

			Mat kernel = new Mat(kernelSize, kernelSize, CvType.CV_32F) {
				{
					put(0, 0, -1);
					put(0, 1, 0);
					put(0, 2, 1);

					put(1, 0 - 1);
					put(1, 1, 0);
					put(1, 2, 1);

					put(2, 0, -1);
					put(2, 1, 0);
					put(2, 2, 1);
				}
			};

			Imgproc.filter2D(mat, destination, -1, kernel);

			imagem = mat2Image(destination);

			return imagem;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static Image dilatacao(Image imagem, int amount) {

		try {

			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

			Mat mat = image2Mat(imagem);
			Mat nova = new Mat();
			
			

			Mat kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(2 * amount + 1, 2 * amount + 1),
					new Point(amount, amount));
			Imgproc.dilate(mat, nova, kernel);

			imagem = mat2Image(nova);

			return imagem;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static Image erosao(Image imagem, int erosion_size) {

		try {

			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

			Mat mat = image2Mat(imagem);
			Mat nova = new Mat();


			Mat element = Imgproc.getStructuringElement(Imgproc.THRESH_BINARY,
					new Size(2 * erosion_size + 1, 2 * erosion_size + 1));
			Imgproc.erode(mat, nova, element);

			imagem = mat2Image(nova);

			return imagem;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	private static Image mat2Image(Mat frame) {
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (frame.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = frame.channels() * frame.cols() * frame.rows();
		byte[] b = new byte[bufferSize];
		frame.get(0, 0, b); // get all the pixels
		BufferedImage image = new BufferedImage(frame.cols(), frame.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		return SwingFXUtils.toFXImage(image, null);

	}

	public static Mat image2Mat(Image image) {

		BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);

		return bufferedImage2Mat(bImage);

	}

	public static Mat bufferedImage2Mat(BufferedImage in) {
		Mat out;
		byte[] data;
		int r, g, b;
		int height = in.getHeight();
		int width = in.getWidth();
		if (in.getType() == BufferedImage.TYPE_INT_RGB || in.getType() == BufferedImage.TYPE_INT_ARGB) {
			out = new Mat(height, width, CvType.CV_8UC3);
			data = new byte[height * width * (int) out.elemSize()];
			int[] dataBuff = in.getRGB(0, 0, width, height, null, 0, width);
			for (int i = 0; i < dataBuff.length; i++) {
				data[i * 3 + 2] = (byte) ((dataBuff[i] >> 16) & 0xFF);
				data[i * 3 + 1] = (byte) ((dataBuff[i] >> 8) & 0xFF);
				data[i * 3] = (byte) ((dataBuff[i] >> 0) & 0xFF);
			}
		} else {
			out = new Mat(height, width, CvType.CV_8UC1);
			data = new byte[height * width * (int) out.elemSize()];
			int[] dataBuff = in.getRGB(0, 0, width, height, null, 0, width);
			for (int i = 0; i < dataBuff.length; i++) {
				r = (byte) ((dataBuff[i] >> 16) & 0xFF);
				g = (byte) ((dataBuff[i] >> 8) & 0xFF);
				b = (byte) ((dataBuff[i] >> 0) & 0xFF);
				data[i] = (byte) ((0.21 * r) + (0.71 * g) + (0.07 * b)); // luminosity
			}
		}
		out.put(0, 0, data);
		return out;
	}

}
