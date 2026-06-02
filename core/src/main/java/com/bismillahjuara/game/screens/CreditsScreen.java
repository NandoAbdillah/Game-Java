package com.bismillahjuara.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.bismillahjuara.game.audio.AudioManager;
import com.bismillahjuara.game.audio.AudioTrack;
import com.bismillahjuara.game.transitions.FadeTransition;
import com.bismillahjuara.game.ui.FontManager;

public class CreditsScreen extends BaseScreen {

    private Table scrollTable;

    private float scrollDuration = 150f;

    public CreditsScreen() {
        super();

        AudioManager.getInstance().playMusic(AudioTrack.CREDITS_THEME, 2f);

        buildCreditsContent();
        setupSkipButton();
        startScrolling();
    }

    private void buildCreditsContent() {
        scrollTable = new Table();
        scrollTable.setWidth(1920);

        Label.LabelStyle titleStyle = new Label.LabelStyle(FontManager.getInstance().getTitleFont(), Color.GOLD);
        Label.LabelStyle subtitleStyle = new Label.LabelStyle(FontManager.getInstance().getBodyFont(), Color.ORANGE);
        Label.LabelStyle roleStyle = new Label.LabelStyle(FontManager.getInstance().getBodyFont(), new Color(0.7f, 0.7f, 0.7f, 1f));
        Label.LabelStyle nameStyle = new Label.LabelStyle(FontManager.getInstance().getBodyFont(), Color.WHITE);
        Label.LabelStyle quoteStyle = new Label.LabelStyle(FontManager.getInstance().getBodyFont(), Color.CYAN);

        addBigSpace();
        addBigSpace();


        addTitle(titleStyle, "SUKMA GOWONG");
        addSingleColumn(roleStyle, "A Game Project by\nTim Sukma Gowong");
        addSpace();
        addQuote(quoteStyle, "\"Di balik setiap baris kode, terdapat error yang menunggu untuk ditemukan.\"");
        addBigSpace();


        addTitle(titleStyle, "CAST");
        addDoubleColumn(roleStyle, "Timun Mas", nameStyle, "Shinta Nur'aini Dwi");
        addDoubleColumn(roleStyle, "Ibu Timun Mas", nameStyle, "Rehil Azrilla Multajabah");
        addDoubleColumn(roleStyle, "Buto Ijo", nameStyle, "Nando Abdillah Salam");
        addDoubleColumn(roleStyle, "Misterious Whispers", nameStyle, "M. Naufal Wicaksono");
        addQuote(quoteStyle, "\"Mereka bilang game horror itu seram, sampai kamu melihat file project-nya.\"");
        addBigSpace();


        addTitle(titleStyle, "PRODUCTION & MANAGEMENT");
        addDoubleColumn(roleStyle, "Project Manager", nameStyle, "Nando Abdillah Salam");
        addDoubleColumn(roleStyle, "Coffee Producer", nameStyle, "Nando Abdillah Salam");
        addDoubleColumn(roleStyle, "Deadline Enforcer", nameStyle, "Nando Abdillah Salam");
        addDoubleColumn(roleStyle, "Mental Breakdown Survivor", nameStyle, "Seluruh Tim");
        addSpace();


        addTitle(titleStyle, "DESIGN & NARRATIVE");
        addDoubleColumn(roleStyle, "Lead Game Designer", nameStyle, "Nando Abdillah Salam");
        addDoubleColumn(roleStyle, "Gameplay Designer", nameStyle, "M. Naufal Wicaksono");
        addDoubleColumn(roleStyle, "Narrative Designer", nameStyle, "Shinta Nur'aini Dwi");
        addDoubleColumn(roleStyle, "Lore & Backstory Writer", nameStyle, "Shinta Nur'aini Dwi");
        addDoubleColumn(roleStyle, "Level Designer", nameStyle, "Nando Abdillah Salam");
        addDoubleColumn(roleStyle, "Jumpscare Architect", nameStyle, "M. Naufal Wicaksono");
        addQuote(quoteStyle, "\"Kalau playernya tidak kaget, tambahkan saja volume audionya sampai 200%.\"");
        addBigSpace();


        addTitle(titleStyle, "PROGRAMMING & TECHNICAL");
        addDoubleColumn(roleStyle, "Core Programmer", nameStyle, "M. Naufal Wicaksono");
        addDoubleColumn(roleStyle, "Gameplay Programmer", nameStyle, "Nando Abdillah Salam");
        addDoubleColumn(roleStyle, "UI Programmer", nameStyle, "M. Naufal Wicaksono");
        addDoubleColumn(roleStyle, "Technical Artist", nameStyle, "M. Naufal Wicaksono");
        addDoubleColumn(roleStyle, "Motion Programming", nameStyle, "Nando Abdillah Salam");
        addDoubleColumn(roleStyle, "Object Program Designer", nameStyle, "Nando Abdillah Salam");
        addSpace();
        addDoubleColumn(roleStyle, "NullPointerException Handler", nameStyle, "M. Naufal Wicaksono");
        addDoubleColumn(roleStyle, "StackOverflow Copy-Paster", nameStyle, "Tim Sukma Gowong");
        addDoubleColumn(roleStyle, "Spaghetti Code Architect", nameStyle, "Nando Abdillah Salam");
        addQuote(quoteStyle, "\"99% coding adalah mencari bug.\n1% sisanya adalah membuat bug baru saat mencoba memperbaiki bug lama.\"");
        addBigSpace();


        addTitle(titleStyle, "ART & VISUAL");
        addDoubleColumn(roleStyle, "3D Modeling", nameStyle, "Shinta Nur'aini Dwi");
        addDoubleColumn(roleStyle, "Texture Artist", nameStyle, "Rehil Azrilla Multajabah");
        addDoubleColumn(roleStyle, "3D Animation", nameStyle, "M. Naufal Wicaksono");
        addDoubleColumn(roleStyle, "UI / UX Design", nameStyle, "M. Naufal Wicaksono");
        addDoubleColumn(roleStyle, "Visual Effects (VFX)", nameStyle, "Nando Abdillah Salam\nM. Naufal Wicaksono");
        addDoubleColumn(roleStyle, "Environment Artist", nameStyle, "Shinta Nur'aini Dwi");
        addDoubleColumn(roleStyle, "Cinematic Lighting", nameStyle, "Nando Abdillah Salam");
        addQuote(quoteStyle, "\"Ketika model 3D terlihat sempurna di Blender,\ntetapi hancur berantakan saat di-import ke game.\"\n— Pengalaman spiritual seluruh developer 3D");
        addBigSpace();


        addTitle(titleStyle, "AUDIO DEPARTMENT");
        addDoubleColumn(roleStyle, "Music Composer", nameStyle, "Rehil Azrilla Multajabah");
        addDoubleColumn(roleStyle, "Sound Designer (SFX)", nameStyle, "Rehil Azrilla Multajabah");
        addDoubleColumn(roleStyle, "Voice Actor", nameStyle, "Shinta Nur'aini Dwi");
        addDoubleColumn(roleStyle, "Monster Screams", nameStyle, "Rehil (Saat revisi tengah malam)");
        addQuote(quoteStyle, "\"Kadang suara yang paling menyeramkan bukanlah hantu...\nmelainkan speaker yang tiba-tiba mengeluarkan noise saat demo ke Dosen.\"");
        addBigSpace();


        addTitle(titleStyle, "QUALITY ASSURANCE");
        addDoubleColumn(roleStyle, "Chief Bug Finder", nameStyle, "Shinta Nur'aini Dwi");
        addDoubleColumn(roleStyle, "Game Testing Team", nameStyle, "Nando Abdillah Salam\nM. Naufal Wicaksono\nRehil Azrilla Multajabah");
        addDoubleColumn(roleStyle, "Rage Quit Specialist", nameStyle, "Rehil Azrilla Multajabah");
        addQuote(quoteStyle, "\"Terima kasih kepada seluruh tester yang rela menjadi korban jatuh ke dalam jurang \nglitched geometry sebelum pemain mengalaminya.\"");
        addBigSpace();


        addTitle(titleStyle, "TECHNOLOGY STACK");
        addSubtitle(subtitleStyle, "Development");
        addSingleColumn(nameStyle, "Android Studio\nJava");
        addSpace();
        addSubtitle(subtitleStyle, "Game Framework");
        addSingleColumn(nameStyle, "libGDX");
        addSpace();
        addSubtitle(subtitleStyle, "3D Production");
        addSingleColumn(nameStyle, "Blender 3D\nMixamo");
        addSpace();
        addSubtitle(subtitleStyle, "AI Assisted Production");
        addSingleColumn(nameStyle, "Tencent Hunyuan");
        addSpace();
        addSubtitle(subtitleStyle, "Video Production");
        addSingleColumn(nameStyle, "CapCut");
        addSpace();
        addQuote(quoteStyle, "\"Powered by air putih, doa ibu, dan Stack Overflow.\"");
        addBigSpace();


        addTitle(titleStyle, "DEVELOPMENT FACTS");
        addDoubleColumn(roleStyle, "Jumlah build gagal", nameStyle, "Tidak dihitung demi kesehatan mental tim");
        addDoubleColumn(roleStyle, "Jumlah \"Ini pasti gampang kok\"", nameStyle, "Tak terhingga");
        addDoubleColumn(roleStyle, "Jumlah \"Coba run sekali lagi\"", nameStyle, "Terlalu banyak");
        addDoubleColumn(roleStyle, "Jumlah kopi yang dikonsumsi", nameStyle, "Rahasia negara");
        addDoubleColumn(roleStyle, "Jumlah error yang hilang sendiri", nameStyle, "Fenomena supranatural yang tidak bisa dijelaskan");
        addBigSpace();


        addTitle(titleStyle, "SPECIAL THANKS");
        addSpace();

        addSubtitle(subtitleStyle, "Academic Support");
        addSingleColumn(nameStyle, "Bapak Rifqi Abdillah, S.Tr.T., M.Kom.");
        addSingleColumn(roleStyle, "Terima kasih atas bimbingan, ilmu, masukan, serta kesabarannya\nselama proses pengembangan proyek ini.");
        addBigSpace();

        addSubtitle(subtitleStyle, "Family Support");
        addSingleColumn(nameStyle, "Bapak dan Ibu Shinta\nBapak dan Ibu Rehil\nBapak dan Ibu Nando\nBapak dan Ibu Naufal");
        addSingleColumn(roleStyle, "Terima kasih atas doa, dukungan, dan pengertiannya ketika\nkami lebih sering menatap monitor daripada tidur dengan normal.");
        addBigSpace();

        addSubtitle(subtitleStyle, "Official Fuel Providers");
        addSingleColumn(nameStyle, "☕ Warkop Pojok Kopi Studio\n☕ Warkop Sidoarjo");
        addSingleColumn(roleStyle, "Tempat lahirnya ide-ide besar, revisi mendadak, dan diskusi\nyang seharusnya selesai 30 menit tetapi berakhir 4 jam kemudian.");
        addBigSpace();

        addSubtitle(subtitleStyle, "Culinary Support Division");
        addSingleColumn(nameStyle, "🦆 Bebek Setia Kawan");
        addSingleColumn(roleStyle, "Terima kasih telah menjaga HP tim tetap penuh\nmelalui konsumsi kalori yang konsisten.");
        addBigSpace();

        addSubtitle(subtitleStyle, "Remote Collaboration Partner");
        addSingleColumn(nameStyle, "💻 Google Meet");
        addSingleColumn(roleStyle, "Terima kasih karena tetap bertahan walaupun koneksi internet\nkami tidak selalu memiliki komitmen yang sama.");
        addBigSpace();

        addTitle(titleStyle, "IN MEMORY OF");
        addSingleColumn(roleStyle, "Semua bug yang berhasil diperbaiki...");
        addSpace();
        addSingleColumn(roleStyle, "Dan juga...");
        addSpace();
        addSingleColumn(nameStyle, "Semua bug yang belum ditemukan.");
        addBigSpace();


        addTitle(titleStyle, "FINAL MESSAGE");
        addSingleColumn(roleStyle, "Terima kasih telah menemani perjalanan Timun Mas.\nGame ini dibuat dari kombinasi kerja keras, pembelajaran, revisi tanpa akhir,\ndan keyakinan penuh bahwa tombol Run suatu hari akan menghasilkan Build Successful.");
        addSpace();
        addQuote(quoteStyle, "\"Every bug tells a story.\"\n\"Every crash teaches a lesson.\"\n\"Every completed project becomes a memory.\"");
        addBigSpace();
        addBigSpace();

        addTitle(titleStyle, "SUKMA GOWONG\n2026");
        addSpace();
        addSpace();
        addSpace();
        addQuote(quoteStyle, "\"Build Successful.\" ✅\n\"0 Errors, 0 Warnings.\" ✨\n(untuk sekali ini saja)");

        addBigSpace();
        addBigSpace();
        addBigSpace();

        scrollTable.pack();
        stage.addActor(scrollTable);
    }



    private void addTitle(Label.LabelStyle style, String text) {
        Label l = new Label(text, style);
        l.setAlignment(Align.center);
        scrollTable.add(l).colspan(2).padBottom(30).row();
    }

    private void addSubtitle(Label.LabelStyle style, String text) {
        Label l = new Label(text, style);
        l.setAlignment(Align.center);
        scrollTable.add(l).colspan(2).padBottom(15).row();
    }

    private void addSingleColumn(Label.LabelStyle style, String text) {
        Label l = new Label(text, style);
        l.setAlignment(Align.center);
        scrollTable.add(l).colspan(2).padBottom(10).row();
    }

    private void addDoubleColumn(Label.LabelStyle roleStyle, String roleText, Label.LabelStyle nameStyle, String nameText) {

        Label lblRole = new Label(roleText, roleStyle);
        lblRole.setAlignment(Align.right);
        lblRole.setWrap(true);


        Label lblName = new Label(nameText, nameStyle);
        lblName.setAlignment(Align.left);
        lblName.setWrap(true);


        float colWidth = 850f;
        scrollTable.add(lblRole).width(colWidth).right().padRight(30).padBottom(15);
        scrollTable.add(lblName).width(colWidth).left().padLeft(30).padBottom(15).row();
    }

    private void addQuote(Label.LabelStyle style, String text) {
        Label l = new Label(text, style);
        l.setAlignment(Align.center);
        scrollTable.add(l).colspan(2).padTop(30).padBottom(60).row();
    }

    private void addSpace() {
        scrollTable.add().colspan(2).height(50).row();
    }

    private void addBigSpace() {
        scrollTable.add().colspan(2).height(150).row();
    }



    private void startScrolling() {
        float startY = -scrollTable.getHeight() / 2f - 200f;
        float endY = 1080f + (scrollTable.getHeight() / 2f) + 200f;

        scrollTable.setPosition(1920 / 2f, startY, Align.center);

        scrollTable.addAction(Actions.sequence(
            Actions.moveToAligned(1920 / 2f, endY, Align.center, scrollDuration, Interpolation.linear),
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    returnToMenu();
                }
            })
        ));
    }

    private void setupSkipButton() {
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = FontManager.getInstance().getBodyFont();
        btnStyle.fontColor = new Color(0.6f, 0.6f, 0.6f, 1f);
        btnStyle.overFontColor = Color.WHITE;

        final TextButton skipBtn = new TextButton("SKIP >>", btnStyle);
        skipBtn.setTransform(true);
        skipBtn.setOrigin(Align.center);
        skipBtn.setPosition(1700, 50);

        skipBtn.addListener(new ClickListener() {
            @Override public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                skipBtn.addAction(Actions.scaleTo(1.2f, 1.2f, 0.2f, Interpolation.smooth));
            }
            @Override public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                skipBtn.addAction(Actions.scaleTo(1.0f, 1.0f, 0.2f, Interpolation.smooth));
            }
            @Override public void clicked(InputEvent event, float x, float y) {
                returnToMenu();
            }
        });

        stage.addActor(skipBtn);
    }

    private void returnToMenu() {
        AudioManager.getInstance().stopMusic(1.5f);
        AudioManager.getInstance().playMusic(AudioTrack.THEME, 1.5f);

        ScreenManager.getInstance().setScreen(new MainMenuScreen(), new FadeTransition(1.5f));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }
}
