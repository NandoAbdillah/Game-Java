package com.bismillahjuara.game.screens;

import com.bismillahjuara.game.transitions.FadeTransition;
import com.bismillahjuara.game.ui.SettingsPanel;

/**
 * Layar Settings di Main Menu.
 * Class ini sekarang super bersih karena semua logika UI (Audio, Graphics, FPS)
 * sudah diisolasi ke dalam komponen reusable: SettingsPanel!
 */
public class SettingsScreen extends BaseScreen {

    public SettingsScreen() {
        super();
        setupUI();
    }

    private void setupUI() {
        // TUGAS KITA DI SINI JADI SANGAT MUDAH BERKAT SETTINGS PANEL!
        // Kita cukup memanggil komponen SettingsPanel dan memberikan instruksi
        // apa yang harus dilakukan saat tombol "Back" ditekan.
        SettingsPanel settingsUI = new SettingsPanel(new Runnable() {
            @Override
            public void run() {
                // Aksi tombol Back: Kembali ke Main Menu dengan transisi memudar
                ScreenManager.getInstance().setScreen(new MainMenuScreen(), new FadeTransition(0.5f));
            }
        });

        // Tempelkan panel raksasa tersebut ke atas panggung (Stage)
        stage.addActor(settingsUI);
    }
}
