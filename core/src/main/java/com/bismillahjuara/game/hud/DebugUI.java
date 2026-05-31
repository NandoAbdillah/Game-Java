package com.bismillahjuara.game.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.bismillahjuara.game.core.GameContext;
import com.bismillahjuara.game.ui.FontManager;

public class DebugUI {
    private Table rootTable;

    // HUD Elements
    private Label missionLabel;
    private Label statsLabel;

    // Cinematic Elements
    private Table cinematicTable;
    private Label cineTitle;
    private Label cineDesc;

    // Typewriter state
    private String targetDescText = "";
    private int typeIndex = 0;
    private float typeTimer = 0f;
    private boolean isTyping = false;

    public DebugUI() {
        rootTable = new Table();
        rootTable.setFillParent(true);

        Label.LabelStyle hudStyle = new Label.LabelStyle(FontManager.getInstance().getFont(), Color.WHITE);

        missionLabel = new Label("", hudStyle);
        statsLabel = new Label("", hudStyle);
        statsLabel.setAlignment(Align.right);

        // Top Left & Top Right HUD Layout
        Table hudTable = new Table();
        hudTable.setFillParent(true);
        hudTable.top().pad(40);
        hudTable.add(missionLabel).expandX().left().top();
        hudTable.add(statsLabel).expandX().right().top();

        // Cinematic Overlay
        cinematicTable = new Table();
        cinematicTable.setFillParent(true);
        cinematicTable.getColor().a = 0f; // Hidden by default

        Label.LabelStyle titleStyle = new Label.LabelStyle(FontManager.getInstance().getFont(), Color.GOLD);
        cineTitle = new Label("", titleStyle);
        cineTitle.setFontScale(1.5f);
        cineTitle.setAlignment(Align.center);

        cineDesc = new Label("", hudStyle);
        cineDesc.setAlignment(Align.center);
        cineDesc.setWrap(true);

        cinematicTable.add(cineTitle).padBottom(30).row();
        cinematicTable.add(cineDesc).width(900).center();

        rootTable.addActor(hudTable);
        rootTable.addActor(cinematicTable);
    }

    public Table getRootTable() { return rootTable; }

    /**
     * Memutar Cinematic Overlay dengan Typewriter Effect
     */
    public void playCinematic(String title, String desc, float duration, Runnable onComplete) {
        cineTitle.setText(title);
        cineDesc.setText("");

        targetDescText = desc;
        typeIndex = 0;
        typeTimer = 0f;
        isTyping = false;

        cinematicTable.clearActions();
        cinematicTable.addAction(Actions.sequence(
            Actions.parallel(
                Actions.fadeIn(1f, Interpolation.fade),
                Actions.scaleTo(1.05f, 1.05f, 1f)
            ),
            Actions.run(new Runnable() { @Override public void run() { isTyping = true; } }),
            Actions.delay(duration),
            Actions.fadeOut(1f, Interpolation.fade),
            Actions.run(new Runnable() {
                @Override public void run() {
                    isTyping = false;
                    if (onComplete != null) onComplete.run();
                }
            })
        ));
    }

    public void update(float delta, GameContext context) {
        // Logika Typewriter Effect (Kecepatan 0.03 detik per huruf)
        if (isTyping && typeIndex < targetDescText.length()) {
            typeTimer += delta;
            if (typeTimer >= 0.03f) {
                typeTimer = 0f;
                typeIndex++;
                cineDesc.setText(targetDescText.substring(0, typeIndex));
            }
        }

        // Update Text HUD sesuai Act
        if (context.currentAct == 1) {
            missionLabel.setText("MISI\nCari 3 Relik Pusaka");
            statsLabel.setText("Relik: " + context.relicsCollected + " / 3\nHP Timun: 100");
        } else if (context.currentAct == 2) {
            missionLabel.setText("MISI\nKalahkan Buto Ijo");
            int butoHp = context.boss != null ? (10 - context.butoHits) : 10;
            if (butoHp < 0) butoHp = 0;
            statsLabel.setText("HP Timun: 100\nHP Buto Ijo: " + butoHp + "\nHit Buto Ijo: " + context.butoHits + " / 10");
        }
    }
}
