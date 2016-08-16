package util.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

	private List<String> fileList;
	private String output_zip_file;
	private String source_folder; // SourceFolder path

	public ZipUtils(String source_folder,String zipFolder) {
		fileList = new ArrayList<String>();
		this.source_folder = source_folder;
		output_zip_file=zipFolder;
	}

	public static void main(String[] args) {
		ZipUtils appZip = new ZipUtils("eyeLog","Nada");
		appZip.generateFileList(new File("eyeLog"));
		appZip.zipIt();
	}

	public void zipIt() {
		byte[] buffer = new byte[1024];
		String source = "";
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		try {
			try {

				source = source_folder.substring(source_folder.lastIndexOf("\\") + 1, source_folder.length());
				
			} catch (Exception e) {
				source = source_folder;
			}
			fos = new FileOutputStream(output_zip_file);
			zos = new ZipOutputStream(fos);

			// System.out.println("Output to Zip : " + output_zip_file);
			FileInputStream in = null;

			for (String file : this.fileList) {
				// System.out.println("File Added : " + file);
				ZipEntry ze = new ZipEntry(source + File.separator + file);
				zos.putNextEntry(ze);
				try {
					in = new FileInputStream(source_folder + File.separator + file);
					int len;
					while ((len = in.read(buffer)) > 0) {
						zos.write(buffer, 0, len);
					}
				} finally {
					in.close();
				}
			}

			zos.closeEntry();

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				zos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void generateFileList(File node) {

		// add file only
		if (node.isFile()) {
			fileList.add(generateZipEntry(node.toString()));

		}

		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				generateFileList(new File(node, filename));
			}
		}
	}

	private String generateZipEntry(String file) {
		return file.substring(source_folder.length() + 1, file.length());
	}
}