package com.bismillahjuara.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.bismillahjuara.game.screens.ScreenManager;
import com.bismillahjuara.game.screens.SplashScreen;
import com.bismillahjuara.game.settings.SettingsManager;

public class Main extends Game {

    @Override
    public void create() {
        // 1. Terapkan Settings (Resolusi, FPS, dll) dari Harddisk ke Engine!
        SettingsManager.getInstance().applyToEngine();

        // 2. Inisialisasi Arsitektur Layar
        ScreenManager.getInstance().initialize(this);

        // 3. Masuk Intro
        ScreenManager.getInstance().setScreen(new SplashScreen(), null);
    }

    @Override
    public void render() {
        // Meneruskan perintah render loop ke konduktor layar
        ScreenManager.getInstance().render(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void dispose() {
        // Bersihkan seluruh memori dari ScreenManager dan UIManager
        ScreenManager.getInstance().dispose();
        com.bismillahjuara.game.ui.MenuUIManager.getInstance().dispose();
    }
}
