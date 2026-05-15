package com.bismillahjuara.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.bismillahjuara.game.screens.GameScreen;
import com.bismillahjuara.game.screens.ScreenManager;
import com.bismillahjuara.game.screens.SplashScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {


//    @Override
//    public void create() {
//        setScreen(new GameScreen());
//    }

    @Override
    public void create() {
        // 1. Inisialisasi ScreenManager (Hanya sekali seumur hidup aplikasi)
        ScreenManager.getInstance().initialize(this);

        // 2. Lempar pemain langsung ke Splash Screen (Tanpa transisi karena baru buka app)
        ScreenManager.getInstance().setScreen(new SplashScreen(), null);
    }

    @Override
    public void render() {
        // 3. JANGAN LUPA: Panggil render milik ScreenManager, bukan super.render() lagi
        ScreenManager.getInstance().render(Gdx.graphics.getDeltaTime());
    }

//    @Override
//    public void render() {
//        super.render();
//    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
