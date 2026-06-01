package com.bismillahjuara.game.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Align;
import com.bismillahjuara.game.core.GameContext;
import com.bismillahjuara.game.ui.FontManager;

public class DebugUI {
    private Table rootTable;
    private Table hudTable;

    private Label missionLabel;
    private Label statsLabel;

    private Image hpBackground;
    private Image hpForeground;
    private Texture hpBgTex;
    private Texture hpFgTex;

    private Table cinematicTable;
    private Label cineTitle;
    private Label cineDesc;

    private Texture cinematicBgTex;

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

        Pixmap bgPix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bgPix.setColor(0.2f, 0.2f, 0.2f, 0.8f);
        bgPix.fill();
        hpBgTex = new Texture(bgPix);
        bgPix.dispose();

        Pixmap fgPix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        fgPix.setColor(Color.WHITE);
        fgPix.fill();
        hpFgTex = new Texture(fgPix);
        fgPix.dispose();

        hpBackground = new Image(hpBgTex);
        hpForeground = new Image(hpFgTex);

        WidgetGroup hpGroup = new WidgetGroup();
        hpBackground.setSize(200, 20);
        hpForeground.setSize(200, 20);
        hpGroup.addActor(hpBackground);
        hpGroup.addActor(hpForeground);

        hudTable = new Table();
        hudTable.setFillParent(true);
        hudTable.top().pad(40);
        hudTable.add(missionLabel).expandX().left().top();

        Table rightTable = new Table();
        rightTable.add(statsLabel).right().row();
        rightTable.add(new Label("HP Timun", hudStyle)).right().padTop(10).row();
        rightTable.add(hpGroup).size(200, 20).right().padTop(5);

        hudTable.add(rightTable).expandX().right().top();

        cinematicTable = new Table();
        cinematicTable.setFillParent(true);
        cinematicTable.getColor().a = 0f;

        Pixmap cinePix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        cinePix.setColor(0f, 0f, 0f, 0.85f); // Hitam transparan 85%
        cinePix.fill();
        cinematicBgTex = new Texture(cinePix);
        cinePix.dispose();

        Image bgImage = new Image(cinematicBgTex);
        bgImage.setFillParent(true); // Penuhi layar
        cinematicTable.addActor(bgImage); // Taruh di layer paling belakang

        // Bikin tabel lagi khusus untuk wadah Teks di atas background hitam
        Table contentTable = new Table();
        contentTable.setFillParent(true);

        Label.LabelStyle titleStyle = new Label.LabelStyle(FontManager.getInstance().getFont(), Color.GOLD);
        cineTitle = new Label("", titleStyle);
        cineTitle.setFontScale(1.5f);
        cineTitle.setAlignment(Align.center);

        cineDesc = new Label("", hudStyle);
        cineDesc.setAlignment(Align.center);
        cineDesc.setWrap(true);

        contentTable.add(cineTitle).padBottom(30).row();
        contentTable.add(cineDesc).width(900).center();

        cinematicTable.addActor(contentTable); // Taruh Teks di atas Background Hitam

        rootTable.addActor(hudTable);
        rootTable.addActor(cinematicTable);
    }

    public Table getRootTable() { return rootTable; }

    public void hideHUD() {
        hudTable.setVisible(false);
    }

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
        float hpPercent = context.player.health / 100f;
        hpForeground.setWidth(200f * hpPercent);

        if (hpPercent < 0.3f) {
            hpForeground.setColor(Color.RED);
        } else {
            hpForeground.setColor(Color.GREEN);
        }

        if (isTyping && typeIndex < targetDescText.length()) {
            typeTimer += delta;
            if (typeTimer >= 0.03f) {
                typeTimer = 0f;
                typeIndex++;
                cineDesc.setText(targetDescText.substring(0, typeIndex));
            }
        }

        if (context.currentAct == 1) {
            missionLabel.setText("MISI\nCari 3 Relik Pusaka");
            statsLabel.setText("Relik: " + context.relicsCollected + " / 3");
        } else if (context.currentAct == 2) {
            missionLabel.setText("MISI\nKalahkan Buto Ijo");
            int butoHp = context.boss != null ? (10 - context.butoHits) : 10;
            if (butoHp < 0) butoHp = 0;
            statsLabel.setText("HP Buto Ijo: " + butoHp + "\nHit Buto Ijo: " + context.butoHits + " / 10");
        }
    }
}
