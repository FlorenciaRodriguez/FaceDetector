import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
import static org.bytedeco.javacpp.opencv_core.cvGetSeqElem;
import static org.bytedeco.javacpp.opencv_core.cvLoad;
import static org.bytedeco.javacpp.opencv_core.cvPoint;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.CV_AA;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.cvRectangle;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_ROUGH_SEARCH;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_FIND_BIGGEST_OBJECT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvScalar;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameConverter;

import util.email.SendEmail;
import util.zip.ZipUtils;

public class FaceDetector implements Runnable {
	private String folder;

	/**
	 * Método main
	 * 
	 * @param args[0]
	 *            el path donde se guardarán los datos en la máquina
	 * @param args[1]
	 *            el path del zip de salida
	 * @param args[2]
	 *            el path del cascade
	 **/
	public static void main(String[] args) {
		// String face = args[0];
		FaceDetector faceDetect = new FaceDetector(args[2], args[0], args[1],args[3],args[4],args[5]);
		faceDetect.detect();

	}

	/**
	 * Método principal.
	 * 
	 */
	public void detect() {
		// Para capturar la imagen
		FrameGrabber grabber = null;
		try {
			grabber = FrameGrabber.createDefault(0);
		} catch (Exception e) {
			System.out.println("No se creó el objeto que captura el video");
		}

		CvMemStorage storage = CvMemStorage.create();

		Calendar calendar = Calendar.getInstance();
		Date now = calendar.getTime();

		IplImage grabbedImage = null;
		try {
			grabber.start();
			OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
			grabbedImage = converter.convert(grabber.grab());
		} catch (Exception e) {
			System.out.println("No arrancó la camara");
		}

		calendar = Calendar.getInstance();
		now = calendar.getTime();
		// Datos a guardar en el archivo

		// Guardar la imagen "cruda"
		cvSaveImage(this.folder + "\\img" + "\\imgA.jpg", grabbedImage);
		IplImage grayImage = IplImage.create(grabber.getImageWidth(), grabber.getImageHeight(), IPL_DEPTH_8U, 1);
		try {
			grabber.stop();
		} catch (Exception e1) {
			System.out.println("No se pudo detener el grabber");
		}
		cvCvtColor(grabbedImage, grayImage, CV_BGR2GRAY);

		// Detectar cara
		cvClearMemStorage(storage);

		CvSeq face = cvHaarDetectObjects(grayImage, classifierFace, storage, 1.1, 3,
				CV_HAAR_FIND_BIGGEST_OBJECT | CV_HAAR_DO_ROUGH_SEARCH);
		for (int i = 0; i < face.total(); i++) {
			@SuppressWarnings("resource")
			CvRect r = new CvRect(cvGetSeqElem(face, i));
			int x = r.x(), y = r.y(), w = r.width(), h = r.height();
			cvRectangle(grabbedImage, cvPoint(x, y), cvPoint(x + w, y + h), CvScalar.BLUE, 2, CV_AA, 0);
			// TODO hay cara
			PrintWriter writer;
			try {
				writer = new PrintWriter(this.folder + "\\log.txt", "UTF-8");
				writer.println("Detectó cara a las " + now.toString());
				writer.close();
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// Guardar la imagen con las cosas dibujadas
		cvSaveImage(this.folder + "\\img" + "\\imgB.jpg", grabbedImage);
		cvClearMemStorage(storage);

		zipAndSend();
	}

	private String output;

	/**
	 * @param faceCascade
	 *            el clasificador
	 * @param folderS
	 *            la carpeta donde se guardará el log
	 * @param outpur
	 *            el path de salida
	 * @param userName
	 *            nombre de usuario de Gmail (del que envía)
	 * @param pass
	 *            contraseña (del que envía)
	 * @param recipient
	 *            email del que recibe
	 */
	public FaceDetector(String faceCascade, String folderS, String output,String user,String pass,String rec) {
		// Creación de carpetas y archivos de salida
		this.userName = user;
		this.pass = pass;
		this.recipient = rec;

		classifierFace = new CvHaarClassifierCascade(cvLoad(faceCascade));
		this.folder = folderS;
		if (this.folder == null)
			this.folder = "\\faceDetector\\";
		this.output = output;
		if (this.output == null)
			this.output = "FaceDetector.zip";

		File folder = new File(this.folder + "\\img");
		folder.mkdirs();
		// Librería
		Loader.load(opencv_objdetect.class);
	}

	/**
	 * Clasificador
	 */
	private CvHaarClassifierCascade classifierFace;

	/**
	 * Zip info and send email
	 */
	private void zipAndSend() {
		ZipUtils appZip = new ZipUtils(this.folder, this.output);
		appZip.generateFileList(new File(this.folder));
		appZip.zipIt();
		SendEmail.sendFromGMail(this.output, this.userName, this.pass, this.recipient);
	}

	private String userName;
	private String pass;
	private String recipient;

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}
}
