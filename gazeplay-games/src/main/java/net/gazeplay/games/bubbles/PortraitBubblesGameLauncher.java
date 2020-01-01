package net.gazeplay.games.bubbles;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public class PortraitBubblesGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.EnumGameVariant<BubblesGameVariant>> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new BubblesGamesStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.EnumGameVariant<BubblesGameVariant> gameVariant,
                                       Stats stats) {
        return new Bubble(gameContext, BubbleType.PORTRAIT, stats, false,gameVariant.getEnumValue());
    }
}