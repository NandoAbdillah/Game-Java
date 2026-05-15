package com.bismillahjuara.game.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.bismillahjuara.game.transitions.FadeTransition;
import com.bismillahjuara.game.ui.AnimatedButton;
import com.bismillahjuara.game.ui.MenuUIManager;

public class CreditsScreen extends BaseScreen {
    public CreditsScreen() {
        super();
        Table t = new Table();
        t.setFillParent(true);

        Label creds = new Label("DIRECTOR: Kamu\nLEAD ENGINEER: BismillahJuara\nENGINE: LibGDX", MenuUIManager.getInstance().skin);
        creds.setAlignment(com.badlogic.gdx.utils.Align.center);

        // Efek scrolling cinematic ke atas
        creds.setPosition(1920/2f, -200f);
        creds.addAction(Actions.moveBy(0, 1500f, 15f));
        stage.addActor(creds);

        AnimatedButton backBtn = new AnimatedButton("Skip / Return", MenuUIManager.getInstance().skin, "default");
        backBtn.setPosition(50, 50);
        backBtn.setSize(200, 60);
        backBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                ScreenManager.getInstance().setScreen(new MainMenuScreen(), new FadeTransition(0.5f));
            }
        });
        stage.addActor(backBtn);
    }
}
