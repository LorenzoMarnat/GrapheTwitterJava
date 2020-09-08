package main;
import javafx.application.Application;
import javafx.stage.Stage;
import main.reader.CSVReader;
import main.scene.Interface;


public class Main extends Application{
	private static CSVReader reader;
	
	public static void main(String[] args) {

		reader = new CSVReader("data/FootVeryShort.txt", "\t");

		reader.readCSV();
		
		Application.launch(Main.class,args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		Interface.launchInterface(stage);
	}
}
