package com.bismillahjuara.game.ui;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

public class AnimatedButton extends TextButton {

    public AnimatedButton(String text, Skin skin, String styleName) {
        super(text, skin, styleName);

        setTransform(true);
        setOrigin(Align.center);

        addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
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
                clearActions();
                addAction(Actions.scaleTo(0.95f, 0.95f, 0.05f));
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                clearActions();
                addAction(Actions.scaleTo(1.0f, 1.0f, 0.1f, Interpolation.bounceOut));
                super.touchUp(event, x, y, pointer, button);
            }
        });
    }
}
