package net.oxog.phonebox.hp71.components;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.DrawableFactory;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

/**
 * Container padrão de card modular do Design System.
 * Suporta cabeçalho opcional (SectionHeaderView), variantes visuais e slot livre para conteúdo.
 */
public class CardViewContainer extends LinearLayout {

    public static final int VARIANT_DEFAULT = 0;
    public static final int VARIANT_HIGHLIGHT = 1;
    public static final int VARIANT_ALT = 2;
    public static final int VARIANT_SUCCESS = 3;

    private final SectionHeaderView header;
    private final LinearLayout body;
    private int variant = VARIANT_DEFAULT;

    public CardViewContainer(Context context) {
        super(context);
        ThemeDimens d = AppTheme.get().dimens();
        setOrientation(VERTICAL);
        setPadding(d.cardPaddingH, d.cardPaddingV, d.cardPaddingH, d.cardPaddingV);

        header = new SectionHeaderView(context);
        header.setVisibility(GONE);
        addView(header, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        body = new LinearLayout(context);
        body.setOrientation(VERTICAL);
        addView(body, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        applyTheme();
    }

    public CardViewContainer setHeader(String title) {
        header.setTitle(title);
        header.setVisibility(VISIBLE);
        return this;
    }

    public CardViewContainer setHeaderMeta(String meta) {
        header.setMeta(meta);
        header.setVisibility(VISIBLE);
        return this;
    }

    public CardViewContainer setHeaderRight(View right) {
        header.setRightView(right);
        header.setVisibility(VISIBLE);
        return this;
    }

    public CardViewContainer setVariant(int variant) {
        if (this.variant != variant) {
            this.variant = variant;
            applyTheme();
        }
        return this;
    }

    public LinearLayout body() {
        return body;
    }

    public void addBodyView(View child) {
        body.addView(child);
    }

    public void addBodyView(View child, LayoutParams lp) {
        body.addView(child, lp);
    }

    public void applyTheme() {
        setBackground(DrawableFactory.cardBackground(AppTheme.get(), variant));
        header.applyTheme();
    }
}

