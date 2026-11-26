package net.oxog.phonebox.hp71.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

import java.util.ArrayList;
import java.util.List;

/**
 * Barra de navegação modular dinâmica (reflete exatamente a ordem, títulos e abas ativas de 127.0.0.1:8765).
 * No tema Hack, transforma-se em deck de comando terminal com trilha neon que se funde à barra preta fixa do Android 4.2.2.
 */
public class BottomNavBarView extends LinearLayout {

    public interface OnTabSelected {
        void onSelect(int tabPosition);
    }

    private final List<TabItemView> items = new ArrayList<TabItemView>();
    private final Paint hackRailPaint = new Paint();
    private final Paint animeRailPaint = new Paint();
    private OnTabSelected listener;
    private int selectedPos = 0;
    private boolean atTop = false;
    private String signature = "";

    public BottomNavBarView(Context context) {
        super(context);
        setOrientation(HORIZONTAL);
        setWillNotDraw(false);
        hackRailPaint.setStyle(Paint.Style.FILL);
        hackRailPaint.setColor(0xFF15693B);
        animeRailPaint.setStyle(Paint.Style.FILL);
        animeRailPaint.setColor(0x99FF7ED4);
        applyTheme();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        AppTheme t = AppTheme.get();
        if (t.isHack() || t.isAnime()) {
            float d = t.dimens().density;
            float lineH = Math.max(1f, d);
            Paint p = t.isHack() ? hackRailPaint : animeRailPaint;
            if (atTop) {
                canvas.drawRect(0f, getHeight() - lineH, getWidth(), getHeight(), p);
            } else {
                canvas.drawRect(0f, 0f, getWidth(), lineH, p);
            }
        }
    }

    public void setListener(OnTabSelected listener) {
        this.listener = listener;
    }

    public void setPositionTop(boolean atTop) {
        if (this.atTop != atTop) {
            this.atTop = atTop;
            applyTheme();
        }
    }

    public void setTabs(List<AppState.PageTab> tabs) {
        StringBuilder sig = new StringBuilder();
        for (int i = 0; i < tabs.size(); i++) {
            AppState.PageTab t = tabs.get(i);
            sig.append(t.id).append('|').append(t.title).append('|').append(t.viewIdx).append(';');
        }
        String nextSig = sig.toString();
        if (nextSig.equals(signature)) return;
        signature = nextSig;

        removeAllViews();
        items.clear();
        for (int i = 0; i < tabs.size(); i++) {
            final int pos = i;
            AppState.PageTab tab = tabs.get(i);
            TabItemView item = new TabItemView(getContext(), tab.viewIdx, tab.title);
            item.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onSelect(pos);
                }
            });
            addView(item, new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
            items.add(item);
        }
        setSelectedPosition(selectedPos);
    }

    public void setSelectedPosition(int pos) {
        this.selectedPos = pos;
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setTabActive(i == pos, atTop);
        }
    }

    public void applyTheme() {
        ThemeColors c = AppTheme.get().colors();
        setBackgroundColor(c.navBar);
        animeRailPaint.setColor(ThemeColors.withAlpha(c.accent, 0x99));
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setTabActive(i == selectedPos, atTop);
        }
        invalidate();
    }

    private static final class TabItemView extends LinearLayout {
        private final View indicatorTop;
        private final NavGlyph glyph;
        private final TextView label;
        private final View indicatorBot;
        private final Paint hackBracketPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint hackDividerPaint = new Paint();
        private final Paint animeDividerPaint = new Paint();
        private final Paint animeStarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path animeStarPath = new Path();
        private boolean activeTab = false;

        TabItemView(Context ctx, int viewIdx, String title) {
            super(ctx);
            ThemeDimens d = AppTheme.get().dimens();
            setOrientation(VERTICAL);
            setGravity(Gravity.CENTER);
            setClickable(true);
            setWillNotDraw(false);

            hackBracketPaint.setStyle(Paint.Style.STROKE);
            hackBracketPaint.setStrokeWidth(Math.max(1.3f, 1.6f * d.density));
            hackBracketPaint.setColor(0xFF00F0FF);

            hackDividerPaint.setStyle(Paint.Style.FILL);
            hackDividerPaint.setColor(0x5515693B);

            animeDividerPaint.setStyle(Paint.Style.FILL);
            animeDividerPaint.setColor(0x386B4C9A);

            animeStarPaint.setStyle(Paint.Style.FILL);
            animeStarPaint.setColor(0xD8FFD56B);

            indicatorTop = new View(ctx);
            addView(indicatorTop, new LayoutParams(LayoutParams.MATCH_PARENT, d.dp(3)));

            LinearLayout center = new LinearLayout(ctx);
            center.setOrientation(VERTICAL);
            center.setGravity(Gravity.CENTER);
            center.setPadding(d.dp(2), d.dp(4), d.dp(2), d.dp(4));

            glyph = new NavGlyph(ctx, viewIdx);
            center.addView(glyph, new LayoutParams(d.dp(22), d.dp(22)));

            label = new TextView(ctx);
            label.setText(title);
            label.setTypeface(Typeface.DEFAULT_BOLD);
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, d.textLabelSp);
            label.setSingleLine(true);
            label.setEllipsize(TextUtils.TruncateAt.END);
            label.setGravity(Gravity.CENTER);
            LayoutParams lblLp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            lblLp.topMargin = d.dp(2);
            center.addView(label, lblLp);

            addView(center, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));

            indicatorBot = new View(ctx);
            addView(indicatorBot, new LayoutParams(LayoutParams.MATCH_PARENT, d.dp(3)));
        }

        void setTabActive(boolean active, boolean atTop) {
            this.activeTab = active;
            ThemeColors c = AppTheme.get().colors();
            animeDividerPaint.setColor(ThemeColors.withAlpha(c.border, 0x40));
            animeStarPaint.setColor(ThemeColors.withAlpha(c.starGold, 0xD8));
            setSelected(active);
            setBackgroundColor(active ? c.cardHighlightBg : c.navBar);
            int fg = active ? ((c.isHack || c.isAnime) ? c.accent : c.textPrimary) : c.textMuted;
            label.setTextColor(fg);
            glyph.setTintColor(fg, c.isHack);
            indicatorTop.setBackgroundColor(active && !atTop ? c.accent : 0x00000000);
            indicatorBot.setBackgroundColor(active && atTop ? c.accent : 0x00000000);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            AppTheme t = AppTheme.get();
            int w = getWidth();
            int h = getHeight();
            float d = t.dimens().density;
            if (t.isHack()) {
                // Subtle vertical terminal cell separator
                canvas.drawRect(w - Math.max(1f, d), 8f * d, w, h - 8f * d, hackDividerPaint);
                if (activeTab) {
                    float pad = 4f * d;
                    float arm = 7f * d;
                    // Glowing cyan/lime side corner ticks on active tab
                    canvas.drawLine(pad, pad + 3f * d, pad, pad + 3f * d + arm, hackBracketPaint);
                    canvas.drawLine(pad, pad + 3f * d, pad + arm, pad + 3f * d, hackBracketPaint);
                    canvas.drawLine(w - pad - arm, h - pad - 3f * d, w - pad, h - pad - 3f * d, hackBracketPaint);
                    canvas.drawLine(w - pad, h - pad - 3f * d - arm, w - pad, h - pad - 3f * d, hackBracketPaint);
                }
            } else if (t.isAnime()) {
                canvas.drawRect(w - Math.max(1f, d), 10f * d, w, h - 10f * d, animeDividerPaint);
                if (activeTab) {
                    drawMiniStar(canvas, 9f * d, 10f * d, 3.6f * d);
                    drawMiniStar(canvas, w - 9f * d, h - 10f * d, 3.0f * d);
                }
            }
        }

        private void drawMiniStar(Canvas canvas, float cx, float cy, float r) {
            float in = r * 0.28f;
            animeStarPath.reset();
            animeStarPath.moveTo(cx, cy - r);
            animeStarPath.lineTo(cx + in, cy - in);
            animeStarPath.lineTo(cx + r, cy);
            animeStarPath.lineTo(cx + in, cy + in);
            animeStarPath.lineTo(cx, cy + r);
            animeStarPath.lineTo(cx - in, cy + in);
            animeStarPath.lineTo(cx - r, cy);
            animeStarPath.lineTo(cx - in, cy - in);
            animeStarPath.close();
            canvas.drawPath(animeStarPath, animeStarPaint);
        }
    }

    private static final class NavGlyph extends View {
        private final int viewIdx;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final RectF rect = new RectF();
        private final RectF oval = new RectF();

        NavGlyph(Context ctx, int viewIdx) {
            super(ctx);
            this.viewIdx = viewIdx;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
        }

        void setTintColor(int color, boolean sharpTech) {
            paint.setColor(color);
            paint.setStrokeCap(sharpTech ? Paint.Cap.SQUARE : Paint.Cap.ROUND);
            paint.setStrokeJoin(sharpTech ? Paint.Join.MITER : Paint.Join.ROUND);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float s = Math.min(getWidth(), getHeight());
            if (s <= 4f) return;
            float u = s / 24f;
            float ox = (getWidth() - s) * 0.5f;
            float oy = (getHeight() - s) * 0.5f;
            paint.setStrokeWidth(2.0f * u);

            switch (viewIdx) {
                case 0: // Home
                    path.reset();
                    path.moveTo(ox + 3f * u, oy + 11f * u);
                    path.lineTo(ox + 12f * u, oy + 3.5f * u);
                    path.lineTo(ox + 21f * u, oy + 11f * u);
                    canvas.drawPath(path, paint);
                    rect.set(ox + 6f * u, oy + 10.5f * u, ox + 18f * u, oy + 20f * u);
                    canvas.drawRect(rect, paint);
                    break;
                case 1: // System monitor
                    rect.set(ox + 3f * u, oy + 4f * u, ox + 21f * u, oy + 16f * u);
                    canvas.drawRoundRect(rect, 2f * u, 2f * u, paint);
                    canvas.drawLine(ox + 12f * u, oy + 16f * u, ox + 12f * u, oy + 20f * u, paint);
                    canvas.drawLine(ox + 8f * u, oy + 20f * u, ox + 16f * u, oy + 20f * u, paint);
                    break;
                case 2: // Deck 2x2 grid
                    rect.set(ox + 3.5f * u, oy + 3.5f * u, ox + 10.5f * u, oy + 10.5f * u);
                    canvas.drawRoundRect(rect, 1.5f * u, 1.5f * u, paint);
                    rect.set(ox + 13.5f * u, oy + 3.5f * u, ox + 20.5f * u, oy + 10.5f * u);
                    canvas.drawRoundRect(rect, 1.5f * u, 1.5f * u, paint);
                    rect.set(ox + 3.5f * u, oy + 13.5f * u, ox + 10.5f * u, oy + 20.5f * u);
                    canvas.drawRoundRect(rect, 1.5f * u, 1.5f * u, paint);
                    rect.set(ox + 13.5f * u, oy + 13.5f * u, ox + 20.5f * u, oy + 20.5f * u);
                    canvas.drawRoundRect(rect, 1.5f * u, 1.5f * u, paint);
                    break;
                case 3: // Procs list
                    canvas.drawLine(ox + 4f * u, oy + 6f * u, ox + 20f * u, oy + 6f * u, paint);
                    canvas.drawLine(ox + 4f * u, oy + 12f * u, ox + 20f * u, oy + 12f * u, paint);
                    canvas.drawLine(ox + 4f * u, oy + 18f * u, ox + 20f * u, oy + 18f * u, paint);
                    break;
                case 4: // AI Sparkle
                    path.reset();
                    path.moveTo(ox + 12f * u, oy + 2.5f * u);
                    path.lineTo(ox + 15f * u, oy + 9f * u);
                    path.lineTo(ox + 21.5f * u, oy + 12f * u);
                    path.lineTo(ox + 15f * u, oy + 15f * u);
                    path.lineTo(ox + 12f * u, oy + 21.5f * u);
                    path.lineTo(ox + 9f * u, oy + 15f * u);
                    path.lineTo(ox + 2.5f * u, oy + 12f * u);
                    path.lineTo(ox + 9f * u, oy + 9f * u);
                    path.close();
                    canvas.drawPath(path, paint);
                    break;
                case 5: // Discord chat bubble
                    oval.set(ox + 3.5f * u, oy + 4f * u, ox + 20.5f * u, oy + 18.5f * u);
                    canvas.drawRoundRect(oval, 5f * u, 5f * u, paint);
                    canvas.drawLine(ox + 8f * u, oy + 18.5f * u, ox + 5f * u, oy + 21f * u, paint);
                    break;
                default: // Clima/Hora clock
                    canvas.drawCircle(ox + 12f * u, oy + 12f * u, 8.5f * u, paint);
                    canvas.drawLine(ox + 12f * u, oy + 7f * u, ox + 12f * u, oy + 12f * u, paint);
                    canvas.drawLine(ox + 12f * u, oy + 12f * u, ox + 16f * u, oy + 14.5f * u, paint);
                    break;
            }
        }
    }
}
