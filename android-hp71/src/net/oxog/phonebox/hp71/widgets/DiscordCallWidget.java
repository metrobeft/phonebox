package net.oxog.phonebox.hp71.widgets;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import net.oxog.phonebox.hp71.components.ActionButtonView;
import net.oxog.phonebox.hp71.components.AvatarView;
import net.oxog.phonebox.hp71.components.CardViewContainer;
import net.oxog.phonebox.hp71.components.StatusPillView;
import net.oxog.phonebox.hp71.core.ApiClient;
import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.core.BitmapCache;
import net.oxog.phonebox.hp71.layouts.ResponsiveGridLayout;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.DrawableFactory;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Widget modular remodelado de Chamada Ativa & Controles de Voz do Discord:
 * - Ordena automaticamente quem está FALANDO no topo;
 * - Alterna para grade compacta de 2 colunas quando há muitos usuários (>4 na call);
 * - Botão "Só falando" para calls lotadas;
 * - Container interno com altura máxima limitada pela viewport real (descontando a barra fixa 48dp do Android 4.2.2)
 *   para nunca vazar para fora da tela;
 * - Dirty-check por assinatura (evita rebuild e perda de scroll a cada 1,5s).
 */
public final class DiscordCallWidget implements DashboardWidget {

    private final WidgetContext wctx;
    private final LinearLayout root;
    private final CardViewContainer topCard;
    private final StatusPillView statusPill;
    private final ActionButtonView refreshBtn;
    private final LinearLayout callBanner;
    private final TextView callTitle;
    private final TextView callSub;
    private final ResponsiveGridLayout ctrlGrid;
    private final ActionButtonView muteBtn;
    private final ActionButtonView deafBtn;
    private final ActionButtonView modeBtn;
    private final ActionButtonView leaveBtn;

    private final CardViewContainer membersCard;
    private final ActionButtonView onlySpeakingBtn;
    private final BoundedScrollView membersScroll;
    private final LinearLayout membersBox;

    private boolean onlySpeaking = false;
    private String lastMembersSig = "";
    private AppState lastState;

    public DiscordCallWidget(final WidgetContext wctx) {
        this.wctx = wctx;
        ThemeDimens d = AppTheme.get().dimens();
        root = new LinearLayout(wctx.context);
        root.setOrientation(LinearLayout.VERTICAL);

        topCard = new CardViewContainer(wctx.context).setHeader("DISCORD · CALL ATUAL");
        LinearLayout rightHead = new LinearLayout(wctx.context);
        rightHead.setOrientation(LinearLayout.HORIZONTAL);
        rightHead.setGravity(Gravity.CENTER_VERTICAL);
        statusPill = new StatusPillView(wctx.context).setPill("VERIFICANDO", StatusPillView.STYLE_NEUTRAL);
        statusPill.setMaxWidth(d.dp(150));
        LinearLayout.LayoutParams spLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        spLp.rightMargin = d.dp(5);
        rightHead.addView(statusPill, spLp);
        refreshBtn = new ActionButtonView(wctx.context).setCompact().setButton("↻", ActionButtonView.VARIANT_NORMAL);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { wctx.sync.requestImmediateSync(false); }
        });
        rightHead.addView(refreshBtn, new LinearLayout.LayoutParams(d.dp(34), d.dp(28)));
        topCard.setHeaderRight(rightHead);

        callBanner = new LinearLayout(wctx.context);
        callBanner.setOrientation(LinearLayout.VERTICAL);
        callBanner.setPadding(d.dp(10), d.dp(7), d.dp(10), d.dp(7));

        callTitle = new TextView(wctx.context);
        callTitle.setTypeface(Typeface.DEFAULT_BOLD);
        callTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        callTitle.setSingleLine(true);
        callTitle.setEllipsize(TextUtils.TruncateAt.END);
        callTitle.setText("Verificando conexão com o Discord Desktop...");
        callBanner.addView(callTitle);

        callSub = new TextView(wctx.context);
        callSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        callSub.setSingleLine(true);
        callSub.setEllipsize(TextUtils.TruncateAt.END);
        callSub.setText("Abra o Discord no PC para sincronizar.");
        callBanner.addView(callSub);
        topCard.addBodyView(callBanner);

        ctrlGrid = new ResponsiveGridLayout(wctx.context).setColumns(4, 2).setGapPx(d.dp(6));
        muteBtn = new ActionButtonView(wctx.context).setCompact().setButton("Mic", ActionButtonView.VARIANT_NORMAL);
        muteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { wctx.sync.postAction("/api/discord/mute", "Discord: Microfone alternado", "Falha no controle do Discord", true); }
        });
        deafBtn = new ActionButtonView(wctx.context).setCompact().setButton("Áudio", ActionButtonView.VARIANT_NORMAL);
        deafBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { wctx.sync.postAction("/api/discord/deafen", "Discord: Áudio alternado", "Falha no controle do Discord", true); }
        });
        modeBtn = new ActionButtonView(wctx.context).setCompact().setButton("Voz", ActionButtonView.VARIANT_NORMAL);
        modeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { wctx.sync.postAction("/api/discord/inputmode", "Discord: Modo alterado", "Falha no controle do Discord", true); }
        });
        leaveBtn = new ActionButtonView(wctx.context).setCompact().setButton("Sair", ActionButtonView.VARIANT_DANGER);
        leaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { wctx.sync.postAction("/api/discord/leave", "Desconectado da call", "Falha ao sair da call", true); }
        });

        List<View> btns = new ArrayList<View>(4);
        btns.add(muteBtn);
        btns.add(deafBtn);
        btns.add(modeBtn);
        btns.add(leaveBtn);
        ctrlGrid.setItems(btns);
        LinearLayout.LayoutParams cgLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cgLp.topMargin = d.dp(7);
        topCard.addBodyView(ctrlGrid, cgLp);

        LinearLayout.LayoutParams tcLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tcLp.bottomMargin = d.spaceSm;
        root.addView(topCard, tcLp);

        // Card Pessoas na Call com ScrollView interno limitado à altura disponível
        membersCard = new CardViewContainer(wctx.context).setHeader("PESSOAS NA CALL");
        onlySpeakingBtn = new ActionButtonView(wctx.context).setCompact().setButton("Só falando", ActionButtonView.VARIANT_NORMAL);
        onlySpeakingBtn.setVisibility(View.GONE);
        onlySpeakingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onlySpeaking = !onlySpeaking;
                onlySpeakingBtn.setButton(onlySpeaking ? "Ver todos" : "Só falando",
                        onlySpeaking ? ActionButtonView.VARIANT_ACTIVE : ActionButtonView.VARIANT_NORMAL);
                lastMembersSig = "";
                if (lastState != null) bindData(lastState);
            }
        });
        membersCard.setHeaderRight(onlySpeakingBtn);

        membersScroll = new BoundedScrollView(wctx.context);
        membersScroll.setMaxHeightPx(d.dp(240));
        membersBox = new LinearLayout(wctx.context);
        membersBox.setOrientation(LinearLayout.VERTICAL);
        membersScroll.addView(membersBox, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        membersCard.addBodyView(membersScroll);
        root.addView(membersCard);

        applyTheme();
    }

    public void updateMaxListHeight(int viewportHeightPx, boolean portrait) {
        ThemeDimens d = AppTheme.get().dimens();
        int maxH;
        if (portrait) {
            maxH = Math.max(d.dp(150), Math.min(d.dp(250), Math.round(viewportHeightPx * 0.28f)));
        } else {
            // Desconta topCard (~146dp) + cabeçalho do membersCard (~46dp) + margens
            maxH = Math.max(d.dp(135), viewportHeightPx - d.dp(208));
        }
        membersScroll.setMaxHeightPx(maxH);
    }

    @Override
    public View getView() { return root; }

    @Override
    public void bindData(AppState state) {
        this.lastState = state;
        ThemeColors c = AppTheme.get().colors();
        ThemeDimens d = AppTheme.get().dimens();
        AppState.DiscordState ds = state.discord;

        ctrlGrid.setVisibility(state.showDiscordControl ? View.VISIBLE : View.GONE);
        muteBtn.setButton(ds.muted ? "Mic: OFF" : "Mic: ON",
                ds.muted ? ActionButtonView.VARIANT_WARN : ActionButtonView.VARIANT_NORMAL);
        deafBtn.setButton(ds.deafened ? "Surdo" : "Áudio",
                ds.deafened ? ActionButtonView.VARIANT_WARN : ActionButtonView.VARIANT_NORMAL);
        boolean ptt = "PUSH_TO_TALK".equals(ds.inputMode);
        modeBtn.setButton(ptt ? "PTT" : "Voz",
                ptt ? ActionButtonView.VARIANT_ACTIVE : ActionButtonView.VARIANT_NORMAL);

        if (!ds.enabled) {
            statusPill.setPill("DESATIVADO", StatusPillView.STYLE_WARN);
            callBanner.setBackground(DrawableFactory.roundedRect(c.surfaceControl, c.border, d.strokePx, d.controlRadius));
            callTitle.setText("Integração do Discord desativada");
            callSub.setText("Ative em http://127.0.0.1:8765 (General > Discord).");
            onlySpeakingBtn.setVisibility(View.GONE);
            setMembersEmpty("Ative o Discord nas configurações do PC.", c);
            return;
        }
        if (!ds.connected) {
            statusPill.setPill("OFFLINE", StatusPillView.STYLE_WARN);
            callBanner.setBackground(DrawableFactory.roundedRect(c.surfaceControl, c.border, d.strokePx, d.controlRadius));
            callTitle.setText("Aguardando Discord Desktop no PC");
            callSub.setText(ds.error.length() > 0 ? ds.error : "Abra o Discord Desktop para conectar.");
            onlySpeakingBtn.setVisibility(View.GONE);
            setMembersEmpty(ds.error.length() > 0 ? ds.error : "Discord não está aberto no PC.", c);
            return;
        }

        statusPill.setPill(ds.user.length() > 0 ? ("@" + ds.user) : "ONLINE", StatusPillView.STYLE_SUCCESS);

        int speakingCount = 0;
        for (int i = 0; i < ds.members.size(); i++) {
            if (ds.members.get(i).speaking) speakingCount++;
        }

        boolean inCall = ds.channelId.length() > 0 || ds.channelName.length() > 0;
        if (inCall) {
            callBanner.setBackground(DrawableFactory.roundedRect(c.greenDim, c.green, d.strokePx, d.controlRadius));
            callTitle.setText((ds.channelName.length() > 0 ? ds.channelName : "Chamada ativa") + " · " + ds.members.size() + " na call");
            callSub.setText((speakingCount > 0 ? (speakingCount + " falando agora") : "Conectado na sala de voz")
                    + (ptt && "false".equals(ds.pttKey) ? " · Sem tecla PTT!" : ""));
        } else {
            callBanner.setBackground(DrawableFactory.roundedRect(c.surfaceControl, c.border, d.strokePx, d.controlRadius));
            callTitle.setText("Fora de canal de voz");
            callSub.setText("Selecione um servidor e toque em Entrar ao lado.");
        }

        membersCard.setHeaderMeta(speakingCount > 0 ? (speakingCount + " falando · " + ds.members.size()) : (ds.members.size() + " na call"));
        onlySpeakingBtn.setVisibility(ds.members.size() > 5 ? View.VISIBLE : View.GONE);
        if (ds.members.size() <= 5 && onlySpeaking) {
            onlySpeaking = false;
            onlySpeakingBtn.setButton("Só falando", ActionButtonView.VARIANT_NORMAL);
        }

        if (ds.members.isEmpty()) {
            setMembersEmpty("Você não está em nenhuma chamada de voz.", c);
            return;
        }

        // Ordena: FALANDO no topo -> ativos -> mutados -> surdos
        List<AppState.DiscordMember> sorted = new ArrayList<AppState.DiscordMember>(ds.members.size());
        for (int i = 0; i < ds.members.size(); i++) {
            AppState.DiscordMember m = ds.members.get(i);
            if (!onlySpeaking || m.speaking) sorted.add(m);
        }
        Collections.sort(sorted, new Comparator<AppState.DiscordMember>() {
            @Override
            public int compare(AppState.DiscordMember a, AppState.DiscordMember b) {
                int sa = a.speaking ? 0 : (!a.muted && !a.deafened ? 1 : (a.muted && !a.deafened ? 2 : 3));
                int sb = b.speaking ? 0 : (!b.muted && !b.deafened ? 1 : (b.muted && !b.deafened ? 2 : 3));
                if (sa != sb) return sa - sb;
                return a.name.compareToIgnoreCase(b.name);
            }
        });

        boolean compactGrid = sorted.size() > 4;
        StringBuilder sig = new StringBuilder();
        sig.append(onlySpeaking ? '1' : '0').append('|').append(compactGrid ? '2' : '1').append(':');
        for (int i = 0; i < sorted.size(); i++) {
            AppState.DiscordMember m = sorted.get(i);
            sig.append(m.id).append('|').append(m.name).append('|').append(m.avatar)
                    .append('|').append(m.speaking ? 'S' : '-').append(m.muted ? 'M' : '-').append(m.deafened ? 'D' : '-').append(';');
        }
        String nextSig = sig.toString();
        if (nextSig.equals(lastMembersSig)) return;
        lastMembersSig = nextSig;

        membersBox.removeAllViews();
        if (sorted.isEmpty()) {
            TextView empty = new TextView(wctx.context);
            empty.setText("Ninguém falando neste instante.");
            empty.setTextColor(c.textMuted);
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            membersBox.addView(empty);
            return;
        }

        String base = wctx.api.baseUrl();
        if (compactGrid) {
            // Grade compacta de 2 colunas para calls grandes (até 100+ usuários rolando suavemente)
            int cols = 2;
            for (int i = 0; i < sorted.size(); i += cols) {
                LinearLayout row = new LinearLayout(wctx.context);
                row.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                if (i > 0) rLp.topMargin = d.dp(5);
                for (int col = 0; col < cols; col++) {
                    int idx = i + col;
                    LinearLayout.LayoutParams cellLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                    if (col > 0) cellLp.leftMargin = d.dp(5);
                    if (idx < sorted.size()) {
                        row.addView(buildMemberTile(sorted.get(idx), true, base, c, d), cellLp);
                    } else {
                        row.addView(new View(wctx.context), cellLp);
                    }
                }
                membersBox.addView(row, rLp);
            }
        } else {
            for (int i = 0; i < sorted.size(); i++) {
                LinearLayout tile = buildMemberTile(sorted.get(i), false, base, c, d);
                LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                if (i > 0) tLp.topMargin = d.dp(5);
                membersBox.addView(tile, tLp);
            }
        }
    }

    private LinearLayout buildMemberTile(AppState.DiscordMember m, boolean compact, String base, ThemeColors c, ThemeDimens d) {
        LinearLayout row = new LinearLayout(wctx.context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(d.dp(compact ? 6 : 8), d.dp(compact ? 5 : 6), d.dp(compact ? 6 : 8), d.dp(compact ? 5 : 6));
        row.setBackground(DrawableFactory.roundedRect(
                m.speaking ? c.greenDim : c.bg, m.speaking ? c.green : c.border, d.strokePx, d.controlRadius));

        int avSz = d.dp(compact ? 28 : 34);
        final AvatarView av = new AvatarView(wctx.context);
        av.setInitials(m.name);
        av.setSpeaking(m.speaking);
        LinearLayout.LayoutParams avLp = new LinearLayout.LayoutParams(avSz, avSz);
        avLp.rightMargin = d.dp(compact ? 6 : 8);
        row.addView(av, avLp);

        if (m.avatar.length() > 0) {
            String avUrl = base + "/api/discord/img?kind=user&id=" + ApiClient.enc(m.id) + "&hash=" + ApiClient.enc(m.avatar);
            wctx.bitmaps.load("dc:" + m.id + ":" + m.avatar, avUrl, null, 96, new BitmapCache.Callback() {
                @Override
                public void onBitmapLoaded(String key, Bitmap bmp) { av.setBitmap(bmp); }
            });
        }

        LinearLayout info = new LinearLayout(wctx.context);
        info.setOrientation(LinearLayout.VERTICAL);

        TextView name = new TextView(wctx.context);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setTextColor(c.textPrimary);
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, compact ? 11.5f : 13f);
        name.setSingleLine(true);
        name.setEllipsize(TextUtils.TruncateAt.END);
        name.setText(m.name);
        info.addView(name);

        TextView st = new TextView(wctx.context);
        st.setTextSize(TypedValue.COMPLEX_UNIT_SP, compact ? 10f : 11f);
        st.setSingleLine(true);
        st.setEllipsize(TextUtils.TruncateAt.END);
        st.setTextColor(m.speaking ? c.green : (m.deafened || m.muted ? c.amber : c.textMuted));
        st.setText(m.speaking ? "Falando" : (m.deafened ? "Surdo" : (m.muted ? "Mutado" : "Na call")));
        info.addView(st);
        row.addView(info, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        if (!compact) {
            if (m.speaking) row.addView(new StatusPillView(wctx.context).setPill("FALANDO", StatusPillView.STYLE_SUCCESS));
            else if (m.deafened) row.addView(new StatusPillView(wctx.context).setPill("SURDO", StatusPillView.STYLE_DANGER));
            else if (m.muted) row.addView(new StatusPillView(wctx.context).setPill("MUDO", StatusPillView.STYLE_WARN));
        }
        return row;
    }

    private void setMembersEmpty(String msg, ThemeColors c) {
        String sig = "EMPTY:" + msg;
        if (sig.equals(lastMembersSig)) return;
        lastMembersSig = sig;
        membersBox.removeAllViews();
        TextView empty = new TextView(wctx.context);
        empty.setText(msg);
        empty.setTextColor(c.textMuted);
        empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        membersBox.addView(empty);
    }

    @Override
    public void onOrientationChanged(boolean portrait) {
        ctrlGrid.setPortrait(portrait);
        if (wctx.host != null) {
            updateMaxListHeight(wctx.host.getViewportHeightPx(), portrait);
        }
    }

    @Override
    public void setActive(boolean visible) { }

    @Override
    public void applyTheme() {
        ThemeColors c = AppTheme.get().colors();
        lastMembersSig = "";
        topCard.applyTheme();
        statusPill.applyTheme();
        refreshBtn.applyTheme();
        callTitle.setTextColor(c.textPrimary);
        callSub.setTextColor(c.textSecondary);
        muteBtn.applyTheme();
        deafBtn.applyTheme();
        modeBtn.applyTheme();
        leaveBtn.applyTheme();
        membersCard.applyTheme();
        onlySpeakingBtn.applyTheme();
    }

    /** ScrollView interno com altura máxima controlada (100% API 17) e proteção contra scroll pai. */
    static final class BoundedScrollView extends ScrollView {
        private int maxHeightPx;

        BoundedScrollView(android.content.Context ctx) {
            super(ctx);
            setVerticalScrollBarEnabled(true);
            setScrollbarFadingEnabled(false);
        }

        void setMaxHeightPx(int maxHeightPx) {
            if (this.maxHeightPx != maxHeightPx) {
                this.maxHeightPx = Math.max(120, maxHeightPx);
                requestLayout();
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            if (maxHeightPx > 0) {
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeightPx, MeasureSpec.AT_MOST);
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                ViewParent p = getParent();
                if (p != null) p.requestDisallowInterceptTouchEvent(true);
            }
            return super.onInterceptTouchEvent(ev);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                ViewParent p = getParent();
                if (p != null) p.requestDisallowInterceptTouchEvent(true);
            }
            return super.onTouchEvent(ev);
        }
    }
}
