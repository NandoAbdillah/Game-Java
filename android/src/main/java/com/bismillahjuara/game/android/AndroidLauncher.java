package com.bismillahjuara.game.android;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.bismillahjuara.game.Main;

public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Konfigurasi LibGDX untuk Android
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        // Navigation Bar dan Status Bar (baterai/sinyal) hilang (Mode Immersive)
        config.useImmersiveMode = true;

        // KUNCI FULLSCREEN T
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            // Mengizinkan game merender grafis menembus area poni/kamera depan layar
            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(layoutParams);
        }

        // 3. Jalankan game-nya!
        initialize(new Main(), config);
    }
}
