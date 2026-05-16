package com.bismillahjuara.game.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

/**
 * Tombol interaktif berbasis murni Gambar PNG.
 * Mendukung animasi Skala (Hover & Click) layaknya game AAA.
 */
public class AnimatedImageButton extends Image {

    public AnimatedImageButton(Texture texture) {
        super(texture);

        // PENTING: Set titik pusat (origin) ke tengah gambar.
        // Kalau tidak, saat membesar gambarnya akan menceng ke kiri bawah!
        setOrigin(Align.center);

        // Tambahkan efek Interaksi (Juice Animation)
        addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                // Animasi Hover (Membesar 10%) - Biasanya hanya jalan di PC (Mouse)
                if (pointer == -1) {
                    clearActions();
                    addAction(Actions.scaleTo(1.1f, 1.1f, 0.15f, Interpolation.fade));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                if (pointer == -1) {
                    clearActions();
                    addAction(Actions.scaleTo(1.0f, 1.0f, 0.25f, Interpolation.fade));
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // Efek Membal (Mengecil) saat ditekan jari/mouse
                clearActions();
                addAction(Actions.scaleTo(0.9f, 0.9f, 0.05f));
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                // Efek Membal Balik (Bounce Out) saat dilepas
                clearActions();
                addAction(Actions.scaleTo(1.0f, 1.0f, 0.3f, Interpolation.bounceOut));
                super.touchUp(event, x, y, pointer, button);
            }
        });
    }
}
