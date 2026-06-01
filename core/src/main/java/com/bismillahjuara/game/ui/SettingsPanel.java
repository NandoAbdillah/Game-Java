package com.bismillahjuara.game.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.bismillahjuara.game.input.GameInputHandler;
import com.bismillahjuara.game.settings.SettingsManager;
import com.bismillahjuara.game.settings.SettingsManager.FPSLimit;
import com.bismillahjuara.game.settings.SettingsManager.GraphicsQuality;

public class SettingsPanel extends Table {

    private SettingsManager settings;
    private MenuUIManager uiManager;

    private Slider masterVolSlider;
    private CycleButton<GraphicsQuality> graphicsCycle;
    private CycleButton<FPSLimit> fpsCycle;
    private CheckBox fullscreenCheck;
    private CheckBox vsyncCheck;

    public SettingsPanel(final Runnable onBackAction) {
        this.settings = SettingsManager.getInstance();
        this.uiManager = MenuUIManager.getInstance();

        setFillParent(true);
        if (uiManager.bgSettings != null) setBackground(uiManager.bgSettings);

        buildLayout(onBackAction);
    }

    private Image createFitImage(TextureRegionDrawable drawable) {
        if (drawable == null) return new Image();
        Image img = new Image(drawable);
        img.setScaling(Scaling.fit);
        return img;
    }

    private void buildLayout(final Runnable onBackAction) {
        Table content = new Table();
        content.pad(100, 150, 100, 150);

        content.add(createFitImage(uiManager.headerAudio)).colspan(2).width(250).height(60).padBottom(20).center().row();

        masterVolSlider = new Slider(0f, 1f, 0.05f, false, uiManager.skin, "custom-slider");
        masterVolSlider.setValue(settings.masterVolume);

        content.add(createFitImage(uiManager.lblMasterVol)).width(250).height(40).right().padRight(40);
        content.add(masterVolSlider).width(350).left().row();

        content.add(new Table()).height(40).row();

        content.add(createFitImage(uiManager.headerGraphics)).colspan(2).width(300).height(60).padBottom(20).center().row();

        graphicsCycle = new CycleButton<>(GraphicsQuality.values(), settings.graphicsPreset);
        content.add(graphicsCycle).colspan(2).center().padBottom(20).row();

        if (!GameInputHandler.IS_MOBILE) {
            fullscreenCheck = new CheckBox("", uiManager.skin, "custom-checkbox");
            fullscreenCheck.setChecked(settings.fullscreen);

            fullscreenCheck.getImage().setScaling(Scaling.fit);
            fullscreenCheck.getImageCell().size(50, 50);

            content.add(createFitImage(uiManager.lblFullscreen)).width(200).height(40).right().padRight(40);
            content.add(fullscreenCheck).left().padBottom(15).row();

            vsyncCheck = new CheckBox("", uiManager.skin, "custom-checkbox");
            vsyncCheck.setChecked(settings.vsync);

            vsyncCheck.getImage().setScaling(Scaling.fit);
            vsyncCheck.getImageCell().size(50, 50);

            content.add(createFitImage(uiManager.lblVsync)).width(200).height(40).right().padRight(40);
            content.add(vsyncCheck).left().padBottom(15).row();
        }

        fpsCycle = new CycleButton<>(FPSLimit.values(), settings.fpsLimit);
        content.add(createFitImage(uiManager.lblFpsLimit)).width(200).height(40).right().padRight(40);
        content.add(fpsCycle).left().padBottom(50).row();

        Table btnTable = new Table();

        if (uiManager.btnApply != null) {
            AnimatedImageButton btnApply = new AnimatedImageButton(uiManager.btnApply.getRegion().getTexture());
            btnApply.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    saveAndApply();
                }
            });
            btnTable.add(btnApply).size(280, 80).padRight(40);
        }

        if (uiManager.btnBack != null) {
            AnimatedImageButton btnBack = new AnimatedImageButton(uiManager.btnBack.getRegion().getTexture());
            btnBack.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    if (onBackAction != null) onBackAction.run();
                }
            });
            btnTable.add(btnBack).size(280, 80);
        }

        content.add(btnTable).colspan(2).center().padTop(20);

        add(content).center();
    }

    private void saveAndApply() {
        settings.masterVolume = masterVolSlider.getValue();
        settings.graphicsPreset = graphicsCycle.getSelectedValue();
        settings.fpsLimit = fpsCycle.getSelectedValue();

        if (!GameInputHandler.IS_MOBILE) {
            settings.fullscreen = fullscreenCheck.isChecked();
            settings.vsync = vsyncCheck.isChecked();
        }

        settings.saveAndApplySettings();

        addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
            com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(0, 15, 0.1f),
            com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(0, -15, 0.1f)
        ));
    }
}
