package net.oxog.phonebox.hp71.widgets;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.oxog.phonebox.hp71.components.CardViewContainer;
import net.oxog.phonebox.hp71.components.NativeLineChartView;
import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.layouts.SplitColumnLayout;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

/**
 * Widget modular com 2 gráficos nativos em tempo real (CPU+RAM e GPU+Temperatura, 60 amostras).
 * Legendas coloridas por série e altura adaptada à janela real (com barra fixa do Android 4.2.2).
 */
public final class SystemChartsWidget implements DashboardWidget {

    private final SplitColumnLayout split;
    private final CardViewContainer cpuCard;
    private final TextView cpuLegend;
    private final NativeLineChartView cpuChart;
    private final TextView cpuFootLeft;
    private final TextView cpuFootRight;

    private final CardViewContainer gpuCard;
    private final TextView gpuLegend;
    private final NativeLineChartView gpuChart;
    private final TextView gpuFootLeft;
    private final TextView gpuFootRight;

    public SystemChartsWidget(WidgetContext wctx) {
        ThemeDimens d = AppTheme.get().dimens();
        split = new SplitColumnLayout(wctx.context).setWeights(1f, 1f).setGapPx(d.spaceSm);

        // Card CPU & RAM
        cpuCard = new CardViewContainer(wctx.context);
        cpuLegend = new TextView(wctx.context);
        cpuLegend.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        cpuLegend.setSingleLine(true);
        cpuCard.setHeader("CPU & RAM (AO VIVO)").setHeaderRight(cpuLegend);

        cpuChart = new NativeLineChartView(wctx.context);
        cpuCard.addBodyView(cpuChart, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, d.dp(106)));

        LinearLayout cpuFoot = new LinearLayout(wctx.context);
        cpuFoot.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams cfLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cfLp.topMargin = d.dp(5);
        cpuFootLeft = new TextView(wctx.context);
        cpuFootLeft.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        cpuFootLeft.setSingleLine(true);
        cpuFootLeft.setEllipsize(TextUtils.TruncateAt.END);
        cpuFoot.addView(cpuFootLeft, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        cpuFootRight = new TextView(wctx.context);
        cpuFootRight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        cpuFootRight.setSingleLine(true);
        cpuFootRight.setGravity(Gravity.RIGHT);
        LinearLayout.LayoutParams cfrLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cfrLp.leftMargin = d.dp(8);
        cpuFoot.addView(cpuFootRight, cfrLp);
        cpuCard.addBodyView(cpuFoot, cfLp);

        // Card GPU & Temp
        gpuCard = new CardViewContainer(wctx.context);
        gpuLegend = new TextView(wctx.context);
        gpuLegend.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        gpuLegend.setSingleLine(true);
        gpuCard.setHeader("GPU & TEMPERATURA").setHeaderRight(gpuLegend);

        gpuChart = new NativeLineChartView(wctx.context);
        gpuCard.addBodyView(gpuChart, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, d.dp(106)));

        LinearLayout gpuFoot = new LinearLayout(wctx.context);
        gpuFoot.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams gfLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        gfLp.topMargin = d.dp(5);
        gpuFootLeft = new TextView(wctx.context);
        gpuFootLeft.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        gpuFootLeft.setSingleLine(true);
        gpuFootLeft.setEllipsize(TextUtils.TruncateAt.END);
        gpuFoot.addView(gpuFootLeft, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        gpuFootRight = new TextView(wctx.context);
        gpuFootRight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        gpuFootRight.setSingleLine(true);
        gpuFootRight.setGravity(Gravity.RIGHT);
        LinearLayout.LayoutParams gfrLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        gfrLp.leftMargin = d.dp(8);
        gpuFoot.addView(gpuFootRight, gfrLp);
        gpuCard.addBodyView(gpuFoot, gfLp);

        split.setPanes(cpuCard, gpuCard);
        applyTheme();
    }

    @Override
    public View getView() { return split; }

    private static CharSequence coloredLegend(String lbl1, String val1, int col1, String lbl2, String val2, int col2) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        int s1 = sb.length();
        sb.append("● ");
        sb.setSpan(new ForegroundColorSpan(col1), s1, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.append(lbl1).append(" ");
        int v1 = sb.length();
        sb.append(val1);
        sb.setSpan(new ForegroundColorSpan(col1), v1, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.append("   ");
        int s2 = sb.length();
        sb.append("● ");
        sb.setSpan(new ForegroundColorSpan(col2), s2, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.append(lbl2).append(" ");
        int v2 = sb.length();
        sb.append(val2);
        sb.setSpan(new ForegroundColorSpan(col2), v2, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }

    @Override
    public void bindData(AppState state) {
        ThemeColors c = AppTheme.get().colors();
        if (state.lastSyncAt == 0L) {
            cpuLegend.setText(coloredLegend("CPU", "--", c.accent, "RAM", "--", c.purple));
            gpuLegend.setText(coloredLegend("Uso", "--", c.green, "Temp", "--", c.amber));
            cpuFootLeft.setText("Conectando...");
            cpuFootRight.setText("");
            gpuFootLeft.setText("--");
            gpuFootRight.setText("");
            return;
        }

        AppState.PcStats pc = state.pc;
        String cpuVal = Math.round(pc.cpuPct) + "%" + (pc.hasCpuTemp ? (" (" + Math.round(pc.cpuTemp) + "°C)") : "");
        String ramVal = pc.ramPct() + "%";
        cpuLegend.setText(coloredLegend("CPU", cpuVal, c.accent, "RAM", ramVal, c.purple));
        cpuChart.setHistories(state.histCpu, state.histRam);
        cpuFootLeft.setText(pc.cpuName);
        cpuFootRight.setText(FormatUtils.round1(pc.ramUsedGb) + "/" + FormatUtils.round1(pc.ramTotalGb) + " GB · " + FormatUtils.fmtUptime(pc.uptimeSec));

        String gUse = pc.hasGpu ? (Math.round(pc.gpuUtil) + "%") : "--";
        String gTemp = pc.hasGpu && pc.hasGpuTemp ? (Math.round(pc.gpuTemp) + "°C") : "--";
        gpuLegend.setText(coloredLegend("Uso", gUse, c.green, "Temp", gTemp, c.amber));
        gpuChart.setHistories(state.histGpu, state.histTemp);
        gpuFootLeft.setText(pc.hasGpu ? pc.gpuName : "Sem GPU");
        if (pc.hasGpu) {
            StringBuilder gEx = new StringBuilder();
            if (pc.hasGpuVram) {
                gEx.append(FormatUtils.round1(pc.gpuVramUsed)).append("/").append(FormatUtils.round1(pc.gpuVramTotal)).append(" GB");
            } else if (pc.gpuSharedTotal > 0f) {
                gEx.append("Comp. ").append(FormatUtils.round1(pc.gpuSharedUsed)).append("/").append(FormatUtils.round1(pc.gpuSharedTotal)).append(" GB");
            }
            if (pc.hasGpuPower) {
                if (gEx.length() > 0) gEx.append(" · ");
                gEx.append(Math.round(pc.gpuPower)).append("W");
            }
            gpuFootRight.setText(gEx.toString());
        } else {
            gpuFootRight.setText("");
        }
    }

    @Override
    public void onOrientationChanged(boolean portrait) {
        split.setPortrait(portrait);
        int chH = AppTheme.get().dimens().dp(portrait ? 126 : 104);
        LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) cpuChart.getLayoutParams();
        if (lp1 != null && lp1.height != chH) {
            lp1.height = chH;
            cpuChart.setLayoutParams(lp1);
        }
        LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams) gpuChart.getLayoutParams();
        if (lp2 != null && lp2.height != chH) {
            lp2.height = chH;
            gpuChart.setLayoutParams(lp2);
        }
    }

    @Override
    public void setActive(boolean visible) { }

    @Override
    public void applyTheme() {
        ThemeColors c = AppTheme.get().colors();
        cpuCard.applyTheme();
        gpuCard.applyTheme();
        cpuLegend.setTextColor(c.textSecondary);
        gpuLegend.setTextColor(c.textSecondary);
        cpuFootLeft.setTextColor(c.textMuted);
        cpuFootRight.setTextColor(c.textMuted);
        gpuFootLeft.setTextColor(c.textMuted);
        gpuFootRight.setTextColor(c.textMuted);
        cpuChart.setColors(c.accent, c.purple).applyTheme();
        gpuChart.setColors(c.green, c.amber).applyTheme();
    }
}

