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

/**
 * Universal Settings UI.
 * Layout sudah diatur ketat (Strict Sizing) agar gambar PNG raksasa
 * menyusut menjadi proporsional dan tidak saling tiban!
 */
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
        // Set background kayu (Akan otomatis fill parent layar)
        if (uiManager.bgSettings != null) setBackground(uiManager.bgSettings);

        buildLayout(onBackAction);
    }

    /**
     * HELPER METHOD (KUNCI UTAMA ANTI-RAKSASA):
     * Memaksa gambar menggunakan Scaling.fit agar tidak gepeng dan
     * selalu tunduk pada ukuran cell Table yang kita tentukan.
     */
    private Image createFitImage(TextureRegionDrawable drawable) {
        if (drawable == null) return new Image(); // Fallback aman
        Image img = new Image(drawable);
        img.setScaling(Scaling.fit);
        return img;
    }

    private void buildLayout(final Runnable onBackAction) {
        // Kontainer dalam agar konten tidak menabrak pinggiran bingkai kayu
        Table content = new Table();
        // Beri jarak (padding) dari tepi layar. Sesuaikan angkanya jika masih nabrak kayu.
        content.pad(100, 150, 100, 150);

        // ==========================================
        // 1. SECTION: AUDIO
        // ==========================================
        // Header Audio (Di tengah, memakan 2 kolom)
        content.add(createFitImage(uiManager.headerAudio)).colspan(2).width(250).height(60).padBottom(20).center().row();

        // Baris 1: Label Kiri, Slider Kanan
        masterVolSlider = new Slider(0f, 1f, 0.05f, false, uiManager.skin, "custom-slider");
        masterVolSlider.setValue(settings.masterVolume);

        content.add(createFitImage(uiManager.lblMasterVol)).width(250).height(40).right().padRight(40);
        content.add(masterVolSlider).width(350).left().row();

        // Spasi pemisah section
        content.add(new Table()).height(40).row();

        // ==========================================
        // 2. SECTION: GRAPHICS
        // ==========================================
        // Header Graphics
        content.add(createFitImage(uiManager.headerGraphics)).colspan(2).width(300).height(60).padBottom(20).center().row();

        // Baris 2: Cycle Button Graphics Quality (Ditaruh di tengah)
        graphicsCycle = new CycleButton<>(GraphicsQuality.values(), settings.graphicsPreset);
        content.add(graphicsCycle).colspan(2).center().padBottom(20).row();

        // Baris 3 & 4: Fullscreen & VSync (PC ONLY)
        if (!GameInputHandler.IS_MOBILE) {
            fullscreenCheck = new CheckBox("", uiManager.skin, "custom-checkbox");
            fullscreenCheck.setChecked(settings.fullscreen);

            // JURUS JINAKKAN CHECKBOX RAKSASA:
            // 1. Paksa gambar aslinya (image child) untuk menyusut (fit)
            fullscreenCheck.getImage().setScaling(Scaling.fit);
            // 2. Paksa ukuran sel penampungnya
            fullscreenCheck.getImageCell().size(50, 50);

            content.add(createFitImage(uiManager.lblFullscreen)).width(200).height(40).right().padRight(40);
            content.add(fullscreenCheck).left().padBottom(15).row();

            vsyncCheck = new CheckBox("", uiManager.skin, "custom-checkbox");
            vsyncCheck.setChecked(settings.vsync);

            // Lakukan hal yang sama untuk VSync
            vsyncCheck.getImage().setScaling(Scaling.fit);
            vsyncCheck.getImageCell().size(50, 50);

            content.add(createFitImage(uiManager.lblVsync)).width(200).height(40).right().padRight(40);
            content.add(vsyncCheck).left().padBottom(15).row();
        }

        // Baris 5: FPS Limit
        fpsCycle = new CycleButton<>(FPSLimit.values(), settings.fpsLimit);
        content.add(createFitImage(uiManager.lblFpsLimit)).width(200).height(40).right().padRight(40);
        content.add(fpsCycle).left().padBottom(50).row();

        // ==========================================
        // 3. SECTION: BUTTONS (Apply & Back)
        // ==========================================
        Table btnTable = new Table();

        if (uiManager.btnApply != null) {
            AnimatedImageButton btnApply = new AnimatedImageButton(uiManager.btnApply.getRegion().getTexture());
            btnApply.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    saveAndApply();
                }
            });
            // DIPERBESAR dari 180x60 menjadi 280x80
            btnTable.add(btnApply).size(280, 80).padRight(40);
        }

        if (uiManager.btnBack != null) {
            AnimatedImageButton btnBack = new AnimatedImageButton(uiManager.btnBack.getRegion().getTexture());
            btnBack.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    if (onBackAction != null) onBackAction.run();
                }
            });
            // DIPERBESAR dari 180x60 menjadi 280x80
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

        // Feedback visual membal saat di-apply
        addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
            com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(0, 15, 0.1f),
            com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(0, -15, 0.1f)
        ));
    }
}
