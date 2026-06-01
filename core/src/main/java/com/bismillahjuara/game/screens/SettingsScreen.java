package com.bismillahjuara.game.screens;

import com.bismillahjuara.game.transitions.FadeTransition;
import com.bismillahjuara.game.ui.SettingsPanel;

public class SettingsScreen extends BaseScreen {

    public SettingsScreen() {
        super();
        setupUI();
    }

    private void setupUI() {
        SettingsPanel settingsUI = new SettingsPanel(new Runnable() {
            @Override
            public void run() {
                ScreenManager.getInstance().setScreen(new MainMenuScreen(), new FadeTransition(0.5f));
            }
        });

        stage.addActor(settingsUI);
    }
}
