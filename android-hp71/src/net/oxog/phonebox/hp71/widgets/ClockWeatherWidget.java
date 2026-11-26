package net.oxog.phonebox.hp71.widgets;

import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.oxog.phonebox.hp71.components.CardViewContainer;
import net.oxog.phonebox.hp71.components.StatusPillView;
import net.oxog.phonebox.hp71.components.WeatherGlyphView;
import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

import java.util.Calendar;

/**
 * Widget modular de Relógio + Clima ao vivo da aba Início.
 */
public final class ClockWeatherWidget implements DashboardWidget {

    private final CardViewContainer card;
    private final TextView timeView;
    private final TextView secView;
    private final StatusPillView locPill;
    private final StatusPillView rainPill;
    private final TextView dateView;
    private final View divider;
    private final LinearLayout weatherRow;
    private final WeatherGlyphView glyphView;
    private final TextView tempView;
    private final TextView condView;
    private final TextView subView;

    private boolean showSeconds = true;
    private boolean hour12 = false;
    private String tzLabel = "Brasília (UTC-3)";

    public ClockWeatherWidget(final WidgetContext wctx) {
        ThemeDimens d = AppTheme.get().dimens();
        card = new CardViewContainer(wctx.context).setVariant(CardViewContainer.VARIANT_HIGHLIGHT);
        card.setClickable(true);
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wctx.host != null) wctx.host.navigateToView(6);
            }
        });

        LinearLayout topRow = new LinearLayout(wctx.context);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.TOP);

        LinearLayout clockWrap = new LinearLayout(wctx.context);
        clockWrap.setOrientation(LinearLayout.HORIZONTAL);
        clockWrap.setGravity(Gravity.BOTTOM);

        timeView = new TextView(wctx.context);
        timeView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 58f);
        timeView.setIncludeFontPadding(false);
        timeView.setText("--:--");
        clockWrap.addView(timeView);

        secView = new TextView(wctx.context);
        secView.setTypeface(Typeface.DEFAULT_BOLD);
        secView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        LinearLayout.LayoutParams secLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        secLp.leftMargin = d.dp(4);
        secLp.bottomMargin = d.dp(5);
        clockWrap.addView(secView, secLp);

        topRow.addView(clockWrap, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout pillsCol = new LinearLayout(wctx.context);
        pillsCol.setOrientation(LinearLayout.VERTICAL);
        pillsCol.setGravity(Gravity.RIGHT);

        locPill = new StatusPillView(wctx.context).setPill("Rio de Janeiro, Brasil", StatusPillView.STYLE_ACCENT);
        locPill.setMaxWidth(d.dp(175));
        pillsCol.addView(locPill);

        rainPill = new StatusPillView(wctx.context).setPill("SEM CHUVA", StatusPillView.STYLE_SUCCESS);
        LinearLayout.LayoutParams rpLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rpLp.topMargin = d.dp(4);
        pillsCol.addView(rainPill, rpLp);

        topRow.addView(pillsCol);
        card.addBodyView(topRow);

        dateView = new TextView(wctx.context);
        dateView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        dateView.setSingleLine(true);
        dateView.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams dateLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dateLp.topMargin = d.dp(4);
        card.addBodyView(dateView, dateLp);

        divider = new View(wctx.context);
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, d.strokePx);
        divLp.topMargin = d.dp(8);
        divLp.bottomMargin = d.dp(8);
        card.addBodyView(divider, divLp);

        weatherRow = new LinearLayout(wctx.context);
        weatherRow.setOrientation(LinearLayout.HORIZONTAL);
        weatherRow.setGravity(Gravity.CENTER_VERTICAL);

        glyphView = new WeatherGlyphView(wctx.context);
        LinearLayout.LayoutParams glLp = new LinearLayout.LayoutParams(d.dp(44), d.dp(44));
        glLp.rightMargin = d.dp(10);
        weatherRow.addView(glyphView, glLp);

        LinearLayout wxTextCol = new LinearLayout(wctx.context);
        wxTextCol.setOrientation(LinearLayout.VERTICAL);

        LinearLayout wxLine1 = new LinearLayout(wctx.context);
        wxLine1.setOrientation(LinearLayout.HORIZONTAL);
        wxLine1.setGravity(Gravity.CENTER_VERTICAL);

        tempView = new TextView(wctx.context);
        tempView.setTypeface(Typeface.DEFAULT_BOLD);
        tempView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f);
        tempView.setText("--°C");
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tLp.rightMargin = d.dp(8);
        wxLine1.addView(tempView, tLp);

        condView = new TextView(wctx.context);
        condView.setTypeface(Typeface.DEFAULT_BOLD);
        condView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        condView.setSingleLine(true);
        condView.setEllipsize(TextUtils.TruncateAt.END);
        condView.setText("Carregando clima...");
        wxLine1.addView(condView, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        wxTextCol.addView(wxLine1);

        subView = new TextView(wctx.context);
        subView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        subView.setSingleLine(true);
        subView.setEllipsize(TextUtils.TruncateAt.END);
        wxTextCol.addView(subView);

        weatherRow.addView(wxTextCol, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        card.addBodyView(weatherRow);

        applyTheme();
        tickClock();
    }

    public void tickClock() {
        Calendar now = Calendar.getInstance();
        int h = now.get(Calendar.HOUR_OF_DAY);
        int m = now.get(Calendar.MINUTE);
        int s = now.get(Calendar.SECOND);
        timeView.setText(FormatUtils.fmtHourMin(h, m, hour12));
        String suffix = FormatUtils.amPmSuffix(h, hour12);
        String secTxt = (showSeconds ? (":" + FormatUtils.pad2(s)) : "") + suffix;
        secView.setText(secTxt);
        secView.setVisibility(secTxt.length() > 0 ? View.VISIBLE : View.GONE);
        dateView.setText(FormatUtils.fmtDatePt(now) + " · " + tzLabel);
    }

    @Override
    public View getView() {
        return card;
    }

    @Override
    public void bindData(AppState state) {
        showSeconds = state.display.showSeconds;
        hour12 = state.display.hour12;
        dateView.setVisibility(state.showHomeDate ? View.VISIBLE : View.GONE);
        weatherRow.setVisibility(state.showHomeWeather ? View.VISIBLE : View.GONE);
        divider.setVisibility(state.showHomeWeather ? View.VISIBLE : View.GONE);

        AppState.WeatherData w = state.weather;
        if (w.tzAbbr != null && w.tzAbbr.length() > 0) {
            tzLabel = "America/Sao_Paulo".equals(w.timezone) ? ("Brasília (" + w.tzAbbr + ")") : w.tzAbbr;
        }
        locPill.setPill(w.city, StatusPillView.STYLE_ACCENT);
        if (w.ok) {
            glyphView.setWeather(w.code, w.isDay);
            String u = "°" + w.unit;
            tempView.setText(Math.round(w.temp) + u);
            condView.setText(FormatUtils.wmoPt(w.code) + " · " + Math.round(w.lo) + "° / " + Math.round(w.hi) + "°");
            subView.setText("Sens. " + Math.round(w.feels) + u
                    + " · Umid. " + w.humidity + "% · Vento " + Math.round(w.windKmh) + " km/h " + FormatUtils.windDirPt(w.windDir));
            boolean raining = w.code >= 51 || w.precipMm > 0.05f;
            if (raining) {
                rainPill.setPill("CHOVENDO · " + w.rainProb + "%", StatusPillView.STYLE_INFO);
            } else if (w.rainProb >= 50) {
                rainPill.setPill("CHUVA ALTA · " + w.rainProb + "%", StatusPillView.STYLE_WARN);
            } else if (w.rainProb >= 20) {
                rainPill.setPill("CHUVA " + w.rainProb + "%", StatusPillView.STYLE_INFO);
            } else {
                rainPill.setPill("SEM CHUVA · " + w.rainProb + "%", StatusPillView.STYLE_SUCCESS);
            }
        }
        tickClock();
    }

    @Override
    public void onOrientationChanged(boolean portrait) {
        timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, portrait ? 60f : 58f);
        locPill.setMaxWidth(AppTheme.get().dimens().dp(portrait ? 200 : 165));
    }

    @Override
    public void setActive(boolean visible) { }

    @Override
    public void applyTheme() {
        ThemeColors c = AppTheme.get().colors();
        card.applyTheme();
        timeView.setTextColor(c.textPrimary);
        secView.setTextColor(c.accent);
        dateView.setTextColor(c.textMuted);
        divider.setBackgroundColor(c.divider);
        tempView.setTextColor(c.sky);
        condView.setTextColor(c.textPrimary);
        subView.setTextColor(c.textMuted);
        locPill.applyTheme();
        rainPill.applyTheme();
    }
}

