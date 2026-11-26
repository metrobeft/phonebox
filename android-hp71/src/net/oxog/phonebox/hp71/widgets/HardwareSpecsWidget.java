package net.oxog.phonebox.hp71.widgets;

import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.oxog.phonebox.hp71.components.CardViewContainer;
import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

import java.util.ArrayList;
import java.util.List;

/**
 * Widget modular de especificações e telemetria detalhada do hardware do PC.
 * Recicla views entre atualizações (zero GC por ciclo de 1,5s no Cortex-A7).
 */
public final class HardwareSpecsWidget implements DashboardWidget {

    private final CardViewContainer card;
    private final LinearLayout listBox;
    private final List<RowHolder> pool = new ArrayList<RowHolder>();

    private static final class RowHolder {
        final LinearLayout row;
        final TextView keyView;
        final TextView valView;
        final View sep;

        RowHolder(LinearLayout row, TextView k, TextView v, View sep) {
            this.row = row;
            this.keyView = k;
            this.valView = v;
            this.sep = sep;
        }
    }

    public HardwareSpecsWidget(WidgetContext wctx) {
        card = new CardViewContainer(wctx.context).setHeader("DETALHES DO HARDWARE");
        listBox = new LinearLayout(wctx.context);
        listBox.setOrientation(LinearLayout.VERTICAL);
        card.addBodyView(listBox);
        applyTheme();
    }

    @Override
    public View getView() { return card; }

    @Override
    public void bindData(AppState state) {
        AppState.PcStats pc = state.pc;
        List<String[]> rows = new ArrayList<String[]>();
        rows.add(new String[] { "RAM", FormatUtils.round1(pc.ramUsedGb) + " / " + FormatUtils.round1(pc.ramTotalGb) + " GB (" + pc.ramPct() + "%)" });
        if (pc.hasCpuTemp) {
            rows.add(new String[] { "Temp. CPU", Math.round(pc.cpuTemp) + "°C" });
        }
        if (pc.hasGpu) {
            rows.add(new String[] { "GPU", pc.gpuName });
            if (pc.hasGpuClock || pc.hasGpuMemClock) {
                String c1 = pc.hasGpuClock ? (pc.gpuClock + " MHz") : "--";
                String c2 = pc.hasGpuMemClock ? (pc.gpuMemClock + " MHz") : "--";
                rows.add(new String[] { "Clock GPU / Mem", c1 + " / " + c2 });
            }
            if (pc.hasGpuPower) {
                rows.add(new String[] { "Energia", FormatUtils.round1(pc.gpuPower) + " W" + (pc.gpuPowerLimit > 0 ? (" / " + FormatUtils.round1(pc.gpuPowerLimit) + " W") : "") });
            }
            if (pc.hasGpuVram) {
                rows.add(new String[] { "VRAM", FormatUtils.round1(pc.gpuVramUsed) + " / " + FormatUtils.round1(pc.gpuVramTotal) + " GB" });
            } else if (pc.gpuSharedTotal > 0f) {
                rows.add(new String[] { "Mem. Compartilhada", FormatUtils.round1(pc.gpuSharedUsed) + " / " + FormatUtils.round1(pc.gpuSharedTotal) + " GB" });
            }
            if (pc.hasGpuFan && pc.gpuFan > 0) {
                rows.add(new String[] { "Ventoinha GPU", pc.gpuFan + "%" });
            }
            if (pc.gpuDriver.length() > 0) rows.add(new String[] { "Driver", pc.gpuDriver });
        }
        rows.add(new String[] { "Download / Upload", FormatUtils.fmtBytesPerSec(pc.netDownBps) + " / " + FormatUtils.fmtBytesPerSec(pc.netUpBps) });
        StringBuilder pSb = new StringBuilder();
        if (state.ping.hasMs) pSb.append(Math.round(state.ping.ms)).append(" ms");
        else pSb.append("--");
        if (state.ping.hasAvgMs) pSb.append(" (méd ").append(FormatUtils.round1(state.ping.avgMs)).append("ms)");
        if (state.ping.lossPct > 0.1f) pSb.append(" · ").append(FormatUtils.round1(state.ping.lossPct)).append("% perda");
        rows.add(new String[] { "Ping (" + state.ping.host + ")", pSb.toString() });
        rows.add(new String[] { "Uptime", FormatUtils.fmtUptime(pc.uptimeSec) });

        ThemeColors c = AppTheme.get().colors();
        ThemeDimens d = AppTheme.get().dimens();
        while (pool.size() < rows.size()) {
            LinearLayout row = new LinearLayout(card.getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, d.dp(4), 0, d.dp(4));

            TextView k = new TextView(card.getContext());
            k.setTextColor(c.textMuted);
            k.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
            k.setSingleLine(true);
            k.setEllipsize(TextUtils.TruncateAt.END);
            k.setMaxWidth(d.dp(155));
            row.addView(k, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView v = new TextView(card.getContext());
            v.setTextColor(c.textPrimary);
            v.setTypeface(Typeface.DEFAULT_BOLD);
            v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
            v.setGravity(Gravity.RIGHT);
            v.setSingleLine(true);
            v.setEllipsize(TextUtils.TruncateAt.END);
            LinearLayout.LayoutParams vLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            vLp.leftMargin = d.dp(8);
            row.addView(v, vLp);

            View sep = new View(card.getContext());
            sep.setBackgroundColor(c.divider);

            listBox.addView(row);
            listBox.addView(sep, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, d.strokePx));
            pool.add(new RowHolder(row, k, v, sep));
        }

        for (int i = 0; i < pool.size(); i++) {
            RowHolder rh = pool.get(i);
            if (i < rows.size()) {
                String[] r = rows.get(i);
                rh.row.setVisibility(View.VISIBLE);
                rh.keyView.setText(r[0]);
                rh.valView.setText(r[1]);
                rh.sep.setVisibility(i < rows.size() - 1 ? View.VISIBLE : View.GONE);
            } else {
                rh.row.setVisibility(View.GONE);
                rh.sep.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onOrientationChanged(boolean portrait) { }

    @Override
    public void setActive(boolean visible) { }

    @Override
    public void applyTheme() {
        ThemeColors c = AppTheme.get().colors();
        card.applyTheme();
        for (int i = 0; i < pool.size(); i++) {
            RowHolder rh = pool.get(i);
            rh.keyView.setTextColor(c.textMuted);
            rh.valView.setTextColor(c.textPrimary);
            rh.sep.setBackgroundColor(c.divider);
        }
    }
}

