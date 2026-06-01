package com.bismillahjuara.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.bismillahjuara.game.audio.AudioManager;
import com.bismillahjuara.game.screens.BootLoadingScreen;
import com.bismillahjuara.game.screens.ScreenManager;
import com.bismillahjuara.game.screens.SplashScreen;
import com.bismillahjuara.game.screens.StoryIntroScreen;
import com.bismillahjuara.game.screens.StreamingLoadingOverlay;
import com.bismillahjuara.game.settings.SettingsManager;
import com.bismillahjuara.game.transitions.FadeTransition;
import com.bismillahjuara.game.ui.MenuUIManager;

public class Main extends Game {

    @Override
    public void create() {
        SettingsManager.getInstance().applyToEngine();

        ScreenManager.getInstance().initialize(this);

        ScreenManager.getInstance().setScreen(new SplashScreen(), null);
    }

    @Override
    public void render() {


        float delta = Gdx.graphics.getDeltaTime();
        AudioManager.getInstance().update(delta);


        ScreenManager.getInstance().render(Gdx.graphics.getDeltaTime());

    }

    @Override
    public void dispose() {
        ScreenManager.getInstance().dispose();
        MenuUIManager.getInstance().dispose();
    }
}
