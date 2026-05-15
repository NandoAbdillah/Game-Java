package com.bismillahjuara.game.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/**
 * Menampilkan data debug di layar.
 * Dioptimasi dengan StringBuilder agar tidak memicu Garbage Collection (Lag Spike).
 */
public class DebugUI {
    private Table table;
    private Label debugLabel;
    private StringBuilder sb;

    public DebugUI(HudAssets assets) {
        table = new Table();
        table.setFillParent(true);
        table.top().left(); // Jangkar di kiri atas

        Label.LabelStyle style = new Label.LabelStyle(assets.defaultFont, Color.YELLOW);
        debugLabel = new Label("Initializing...", style);

        table.add(debugLabel).pad(10);

        sb = new StringBuilder();
    }

    public Table getRootTable() {
        return table;
    }

    public void update(Vector3 playerPos, float camYaw, boolean isMobile) {
        // Clear isi string lama
        sb.setLength(0);

        // Bangun string baru tanpa alokasi memori tambahan
        sb.append("Pos: ").append(String.format("%.1f", playerPos.x)).append(", ")
            .append(String.format("%.1f", playerPos.z))
            .append(" | Yaw: ").append(String.format("%.1f", camYaw)).append("\n");

        if (!isMobile) {
            sb.append("WASD: Gerak | Shift: Lari | C: Jongkok | F: Pukul\n");
            sb.append("Space: Lompat | E: Lempar | R: Tendang | H: Heal\n");
            sb.append("Drag Mouse Kiri: Putar Kamera | Scroll: Zoom");
        }

        debugLabel.setText(sb);
    }
}
