package com.bismillahjuara.game.ui;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

/**
 * Reusable Action RPG Style Button.
 * Punya built-in animasi Hover dan Click yang smooth tanpa membebani CPU.
 */
public class AnimatedButton extends TextButton {

    public AnimatedButton(String text, Skin skin, String styleName) {
        super(text, skin, styleName);

        // PENTING: Agar bisa membesar (scale) dari tengah, origin harus di tengah!
        setTransform(true);
        setOrigin(Align.center);

        // Tambahkan Hover & Click Animation Juice
        addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                // Hanya animasi hover jika menggunakan Mouse (PC)
                if (pointer == -1) {
                    clearActions();
                    addAction(Actions.scaleTo(1.1f, 1.1f, 0.15f, Interpolation.fade));
                    // TODO: Panggil UISoundManager.playHoverSound()
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
                clearActions();
                addAction(Actions.scaleTo(0.95f, 0.95f, 0.05f)); // Efek membal saat ditekan
                // TODO: Panggil UISoundManager.playClickSound()
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                clearActions();
                addAction(Actions.scaleTo(1.0f, 1.0f, 0.1f, Interpolation.bounceOut)); // Balik membal
                super.touchUp(event, x, y, pointer, button);
            }
        });
    }
}
