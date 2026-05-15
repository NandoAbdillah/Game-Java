package com.bismillahjuara.game.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.bismillahjuara.game.transitions.FadeTransition;
import com.bismillahjuara.game.ui.AnimatedButton;
import com.bismillahjuara.game.ui.MenuUIManager;

public class StoryMenuScreen extends BaseScreen {
    public StoryMenuScreen() {
        super();
        Table t = new Table();
        t.setFillParent(true);
        t.add(new Label("STORY LOG\n(Placeholder Lore Collection)", MenuUIManager.getInstance().skin, "title")).padBottom(50).row();

        AnimatedButton backBtn = new AnimatedButton("Return", MenuUIManager.getInstance().skin, "default");
        backBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                ScreenManager.getInstance().setScreen(new MainMenuScreen(), new FadeTransition(0.5f));
            }
        });
        t.add(backBtn).size(200, 60);
        stage.addActor(t);
    }
}
