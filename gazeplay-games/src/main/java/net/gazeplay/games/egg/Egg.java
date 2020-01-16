package net.gazeplay.games.egg;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.games.ForegroundSoundsUtils;
import net.gazeplay.commons.utils.stats.Stats;


@Slf4j
public class Egg extends Parent {

    private final double fixationlength;

    private final StackPane cards;

    private final IGameContext gameContext;

    private final EggGame gameInstance;

    private int turnNumber = 0;
    private final int totalNumberOfTurns;

    private final ProgressIndicator progressIndicator;

    private Timeline timelineProgressBar;
    private final Stats stats;

    private final EventHandler<Event> enterEvent;

    public Egg(double positionX, double positionY, double width, double height, IGameContext gameContext, Stats stats,
               EggGame gameInstance, int fixationlength, int numberOfTurn) {

        this.totalNumberOfTurns = numberOfTurn;

        this.cards = new StackPane();
        this.cards.setLayoutX(positionX);
        this.cards.setLayoutY(positionY);
        this.cards.setPrefWidth(width);
        this.cards.setPrefHeight(height);

        Rectangle image1 = new Rectangle(positionX, positionY, width, height);
        image1.setFill(new ImagePattern(new Image("data/egg/images/egg1.jpg"), 0, 0, 1, 1, true));

        Rectangle image2 = new Rectangle(positionX, positionY, width, height);
        image2.setFill(new ImagePattern(new Image("data/egg/images/egg2.jpg"), 0, 0, 1, 1, true));

        Rectangle image3 = new Rectangle(positionX, positionY, width, height);
        image3.setFill(new ImagePattern(new Image("data/egg/images/egg3.jpg"), 0, 0, 1, 1, true));

        this.cards.getChildren().addAll(image3, image2, image1);

        this.gameContext = gameContext;

        this.stats = stats;

        this.fixationlength = fixationlength;

        this.gameInstance = gameInstance;

        this.getChildren().add(cards);

        this.progressIndicator = createProgressIndicator(width / 2, height / 2);
        this.getChildren().add(this.progressIndicator);

        this.enterEvent = buildEvent();

        gameContext.getGazeDeviceManager().addEventFilter(cards);

        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);

    }

    private ProgressIndicator createProgressIndicator(double width, double height) {
        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(cards.getLayoutX() + width / 2);
        indicator.setTranslateY(cards.getLayoutY() + height / 2);
        indicator.setMinWidth(width);
        indicator.setMinHeight(width);
        indicator.setOpacity(0);
        return indicator;
    }

    private EventHandler<Event> buildEvent() {
        Egg that = this;

        return e -> {

            if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                if (turnNumber < totalNumberOfTurns) {

                    progressIndicator.setOpacity(0.5);
                    progressIndicator.setProgress(0);

                    timelineProgressBar = new Timeline();

                    timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(fixationlength),
                        new KeyValue(progressIndicator.progressProperty(), 1)));

                    timelineProgressBar.setOnFinished(actionEvent -> {

                        if (turnNumber < totalNumberOfTurns - 1) {

                            turnNumber++;
                            cards.getChildren().get(2).setOpacity(1 - turnNumber / (float) (totalNumberOfTurns - 1));
                            stats.incNbGoals();
                            playSound(1);

                        } else if (turnNumber == totalNumberOfTurns - 1) {

                            turnNumber++;
                            gameContext.getGazeDeviceManager().removeEventFilter(cards);
                            that.removeEventFilter(MouseEvent.ANY, enterEvent);
                            that.removeEventFilter(GazeEvent.ANY, enterEvent);

                            cards.getChildren().get(1).setOpacity(0);

                            progressIndicator.setOpacity(0);
                            stats.notifyNewRoundReady();
                            stats.incNbGoals();
                            playSound(2);

                            PauseTransition t = new PauseTransition(Duration.seconds(2));

                            t.setOnFinished(actionEvent1 -> {

                                ForegroundSoundsUtils.stopSound();

                                gameInstance.dispose();

                                gameContext.showRoundStats(stats, gameInstance);

                            });

                            t.play();

                        }
                    });

                    timelineProgressBar.play();
                }
            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                timelineProgressBar.stop();
                progressIndicator.setOpacity(0);
                progressIndicator.setProgress(0);
            }
        };
    }

    public void playSound(int i) {
        String soundResource = "data/egg/sounds/" + i + ".mp3";
        try {
            ForegroundSoundsUtils.playSound(soundResource);
        } catch (Exception e) {

            log.warn("file doesn't exist : {}", soundResource);
            log.warn(e.getMessage());
        }
    }

}