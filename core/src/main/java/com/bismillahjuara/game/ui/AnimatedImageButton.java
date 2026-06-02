package com.bismillahjuara.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
//inheritance,mewarisi dari LibGDX
public class AnimatedImageButton extends Image {

    private final Color idleColor = new Color(0.85f, 0.85f, 0.85f, 1f);
    private final Color hoverColor = Color.WHITE;

    public AnimatedImageButton(Texture texture) {
        super(texture);

        setColor(idleColor);

        addListener(new ClickListener() {
            //polimorfisme overriding, timpa fungsi klik bawaan buat nambah animasi efek tombol
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                if (pointer == -1) {
                    clearActions();
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
                    addAction(Actions.parallel(
                        Actions.scaleTo(1.0f, 1.0f, 0.2f, Interpolation.smooth),
                        Actions.color(idleColor, 0.2f, Interpolation.smooth)
                    ));
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                clearActions();
                addAction(Actions.parallel(
                    Actions.scaleTo(0.95f, 0.95f, 0.05f),
                    Actions.color(hoverColor, 0.05f)
                ));
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                clearActions();
                addAction(Actions.parallel(
                    Actions.scaleTo(1.05f, 1.05f, 0.15f, Interpolation.circleOut),
                    Actions.color(hoverColor, 0.15f)
                ));
                super.touchUp(event, x, y, pointer, button);
            }
        });
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        setOrigin(Align.center);
    }
}
