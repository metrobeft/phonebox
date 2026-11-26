package net.oxog.phonebox.hp71.layouts;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Container modular de 2 colunas com proporção configurável na Horizontal (ex.: 44/56 ou 50/50)
 * que automaticamente empilha em 1 coluna verticalmente no modo Retrato (600x1024).
 */
public class SplitColumnLayout extends LinearLayout {

    private View firstView;
    private View secondView;
    private float firstWeight = 1f;
    private float secondWeight = 1f;
    private int gapPx = 8;
    private boolean portrait = false;
    private boolean matchHeightHorizontal = true;

    public SplitColumnLayout(Context context) {
        super(context);
        setOrientation(HORIZONTAL);
        setBaselineAligned(false);
    }

    public SplitColumnLayout setMatchHeightHorizontal(boolean match) {
        this.matchHeightHorizontal = match;
        applyLayout();
        return this;
    }

    public SplitColumnLayout setWeights(float firstWeight, float secondWeight) {
        this.firstWeight = firstWeight;
        this.secondWeight = secondWeight;
        applyLayout();
        return this;
    }

    public SplitColumnLayout setGapPx(int gapPx) {
        this.gapPx = Math.max(0, gapPx);
        applyLayout();
        return this;
    }

    public void setPanes(View first, View second) {
        this.firstView = first;
        this.secondView = second;
        removeAllViews();
        if (first != null) addView(first);
        if (second != null) addView(second);
        applyLayout();
    }

    public void setPortrait(boolean portrait) {
        if (this.portrait != portrait) {
            this.portrait = portrait;
            applyLayout();
        }
    }

    private void applyLayout() {
        setOrientation(portrait ? VERTICAL : HORIZONTAL);
        int hMode = matchHeightHorizontal ? LayoutParams.MATCH_PARENT : LayoutParams.WRAP_CONTENT;
        if (firstView != null) {
            LinearLayout.LayoutParams lp1 = portrait
                    ? new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                    : new LinearLayout.LayoutParams(0, hMode, firstWeight);
            firstView.setLayoutParams(lp1);
        }
        if (secondView != null) {
            LinearLayout.LayoutParams lp2 = portrait
                    ? new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                    : new LinearLayout.LayoutParams(0, hMode, secondWeight);
            if (portrait) {
                lp2.topMargin = gapPx;
                lp2.leftMargin = 0;
            } else {
                lp2.topMargin = 0;
                lp2.leftMargin = gapPx;
            }
            secondView.setLayoutParams(lp2);
        }
        requestLayout();
    }
}

