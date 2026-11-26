package net.oxog.phonebox.hp71.theme.hack;

import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.TransformationMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import net.oxog.phonebox.hp71.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Motor global de tipografia Cyberdeck + Glitch de texto legível ("h4ck3r") para o tema Hack.
 *
 * Como funciona sem nunca quebrar dados nem layouts:
 * - Usa TransformationMethod encapsulando o TransformationMethod original (ex.: SingleLineTransformationMethod),
 *   de modo que TextView.getText() SEMPRE retorna o texto original limpo!
 * - Nunca toca em EditText (digitação intacta) nem corrompe unidades numéricas (°C, GB, ms, W, %).
 * - Em ondas orgânicas (e sincronizado com surtos de glitch da tela), transforma 2 a 5 textos visíveis em:
 *     Fase 1 (100ms): micro-bug de símbolos (@, $, #, |, +) + flash ciano;
 *     Fase 2 (950-1550ms): l33t h4ck3r legível (A->4, E->3, I->1, O->0, S->5, T->7, B->8, G->6);
 *     Fase 3 (90ms): micro-bug de saída -> volta perfeitamente ao texto original limpo!
 * - Ao sair do tema Hack, restaura Typeface, sombra e TransformationMethod originais de toda a árvore.
 */
public final class HackTextGlitchManager {

    private static final int MODE_LEET = 1;
    private static final int MODE_SURGE = 2;

    private static final int PHASE_SURGE_IN = 0;
    private static final int PHASE_LEET = 1;
    private static final int PHASE_SURGE_OUT = 2;

    private static final int SHADOW_NONE = 0;
    private static final int SHADOW_SMALL = 1;
    private static final int SHADOW_LARGE = 2;
    private static final int SHADOW_CYAN = 3;

    private static final Typeface MONO_NORMAL = Typeface.MONOSPACE;
    private static final Typeface MONO_BOLD = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<ActiveGlitch> active = new ArrayList<ActiveGlitch>();
    private final List<TextView> scratchCandidates = new ArrayList<TextView>();

    private View rootView;
    private boolean hackActive = false;
    private boolean effectsEnabled = false;
    private int rng = 0x4834434B; // "H4CK"
    private long nextWaveAtMs = 0L;

    private static final class ActiveGlitch {
        final WeakReference<TextView> ref;
        int phase;
        long nextStepMs;
        int seed;

        ActiveGlitch(TextView tv, int seed, long nextStepMs) {
            this.ref = new WeakReference<TextView>(tv);
            this.phase = PHASE_SURGE_IN;
            this.seed = seed;
            this.nextStepMs = nextStepMs;
        }
    }

    private final Runnable loop = new Runnable() {
        @Override
        public void run() {
            if (!hackActive || !effectsEnabled || rootView == null) return;
            long now = System.currentTimeMillis();

            // 1. Advance active glitches
            for (int i = active.size() - 1; i >= 0; i--) {
                ActiveGlitch ag = active.get(i);
                TextView tv = ag.ref.get();
                if (tv == null || !tv.isShown()) {
                    if (tv != null) restoreTransformOnly(tv);
                    active.remove(i);
                    continue;
                }
                if (now < ag.nextStepMs) continue;

                if (ag.phase == PHASE_SURGE_IN) {
                    ag.phase = PHASE_LEET;
                    ag.nextStepMs = now + 950L + (Math.abs(nextRand()) % 650);
                    applyGlitchTransform(tv, MODE_LEET, ag.seed);
                } else if (ag.phase == PHASE_LEET) {
                    ag.phase = PHASE_SURGE_OUT;
                    ag.nextStepMs = now + 95L;
                    applyGlitchTransform(tv, MODE_SURGE, ag.seed + 7);
                } else {
                    restoreTransformOnly(tv);
                    active.remove(i);
                }
            }

            // 2. Spawn next organic rolling wave every 1.7s - 3.1s
            if (now >= nextWaveAtMs) {
                int count = 2 + (Math.abs(nextRand()) % 3); // 2 to 4 labels per wave
                spawnGlitchWave(count, now);
                nextWaveAtMs = now + 1700L + (Math.abs(nextRand()) % 1450);
            }

            long delay = active.isEmpty() ? 220L : 85L;
            handler.postDelayed(this, delay);
        }
    };

    public void attachRoot(View root) {
        this.rootView = root;
    }

    /**
     * Aplica ou restaura a identidade tipográfica Monospace + brilho fosforescente na árvore inteira.
     */
    public void syncTreeStyle(boolean hackMode, boolean animateEffects) {
        boolean leavingHack = this.hackActive && !hackMode;
        this.hackActive = hackMode;
        this.effectsEnabled = hackMode && animateEffects;

        handler.removeCallbacks(loop);
        clearAllActiveGlitches();

        if (rootView != null) {
            if (hackMode) {
                applyHackTypographyRecursive(rootView);
            } else if (leavingHack) {
                restoreTypographyRecursive(rootView);
            }
        }

        if (this.effectsEnabled && rootView != null) {
            nextWaveAtMs = System.currentTimeMillis() + 650L;
            handler.postDelayed(loop, 250L);
        }
    }

    /**
     * Reaplica tipografia Monospace em views recém-criadas dinamicamente (ex.: linhas de processos/Discord/IA).
     */
    public void refreshDynamicTypography() {
        if (hackActive && rootView != null) {
            applyHackTypographyRecursive(rootView);
        }
    }

    /**
     * Pausa animações de texto (onPause ou checkbox desmarcado), devolvendo textos ao estado limpo
     * mas preservando o tema visual Monospace se o tema Hack continuar ativo.
     */
    public void setRunning(boolean running) {
        this.effectsEnabled = hackActive && running;
        handler.removeCallbacks(loop);
        clearAllActiveGlitches();
        if (this.effectsEnabled && rootView != null) {
            nextWaveAtMs = System.currentTimeMillis() + 800L;
            handler.postDelayed(loop, 200L);
        }
    }

    /**
     * Dispara uma onda sincronizada de textos "h4ck3r" (chamado em surtos de glitch visual ou troca de aba).
     */
    public void triggerSyncWave(int count) {
        if (!hackActive || !effectsEnabled || rootView == null) return;
        long now = System.currentTimeMillis();
        spawnGlitchWave(count, now);
        handler.removeCallbacks(loop);
        handler.postDelayed(loop, 85L);
    }

    private void spawnGlitchWave(int desiredCount, long now) {
        if (rootView == null) return;
        scratchCandidates.clear();
        collectVisibleCandidates(rootView, scratchCandidates);
        int avail = scratchCandidates.size();
        if (avail == 0) return;

        int spawned = 0;
        int attempts = Math.min(avail * 2, desiredCount * 4);
        for (int a = 0; a < attempts && spawned < desiredCount && active.size() < 7; a++) {
            TextView tv = scratchCandidates.get(Math.abs(nextRand()) % avail);
            if (Boolean.TRUE.equals(tv.getTag(R.id.tag_hack_glitch_active))) continue;

            int seed = nextRand() & 0x7FFF;
            applyGlitchTransform(tv, MODE_SURGE, seed);
            active.add(new ActiveGlitch(tv, seed, now + 105L));
            spawned++;
        }
        scratchCandidates.clear();
    }

    private void clearAllActiveGlitches() {
        for (int i = 0; i < active.size(); i++) {
            TextView tv = active.get(i).ref.get();
            if (tv != null) restoreTransformOnly(tv);
        }
        active.clear();
    }

    private void collectVisibleCandidates(View v, List<TextView> out) {
        if (v == null || v.getVisibility() != View.VISIBLE) return;
        if (v instanceof TextView && !(v instanceof EditText)) {
            TextView tv = (TextView) v;
            if (tv.isShown() && isEligibleText(tv.getText())) {
                out.add(tv);
            }
            return;
        }
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            int n = vg.getChildCount();
            for (int i = 0; i < n; i++) {
                collectVisibleCandidates(vg.getChildAt(i), out);
            }
        }
    }

    private static boolean isEligibleText(CharSequence cs) {
        if (cs == null) return false;
        int len = cs.length();
        if (len < 3 || len > 56) return false;
        int mutatableLetters = 0;
        for (int i = 0; i < len; i++) {
            char ch = cs.charAt(i);
            if (isLeetCandidate(ch)) {
                mutatableLetters++;
                if (mutatableLetters >= 2) return true;
            }
        }
        return false;
    }

    private void applyGlitchTransform(TextView tv, int mode, int seed) {
        if (tv.getTag(R.id.tag_hack_orig_transform) == null) {
            TransformationMethod cur = tv.getTransformationMethod();
            tv.setTag(R.id.tag_hack_orig_transform, cur == null ? NULL_SENTINEL : cur);
        }
        Object origObj = tv.getTag(R.id.tag_hack_orig_transform);
        TransformationMethod orig = (origObj instanceof TransformationMethod) ? (TransformationMethod) origObj : null;

        tv.setTag(R.id.tag_hack_glitch_active, Boolean.TRUE);
        tv.setTransformationMethod(new HackGlitchTransform(orig, mode, seed));

        // Chromatic cyan text shimmer during brief surge frames
        if (mode == MODE_SURGE) {
            setShadowState(tv, SHADOW_CYAN);
        } else {
            applyBaseHackShadow(tv);
        }
    }

    private void restoreTransformOnly(TextView tv) {
        tv.setTag(R.id.tag_hack_glitch_active, Boolean.FALSE);
        Object origObj = tv.getTag(R.id.tag_hack_orig_transform);
        if (origObj != null) {
            TransformationMethod orig = (origObj instanceof TransformationMethod) ? (TransformationMethod) origObj : null;
            tv.setTransformationMethod(orig);
        }
        if (hackActive) {
            applyBaseHackShadow(tv);
        } else {
            setShadowState(tv, SHADOW_NONE);
        }
    }

    private static void applyBaseHackShadow(TextView tv) {
        float px = tv.getTextSize();
        float dens = tv.getResources().getDisplayMetrics().density;
        if (dens <= 0f) dens = 1f;
        int target = px >= 20f * dens ? SHADOW_LARGE : (px >= 13f * dens ? SHADOW_SMALL : SHADOW_NONE);
        setShadowState(tv, target);
    }

    private static void setShadowState(TextView tv, int targetState) {
        Object cur = tv.getTag(R.id.tag_hack_orig_shadow);
        if (cur instanceof Integer && ((Integer) cur).intValue() == targetState) return;
        tv.setTag(R.id.tag_hack_orig_shadow, Integer.valueOf(targetState));
        if (targetState == SHADOW_CYAN) {
            tv.setShadowLayer(4f, 1.5f, 0f, 0xCC00F0FF);
        } else if (targetState == SHADOW_LARGE) {
            tv.setShadowLayer(6f, 0f, 0f, 0xAA00FF66);
        } else if (targetState == SHADOW_SMALL) {
            tv.setShadowLayer(2.5f, 0f, 0f, 0x5500FF66);
        } else {
            tv.setShadowLayer(0f, 0f, 0f, 0);
        }
    }

    private void applyHackTypographyRecursive(View v) {
        if (v instanceof TextView) {
            TextView tv = (TextView) v;
            if (tv.getTag(R.id.tag_hack_orig_typeface) == null) {
                Typeface cur = tv.getTypeface();
                tv.setTag(R.id.tag_hack_orig_typeface, cur == null ? Typeface.DEFAULT : cur);
            }
            Typeface cur = tv.getTypeface();
            float dens = tv.getResources().getDisplayMetrics().density;
            if (dens <= 0f) dens = 1f;
            boolean forceBoldDisplay = tv.getTextSize() >= 30f * dens;
            int style = cur != null ? cur.getStyle() : Typeface.NORMAL;
            Typeface mono = (forceBoldDisplay || (style & Typeface.BOLD) != 0) ? MONO_BOLD : MONO_NORMAL;
            if (cur != mono) {
                tv.setTypeface(mono);
            }
            if (!Boolean.TRUE.equals(tv.getTag(R.id.tag_hack_glitch_active))) {
                applyBaseHackShadow(tv);
            }
        }
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            int n = vg.getChildCount();
            for (int i = 0; i < n; i++) {
                applyHackTypographyRecursive(vg.getChildAt(i));
            }
        }
    }

    private void restoreTypographyRecursive(View v) {
        if (v instanceof TextView) {
            TextView tv = (TextView) v;
            Object tfObj = tv.getTag(R.id.tag_hack_orig_typeface);
            if (tfObj instanceof Typeface) {
                tv.setTypeface((Typeface) tfObj);
                tv.setTag(R.id.tag_hack_orig_typeface, null);
            }
            Object origTr = tv.getTag(R.id.tag_hack_orig_transform);
            if (origTr != null) {
                TransformationMethod orig = (origTr instanceof TransformationMethod) ? (TransformationMethod) origTr : null;
                tv.setTransformationMethod(orig);
                tv.setTag(R.id.tag_hack_orig_transform, null);
            }
            tv.setTag(R.id.tag_hack_glitch_active, Boolean.FALSE);
            setShadowState(tv, SHADOW_NONE);
            tv.setTag(R.id.tag_hack_orig_shadow, null);
        }
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            int n = vg.getChildCount();
            for (int i = 0; i < n; i++) {
                restoreTypographyRecursive(vg.getChildAt(i));
            }
        }
    }

    private int nextRand() {
        int x = rng;
        x ^= (x << 13);
        x ^= (x >>> 17);
        x ^= (x << 5);
        rng = x;
        return x;
    }

    private static final Object NULL_SENTINEL = new Object();

    private static boolean isLeetCandidate(char ch) {
        switch (ch) {
            case 'A': case 'a': case 'Á': case 'á': case 'À': case 'à': case 'Ã': case 'ã': case 'Â': case 'â':
            case 'E': case 'e': case 'É': case 'é': case 'Ê': case 'ê':
            case 'I': case 'i': case 'Í': case 'í':
            case 'O': case 'o': case 'Ó': case 'ó': case 'Õ': case 'õ': case 'Ô': case 'ô':
            case 'S': case 's':
            case 'T': case 't':
            case 'B': case 'b':
            case 'G': case 'g':
            case 'Z': case 'z':
                return true;
            default:
                return false;
        }
    }

    private static boolean isUnitOrNumericContext(CharSequence s, int i) {
        int len = s.length();
        // Avoid mutating short measurement units right after numbers (e.g. "12 GB", "40 ms", "25°C", "65 W")
        if (i > 0) {
            char prev = s.charAt(i - 1);
            if (prev == '°' || (prev >= '0' && prev <= '9')) return true;
            if (prev == ' ' && i >= 2) {
                char prev2 = s.charAt(i - 2);
                if (prev2 >= '0' && prev2 <= '9') {
                    // Check if current word is a 1-3 char unit token (GB, MB, ms, km, W)
                    int wEnd = i;
                    while (wEnd < len && Character.isLetter(s.charAt(wEnd))) wEnd++;
                    if (wEnd - i <= 2) return true;
                }
            }
        }
        return false;
    }

    private static char mapLeetChar(char ch) {
        switch (ch) {
            case 'A': case 'a': case 'Á': case 'á': case 'À': case 'à': case 'Ã': case 'ã': case 'Â': case 'â':
                return '4';
            case 'E': case 'e': case 'É': case 'é': case 'Ê': case 'ê':
                return '3';
            case 'I': case 'i': case 'Í': case 'í':
                return '1';
            case 'O': case 'o': case 'Ó': case 'ó': case 'Õ': case 'õ': case 'Ô': case 'ô':
                return '0';
            case 'S': case 's':
                return '5';
            case 'T': case 't':
                return '7';
            case 'B': case 'b':
                return '8';
            case 'G': case 'g':
                return '6';
            case 'Z': case 'z':
                return '2';
            default:
                return ch;
        }
    }

    private static char mapSurgeSymbol(char ch, int hash) {
        switch (ch) {
            case 'A': case 'a': case 'Á': case 'á': case 'Ã': case 'ã': case 'Â': case 'â':
                return (hash & 1) == 0 ? '@' : '4';
            case 'S': case 's':
                return (hash & 1) == 0 ? '$' : '5';
            case 'E': case 'e': case 'É': case 'é': case 'Ê': case 'ê':
                return (hash % 3 == 0) ? '#' : '3';
            case 'I': case 'i': case 'Í': case 'í':
                return (hash & 1) == 0 ? '!' : '|';
            case 'T': case 't':
                return (hash & 1) == 0 ? '+' : '7';
            case 'O': case 'o': case 'Ó': case 'ó': case 'Õ': case 'õ': case 'Ô': case 'ô':
                return '0';
            default:
                return mapLeetChar(ch);
        }
    }

    /**
     * TransformationMethod que preserva getText() original, SingleLine e Spans coloridos.
     */
    private static final class HackGlitchTransform implements TransformationMethod {
        private final TransformationMethod wrapped;
        private final int mode;
        private final int seed;

        HackGlitchTransform(TransformationMethod wrapped, int mode, int seed) {
            this.wrapped = wrapped;
            this.mode = mode;
            this.seed = seed;
        }

        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            CharSequence base = wrapped != null ? wrapped.getTransformation(source, view) : source;
            if (base == null || base.length() == 0) return base;

            int len = base.length();
            char[] out = new char[len];
            int changed = 0;
            for (int i = 0; i < len; i++) {
                char ch = base.charAt(i);
                char rep = ch;
                if (isLeetCandidate(ch) && !isUnitOrNumericContext(base, i)) {
                    int h = Math.abs((seed * 31 + i * 17) ^ (ch * 13));
                    if (mode == MODE_SURGE) {
                        if (h % 10 < 8) rep = mapSurgeSymbol(ch, h);
                    } else {
                        // Readable h4ck3r: mutate ~70% of eligible vowels/digits so word stays immediately legible
                        if (h % 10 < 7) rep = mapLeetChar(ch);
                    }
                }
                if (rep != ch) changed++;
                out[i] = rep;
            }

            // Guarantee at least 1-2 legible substitutions if any candidate existed
            if (changed == 0) {
                for (int i = 0; i < len && changed < 2; i++) {
                    char ch = base.charAt(i);
                    if (isLeetCandidate(ch) && !isUnitOrNumericContext(base, i)) {
                        out[i] = (mode == MODE_SURGE) ? mapSurgeSymbol(ch, seed + i) : mapLeetChar(ch);
                        changed++;
                    }
                }
            }

            String mutated = new String(out);
            if (base instanceof Spanned) {
                Spanned sp = (Spanned) base;
                SpannableString ss = new SpannableString(mutated);
                Object[] spans = sp.getSpans(0, len, Object.class);
                for (int i = 0; i < spans.length; i++) {
                    int st = sp.getSpanStart(spans[i]);
                    int en = sp.getSpanEnd(spans[i]);
                    int fl = sp.getSpanFlags(spans[i]);
                    if (st >= 0 && en <= len && st <= en) {
                        ss.setSpan(spans[i], st, en, fl);
                    }
                }
                return ss;
            }
            return mutated;
        }

        @Override
        public void onFocusChanged(View view, CharSequence sourceText, boolean focused,
                                   int direction, Rect previouslyFocusedRect) {
            if (wrapped != null) {
                wrapped.onFocusChanged(view, sourceText, focused, direction, previouslyFocusedRect);
            }
        }
    }
}
