package com.bismillahjuara.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class FontManager {

    private static FontManager instance;

    private BitmapFont titleFont; // Untuk Triforce.otf (Heading)
    private BitmapFont bodyFont;  // Untuk Wild.otf (Paragraph/Sub)

    private FontManager() {
        // 1. LOAD FONT WILD (BODY)
        try {
            FileHandle wildFile = Gdx.files.internal("font/wild.otf");
            if (wildFile.exists()) {
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(wildFile);
                FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
                parameter.size = 48; // Ukuran standar untuk teks
                parameter.minFilter = Texture.TextureFilter.Linear;
                parameter.magFilter = Texture.TextureFilter.Linear;
                parameter.genMipMaps = true;
                bodyFont = generator.generateFont(parameter);
                generator.dispose();
                Gdx.app.log("FONT_MANAGER", "Font Body (Wild) berhasil dimuat.");
            } else {
                Gdx.app.error("FONT_MANAGER", "font/wild.otf tidak ditemukan.");
                bodyFont = new BitmapFont();
            }
        } catch (Exception e) {
            Gdx.app.error("FONT_MANAGER", "Gagal memuat font Wild.", e);
            bodyFont = new BitmapFont();
        }

        // 2. LOAD FONT TRIFORCE (TITLE)
        try {
            FileHandle triforceFile = Gdx.files.internal("font/triforce.otf");
            if (triforceFile.exists()) {
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(triforceFile);
                FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
                parameter.size = 72; // Ukuran besar untuk Judul
                parameter.minFilter = Texture.TextureFilter.Linear;
                parameter.magFilter = Texture.TextureFilter.Linear;
                parameter.genMipMaps = true;
                titleFont = generator.generateFont(parameter);
                generator.dispose();
                Gdx.app.log("FONT_MANAGER", "Font Title (Triforce) berhasil dimuat.");
            } else {
                Gdx.app.error("FONT_MANAGER", "font/triforce.otf tidak ditemukan.");
                titleFont = new BitmapFont();
            }
        } catch (Exception e) {
            Gdx.app.error("FONT_MANAGER", "Gagal memuat font Triforce.", e);
            titleFont = new BitmapFont();
        }
    }

    public static FontManager getInstance() {
        if (instance == null) {
            instance = new FontManager();
        }
        return instance;
    }

    public BitmapFont getTitleFont() {
        return titleFont;
    }

    public BitmapFont getBodyFont() {
        return bodyFont;
    }

    // BACKWARD COMPATIBILITY: Menjaga kodingan lama yang masih memanggil getFont() agar tidak error
    public BitmapFont getFont() {
        return bodyFont;
    }

    public void dispose() {
        if (titleFont != null) titleFont.dispose();
        if (bodyFont != null) bodyFont.dispose();
    }
}
