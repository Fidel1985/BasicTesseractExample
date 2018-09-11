package com.fidel;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Main {

	public static void main(String[] args) {
		PDDocument document = null;
		System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
		List<String> imageNames = new ArrayList<>();
		Tesseract instance = new Tesseract();
		instance.setDatapath("./tessdata/");
		XWPFDocument wordDoc = new XWPFDocument();

		try (InputStream is = Main.class.getResourceAsStream("/scansione0004.pdf")) {
			FileOutputStream out = new FileOutputStream( new File("test.docx"));
			document = PDDocument.load(is);
			PDFRenderer pdfRenderer = new PDFRenderer(document);
			int pageCounter = 0;
			for (PDPage page : document.getPages())
			{
				BufferedImage bim = pdfRenderer.renderImageWithDPI(pageCounter, 300, ImageType.GRAY);
				String fileName = "example-" + (pageCounter++) + ".png";
				ImageIOUtil.writeImage(bim, fileName, 50);
				imageNames.add(fileName);
				System.out.println("Composed image: " + fileName);
/*				if (pageCounter == 2) {
					break;
				}*/
			}

			for (String imageName: imageNames) {
				File imageFile = new File(imageName);
				String result = instance.doOCR(imageFile);
				XWPFParagraph paragraph = wordDoc.createParagraph();
				paragraph.setPageBreak(true);
				XWPFRun run = paragraph.createRun();
				run.setText(result);
			}
			wordDoc.write(out);
			wordDoc.close();
		} catch (IOException | TesseractException e) {
			e.printStackTrace();
		} finally {
			// Oh, god...
			if (document != null) {
				try {
					document.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

}
