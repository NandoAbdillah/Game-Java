package com.bismillahjuara.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

public class CycleButton<T extends Enum<T>> extends Table {

    private T[] options;
    private int currentIndex;
    private Label valueLabel;
    private MenuUIManager uiManager;

    public CycleButton(T[] options, T initialValue) {
        this.options = options;
        this.uiManager = MenuUIManager.getInstance();

        // Cari index awal
        for (int i = 0; i < options.length; i++) {
            if (options[i] == initialValue) {
                currentIndex = i;
                break;
            }
        }

        buildUI();
    }

    private void buildUI() {
        // Tombol Kiri
        TextButton btnLeft = new TextButton("<", uiManager.skin, "default");
        btnLeft.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                currentIndex--;
                if (currentIndex < 0) currentIndex = options.length - 1;
                updateLabel();
            }
        });

        // Tombol Kanan
        TextButton btnRight = new TextButton(">", uiManager.skin, "default");
        btnRight.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                currentIndex++;
                if (currentIndex >= options.length) currentIndex = 0;
                updateLabel();
            }
        });

        // Label Nilai
        Label.LabelStyle style = new Label.LabelStyle(uiManager.bodyFont, Color.YELLOW);
        valueLabel = new Label("", style);
        valueLabel.setAlignment(Align.center);
        updateLabel();

        // Susun Layout
        add(btnLeft).size(50, 50).padRight(20);
        add(valueLabel).width(150);
        add(btnRight).size(50, 50).padLeft(20);
    }

    private void updateLabel() {
        String text = options[currentIndex].name().replace("FPS_", "");
        valueLabel.setText(text);
    }

    public T getSelectedValue() {
        return options[currentIndex];
    }
}
