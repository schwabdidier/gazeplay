package net.gazeplay;

import com.sun.glass.ui.Screen;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.configuration.Configuration;
import net.gazeplay.commons.gaze.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.HomeUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by schwab on 17/12/2016.
 */
@Slf4j
public class GazePlay extends Application {

	@Data
	public static class HomeMenuScreen {

		@Getter
		private final Scene scene;

		@Getter
		private final Group root;

		@Getter
		private final ChoiceBox<String> cbxGames;

		private final List<GameSpec> games;

		private final GamesLocator gamesLocator;

		public HomeMenuScreen(final Configuration config) {

			gamesLocator = new DefaultGamesLocator();
			games = gamesLocator.listGames();

			root = new Group();

			final Screen screen = Screen.getScreens().get(0);
			log.info("Screen size: {} x {}", screen.getWidth(), screen.getHeight());

			scene = new Scene(root, screen.getWidth(), screen.getHeight(), Color.BLACK);

			ObservableList<String> stylesheets = scene.getStylesheets();
			stylesheets.add(config.getCssfile());
			Utils.addStylesheets(stylesheets);
			log.info(stylesheets.toString());

			// end of System information
			for (int i = 0; i < 5; i++) {
				log.info("***********************");
			}

			cbxGames = createChoiceBox(games, config);

			HomeUtils.goHome(scene, root, cbxGames);
		}

		public void setUpHomeMenuScreen(Stage primaryStage) {
			primaryStage.setTitle("GazePlay");
			primaryStage.setFullScreen(true);
			primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));
			primaryStage.setScene(scene);
			primaryStage.show();
			// SecondScreen secondScreen = SecondScreen.launch();
		}

		public void onLanguageChanged() {
			final Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

			final List<String> gamesLabels = generateTranslatedGamesNames(games, config);

			this.cbxGames.getItems().clear();
			this.cbxGames.getItems().addAll(gamesLabels);
		}

		/**
		 * This command is called when games have to be updated (example: when language changed)
		 */
		private ChoiceBox createChoiceBox(List<GameSpec> games, Configuration config) {
			List<String> gamesLabels = generateTranslatedGamesNames(games, config);

			ChoiceBox<String> cbxGames = new ChoiceBox<>();
			cbxGames.getItems().addAll(gamesLabels);
			cbxGames.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					chooseGame(newValue.intValue());
				}
			});
			return cbxGames;
		}

		private List<String> generateTranslatedGamesNames(List<GameSpec> games, Configuration config) {
			final String language = config.getLanguage();
			final Multilinguism multilinguism = Multilinguism.getSingleton();

			return games.stream()
					.map(gameSpec -> new Pair<>(gameSpec, multilinguism.getTrad(gameSpec.getNameCode(), language)))
					.map(pair -> {
						String variationHint = pair.getKey().getVariationHint();
						if (variationHint == null) {
							return pair.getValue();
						}
						return pair.getValue() + " " + variationHint;
					}).collect(Collectors.toList());
		}

		private void chooseGame(int gameIndex) {
			log.info("Game number: " + gameIndex);

			HomeUtils.clear(scene, root);

			if (gameIndex == -1) {
				return;
			}

			GameSpec selectedGameSpec = games.get(gameIndex);

			log.info(selectedGameSpec.getNameCode() + " " + selectedGameSpec.getVariationHint());

			final Stats stats = selectedGameSpec.launch(scene, root, cbxGames);

			HomeUtils.home(scene, root, cbxGames, stats);
		}

	}

	@Getter
	private static GazePlay instance;

	@Getter
	private HomeMenuScreen homeMenuScreen;

	public GazePlay() {
		instance = this;
	}

	@Override
	public void start(Stage primaryStage) {
		homeMenuScreen = new HomeMenuScreen(ConfigurationBuilder.createFromPropertiesResource().build());
		homeMenuScreen.setUpHomeMenuScreen(primaryStage);
	}

}
