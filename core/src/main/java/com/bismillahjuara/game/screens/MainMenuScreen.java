package com.bismillahjuara.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.bismillahjuara.game.transitions.FadeTransition;
import com.bismillahjuara.game.ui.AnimatedButton;
import com.bismillahjuara.game.ui.MenuUIManager;

public class MainMenuScreen extends BaseScreen {

    private Table mainTable;
    private Skin skin;

    public MainMenuScreen() {
        super();
        this.skin = MenuUIManager.getInstance().skin;
        setupUI();
        animateEntrance();
    }

    private void setupUI() {
        // TODO: Nanti di sini pasang Parallax Background Image atau Video Cinematic

        mainTable = new Table();
        mainTable.setFillParent(true);
        // Menu ala AAA RPG modern biasanya condong di kiri/kanan, kita taruh kiri dengan padding.
        mainTable.left().padLeft(150);

        // Judul Game
        Label title = new Label("TIMUN\nThe Soil of Debt", skin, "title");
        title.setAlignment(Align.left);
        mainTable.add(title).padBottom(80).left().row();

        // Pembuatan Tombol menggunakan komponen cerdas kita
        addMenuButton("New Game", new Runnable() {
            @Override public void run() { startGame(); }
        });

        addMenuButton("Continue", new Runnable() {
            @Override public void run() { /* TODO: Load Save Game */ }
        });

        addMenuButton("Story Log", new Runnable() {
            @Override public void run() { ScreenManager.getInstance().setScreen(new StoryMenuScreen(), new FadeTransition(0.5f)); }
        });

        addMenuButton("Settings", new Runnable() {
            @Override public void run() { ScreenManager.getInstance().setScreen(new SettingsScreen(), new FadeTransition(0.5f)); }
        });

        addMenuButton("Credits", new Runnable() {
            @Override public void run() { ScreenManager.getInstance().setScreen(new CreditsScreen(), new FadeTransition(0.5f)); }
        });

        addMenuButton("Exit Nightmare", new Runnable() {
            @Override public void run() { Gdx.app.exit(); }
        });

        stage.addActor(mainTable);
    }

    private void addMenuButton(String text, final Runnable action) {
        AnimatedButton btn = new AnimatedButton(text, skin, "default");
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                action.run();
            }
        });

        // Setup layout per tombol
        mainTable.add(btn).size(300, 70).padBottom(15).left().row();
    }

    private void animateEntrance() {
        // Efek UI Masuk: Slide In dari kiri dengan Cascade (berurutan)
        float delay = 0f;
        for (com.badlogic.gdx.scenes.scene2d.Actor actor : mainTable.getChildren()) {
            actor.addAction(Actions.sequence(
                Actions.alpha(0f), // Mulai transparan
                Actions.moveBy(-50f, 0f), // Geser kiri 50px
                Actions.delay(delay),
                Actions.parallel(
                    Actions.fadeIn(0.5f, Interpolation.fade),
                    Actions.moveBy(50f, 0f, 0.5f, Interpolation.circleOut) // Memantul elegan
                )
            ));
            delay += 0.1f; // Tombol berikutnya telat 0.1 detik (Efek Cascade/Tarik beruntun)
        }
    }

    private void startGame() {
        ScreenManager.getInstance().setScreen(new StoryIntroScreen(), new FadeTransition(1.0f));
    }
}
