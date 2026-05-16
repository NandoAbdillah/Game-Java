package com.bismillahjuara.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

/**
 * Tombol interaktif berbasis murni Gambar PNG ala AAA.
 * Fitur: Smooth Scale, Auto-Center Origin, dan efek Glow Lighting.
 */
public class AnimatedImageButton extends Image {

    // --- PENGATURAN EFEK PENCAHAYAAN (GLOW) ---
    // idleColor = Abu-abu terang (agar tombol terlihat agak redup saat tidak disentuh)
    private final Color idleColor = new Color(0.85f, 0.85f, 0.85f, 1f);
    // hoverColor = Putih murni (warna asli gambar) seolah tersorot cahaya
    private final Color hoverColor = Color.WHITE;

    public AnimatedImageButton(Texture texture) {
        super(texture);

        // Atur warna awal menjadi agak redup
        setColor(idleColor);

        addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                // Hanya jalankan efek Hover jika menggunakan Mouse PC
                if (pointer == -1) {
                    clearActions();
                    // Animasi elegan: Zoom tipis (1.05x) + Warnanya bersinar jadi putih (Glow)
                    addAction(Actions.parallel(
                        Actions.scaleTo(1.05f, 1.05f, 0.15f, Interpolation.smooth),
                        Actions.color(hoverColor, 0.15f, Interpolation.smooth)
                    ));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                if (pointer == -1) {
                    clearActions();
                    // Kembali ke ukuran normal dan redup kembali
                    addAction(Actions.parallel(
                        Actions.scaleTo(1.0f, 1.0f, 0.2f, Interpolation.smooth),
                        Actions.color(idleColor, 0.2f, Interpolation.smooth)
                    ));
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                clearActions();
                // Efek ditekan: Mengecil membal dan menyala terang
                addAction(Actions.parallel(
                    Actions.scaleTo(0.95f, 0.95f, 0.05f),
                    Actions.color(hoverColor, 0.05f)
                ));
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                clearActions();
                // Balik ke posisi agak membesar (hover state) setelah dilepas
                addAction(Actions.parallel(
                    Actions.scaleTo(1.05f, 1.05f, 0.15f, Interpolation.circleOut),
                    Actions.color(hoverColor, 0.15f)
                ));
                super.touchUp(event, x, y, pointer, button);
            }
        });
    }

    /**
     * KUNCI UTAMA FIX ANIMASI TERBANG KE KIRI!
     * Fungsi bawaan Scene2D ini dipanggil setiap kali ukuran tabel berubah.
     * Kita paksa Titik Pusat (Origin) untuk selalu berada di tengah.
     */
    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        setOrigin(Align.center);
    }
}
