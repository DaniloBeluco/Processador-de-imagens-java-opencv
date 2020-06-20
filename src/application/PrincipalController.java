package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PrincipalController {

	@FXML
	private Button abrirImagem1;

	@FXML
	private Button abrirImagem2;

	@FXML
	private Button salvar;

	@FXML
	private Label lblR;

	@FXML
	private Label lblG;

	@FXML
	private Label lblB;

	@FXML
	private Button histograma;

	@FXML
	private ImageView imageView1;

	@FXML
	private ImageView imageView2;

	@FXML
	private ImageView imageView3;

	@FXML
	private ImageView imgRes;

	@FXML
	private Slider limiarizacao;
	
	@FXML
	private Slider limiarizacaoNovo;

	@FXML
	private Label lblLimiarizacao;

	@FXML
	private TextField txtR;

	@FXML
	private TextField txtG;

	@FXML
	private TextField txtB;

	@FXML
	private RadioButton rdCruz;

	@FXML
	private RadioButton rdX;

	@FXML
	private RadioButton rd3X3;

	@FXML
	private Slider sldAdicaoImg1;

	@FXML
	private Slider sldAdicaoImg2;
	
	@FXML
	private Slider SldDilatacao;
	
	@FXML
	private Slider SldErosao;
	
	@FXML
	private Slider SldDilatacaonovo;
	
	@FXML
	private Slider SldErosaonovo;
	
	
	
	private Image img1;
	private Image img2;
	private Image img3;
	
	

	@FXML
	public void atualizaLabel() {
		Double v = limiarizacao.getValue();
		lblLimiarizacao.setText(v.toString());
	}

	@FXML
	public void cinzaAritmetica() {
		img1 = Pdi.cinzaMediaAritmetica(img1, 0, 0, 0);
		atualizaImagem1();
	}

	@FXML
	public void cinzaPonderada() {
		int pr, pg, pb;
		pr = Integer.parseInt(txtR.getText().toString()) / 3;
		pg = Integer.parseInt(txtG.getText().toString()) / 3;
		pb = Integer.parseInt(txtB.getText().toString()) / 3;

		img3 = Pdi.cinzaMediaAritmetica(img1, pr, pg, pb);
		atualizaImagem3();
	}

	@FXML
	public void limiarizarImagem() {
		img1 = Pdi.limiarizacao(img1, limiarizacao.getValue());
		atualizaImagem1();
	}
	
	@FXML
	public void limiarizarImagemNovo() {
		img3 = Pdi.limiarizacao(img1, limiarizacaoNovo.getValue());
		atualizaImagem3();
	}

	@FXML
	public void negativa() {
		img1 = Pdi.negativa(img1);
		atualizaImagem1();
	}

	@FXML
	public void ruidos() {

		int op = 3;

		if (rd3X3.isSelected())
			op = 1;
		if (rdCruz.isSelected())
			op = 2;
		if (rdX.isSelected())
			op = 3;

		img1 = Pdi.ruidos(img1, op);
		atualizaImagem1();
	}

	@FXML
	public void adicao() {
		img3 = Pdi.adicao(img1, img2, sldAdicaoImg1.getValue(), sldAdicaoImg2.getValue());
		atualizaImagem3();
	}

	@FXML
	public void subtracao() {
		img3 = Pdi.subtracao(img1, img2);
		atualizaImagem3();
	}

	@FXML
	public void rasterImg(MouseEvent evt) {
		ImageView iw = (ImageView) evt.getTarget();
		if (iw.getImage() != null)
			verificaCor(iw.getImage(), (int) evt.getX(), (int) evt.getY());
	}

	private void verificaCor(Image img, int x, int y) {
		try {
			Color cor = img.getPixelReader().getColor(x - 1, y - 1); // Recebe o pixel que o mouse está passando
			lblR.setText("R: " + (int) (cor.getRed() * 255));
			lblG.setText("G: " + (int) (cor.getGreen() * 255));
			lblB.setText("B: " + (int) (cor.getBlue() * 255));
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	@FXML
	public void abreImagem1() {
		img1 = abreImagem(imageView1, img1);
	}

	@FXML
	public void abreImagem2() {
		img2 = abreImagem(imageView2, img2);
	}

	@FXML
	public void atualizaImagem3() {
		imageView3.setImage(img3);
		imageView3.setFitWidth(img3.getWidth());
		imageView3.setFitHeight(img3.getHeight());
	}

	@FXML
	public void atualizaImagem1() {
		imageView1.setImage(img1);
		imageView1.setFitWidth(img1.getWidth());
		imageView1.setFitHeight(img1.getHeight());
	}

	private Image abreImagem(ImageView imageView, Image image) {
		File f = selecionaImagem();
		if (f != null) {
			image = new Image(f.toURI().toString());
			imageView.setImage(image);
			imageView.setFitWidth(image.getWidth());
			imageView.setFitHeight(image.getHeight());
			return image;
		}
		return null;
	}

	private File selecionaImagem() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.jpg", "*.JPG", "*.png",
				"*.PNG", "*.gif", "*.GIF", "*.bmp", "*.BMP"));
		fileChooser.setInitialDirectory(new File("C:\\Users\\danil\\Imagens"));
		File imgSelec = fileChooser.showOpenDialog(null);
		try {
			if (imgSelec != null) {
				return imgSelec;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void salvar() {
		if (img3 != null) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.jpg", "*.JPG", "*.png",
					"*.PNG", "*.gif", "*.GIF", "*.bmp", "*.BMP"));
			fileChooser.setInitialDirectory(new File("C:\\Users\\danil\\Imagens"));
			File file = fileChooser.showOpenDialog(null);
			if (file != null) {
				BufferedImage bImg = SwingFXUtils.fromFXImage(img3, null);
				try {
					ImageIO.write(bImg, "PNG", file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			exibeMsg("Salvar imagem", "Não é possível salvar a imagem.", "Não há nenhuma imagem modificada",
					AlertType.ERROR);
		}
	}
	
	public void salvarimg1() {
		if (img1 != null) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.jpg", "*.JPG", "*.png",
					"*.PNG", "*.gif", "*.GIF", "*.bmp", "*.BMP"));
			fileChooser.setInitialDirectory(new File("C:\\Users\\danil\\Imagens"));
			File file = fileChooser.showOpenDialog(null);
			if (file != null) {
				BufferedImage bImg = SwingFXUtils.fromFXImage(img1, null);
				try {
					ImageIO.write(bImg, "PNG", file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			exibeMsg("Salvar imagem", "Não é possível salvar a imagem.", "Não há nenhuma imagem modificada",
					AlertType.ERROR);
		}
	}

	private void exibeMsg(String string, String string2, String string3, AlertType error) {
		// TODO Auto-generated method stub

	}

	double vetor[] = { 0, 0, 0, 0 };

	@FXML
	public void marcaImagem(MouseEvent evt) {

		ImageView iw = (ImageView) evt.getTarget();
		if (iw.getImage() != null) {
			verificaCor(iw.getImage(), (int) evt.getX(), (int) evt.getY());
			if (vetor[0] == 0) {
				vetor[0] = evt.getX();
				vetor[1] = evt.getY();
			} else {
				vetor[2] = evt.getX();
				vetor[3] = evt.getY();

				img1 = Pdi.marca(img1, vetor[0], vetor[1], vetor[2], vetor[3]);
				atualizaImagem1();

			/*	System.out.println("x1: " + vetor[0]);
				System.out.println("y1: " + vetor[1]);
				System.out.println("x2: " + vetor[2]);
				System.out.println("y2: " + vetor[3]); */

				vetor[0] = 0;
				vetor[1] = 0;
				vetor[2] = 0;
				vetor[3] = 0;
			}

		}
	}

	@FXML
	public void abreModalHistograma(ActionEvent event) {
		try {

			/* Abrindo o modal */
			Stage stage = new Stage();
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Modal.fxml"));
			Parent root = loader.load();
			stage.setScene(new Scene(root));
			stage.setTitle("Histograma");
			// stage.initModality(Modality.WINDOW_MODAL);
			// stage.initOwner(((Node)event.getSource()).getScene().getWindow());
			stage.show();

			/* Controlador da janela modal */
			ModalHistogramaController controller = (ModalHistogramaController) loader.getController();

			/* Se a imagem estiver carregada, ele traz o histograma */
			if (img1 != null)
				Pdi.getGrafico(img1, controller.grafico1);
			if (img2 != null)
				Pdi.getGrafico(img2, controller.grafico2);
			if (img3 != null)
				Pdi.getGrafico(img3, controller.grafico3);

		} catch (Exception e) {

		}
	}
	
	
	@FXML
	public void equalizacao() {
		img1 = Pdi.equalizacaoHistograma(img1, true);
		atualizaImagem1();
		int[] vetor = {50, 120, 12, 45, 0, 12, 11, 0, 5, 6, 82, 36, 57};
		histogramaAc(vetor);
	}
	
	@FXML
	public void equalizacaoValidos() {
		img3 = Pdi.equalizacaoHistograma(img1, false);
		atualizaImagem3();
	}
	
	@FXML
	public void blur() {
		img1 = Pdi.blur(img1);
		atualizaImagem1();
	}
	
	@FXML
	public void blurGaussiano() {
		img1 = Pdi.blurGaussiano(img1);
		atualizaImagem1();
	}
	
	@FXML
	public void canny() {
		img1 = Pdi.canny(img1);
		atualizaImagem1();
	}
	
	@FXML
	public void sobel() {
		img1 = Pdi.canny(img1);
		atualizaImagem1();
	}
	
	@FXML
	public void dilatacao() {
		img1 = Pdi.dilatacao(img1, (int)SldDilatacao.getValue());
		atualizaImagem1();
	}
	
	@FXML
	public void dilatacaonovo() {
		img3 = Pdi.dilatacao(img1, (int)SldDilatacaonovo.getValue());
		atualizaImagem3();
	}
	
	@FXML
	public void erosao() {
		img1 = Pdi.erosao(img1, (int)SldErosao.getValue());
		atualizaImagem1();
	}
	
	@FXML
	public void erosaonovo() {
		img3 = Pdi.erosao(img1, (int)SldErosaonovo.getValue());
		atualizaImagem3();
	}
	
	
	
	
	/* Retorna o Histograma Acumulado */
	public static int[] histogramaAc(int[] hist) {
		
		int histAc[] = new int[hist.length];
		
		for(int i=0; i<hist.length; i++) {
			if(i>0) {
				System.out.println("na posição "+i+" recebe "+hist[i]+" "+" "+hist[i-1]);
				histAc[i] = hist[i]+histAc[i-1];
			} else {
				histAc[i] = hist[i];
				System.out.println("nn é maior que zero, e na posição "+i+" recebe "+hist[i]);
			}
		}
		/* System.out.println("histograma acumulado: \n");
		for(int i=0; i<histAc.length; i++) {
		System.out.println(histAc[i]+" "); 
		}*/
		
		return histAc;
	
	}

}
