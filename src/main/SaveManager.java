package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.List;

public abstract class SaveManager {

	public static void save(String filePath, List<String> linesToSave) {
		Path path = Paths.get("save\\" + filePath);
		
		try {
			try {
				Files.createFile(path, new FileAttribute[] {});
			} catch(Exception e) {}
			Files.write(path, linesToSave);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static List<String> load(String filePath) throws IOException {
		Path path = Paths.get("save\\" + filePath);
		
		return Files.readAllLines(path);
	}
}
