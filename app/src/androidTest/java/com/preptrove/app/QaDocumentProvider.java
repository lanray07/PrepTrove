package com.preptrove.app;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Binder;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/** Test APK only; Java keeps this separate provider process independent of the app's Kotlin runtime. */
public final class QaDocumentProvider extends ContentProvider {
    @Override public boolean onCreate() { return true; }

    private void checkCaller() {
        String testPackage = getContext().getPackageName();
        if (!testPackage.endsWith(".test")) throw new SecurityException("Test package required");
        String target = testPackage.substring(0, testPackage.length() - 5);
        try {
            int targetUid = getContext().getPackageManager().getApplicationInfo(target, 0).uid;
            int caller = Binder.getCallingUid();
            if (caller != targetUid && caller != android.os.Process.myUid())
                throw new SecurityException("Test provider caller rejected");
        } catch (PackageManager.NameNotFoundException error) {
            throw new SecurityException(error);
        }
    }

    private File file(Uri uri) {
        checkCaller();
        String id = uri.getLastPathSegment();
        if (id == null || !id.matches("[0-9a-f-]{36}")) throw new IllegalArgumentException("Invalid fixture ID");
        try {
            File root = new File(getContext().getCacheDir(), "qa-documents").getCanonicalFile();
            if (!root.isDirectory() && !root.mkdirs()) throw new IllegalStateException("Fixture directory unavailable");
            File result = new File(root, id).getCanonicalFile();
            if (!root.equals(result.getParentFile())) throw new SecurityException("Fixture path escaped");
            return result;
        } catch (IOException error) { throw new IllegalArgumentException(error); }
    }

    @Override public Uri insert(Uri uri, ContentValues values) {
        checkCaller();
        if (uri.getPath() != null && !uri.getPath().isEmpty() && !uri.getPath().equals("/"))
            throw new IllegalArgumentException("Fixture root required");
        Uri result = Uri.withAppendedPath(uri, UUID.randomUUID().toString());
        try {
            if (!file(result).createNewFile()) throw new IllegalStateException("Fixture already exists");
        } catch (IOException error) { throw new IllegalArgumentException(error); }
        return result;
    }

    @Override public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        return ParcelFileDescriptor.open(file(uri), ParcelFileDescriptor.parseMode(mode));
    }
    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] args, String order) {
        File document = file(uri);
        MatrixCursor cursor = new MatrixCursor(new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE});
        cursor.addRow(new Object[]{document.getName(), document.length()});
        return cursor;
    }
    @Override public String getType(Uri uri) { checkCaller(); return "application/octet-stream"; }
    @Override public int delete(Uri uri, String selection, String[] args) { return file(uri).delete() ? 1 : 0; }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] args) {
        throw new UnsupportedOperationException();
    }
}
