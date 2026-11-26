package net.oxog.phonebox.hp71.layouts;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Container modular em grade responsiva (N colunas em Horizontal / M colunas em Vertical).
 * Sem RecyclerView / ConstraintLayout: usa LinearLayouts leves e reutilizáveis, 100% API 17.
 */
public class ResponsiveGridLayout extends LinearLayout {

    private final List<View> items = new ArrayList<View>();
    private int landscapeCols = 4;
    private int portraitCols = 2;
    private int gapPx = 8;
    private boolean portrait = false;

    public ResponsiveGridLayout(Context context) {
        super(context);
        setOrientation(VERTICAL);
    }

    public ResponsiveGridLayout setColumns(int landscapeCols, int portraitCols) {
        this.landscapeCols = Math.max(1, landscapeCols);
        this.portraitCols = Math.max(1, portraitCols);
        rebuildGrid();
        return this;
    }

    public ResponsiveGridLayout setGapPx(int gapPx) {
        this.gapPx = Math.max(0, gapPx);
        rebuildGrid();
        return this;
    }

    public void setPortrait(boolean portrait) {
        if (this.portrait != portrait) {
            this.portrait = portrait;
            rebuildGrid();
        }
    }

    public void setItems(List<View> views) {
        items.clear();
        if (views != null) items.addAll(views);
        rebuildGrid();
    }

    public void addGridItem(View v) {
        if (v != null) {
            items.add(v);
            rebuildGrid();
        }
    }

    public void clearGridItems() {
        items.clear();
        removeAllViews();
    }

    public void rebuildGrid() {
        // Desanexa filhos com segurança antes de remontar as linhas
        for (int i = 0; i < items.size(); i++) {
            View child = items.get(i);
            if (child.getParent() instanceof android.view.ViewGroup) {
                ((android.view.ViewGroup) child.getParent()).removeView(child);
            }
        }
        removeAllViews();

        if (items.isEmpty()) return;
        int cols = portrait ? portraitCols : landscapeCols;
        int rows = (items.size() + cols - 1) / cols;

        for (int r = 0; r < rows; r++) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(HORIZONTAL);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            if (r > 0) rowLp.topMargin = gapPx;
            row.setLayoutParams(rowLp);

            for (int c = 0; c < cols; c++) {
                int idx = r * cols + c;
                LinearLayout.LayoutParams cellLp = new LinearLayout.LayoutParams(
                        0, LayoutParams.MATCH_PARENT, 1f);
                if (c > 0) cellLp.leftMargin = gapPx;
                if (idx < items.size()) {
                    View child = items.get(idx);
                    child.setLayoutParams(cellLp);
                    row.addView(child);
                } else {
                    View spacer = new View(getContext());
                    spacer.setLayoutParams(cellLp);
                    row.addView(spacer);
                }
            }
            addView(row);
        }
    }
}

