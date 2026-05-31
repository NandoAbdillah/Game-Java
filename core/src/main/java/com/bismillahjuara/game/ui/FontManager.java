package com.bismillahjuara.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class FontManager {

    private static FontManager instance;

    private BitmapFont customFont;

    private FontManager() {

        try {

            FileHandle fontFile =
                Gdx.files.internal("font/wild.otf");

            if (fontFile.exists()) {

                FreeTypeFontGenerator generator =
                    new FreeTypeFontGenerator(fontFile);

                FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();

                parameter.size = 48;

                parameter.minFilter = Texture.TextureFilter.Linear;
                parameter.magFilter = Texture.TextureFilter.Linear;

                parameter.genMipMaps = true;

                customFont =
                    generator.generateFont(parameter);

                generator.dispose();

                Gdx.app.log(
                    "FONT_MANAGER",
                    "Custom OTF Font berhasil dimuat."
                );

            } else {

                Gdx.app.error(
                    "FONT_MANAGER",
                    "font/wild.otf tidak ditemukan."
                );

                customFont = new BitmapFont();
            }

        } catch (Exception e) {

            Gdx.app.error(
                "FONT_MANAGER",
                "Gagal memuat font OTF.",
                e
            );

            customFont = new BitmapFont();
        }
    }

    public static FontManager getInstance() {

        if (instance == null) {
            instance = new FontManager();
        }

        return instance;
    }

    public BitmapFont getFont() {
        return customFont;
    }

    public void dispose() {

        if (customFont != null) {
            customFont.dispose();
        }
    }
}
