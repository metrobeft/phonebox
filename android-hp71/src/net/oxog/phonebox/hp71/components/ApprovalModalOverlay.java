package net.oxog.phonebox.hp71.components;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.DrawableFactory;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

/**
 * Modal global sobreposto a qualquer aba para aprovação de ferramentas de IA (Claude Code / Codex).
 */
public class ApprovalModalOverlay extends FrameLayout {

    public interface DecisionListener {
        void onDecide(String approvalId, String decision);
    }

    private final LinearLayout card;
    private final TextView sourceView;
    private final TextView projectView;
    private final TextView toolView;
    private final TextView detailView;
    private final ActionButtonView denyBtn;
    private final ActionButtonView allowBtn;

    private String activeApprovalId = "";
    private DecisionListener listener;

    public ApprovalModalOverlay(Context context) {
        super(context);
        ThemeDimens d = AppTheme.get().dimens();
        setBackgroundColor(0xDD04060A);
        setClickable(true);
        setVisibility(GONE);
        setPadding(d.dp(18), d.dp(18), d.dp(18), d.dp(18));

        final int maxCardW = d.dp(520);
        card = new LinearLayout(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int wSize = MeasureSpec.getSize(widthMeasureSpec);
                int wMode = MeasureSpec.getMode(widthMeasureSpec);
                if (wMode != MeasureSpec.UNSPECIFIED && wSize > maxCardW) {
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxCardW, MeasureSpec.AT_MOST);
                }
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        };
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(d.dp(16), d.dp(14), d.dp(16), d.dp(14));
        FrameLayout.LayoutParams cardLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        addView(card, cardLp);

        sourceView = new TextView(context);
        sourceView.setTypeface(Typeface.DEFAULT_BOLD);
        sourceView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        card.addView(sourceView);

        projectView = new TextView(context);
        projectView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        LinearLayout.LayoutParams projLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        projLp.topMargin = d.dp(3);
        projLp.bottomMargin = d.dp(8);
        card.addView(projectView, projLp);

        toolView = new TextView(context);
        toolView.setTypeface(Typeface.DEFAULT_BOLD);
        toolView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        card.addView(toolView);

        ScrollView sv = new ScrollView(context);
        detailView = new TextView(context);
        detailView.setTypeface(Typeface.MONOSPACE);
        detailView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        detailView.setPadding(d.dp(10), d.dp(10), d.dp(10), d.dp(10));
        sv.addView(detailView);
        LinearLayout.LayoutParams svLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, d.dp(140));
        svLp.topMargin = d.dp(8);
        svLp.bottomMargin = d.dp(14);
        card.addView(sv, svLp);

        LinearLayout actions = new LinearLayout(context);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        denyBtn = new ActionButtonView(context);
        denyBtn.setButton("NEGAR", ActionButtonView.VARIANT_DANGER);
        denyBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null && activeApprovalId.length() > 0) {
                    listener.onDecide(activeApprovalId, "deny");
                }
                setVisibility(GONE);
            }
        });
        LinearLayout.LayoutParams b1 = new LinearLayout.LayoutParams(0, d.dp(48), 1f);
        b1.rightMargin = d.dp(10);
        actions.addView(denyBtn, b1);

        allowBtn = new ActionButtonView(context);
        allowBtn.setButton("PERMITIR", ActionButtonView.VARIANT_PRIMARY);
        allowBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null && activeApprovalId.length() > 0) {
                    listener.onDecide(activeApprovalId, "allow");
                }
                setVisibility(GONE);
            }
        });
        actions.addView(allowBtn, new LinearLayout.LayoutParams(0, d.dp(48), 1f));

        card.addView(actions);
        applyTheme();
    }

    public void setDecisionListener(DecisionListener listener) {
        this.listener = listener;
    }

    public void bind(AppState state) {
        if (state == null || state.approvals.isEmpty()) {
            activeApprovalId = "";
            setVisibility(GONE);
            return;
        }
        AppState.ApprovalItem item = state.approvals.get(0);
        activeApprovalId = item.id;
        sourceView.setText(item.source + " · Permissão");
        projectView.setText(item.project + (item.cwd.length() > 0 ? " — " + item.cwd : ""));
        toolView.setText(item.tool);
        detailView.setText(item.detail);
        setVisibility(VISIBLE);
    }

    public void applyTheme() {
        AppTheme t = AppTheme.get();
        ThemeColors c = t.colors();
        ThemeDimens d = t.dimens();
        if (t.isHack()) {
            setBackgroundColor(0xEB000503);
            card.setBackground(DrawableFactory.cardBackground(t, CardViewContainer.VARIANT_HIGHLIGHT));
        } else if (t.isAnime()) {
            setBackgroundColor(0xE8090516);
            card.setBackground(DrawableFactory.cardBackground(t, CardViewContainer.VARIANT_HIGHLIGHT));
        } else {
            setBackgroundColor(0xDD04060A);
            card.setBackground(DrawableFactory.roundedRect(c.cardBg, c.accent, d.dp(2), d.dp(12)));
        }
        sourceView.setTextColor(c.accent);
        projectView.setTextColor(c.textMuted);
        toolView.setTextColor(c.textPrimary);
        detailView.setTextColor(c.textSecondary);
        detailView.setBackground(DrawableFactory.roundedRect(t.isAnime() ? 0xE0110A26 : c.bg, c.border, d.strokePx, t.isHack() ? 0f : d.dp(8)));
        denyBtn.applyTheme();
        allowBtn.applyTheme();
    }
}

