package net.oxog.phonebox.hp71.widgets;

import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import net.oxog.phonebox.hp71.components.ActionButtonView;
import net.oxog.phonebox.hp71.components.CardViewContainer;
import net.oxog.phonebox.hp71.components.StatusPillView;
import net.oxog.phonebox.hp71.core.ApiClient;
import net.oxog.phonebox.hp71.core.AppState;
import net.oxog.phonebox.hp71.theme.AppTheme;
import net.oxog.phonebox.hp71.theme.DrawableFactory;
import net.oxog.phonebox.hp71.theme.ThemeColors;
import net.oxog.phonebox.hp71.theme.ThemeDimens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Widget modular remodelado para servidores gigantes do Discord (muitos servidores, canais e usuários):
 * - Sub-abas limpas no cabeçalho: [ Canais (N) ] | [ Notif. (M) ];
 * - Barra horizontal rolável de chips por Servidor (Todos + cada servidor com contagem);
 * - Filtro rápido "Com gente" vs "Todos" + busca instantânea por canal/servidor/usuário;
 * - Acordeões recolhíveis por servidor (ordena servidor da call atual e servidores ativos no topo);
 * - Sem corte arbitrário de 35 canais — alcança 100% de todos os canais com scroll interno limitado à tela real;
 * - Dirty-check por assinatura (nunca perde foco de busca nem posição de rolagem no polling de 1,5s).
 */
public final class DiscordVoiceDirectoryWidget implements DashboardWidget {

    private static final String GUILD_ALL = "__ALL__";

    private final WidgetContext wctx;
    private final CardViewContainer card;
    private final ActionButtonView tabChansBtn;
    private final ActionButtonView tabNotifsBtn;

    // Pane Canais
    private final LinearLayout chansPane;
    private final HorizontalScrollView guildStripScroll;
    private final LinearLayout guildStripRow;
    private final LinearLayout filterRow;
    private final EditText searchInput;
    private final ActionButtonView clearSearchBtn;
    private final ActionButtonView occupiedBtn;
    private final DiscordCallWidget.BoundedScrollView chansScroll;
    private final LinearLayout channelsBox;

    // Pane Notificações
    private final DiscordCallWidget.BoundedScrollView notifsScroll;
    private final LinearLayout notifsBox;

    private int activeSubTab = 0; // 0 = canais, 1 = notificações
    private String selectedGuild = GUILD_ALL;
    private boolean onlyOccupied = false;
    private String searchQuery = "";
    private final Set<String> expandedGuilds = new HashSet<String>();
    private final Set<String> collapsedGuilds = new HashSet<String>();

    private String lastGuildStripSig = "";
    private String lastChansSig = "";
    private String lastNotifsSig = "";
    private AppState lastState;

    private static final class GuildBucket {
        final String key;
        final String name;
        final List<AppState.DiscordChannel> channels = new ArrayList<AppState.DiscordChannel>();
        int totalPeople = 0;
        boolean hasCurrentCall = false;

        GuildBucket(String key, String name) {
            this.key = key;
            this.name = name;
        }
    }

    public DiscordVoiceDirectoryWidget(final WidgetContext wctx) {
        this.wctx = wctx;
        ThemeDimens d = AppTheme.get().dimens();

        card = new CardViewContainer(wctx.context).setHeader("SERVIDORES & VOZ");

        LinearLayout tabRow = new LinearLayout(wctx.context);
        tabRow.setOrientation(LinearLayout.HORIZONTAL);
        tabChansBtn = new ActionButtonView(wctx.context).setCompact().setButton("Canais", ActionButtonView.VARIANT_PRIMARY);
        tabChansBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { setSubTab(0); }
        });
        LinearLayout.LayoutParams tcLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, d.dp(28));
        tcLp.rightMargin = d.dp(5);
        tabRow.addView(tabChansBtn, tcLp);

        tabNotifsBtn = new ActionButtonView(wctx.context).setCompact().setButton("Notif.", ActionButtonView.VARIANT_NORMAL);
        tabNotifsBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { setSubTab(1); }
        });
        tabRow.addView(tabNotifsBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, d.dp(28)));
        card.setHeaderRight(tabRow);

        // === PANE 0: CANAIS ===
        chansPane = new LinearLayout(wctx.context);
        chansPane.setOrientation(LinearLayout.VERTICAL);

        // Chips horizontais de servidores
        guildStripScroll = new HorizontalScrollView(wctx.context);
        guildStripScroll.setHorizontalScrollBarEnabled(false);
        guildStripRow = new LinearLayout(wctx.context);
        guildStripRow.setOrientation(LinearLayout.HORIZONTAL);
        guildStripScroll.addView(guildStripRow);
        LinearLayout.LayoutParams gsLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        gsLp.bottomMargin = d.dp(6);
        chansPane.addView(guildStripScroll, gsLp);

        // Barra de busca + filtro "Com gente"
        filterRow = new LinearLayout(wctx.context);
        filterRow.setOrientation(LinearLayout.HORIZONTAL);
        filterRow.setGravity(Gravity.CENTER_VERTICAL);

        searchInput = new EditText(wctx.context);
        searchInput.setSingleLine(true);
        searchInput.setHint("Buscar canal, servidor ou pessoa...");
        searchInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        searchInput.setPadding(d.dp(9), d.dp(5), d.dp(9), d.dp(5));
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                String next = s == null ? "" : s.toString().trim().toLowerCase(Locale.ROOT);
                if (!next.equals(searchQuery)) {
                    searchQuery = next;
                    clearSearchBtn.setVisibility(searchQuery.length() > 0 ? View.VISIBLE : View.GONE);
                    lastChansSig = "";
                    if (lastState != null) renderChannels(lastState);
                }
            }
        });
        LinearLayout.LayoutParams siLp = new LinearLayout.LayoutParams(0, d.dp(32), 1f);
        siLp.rightMargin = d.dp(5);
        filterRow.addView(searchInput, siLp);

        clearSearchBtn = new ActionButtonView(wctx.context).setCompact().setButton("✕", ActionButtonView.VARIANT_NORMAL);
        clearSearchBtn.setVisibility(View.GONE);
        clearSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchInput.setText("");
            }
        });
        LinearLayout.LayoutParams csLp = new LinearLayout.LayoutParams(d.dp(30), d.dp(32));
        csLp.rightMargin = d.dp(5);
        filterRow.addView(clearSearchBtn, csLp);

        occupiedBtn = new ActionButtonView(wctx.context).setCompact().setButton("Com gente", ActionButtonView.VARIANT_NORMAL);
        occupiedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onlyOccupied = !onlyOccupied;
                occupiedBtn.setButton(onlyOccupied ? "Com gente ✓" : "Com gente",
                        onlyOccupied ? ActionButtonView.VARIANT_ACTIVE : ActionButtonView.VARIANT_NORMAL);
                lastChansSig = "";
                if (lastState != null) renderChannels(lastState);
            }
        });
        filterRow.addView(occupiedBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, d.dp(32)));

        LinearLayout.LayoutParams frLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        frLp.bottomMargin = d.dp(6);
        chansPane.addView(filterRow, frLp);

        // Lista rolável estritamente limitada à altura real da tela
        chansScroll = new DiscordCallWidget.BoundedScrollView(wctx.context);
        chansScroll.setMaxHeightPx(d.dp(310));
        channelsBox = new LinearLayout(wctx.context);
        channelsBox.setOrientation(LinearLayout.VERTICAL);
        chansScroll.addView(channelsBox, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        chansPane.addView(chansScroll);
        card.addBodyView(chansPane);

        // === PANE 1: NOTIFICAÇÕES ===
        notifsScroll = new DiscordCallWidget.BoundedScrollView(wctx.context);
        notifsScroll.setMaxHeightPx(d.dp(360));
        notifsBox = new LinearLayout(wctx.context);
        notifsBox.setOrientation(LinearLayout.VERTICAL);
        notifsScroll.addView(notifsBox, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        notifsScroll.setVisibility(View.GONE);
        card.addBodyView(notifsScroll);

        applyTheme();
    }

    public void updateMaxListHeight(int viewportHeightPx, boolean portrait) {
        ThemeDimens d = AppTheme.get().dimens();
        int chanMax;
        int notifMax;
        if (portrait) {
            chanMax = Math.max(d.dp(220), Math.round(viewportHeightPx * 0.44f));
            notifMax = Math.max(d.dp(240), Math.round(viewportHeightPx * 0.48f));
        } else {
            // Desconta cabeçalho do card (~42dp) + chips (~36dp) + busca (~38dp) + padding (~18dp)
            chanMax = Math.max(d.dp(170), viewportHeightPx - d.dp(136));
            notifMax = Math.max(d.dp(220), viewportHeightPx - d.dp(64));
        }
        chansScroll.setMaxHeightPx(chanMax);
        notifsScroll.setMaxHeightPx(notifMax);
    }

    private void setSubTab(int tab) {
        this.activeSubTab = tab;
        chansPane.setVisibility(tab == 0 ? View.VISIBLE : View.GONE);
        notifsScroll.setVisibility(tab == 1 ? View.VISIBLE : View.GONE);
        updateTabLabels();
    }

    private void updateTabLabels() {
        int chCount = lastState != null ? lastState.discord.channels.size() : 0;
        int nCount = lastState != null ? lastState.discord.notifications.size() : 0;
        tabChansBtn.setButton("Canais (" + chCount + ")",
                activeSubTab == 0 ? ActionButtonView.VARIANT_PRIMARY : ActionButtonView.VARIANT_NORMAL);
        tabNotifsBtn.setButton("Notif. (" + nCount + ")",
                activeSubTab == 1 ? ActionButtonView.VARIANT_PRIMARY : ActionButtonView.VARIANT_NORMAL);
    }

    @Override
    public View getView() { return card; }

    @Override
    public void bindData(AppState state) {
        this.lastState = state;
        boolean showAny = state.showDiscordChannels || state.showDiscordNotifs;
        card.setVisibility(showAny ? View.VISIBLE : View.GONE);
        if (!showAny) return;

        tabChansBtn.setVisibility(state.showDiscordChannels ? View.VISIBLE : View.GONE);
        tabNotifsBtn.setVisibility(state.showDiscordNotifs ? View.VISIBLE : View.GONE);
        if (!state.showDiscordChannels && activeSubTab == 0) setSubTab(1);
        else if (!state.showDiscordNotifs && activeSubTab == 1) setSubTab(0);
        else updateTabLabels();

        if (state.showDiscordChannels) renderChannels(state);
        if (state.showDiscordNotifs) renderNotifications(state);
    }

    private List<GuildBucket> groupByGuild(AppState.DiscordState ds) {
        Map<String, GuildBucket> map = new HashMap<String, GuildBucket>();
        List<GuildBucket> list = new ArrayList<GuildBucket>();
        for (int i = 0; i < ds.channels.size(); i++) {
            AppState.DiscordChannel ch = ds.channels.get(i);
            String gKey = (ch.guildId != null && ch.guildId.length() > 0) ? ch.guildId : ch.guild;
            GuildBucket b = map.get(gKey);
            if (b == null) {
                b = new GuildBucket(gKey, ch.guild.length() > 0 ? ch.guild : "Servidor");
                map.put(gKey, b);
                list.add(b);
            }
            b.channels.add(ch);
            b.totalPeople += ch.peopleCount;
            if (ds.channelId.length() > 0 && ds.channelId.equals(ch.id)) {
                b.hasCurrentCall = true;
            }
        }
        Collections.sort(list, new Comparator<GuildBucket>() {
            @Override
            public int compare(GuildBucket a, GuildBucket b) {
                if (a.hasCurrentCall != b.hasCurrentCall) return a.hasCurrentCall ? -1 : 1;
                if (a.totalPeople != b.totalPeople) return b.totalPeople - a.totalPeople;
                return a.name.compareToIgnoreCase(b.name);
            }
        });
        return list;
    }

    private void renderChannels(AppState state) {
        ThemeColors c = AppTheme.get().colors();
        ThemeDimens d = AppTheme.get().dimens();
        AppState.DiscordState ds = state.discord;

        List<GuildBucket> guilds = groupByGuild(ds);

        // Valida se o servidor selecionado ainda existe
        if (!GUILD_ALL.equals(selectedGuild)) {
            boolean found = false;
            for (int i = 0; i < guilds.size(); i++) {
                if (selectedGuild.equals(guilds.get(i).key)) { found = true; break; }
            }
            if (!found) selectedGuild = GUILD_ALL;
        }

        // 1. Atualiza chips horizontais de servidores apenas se assinatura mudar
        StringBuilder gSig = new StringBuilder(selectedGuild).append(':');
        for (int i = 0; i < guilds.size(); i++) {
            GuildBucket gb = guilds.get(i);
            gSig.append(gb.key).append('|').append(gb.name).append('|').append(gb.channels.size())
                    .append('|').append(gb.totalPeople).append('|').append(gb.hasCurrentCall ? '1' : '0').append(';');
        }
        String nextGSig = gSig.toString();
        if (!nextGSig.equals(lastGuildStripSig)) {
            lastGuildStripSig = nextGSig;
            guildStripRow.removeAllViews();
            guildStripScroll.setVisibility(guilds.size() > 1 ? View.VISIBLE : View.GONE);
            if (guilds.size() > 1) {
                addGuildChip("Todos (" + ds.channels.size() + ")", GUILD_ALL, d);
                for (int i = 0; i < guilds.size(); i++) {
                    GuildBucket gb = guilds.get(i);
                    String lbl = gb.name + (gb.totalPeople > 0 ? (" · " + gb.totalPeople) : (" (" + gb.channels.size() + ")"));
                    addGuildChip(lbl, gb.key, d);
                }
            }
        }

        // 2. Assinatura dos canais + filtros + acordeões
        StringBuilder cSig = new StringBuilder();
        cSig.append(selectedGuild).append('|').append(onlyOccupied ? '1' : '0').append('|')
                .append(searchQuery).append('|').append(ds.channelId).append('|')
                .append(expandedGuilds.toString()).append('|').append(collapsedGuilds.toString()).append(':');
        for (int i = 0; i < ds.channels.size(); i++) {
            AppState.DiscordChannel ch = ds.channels.get(i);
            cSig.append(ch.id).append('|').append(ch.name).append('|').append(ch.peopleCount).append('|').append(ch.peopleSummary).append(';');
        }
        String nextCSig = cSig.toString();
        if (nextCSig.equals(lastChansSig)) return;
        lastChansSig = nextCSig;

        channelsBox.removeAllViews();
        if (ds.channels.isEmpty()) {
            TextView empty = new TextView(wctx.context);
            empty.setText("Nenhum canal de voz listado.");
            empty.setTextColor(c.textMuted);
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            channelsBox.addView(empty);
            return;
        }

        boolean autoCollapseEmpty = GUILD_ALL.equals(selectedGuild) && searchQuery.length() == 0 && guilds.size() > 3;
        int matchedChannels = 0;

        for (int gIdx = 0; gIdx < guilds.size(); gIdx++) {
            final GuildBucket gb = guilds.get(gIdx);
            if (!GUILD_ALL.equals(selectedGuild) && !selectedGuild.equals(gb.key)) continue;

            List<AppState.DiscordChannel> filtered = new ArrayList<AppState.DiscordChannel>();
            boolean guildMatchesQuery = searchQuery.length() > 0 && gb.name.toLowerCase(Locale.ROOT).contains(searchQuery);
            for (int j = 0; j < gb.channels.size(); j++) {
                AppState.DiscordChannel ch = gb.channels.get(j);
                boolean isCur = ds.channelId.length() > 0 && ds.channelId.equals(ch.id);
                if (onlyOccupied && ch.peopleCount == 0 && !isCur) continue;
                if (searchQuery.length() > 0 && !guildMatchesQuery) {
                    boolean hit = ch.name.toLowerCase(Locale.ROOT).contains(searchQuery)
                            || ch.peopleSummary.toLowerCase(Locale.ROOT).contains(searchQuery);
                    if (!hit) continue;
                }
                filtered.add(ch);
            }
            if (filtered.isEmpty()) continue;

            // Ordena canais dentro do servidor: call atual > com gente > ordem original
            Collections.sort(filtered, new Comparator<AppState.DiscordChannel>() {
                @Override
                public int compare(AppState.DiscordChannel a, AppState.DiscordChannel b) {
                    boolean ca = ds.channelId.length() > 0 && ds.channelId.equals(a.id);
                    boolean cb = ds.channelId.length() > 0 && ds.channelId.equals(b.id);
                    if (ca != cb) return ca ? -1 : 1;
                    if (a.peopleCount != b.peopleCount) return b.peopleCount - a.peopleCount;
                    return 0;
                }
            });

            matchedChannels += filtered.size();

            boolean expanded;
            if (searchQuery.length() > 0 || !GUILD_ALL.equals(selectedGuild)) {
                expanded = !collapsedGuilds.contains(gb.key);
            } else if (expandedGuilds.contains(gb.key)) {
                expanded = true;
            } else if (collapsedGuilds.contains(gb.key)) {
                expanded = false;
            } else {
                expanded = !autoCollapseEmpty || gb.hasCurrentCall || gb.totalPeople > 0 || gIdx < 2;
            }

            // Cabeçalho acordeão clicável do servidor
            LinearLayout gHead = new LinearLayout(wctx.context);
            gHead.setOrientation(LinearLayout.HORIZONTAL);
            gHead.setGravity(Gravity.CENTER_VERTICAL);
            gHead.setPadding(d.dp(8), d.dp(6), d.dp(8), d.dp(6));
            gHead.setBackground(DrawableFactory.roundedRect(c.cardAltBg, c.border, d.strokePx, d.dp(6)));
            LinearLayout.LayoutParams ghLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (channelsBox.getChildCount() > 0) ghLp.topMargin = d.dp(6);

            TextView gTitle = new TextView(wctx.context);
            gTitle.setTypeface(Typeface.DEFAULT_BOLD);
            gTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
            gTitle.setTextColor(gb.hasCurrentCall ? c.green : c.accent);
            gTitle.setSingleLine(true);
            gTitle.setEllipsize(TextUtils.TruncateAt.END);
            gTitle.setText((expanded ? "▼ " : "▶ ") + gb.name.toUpperCase(Locale.ROOT));
            gHead.addView(gTitle, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            String meta = filtered.size() + (filtered.size() == 1 ? " canal" : " canais")
                    + (gb.totalPeople > 0 ? (" · " + gb.totalPeople + " online") : "");
            StatusPillView gPill = new StatusPillView(wctx.context).setPill(meta,
                    gb.totalPeople > 0 ? StatusPillView.STYLE_SUCCESS : StatusPillView.STYLE_NEUTRAL);
            gHead.addView(gPill);

            final boolean currentlyExpanded = expanded;
            gHead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentlyExpanded) {
                        expandedGuilds.remove(gb.key);
                        collapsedGuilds.add(gb.key);
                    } else {
                        collapsedGuilds.remove(gb.key);
                        expandedGuilds.add(gb.key);
                    }
                    lastChansSig = "";
                    if (lastState != null) renderChannels(lastState);
                }
            });
            channelsBox.addView(gHead, ghLp);

            if (!expanded) continue;

            for (int j = 0; j < filtered.size(); j++) {
                final AppState.DiscordChannel ch = filtered.get(j);
                boolean isCur = ds.channelId.length() > 0 && ds.channelId.equals(ch.id);

                LinearLayout row = new LinearLayout(wctx.context);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(d.dp(6), d.dp(5), d.dp(4), d.dp(5));
                if (isCur) {
                    row.setBackground(DrawableFactory.roundedRect(c.greenDim, c.green, d.strokePx, d.dp(6)));
                }

                LinearLayout info = new LinearLayout(wctx.context);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView cname = new TextView(wctx.context);
                cname.setTypeface(Typeface.DEFAULT_BOLD);
                cname.setTextColor(isCur ? c.green : c.textPrimary);
                cname.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
                cname.setSingleLine(true);
                cname.setEllipsize(TextUtils.TruncateAt.END);
                cname.setText("# " + ch.name + (ch.peopleCount > 0 ? (" (" + ch.peopleCount + ")") : ""));
                info.addView(cname);

                if (ch.peopleSummary.length() > 0) {
                    TextView ppl = new TextView(wctx.context);
                    ppl.setTextColor(c.accent);
                    ppl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
                    ppl.setSingleLine(true);
                    ppl.setEllipsize(TextUtils.TruncateAt.END);
                    ppl.setText("Na sala: " + ch.peopleSummary);
                    info.addView(ppl);
                }
                LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                infoLp.rightMargin = d.dp(6);
                row.addView(info, infoLp);

                ActionButtonView joinBtn = new ActionButtonView(wctx.context).setCompact();
                joinBtn.setButton(isCur ? "Atual" : "Entrar",
                        isCur ? ActionButtonView.VARIANT_PRIMARY : ActionButtonView.VARIANT_NORMAL);
                joinBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        wctx.sync.postAction("/api/discord/join?channel=" + ApiClient.enc(ch.id),
                                "Entrando em " + ch.name, "Falha ao entrar no canal", true);
                    }
                });
                row.addView(joinBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, d.dp(30)));

                LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                rLp.topMargin = d.dp(3);
                channelsBox.addView(row, rLp);
            }
        }

        if (matchedChannels == 0) {
            TextView empty = new TextView(wctx.context);
            empty.setText(onlyOccupied ? "Nenhum canal com pessoas no filtro atual." : "Nenhum canal encontrado para esta busca.");
            empty.setTextColor(c.textMuted);
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            empty.setPadding(d.dp(4), d.dp(8), d.dp(4), d.dp(8));
            channelsBox.addView(empty);
        }
    }

    private void addGuildChip(String label, final String guildKey, ThemeDimens d) {
        boolean sel = guildKey.equals(selectedGuild);
        ActionButtonView chip = new ActionButtonView(wctx.context).setCompact()
                .setButton(label, sel ? ActionButtonView.VARIANT_PRIMARY : ActionButtonView.VARIANT_NORMAL);
        chip.setMaxWidth(d.dp(170));
        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedGuild = guildKey;
                collapsedGuilds.remove(guildKey);
                lastGuildStripSig = "";
                lastChansSig = "";
                if (lastState != null) renderChannels(lastState);
            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, d.dp(28));
        if (guildStripRow.getChildCount() > 0) lp.leftMargin = d.dp(5);
        guildStripRow.addView(chip, lp);
    }

    private void renderNotifications(AppState state) {
        ThemeColors c = AppTheme.get().colors();
        ThemeDimens d = AppTheme.get().dimens();
        AppState.DiscordState ds = state.discord;

        StringBuilder nSig = new StringBuilder();
        for (int i = 0; i < ds.notifications.size(); i++) {
            AppState.DiscordNotif n = ds.notifications.get(i);
            nSig.append(n.ts).append('|').append(n.title).append('|').append(n.body).append(';');
        }
        String nextSig = nSig.toString();
        if (nextSig.equals(lastNotifsSig) && notifsBox.getChildCount() > 0) return;
        lastNotifsSig = nextSig;

        notifsBox.removeAllViews();
        if (ds.notifications.isEmpty()) {
            TextView empty = new TextView(wctx.context);
            empty.setText("Nenhuma notificação recente do Discord.");
            empty.setTextColor(c.textMuted);
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            notifsBox.addView(empty);
            return;
        }
        for (int i = ds.notifications.size() - 1; i >= 0 && i >= ds.notifications.size() - 25; i--) {
            AppState.DiscordNotif n = ds.notifications.get(i);
            LinearLayout row = new LinearLayout(wctx.context);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(d.dp(8), d.dp(6), d.dp(8), d.dp(6));
            row.setBackground(DrawableFactory.roundedRect(c.surfaceControl, c.border, d.strokePx, d.dp(6)));

            LinearLayout head = new LinearLayout(wctx.context);
            head.setOrientation(LinearLayout.HORIZONTAL);
            TextView t = new TextView(wctx.context);
            t.setTypeface(Typeface.DEFAULT_BOLD);
            t.setTextColor(c.textPrimary);
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
            t.setSingleLine(true);
            t.setEllipsize(TextUtils.TruncateAt.END);
            t.setText(n.title);
            head.addView(t, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView tm = new TextView(wctx.context);
            tm.setTextColor(c.textMuted);
            tm.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
            tm.setText(FormatUtils.fmtTimeOnly(n.ts));
            head.addView(tm);
            row.addView(head);

            TextView body = new TextView(wctx.context);
            body.setTextColor(c.textSecondary);
            body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
            body.setText(n.body);
            row.addView(body);

            LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (notifsBox.getChildCount() > 0) rLp.topMargin = d.dp(5);
            notifsBox.addView(row, rLp);
        }
    }

    @Override
    public void onOrientationChanged(boolean portrait) {
        if (wctx.host != null) {
            updateMaxListHeight(wctx.host.getViewportHeightPx(), portrait);
        }
    }

    @Override
    public void setActive(boolean visible) { }

    @Override
    public void applyTheme() {
        ThemeColors c = AppTheme.get().colors();
        ThemeDimens d = AppTheme.get().dimens();
        lastGuildStripSig = "";
        lastChansSig = "";
        lastNotifsSig = "";
        card.applyTheme();
        tabChansBtn.applyTheme();
        tabNotifsBtn.applyTheme();
        occupiedBtn.applyTheme();
        clearSearchBtn.applyTheme();
        searchInput.setTextColor(c.textPrimary);
        searchInput.setHintTextColor(c.textMuted);
        searchInput.setBackground(DrawableFactory.roundedRect(c.surfaceControl, c.border, d.strokePx, d.controlRadius));
    }
}
