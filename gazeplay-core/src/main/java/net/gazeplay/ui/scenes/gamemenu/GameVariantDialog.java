package net.gazeplay.ui.scenes.gamemenu;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameSpec;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.I18NLabel;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.components.CssUtil;

import static javafx.scene.input.MouseEvent.MOUSE_CLICKED;

@Slf4j
public class GameVariantDialog extends Stage {

    private boolean easymode = false;

    public GameVariantDialog(
        final GazePlay gazePlay,
        final GameMenuController gameMenuController,
        final Stage primaryStage,
        final GameSpec gameSpec,
        final Parent root,
        final String chooseVariantPromptLabelTextKey
    ) {

        initModality(Modality.WINDOW_MODAL);
        initOwner(primaryStage);
        initStyle(StageStyle.UTILITY);
        setOnCloseRequest(windowEvent -> {
            primaryStage.getScene().getRoot().setEffect(null);
            root.setDisable(false);
        });

        FlowPane choicePane = new FlowPane();
        choicePane.setAlignment(Pos.CENTER);
        choicePane.setHgap(10);
        choicePane.setVgap(10);

        FlowPane choicePaneEasy = new FlowPane();
        choicePaneEasy.setAlignment(Pos.CENTER);
        choicePaneEasy.setHgap(10);
        choicePaneEasy.setVgap(10);

        ScrollPane choicePanelScroller = new ScrollPane();
        choicePanelScroller.setContent(choicePane);
        //choicePanelScroller.setMinHeight(primaryStage.getHeight() / 5);
        //choicePanelScroller.setMinWidth(primaryStage.getWidth() / 5);
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);
        choicePanelScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        final String labelStyle = "-fx-font-weight: bold; -fx-font-size: 24; -fx-text-fill: black;";
        I18NLabel chooseVariantPromptLabel = new I18NLabel(gazePlay.getTranslator(), chooseVariantPromptLabelTextKey);
        chooseVariantPromptLabel.setStyle(labelStyle);

        VBox topPane = new VBox();
        topPane.setAlignment(Pos.CENTER);
        topPane.getChildren().add(chooseVariantPromptLabel);

        BorderPane sceneContentPane = new BorderPane();
        sceneContentPane.setTop(topPane);
        sceneContentPane.setCenter(choicePanelScroller);

        final Configuration config = ActiveConfigurationContext.getInstance();

        final Translator translator = gazePlay.getTranslator();

        for (GameSpec.GameVariant variant : gameSpec.getGameVariantGenerator().getVariants()) {
            Button button = new Button(variant.getLabel(translator));
            button.getStyleClass().add("gameChooserButton");
            button.getStyleClass().add("gameVariation");
            button.getStyleClass().add("button");
            //
            button.wrapTextProperty().setValue(true);
            //
            button.setMinWidth(primaryStage.getWidth() / 15);
            button.setMinHeight(primaryStage.getHeight() / 15);
            //
            button.setPrefWidth(primaryStage.getWidth() / 10);
            button.setPrefHeight(primaryStage.getHeight() / 10);
            //
            button.setMaxWidth(primaryStage.getWidth() / 8);
            button.setMaxHeight(primaryStage.getHeight() / 8);
            //
            if (variant instanceof GameSpec.DimensionDifficultyGameVariant) {
                choicePaneEasy.getChildren().add(button);
            } else {
                choicePane.getChildren().add(button);
            }

            if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheColor")) {
                if (variant instanceof GameSpec.DimensionGameVariant) {
                    variant = new GameSpec.DimensionDifficultyGameVariant(((GameSpec.DimensionGameVariant) variant).getWidth(), ((GameSpec.DimensionGameVariant) variant).getHeight(), "normal");
                }
                ToggleGroup group = new ToggleGroup();
                RadioButton normal = new RadioButton("normal");
                normal.setToggleGroup(group);
                normal.setSelected(true);
                RadioButton facile = new RadioButton("easy");
                facile.setToggleGroup(group);
                HBox bottom = new HBox();
                bottom.getChildren().add(facile);
                bottom.getChildren().add(normal);
                sceneContentPane.setBottom(bottom);
                facile.setOnAction(actionEvent -> {
                    if (!easymode) {
                        easymode = true;
                        choicePanelScroller.setContent(choicePaneEasy);
                    }
                });
                normal.setOnAction(actionEvent -> {
                    if (easymode) {
                        easymode = false;
                        choicePanelScroller.setContent(choicePane);
                    }
                });
            }

            GameSpec.GameVariant finalVariant = variant;
            EventHandler<Event> event = mouseEvent -> {
                close();
                root.setDisable(false);
                gameMenuController.chooseGame(gazePlay, gameSpec, finalVariant);
            };
            button.addEventHandler(MOUSE_CLICKED, event);

        }


        Scene scene = new Scene(sceneContentPane, Color.TRANSPARENT);

        CssUtil.setPreferredStylesheets(config, scene, gazePlay.getCurrentScreenDimensionSupplier());

        setScene(scene);
        setWidth(primaryStage.getWidth() / 2);
        setHeight(primaryStage.getHeight() / 2);
        // scene.getStylesheets().add(getClass().getResource("modal-dialog.css").toExternalForm());
    }

    public boolean isEasymode() {
        return easymode;
    }

}
