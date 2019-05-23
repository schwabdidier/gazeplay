package net.gazeplay.games.literacy;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.Parent;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

public class Bloc extends Parent {// Rectangle {

    private static final float zoom_factor = 1.05f;
    private final double fixationlength;

    private final String letterStr;
    private final Text letter;

    @Getter
    private final boolean isMainLetter;

    private final int posX;
    private final int posY;

    private final double width;
    private final double height;

    private final Rectangle bloc;
    final StackPane stack;

    private final Letters gameInstance;

    private Timeline timelineProgressBar;

    final EventHandler<Event> enterEvent;
    final Stats stats;
    private final GameContext gameContext;

    private Timeline currentTimeline;

    public Bloc(double x, double y, double width, double height, int posX, int posY, String currentLetter,
            boolean isMainLetter, Letters gameInstance, Stats stats, GameContext gameContext, int fixationlength) {

        this.gameInstance = gameInstance;
        this.stats = stats;
        this.gameContext = gameContext;

        this.fixationlength = fixationlength;

        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;

        this.bloc = new Rectangle(x, y, width, height);

        this.letterStr = currentLetter;
        this.isMainLetter = isMainLetter;

        this.letter = new Text("" + letterStr.toUpperCase());
        this.letter.setFont(new Font("Tsukushi A Round Gothic Bold", 250));

        this.bloc.setFill(new Color(Math.random(), Math.random(), Math.random(), 1));

        this.stack = new StackPane();
        this.stack.getChildren().addAll(bloc, letter);

        stack.setLayoutX(x);
        stack.setLayoutY(y);

        this.getChildren().add(stack);

        this.enterEvent = buildEvent();

        gameContext.getGazeDeviceManager().addEventFilter(bloc);

        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);

        currentTimeline = new Timeline();

    }

    private void onCorrectBlocSelected() {
        System.out.println("WAFAAAA onCorrectBlocSelected");

        javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        stats.incNbGoals();

        double final_zoom = 1.15;

        // progressIndicator.setOpacity(0);
        letter.setOpacity(0);
        // NEED TO PLAY AUDIO SOMEWHERE HERE!

        gameInstance.currentRoundDetails.remainingCount--;

        if (gameInstance.currentRoundDetails.remainingCount == 0) {
            // REMOVE ALL CARDS AND REVEAL THE IMAGE
            gameInstance.removeAllBlocs();
        } else {
            gameInstance.removeBloc(this);
        }

        playSound("" + this.gameInstance.currentLanguage, this.letterStr);

        currentTimeline.stop();
        currentTimeline = new Timeline();

        currentTimeline.getKeyFrames().add(
                new KeyFrame(new Duration(1000), new KeyValue(bloc.widthProperty(), bloc.getWidth() * final_zoom)));
        currentTimeline.getKeyFrames().add(
                new KeyFrame(new Duration(1000), new KeyValue(bloc.heightProperty(), bloc.getHeight() * final_zoom)));
        currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(bloc.xProperty(), 0)));

        currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(bloc.yProperty(), 0)));

        // currentTimeline.onFinishedProperty().set(new EventHandler<ActionEvent>() {
        if (gameInstance.currentRoundDetails.remainingCount == 0) {
            currentTimeline.onFinishedProperty().set(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {

                    gameContext.playWinTransition(500, new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent actionEvent) {
                            gameInstance.dispose();

                            gameContext.clear();

                            gameInstance.launch();

                            stats.notifyNewRoundReady();

                            gameContext.onGameStarted();
                        }
                    });
                }
            });
        }

        currentTimeline.play();
    }

    private String createSoundPath(String currentLanguage, String currentLetter) {
        return "data/literacy/sounds/" + currentLanguage.toLowerCase() + "/" + currentLanguage.toLowerCase() + "_"
                + currentLetter.toUpperCase() + ".mp3";

    }

    private void playSound(String currentLanguage, String currentLetter) {
        String path = "data/literacy/sounds/" + currentLanguage.toLowerCase() + "/" + currentLanguage.toLowerCase()
                + "_" + currentLetter.toUpperCase() + ".mp3";
        try {
            // log.debug("Letter sound path {}", path);
            Utils.playSound(path);
        } catch (Exception e) {
            // log.warn("Can't play sound: no associated sound : " + e.toString());
        }

    }

    private void onWrongBlocSelected() {

        currentTimeline.stop();
        currentTimeline = new Timeline();

        currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(2000), new KeyValue(bloc.opacityProperty(), 0.9)));

        currentTimeline.play();

    }

    private EventHandler<Event> buildEvent() {

        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                    // progressIndicator.setOpacity(1);
                    // progressIndicator.setProgress(0);

                    currentTimeline.stop();
                    currentTimeline = new Timeline();

                    currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(bloc.xProperty(), bloc.getX() - (width * zoom_factor - width) / 2)));
                    currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(bloc.yProperty(), bloc.getY() - (height * zoom_factor - height) / 2)));
                    currentTimeline.getKeyFrames().add(
                            new KeyFrame(new Duration(1), new KeyValue(bloc.widthProperty(), width * zoom_factor)));
                    currentTimeline.getKeyFrames().add(
                            new KeyFrame(new Duration(1), new KeyValue(bloc.heightProperty(), height * zoom_factor)));

                    timelineProgressBar = new Timeline();

                    timelineProgressBar.getKeyFrames()
                            .add(new KeyFrame(new Duration(fixationlength), new KeyValue(bloc.widthProperty(), width)));

                    currentTimeline.play();

                    timelineProgressBar.play();

                    // System.out.println("WAFAAAA timelineProgressBar");

                    timelineProgressBar.setOnFinished(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent actionEvent) {
                            // System.out.println("WAFAAAA timelineProgressBar.setOnFinished");
                            // turned = true;
                            //
                            // card.setFill(new ImagePattern(image, 0, 100, 1, 1, true));

                            bloc.removeEventFilter(MouseEvent.ANY, enterEvent);
                            bloc.removeEventFilter(GazeEvent.ANY, enterEvent);

                            if (isMainLetter) {
                                onCorrectBlocSelected();
                            } else {// bad card
                                onWrongBlocSelected();
                            }
                        }
                    });
                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                    currentTimeline.stop();
                    currentTimeline = new Timeline();

                    currentTimeline.getKeyFrames().add(
                            new KeyFrame(new Duration(1), new KeyValue(bloc.xProperty(), bloc.getX() + (width) / 2)));
                    currentTimeline.getKeyFrames().add(
                            new KeyFrame(new Duration(1), new KeyValue(bloc.yProperty(), bloc.getY() + (height) / 2)));
                    currentTimeline.getKeyFrames()
                            .add(new KeyFrame(new Duration(1), new KeyValue(bloc.widthProperty(), width)));
                    currentTimeline.getKeyFrames()
                            .add(new KeyFrame(new Duration(1), new KeyValue(bloc.heightProperty(), height)));

                    // Be sure that the card is properly positionned at the end
                    currentTimeline.setOnFinished((event) -> {
                        bloc.setX(posX);
                        bloc.setY(posY);
                    });

                    currentTimeline.play();

                    timelineProgressBar.stop();

                    // progressIndicator.setOpacity(0);
                    // progressIndicator.setProgress(0);
                }
            }
        };
    }
}