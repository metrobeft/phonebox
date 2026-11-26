package net.oxog.phonebox.hp71.core;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

/**
 * Atualizador nativo do APK direto pelo servidor local PhoneBoxAgent (100% compatível Android 4.2.2 / API 17).
 * - Compara versionCode e SHA-256 real do APK instalado com o APK hospedado no PC;
 * - Faz download em segundo plano com barra de progresso e validação de integridade SHA-256;
 * - Abre diretamente o instalador nativo do Android (ACTION_VIEW) para o usuário confirmar com 1 toque
 *   (com fallback seguro para o navegador se o armazenamento externo não estiver disponível).
 */
public final class ApkUpdater {

    public interface CheckCallback {
        void onResult(AppState.ApkReleaseInfo remote, boolean updateAvailable, String error);
    }

    private static final String UPDATE_FILENAME = "PhoneBox-HP71-update.apk";

    private final Context appCtx;
    private final ApiClient api;
    private final Handler ui = new Handler(Looper.getMainLooper());

    private final int localVersionCode;
    private final String localVersionName;
    private volatile String localApkSha256 = "";
    private volatile boolean downloading = false;
    private volatile boolean cancelRequested = false;

    public ApkUpdater(Context context, ApiClient api) {
        this.appCtx = context.getApplicationContext();
        this.api = api;

        int vc = 0;
        String vn = "0.0.0";
        try {
            PackageInfo pi = appCtx.getPackageManager().getPackageInfo(appCtx.getPackageName(), 0);
            vc = pi.versionCode;
            vn = pi.versionName != null ? pi.versionName : "0.0.0";
        } catch (Exception ignored) { }
        this.localVersionCode = vc;
        this.localVersionName = vn;

        // Calcula o SHA-256 real do APK instalado e limpa arquivos temporários antigos em background
        new Thread(new Runnable() {
            @Override
            public void run() {
                localApkSha256 = computeInstalledApkSha256();
                cleanupOldUpdateFiles();
            }
        }, "PhoneBoxApkHash").start();
    }

    public int getLocalVersionCode() {
        return localVersionCode;
    }

    public String getLocalVersionName() {
        return localVersionName;
    }

    public String formatLocalVersion() {
        return "v" + localVersionName + " (build " + localVersionCode + ")";
    }

    public static String formatRemoteVersion(AppState.ApkReleaseInfo r) {
        if (r == null) return "--";
        String vn = (r.versionName != null && r.versionName.length() > 0) ? r.versionName : "?";
        return "v" + vn + (r.versionCode > 0 ? (" (build " + r.versionCode + ")") : "");
    }

    /**
     * Verdadeiro quando o servidor possui uma versão mais nova (versionCode maior)
     * ou uma recompilação diferente do mesmo versionCode (SHA-256 distinto).
     */
    public boolean isUpdateAvailable(AppState.ApkReleaseInfo remote) {
        if (remote == null || !remote.available) return false;
        if (remote.versionCode > 0) {
            if (remote.versionCode > localVersionCode) return true;
            if (remote.versionCode < localVersionCode) return false;
        }
        String localHash = localApkSha256;
        if (localHash != null && localHash.length() == 64
                && remote.sha256 != null && remote.sha256.length() == 64) {
            return !localHash.equalsIgnoreCase(remote.sha256);
        }
        return false;
    }

    public void checkNow(final CheckCallback cb) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final AppState.ApkReleaseInfo remote = api.fetchApkInfo();
                    final boolean newer = isUpdateAvailable(remote);
                    ui.post(new Runnable() {
                        @Override
                        public void run() {
                            if (cb != null) cb.onResult(remote, newer, null);
                        }
                    });
                } catch (final Exception e) {
                    ui.post(new Runnable() {
                        @Override
                        public void run() {
                            if (cb != null) cb.onResult(null, false, e.getMessage() != null ? e.getMessage() : "Sem conexão com o PC");
                        }
                    });
                }
            }
        }, "PhoneBoxUpdateCheck").start();
    }

    public void showUpdateDialog(final Activity activity, final AppState.ApkReleaseInfo remote, boolean forceOption) {
        if (activity == null || activity.isFinishing() || remote == null || !remote.available) return;
        boolean newer = isUpdateAvailable(remote);
        String sizeTxt = remote.sizeBytes > 0 ? (Math.round(remote.sizeBytes / 102.4d) / 10.0d + " KB") : "desconhecido";
        String title = newer ? "Atualização do APK disponível" : "Aplicativo atualizado";
        String msg;
        if (newer) {
            msg = "Versão instalada: " + formatLocalVersion()
                    + "\nNova versão no PC: " + formatRemoteVersion(remote)
                    + "\nTamanho: " + sizeTxt
                    + "\n\nO app vai baixar o novo APK do servidor local e abrir a tela de instalação do Android para você confirmar.";
        } else {
            msg = "Você já está na versão mais recente!\n\nInstalada: " + formatLocalVersion()
                    + "\nServidor: " + formatRemoteVersion(remote)
                    + "\nTamanho: " + sizeTxt;
        }

        AlertDialog.Builder b = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(msg);
        if (newer || forceOption) {
            b.setPositiveButton(newer ? "Baixar e instalar" : "Reinstalar mesmo assim", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    downloadAndInstall(activity, remote);
                }
            });
            b.setNegativeButton(newer ? "Depois" : "Fechar", null);
        } else {
            b.setPositiveButton("Fechar", null);
        }
        b.show();
    }

    public void downloadAndInstall(final Activity activity, final AppState.ApkReleaseInfo remote) {
        if (activity == null || activity.isFinishing() || remote == null) return;
        if (downloading) {
            Toast.makeText(activity, "Download de atualização já em andamento...", Toast.LENGTH_SHORT).show();
            return;
        }
        downloading = true;
        cancelRequested = false;

        final ProgressDialog prog = new ProgressDialog(activity);
        prog.setTitle("Baixando atualização");
        prog.setMessage("Conectando ao PC para baixar " + formatRemoteVersion(remote) + "...");
        prog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        prog.setIndeterminate(true);
        prog.setMax(100);
        prog.setCancelable(false);
        prog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelRequested = true;
            }
        });
        prog.show();

        final String urlPath = (remote.url != null && remote.url.length() > 0) ? remote.url : "/legacy/PhoneBox-HP71.apk";
        final String fullUrl = api.baseUrl() + urlPath + (urlPath.indexOf('?') >= 0 ? "&" : "?") + "_t=" + System.currentTimeMillis();

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                InputStream in = null;
                FileOutputStream out = null;
                File targetFile = null;
                try {
                    URL url = new URL(fullUrl);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(20000);
                    conn.setUseCaches(false);
                    conn.setRequestProperty("X-PhoneBox-Device", ApiClient.HEADER_DEVICE);
                    conn.setRequestProperty("X-PhoneBox-Model", ApiClient.HEADER_MODEL);
                    conn.setRequestProperty("X-PhoneBox-App", ApiClient.HEADER_APP);

                    int code = conn.getResponseCode();
                    if (code < 200 || code >= 300) {
                        throw new Exception("Servidor respondeu HTTP " + code);
                    }

                    final long totalLen = conn.getContentLength() > 0 ? conn.getContentLength() : remote.sizeBytes;
                    targetFile = resolveWritableApkFile();
                    if (targetFile == null) {
                        throw new Exception("Sem permissão de escrita no armazenamento do tablet");
                    }

                    in = conn.getInputStream();
                    out = openOutputForFile(targetFile);
                    MessageDigest sha = MessageDigest.getInstance("SHA-256");

                    byte[] buf = new byte[8192];
                    long downloaded = 0L;
                    int firstByte0 = -1, firstByte1 = -1;
                    int lastPct = -1;
                    int n;
                    while ((n = in.read(buf)) > 0) {
                        if (cancelRequested) {
                            throw new CancelledException();
                        }
                        if (downloaded == 0L && n >= 2) {
                            firstByte0 = buf[0] & 0xFF;
                            firstByte1 = buf[1] & 0xFF;
                        }
                        out.write(buf, 0, n);
                        sha.update(buf, 0, n);
                        downloaded += n;

                        if (totalLen > 0L) {
                            final int pct = (int) Math.min(100L, (downloaded * 100L) / totalLen);
                            if (pct != lastPct) {
                                lastPct = pct;
                                final long dlKb = downloaded / 1024L;
                                final long totKb = totalLen / 1024L;
                                ui.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (prog.isShowing()) {
                                            prog.setIndeterminate(false);
                                            prog.setProgress(pct);
                                            prog.setMessage("Baixando " + formatRemoteVersion(remote) + " (" + dlKb + " / " + totKb + " KB)...");
                                        }
                                    }
                                });
                            }
                        }
                    }
                    out.flush();
                    out.close();
                    out = null;

                    if (downloaded < 4096L || firstByte0 != 0x50 || firstByte1 != 0x4B) {
                        throw new Exception("Arquivo baixado não é um APK válido");
                    }
                    String actualSha = bytesToHex(sha.digest());
                    if (remote.sha256 != null && remote.sha256.length() == 64
                            && !actualSha.equalsIgnoreCase(remote.sha256)) {
                        throw new Exception("Falha na verificação SHA-256 do APK baixado");
                    }

                    try { targetFile.setReadable(true, false); } catch (Exception ignored) { }

                    final File readyFile = targetFile;
                    ui.post(new Runnable() {
                        @Override
                        public void run() {
                            downloading = false;
                            dismissSafe(prog);
                            launchInstaller(activity, readyFile, fullUrl);
                        }
                    });
                } catch (CancelledException ce) {
                    deleteQuietly(targetFile);
                    ui.post(new Runnable() {
                        @Override
                        public void run() {
                            downloading = false;
                            dismissSafe(prog);
                            Toast.makeText(activity, "Download cancelado.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (final Exception e) {
                    deleteQuietly(targetFile);
                    ui.post(new Runnable() {
                        @Override
                        public void run() {
                            downloading = false;
                            dismissSafe(prog);
                            offerBrowserFallback(activity, fullUrl, e.getMessage());
                        }
                    });
                } finally {
                    if (in != null) { try { in.close(); } catch (Exception ignored) { } }
                    if (out != null) { try { out.close(); } catch (Exception ignored) { } }
                    if (conn != null) conn.disconnect();
                }
            }
        }, "PhoneBoxApkDownload").start();
    }

    private void launchInstaller(Activity activity, File apkFile, String fallbackUrl) {
        if (activity == null || activity.isFinishing()) return;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            offerBrowserFallback(activity, fallbackUrl, "Não foi possível abrir o instalador direto (" + e.getMessage() + ")");
        }
    }

    private void offerBrowserFallback(final Activity activity, final String downloadUrl, String reason) {
        if (activity == null || activity.isFinishing()) return;
        String detail = (reason != null && reason.length() > 0) ? reason : "Erro ao baixar APK";
        new AlertDialog.Builder(activity)
                .setTitle("Falha ao instalar direto")
                .setMessage(detail + "\n\nDeseja abrir o link no navegador do tablet para baixar e instalar manualmente?")
                .setPositiveButton("Abrir no navegador", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                            activity.startActivity(i);
                        } catch (Exception ex) {
                            Toast.makeText(activity, "Erro ao abrir navegador.", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private File resolveWritableApkFile() {
        // 1. Pasta pública Download (ideal para PackageInstaller do Android 4.2.2)
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                File dlDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (dlDir != null && (dlDir.exists() || dlDir.mkdirs()) && dlDir.canWrite()) {
                    return new File(dlDir, UPDATE_FILENAME);
                }
            }
        } catch (Exception ignored) { }

        // 2. Cache externo do app
        try {
            File extCache = appCtx.getExternalCacheDir();
            if (extCache != null && (extCache.exists() || extCache.mkdirs()) && extCache.canWrite()) {
                return new File(extCache, UPDATE_FILENAME);
            }
        } catch (Exception ignored) { }

        // 3. Fallback interno com permissão de leitura para PackageInstaller (API 17)
        try {
            return appCtx.getFileStreamPath(UPDATE_FILENAME);
        } catch (Exception ignored) { }
        return null;
    }

    @SuppressWarnings("deprecation")
    private FileOutputStream openOutputForFile(File f) throws Exception {
        File internal = appCtx.getFileStreamPath(UPDATE_FILENAME);
        if (internal != null && internal.getAbsolutePath().equals(f.getAbsolutePath())) {
            return appCtx.openFileOutput(UPDATE_FILENAME, Context.MODE_WORLD_READABLE);
        }
        return new FileOutputStream(f);
    }

    private void cleanupOldUpdateFiles() {
        try {
            File dlDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (dlDir != null) deleteQuietly(new File(dlDir, UPDATE_FILENAME));
        } catch (Exception ignored) { }
        try {
            File extCache = appCtx.getExternalCacheDir();
            if (extCache != null) deleteQuietly(new File(extCache, UPDATE_FILENAME));
        } catch (Exception ignored) { }
        try {
            deleteQuietly(appCtx.getFileStreamPath(UPDATE_FILENAME));
        } catch (Exception ignored) { }
    }

    private String computeInstalledApkSha256() {
        FileInputStream fis = null;
        try {
            ApplicationInfo ai = appCtx.getApplicationInfo();
            if (ai == null || ai.sourceDir == null) return "";
            File apk = new File(ai.sourceDir);
            if (!apk.exists() || !apk.canRead()) return "";
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            fis = new FileInputStream(apk);
            byte[] buf = new byte[8192];
            int n;
            while ((n = fis.read(buf)) > 0) {
                md.update(buf, 0, n);
            }
            return bytesToHex(md.digest());
        } catch (Exception e) {
            return "";
        } finally {
            if (fis != null) { try { fis.close(); } catch (Exception ignored) { } }
        }
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hex = "0123456789ABCDEF".toCharArray();
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            out[i * 2] = hex[v >>> 4];
            out[i * 2 + 1] = hex[v & 0x0F];
        }
        return new String(out);
    }

    private static void dismissSafe(ProgressDialog d) {
        try { if (d != null && d.isShowing()) d.dismiss(); } catch (Exception ignored) { }
    }

    private static void deleteQuietly(File f) {
        try { if (f != null && f.exists()) f.delete(); } catch (Exception ignored) { }
    }

    private static final class CancelledException extends Exception { }
}
